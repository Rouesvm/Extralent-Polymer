package com.rouesvm.extralent.block.entity;

import com.rouesvm.extralent.block.TickableBlockEntity;
import com.rouesvm.extralent.item.custom.data.InfoData;
import com.rouesvm.extralent.visual.ui.inventory.ExtralentInventory;
import com.rouesvm.extralent.block.entity.text.ProgressBarText;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import team.reborn.energy.api.base.SimpleEnergyStorage;

import java.util.HashMap;
import java.util.Map;

public class BasicMachineBlockEntity extends BlockEntity implements TickableBlockEntity {
    public final ExtralentInventory inventory;
    public InventoryStorage inventoryStorage;

    public final SimpleEnergyStorage energyStorage;

    public BasicMachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);

        this.energyStorage = createEnergyStorage();
        this.inventory = createInventory();

        if (this.inventory != null)
            this.inventoryStorage = InventoryStorage.of(inventory, null);
        else this.inventoryStorage = null;
    }

    public ExtralentInventory createInventory() {
        return null;
    }

    public ExtralentInventory createInventory(int size) {
        return new ExtralentInventory(size) {
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
            Inventories.readNbt(nbt, this.inventory.getStacks(), registryLookup);
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
            Inventories.writeNbt(nbt, this.inventory.getStacks(), registryLookup);
        if (this.energyStorage != null)
            nbt.putLong("energy", this.energyStorage.amount);
    }

    public void update() {
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

    public ExtralentInventory getInventory() {
        return this.inventory;
    }

    public long calculateEnergyUsed(long energy_used, double seconds) {
        return (long) (energy_used * (seconds * 20));
    }

    public Text infoOnClicked() {
        return this.getFormattedInfo();
    }

    public Text infoOnClicked(InfoData.CONTENT_DISPLAY content) {
        return switch (content) {
            case ENERGY -> getEnergyInfo(null, true);
            case INVENTORY -> getInventoryInfo(null, true);
            default -> Text.empty();
        };
    }

    private Text getInventoryInfo(Text text, boolean isUI) {
        if (text == null) text = Text.empty();
        if (getInventory() != null) {
            int empty = 0;

            Map<String, Integer> itemCounts = new HashMap<>();
            for (ItemStack stack : this.inventory.getStacks()) {
                if (stack.isEmpty()) {
                    empty++;
                    continue;
                }

                String itemName = stack.getName().getString();
                itemCounts.put(itemName, itemCounts.getOrDefault(itemName, 0) + stack.getCount());
            }

            if (empty == this.inventory.size()) {
                text = Text.translatable("info.machine.inventory_empty");
                return text;
            }

            String toAppend = isUI ? " " : "\n";
            for (Map.Entry<String, Integer> entry : itemCounts.entrySet())
                text = text.copy().append(toAppend).append(String.valueOf(entry.getValue())).append(" ").append(entry.getKey());
        }
        return text;
    }

    private Text getEnergyInfo(Text text, boolean isUI) {
        if (text == null) text = Text.empty();
        if (getEnergyStorage() != null) {
            Text energyAmount = Text.literal(String.valueOf(this.energyStorage.getAmount()))
                    .append("/")
                    .append(String.valueOf(this.energyStorage.getCapacity()));

            if (!isUI) {
                text = text.copy().append("\n\n").append(ProgressBarText.getProgressBar(this.energyStorage.getAmount(), this.energyStorage.getCapacity()));
                text = text.copy().append(Text.literal("\n")
                        .append(energyAmount.copy())
                        .setStyle(Style.EMPTY.withFont(Style.DEFAULT_FONT_ID)));
                return text;
            }

            text = text.copy().append(energyAmount.copy());
        }
        return text;
    }

    private Text getFormattedInfo() {
        Text text = null;
        text = getInventoryInfo(text, false);
        text = getEnergyInfo(text, false);
        return text;
    }
}
