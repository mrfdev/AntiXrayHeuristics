package com.greymagic27.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Properties;
import org.junit.jupiter.api.Test;

class BuildMetadataTest {

    @Test
    void loadsCompleteGeneratedMetadata() {
        BuildMetadata metadata = BuildMetadata.fromProperties(validProperties());

        assertEquals("2.0.1-029-j25-26.2", metadata.artifactVersion());
        assertEquals("26.2.build.84-stable", metadata.paperApi());
        assertEquals("stable", metadata.paperApiChannel());
        assertEquals(25, metadata.javaTarget());
        assertEquals("24.0-dev1 (API 12)", metadata.coreProtectTarget());
    }

    @Test
    void rejectsMissingReleaseMetadata() {
        Properties properties = validProperties();
        properties.remove("paperApi");

        assertThrows(IllegalStateException.class, () -> BuildMetadata.fromProperties(properties));
    }

    private static Properties validProperties() {
        Properties properties = new Properties();
        properties.setProperty("pluginVersion", "2.0.1");
        properties.setProperty("buildNumber", "029");
        properties.setProperty("artifactVersion", "2.0.1-029-j25-26.2");
        properties.setProperty("javaTarget", "25");
        properties.setProperty("paperVersion", "26.2");
        properties.setProperty("paperApi", "26.2.build.84-stable");
        properties.setProperty("paperApiBuild", "84");
        properties.setProperty("paperApiChannel", "stable");
        properties.setProperty("declaredApiVersion", "1.21.11");
        properties.setProperty("coreProtectVersion", "24.0-dev1");
        properties.setProperty("coreProtectApiVersion", "12");
        return properties;
    }
}
