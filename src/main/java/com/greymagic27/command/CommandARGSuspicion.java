//--------------------------------------------------------------------
// Copyright © Dylan Calaf Latham 2019-2021 AntiXrayHeuristics
//--------------------------------------------------------------------

package com.greymagic27.command;

import com.greymagic27.AntiXrayHeuristics;
import com.greymagic27.manager.LocaleManager;
import com.greymagic27.util.MiningSession;
import java.util.Objects;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandARGSuspicion {
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacyAmpersand();
    private static final String USE_PERMISSION = "xrayheuristics.use";

    public static void S(CommandSender sender, AntiXrayHeuristics mainClass) {//Non-parametrized
        if (sender instanceof Player player) { //Is player
            if (!player.hasPermission("AXH.Commands.Suspicion") && !player.hasPermission(USE_PERMISSION)) {
                Component noPerm = LEGACY_SERIALIZER.deserialize(Objects.requireNonNull(LocaleManager.get().getString("NoPermissionForCommand")));
                player.sendMessage(noPerm);
                return;
            }
            MiningSession tempMS = mainClass.sessions.get(player.getName());
            if (tempMS != null) player.sendMessage("Your suspicion level: " + tempMS.GetSuspicionLevel());
            else player.sendMessage("You are not suspicious of Xray usage. No suspicion level available.");
        } else {
            Component playerOnly = LEGACY_SERIALIZER.deserialize(Objects.requireNonNull(LocaleManager.get().getString("PlayerOnlyCommand")));
            sender.sendMessage(playerOnly);
        }
    }

    public static void S(CommandSender sender, String arg, AntiXrayHeuristics mainClass) //Parametrized
    {
        if (sender instanceof Player player) { //Is player
            if (!player.hasPermission("AXH.Commands.Suspicion") && !player.hasPermission(USE_PERMISSION)) {
                Component noPerm = LEGACY_SERIALIZER.deserialize(Objects.requireNonNull(LocaleManager.get().getString("NoPermissionForCommand")));
                player.sendMessage(noPerm);
                return;
            }
            MiningSession tempMS = mainClass.sessions.get(arg);
            if (tempMS != null)
                player.sendMessage(arg + "'s suspicion level: " + tempMS.GetSuspicionLevel());
            else
                player.sendMessage(arg + " Is not suspicious of Xray usage. No suspicion level available.");
        } else { //Is console
            MiningSession tempMS = mainClass.sessions.get(arg);
            if (tempMS != null)
                sender.sendMessage(arg + "'s suspicion level: " + tempMS.GetSuspicionLevel());
            else
                sender.sendMessage(arg + " Is not suspicious of Xray usage. No suspicion level available.");
        }
    }
}
