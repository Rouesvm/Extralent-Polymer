package com.rouesvm.extralent.registries.item;

import com.rouesvm.extralent.Extralent;
import com.rouesvm.extralent.item.BasicPolymerItem;
import com.rouesvm.extralent.item.custom.ConnectorItem;
import com.rouesvm.extralent.item.custom.InfoItem;
import com.rouesvm.extralent.item.custom.VacuumItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ItemRegistry {
    public static final ConnectorItem CONNECTOR = register(new ConnectorItem(new Item.Settings().maxCount(1)));
    public static final InfoItem INFO = register(new InfoItem(new Item.Settings().maxCount(1)));

    public static final VacuumItem VACUUM = register(new VacuumItem(new Item.Settings().maxCount(1)));

    private static <T extends BasicPolymerItem> T register(T item) {
        Identifier id = Extralent.of(item.getItemName());
        return Registry.register(Registries.ITEM, id, item);
    }

    public static void initialize() {}
}
