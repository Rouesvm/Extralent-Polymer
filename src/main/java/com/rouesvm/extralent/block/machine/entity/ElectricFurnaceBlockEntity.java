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
import net.minecraft.recipe.ServerRecipeManager;
import net.minecraft.recipe.SmeltingRecipe;
import net.minecraft.recipe.input.SingleStackRecipeInput;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
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

    private boolean is_burning = false;
    private SmeltingRecipe current_recipe = null;

    private final InventoryStorage outputInventory;
    private final ServerRecipeManager.MatchGetter<SingleStackRecipeInput, SmeltingRecipe> matchGetter;

    public ElectricFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.ELECTRIC_FURNACE_BLOCK_ENTITY, pos, state);
        this.outputInventory = InventoryStorage.of(inventory, Direction.UP);
        this.inventoryStorage = InventoryStorage.of(inventory, Direction.DOWN);
        this.matchGetter = ServerRecipeManager.createCachedMatchGetter(RecipeType.SMELTING);
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
    public void tick(World world, BlockPos pos, BlockState state, BlockEntity entity) {
        if (world == null || world.isClient) return;

        long energy_used = calculateEnergyUsed(ENERGY_USED_PER_SECOND, TIME_TO_BURN_IN_SECONDS);
        if (energyStorage.amount < energy_used) return;

        Block machineBlock = getCachedState().getBlock();
        if (!(machineBlock instanceof MachineBlock machineBaseBlock)) return;

        if (!is_burning && validItem()) {
            boolean isActivated = machineBlock.getDefaultState().get(ActivatedPolymerBlock.ACTIVATED);
            if (!isActivated) {
                machineBaseBlock.setState(true, world, pos);
                markDirty();
            }

            if (progress++ >= TIME_TO_BURN_IN_SECONDS * 20) {
                if (energyStorage.amount < energy_used) return;
                if (outputItem()) {
                    energyStorage.amount = MathHelper.clamp(energyStorage.amount - energy_used, 0, energyStorage.getCapacity());

                    machineBaseBlock.setState(false, world, pos);
                    markDirty();
                }
            }
        } else if (!is_burning) progress = 0;
    }

    private ItemStack getOutputStack() {
        ItemStack stack = inventory.getStack(INPUT_SLOT_INDEX);
        return getOutputStack(current_recipe, stack);
    }

    private ItemStack getOutputStack(SmeltingRecipe recipe, ItemStack inputStack) {
        if (recipe == null) return null;
        return recipe.craft(new SingleStackRecipeInput(inputStack), world.getRegistryManager());
    }

    private Optional<SmeltingRecipe> canSmelt(ItemStack input) {
        Optional<RecipeEntry<SmeltingRecipe>> stackRecipe = matchGetter
                .getFirstMatch(
                        new SingleStackRecipeInput(input),
                        (ServerWorld) world
                ).stream().findFirst();

        if (stackRecipe.isPresent()) {
            RecipeEntry<SmeltingRecipe> recipe = stackRecipe.get();
            if (!getOutputStack(recipe.value(), input).isEmpty()
            ) return Optional.of(recipe.value());
        }

        return Optional.empty();
    }

    private boolean validItem() {
        ItemStack inputStack = inventory.getStack(INPUT_SLOT_INDEX);
        if (inputStack.isEmpty()) return is_burning;

        Optional<SmeltingRecipe> stackRecipe = canSmelt(inputStack);
        if (stackRecipe.isEmpty()) return is_burning;

        if (!canAcceptOutput(getOutputStack(stackRecipe.get(), inputStack))) return is_burning;

        current_recipe = stackRecipe.get();
        is_burning = true;
        return true;
    }

    private boolean canAcceptOutput(ItemStack recipeOutput) {
        if (recipeOutput == null || recipeOutput.isEmpty()) return true;

        ItemStack outputStack = inventory.getStack(OUTPUT_SLOT_INDEX);

        if (outputStack.isEmpty()) return true;
        if (!ItemStack.areItemsAndComponentsEqual(outputStack, recipeOutput)) return false;

        return outputStack.getCount() < outputStack.getMaxCount();
    }

    private boolean outputItem() {
        if (current_recipe == null) return false;

        ItemStack inputStack = inventory.getStack(INPUT_SLOT_INDEX);
        if (inputStack.isEmpty()) return false;
        if (canAcceptOutput(getOutputStack(current_recipe, inputStack))) return false;

        ItemStack outputStack = inventory.getStack(OUTPUT_SLOT_INDEX);
        if (outputStack.getCount() >= outputStack.getMaxCount()) return false;

        ItemStack result = getOutputStack();
        inventory.insertStackTo(result.copy(), OUTPUT_SLOT_INDEX);
        inputStack.decrement(1);

        is_burning = false;
        return true;
    }

    @Override
    public InventoryStorage getInventoryProvider(Direction direction) {
        if (direction == Direction.UP)
            return outputInventory;
        return inventoryStorage;
    }
}
