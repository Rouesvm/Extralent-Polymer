package com.rouesvm.extralent.item;

import com.rouesvm.extralent.Extralent;
import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.core.api.item.SimplePolymerItem;
import eu.pb4.polymer.core.api.utils.PolymerKeepModel;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

public class BasicPolymerItem extends Item implements PolymerItem, PolymerKeepModel {
    private final String name;
    private final Item vanillaItem;

    public BasicPolymerItem(String name, Settings settings, Item vanillaItem) {
        super(settings.registryKey(RegistryKey.of(RegistryKeys.ITEM, Extralent.of(name))));
        this.name = name;
        this.vanillaItem = vanillaItem;
    }

    public String getItemName() {
        return this.name;
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, PacketContext context) {
        return this.vanillaItem;
    }
}
