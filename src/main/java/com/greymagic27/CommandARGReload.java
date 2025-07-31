package com.greymagic27;

import java.util.Objects;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

class CommandARGReload {
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacyAmpersand();

    public static void R(CommandSender sender, AntiXrayHeuristics mainClass) {
        if (sender instanceof Player player) { // Is player
            if (player.hasPermission("AXH.Commands.Reload")) {
                // Do reload
                mainClass.reloadConfig(); // Reload main config
                LocaleManager.reload(); // Reload locale config
                WeightsCard.reload(); // Reload weights card config
                Component reloadedMsg = LEGACY_SERIALIZER.deserialize(Objects.requireNonNull(LocaleManager.get().getString("Reloaded")));
                player.sendMessage(reloadedMsg);
            } else {
                Component noPermMsg = LEGACY_SERIALIZER.deserialize(Objects.requireNonNull(LocaleManager.get().getString("NoPermissionForCommand")));
                player.sendMessage(noPermMsg);
            }
        } else { // Is console
            mainClass.reloadConfig();
            LocaleManager.reload();
            WeightsCard.reload();
            Component reloadedMsg = LEGACY_SERIALIZER.deserialize(Objects.requireNonNull(LocaleManager.get().getString("Reloaded")));
            Bukkit.getConsoleSender().sendMessage(reloadedMsg);
        }
    }
}
