package com.rouesvm.extralent.utils.visual;

import com.rouesvm.extralent.entity.elements.BlockHighlight;
import com.rouesvm.extralent.utils.Connection;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;

public class HighlightManager {
    private final HashMap<ItemStack, HashMap<BlockPos, BlockHighlight>> multipleHighlights;
    private final HashMap<ItemStack, BlockHighlight> singularHighlight;

    public HighlightManager() {
        this.multipleHighlights = new HashMap<>();
        this.singularHighlight = new HashMap<>();
    }

    // Singular
    public BlockHighlight getSingularHighlight(ItemStack stack) {
        return singularHighlight.get(stack);
    }

    public void createSingularHighlight(ItemStack stack, ServerWorld world, Connection connection) {
        singularHighlight.put(stack, BlockHighlight.createHighlight(world, connection));
    }

    public void removeSingularHighlight(ItemStack stack) {
        BlockHighlight highlight = getSingularHighlight(stack);
        if (highlight != null) {
            singularHighlight.remove(stack);
        }
    }

    // Multiple
    public HashMap<BlockPos, BlockHighlight> getMultipleHighlights(ItemStack stack) {
        return multipleHighlights.getOrDefault(stack, new HashMap<>());
    }

    public void addHighlightToMultiple(BlockPos pos, BlockHighlight highlight, ItemStack stack) {
        multipleHighlights.computeIfAbsent(stack, k -> new HashMap<>()).put(pos, highlight);
    }

    public BlockHighlight getHighlightFromMultiple(BlockPos pos, ItemStack stack) {
        return getMultipleHighlights(stack).get(pos);
    }

    public void removeHighlightFromMultiple(BlockPos pos, ItemStack stack) {
        HashMap<BlockPos, BlockHighlight> highlights = getMultipleHighlights(stack);
        BlockHighlight highlight = highlights.get(pos);
        if (highlight != null) {
            highlights.remove(pos);
        }
    }

    public void removeAllHighlightsFromMultiple(ItemStack stack) {
        multipleHighlights.remove(stack);
    }

    // Tick
    public void tickHighlights(ItemStack stack) {
        getMultipleHighlights(stack).forEach((pos, blockHighlight) -> blockHighlight.tick());

        if (getSingularHighlight(stack) != null)
            getSingularHighlight(stack).tick();
    }

    // Clear
    public void clearAllHighlights(ItemStack stack) {
        removeAllHighlightsFromMultiple(stack);
        removeSingularHighlight(stack);
    }
}
