package com.rouesvm.extralent.item.custom;

import com.rouesvm.extralent.block.transport.entity.PipeBlockEntity;
import com.rouesvm.extralent.block.transport.entity.PipeState;
import com.rouesvm.extralent.entity.elements.BlockHighlight;
import com.rouesvm.extralent.item.DoubleTexturedItem;
import com.rouesvm.extralent.item.custom.data.ConnecterData;
import com.rouesvm.extralent.utils.Connection;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;

import java.util.UUID;

import static com.rouesvm.extralent.Extralent.HIGHLIGHT_MANAGER;

public class ConnectorItem extends DoubleTexturedItem {
    public ConnectorItem(Settings settings) {
        super("connector", settings, Items.COAL);
    }

    @Override
    public void onItemEntityDestroyed(ItemEntity entity) {
        ConnecterData connecterData = new ConnecterData(entity.getStack());
        PipeBlockEntity currentBlockEntity = connecterData.getCurrentEntity((ServerWorld) entity.getWorld());

        currentBlockEntity.setConnected(false);
        HIGHLIGHT_MANAGER.clearAllHighlights(connecterData.getUuid());
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (world == null || world.isClient) return;

        ConnecterData connecterData = new ConnecterData(stack);
        if (connecterData.showVisual()) HIGHLIGHT_MANAGER.tickHighlights(connecterData.getUuid());

        if (selected) {
            PipeBlockEntity currentBlockEntity = connecterData.getCurrentEntity((ServerWorld) world);
            if (currentBlockEntity == null) return;
            if (!connecterData.showVisual()) connecterData.setVisual(true);
        } else if (connecterData.showVisual()) connecterData.setVisual(false);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (world != null && !world.isClient) {
            var cast = user.raycast(5, 0, false);
            if (cast.getType() == HitResult.Type.ENTITY)
                return TypedActionResult.pass(stack);
            if (cast.getType() == HitResult.Type.BLOCK)
                return TypedActionResult.pass(stack);

            if (user.isSneaking() && changeWeight(user, (ServerWorld) world, stack))
                return TypedActionResult.success(stack);
        }
        return TypedActionResult.pass(stack);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if (context.getWorld().isClient || context.getPlayer() == null) return ActionResult.PASS;
        ServerWorld world = (ServerWorld) context.getWorld();
        var blockEntityResult = world.getBlockEntity(context.getBlockPos());

        ConnecterData connecterData = new ConnecterData(context.getStack());
        PipeBlockEntity currentBlockEntity = connecterData.getCurrentEntity(world);

        if (currentBlockEntity != null && currentBlockEntity.isRemoved()) {
            onConnectedChanged(connecterData, world, context.getPlayer(), false);
        }

        if (blockEntityResult instanceof PipeBlockEntity pipeBlockEntity) {
            if (currentBlockEntity != null) {
                if (currentBlockEntity.isRemoved()) connecterData.setCurrentEntity(null);
                if (currentBlockEntity == pipeBlockEntity)
                    onConnectedChanged(connecterData, world, context.getPlayer(), false);
                else sendMessage(connecterData, world, context.getPlayer(), Connection.of(context.getBlockPos(), connecterData.getWeight()));

                return ActionResult.SUCCESS;
            }

            if (pipeBlockEntity.isConnected()) {
                context.getPlayer().sendMessage(Text.translatable("info.viewer.already_connected"), true);
                return ActionResult.PASS;
            }

            connecterData.setCurrentEntity(pipeBlockEntity.getPos());

            pipeBlockEntity.onUpdate();
            onConnectedChanged(connecterData, world, context.getPlayer(), true);
            highlightConnectedBlocks(connecterData.getUuid(), world, pipeBlockEntity);

            return ActionResult.SUCCESS;
        } else if (blockEntityResult != null
                && currentBlockEntity != null
                && !currentBlockEntity.isRemoved()
        ) {
            sendMessage(connecterData, world, context.getPlayer(), Connection.of(context.getBlockPos(), connecterData.getWeight()));
            return ActionResult.SUCCESS;
        } else if (changeWeight(context.getPlayer(), world, context.getStack()))
                return ActionResult.SUCCESS;
        else return ActionResult.PASS;
    }

    private boolean changeWeight(PlayerEntity player, ServerWorld world, ItemStack stack) {
        ConnecterData connecterData = new ConnecterData(stack);
        PipeBlockEntity currentBlockEntity = connecterData.getCurrentEntity(world);

        if (connecterData.getCurrentEntity(world) != null && currentBlockEntity.isRemoved()) {
            onConnectedChanged(connecterData, world, player, false);
            return false;
        } else if (currentBlockEntity == null) return false;

        int weight = connecterData.getWeight() == 1 ? 0 : 1;
        connecterData.setWeight(weight);

        playSoundChanged(player, 2f);
        player.sendMessage(Text.translatable("info.viewer.weight_changed").copy().append(" ").append(String.valueOf(weight)), true);

        return true;
    }

