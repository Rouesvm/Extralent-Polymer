package com.rouesvm.extralent.block.machine.entity;

import com.rouesvm.extralent.block.MachineBlock;
import com.rouesvm.extralent.block.entity.BasicMachineBlockEntity;
import com.rouesvm.extralent.registries.block.BlockEntityRegistry;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
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
import net.minecraft.world.World;
import team.reborn.energy.api.base.SimpleEnergyStorage;

import java.util.ArrayList;
import java.util.List;

public class QuarryBlockEntity extends BasicMachineBlockEntity {
    public static final long ENERGY_USED = 100;

    private BlockPos mining_pos = this.pos.down();

    public QuarryBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.QUARRY_BLOCK_ENTITY, pos, state);
    }

    @Override
    public SimpleEnergyStorage createEnergyStorage() {
        return super.createEnergyStorage(100000, 800, 0);
    }

    @Override
    public void tick(World world, BlockPos pos, BlockState state, BlockEntity entity) {
        if (this.world == null || this.world.isClient) return;

        Block machineBlock = getCachedState().getBlock();
        if (!(machineBlock instanceof MachineBlock machineBaseBlock)) return;

        if (energyStorage.amount < ENERGY_USED) {
            machineBaseBlock.setState(false, world, pos);
            return;
        }

        if (this.progress++ % 10 == 0) {
            machineBaseBlock.setState(true, world, pos);

            if (this.mining_pos.getY() <= this.world.getBottomY()) {
                this.mining_pos = this.pos.down();
            }

            BlockState otherState = this.world.getBlockState(this.mining_pos);
            if (otherState.isAir() || otherState.getHardness(this.world, this.mining_pos) < 0) {
                this.mining_pos = this.mining_pos.down();
                return;
            }

            LootWorldContext.Builder builder = new LootWorldContext
                    .Builder((ServerWorld) this.world)
                    .add(LootContextParameters.TOOL, Items.DIAMOND_PICKAXE.getDefaultStack())
                    .add(LootContextParameters.ORIGIN, this.mining_pos.toCenterPos())
                    .addOptional(LootContextParameters.BLOCK_ENTITY, this);
            List<ItemStack> drops = new ArrayList<>(otherState.getDroppedStacks(builder));

            this.world.breakBlock(this.mining_pos, false);

            if (!drops.isEmpty()) {
                Storage<ItemVariant> aboveStorage = findItemStorage((ServerWorld) this.world, this.pos.up());
                if (aboveStorage != null && aboveStorage.supportsInsertion())
                    insertDrops(drops, aboveStorage);
                else spawnDrops(drops, (ServerWorld) this.world, this.pos);
            }

            energyStorage.amount = MathHelper.clamp(energyStorage.amount - ENERGY_USED, 0, energyStorage.getCapacity());
            markDirty();

            this.mining_pos = this.mining_pos.down();
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
        this.mining_pos = BlockPos.fromLong(nbt.getLong("mining_pos", 0));
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        nbt.putInt("progress", this.progress);
        nbt.putLong("mining_pos", this.mining_pos.asLong());
    }
}
