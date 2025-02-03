package com.rouesvm.extralent.block.machine.entity;

import com.rouesvm.extralent.block.ActivatedPolymerBlock;
import com.rouesvm.extralent.block.MachineBlock;
import com.rouesvm.extralent.block.entity.BasicMachineBlockEntity;
import com.rouesvm.extralent.registries.block.BlockEntityRegistry;
import com.rouesvm.extralent.visual.ui.inventory.ExtralentInventory;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.SmeltingRecipe;
import net.minecraft.recipe.input.SingleStackRecipeInput;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import team.reborn.energy.api.base.SimpleEnergyStorage;

import java.util.Optional;

public class ElectricFurnaceBlockEntity extends BasicMachineBlockEntity {
    protected static final int INPUT_SLOT_INDEX = 0;
    protected static final int OUTPUT_SLOT_INDEX = 1;

    private static final int[] INPUT_SLOTS_ARRAY = {INPUT_SLOT_INDEX};
    private static final int[] OUTPUT_SLOTS_ARRAY = {OUTPUT_SLOT_INDEX};

    private static final long ENERGY_USED_PER_SECOND = 10; // ENERGY_USED * (SECONDS * 20)
    private static final double TIME_TO_BURN_IN_SECONDS = 0.5;

    private SmeltingRecipe current_recipe;
    private final InventoryStorage outputInventory;

    public ElectricFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.ELECTRIC_FURNACE_BLOCK_ENTITY, pos, state);
        this.outputInventory = InventoryStorage.of(inventory, Direction.UP);
        this.inventoryStorage = InventoryStorage.of(inventory, Direction.DOWN);
    }

    @Override
    public SimpleEnergyStorage createEnergyStorage() {
        return super.createEnergyStorage(100000, 800, 0);
    }

    @Override
    public ExtralentInventory createInventory() {
        return new ExtralentInventory(2) {
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
            public boolean isValid(int slot, ItemStack stack) {
                return canInsert(slot, stack, null);
            }

            @Override
            public boolean canInsert(int slot, ItemStack stack, Direction dir) {
                if (canSmelt(stack).isEmpty())
                    return false;
                return slot == INPUT_SLOT_INDEX;
            }

            @Override
            public boolean canExtract(int slot, ItemStack stack, Direction dir) {
                if (dir == Direction.UP && slot == INPUT_SLOT_INDEX)
                    return true;
                else return slot != INPUT_SLOT_INDEX;
            }
        };
    }

    @Override
    public void tick(World world, BlockPos pos, BlockState state, BlockEntity blockEntity) {
        if (world == null || world.isClient) return;

        long energy_used = calculateEnergyUsed(ENERGY_USED_PER_SECOND, TIME_TO_BURN_IN_SECONDS);
        if (energyStorage.amount < energy_used) {
            state = state.with(ActivatedPolymerBlock.ACTIVATED, false);
            world.setBlockState(pos, state, Block.NOTIFY_ALL);
            return;
        }

        if (validItem()) {
            if (progress++ >= TIME_TO_BURN_IN_SECONDS * 20) {
                if (energyStorage.amount < energy_used) return;
                if (outputItem()) {
                    energyStorage.amount = MathHelper.clamp(energyStorage.amount - energy_used, 0, energyStorage.getCapacity());
                    state = state.with(ActivatedPolymerBlock.ACTIVATED, false);
                    world.setBlockState(pos, state, Block.NOTIFY_ALL);
                }
            } else {
                state = state.with(ActivatedPolymerBlock.ACTIVATED, true);
                world.setBlockState(pos, state, Block.NOTIFY_ALL);
            }

            BlockEntity.markDirty(world, pos, state);
        } else progress = 0;
    }

    private Optional<SmeltingRecipe> canSmelt(ItemStack input) {
        Optional<SmeltingRecipe> stackRecipe = world.getRecipeManager().getFirstMatch(RecipeType.SMELTING,
                new SingleStackRecipeInput(input), world).map(RecipeEntry::value);

        if (stackRecipe.isPresent() && !stackRecipe.get().getResult(world.getRegistryManager()).isEmpty())
            return stackRecipe;
        else return Optional.empty();
    }

    private boolean validItem() {
        ItemStack inputStack = inventory.getStack(INPUT_SLOT_INDEX);
        if (!inputStack.isEmpty()) {
            Optional<SmeltingRecipe> stackRecipe = canSmelt(inputStack);

            if (stackRecipe.isPresent() && canAcceptOutput(stackRecipe.get())) {
                current_recipe = stackRecipe.get();
                return true;
            }
        }
        return false;
    }

    private boolean canAcceptOutput(SmeltingRecipe recipe) {
        ItemStack recipeOutput = recipe.getResult(world.getRegistryManager());
        ItemStack stack = inventory.getStack(OUTPUT_SLOT_INDEX);
        if (recipeOutput.isEmpty()) return false;
        if (stack.getCount() > 64) return false;
        if (stack.isEmpty()) return true;
        return stack.getItem() == recipeOutput.getItem();
    }

    private boolean outputItem() {
        if (current_recipe == null) return false;
        if (inventory.getStack(INPUT_SLOT_INDEX).isEmpty()) return false;
        if (!canAcceptOutput(current_recipe)) return false;

        ItemStack outputStack = inventory.getStack(OUTPUT_SLOT_INDEX);
        if (outputStack.getCount() >= outputStack.getMaxCount()) return false;

        ItemStack result = current_recipe.getResult(world.getRegistryManager());
        inventory.insertStackTo(result.copy(), OUTPUT_SLOT_INDEX);
        inventory.getStack(INPUT_SLOT_INDEX).decrement(1);

        return true;
    }

    @Override
    public InventoryStorage getInventoryProvider(Direction direction) {
        if (direction == Direction.UP)
            return outputInventory;
        return inventoryStorage;
    }
}
