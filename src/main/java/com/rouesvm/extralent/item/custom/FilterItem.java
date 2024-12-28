package com.rouesvm.extralent.item.custom;

import com.rouesvm.extralent.block.transport.entity.TransporterBlockEntity;
import com.rouesvm.extralent.item.BasicPolymerItem;
import com.rouesvm.extralent.visual.ui.FilterMenu;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class FilterItem extends BasicPolymerItem {
    public FilterItem(Settings settings) {
        super("filter", settings, Items.COAL);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if (context.getWorld() != null && !context.getWorld().isClient) {
            ServerWorld world = (ServerWorld) context.getWorld();
            var blockEntityResult = world.getBlockEntity(context.getBlockPos());

            ItemStack stack = context.getStack();
            if (blockEntityResult instanceof TransporterBlockEntity transporterBlockEntity) {
                ContainerComponent component = stack.getOrDefault(DataComponentTypes.CONTAINER, ContainerComponent.DEFAULT);
                transporterBlockEntity.setItemList(component.stream().toList());
                stack.copyAndEmpty();
                return ActionResult.SUCCESS;
            }
        }

        return ActionResult.PASS;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        if (world == null || world.isClient) return TypedActionResult.pass(stack);
        new FilterMenu(stack, (ServerPlayerEntity) player);

        return TypedActionResult.pass(stack);
    }
}
