package com.greymagic27;

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
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XrayerHandler {

    private static final LegacyComponentSerializer legacySerializer = LegacyComponentSerializer.legacyAmpersand();
    private static final Logger log = LoggerFactory.getLogger(XrayerHandler.class);

    private static void XrayerWarn(String xrayername) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("AXH.XrayerWarning")) {
                String prefix = Objects.requireNonNull(LocaleManager.get().getString("MessagesPrefix"));
                String rawMsg = LocaleManager.get().getString("AutoHandledPlayer");
                String substituted = PlaceholderManager.SubstitutePlayerNameAndColorCodePlaceholders(rawMsg, xrayername);
                Component message = legacySerializer.deserialize(prefix + " " + substituted);
                player.sendMessage(message);
            }
        }
    }

    public static void HandleXrayer(String xrayername) {
        AntiXrayHeuristics mainClass = JavaPlugin.getPlugin(AntiXrayHeuristics.class);
        Player player = Bukkit.getPlayer(xrayername);

        HandlingXrayerEvent ev = new HandlingXrayerEvent(player);
        Bukkit.getServer().getPluginManager().callEvent(ev);

        if (!ev.isCancelled()) {
            if (player != null) {
                if (mainClass.getConfig().getBoolean("SendMessageToPlayer")) {
                    String prefix = Objects.requireNonNull(LocaleManager.get().getString("MessagesPrefix"));
                    String playerMessageRaw = Objects.requireNonNull(LocaleManager.get().getString("PlayerMessageOnXray"));
                    Component playerMessage = legacySerializer.deserialize(prefix + " " + playerMessageRaw);
                    player.sendMessage(playerMessage);
                }

                Bukkit.getScheduler().runTaskAsynchronously(mainClass, () -> mainClass.mm.StorePlayerData(player, handleTimes -> {

                    if (mainClass.getConfig().getBoolean("UseHeadsInGUI")) {
                        mainClass.vault.AddXrayerHeadToCache(player.getUniqueId());
                    }

                    if (mainClass.getConfig().getBoolean("ClensePlayerItems")) {
                        try {
                            player.getInventory().clear();
                            player.getEquipment().clear();
                        } catch (Exception e) {
                            log.error("Failed to remove player {}'s equipment while attempting to handle as Xrayer.", xrayername);
                        }
                    }

                    ConfigurationSection section = mainClass.getConfig().getConfigurationSection("CommandsExecutedOnXrayerDetected");
                    if (section != null && section.contains(String.valueOf(handleTimes))) {
                        ConfigurationSection subSection = mainClass.getConfig().getConfigurationSection("CommandsExecutedOnXrayerDetected." + handleTimes);
                        if (subSection != null) {
                            Map<String, Object> commandsToExecute = subSection.getValues(false);
                            for (Map.Entry<String, Object> pair : commandsToExecute.entrySet()) {
                                String command = PlaceholderManager.SubstitutePlayerNameAndHandleTimesPlaceholders((String) pair.getValue(), xrayername, Integer.toString(handleTimes));
                                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                            }
                        }
                    }
                }));

                if (mainClass.getConfig().getBoolean("NullifySuspicionAferPunish")) {
                    mainClass.sessions.remove(player.getName());
                }

                // Console message using Adventure + legacy serializer
                String prefix = Objects.requireNonNull(LocaleManager.get().getString("MessagesPrefix"));
                String rawMsg = LocaleManager.get().getString("AutoHandledPlayer");
                String substituted = PlaceholderManager.SubstitutePlayerNameAndColorCodePlaceholders(rawMsg, xrayername);
                Component consoleMessage = legacySerializer.deserialize(prefix + " " + substituted);
                log.info(legacySerializer.serialize(consoleMessage));

                if (mainClass.getConfig().getBoolean("TellPlayersWithPermission")) {
                    XrayerWarn(xrayername);
                }
            } else {
                String prefix = Objects.requireNonNull(LocaleManager.get().getString("MessagesPrefix"));
                String rawMsg = LocaleManager.get().getString("PlayerNotOnlineOnHandle");
                String substituted = PlaceholderManager.SubstitutePlayerNameAndColorCodePlaceholders(rawMsg, xrayername);
                Component consoleMessage = legacySerializer.deserialize(prefix + " " + substituted);
                log.info(legacySerializer.serialize(consoleMessage));
            }
        }
    }

    public static void AddDummyXrayer() {
        AntiXrayHeuristics mainClass = JavaPlugin.getPlugin(AntiXrayHeuristics.class);
        Bukkit.getScheduler().runTaskAsynchronously(mainClass, () -> mainClass.mm.StoreDummyPlayerData(handleTimes -> {
            if (mainClass.getConfig().getBoolean("UseHeadsInGUI")) {
                String nonRepeatingRandomUUID;
                do {
                    nonRepeatingRandomUUID = UUID.randomUUID().toString();
                } while (mainClass.vault.GetUUIDs().contains(nonRepeatingRandomUUID));
                mainClass.vault.AddDummyXrayerHeadToCache();
            }
        }));
    }

    private static void DropItemAtPlayerLocation(ItemStack item, Player p) {
        if (item != null && item.getType() != Material.AIR) {
            Item droppedItem = p.getWorld().dropItem(p.getLocation(), item);
            droppedItem.setVelocity(new Vector(0, 0, 0));
        }
    }

    public static boolean PlayerAbsolver(String uuid, ItemStack[] belongings, AntiXrayHeuristics mainClassAccess) {
        Player target = Bukkit.getPlayer(UUID.fromString(uuid));
        if (target != null) {
            if (belongings != null) {
                for (int i = 0; i < 36; i++) {
                    if (belongings[i] != null && belongings[i].getType() != Material.AIR && target.getInventory().firstEmpty() != -1)
                        target.getInventory().addItem(belongings[i]);
                    else if (belongings[i] != null && belongings[i].getType() != Material.AIR)
                        DropItemAtPlayerLocation(belongings[i], target);
                }
                if (target.getEquipment().getItemInOffHand().getType().equals(Material.AIR))
                    target.getEquipment().setItemInOffHand(belongings[36]);
                else DropItemAtPlayerLocation(belongings[36], target);

                if (target.getEquipment().getBoots() == null)
                    target.getEquipment().setBoots(belongings[40]);
                else DropItemAtPlayerLocation(belongings[40], target);

                if (target.getEquipment().getLeggings() == null)
                    target.getEquipment().setLeggings(belongings[39]);
                else DropItemAtPlayerLocation(belongings[39], target);

                if (target.getEquipment().getChestplate() == null)
                    target.getEquipment().setChestplate(belongings[38]);
                else DropItemAtPlayerLocation(belongings[38], target);

                if (target.getEquipment().getHelmet() == null)
                    target.getEquipment().setHelmet(belongings[37]);
                else DropItemAtPlayerLocation(belongings[37], target);
            }

            // Log AbsolvedPlayer in console with Adventure
            String prefix = Objects.requireNonNull(LocaleManager.get().getString("MessagesPrefix"));
            String rawMsg = LocaleManager.get().getString("AbsolvedPlayer");
            String substituted = PlaceholderManager.SubstitutePlayerNameAndColorCodePlaceholders(rawMsg, target.getName());
            Component consoleMessage = legacySerializer.deserialize(prefix + " " + substituted);
            log.info(legacySerializer.serialize(consoleMessage));

            for (String cmd : mainClassAccess.getConfig().getStringList("CommandsExecutedOnPlayerAbsolved")) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), PlaceholderManager.SubstitutePlayerNameAndColorCodePlaceholders(cmd, target.getName()));
            }

            return true;
        } else return false;
    }
}
