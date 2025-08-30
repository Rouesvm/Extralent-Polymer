package com.rouesvm.extralent.visual.text;

public class ProgressBarText {
    private static final char FIRST_FILLED = '\uE002';
    private static final char FIRST_FILLED_QUARTER = '\uE014';
    private static final char FIRST_HALF = '\uE007';
    private static final char FIRST_HALF_QUARTER = '\uE011';
    private static final char FIRST_EMPTY = '\uE001';

    private static final char MIDDLE_FILLED = '\uE004';
    private static final char MIDDLE_FILLED_QUARTER = '\uE015';
    private static final char MIDDLE_HALF = '\uE008';
    private static final char MIDDLE_HALF_QUARTER = '\uE012';
    private static final char MIDDLE_EMPTY = '\uE003';

    private static final char END_FILLED = '\uE006';
    private static final char END_FILLED_QUARTER = '\uE013';
    private static final char END_HALF = '\uE009';
    private static final char END_HALF_QUARTER = '\uE010';
    private static final char END_EMPTY = '\uE005';

    public static final char[] FIRST = {
            FIRST_FILLED, FIRST_FILLED_QUARTER, FIRST_HALF, FIRST_HALF_QUARTER, FIRST_EMPTY
    };

    public static final char[] MIDDLE = {
            MIDDLE_FILLED, MIDDLE_FILLED_QUARTER, MIDDLE_HALF, MIDDLE_HALF_QUARTER, MIDDLE_EMPTY
    };

    public static final char[] END = {
            END_FILLED, END_FILLED_QUARTER, END_HALF, END_HALF_QUARTER, END_EMPTY
    };
}
