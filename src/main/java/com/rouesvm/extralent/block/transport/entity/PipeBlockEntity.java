package com.rouesvm.extralent.block.transport.entity;

import com.rouesvm.extralent.block.entity.BasicMachineBlockEntity;
import com.rouesvm.extralent.utils.Connection;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.ApiStatus;

import java.util.*;

public class PipeBlockEntity extends BasicMachineBlockEntity {
    private boolean connected;

    public int ticks;
    public final HashSet<Connection> blocks = new HashSet<>();

    public PipeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    private LinkedHashSet<Connection> orderedConnections = new LinkedHashSet<>();

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
            blocks.remove(connection);
            orderedConnections = new LinkedHashSet<>(blocks);
            return true;
        }
        return false;
    }

    public PipeState putBlock(Connection connection) {
        if (blocks.contains(connection)) return PipeState.IDENTICAL;
        if (this.world == null) return PipeState.FAIL;
        if (this.world.isClient) return PipeState.FAIL;

        int maxDist = 5;
        if (connection.getPos().isWithinDistance(this.pos, maxDist)) {
            if (correctBlock(connection.getPos())) {
                blocks.add(connection);
                orderedConnections = new LinkedHashSet<>(blocks);
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
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        Connection.writeNbt(nbt, this.blocks, registryLookup);
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
