package com.rouesvm.extralent.block.transport;

import com.rouesvm.extralent.block.MachineBlock;
import com.rouesvm.extralent.registries.block.BlockEntityRegistry;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class TransporterBlock extends MachineBlock {
    public TransporterBlock(AbstractBlock.Settings settings) {
        super("transporter", settings, false);
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return BlockEntityRegistry.TRANSPORTER_BLOCK_ENTITY.instantiate(pos, state);
    }
}
