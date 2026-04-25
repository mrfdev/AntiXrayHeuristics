package com.greymagic27;

import com.greymagic27.api.APIAntiXrayHeuristics;
import com.greymagic27.api.APIAntiXrayHeuristicsImpl;
import com.greymagic27.command.CommandAXH;
import com.greymagic27.command.CommandAXHAutoCompleter;
import com.greymagic27.event.EventBlockBreak;
import com.greymagic27.event.EventBlockPlace;
import com.greymagic27.event.EventClick;
import com.greymagic27.event.EventInventoryClose;
import com.greymagic27.event.EventItemDrag;
import com.greymagic27.event.EventPlayerChangedWorld;
import com.greymagic27.integration.CoreProtectHook;
import com.greymagic27.manager.LocaleManager;
import com.greymagic27.manager.MemoryManager;
import com.greymagic27.util.BlockWeightInfo;
import com.greymagic27.util.MiningSession;
import com.greymagic27.util.WeightsCard;
import com.greymagic27.util.YamlFiles;
import com.greymagic27.xrayer.XrayerHandler;
import com.greymagic27.xrayer.XrayerVault;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
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
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BlockVector;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public final class AntiXrayHeuristics extends JavaPlugin implements Listener {
    private static final String CUSTOM_DATA_DIRECTORY_NAME = "1MB-XRayHeuristics";
    private static final EnumSet<Material> BASE_TRACKING_BLOCKS = EnumSet.of(
            Material.STONE,
            Material.DEEPSLATE,
            Material.TUFF,
            Material.NETHERRACK,
            Material.BASALT
    );
    private static AntiXrayHeuristics plugin;
    public final float maxSuspicionDecreaseProportion = -10.0F;
    public final float minSuspicionDecreaseProportion = -0.1F;
    public final float absoluteMinimumSuspicionDecrease = -3.0F;
    public final int maxAccountableMillisecondDeltaForThirtyMinedBlocks = 20000;
    public final int minAccountableMillisecondDeltaForThirtyMinedBlocks = 0;
    public final HashMap<String, MiningSession> sessions = new HashMap<>();
    public final MemoryManager mm = new MemoryManager(this);
    public XrayerVault vault;
    private APIAntiXrayHeuristics api;
    private CoreProtectHook coreProtectHook;
    private File pluginDataDirectory;
    private File configFile;
    private FileConfiguration configuration;
    private int nonOreStreakDecreaseAmount;
    private int usualEncounterThreshold;
    private float suspicionThreshold;
    private boolean verboseMiningSessionDebug;

    @SuppressWarnings("unused")
    public static AntiXrayHeuristics GetPlugin() {
        return plugin;
    }

    @SuppressWarnings("unused")
    public APIAntiXrayHeuristics GetAPI() {
        return this.api;
    }

    public @NonNull CoreProtectHook getCoreProtectHook() {
        return Objects.requireNonNull(coreProtectHook, "CoreProtect hook has not been initialized yet.");
    }

    public float getSuspicionThreshold() {
        return suspicionThreshold;
    }

    public @NonNull File getPluginDataDirectory() {
        if (pluginDataDirectory == null) {
            File parentDirectory = getDataFolder().getParentFile();
            if (parentDirectory == null) {
                parentDirectory = getDataFolder();
            }
            pluginDataDirectory = new File(parentDirectory, CUSTOM_DATA_DIRECTORY_NAME);
        }
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

    @Override
    public FileConfiguration getConfig() {
        if (configuration == null) {
            reloadConfig();
        }
        return configuration;
    }

    @Override
    public void reloadConfig() {
        ensurePluginDataDirectory();
        if (configFile == null) {
            configFile = getPluginDataFile("config.yml");
        }
        try {
            configuration = YamlFiles.load(configFile);
        } catch (IOException | InvalidConfigurationException e) {
            configuration = new YamlConfiguration();
            getLogger().log(Level.SEVERE, "Could not load config.yml.", e);
        }

        try (var defaultConfigReader = getTextResource("config.yml")) {
            if (defaultConfigReader != null) {
                YamlConfiguration defaultConfiguration = YamlConfiguration.loadConfiguration(defaultConfigReader);
                configuration.setDefaults(defaultConfiguration);
            }
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Could not read embedded config.yml.", e);
        }
    }

    @Override
    public void saveConfig() {
        if (configuration == null || configFile == null) {
            return;
        }
        try {
            configuration.save(configFile);
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Could not save config.yml.", e);
        }
    }

    @Override
    public void saveDefaultConfig() {
        ensurePluginDataDirectory();
        if (configFile == null) {
            configFile = getPluginDataFile("config.yml");
        }
        if (!configFile.exists()) {
            saveResource("config.yml", false);
        }
    }

    @Override
    public void saveResource(@NonNull String resourcePath, boolean replace) {
        if (resourcePath.isEmpty()) {
            throw new IllegalArgumentException("ResourcePath cannot be empty.");
        }

        String normalizedResourcePath = resourcePath.replace('\\', '/');
        try (InputStream resource = getResource(normalizedResourcePath)) {
            if (resource == null) {
                throw new IllegalArgumentException("The embedded resource '" + normalizedResourcePath + "' cannot be found.");
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

    @Override
    public void onEnable() {
        plugin = this;
        this.api = new APIAntiXrayHeuristicsImpl(this);

        ensurePluginDataDirectory();
        this.configFile = getPluginDataFile("config.yml");
        saveDefaultConfig();
        reloadConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();

        this.coreProtectHook = new CoreProtectHook(this);
        if (!refreshCoreProtectHook()) {
            getLogger().severe("CoreProtect API 11 or newer is required. Tested with CoreProtect 24.0-dev1 (API 12). Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        ConfigurationSerialization.registerClass(BlockWeightInfo.class);
        LocaleManager.setup(getPluginDataDirectory());
        LocaleManager.get().options().copyDefaults(true);
        LocaleManager.save();
        WeightsCard.setup(getPluginDataDirectory());
        WeightsCard.get().options().copyDefaults(true);
        WeightsCard.save();

        applyRuntimeConfig();
        this.vault = new XrayerVault(this);

        Objects.requireNonNull(getCommand("xrayer")).setExecutor(new CommandAXH(this));
        Objects.requireNonNull(getCommand("xrayer")).setTabCompleter(new CommandAXHAutoCompleter(this));

        initializeStorage();
        registerEvents();
        mainRunnable();

        Bukkit.getConsoleSender().sendMessage(
                LegacyComponentSerializer.legacyAmpersand().deserialize(
                        "&5[&b1MB Heuristics&5] &aEnabled successfully with " + getCoreProtectHook().getSummaryLine()
                )
        );
    }

    @Override
    public void onDisable() {
        if (Objects.equals(getConfig().getString("StorageType"), "MYSQL")) {
            this.mm.CloseDataSource();
        }
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
        if (Objects.equals(getConfig().getString("StorageType"), "MYSQL")) {
            this.mm.InitializeDataSource();
            Bukkit.getScheduler().runTaskAsynchronously(this, this.mm::SQLCreateTableIfNotExists);
        } else if (Objects.equals(getConfig().getString("StorageType"), "JSON")) {
            this.mm.JSONFileCreateIfNotExists();
        }
    }

    private void registerEvents() {
        getServer().getPluginManager().registerEvents(new EventBlockBreak(this), this);
        getServer().getPluginManager().registerEvents(new EventBlockPlace(this), this);
        getServer().getPluginManager().registerEvents(new EventClick(this), this);
        getServer().getPluginManager().registerEvents(new EventItemDrag(), this);
        getServer().getPluginManager().registerEvents(new EventInventoryClose(this), this);
        getServer().getPluginManager().registerEvents(new EventPlayerChangedWorld(this), this);
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

    private void mainRunnable() {
        (new BukkitRunnable() {
            @Override
            public void run() {
                Iterator<Map.Entry<String, MiningSession>> iterator = AntiXrayHeuristics.this.sessions.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, MiningSession> entry = iterator.next();
                    MiningSession session = entry.getValue();
                    session.SelfSuspicionReducer();
                    session.minedNonOreBlocksStreak += AntiXrayHeuristics.this.nonOreStreakDecreaseAmount;
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
        }).runTaskTimer(this, 200L, 200L);
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
            XrayerHandler.HandleXrayer(ev.getPlayer().getName());
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
