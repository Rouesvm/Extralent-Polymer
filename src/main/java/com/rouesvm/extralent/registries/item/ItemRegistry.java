package com.rouesvm.extralent.registries.item;

import com.rouesvm.extralent.Main;
import com.rouesvm.extralent.item.BasicPolymerItem;
import com.rouesvm.extralent.item.custom.ConnectorItem;
import com.rouesvm.extralent.item.custom.InfoItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ItemRegistry {
    public static final ConnectorItem CONNECTOR = register(new ConnectorItem(new Item.Settings()));
    public static final InfoItem INFO = register(new InfoItem(new Item.Settings()));

    private static <T extends BasicPolymerItem> T register(T item) {
        Identifier id = Main.of(item.getItemName());
        return Registry.register(Registries.ITEM, id, item);
    }

    public static void initialize() {}
}
