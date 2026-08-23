package com.onemoreblock.coreprotectaddons.xrayheuristics.command;

import com.onemoreblock.coreprotectaddons.xrayheuristics.XRayHeuristicsModule;
import com.onemoreblock.coreprotectaddons.xrayheuristics.manager.LocaleManager;
import java.util.Objects;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AbsolveSubcommand {
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacyAmpersand();
    private static final String ADMIN_PERMISSION = "xrayheuristics.admin";

    public static void execute(CommandSender sender, String arg, XRayHeuristicsModule module) {
        if (sender instanceof Player player) //Is player
        {
            if (player.hasPermission(ADMIN_PERMISSION) || player.hasPermission("AXH.Commands.AbsolvePlayer")) {
                Player target = Bukkit.getServer().getPlayer(arg);
                if (target != null) { //Player online
                    final String targetUUID = target.getUniqueId().toString();
                    Bukkit.getScheduler().runTaskAsynchronously(module.getHostPlugin(), () -> module.handledPlayerStore.GetXrayerBelongings(targetUUID, belongings -> {
                        if (module.getHandledPlayerService().restoreBelongings(targetUUID, belongings)) {
                            Component msgPrefix = LEGACY_SERIALIZER.deserialize(Objects.requireNonNull(LocaleManager.get().getString("MessagesPrefix")));
                            Component msgAbsolved = LEGACY_SERIALIZER.deserialize(Objects.requireNonNull(LocaleManager.get().getString("PlayerAbsolved")));
                            sender.sendMessage(msgPrefix.append(Component.text(" ")).append(msgAbsolved));
                            // purge player from database:
                            module.handledPlayerVault.XrayerDataRemover(arg, false);
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
                Bukkit.getScheduler().runTaskAsynchronously(module.getHostPlugin(), () -> module.handledPlayerStore.GetXrayerBelongings(targetUUID, belongings -> {
                    if (module.getHandledPlayerService().restoreBelongings(targetUUID, belongings)) {
                        module.handledPlayerVault.XrayerDataRemover(arg, false);
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
