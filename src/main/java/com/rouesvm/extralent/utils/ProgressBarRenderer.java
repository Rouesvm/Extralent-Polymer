package com.rouesvm.extralent.utils;

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
        System.out.print(energyAmount);
        long percentage = Math.round((float) energyAmount / maxEnergy * maxPercentageSize);
        return Text.literal(getEnergyUnicode(percentage)).setStyle(
                Style.EMPTY.withFont(Identifier.of("extralent", "energybar"))
        );
    }

    private static String getEnergyUnicode(long percentage) {
        StringBuilder progressBar = new StringBuilder();

        for (int i = 0; i <= maxSize; i++) {
            char character = getCharacterForPosition(i, percentage);
            progressBar.append(character).append(NEGATIVE_SPACE);
        }

        return progressBar.toString();
    }

    private static DRAW getIcon2Draw(long position, long percentage) {
        long filledPosition = Math.round(((float) percentage / maxPercentageSize * maxSize));

        if (position < filledPosition) {
            if (percentage % 2 == 1 && position == filledPosition - 1)
                return DRAW.HALF;
            return DRAW.FILLED;
        }
        return DRAW.EMPTY;    }

    private static char getCharacterForPosition(long position, long percentage) {
        DRAW draw = getIcon2Draw(position, percentage);
        if (position == 0) {
            return getIconForDRAW(draw, FIRST_FILLED, FIRST_HALF, FIRST_EMPTY);
        } else if (position == maxSize) {
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

    public enum DRAW {
        FILLED,
        HALF,
        EMPTY
    }
}
