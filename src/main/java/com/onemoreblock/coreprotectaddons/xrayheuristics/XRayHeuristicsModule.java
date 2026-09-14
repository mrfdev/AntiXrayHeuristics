package com.onemoreblock.coreprotectaddons.xrayheuristics;

import com.onemoreblock.coreprotectaddons.xrayheuristics.api.DefaultXRayHeuristicsApi;
import com.onemoreblock.coreprotectaddons.xrayheuristics.api.XRayHeuristicsApi;
import com.onemoreblock.coreprotectaddons.xrayheuristics.command.XrayerCommand;
import com.onemoreblock.coreprotectaddons.xrayheuristics.command.XrayerTabCompleter;
import com.onemoreblock.coreprotectaddons.xrayheuristics.event.MiningBlockBreakListener;
import com.onemoreblock.coreprotectaddons.xrayheuristics.event.ExplosiveBlockPlaceListener;
import com.onemoreblock.coreprotectaddons.xrayheuristics.event.HandledPlayerVaultClickListener;
import com.onemoreblock.coreprotectaddons.xrayheuristics.event.HandledPlayerVaultCloseListener;
import com.onemoreblock.coreprotectaddons.xrayheuristics.event.HandledPlayerVaultDragListener;
import com.onemoreblock.coreprotectaddons.xrayheuristics.event.PlayerWorldChangeListener;
import com.onemoreblock.coreprotectaddons.xrayheuristics.integration.CoreProtectHook;
import com.onemoreblock.coreprotectaddons.xrayheuristics.manager.LocaleManager;
import com.onemoreblock.coreprotectaddons.xrayheuristics.manager.HandledPlayerStore;
import com.onemoreblock.coreprotectaddons.xrayheuristics.util.BlockWeightInfo;
import com.onemoreblock.coreprotectaddons.xrayheuristics.util.BuildMetadata;
import com.onemoreblock.coreprotectaddons.xrayheuristics.util.CommentedConfigFile;
import com.onemoreblock.coreprotectaddons.xrayheuristics.util.MiningSession;
import com.onemoreblock.coreprotectaddons.xrayheuristics.util.WeightsCard;
import com.onemoreblock.coreprotectaddons.xrayheuristics.util.YamlFiles;
import com.onemoreblock.coreprotectaddons.xrayheuristics.handling.HandledPlayerService;
import com.onemoreblock.coreprotectaddons.xrayheuristics.handling.HandledPlayerVault;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.EnumSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.PluginCommand;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.Server;
import org.bukkit.util.BlockVector;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Runtime for the X-ray Heuristics feature.
 *
 * <p>The runtime deliberately does not extend {@link JavaPlugin}. The current standalone plugin
 * supplies its plugin instance and legacy data directory, while the future CoreProtect Add-ons
 * plugin can supply its shared plugin instance and a feature-specific directory.</p>
 */
public final class XRayHeuristicsModule {
    public static final String FEATURE_ID = "xrayheuristics";
    public static final String FEATURE_NAME = "1MB XRayHeuristics";
    public static final String FEATURE_DESCRIPTION =
            "Heuristic Anti-XRay feature for CoreProtect that evaluates suspicious mining patterns.";
    public static final String STANDALONE_DATA_DIRECTORY_NAME = "1MB-XRayHeuristics";
    private static final String DEFAULT_CONFIG_RESOURCE_PATH = "xrayheuristics/config.yml";
    private static final String LEGACY_BLOCK_WEIGHT_ALIAS = "com.greymagic27.util.BlockWeightInfo";
    private static final EnumSet<Material> BASE_TRACKING_BLOCKS = EnumSet.of(
            Material.STONE,
            Material.DEEPSLATE,
            Material.TUFF,
            Material.NETHERRACK,
            Material.BASALT
    );
    private final JavaPlugin hostPlugin;
    private final File pluginDataDirectory;
    private final List<Listener> registeredListeners = new ArrayList<>();
    public final float maxSuspicionDecreaseProportion = -10.0F;
    public final float minSuspicionDecreaseProportion = -0.1F;
    public final float absoluteMinimumSuspicionDecrease = -3.0F;
    public final int maxAccountableMillisecondDeltaForThirtyMinedBlocks = 20000;
    public final int minAccountableMillisecondDeltaForThirtyMinedBlocks = 0;
    public final HashMap<String, MiningSession> sessions = new HashMap<>();
    public final HandledPlayerStore handledPlayerStore;
    private final HandledPlayerService handledPlayerService;
    public HandledPlayerVault handledPlayerVault;
    private XRayHeuristicsApi api;
    private BuildMetadata buildMetadata;
    private final CoreProtectHook coreProtectHook;
    private File configFile;
    private YamlConfiguration configuration;
    private int nonOreStreakDecreaseAmount;
    private int usualEncounterThreshold;
    private float suspicionThreshold;
    private boolean verboseMiningSessionDebug;
    private boolean enabled;
    private @Nullable BukkitTask sessionDecayTask;

