package com.rouesvm.extralent.block.entity;

import com.rouesvm.extralent.block.TickableBlockEntity;
import com.rouesvm.extralent.item.custom.info.InfoData;
import com.rouesvm.extralent.visual.ui.inventory.ExtralentInventory;
import com.rouesvm.extralent.visual.text.ProgressBarBuilder;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import team.reborn.energy.api.base.SimpleEnergyStorage;

import java.util.HashMap;
import java.util.Map;

public class BasicMachineBlockEntity extends BlockEntity implements TickableBlockEntity {

    public InventoryStorage inventoryStorage;

    public final ExtralentInventory inventory;
    public final SimpleEnergyStorage energyStorage;

    public int progress;

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
    public void tick(World world, BlockPos pos, BlockState state, BlockEntity entity) {}

    @Override
    protected void readData(ReadView data) {
        super.readData(data);

        if (this.inventory != null
        ) Inventories.readData(data, this.inventory.getStacks());
        if (this.energyStorage != null) {
            if (data.getOptionalLong("energy").isPresent()
            ) this.energyStorage.amount = data.getLong("energy", 0);
        }

        this.progress = data.getInt("progress", 0);
    }

    @Override
    protected void writeData(WriteView data) {
        super.writeData(data);

        if (this.inventory != null
        ) Inventories.writeData(data, this.inventory.getStacks());
        if (this.energyStorage != null
        ) data.putLong("energy", this.energyStorage.amount);

        data.putInt("progress", this.progress);
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
            case MACHINE -> getCustomInfo();
        };
    }

    private Text getFormattedInfo() {
        Text text = null;
        text = getInventoryInfo(text, false);
        text = getEnergyInfo(text, false);
        return text;
    }

    private Text getCustomInfo() {
        return getEnergyInfo(null, true);
    }

    public int setWeight(int prevWeight) {
        return prevWeight == 1 ? 0 : 1;
    }

    private Text ensureTextNotNull(Text text) {
        return text != null ? text : Text.empty();
    }

    private Text getInventoryInfo(Text text, boolean isUI) {
        text = ensureTextNotNull(text);
        if (getInventory() == null) return text;

        Map<String, Integer> itemCounts = getItemCounts();

        if (itemCounts.isEmpty()) return Text.translatable("info.machine.inventory_empty");
        return appendItemCounts(text, itemCounts, isUI);
    }

    private Text getEnergyInfo(Text text, boolean isUI) {
        text = ensureTextNotNull(text);
        if (getEnergyStorage() == null) return text;

        Text energyAmount = createEnergyAmountText();

        if (isUI) return text.copy().append(energyAmount.copy());
        return appendEnergyInfoWithProgressBar(text, energyAmount);
    }

    private Map<String, Integer> getItemCounts() {
        Map<String, Integer> itemCounts = new HashMap<>();

        for (ItemStack stack : this.inventory.getStacks()) {
            if (stack.isEmpty()) {
                continue;
            }

            String itemName = stack.getName().getString();
            itemCounts.merge(itemName, stack.getCount(), Integer::sum);
        }

        return itemCounts;
    }

    private Text appendItemCounts(Text text, Map<String, Integer> itemCounts, boolean isUI) {
        String separator = isUI ? " " : "\n";
        MutableText result = text.copy();

        for (Map.Entry<String, Integer> entry : itemCounts.entrySet()) {
            result = result.append(separator)
                    .append(String.valueOf(entry.getValue()))
                    .append(" ")
                    .append(entry.getKey());
        }

        return result;
    }

    private Text createEnergyAmountText() {
        return Text.literal(String.valueOf(this.energyStorage.getAmount()))
                .append("/")
                .append(String.valueOf(this.energyStorage.getCapacity()));
    }

    private Text appendEnergyInfoWithProgressBar(Text text, Text energyAmount) {
        MutableText result = text.copy()
                .append("\n\n")
                .append(ProgressBarBuilder.getProgressBar(
                        this.energyStorage.getAmount(),
                        this.energyStorage.getCapacity()));

        return result.append(Text.literal("\n")
                .append(energyAmount.copy())
                .setStyle(Style.EMPTY));
    }
}
