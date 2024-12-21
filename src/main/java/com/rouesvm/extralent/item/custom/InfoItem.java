package com.rouesvm.extralent.item.custom;

import com.rouesvm.extralent.block.entity.BasicMachineBlockEntity;
import com.rouesvm.extralent.item.DoubleTexturedItem;
import com.rouesvm.extralent.item.custom.data.Activated;
import com.rouesvm.extralent.item.custom.data.BasicData;
import com.rouesvm.extralent.item.custom.data.InfoData;
import com.rouesvm.extralent.visual.elements.InfoText;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Objects;
import java.util.UUID;

import static com.rouesvm.extralent.Extralent.ELEMENT_MANAGER;
import static com.rouesvm.extralent.Extralent.HIGHLIGHT_MANAGER;

public class InfoItem extends DoubleTexturedItem {
    public InfoItem(Settings settings) {
        super("viewer", settings, Items.COAL);
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (world != null && !world.isClient) {
            if (!selected) return;
            if (!Activated.showVisual(stack)) return;

            InfoData data = new InfoData(stack);
            if (!Activated.showVisual(stack) && !data.showVisual()) return;
            if (data.getBlockPos() == null ||
                    data.getBlockPos() != null
                            && !entity.getBlockPos().isWithinDistance(data.getBlockPos(), 5)
            ) {
                Activated.setVisual(stack, false);
                data.setVisual(false);
                return;
            }

            if (world.getTime() % 60 == 0)
                HIGHLIGHT_MANAGER.clearAllHighlights(data.getUuid());
            else HIGHLIGHT_MANAGER.tickHighlights(data.getUuid());

            if (data.getDisplay() == InfoData.DISPLAY.UI) {
                var blockEntity = world.getBlockEntity(data.getBlockPos());
                if (!(blockEntity instanceof BasicMachineBlockEntity basicMachineBlock)) return;
                if (basicMachineBlock.infoOnClicked() == null) return;
                data.setVisual(true);
                PlayerEntity player = (PlayerEntity) entity;
                player.sendMessage(basicMachineBlock.infoOnClicked(data.getContent()), true);
            }
        }
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);

        if (world != null && !world.isClient) {
            UUID uuid = BasicData.getUuid(stack);
            InfoData data = new InfoData(stack);
            if (player.isSneaking()) {
                if (data.showVisual()) {
                    data.setVisual(false);
                    Activated.setVisual(stack, false);
                    return TypedActionResult.pass(stack);
                }

                if (ELEMENT_MANAGER.getElement(uuid) != null) {
                    ELEMENT_MANAGER.removeElement(uuid);
                    return TypedActionResult.pass(stack);
                }

                data.setDisplay(data.getDisplay() == InfoData.DISPLAY.FLOATING ? InfoData.DISPLAY.UI : InfoData.DISPLAY.FLOATING);
                player.sendMessage(Text.translatable("info.viewer.display_changed").copy().append(" ").append(data.getDisplay().toString()), true);
            } else if (data.getDisplay() == InfoData.DISPLAY.UI) {
                if (!setContent(data, (ServerWorld) world)) return TypedActionResult.pass(stack);
            }

            return TypedActionResult.success(stack);
        }

        return TypedActionResult.pass(stack);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if (context.getPlayer() != null && !context.getWorld().isClient) {
            ServerWorld world = (ServerWorld) context.getWorld();

            InfoData data = new InfoData(context.getStack());

            var blockEntityResult = world.getBlockEntity(context.getBlockPos());
            if (blockEntityResult instanceof BasicMachineBlockEntity basicPoweredEntity) {
                ConnectorItem.playSoundConnection(context.getPlayer(), 2F);

                HIGHLIGHT_MANAGER.createSingularHighlight(
                        data.getUuid(),
                        (ServerWorld) context.getWorld(),
                        (ServerPlayerEntity) context.getPlayer(),
                        context.getBlockPos()
                );

                if (data.getDisplay() != InfoData.DISPLAY.FLOATING) {
                    Activated.setVisual(context.getStack(), true);
                    if (!data.setBlockPos(basicPoweredEntity.getPos())) setContent(data, world);
                    return ActionResult.PASS;
                }

                UUID uuid = BasicData.getUuid(context.getStack());
                if (ELEMENT_MANAGER.getElement(uuid) != null) ELEMENT_MANAGER.removeElement(uuid);
                if (basicPoweredEntity.infoOnClicked() == null)
                    return ActionResult.PASS;

                Direction direction = context.getSide();
                Vec3d displayPos = context.getBlockPos().toCenterPos().offset(direction, 1)
                        .add(new Vec3d(0, 0.275, 0));

                ELEMENT_MANAGER.createElement(uuid, InfoText.createText(displayPos, basicPoweredEntity, world));
                return ActionResult.SUCCESS;
            } else data.setBlockPos(null);
        }
        return ActionResult.PASS;
    }

    private boolean setContent(InfoData data, ServerWorld world) {
        data.nextContent();

        if (data.getBlockPos() != null) {
            var blockEntity = world.getBlockEntity(data.getBlockPos());
            if (!(blockEntity instanceof BasicMachineBlockEntity basicMachineBlock)) return false;
            if (Objects.equals(basicMachineBlock.infoOnClicked(data.getContent()), Text.empty())) {
                data.nextContent();
            }
        }

        return true;
    }
}
