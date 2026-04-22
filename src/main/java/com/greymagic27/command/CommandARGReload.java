package com.greymagic27.command;

import com.greymagic27.AntiXrayHeuristics;
import com.greymagic27.manager.LocaleManager;
import java.util.Objects;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandARGReload {
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacyAmpersand();
    private static final String ADMIN_PERMISSION = "xrayheuristics.admin";

    public static void R(CommandSender sender, AntiXrayHeuristics mainClass) {
        if (sender instanceof Player player) { // Is player
            if (player.hasPermission(ADMIN_PERMISSION) || player.hasPermission("AXH.Commands.Reload")) {
                mainClass.reloadPluginState();
                Component reloadedMsg = LEGACY_SERIALIZER.deserialize(Objects.requireNonNull(LocaleManager.get().getString("Reloaded")));
                player.sendMessage(reloadedMsg);
            } else {
                Component noPermMsg = LEGACY_SERIALIZER.deserialize(Objects.requireNonNull(LocaleManager.get().getString("NoPermissionForCommand")));
                player.sendMessage(noPermMsg);
            }
        } else { // Is console
            mainClass.reloadPluginState();
            Component reloadedMsg = LEGACY_SERIALIZER.deserialize(Objects.requireNonNull(LocaleManager.get().getString("Reloaded")));
            Bukkit.getConsoleSender().sendMessage(reloadedMsg);
        }
    }
}
