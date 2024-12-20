package com.rouesvm.extralent.visual.elements;

import com.rouesvm.extralent.block.transport.entity.connection.Connection;
import net.minecraft.particle.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.joml.Vector3f;

import java.util.concurrent.ThreadLocalRandom;

public class BlockHighlight {
    private static final Vector3f CONNECTED_BLOCK_COLOR = new Vector3f(0.25F, 1F, 0.25F);
    private static final Vector3f OUTPUT_BLOCK_COLOR = new Vector3f(1F, 0.5F, 0F);
    private static final Vector3f INPUT_BLOCK_COLOR = new Vector3f(0F, 0.75F, 1F);

    private static final int[][] edges = {
            {0, 1}, {0, 2}, {0, 4}, {1, 3}, {1, 5}, {2, 3}, {2, 6}, {3, 7},
            {4, 5}, {4, 6}, {5, 7}, {6, 7}
    };

    private final DustParticleEffect particleType;

    private final BlockPos[] corners;

    private final ServerWorld world;
    private final ServerPlayerEntity player;

    private int ticks = 0;
    private int lastEdge = -1;

    private BlockHighlight(ServerWorld world, ServerPlayerEntity player, BlockPos position, Vector3f color) {
        this.particleType = new DustParticleEffect(color, 0.725F);

        this.world = world;
        this.player = player;

        this.corners = new BlockPos[] {
                position.add(0, 0, 0), position.add(1, 0, 0), position.add(0, 0, 1), position.add(1, 0, 1),
                position.add(0, 1, 0), position.add(1, 1, 0), position.add(0, 1, 1), position.add(1, 1, 1)
        };
    }

    public void spawnEdgeParticles() {
        int randomEdge;
        do {
            randomEdge = ThreadLocalRandom.current().nextInt(edges.length);
        } while (randomEdge == lastEdge);
        lastEdge = randomEdge;

        int[] assignedPos = edges[randomEdge];
        BlockPos start = corners[assignedPos[0]];
        BlockPos end = corners[assignedPos[1]];

        spawnParticlesAlongEdge(start, end);
    }

    public void spawnParticlesAlongEdge(BlockPos start, BlockPos end) {
        double steps = 5;
        double dx = (end.getX() - start.getX()) / steps;
        double dy = (end.getY() - start.getY()) / steps;
        double dz = (end.getZ() - start.getZ()) / steps;

        for (int i = 0; i <= steps; i++) {
            double x = start.getX() + i * dx;
            double y = start.getY() + i * dy;
            double z = start.getZ() + i * dz;

            world.spawnParticles(player, particleType, true,
                    x, y, z, 0, 0, 0, 0, 0);
        }
    }

    public void tick() {
        if (this.world != null) {
            if (this.ticks++ % 40 == 0) {
                this.ticks = 0;
                spawnEdgeParticles();
            }
        }
    }

    public static BlockHighlight createHighlight(ServerWorld world, ServerPlayerEntity player, BlockPos position) {
        return new BlockHighlight(world, player, position, CONNECTED_BLOCK_COLOR);
    }

    public static BlockHighlight createHighlight(ServerWorld world, ServerPlayerEntity player, Connection connection) {
        Vector3f color = connection.getWeight() == 0 ? OUTPUT_BLOCK_COLOR : INPUT_BLOCK_COLOR;

        return new BlockHighlight(world, player, connection.getPos(), color);
    }
}
