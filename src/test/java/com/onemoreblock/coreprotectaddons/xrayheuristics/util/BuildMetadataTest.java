package com.onemoreblock.coreprotectaddons.xrayheuristics.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Properties;
import org.junit.jupiter.api.Test;

class BuildMetadataTest {

    @Test
    void loadsCompleteGeneratedMetadata() {
        BuildMetadata metadata = BuildMetadata.fromProperties(validProperties());

        assertEquals("2.0.5-033-j25-26.3", metadata.artifactVersion());
        assertEquals("26.3.build.175-stable", metadata.paperApi());
        assertEquals("stable", metadata.paperApiChannel());
        assertEquals(25, metadata.javaTarget());
        assertEquals("25.0 (API 13)", metadata.coreProtectTarget());
    }

    @Test
    void rejectsMissingReleaseMetadata() {
        Properties properties = validProperties();
        properties.remove("paperApi");

        assertThrows(IllegalStateException.class, () -> BuildMetadata.fromProperties(properties));
    }

    private static Properties validProperties() {
        Properties properties = new Properties();
        properties.setProperty("pluginVersion", "2.0.5");
        properties.setProperty("buildNumber", "033");
        properties.setProperty("artifactVersion", "2.0.5-033-j25-26.3");
        properties.setProperty("javaTarget", "25");
        properties.setProperty("paperVersion", "26.3");
        properties.setProperty("paperApi", "26.3.build.175-stable");
        properties.setProperty("paperApiBuild", "175");
        properties.setProperty("paperApiChannel", "stable");
        properties.setProperty("declaredApiVersion", "26.3");
        properties.setProperty("coreProtectVersion", "25.0");
        properties.setProperty("coreProtectApiVersion", "13");
        return properties;
    }
}
