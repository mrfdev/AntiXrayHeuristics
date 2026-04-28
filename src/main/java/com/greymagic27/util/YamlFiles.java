package com.greymagic27.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jspecify.annotations.NonNull;

public final class YamlFiles {
    private YamlFiles() {
    }

    public static @NonNull YamlConfiguration load(@NonNull File file) throws IOException, InvalidConfigurationException {
        YamlConfiguration configuration = newConfiguration();
        if (!file.exists()) {
            return configuration;
        }

        configuration.loadFromString(Files.readString(file.toPath(), StandardCharsets.UTF_8));
        return configuration;
    }

    public static @NonNull YamlConfiguration load(@NonNull Reader reader) throws IOException, InvalidConfigurationException {
        YamlConfiguration configuration = newConfiguration();
        StringWriter writer = new StringWriter();
        reader.transferTo(writer);
        configuration.loadFromString(writer.toString());
        return configuration;
    }

    public static @NonNull YamlConfiguration load(@NonNull InputStream inputStream) throws IOException, InvalidConfigurationException {
        try (InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
            return load(reader);
        }
    }

    private static @NonNull YamlConfiguration newConfiguration() {
        YamlConfiguration configuration = new YamlConfiguration();
        configuration.options().parseComments(true);
        return configuration;
    }
}
