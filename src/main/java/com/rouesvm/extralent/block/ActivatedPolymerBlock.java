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
import xyz.nucleoid.packettweaker.PacketContext;

public class ActivatedPolymerBlock extends BasicPolymerBlock  {
    private final BlockState activatedState;
    public static final BooleanProperty ACTIVATED = BooleanProperty.of("activated");

    boolean hasCustomStates;

    public ActivatedPolymerBlock(String name, AbstractBlock.Settings settings, boolean hasCustomStates) {
        super(name, settings);

        this.hasCustomStates = hasCustomStates;
        if (hasCustomStates) {
            setDefaultState(this.getStateManager().getDefaultState().with(ACTIVATED, false));
            this.activatedState = PolymerBlockResourceUtils.requestBlock(BlockModelType.FULL_BLOCK,
                    PolymerBlockModel.of(Extralent.of("block/" + name + "_on")));
        } else this.activatedState = null;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(ACTIVATED);
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state, PacketContext packetContext) {
        if (state.get(ACTIVATED) && activatedState != null)
            return activatedState;
        return super.getPolymerBlockState(state, packetContext);
    }
}
