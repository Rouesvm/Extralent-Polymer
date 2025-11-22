package com.rouesvm.extralent.item.custom.connector;

import com.rouesvm.extralent.block.transport.entity.PipeBlockEntity;
import com.rouesvm.extralent.item.custom.BasicEnergyItem;
import com.rouesvm.extralent.item.custom.data.Activated;
import com.rouesvm.extralent.item.DoubleTexturedItem;
import com.rouesvm.extralent.block.transport.entity.connection.Connection;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
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
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.List;

import static com.rouesvm.extralent.Extralent.HIGHLIGHT_MANAGER;
import static com.rouesvm.extralent.item.custom.connector.ConnectorLogic.onConnectionChanged;

public class ConnectorItem extends DoubleTexturedItem implements BasicEnergyItem {
    public ConnectorItem(Settings settings) {
        super("connector", settings, Items.COAL);
    }

    @Override
    public long getEnergyCapacity(ItemStack stack) {
        return 1_000;
    }

    @Override
    public long getEnergyMaxInput(ItemStack stack) {
        return 500;
    }

    @Override
    public long getEnergyMaxOutput(ItemStack stack) {
        return 0;
    }

    @Override
    public long getEnergyCost() {
        return 15;
    }

    @Override
    public void modifyClientTooltip(List<Text> tooltip, ItemStack stack, PacketContext context) {
        addEnergyTooltip(tooltip, stack);
    }

    @Override
    public void onItemEntityDestroyed(ItemEntity entity) {
        ConnectorData connectorData = new ConnectorData(entity.getStack());
        PipeBlockEntity currentBlockEntity = connectorData.getCurrentEntity((ServerWorld) entity.getEntityWorld());

        currentBlockEntity.setConnected(false);
        HIGHLIGHT_MANAGER.clearAllHighlights(connectorData.getBlockPos());
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerWorld world, Entity entity, @Nullable EquipmentSlot slot) {
        if (world != null && !world.isClient()) {
            if (!(entity instanceof PlayerEntity player)) return;

            if (!Activated.showVisual(stack)) return;
            if (isLowEnergy(stack, player, false)) return;

            HIGHLIGHT_MANAGER.tickHighlights(ConnectorData.getBlockPos(stack));

            if (player.isHolding(stack.getItem())) {
                PipeBlockEntity currentBlockEntity = ConnectorData.getCurrentEntity(world, stack);
                if (currentBlockEntity == null) {
                    ConnectorLogic.onConnectionChanged(new ConnectorData(stack), world, player, false);
                    return;
                }
                if (!ConnectorData.getVisual(stack)) ConnectorData.setVisual(stack, true);
            } else if (ConnectorData.getVisual(stack)) ConnectorData.setVisual(stack, false);
        }
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if (world != null && !world.isClient()) {
            ItemStack stack = user.getStackInHand(hand);
            if (isLowEnergy(stack, user, true)) return ActionResult.PASS;

            var cast = user.raycast(5, 0, false);
            if (cast.getType() == HitResult.Type.ENTITY)
                return ActionResult.PASS;
            if (cast.getType() == HitResult.Type.BLOCK)
                return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;

            if (user.isSneaking() && ConnectorLogic.tryChangeWeight(new ConnectorData(stack), user, (ServerWorld) world)) {
                user.swingHand(hand, true);
                return ActionResult.SUCCESS;
            }
        }
        return ActionResult.PASS;
    }

    @Override
    public ActionResult useOnBlock(@NotNull ItemUsageContext context) {
        if (context.getWorld().isClient() || context.getPlayer() == null) return ActionResult.PASS;

        ServerWorld world = (ServerWorld) context.getWorld();
        PlayerEntity player = context.getPlayer();
        ItemStack stack = context.getStack();
        BlockPos clickedPos = context.getBlockPos();
        BlockEntity blockEntity = world.getBlockEntity(clickedPos);

        ConnectorData data = new ConnectorData(stack);
        PipeBlockEntity currentEntity = data.getCurrentEntity(world);
        Connection connection = Connection.of(clickedPos, data.getWeight(), context.getSide());

        if ((currentEntity != null && currentEntity.isRemoved())) {
            ConnectorLogic.onConnectionChanged(data, world, player, false);
        }

        if (isLowEnergy(data.stack(), player, true)) {
            return ActionResult.PASS;
        }

        if (HIGHLIGHT_MANAGER.getMultipleHighlights(clickedPos) == null) HIGHLIGHT_MANAGER.createMultipleHighlights(clickedPos, world, (ServerPlayerEntity) player);

        if (blockEntity instanceof PipeBlockEntity pipeEntity) {
            return ConnectorLogic.handlePipeBlockInteraction(data, world, player, pipeEntity, connection);
        }

        if (blockEntity != null && currentEntity != null && !currentEntity.isRemoved()) {
            ConnectorLogic.tryBind(data, world, player, connection);
            return ActionResult.SUCCESS;
        }

        if (player.isSneaking() && ConnectorLogic.tryChangeWeight(data, player, world)) {
            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }

    @Override
    public void onLowEnergy(ItemStack stack, PlayerEntity player) {
        onConnectionChanged(new ConnectorData(stack), (ServerWorld) player.getEntityWorld(), player, false);
    }

    public void decreaseEnergy(ItemStack stack) {
        if (getStoredEnergy(stack) <= 0) return;
        setStoredEnergy(stack, getStoredEnergy(stack) - getEnergyCost());
    }

    public static void playSound(@NotNull PlayerEntity player, float pitch) {
        player.playSoundToPlayer(SoundEvents.BLOCK_NOTE_BLOCK_HARP.value(), SoundCategory.BLOCKS, 1f, pitch + player.getEntityWorld().getRandom().nextFloat() * 0.4F);
    }

    public static void playSoundConnection(@NotNull PlayerEntity player, float pitch) {
        player.playSoundToPlayer(SoundEvents.BLOCK_NOTE_BLOCK_SNARE.value(), SoundCategory.BLOCKS, 1f, pitch + player.getEntityWorld().getRandom().nextFloat() * 0.4F);
    }

    public static void playSoundChanged(@NotNull PlayerEntity player, float pitch) {
        player.playSoundToPlayer(SoundEvents.BLOCK_NOTE_BLOCK_BANJO.value(), SoundCategory.BLOCKS, 1f, pitch + player.getEntityWorld().getRandom().nextFloat() * 0.4F);
    }
}
