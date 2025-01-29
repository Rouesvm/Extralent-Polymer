package com.rouesvm.extralent.registries.item;

import com.rouesvm.extralent.Extralent;
import com.rouesvm.extralent.item.BasicPolymerItem;
import com.rouesvm.extralent.item.custom.ConnectorItem;
import com.rouesvm.extralent.item.custom.FilterItem;
import com.rouesvm.extralent.item.custom.InfoItem;
import com.rouesvm.extralent.item.custom.VacuumItem;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ItemRegistry {
    public static final ConnectorItem CONNECTOR = register(new ConnectorItem(new Item.Settings().maxCount(1)));
    public static final InfoItem INFO = register(new InfoItem(new Item.Settings().maxCount(1)));

    public static final VacuumItem VACUUM = register(new VacuumItem(new Item.Settings().maxCount(1)));
    public static final FilterItem FILTER = register(new FilterItem(new Item.Settings().maxCount(1)));

    // Materials
    public static final BasicPolymerItem MACHINE_BASE = register(new BasicPolymerItem("machine_base", new Item.Settings(), Items.POPPED_CHORUS_FRUIT));

    public static final BasicPolymerItem COPPER_ROD = register(new BasicPolymerItem("copper_rod", new Item.Settings(), Items.CHORUS_FRUIT));
    public static final BasicPolymerItem ANTENNA = register(new BasicPolymerItem("antenna", new Item.Settings().maxCount(16), Items.CHORUS_FRUIT));

    public static final BasicPolymerItem JAR = register(new BasicPolymerItem("jar", new Item.Settings(), Items.CHORUS_FRUIT));

    // Foods
    public static final BasicPolymerItem APPLE_JAM = register(new BasicPolymerItem("apple_jam", new Item.Settings()
            .maxCount(16).recipeRemainder(JAR)
            .food(new FoodComponent.Builder().nutrition(6).saturationModifier(0.5F).usingConvertsTo(JAR).build())
            , Items.POPPED_CHORUS_FRUIT)
    );

    public static final BasicPolymerItem APPLE_JAM_SANDWICH = register(new BasicPolymerItem("apple_jam_sandwich", new Item.Settings()
            .food(new FoodComponent.Builder().nutrition(9).saturationModifier(0.7F).build())
            , Items.POPPED_CHORUS_FRUIT)
    );

    private static <T extends BasicPolymerItem> T register(T item) {
        Identifier id = Extralent.of(item.getItemName());
        return Registry.register(Registries.ITEM, id, item);
    }

    public static void initialize() {}
}
