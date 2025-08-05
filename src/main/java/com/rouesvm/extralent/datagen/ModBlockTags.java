package com.rouesvm.extralent.datagen;

import com.rouesvm.extralent.Extralent;
import com.rouesvm.extralent.registries.block.BlockRegistry;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.block.Block;
import net.minecraft.registry.*;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.Identifier;

import java.util.concurrent.CompletableFuture;

public class ModBlockTags extends FabricTagProvider.BlockTagProvider {
    public ModBlockTags(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registryCompletableFuture) {
        super(output, registryCompletableFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {
        this.getTagBuilder(BlockTags.PICKAXE_MINEABLE)
                .addOptional(Extralent.of(BlockRegistry.ELECTRIC_FURNACE.getBlockName()))
                .addOptional(Extralent.of(BlockRegistry.GENERATOR.getBlockName()))
                .addOptional(Extralent.of(BlockRegistry.TRANSMITTER.getBlockName()))
                .addOptional(Extralent.of(BlockRegistry.HARVESTER.getBlockName()))
                .addOptional(Extralent.of(BlockRegistry.QUARRY.getBlockName()));

        this.getTagBuilder(BlockTags.AXE_MINEABLE)
                .addOptional(Extralent.of(BlockRegistry.TRANSPORTER.getBlockName()));
    }
}
