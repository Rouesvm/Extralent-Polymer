package com.rouesvm.extralent.item.custom.info;

import com.rouesvm.extralent.block.entity.BasicMachineBlockEntity;
import com.rouesvm.extralent.item.DoubleTexturedItem;
import com.rouesvm.extralent.item.custom.connector.ConnectorItem;
import com.rouesvm.extralent.item.custom.data.Activated;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import static com.rouesvm.extralent.Extralent.HIGHLIGHT_MANAGER;

public class InfoItem extends DoubleTexturedItem {
    public InfoItem(Settings settings) {
        super("viewer", settings, Items.COAL);
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerWorld world, Entity entity, @Nullable EquipmentSlot slot) {
        if (!world.isClient() && entity instanceof PlayerEntity player) {
            if (!player.isHolding(stack.getItem())) return;

            if (!InfoLogic.canTick(stack, entity)) return;

            if (InfoData.getDisplay(stack) == InfoData.DISPLAY.UI) {
                InfoLogic.showUIMessage(world, player, stack);
            }
        }
    }

    @Override
    public ActionResult use(World world, PlayerEntity player, Hand hand) {
        if (!world.isClient()) {
            ItemStack stack = player.getStackInHand(hand);

            if (player.isSneaking()) {
                if (InfoLogic.removeVisuals(stack)) return ActionResult.PASS;

                InfoData.setDisplay(stack, InfoData.getDisplay(stack) == InfoData.DISPLAY.FLOATING
                        ? InfoData.DISPLAY.UI
                        : InfoData.DISPLAY.FLOATING);

                player.sendMessage(Text.translatable("info.viewer.display_changed")
                        .append(" ").append(InfoData.getDisplay(stack).toString()), true);
            } else if (InfoData.getDisplay(stack) == InfoData.DISPLAY.UI) {
                if (!InfoLogic.setContent(stack, (ServerWorld) world)) return ActionResult.PASS;
            }

            player.swingHand(hand, true);
            return ActionResult.SUCCESS_SERVER;
        }

        return ActionResult.PASS;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if (context.getPlayer() != null && !context.getWorld().isClient()) {
            ServerWorld world = (ServerWorld) context.getWorld();

            BlockEntity blockEntity = world.getBlockEntity(context.getBlockPos());
            ItemStack stack = context.getStack();

            if (blockEntity instanceof BasicMachineBlockEntity machine) {
                ConnectorItem.playSoundConnection(context.getPlayer(), 2F);
                Activated.setVisual(stack, true);

                InfoData.setBlockPos(stack, machine.getPos());

                if (InfoData.getDisplay(stack) != InfoData.DISPLAY.FLOATING) {
                    InfoLogic.setContent(stack, world);
                    return ActionResult.PASS;
                }

                if (machine.infoOnClicked() == null) return ActionResult.PASS;
                InfoLogic.updateFloatingText(world, context.getBlockPos(), context.getSide(), machine, stack);

                return ActionResult.SUCCESS;
            } else InfoData.setBlockPos(stack, null);
        }

        return ActionResult.PASS;
    }
}
