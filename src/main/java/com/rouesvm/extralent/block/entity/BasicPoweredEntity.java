package com.rouesvm.extralent.block.entity;

import com.rouesvm.extralent.interfaces.block.TickableBlockEntity;
import com.rouesvm.extralent.utils.ProgressBarRenderer;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import team.reborn.energy.api.base.SimpleEnergyStorage;

public class BasicPoweredEntity extends BlockEntity implements TickableBlockEntity {

    public final SimpleEnergyStorage energyStorage;

    public BasicPoweredEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.energyStorage = createEnergyStorage();
    }

    public SimpleEnergyStorage createEnergyStorage() {
        return new SimpleEnergyStorage(500000, 1000, 1000) {
            @Override
            protected void onFinalCommit() {
                super.onFinalCommit();
                markDirty();
            }
        };
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
        if (nbt.contains("energy", NbtElement.LONG_TYPE)) {
            this.energyStorage.amount = nbt.getLong("energy");
        }
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        nbt.putLong("energy", this.energyStorage.amount);
    }

    public SimpleEnergyStorage getEnergyProvider(Direction direction) {
        return this.energyStorage;
    }

    public SimpleEnergyStorage getEnergyStorage() {
        return this.energyStorage;
    }

    public Text infoOnClicked() {
        return this.getFormattedInfo();
    }

    private Text getFormattedInfo() {
        Text text = ProgressBarRenderer.getProgressBar(this.energyStorage.getAmount(), this.energyStorage.getCapacity());
        Text energyAmount = Text.literal(String.valueOf(this.energyStorage.getAmount()))
                .append("/")
                .append(String.valueOf(this.energyStorage.getCapacity()));

        return text.copy().append(Text.literal("   ")
                .append(energyAmount.copy())
                .setStyle(Style.EMPTY.withFont(Style.DEFAULT_FONT_ID))
        );
    }
}