    public XRayHeuristicsModule(@NonNull JavaPlugin hostPlugin, @NonNull File pluginDataDirectory) {
        this(hostPlugin, pluginDataDirectory, new CoreProtectHook(hostPlugin));
    }

    public XRayHeuristicsModule(
            @NonNull JavaPlugin hostPlugin,
            @NonNull File pluginDataDirectory,
            @NonNull CoreProtectHook coreProtectHook
    ) {
        this.hostPlugin = Objects.requireNonNull(hostPlugin, "hostPlugin");
        this.pluginDataDirectory = Objects.requireNonNull(pluginDataDirectory, "pluginDataDirectory");
        this.coreProtectHook = Objects.requireNonNull(coreProtectHook, "coreProtectHook");
        this.handledPlayerStore = new HandledPlayerStore(this);
        this.handledPlayerService = new HandledPlayerService(this);
    }

    public @NonNull JavaPlugin getHostPlugin() {
        return hostPlugin;
    }

    public @NonNull Server getServer() {
        return hostPlugin.getServer();
    }

    public @NonNull Logger getLogger() {
        return hostPlugin.getLogger();
    }

    public @NonNull String getFeatureId() {
        return FEATURE_ID;
    }

    public @NonNull String getFeatureName() {
        return FEATURE_NAME;
    }

    public @NonNull String getFeatureDescription() {
        return FEATURE_DESCRIPTION;
    }

    public @NonNull XRayHeuristicsApi getApi() {
        return Objects.requireNonNull(api, "X-ray Heuristics API has not been initialized yet.");
    }

    public @NonNull HandledPlayerService getHandledPlayerService() {
        return handledPlayerService;
    }

    public @NonNull CoreProtectHook getCoreProtectHook() {
        return coreProtectHook;
    }

    public @NonNull BuildMetadata getBuildMetadata() {
        return Objects.requireNonNull(buildMetadata, "Build metadata has not been initialized yet.");
    }

    public float getSuspicionThreshold() {
        return suspicionThreshold;
    }

    public @NonNull File getPluginDataDirectory() {
        return pluginDataDirectory;
    }

    public @NonNull File getPluginDataFile(@NonNull String fileName) {
        return new File(getPluginDataDirectory(), fileName);
    }

    public boolean isVerboseMiningSessionDebug() {
        return verboseMiningSessionDebug;
    }

    public boolean shouldCleansePlayerItems() {
        return getBooleanCompat("CleansePlayerItems", "ClensePlayerItems");
    }

    public boolean shouldNullifySuspicionAfterPunish() {
        return getBooleanCompat("NullifySuspicionAfterPunish", "NullifySuspicionAferPunish");
    }

    public int getMinimumBlocksMinedToNextVein() {
        return Math.max(0, getConfig().getInt("MinimumBlocksMinedToNextVein", 10));
    }

    public @NonNull String getStorageType() {
        String configuredValue = getStringCompat("StorageType", "StorageMethod");
        if (configuredValue == null || configuredValue.isBlank()) {
            return "JSON";
        }
        return configuredValue.trim().toUpperCase(Locale.ROOT);
    }

