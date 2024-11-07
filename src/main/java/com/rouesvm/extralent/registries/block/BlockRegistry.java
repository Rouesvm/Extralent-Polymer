package com.rouesvm.extralent.registries.block;

import com.rouesvm.extralent.Main;
import com.rouesvm.extralent.block.BasicPolymerBlock;
import com.rouesvm.extralent.block.generator.GeneratorBlock;
import com.rouesvm.extralent.block.quary.QuaryBlock;
import com.rouesvm.extralent.block.transport.TransmitterBlock;
import com.rouesvm.extralent.item.PolymerBlockItem;
import net.minecraft.block.AbstractBlock;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class BlockRegistry {
    public static final BasicPolymerBlock QUARY = registry(new QuaryBlock(AbstractBlock.Settings.create()));
    public static final BasicPolymerBlock GENERATOR = registry(new GeneratorBlock(AbstractBlock.Settings.create()));
    public static final BasicPolymerBlock TRANSMITTER = registry(new TransmitterBlock(AbstractBlock.Settings.create()));

    private static <T extends BasicPolymerBlock> T registry(T block) {
        Identifier id = Main.of(block.getBlockName());
        Registry.register(Registries.ITEM, id, new PolymerBlockItem(new Item.Settings(), block, id.getPath()));
        return Registry.register(Registries.BLOCK, id, block);
    }

    public static void initialize() {}
}
