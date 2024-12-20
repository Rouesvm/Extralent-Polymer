package com.rouesvm.extralent.item.custom.data;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

public class InfoData extends BasicData {
    private DISPLAY display = DISPLAY.FLOATING;
    private CONTENT_DISPLAY content = CONTENT_DISPLAY.ENERGY;

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

    public CONTENT_DISPLAY getContent() {
        if (getStackNbt().contains("content_visual"))
            content = CONTENT_DISPLAY.valueOf(nbtCompound.getString("content_visual"));
        return content;
    }

    public void nextContent() {
        CONTENT_DISPLAY[] contents = CONTENT_DISPLAY.values();
        int nextIndex = (getContent().ordinal() + 1) % contents.length;
        setContent(contents[nextIndex]);
    }

    public void setContent(CONTENT_DISPLAY content) {
        nbtCompound.putString("content_visual", content.toString());
        saveToStack();
        this.content = content;
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
