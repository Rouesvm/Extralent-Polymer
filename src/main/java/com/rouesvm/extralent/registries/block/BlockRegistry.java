package com.rouesvm.extralent.registries.block;

import com.rouesvm.extralent.Extralent;
import com.rouesvm.extralent.block.BasicPolymerBlock;
import com.rouesvm.extralent.block.MachineBlock;
import com.rouesvm.extralent.block.machine.ElectricFurnaceBlock;
import com.rouesvm.extralent.block.generator.GeneratorBlock;
import com.rouesvm.extralent.block.machine.HarvesterBlock;
import com.rouesvm.extralent.block.machine.QuarryBlock;
import com.rouesvm.extralent.block.transport.TransmitterBlock;
import com.rouesvm.extralent.block.transport.TransporterBlock;
import com.rouesvm.extralent.item.PolymerBlockItem;
import net.minecraft.block.AbstractBlock;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class BlockRegistry {
    public static final MachineBlock QUARRY = registry(new QuarryBlock(AbstractBlock.Settings.create().hardness(2f)));
    public static final MachineBlock GENERATOR = registry(new GeneratorBlock(AbstractBlock.Settings.create().hardness(2f)));

    public static final MachineBlock TRANSPORTER = registry(new TransporterBlock(AbstractBlock.Settings.create().hardness(1.5f)));
    public static final MachineBlock TRANSMITTER = registry(new TransmitterBlock(AbstractBlock.Settings.create().hardness(1.5f)));

    public static final MachineBlock HARVESTER = registry(new HarvesterBlock(AbstractBlock.Settings.create().hardness(2f)));
    public static final MachineBlock ELECTRIC_FURNACE = registry(new ElectricFurnaceBlock(AbstractBlock.Settings.create().hardness(2f)));

    private static <T extends BasicPolymerBlock> T registry(T block) {
        Identifier id = Extralent.of(block.getBlockName());
        Registry.register(Registries.ITEM, id, new PolymerBlockItem(new Item.Settings(), block, id.getPath()));
        return Registry.register(Registries.BLOCK, id, block);
    }

    public static void initialize() {}
}
