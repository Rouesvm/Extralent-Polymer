package com.rouesvm.extralent.block.transport.entity;

import com.rouesvm.extralent.Extralent;
import com.rouesvm.extralent.block.entity.BasicMachineBlockEntity;
import com.rouesvm.extralent.block.transport.entity.connection.Connection;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.ApiStatus;

import java.util.*;

public class PipeBlockEntity extends BasicMachineBlockEntity {
    private boolean connected_to_connector;
    private int current_connections = 0;

    public final List<Connection> incomingConnections = new ArrayList<>(10);
    public final List<Connection> outgoingConnections = new ArrayList<>(10);

    private final Queue<Connection> queued_connections = new LinkedList<>();

    public PipeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public int tick = 0;

    @Override
    public void tick(World world, BlockPos pos, BlockState state, BlockEntity entity) {
        if (this.getWorld() == null || this.getWorld().isClient()) return;
        if (!canTick()) return;
        if (tick++ % getTickDelay() != 0) return;
        onUpdate();
    }

    private void onUpdate() {
        if (outgoingConnections.isEmpty()) return;
        if (queued_connections.isEmpty()) {
            refreshOrderedConnections();
        }

        Connection connection = queued_connections.poll();

        if (connection == null) return;

        if (!blockExists(connection.getPos())) {
            removeConnection(connection);
            return;
        }

        if (!blockLogic(connection)) queued_connections.offer(connection);
    }

    public void removeIncomingConnection(Connection connection) {
        if (!incomingConnections.contains(connection)) return;

        if (world != null && !world.isClient()) {
            PipeBlockEntity sourceEntity = getPipeAt(connection.getPos());
            if (sourceEntity != null) {
                Connection reverseConnection = Connection.of(this.pos);
                sourceEntity.removeOutgoingConnection(reverseConnection);

                Extralent.HIGHLIGHT_MANAGER.removeHighlightFromMultiple(
                        reverseConnection,
                        connection.getPos()
                );
            }
        }

        incomingConnections.remove(connection);
        this.markDirty();
    }

    public void addIncomingConnection(Connection connection) {
        if (world == null || world.isClient()) return;
        if (incomingConnections.contains(connection)) return;

        PipeBlockEntity sourceEntity = getPipeAt(connection.getPos());
        if (sourceEntity == null) return;
        if (incorrectBlock(connection.getPos())) return;

        incomingConnections.add(connection);
        markDirty();
    }

    private void removeAllIncomingConnections() {
        if (world == null || incomingConnections.isEmpty()) return;

        List<Connection> copy = new ArrayList<>(incomingConnections);
        for (Connection connection : copy) {
            removeIncomingConnection(connection);
        }
    }

    public void removeOutgoingConnection(Connection connection) {
        if (outgoingConnections.remove(connection)) {
            if (current_connections > 0) current_connections--;
            refreshOrderedConnections();
            markDirty();
        }
    }

    public boolean removeConnection(Connection connection) {
        boolean removed = false;

        if (outgoingConnections.remove(connection)) {
            if (current_connections > 0) current_connections--;
            removed = true;
        }

        if (removed) {
            refreshOrderedConnections();
            markDirty();
        }

        return removed;
    }

    public PipeState addConnection(Connection connection) {
        if (world == null || world.isClient()) return PipeState.FAIL;
        if (outgoingConnections.contains(connection)) return PipeState.IDENTICAL;
        if (current_connections >= getMaxConnections()) return PipeState.OVERFLOW;

        if (!isWithinRange(connection.getPos())) return PipeState.FAR;
        if (incorrectBlock(connection.getPos())) return PipeState.TYPE_ERROR;

        PipeBlockEntity targetEntity = getPipeAt(connection.getPos());
        if (targetEntity != null) {
            Connection reverseConnection = Connection.of(this.pos);
            targetEntity.addIncomingConnection(reverseConnection);
        }

        current_connections++;
        outgoingConnections.add(connection);
        refreshOrderedConnections();
        markDirty();

        return PipeState.SUCCESS;
    }

    public boolean blockExists(BlockPos pos) {
        if (world == null || world.isClient()) return false;
        BlockEntity block = world.getBlockEntity(pos);
        return block != null && !block.isRemoved();
    }

    private PipeBlockEntity getPipeAt(BlockPos pos) {
        if (world == null) return null;
        BlockEntity be = world.getBlockEntity(pos);
        return (be instanceof PipeBlockEntity) ? (PipeBlockEntity) be : null;
    }

    private boolean isWithinRange(BlockPos pos) {
        return getMaxDistance() == 0 || pos.isWithinDistance(this.pos, getMaxDistance());
    }

    private void refreshOrderedConnections() {
        queued_connections.clear();
        queued_connections.addAll(outgoingConnections);
    }

    public int getTickDelay() {
        return 5;
    }

    public int getMaxDistance() {
        return 5;
    }

    public int getMaxConnections() {
        return 125;
    }

    public boolean canTick() {
        return true;
    }

    @ApiStatus.OverrideOnly
    public boolean blockLogic(Connection connection) {
        return true;
    }

    @ApiStatus.OverrideOnly
    public boolean incorrectBlock(BlockPos pos) {
        return false;
    }

    @Override
    protected void readData(ReadView data) {
        super.readData(data);
        Connection.read(data, "connected", this.outgoingConnections);
        Connection.read(data, "connected_to", this.incomingConnections);
        current_connections = data.getInt("connections", 0);
    }

    @Override
    protected void writeData(WriteView data) {
        super.writeData(data);
        Connection.write(data, "connected", this.outgoingConnections);
        Connection.write(data, "connected_to", this.incomingConnections);
        data.putInt("connections", current_connections);
    }

    public List<Connection> getOutgoingConnections() {
        return new ArrayList<>(outgoingConnections);
    }

    public List<Connection> getIncomingConnections() {
        return new ArrayList<>(incomingConnections);
    }

    public void setConnected(boolean connected_to_connector) {
        this.connected_to_connector = connected_to_connector;
    }

    public boolean isConnected() {
        return connected_to_connector;
    }

    @Override
    public void markRemoved() {
        removeAllIncomingConnections();

        List<Connection> outgoingCopy = new ArrayList<>(outgoingConnections);
        for (Connection connection : outgoingCopy) {
            PipeBlockEntity targetEntity = getPipeAt(connection.getPos());
            if (targetEntity != null) {
                Connection reverseConnection = Connection.of(this.pos);
                targetEntity.removeOutgoingConnection(reverseConnection);
            }
        }

        outgoingConnections.clear();
        queued_connections.clear();

        super.markRemoved();
    }
}