package com.rouesvm.extralent.item.connector;

import com.rouesvm.extralent.block.transmitter.entity.TransmitterBlockEntity;
import com.rouesvm.extralent.item.BasicPolymerItem;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.List;

public class ConnectorItem extends BasicPolymerItem {
    private TransmitterBlockEntity currentBlockEntity;

    public ConnectorItem(Settings settings) {
        super("connector", settings, Items.COAL);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        if (this.currentBlockEntity != null) {
            tooltip.add(Text.literal("Connected to one transmitter"));
            tooltip.add(Text.literal("Now able to bind blocks"));
        } else {
            tooltip.add(Text.literal("Not connected"));
        }
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        if (!world.isClient) {
            if (player.isSneaking()) {
                this.currentBlockEntity = null;
                stack.set(DataComponentTypes.CONTAINER, null);
                return TypedActionResult.success(stack, true);
            }
        }
        return TypedActionResult.pass(stack);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if (!context.getWorld().isClient && context.getPlayer() != null) {
            ServerWorld world = (ServerWorld) context.getWorld();

            var blockEntityResult = world.getBlockEntity(context.getBlockPos());

            System.out.print("COoooOOOoRRECT!");

            if (blockEntityResult instanceof TransmitterBlockEntity transmitterBlockEntity) {
                this.currentBlockEntity = transmitterBlockEntity;
                context.getStack().set(DataComponentTypes.CONTAINER, ContainerComponent.DEFAULT);

                return ActionResult.SUCCESS;
            } else if (blockEntityResult != null) {
                System.out.print("This one!");
                if (this.currentBlockEntity != null) {
                    this.currentBlockEntity.putBlock(context.getBlockPos());
                    return ActionResult.SUCCESS;
                }
            }
        }
        return ActionResult.PASS;
    }
}
