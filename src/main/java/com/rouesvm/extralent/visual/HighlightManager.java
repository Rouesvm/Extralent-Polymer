package com.rouesvm.extralent.visual;

import com.rouesvm.extralent.block.transport.entity.connection.Connection;
import com.rouesvm.extralent.visual.elements.BlockHighlight;
import com.rouesvm.extralent.visual.elements.BlockHighlights;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.*;

public class HighlightManager {
    private final HashMap<UUID, BlockHighlights> multipleHighlights;
    private final HashMap<UUID, BlockHighlight> singularHighlight;

    public HighlightManager() {
        this.multipleHighlights = new HashMap<>();
        this.singularHighlight = new HashMap<>();
    }

    // Singular
    public BlockHighlight getSingularHighlight(UUID uuid) {
        return singularHighlight.get(uuid);
    }

    public void createSingularHighlight(UUID uuid, ServerWorld world, ServerPlayerEntity player, BlockPos blockPos) {
        singularHighlight.put(uuid, BlockHighlight.createHighlight(world, player, blockPos));
    }

    public void removeSingularHighlight(UUID uuid) {
        BlockHighlight highlight = getSingularHighlight(uuid);
        if (highlight != null) singularHighlight.remove(uuid);
    }

    // Multiple
    public void createMultipleHighlights(UUID uuid, ServerWorld world, ServerPlayerEntity player) {
        multipleHighlights.putIfAbsent(uuid, new BlockHighlights(world, player));
    }

    public BlockHighlights getMultipleHighlights(UUID uuid) {
        return multipleHighlights.get(uuid);
    }

    public void addHighlightToMultiple(Connection connection, UUID uuid) {
        BlockHighlights highlights = getMultipleHighlights(uuid);
        highlights.addConnection(connection);
    }

    public void removeHighlightFromMultiple(Connection connection, UUID uuid) {
        BlockHighlights highlights = getMultipleHighlights(uuid);
        highlights.removeConnection(connection);
    }

    public void removeAllHighlightsFromMultiple(UUID uuid) {
        multipleHighlights.replace(uuid, null);
    }

    // Tick
    public void tickHighlights(UUID uuid) {
        var highlight = getSingularHighlight(uuid);
        var highlights = getMultipleHighlights(uuid);

        if (highlight != null) highlight.tick();
        if (highlights != null) highlights.tick();
    }

    // Clear
    public void clearAllHighlights(UUID uuid) {
        removeAllHighlightsFromMultiple(uuid);
        removeSingularHighlight(uuid);
    }
}
