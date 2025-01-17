package com.rouesvm.extralent.item;

import com.rouesvm.extralent.Extralent;
import com.rouesvm.extralent.item.custom.data.Activated;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

public class DoubleTexturedItem extends BasicPolymerItem {
    private final Identifier secondId;

    public DoubleTexturedItem(String name, Settings settings, Item vanillaItem) {
        super(name, settings, vanillaItem);
        this.secondId = Extralent.of(name + "_on");
    }

    @Override
    public @Nullable Identifier getPolymerItemModel(ItemStack stack, PacketContext context) {
        if (Activated.showVisual(stack)) return secondId;
        return super.getPolymerItemModel(stack, context);
    }

    public void setTexture(ItemStack stack, boolean activated) {
        Activated.setVisual(stack, activated);
    }
}