    public FileConfiguration getConfig() {
        if (configuration == null) {
            reloadConfig();
        }
        return configuration;
    }

    public void reloadConfig() {
        ensurePluginDataDirectory();
        if (configFile == null) {
            configFile = getPluginDataFile("config.yml");
        }
        try {
            configuration = CommentedConfigFile.loadAndSync(hostPlugin, configFile, DEFAULT_CONFIG_RESOURCE_PATH);
            saveConfig();
        } catch (IOException | InvalidConfigurationException e) {
            configuration = new YamlConfiguration();
            getLogger().log(Level.SEVERE, "Could not load config.yml.", e);
        }
    }

    public void saveConfig() {
        if (configuration == null || configFile == null) {
            return;
        }
        try {
            CommentedConfigFile.saveAndSync(hostPlugin, configuration, configFile, DEFAULT_CONFIG_RESOURCE_PATH);
        } catch (IOException | InvalidConfigurationException e) {
            getLogger().log(Level.SEVERE, "Could not save config.yml.", e);
        }
    }

    public void saveDefaultConfig() {
        ensurePluginDataDirectory();
        if (configFile == null) {
            configFile = getPluginDataFile("config.yml");
        }
        if (!configFile.exists()) {
            reloadConfig();
        }
    }

    public void saveResource(@NonNull String resourcePath, boolean replace) {
        if (resourcePath.isEmpty()) {
            throw new IllegalArgumentException("ResourcePath cannot be empty.");
        }

        String normalizedResourcePath = resourcePath.replace('\\', '/');
        String embeddedResourcePath = normalizedResourcePath.equals("config.yml")
                ? DEFAULT_CONFIG_RESOURCE_PATH
                : normalizedResourcePath;
        try (InputStream resource = hostPlugin.getResource(embeddedResourcePath)) {
            if (resource == null) {
                throw new IllegalArgumentException("The embedded resource '" + embeddedResourcePath + "' cannot be found.");
            }

            File outFile = getPluginDataFile(normalizedResourcePath);
            File outDirectory = outFile.getParentFile();
            if (outDirectory != null) {
                Files.createDirectories(outDirectory.toPath());
            }
            if (outFile.exists() && !replace) {
                return;
            }

            if (replace) {
                Files.copy(resource, outFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } else {
                Files.copy(resource, outFile.toPath());
            }
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Could not save resource '" + resourcePath + "'.", e);
        }
    }

    /**
     * Enables the feature inside its host plugin.
     *
     * @return {@code true} when the feature is ready, or {@code false} when its required
     *         CoreProtect integration is unavailable
     */
    public boolean enable() {
        if (enabled) {
            return true;
        }

        this.buildMetadata = BuildMetadata.load(hostPlugin);
        this.api = new DefaultXRayHeuristicsApi(this);

        ensurePluginDataDirectory();
        this.configFile = getPluginDataFile("config.yml");
        reloadConfig();

        if (!refreshCoreProtectHook()) {
            getLogger().severe("CoreProtect API 11 or newer is required. Tested with CoreProtect "
                    + getBuildMetadata().coreProtectTarget() + ".");
            return false;
        }

        ConfigurationSerialization.registerClass(BlockWeightInfo.class, BlockWeightInfo.SERIALIZATION_ALIAS);
        ConfigurationSerialization.registerClass(BlockWeightInfo.class, LEGACY_BLOCK_WEIGHT_ALIAS);
        LocaleManager.setup(getPluginDataDirectory());
        LocaleManager.get().options().copyDefaults(true);
        LocaleManager.save();
        WeightsCard.setup(getPluginDataDirectory());
        WeightsCard.get().options().copyDefaults(true);
        WeightsCard.save();

        applyRuntimeConfig();
        this.handledPlayerVault = new HandledPlayerVault(this);

        initializeStorage();
        registerEvents();
        mainRunnable();
        enabled = true;

        Bukkit.getConsoleSender().sendMessage(
                LegacyComponentSerializer.legacyAmpersand().deserialize(
                        "&5[&b1MB Heuristics&5] &aEnabled " + getBuildMetadata().artifactVersion()
                                + " [Paper API " + getBuildMetadata().paperApi()
                                + ", Java target " + getBuildMetadata().javaTarget()
                                + "] with " + getCoreProtectHook().getSummaryLine()
                )
        );
        return true;
    }

    public void registerStandaloneCommand(@NonNull PluginCommand command) {
        command.setExecutor(new XrayerCommand(this));
        command.setTabCompleter(new XrayerTabCompleter(this));
    }

    public void disable() {
        if (!enabled) {
            return;
        }
        if (sessionDecayTask != null) {
            sessionDecayTask.cancel();
            sessionDecayTask = null;
        }
        registeredListeners.forEach(HandlerList::unregisterAll);
        registeredListeners.clear();
        if (Objects.equals(getStorageType(), "MYSQL")) {
            this.handledPlayerStore.CloseDataSource();
        }
        ConfigurationSerialization.unregisterClass(BlockWeightInfo.SERIALIZATION_ALIAS);
        ConfigurationSerialization.unregisterClass(LEGACY_BLOCK_WEIGHT_ALIAS);
        enabled = false;
    }

    public void reloadPluginState() {
        reloadConfig();
        LocaleManager.reload();
        WeightsCard.reload();
        applyRuntimeConfig();
        refreshCoreProtectHook();
    }

    public boolean refreshCoreProtectHook() {
        return getCoreProtectHook().refresh();
    }

    private void ensurePluginDataDirectory() {
        File dataDirectory = getPluginDataDirectory();
        if (!dataDirectory.exists() && !dataDirectory.mkdirs()) {
            getLogger().warning("Could not create plugin data directory: " + dataDirectory.getAbsolutePath());
        }
    }

    private void initializeStorage() {
        if (Objects.equals(getStorageType(), "MYSQL")) {
            this.handledPlayerStore.InitializeDataSource();
            Bukkit.getScheduler().runTaskAsynchronously(hostPlugin, this.handledPlayerStore::SQLCreateTableIfNotExists);
        } else if (Objects.equals(getStorageType(), "JSON")) {
            this.handledPlayerStore.JSONFileCreateIfNotExists();
        }
    }

    private void registerEvents() {
        registeredListeners.add(new MiningBlockBreakListener(this));
        registeredListeners.add(new ExplosiveBlockPlaceListener(this));
        registeredListeners.add(new HandledPlayerVaultClickListener(this));
        registeredListeners.add(new HandledPlayerVaultDragListener());
        registeredListeners.add(new HandledPlayerVaultCloseListener(this));
        registeredListeners.add(new PlayerWorldChangeListener(this));
        registeredListeners.forEach(listener -> getServer().getPluginManager().registerEvents(listener, hostPlugin));
    }

    private void applyRuntimeConfig() {
        int minimumBlocksToNextVein = getMinimumBlocksMinedToNextVein();
        this.nonOreStreakDecreaseAmount = -((int) Math.ceil(Math.max(1, minimumBlocksToNextVein) / 4.0D));
        this.usualEncounterThreshold = Math.max(1, minimumBlocksToNextVein) * 4;
        this.suspicionThreshold = (float) Math.max(0.01D, getConfig().getDouble("SuspicionThreshold", 100.0D));
        this.verboseMiningSessionDebug = getConfig().getBoolean("DebugVerboseMiningSession", false);
    }

    private boolean getBooleanCompat(@NonNull String preferred, @Nullable String legacy) {
        if (getConfig().contains(preferred)) {
            return getConfig().getBoolean(preferred);
        }
        return legacy != null && getConfig().getBoolean(legacy);
    }

    private @Nullable String getStringCompat(@NonNull String preferred, @Nullable String legacy) {
        if (getConfig().contains(preferred)) {
            return getConfig().getString(preferred);
        }
        return legacy != null ? getConfig().getString(legacy) : null;
    }

    private void mainRunnable() {
        sessionDecayTask = (new BukkitRunnable() {
            @Override
            public void run() {
                Iterator<Map.Entry<String, MiningSession>> iterator = XRayHeuristicsModule.this.sessions.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, MiningSession> entry = iterator.next();
                    MiningSession session = entry.getValue();
                    session.SelfSuspicionReducer();
                    session.minedNonOreBlocksStreak += XRayHeuristicsModule.this.nonOreStreakDecreaseAmount;
                    if (session.GetSuspicionLevel() < 0.0F) {
                        session.SetSuspicionLevel(0.0F);
                        session.foundAtZeroSuspicionStreak++;
                        if (session.foundAtZeroSuspicionStreak >= 20) {
                            iterator.remove();
                            continue;
                        }
                    } else {
                        session.foundAtZeroSuspicionStreak = 0;
                    }
                    if (session.minedNonOreBlocksStreak < 0) {
                        session.minedNonOreBlocksStreak = 0;
                    }
                }
            }
        }).runTaskTimer(hostPlugin, 200L, 200L);
    }

