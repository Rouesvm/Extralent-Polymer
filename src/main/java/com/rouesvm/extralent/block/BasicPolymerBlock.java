package com.rouesvm.extralent.block;

import com.rouesvm.extralent.Main;
import eu.pb4.polymer.blocks.api.BlockModelType;
import eu.pb4.polymer.blocks.api.PolymerBlockModel;
import eu.pb4.polymer.blocks.api.PolymerBlockResourceUtils;
import eu.pb4.polymer.blocks.api.PolymerTexturedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.Identifier;

public class BasicPolymerBlock extends Block implements PolymerTexturedBlock {
    private final String name;
    private final BlockState polymerBlockState;

    public BasicPolymerBlock(String name, Settings settings) {
        super(settings);
        this.name = name;
        this.polymerBlockState = PolymerBlockResourceUtils.requestBlock(BlockModelType.FULL_BLOCK,
                PolymerBlockModel.of(Main.of("block/" + name)));
    }

    public String getBlockName() {
        return this.name;
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state) {
        return this.polymerBlockState;
    }
}
