package com.rouesvm.extralent.block.generator.entity;

import com.rouesvm.extralent.interfaces.block.TickableBlockEntity;
import com.rouesvm.extralent.registries.block.BlockEntityRegistry;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.base.SimpleEnergyStorage;

public class GeneratorBlockEntity extends BlockEntity implements TickableBlockEntity {
    private int progress;
    private int burnTime;

    private final SimpleEnergyStorage energyStorage = new SimpleEnergyStorage(100_000, 0, 1_000) {
        @Override
        protected void onFinalCommit() {
            super.onFinalCommit();
            markDirty();
        }
    };
    private final SimpleInventory inventory = new SimpleInventory(1) {
        @Override
        public void markDirty() {
            super.markDirty();
            update();
        }
    };

    private final InventoryStorage inventoryStorage = InventoryStorage.of(inventory, null);

    public GeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.GENERATOR_BLOCK_ENTITY, pos, state);
    }

    @Override
    public void tick() {
        if (this.world == null || this.world.isClient) return;
        if (energyStorage.amount < energyStorage.capacity) {
            if (this.burnTime == 0) {
                validFuel();
                markDirty();
            }
            if (this.progress++ < this.burnTime) {
                energyStorage.amount = MathHelper.clamp(energyStorage.amount + this.burnTime / 80, 0, energyStorage.getCapacity());
            } else {
                this.progress = 0;
                this.burnTime = 0;
            }
        }
        extractEnergy();
    }

    public void extractEnergy() {
        for (Direction direction : Direction.values()) {
            EnergyStorage storage = EnergyStorage.SIDED.find(this.world, this.pos.offset(direction), direction.getOpposite());
            if (storage != null && storage.supportsInsertion()) {
                try(Transaction transaction = Transaction.openOuter()) {
                    long extracted = this.energyStorage.extract(energyStorage.maxExtract, transaction);
                    long inserted = storage.insert(extracted, transaction);
                    this.energyStorage.amount += extracted - inserted;
                    transaction.commit();
                }
            }
        }
    }

    public void validFuel() {
        ItemStack fuelStack = this.inventory.getStack(0);
        if (this.progress == 0 && !fuelStack.isEmpty()) {
            Item fuelItem = fuelStack.getItem();
            var burning = FuelRegistry.INSTANCE.get(fuelItem);
            if (burning != null) {
                fuelStack.decrement(1);
                this.inventory.setStack(0, fuelStack);
                this.burnTime = burning;
            }
        }
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        this.progress = nbt.getInt("progress");
        this.burnTime = nbt.getInt("burnTime");
        Inventories.readNbt(nbt, this.inventory.getHeldStacks(), registryLookup);

        if (nbt.contains("energy", NbtElement.LONG_TYPE)) {
            energyStorage.amount = nbt.getLong("energy");
        }
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        nbt.putInt("progress", this.progress);
        nbt.putInt("burnTime", this.burnTime);
        nbt.putLong("energy", this.energyStorage.amount);
        Inventories.writeNbt(nbt, this.inventory.getHeldStacks(), registryLookup);
    }

    private void update() {
        markDirty();
        if (world != null)
            world.updateListeners(pos, getCachedState(), getCachedState(), Block.NOTIFY_ALL);
    }

    public InventoryStorage getInventoryProvider(Direction direction) {
        return this.inventoryStorage;
    }

    public SimpleInventory getInventory() {
        return this.inventory;
    }

    public SimpleEnergyStorage getEnergyProvider(Direction direction) {
        return this.energyStorage;
    }

    public SimpleEnergyStorage getEnergyStorage() {
        return this.energyStorage;
    }

    public Text getProgress() {
        return Text.of(this.progress + "/" + this.burnTime);
    }
}
