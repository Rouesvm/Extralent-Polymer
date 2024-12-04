package com.rouesvm.extralent.datagen;

import com.rouesvm.extralent.registries.block.BlockRegistry;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.advancement.criterion.InventoryChangedCriterion;
import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.item.Items;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

public class ModRecipeGenerator extends FabricRecipeProvider {
    public ModRecipeGenerator(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    public void generate(RecipeExporter exporter) {
        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, BlockRegistry.GENERATOR, 1)
                .pattern("rir").pattern("ifi").pattern("ccc")
                .input('r', Items.REDSTONE).input('i', Items.IRON_INGOT)
                .input('f', Items.FURNACE).input('c', Items.COPPER_BLOCK)
                .criterion("get_redstone", InventoryChangedCriterion.Conditions.items(Items.REDSTONE))
                .offerTo(exporter);
    }

    @Override
    public String getName() {
        return "recipes";
    }
}
