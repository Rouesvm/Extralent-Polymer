package com.rouesvm.extralent.visual;

import com.rouesvm.extralent.block.transport.entity.connection.Connection;
import com.rouesvm.extralent.visual.elements.BlockHighlight;
import com.rouesvm.extralent.visual.elements.BlockHighlights;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.*;

public class HighlightManager {
    private final HashMap<Long, BlockHighlights> multipleHighlights;
    private final HashMap<Long, BlockHighlight> singularHighlight;

    public HighlightManager() {
        this.multipleHighlights = new HashMap<>();
        this.singularHighlight = new HashMap<>();
    }

    // Singular
    public BlockHighlight getSingularHighlight(BlockPos uuid) {
        return singularHighlight.get(uuid.asLong());
    }

    public void createSingularHighlight(BlockPos uuid, ServerWorld world, ServerPlayerEntity player, BlockPos blockPos) {
        singularHighlight.put(uuid.asLong(), BlockHighlight.createHighlight(world, player, blockPos));
    }

    public void removeSingularHighlight(BlockPos uuid) {
        BlockHighlight highlight = getSingularHighlight(uuid);
        if (highlight != null) singularHighlight.remove(uuid.asLong());
    }

    // Multiple
    public void createMultipleHighlights(BlockPos uuid, ServerWorld world, ServerPlayerEntity player) {
        multipleHighlights.putIfAbsent(uuid.asLong(), new BlockHighlights(world, player));
    }

    public BlockHighlights getMultipleHighlights(BlockPos uuid) {
        return multipleHighlights.get(uuid.asLong());
    }

    public void addHighlightToMultiple(Connection connection, BlockPos uuid) {
        BlockHighlights highlights = getMultipleHighlights(uuid);
        highlights.addConnection(connection);
    }

    public void removeHighlightFromMultiple(Connection connection, BlockPos uuid) {
        BlockHighlights highlights = getMultipleHighlights(uuid);
        highlights.removeConnection(connection);
    }

    public void removeAllHighlightsFromMultiple(BlockPos uuid) {
        multipleHighlights.replace(uuid.asLong(), null);
    }

    public void tickHighlights(BlockPos uuid) {
        var highlight = getSingularHighlight(uuid);
        var highlights = getMultipleHighlights(uuid);

        if (highlight != null) highlight.tick();
        if (highlights != null) highlights.tick();
    }

    public void clearAllHighlights(BlockPos uuid) {
        removeAllHighlightsFromMultiple(uuid);
        removeSingularHighlight(uuid);
    }
}
