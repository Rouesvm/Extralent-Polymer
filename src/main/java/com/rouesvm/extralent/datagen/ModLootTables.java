package com.rouesvm.extralent.datagen;

import com.rouesvm.extralent.registries.block.BlockRegistry;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

public class ModLootTables extends FabricBlockLootTableProvider {
    protected ModLootTables(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        super(dataOutput, registryLookup);
    }

    @Override
    public void generate() {
        this.addDrop(BlockRegistry.ELECTRIC_FURNACE);
        this.addDrop(BlockRegistry.GENERATOR);
        this.addDrop(BlockRegistry.TRANSMITTER);
        this.addDrop(BlockRegistry.TRANSPORTER);
        this.addDrop(BlockRegistry.HARVESTER);
        this.addDrop(BlockRegistry.QUARRY);
    }
}
