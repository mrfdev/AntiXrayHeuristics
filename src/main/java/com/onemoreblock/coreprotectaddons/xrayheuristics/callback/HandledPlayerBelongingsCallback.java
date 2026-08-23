package com.onemoreblock.coreprotectaddons.xrayheuristics.callback;

import org.bukkit.inventory.ItemStack;

public interface HandledPlayerBelongingsCallback {
    void onQueryDone(ItemStack[] belongings);
}