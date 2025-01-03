package com.rouesvm.extralent.block.transport.entity;

import com.rouesvm.extralent.registries.block.BlockEntityRegistry;
import com.rouesvm.extralent.block.transport.entity.connection.Connection;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.base.SimpleEnergyStorage;

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
        if (this.getWorld() == null || this.getWorld().isClient) return;
        if (this.energyStorage.getCapacity() <= 0) return;
        if (this.getWorld().getTime() % 4 != 0) return;

        super.onUpdate();
    }

    @Override
    public boolean correctBlock(BlockPos blockPos) {
        EnergyStorage storage = EnergyStorage.SIDED.find(this.world, blockPos, null);
        return storage != null && storage.supportsInsertion();
    }

    @Override
    public boolean blockLogic(Connection connection) {
        EnergyStorage storage = EnergyStorage.SIDED.find(this.world, connection.getPos(), null);
        if (storage != null && storage.supportsInsertion()) {
            try (Transaction transaction = Transaction.openOuter()) {
                long extracted = this.energyStorage.extract(energyStorage.maxExtract, transaction);
                long inserted = storage.insert(extracted, transaction);

                this.energyStorage.amount += extracted - inserted;
                transaction.commit();

                return true;
            }
        }
        return false;
    }
}