    private void onConnectedChanged(ConnecterData data, ServerWorld world, PlayerEntity player, boolean connected) {
        this.setActivated(connected);
        int customModelData = getPolymerCustomModelData(data.getStack(), (ServerPlayerEntity) player);
        data.getStack().set(DataComponentTypes.CUSTOM_MODEL_DATA, new CustomModelDataComponent(customModelData));

        PipeBlockEntity currentBlockEntity = data.getCurrentEntity(world);

        if (!connected) {
            player.sendMessage(Text.translatable("info.viewer.disconnected"), true);
            currentBlockEntity.setConnected(false);
            data.setCurrentEntity(null);
            playSoundConnection(player, 5f);
            HIGHLIGHT_MANAGER.clearAllHighlights(data.getUuid());
        } else {
            player.sendMessage(Text.translatable("info.viewer.connected"), true);
            currentBlockEntity.setConnected(true);
            playSoundConnection(player, 3f);
            HIGHLIGHT_MANAGER.createSingularHighlight(data.getUuid(), world, Connection.of(currentBlockEntity.getPos(), 10));
        }
    }

    private void sendMessage(ConnecterData data, ServerWorld world, PlayerEntity player, Connection connection) {
        PipeBlockEntity currentBlockEntity = data.getCurrentEntity(world);

        if (player.isSneaking()) {
            boolean removed = currentBlockEntity.removeBlock(connection);
            if (removed && changeWeight(player, world, data.getStack())) {
                data = new ConnecterData(data.getStack());
                connection.setWeight(data.getWeight());
                currentBlockEntity.putBlock(connection);

                playSoundChanged(player, 3f);
                HIGHLIGHT_MANAGER.removeHighlightFromMultiple(connection.getPos(), data.getUuid());
                HIGHLIGHT_MANAGER.addHighlightToMultiple(connection.getPos(),
                        BlockHighlight.createHighlight(world, connection),
                        data.getUuid()
                );
                return;
            }
        }

        PipeState output = currentBlockEntity.putBlock(connection);
        switch (output) {
            case SUCCESS -> {
                player.sendMessage(Text.translatable("info.viewer.bound"), true);
                playSound(player, 2f);
                if (HIGHLIGHT_MANAGER.getHighlightFromMultiple(connection.getPos(), data.getUuid()) != null)
                    HIGHLIGHT_MANAGER.removeHighlightFromMultiple(connection.getPos(), data.getUuid());
                HIGHLIGHT_MANAGER.addHighlightToMultiple(connection.getPos(),
                        BlockHighlight.createHighlight(world, connection),
                        data.getUuid()
                );
            }
            case IDENTICAL -> {
                boolean removed = currentBlockEntity.removeBlock(connection);
                if (removed) {
                    player.sendMessage(Text.translatable("info.viewer.unbound"), true);
                    playSound(player, -2f);
                    HIGHLIGHT_MANAGER.removeHighlightFromMultiple(connection.getPos(), data.getUuid());
                }
            }
            case FAR -> player.sendMessage(Text.translatable("info.viewer.far_away"), true);
            case TYPE_ERROR -> player.sendMessage(Text.translatable("info.viewer.type_wrong"), true);
        }
    }

    private void highlightConnectedBlocks(UUID uuid, ServerWorld world, PipeBlockEntity pipeBlockEntity) {
        if (!pipeBlockEntity.getBlocks().isEmpty()) {
            pipeBlockEntity.getBlocks().forEach(connection ->
                    HIGHLIGHT_MANAGER.addHighlightToMultiple(connection.getPos(),
                            BlockHighlight.createHighlight(world, connection),
                            uuid)
            );
        }
    }

    private void playSound(PlayerEntity player, float pitch) {
        player.playSoundToPlayer(SoundEvents.BLOCK_NOTE_BLOCK_HARP.value(), SoundCategory.BLOCKS, 1f, pitch + player.getWorld().getRandom().nextFloat() * 0.4F);
    }

    private void playSoundConnection(PlayerEntity player, float pitch) {
        player.playSoundToPlayer(SoundEvents.BLOCK_NOTE_BLOCK_SNARE.value(), SoundCategory.BLOCKS, 1f, pitch + player.getWorld().getRandom().nextFloat() * 0.4F);
    }

    private void playSoundChanged(PlayerEntity player, float pitch) {
        player.playSoundToPlayer(SoundEvents.BLOCK_NOTE_BLOCK_BANJO.value(), SoundCategory.BLOCKS, 1f, pitch + player.getWorld().getRandom().nextFloat() * 0.4F);
    }
}
