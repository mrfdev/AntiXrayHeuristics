package com.greymagic27;

import java.util.Objects;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

class CommandARGXrayer {
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacyAmpersand();

    public static void X(CommandSender sender, @NotNull AntiXrayHeuristics mainClass) { // Non-parametrized
        if (mainClass.getConfig().getBoolean("AddRandomDummyXrayerIfNoXrayerCommandParameters")) {
            if (sender instanceof Player player) {
                if (player.hasPermission("AXH.Commands.Xrayer")) {
                    XrayerHandler.AddDummyXrayer();
                } else {
                    Component noPerm = LEGACY_SERIALIZER.deserialize(Objects.requireNonNull(LocaleManager.get().getString("NoPermissionForCommand")));
                    player.sendMessage(noPerm);
                }
            } else { // Console
                XrayerHandler.AddDummyXrayer();
            }
        }
    }

    public static void X(CommandSender sender, String arg) { // Parametrized
        if (sender instanceof Player player) {
            if (player.hasPermission("AXH.Commands.Xrayer")) {
                XrayerHandler.HandleXrayer(arg);
            } else {
                Component noPerm = LEGACY_SERIALIZER.deserialize(Objects.requireNonNull(LocaleManager.get().getString("NoPermissionForCommand")));
                player.sendMessage(noPerm);
            }
        } else { // Console
            XrayerHandler.HandleXrayer(arg);
        }
    }
}
