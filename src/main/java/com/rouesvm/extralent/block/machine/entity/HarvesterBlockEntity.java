package com.rouesvm.extralent.block.machine.entity;

import com.rouesvm.extralent.block.MachineBlock;
import com.rouesvm.extralent.block.entity.BasicMachineBlockEntity;
import com.rouesvm.extralent.block.transport.entity.connection.Connection;
import com.rouesvm.extralent.registries.block.BlockEntityRegistry;
import com.rouesvm.extralent.registries.block.BlockRegistry;
import com.rouesvm.extralent.ui.inventory.ExtralentInventory;
import com.rouesvm.extralent.visual.LineDrawer;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SaplingBlock;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.CuboidBlockIterator;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import team.reborn.energy.api.base.SimpleEnergyStorage;

import java.util.*;

public class HarvesterBlockEntity extends BasicMachineBlockEntity {
    private static final Vec3i boxSize = new Vec3i(8, 8, 8);

    public static final long ENERGY_USED = 500;

    private static final int[] INPUT_SLOTS_ARRAY = {0, 1, 2};
    private static final int[] OUTPUT_SLOTS_ARRAY = {3, 4, 5, 6, 7, 8};

    private int ticks;
    private final InventoryStorage outputInventory;

    private final HashSet<BlockPos> soilPos = new HashSet<>(boxSize.getX() * boxSize.getZ() / 2);
    private HashSet<BlockPos> cachedPos = null;

    private Queue<BlockPos> soilQueue = new LinkedList<>();

