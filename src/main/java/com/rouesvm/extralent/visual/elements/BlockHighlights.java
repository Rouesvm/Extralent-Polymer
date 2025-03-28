package com.rouesvm.extralent.visual.elements;

import com.rouesvm.extralent.block.transport.entity.connection.Connection;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

import static com.rouesvm.extralent.visual.elements.BlockHighlight.*;

public class BlockHighlights {
    private final static ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    public static final Vector3f OUTPUT_BLOCK_COLOR = new Vector3f(1F, 0.5F, 0F);
    public static final Vector3f INPUT_BLOCK_COLOR = new Vector3f(0F, 0.75F, 1F);

    public final Set<Connection> connections = ConcurrentHashMap.newKeySet();

    public Map<BlockPos, DustParticleEffect> dustParticleEffects = new ConcurrentHashMap<>();
    public Map<BlockPos, DustParticleEffect> dustParticleEffectXs = new ConcurrentHashMap<>();
    public Map<BlockPos, BlockPos[]> corners = new ConcurrentHashMap<>();

    private final ServerWorld world;
    private final ServerPlayerEntity player;

    public boolean emit = true;
    public int tick = 0;

    public BlockHighlights(@NotNull ServerWorld world, @NotNull ServerPlayerEntity player) {
        this.world = world;
        this.player = player;
    }

    public void spawnParticles(Connection connection) {
        BlockPos pos = connection.getPos();
        BlockPos[] blockCorners = corners.get(pos);
        if (blockCorners == null) return;

        int randomEdge = ThreadLocalRandom.current().nextInt(BLOCK_EDGES.length);
        int[] assignedPos = BLOCK_EDGES[randomEdge];

        BlockPos start = blockCorners[assignedPos[0]];
        BlockPos end = blockCorners[assignedPos[1]];

        DustParticleEffect edgeParticle = dustParticleEffects.get(pos);
        if (edgeParticle != null) {
            spawnParticlesAlongEdge(edgeParticle, start, end);
        }

        DustParticleEffect sideParticle = dustParticleEffectXs.get(pos);
        if (sideParticle != null) {
            drawXOnBlockSide(pos.toCenterPos(), connection.getSide(), sideParticle);
        }
    }

    public void spawnParticlesAlongEdge(DustParticleEffect dustParticleEffect, BlockPos start, BlockPos end) {
        double steps = 4.5;
        double dx = (end.getX() - start.getX()) / steps;
        double dy = (end.getY() - start.getY()) / steps;
        double dz = (end.getZ() - start.getZ()) / steps;

        for (int i = 0; i <= steps; i++) {
            double x = start.getX() + i * dx;
            double y = start.getY() + i * dy;
            double z = start.getZ() + i * dz;
            world.spawnParticles(player, dustParticleEffect, true,true,
                    x, y, z,
                    0, 0, 0, 0,
                    0.001);
        }
    }

    public void drawXOnBlockSide(Vec3d sidePos, Direction blockSide, DustParticleEffect sideParticleType) {
        if (blockSide == null) return;

        double step = 0.25;
        double min = -0.4, max = 0.4;

        for (double t = 0; t <= 1; t += step) {
            double offset1 = min + (max - min) * t;
            spawnParticleOnSide(sidePos, blockSide, world, player, sideParticleType, offset1, offset1);
            double offset2 = max - (max - min) * t;
            spawnParticleOnSide(sidePos, blockSide, world, player, sideParticleType, offset1, offset2);
        }
    }

    private void spawnParticleOnSide(Vec3d sidePos, Direction blockSide, ServerWorld world, ServerPlayerEntity player, DustParticleEffect particleType, double offsetA, double offsetB) {
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

    public void removeConnection(Connection connection) {
        BlockPos position = connection.getPos();
        corners.remove(position);
        dustParticleEffects.remove(position);
        dustParticleEffectXs.remove(position);
        connections.remove(connection);
    }

    public void addConnection(Connection connection) {
        connections.add(connection);
        BlockPos position = connection.getPos();

        Vector3f color = connection.getWeight() == 0 ? OUTPUT_BLOCK_COLOR : INPUT_BLOCK_COLOR;
        DustParticleEffect dustParticleEffect = new DustParticleEffect(ColorHelper.fromFloats(0, color.x, color.y, color.z), 0.725F);
        dustParticleEffects.put(position, dustParticleEffect);

        Vector3f desaturatedColor = desaturateColor(color, 0.8F);
        DustParticleEffect dustParticleEffectX = new DustParticleEffect(ColorHelper.fromFloats(0, desaturatedColor.x, desaturatedColor.y, desaturatedColor.z), 0.725F);
        dustParticleEffectXs.put(position, dustParticleEffectX);

        corners.put(position, new BlockPos[]{
                position.add(0, 0, 0), position.add(1, 0, 0), position.add(0, 0, 1), position.add(1, 0, 1),
                position.add(0, 1, 0), position.add(1, 1, 0), position.add(0, 1, 1), position.add(1, 1, 1)
        });
    }

    public void tick() {
        if (!this.emit) return;
        for (Connection connection : connections) executor.submit(() -> spawnParticles(connection));
    }
}