    private void updateTrail(@NonNull BlockBreakEvent ev, @NonNull MiningSession session) {
        if (session.GetLastBlockCoordsStoreCounter() == 3) {
            session.SetMinedBlocksTrailArrayPos(
                    session.GetNextCoordsStorePos(),
                    ev.getBlock().getLocation().toVector().toBlockVector()
            );
        }
        session.CycleBlockCoordsStoreCounter();
        session.CycleNextCoordsStorePos();
    }

    private float getWeightFromAnalyzingTrail(@NonNull BlockBreakEvent ev, @NonNull MiningSession session, float mineralWeight) {
        int unalignedMinedBlocksTimesDetected = 0;
        int iteratedBlockCoordSlots = 0;
        BlockVector block = ev.getBlock().getLocation().toVector().toBlockVector();
        for (int i = 0; i < 10; i++) {
            BlockVector pos = session.GetMinedBlocksTrailArrayPos(i);
            if (pos == null) {
                continue;
            }
            boolean yOff = Math.abs(pos.getBlockY() - block.getBlockY()) > 2;
            boolean xOff = Math.abs(pos.getBlockX() - block.getBlockX()) > 2;
            boolean zOff = Math.abs(pos.getBlockZ() - block.getBlockZ()) > 2;
            if (yOff || (xOff && zOff)) {
                unalignedMinedBlocksTimesDetected++;
            }
            iteratedBlockCoordSlots++;
        }
        float halfUnaligned = unalignedMinedBlocksTimesDetected / 2.0f;
        float halfIterated = iteratedBlockCoordSlots / 2.0f;
        float fractionReducerValue = iteratedBlockCoordSlots - halfIterated;
        if (halfUnaligned > halfIterated) {
            fractionReducerValue /= 3.0f;
        }
        if (fractionReducerValue < 1.0F) {
            fractionReducerValue = 1.0F;
        }
        session.ResetBlocksTrailArray();
        return mineralWeight + mineralWeight / fractionReducerValue;
    }

