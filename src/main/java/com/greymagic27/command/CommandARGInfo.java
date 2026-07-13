package com.greymagic27.command;

import com.greymagic27.AntiXrayHeuristics;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.jspecify.annotations.NonNull;

public final class CommandARGInfo {
    static final String ROOT = "xrayer";
    static final String PLAYER_FACING_NAME = "1MB XRayHeuristics";
    static final String DOCS_URL = "https://docs.1moreblock.com/custom-server-plugins/xrayheuristics/";

    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacyAmpersand();
    private static final String SHORT_INTRO =
            "Heuristic Anti-XRay add-on for 1MoreBlock that watches suspicious mining patterns instead of lookup reports.";

    private CommandARGInfo() {
    }

    public static void sendInfo(@NonNull CommandSender sender, @NonNull AntiXrayHeuristics plugin) {
        for (String line : buildInfoLines(
                plugin.getPluginMeta().getVersion(),
                plugin.getPluginMeta().getAPIVersion(),
                plugin.getPluginMeta().getName(),
                CommandARGHelp.hasAdminAccess(sender)
        )) {
            sender.sendMessage(LEGACY_SERIALIZER.deserialize(line));
        }
        sender.sendMessage(buildDocsLine());
        sender.sendMessage(LEGACY_SERIALIZER.deserialize("&8&m------------------------------------------------"));
    }

    static @NonNull List<String> buildInfoLines(
            @NonNull String version,
            @NonNull String declaredApiVersion,
            @NonNull String pluginId,
            boolean adminAccess
    ) {
        List<String> lines = new ArrayList<>();
        lines.add("&8&m------------------------------------------------");
        lines.add("&d&l" + PLAYER_FACING_NAME + " / " + ROOT);
        lines.add("&7" + SHORT_INTRO);
        lines.add("&7This plugin is primarily staff-facing. Regular players usually only need this info page or the help page.");
        lines.add("&7Installed version: &f" + version);
        lines.add("&7Plugin id: &f" + pluginId);
        lines.add("&7Declared API floor: &f" + declaredApiVersion);
        lines.add("&7Quick start:");
        lines.add("&f/" + ROOT + " info &7- Reopen this introduction and docs page.");
        lines.add("&f/" + ROOT + " help &7- Show the full command list.");
        lines.add("&f/" + ROOT + " suspicion [player] &7- Check a live suspicion session when you have permission.");
        if (adminAccess) {
            lines.add("&f/" + ROOT + " debug &7- Verify CoreProtect hook, config, storage, and build status.");
        } else {
            lines.add("&7Staff tools such as &f/" + ROOT + " debug &7and &f/" + ROOT + " vault &7require admin permission.");
        }
        return lines;
    }

    private static @NonNull Component buildDocsLine() {
        return Component.text("Docs: ", NamedTextColor.GRAY)
                .append(
                        Component.text(DOCS_URL, NamedTextColor.AQUA)
                                .decorate(TextDecoration.UNDERLINED)
                                .clickEvent(ClickEvent.openUrl(DOCS_URL))
                                .hoverEvent(HoverEvent.showText(Component.text("Open the canonical XRayHeuristics documentation.")))
                );
    }
}
