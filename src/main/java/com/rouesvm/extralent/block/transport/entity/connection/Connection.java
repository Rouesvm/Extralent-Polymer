package com.rouesvm.extralent.block.transport.entity.connection;

import com.mojang.serialization.Codec;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.storage.NbtReadView;
import net.minecraft.storage.NbtWriteView;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.ErrorReporter;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class Connection {
    private final BlockPos pos;
    private final Direction side;
    private int weight;

    protected Connection(BlockPos pos, int weight, Direction side) {
        this.pos = pos;
        this.weight = weight;
        this.side = side;
    }

    public static Connection of(BlockPos pos, int weight, Direction side) {
        return new Connection(pos, weight, side);
    }
    public static Connection of(BlockPos pos) {
        return new Connection(pos, 0, null);
    }

    public Direction getSide() {
        return side;
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

    public static final Codec<List<Connection>> CODEC =
            Codec.list(NbtCompound.CODEC).xmap(
                    list -> {
                        List<Connection> connections = new ArrayList<>();
                        for (NbtCompound entry : list) {
                            int side = entry.getInt("side", 0);
                            long pos = entry.getLong("pos", 0);
                            int weight = entry.getInt("weight", 0);
                            connections.add(of(BlockPos.fromLong(pos), weight, Direction.byIndex(side)));
                        }
                        return connections;
                    },
                    connections -> {
                        List<NbtCompound> out = new ArrayList<>();
                        for (Connection connection : connections) {
                            NbtCompound compound = new NbtCompound();
                            @Nullable Direction side = connection.getSide();
                            compound.putInt("side", side != null ? side.getIndex() : Direction.DOWN.getIndex());
                            compound.putInt("weight", connection.getWeight());
                            compound.putLong("pos", connection.getPos().asLong());
                            out.add(compound);
                        }
                        return out;
                    }
            );

    public static void write(WriteView data, String name, List<Connection> connections) {
        WriteView.ListAppender<List<Connection>> nbtList = data.getListAppender(name, CODEC);
        nbtList.add(connections);
    }

    public static void read(ReadView data, String name, List<Connection> connections) {
        ReadView.TypedListReadView<List<Connection>> nbtList = data.getTypedListView(name, CODEC);
        Optional<List<Connection>> dataConnection = nbtList.stream().findFirst();
        dataConnection.ifPresent((connections::addAll));
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
