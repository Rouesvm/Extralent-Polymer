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
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TransporterBlockEntity extends PipeBlockEntity {
    private static final int ITEM_TRANSFER_RATE = 5;
    public Set<Item> itemList = new HashSet<>();

    public TransporterBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.TRANSPORTER_BLOCK_ENTITY, pos, state);
    }

    @Override
    public ExtralentInventory createInventory() {
        return super.createInventory(1);
    }

    public void setItemList(List<ItemStack> inventory) {
        itemList.clear();
        if (inventory.isEmpty()) return;
        inventory.forEach(stack -> itemList.add(stack.getItem()));
    }

    @Override
    public boolean incorrectBlock(BlockPos blockPos) {
        Storage<ItemVariant> storage = ItemStorage.SIDED.find(this.world, blockPos, null);
        return storage == null || !storage.supportsInsertion();
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

    @Override
    protected void writeData(WriteView data) {
        super.writeData(data);

        WriteView.ListAppender<String> listAppender = data.getListAppender("filter", Codec.STRING);
        itemList.stream()
                .map(item -> Registries.ITEM.getId(item).toString())
                .forEach(listAppender::add);

        if (listAppender.isEmpty()) data.remove("filter");
    }

    @Override
    protected void readData(ReadView data) {
        super.readData(data);

        ReadView.TypedListReadView<String> listReadView = data.getTypedListView("filter", Codec.STRING);
        listReadView.stream().map(id -> id != null ? Registries.ITEM.get(Identifier.of(id)) : null)
                .filter(item -> item != null && item != Items.AIR)
                .forEach(itemList::add);
    }

    public boolean insertItem(Storage<ItemVariant> storage) {
        return transferItems(this.inventoryStorage, storage, false);
    }

    public boolean extractItem(Storage<ItemVariant> storage) {
        return transferItems(storage, this.inventoryStorage, true);
    }

    private boolean transferItems(Storage<ItemVariant> source, Storage<ItemVariant> target, boolean filterSource) {
        long remains = ITEM_TRANSFER_RATE;
        boolean transferred = false;

        for (StorageView<ItemVariant> storageView : source) {
            if (!isValidStorageView(storageView)) continue;

            ItemVariant resource = storageView.getResource();
            if (filterSource && isInvalidResource(resource)) continue;

            try (Transaction transaction = Transaction.openOuter()) {
                long extracted = source.extract(resource, remains, transaction);
                if (extracted > 0) {
                    long inserted = target.insert(resource, extracted, transaction);
                    if (inserted > 0) {
                        transaction.commit();
                        transferred = true;

                        remains -= inserted;
                        if (remains <= 0) break;
                    }
                }
            }
        }

        return transferred;
    }

    private boolean isValidStorageView(StorageView<ItemVariant> storageView) {
        return storageView != null && !storageView.isResourceBlank() && storageView.getAmount() > 0;
    }

    private boolean isInvalidResource(ItemVariant resource) {
        return !itemList.isEmpty() && !itemList.contains(resource.getItem());
    }
}
