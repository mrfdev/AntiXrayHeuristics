//--------------------------------------------------------------------
// Copyright © Dylan Calaf Latham 2019-2021 AntiXrayHeuristics
//--------------------------------------------------------------------

package com.greymagic27;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.jetbrains.annotations.NotNull;

class EventItemDrag implements Listener {
    @EventHandler
    public void dragEv(@NotNull InventoryDragEvent e) //Stops items from being placed in top Xrayer Vault inventory by dragging them
    {
        if (PlainTextComponentSerializer.plainText().serialize(e.getView().title()).equals("Xrayer Vault")) {
            e.setCancelled(true);
        }
    }
}
