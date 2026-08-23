//--------------------------------------------------------------------
// Copyright © Dylan Calaf Latham 2019-2021 XRay Heuristics
//--------------------------------------------------------------------

package com.onemoreblock.coreprotectaddons.xrayheuristics.util;

import java.util.Random;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;

public class RandomItemStackGenerator {
    public static @NonNull ItemStack GetRandomItemStack() {
        Random random = new Random();
        int material = random.nextInt(Material.values().length); //Random material
        int amount = Material.values()[material].getMaxStackSize(); //Max stack size

        return new ItemStack(Material.values()[material], amount);
    }
}