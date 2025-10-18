package com.rouesvm.extralent.item.custom;

import com.rouesvm.extralent.item.DoubleTexturedItem;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;

public class DrillItem extends DoubleTexturedItem {
    public DrillItem(Settings settings) {
        super("portable_drill", settings, Items.COAL);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        return super.useOnBlock(context);
    }
}
