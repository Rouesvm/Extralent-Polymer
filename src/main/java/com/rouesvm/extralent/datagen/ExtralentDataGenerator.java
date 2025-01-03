package com.rouesvm.extralent.datagen;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class ExtralentDataGenerator implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator generator) {
        FabricDataGenerator.Pack pack = generator.createPack();
        pack.addProvider(ModRecipeProvider::new);
        pack.addProvider(ModBlockTags::new);
        pack.addProvider(ModLootTables::new);
        pack.addProvider(ModAdvancementProvider::new);
    }
}
