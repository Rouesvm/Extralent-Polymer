package com.rouesvm.extralent.visual.elements;

import com.rouesvm.extralent.block.transport.entity.connection.Connection;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BlockHighlights extends ElementHolder {
    private final static ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    public final Set<Connection> connections;
    private final DustParticleEffect particleType;

    private final ServerWorld world;
    private final ServerPlayerEntity player;

    public boolean emit = false;

    public BlockHighlights(@NotNull ServerWorld world, @NotNull ServerPlayerEntity player, @NotNull Set<Connection> connections, @NotNull Vector3f color) {
        this.particleType = new DustParticleEffect(0, 0.725F);

        this.world = world;
        this.player = player;

        this.connections = connections;
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

            world.spawnParticles(player, particleType, true,true,
                    x, y, z,
                    0,
                    0, 0, 0,
                    0.001);
        }
    }

    @Override
    public void tick() {
        if (this.getAttachment() == null) return;

        this.updatePosition();

        for (Connection connection : connections) {

        }
    }
}
