package com.rouesvm.extralent.block.machines.entity;

import com.rouesvm.extralent.block.MachineBlock;
import com.rouesvm.extralent.block.entity.BasicMachineBlockEntity;
import com.rouesvm.extralent.registries.block.BlockEntityRegistry;
import com.rouesvm.extralent.ui.inventory.ExtralentInventory;
import com.rouesvm.extralent.visual.LineDrawer;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import team.reborn.energy.api.base.SimpleEnergyStorage;

import java.util.*;

public class HarvesterBlockEntity extends BasicMachineBlockEntity {
    private static final int width = 8;
    private static final int height = 8;
    private static final int depth = 8;

    private int ticks;

    private static final int[] INPUT_SLOTS_ARRAY = {0, 1, 2};
    private static final int[] OUTPUT_SLOTS_ARRAY = {3, 4, 5, 6, 7, 8};

    private final InventoryStorage outputInventory;

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

        if (energyStorage.amount <= 0) {
            machineBaseBlock.setState(false, world, pos);
            return;
        }

        if (ticks++ % 20 == 0) {
            ticks = 0;
            scanArea(pos, world);

            energyStorage.amount = MathHelper.clamp(energyStorage.amount - 500, 0, energyStorage.getCapacity());
            machineBaseBlock.setState(true, world, pos);
        }
    }

    public void scanArea(BlockPos machinePos, World world) {
        int xStart = machinePos.getX() - width / 2;
        int zStart = machinePos.getZ() - depth / 2;
        int yStart = machinePos.getY() + 1;
        int yEnd = yStart + height;

        for (int x = xStart; x <= xStart + width; x++) {
            for (int z = zStart; z <= zStart + depth; z++) {
                for (int y = yStart; y < yEnd; y++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockState state = world.getBlockState(pos);
                    if (isLog(state)) {
                        harvestTree(world, pos);
                    } else if (isGroundSuitable(world.getBlockState(pos.down())) && isAirAbove(world, pos.down())) {
                        plantSapling(world, pos.down());
                    }
                }
            }
        }
    }

    private void harvestTree(World world, BlockPos pos) {
        Queue<BlockPos> toCheck = new LinkedList<>();
        toCheck.add(pos);

        while (!toCheck.isEmpty()) {
            BlockPos current = toCheck.poll();
            BlockState state = world.getBlockState(current);
            if (isLog(state)) {
                insertDrops(getDrops(state, current));
                world.breakBlock(current, false);
                for (Direction direction : Direction.values()) {
                    BlockPos neighbor = current.offset(direction);
                    if (isLog(world.getBlockState(neighbor))) {
                        toCheck.add(neighbor);
                    }
                }
            }
        }
    }

    private List<ItemStack> getDrops(BlockState state, BlockPos current) {
        return new ArrayList<>(state.getDroppedStacks(
                new LootContextParameterSet.Builder((ServerWorld) this.world)
                        .add(LootContextParameters.TOOL, Items.DIAMOND_AXE.getDefaultStack())
                        .add(LootContextParameters.ORIGIN, current.toCenterPos())
                        .addOptional(LootContextParameters.BLOCK_ENTITY, this)));
    }

    private void insertDrops(List<ItemStack> drops) {
        drops.forEach(itemStack -> {
            ItemStack stack = itemStack.copy();

            for (int slot : OUTPUT_SLOTS_ARRAY) {
                ItemStack slotStack = inventory.getStack(slot);
                if (slotStack.isEmpty() || ItemStack.areItemsEqual(slotStack, stack)) {
                    int availableSpace = 64 - slotStack.getCount();
                    int toInsert = Math.min(stack.getCount(), availableSpace);

                    if (slotStack.isEmpty()) {
                        ItemStack newStack = stack.copy();
                        newStack.setCount(toInsert);
                        inventory.setStack(slot, newStack);
                    } else slotStack.increment(toInsert);

                    stack.decrement(toInsert);
                    itemStack.decrement(toInsert);

                    if (stack.isEmpty()) break;
                }
            }
        });
        drops.removeIf(ItemStack::isEmpty);
    }

    private boolean isLog(BlockState state) {
        return state.isIn(BlockTags.LOGS) || state.isIn(BlockTags.LEAVES);
    }

    private boolean isAirAbove(World world, BlockPos pos) {
        return world.isAir(pos.up());
    }

    private void plantSapling(World world, BlockPos pos) {
        if (world.isAir(pos.up())) {
            if (inventory.isEmpty()) return;
            Item selectedSapling = null;
            for (ItemStack stack : inventory.getStacks()) {
                if (stack.isIn(ItemTags.SAPLINGS)) selectedSapling = stack.getItem();
            }

            if (selectedSapling != null
                    && !inventory.removeItem(selectedSapling, 1).isEmpty()
            ) {
                Block saplingBlock = Block.getBlockFromItem(selectedSapling);
                world.setBlockState(pos.up(), saplingBlock.getDefaultState());
            }
        }
    }

    private boolean isGroundSuitable(BlockState state) {
        return state.isOf(Blocks.DIRT) || state.isOf(Blocks.GRASS_BLOCK);
    }

    private void visualizeScanArea(BlockPos machinePos, ServerWorld world) {
        BlockPos start = machinePos.add(-width / 2, 0, -depth / 2); // Bottom-left front corner
        BlockPos end = machinePos.add(width / 2, height, depth / 2); // Top-right back corner

        for (int i = 0; i < 4; i++) {
            BlockPos topCorner = getCorner(start, end, i, true);
            BlockPos bottomCorner = getCorner(start, end, i, false);
            LineDrawer.drawLine(ParticleTypes.WAX_ON, topCorner, bottomCorner, world);
        }

        for (int i = 0; i < 4; i++) {
            BlockPos startCorner = getCorner(start, end, i, true);
            BlockPos endCorner = getCorner(start, end, (i + 1) % 4, true);
            LineDrawer.drawLine(ParticleTypes.WAX_ON, startCorner, endCorner, world);
        }

        for (int i = 0; i < 4; i++) {
            BlockPos startCorner = getCorner(start, end, i, false);
            BlockPos endCorner = getCorner(start, end, (i + 1) % 4, false);
            LineDrawer.drawLine(ParticleTypes.WAX_ON, startCorner, endCorner, world);
        }
    }

    private BlockPos getCorner(BlockPos start, BlockPos end, int cornerIndex, boolean isTop) {
        int x = (cornerIndex == 0 || cornerIndex == 3) ? start.getX() : end.getX();
        int z = (cornerIndex == 0 || cornerIndex == 1) ? start.getZ() : end.getZ();
        int y = isTop ? end.getY() : start.getY();
        return new BlockPos(x, y, z);
    }

    @Override
    public Text infoOnClicked() {
        visualizeScanArea(pos, (ServerWorld) world);
        return super.infoOnClicked();
    }

    @Override
    public InventoryStorage getInventoryProvider(Direction direction) {
        if (direction == Direction.UP)
            return outputInventory;
        return inventoryStorage;
    }
}
