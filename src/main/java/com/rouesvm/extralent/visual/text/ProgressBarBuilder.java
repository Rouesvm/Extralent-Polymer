package com.rouesvm.extralent.visual.text;

import net.minecraft.text.Style;
import net.minecraft.text.StyleSpriteSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ProgressBarBuilder {
    private static final char NEGATIVE_SPACE = '\uF801';
    private static final char[][] PROGRESS_PART = {ProgressBarText.FIRST, ProgressBarText.MIDDLE, ProgressBarText.END};

    private static final int MAX_LENGTH = 10;

    private static final int QUARTERS_PER_SEGMENT = 4;
    private static final int MAX_PERCENTAGE_SIZE = MAX_LENGTH * QUARTERS_PER_SEGMENT;

    private static final int SPACING_MULTIPLIER = 2;

    private static final Style EXTRALENT_FONT = Style.EMPTY.withFont(new StyleSpriteSource.Font(Identifier.of("extralent", "energy_bar")));

    private static final Text[] CACHE = new Text[MAX_PERCENTAGE_SIZE + 1];
    static {
        for (int i = 0; i <= MAX_PERCENTAGE_SIZE; i++) {
            CACHE[i] = Text.literal(buildProgressBar(i)).setStyle(EXTRALENT_FONT);
        }
    }

    public static Text getProgressBar(float energyAmount, float maxEnergy) {
        int percentage = Math.max(0, Math.min(
                Math.round(energyAmount / maxEnergy * MAX_PERCENTAGE_SIZE),
                MAX_PERCENTAGE_SIZE
        ));

        return CACHE[percentage];
    }

    private static String buildProgressBar(float percentage) {
        char[] chars = new char[MAX_LENGTH * SPACING_MULTIPLIER];

        for (int i = 0; i < MAX_LENGTH; i++) {
            char character = getCharacterForPosition(getEnumToDraw(i, percentage), getPositionIndex(i));
            chars[i * SPACING_MULTIPLIER] = character;
            chars[i * SPACING_MULTIPLIER + 1] = NEGATIVE_SPACE;
        }

        return new String(chars);
    }

    private static DRAW getEnumToDraw(int position, float modifiedAmount) {
        float filledLevel = (modifiedAmount - position * QUARTERS_PER_SEGMENT) / QUARTERS_PER_SEGMENT;
        return DRAW.fromFillLevel(filledLevel);
    }

    private static int getPositionIndex(int i) {
        boolean isFirst = i <= 0;
        boolean isLast = i >= MAX_LENGTH - 1;

        if (isFirst) return 0;
        if (isLast) return 2;
        return 1;
    }

    private static char getCharacterForPosition(DRAW draw, int position) {
        return PROGRESS_PART[position][draw.ordinal()];
    }

    private enum DRAW {
        FILLED(1F),
        FILLED_QUARTER(0.75F),
        HALF(0.5F),
        HALF_QUARTER(0.25F),
        EMPTY(0);

        private final float threshold;

        DRAW(float threshold) {
            this.threshold = threshold;
        }

        static DRAW fromFillLevel(float fillLevel) {
            for (DRAW draw : values()) {
                if (fillLevel >= draw.threshold) {
                    return draw;
                }
            }
            return EMPTY;
        }
    }
}
