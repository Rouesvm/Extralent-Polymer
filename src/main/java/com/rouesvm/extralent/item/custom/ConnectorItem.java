package com.rouesvm.extralent.item.custom;

import com.rouesvm.extralent.Extralent;
import com.rouesvm.extralent.block.transport.entity.PipeBlockEntity;
import com.rouesvm.extralent.block.transport.entity.PipeState;
import com.rouesvm.extralent.entity.elements.BlockHighlight;
import com.rouesvm.extralent.item.BasicPolymerItem;
import com.rouesvm.extralent.utils.Connection;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BundleItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.HashMap;
import java.util.Objects;

public class ConnectorItem extends BasicPolymerItem {
    private HashMap<BlockPos, BlockHighlight> blockHighlights = new HashMap<>();

    private PipeBlockEntity currentBlockEntity;
    private BlockHighlight blockEntityHighlight;

    private boolean connected;

    private int weight = 0;

    public ConnectorItem(Settings settings) {
        super("connector", settings, Items.COAL);
    }

    @Override
    public @Nullable Identifier getPolymerItemModel(ItemStack stack, PacketContext context) {
        if (connected) return Extralent.of("connector_on");
        return Extralent.of("connector");
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if (world != null && !world.isClient) {
            var cast = user.raycast(5, 0, false);
            if (cast.getType() == HitResult.Type.ENTITY)
                return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
            if (cast.getType() == HitResult.Type.BLOCK)
                return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;

            if (user.isSneaking()) {
                ItemStack itemStack = user.getStackInHand(hand);

                if (itemStack.get(DataComponentTypes.CUSTOM_DATA) != null) {
                    NbtCompound compound = Objects.requireNonNull(itemStack.get(DataComponentTypes.CUSTOM_DATA)).copyNbt();
                    if (compound != null) this.weight = compound.getInt("weight");
                }

                this.weight = weight == 1 ? 0 : 1;
                playSoundChanged(user, 2f);

                user.sendMessage(Text.translatable("info.viewer.weight_changed").copy().append(" ").append(String.valueOf(weight)), true);

                NbtCompound nbtCompound = new NbtCompound();
                nbtCompound.putInt("weight", weight);
                itemStack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbtCompound));

                return ActionResult.SUCCESS;
            }
        }
        return super.use(world, user, hand);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if (!context.getWorld().isClient && context.getPlayer() != null) {
            ServerWorld world = (ServerWorld) context.getWorld();
            var blockEntityResult = world.getBlockEntity(context.getBlockPos());

            if (blockEntityResult instanceof PipeBlockEntity pipeBlockEntity) {
                if (currentBlockEntity != null) {
                    if (currentBlockEntity.isRemoved())
                        this.currentBlockEntity = null;

                    if (currentBlockEntity == pipeBlockEntity)
                        onConnectedChanged(context.getStack(), world, context.getPlayer(), false);
                    else sendMessage(world, context.getPlayer(), Connection.of(context.getBlockPos(), this.weight));

                    return ActionResult.SUCCESS;
                }

                pipeBlockEntity.onUpdate();
                this.currentBlockEntity = pipeBlockEntity;

                onConnectedChanged(context.getStack(), world, context.getPlayer(), true);

                if (!this.currentBlockEntity.getBlocks().isEmpty()) {
                    this.currentBlockEntity.getBlocks().forEach(connection -> this.blockHighlights.put(connection.getPos(),
                            BlockHighlight.createHighlight(world, connection))
                    );
                }

                return ActionResult.SUCCESS;
            } else if (blockEntityResult != null) {
                if (this.currentBlockEntity != null) {
                    sendMessage(world, context.getPlayer(), Connection.of(context.getBlockPos(), this.weight));
                    return ActionResult.SUCCESS;
                }
            }
        }
        return ActionResult.PASS;
    }

    private void onConnectedChanged(ItemStack stack, ServerWorld world, PlayerEntity player, boolean connected) {
        this.connected = connected;
        stack.set(DataComponentTypes.ITEM_MODEL, getPolymerItemModel(stack, PacketContext.create()));
        if (!connected) {
            player.sendMessage(Text.translatable("info.viewer.disconnected"), true);
            playSoundConnection(player, 5f);
            blockEntityHighlight.kill();

            this.currentBlockEntity = null;
            this.blockHighlights.forEach((pos, blockHighlight) -> blockHighlight.kill());
            this.blockHighlights = new HashMap<>();
        } else {
            player.sendMessage(Text.translatable("info.viewer.connected"), true);
            playSoundConnection(player, 3f);
            this.blockEntityHighlight = BlockHighlight.createHighlight(world,
                    Connection.of(currentBlockEntity.getPos(), 10));
        }
    }

    private void sendMessage(ServerWorld world, PlayerEntity player, Connection connection) {
        if (player.isSneaking()) {
            boolean removed = this.currentBlockEntity.removeBlock(connection);
            if (removed) {
                player.sendMessage(Text.translatable("info.viewer.unbound"), true);
                playSound(player, -2f);
                BlockHighlight blockHighlight = this.blockHighlights.get(connection.getPos());
                if (blockHighlight != null) {
                    this.blockHighlights.remove(connection.getPos());
                    blockHighlight.kill();
                }
                return;
            }
        }

        PipeState output = this.currentBlockEntity.putBlock(connection);
        switch (output) {
            case SUCCESS -> {
                player.sendMessage(Text.translatable("info.viewer.bound"), true);
                playSound(player, 2f);
                if (this.blockHighlights.get(connection.getPos()) != null)
                    this.blockHighlights.get(connection.getPos()).kill();
                this.blockHighlights.put(connection.getPos(),
                        BlockHighlight.createHighlight(world, connection)
                );
            }
            case IDENTICAL -> player.sendMessage(Text.translatable("info.viewer.type_same_bound"), true);
            case FAR -> player.sendMessage(Text.translatable("info.viewer.far_away"), true);
            case TYPE_ERROR -> player.sendMessage(Text.translatable("info.viewer.type_wrong"), true);
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
