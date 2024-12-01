package com.rouesvm.extralent.utils;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;

import java.util.HashSet;
import java.util.Objects;

public class Connection {
    private final BlockPos pos;
    private int weight;

    protected Connection(BlockPos pos, int weight) {
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

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public static void writeNbt(NbtCompound nbt, HashSet<Connection> connections, RegistryWrapper.WrapperLookup registries) {
        NbtList nbtList = new NbtList();

        connections.forEach(connection -> {
            NbtCompound nbtCompound = new NbtCompound();
            nbtCompound.putInt("weight", connection.getWeight());
            nbtCompound.putLong("pos", connection.getPos().asLong());
            nbtList.add(nbtCompound);
        });

        if (!nbtList.isEmpty()) nbt.put("storedBlocks", nbtList);
    }

    public static void readNbt(NbtCompound nbt, HashSet<Connection> connections, RegistryWrapper.WrapperLookup registries) {
        NbtList nbtList = nbt.getList("storedBlocks", 10);

        for(int i = 0; i < nbtList.size(); ++i) {
            NbtCompound nbtCompound = nbtList.getCompound(i);
            long l = nbtCompound.getLong("pos");
            int j = nbtCompound.getInt("weight");
            connections.add(of(BlockPos.fromLong(l), j));
        }
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
