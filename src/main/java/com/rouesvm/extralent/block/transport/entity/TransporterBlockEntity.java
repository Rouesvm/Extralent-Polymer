package com.rouesvm.extralent.block.transport.entity;

import com.rouesvm.extralent.registries.block.BlockEntityRegistry;
import com.rouesvm.extralent.block.transport.entity.connection.Connection;
import com.rouesvm.extralent.ui.inventory.ExtralentInventory;
import com.rouesvm.extralent.ui.inventory.FakeInventory;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.block.BlockState;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import team.reborn.energy.api.EnergyStorageUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class TransporterBlockEntity extends PipeBlockEntity {
    public List<Item> itemList = new ArrayList<>();
    private static final int ITEM_TRANSFER_RATE = 2;

    public TransporterBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.TRANSPORTER_BLOCK_ENTITY, pos, state);
    }

    @Override
    public ExtralentInventory createInventory() {
        return super.createInventory(1);
    }

    public void setItemList(List<ItemStack> inventory) {
        itemList = new ArrayList<>();
        if (inventory.isEmpty()) return;

        inventory.forEach(stack -> itemList.add(stack.getItem()));
    }

    @Override
    public void tick() {
        if (this.getWorld() == null || this.getWorld().isClient) return;
        if (this.getWorld().getTime() % 5 != 0) return;

        super.onUpdate();
    }

    @Override
    public boolean correctBlock(BlockPos blockPos) {
        Storage<ItemVariant> storage = ItemStorage.SIDED.find(this.world, blockPos, null);
        return storage != null && storage.supportsInsertion();
    }

    @Override
    public boolean blockLogic(Connection connection) {
        Storage<ItemVariant> storage = ItemStorage.SIDED.find(this.world, connection.getPos(), connection.getSide());
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
            if (isValidStorageView(storageView)) {
                Transaction transaction = Transaction.openOuter();
                ItemVariant resource = storageView.getResource();
                long extracted = this.inventoryStorage.extract(resource, ITEM_TRANSFER_RATE, transaction);
                if (extracted > 0) {
                    long inserted = storage.insert(resource, extracted, transaction);
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
            if (isValidStorageView(storageView)) {
                ItemVariant resource = storageView.getResource();
                Transaction transaction = Transaction.openOuter();
                long extracted = storage.extract(resource, ITEM_TRANSFER_RATE, transaction);

                if (isInvalidResource(resource) || extracted == 0)  {
                    transaction.close();
                    continue;
                }

                long inserted = this.inventoryStorage.insert(resource, extracted, transaction);
                if (inserted > 0) {
                    transaction.commit();
                    return true;
                } else transaction.close();
            }
        }
        return false;
    }

    private boolean isValidStorageView(StorageView<ItemVariant> storageView) {
        return storageView != null && !storageView.isResourceBlank() && storageView.getAmount() > 0;
    }

    private boolean isInvalidResource(ItemVariant resource) {
        return !itemList.isEmpty() && !itemList.contains(resource.getItem());
    }
}
