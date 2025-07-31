package com.greymagic27;

import java.util.Objects;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandARGResetSuspicion {
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacyAmpersand();

    public static void RS(CommandSender sender, AntiXrayHeuristics mainClass) { // Non-parametrized
        if (sender instanceof Player player) {
            if (player.hasPermission("AXH.Commands.ResetSuspicion")) {
                MiningSession tempMS = mainClass.sessions.get(player.getName());
                if (tempMS != null) {
                    mainClass.sessions.remove(player.getName());
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
            System.out.println(LocaleManager.get().getString("PlayerOnlyCommand"));
        }
    }

    public static void RS(CommandSender sender, String arg, AntiXrayHeuristics mainClass) { // Parametrized
        if (sender instanceof Player player) {
            if (player.hasPermission("AXH.Commands.ResetSuspicion")) {
                MiningSession tempMS = mainClass.sessions.get(arg);
                if (tempMS != null) {
                    mainClass.sessions.remove(arg);
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
            MiningSession tempMS = mainClass.sessions.get(arg);
            String prefix = LocaleManager.get().getString("MessagesPrefix");
            if (tempMS != null) {
                mainClass.sessions.remove(arg);
                String message = PlaceholderManager.SubstitutePlayerNameAndColorCodePlaceholders(LocaleManager.get().getString("PlayerSuspicionNullified"), arg);
                Component comp = LEGACY_SERIALIZER.deserialize(Objects.requireNonNull(prefix)).append(Component.space()).append(LEGACY_SERIALIZER.deserialize(message));
                Bukkit.getConsoleSender().sendMessage(comp);
            } else {
                String message = PlaceholderManager.SubstitutePlayerNameAndColorCodePlaceholders(LocaleManager.get().getString("NoPlayerSuspicionReset"), arg);
                Component comp = LEGACY_SERIALIZER.deserialize(Objects.requireNonNull(prefix)).append(Component.space()).append(LEGACY_SERIALIZER.deserialize(message));
                Bukkit.getConsoleSender().sendMessage(comp);
            }
        }
    }
}
