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
    public BlockHighlight getSingularHighlight(BlockPos pos) {
        return singularHighlight.get(pos.asLong());
    }

    public void createSingularHighlight(ServerWorld world, ServerPlayerEntity player, BlockPos blockPos) {
        singularHighlight.put(blockPos.asLong(), BlockHighlight.createHighlight(world, player, blockPos));
    }

    public void removeSingularHighlight(BlockPos pos) {
        BlockHighlight highlight = getSingularHighlight(pos);
        if (highlight != null) singularHighlight.remove(pos.asLong());
    }

    // Multiple
    public void createMultipleHighlights(BlockPos pos, ServerWorld world, ServerPlayerEntity player) {
        multipleHighlights.putIfAbsent(pos.asLong(), new BlockHighlights(world, player));
    }

    public BlockHighlights getMultipleHighlights(BlockPos pos) {
        return multipleHighlights.get(pos.asLong());
    }

    public void addHighlightToMultiple(Connection connection, BlockPos pos) {
        BlockHighlights highlights = getMultipleHighlights(pos);
        if (highlights != null) highlights.addConnection(connection);
    }

    public void removeHighlightFromMultiple(Connection connection, BlockPos pos) {
        BlockHighlights highlights = getMultipleHighlights(pos);
        if (highlights != null) highlights.removeConnection(connection);
    }

    public void replaceHighlightToMultiple(Connection connection, BlockPos pos) {
        BlockHighlights highlights = getMultipleHighlights(pos);
        highlights.removeConnection(connection);
        highlights.addConnection(connection);
    }

    public void removeAllHighlightsFromMultiple(BlockPos pos) {
        multipleHighlights.replace(pos.asLong(), null);
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
