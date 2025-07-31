//--------------------------------------------------------------------
// Copyright © Dylan Calaf Latham 2019-2021 AntiXrayHeuristics
//--------------------------------------------------------------------

package com.greymagic27;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.jetbrains.annotations.NotNull;

public class EventInventoryClose implements Listener {

    private final AntiXrayHeuristics mainClassAccess;

    public EventInventoryClose(AntiXrayHeuristics main) {
        this.mainClassAccess = main;
    }

    @EventHandler
    public void closeEv(@NotNull InventoryCloseEvent e) //Removes the player as an Xrayer Vault viewer with a delay after closing the Xrayer Vault inventory, only if player isn't still looking at inv.
    {
        if (PlainTextComponentSerializer.plainText().serialize(e.getView().title()).equals("Xrayer Vault")) {
            Bukkit.getScheduler().runTaskLater(mainClassAccess, () -> {
                DelayedInventoryCloseExecution delay = new DelayedInventoryCloseExecution(e.getPlayer(), mainClassAccess);
                delay.run();
            }, 10L);
        }
    }
}
