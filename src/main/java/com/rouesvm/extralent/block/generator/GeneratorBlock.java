package com.rouesvm.extralent.block.generator;

import com.rouesvm.extralent.block.MachineBlock;
import com.rouesvm.extralent.block.generator.entity.GeneratorBlockEntity;
import com.rouesvm.extralent.item.custom.data.Activated;
import com.rouesvm.extralent.registries.block.BlockEntityRegistry;
import com.rouesvm.extralent.visual.ui.inventory.ExtralentInventory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorageUtil;
import team.reborn.energy.api.base.SimpleEnergyItem;

import java.util.Optional;

public class GeneratorBlock extends MachineBlock {
    public GeneratorBlock(Settings settings) {
        super("generator", settings, true);
    }

    @Override
    protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (world == null || world.isClient) return ItemActionResult.FAIL;

        Optional<GeneratorBlockEntity> blockEntity = world.getBlockEntity(pos, BlockEntityRegistry.GENERATOR_BLOCK_ENTITY);
        if (blockEntity.isEmpty()) return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        GeneratorBlockEntity generatorBlockEntity = blockEntity.get();
        ExtralentInventory inventory = generatorBlockEntity.getInventory();
        if (inventory == null) return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        ItemStack existingStack = inventory.getStack(GeneratorBlockEntity.CHARGING_SLOT_INDEX);

        if (existingStack.isEmpty()
                && EnergyStorageUtil.isEnergyStorage(stack)
                && stack.getItem() instanceof SimpleEnergyItem
                || (ItemStack.areItemsEqual(stack, existingStack)
                && existingStack.getCount() < existingStack.getMaxCount())
        ) {
            if (Activated.showVisual(stack)) return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

            int transferableAmount = Math.min(stack.getCount(), existingStack.getMaxCount() - existingStack.getCount());
            ItemStack toInsert = stack.split(transferableAmount);

            inventory.setStack(GeneratorBlockEntity.CHARGING_SLOT_INDEX, toInsert);
            generatorBlockEntity.markDirty();

            if (stack.isEmpty()) return ItemActionResult.SUCCESS;
        } else if (!existingStack.isEmpty()) {
            ItemStack extractedStack = inventory.removeStack(GeneratorBlockEntity.CHARGING_SLOT_INDEX);
            generatorBlockEntity.markDirty();

            if (!player.getInventory().insertStack(extractedStack)) player.dropItem(extractedStack, false);

            return ItemActionResult.SUCCESS;
        }

        return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return BlockEntityRegistry.GENERATOR_BLOCK_ENTITY.instantiate(pos, state);
    }
}
