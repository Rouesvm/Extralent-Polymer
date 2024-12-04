package com.rouesvm.extralent.block;

import com.rouesvm.extralent.Extralent;
import eu.pb4.polymer.blocks.api.BlockModelType;
import eu.pb4.polymer.blocks.api.PolymerBlockModel;
import eu.pb4.polymer.blocks.api.PolymerBlockResourceUtils;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;

public class ActivatedPolymerBlock extends BasicPolymerBlock {
    private final BlockState activatedState;

    public static final BooleanProperty ACTIVATED = BooleanProperty.of("activated");

    public ActivatedPolymerBlock(String name, AbstractBlock.Settings settings) {
        super(name, settings);
        setDefaultState(getDefaultState().with(ACTIVATED, false));

        this.activatedState = PolymerBlockResourceUtils.requestBlock(BlockModelType.FULL_BLOCK,
                PolymerBlockModel.of(Extralent.of("block/" + name + "_on")));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(ACTIVATED);
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state) {
        if (state.get(ACTIVATED))
            return this.activatedState;
        return super.getPolymerBlockState(state);
    }
}
