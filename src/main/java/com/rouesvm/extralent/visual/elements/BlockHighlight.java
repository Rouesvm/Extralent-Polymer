package com.rouesvm.extralent.visual.elements;

import com.rouesvm.extralent.block.transport.entity.connection.Connection;
import net.minecraft.particle.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.joml.Vector3f;

public class BlockHighlight {
    private final BlockPos position;
    private final Vector3f color;

    private final ServerWorld world;
    private final ServerPlayerEntity player;

    private int ticks = 40;
    private int randomInt = 0;

    private BlockHighlight(ServerWorld world, ServerPlayerEntity player, BlockPos position) {
        this.world = world;
        this.player = player;
        this.color = new Vector3f(0.5F, 0.5F, 0.5F);
        this.position = position;
    }

    private BlockHighlight(ServerWorld world, ServerPlayerEntity player, Connection connection) {
        this.world = world;
        this.position = connection.getPos();
        this.player = player;

        if (connection.getWeight() == 0) {
            this.color = new Vector3f(1F, 0.5F, 0F);
        } else if (connection.getWeight() == 10) {
            this.color = new Vector3f(0.25F, 1F, 0.25F);
        } else this.color = new Vector3f(0F, 0.75F, 1F);
    }

    public void spawnEdgeParticles(BlockPos pos) {
        BlockPos[] corners = {
                pos.add(0, 0, 0), pos.add(1, 0, 0), pos.add(0, 0, 1), pos.add(1, 0, 1),
                pos.add(0, 1, 0), pos.add(1, 1, 0), pos.add(0, 1, 1), pos.add(1, 1, 1)
        };

        int[][] edges = {
                {0, 1}, {0, 2}, {0, 4}, {1, 3}, {1, 5}, {2, 3}, {2, 6}, {3, 7},
                {4, 5}, {4, 6}, {5, 7}, {6, 7}
        };

        randomInt++;
        if (randomInt <= edges.length) {
            int[] assignedPos = edges[randomInt - 1];
            BlockPos start = corners[assignedPos[0]];
            BlockPos end = corners[assignedPos[1]];

            spawnParticlesAlongEdge(start, end);
        } else randomInt = 0;
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

            world.spawnParticles(player, new DustParticleEffect(color, 0.75F), true,
                    x, y, z, 0, 0, 0, 0, 0);
        }
    }

    public void tick() {
        if (this.world != null) {
            if (this.ticks++ % 20 == 0) {
                ticks = 0;
                spawnEdgeParticles(position);
            }
        }
    }

    public static BlockHighlight createHighlight(ServerWorld world, ServerPlayerEntity player, BlockPos position) {
        return new BlockHighlight(world, player, position);
    }

    public static BlockHighlight createHighlight(ServerWorld world, ServerPlayerEntity player, Connection connection) {
        return new BlockHighlight(world, player, connection);
    }
}
