package com.rouesvm.extralent.visual;

import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import org.joml.Vector3f;

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

    public static void visualizeScanArea(BlockPos machinePos, ServerWorld world, Vec3i size) {
        BlockPos start = machinePos.add(-size.getX() / 2, 0, -size.getZ() / 2); // Bottom-left front corner
        BlockPos end = machinePos.add(size.getX() / 2, size.getY(), size.getZ() / 2); // Top-right back corner

        for (int i = 0; i < 4; i++) {
            BlockPos topCorner = getCorner(start, end, i, true);
            BlockPos bottomCorner = getCorner(start, end, i, false);
            LineDrawer.drawLine(new DustParticleEffect(new Vector3f(0F, 0.75F, 1F), 0.75F), topCorner, bottomCorner, world);
        }

        for (int i = 0; i < 4; i++) {
            BlockPos startCorner = getCorner(start, end, i, true);
            BlockPos endCorner = getCorner(start, end, (i + 1) % 4, true);
            LineDrawer.drawLine(new DustParticleEffect(new Vector3f(0F, 0.75F, 1F), 0.75F), startCorner, endCorner, world);
        }

        for (int i = 0; i < 4; i++) {
            BlockPos startCorner = getCorner(start, end, i, false);
            BlockPos endCorner = getCorner(start, end, (i + 1) % 4, false);
            LineDrawer.drawLine(new DustParticleEffect(new Vector3f(0F, 0.75F, 1F), 0.75F), startCorner, endCorner, world);
        }
    }

    private static BlockPos getCorner(BlockPos start, BlockPos end, int cornerIndex, boolean isTop) {
        int x = (cornerIndex == 0 || cornerIndex == 3) ? start.getX() : end.getX();
        int z = (cornerIndex == 0 || cornerIndex == 1) ? start.getZ() : end.getZ();
        int y = isTop ? end.getY() : start.getY();
        return new BlockPos(x, y, z);
    }
}
