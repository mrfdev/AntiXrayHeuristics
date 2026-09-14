package com.onemoreblock.coreprotectaddons.xrayheuristics.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Properties;
import java.util.jar.JarFile;
import net.coreprotect.CoreProtectAPI;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

class CoreProtectCompatibilityTest {

    @Test
    void actualCoreProtectJarMatchesPackagedCompatibilityTarget() throws Exception {
        Properties metadata = new Properties();
        try (InputStream input = getClass().getResourceAsStream("/xrayheuristics/build-info.properties")) {
            assertNotNull(input);
            metadata.load(input);
        }

        Path jarPath = Path.of(CoreProtectAPI.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        try (JarFile jar = new JarFile(jarPath.toFile());
             InputStreamReader reader = new InputStreamReader(
                     jar.getInputStream(jar.getJarEntry("plugin.yml")), StandardCharsets.UTF_8)) {
            YamlConfiguration plugin = YamlConfiguration.loadConfiguration(reader);
            assertEquals("CoreProtect", plugin.getString("name"));
            assertEquals(metadata.getProperty("coreProtectVersion"), plugin.getString("version"),
                    "Compile and test against the CoreProtect release declared in version.properties");
        }

        assertEquals(Integer.parseInt(metadata.getProperty("coreProtectApiVersion")),
                new CoreProtectAPI().APIVersion(), "CoreProtect's actual API version must match the release metadata");
    }
}
