package com.greymagic27.command;

import com.greymagic27.AntiXrayHeuristics;
import com.greymagic27.integration.CoreProtectHook;
import com.greymagic27.manager.LocaleManager;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public final class CommandARGDebug {
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacyAmpersand();
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.##");
    private static final String ROOT = "xrayer";
    private static final String PERMISSION_PREFIX = "xrayheuristics";
    private static final Map<String, DebugConfigKey> DEBUG_CONFIG_KEYS = buildConfigKeys();

    private CommandARGDebug() {
    }

    public static boolean handle(
            @NonNull CommandSender sender,
            String @NonNull [] args,
            @NonNull AntiXrayHeuristics plugin
    ) {
        if (!CommandARGHelp.hasAdminAccess(sender)) {
            Component noPermission = LEGACY_SERIALIZER.deserialize(
                    Objects.requireNonNull(LocaleManager.get().getString("NoPermissionForCommand"))
            );
            sender.sendMessage(noPermission);
            return true;
        }

        if (args.length == 1) {
            sendOverview(sender, plugin);
            return true;
        }

        String page = args[1].toLowerCase(Locale.ROOT);
        return switch (page) {
            case "help", "?" -> {
                sendDebugHelp(sender);
                yield true;
            }
            case "permissions" -> {
                sendPermissions(sender);
                yield true;
            }
            case "commands" -> {
                sendCommands(sender);
                yield true;
            }
            case "config" -> {
                sendConfig(sender, plugin);
                yield true;
            }
            case "set" -> {
                handleSet(sender, args, plugin);
                yield true;
            }
            default -> {
                sendLine(sender, "&cUnknown debug page: &f" + args[1]);
                sendLine(sender, "&7Try &f/" + ROOT + " debug help&7 for the available options.");
                yield true;
            }
        };
    }

    public static @NonNull List<String> getConfigKeys() {
        return List.copyOf(DEBUG_CONFIG_KEYS.keySet());
    }

    public static @NonNull List<String> getValueSuggestions(@NonNull String key) {
        DebugConfigKey configKey = DEBUG_CONFIG_KEYS.get(key.toLowerCase(Locale.ROOT));
        if (configKey == null) {
            return List.of();
        }
        return switch (configKey.valueType()) {
            case BOOLEAN -> List.of("true", "false");
            case INTEGER -> List.of("0", "5", "10", "25", "100");
            case DOUBLE -> List.of("0.0", "5.0", "10.0", "22.0", "100.0");
            case STRING_LIST -> List.of("general,wild,nether", "world,world_nether", "general");
        };
    }

    private static void sendOverview(@NonNull CommandSender sender, @NonNull AntiXrayHeuristics plugin) {
        CoreProtectHook hook = plugin.getCoreProtectHook();
        String pluginVersion = plugin.getPluginMeta().getVersion();
        sendLine(sender, "&8&m------------------------------------------------");
        sendLine(sender, "&d&l1MB Heuristics Debug");
        sendLine(sender, "&7Plugin: &f" + plugin.getPluginMeta().getName());
        sendLine(sender, "&7Version: &f" + pluginVersion);
        sendLine(sender, "&7Build number: &f" + extractBuildPart(pluginVersion, 1, "unknown"));
        sendLine(sender, "&7Compiled Java: &f" + extractBuildPart(pluginVersion, 2, "j??"));
        sendLine(sender, "&7Compiled Paper: &f" + extractBuildPart(pluginVersion, 3, "26.1.2"));
        sendLine(sender, "&7Declared API floor: &f" + plugin.getPluginMeta().getAPIVersion());
        sendLine(sender, "&7Runtime Java: &f" + System.getProperty("java.version"));
        sendLine(sender, "&7Server: &f" + Bukkit.getName() + " " + Bukkit.getVersion());
        sendLine(sender, "&7CoreProtect hooked: &f" + yesNo(hook.isHooked()));
        sendLine(sender, "&7CoreProtect version: &f" + hook.getCoreProtectVersion());
        sendLine(sender, "&7CoreProtect API: &f" + hook.getCoreProtectApiVersion());
        sendLine(sender, "&7CoreProtect jar: &f" + hook.getJarPath());
        sendLine(sender, "&7CoreProtect data: &f" + hook.getDataFolderPath());
        sendLine(sender, "&7Hook status: &f" + hook.getStatusMessage());
        sendLine(sender, "&7Description: &f" + plugin.getPluginMeta().getDescription());
        sendLine(sender, "&7Storage type: &f" + plugin.getConfig().getString("StorageType", "JSON"));
        sendLine(sender, "&7Tracked worlds: &f" + join(plugin.getConfig().getStringList("TrackWorlds")));
        sendLine(sender, "&7Suspicion threshold: &f" + DECIMAL_FORMAT.format(plugin.getSuspicionThreshold()));
        sendLine(sender, "&7Verbose mining debug: &f" + yesNo(plugin.isVerboseMiningSessionDebug()));
        sendLine(sender, "&7Active sessions: &f" + plugin.sessions.size());
        sendLine(sender, "&7Config file: &f" + plugin.getPluginDataFile("config.yml").getAbsolutePath());
        sendLine(sender, "&7Locale file: &f" + plugin.getPluginDataFile("locale.yml").getAbsolutePath());
        sendLine(sender, "&7Weights file: &f" + plugin.getPluginDataFile("weights.yml").getAbsolutePath());
        sendLine(sender, "&7More debug: &f/" + ROOT + " debug help");
        sendLine(sender, "&8&m------------------------------------------------");
    }

    private static void sendDebugHelp(@NonNull CommandSender sender) {
        sendLine(sender, "&8&m------------------------------------------------");
        sendLine(sender, "&d&l" + ROOT + " debug help");
        sendLine(sender, "&f/" + ROOT + " debug &7- Main plugin, server, and CoreProtect overview.");
        sendLine(sender, "&f/" + ROOT + " debug permissions &7- List current permission nodes and defaults.");
        sendLine(sender, "&f/" + ROOT + " debug commands &7- List command syntax and quick examples.");
        sendLine(sender, "&f/" + ROOT + " debug config &7- Show the live config values exposed to debug.");
        sendLine(sender, "&f/" + ROOT + " debug set <key> <value> &7- Save a supported config value and reload it.");
        sendLine(sender, "&8&m------------------------------------------------");
    }

    private static void sendPermissions(@NonNull CommandSender sender) {
        sendLine(sender, "&8&m------------------------------------------------");
        sendLine(sender, "&d&l" + ROOT + " debug permissions");
        sendLine(sender, "&f" + PERMISSION_PREFIX + ".use &7(default: op) - Help output and suspicion lookups.");
        sendLine(sender, "&f" + PERMISSION_PREFIX + ".admin &7(default: op) - Reload, debug pages, vault actions, and admin commands.");
        sendLine(sender, "&f" + PERMISSION_PREFIX + ".notify &7(default: op) - Receives automatic handled-player warnings.");
        sendLine(sender, "&f" + PERMISSION_PREFIX + ".ignore &7(default: false) - Bypasses heuristic tracking.");
        sendLine(sender, "&7Legacy nodes still supported: &fAXH.Commands.*&7, &fAXH.Vault.Purge&7, &fAXH.XrayerWarning&7, &fAXH.Ignore");
        sendLine(sender, "&8&m------------------------------------------------");
    }

    private static void sendCommands(@NonNull CommandSender sender) {
        sendLine(sender, "&8&m------------------------------------------------");
        sendLine(sender, "&d&l" + ROOT + " debug commands");
        sendLine(sender, "&f/" + ROOT + " help &7- Show command help.");
        sendLine(sender, "&f/" + ROOT + " debug &7- Show plugin and CoreProtect status.");
        sendLine(sender, "&f/" + ROOT + " debug permissions &7- Show permission nodes.");
        sendLine(sender, "&f/" + ROOT + " debug commands &7- Show this command reference.");
        sendLine(sender, "&f/" + ROOT + " debug config &7- Show supported live config values.");
        sendLine(sender, "&f/" + ROOT + " debug set <key> <value> &7- Write a supported config value.");
        sendLine(sender, "&f/" + ROOT + " reload &7- Reload config, locale, weights, and hook state.");
        sendLine(sender, "&f/" + ROOT + " suspicion [player] &7- Show current suspicion.");
        sendLine(sender, "&f/" + ROOT + " resetsuspicion [player] &7- Remove a tracked suspicion session.");
        sendLine(sender, "&f/" + ROOT + " <player> &7- Manually handle a player as xrayer.");
        sendLine(sender, "&f/" + ROOT + " vault &7- Open the xrayer vault.");
        sendLine(sender, "&f/" + ROOT + " absolve <player> &7- Return stored items and clear the vault entry.");
        sendLine(sender, "&f/" + ROOT + " purge <player> &7- Delete the vault entry.");
        sendLine(sender, "&7Placeholders used in config commands: &f{PlayerName}&7 and &f{TimesDetected}");
        sendLine(sender, "&8&m------------------------------------------------");
    }

    private static void sendConfig(@NonNull CommandSender sender, @NonNull AntiXrayHeuristics plugin) {
        sendLine(sender, "&8&m------------------------------------------------");
        sendLine(sender, "&d&l" + ROOT + " debug config");
        sendLine(sender, "&7Use &f/" + ROOT + " debug set <key> <value> &7to change one of these values.");
        for (DebugConfigKey configKey : DEBUG_CONFIG_KEYS.values()) {
            sendLine(
                    sender,
                    "&f"
                            + configKey.key()
                            + "&7 = &b"
                            + formatConfigValue(plugin, configKey)
                            + "&7 - "
                            + configKey.description()
            );
        }
        sendLine(sender, "&8&m------------------------------------------------");
    }

    private static void handleSet(
            @NonNull CommandSender sender,
            String @NonNull [] args,
            @NonNull AntiXrayHeuristics plugin
    ) {
        if (args.length < 4) {
            sendLine(sender, "&cUsage: &f/" + ROOT + " debug set <key> <value>");
            sendLine(sender, "&7Try &f/" + ROOT + " debug config&7 to view supported keys.");
            return;
        }

        String key = args[2].toLowerCase(Locale.ROOT);
        DebugConfigKey configKey = DEBUG_CONFIG_KEYS.get(key);
        if (configKey == null) {
            sendLine(sender, "&cUnsupported config key: &f" + args[2]);
            sendLine(sender, "&7Try &f/" + ROOT + " debug config&7 to view supported keys.");
            return;
        }

        String rawValue = String.join(" ", Arrays.copyOfRange(args, 3, args.length)).trim();
        try {
            Object parsedValue = parseValue(rawValue, configKey.valueType());
            plugin.getConfig().set(configKey.path(), parsedValue);
            if (configKey.legacyPath() != null) {
                plugin.getConfig().set(configKey.legacyPath(), parsedValue);
            }
            plugin.saveConfig();
            plugin.reloadPluginState();
            sendLine(sender, "&aSaved &f" + configKey.key() + "&a = &b" + formatValueForDisplay(parsedValue));
        } catch (IllegalArgumentException exception) {
            sendLine(sender, "&cCould not parse value for &f" + configKey.key() + "&c: " + exception.getMessage());
        }
    }

    private static @NonNull Object parseValue(@NonNull String rawValue, @NonNull ValueType valueType) {
        return switch (valueType) {
            case BOOLEAN -> {
                if (!rawValue.equalsIgnoreCase("true") && !rawValue.equalsIgnoreCase("false")) {
                    throw new IllegalArgumentException("expected true or false");
                }
                yield Boolean.parseBoolean(rawValue);
            }
            case INTEGER -> Integer.parseInt(rawValue);
            case DOUBLE -> Double.parseDouble(rawValue);
            case STRING_LIST -> Arrays.stream(rawValue.split(","))
                    .map(String::trim)
                    .filter(part -> !part.isEmpty())
                    .collect(Collectors.toList());
        };
    }

    private static @NonNull String formatConfigValue(@NonNull AntiXrayHeuristics plugin, @NonNull DebugConfigKey configKey) {
        Object value = plugin.getConfig().get(configKey.path());
        if (value == null && configKey.legacyPath() != null) {
            value = plugin.getConfig().get(configKey.legacyPath());
        }
        return formatValueForDisplay(value);
    }

    private static @NonNull String formatValueForDisplay(@Nullable Object value) {
        if (value instanceof List<?> list) {
            return join(list.stream().map(String::valueOf).collect(Collectors.toList()));
        }
        if (value instanceof Number number) {
            return DECIMAL_FORMAT.format(number.doubleValue());
        }
        return String.valueOf(value);
    }

    private static @NonNull String join(@NonNull List<String> values) {
        return values.isEmpty() ? "(empty)" : String.join(", ", values);
    }

    private static @NonNull String extractBuildPart(@NonNull String version, int index, @NonNull String fallback) {
        String[] parts = version.split("-");
        return parts.length > index ? parts[index] : fallback;
    }

    private static @NonNull String yesNo(boolean value) {
        return value ? "yes" : "no";
    }

    private static void sendLine(@NonNull CommandSender sender, @NonNull String line) {
        sender.sendMessage(LEGACY_SERIALIZER.deserialize(line));
    }

    private static @NonNull Map<String, DebugConfigKey> buildConfigKeys() {
        Map<String, DebugConfigKey> keys = new LinkedHashMap<>();
        add(keys, "suspicion-threshold", "SuspicionThreshold", null, ValueType.DOUBLE, "Points required before auto-handling.");
        add(keys, "debug-verbose-mining-session", "DebugVerboseMiningSession", null, ValueType.BOOLEAN, "Verbose console output for mining-session math.");
        add(keys, "track-worlds", "TrackWorlds", null, ValueType.STRING_LIST, "Comma-separated world names that should be tracked.");
        add(keys, "consider-adjacent-within-distance", "ConsiderAdjacentWithinDistance", null, ValueType.INTEGER, "Blocks still counted as the same ore vein.");
        add(keys, "minimum-blocks-mined-to-next-vein", "MinimumBlocksMinedToNextVein", null, ValueType.INTEGER, "Minimum non-ore blocks between counted veins.");
        add(keys, "ignore-higher-than-overworld-altitude", "IgnoreHigherThanOverworldAltitude", null, ValueType.INTEGER, "Ignore overworld tracking above this Y level.");
        add(keys, "ignore-higher-than-nether-altitude", "IgnoreHigherThanNetherAltitude", null, ValueType.INTEGER, "Ignore nether tracking above this Y level.");
        add(keys, "store-copy", "StoreCopy", null, ValueType.BOOLEAN, "Store a copy of inventory/equipment before handling.");
        add(keys, "cleanse-player-items", "CleansePlayerItems", "ClensePlayerItems", ValueType.BOOLEAN, "Clear player inventory/equipment when handled.");
        add(keys, "send-message-to-player", "SendMessageToPlayer", null, ValueType.BOOLEAN, "Send the handled-player warning message.");
        add(keys, "nullify-suspicion-after-punish", "NullifySuspicionAfterPunish", "NullifySuspicionAferPunish", ValueType.BOOLEAN, "Remove the active suspicion session after handling.");
        add(keys, "tell-players-with-permission", "TellPlayersWithPermission", null, ValueType.BOOLEAN, "Notify players with warning permission when someone is handled.");
        add(keys, "add-random-dummy-xrayer", "AddRandomDummyXrayerIfNoXrayerCommandParameters", null, ValueType.BOOLEAN, "Allow the legacy manual-handle syntax with no player for testing.");
        add(keys, "use-heads-in-gui", "UseHeadsInGUI", null, ValueType.BOOLEAN, "Use player heads in the vault GUI.");
        add(keys, "coal-weight", "CoalWeight", null, ValueType.DOUBLE, "Suspicion weight for COAL_ORE.");
        add(keys, "iron-weight", "IronWeight", null, ValueType.DOUBLE, "Suspicion weight for IRON_ORE and RAW_IRON_BLOCK.");
        add(keys, "copper-weight", "CopperWeight", null, ValueType.DOUBLE, "Suspicion weight for COPPER_ORE and RAW_COPPER_BLOCK.");
        add(keys, "gold-weight", "GoldWeight", null, ValueType.DOUBLE, "Suspicion weight for GOLD_ORE.");
        add(keys, "redstone-weight", "RedstoneWeight", null, ValueType.DOUBLE, "Suspicion weight for REDSTONE_ORE.");
        add(keys, "emerald-weight", "EmeraldWeight", null, ValueType.DOUBLE, "Suspicion weight for EMERALD_ORE.");
        add(keys, "lapis-weight", "LapisWeight", null, ValueType.DOUBLE, "Suspicion weight for LAPIS_ORE.");
        add(keys, "diamond-weight", "DiamondWeight", null, ValueType.DOUBLE, "Suspicion weight for DIAMOND_ORE.");
        add(keys, "deepslate-coal", "DeepslateCoal", null, ValueType.DOUBLE, "Suspicion weight for DEEPSLATE_COAL_ORE.");
        add(keys, "deepslate-iron", "DeepslateIron", null, ValueType.DOUBLE, "Suspicion weight for DEEPSLATE_IRON_ORE.");
        add(keys, "deepslate-copper", "DeepslateCopper", null, ValueType.DOUBLE, "Suspicion weight for DEEPSLATE_COPPER_ORE.");
        add(keys, "deepslate-gold", "DeepslateGold", null, ValueType.DOUBLE, "Suspicion weight for DEEPSLATE_GOLD_ORE.");
        add(keys, "deepslate-redstone", "DeepslateRedstone", null, ValueType.DOUBLE, "Suspicion weight for DEEPSLATE_REDSTONE_ORE.");
        add(keys, "deepslate-emerald", "DeepslateEmerald", null, ValueType.DOUBLE, "Suspicion weight for DEEPSLATE_EMERALD_ORE.");
        add(keys, "deepslate-lapis", "DeepslateLapis", null, ValueType.DOUBLE, "Suspicion weight for DEEPSLATE_LAPIS_ORE.");
        add(keys, "deepslate-diamond", "DeepslateDiamond", null, ValueType.DOUBLE, "Suspicion weight for DEEPSLATE_DIAMOND_ORE.");
        add(keys, "nether-gold-weight", "NetherGoldWeight", null, ValueType.DOUBLE, "Suspicion weight for NETHER_GOLD_ORE and GILDED_BLACKSTONE.");
        add(keys, "quartz-weight", "QuartzWeight", null, ValueType.DOUBLE, "Suspicion weight for NETHER_QUARTZ_ORE.");
        add(keys, "ancient-debris-weight", "AncientDebrisWeight", null, ValueType.DOUBLE, "Suspicion weight for ANCIENT_DEBRIS.");
        return keys;
    }

    private static void add(
            @NonNull Map<String, DebugConfigKey> keys,
            @NonNull String key,
            @NonNull String path,
            @Nullable String legacyPath,
            @NonNull ValueType valueType,
            @NonNull String description
    ) {
        keys.put(key, new DebugConfigKey(key, path, legacyPath, valueType, description));
    }

    private record DebugConfigKey(
            String key,
            String path,
            @Nullable String legacyPath,
            ValueType valueType,
            String description
    ) {
    }

    private enum ValueType {
        BOOLEAN,
        INTEGER,
        DOUBLE,
        STRING_LIST
    }
}