    public HarvesterBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.HARVESTER_BLOCK_ENTITY, pos, state);
        this.outputInventory = InventoryStorage.of(inventory, Direction.UP);
        this.inventoryStorage = InventoryStorage.of(inventory, Direction.DOWN);
    }

    @Override
    public SimpleEnergyStorage createEnergyStorage() {
        return super.createEnergyStorage(100000, 800, 0);
    }

    @Override
    public ExtralentInventory createInventory() {
        return new ExtralentInventory(9) {
            @Override
            public void markDirty() {
                super.markDirty();
                update();
            }

            @Override
            public int[] getAvailableSlots(Direction side) {
                if (side == Direction.UP)
                    return INPUT_SLOTS_ARRAY;
                return OUTPUT_SLOTS_ARRAY;
            }

            @Override
            public boolean canInsert(int slot, ItemStack stack, Direction dir) {
                return Arrays.stream(INPUT_SLOTS_ARRAY).anyMatch(input -> slot == input);
            }

            @Override
            public boolean canExtract(int slot, ItemStack stack, Direction dir) {
                if (dir == Direction.UP && slot == INPUT_SLOTS_ARRAY[slot])
                    return true;
                else return Arrays.stream(INPUT_SLOTS_ARRAY).anyMatch(input -> slot != input);
            }
        };
    }

    @Override
    public void tick() {
        if (world == null || world.isClient) return;
        Block machineBlock = getCachedState().getBlock();
        if (!(machineBlock instanceof MachineBlock machineBaseBlock)) return;

        if (cachedPos == null) {
            cachedPos = new HashSet<>(boxSize.getX() * boxSize.getY() * boxSize.getZ());
            cacheArea();
        }

        if (energyStorage.amount <= ENERGY_USED) {
            machineBaseBlock.setState(false, world, pos);
            return;
        } else machineBaseBlock.setState(true, world, pos);

        ticks++;
        if (ticks % 6 == 0) {
            plantSaplings(world);
            energyStorage.amount = MathHelper.clamp(energyStorage.amount - ENERGY_USED / ((long) boxSize.getX() * boxSize.getZ() / 2), 0, energyStorage.getCapacity());
            markDirty();
        }

        if (ticks % 80 == 0) {
            scanAreaForSoil(world);
            energyStorage.amount = MathHelper.clamp(energyStorage.amount - ENERGY_USED, 0, energyStorage.getCapacity());
            markDirty();
        }
    }

    private void cacheArea() {
        int xStart = pos.getX() - boxSize.getX() / 2;
        int zStart = pos.getZ() - boxSize.getZ() / 2;
        int yStart = pos.getY() + 1;
        int yEnd = yStart + boxSize.getY();

        for (int x = xStart; x <= xStart + boxSize.getX(); x++) {
            for (int z = zStart; z <= zStart + boxSize.getZ(); z++) {
                for (int y = yStart; y < yEnd; y++) {
                    cachedPos.add(new BlockPos(x, y, z));
                }
            }
        }
    }

    private void plantSaplings(World world) {
        if (!soilQueue.isEmpty()) {
            BlockPos pos = soilQueue.poll();
            boolean didPlant = plantSapling(world, pos.down());
            if (!didPlant) plantSaplings(world);
        }
    }

    private void scanAreaForSoil(World world) {
        cachedPos.forEach(pos -> {
            BlockState state = world.getBlockState(pos);
            BlockState downState = world.getBlockState(pos.down());
            if (isBreakableBlock(state))
                harvestTree(world, pos);
            if (isGroundSuitable(downState) && world.isAir(pos.up()))
                soilPos.add(pos);
            else soilPos.remove(pos);
        });

        if (soilQueue.isEmpty()) soilQueue.addAll(soilPos);
    }

    private void harvestTree(World world, BlockPos pos) {
        Queue<BlockPos> toCheck = new LinkedList<>();
        toCheck.add(pos);

        boolean playSound = false;

        while (!toCheck.isEmpty()) {
            BlockPos current = toCheck.poll();
            BlockState state = world.getBlockState(current);
            if (!isBreakableBlock(state)) continue;

            if (!playSound) {
                playSound = true;
                world.playSound(null, pos, SoundEvents.BLOCK_WOOD_BREAK, SoundCategory.BLOCKS, 2f, 1f);
            }

            insertDrops(getDrops(state, current));
            world.setBlockState(current, Blocks.AIR.getDefaultState());
            for (Direction direction : Direction.values()) {
                BlockPos neighbor = current.offset(direction);
                if (isBreakableBlock(world.getBlockState(neighbor))) {
                    toCheck.add(neighbor);
                }
            }
        }
    }

    private boolean plantSapling(World world, BlockPos pos) {
        if (world.isAir(pos)) return false;
        if (!world.isAir(pos.up())) return false;
        if (inventory.isEmpty()) return true;

        Item selectedSapling = inventory.getStacks().stream()
                .filter(stack -> stack.isIn(ItemTags.SAPLINGS))
                .map(ItemStack::getItem)
                .findFirst()
                .orElse(null);

        if (selectedSapling != null) {
            ItemStack removedStack = inventory.removeItem(selectedSapling, 1);
            if (!removedStack.isEmpty()) {
                Block saplingBlock = Block.getBlockFromItem(selectedSapling);
                world.playSound(null, pos.up(), SoundEvents.BLOCK_GRASS_PLACE, SoundCategory.BLOCKS, 1f, 1f);
                world.setBlockState(pos.up(), saplingBlock.getDefaultState());
                return true;
            }
        }

        return false;
    }

    private List<ItemStack> getDrops(BlockState state, BlockPos current) {
        return new ArrayList<>(state.getDroppedStacks(
                new LootContextParameterSet.Builder((ServerWorld) this.world)
                        .add(LootContextParameters.TOOL, Items.DIAMOND_AXE.getDefaultStack())
                        .add(LootContextParameters.ORIGIN, current.toCenterPos())
                        .addOptional(LootContextParameters.BLOCK_ENTITY, this)));
    }

    private void insertDrops(List<ItemStack> drops) {
        drops.forEach(drop -> {
            if (drop.isIn(ItemTags.SAPLINGS))
                drop = inventory.insertStack(drop, INPUT_SLOTS_ARRAY);
            inventory.insertStack(drop, OUTPUT_SLOTS_ARRAY);
        });
    }

    private boolean isBreakableBlock(BlockState state) {
        return state.isIn(BlockTags.LOGS) || state.isIn(BlockTags.LEAVES);
    }

    private boolean isGroundSuitable(BlockState state) {
        return state.isIn(BlockTags.DIRT);
    }

    @Override
    public Text infoOnClicked() {
        LineDrawer.visualizeScanArea(pos, (ServerWorld) world, boxSize);
        return super.infoOnClicked();
    }

    @Override
    public InventoryStorage getInventoryProvider(Direction direction) {
        if (direction == Direction.UP)
            return outputInventory;
        return inventoryStorage;
    }
}
