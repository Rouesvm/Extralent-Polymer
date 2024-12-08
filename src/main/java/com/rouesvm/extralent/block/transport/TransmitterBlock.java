package com.rouesvm.extralent.block.transport;

import com.rouesvm.extralent.block.MachineBlock;
import com.rouesvm.extralent.registries.block.BlockEntityRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class TransmitterBlock extends MachineBlock {
    public TransmitterBlock(Settings settings) {
        super("transmitter", settings, false);
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return BlockEntityRegistry.TRANSMITTER_BLOCK_ENTITY.instantiate(pos, state);
    }
}
