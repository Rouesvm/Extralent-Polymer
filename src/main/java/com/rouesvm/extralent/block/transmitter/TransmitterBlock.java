package com.rouesvm.extralent.block.transmitter;

import com.rouesvm.extralent.block.BasicPolymerBlock;
import com.rouesvm.extralent.block.transmitter.entity.TransmitterBlockEntity;
import com.rouesvm.extralent.interfaces.block.TickableBlockEntity;
import com.rouesvm.extralent.registries.block.BlockEntityRegistry;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class TransmitterBlock extends BasicPolymerBlock implements BlockEntityProvider {
    public TransmitterBlock(Settings settings) {
        super("transmitter", settings);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world.isClient) return ActionResult.PASS;
        if (player == null) return ActionResult.PASS;

        var blockEntityResult = world.getBlockEntity(pos, BlockEntityRegistry.TRANSMITTER_BLOCK_ENTITY);
        if (blockEntityResult.isPresent()) {
            if (!player.isSneaking()) return ActionResult.PASS;

            TransmitterBlockEntity blockEntity = blockEntityResult.get();
            player.sendMessage(Text.of("Capacity: " + blockEntity.getEnergyStorage().amount), true);
        }

        return ActionResult.SUCCESS_NO_ITEM_USED;
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return BlockEntityRegistry.TRANSMITTER_BLOCK_ENTITY.instantiate(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return TickableBlockEntity.getTicker(world);
    }
}
