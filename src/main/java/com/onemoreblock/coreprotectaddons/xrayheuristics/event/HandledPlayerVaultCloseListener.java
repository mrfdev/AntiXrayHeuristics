//--------------------------------------------------------------------
// Copyright © Dylan Calaf Latham 2019-2021 XRay Heuristics
//--------------------------------------------------------------------

package com.onemoreblock.coreprotectaddons.xrayheuristics.event;

import com.onemoreblock.coreprotectaddons.xrayheuristics.XRayHeuristicsModule;
import com.onemoreblock.coreprotectaddons.xrayheuristics.util.DelayedInventoryCloseExecution;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.jspecify.annotations.NonNull;

public class HandledPlayerVaultCloseListener implements Listener {

    private final XRayHeuristicsModule module;

    public HandledPlayerVaultCloseListener(XRayHeuristicsModule main) {
        this.module = main;
    }

    @EventHandler
    public void closeEv(@NonNull InventoryCloseEvent e) //Removes the player as an Xrayer Vault viewer with a delay after closing the Xrayer Vault inventory, only if player isn't still looking at inv.
    {
        if (PlainTextComponentSerializer.plainText().serialize(e.getView().title()).equals("Xrayer Vault")) {
            Bukkit.getScheduler().runTaskLater(module.getHostPlugin(), () -> {
                DelayedInventoryCloseExecution delay = new DelayedInventoryCloseExecution(e.getPlayer(), module);
                delay.run();
            }, 10L);
        }
    }
}
