package com.rouesvm.extralent.utils;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ProgressBarRenderer {
    private static final char FIRST_FILLED = '\uE002';
    private static final char FIRST_HALF = '\uE007';
    private static final char FIRST_EMPTY = '\uE001';

    private static final char MIDDLE_FILLED = '\uE004';
    private static final char MIDDLE_HALF = '\uE008';
    private static final char MIDDLE_EMPTY = '\uE003'
            ;
    private static final char END_FILLED = '\uE006';
    private static final char END_HALF = '\uE009';
    private static final char END_EMPTY = '\uE005';

    private static final char NEGATIVE_SPACE = '\uF801';

    private static final long maxSize = 10;
    private static final long maxPercentageSize = 20;

    public static Text getProgressBar(long energyAmount, long maxEnergy) {
        long percentage = Math.round((float) energyAmount / maxEnergy * maxPercentageSize);
        return Text.literal(getEnergyUnicode(percentage)).setStyle(
                Style.EMPTY.withFont(Identifier.of("extralent", "energybar"))
        );
    }

    private static String getEnergyUnicode(long percentage) {
        StringBuilder progressBar = new StringBuilder();

        float modifiedAmount = Math.max(0, Math.min(percentage, 20));
        for (int i = 0; i < maxSize; i++) {
            DRAW draw = getIcon2Draw(i, modifiedAmount);
            char character = getCharacterForPosition(draw, i);
            progressBar.append(character).append(NEGATIVE_SPACE);
        }

        return progressBar.toString();
    }

    private static DRAW getIcon2Draw(int position, float modifiedAmount) {
        float effectiveEnergyBar = (modifiedAmount / 2.0F) - position;

        if (effectiveEnergyBar >= 1)
            return DRAW.FILLED;
        else if (effectiveEnergyBar > .5)
            return DRAW.HALF;
        else if (effectiveEnergyBar > .25)
            return DRAW.HALF;
        else return DRAW.EMPTY;
    }

    private static char getCharacterForPosition(DRAW draw, int position) {
        if (position == 0) {
            return getIconForDRAW(draw, FIRST_FILLED, FIRST_HALF, FIRST_EMPTY);
        } else if (position == maxSize - 1) {
            return getIconForDRAW(draw, END_FILLED, END_HALF, END_EMPTY);
        } else {
            return getIconForDRAW(draw, MIDDLE_FILLED, MIDDLE_HALF, MIDDLE_EMPTY);
        }
    }

    private static char getIconForDRAW(DRAW draw, char filled, char half, char empty) {
        switch (draw) {
            case EMPTY -> {
                return empty;
            } case HALF -> {
                return half;
            } case FILLED -> {
                return filled;
            } default -> {
                return empty;
            }
        }
    }

    private enum DRAW {
        FILLED,
        HALF,
        EMPTY
    }
}
