package com.rouesvm.extralent.block.transport;

import com.rouesvm.extralent.block.MachineBlock;
import com.rouesvm.extralent.block.transport.entity.PipeBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PipeBlock extends MachineBlock {
    public PipeBlock(String name, Settings settings, boolean hasCustomStates) {
        super(name, settings, hasCustomStates);
    }

    @Override
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        PipeBlockEntity blockEntity = (PipeBlockEntity) world.getBlockEntity(pos);
        if (blockEntity != null) blockEntity.removeOtherConnections();
        return super.onBreak(world, pos, state, player);
    }
}
