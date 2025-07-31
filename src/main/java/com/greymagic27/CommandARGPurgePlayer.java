package com.greymagic27;

import java.util.Objects;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

class CommandARGPurgePlayer {
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacyAmpersand();

    public static void P(CommandSender sender, String arg, AntiXrayHeuristics mainClass) {
        if (sender instanceof Player player) // Is player
        {
            if (player.hasPermission("AXH.Commands.PurgePlayer")) {
                Player target = Bukkit.getServer().getPlayer(arg);
                if (target != null) { // Player online
                    mainClass.vault.XrayerDataRemover(arg, false);
                    Component msgPurged = LEGACY_SERIALIZER.deserialize(Objects.requireNonNull(LocaleManager.get().getString("PlayerDataPurged")));
                    sender.sendMessage(msgPurged);
                } else {
                    Component prefix = LEGACY_SERIALIZER.deserialize(Objects.requireNonNull(LocaleManager.get().getString("MessagesPrefix")));
                    Component limitMsg = LEGACY_SERIALIZER.deserialize(Objects.requireNonNull(LocaleManager.get().getString("PurgeCommandLimit")));
                    sender.sendMessage(prefix.append(Component.text(" ")).append(limitMsg));
                }
            } else {
                Component noPermMsg = LEGACY_SERIALIZER.deserialize(Objects.requireNonNull(LocaleManager.get().getString("NoPermissionForCommand")));
                sender.sendMessage(noPermMsg);
            }
        } else { // Is console
            Player target = Bukkit.getServer().getPlayer(arg);
            if (target != null) {
                mainClass.vault.XrayerDataRemover(arg, false);
            } else {
                Component prefix = LEGACY_SERIALIZER.deserialize(Objects.requireNonNull(LocaleManager.get().getString("MessagesPrefix")));
                Component limitMsg = LEGACY_SERIALIZER.deserialize(Objects.requireNonNull(LocaleManager.get().getString("PurgeCommandLimit")));
                Bukkit.getConsoleSender().sendMessage(prefix.append(Component.text(" ")).append(limitMsg));
            }
        }
    }
}
