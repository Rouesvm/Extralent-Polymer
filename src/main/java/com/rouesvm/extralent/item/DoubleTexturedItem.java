package com.rouesvm.extralent.item;

import com.rouesvm.extralent.Extralent;
import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.core.api.utils.PolymerKeepModel;
import eu.pb4.polymer.resourcepack.api.PolymerModelData;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class DoubleTexturedItem extends BasicPolymerItem {
    private final PolymerModelData secondModel;
    private boolean activated;

    public DoubleTexturedItem(String name, Settings settings, Item vanillaItem) {
        super(name, settings, vanillaItem);
        this.activated = false;
        this.secondModel = PolymerResourcePackUtils.requestModel(vanillaItem,
                Identifier.of(Extralent.MOD_ID, "item/" + name + "_on"));
    }

    @Override
    public int getPolymerCustomModelData(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        if (this.activated) return this.secondModel.value();
        return super.getPolymerCustomModelData(itemStack, player);
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }
}
