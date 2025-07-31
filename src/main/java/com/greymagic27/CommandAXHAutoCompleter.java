package com.greymagic27;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

public class CommandAXHAutoCompleter implements TabCompleter {
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String @NotNull [] args) {
        if (args.length == 1) {
            List<String> firstArguments = new ArrayList<>();

            firstArguments.add("vault");
            firstArguments.add("reload");
            firstArguments.add("resetsuspicion");
            firstArguments.add("xrayer");
            firstArguments.add("absolve");
            firstArguments.add("purge");
            firstArguments.add("suspicion");

            return firstArguments;
        }

        return null;
    }
}
