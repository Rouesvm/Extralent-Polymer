package com.rouesvm.extralent.ui.inventory;

import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

public class ExtralentInventory extends SimpleInventory implements MinimalSidedInventory {
    public ExtralentInventory(int size) {
        super(size);
    }

    @Override
    public DefaultedList<ItemStack> getStacks() {
        return this.getHeldStacks();
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        return new int[0];
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return super.canInsert(stack);
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        return isValid(slot, stack);
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return false;
    }
}
