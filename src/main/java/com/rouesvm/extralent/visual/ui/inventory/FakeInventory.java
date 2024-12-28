package com.rouesvm.extralent.visual.ui.inventory;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class FakeInventory extends SimpleInventory {
    public FakeInventory(int size) {
        super(size);
    }


    @Override
    public int count(Item item) {
        return 1;
    }

    @Override
    public ItemStack addStack(ItemStack stack) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        } else {
            ItemStack itemStack = stack.copy();
            if (itemStack.isEmpty()) {
                return ItemStack.EMPTY;
            } else {
                this.addToNewSlot(itemStack);
                return itemStack.isEmpty() ? ItemStack.EMPTY : itemStack;
            }
        }
    }

    @Override
    public int getMaxCountPerStack() {
        return 1;
    }

    @Override
    public int getMaxCount(ItemStack stack) {
        return 1;
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return false;
    }

    @Override
    public boolean canTransferTo(Inventory hopperInventory, int slot, ItemStack stack) {
        return false;
    }

    private void addToNewSlot(ItemStack stack) {
        for(int i = 0; i < this.size(); ++i) {
            ItemStack itemStack = this.getStack(i);
            if (itemStack.isEmpty() && !itemStack.equals(stack)) {
                this.setStack(i, stack.copyAndEmpty());
                return;
            } else if (itemStack.isOf(stack.getItem())) {
                break;
            }
        }
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return false;
    }
}
