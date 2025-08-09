package com.rouesvm.extralent.visual.text;

import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ProgressBarText {
    private static final char FIRST_FILLED = '\uE002';
    private static final char FIRST_HALF = '\uE007';
    private static final char FIRST_EMPTY = '\uE001';

    private static final char MIDDLE_FILLED = '\uE004';
    private static final char MIDDLE_HALF = '\uE008';
    private static final char MIDDLE_EMPTY = '\uE003';

    private static final char END_FILLED = '\uE006';
    private static final char END_HALF = '\uE009';
    private static final char END_EMPTY = '\uE005';

    private static final char FIRST_FILLEDHALFHALF = '\uE014';
    private static final char MIDDLE_FILLEDHALFHALF = '\uE015';
    private static final char END_FILLEDHALFHALF = '\uE013';

    private static final char FIRST_HALFHALF = '\uE011';
    private static final char MIDDLE_HALFHALF = '\uE012';
    private static final char END_HALFHALF = '\uE010';

    private static final char NEGATIVE_SPACE = '\uF801';

    private static final int MAX_SIZE = 10;
    private static final int MAX_PERCENTAGE_SIZE = 40;

    private static final Text[] CACHE = new Text[MAX_PERCENTAGE_SIZE + 1];

    static {
        for (int i = 0; i <= MAX_PERCENTAGE_SIZE; i++) {
            CACHE[i] = Text.literal(getEnergyUnicode(i))
                    .setStyle(Style.EMPTY.withFont(Identifier.of("extralent", "energybar")));
        }
    }

    public static Text getProgressBar(long energyAmount, long maxEnergy) {
        int percentage = Math.max(0, Math.min(
                Math.round((float) energyAmount / maxEnergy * MAX_PERCENTAGE_SIZE),
                MAX_PERCENTAGE_SIZE
        ));
        return CACHE[percentage];
    }

    private static String getEnergyUnicode(long percentage) {
        float clamped = Math.min(Math.max(percentage, 0), MAX_PERCENTAGE_SIZE);
        char[] chars = new char[MAX_SIZE * 2];
        int index = 0;

        for (int i = 0; i < MAX_SIZE; i++) {
            DRAW draw = getIcon2Draw(i, clamped);
            char character = getCharacterForPosition(draw, i);
            chars[index++] = character;
            chars[index++] = NEGATIVE_SPACE;
        }

        return new String(chars);
    }

    private static DRAW getIcon2Draw(int position, float modifiedAmount) {
        float fillPerSlot = MAX_PERCENTAGE_SIZE / (float) MAX_SIZE;
        float bar = (modifiedAmount - position * fillPerSlot) / fillPerSlot;

        return bar >= 1 ? DRAW.FILLED
                : bar > 0.75 ? DRAW.FILLED_HALF
                : bar > 0.5 ? DRAW.HALF
                : bar > 0.25 ? DRAW.HALF_FILLED
                : DRAW.EMPTY;
    }

    private static char getCharacterForPosition(DRAW draw, int position) {
        boolean isFirst = position == 0;
        boolean isLast = position == MAX_SIZE - 1;

        return getIconForDRAW(
                draw,
                isFirst ? FIRST_FILLED     : isLast ? END_FILLED     : MIDDLE_FILLED,
                isFirst ? FIRST_FILLEDHALFHALF : isLast ? END_FILLEDHALFHALF : MIDDLE_FILLEDHALFHALF,
                isFirst ? FIRST_HALF       : isLast ? END_HALF       : MIDDLE_HALF,
                isFirst ? FIRST_HALFHALF   : isLast ? END_HALFHALF   : MIDDLE_HALFHALF,
                isFirst ? FIRST_EMPTY      : isLast ? END_EMPTY      : MIDDLE_EMPTY
        );
    }

    private static char getIconForDRAW(DRAW draw, char filled, char full_half, char half, char half_full, char empty) {
        switch (draw) {
            case EMPTY -> {
                return empty;
            } case HALF_FILLED -> {
                return half_full;
            } case HALF -> {
                return half;
            } case FILLED_HALF -> {
                return full_half;
            } case FILLED -> {
                return filled;
            } default -> {
                return empty;
            }
        }
    }

    private enum DRAW {
        FILLED,
        FILLED_HALF,
        HALF,
        HALF_FILLED,
        EMPTY
    }
}
