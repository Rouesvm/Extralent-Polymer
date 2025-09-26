package com.rouesvm.extralent.block.transport.entity;

import com.rouesvm.extralent.block.transport.entity.connection.Connection;
import com.rouesvm.extralent.registries.block.BlockEntityRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TeleporterBlockEntity extends PipeBlockEntity {
    private boolean teleported = false;

    private Connection blockEntity;
    private BlockPos otherPos;

    public TeleporterBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.TELEPORTER_BLOCK_ENTITY, pos, state);
    }

    @Override
    public void tick(World world, BlockPos pos, BlockState state, BlockEntity entity) {
        if (world == null || world.isClient()) return;
        if (teleported) {
            progress++;
            if (progress % 100 != 0) return;

            teleported = false;
            progress = 0;
        }

        if (blockEntity != null && world.getBlockEntity(blockEntity.getPos()) == null) blockEntity = null;
        if (blockEntity == null) for (Connection connection : getConnections()) {
                blockEntity = connection;
        }

        if (blockEntity != null && otherPos == null) otherPos = blockEntity.getPos();
        else otherPos = null;

        if (otherPos == null) return;

        BlockPos blockPos = this.pos;
        double blockTopY = blockPos.getY() + 1.0;

        ServerPlayerEntity player = (ServerPlayerEntity) world.getClosestPlayer(
                blockPos.getX() + 0.5,
                blockTopY,
                blockPos.getZ() + 0.5,
                1.0,
                false
        );

        TeleporterBlockEntity otherEntity = (TeleporterBlockEntity) world.getBlockEntity(otherPos);

        if (otherEntity != null && player != null) {
            double playerX = player.getX();
            double playerY = player.getY();
            double playerZ = player.getZ();

            boolean withinX = playerX >= blockPos.getX() && playerX <= blockPos.getX() + 1;
            boolean withinZ = playerZ >= blockPos.getZ() && playerZ <= blockPos.getZ() + 1;
            boolean nearTopY = Math.abs(playerY - blockTopY) < 0.5;

            if (withinX && withinZ && nearTopY) {
                otherEntity.setTeleported(true);

                player.teleport(
                        otherPos.getX() + 0.5,
                        otherPos.getY() + 1.5,
                        otherPos.getZ() + 0.5,
                        true
                );

                teleported = true;
            }
        }
    }

    @Override
    public boolean incorrectBlock(BlockPos pos) {
        return super.incorrectBlock(pos);
    }

    @Override
    public int getMaxDistance() {
        return 0;
    }

    @Override
    public int getMaxConnections() {
        return 1;
    }

    public void setTeleported(boolean teleported) {
        this.teleported = teleported;
    }
}
