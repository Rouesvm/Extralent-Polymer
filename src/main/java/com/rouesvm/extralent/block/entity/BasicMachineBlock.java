package com.rouesvm.extralent.block.entity;

import com.rouesvm.extralent.interfaces.block.TickableBlockEntity;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import team.reborn.energy.api.base.SimpleEnergyStorage;

public class BasicMachineBlock extends BlockEntity implements TickableBlockEntity {
    public final SimpleInventory inventory;
    public final InventoryStorage inventoryStorage;

    public final SimpleEnergyStorage energyStorage;

    public BasicMachineBlock(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);

        this.energyStorage = createEnergyStorage();
        this.inventory = createInventory();

        if (this.inventory != null)
            this.inventoryStorage = InventoryStorage.of(inventory, null);
        else this.inventoryStorage = null;
    }

    public SimpleInventory createInventory() {
        return null;
    }

    public SimpleInventory createInventory(int size) {
        return new SimpleInventory(size) {
            @Override
            public void markDirty() {
                super.markDirty();
                update();
            }
        };
    }

    public SimpleEnergyStorage createEnergyStorage() {
        return null;
    }

    public SimpleEnergyStorage createEnergyStorage(int capacity, int maxInsert, int maxExtract) {
        return new SimpleEnergyStorage(capacity, maxInsert, maxExtract) {
            @Override
            protected void onFinalCommit() {
                super.onFinalCommit();
                markDirty();
            }
        };
    }

    @Override
    public void tick() {}

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);

        if (this.inventory != null)
            Inventories.readNbt(nbt, this.inventory.getHeldStacks(), registryLookup);
        if (this.energyStorage != null) {
            if (nbt.contains("energy", NbtElement.LONG_TYPE)) {
                this.energyStorage.amount = nbt.getLong("energy");
            }
        }
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);

        if (this.inventory != null)
            Inventories.writeNbt(nbt, this.inventory.getHeldStacks(), registryLookup);
        if (this.energyStorage != null)
            nbt.putLong("energy", this.energyStorage.amount);
    }

    private void update() {
        markDirty();
        if (world != null)
            world.updateListeners(pos, getCachedState(), getCachedState(), Block.NOTIFY_ALL);
    }

    public SimpleEnergyStorage getEnergyProvider(Direction direction) {
        return this.energyStorage;
    }

    public SimpleEnergyStorage getEnergyStorage() {
        return this.energyStorage;
    }

    public InventoryStorage getInventoryProvider(Direction direction) {
        return this.inventoryStorage;
    }

    public SimpleInventory getInventory() {
        return this.inventory;
    }
}
