package com.rouesvm.extralent.block.generator.entity;

import com.rouesvm.extralent.block.ActivatedPolymerBlock;
import com.rouesvm.extralent.block.MachineBlock;
import com.rouesvm.extralent.block.entity.BasicMachineBlockEntity;
import com.rouesvm.extralent.registries.block.BlockEntityRegistry;
import com.rouesvm.extralent.visual.ui.inventory.ExtralentInventory;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.EnergyStorageUtil;
import team.reborn.energy.api.base.SimpleEnergyStorage;

public class GeneratorBlockEntity extends BasicMachineBlockEntity {
    public static final double base_energy_produced_per_tick = 2.5;

    private int current_burn_time = 0;
    private double energy_buffer = 0;

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
                return isFuel(stack);
            }

            @Override
            public boolean canExtract(int slot, ItemStack stack, Direction dir) {
                return slot == INPUT_SLOT_INDEX;
            }
        };
    }

    @Override
    public SimpleEnergyStorage createEnergyStorage() {
        return super.createEnergyStorage(50_000, 0, 1_000);
    }

    @Override
    public void tick(World world, BlockPos pos, BlockState state, BlockEntity entity) {
        if (this.world == null || this.world.isClient) return;
        if (energyStorage.amount >= energyStorage.capacity) return;

        boolean stateChanged = false;

        if (current_burn_time == 0) {
            validFuel();

            if (current_burn_time > 0) {
                state = state.with(ActivatedPolymerBlock.ACTIVATED, true);
                stateChanged = true;
            }
        }

        if (progress < current_burn_time) {
            if (progress % 40 == 0) {
                world.playSound(null, pos, SoundEvents.BLOCK_BLASTFURNACE_FIRE_CRACKLE, SoundCategory.BLOCKS, 1.0F, 0.95F);
            }

            progress++;
            energy_buffer += base_energy_produced_per_tick;
                
            if (energy_buffer >= 1.0) {
                final long energyToAdd = (long) energy_buffer;
                energy_buffer -= energyToAdd;

                energyStorage.amount = MathHelper.clamp(
                        energyStorage.amount + energyToAdd,
                        0, energyStorage.getCapacity()
                );
            }
        } else if (progress != 0 || current_burn_time != 0) {
            progress = 0;
            current_burn_time = 0;

            state = state.with(ActivatedPolymerBlock.ACTIVATED, false);
            stateChanged = true;
        }

        extractEnergy();
        if (stateChanged) {
            world.setBlockState(pos, state);
            markDirty(world, pos, state);
        }
    }

    private void extractEnergy() {
        if (energyStorage.amount <= 0) return;

        ItemStack stack = getInventory().getStack(CHARGING_SLOT_INDEX);
        if (!stack.isEmpty() && EnergyStorageUtil.isEnergyStorage(stack)) {
            EnergyStorageUtil.move(
                    getEnergyProvider(null),
                    ContainerItemContext.ofSingleSlot(getInventoryProvider(null).getSlot(CHARGING_SLOT_INDEX)).find(EnergyStorage.ITEM),
                    this.energyStorage.maxExtract,
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

    public boolean isFuel(ItemStack stack) {
        var burning = world.getFuelRegistry().getFuelTicks(stack);
        return burning != 0;
    }

    public Integer getBurnTime(ItemStack item) {
        return world.getFuelRegistry().getFuelTicks(item);
    }

    public void validFuel() {
        ItemStack fuelStack = this.inventory.getStack(0);
        if (this.progress == 0 && !fuelStack.isEmpty()) {
            var burning = getBurnTime(fuelStack);
            if (burning != null && burning != 0) {
                fuelStack.decrement(1);
                this.inventory.setStack(0, fuelStack);
                this.current_burn_time = burning;
            }
        }
    }

    @Override
    protected void readData(ReadView data) {
        super.readData(data);
        this.current_burn_time = data.getInt("burnTime", 0);
    }

    @Override
    protected void writeData(WriteView data) {
        super.writeData(data);
        data.putInt("burnTime", this.current_burn_time);
    }
}
