package com.greymagic27;

import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class BukkitSerializer {

    public static ItemStack @NotNull [] InventoryAndEquipmentToSingleItemStackArray(Inventory inv, EntityEquipment equip) //Returns inventory and equipment item stacks put into an array
    {
        ItemStack[] confiscatedItems = new ItemStack[41]; //41 = max number of items a player can have in inventory and equipment combined.
        for (int i = 0; i < 36; i++) confiscatedItems[i] = inv.getItem(i);
        confiscatedItems[36] = equip.getItemInOffHand();
        confiscatedItems[37] = equip.getHelmet();
        confiscatedItems[38] = equip.getChestplate();
        confiscatedItems[39] = equip.getLeggings();
        confiscatedItems[40] = equip.getBoots();

        return confiscatedItems;
    }

    public static @NotNull String itemStackArrayToBase64(ItemStack[] items) throws IllegalStateException {
        try {
            byte[] serial = ItemStack.serializeItemsAsBytes(Arrays.asList(items));
            return Base64.getEncoder().encodeToString(serial);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to save item stacks.", e);
        }
    }

    public static ItemStack[] itemStackArrayFromBase64(String data) throws IOException {
        if (data == null) return null;
        try {
            byte[] bytes = Base64.getDecoder().decode(data);
            return ItemStack.deserializeItemsFromBytes(bytes);
        } catch (Exception e) {
            throw new IOException("Unable to load item stacks.", e);
        }
    }
}