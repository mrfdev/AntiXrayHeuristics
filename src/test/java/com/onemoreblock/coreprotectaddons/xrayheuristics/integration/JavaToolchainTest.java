package com.onemoreblock.coreprotectaddons.xrayheuristics.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class JavaToolchainTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void workerAndForkedJavaUseSelectedOracle27() throws Exception {
        Path expectedHome = Path.of(System.getProperty("oneMb.expectedTestJavaHome")).toRealPath();
        assertEquals(27, Runtime.version().feature());
        assertEquals("Oracle Corporation", System.getProperty("java.vendor"));
        assertEquals(expectedHome, Path.of(System.getProperty("java.home")).toRealPath());
        assertEquals(expectedHome, Path.of(System.getenv("JAVA_HOME")).toRealPath());
        assertEquals(expectedHome.resolve("bin").toString(),
                System.getenv("PATH").split(java.io.File.pathSeparator)[0]);
        System.out.printf("Test worker: %s; %s; %s%n", System.getProperty("java.runtime.version"),
                System.getProperty("java.vendor"), expectedHome);

        Path log = temporaryDirectory.resolve("forked-java.log");
        Process child = new ProcessBuilder(expectedHome.resolve("bin/java").toString(),
                "-XshowSettings:properties", "-version")
                .redirectErrorStream(true).redirectOutput(log.toFile()).start();
        try {
            assertTrue(child.waitFor(30, TimeUnit.SECONDS), "Forked Java must finish");
            assertEquals(0, child.exitValue());
            String output = java.nio.file.Files.readString(log, StandardCharsets.UTF_8);
            assertTrue(output.contains("java.specification.version = 27"), output);
            assertTrue(output.contains("java.vendor = Oracle Corporation"), output);
            assertTrue(output.contains("java.home = " + expectedHome), output);
        } finally {
            if (child.isAlive()) {
                child.destroyForcibly().waitFor(10, TimeUnit.SECONDS);
            }
        }
    }
}
