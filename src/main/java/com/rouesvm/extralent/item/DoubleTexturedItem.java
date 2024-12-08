package com.rouesvm.extralent.item;

import com.rouesvm.extralent.Extralent;
import com.rouesvm.extralent.item.custom.data.Activated;
import eu.pb4.polymer.resourcepack.api.PolymerModelData;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class DoubleTexturedItem extends BasicPolymerItem {
    private final PolymerModelData secondModel;

    public DoubleTexturedItem(String name, Settings settings, Item vanillaItem) {
        super(name, settings, vanillaItem);
        this.secondModel = PolymerResourcePackUtils.requestModel(vanillaItem,
                Identifier.of(Extralent.MOD_ID, "item/" + name + "_on"));
    }

    @Override
    public int getPolymerCustomModelData(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        if (Activated.showVisual(itemStack)) return secondModel.value();
        return super.getPolymerCustomModelData(itemStack, player);
    }

    public void setTexture(ItemStack stack, boolean activated) {
        Activated.setVisual(stack, activated);
    }
}
