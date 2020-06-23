package com.strangeone101.platinumarenas;

public class ConfigManager {

    /**
     * How many blocks to analyze/count before waiting a bit before continuing.
     */
    public static final int BLOCKS_ANALYZED_PER_SECOND = 40_000;

    /**
     * How many blocks can be per section in arenas. Max is 2,147,483,647
     */
    public static final int BLOCKS_PER_SECTION = 2_097_152;

    public static final int BLOCKS_RESET_PER_SECOND = 20_000;
}
