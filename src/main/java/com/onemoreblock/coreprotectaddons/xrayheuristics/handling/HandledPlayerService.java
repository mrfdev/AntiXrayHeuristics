package com.onemoreblock.coreprotectaddons.xrayheuristics.handling;

import com.onemoreblock.coreprotectaddons.xrayheuristics.XRayHeuristicsModule;
import com.onemoreblock.coreprotectaddons.xrayheuristics.event.HandledPlayerEvent;
import com.onemoreblock.coreprotectaddons.xrayheuristics.manager.LocaleManager;
import com.onemoreblock.coreprotectaddons.xrayheuristics.manager.PlaceholderManager;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jspecify.annotations.NonNull;

/** Coordinates the configured workflow for handled players. */
public final class HandledPlayerService {
    private final XRayHeuristicsModule module;

    public HandledPlayerService(@NonNull XRayHeuristicsModule module) {
        this.module = Objects.requireNonNull(module, "module");
    }

    private void warnStaff(String playerName) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("xrayheuristics.notify") || player.hasPermission("AXH.XrayerWarning")) {
                String prefix = Objects.requireNonNull(LocaleManager.get().getString("MessagesPrefix"));
                String rawMsg = LocaleManager.get().getString("AutoHandledPlayer");
                String substituted = PlaceholderManager.SubstitutePlayerNameAndColorCodePlaceholders(rawMsg, playerName);
                Component message = LegacyComponentSerializer.legacyAmpersand().deserialize(prefix + " " + substituted);
                player.sendMessage(message);
            }
        }
    }

    public void handlePlayer(@NonNull String playerName) {
        Player player = Bukkit.getPlayer(playerName);

        HandledPlayerEvent ev = new HandledPlayerEvent(player);
        Bukkit.getServer().getPluginManager().callEvent(ev);

        if (!ev.isCancelled()) {
            if (player != null) {
                if (module.getConfig().getBoolean("SendMessageToPlayer")) {
                    String prefix = Objects.requireNonNull(LocaleManager.get().getString("MessagesPrefix"));
                    String playerMessageRaw = Objects.requireNonNull(LocaleManager.get().getString("PlayerMessageOnXray"));
                    Component playerMessage = LegacyComponentSerializer.legacyAmpersand().deserialize(prefix + " " + playerMessageRaw);
                    player.sendMessage(playerMessage);
                }

                Bukkit.getScheduler().runTaskAsynchronously(module.getHostPlugin(), () -> module.handledPlayerStore.StorePlayerData(player, handleTimes -> {

                    if (module.getConfig().getBoolean("UseHeadsInGUI")) {
                        module.handledPlayerVault.AddXrayerHeadToCache(player.getUniqueId());
                    }

                    if (module.shouldCleansePlayerItems()) {
                        try {
                            player.getInventory().clear();
                            player.getEquipment().clear();
                        } catch (Exception e) {
                            module.getLogger().warning("Failed to remove player " + playerName + "'s equipment while handling the player.");
                        }
                    }

                    ConfigurationSection section = module.getConfig().getConfigurationSection("CommandsExecutedOnXrayerDetected");
                    if (section != null && section.contains(String.valueOf(handleTimes))) {
                        ConfigurationSection subSection = module.getConfig().getConfigurationSection("CommandsExecutedOnXrayerDetected." + handleTimes);
                        if (subSection != null) {
                            Map<String, Object> commandsToExecute = subSection.getValues(false);
                            for (Map.Entry<String, Object> pair : commandsToExecute.entrySet()) {
                                String command = PlaceholderManager.SubstitutePlayerNameAndHandleTimesPlaceholders((String) pair.getValue(), playerName, Integer.toString(handleTimes));
                                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                            }
                        }
                    }
                }));

                if (module.shouldNullifySuspicionAfterPunish()) {
                    module.sessions.remove(player.getName());
                }

                // Console message using Adventure + legacy serializer
                String prefix = Objects.requireNonNull(LocaleManager.get().getString("MessagesPrefix"));
                String rawMsg = LocaleManager.get().getString("AutoHandledPlayer");
                String substituted = PlaceholderManager.SubstitutePlayerNameAndColorCodePlaceholders(rawMsg, playerName);
                Bukkit.getConsoleSender().sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(prefix + " " + substituted));

                if (module.getConfig().getBoolean("TellPlayersWithPermission")) {
                    warnStaff(playerName);
                }
            } else {
                String prefix = Objects.requireNonNull(LocaleManager.get().getString("MessagesPrefix"));
                String rawMsg = LocaleManager.get().getString("PlayerNotOnlineOnHandle");
                String substituted = PlaceholderManager.SubstitutePlayerNameAndColorCodePlaceholders(rawMsg, playerName);
                Bukkit.getConsoleSender().sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(prefix + " " + substituted));
            }
        }
    }

    public void addDummyPlayer() {
        Bukkit.getScheduler().runTaskAsynchronously(module.getHostPlugin(), () -> module.handledPlayerStore.StoreDummyPlayerData(_ -> {
            if (module.getConfig().getBoolean("UseHeadsInGUI")) {
                String nonRepeatingRandomUUID;
                do {
                    nonRepeatingRandomUUID = UUID.randomUUID().toString();
                } while (module.handledPlayerVault.GetUUIDs().contains(nonRepeatingRandomUUID));
                module.handledPlayerVault.AddDummyXrayerHeadToCache();
            }
        }));
    }

    private void dropItemAtPlayerLocation(ItemStack item, Player p) {
        if (item != null && item.getType() != Material.AIR) {
            Item droppedItem = p.getWorld().dropItem(p.getLocation(), item);
            droppedItem.setVelocity(new Vector(0, 0, 0));
        }
    }

    public boolean restoreBelongings(String uuid, ItemStack[] belongings) {
        Player target = Bukkit.getPlayer(UUID.fromString(uuid));
        if (target != null) {
            if (belongings != null) {
                for (int i = 0; i < 36; i++) {
                    if (belongings[i] != null && belongings[i].getType() != Material.AIR && target.getInventory().firstEmpty() != -1)
                        target.getInventory().addItem(belongings[i]);
                    else if (belongings[i] != null && belongings[i].getType() != Material.AIR)
                        dropItemAtPlayerLocation(belongings[i], target);
                }
                if (target.getEquipment().getItemInOffHand().getType().equals(Material.AIR))
                    target.getEquipment().setItemInOffHand(belongings[36]);
                else dropItemAtPlayerLocation(belongings[36], target);

                if (target.getEquipment().getBoots() == null) target.getEquipment().setBoots(belongings[40]);
                else dropItemAtPlayerLocation(belongings[40], target);

                if (target.getEquipment().getLeggings() == null) target.getEquipment().setLeggings(belongings[39]);
                else dropItemAtPlayerLocation(belongings[39], target);

                if (target.getEquipment().getChestplate() == null) target.getEquipment().setChestplate(belongings[38]);
                else dropItemAtPlayerLocation(belongings[38], target);

                if (target.getEquipment().getHelmet() == null) target.getEquipment().setHelmet(belongings[37]);
                else dropItemAtPlayerLocation(belongings[37], target);
            }

            // Log AbsolvedPlayer in console with Adventure
            String prefix = Objects.requireNonNull(LocaleManager.get().getString("MessagesPrefix"));
            String rawMsg = LocaleManager.get().getString("AbsolvedPlayer");
            String substituted = PlaceholderManager.SubstitutePlayerNameAndColorCodePlaceholders(rawMsg, target.getName());
            Bukkit.getConsoleSender().sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(prefix + " " + substituted));

            for (String cmd : module.getConfig().getStringList("CommandsExecutedOnPlayerAbsolved")) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), PlaceholderManager.SubstitutePlayerNameAndColorCodePlaceholders(cmd, target.getName()));
            }

            return true;
        } else return false;
    }
}
