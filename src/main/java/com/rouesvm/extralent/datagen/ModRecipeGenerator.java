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
        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, BlockRegistry.ELECTRIC_FURNACE, 1)
                .pattern("rir").pattern("ifi").pattern("ccc")
                .input('r', Items.REDSTONE).input('i', Items.COPPER_INGOT)
                .input('f', BlockRegistry.GENERATOR).input('c', Items.COPPER_BLOCK)
                .criterion("get_redstone", InventoryChangedCriterion.Conditions.items(Items.REDSTONE))
                .offerTo(exporter);

        ShapedRecipeJsonBuilder.create(RecipeCategory.REDSTONE, BlockRegistry.GENERATOR, 1)
                .pattern("rir").pattern("ifi").pattern("ccc")
                .input('r', Items.REDSTONE).input('i', Items.IRON_INGOT)
                .input('f', ItemRegistry.MACHINE_BASE).input('c', Items.STONE)
                .criterion("get_redstone", InventoryChangedCriterion.Conditions.items(Items.REDSTONE))
                .offerTo(exporter);

        ShapedRecipeJsonBuilder.create(RecipeCategory.FOOD, BlockRegistry.HARVESTER, 1)
                .pattern("rir").pattern("aea").pattern("rir")
                .input('r', Items.COPPER_INGOT).input('e', BlockRegistry.TRANSPORTER)
                .input('i', Items.REDSTONE).input('a', Items.DIAMOND_AXE)
                .criterion("get_generator", InventoryChangedCriterion.Conditions.items(BlockRegistry.GENERATOR))
                .offerTo(exporter);

        ShapedRecipeJsonBuilder.create(RecipeCategory.TRANSPORTATION, BlockRegistry.TRANSMITTER, 4)
                .pattern("rir").pattern("gbg").pattern("rir")
                .input('r', ItemRegistry.ANTENNA).input('g', Items.GOLD_BLOCK)
                .input('i', Items.COPPER_INGOT).input('b', ItemRegistry.MACHINE_BASE)
                .criterion("get_redstone", InventoryChangedCriterion.Conditions.items(Items.REDSTONE))
                .offerTo(exporter);

        ShapedRecipeJsonBuilder.create(RecipeCategory.TRANSPORTATION, BlockRegistry.TRANSPORTER, 6)
                .pattern("rer").pattern("ibi").pattern("rer")
                .input('r', Items.COPPER_INGOT).input('e', Items.ENDER_PEARL)
                .input('i', ItemTags.PLANKS).input('b', ItemRegistry.MACHINE_BASE)
                .criterion("get_pearl", InventoryChangedCriterion.Conditions.items(Items.ENDER_PEARL))
                .offerTo(exporter);
    }

    private void itemRecipes(RecipeExporter exporter) {
        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ItemRegistry.COPPER_ROD, 6)
                .pattern("i  ").pattern(" i ").pattern("  i")
                .input('i', Items.COPPER_INGOT)
                .criterion("get_copper", InventoryChangedCriterion.Conditions.items(Items.COPPER_INGOT))
                .offerTo(exporter);

        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ItemRegistry.ANTENNA, 1)
                .pattern(" r ").pattern(" i ").pattern(" i ")
                .input('r', Items.REDSTONE).input('i', ItemRegistry.COPPER_ROD)
                .criterion("get_copper_rod", InventoryChangedCriterion.Conditions.items(ItemRegistry.COPPER_ROD))
                .offerTo(exporter);

        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ItemRegistry.CONNECTOR, 1)
                .pattern("i i").pattern("rbr").pattern("rbr")
                .input('b', Items.IRON_INGOT).input('i', ItemRegistry.ANTENNA)
                .input('r', Items.REDSTONE)
                .criterion("get_transmitter", InventoryChangedCriterion.Conditions.items(BlockRegistry.TRANSMITTER))
                .offerTo(exporter);

        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ItemRegistry.VACUUM, 1)
                .pattern("i  ").pattern("cii").pattern("i i")
                .input('c', BlockRegistry.TRANSPORTER).input('i', Items.IRON_INGOT)
                .criterion("get_transporter", InventoryChangedCriterion.Conditions.items(BlockRegistry.TRANSPORTER))
                .offerTo(exporter);

        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ItemRegistry.INFO, 1)
                .pattern("   ").pattern("ici").pattern("   ")
                .input('c', ItemRegistry.CONNECTOR).input('i', Items.REDSTONE)
                .criterion("get_pearl", InventoryChangedCriterion.Conditions.items(Items.ENDER_PEARL))
                .offerTo(exporter);

        ShapedRecipeJsonBuilder.create(RecipeCategory.REDSTONE, ItemRegistry.MACHINE_BASE, 1)
                .pattern("iri").pattern("rcr").pattern("iri")
                .input('r', Items.REDSTONE).input('i', Items.IRON_INGOT)
                .input('c', Items.COPPER_BLOCK)
                .criterion("get_redstone", InventoryChangedCriterion.Conditions.items(Items.REDSTONE))
                .offerTo(exporter);
    }

    @Override
    public String getName() {
        return "recipes";
    }
}
