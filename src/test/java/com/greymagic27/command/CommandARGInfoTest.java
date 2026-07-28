package com.greymagic27.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.greymagic27.util.BuildMetadata;
import java.util.List;
import org.junit.jupiter.api.Test;

class CommandARGInfoTest {

    @Test
    void infoLinesIncludePublicQuickStartAndVersion() {
        List<String> lines = CommandARGInfo.buildInfoLines(
                metadata(),
                "xrayheuristics",
                false
        );

        assertTrue(lines.stream().anyMatch(line -> line.contains("1MB XRayHeuristics / xrayer")), lines.toString());
        assertTrue(lines.stream().anyMatch(line -> line.contains("/xrayer info")), lines.toString());
        assertTrue(lines.stream().anyMatch(line -> line.contains("/xrayer help")), lines.toString());
        assertTrue(lines.stream().anyMatch(line -> line.contains("2.0.1-029-j25-26.2")), lines.toString());
        assertTrue(lines.stream().anyMatch(line -> line.contains("26.2.build.84-stable")), lines.toString());
        assertTrue(lines.stream().anyMatch(line -> line.contains("Java target: &f25")), lines.toString());
        assertTrue(lines.stream().anyMatch(line -> line.contains("xrayheuristics")), lines.toString());
        assertEquals("https://docs.1moreblock.com/custom-server-plugins/xrayheuristics/", CommandARGInfo.DOCS_URL);
    }

    @Test
    void infoLinesShowAdminDebugHintOnlyWhenAdminAccessExists() {
        List<String> adminLines = CommandARGInfo.buildInfoLines(
                metadata(),
                "xrayheuristics",
                true
        );
        List<String> publicLines = CommandARGInfo.buildInfoLines(
                metadata(),
                "xrayheuristics",
                false
        );

        assertTrue(adminLines.stream().anyMatch(line -> line.contains("/xrayer debug")), adminLines.toString());
        assertTrue(publicLines.stream().anyMatch(line -> line.contains("require admin permission")), publicLines.toString());
    }

    private static BuildMetadata metadata() {
        return new BuildMetadata(
                "2.0.1",
                "029",
                "2.0.1-029-j25-26.2",
                25,
                "26.2",
                "26.2.build.84-stable",
                84,
                "stable",
                "1.21.11",
                "24.0-dev1",
                12
        );
    }
}
