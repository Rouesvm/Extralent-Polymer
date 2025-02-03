package com.rouesvm.extralent.block.machine.entity;

import com.rouesvm.extralent.block.ActivatedPolymerBlock;
import com.rouesvm.extralent.block.entity.BasicMachineBlockEntity;
import com.rouesvm.extralent.item.custom.data.InfoData;
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
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
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
    private static final Vec3i BOX_SIZE = new Vec3i(9, 2, 9);

    public static final long ENERGY_USED = 500;

    private static final int[] INPUT_SLOTS_ARRAY = {0, 1, 2};
    private static final int[] OUTPUT_SLOTS_ARRAY = {3, 4, 5, 6, 7, 8};


    private final Box box;

    private final InventoryStorage outputInventory;

    private final HashSet<BlockPos> soilPos = new HashSet<>(BOX_SIZE.getX() * BOX_SIZE.getZ() / 2);
    private final Queue<BlockPos> soilQueue = new LinkedList<>();

    private final HashSet<BlockPos> toHarvestPos = new HashSet<>(BOX_SIZE.getX() * BOX_SIZE.getZ() / 2);
    private final Queue<BlockPos> toHarvestQueue = new LinkedList<>();

    public HarvesterBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.HARVESTER_BLOCK_ENTITY, pos, state);
        this.outputInventory = InventoryStorage.of(inventory, Direction.UP);
        this.inventoryStorage = InventoryStorage.of(inventory, Direction.DOWN);

        Vec3d startPos = new Vec3d((pos.getX() - (double) BOX_SIZE.getX() / 2), (pos.getY() + 1), (pos.getZ() - (double) BOX_SIZE.getZ() / 2));
        Vec3d endPos = startPos.add(Vec3d.of(BOX_SIZE));
        this.box = new Box(startPos, endPos);
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
                if (dir == Direction.UP && slot == INPUT_SLOTS_ARRAY[slot])
                    return true;
                else return Arrays.stream(INPUT_SLOTS_ARRAY)
                        .anyMatch(input -> slot != input);
            }
        };
    }

    @Override
    public void tick(World world, BlockPos pos, BlockState state, BlockEntity blockEntity) {
        if (world == null || world.isClient) return;

        if (energyStorage.amount <= ENERGY_USED) {
            state = state.with(ActivatedPolymerBlock.ACTIVATED, false);
            world.setBlockState(pos, state, Block.NOTIFY_ALL);
        } else {
            state = state.with(ActivatedPolymerBlock.ACTIVATED, true);
            world.setBlockState(pos, state, Block.NOTIFY_ALL);

            progress++;
            if (progress % 6 == 0) {
                harvestAndPlant(world);
                energyStorage.amount = MathHelper.clamp(
                        energyStorage.amount - ENERGY_USED / (((long) BOX_SIZE.getX() * BOX_SIZE.getZ())/ 2),
                        0,
                        energyStorage.getCapacity());
            }

            if ((soilQueue.isEmpty() && toHarvestQueue.isEmpty())
                    && progress % 80 == 0
            ) {
                scanArea(world);
                energyStorage.amount = MathHelper.clamp(
                        energyStorage.amount - ENERGY_USED,
                        0,
                        energyStorage.getCapacity());
            }

            BlockEntity.markDirty(world, pos, state);
        }
    }

    private void harvestAndPlant(World world) {
        if (!soilQueue.isEmpty()) {
            BlockPos pos = soilQueue.poll();
            plantSapling(world, pos);
            soilQueue.remove(pos);
        }

        if (!toHarvestQueue.isEmpty()) {
            BlockPos pos = toHarvestQueue.poll();
            harvestTree(world, pos);
            toHarvestQueue.remove(pos);
        }
    }

    private void scanArea(World world) {
        for (BlockPos pos : getBlockPosInBox(box)) {
            if (isLoaded(pos)) {
                BlockState state = world.getBlockState(pos);

                if (isBreakableBlock(state)) toHarvestPos.add(pos);
                if (isGroundSuitable(state) && world.isAir(pos.up())) soilPos.add(pos);
            }
        }

        if (toHarvestQueue.isEmpty()) toHarvestQueue.addAll(toHarvestPos);
        if (soilQueue.isEmpty()) soilQueue.addAll(soilPos);
    }

    private void harvestTree(World world, BlockPos pos) {
        if (world.isAir(pos)) return;

        Queue<BlockPos> toCheck = new LinkedList<>();
        toCheck.add(pos);

        boolean playedSound = false;
        while (!toCheck.isEmpty()) {
            BlockPos current = toCheck.poll();
            BlockState state = world.getBlockState(current);
            if (!isBreakableBlock(state)) continue;

            if (!playedSound) {
                playedSound = true;
                world.playSound(null, pos, SoundEvents.BLOCK_WOOD_BREAK, SoundCategory.BLOCKS, 2f, 1f);
            }

            insertDrops(state, current);
            world.setBlockState(current, Blocks.AIR.getDefaultState());
            for (Direction direction : Direction.values()) {
                BlockPos neighbor = current.offset(direction);
                if (isBreakableBlock(world.getBlockState(neighbor))) {
                    toCheck.add(neighbor);
                }
            }
        }
    }

    private void plantSapling(World world, BlockPos pos) {
        if (world.isAir(pos) && !world.isAir(pos.up())) return;
        if (inventory.isEmpty()) return;

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
                new LootContextParameterSet.Builder((ServerWorld) this.world)
                        .add(LootContextParameters.TOOL, Items.DIAMOND_AXE.getDefaultStack())
                        .add(LootContextParameters.ORIGIN, current.toCenterPos())
                        .addOptional(LootContextParameters.BLOCK_ENTITY, this));

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

    private boolean isLoaded(BlockPos pos) {
        if (world == null) return false;
        return world.isChunkLoaded(ChunkSectionPos.getSectionCoord(pos.getX()), ChunkSectionPos.getSectionCoord(pos.getZ()));
    }

    private static List<BlockPos> getBlockPosInBox(Box axisAlignedBox) {
        List<BlockPos> blocks = new ArrayList<>();
        for (double y = axisAlignedBox.minY; y < axisAlignedBox.maxY; ++y) {
            for (double x = axisAlignedBox.minX; x < axisAlignedBox.maxX; ++x) {
                for (double z = axisAlignedBox.minZ; z < axisAlignedBox.maxZ; ++z) {
                    blocks.add(new BlockPos((int) x, (int) y, (int) z));
                }
            }
        }
        return blocks;
    }

    @Override
    public Text infoOnClicked(InfoData.DISPLAY display) {
        if (display.equals(InfoData.DISPLAY.FLOATING)) getCustomInfo();
        return super.infoOnClicked(display);
    }

    @Override
    public Text getCustomInfo() {
        LineDrawer.visualizeScanArea(pos, (ServerWorld) world, BOX_SIZE);
        return Text.translatable("info.machine.display_range").append(
                "XYZ: " + BOX_SIZE.getX() + " / " +
                BOX_SIZE.getY() + " / " +
                BOX_SIZE.getZ());
    }

    @Override
    public InventoryStorage getInventoryProvider(Direction direction) {
        if (direction == Direction.UP)
            return outputInventory;
        return inventoryStorage;
    }
}
