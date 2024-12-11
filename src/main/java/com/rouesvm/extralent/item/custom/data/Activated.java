package com.rouesvm.extralent.item.custom.data;

import com.rouesvm.extralent.registries.data.DataComponentRegistry;
import net.minecraft.item.ItemStack;

public class Activated {
    public static boolean showVisual(ItemStack stack) {
        return stack.getOrDefault(DataComponentRegistry.BOOLEAN_TYPE, false);
    }

    public static void setVisual(ItemStack stack, boolean visual) {
        if (showVisual(stack) == visual) return;
        stack.set(DataComponentRegistry.BOOLEAN_TYPE, visual);
    }
}
