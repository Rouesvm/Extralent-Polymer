package com.rouesvm.extralent.item;

import com.rouesvm.extralent.Extralent;
import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.core.api.utils.PolymerKeepModel;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import xyz.nucleoid.packettweaker.PacketContext;

public class PolymerBlockItem extends BlockItem implements PolymerItem, PolymerKeepModel {
    public PolymerBlockItem(Settings settings, Block block, String name) {
        super(block, settings.registryKey(RegistryKey.of(RegistryKeys.ITEM, Extralent.of(name))));
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, PacketContext context) {
        return Items.POISONOUS_POTATO;
    }
}
