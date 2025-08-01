//--------------------------------------------------------------------
// Copyright © Dylan Calaf Latham 2019-2021 AntiXrayHeuristics
//--------------------------------------------------------------------

package com.greymagic27;

import java.util.Random;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class RandomItemStackGenerator {
    public static @NotNull ItemStack GetRandomItemStack() {
        Random random = new Random();
        int material = random.nextInt(Material.values().length); //Random material
        int amount = Material.values()[material].getMaxStackSize(); //Max stack size

        return new ItemStack(Material.values()[material], amount);
    }
}