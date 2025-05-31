package com.rouesvm.extralent.item.custom.data;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Uuids;

import java.util.UUID;

public class BasicData {
    public NbtCompound nbtCompound;
    private final ItemStack stack;

    public BasicData(ItemStack stack) {
        this.stack = stack;
        this.nbtCompound = getStackNbt();
    }

    public static UUID getUuid(ItemStack stack) {
        UUID uuid = UUID.randomUUID();
        NbtCompound nbtCompound = getStackNbt(stack);
        if (nbtCompound.contains("uuid"))
            uuid = nbtCompound.get("uuid", Uuids.CODEC).get();
        else {
            nbtCompound.put("uuid", Uuids.CODEC, uuid);
            saveToStack(stack, nbtCompound);
        }
        return uuid;
    }

    public ItemStack stack() {
        return stack;
    }

    public void removeFromNbt(String name) {
        removeFromNbt(stack, name);
    }

    public static void removeFromNbt(ItemStack stack, String name) {
        NbtCompound compound = getStackNbt(stack);
        compound.remove(name);
        saveToStack(stack, compound);
    }

    public void saveToStack() {
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbtCompound));
    }

    public static void saveToStack(ItemStack stack, NbtCompound compound) {
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(compound));
    }

    public NbtCompound getStackNbt() {
        NbtComponent stackCompound = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT);
        this.nbtCompound = stackCompound.copyNbt();
        return nbtCompound;
    }

    public static NbtCompound getStackNbt(ItemStack stack) {
        return stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();
    }
}
