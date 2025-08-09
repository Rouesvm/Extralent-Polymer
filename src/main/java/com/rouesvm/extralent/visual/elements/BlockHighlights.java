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

import java.util.*;
import java.util.concurrent.*;

import static com.rouesvm.extralent.visual.elements.BlockHighlight.*;

public class BlockHighlights {
    private final static ExecutorService executor = ForkJoinPool.commonPool();

    public static final Vector3f OUTPUT_BLOCK_COLOR = new Vector3f(1F, 0.5F, 0F);
    public static final Vector3f INPUT_BLOCK_COLOR = new Vector3f(0F, 0.75F, 1F);

    private static final float PARTICLE_SIZE = 0.725F;
    private static final double PARTICLE_STEPS = 4.5;
    private static final double X_STEP = 0.25;
    private static final double X_MIN = -0.4, X_MAX = 0.4;

    private static final Map<Integer, DustParticleEffect> dustParticleEffects = new ConcurrentHashMap<>();
    static {
        Vector3f desaturatedOutputColor = desaturateColor(OUTPUT_BLOCK_COLOR, 0.8F);
        Vector3f desaturatedInputColor = desaturateColor(INPUT_BLOCK_COLOR, 0.8F);

        dustParticleEffects.put(0, new DustParticleEffect(
                ColorHelper.fromFloats(0, OUTPUT_BLOCK_COLOR.x, OUTPUT_BLOCK_COLOR.y, OUTPUT_BLOCK_COLOR.z),
                PARTICLE_SIZE));
        dustParticleEffects.put(1, new DustParticleEffect(
                ColorHelper.fromFloats(0, INPUT_BLOCK_COLOR.x, INPUT_BLOCK_COLOR.y, INPUT_BLOCK_COLOR.z),
                PARTICLE_SIZE));
        dustParticleEffects.put(2, new DustParticleEffect(
                ColorHelper.fromFloats(0, desaturatedOutputColor.x, desaturatedOutputColor.y, desaturatedOutputColor.z),
                PARTICLE_SIZE));
        dustParticleEffects.put(3, new DustParticleEffect(
                ColorHelper.fromFloats(0, desaturatedInputColor.x, desaturatedInputColor.y, desaturatedInputColor.z),
                PARTICLE_SIZE));
    }

    public final Set<Connection> connections = ConcurrentHashMap.newKeySet();

    private final Map<BlockPos, Map<Integer, Vec3d[]>> edgeParticlePositions = new ConcurrentHashMap<>();
    private final Map<BlockPos, Vec3d[]> xParticlePositions = new ConcurrentHashMap<>();

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

        Map<Integer, Vec3d[]> edges = edgeParticlePositions.get(pos);
        if (edges != null) {
            int randomEdge = ThreadLocalRandom.current().nextInt(BLOCK_EDGES.length);
            Vec3d[] positions = edges.get(randomEdge);
            DustParticleEffect effect = dustParticleEffects.get(connection.getWeight());

            if (positions != null && effect != null) {
                for (Vec3d pos3d : positions) {
                    world.spawnParticles(player, effect, true, true,
                            pos3d.x, pos3d.y, pos3d.z, 0, 0, 0, 0, 0.001);
                }
            }
        }

        Vec3d[] xPositions = xParticlePositions.get(pos);
        if (xPositions != null) {
            DustParticleEffect effect = dustParticleEffects.get(connection.getWeight() + 2);
            for (Vec3d pos3d : xPositions) {
                world.spawnParticles(player, effect, true, true,
                        pos3d.x, pos3d.y, pos3d.z, 0, 0, 0, 0, 0.001);
            }
        }
    }

    public void removeConnection(Connection connection) {
        BlockPos position = connection.getPos();
        connections.remove(connection);
        edgeParticlePositions.remove(position);
        xParticlePositions.remove(position);
    }

    public void addConnection(Connection connection) {
        connections.add(connection);

        BlockPos position = connection.getPos();
        Vec3d[] worldCornerPositions = calculateCorners(position, Map.of(position, new BlockPos[]{
                position.add(0, 0, 0), position.add(1, 0, 0), position.add(0, 0, 1), position.add(1, 0, 1),
                position.add(0, 1, 0), position.add(1, 1, 0), position.add(0, 1, 1), position.add(1, 1, 1)
        }));

        edgeParticlePositions.put(position, calculateAllEdgeParticles(worldCornerPositions));
        xParticlePositions.put(position, calculateXParticles(position.toCenterPos(), connection.getSide()));
    }

    private static Vec3d[] calculateCorners(BlockPos position, Map<BlockPos, BlockPos[]> blockPos) {
        Vec3d[] worldCornerPositions = new Vec3d[8];
        for (int i = 0; i < 8; i++) {
            BlockPos corner = blockPos.get(position)[i];
            worldCornerPositions[i] = new Vec3d(corner.getX(), corner.getY(), corner.getZ());
        }
        return worldCornerPositions;
    }

    private static Map<Integer, Vec3d[]> calculateAllEdgeParticles(Vec3d[] corners) {
        Map<Integer, Vec3d[]> edgePositions = new HashMap<>();

        for (int edgeIndex = 0; edgeIndex < BLOCK_EDGES.length; edgeIndex++) {
            int[] edge = BLOCK_EDGES[edgeIndex];
            Vec3d start = corners[edge[0]];
            Vec3d end = corners[edge[1]];
            edgePositions.put(edgeIndex, calculateEdgeParticles(start, end));
        }

        return edgePositions;
    }

    public static Vec3d[] calculateXParticles(Vec3d center, Direction side) {
        List<Vec3d> positions = new ArrayList<>();
        for (double t = 0; t <= 1; t += X_STEP) {
            double offset1 = X_MIN + (X_MAX - X_MIN) * t;
            double offset2 = X_MAX - (X_MAX - X_MIN) * t;

            positions.add(calculateSidePosition(center, side, offset1, offset1));
            positions.add(calculateSidePosition(center, side, offset1, offset2));
        }

        return positions.toArray(new Vec3d[0]);
    }

    public static Vec3d[] calculateEdgeParticles(Vec3d start, Vec3d end) {
        Vec3d[] positions = new Vec3d[(int)PARTICLE_STEPS + 1];

        double dx = (end.x - start.x) / PARTICLE_STEPS;
        double dy = (end.y - start.y) / PARTICLE_STEPS;
        double dz = (end.z - start.z) / PARTICLE_STEPS;

        for (int i = 0; i <= PARTICLE_STEPS; i++) {
            positions[i] = new Vec3d(
                    start.x + i * dx,
                    start.y + i * dy,
                    start.z + i * dz
            );
        }
        return positions;
    }

    public static Vec3d calculateSidePosition(Vec3d center, Direction side, double offsetA, double offsetB) {
        double x = center.getX();
        double y = center.getY();
        double z = center.getZ();

        switch (side) {
            case UP:
            case DOWN:
                x += offsetA;
                z += offsetB;
                y += side == Direction.UP ? 0.5 : -0.5;
                break;
            case NORTH:
            case SOUTH:
                x += offsetA;
                y += offsetB;
                z += side == Direction.SOUTH ? 0.5 : -0.5;
                break;
            case EAST:
            case WEST:
                z += offsetA;
                y += offsetB;
                x += side == Direction.EAST ? 0.5 : -0.5;
                break;
            case null:
                break;
        }

        return new Vec3d(x, y, z);
    }

    public void tick() {
        if (!this.emit) return;
        for (Connection connection : connections) {
            executor.submit(() -> spawnParticles(connection));
        }
    }

    public static void shutdownThread() {
        executor.shutdown();
    }
}
