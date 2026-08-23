package com.onemoreblock.coreprotectaddons.xrayheuristics.command;

import com.onemoreblock.coreprotectaddons.xrayheuristics.XRayHeuristicsModule;
import com.onemoreblock.coreprotectaddons.xrayheuristics.manager.LocaleManager;
import java.util.Objects;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

public class HandlePlayerSubcommand {
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacyAmpersand();
    private static final String ADMIN_PERMISSION = "xrayheuristics.admin";

    public static void execute(CommandSender sender, @NonNull XRayHeuristicsModule module) { // Non-parametrized
        if (module.getConfig().getBoolean("AddRandomDummyXrayerIfNoXrayerCommandParameters")) {
            if (sender instanceof Player player) {
                if (player.hasPermission(ADMIN_PERMISSION) || player.hasPermission("AXH.Commands.Xrayer")) {
                    module.getHandledPlayerService().addDummyPlayer();
                } else {
                    Component noPerm = LEGACY_SERIALIZER.deserialize(Objects.requireNonNull(LocaleManager.get().getString("NoPermissionForCommand")));
                    player.sendMessage(noPerm);
                }
            } else { // Console
                module.getHandledPlayerService().addDummyPlayer();
            }
        }
    }

    public static void execute(CommandSender sender, String arg, @NonNull XRayHeuristicsModule module) { // Parametrized
        if (sender instanceof Player player) {
            if (player.hasPermission(ADMIN_PERMISSION) || player.hasPermission("AXH.Commands.Xrayer")) {
                module.getHandledPlayerService().handlePlayer(arg);
            } else {
                Component noPerm = LEGACY_SERIALIZER.deserialize(Objects.requireNonNull(LocaleManager.get().getString("NoPermissionForCommand")));
                player.sendMessage(noPerm);
            }
        } else { // Console
            module.getHandledPlayerService().handlePlayer(arg);
        }
    }
}
