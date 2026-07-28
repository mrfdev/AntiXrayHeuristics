package com.greymagic27.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NonNull;

public record BuildMetadata(
        @NonNull String pluginVersion,
        @NonNull String buildNumber,
        @NonNull String artifactVersion,
        int javaTarget,
        @NonNull String paperVersion,
        @NonNull String paperApi,
        int paperApiBuild,
        @NonNull String paperApiChannel,
        @NonNull String declaredApiVersion,
        @NonNull String coreProtectVersion,
        int coreProtectApiVersion
) {
    private static final String RESOURCE_PATH = "build-info.properties";

    public static @NonNull BuildMetadata load(@NonNull JavaPlugin plugin) {
        try (InputStream input = plugin.getResource(RESOURCE_PATH)) {
            if (input == null) {
                throw new IllegalStateException("Missing packaged release metadata: " + RESOURCE_PATH);
            }
            Properties properties = new Properties();
            properties.load(input);
            return fromProperties(properties);
        } catch (IOException e) {
            throw new IllegalStateException("Could not read packaged release metadata.", e);
        }
    }

    static @NonNull BuildMetadata fromProperties(@NonNull Properties properties) {
        return new BuildMetadata(
                required(properties, "pluginVersion"),
                required(properties, "buildNumber"),
                required(properties, "artifactVersion"),
                requiredInt(properties, "javaTarget"),
                required(properties, "paperVersion"),
                required(properties, "paperApi"),
                requiredInt(properties, "paperApiBuild"),
                required(properties, "paperApiChannel"),
                required(properties, "declaredApiVersion"),
                required(properties, "coreProtectVersion"),
                requiredInt(properties, "coreProtectApiVersion")
        );
    }

    public @NonNull String coreProtectTarget() {
        return coreProtectVersion + " (API " + coreProtectApiVersion + ")";
    }

    private static @NonNull String required(@NonNull Properties properties, @NonNull String key) {
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing release metadata property: " + key);
        }
        return value.trim();
    }

    private static int requiredInt(@NonNull Properties properties, @NonNull String key) {
        String value = required(properties, key);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Release metadata property '" + key + "' must be numeric: " + value, e);
        }
    }
}
