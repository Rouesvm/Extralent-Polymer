package com.rouesvm.extralent.block.transport.entity;

import com.rouesvm.extralent.block.entity.BasicMachineBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashSet;
import java.util.Set;

public class PipeBlockEntity extends BasicMachineBlockEntity {
    public int ticks;
    public final Set<BlockPos> blocks = new HashSet<>();

    public PipeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public void onUpdate() {
        if (this.blocks.isEmpty()) return;
        Set<BlockPos> posToRemove = new HashSet<>();
        this.blocks.forEach(pos -> {
            if (blockExists(pos))
                extractBlock(pos);
            else posToRemove.add(pos);
        });
        if (!posToRemove.isEmpty()) posToRemove.forEach(this::removeBlock);
    }

    public boolean removeBlock(BlockPos pos) {
        if (this.blocks.contains(pos)) {
            this.blocks.remove(pos);
            return true;
        }
        return false;
    }

    public PipeState putBlock(BlockPos pos) {
        if (this.blocks.contains(pos)) return PipeState.IDENTICAL;
        if (this.world == null) return PipeState.FAIL;
        if (this.world.isClient) return PipeState.FAIL;

        int maxDist = 5;
        if (pos.isWithinDistance(this.pos, maxDist)) {
            if (correctBlock(pos)) {
                this.blocks.add(pos);
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
    public void extractBlock(BlockPos blockPos) {
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
