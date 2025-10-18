package com.rouesvm.extralent.visual.elements;

import net.minecraft.particle.DustParticleEffect;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.Vec3i;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.concurrent.ThreadLocalRandom;

public class BlockHighlight {
    public static final Vector3f CONNECTED_BLOCK_COLOR = new Vector3f(0.25F, 1F, 0.25F);
    private static final DustParticleEffect particleType = new DustParticleEffect(ColorHelper.fromFloats(0,
            CONNECTED_BLOCK_COLOR.x, CONNECTED_BLOCK_COLOR.y, CONNECTED_BLOCK_COLOR.z),
            0.725F
    );

    public static final int[][] BLOCK_EDGES = {
            {0, 1}, {0, 2}, {0, 4}, {1, 3},
            {1, 5}, {2, 3}, {2, 6}, {3, 7},
            {4, 5}, {4, 6}, {5, 7}, {6, 7}
    };

    public static final int EDGES_AMOUNT = BLOCK_EDGES.length;

    private final ServerWorld world;
    private final ServerPlayerEntity player;
    private final Vector3f[][] edgeParticlePositions = new Vector3f[EDGES_AMOUNT][];

    private BlockHighlight(@NotNull ServerWorld world, @Nullable ServerPlayerEntity player, @NotNull BlockPos position) {
        this.world = world;
        this.player = player;

        BlockPos[] corners = new BlockPos[] {
                position.add(0, 0, 0), position.add(1, 0, 0), position.add(0, 0, 1), position.add(1, 0, 1),
                position.add(0, 1, 0), position.add(1, 1, 0), position.add(0, 1, 1), position.add(1, 1, 1)
        };

        for (int edgeIndex = 0; edgeIndex < EDGES_AMOUNT; edgeIndex++) {
            int[] edge = BLOCK_EDGES[edgeIndex];
            Vec3i start = corners[edge[0]];
            Vec3i end = corners[edge[1]];
            this.edgeParticlePositions[edgeIndex] = BlockHighlights.calculateEdgeParticles(start, end);
        }
    }

    public void tick() {
        if (!this.world.isClient()) {
            int randomEdge = ThreadLocalRandom.current().nextInt(EDGES_AMOUNT);
            Vector3f[] positions = edgeParticlePositions[randomEdge];

            if (positions == null) return;

            for (Vector3f pos3d : positions) {
                if (player == null) world.spawnParticles(particleType, true, true,
                        pos3d.x, pos3d.y, pos3d.z, 0, 0, 0, 0, 0.001);
                else world.spawnParticles(player, particleType, true, true,
                        pos3d.x, pos3d.y, pos3d.z, 0, 0, 0, 0, 0.001);
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
        return new BlockHighlight(world, player, position);
    }

    public static BlockHighlight createHighlight(ServerWorld world, BlockPos position) {
        return createHighlight(world, null, position);
    }
}
