//--------------------------------------------------------------------
// Copyright © Dylan Calaf Latham 2019-2021 AntiXrayHeuristics
//--------------------------------------------------------------------

package com.greymagic27;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

class CommandARGPurgePlayer {
    public static void P(CommandSender sender, String arg, AntiXrayHeuristics mainClass) {
        if (sender instanceof Player player) //Is player
        {
            if (player.hasPermission("AXH.Commands.PurgePlayer")) {
                //purge xrayer from database:
                Player target = Bukkit.getServer().getPlayer(arg);
                if (target != null) { //Player online
                    mainClass.vault.XrayerDataRemover(arg, false);
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', LocaleManager.get().getString("PlayerDataPurged")));
                } else
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', LocaleManager.get().getString("MessagesPrefix")) + " " + ChatColor.translateAlternateColorCodes('&', LocaleManager.get().getString("PurgeCommandLimit")));
            } else
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', LocaleManager.get().getString("NoPermissionForCommand")));
        } else { //Is console
            Player target = Bukkit.getServer().getPlayer(arg);
            if (target != null) {
                mainClass.vault.XrayerDataRemover(arg, false);
            } else
                System.out.print(ChatColor.translateAlternateColorCodes('&', LocaleManager.get().getString("MessagesPrefix")) + " " + ChatColor.translateAlternateColorCodes('&', LocaleManager.get().getString("PurgeCommandLimit")));
        }
    }
}