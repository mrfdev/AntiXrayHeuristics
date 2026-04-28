package com.greymagic27.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

class YamlFilesTest {

    @Test
    void parsesTopLevelCommentsFromBundledConfig() throws Exception {
        File configFile = new File("src/main/resources/config.yml");
        YamlConfiguration configuration = YamlFiles.load(configFile);

        List<String> trackWorldsComments = configuration.getComments("TrackWorlds");
        List<String> suspicionThresholdComments = configuration.getComments("SuspicionThreshold");
        List<String> commandComments = configuration.getComments("CommandsExecutedOnXrayerDetected");

        assertFalse(trackWorldsComments.isEmpty(), "TrackWorlds comments should be parsed from the bundled config.");
        assertTrue(normalize(trackWorldsComments).getFirst().contains("World names that should be monitored"), trackWorldsComments.toString());

        assertFalse(suspicionThresholdComments.isEmpty(), "SuspicionThreshold comments should be parsed from the bundled config.");
        assertTrue(normalize(suspicionThresholdComments).getFirst().contains("Suspicion points required"), suspicionThresholdComments.toString());

        assertFalse(commandComments.isEmpty(), "CommandsExecutedOnXrayerDetected comments should be parsed from the bundled config.");
        assertTrue(normalize(commandComments).getFirst().contains("Console commands to run"), commandComments.toString());
    }

    @Test
    void savesTopLevelCommentsWhenAssignedProgrammatically() {
        YamlConfiguration configuration = new YamlConfiguration();
        configuration.options().parseComments(true);
        configuration.set("TrackWorlds", List.of("general", "wild", "nether"));
        configuration.setComments(
                "TrackWorlds",
                List.of(
                        "World names that should be monitored for heuristic mining behaviour.",
                        "Default: [general, wild, nether]"
                )
        );

        String saved = configuration.saveToString();
        assertTrue(saved.contains("# World names that should be monitored for heuristic mining behaviour."), saved);
        assertTrue(saved.contains("TrackWorlds:"));
    }

    @Test
    void savesParsedCommentsWhenAssignedProgrammatically() throws Exception {
        YamlConfiguration defaults = YamlFiles.load(new File("src/main/resources/config.yml"));
        YamlConfiguration configuration = new YamlConfiguration();
        configuration.options().parseComments(true);
        configuration.set("TrackWorlds", defaults.get("TrackWorlds"));
        configuration.setComments("TrackWorlds", normalize(defaults.getComments("TrackWorlds")));

        String saved = configuration.saveToString();
        assertTrue(saved.contains("# World names that should be monitored for heuristic mining behaviour."), normalize(defaults.getComments("TrackWorlds")) + "\n" + saved);
    }

    @Test
    void commentSyncFlowKeepsTopLevelComments() throws Exception {
        YamlConfiguration defaults = YamlFiles.load(new File("src/main/resources/config.yml"));
        YamlConfiguration configuration = new YamlConfiguration();
        configuration.options().parseComments(true);

        configuration.options().setHeader(defaults.options().getHeader());
        configuration.options().setFooter(defaults.options().getFooter());
        mergeMissingValues(defaults, configuration, "");
        applyComments(defaults, configuration, "");
        configuration.setDefaults(defaults);

        String saved = configuration.saveToString();
        assertTrue(saved.contains("# World names that should be monitored for heuristic mining behaviour."), saved);
        assertTrue(saved.contains("# Suspicion points required before a player is automatically handled as an xrayer."), saved);
        assertTrue(saved.contains("# Console commands to run when a player is automatically handled."), saved);
    }

    @Test
    void commentSyncFlowPreservesExistingAdminValues() throws Exception {
        YamlConfiguration defaults = YamlFiles.load(new File("src/main/resources/config.yml"));
        YamlConfiguration configuration = new YamlConfiguration();
        configuration.options().parseComments(true);
        configuration.set("SuspicionThreshold", 175.0D);
        configuration.set("TrackWorlds", List.of("general", "mines"));

        mergeMissingValues(defaults, configuration, "");
        applyComments(defaults, configuration, "");
        configuration.setDefaults(defaults);

        String saved = configuration.saveToString();
        assertTrue(saved.contains("SuspicionThreshold: 175.0"), saved);
        assertTrue(saved.contains("- mines"), saved);
        assertTrue(saved.contains("# Suspicion points required before a player is automatically handled as an xrayer."), saved);
    }

    @Test
    void commentSyncFlowPreservesExistingAdminComments() throws Exception {
        YamlConfiguration defaults = YamlFiles.load(new File("src/main/resources/config.yml"));
        YamlConfiguration configuration = new YamlConfiguration();
        configuration.options().parseComments(true);
        configuration.set("SuspicionThreshold", 150.0D);
        configuration.setComments("SuspicionThreshold", List.of("Admin note: tuned for survival world."));

        mergeMissingValues(defaults, configuration, "");
        applyComments(defaults, configuration, "");

        String saved = configuration.saveToString();
        assertTrue(saved.contains("# Admin note: tuned for survival world."), saved);
        assertTrue(saved.contains("# Suspicion points required before a player is automatically handled as an xrayer."), saved);
    }

    private static List<String> normalize(List<String> comments) {
        List<String> normalized = new ArrayList<>();
        for (String comment : comments) {
            if (comment != null && !comment.isBlank()) {
                normalized.add(comment);
            }
        }
        return normalized;
    }

    private static void mergeMissingValues(
            ConfigurationSection defaults,
            YamlConfiguration configuration,
            String parentPath
    ) {
        for (String key : defaults.getKeys(false)) {
            String path = parentPath.isEmpty() ? key : parentPath + "." + key;

            ConfigurationSection defaultChild = defaults.getConfigurationSection(key);
            if (defaultChild != null) {
                if (!configuration.isConfigurationSection(path) && !configuration.isSet(path)) {
                    configuration.createSection(path);
                }
                mergeMissingValues(defaultChild, configuration, path);
                continue;
            }

            if (!configuration.isSet(path)) {
                configuration.set(path, defaults.get(key));
            }
        }
    }

    private static void applyComments(
            ConfigurationSection defaults,
            YamlConfiguration configuration,
            String parentPath
    ) {
        for (String key : defaults.getKeys(false)) {
            String path = parentPath.isEmpty() ? key : parentPath + "." + key;

            List<String> comments = mergeComments(
                    normalize(configuration.getComments(path)),
                    normalize(defaults.getComments(key))
            );
            if (!comments.isEmpty()) {
                configuration.setComments(path, comments);
            }

            List<String> inlineComments = mergeComments(
                    normalize(configuration.getInlineComments(path)),
                    normalize(defaults.getInlineComments(key))
            );
            if (!inlineComments.isEmpty()) {
                configuration.setInlineComments(path, inlineComments);
            }

            ConfigurationSection defaultChild = defaults.getConfigurationSection(key);
            if (defaultChild != null) {
                applyComments(defaultChild, configuration, path);
            }
        }
    }

    private static List<String> mergeComments(List<String> existingComments, List<String> defaultComments) {
        if (existingComments.isEmpty()) {
            return defaultComments;
        }
        if (defaultComments.isEmpty()) {
            return existingComments;
        }

        List<String> merged = new ArrayList<>(existingComments);
        for (String defaultComment : defaultComments) {
            if (!merged.contains(defaultComment)) {
                merged.add(defaultComment);
            }
        }
        return merged;
    }
}
