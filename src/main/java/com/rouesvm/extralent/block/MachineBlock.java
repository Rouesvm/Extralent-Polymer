package com.rouesvm.extralent.block;

import com.rouesvm.extralent.block.entity.BasicMachineBlockEntity;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public class MachineBlock extends ActivatedPolymerBlock implements BlockEntityProvider {
    public MachineBlock(String name, Settings settings, boolean hasCustomStates) {
        super(name, settings, hasCustomStates);
    }

    @Override
    protected void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity tileEntity = world.getBlockEntity(pos);
            if (tileEntity instanceof BasicMachineBlockEntity basicMachineBlock) {
                if (basicMachineBlock.getInventory() == null) return;
                ItemScatterer.spawn(world, pos, basicMachineBlock.getInventory());
            }
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }

    @ApiStatus.OverrideOnly
    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return null;
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return TickableBlockEntity.getTicker(world);
    }
}
