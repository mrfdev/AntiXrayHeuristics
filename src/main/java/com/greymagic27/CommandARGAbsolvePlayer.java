package com.greymagic27;

import java.util.Objects;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandARGAbsolvePlayer {
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacyAmpersand();

    public static void A(CommandSender sender, String arg, AntiXrayHeuristics mainClass) {
        if (sender instanceof Player player) //Is player
        {
            if (player.hasPermission("AXH.Commands.AbsolvePlayer")) {
                Player target = Bukkit.getServer().getPlayer(arg);
                if (target != null) { //Player online
                    final String targetUUID = target.getUniqueId().toString();
                    Bukkit.getScheduler().runTaskAsynchronously(mainClass, () -> mainClass.mm.GetXrayerBelongings(targetUUID, belongings -> {
                        if (XrayerHandler.PlayerAbsolver(targetUUID, belongings, mainClass)) {
                            Component msgPrefix = LEGACY_SERIALIZER.deserialize(Objects.requireNonNull(LocaleManager.get().getString("MessagesPrefix")));
                            Component msgAbsolved = LEGACY_SERIALIZER.deserialize(Objects.requireNonNull(LocaleManager.get().getString("PlayerAbsolved")));
                            sender.sendMessage(msgPrefix.append(Component.text(" ")).append(msgAbsolved));
                            // purge player from database:
                            mainClass.vault.XrayerDataRemover(arg, false);
                        } else {
                            Component msgPrefix = LEGACY_SERIALIZER.deserialize(Objects.requireNonNull(LocaleManager.get().getString("MessagesPrefix")));
                            Component msgNotOnline = LEGACY_SERIALIZER.deserialize(Objects.requireNonNull(LocaleManager.get().getString("PlayerNotOnlineOnAbsolution")));
                            sender.sendMessage(msgPrefix.append(Component.text(" ")).append(msgNotOnline));
                        }
                    }));
                } else {
                    Component msgPrefix = LEGACY_SERIALIZER.deserialize(Objects.requireNonNull(LocaleManager.get().getString("MessagesPrefix")));
                    Component msgNotOnline = LEGACY_SERIALIZER.deserialize(Objects.requireNonNull(LocaleManager.get().getString("PlayerNotOnlineOnAbsolution")));
                    sender.sendMessage(msgPrefix.append(Component.text(" ")).append(msgNotOnline));
                }
            } else {
                Component msgNoPermission = LEGACY_SERIALIZER.deserialize(Objects.requireNonNull(LocaleManager.get().getString("NoPermissionForCommand")));
                sender.sendMessage(msgNoPermission);
            }
        } else { // Is console
            Player target = Bukkit.getServer().getPlayer(arg);
            if (target != null) { // Player online
                final String targetUUID = target.getUniqueId().toString();
                Bukkit.getScheduler().runTaskAsynchronously(mainClass, () -> mainClass.mm.GetXrayerBelongings(targetUUID, belongings -> {
                    if (XrayerHandler.PlayerAbsolver(targetUUID, belongings, mainClass)) {
                        mainClass.vault.XrayerDataRemover(arg, false);
                    } else {
                        Component msgPrefix = LEGACY_SERIALIZER.deserialize(Objects.requireNonNull(LocaleManager.get().getString("MessagesPrefix")));
                        Component msgNotOnline = LEGACY_SERIALIZER.deserialize(Objects.requireNonNull(LocaleManager.get().getString("PlayerNotOnlineOnAbsolution")));
                        Bukkit.getConsoleSender().sendMessage(msgPrefix.append(Component.text(" ")).append(msgNotOnline));
                    }
                }));
            } else {
                Component msgPrefix = LEGACY_SERIALIZER.deserialize(Objects.requireNonNull(LocaleManager.get().getString("MessagesPrefix")));
                Component msgNotOnline = LEGACY_SERIALIZER.deserialize(Objects.requireNonNull(LocaleManager.get().getString("PlayerNotOnlineOnAbsolution")));
                Bukkit.getConsoleSender().sendMessage(msgPrefix.append(Component.text(" ")).append(msgNotOnline));
            }
        }
    }
}
