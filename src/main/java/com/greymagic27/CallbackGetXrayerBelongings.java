package com.greymagic27;

import org.bukkit.inventory.ItemStack;

interface CallbackGetXrayerBelongings {
    void onQueryDone(ItemStack[] belongings);
}