package com.rouesvm.extralent.block.transport.entity;

import com.rouesvm.extralent.Extralent;
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

    public final HashSet<Connection> connectedTo = new HashSet<>(10);
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
            current_connections -= posToRemove.size();
            blocks.removeAll(posToRemove);
            orderedConnections.removeAll(posToRemove);
        }
    }

    // To be removed from this block's connected from list.
    public void removeConnection(Connection connection) {
        if (connectedTo.contains(connection)) {
            PipeBlockEntity entity = (PipeBlockEntity) world.getBlockEntity(connection.getPos());
            if (entity != null) {
                Connection newConnection = Connection.of(pos);
                entity.removeBlock(newConnection);
                Extralent.HIGHLIGHT_MANAGER.removeHighlightFromMultiple(newConnection, connection.getPos());
            }

            connectedTo.remove(connection);
            this.markDirty();
        }
    }

    public void removeConnections() {
        if (world == null) return;
        if (connectedTo.isEmpty()) return;
        for (Connection connection : connectedTo) removeConnection(connection);
    }

    // To be added from this block's connected from list.
    public void putConnection(Connection connection) {
        if (this.world == null || this.world.isClient) return;
        if (connectedTo.contains(connection)) return;
        if (!(world.getBlockEntity(connection.getPos()) instanceof PipeBlockEntity)) return;

        if (correctBlock(connection.getPos())) {
            connectedTo.add(connection);
            this.markDirty();
        }
    }

    // To be removed from this block's connected to list.
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

    // To be added from this block's connected to list.
    public PipeState putBlock(Connection connection) {
        if (this.world == null || this.world.isClient) return PipeState.FAIL;
        if (blocks.contains(connection)) return PipeState.IDENTICAL;
        if (!blocks.contains(connection) && current_connections > getMaxConnections() - 1) return PipeState.OVERFLOW;

        if (getMaxDistance() == 0
                || connection.getPos().isWithinDistance(this.pos, getMaxDistance())
        ) {
            if (correctBlock(connection.getPos())) {
                putConnection(connection);
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

    public int getMaxDistance() {
        return 5;
    }

    public int getMaxConnections() {
        return 125;
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
        Connection.readNbt(nbt, this.connectedTo, registryLookup);
        current_connections = nbt.getInt("connections");
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        Connection.writeNbt(nbt, this.blocks, registryLookup);
        Connection.writeNbt(nbt, this.connectedTo, registryLookup);
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
