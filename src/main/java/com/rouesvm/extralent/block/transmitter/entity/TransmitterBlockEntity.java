package com.rouesvm.extralent.block.transmitter.entity;

import com.rouesvm.extralent.block.entity.BasicPoweredEntity;
import com.rouesvm.extralent.registries.block.BlockEntityRegistry;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.base.SimpleEnergyStorage;

import java.util.HashSet;
import java.util.Set;

public class TransmitterBlockEntity extends BasicPoweredEntity {
    private int ticks;
    private final Set<BlockPos> blocks = new HashSet<>();

    public TransmitterBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.TRANSMITTER_BLOCK_ENTITY, pos, state);
    }

    @Override
    public SimpleEnergyStorage createEnergyStorage() {
        return super.createEnergyStorage(500000, 1000, 1000);
    }

    @Override
    public void tick() {
        if (this.ticks++ % 5 == 0) {
            this.blocks.forEach(block -> {
                if (!insertPowerToBlock(false, block)) removeBlock(block);
            });
        }
    }

    public String putBlock(BlockPos pos) {
        if (this.blocks.contains(pos)) return "SAME";
        if (this.world == null) return "FAIL";
        if (this.world.isClient) return "FAIL";

        int maxDist = 5;
        if (pos.isWithinDistance(this.pos, maxDist)) {
            if (insertPowerToBlock(true, pos)) {
                this.blocks.add(pos);
                return "SUCCESS";
            }
        } else {
            return "FAR";
        }
        return "FAIL";
    }

    public boolean removeBlock(BlockPos pos) {
        if (this.blocks.contains(pos)) {
            this.blocks.remove(pos);
            return true;
        }
        return false;
    }

    private boolean insertPowerToBlock(boolean checkEnergyStorage, BlockPos blockpos) {
        EnergyStorage storage = EnergyStorage.SIDED.find(this.world, blockpos, null);
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
        readConnections(nbt);
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        nbt.putLongArray("blocks", blocks.stream().map(BlockPos::asLong).toList());
    }

    private void readConnections(NbtCompound nbt) {
        if(nbt.contains("blocks", NbtElement.LONG_ARRAY_TYPE)) {
            long[] positions = nbt.getLongArray("blocks");
            for(long blockPos : positions) {
                this.blocks.add(BlockPos.fromLong(blockPos));
            }
        }
    }

    public Set<BlockPos> getBlocks() {
        return this.blocks;
    }
}
