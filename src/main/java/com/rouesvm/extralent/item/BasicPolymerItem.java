package com.rouesvm.extralent.item;

import com.rouesvm.extralent.Extralent;
import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.core.api.utils.PolymerKeepModel;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

public class BasicPolymerItem extends Item implements PolymerItem, PolymerKeepModel {
    private final Identifier id;
    private final Item vanillaModel;

    public BasicPolymerItem(String name, Settings settings, Item vanillaItem) {
        super(settings.registryKey(RegistryKey.of(RegistryKeys.ITEM, Extralent.of(name))));
        this.id = Extralent.of(name);
        this.vanillaModel = vanillaItem;
    }

    public Identifier getId() {
        return id;
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, PacketContext packetContext) {
        return this.vanillaModel;
    }

    @Override
    public @Nullable Identifier getPolymerItemModel(ItemStack stack, PacketContext context) {
        return this.id;
    }
}
