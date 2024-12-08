package com.rouesvm.extralent.block.generator.entity;

import com.rouesvm.extralent.block.MachineBlock;
import com.rouesvm.extralent.block.entity.BasicMachineBlockEntity;
import com.rouesvm.extralent.registries.block.BlockEntityRegistry;
import com.rouesvm.extralent.ui.inventory.ExtralentInventory;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.base.SimpleEnergyStorage;

public class GeneratorBlockEntity extends BasicMachineBlockEntity {
    private int progress;
    private int burnTime;

    public GeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.GENERATOR_BLOCK_ENTITY, pos, state);
    }

    @Override
    public ExtralentInventory createInventory() {
        return super.createInventory(1);
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
            var burning = FuelRegistry.INSTANCE.get(fuelStack.getItem());
            if (burning != 0) {
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
