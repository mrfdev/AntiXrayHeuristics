package com.greymagic27.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class CommandARGInfoTest {

    @Test
    void infoLinesIncludePublicQuickStartAndVersion() {
        List<String> lines = CommandARGInfo.buildInfoLines(
                "2.0.0-027-j25-26.2",
                "1.21.11",
                "xrayheuristics",
                false
        );

        assertTrue(lines.stream().anyMatch(line -> line.contains("1MB XRayHeuristics / xrayer")), lines.toString());
        assertTrue(lines.stream().anyMatch(line -> line.contains("/xrayer info")), lines.toString());
        assertTrue(lines.stream().anyMatch(line -> line.contains("/xrayer help")), lines.toString());
        assertTrue(lines.stream().anyMatch(line -> line.contains("2.0.0-027-j25-26.2")), lines.toString());
        assertTrue(lines.stream().anyMatch(line -> line.contains("xrayheuristics")), lines.toString());
        assertEquals("https://docs.1moreblock.com/custom-server-plugins/xrayheuristics/", CommandARGInfo.DOCS_URL);
    }

    @Test
    void infoLinesShowAdminDebugHintOnlyWhenAdminAccessExists() {
        List<String> adminLines = CommandARGInfo.buildInfoLines(
                "2.0.0-027-j25-26.2",
                "1.21.11",
                "xrayheuristics",
                true
        );
        List<String> publicLines = CommandARGInfo.buildInfoLines(
                "2.0.0-027-j25-26.2",
                "1.21.11",
                "xrayheuristics",
                false
        );

        assertTrue(adminLines.stream().anyMatch(line -> line.contains("/xrayer debug")), adminLines.toString());
        assertTrue(publicLines.stream().anyMatch(line -> line.contains("require admin permission")), publicLines.toString());
    }
}
