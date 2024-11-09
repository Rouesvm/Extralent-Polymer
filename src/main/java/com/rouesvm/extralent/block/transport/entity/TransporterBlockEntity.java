package com.rouesvm.extralent.block.transport.entity;

import com.rouesvm.extralent.block.transport.TransporterBlock;
import com.rouesvm.extralent.registries.block.BlockEntityRegistry;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
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
            super.onUpdate();
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
        if (storage != null) {
            var blockState = this.world.getBlockState(blockPos);
            if (blockState != null && blockState.getBlock() instanceof TransporterBlock) {
                insertItem(storage);
                return;
            }
            if (storage.supportsInsertion())
                if (insertItem(storage)) return;
            if (storage.supportsExtraction())
                extractItem(storage);
        }
    }

    public boolean insertItem(Storage<ItemVariant> storage) {
        for (StorageView<ItemVariant> storageView : this.inventoryStorage) {
            if (!storageView.isResourceBlank() && storageView.getAmount() > 0) {
                Transaction transaction = Transaction.openOuter();
                var resource = storageView.getResource();
                long extracted = this.inventoryStorage.extract(resource, 1, transaction);
                if (extracted > 0) {
                    long inserted = storage.insert(resource, 1, transaction);
                    if (inserted > 0) {
                        transaction.commit();
                        return true;
                    }
                }
                transaction.close();
            }
        }
        return false;
    }

    public boolean extractItem(Storage<ItemVariant> storage) {
        for (StorageView<ItemVariant> storageView : storage) {
            if (!storageView.isResourceBlank() && storageView.getAmount() > 0) {
                Transaction transaction = Transaction.openOuter();
                var resource = storageView.getResource();
                long extracted = storage.extract(resource, 1, transaction);
                if (extracted > 0) {
                    long inserted = this.inventoryStorage.insert(resource, 1, transaction);
                    if (inserted > 0) {
                        transaction.commit();
                        return true;
                    }
                }
                transaction.close();
            }
        }
        return false;
    }
}
