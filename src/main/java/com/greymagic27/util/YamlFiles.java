package com.greymagic27.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jspecify.annotations.NonNull;

public final class YamlFiles {
    private YamlFiles() {
    }

    public static @NonNull YamlConfiguration load(@NonNull File file) throws IOException, InvalidConfigurationException {
        YamlConfiguration configuration = new YamlConfiguration();
        if (!file.exists()) {
            return configuration;
        }

        configuration.loadFromString(Files.readString(file.toPath(), StandardCharsets.UTF_8));
        return configuration;
    }
}
