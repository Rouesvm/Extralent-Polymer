package com.rouesvm.extralent.block.transport.entity;

import com.rouesvm.extralent.registries.block.BlockEntityRegistry;
import com.rouesvm.extralent.utils.Connection;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.block.BlockState;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.util.math.BlockPos;

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
        if (this.ticks++ % 2 == 0) {
            super.onUpdate();
        }
    }

    @Override
    public boolean correctBlock(BlockPos blockPos) {
        Storage<ItemVariant> storage = ItemStorage.SIDED.find(this.world, blockPos, null);
        return storage != null && storage.supportsInsertion();
    }

    @Override
    public boolean blockLogic(Connection connection) {
        Storage<ItemVariant> storage = ItemStorage.SIDED.find(this.world, connection.getPos(), null);
        if (storage != null) {
            if (connection.getWeight() == 1 && storage.supportsInsertion())
                return insertItem(storage);
            if (storage.supportsExtraction())
                return extractItem(storage);
        }
        return false;
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
