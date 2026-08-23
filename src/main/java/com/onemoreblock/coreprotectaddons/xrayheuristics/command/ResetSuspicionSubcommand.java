package com.onemoreblock.coreprotectaddons.xrayheuristics.command;

import com.onemoreblock.coreprotectaddons.xrayheuristics.XRayHeuristicsModule;
import com.onemoreblock.coreprotectaddons.xrayheuristics.manager.LocaleManager;
import com.onemoreblock.coreprotectaddons.xrayheuristics.manager.PlaceholderManager;
import com.onemoreblock.coreprotectaddons.xrayheuristics.util.MiningSession;
import java.util.Objects;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ResetSuspicionSubcommand {
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacyAmpersand();
    private static final String ADMIN_PERMISSION = "xrayheuristics.admin";

    public static void execute(CommandSender sender, XRayHeuristicsModule module) { // Non-parametrized
        if (sender instanceof Player player) {
            if (player.hasPermission(ADMIN_PERMISSION) || player.hasPermission("AXH.Commands.ResetSuspicion")) {
                MiningSession tempMS = module.sessions.get(player.getName());
                if (tempMS != null) {
                    module.sessions.remove(player.getName());
                    String prefix = LocaleManager.get().getString("MessagesPrefix");
                    String message = LocaleManager.get().getString("OwnSuspicionNullified");
                    Component comp = LEGACY_SERIALIZER.deserialize(Objects.requireNonNull(prefix)).append(Component.space()).append(LEGACY_SERIALIZER.deserialize(Objects.requireNonNull(message)));
                    player.sendMessage(comp);
                } else {
                    String prefix = LocaleManager.get().getString("MessagesPrefix");
                    String message = LocaleManager.get().getString("NoOwnSuspicionReset");
                    Component comp = LEGACY_SERIALIZER.deserialize(Objects.requireNonNull(prefix)).append(Component.space()).append(LEGACY_SERIALIZER.deserialize(Objects.requireNonNull(message)));
                    player.sendMessage(comp);
                }
            } else {
                Component noPerm = LEGACY_SERIALIZER.deserialize(Objects.requireNonNull(LocaleManager.get().getString("NoPermissionForCommand")));
                player.sendMessage(noPerm);
            }
        } else {
            // Console sender
            Component playerOnly = LEGACY_SERIALIZER.deserialize(Objects.requireNonNull(LocaleManager.get().getString("PlayerOnlyCommand")));
            sender.sendMessage(playerOnly);
        }
    }

    public static void execute(CommandSender sender, String arg, XRayHeuristicsModule module) { // Parametrized
        if (sender instanceof Player player) {
            if (player.hasPermission(ADMIN_PERMISSION) || player.hasPermission("AXH.Commands.ResetSuspicion")) {
                MiningSession tempMS = module.sessions.get(arg);
                if (tempMS != null) {
                    module.sessions.remove(arg);
                    String prefix = LocaleManager.get().getString("MessagesPrefix");
                    String message = PlaceholderManager.SubstitutePlayerNameAndColorCodePlaceholders(LocaleManager.get().getString("PlayerSuspicionNullified"), arg);
                    Component comp = LEGACY_SERIALIZER.deserialize(Objects.requireNonNull(prefix)).append(Component.space()).append(LEGACY_SERIALIZER.deserialize(message));
                    player.sendMessage(comp);
                } else {
                    String prefix = LocaleManager.get().getString("MessagesPrefix");
                    String message = PlaceholderManager.SubstitutePlayerNameAndColorCodePlaceholders(LocaleManager.get().getString("NoPlayerSuspicionReset"), arg);
                    Component comp = LEGACY_SERIALIZER.deserialize(Objects.requireNonNull(prefix)).append(Component.space()).append(LEGACY_SERIALIZER.deserialize(message));
                    player.sendMessage(comp);
                }
            } else {
                Component noPerm = LEGACY_SERIALIZER.deserialize(Objects.requireNonNull(LocaleManager.get().getString("NoPermissionForCommand")));
                player.sendMessage(noPerm);
            }
        } else {
            // Console sender
            MiningSession tempMS = module.sessions.get(arg);
            String prefix = LocaleManager.get().getString("MessagesPrefix");
            if (tempMS != null) {
                module.sessions.remove(arg);
                String message = PlaceholderManager.SubstitutePlayerNameAndColorCodePlaceholders(LocaleManager.get().getString("PlayerSuspicionNullified"), arg);
                Component comp = LEGACY_SERIALIZER.deserialize(Objects.requireNonNull(prefix)).append(Component.space()).append(LEGACY_SERIALIZER.deserialize(message));
                sender.sendMessage(comp);
            } else {
                String message = PlaceholderManager.SubstitutePlayerNameAndColorCodePlaceholders(LocaleManager.get().getString("NoPlayerSuspicionReset"), arg);
                Component comp = LEGACY_SERIALIZER.deserialize(Objects.requireNonNull(prefix)).append(Component.space()).append(LEGACY_SERIALIZER.deserialize(message));
                sender.sendMessage(comp);
            }
        }
    }
}
