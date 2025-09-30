package com.rouesvm.extralent.block.transport.entity;

import com.mojang.serialization.Codec;
import com.rouesvm.extralent.registries.block.BlockEntityRegistry;
import com.rouesvm.extralent.block.transport.entity.connection.Connection;
import com.rouesvm.extralent.visual.ui.inventory.ExtralentInventory;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.*;

public class TransporterBlockEntity extends PipeBlockEntity {
    private static final int ITEM_TRANSFER_RATE = 5;

    private final Set<Item> itemFilter = new HashSet<>();

    public TransporterBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.TRANSPORTER_BLOCK_ENTITY, pos, state);
    }

    @Override
    public ExtralentInventory createInventory() {
        return super.createInventory(1);
    }

    public void setItemFilter(List<ItemStack> inventory) {
        itemFilter.clear();
        if (inventory == null || inventory.isEmpty()) return;

        inventory.stream()
                .filter(stack -> stack != null && !stack.isEmpty())
                .map(ItemStack::getItem)
                .filter(item -> item != null && item != Items.AIR)
                .forEach(itemFilter::add);
    }

    public Set<Item> getItemFilter() {
        return new HashSet<>(itemFilter);
    }

    public boolean hasFilter() {
        return !itemFilter.isEmpty();
    }

    @Override
    public boolean incorrectBlock(BlockPos blockPos) {
        if (world == null || world.isClient) return true;
        Storage<ItemVariant> storage = ItemStorage.SIDED.find(world, blockPos, null);

        if (storage == null) {
            for (Direction direction : Direction.values()) {
                storage = ItemStorage.SIDED.find(world, blockPos, direction);
                if (storage != null) break;
            }
        }

        boolean test = storage == null || (!storage.supportsInsertion() && !storage.supportsExtraction());
        System.out.println(test);
        return test;
    }

    @Override
    public boolean blockLogic(Connection connection) {
        if (world == null || world.isClient) return false;

        Storage<ItemVariant> storage = ItemStorage.SIDED.find(world, connection.getPos(), connection.getSide());
        if (storage == null) return false;

        boolean success = false;

        if (connection.getWeight() == 1 && storage.supportsInsertion()) {
            success = insertItem(storage);
        } else if (storage.supportsExtraction()) {
            success = extractItem(storage);
        }

        return success;
    }

    @Override
    protected void writeData(WriteView data) {
        super.writeData(data);

        WriteView.ListAppender<String> listAppender = data.getListAppender("filter", Codec.STRING);
        itemFilter.stream()
                .map(item -> Registries.ITEM.getId(item).toString())
                .forEach(listAppender::add);

        if (listAppender.isEmpty()) {
            data.remove("filter");
        }
    }

    @Override
    protected void readData(ReadView data) {
        super.readData(data);

        itemFilter.clear();
        ReadView.TypedListReadView<String> listReadView = data.getTypedListView("filter", Codec.STRING);
        listReadView.stream()
                .filter(Objects::nonNull)
                .map(id -> {
                    try {
                        return Registries.ITEM.get(Identifier.of(id));
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(item -> item != null && item != Items.AIR)
                .forEach(itemFilter::add);
    }

    public boolean insertItem(Storage<ItemVariant> storage) {
        if (storage == null || !storage.supportsInsertion()) return false;
        return transferItems(this.inventoryStorage, storage, false);
    }

    public boolean extractItem(Storage<ItemVariant> storage) {
        if (storage == null || !storage.supportsExtraction()) return false;
        return transferItems(storage, this.inventoryStorage, true);
    }

    private boolean transferItems(Storage<ItemVariant> source, Storage<ItemVariant> target, boolean applyFilter) {
        if (source == null || target == null) return false;

        long remainingTransferCapacity = ITEM_TRANSFER_RATE;
        boolean anyTransferred = false;

        try (Transaction transaction = Transaction.openOuter()) {
            for (StorageView<ItemVariant> storageView : source) {
                if (remainingTransferCapacity <= 0) break;
                if (!isValidStorageView(storageView)) continue;

                ItemVariant resource = storageView.getResource();
                if (applyFilter && !passesFilter(resource)) continue;

                long availableAmount = storageView.getAmount();
                long maxTransferable = Math.min(remainingTransferCapacity, availableAmount);

                if (maxTransferable <= 0) continue;

                long actualExtracted = source.extract(resource, maxTransferable, transaction);
                if (actualExtracted <= 0) continue;

                long actualInserted = target.insert(resource, actualExtracted, transaction);

                if (actualInserted > 0) {
                    remainingTransferCapacity -= actualInserted;
                    anyTransferred = true;
                }

                if (actualInserted < actualExtracted) {
                    long remainder = actualExtracted - actualInserted;
                    source.insert(resource, remainder, transaction);
                }
            }

            if (anyTransferred) {
                transaction.commit();
            }
        }

        return anyTransferred;
    }

    private boolean isValidStorageView(StorageView<ItemVariant> storageView) {
        return storageView != null
                && !storageView.isResourceBlank()
                && storageView.getAmount() > 0;
    }

    private boolean passesFilter(ItemVariant resource) {
        if (itemFilter.isEmpty()) return true;
        return resource != null && itemFilter.contains(resource.getItem());
    }

    @Override
    public void markRemoved() {
        itemFilter.clear();
        super.markRemoved();
    }
}