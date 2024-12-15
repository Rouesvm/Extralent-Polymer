package com.rouesvm.extralent.block.machine;

import com.rouesvm.extralent.block.MachineBlock;
import com.rouesvm.extralent.registries.block.BlockEntityRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class QuarryBlock extends MachineBlock {
    public QuarryBlock(Settings settings) {
        super("quarry", settings, true);
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return BlockEntityRegistry.QUARRY_BLOCK_ENTITY.instantiate(pos, state);
    }
}
