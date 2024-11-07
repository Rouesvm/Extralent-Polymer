package com.rouesvm.extralent.block.transport.entity;

import com.rouesvm.extralent.registries.block.BlockEntityRegistry;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.block.BlockState;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TransporterBlockEntity extends PipeBlockEntity {
    public TransporterBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.TRANSPORTER_BLOCK_ENTITY, pos, state);
    }

    @Override
    public SimpleInventory createInventory() {
        return super.createInventory(1);
    }

    @Override
    public void tick() {
        if (this.ticks++ % 5 == 0) {
            if (this.inventory.isEmpty()) return;
            onUpdate();
        }
    }

    @Override
    public boolean correctBlock(BlockPos blockPos) {
        Storage<ItemVariant> storage = ItemStorage.SIDED.find(this.world, blockPos, null);
        return storage != null && storage.supportsInsertion();
    }

    @Override
    public void extractBlock(BlockPos blockPos) {
        Storage<ItemVariant> storage = ItemStorage.SIDED.find(this.world, blockPos, null);
        if (storage != null && storage.supportsInsertion()) {
            try (Transaction transaction = Transaction.openOuter()) {
                var itemVariant = inventoryStorage.getSlots().getFirst();
                ItemStack stack = inventory.getStack(0);

                if (!stack.isEmpty()) {
                    long insertion = storage.insert(itemVariant.getResource(), 1, transaction);
                    stack.decrement((int) insertion);

                    inventory.setStack(0, stack);
                    transaction.commit();
                } else inventory.setStack(0, ItemStack.EMPTY);
            }
        }
    }
}
