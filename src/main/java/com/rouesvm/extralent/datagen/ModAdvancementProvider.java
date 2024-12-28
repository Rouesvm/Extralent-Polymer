package com.rouesvm.extralent.datagen;

import com.rouesvm.extralent.registries.block.BlockRegistry;
import com.rouesvm.extralent.registries.item.ItemRegistry;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricAdvancementProvider;
import net.minecraft.advancement.*;
import net.minecraft.advancement.criterion.InventoryChangedCriterion;
import net.minecraft.advancement.criterion.ItemCriterion;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static com.rouesvm.extralent.Extralent.MOD_ID;

public class ModAdvancementProvider extends FabricAdvancementProvider {
    protected ModAdvancementProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        super(output, registryLookup);
    }

    @Override
    public void generateAdvancement(RegistryWrapper.WrapperLookup wrapperLookup, Consumer<AdvancementEntry> consumer) {
        AdvancementEntry rootAdvancement = Advancement.Builder.create()
                .display(
                        Items.REDSTONE,
                        Text.literal("Extralent"),
                        Text.literal("The tech mod."),
                        Identifier.of("textures/gui/advancements/backgrounds/adventure.png"),
                        AdvancementFrame.TASK,
                        true,
                        true,
                        false
                ).criterion("got_redstone", InventoryChangedCriterion.Conditions.items(Items.REDSTONE))
                .build(consumer, MOD_ID + "/root");

        AdvancementEntry transporterAdvancement = Advancement.Builder.create().parent(rootAdvancement)
                .display(
                        BlockRegistry.TRANSPORTER,
                        Text.literal("A WHAT TELEPORTER!?!?"),
                        Text.literal("Place a transporter."),
                        null,
                        AdvancementFrame.TASK,
                        true,
                        true,
                        false
                )
                .criterion("got_transporter", ItemCriterion.Conditions.createPlacedBlock(BlockRegistry.TRANSPORTER))
                .build(consumer, MOD_ID + "/got_transporter");

        AdvancementEntry copperAdvancement = Advancement.Builder.create().parent(rootAdvancement)
                .display(
                        ItemRegistry.COPPER_ROD,
                        Text.literal("A long rod."),
                        Text.literal("Obtain a copper rod."),
                        null,
                        AdvancementFrame.TASK,
                        true,
                        true,
                        false
                )
                .criterion("got_copper_rod", InventoryChangedCriterion.Conditions.items(ItemRegistry.COPPER_ROD))
                .build(consumer, MOD_ID + "/got_copper_rod");

        generatorAdvancementTree(rootAdvancement, consumer);
        transmissionAdvancementTree(copperAdvancement, consumer);
    }

    public void transmissionAdvancementTree(AdvancementEntry rootAdvancement, Consumer<AdvancementEntry> consumer) {
        AdvancementEntry root = Advancement.Builder.create().parent(rootAdvancement)
                .display(
                        BlockRegistry.TRANSMITTER,
                        Text.literal("Wireless Transmission"),
                        Text.literal("Place a transmitter."),
                        null,
                        AdvancementFrame.TASK,
                        true,
                        true,
                        false
                )
                .criterion("got_transmitter", ItemCriterion.Conditions.createPlacedBlock(BlockRegistry.TRANSMITTER))
                .build(consumer, MOD_ID + "/got_transmitter");
    }

    public void generatorAdvancementTree(AdvancementEntry rootAdvancement, Consumer<AdvancementEntry> consumer) {
        AdvancementEntry root = Advancement.Builder.create().parent(rootAdvancement)
                .display(
                        BlockRegistry.GENERATOR,
                        Text.literal("Basic Power"),
                        Text.literal("Place a generator."),
                        null,
                        AdvancementFrame.TASK,
                        true,
                        true,
                        false
                )
                .criterion("got_generator", ItemCriterion.Conditions.createPlacedBlock(BlockRegistry.GENERATOR))
                .build(consumer, MOD_ID + "/got_generator");

        AdvancementEntry connectorAdvancement = Advancement.Builder.create().parent(root)
                .display(
                        ItemRegistry.CONNECTOR,
                        Text.literal("Connecting blocks, one at a time!"),
                        Text.literal("Obtain a Connector."),
                        null,
                        AdvancementFrame.TASK,
                        true,
                        true,
                        false
                )
                .criterion("got_connector", InventoryChangedCriterion.Conditions.items(ItemRegistry.CONNECTOR))
                .build(consumer, MOD_ID + "/got_connector");

        AdvancementEntry viewerAdvancement = Advancement.Builder.create().parent(root)
                .display(
                        ItemRegistry.INFO,
                        Text.literal("What's inside?"),
                        Text.literal("Obtain a Viewer."),
                        null,
                        AdvancementFrame.TASK,
                        true,
                        true,
                        false
                )
                .criterion("got_viewer", InventoryChangedCriterion.Conditions.items(ItemRegistry.INFO))
                .build(consumer, MOD_ID + "/got_viewer");


        AdvancementEntry treeHarvesterAdvancement = Advancement.Builder.create().parent(root)
                .display(
                        BlockRegistry.HARVESTER,
                        Text.literal("Finally... INFINITE WOOD!"),
                        Text.literal("Place a Tree Harvester."),
                        null,
                        AdvancementFrame.TASK,
                        true,
                        true,
                        false
                )
                .criterion("got_harvestor", ItemCriterion.Conditions.createPlacedBlock(BlockRegistry.HARVESTER))
                .build(consumer, MOD_ID + "/got_harvester");
    }
}
