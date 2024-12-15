package com.rouesvm.extralent.item.custom;

import com.rouesvm.extralent.item.BasicPolymerItem;
import com.rouesvm.extralent.ui.FilterMenu;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class FilterItem extends BasicPolymerItem {
    public FilterItem(Settings settings) {
        super("filter", settings, Items.COAL);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        if (world == null || world.isClient) return TypedActionResult.pass(stack);
        new FilterMenu(stack, (ServerPlayerEntity) player);

        return TypedActionResult.pass(stack);
    }
}
