package com.rouesvm.extralent.block.machine.entity;

import com.rouesvm.extralent.block.ActivatedPolymerBlock;
import com.rouesvm.extralent.block.MachineBlock;
import com.rouesvm.extralent.block.entity.BasicMachineBlockEntity;
import com.rouesvm.extralent.registries.block.BlockEntityRegistry;
import com.rouesvm.extralent.visual.ui.inventory.ExtralentInventory;
import com.rouesvm.extralent.visual.LineDrawer;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootWorldContext;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import team.reborn.energy.api.base.SimpleEnergyStorage;

import java.util.*;

public class HarvesterBlockEntity extends BasicMachineBlockEntity {
    private static final Vec3i boxSize = new Vec3i(9, 2, 9);

    public static final long ENERGY_USED = 500;
    private static final long ENERGY_PER_OPERATION = ENERGY_USED / Math.max(1, (boxSize.getX() * boxSize.getZ()) / 2);

    private static final int[] INPUT_SLOTS_ARRAY = {0, 1, 2};
    private static final int[] OUTPUT_SLOTS_ARRAY = {3, 4, 5, 6, 7, 8};

    private static final List<BlockPos> BOX_POSITIONS = preCalculateBoxPositions();

    private final InventoryStorage outputInventory;

    private final HashSet<BlockPos> soilPos = new HashSet<>(boxSize.getX() * boxSize.getZ() / 2);
    private final Queue<BlockPos> soilQueue = new LinkedList<>();

    private final Queue<BlockPos> toHarvestQueue = new LinkedList<>();

