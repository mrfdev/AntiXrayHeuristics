//--------------------------------------------------------------------
// Copyright © Dylan Calaf Latham 2019-2021 XRay Heuristics
//--------------------------------------------------------------------

package com.onemoreblock.coreprotectaddons.xrayheuristics.support;

import com.onemoreblock.coreprotectaddons.xrayheuristics.util.RandomItemStackGenerator;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

@SuppressWarnings("DataFlowIssue")
public class DummyInventory implements Inventory {
    @Override
    public int getSize() {
        return 0;
    }

    @Override
    public int getMaxStackSize() {
        return 0;
    }

    @Override
    public void setMaxStackSize(int size) {

    }

    @Override
    public ItemStack getItem(int index) {
        return RandomItemStackGenerator.GetRandomItemStack();
    }

    @Override
    public void setItem(int index, ItemStack item) {

    }

    @Override
    public @NonNull HashMap<Integer, ItemStack> addItem(ItemStack... items) throws IllegalArgumentException {
        return null;
    }

    @Override
    public @NonNull HashMap<Integer, ItemStack> removeItem(ItemStack... items) throws IllegalArgumentException {
        return null;
    }

    @Override
    public @NonNull HashMap<Integer, ItemStack> removeItemAnySlot(@NonNull ItemStack... items) throws IllegalArgumentException {
        return null;
    }

    @Override
    public ItemStack @NonNull [] getContents() {
        return new ItemStack[0];
    }

    @Override
    public void setContents(ItemStack @NonNull [] items) throws IllegalArgumentException {

    }

    @Override
    public ItemStack @NonNull [] getStorageContents() {
        return new ItemStack[0];
    }

    @Override
    public void setStorageContents(ItemStack @NonNull [] items) throws IllegalArgumentException {

    }

    @Override
    public boolean contains(@NonNull Material material) throws IllegalArgumentException {
        return false;
    }

    @Override
    public boolean contains(ItemStack item) {
        return false;
    }

    @Override
    public boolean contains(@NonNull Material material, int amount) throws IllegalArgumentException {
        return false;
    }

    @Override
    public boolean contains(ItemStack item, int amount) {
        return false;
    }

    @Override
    public boolean containsAtLeast(ItemStack item, int amount) {
        return false;
    }

    @Override
    public @NonNull HashMap<Integer, ? extends ItemStack> all(@NonNull Material material) throws IllegalArgumentException {
        return null;
    }

    @Override
    public @NonNull HashMap<Integer, ? extends ItemStack> all(ItemStack item) {
        return null;
    }

    @Override
    public int first(@NonNull Material material) throws IllegalArgumentException {
        return 0;
    }

    @Override
    public int first(@NonNull ItemStack item) {
        return 0;
    }

    @Override
    public int firstEmpty() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public void remove(@NonNull Material material) throws IllegalArgumentException {

    }

    @Override
    public void remove(@NonNull ItemStack item) {

    }

    @Override
    public void clear(int index) {

    }

    @Override
    public void clear() {

    }

    @Override
    public int close() {
        return 0;
    }

    @Override
    public @NonNull List<HumanEntity> getViewers() {
        return null;
    }

    @Override
    public @NonNull InventoryType getType() {
        return null;
    }

    @Override
    public InventoryHolder getHolder() {
        return null;
    }

    @Override
    public @Nullable InventoryHolder getHolder(boolean useSnapshot) {
        return null;
    }

    @Override
    public @NonNull ListIterator<ItemStack> iterator() {
        return null;
    }

    @Override
    public @NonNull ListIterator<ItemStack> iterator(int index) {
        return null;
    }

    @Override
    public Location getLocation() {
        return null;
    }
}
