package com.onemoreblock.coreprotectaddons.xrayheuristics.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.onemoreblock.coreprotectaddons.xrayheuristics.XRayHeuristicsModule;
import java.io.DataInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Properties;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

class PaperCompatibilityTest {
    @Test
    void actualApiDependencyAndManifestMatchPackagedStableTarget() throws Exception {
        Properties metadata = metadata();
        Path apiJar = Path.of(Bukkit.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        assertEquals("paper-api-" + metadata.getProperty("paperApi") + ".jar", apiJar.getFileName().toString());
        assertEquals("stable", metadata.getProperty("paperApiChannel"));
        try (InputStream input = getClass().getResourceAsStream("/plugin.yml")) {
            assertNotNull(input);
            YamlConfiguration manifest = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(input, StandardCharsets.UTF_8));
            assertEquals(metadata.getProperty("paperVersion"), manifest.getString("api-version"));
            assertEquals(metadata.getProperty("artifactVersion"), manifest.getString("version"));
            assertEquals("xrayheuristics", manifest.getString("name"));
        }
    }

    @Test
    void compiledFeatureRetainsJava25BytecodeWithoutPreviewFeatures() throws Exception {
        try (DataInputStream input = new DataInputStream(
                XRayHeuristicsModule.class.getResourceAsStream("XRayHeuristicsModule.class"))) {
            assertEquals(0xCAFEBABE, input.readInt());
            assertEquals(0, input.readUnsignedShort(), "Preview bytecode must not be required");
            assertEquals(69, input.readUnsignedShort(), "Java 25 class-file version");
        }
    }

    private Properties metadata() throws Exception {
        Properties metadata = new Properties();
        try (InputStream input = getClass().getResourceAsStream("/xrayheuristics/build-info.properties")) {
            assertNotNull(input);
            metadata.load(input);
        }
        return metadata;
    }
}