    private boolean checkGoldBiome(@NonNull BlockBreakEvent ev) {
        Biome biome = ev.getPlayer().getLocation().getBlock().getBiome();
        return biome == Biome.BADLANDS || biome == Biome.WOODED_BADLANDS || biome == Biome.ERODED_BADLANDS;
    }

    private boolean checkEmeraldBiome(@NonNull BlockBreakEvent ev) {
        Biome biome = ev.getPlayer().getLocation().getBlock().getBiome();
        return biome == Biome.MEADOW
                || biome == Biome.CHERRY_GROVE
                || biome == Biome.GROVE
                || biome == Biome.SNOWY_SLOPES
                || biome == Biome.JAGGED_PEAKS
                || biome == Biome.FROZEN_PEAKS
                || biome == Biome.STONY_PEAKS
                || biome == Biome.WINDSWEPT_HILLS
                || biome == Biome.WINDSWEPT_GRAVELLY_HILLS
                || biome == Biome.WINDSWEPT_FOREST;
    }

    private boolean updateMiningSession(@NonNull BlockBreakEvent ev, @NonNull Material material) {
        MiningSession session = this.sessions.get(ev.getPlayer().getName());
        if (session == null) {
            return false;
        }

        session.UpdateTimeAccountingProperties(ev.getPlayer());
        if (isBaseTrackingBlock(material)) {
            session.minedNonOreBlocksStreak++;
            updateTrail(ev, session);
            return finalizeSuspicion(ev, session);
        }

        if (isTrackedOre(material)) {
            if (shouldCountAsNewVein(session, material, ev.getBlock().getLocation())
                    && session.minedNonOreBlocksStreak > getMinimumBlocksMinedToNextVein()) {
                float weight = getSuspicionWeight(ev, session, material);
                session.AddSuspicionLevel(getWeightFromAnalyzingTrail(ev, session, weight));
                session.minedNonOreBlocksStreak = 0;
            }
            session.SetLastMinedOreData(normalizeOreFamily(material), ev.getBlock().getLocation());
            return finalizeSuspicion(ev, session);
        }

        session.minedNonOreBlocksStreak++;
        updateTrail(ev, session);
        return finalizeSuspicion(ev, session);
    }

