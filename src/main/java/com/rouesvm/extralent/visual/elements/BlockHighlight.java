package com.rouesvm.extralent.visual.elements;

import com.rouesvm.extralent.block.transport.entity.connection.Connection;
import net.minecraft.particle.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Colors;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.concurrent.ThreadLocalRandom;

public class BlockHighlight {
    public static final Vector3f CONNECTED_BLOCK_COLOR = new Vector3f(0.25F, 1F, 0.25F);
    public static final Vector3f OUTPUT_BLOCK_COLOR = new Vector3f(1F, 0.5F, 0F);
    public static final Vector3f INPUT_BLOCK_COLOR = new Vector3f(0F, 0.75F, 1F);

    public static final int[][] BLOCK_EDGES = {
            {0, 1}, {0, 2}, {0, 4}, {1, 3},
            {1, 5}, {2, 3}, {2, 6}, {3, 7},
            {4, 5}, {4, 6}, {5, 7}, {6, 7}
    };

    private final DustParticleEffect particleType;
    private final DustParticleEffect sideParticleType;

    private final BlockPos[] corners;

    private final ServerWorld world;
    private final ServerPlayerEntity player;

    @Nullable private final Direction blockSide;
    private final Vec3d sidePos;

    private BlockHighlight(@NotNull ServerWorld world, @NotNull ServerPlayerEntity player,
                           @NotNull BlockPos position, @Nullable Direction side,
                           @NotNull Vector3f color
    ) {
        this.particleType = new DustParticleEffect(ColorHelper.fromFloats(0, color.x, color.y, color.z), 0.725F);

        var desaturatedColor = desaturateColor(color, 0.8F);
        this.sideParticleType = new DustParticleEffect(ColorHelper.fromFloats(0, desaturatedColor.x, desaturatedColor.y, desaturatedColor.z), 0.725F);

        this.world = world;
        this.player = player;

        this.corners = new BlockPos[] {
                position.add(0, 0, 0), position.add(1, 0, 0), position.add(0, 0, 1), position.add(1, 0, 1),
                position.add(0, 1, 0), position.add(1, 1, 0), position.add(0, 1, 1), position.add(1, 1, 1)
        };

        this.blockSide = side;
        if (side != null) {
            this.sidePos = position.toCenterPos();
        } else this.sidePos = null;
    }

    public void spawnEdgeParticles() {
        int randomEdge = ThreadLocalRandom.current().nextInt(BLOCK_EDGES.length);

        int[] assignedPos = BLOCK_EDGES[randomEdge];
        BlockPos start = corners[assignedPos[0]];
        BlockPos end = corners[assignedPos[1]];

        spawnParticlesAlongEdge(start, end);
    }

    public void spawnParticlesAlongEdge(BlockPos start, BlockPos end) {
        double steps = 4.5;

        double dx = (end.getX() - start.getX()) / steps;
        double dy = (end.getY() - start.getY()) / steps;
        double dz = (end.getZ() - start.getZ()) / steps;

        for (int i = 0; i <= steps; i++) {
            double x = start.getX() + i * dx;
            double y = start.getY() + i * dy;
            double z = start.getZ() + i * dz;

            world.spawnParticles(player, particleType, true, true,
                    x, y, z,
                    0,
                    0, 0, 0,
                    0.001);
        }
    }

    public void drawXOnBlockSide() {
        if (blockSide == null) return;

        double step = 0.25;
        double min = -0.4, max = 0.4;

        for (double t = 0; t <= 1; t += step) {
            double offset1 = min + (max - min) * t;
            spawnParticleOnSide(world, player, sideParticleType, offset1, offset1);
            double offset2 = max - (max - min) * t;
            spawnParticleOnSide(world, player, sideParticleType, offset1, offset2);
        }
    }

    private void spawnParticleOnSide(ServerWorld world, ServerPlayerEntity player, DustParticleEffect particleType, double offsetA, double offsetB) {
        double x = sidePos.getX();
        double y = sidePos.getY();
        double z = sidePos.getZ();

        switch (blockSide) {
            case UP:
                case DOWN:
                    x += offsetA;
                    z += offsetB;
                    y += blockSide == Direction.UP ? 0.5 : -0.5;
                    break;
                case NORTH:
                case SOUTH:
                    x += offsetA;
                    y += offsetB;
                    z += blockSide == Direction.SOUTH ? 0.5 : -0.5;
                    break;
                case EAST:
                case WEST:
                    z += offsetA;
                    y += offsetB;
                    x += blockSide == Direction.EAST ? 0.5 : -0.5;
                    break;
                case null:
                    break;
        }

        world.spawnParticles(player, particleType, true, true, x, y, z, 0, 0, 0, 0, 0.001);
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

    public void tick() {
        if (this.world != null && !this.world.isClient) {
            spawnEdgeParticles();
            drawXOnBlockSide();
        }
    }

    public static BlockHighlight createHighlight(ServerWorld world, ServerPlayerEntity player, BlockPos position) {
        return new BlockHighlight(world, player, position, null, CONNECTED_BLOCK_COLOR);
    }

    public static BlockHighlight createHighlight(ServerWorld world, ServerPlayerEntity player, Connection connection) {
        Vector3f color = connection.getWeight() == 0 ? OUTPUT_BLOCK_COLOR : INPUT_BLOCK_COLOR;
        return new BlockHighlight(world, player, connection.getPos(), connection.getSide(), color);
    }
}
