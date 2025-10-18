package com.rouesvm.extralent.block.machine.entity;

import com.rouesvm.extralent.block.ActivatedPolymerBlock;
import com.rouesvm.extralent.block.entity.BasicMachineBlockEntity;
import com.rouesvm.extralent.registries.block.BlockEntityRegistry;
import com.rouesvm.extralent.visual.ui.inventory.ExtralentInventory;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
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

    private static final double BURN_TIME_TICKS = TIME_TO_BURN_IN_SECONDS * 20;

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
                return slot == INPUT_SLOT_INDEX && canSmelt(stack).isPresent();
            }

            @Override
            public boolean canExtract(int slot, ItemStack stack, Direction dir) {
                if (dir == Direction.UP && slot == INPUT_SLOT_INDEX)
                    return true;
                else return slot == OUTPUT_SLOT_INDEX;
            }
        };
    }

    private void reset(BlockState state) {
        if (world != null) {
            progress = 0;
            is_burning = false;
            if (state.get(ActivatedPolymerBlock.ACTIVATED))
                world.setBlockState(pos, state.with(ActivatedPolymerBlock.ACTIVATED, false));
        }
    }

    @Override
    public void tick(World world, BlockPos pos, BlockState state, BlockEntity entity) {
        if (world == null || world.isClient()) return;

        final long energyUsed = calculateEnergyUsed(ENERGY_USED_PER_SECOND, TIME_TO_BURN_IN_SECONDS);
        ItemStack inputStack = inventory.getStack(INPUT_SLOT_INDEX);

        if (inputStack.isEmpty() || energyStorage.amount < energyUsed) {
            reset(state);
            return;
        }

        if (!is_burning) {
            Optional<SmeltingRecipe> recipe = canSmelt(inputStack);
            if (recipe.isPresent() && canInsertResult(getOutputStack(recipe.get(), inputStack))) {
                current_recipe = recipe.get();
                is_burning = true;
                progress = 0;
                world.setBlockState(pos, state.with(ActivatedPolymerBlock.ACTIVATED, true));
            } else {
                reset(state);
            }
            return;
        }

        progress++;
        if (progress >= BURN_TIME_TICKS) {
            if (current_recipe != null && canOutputItem()) {
                energyStorage.amount = MathHelper.clamp(
                        energyStorage.amount - energyUsed, 0, energyStorage.capacity
                );
            }
            reset(state);
        }
    }

    private ItemStack getOutputStack(SmeltingRecipe recipe, ItemStack inputStack) {
        if (world == null) return ItemStack.EMPTY;
        return recipe.craft(new SingleStackRecipeInput(inputStack), world.getRegistryManager());
    }

    private Optional<SmeltingRecipe> canSmelt(ItemStack input) {
        Optional<RecipeEntry<SmeltingRecipe>> stackRecipe = matchGetter
                .getFirstMatch(new SingleStackRecipeInput(input), (ServerWorld) world)
                .stream().findFirst();

        if (stackRecipe.isPresent()) {
            SmeltingRecipe recipe = stackRecipe.get().value();
            ItemStack stack = getOutputStack(recipe, input);
            if (!stack.isEmpty()) return Optional.of(recipe);
        }

        return Optional.empty();
    }

    private boolean canInsertResult(ItemStack result) {
        if (result.isEmpty()) return false;
        ItemStack output = inventory.getStack(OUTPUT_SLOT_INDEX);

        if (output.isEmpty()) return true;
        if (!ItemStack.areItemsAndComponentsEqual(output, result)) return false;
        return output.getCount() + result.getCount() <= output.getMaxCount();
    }

    private boolean insertResult(ItemStack result) {
        if (result.isEmpty()) return false;
        ItemStack output = inventory.getStack(OUTPUT_SLOT_INDEX);

        if (output.isEmpty()) {
            inventory.setStack(OUTPUT_SLOT_INDEX, result.copy());
            return true;
        } else if (ItemStack.areItemsAndComponentsEqual(output, result)) {
            output.increment(result.getCount());
            return true;
        }
        return false;
    }

    private boolean canOutputItem() {
        if (current_recipe == null) return false;
        ItemStack inputStack = inventory.getStack(INPUT_SLOT_INDEX);

        if (inputStack.isEmpty()) return false;

        ItemStack result = getOutputStack(current_recipe, inputStack);
        if (insertResult(result)) {
            inputStack.decrement(1);
            is_burning = false;
            return true;
        }
        return false;
    }

    @Override
    public InventoryStorage getInventoryProvider(Direction direction) {
        if (direction == Direction.UP)
            return outputInventory;
        return inventoryStorage;
    }
}
