package com.rouesvm.extralent.registries.block;

import com.rouesvm.extralent.Extralent;
import com.rouesvm.extralent.block.generator.entity.GeneratorBlockEntity;
import com.rouesvm.extralent.block.quarry.entity.QuarryBlockEntity;
import com.rouesvm.extralent.block.transport.entity.TransmitterBlockEntity;
import com.rouesvm.extralent.block.transport.entity.TransporterBlockEntity;
import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import team.reborn.energy.api.EnergyStorage;

public class BlockEntityRegistry {
    public static final BlockEntityType<GeneratorBlockEntity> GENERATOR_BLOCK_ENTITY = register(
            "generator_block_entity",
            BlockEntityType.Builder.create(GeneratorBlockEntity::new, BlockRegistry.GENERATOR).build());

    public static final BlockEntityType<QuarryBlockEntity> QUARRY_BLOCK_ENTITY = register(
            "quarry_block_entity",
            BlockEntityType.Builder.create(QuarryBlockEntity::new, BlockRegistry.QUARRY).build());

    public static final BlockEntityType<TransporterBlockEntity> TRANSPORTER_BLOCK_ENTITY = register(
            "transporter_block_entity",
            BlockEntityType.Builder.create(TransporterBlockEntity::new, BlockRegistry.TRANSPORTER).build());
    public static final BlockEntityType<TransmitterBlockEntity> TRANSMITTER_BLOCK_ENTITY = register(
            "transmitter_block_entity",
            BlockEntityType.Builder.create(TransmitterBlockEntity::new, BlockRegistry.TRANSMITTER).build());

    static {
        EnergyStorage.SIDED.registerForBlockEntity(QuarryBlockEntity::getEnergyProvider, QUARRY_BLOCK_ENTITY);
        EnergyStorage.SIDED.registerForBlockEntity(GeneratorBlockEntity::getEnergyProvider, GENERATOR_BLOCK_ENTITY);
        EnergyStorage.SIDED.registerForBlockEntity(TransmitterBlockEntity::getEnergyProvider, TRANSMITTER_BLOCK_ENTITY);

        ItemStorage.SIDED.registerForBlockEntity(GeneratorBlockEntity::getInventoryProvider, GENERATOR_BLOCK_ENTITY);
        ItemStorage.SIDED.registerForBlockEntity(TransporterBlockEntity::getInventoryProvider, TRANSPORTER_BLOCK_ENTITY);
    }

    private static <T extends BlockEntity> BlockEntityType<T> register(String name, BlockEntityType<T> blockEntityType) {
        PolymerBlockUtils.registerBlockEntity(blockEntityType);
        return Registry.register(Registries.BLOCK_ENTITY_TYPE, Extralent.of(name), blockEntityType);
    }

    public static void initialize() {}
}
