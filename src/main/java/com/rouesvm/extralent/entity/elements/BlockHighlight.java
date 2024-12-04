package com.rouesvm.extralent.entity.elements;

import com.rouesvm.extralent.utils.Connection;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.ChunkAttachment;
import eu.pb4.polymer.virtualentity.api.elements.MarkerElement;
import net.minecraft.block.Block;
import net.minecraft.particle.*;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.joml.Vector3f;

import java.util.Random;
import java.util.random.RandomGenerator;

public class BlockHighlight extends ElementHolder {
    private final BlockPos position;
    private final MarkerElement markerElement;

    private final SimpleParticleType particleTypes;

    private int ticks = 40;
    private int randomInt = 0;

    private BlockHighlight(BlockPos position) {
        this.particleTypes = ParticleTypes.SCRAPE;
        this.markerElement = new MarkerElement();
        this.position = position;
        this.addElement(markerElement);
    }

    private BlockHighlight(Connection connection) {
        this.markerElement = new MarkerElement();
        this.position = connection.getPos();

        if (connection.getWeight() == 0) {
            this.particleTypes = ParticleTypes.WAX_ON;
        } else if (connection.getWeight() == 10) {
            this.particleTypes = ParticleTypes.ELECTRIC_SPARK;
        } else this.particleTypes = ParticleTypes.SCRAPE;

        this.addElement(markerElement);
    }

    public void spawnEdgeParticles(ServerWorld world, BlockPos pos) {
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

            spawnParticlesAlongEdge(world, start, end);
        } else randomInt = 0;
    }

    public void spawnParticlesAlongEdge(ServerWorld world, BlockPos start, BlockPos end) {
        double steps = 5;
        double dx = (end.getX() - start.getX()) / steps;
        double dy = (end.getY() - start.getY()) / steps;
        double dz = (end.getZ() - start.getZ()) / steps;

        for (int i = 0; i <= steps; i++) {
            double x = start.getX() + i * dx;
            double y = start.getY() + i * dy;
            double z = start.getZ() + i * dz;

            world.spawnParticles(particleTypes, x, y, z, 0, 0, 0, 0, 5);
        }
    }

    @Override
    public void tick() {
        if (this.getAttachment() != null && this.getAttachment().getWorld() != null) {
            if (this.ticks++ % 10 == 0)
                ticks = 0;
            spawnEdgeParticles(this.getAttachment().getWorld(), position);
        }
        super.tick();
    }

    @Override
    public boolean startWatching(ServerPlayNetworkHandler player) {
        return super.startWatching(player);
    }

    public void kill() {
        this.destroy();
    }

    public static BlockHighlight createHighlight(ServerWorld world, BlockPos position) {
        BlockHighlight model = new BlockHighlight(position);

        ChunkAttachment.ofTicking(model, world, position);
        return model;
    }

    public static BlockHighlight createHighlight(ServerWorld world, Connection connection) {
        BlockHighlight model = new BlockHighlight(connection);

        ChunkAttachment.ofTicking(model, world, connection.getPos());
        return model;
    }
}
