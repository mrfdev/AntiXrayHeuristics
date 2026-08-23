package com.onemoreblock.coreprotectaddons.xrayheuristics.command;

import com.onemoreblock.coreprotectaddons.xrayheuristics.XRayHeuristicsModule;
import com.onemoreblock.coreprotectaddons.xrayheuristics.manager.LocaleManager;
import java.util.Locale;
import java.util.Objects;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jspecify.annotations.NonNull;

public class XrayerCommand implements CommandExecutor {
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacyAmpersand();
    private final XRayHeuristicsModule module;

    public XrayerCommand(XRayHeuristicsModule main) {
        this.module = main;
    }

    @Override
    public boolean onCommand(
            @NonNull CommandSender sender,
            @NonNull Command command,
            @NonNull String label,
            String @NonNull [] args
    ) {
        if (args.length == 0 || isHelpCommand(args[0])) {
            HelpSubcommand.sendHelp(sender, module);
            return true;
        }

        String subcommand = args[0].toLowerCase(Locale.ROOT);
        if (subcommand.equals("info")) {
            InfoSubcommand.sendInfo(sender, module);
            return true;
        }
        if (subcommand.equals("debug")) {
            return DebugSubcommand.handle(sender, args, module);
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
            case "info" -> {
                InfoSubcommand.sendInfo(sender, module);
                yield true;
            }
            case "xrayer", "x" -> {
                HandlePlayerSubcommand.execute(sender, module);
                yield true;
            }
            case "vault", "v" -> {
                VaultSubcommand.execute(sender, module);
                yield true;
            }
            case "reload", "r" -> {
                ReloadSubcommand.execute(sender, module);
                yield true;
            }
            case "resetsuspicion", "rs" -> {
                ResetSuspicionSubcommand.execute(sender, module);
                yield true;
            }
            case "suspicion", "s" -> {
                SuspicionSubcommand.execute(sender, module);
                yield true;
            }
            default -> {
                HandlePlayerSubcommand.execute(sender, subcommand, module);
                yield true;
            }
        };
    }

    private boolean handleDoubleArgument(@NonNull CommandSender sender, @NonNull String subcommand, @NonNull String target) {
        return switch (subcommand) {
            case "xrayer", "x" -> {
                HandlePlayerSubcommand.execute(sender, target, module);
                yield true;
            }
            case "absolve", "a" -> {
                AbsolveSubcommand.execute(sender, target, module);
                yield true;
            }
            case "purge", "p" -> {
                PurgeSubcommand.execute(sender, target, module);
                yield true;
            }
            case "resetsuspicion", "rs" -> {
                ResetSuspicionSubcommand.execute(sender, target, module);
                yield true;
            }
            case "suspicion", "s" -> {
                SuspicionSubcommand.execute(sender, target, module);
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
