package com.rouesvm.extralent.item.custom;

import com.rouesvm.extralent.block.transport.entity.PipeBlockEntity;
import com.rouesvm.extralent.block.transport.entity.PipeState;
import com.rouesvm.extralent.entity.elements.BlockHighlight;
import com.rouesvm.extralent.item.DoubleTexturedItem;
import com.rouesvm.extralent.item.custom.data.ConnecterData;
import com.rouesvm.extralent.utils.Connection;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
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

import static com.rouesvm.extralent.Extralent.HIGHLIGHT_MANAGER;

public class ConnectorItem extends DoubleTexturedItem {
    public ConnectorItem(Settings settings) {
        super("connector", settings, Items.COAL);
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

            ConnecterData connecterData = new ConnecterData(stack);
            PipeBlockEntity currentBlockEntity = connecterData.getCurrentEntity((ServerWorld) world);

            if (user.isSneaking()) {
                if (connecterData.getCurrentEntity((ServerWorld) world) != null && currentBlockEntity.isRemoved()) {
                    onConnectedChanged(stack, (ServerWorld) world, user, false);
                    return TypedActionResult.success(stack, true);
                }

                int weight = connecterData.getWeight() == 1 ? 0 : 1;
                connecterData.setWeight(weight);

                playSoundChanged(user, 2f);
                user.sendMessage(Text.translatable("info.viewer.weight_changed").copy().append(" ").append(String.valueOf(weight)), true);

                return TypedActionResult.success(stack, true);
            }
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
            onConnectedChanged(context.getStack(), world, context.getPlayer(), false);
        }

        if (blockEntityResult instanceof PipeBlockEntity pipeBlockEntity) {
            if (currentBlockEntity != null) {
                if (currentBlockEntity.isRemoved()) connecterData.setCurrentEntity(null);
                if (currentBlockEntity == pipeBlockEntity)
                    onConnectedChanged(context.getStack(), world, context.getPlayer(), false);
                else sendMessage(connecterData, world, context.getPlayer(), Connection.of(context.getBlockPos(), connecterData.getWeight()));

                return ActionResult.SUCCESS;
            }

            connecterData.setCurrentEntity(pipeBlockEntity.getPos());

            pipeBlockEntity.onUpdate();
            onConnectedChanged(context.getStack(), world, context.getPlayer(), true);
            highlightConnectedBlocks(connecterData.getStack(), world, pipeBlockEntity);

            return ActionResult.SUCCESS;
        } else if (blockEntityResult != null) {
            if (currentBlockEntity != null && !currentBlockEntity.isRemoved()) {
                sendMessage(connecterData, world, context.getPlayer(), Connection.of(context.getBlockPos(), connecterData.getWeight()));
                return ActionResult.SUCCESS;
            }
        }
        return ActionResult.PASS;
    }

    private void onConnectedChanged(ItemStack stack, ServerWorld world, PlayerEntity player, boolean connected) {
        this.setActivated(connected);
        int customModelData = getPolymerCustomModelData(stack, (ServerPlayerEntity) player);
        stack.set(DataComponentTypes.CUSTOM_MODEL_DATA, new CustomModelDataComponent(customModelData));

        ConnecterData connecterData = new ConnecterData(stack);
        PipeBlockEntity currentBlockEntity = connecterData.getCurrentEntity(world);

        if (!connected) {
            player.sendMessage(Text.translatable("info.viewer.disconnected"), true);
            connecterData.setCurrentEntity(null);
            playSoundConnection(player, 5f);
            HIGHLIGHT_MANAGER.clearAllHighlights(stack);
        } else {
            player.sendMessage(Text.translatable("info.viewer.connected"), true);
            playSoundConnection(player, 3f);
            HIGHLIGHT_MANAGER.createSingularHighlight(stack, world, Connection.of(currentBlockEntity.getPos(), 10));
        }
    }

    private void sendMessage(ConnecterData data, ServerWorld world, PlayerEntity player, Connection connection) {
        PipeBlockEntity currentBlockEntity = data.getCurrentEntity(world);

        if (player.isSneaking()) {
            boolean removed = currentBlockEntity.removeBlock(connection);
            if (removed) {
                player.sendMessage(Text.translatable("info.viewer.unbound"), true);
                playSound(player, -2f);
                HIGHLIGHT_MANAGER.removeHighlightFromMultiple(connection.getPos(), data.getStack());
                return;
            }
        }

        PipeState output = currentBlockEntity.putBlock(connection);
        switch (output) {
            case SUCCESS -> {
                player.sendMessage(Text.translatable("info.viewer.bound"), true);
                playSound(player, 2f);
                if (HIGHLIGHT_MANAGER.getHighlightFromMultiple(connection.getPos(), data.getStack()) != null)
                    HIGHLIGHT_MANAGER.removeHighlightFromMultiple(connection.getPos(), data.getStack());
                HIGHLIGHT_MANAGER.addHighlightToMultiple(connection.getPos(),
                        BlockHighlight.createHighlight(world, connection),
                        data.getStack()
                );
            }
            case IDENTICAL -> player.sendMessage(Text.translatable("info.viewer.type_same_bound"), true);
            case FAR -> player.sendMessage(Text.translatable("info.viewer.far_away"), true);
            case TYPE_ERROR -> player.sendMessage(Text.translatable("info.viewer.type_wrong"), true);
        }
    }

    private void highlightConnectedBlocks(ItemStack stack, ServerWorld world, PipeBlockEntity pipeBlockEntity) {
        if (!pipeBlockEntity.getBlocks().isEmpty()) {
            pipeBlockEntity.getBlocks().forEach(connection ->
                    HIGHLIGHT_MANAGER.addHighlightToMultiple(connection.getPos(),
                            BlockHighlight.createHighlight(world, connection),
                            stack)
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
