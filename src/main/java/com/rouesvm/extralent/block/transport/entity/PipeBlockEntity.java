package com.rouesvm.extralent.block.transport.entity;

import com.rouesvm.extralent.block.entity.BasicMachineBlockEntity;
import com.rouesvm.extralent.block.transport.entity.connection.Connection;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.ApiStatus;

import java.util.*;

public class PipeBlockEntity extends BasicMachineBlockEntity {
    private boolean connected;
    private int current_connections = 0;

    public PipeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public final HashSet<Connection> blocks = new HashSet<>(10);
    private LinkedHashSet<Connection> orderedConnections = new LinkedHashSet<>(10);

    public void onUpdate() {
        if (blocks.isEmpty()) return;

        if (!orderedConnections.equals(blocks)) {
            orderedConnections = new LinkedHashSet<>(blocks);
        }

        Set<Connection> posToRemove = new HashSet<>();
        Iterator<Connection> iterator = orderedConnections.iterator();

        while (iterator.hasNext()) {
            Connection connection = iterator.next();

            if (blockExists(connection.getPos())) {
                if (blockLogic(connection)) {
                    iterator.remove();
                    orderedConnections.add(connection);
                    break;
                }
            } else posToRemove.add(connection);
        }

        if (!posToRemove.isEmpty()) {
            blocks.removeAll(posToRemove);
            orderedConnections.removeAll(posToRemove);
        }
    }

    public boolean removeBlock(Connection connection) {
        if (blocks.contains(connection)) {
            if (current_connections > 0) current_connections -= 1;
            blocks.remove(connection);
            orderedConnections = new LinkedHashSet<>(blocks);
            this.markDirty();
            return true;
        }
        return false;
    }

    public PipeState putBlock(Connection connection) {
        if (this.world == null || this.world.isClient) return PipeState.FAIL;
        if (blocks.contains(connection)) return PipeState.IDENTICAL;
        if (!blocks.contains(connection) && current_connections > getMaxConnections() - 1) return PipeState.OVERFLOW;

        if (getMaxDistance() == 0
                || connection.getPos().isWithinDistance(this.pos, getMaxDistance())
        ) {
            if (correctBlock(connection.getPos())) {
                current_connections += 1;
                blocks.add(connection);
                orderedConnections = new LinkedHashSet<>(blocks);
                this.markDirty();
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

    public int getCurrentConnections() {
        return current_connections;
    }

    public int getMaxDistance() {
        return 5;
    }

    public int getMaxConnections() {
        return 125;
    }

    public void setCurrentConnections(int current_connections) {
        this.current_connections = current_connections;
    }

    @ApiStatus.OverrideOnly
    public boolean blockLogic(Connection connection) {
        return true;
    }

    @ApiStatus.OverrideOnly
    public boolean correctBlock(BlockPos pos) {
        return true;
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        Connection.readNbt(nbt, this.blocks, registryLookup);
        current_connections = nbt.getInt("connections");
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        Connection.writeNbt(nbt, this.blocks, registryLookup);
        nbt.putInt("connections", current_connections);
    }

    public Set<Connection> getBlocks() {
        return blocks;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public boolean isConnected() {
        return connected;
    }
}
