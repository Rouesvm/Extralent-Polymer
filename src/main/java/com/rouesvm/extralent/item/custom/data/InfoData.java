package com.rouesvm.extralent.item.custom.data;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

public class InfoData extends BasicData {
    private DISPLAY display = DISPLAY.FLOATING;
    private CONTENT content = CONTENT.ENERGY;

    private boolean visual = false;
    private BlockPos pos;

    public InfoData(ItemStack stack) {
        super(stack);
    }

    public boolean showVisual() {
        if (getStackNbt().contains("visual"))
            visual = nbtCompound.getBoolean("visual");
        return visual;
    }

    public void setVisual(boolean visual) {
        nbtCompound.putBoolean("visual", visual);
        saveToStack();
        this.visual = visual;
    }

    public BlockPos getBlockPos() {
        if (getStackNbt().contains("blockPos")) {
            long data = nbtCompound.getLong("blockPos");
            pos = BlockPos.fromLong(data);
        } else pos = null;
        return pos;
    }

    public boolean setBlockPos(BlockPos pos) {
        if (pos == null) {
            removeFromNbt("blockPos");
            return false;
        } else if (pos.asLong() == nbtCompound.getLong("blockPos"))
            return false;

        nbtCompound.putLong("blockPos", pos.asLong());
        saveToStack();
        this.pos = pos;
        return true;
    }

    public DISPLAY getDisplay() {
        if (getStackNbt().contains("display_visual"))
            display = DISPLAY.valueOf(nbtCompound.getString("display_visual"));
        return display;
    }

    public void setDisplay(DISPLAY display) {
        nbtCompound.putString("display_visual", display.toString());
        saveToStack();
        this.display = display;
    }

    public CONTENT getContent() {
        if (getStackNbt().contains("content_visual"))
            content = CONTENT.valueOf(nbtCompound.getString("content_visual"));
        return content;
    }

    public void setContent(CONTENT content) {
        nbtCompound.putString("content_visual", content.toString());
        saveToStack();
        this.content = content;
    }

    public enum DISPLAY {
        FLOATING,
        UI
    }

    public enum CONTENT {
        INVENTORY,
        ENERGY
    }
}
