package com.onemoreblock.coreprotectaddons.xrayheuristics.command;

import com.onemoreblock.coreprotectaddons.xrayheuristics.XRayHeuristicsModule;
import com.onemoreblock.coreprotectaddons.xrayheuristics.manager.LocaleManager;
import java.util.Objects;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PurgeSubcommand {
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacyAmpersand();
    private static final String ADMIN_PERMISSION = "xrayheuristics.admin";

    public static void execute(CommandSender sender, String arg, XRayHeuristicsModule module) {
        if (sender instanceof Player player) // Is player
        {
            if (player.hasPermission(ADMIN_PERMISSION) || player.hasPermission("AXH.Commands.PurgePlayer")) {
                Player target = Bukkit.getServer().getPlayer(arg);
                if (target != null) { // Player online
                    module.handledPlayerVault.XrayerDataRemover(arg, false);
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
                module.handledPlayerVault.XrayerDataRemover(arg, false);
            } else {
                Component prefix = LEGACY_SERIALIZER.deserialize(Objects.requireNonNull(LocaleManager.get().getString("MessagesPrefix")));
                Component limitMsg = LEGACY_SERIALIZER.deserialize(Objects.requireNonNull(LocaleManager.get().getString("PurgeCommandLimit")));
                Bukkit.getConsoleSender().sendMessage(prefix.append(Component.text(" ")).append(limitMsg));
            }
        }
    }
}
