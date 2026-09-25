package com.onemoreblock.coreprotectaddons.xrayheuristics.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.onemoreblock.coreprotectaddons.xrayheuristics.util.BuildMetadata;
import java.util.List;
import org.junit.jupiter.api.Test;

class InfoSubcommandTest {

    @Test
    void infoLinesIncludePublicQuickStartAndVersion() {
        List<String> lines = InfoSubcommand.buildInfoLines(
                metadata(),
                "xrayheuristics",
                false
        );

        assertTrue(lines.stream().anyMatch(line -> line.contains("1MB XRayHeuristics / xrayer")), lines.toString());
        assertTrue(lines.stream().anyMatch(line -> line.contains("/xrayer info")), lines.toString());
        assertTrue(lines.stream().anyMatch(line -> line.contains("/xrayer help")), lines.toString());
        assertTrue(lines.stream().anyMatch(line -> line.contains("2.0.4-032-j25-26.3")), lines.toString());
        assertTrue(lines.stream().anyMatch(line -> line.contains("26.3.build.41-alpha")), lines.toString());
        assertTrue(lines.stream().anyMatch(line -> line.contains("Java target: &f25")), lines.toString());
        assertTrue(lines.stream().anyMatch(line -> line.contains("xrayheuristics")), lines.toString());
        assertEquals("https://docs.1moreblock.com/custom-server-plugins/xrayheuristics/", InfoSubcommand.DOCS_URL);
    }

    @Test
    void infoLinesShowAdminDebugHintOnlyWhenAdminAccessExists() {
        List<String> adminLines = InfoSubcommand.buildInfoLines(
                metadata(),
                "xrayheuristics",
                true
        );
        List<String> publicLines = InfoSubcommand.buildInfoLines(
                metadata(),
                "xrayheuristics",
                false
        );

        assertTrue(adminLines.stream().anyMatch(line -> line.contains("/xrayer debug")), adminLines.toString());
        assertTrue(publicLines.stream().anyMatch(line -> line.contains("require admin permission")), publicLines.toString());
    }

    private static BuildMetadata metadata() {
        return new BuildMetadata(
                "2.0.4",
                "032",
                "2.0.4-032-j25-26.3",
                25,
                "26.3",
                "26.3.build.41-alpha",
                41,
                "alpha",
                "26.3",
                "25.0",
                13
        );
    }
}
