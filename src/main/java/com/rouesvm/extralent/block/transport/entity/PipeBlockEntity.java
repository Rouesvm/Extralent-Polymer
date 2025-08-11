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
    private boolean connected;
    private int current_connections = 0;

    public final List<Connection> connected_from = new ArrayList<>(10);
    public final List<Connection> connected_to = new ArrayList<>(10);

    private List<Connection> queued_connections = new ArrayList<>(10);

    public PipeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    private int tick = 0;

    @Override
    public void tick(World world, BlockPos pos, BlockState state, BlockEntity entity) {
        if (this.getWorld() == null || this.getWorld().isClient) return;
        if (!canTick()) return;
        if (tick++ % getTickDelay() == 0) return;
        onUpdate();
    }

    public void onUpdate() {
        if (connected_to.isEmpty()) return;
        if (queued_connections.isEmpty()) queued_connections.addAll(connected_to);

        Set<Connection> posToRemove = new HashSet<>();
        Iterator<Connection> iterator = queued_connections.iterator();

        if (iterator.hasNext()) {
            Connection connection = iterator.next();

            if (blockExists(connection.getPos())) {
                if (blockLogic(connection)) iterator.remove();
            } else posToRemove.add(connection);
        }

        if (!posToRemove.isEmpty()) {
            current_connections -= posToRemove.size();
            connected_to.removeAll(posToRemove);
            queued_connections.removeAll(posToRemove);
        }
    }

    public void removeConnected(Connection connection) {
        if (connected_from.contains(connection)) {
            PipeBlockEntity entity = (PipeBlockEntity) world.getBlockEntity(connection.getPos());
            if (entity != null) {
                Connection newConnection = Connection.of(pos);
                entity.removeConnected(newConnection);
                Extralent.HIGHLIGHT_MANAGER.removeHighlightFromMultiple(newConnection, connection.getPos());
            }

            connected_from.remove(connection);
            this.markDirty();
        }
    }

    public void putConnected(Connection connection) {
        if (world == null || world.isClient) return;
        if (connected_from.contains(connection)) return;
        if (!(getPipeAt(connection.getPos()) instanceof PipeBlockEntity)) return;
        if (!correctBlock(connection.getPos())) return;

        connected_from.add(connection);
        markDirty();
    }

    public void removeOtherConnections() {
        if (world == null || connected_from.isEmpty()) return;
        List<Connection> copy = new ArrayList<>(connected_from);
        for (Connection connections : copy) {
            removeConnected(connections);
        }
    }

    public boolean removeConnection(Connection connection) {
        if (connected_to.remove(connection)) {
            if (current_connections > 0) current_connections--;
            refreshOrderedConnections();
            markDirty();
            return true;
        }
        return false;
    }

    public PipeState putConnection(Connection connection) {
        if (world == null || world.isClient) return PipeState.FAIL;
        if (connected_to.contains(connection)) return PipeState.IDENTICAL;
        if (current_connections >= getMaxConnections()) return PipeState.OVERFLOW;

        if (!isWithinRange(connection.getPos())) return PipeState.FAR;
        if (!correctBlock(connection.getPos())) return PipeState.TYPE_ERROR;

        putConnected(connection);
        current_connections++;
        connected_to.add(connection);
        refreshOrderedConnections();
        markDirty();
        return PipeState.SUCCESS;
    }

    public boolean blockExists(BlockPos pos) {
        if (world == null || world.isClient) return false;
        BlockEntity block = world.getBlockEntity(pos);
        return block != null && !block.isRemoved();
    }

    private PipeBlockEntity getPipeAt(BlockPos pos) {
        BlockEntity be = world.getBlockEntity(pos);
        return (be instanceof PipeBlockEntity) ? (PipeBlockEntity) be : null;
    }

    private boolean isWithinRange(BlockPos pos) {
        return getMaxDistance() == 0 || pos.isWithinDistance(this.pos, getMaxDistance());
    }

    private void refreshOrderedConnections() {
        queued_connections = new ArrayList<>(connected_to);
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
    public boolean correctBlock(BlockPos pos) {
        return true;
    }

    @Override
    protected void readData(ReadView data) {
        super.readData(data);
        Connection.read(data, "connected", this.connected_to);
        Connection.read(data, "connected_to", this.connected_from);
        current_connections = data.getInt("connections", 0);
    }

    @Override
    protected void writeData(WriteView data) {
        super.writeData(data);
        Connection.write(data, "connected", this.connected_to);
        Connection.write(data, "connected_to", this.connected_from);
        data.putInt("connections", current_connections);
    }

    public List<Connection> getConnections() {
        return connected_to;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public boolean isConnected() {
        return connected;
    }
}
