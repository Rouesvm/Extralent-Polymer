package com.rouesvm.extralent.block.quary.entity;

import com.rouesvm.extralent.block.entity.BasicMachineBlockEntity;
import com.rouesvm.extralent.registries.block.BlockEntityRegistry;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootWorldContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import team.reborn.energy.api.base.SimpleEnergyStorage;

import java.util.ArrayList;
import java.util.List;

public class QuaryBlockEntity extends BasicMachineBlockEntity {
    private int progress;
    private BlockPos miningPos = this.pos.down();

    public QuaryBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.QUARY_BLOCK_ENTITY, pos, state);
    }

    @Override
    public SimpleEnergyStorage createEnergyStorage() {
        return super.createEnergyStorage(100000, 1000, 0);
    }

    @Override
    public void tick() {
        if (this.world == null || this.world.isClient) return;
        if (energyStorage.amount == 0) return;
        energyStorage.amount = MathHelper.clamp(energyStorage.amount - 100, 0, energyStorage.getCapacity());

        if (this.progress++ % 10 == 0) {
            if (this.miningPos.getY() <= this.world.getBottomY()) {
                this.miningPos = this.pos.down();
            }

            BlockState state = this.world.getBlockState(this.miningPos);
            if (state.isAir() || state.getHardness(this.world, this.miningPos) < 0) {
                this.miningPos = this.miningPos.down();
                return;
            }

            List<ItemStack> drops = new ArrayList<>(state.getDroppedStacks(
                    new LootWorldContext.Builder((ServerWorld) this.world)
                            .add(LootContextParameters.TOOL, Items.DIAMOND_PICKAXE.getDefaultStack())
                            .add(LootContextParameters.ORIGIN, this.miningPos.toCenterPos())
                            .addOptional(LootContextParameters.BLOCK_ENTITY, this)));

            this.world.breakBlock(this.miningPos, false);

            Storage<ItemVariant> aboveStorage = findItemStorage((ServerWorld) this.world, this.pos.up());
            if (aboveStorage != null && aboveStorage.supportsInsertion())
                insertDrops(drops, aboveStorage);
            if (!drops.isEmpty())
                spawnDrops(drops, (ServerWorld) this.world, this.pos);
            this.miningPos = this.miningPos.down();
        }
    }

    private Storage<ItemVariant> findItemStorage(ServerWorld world, BlockPos up) {
        return ItemStorage.SIDED.find(world, up, Direction.DOWN);
    }


    private void insertDrops(List<ItemStack> drops, Storage<ItemVariant> aboveStorage) {
        drops.forEach(itemStack -> {
            try(Transaction transaction = Transaction.openOuter()) {
                long inserted = aboveStorage.insert(ItemVariant.of(itemStack), itemStack.getCount(),transaction);
                if (inserted > 0) {
                    itemStack.decrement((int) inserted);
                    transaction.commit();
                }
            }
        });
        drops.removeIf(ItemStack::isEmpty);
    }

    private void spawnDrops(List<ItemStack> drops, ServerWorld world, BlockPos pos) {
        drops.forEach(itemStack ->
                ItemScatterer.spawn(world, pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 1.0D, itemStack)
        );
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        this.progress = nbt.getInt("progress");
        this.miningPos = BlockPos.fromLong(nbt.getLong("mining_pos"));
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        nbt.putInt("progress", this.progress);
        nbt.putLong("mining_pos", this.miningPos.asLong());
    }

    public int getProgress() {
        return this.progress;
    }
}
