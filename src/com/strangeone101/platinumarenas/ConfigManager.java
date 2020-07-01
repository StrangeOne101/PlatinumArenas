package com.strangeone101.platinumarenas;

public class ConfigManager {

    /**
     * How many blocks to analyze/count before waiting a bit before continuing.
     */
    public static final int BLOCKS_ANALYZED_PER_SECOND = 40_000;

    /**
     * How many blocks can be per section in arenas. Max is 2,147,483,647
     */
    public static final int BLOCKS_PER_SECTION = 10000; //2_097_152;

    @Deprecated
    public static final int BLOCKS_RESET_PER_SECOND = 20_000;

    public static final int BLOCKS_RESET_PER_SECOND_VERYSLOW = 10 * 20;
    public static final int BLOCKS_RESET_PER_SECOND_SLOW = 50 * 20;
    public static final int BLOCKS_RESET_PER_SECOND_NORMAL = 500 * 20;
    public static final int BLOCKS_RESET_PER_SECOND_FAST = 2000 * 20;
    public static final int BLOCKS_RESET_PER_SECOND_VERYFAST = 5000 * 20;
    public static final int BLOCKS_RESET_PER_SECOND_EXTREME = 10000 * 20;
}
