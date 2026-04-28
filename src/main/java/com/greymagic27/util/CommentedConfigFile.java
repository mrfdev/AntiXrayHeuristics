package com.greymagic27.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public final class CommentedConfigFile {
    private CommentedConfigFile() {
    }

    public static @NonNull YamlConfiguration loadAndSync(
            @NonNull JavaPlugin plugin,
            @NonNull File file,
            @NonNull String resourcePath
    ) throws IOException, InvalidConfigurationException {
        YamlConfiguration configuration = YamlFiles.load(file);
        synchronizeWithDefaults(plugin, configuration, resourcePath);
        return configuration;
    }

    public static void saveAndSync(
            @NonNull JavaPlugin plugin,
            @NonNull YamlConfiguration configuration,
            @NonNull File file,
            @NonNull String resourcePath
    ) throws IOException, InvalidConfigurationException {
        synchronizeWithDefaults(plugin, configuration, resourcePath);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        configuration.save(file);
    }

    private static void synchronizeWithDefaults(
            @NonNull JavaPlugin plugin,
            @NonNull YamlConfiguration configuration,
            @NonNull String resourcePath
    ) throws IOException, InvalidConfigurationException {
        YamlConfiguration defaults = loadDefaults(plugin, resourcePath);
        if (defaults == null) {
            return;
        }

        copyHeaderAndFooter(defaults, configuration);
        mergeMissingValues(defaults, configuration, "");
        applyComments(defaults, configuration, "");
        configuration.setDefaults(defaults);
    }

    private static @Nullable YamlConfiguration loadDefaults(
            @NonNull JavaPlugin plugin,
            @NonNull String resourcePath
    ) throws IOException, InvalidConfigurationException {
        try (var inputStream = plugin.getResource(resourcePath)) {
            if (inputStream == null) {
                return null;
            }
            return YamlFiles.load(inputStream);
        }
    }

    private static void copyHeaderAndFooter(
            @NonNull YamlConfiguration defaults,
            @NonNull YamlConfiguration configuration
    ) {
        if (configuration.options().getHeader().isEmpty()) {
            configuration.options().setHeader(defaults.options().getHeader());
        }
        if (configuration.options().getFooter().isEmpty()) {
            configuration.options().setFooter(defaults.options().getFooter());
        }
    }

    private static void mergeMissingValues(
            @NonNull ConfigurationSection defaults,
            @NonNull YamlConfiguration configuration,
            @NonNull String parentPath
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
            @NonNull ConfigurationSection defaults,
            @NonNull YamlConfiguration configuration,
            @NonNull String parentPath
    ) {
        for (String key : defaults.getKeys(false)) {
            String path = parentPath.isEmpty() ? key : parentPath + "." + key;

            List<String> comments = mergeComments(
                    sanitizeComments(configuration.getComments(path)),
                    sanitizeComments(defaults.getComments(key))
            );
            if (!comments.isEmpty()) {
                configuration.setComments(path, comments);
            }

            List<String> inlineComments = mergeComments(
                    sanitizeComments(configuration.getInlineComments(path)),
                    sanitizeComments(defaults.getInlineComments(key))
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

    private static @NonNull List<String> sanitizeComments(@NonNull List<String> comments) {
        List<String> sanitized = new ArrayList<>(comments.size());
        for (String comment : comments) {
            if (comment != null && !comment.isBlank()) {
                sanitized.add(comment);
            }
        }
        return sanitized;
    }

    private static @NonNull List<String> mergeComments(
            @NonNull List<String> existingComments,
            @NonNull List<String> defaultComments
    ) {
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
