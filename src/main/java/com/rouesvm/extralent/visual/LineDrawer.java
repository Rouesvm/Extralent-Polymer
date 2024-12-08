package com.rouesvm.extralent.visual;

import net.minecraft.particle.ParticleEffect;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class LineDrawer {
    public static <T extends ParticleEffect> void drawLine(T particleType, BlockPos start, BlockPos end, ServerWorld world) {
        double x1 = start.getX() + 0.5;
        double y1 = start.getY() + 0.5;
        double z1 = start.getZ() + 0.5;
        double x2 = end.getX() + 0.5;
        double y2 = end.getY() + 0.5;
        double z2 = end.getZ() + 0.5;

        double distance = Math.sqrt(start.getSquaredDistance(end));
        int steps = (int) Math.ceil(distance / 1.5);
        for (int i = 0; i <= steps; i++) {
            double t = i / (double) steps;
            double x = x1 + t * (x2 - x1);
            double y = y1 + t * (y2 - y1);
            double z = z1 + t * (z2 - z1);

            world.spawnParticles(particleType, x, y, z, 0, 0, 0, 0, 0);
        }
    }
}
