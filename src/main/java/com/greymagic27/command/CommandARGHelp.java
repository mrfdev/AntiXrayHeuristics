package com.greymagic27.command;

import com.greymagic27.AntiXrayHeuristics;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.jspecify.annotations.NonNull;

public final class CommandARGHelp {
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacyAmpersand();
    private static final String ROOT = "xrayer";
    private static final String PERMISSION_PREFIX = "xrayheuristics";

    private CommandARGHelp() {
    }

    public static void sendHelp(@NonNull CommandSender sender, @NonNull AntiXrayHeuristics plugin) {
        sendLine(sender, "&8&m------------------------------------------------");
        sendLine(sender, "&d&l1MB XRayHeuristics / " + ROOT);
        sendLine(sender, "&7Version: &f" + plugin.getPluginMeta().getVersion());
        sendLine(sender, "&7Plugin: &f" + plugin.getPluginMeta().getName());
        sendLine(sender, "&7CoreProtect target: &f23.4 (API 11)");
        sendLine(sender, "&7Use &f/" + ROOT + " debug &7for live status and hook information.");
        sendLine(sender, "&8&m------------------------------------------------");
        sendLine(sender, "&f/" + ROOT + " help &7- Show this help page.");
        sendLine(sender, "&f/" + ROOT + " suspicion [player] &7- Show current suspicion for yourself or a player.");
        if (hasAdminAccess(sender)) {
            sendLine(sender, "&f/" + ROOT + " debug &7- Show plugin, server, and CoreProtect status.");
            sendLine(sender, "&f/" + ROOT + " debug help &7- Show the available debug pages.");
            sendLine(sender, "&f/" + ROOT + " debug permissions &7- List permission nodes and what they do.");
            sendLine(sender, "&f/" + ROOT + " debug commands &7- List commands and quick usage notes.");
            sendLine(sender, "&f/" + ROOT + " debug config &7- Show the active config values exposed by debug.");
            sendLine(sender, "&f/" + ROOT + " debug set <key> <value> &7- Change a supported config value and reload it.");
            sendLine(sender, "&f/" + ROOT + " reload &7- Reload config, locale, weights, and CoreProtect hook state.");
            sendLine(sender, "&f/" + ROOT + " resetsuspicion [player] &7- Clear a tracked suspicion session.");
            sendLine(sender, "&f/" + ROOT + " <player> &7- Manually handle a player as xrayer.");
            sendLine(sender, "&f/" + ROOT + " vault &7- Open the handled-player vault GUI.");
            sendLine(sender, "&f/" + ROOT + " absolve <player> &7- Return stored items and remove the vault entry.");
            sendLine(sender, "&f/" + ROOT + " purge <player> &7- Remove the vault entry without returning items.");
        } else {
            sendLine(sender, "&7Admin-only: &f/" + ROOT + " debug&7, &f/" + ROOT + " reload&7, &f/" + ROOT + " <player>&7, &f/" + ROOT + " vault&7, &f/" + ROOT + " absolve&7, &f/" + ROOT + " purge&7.");
        }
        sendLine(sender, "&7Tracked ore families include deepslate variants, ancient debris, gilded blackstone, raw iron blocks, and raw copper blocks.");
        sendLine(sender, "&7Config placeholders: &f{PlayerName}&7, &f{TimesDetected}");
        sendLine(sender, "&8&m------------------------------------------------");
    }

    static boolean hasAdminAccess(@NonNull CommandSender sender) {
        return sender.hasPermission(PERMISSION_PREFIX + ".admin") || sender.hasPermission("AXH.Commands.Reload");
    }

    static void sendLine(@NonNull CommandSender sender, @NonNull String line) {
        Component component = LEGACY_SERIALIZER.deserialize(line);
        sender.sendMessage(component);
    }
}
