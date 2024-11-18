package com.rouesvm.extralent.utils;

import net.minecraft.util.math.BlockPos;

import java.util.Objects;

public class Connection {
    private final BlockPos pos;
    private final int weight;

    private Connection(BlockPos pos, int weight) {
        this.pos = pos;
        this.weight = weight;
    }

    public static Connection of(BlockPos pos, int weight) {
        return new Connection(pos, weight);
    }

    public BlockPos getPos() {
        return pos;
    }

    public int getWeight() {
        return weight;
    }

    public long asLong() {
        int POS_BITS = 60;
        int WEIGHT_BITS = 4;

        long posMask = (1L << POS_BITS) - 1;
        long weightMask = (1L << WEIGHT_BITS) - 1;

        if ((this.getPos().asLong() & ~posMask) != 0 || (this.getWeight() & ~weightMask) != 0) {
            return 0;
        }

        return (this.getPos().asLong() << WEIGHT_BITS) | (this.getWeight() & weightMask);
    }

    public static Connection fromLong(long packed) {
        return Connection.of(BlockPos.fromLong(extractPos(packed)), extractWeight(packed));
    }

    private static long extractPos(long combined) {
        int WEIGHT_BITS = 4;
        return combined >>> WEIGHT_BITS;
    }

    private static int extractWeight(long combined) {
        int WEIGHT_BITS = 4;
        long weightMask = (1L << WEIGHT_BITS) - 1;
        return (int)(combined & weightMask);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Connection other = (Connection) obj;
        return Objects.equals(this.getPos(), other.getPos());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPos());
    }
}
