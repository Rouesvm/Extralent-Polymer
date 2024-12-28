package com.rouesvm.extralent.item.custom;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import team.reborn.energy.api.base.SimpleEnergyItem;

import java.util.List;

public interface BasicEnergyItem extends SimpleEnergyItem {
    default void addEnergyTooltip(List<Text> tooltip, ItemStack stack) {
        tooltip.add(Text.translatable("general.info.stored_energy")
                .append(" ")
                .append(String.valueOf(getStoredEnergy(stack)))
                .setStyle(Style.EMPTY.withColor(Formatting.YELLOW)));
    }

    default boolean shouldPass(@NotNull ItemStack stack, PlayerEntity player, boolean showMessage) {
        if (getStoredEnergy(stack) <= getEnergyCost()) {
            if (showMessage) player.sendMessage(Text.translatable("general.info.out_of_energy")
                    .setStyle(Style.EMPTY.withColor(Formatting.RED)), true);
            onLowEnergy(stack, player);
            return true;
        }

        return false;
    }

    long getEnergyCost();
    void onLowEnergy(ItemStack stack, PlayerEntity player);
}
