package com.rouesvm.extralent.block.machines.entity;

import com.rouesvm.extralent.block.entity.BasicMachineBlockEntity;
import com.rouesvm.extralent.registries.block.BlockEntityRegistry;
import com.rouesvm.extralent.utils.visual.LineDrawer;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import team.reborn.energy.api.base.SimpleEnergyStorage;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class HarvesterBlockEntity extends BasicMachineBlockEntity {
    private final int width = 8;
    private final int height = 8;
    private final int depth = 8;

    private int ticks;

    public HarvesterBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.HARVESTER_BLOCK_ENTITY, pos, state);
    }

    @Override
    public SimpleEnergyStorage createEnergyStorage() {
        return super.createEnergyStorage(100000, 1500, 0);
    }

    @Override
    public SimpleInventory createInventory() {
        return super.createInventory(9);
    }

    @Override
    public void tick() {
        if (world == null || world.isClient) return;
        if (ticks++ % 20 == 0) {
            ticks = 0;
            scanArea(pos, world);
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
                List<ItemStack> drops = new ArrayList<>(state.getDroppedStacks(
                        new LootContextParameterSet.Builder((ServerWorld) this.world)
                                .add(LootContextParameters.TOOL, Items.DIAMOND_AXE.getDefaultStack())
                                .add(LootContextParameters.ORIGIN, current.toCenterPos())
                                .addOptional(LootContextParameters.BLOCK_ENTITY, this)));

                insertDrops(drops);

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

    private void insertDrops(List<ItemStack> drops) {
        drops.forEach(itemStack -> {
            ItemStack stack = getInventory().addStack(itemStack);
            itemStack.decrement(stack.getCount());
        });
        drops.removeIf(ItemStack::isEmpty);
    }

    private boolean isLog(BlockState state) {
        return state.isOf(Blocks.OAK_LOG) || state.isOf(Blocks.OAK_LEAVES);
    }

    private boolean isAirAbove(World world, BlockPos pos) {
        return world.isAir(pos.up());
    }

    private void plantSapling(World world, BlockPos pos) {
        if (world.isAir(pos.up())) {
            if (inventory.isEmpty()) return;
            if (!inventory.removeItem(Items.OAK_SAPLING, 1).isEmpty()) {
                world.setBlockState(pos.up(), Blocks.OAK_SAPLING.getDefaultState());
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
}
