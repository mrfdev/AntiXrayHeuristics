package com.greymagic27;

import java.util.Objects;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class CommandAXH implements CommandExecutor {

    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacyAmpersand();
    private final AntiXrayHeuristics mainClassAccess;

    public CommandAXH(AntiXrayHeuristics main) {
        this.mainClassAccess = main;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("xrayer") || args[0].equalsIgnoreCase("x")) {
                CommandARGXrayer.X(sender, mainClassAccess);
            } else if (args[0].equalsIgnoreCase("vault") || args[0].equalsIgnoreCase("v")) {
                CommandARGVault.V(sender, mainClassAccess);
            } else if (args[0].equalsIgnoreCase("reload") || args[0].equalsIgnoreCase("r")) {
                CommandARGReload.R(sender, mainClassAccess);
            } else if (args[0].equalsIgnoreCase("resetsuspicion") || args[0].equalsIgnoreCase("rs")) {
                CommandARGResetSuspicion.RS(sender, mainClassAccess);
            } else if (args[0].equalsIgnoreCase("suspicion") || args[0].equalsIgnoreCase("s")) {
                CommandARGSuspicion.S(sender, mainClassAccess);
            } else {
                Component invalid = LEGACY_SERIALIZER.deserialize(Objects.requireNonNull(LocaleManager.get().getString("InvalidCMDArg")));
                sender.sendMessage(invalid);
            }
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("xrayer") || args[0].equalsIgnoreCase("x")) {
                CommandARGXrayer.X(sender, args[1]);
            } else if (args[0].equalsIgnoreCase("absolve") || args[0].equalsIgnoreCase("a")) {
                CommandARGAbsolvePlayer.A(sender, args[1], mainClassAccess);
            } else if (args[0].equalsIgnoreCase("purge") || args[0].equalsIgnoreCase("p")) {
                CommandARGPurgePlayer.P(sender, args[1], mainClassAccess);
            } else if (args[0].equalsIgnoreCase("resetsuspicion") || args[0].equalsIgnoreCase("rs")) {
                CommandARGResetSuspicion.RS(sender, args[1], mainClassAccess);
            } else if (args[0].equalsIgnoreCase("suspicion") || args[0].equalsIgnoreCase("s")) {
                CommandARGSuspicion.S(sender, args[1], mainClassAccess);
            } else {
                Component invalid = LEGACY_SERIALIZER.deserialize(Objects.requireNonNull(LocaleManager.get().getString("InvalidCMDArg")));
                sender.sendMessage(invalid);
            }
        } else {
            Component invalid = LEGACY_SERIALIZER.deserialize(Objects.requireNonNull(LocaleManager.get().getString("InvalidCMDArg")));
            sender.sendMessage(invalid);
        }
        return false;
    }
}
