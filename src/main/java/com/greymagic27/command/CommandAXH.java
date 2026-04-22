package com.greymagic27.command;

import com.greymagic27.AntiXrayHeuristics;
import com.greymagic27.manager.LocaleManager;
import java.util.Locale;
import java.util.Objects;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jspecify.annotations.NonNull;

public class CommandAXH implements CommandExecutor {
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacyAmpersand();
    private final AntiXrayHeuristics mainClassAccess;

    public CommandAXH(AntiXrayHeuristics main) {
        this.mainClassAccess = main;
    }

    @Override
    public boolean onCommand(
            @NonNull CommandSender sender,
            @NonNull Command command,
            @NonNull String label,
            String @NonNull [] args
    ) {
        if (args.length == 0 || isHelpCommand(args[0])) {
            CommandARGHelp.sendHelp(sender, mainClassAccess);
            return true;
        }

        String subcommand = args[0].toLowerCase(Locale.ROOT);
        if (subcommand.equals("debug")) {
            return CommandARGDebug.handle(sender, args, mainClassAccess);
        }

        if (args.length == 1) {
            return handleSingleArgument(sender, subcommand);
        }

        if (args.length == 2) {
            return handleDoubleArgument(sender, subcommand, args[1]);
        }

        sendInvalidArgument(sender);
        return true;
    }

    private boolean handleSingleArgument(@NonNull CommandSender sender, @NonNull String subcommand) {
        return switch (subcommand) {
            case "xrayer", "x" -> {
                CommandARGXrayer.X(sender, mainClassAccess);
                yield true;
            }
            case "vault", "v" -> {
                CommandARGVault.V(sender, mainClassAccess);
                yield true;
            }
            case "reload", "r" -> {
                CommandARGReload.R(sender, mainClassAccess);
                yield true;
            }
            case "resetsuspicion", "rs" -> {
                CommandARGResetSuspicion.RS(sender, mainClassAccess);
                yield true;
            }
            case "suspicion", "s" -> {
                CommandARGSuspicion.S(sender, mainClassAccess);
                yield true;
            }
            default -> {
                CommandARGXrayer.X(sender, subcommand);
                yield true;
            }
        };
    }

    private boolean handleDoubleArgument(@NonNull CommandSender sender, @NonNull String subcommand, @NonNull String target) {
        return switch (subcommand) {
            case "xrayer", "x" -> {
                CommandARGXrayer.X(sender, target);
                yield true;
            }
            case "absolve", "a" -> {
                CommandARGAbsolvePlayer.A(sender, target, mainClassAccess);
                yield true;
            }
            case "purge", "p" -> {
                CommandARGPurgePlayer.P(sender, target, mainClassAccess);
                yield true;
            }
            case "resetsuspicion", "rs" -> {
                CommandARGResetSuspicion.RS(sender, target, mainClassAccess);
                yield true;
            }
            case "suspicion", "s" -> {
                CommandARGSuspicion.S(sender, target, mainClassAccess);
                yield true;
            }
            default -> {
                sendInvalidArgument(sender);
                yield true;
            }
        };
    }

    private boolean isHelpCommand(@NonNull String input) {
        return input.equalsIgnoreCase("help") || input.equalsIgnoreCase("?");
    }

    private void sendInvalidArgument(@NonNull CommandSender sender) {
        Component invalid = LEGACY_SERIALIZER.deserialize(
                Objects.requireNonNull(LocaleManager.get().getString("InvalidCMDArg"))
        );
        sender.sendMessage(invalid);
    }
}
