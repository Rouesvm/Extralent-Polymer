package com.rouesvm.extralent.datagen;

import com.rouesvm.extralent.registries.block.BlockRegistry;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.Identifier;

import java.util.concurrent.CompletableFuture;

public class ModBlockTags extends FabricTagProvider.BlockTagProvider {
    public ModBlockTags(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {
        this.getTagBuilder(BlockTags.PICKAXE_MINEABLE)
                .add(Identifier.tryParse(BlockRegistry.ELECTRIC_FURNACE.toString()))
                .add(Identifier.tryParse(BlockRegistry.GENERATOR.toString()))
                .add(Identifier.tryParse(BlockRegistry.TRANSMITTER.toString()))
                .add(Identifier.tryParse(BlockRegistry.HARVESTER.toString()))
                .add(Identifier.tryParse(BlockRegistry.QUARRY.toString()));

        this.getTagBuilder(BlockTags.AXE_MINEABLE)
                .add(Identifier.tryParse(BlockRegistry.TRANSPORTER.toString()));
    }
}