    private boolean finalizeSuspicion(@NonNull BlockBreakEvent ev, @NonNull MiningSession session) {
        if (session.GetSuspicionLevel() < 0.0F) {
            session.SetSuspicionLevel(0.0F);
        }
        if (session.GetSuspicionLevel() >= suspicionThreshold) {
            handledPlayerService.handlePlayer(ev.getPlayer().getName());
        }
        return true;
    }

    private boolean shouldCountAsNewVein(@NonNull MiningSession session, @NonNull Material material, @NonNull Location location) {
        Material normalizedMaterial = normalizeOreFamily(material);
        Material lastOre = session.GetLastMinedOre();
        if (lastOre == null || lastOre != normalizedMaterial) {
            return true;
        }

        int distance = getConfig().getInt("ConsiderAdjacentWithinDistance", 10);
        if (distance <= 0) {
            return true;
        }

        Location lastLocation = session.GetLastMinedOreLocation();
        return lastLocation == null || lastLocation.distance(location) > distance;
    }

    private float getSuspicionWeight(@NonNull BlockBreakEvent ev, @NonNull MiningSession session, @NonNull Material material) {
        float weight = getConfiguredWeight(material);
        if (usesEncounterBoost(material) && session.minedNonOreBlocksStreak <= this.usualEncounterThreshold) {
            weight *= 1.5F;
        }
        if (usesGoldBiomeReducer(material) && checkGoldBiome(ev)) {
            weight /= (float) Math.max(1.0D, getConfig().getDouble("FinalGoldWeightDivisionReducer", 4.0D));
        }
        if (usesEmeraldBiomeReducer(material) && checkEmeraldBiome(ev)) {
            weight /= (float) Math.max(1.0D, getConfig().getDouble("FinalEmeraldWeightDivisionReducer", 2.0D));
        }
        return weight;
    }

    private boolean usesEncounterBoost(@NonNull Material material) {
        return material == Material.DIAMOND_ORE
                || material == Material.DEEPSLATE_DIAMOND_ORE
                || material == Material.EMERALD_ORE
                || material == Material.DEEPSLATE_EMERALD_ORE
                || material == Material.ANCIENT_DEBRIS;
    }

    private boolean usesGoldBiomeReducer(@NonNull Material material) {
        return material == Material.GOLD_ORE || material == Material.DEEPSLATE_GOLD_ORE;
    }

    private boolean usesEmeraldBiomeReducer(@NonNull Material material) {
        return material == Material.EMERALD_ORE || material == Material.DEEPSLATE_EMERALD_ORE;
    }

    private boolean isBaseTrackingBlock(@NonNull Material material) {
        return BASE_TRACKING_BLOCKS.contains(material);
    }

    private boolean isTrackedOre(@NonNull Material material) {
        return getConfiguredWeight(material) > 0.0F && normalizeOreFamily(material) != Material.AIR;
    }

