package com.rouesvm.extralent.datagen;

import com.rouesvm.extralent.registries.block.BlockRegistry;
import com.rouesvm.extralent.registries.item.ItemRegistry;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.advancement.criterion.InventoryChangedCriterion;
import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.item.Items;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.TagKey;

import java.util.concurrent.CompletableFuture;

public class ModRecipeGenerator extends FabricRecipeProvider {
    public ModRecipeGenerator(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    public void generate(RecipeExporter exporter) {
        blockRecipes(exporter);
        itemRecipes(exporter);
    }

    private void blockRecipes(RecipeExporter exporter) {
        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, BlockRegistry.GENERATOR, 1)
                .pattern("rir").pattern("ifi").pattern("ccc")
                .input('r', Items.REDSTONE).input('i', Items.IRON_INGOT)
                .input('f', Items.FURNACE).input('c', Items.COPPER_BLOCK)
                .criterion("get_redstone", InventoryChangedCriterion.Conditions.items(Items.REDSTONE))
                .offerTo(exporter);

        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, BlockRegistry.HARVESTER, 1)
                .pattern("rir").pattern("iei").pattern("rir")
                .input('r', Items.COPPER_INGOT).input('e', BlockRegistry.TRANSPORTER)
                .input('i', Items.REDSTONE)
                .criterion("get_generator", InventoryChangedCriterion.Conditions.items(BlockRegistry.GENERATOR))
                .offerTo(exporter);

        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, BlockRegistry.TRANSMITTER, 4)
                .pattern("rir").pattern("grg").pattern("rir")
                .input('r', Items.REDSTONE).input('g', Items.GOLD_BLOCK)
                .input('i', Items.COPPER_INGOT)
                .criterion("get_redstone", InventoryChangedCriterion.Conditions.items(Items.REDSTONE))
                .offerTo(exporter);

        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, BlockRegistry.TRANSPORTER, 6)
                .pattern("rir").pattern("iei").pattern("rir")
                .input('r', Items.COPPER_INGOT).input('e', Items.ENDER_PEARL)
                .input('i', ItemTags.PLANKS)
                .criterion("get_pearl", InventoryChangedCriterion.Conditions.items(Items.ENDER_PEARL))
                .offerTo(exporter);
    }

    private void itemRecipes(RecipeExporter exporter) {
        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ItemRegistry.CONNECTOR, 1)
                .pattern(" i ").pattern("bbb").pattern("bcb")
                .input('c', BlockRegistry.TRANSMITTER).input('b', Items.COPPER_INGOT)
                .input('i', Items.LIGHTNING_ROD)
                .criterion("get_transmitter", InventoryChangedCriterion.Conditions.items(BlockRegistry.TRANSMITTER))
                .offerTo(exporter);

        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ItemRegistry.VACUUM, 1)
                .pattern("i  ").pattern("cii").pattern("i  ")
                .input('c', BlockRegistry.TRANSPORTER).input('i', Items.IRON_INGOT)
                .criterion("get_transporter", InventoryChangedCriterion.Conditions.items(BlockRegistry.TRANSPORTER))
                .offerTo(exporter);

        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ItemRegistry.INFO, 1)
                .pattern(" i ").pattern("ici").pattern(" i ")
                .input('c', ItemRegistry.CONNECTOR).input('i', Items.IRON_INGOT)
                .criterion("get_pearl", InventoryChangedCriterion.Conditions.items(Items.ENDER_PEARL))
                .offerTo(exporter);
    }

    @Override
    public String getName() {
        return "recipes";
    }
}
