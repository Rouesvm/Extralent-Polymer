package com.rouesvm.extralent.item.custom.info;

import com.rouesvm.extralent.item.custom.data.BasicData;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;

public class InfoData extends BasicData {
    protected InfoData(ItemStack stack) {
        super(stack);
    }

    public static BlockPos getBlockPos(ItemStack stack) {
        NbtCompound compound = getStackNbt(stack);
        if (compound.contains("blockPos")) {
            long data = compound.getLong("blockPos", 0);
            return BlockPos.fromLong(data);
        } else return null;
    }

    public static DISPLAY getDisplay(ItemStack stack) {
        NbtCompound compound = getStackNbt(stack);
        return DISPLAY.valueOf(compound.getString("display_visual", DISPLAY.FLOATING.toString()));
    }

    public static CONTENT_DISPLAY getContent(ItemStack stack) {
        NbtCompound compound = getStackNbt(stack);
        return CONTENT_DISPLAY.valueOf(compound.getString("content_visual", CONTENT_DISPLAY.MACHINE.toString()));
    }

    public static boolean setBlockPos(ItemStack stack, BlockPos pos) {
        NbtCompound compound = getStackNbt(stack);

        if (pos == null) {
            removeFromNbt(stack, "blockPos");
            return false;
        } else if (pos.asLong() == compound.getLong("blockPos", 0)) {
            return false;
        }

        compound.putLong("blockPos", pos.asLong());
        saveToStack(stack, compound);
        return true;
    }

    public static void setDisplay(ItemStack stack, DISPLAY display) {
        NbtCompound compound = getStackNbt(stack);
        compound.putString("display_visual", display.toString());
        saveToStack(stack, compound);
    }

    public static void nextContent(ItemStack stack) {
        CONTENT_DISPLAY[] contents = CONTENT_DISPLAY.values();
        int nextIndex = (getContent(stack).ordinal() + 1) % contents.length;
        setContent(stack, contents[nextIndex]);
    }

    public static void setContent(ItemStack stack, CONTENT_DISPLAY content) {
        NbtCompound compound = getStackNbt(stack);
        compound.putString("content_visual", content.toString());
        saveToStack(stack, compound);
    }

    public enum DISPLAY {
        FLOATING,
        UI
    }

    public enum CONTENT_DISPLAY {
        INVENTORY,
        ENERGY,
        MACHINE
    }
}