    private float getConfiguredWeight(@NonNull Material material) {
        return switch (material) {
            case COAL_ORE -> getConfigFloat("CoalWeight");
            case DEEPSLATE_COAL_ORE -> getConfigFloat("DeepslateCoal");
            case IRON_ORE, RAW_IRON_BLOCK -> getConfigFloat("IronWeight");
            case DEEPSLATE_IRON_ORE -> getConfigFloat("DeepslateIron");
            case COPPER_ORE, RAW_COPPER_BLOCK -> getConfigFloat("CopperWeight");
            case DEEPSLATE_COPPER_ORE -> getConfigFloat("DeepslateCopper");
            case GOLD_ORE -> getConfigFloat("GoldWeight");
            case DEEPSLATE_GOLD_ORE -> getConfigFloat("DeepslateGold");
            case REDSTONE_ORE -> getConfigFloat("RedstoneWeight");
            case DEEPSLATE_REDSTONE_ORE -> getConfigFloat("DeepslateRedstone");
            case EMERALD_ORE -> getConfigFloat("EmeraldWeight");
            case DEEPSLATE_EMERALD_ORE -> getConfigFloat("DeepslateEmerald");
            case LAPIS_ORE -> getConfigFloat("LapisWeight");
            case DEEPSLATE_LAPIS_ORE -> getConfigFloat("DeepslateLapis");
            case DIAMOND_ORE -> getConfigFloat("DiamondWeight");
            case DEEPSLATE_DIAMOND_ORE -> getConfigFloat("DeepslateDiamond");
            case NETHER_QUARTZ_ORE -> getConfigFloat("QuartzWeight");
            case NETHER_GOLD_ORE, GILDED_BLACKSTONE -> getConfigFloat("NetherGoldWeight");
            case ANCIENT_DEBRIS -> getConfigFloat("AncientDebrisWeight");
            default -> 0.0F;
        };
    }

    private float getConfigFloat(@NonNull String path) {
        return (float) getConfig().getDouble(path, 0.0D);
    }

    private @NonNull Material normalizeOreFamily(@NonNull Material material) {
        return switch (material) {
            case COAL_ORE, DEEPSLATE_COAL_ORE -> Material.COAL_ORE;
            case IRON_ORE, DEEPSLATE_IRON_ORE, RAW_IRON_BLOCK -> Material.IRON_ORE;
            case COPPER_ORE, DEEPSLATE_COPPER_ORE, RAW_COPPER_BLOCK -> Material.COPPER_ORE;
            case GOLD_ORE, DEEPSLATE_GOLD_ORE -> Material.GOLD_ORE;
            case REDSTONE_ORE, DEEPSLATE_REDSTONE_ORE -> Material.REDSTONE_ORE;
            case EMERALD_ORE, DEEPSLATE_EMERALD_ORE -> Material.EMERALD_ORE;
            case LAPIS_ORE, DEEPSLATE_LAPIS_ORE -> Material.LAPIS_ORE;
            case DIAMOND_ORE, DEEPSLATE_DIAMOND_ORE -> Material.DIAMOND_ORE;
            case NETHER_QUARTZ_ORE -> Material.NETHER_QUARTZ_ORE;
            case NETHER_GOLD_ORE, GILDED_BLACKSTONE -> Material.NETHER_GOLD_ORE;
            case ANCIENT_DEBRIS -> Material.ANCIENT_DEBRIS;
            default -> Material.AIR;
        };
    }

    private @NonNull Material relevantBlockCheck(@NonNull BlockBreakEvent event) {
        Material material = event.getBlock().getType();
        if (isBaseTrackingBlock(material) || isTrackedOre(material)) {
            return material;
        }
        return Material.AIR;
    }

    public void BBEventAnalyzer(@NonNull BlockBreakEvent ev) {
        if (ev.getPlayer().hasPermission("AXH.Ignore") || ev.getPlayer().hasPermission("xrayheuristics.ignore")) {
            return;
        }

        Material material = relevantBlockCheck(ev);
        if (material == Material.AIR) {
            return;
        }

        if (!updateMiningSession(ev, material)) {
            this.sessions.put(ev.getPlayer().getName(), new MiningSession(this));
            updateMiningSession(ev, material);
        }
    }
}
