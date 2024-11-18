package com.rouesvm.extralent.block.transport.entity;

import com.rouesvm.extralent.registries.block.BlockEntityRegistry;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.base.SimpleEnergyStorage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TransmitterBlockEntity extends PipeBlockEntity {
    public TransmitterBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.TRANSMITTER_BLOCK_ENTITY, pos, state);
    }

    @Override
    public SimpleEnergyStorage createEnergyStorage() {
        return super.createEnergyStorage(2000, 1000, 1000);
    }

    @Override
    public void tick() {
        if (this.ticks++ % 5 == 0) {
            if (this.energyStorage.getCapacity() <= 0) return;
            super.onUpdate();
        }
    }

    @Override
    public boolean correctBlock(BlockPos blockPos) {
        EnergyStorage storage = EnergyStorage.SIDED.find(this.world, blockPos, null);
        return storage != null && storage.supportsInsertion();
    }

    @Override
    public void blockLogic(BlockPos blockPos) {
        EnergyStorage storage = EnergyStorage.SIDED.find(this.world, blockPos, null);
        if (storage != null && storage.supportsInsertion()) {
            try (Transaction transaction = Transaction.openOuter()) {
                long extracted = this.energyStorage.extract(energyStorage.maxExtract, transaction);
                long inserted = storage.insert(extracted, transaction);

                this.energyStorage.amount += extracted - inserted;
                transaction.commit();
            }
        }
    }
}
