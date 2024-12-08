package com.rouesvm.extralent.block.machines;

import com.rouesvm.extralent.block.MachineBlock;
import com.rouesvm.extralent.registries.block.BlockEntityRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class ElectricFurnaceBlock extends MachineBlock {
    public ElectricFurnaceBlock(Settings settings) {
        super("electric_furnace", settings, true);
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return BlockEntityRegistry.ELECTRIC_FURNACE_BLOCK_ENTITY.instantiate(pos, state);
    }
}