    private static List<BlockPos> preCalculateBoxPositions() {
        List<BlockPos> positions = new ArrayList<>();
        for (int y = 0; y < boxSize.getY(); y++) {
            for (int x = -boxSize.getX()/2; x < boxSize.getX()/2; x++) {
                for (int z = -boxSize.getZ()/2; z < boxSize.getZ()/2; z++) {
                    positions.add(new BlockPos(x, y + 2, z));
                }
            }
        }
        return positions;
    }

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
                return Arrays.stream(INPUT_SLOTS_ARRAY)
                        .anyMatch(input -> slot == input);
            }

            @Override
            public boolean canExtract(int slot, ItemStack stack, Direction dir) {
                if (dir == Direction.UP) {
                    return Arrays.stream(INPUT_SLOTS_ARRAY).anyMatch(input -> slot == input);
                } else {
                    return Arrays.stream(OUTPUT_SLOTS_ARRAY).anyMatch(output -> slot == output);
                }
            }
        };
    }

    @Override
    public void tick(World world, BlockPos pos, BlockState state, BlockEntity entity) {
        if (world == null || world.isClient()) return;

        boolean activated = state.get(ActivatedPolymerBlock.ACTIVATED);
        boolean shouldActivate = energyStorage.amount > ENERGY_USED;

        if (activated != shouldActivate) {
            world.setBlockState(pos, state.with(ActivatedPolymerBlock.ACTIVATED, shouldActivate));
            markDirty(world, pos, state);
        }

        if (energyStorage.amount > ENERGY_USED) {
            progress++;

            if (progress % 2 == 0) harvestAndPlant(world);
            if (progress % 40 == 0) scanArea(world);
        }
    }

    private void harvestAndPlant(World world) {
        boolean didWork = false;

        if (!soilQueue.isEmpty()) {
            BlockPos pos = soilQueue.poll();
            if (isLoaded(pos)) {
                plantSapling(world, pos);
                didWork = true;
            }
        }

        if (!toHarvestQueue.isEmpty()) {
            BlockPos pos = toHarvestQueue.poll();
            if (isLoaded(pos)) {
                harvestTree(world, pos);
                didWork = true;
            }
        }

        if (didWork) {
            energyStorage.amount = MathHelper.clamp(
                    energyStorage.amount - ENERGY_PER_OPERATION,
                    0,
                    energyStorage.getCapacity());

            markDirty();
        }
    }

    private void scanArea(World world) {
        for (BlockPos relativePos : BOX_POSITIONS) {
            BlockPos worldPos = pos.add(relativePos);
            if (!isLoaded(worldPos)) continue;

            BlockState state = world.getBlockState(worldPos);

            if (isGroundSuitable(state) && !world.isAir(worldPos)) {
                soilPos.add(worldPos);
            }
        }

        if (soilQueue.isEmpty()) soilQueue.addAll(soilPos);

        if (!soilQueue.isEmpty() && toHarvestQueue.isEmpty()) {
            for (BlockPos pos : soilQueue) {
                if (isBreakableBlock(world.getBlockState(pos.up()))
                ) toHarvestQueue.add(pos.up());
            }
        }

        if (!soilPos.isEmpty()) {
            energyStorage.amount = MathHelper.clamp(
                    energyStorage.amount - ENERGY_USED,
                    0,
                    energyStorage.getCapacity());
            markDirty();
        }
    }

    private void harvestTree(World world, BlockPos pos) {
        if (world.isAir(pos)) return;

        Queue<BlockPos> toCheck = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();

        toCheck.add(pos);
        visited.add(pos);

        boolean playedSound = false;
        int blocksProcessed = 0;

        final int MAX_BLOCKS_PER_TICK = 32;

        while (!toCheck.isEmpty() && blocksProcessed < MAX_BLOCKS_PER_TICK) {
            BlockPos current = toCheck.poll();
            BlockState state = world.getBlockState(current);

            if (!isBreakableBlock(state)) continue;

            if (!playedSound) {
                playedSound = true;
                world.playSound(null, pos, SoundEvents.BLOCK_WOOD_BREAK, SoundCategory.BLOCKS, 2f, 1f);
            }

            insertDrops(state, current);
            world.breakBlock(current, false, null, 1);
            blocksProcessed++;

            for (Direction direction : Direction.values()) {
                BlockPos neighbor = current.offset(direction);

                if (visited.add(neighbor)
                        && isBreakableBlock(world.getBlockState(neighbor))
                ) toCheck.add(neighbor);
            }
        }

        if (!toCheck.isEmpty()) {
            for (BlockPos remaining : toCheck) {
                if (!toHarvestQueue.contains(remaining)
                ) toHarvestQueue.add(remaining);
            }
        }
    }

    private void plantSapling(World world, BlockPos pos) {
        if (inventory.isEmpty()) return;

        if (world.isAir(pos) || !world.isAir(pos.up())) {
            soilPos.remove(pos);
            return;
        }

        Optional<Item> selectedSapling = inventory.hasTag(ItemTags.SAPLINGS);

        if (selectedSapling.isPresent()) {
            ItemStack removedStack = inventory.removeItem(selectedSapling.get(), 1);
            if (!removedStack.isEmpty()) {
                Block saplingBlock = Block.getBlockFromItem(selectedSapling.get());
                world.playSound(null, pos.up(), SoundEvents.BLOCK_GRASS_PLACE, SoundCategory.BLOCKS, 1f, 1f);
                world.setBlockState(pos.up(), saplingBlock.getDefaultState());
            }
        }
    }

    private void insertDrops(BlockState state, BlockPos current) {
        List<ItemStack> drops = state.getDroppedStacks(
                new LootWorldContext.Builder((ServerWorld) this.world)
                        .add(LootContextParameters.TOOL, Items.DIAMOND_AXE.getDefaultStack())
                        .add(LootContextParameters.ORIGIN, current.toCenterPos())
                        .addOptional(LootContextParameters.BLOCK_ENTITY, this));

        for (ItemStack drop : drops) {
            if (!drop.isEmpty()) {
                if (drop.isIn(ItemTags.SAPLINGS)
                ) drop = inventory.insertStack(drop, INPUT_SLOTS_ARRAY);
                if (!drop.isEmpty()
                ) inventory.insertStack(drop, OUTPUT_SLOTS_ARRAY);
            }
        }
    }

    private boolean isBreakableBlock(BlockState state) {
        return state.isIn(BlockTags.LOGS) || state.isIn(BlockTags.LEAVES);
    }

    private boolean isGroundSuitable(BlockState state) {
        return state.isIn(BlockTags.DIRT);
    }

    private boolean isLoaded(BlockPos pos) {
        if (world == null) return false;
        return world.isChunkLoaded(ChunkSectionPos.getSectionCoord(pos.getX()), ChunkSectionPos.getSectionCoord(pos.getZ()));
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
