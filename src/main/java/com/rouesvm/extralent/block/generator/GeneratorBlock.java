package com.rouesvm.extralent.block.generator;

import com.rouesvm.extralent.block.MachineBlock;
import com.rouesvm.extralent.registries.block.BlockEntityRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class GeneratorBlock extends MachineBlock {
    public GeneratorBlock(Settings settings) {
        super("generator", settings, true);
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return BlockEntityRegistry.GENERATOR_BLOCK_ENTITY.instantiate(pos, state);
    }
}
