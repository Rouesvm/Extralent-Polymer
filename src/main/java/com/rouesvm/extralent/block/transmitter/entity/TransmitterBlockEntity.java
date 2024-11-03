package com.rouesvm.extralent.block.transmitter.entity;

import com.rouesvm.extralent.interfaces.block.TickableBlockEntity;
import com.rouesvm.extralent.registries.block.BlockEntityRegistry;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.base.SimpleEnergyStorage;

import java.util.HashSet;
import java.util.Set;

public class TransmitterBlockEntity extends BlockEntity implements TickableBlockEntity {
    private int ticks;
    private final int maxDist = 5;
    private Set<BlockPos> blocks = new HashSet<>();

    private final SimpleEnergyStorage energyStorage = new SimpleEnergyStorage(100_000, 1_000, 1_000) {
        @Override
        protected void onFinalCommit() {
            super.onFinalCommit();
            markDirty();
        }
    };

    public TransmitterBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.TRANSMITTER_BLOCK_ENTITY, pos, state);
    }

    @Override
    public void tick() {
        if (this.ticks++ % 5 == 0) {
            this.blocks.forEach(block -> insertPowerToBlock(false, block));
        }
    }

    public void putBlock(BlockPos pos) {
        if (this.blocks.contains(pos)) return;

        if (pos.isWithinDistance(this.pos, maxDist)) {
            if (this.world != null && !this.world.isClient) {
                if (insertPowerToBlock(true, pos)) {
                    this.blocks.add(pos);
                }
            }
        }
    }

    private boolean insertPowerToBlock(boolean checkEnergyStorage, BlockPos blockpos) {
        EnergyStorage storage = EnergyStorage.SIDED.find(this.world, blockpos, Direction.UP);
        if (storage != null && storage.supportsInsertion()) {
            if (checkEnergyStorage) return true;

            try (Transaction transaction = Transaction.openOuter()) {
                long extracted = this.energyStorage.extract(energyStorage.maxExtract, transaction);
                long inserted = storage.insert(extracted, transaction);

                this.energyStorage.amount += extracted - inserted;
                transaction.commit();
                return true;
            }
        } else {
            return checkEnergyStorage;
        }
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        if (nbt.contains("energy", NbtElement.LONG_TYPE)) {
            energyStorage.amount = nbt.getLong("energy");
        }
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        nbt.putLong("energy", this.energyStorage.amount);
    }

    public SimpleEnergyStorage getEnergyStorage() {
        return this.energyStorage;
    }

    public @Nullable EnergyStorage getEnergyProvider(@Nullable Direction direction) {
        return this.energyStorage;
    }
}
