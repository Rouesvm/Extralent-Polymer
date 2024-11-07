package com.rouesvm.extralent.block.entity;

import com.rouesvm.extralent.interfaces.block.TickableBlockEntity;
import com.rouesvm.extralent.utils.ProgressBarRenderer;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import team.reborn.energy.api.base.SimpleEnergyStorage;

public class BasicMachineBlockEntity extends BlockEntity implements TickableBlockEntity {
    public final SimpleInventory inventory;
    public final InventoryStorage inventoryStorage;

    public final SimpleEnergyStorage energyStorage;

    public BasicMachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);

        this.energyStorage = createEnergyStorage();
        this.inventory = createInventory();

        if (this.inventory != null)
            this.inventoryStorage = InventoryStorage.of(inventory, null);
        else this.inventoryStorage = null;
    }

    public SimpleInventory createInventory() {
        return null;
    }

    public SimpleInventory createInventory(int size) {
        return new SimpleInventory(size) {
            @Override
            public void markDirty() {
                super.markDirty();
                update();
            }
        };
    }

    public SimpleEnergyStorage createEnergyStorage() {
        return null;
    }

    public SimpleEnergyStorage createEnergyStorage(int capacity, int maxInsert, int maxExtract) {
        return new SimpleEnergyStorage(capacity, maxInsert, maxExtract) {
            @Override
            protected void onFinalCommit() {
                super.onFinalCommit();
                markDirty();
            }
        };
    }

    @Override
    public void tick() {}

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);

        if (this.inventory != null)
            Inventories.readNbt(nbt, this.inventory.getHeldStacks(), registryLookup);
        if (this.energyStorage != null) {
            if (nbt.contains("energy", NbtElement.LONG_TYPE)) {
                this.energyStorage.amount = nbt.getLong("energy");
            }
        }
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);

        if (this.inventory != null)
            Inventories.writeNbt(nbt, this.inventory.getHeldStacks(), registryLookup);
        if (this.energyStorage != null)
            nbt.putLong("energy", this.energyStorage.amount);
    }

    private void update() {
        markDirty();
        if (world != null)
            world.updateListeners(pos, getCachedState(), getCachedState(), Block.NOTIFY_ALL);
    }

    public SimpleEnergyStorage getEnergyProvider(Direction direction) {
        return this.energyStorage;
    }

    public SimpleEnergyStorage getEnergyStorage() {
        return this.energyStorage;
    }

    public InventoryStorage getInventoryProvider(Direction direction) {
        return this.inventoryStorage;
    }

    public SimpleInventory getInventory() {
        return this.inventory;
    }

    public Text infoOnClicked() {
        return this.getFormattedInfo();
    }

    private Text getFormattedInfo() {
        Text text = null;
        if (this.inventory != null) {
            text = Text.empty();
            for (ItemStack stack : this.inventory.getHeldStacks()) {
                text = text.copy().append("\n").append(stack.getCount() + " ").append(stack.getName());
            }
        }
        if (this.energyStorage != null) {
            if (text == null) text = Text.empty();

            text = text.copy().append("\n\n").append(ProgressBarRenderer.getProgressBar(this.energyStorage.getAmount(), this.energyStorage.getCapacity()));
            Text energyAmount = Text.literal(String.valueOf(this.energyStorage.getAmount()))
                    .append("/")
                    .append(String.valueOf(this.energyStorage.getCapacity()));

            text = text.copy().append(Text.literal("\n")
                    .append(energyAmount.copy())
                    .setStyle(Style.EMPTY.withFont(Style.DEFAULT_FONT_ID)));
        }
        return text;
    }
}
