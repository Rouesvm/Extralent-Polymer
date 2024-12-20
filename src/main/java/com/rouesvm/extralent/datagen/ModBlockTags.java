package com.rouesvm.extralent.datagen;

import com.rouesvm.extralent.registries.block.BlockRegistry;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.BlockTags;

import java.util.concurrent.CompletableFuture;

public class ModBlockTags extends FabricTagProvider.BlockTagProvider {
    public ModBlockTags(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {
        this.getOrCreateTagBuilder(BlockTags.PICKAXE_MINEABLE)
                .add(BlockRegistry.ELECTRIC_FURNACE)
                .add(BlockRegistry.GENERATOR)
                .add(BlockRegistry.TRANSMITTER)
                .add(BlockRegistry.HARVESTER)
                .add(BlockRegistry.QUARRY);

        this.getOrCreateTagBuilder(BlockTags.AXE_MINEABLE)
                .add(BlockRegistry.TRANSPORTER);
    }
}
