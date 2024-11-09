package com.rouesvm.extralent.item.custom;

import com.rouesvm.extralent.block.transport.entity.PipeBlockEntity;
import com.rouesvm.extralent.block.transport.entity.PipeState;
import com.rouesvm.extralent.entity.elements.BlockHighlight;
import com.rouesvm.extralent.item.BasicPolymerItem;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Set;

public class ConnectorItem extends BasicPolymerItem {
    private HashMap<BlockPos, BlockHighlight> blockHighlights = new HashMap<>();
    private PipeBlockEntity currentBlockEntity;

    public ConnectorItem(Settings settings) {
        super("connector", settings, Items.COAL);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if (!context.getWorld().isClient && context.getPlayer() != null) {
            ServerWorld world = (ServerWorld) context.getWorld();
            var blockEntityResult = world.getBlockEntity(context.getBlockPos());

            if (blockEntityResult instanceof PipeBlockEntity pipeBlockEntity) {
                if (this.currentBlockEntity != null) {
                    if (this.currentBlockEntity.isRemoved())
                        this.currentBlockEntity = null;

                    if (this.currentBlockEntity == pipeBlockEntity) {
                        context.getPlayer().sendMessage(Text.literal("Disconnected"), true);
                        this.currentBlockEntity = null;
                        this.blockHighlights.forEach((pos, blockHighlight) -> blockHighlight.kill());
                        this.blockHighlights = new HashMap<>();
                    } else {
                        sendMessage(world, context.getPlayer(), context.getBlockPos());
                    }
                    return ActionResult.SUCCESS;
                }

                pipeBlockEntity.onUpdate();

                this.currentBlockEntity = pipeBlockEntity;
                context.getStack().set(DataComponentTypes.CONTAINER, ContainerComponent.DEFAULT);
                context.getPlayer().sendMessage(Text.literal("Connected"), true);

                if (!this.currentBlockEntity.getBlocks().isEmpty()) {
                    this.currentBlockEntity.getBlocks().forEach(pos -> this.blockHighlights.put(pos, BlockHighlight.createHighlight(world, pos)));
                }

                return ActionResult.SUCCESS;
            } else if (blockEntityResult != null) {
                if (this.currentBlockEntity != null) {
                    sendMessage(world, context.getPlayer(), context.getBlockPos());
                    return ActionResult.SUCCESS;
                }
            }
        }
        return ActionResult.PASS;
    }

    private void sendMessage(ServerWorld world, PlayerEntity player, BlockPos pos) {
        if (player.isSneaking()) {
            boolean removed = this.currentBlockEntity.removeBlock(pos);
            if (removed) {
                player.sendMessage(Text.literal("Unbound"), true);
                BlockHighlight blockHighlight = this.blockHighlights.get(pos);
                if (blockHighlight != null) {
                    this.blockHighlights.remove(pos);
                    blockHighlight.kill();
                }
                return;
            }
        }

        PipeState output = this.currentBlockEntity.putBlock(pos);
        switch (output) {
            case SUCCESS -> {
                player.sendMessage(Text.literal("Bound"), true);
                this.blockHighlights.put(pos, BlockHighlight.createHighlight(world, pos));
            }
            case IDENTICAL -> player.sendMessage(Text.literal("Already bound"), true);
            case FAR -> player.sendMessage(Text.literal("Too far"), true);
            case TYPE_ERROR -> player.sendMessage(Text.literal("Wrong type"), true);
        }
    }

    private Text tableToText(Set<BlockPos> blockPosSet) {
        Text text = Text.literal("");
        for (BlockPos blockPos : blockPosSet) {
            text = Text.of(text).copy().append(" ").append(blockPos.toString());
        }
        return text;
    }
}
