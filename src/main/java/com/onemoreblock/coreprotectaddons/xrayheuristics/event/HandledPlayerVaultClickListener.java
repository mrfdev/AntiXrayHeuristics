//--------------------------------------------------------------------
// Copyright © Dylan Calaf Latham 2019-2021 XRay Heuristics
//--------------------------------------------------------------------

package com.onemoreblock.coreprotectaddons.xrayheuristics.event;

import com.onemoreblock.coreprotectaddons.xrayheuristics.XRayHeuristicsModule;
import com.onemoreblock.coreprotectaddons.xrayheuristics.manager.LocaleManager;
import java.util.Locale;
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
import org.jspecify.annotations.NonNull;

public class HandledPlayerVaultClickListener implements Listener {

    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacyAmpersand();

    private final XRayHeuristicsModule module;

    public HandledPlayerVaultClickListener(XRayHeuristicsModule main) {
        this.module = main;
    }

    @EventHandler
    public void clickEvent(@NonNull InventoryClickEvent e) {
        //Check if click occurred with xrayer vault gui view open:
        String title = PlainTextComponentSerializer.plainText().serialize(e.getView().title());
        if (title.toLowerCase(Locale.ROOT).startsWith("Xrayer".toLowerCase(Locale.ROOT))) {
            //A non-null, non-AIR, upper window slot was clicked
            if (e.getCurrentItem() != null && e.getRawSlot() < e.getView().getTopInventory().getSize() && e.getCurrentItem().getType() != Material.AIR) {
                //An item was clicked
                final String playerWhoClicked = e.getWhoClicked().getName();
                if (module.handledPlayerVault.GetInspectedXrayer(playerWhoClicked) == null) //We're on the overall xrayer vault which shows xrayer entries
                {
                    switch (e.getSlot()) {
                        case 48: {
                            //Purge all xrayers:
                            if (e.getWhoClicked().hasPermission("xrayheuristics.admin") || e.getWhoClicked().hasPermission("AXH.Vault.Purge")) {
                                module.handledPlayerVault.PurgeAllXrayersAndRefreshVault();
                            } else {
                                Component noPerm = LEGACY_SERIALIZER.deserialize(Objects.requireNonNull(LocaleManager.get().getString("NoPermissionForCommand")));
                                e.getView().getPlayer().sendMessage(noPerm);
                            }

                            break;
                        }
                        case 50: {
                            //Refresh vault:
                            module.handledPlayerVault.UpdateXrayerInfoLists((Player) e.getView().getPlayer(), module.handledPlayerVault.GetPage(playerWhoClicked));

                            break;
                        }
                        case 53: {
                            //Show next vault row:
                            if (Objects.requireNonNull(e.getCurrentItem().getItemMeta().displayName()).equals(module.handledPlayerVault.nextButton.getItemMeta().displayName()))
                                module.handledPlayerVault.OpenVault((Player) e.getView().getPlayer(), module.handledPlayerVault.GetPage(playerWhoClicked) + 1);

                            break;
                        }
                        case 45: {
                            //Show previous vault row:
                            if (Objects.requireNonNull(e.getCurrentItem().getItemMeta().displayName()).equals(module.handledPlayerVault.prevButton.getItemMeta().displayName()))
                                module.handledPlayerVault.OpenVault((Player) e.getView().getPlayer(), module.handledPlayerVault.GetPage(playerWhoClicked) - 1);

                            break;
                        }
                        default: {
                            if (e.getSlot() > -1 && e.getSlot() < 45) {
                                //Open xrayer's confiscated inventory: The slot the item we clicked is on + the page we're on multiplied by the entry slots range (45 player heads) is equal to the xrayer's UUID position in the vault's XrayerUUID's array:
                                module.handledPlayerVault.OpenXrayerConfiscatedInventory((Player) e.getWhoClicked(), e.getRawSlot() + module.handledPlayerVault.GetPage(playerWhoClicked) * 45);
                            }

                            break;
                        }
                    }
                } else //We're inspecting an xrayer's information and possible confiscated items
                {
                    switch (e.getSlot()) {
                        case 45: {
                            //Go back to previous page:
                            module.handledPlayerVault.OpenVault((Player) e.getView().getPlayer(), module.handledPlayerVault.GetPage(playerWhoClicked));

                            break;
                        }
                        case 53: {
                            //Absolve player:
                            if (e.getWhoClicked().hasPermission("xrayheuristics.admin") || e.getWhoClicked().hasPermission("AXH.Commands.AbsolvePlayer")) {
                                final String xrayerUUID = module.handledPlayerVault.GetInspectedXrayer(playerWhoClicked);
                                //Return inventory to player, and do the rest if player was online:
                                Bukkit.getScheduler().runTaskAsynchronously(module.getHostPlugin(), () -> module.handledPlayerStore.GetXrayerBelongings(xrayerUUID, belongings -> {
                                    if (module.getHandledPlayerService().restoreBelongings(xrayerUUID, belongings)) {
                                        module.handledPlayerVault.XrayerDataRemover(playerWhoClicked, true);
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
                            if (e.getWhoClicked().hasPermission("xrayheuristics.admin") || e.getWhoClicked().hasPermission("AXH.Commands.PurgePlayer")) {
                                module.handledPlayerVault.XrayerDataRemover(playerWhoClicked, true);
                            } else {
                                Component noPerm = LEGACY_SERIALIZER.deserialize(Objects.requireNonNull(LocaleManager.get().getString("NoPermissionForCommand")));
                                e.getView().getPlayer().sendMessage(noPerm);
                            }

                            break;
                        }
                        case 49: {
                            //Teleport to player detection (HandleLocation) coordinates:
                            String xrayerUUID = module.handledPlayerVault.GetInspectedXrayer(playerWhoClicked);
                            module.handledPlayerVault.TeleportToDetectionCoordinates((Player) e.getWhoClicked(), xrayerUUID);

                            break;
                        }
                    }
                }
            }

            e.setCancelled(true);
        }
    }
}
