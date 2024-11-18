package com.rouesvm.extralent.block.transport.entity;

import com.rouesvm.extralent.block.entity.BasicMachineBlockEntity;
import com.rouesvm.extralent.utils.Connection;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.ApiStatus;

import java.util.*;

public class PipeBlockEntity extends BasicMachineBlockEntity {
    public int ticks;
    public final HashSet<Connection> blocks = new HashSet<>();

    public PipeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public void onUpdate() {
        if (this.blocks.isEmpty()) return;
        Set<Connection> posToRemove = new HashSet<>();
        this.blocks.forEach(connection -> {
            if (blockExists(connection.getPos()))
                blockLogic(connection.getPos());
            else posToRemove.add(connection);
        });
        if (!posToRemove.isEmpty()) posToRemove.forEach(this::removeBlock);
    }

    public boolean removeBlock(Connection connection) {
        if (this.blocks.contains(connection)) {
            this.blocks.remove(connection);
            return true;
        }
        return false;
    }

    public PipeState putBlock(Connection connection) {
        if (this.blocks.contains(connection)) return PipeState.IDENTICAL;
        if (this.world == null) return PipeState.FAIL;
        if (this.world.isClient) return PipeState.FAIL;

        int maxDist = 5;
        if (connection.getPos().isWithinDistance(this.pos, maxDist)) {
            if (correctBlock(connection.getPos())) {
                this.blocks.add(connection);
                return PipeState.SUCCESS;
            } else return PipeState.TYPE_ERROR;
        } else return PipeState.FAR;
    }

    public boolean blockExists(BlockPos blockPos) {
        if (this.world != null && !this.world.isClient) {
            BlockEntity block = this.world.getBlockEntity(blockPos);
            if (block != null && block.isRemoved())
                return false;
            return block != null;
        }
        return false;
    }

    @ApiStatus.OverrideOnly
    public void blockLogic(BlockPos blockPos) {
    }

    @ApiStatus.OverrideOnly
    public boolean correctBlock(BlockPos pos) {
        return true;
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        readConnections(nbt);
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        nbt.putLongArray("storedBlocks", this.blocks.stream().map(Connection::asLong).toList());
    }

    private void readConnections(NbtCompound nbt) {
        if(nbt.contains("storedBlocks", NbtElement.LONG_ARRAY_TYPE)) {
            long[] positions = nbt.getLongArray("storedBlocks");
            for (long connection : positions) {
                this.blocks.add(Connection.fromLong(connection));
            }
        }
    }

    public Set<Connection> getBlocks() {
        return this.blocks;
    }
}
