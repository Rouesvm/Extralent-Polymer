package com.rouesvm.extralent.item;

import com.rouesvm.extralent.Main;
import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.core.api.utils.PolymerKeepModel;
import eu.pb4.polymer.resourcepack.api.PolymerModelData;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

public class PolymerBlockItem extends BlockItem implements PolymerItem, PolymerKeepModel {
    private final PolymerModelData polymerModel;

    public PolymerBlockItem(Settings settings, Block block, String name) {
        super(block, settings);
        this.polymerModel = PolymerResourcePackUtils.requestModel(Items.POISONOUS_POTATO,
                Main.of("item/" + name));
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return this.polymerModel.item();
    }

    @Override
    public int getPolymerCustomModelData(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return this.polymerModel.value();
    }
}
