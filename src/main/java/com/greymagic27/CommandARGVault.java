package com.greymagic27;

import java.util.Objects;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;

class CommandARGVault {
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacyAmpersand();

    static void V(CommandSender sender, AntiXrayHeuristics mainClass) {
        if (sender instanceof Player player) {
            if (player.hasPermission("AXH.Commands.Vault")) {
                mainClass.vault.UpdateXrayerInfoLists(player, 0); // Update vault and open UI for player
            } else {
                Component noPerm = LEGACY_SERIALIZER.deserialize(Objects.requireNonNull(LocaleManager.get().getString("NoPermissionForCommand")));
                player.sendMessage(noPerm);
            }
        } else {
            // Console sender
            Component playerOnly = LEGACY_SERIALIZER.deserialize(Objects.requireNonNull(LocaleManager.get().getString("PlayerOnlyCommand")));
            Bukkit.getConsoleSender().sendMessage(playerOnly);
        }
    }
}
