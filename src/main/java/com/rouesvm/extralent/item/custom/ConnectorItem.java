package com.rouesvm.extralent.item.custom;

import com.rouesvm.extralent.block.transport.entity.PipeBlockEntity;
import com.rouesvm.extralent.block.transport.entity.PipeState;
import com.rouesvm.extralent.item.custom.data.Activated;
import com.rouesvm.extralent.visual.elements.BlockHighlight;
import com.rouesvm.extralent.item.DoubleTexturedItem;
import com.rouesvm.extralent.item.custom.data.ConnecterData;
import com.rouesvm.extralent.block.transport.entity.connection.Connection;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.base.SimpleEnergyItem;

import java.util.List;
import java.util.UUID;

import static com.rouesvm.extralent.Extralent.HIGHLIGHT_MANAGER;

public class ConnectorItem extends DoubleTexturedItem implements SimpleEnergyItem {
    public ConnectorItem(Settings settings) {
        super("connector", settings, Items.COAL);
    }

    @Override
    public long getEnergyCapacity(ItemStack stack) {
        return 100_000;
    }

    @Override
    public long getEnergyMaxInput(ItemStack stack) {
        return 1_000;
    }

    @Override
    public long getEnergyMaxOutput(ItemStack stack) {
        return 0;
    }

    @Override
    public void modifyClientTooltip(List<Text> tooltip, ItemStack stack, @Nullable ServerPlayerEntity player) {
        tooltip.add(Text.translatable("general.info.stored_energy")
                .append(" ")
                .append(String.valueOf(getStoredEnergy(stack)))
                .setStyle(Style.EMPTY.withColor(Formatting.BLUE)));
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
        if (world != null && !world.isClient) {
            if (!(entity instanceof PlayerEntity player)) return;

            if (!Activated.showVisual(stack)) return;
            if (shouldPass(stack, player, false)) return;

            ConnecterData connecterData = new ConnecterData(stack);
            if (connecterData.showVisual()) HIGHLIGHT_MANAGER.tickHighlights(connecterData.getUuid());

            if (selected) {
                PipeBlockEntity currentBlockEntity = connecterData.getCurrentEntity((ServerWorld) world);
                if (currentBlockEntity == null) {
                    onConnectedChanged(connecterData, (ServerWorld) world, player, false);
                    return;
                }
                if (!connecterData.showVisual()) connecterData.setVisual(true);
            } else if (connecterData.showVisual()) connecterData.setVisual(false);
        }
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (world != null && !world.isClient) {
            if (shouldPass(stack, user, true)) return TypedActionResult.pass(stack);

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
    public ActionResult useOnBlock(@NotNull ItemUsageContext context) {
        if (context.getWorld().isClient || context.getPlayer() == null) return ActionResult.PASS;
        ServerWorld world = (ServerWorld) context.getWorld();
        var blockEntityResult = world.getBlockEntity(context.getBlockPos());

        ConnecterData connecterData = new ConnecterData(context.getStack());
        PipeBlockEntity currentBlockEntity = connecterData.getCurrentEntity(world);

        if (currentBlockEntity != null && currentBlockEntity.isRemoved()) {
            onConnectedChanged(connecterData, world, context.getPlayer(), false);
        }

        Connection connection = Connection.of(context.getBlockPos(), connecterData.getWeight(), context.getSide());

        if (shouldPass(connecterData.getStack(), context.getPlayer(), true)) return ActionResult.PASS;

        if (blockEntityResult instanceof PipeBlockEntity pipeBlockEntity) {
            if (currentBlockEntity != null) {
                if (currentBlockEntity.isRemoved()) connecterData.setCurrentEntity(null);
                if (currentBlockEntity == pipeBlockEntity)
                    onConnectedChanged(connecterData, world, context.getPlayer(), false);
                else sendMessage(connecterData, world, context.getPlayer(), connection);

                return ActionResult.SUCCESS;
            }

            if (pipeBlockEntity.isConnected()) {
                context.getPlayer().sendMessage(Text.translatable("info.viewer.already_connected"), true);
                return ActionResult.PASS;
            }

            connecterData.setCurrentEntity(pipeBlockEntity.getPos());

            pipeBlockEntity.onUpdate();
            onConnectedChanged(connecterData, world, context.getPlayer(), true);

            if (!pipeBlockEntity.getBlocks().isEmpty()) {
                pipeBlockEntity.getBlocks().parallelStream().forEach(blockConnection ->
                        HIGHLIGHT_MANAGER.addHighlightToMultiple(blockConnection.getPos(),
                                BlockHighlight.createHighlight(world, (ServerPlayerEntity) context.getPlayer(), blockConnection),
                                connecterData.getUuid())
                );
            }

            return ActionResult.SUCCESS;
        } else if (blockEntityResult != null
                && currentBlockEntity != null
                && !currentBlockEntity.isRemoved()
        ) {
            sendMessage(connecterData, world, context.getPlayer(), connection);
            return ActionResult.SUCCESS;
        } else if (context.getPlayer().isSneaking() && changeWeight(context.getPlayer(), world, context.getStack()))
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

        decreaseEnergy(stack);

        return true;
    }

    private void onConnectedChanged(@NotNull ConnecterData data, ServerWorld world, PlayerEntity player, boolean connected) {
        this.setTexture(data.getStack(), connected);
        PipeBlockEntity currentBlockEntity = data.getCurrentEntity(world);
        data.setVisual(connected);

        if (currentBlockEntity == null) {
            onEntityNull(data, player);
            return;
        }

        if (!connected) {
            currentBlockEntity.setConnected(false);
            onEntityNull(data, player);
        } else {
            decreaseEnergy(data.getStack());
            player.sendMessage(Text.translatable("info.viewer.connected"), true);
            currentBlockEntity.setConnected(true);
            playSoundConnection(player, 3f);
            HIGHLIGHT_MANAGER.createSingularHighlight(data.getUuid(), world, (ServerPlayerEntity) player, Connection.of(currentBlockEntity.getPos(), 10));
        }
    }

    private void onEntityNull(ConnecterData data, PlayerEntity player) {
        player.sendMessage(Text.translatable("info.viewer.disconnected"), true);
        data.setCurrentEntity(null);
        playSoundConnection(player, 5f);
        HIGHLIGHT_MANAGER.clearAllHighlights(data.getUuid());
    }

    private void sendMessage(@NotNull ConnecterData data, ServerWorld world, @NotNull PlayerEntity player, Connection connection) {
        PipeBlockEntity currentBlockEntity = data.getCurrentEntity(world);

        if (player.isSneaking()) {
            boolean removed = currentBlockEntity.removeBlock(connection);
            if (removed) {
                player.sendMessage(Text.translatable("info.viewer.unbound"), true);
                playSound(player, -2f);
                HIGHLIGHT_MANAGER.removeHighlightFromMultiple(connection.getPos(), data.getUuid());
                return;
            }
        }

        PipeState output = currentBlockEntity.putBlock(connection);
        switch (output) {
            case SUCCESS -> {
                decreaseEnergy(data.getStack());
                player.sendMessage(Text.translatable("info.viewer.bound"), true);
                playSound(player, 2f);
                if (HIGHLIGHT_MANAGER.getHighlightFromMultiple(connection.getPos(), data.getUuid()) != null)
                    HIGHLIGHT_MANAGER.removeHighlightFromMultiple(connection.getPos(), data.getUuid());
                HIGHLIGHT_MANAGER.addHighlightToMultiple(connection.getPos(),
                        BlockHighlight.createHighlight(world, (ServerPlayerEntity) player, connection),
                        data.getUuid()
                );
            }
            case IDENTICAL -> {
                boolean removed = currentBlockEntity.removeBlock(connection);
                if (removed && changeWeight(player, world, data.getStack())) {
                    data = new ConnecterData(data.getStack());
                    connection.setWeight(data.getWeight());
                    currentBlockEntity.putBlock(connection);

                    playSoundChanged(player, 3f);
                    HIGHLIGHT_MANAGER.removeHighlightFromMultiple(connection.getPos(), data.getUuid());
                    HIGHLIGHT_MANAGER.addHighlightToMultiple(connection.getPos(),
                            BlockHighlight.createHighlight(world, (ServerPlayerEntity) player, connection),
                            data.getUuid()
                    );
                }
            }
            case FAR -> player.sendMessage(Text.translatable("info.viewer.far_away"), true);
            case TYPE_ERROR -> player.sendMessage(Text.translatable("info.viewer.type_wrong"), true);
        }
    }

    private boolean shouldPass(@NotNull ItemStack stack, PlayerEntity player, boolean showMessage) {
        if (getStoredEnergy(stack) <= 0) {
            if (showMessage) player.sendMessage(Text.translatable("general.info.out_of_energy")
                    .setStyle(Style.EMPTY.withColor(Formatting.RED)), true);
            this.setTexture(stack, false);
            return true;
        }

        return false;
    }

    private void decreaseEnergy(ItemStack stack) {
        if (getStoredEnergy(stack) < 15) return;
        setStoredEnergy(stack, getStoredEnergy(stack) - 15);
    }

    private void playSound(@NotNull PlayerEntity player, float pitch) {
        player.playSoundToPlayer(SoundEvents.BLOCK_NOTE_BLOCK_HARP.value(), SoundCategory.BLOCKS, 1f, pitch + player.getWorld().getRandom().nextFloat() * 0.4F);
    }

    private void playSoundConnection(@NotNull PlayerEntity player, float pitch) {
        player.playSoundToPlayer(SoundEvents.BLOCK_NOTE_BLOCK_SNARE.value(), SoundCategory.BLOCKS, 1f, pitch + player.getWorld().getRandom().nextFloat() * 0.4F);
    }

    private void playSoundChanged(@NotNull PlayerEntity player, float pitch) {
        player.playSoundToPlayer(SoundEvents.BLOCK_NOTE_BLOCK_BANJO.value(), SoundCategory.BLOCKS, 1f, pitch + player.getWorld().getRandom().nextFloat() * 0.4F);
    }
}
