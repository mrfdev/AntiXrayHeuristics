//--------------------------------------------------------------------
// Copyright © Dylan Calaf Latham 2019-2021 AntiXrayHeuristics
//--------------------------------------------------------------------

package com.greymagic27;

import java.util.Objects;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

public class EventClick implements Listener {

    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacyAmpersand();

    private final AntiXrayHeuristics mainClassAccess;

    public EventClick(AntiXrayHeuristics main) {
        this.mainClassAccess = main;
    }

    @EventHandler
    public void clickEvent(@NotNull InventoryClickEvent e) {
        //Check if click occured with xrayer vault gui view open:
        if (PlainTextComponentSerializer.plainText().serialize(e.getView().title()).contains(mainClassAccess.vault.GetGUITitle())) {
            //A non-null, non AIR, upper window slot was clicked
            if (e.getCurrentItem() != null && e.getRawSlot() < e.getView().getTopInventory().getSize() && e.getCurrentItem().getType() != Material.AIR) {
                //An item was clicked
                final String playerWhoClicked = e.getWhoClicked().getName();
                if (mainClassAccess.vault.GetInspectedXrayer(playerWhoClicked) == null) //We're on the overall xrayer vault which shows xrayer entries
                {
                    switch (e.getSlot()) {
                        case 48: {
                            //Purge all xrayers:
                            if (e.getWhoClicked().hasPermission("AXH.Vault.Purge")) {
                                mainClassAccess.vault.PurgeAllXrayersAndRefreshVault();
                            } else {
                                Component noPerm = LEGACY_SERIALIZER.deserialize(Objects.requireNonNull(LocaleManager.get().getString("NoPermissionForCommand")));
                                e.getView().getPlayer().sendMessage(noPerm);
                            }

                            break;
                        }
                        case 50: {
                            //Refresh vault:
                            mainClassAccess.vault.UpdateXrayerInfoLists((Player) e.getView().getPlayer(), mainClassAccess.vault.GetPage(playerWhoClicked));

                            break;
                        }
                        case 53: {
                            //Show next vault row:
                            if (Objects.requireNonNull(e.getCurrentItem().getItemMeta().displayName()).equals(mainClassAccess.vault.nextButton.getItemMeta().displayName()))
                                mainClassAccess.vault.OpenVault((Player) e.getView().getPlayer(), mainClassAccess.vault.GetPage(playerWhoClicked) + 1);

                            break;
                        }
                        case 45: {
                            //Show previous vault row:
                            if (Objects.requireNonNull(e.getCurrentItem().getItemMeta().displayName()).equals(mainClassAccess.vault.prevButton.getItemMeta().displayName()))
                                mainClassAccess.vault.OpenVault((Player) e.getView().getPlayer(), mainClassAccess.vault.GetPage(playerWhoClicked) - 1);

                            break;
                        }
                        default: {
                            if (e.getSlot() > -1 && e.getSlot() < 45) {
                                //Open xrayer's confiscated inventory: The slot the item we clicked is on + the page we're on multiplied by the entry slots range (45 player heads) is equal to the xrayer's UUID position in the vault's XrayerUUID's array:
                                mainClassAccess.vault.OpenXrayerConfiscatedInventory((Player) e.getWhoClicked(), e.getRawSlot() + mainClassAccess.vault.GetPage(playerWhoClicked) * 45);
                            }

                            break;
                        }
                    }
                } else //We're inspecting an xrayer's information and possible confiscated items
                {
                    switch (e.getSlot()) {
                        case 45: {
                            //Go back to previous page:
                            mainClassAccess.vault.OpenVault((Player) e.getView().getPlayer(), mainClassAccess.vault.GetPage(playerWhoClicked));

                            break;
                        }
                        case 53: {
                            //Absolve player:
                            if (e.getWhoClicked().hasPermission("AXH.Commands.AbsolvePlayer")) {
                                final String xrayerUUID = mainClassAccess.vault.GetInspectedXrayer(playerWhoClicked);
                                //Return inventory to player, and do the rest if player was online:
                                Bukkit.getScheduler().runTaskAsynchronously(mainClassAccess, () -> mainClassAccess.mm.GetXrayerBelongings(xrayerUUID, belongings -> {
                                    if (XrayerHandler.PlayerAbsolver(xrayerUUID, belongings, mainClassAccess)) {
                                        mainClassAccess.vault.XrayerDataRemover(playerWhoClicked, true);
                                    } else {
                                        Component notOnline = LEGACY_SERIALIZER.deserialize(LocaleManager.get().getString("MessagesPrefix") + " " + LocaleManager.get().getString("PlayerNotOnlineOnAbsolution"));
                                        e.getWhoClicked().sendMessage(notOnline);
                                    }
                                }));
                            } else {
                                Component noPerm = LEGACY_SERIALIZER.deserialize(Objects.requireNonNull(LocaleManager.get().getString("NoPermissionForCommand")));
                                e.getView().getPlayer().sendMessage(noPerm);
                            }

                            break;
                        }
                        case 51: {
                            //Purge player:
                            if (e.getWhoClicked().hasPermission("AXH.Commands.PurgePlayer")) {
                                mainClassAccess.vault.XrayerDataRemover(playerWhoClicked, true);
                            } else {
                                Component noPerm = LEGACY_SERIALIZER.deserialize(Objects.requireNonNull(LocaleManager.get().getString("NoPermissionForCommand")));
                                e.getView().getPlayer().sendMessage(noPerm);
                            }

                            break;
                        }
                        case 49: {
                            //Teleport to player detection (HandleLocation) coordinates:
                            String xrayerUUID = mainClassAccess.vault.GetInspectedXrayer(playerWhoClicked);
                            mainClassAccess.vault.TeleportToDetectionCoordinates((Player) e.getWhoClicked(), xrayerUUID);

                            break;
                        }
                    }
                }
            }

            e.setCancelled(true);
        }
    }
}
