package com.rouesvm.extralent.block.transport.entity;

import com.rouesvm.extralent.registries.block.BlockEntityRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public class TeleporterBlockEntity extends PipeBlockEntity {
    public TeleporterBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.TELEPORTER_BLOCK_ENTITY, pos, state);
    }

    @Override
    public int getMaxDist() {
        return 0;
    }
}
