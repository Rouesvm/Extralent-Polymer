package com.rouesvm.extralent.item.custom.connector;

import com.rouesvm.extralent.block.transport.entity.PipeBlockEntity;
import com.rouesvm.extralent.item.custom.data.BasicData;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class ConnectorData extends BasicData {
    private int weight = 0;
    private BlockPos currentEntity;

    public ConnectorData(ItemStack stack) {
        super(stack);
    }

    public PipeBlockEntity getCurrentEntity(ServerWorld world) {
        if (getBlockPos() == null) return null;
        BlockEntity state = world.getBlockEntity(getBlockPos());

        if (state == null) return null;
        else if (state instanceof PipeBlockEntity blockEntity) return blockEntity;
        else return null;
    }

    public static PipeBlockEntity getCurrentEntity(ServerWorld world, ItemStack stack) {
        NbtCompound compound = getStackNbt(stack);
        if (compound.contains("blockPos")) {
            long data = compound.getLong("blockPos", 0);
            BlockPos blockPos =  BlockPos.fromLong(data);
            BlockEntity state = world.getBlockEntity(blockPos);

            if (state == null) return null;
            else if (state instanceof PipeBlockEntity blockEntity) return blockEntity;
            else return null;
        } else return null;
    }

    public static BlockPos getBlockPos(ItemStack stack) {
        NbtCompound compound = getStackNbt(stack);
        if (compound.contains("blockPos")) {
            long data = compound.getLong("blockPos", 0);
            return BlockPos.fromLong(data);
        } else return null;
    }

    public static boolean getVisual(ItemStack stack) {
        return getStackNbt(stack).getBoolean("visual", false);
    }

    public static void setVisual(ItemStack stack, boolean visual) {
        NbtCompound compound = getStackNbt(stack);
        compound.putBoolean("visual", visual);
        saveToStack(stack, compound);
    }

    public int getWeight() {
        if (getStackNbt().contains("weight"))
            weight = nbtCompound.getInt("weight", 0);
        return weight;
    }

    public BlockPos getBlockPos() {
        return getBlockPos(stack());
    }

    public void setVisual(boolean visual) {
        nbtCompound.putBoolean("visual", visual);
        saveToStack();
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
