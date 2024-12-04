package com.rouesvm.extralent.block;

import com.rouesvm.extralent.Extralent;
import eu.pb4.polymer.blocks.api.BlockModelType;
import eu.pb4.polymer.blocks.api.PolymerBlockModel;
import eu.pb4.polymer.blocks.api.PolymerBlockResourceUtils;
import eu.pb4.polymer.blocks.api.PolymerTexturedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;

public class BasicPolymerBlock extends Block implements PolymerTexturedBlock {
    private final String name;
    private final BlockState polymerBlockState;

    public BasicPolymerBlock(String name, Settings settings) {
        super(settings);
        this.name = name;
        this.polymerBlockState = PolymerBlockResourceUtils.requestBlock(BlockModelType.FULL_BLOCK,
                PolymerBlockModel.of(Extralent.of("block/" + name)));
    }

    public String getBlockName() {
        return name;
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state) {
        return this.polymerBlockState;
    }
}
