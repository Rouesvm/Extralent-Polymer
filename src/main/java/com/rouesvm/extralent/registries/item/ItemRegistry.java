package com.rouesvm.extralent.registries.item;

import com.rouesvm.extralent.Extralent;
import com.rouesvm.extralent.item.BasicPolymerItem;
import com.rouesvm.extralent.item.custom.ConnectorItem;
import com.rouesvm.extralent.item.custom.InfoItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import net.minecraft.item.Item.Settings;

public class ItemRegistry {
    public static final ConnectorItem CONNECTOR = register(new ConnectorItem(new Settings()));
    public static final InfoItem INFO = register(new InfoItem(new Settings()));

    public static final BasicPolymerItem BASIC_CIRCUIT = register(new BasicPolymerItem("basic_circuit", new Settings().maxCount(1), Items.POISONOUS_POTATO));

    private static <T extends BasicPolymerItem> T register(T item) {
        Identifier id = Extralent.of(item.getItemName());
        return Registry.register(Registries.ITEM, id, item);
    }

    public static void initialize() {}
}
