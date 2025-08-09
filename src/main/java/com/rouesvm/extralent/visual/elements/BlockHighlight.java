package com.rouesvm.extralent.visual.elements;

import net.minecraft.particle.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

import static com.rouesvm.extralent.visual.elements.BlockHighlights.calculateEdgeParticles;

public class BlockHighlight {
    public static final Vector3f CONNECTED_BLOCK_COLOR = new Vector3f(0.25F, 1F, 0.25F);

    public static final int[][] BLOCK_EDGES = {
            {0, 1}, {0, 2}, {0, 4}, {1, 3},
            {1, 5}, {2, 3}, {2, 6}, {3, 7},
            {4, 5}, {4, 6}, {5, 7}, {6, 7}
    };

    private final ServerPlayerEntity player;
    private final ServerWorld world;

    private final DustParticleEffect particleType;
    private final Map<Integer, Vec3d[]> edgeParticlePositions = new ConcurrentHashMap<>();

    private BlockHighlight(@NotNull ServerWorld world, @Nullable ServerPlayerEntity player, @NotNull BlockPos position, @NotNull Vector3f color) {
        this.particleType = new DustParticleEffect(ColorHelper.fromFloats(0, color.x, color.y, color.z), 0.725F);

        this.world = world;
        this.player = player;

        BlockPos[] corners = new BlockPos[] {
                position.add(0, 0, 0), position.add(1, 0, 0), position.add(0, 0, 1), position.add(1, 0, 1),
                position.add(0, 1, 0), position.add(1, 1, 0), position.add(0, 1, 1), position.add(1, 1, 1)
        };

        for (int edgeIndex = 0; edgeIndex < BLOCK_EDGES.length; edgeIndex++) {
            int[] edge = BLOCK_EDGES[edgeIndex];
            Vec3d start = Vec3d.of(corners[edge[0]]);
            Vec3d end = Vec3d.of(corners[edge[1]]);
            this.edgeParticlePositions.put(edgeIndex, calculateEdgeParticles(start, end));
        }
    }

    public void tick() {
        if (this.world != null && !this.world.isClient) {
            int randomEdge = ThreadLocalRandom.current().nextInt(BLOCK_EDGES.length);
            Vec3d[] positions = edgeParticlePositions.get(randomEdge);

            if (positions != null && particleType != null) {
                for (Vec3d pos3d : positions) {
                    if (player == null) world.spawnParticles(particleType, true, true,
                            pos3d.x, pos3d.y, pos3d.z, 0, 0, 0, 0, 0.001);
                    else world.spawnParticles(player, particleType, true, true,
                            pos3d.x, pos3d.y, pos3d.z, 0, 0, 0, 0, 0.001);
                }
            }
        }
    }

    public static Vector3f desaturateColor(Vector3f color, float desaturationFactor) {
        desaturationFactor = Math.max(0.0f, Math.min(1.0f, desaturationFactor));

        float r = color.x();
        float g = color.y();
        float b = color.z();

        float luminance = 0.299f * r + 0.587f * g + 0.114f * b;

        float newR = r + desaturationFactor * (luminance - r);
        float newG = g + desaturationFactor * (luminance - g);
        float newB = b + desaturationFactor * (luminance - b);

        return new Vector3f(newR, newG, newB);
    }

    public static BlockHighlight createHighlight(ServerWorld world, ServerPlayerEntity player, BlockPos position) {
        return new BlockHighlight(world, player, position, CONNECTED_BLOCK_COLOR);
    }

    public static BlockHighlight createHighlight(ServerWorld world, BlockPos position) {
        return createHighlight(world, null, position);
    }
}
