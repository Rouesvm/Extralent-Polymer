package com.rouesvm.extralent.registries.item;

import com.rouesvm.extralent.Main;
import com.rouesvm.extralent.item.BasicPolymerItem;
import com.rouesvm.extralent.item.connector.ConnectorItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ItemRegistry {
    public static final ConnectorItem CONNECTOR_ITEM = register(new ConnectorItem(new Item.Settings()));

    private static <T extends BasicPolymerItem> T register(T item) {
        Identifier id = Main.of(item.getItemName());
        return Registry.register(Registries.ITEM, id, item);
    }

    public static void initialize() {}
}
