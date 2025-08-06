package com.rouesvm.extralent.item.custom.connector;

import com.rouesvm.extralent.block.transport.entity.PipeBlockEntity;
import com.rouesvm.extralent.block.transport.entity.PipeState;
import com.rouesvm.extralent.block.transport.entity.connection.Connection;
import com.rouesvm.extralent.item.custom.data.ConnectorData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import static com.rouesvm.extralent.Extralent.HIGHLIGHT_MANAGER;
import static com.rouesvm.extralent.item.custom.connector.ConnectorItem.playSoundConnection;

public class ConnectorLogic {
    public static ActionResult handlePipeBlockInteraction(ConnectorData data, ServerWorld world, PlayerEntity player, PipeBlockEntity pipeEntity, Connection connection) {
        BlockPos currentPos = data.getBlockPos();
        PipeBlockEntity currentEntity = data.getCurrentEntity(world);

        if (currentPos != null)
            HIGHLIGHT_MANAGER.createMultipleHighlights(currentPos, world, (ServerPlayerEntity) player);

        if (currentEntity != null) {
            if (currentEntity.isRemoved()) data.setCurrentEntity(null);

            if (currentEntity == pipeEntity)
                onConnectionChanged(data, world, player, false);
            else ConnectorLogic.tryBind(data, world, player, connection);

            return ActionResult.SUCCESS;
        }

        if (pipeEntity.isConnected()) {
            player.sendMessage(Text.translatable("info.viewer.already_connected"), true);
            return ActionResult.PASS;
        }

        if (currentPos == null)
            HIGHLIGHT_MANAGER.createMultipleHighlights(pipeEntity.getPos(), world, (ServerPlayerEntity) player);

        data.setCurrentEntity(pipeEntity.getPos());
        pipeEntity.onUpdate();

        onConnectionChanged(data, world, player, true);

        pipeEntity.getBlocks().parallelStream().forEach(blockConnection ->
                HIGHLIGHT_MANAGER.addHighlightToMultiple(blockConnection, data.getBlockPos()));

        return ActionResult.SUCCESS;
    }

    public static boolean tryChangeWeight(ConnectorData data, PlayerEntity player, ServerWorld world) {
        PipeBlockEntity entity = data.getCurrentEntity(world);
        if (entity == null || entity.isRemoved()) {
            onConnectionChanged(data, world, player, false);
            return false;
        }

        int oldWeight = data.getWeight();
        int newWeight = entity.setWeight(oldWeight);
        data.setWeight(newWeight);

        if (newWeight != oldWeight) {
            ConnectorItem.playSoundChanged(player, 2f);
            player.sendMessage(Text.translatable("info.viewer.weight_changed").append(" ").append(String.valueOf(newWeight)), true);
            ((ConnectorItem) data.stack().getItem()).decreaseEnergy(data.stack());
        }

        return true;
    }

    public static void tryBind(@NotNull ConnectorData data, ServerWorld world, @NotNull PlayerEntity player, Connection connection) {
        PipeBlockEntity entity = data.getCurrentEntity(world);
        BlockPos base = data.getBlockPos();

        boolean alreadyConnected = entity.getBlocks().contains(connection);

        if (player.isSneaking() && alreadyConnected) {
            boolean removed = entity.removeBlock(connection);
            if (removed) {
                player.sendMessage(Text.translatable("info.viewer.unbound"), true);
                ConnectorItem.playSound(player, -2f);
                HIGHLIGHT_MANAGER.removeHighlightFromMultiple(connection, base);
            }

            return;
        }

        PipeState result = entity.putBlock(connection);
        switch (result) {
            case SUCCESS -> {
                ((ConnectorItem) data.stack().getItem()).decreaseEnergy(data.stack());
                player.sendMessage(Text.translatable("info.viewer.bound"), true);
                ConnectorItem.playSound(player, 2f);
                HIGHLIGHT_MANAGER.replaceHighlightToMultiple(connection, base);
            }
            case IDENTICAL -> {
                boolean removed = entity.removeBlock(connection);
                if (removed && tryChangeWeight(data, player, world)) {
                    connection.setWeight(data.getWeight());
                    entity.putBlock(connection);
                    ConnectorItem.playSoundChanged(player, 3f);
                    HIGHLIGHT_MANAGER.replaceHighlightToMultiple(connection, base);
                }
            }
            case FAR -> player.sendMessage(Text.translatable("info.viewer.far_away"), true);
            case TYPE_ERROR -> player.sendMessage(Text.translatable("info.viewer.type_wrong"), true);
            case OVERFLOW -> player.sendMessage(Text.translatable("info.viewer.overflow"), true);
        }
    }

    public static void onConnectionChanged(@NotNull ConnectorData data, ServerWorld world, PlayerEntity player, boolean connected) {
        data.setVisual(connected);
        ConnectorItem.setTexture(data.stack(), connected);

        PipeBlockEntity entity = data.getCurrentEntity(world);
        if (entity == null) {
            onDisconnected(data, player);
            return;
        }

        entity.setConnected(connected);

        if (connected) {
            ConnectorItem item = (ConnectorItem) data.stack().getItem();
            item.decreaseEnergy(data.stack());
            player.sendMessage(Text.translatable("info.viewer.connected"), true);
            playSoundConnection(player, 3f);
            HIGHLIGHT_MANAGER.createSingularHighlight(world, (ServerPlayerEntity) player, entity.getPos());
        } else onDisconnected(data, player);
    }

    private static void onDisconnected(ConnectorData data, PlayerEntity player) {
        player.sendMessage(Text.translatable("info.viewer.disconnected"), true);
        playSoundConnection(player, 5f);

        if (data.getBlockPos() != null) {
            HIGHLIGHT_MANAGER.clearAllHighlights(data.getBlockPos());
            data.setCurrentEntity(null);
        }
    }
}
