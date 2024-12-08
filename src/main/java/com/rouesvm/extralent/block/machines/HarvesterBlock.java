package com.rouesvm.extralent.block.machines;

import com.rouesvm.extralent.block.MachineBlock;
import com.rouesvm.extralent.registries.block.BlockEntityRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class HarvesterBlock extends MachineBlock {
    public HarvesterBlock(Settings settings) {
        super("tree_harvester", settings, false);
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return BlockEntityRegistry.HARVESTER_BLOCK_ENTITY.instantiate(pos, state);
    }
}
