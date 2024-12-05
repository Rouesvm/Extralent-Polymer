package com.rouesvm.extralent.item.custom.data;

import com.rouesvm.extralent.block.transport.entity.PipeBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class ConnecterData {
    private NbtCompound nbtCompound;

    private boolean visual = false;

    private int weight = 0;
    private BlockPos currentEntity;

    private final ItemStack stack;

    public ConnecterData(ItemStack stack) {
        this.stack = stack;
        this.nbtCompound = getStackNbt();
    }

    public PipeBlockEntity getCurrentEntity(ServerWorld world) {
        if (getBlockPos() == null) return null;
        BlockEntity state = world.getBlockEntity(getBlockPos());

        if (state == null) return null;
        else if (state instanceof PipeBlockEntity blockEntity) return blockEntity;
        else return null;
    }

    public ItemStack getStack() {
        return stack;
    }

    public boolean showVisual() {
        if (nbtCompound.contains("visual"))
            visual = nbtCompound.getBoolean("visual");
        return visual;
    }

    public int getWeight() {
        if (nbtCompound.contains("weight"))
            weight = nbtCompound.getInt("weight");
        return weight;
    }

    public BlockPos getBlockPos() {
        if (nbtCompound.contains("blockPos")) {
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

    public void removeFromNbt(String name) {
        nbtCompound.remove(name);
        saveToStack();
    }

    private void saveToStack() {
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbtCompound));
    }

    private NbtCompound getStackNbt() {
        NbtComponent stackCompound = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT);
        this.nbtCompound = stackCompound.copyNbt();
        return nbtCompound;
    }
}
