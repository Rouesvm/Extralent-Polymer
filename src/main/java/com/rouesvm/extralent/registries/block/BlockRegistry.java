package com.rouesvm.extralent.registries.block;

import com.rouesvm.extralent.Extralent;
import com.rouesvm.extralent.block.BasicPolymerBlock;
import com.rouesvm.extralent.block.generator.GeneratorBlock;
import com.rouesvm.extralent.block.machines.HarvesterBlock;
import com.rouesvm.extralent.block.machines.QuarryBlock;
import com.rouesvm.extralent.block.transport.TransmitterBlock;
import com.rouesvm.extralent.block.transport.TransporterBlock;
import com.rouesvm.extralent.item.PolymerBlockItem;
import net.minecraft.block.AbstractBlock;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class BlockRegistry {
    public static final BasicPolymerBlock QUARRY = registry(new QuarryBlock(AbstractBlock.Settings.create()));
    public static final BasicPolymerBlock GENERATOR = registry(new GeneratorBlock(AbstractBlock.Settings.create()));

    public static final BasicPolymerBlock TRANSPORTER = registry(new TransporterBlock(AbstractBlock.Settings.create()));
    public static final BasicPolymerBlock TRANSMITTER = registry(new TransmitterBlock(AbstractBlock.Settings.create()));

    public static final BasicPolymerBlock HARVESTER = registry(new HarvesterBlock(AbstractBlock.Settings.create()));

    private static <T extends BasicPolymerBlock> T registry(T block) {
        Identifier id = Extralent.of(block.getBlockName());
        Registry.register(Registries.ITEM, id, new PolymerBlockItem(new Item.Settings(), block, id.getPath()));
        return Registry.register(Registries.BLOCK, id, block);
    }

    public static void initialize() {}
}
