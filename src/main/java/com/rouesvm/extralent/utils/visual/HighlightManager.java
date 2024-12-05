package com.rouesvm.extralent.utils.visual;

import com.rouesvm.extralent.entity.elements.BlockHighlight;
import com.rouesvm.extralent.utils.Connection;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.UUID;

public class HighlightManager {
    private final HashMap<UUID, HashMap<BlockPos, BlockHighlight>> multipleHighlights;
    private final HashMap<UUID, BlockHighlight> singularHighlight;

    public HighlightManager() {
        this.multipleHighlights = new HashMap<>();
        this.singularHighlight = new HashMap<>();
    }

    // Singular
    public BlockHighlight getSingularHighlight(UUID uuid) {
        return singularHighlight.get(uuid);
    }

    public void createSingularHighlight(UUID uuid, ServerWorld world, Connection connection) {
        singularHighlight.put(uuid, BlockHighlight.createHighlight(world, connection));
    }

    public void removeSingularHighlight(UUID uuid) {
        BlockHighlight highlight = getSingularHighlight(uuid);
        if (highlight != null) singularHighlight.remove(uuid);
    }

    // Multiple
    public HashMap<BlockPos, BlockHighlight> getMultipleHighlights(UUID uuid) {
        return multipleHighlights.getOrDefault(uuid, new HashMap<>());
    }

    public void addHighlightToMultiple(BlockPos pos, BlockHighlight highlight, UUID uuid) {
        multipleHighlights.computeIfAbsent(uuid, k -> new HashMap<>()).put(pos, highlight);
    }

    public BlockHighlight getHighlightFromMultiple(BlockPos pos, UUID uuid) {
        return getMultipleHighlights(uuid).get(pos);
    }

    public void removeHighlightFromMultiple(BlockPos pos, UUID uuid) {
        HashMap<BlockPos, BlockHighlight> highlights = getMultipleHighlights(uuid);
        BlockHighlight highlight = highlights.get(pos);
        if (highlight != null) highlights.remove(pos);
    }

    public void removeAllHighlightsFromMultiple(UUID uuid) {
        multipleHighlights.remove(uuid);
    }

    // Tick
    public void tickHighlights(UUID uuid) {
        getMultipleHighlights(uuid).forEach((pos, blockHighlight) -> blockHighlight.tick());

        if (getSingularHighlight(uuid) != null)
            getSingularHighlight(uuid).tick();
    }

    // Clear
    public void clearAllHighlights(UUID uuid) {
        removeAllHighlightsFromMultiple(uuid);
        removeSingularHighlight(uuid);
    }
}
