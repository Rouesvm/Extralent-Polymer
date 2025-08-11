package com.rouesvm.extralent.visual.elements;

import com.rouesvm.extralent.block.transport.entity.connection.Connection;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.*;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.*;
import java.util.concurrent.*;

import static com.rouesvm.extralent.visual.elements.BlockHighlight.*;

public class BlockHighlights {
    private static final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "BlockHighlights-Particles");
        t.setDaemon(true);
        return t;
    });

    public static final Vector3f OUTPUT_BLOCK_COLOR = new Vector3f(1F, 0.5F, 0F);
    public static final Vector3f INPUT_BLOCK_COLOR = new Vector3f(0F, 0.75F, 1F);

    private static final int PARTICLE_BATCHES = 3;
    private static final int PARTICLE_STEPS = 3;
    private static final float PARTICLE_SIZE = 0.75F;

    private static final float X_STEP = 0.25F;
    private static final float X_MIN = -0.4F, X_MAX = 0.4F;

    private static final DustParticleEffect[] dustParticleEffects = new DustParticleEffect[4];
    static {
        Vector3f desaturatedOutputColor = desaturateColor(OUTPUT_BLOCK_COLOR, 0.8F);
        Vector3f desaturatedInputColor = desaturateColor(INPUT_BLOCK_COLOR, 0.8F);

        dustParticleEffects[0] = new DustParticleEffect(
                ColorHelper.fromFloats(0, OUTPUT_BLOCK_COLOR.x, OUTPUT_BLOCK_COLOR.y, OUTPUT_BLOCK_COLOR.z),
                PARTICLE_SIZE);
        dustParticleEffects[1] = new DustParticleEffect(
                ColorHelper.fromFloats(0, INPUT_BLOCK_COLOR.x, INPUT_BLOCK_COLOR.y, INPUT_BLOCK_COLOR.z),
                PARTICLE_SIZE);
        dustParticleEffects[2] = new DustParticleEffect(
                ColorHelper.fromFloats(0, desaturatedOutputColor.x, desaturatedOutputColor.y, desaturatedOutputColor.z),
                PARTICLE_SIZE);
        dustParticleEffects[3] = new DustParticleEffect(
                ColorHelper.fromFloats(0, desaturatedInputColor.x, desaturatedInputColor.y, desaturatedInputColor.z),
                PARTICLE_SIZE);
    }

    public final List<Connection> connections = Collections.synchronizedList(new ArrayList<>());

    private final Map<BlockPos, Vector3f[][]> edgeParticlePositions = new ConcurrentHashMap<>();
    private final Map<BlockPos, Vector3f[]> xParticlePositions = new ConcurrentHashMap<>();

    private final ServerWorld world;
    private final ServerPlayerEntity player;

    public boolean emit = true;
    private int currentTick = 0;

    public BlockHighlights(@NotNull ServerWorld world, @NotNull ServerPlayerEntity player) {
        this.world = world;
        this.player = player;
    }

    public void removeConnection(Connection connection) {
        if (connection == null) return;
        BlockPos position = connection.getPos();
        xParticlePositions.remove(position);
        edgeParticlePositions.remove(position);
        connections.remove(connection);
    }

    public void addConnection(Connection connection) {
        if (connection == null) return;
        connections.add(connection);

        BlockPos position = connection.getPos();
        xParticlePositions.put(position, calculateXParticles(position.toCenterPos().toVector3f(), connection.getSide()));
        edgeParticlePositions.put(position, calculateAllEdgeParticles(calculateCorners(position)));
    }

    public void tick() {
        if (!emit || connections.isEmpty()) return;

        final int currentBatchIndex = currentTick;

        executor.submit(() -> {
            try {
                synchronized (connections) {
                    for (Connection connection : connections) {
                        int randomEdge = ThreadLocalRandom.current().nextInt(EDGES_AMOUNT);
                        spawnParticles(connection, randomEdge, currentBatchIndex);
                    }
                }
            } catch (Exception e) {
                System.err.println("Error while spawning particles: " + e.getMessage());
                e.printStackTrace(System.err);
            }
        });

        currentTick = (currentTick + 1) % PARTICLE_BATCHES;
    }

    public void spawnParticlesOvertime(ServerWorld world, DustParticleEffect effect, ServerPlayerEntity player, Vector3f[] positions, int currentTick) {
        int n = positions.length;
        if (n == 0) return;

        int batches = Math.min(PARTICLE_BATCHES, n);

        for (int index = currentTick; index < n; index += batches) {
            Vector3f p = positions[index];
            world.spawnParticles(player, effect, true, true,
                    p.x, p.y, p.z,
                    1, 0, 0, 0, 0.01);
        }
    }

    public void spawnParticles(Connection connection, int randomEdge, int currentTime) {
        BlockPos pos = connection.getPos();

        Vector3f[][] edges = edgeParticlePositions.get(pos);
        if (edges != null) {
            Vector3f[] positions = edges[randomEdge];
            DustParticleEffect effect = dustParticleEffects[connection.getWeight()];

            if (positions != null && effect != null) {
                spawnParticlesOvertime(world, effect, player, positions, currentTime);
            }
        }

        Vector3f[] xPositions = xParticlePositions.get(pos);
        if (xPositions != null) {
            DustParticleEffect effect = dustParticleEffects[connection.getWeight() + 2];
            spawnParticlesOvertime(world, effect, player, xPositions, currentTime);
        }
    }

    private static Vec3i[] calculateCorners(Vec3i position) {
        return new Vec3i[]{
                position.add(0, 0, 0), position.add(1, 0, 0), position.add(0, 0, 1), position.add(1, 0, 1),
                position.add(0, 1, 0), position.add(1, 1, 0), position.add(0, 1, 1), position.add(1, 1, 1)
        };
    }

    private static Vector3f[][] calculateAllEdgeParticles(Vec3i[] corners) {
        Vector3f[][] edgePositions = new Vector3f[EDGES_AMOUNT][];

        for (int edgeIndex = 0; edgeIndex < EDGES_AMOUNT; edgeIndex++) {
            int[] edge = BLOCK_EDGES[edgeIndex];
            Vec3i start = corners[edge[0]];
            Vec3i end = corners[edge[1]];
            edgePositions[edgeIndex] = calculateEdgeParticles(start, end);
        }

        return edgePositions;
    }

    public static Vector3f[] calculateXParticles(Vector3f center, Direction side) {
        List<Vector3f> positions = new ArrayList<>();
        for (float t = 0; t <= 1; t += X_STEP) {
            float offset1 = X_MIN + (X_MAX - X_MIN) * t;
            float offset2 = X_MAX - (X_MAX - X_MIN) * t;

            positions.add(calculateSidePosition(center, side, offset1, offset1));
            positions.add(calculateSidePosition(center, side, offset1, offset2));
        }

        return positions.toArray(new Vector3f[0]);
    }

    public static Vector3f[] calculateEdgeParticles(Vec3i start, Vec3i end) {
        Vector3f[] positions = new Vector3f[PARTICLE_STEPS + 1];

        int x = start.getX();
        int y = start.getY();
        int z = start.getZ();

        float steps = (float) PARTICLE_STEPS;
        float dx = (end.getX() - x) / steps;
        float dy = (end.getY() - y) / steps;
        float dz = (end.getZ() - z) / steps;

        for (int i = 0; i <= PARTICLE_STEPS; i++) {
            positions[i] = new Vector3f(
                    x + i * dx,
                    y + i * dy,
                    z + i * dz
            );
        }
        return positions;
    }

    public static Vector3f calculateSidePosition(Vector3f center, Direction side, float offsetA, float offsetB) {
        float x = center.x();
        float y = center.y();
        float z = center.z();

        final float halfBlockOffset = 0.5F;

        switch (side) {
            case UP:
            case DOWN:
                x += offsetA;
                z += offsetB;
                y += (side == Direction.UP ? halfBlockOffset : -halfBlockOffset);
                break;
            case NORTH:
            case SOUTH:
                x += offsetA;
                y += offsetB;
                z += side == Direction.SOUTH ? halfBlockOffset : -halfBlockOffset;
                break;
            case EAST:
            case WEST:
                z += offsetA;
                y += offsetB;
                x += side == Direction.EAST ? halfBlockOffset : -halfBlockOffset;
                break;
        }

        return new Vector3f(x, y, z);
    }

    public static void shutdownThread() {
        executor.shutdown();
    }
}
