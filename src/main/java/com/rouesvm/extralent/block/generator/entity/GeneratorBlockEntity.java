package com.rouesvm.extralent.block.generator.entity;

import com.rouesvm.extralent.block.MachineBlock;
import com.rouesvm.extralent.block.entity.BasicMachineBlockEntity;
import com.rouesvm.extralent.registries.block.BlockEntityRegistry;
import com.rouesvm.extralent.ui.inventory.ExtralentInventory;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.EnergyStorageUtil;
import team.reborn.energy.api.base.SimpleEnergyStorage;

public class GeneratorBlockEntity extends BasicMachineBlockEntity {
    private int progress;
    private int burnTime;

    public static final int INPUT_SLOT_INDEX = 0;
    public static final int CHARGING_SLOT_INDEX = 1;

    private static final int[] INPUT_SLOTS_ARRAY = {0};

    public GeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.GENERATOR_BLOCK_ENTITY, pos, state);
    }

    @Override
    public ExtralentInventory createInventory() {
        return new ExtralentInventory(2) {
            @Override
            public void markDirty() {
                super.markDirty();
                update();
            }

            @Override
            public int[] getAvailableSlots(Direction side) {
                return INPUT_SLOTS_ARRAY;
            }

            @Override
            public boolean isValid(int slot, ItemStack stack) {
                return canInsert(slot, stack, null);
            }

            @Override
            public boolean canInsert(int slot, ItemStack stack, Direction dir) {
                if (slot != INPUT_SLOT_INDEX)
                    return EnergyStorageUtil.isEnergyStorage(stack);
                return isFuel(stack.getItem());
            }

            @Override
            public boolean canExtract(int slot, ItemStack stack, Direction dir) {
                return slot == INPUT_SLOT_INDEX;
            }
        };
    }

    @Override
    public SimpleEnergyStorage createEnergyStorage() {
        return super.createEnergyStorage(250000, 0, 1000);
    }

    @Override
    public void tick() {
        if (this.world == null || this.world.isClient) return;

        if (energyStorage.amount < energyStorage.capacity) {
            Block machineBlock = getCachedState().getBlock();
            if (!(machineBlock instanceof MachineBlock machineBaseBlock)) return;

            if (this.burnTime == 0) {
                machineBaseBlock.setState(true, world, pos);
                validFuel();
                markDirty();
            }

            if (this.progress++ < this.burnTime) {
                energyStorage.amount = MathHelper.clamp(energyStorage.amount + this.burnTime / 80, 0, energyStorage.getCapacity());
            } else {
                machineBaseBlock.setState(false, world, pos);
                markDirty();
                this.progress = 0;
                this.burnTime = 0;
            }
        }

        extractEnergy();
    }

    private void extractEnergy() {
        if (energyStorage.amount <= 0) return;

        ItemStack stack = getInventory().getStack(CHARGING_SLOT_INDEX);
        if (!stack.isEmpty() && EnergyStorageUtil.isEnergyStorage(stack)) {
            EnergyStorageUtil.move(
                    getEnergyProvider(null),
                    ContainerItemContext.ofSingleSlot(getInventoryProvider(null).getSlot(CHARGING_SLOT_INDEX)).find(EnergyStorage.ITEM),
                    getEnergyStorage().maxExtract,
                    null
            );
        }

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

    public boolean isFuel(Item item) {
        var burning = FuelRegistry.INSTANCE.get(item);
        return burning != null && burning != 0;
    }

    public void validFuel() {
        ItemStack fuelStack = this.inventory.getStack(0);
        if (this.progress == 0 && !fuelStack.isEmpty()) {
            var burning = FuelRegistry.INSTANCE.get(fuelStack.getItem());
            if (burning != null && burning != 0) {
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
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        nbt.putInt("progress", this.progress);
        nbt.putInt("burnTime", this.burnTime);
    }
}
