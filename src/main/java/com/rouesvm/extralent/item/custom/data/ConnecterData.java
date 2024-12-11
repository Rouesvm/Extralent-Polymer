package com.rouesvm.extralent.item.custom.data;

import com.rouesvm.extralent.block.transport.entity.PipeBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class ConnecterData extends BasicData {
    private boolean visual = false;

    private int weight = 0;
    private BlockPos currentEntity;

    public ConnecterData(ItemStack stack) {
        super(stack);
    }

    public PipeBlockEntity getCurrentEntity(ServerWorld world) {
        if (getBlockPos() == null) return null;
        BlockEntity state = world.getBlockEntity(getBlockPos());

        if (state == null) return null;
        else if (state instanceof PipeBlockEntity blockEntity) return blockEntity;
        else return null;
    }

    public boolean showVisual() {
        if (getStackNbt().contains("visual"))
            visual = nbtCompound.getBoolean("visual");
        return visual;
    }

    public int getWeight() {
        if (getStackNbt().contains("weight"))
            weight = nbtCompound.getInt("weight");
        return weight;
    }

    public BlockPos getBlockPos() {
        if (getStackNbt().contains("blockPos")) {
            long data = nbtCompound.getLong("blockPos");
            currentEntity = BlockPos.fromLong(data);
        } else currentEntity = null;
        return currentEntity;
    }

    public void setVisual(boolean visual) {
        nbtCompound.putBoolean("visual", visual);
        saveToStack();
        this.visual = visual;
    }

    public void setCurrentEntity(BlockPos currentEntity) {
        if (currentEntity == null) {
            removeFromNbt("blockPos");
            return;
        }
        nbtCompound.putLong("blockPos", currentEntity.asLong());
        saveToStack();
        this.currentEntity = currentEntity;
    }

    public void setWeight(int weight) {
        nbtCompound.putInt("weight", weight);
        saveToStack();
        this.weight = weight;
    }
}
