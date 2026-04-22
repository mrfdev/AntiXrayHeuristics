package com.greymagic27.command;

import com.greymagic27.AntiXrayHeuristics;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

public class CommandAXHAutoCompleter implements TabCompleter {
    private final AntiXrayHeuristics plugin;

    public CommandAXHAutoCompleter(@NonNull AntiXrayHeuristics plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(
            @NonNull CommandSender sender,
            @NonNull Command command,
            @NonNull String alias,
            String @NonNull [] args
    ) {
        if (args.length == 1) {
            List<String> suggestions = new ArrayList<>(List.of(
                    "help",
                    "debug",
                    "reload",
                    "suspicion",
                    "resetsuspicion",
                    "vault",
                    "absolve",
                    "purge"
            ));
            suggestions.addAll(Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .collect(Collectors.toCollection(ArrayList::new)));
            suggestions.addAll(plugin.sessions.keySet());
            return filterSuggestions(suggestions, args[0]);
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("debug")) {
            return filterSuggestions(List.of("help", "permissions", "commands", "config", "set"), args[1]);
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("debug") && args[1].equalsIgnoreCase("set")) {
            return filterSuggestions(CommandARGDebug.getConfigKeys(), args[2]);
        }

        if (args.length == 4 && args[0].equalsIgnoreCase("debug") && args[1].equalsIgnoreCase("set")) {
            return filterSuggestions(CommandARGDebug.getValueSuggestions(args[2].toLowerCase(Locale.ROOT)), args[3]);
        }

        if (args.length == 2 && needsPlayerName(args[0])) {
            List<String> playerNames = Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .collect(Collectors.toCollection(ArrayList::new));
            playerNames.addAll(plugin.sessions.keySet());
            return filterSuggestions(playerNames, args[1]);
        }

        return List.of();
    }

    private boolean needsPlayerName(@NonNull String subcommand) {
        return subcommand.equalsIgnoreCase("suspicion")
                || subcommand.equalsIgnoreCase("resetsuspicion")
                || subcommand.equalsIgnoreCase("xrayer")
                || subcommand.equalsIgnoreCase("absolve")
                || subcommand.equalsIgnoreCase("purge");
    }

    private @NonNull List<String> filterSuggestions(@NonNull List<String> suggestions, @NonNull String input) {
        String loweredInput = input.toLowerCase(Locale.ROOT);
        return suggestions.stream()
                .filter(suggestion -> suggestion.toLowerCase(Locale.ROOT).startsWith(loweredInput))
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }
}
