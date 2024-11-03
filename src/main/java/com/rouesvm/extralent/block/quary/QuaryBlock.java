package com.rouesvm.extralent.block.quary;

import com.rouesvm.extralent.block.BasicPolymerBlock;
import com.rouesvm.extralent.block.quary.entity.QuaryBlockEntity;
import com.rouesvm.extralent.interfaces.block.TickableBlockEntity;
import com.rouesvm.extralent.registries.block.BlockEntityRegistry;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class QuaryBlock extends BasicPolymerBlock implements BlockEntityProvider {
    public QuaryBlock(Settings settings) {
        super("quary", settings);
    }

    private Text showMessage(QuaryBlockEntity blockEntity) {
        return Text.of(blockEntity.getProgress() + "");
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return BlockEntityRegistry.QUARY_BLOCK_ENTITY.instantiate(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return TickableBlockEntity.getTicker(world);
    }
}
