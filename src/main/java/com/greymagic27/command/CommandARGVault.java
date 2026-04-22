package com.greymagic27.command;

import com.greymagic27.AntiXrayHeuristics;
import com.greymagic27.manager.LocaleManager;
import java.util.Objects;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandARGVault {
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacyAmpersand();
    private static final String ADMIN_PERMISSION = "xrayheuristics.admin";

    static void V(CommandSender sender, AntiXrayHeuristics mainClass) {
        if (sender instanceof Player player) {
            if (player.hasPermission(ADMIN_PERMISSION) || player.hasPermission("AXH.Commands.Vault")) {
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
