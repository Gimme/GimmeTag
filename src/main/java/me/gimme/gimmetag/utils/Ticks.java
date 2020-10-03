package me.gimme.gimmetag.utils;

import java.text.DecimalFormat;

/**
 * Contains utility functions for working with server ticks as a time unit.
 */
public abstract class Ticks {

    public static final int TICKS_PER_SECOND = 20; // Minecraft standard amount of server ticks per second
    private static final DecimalFormat DF = new DecimalFormat("#.##"); // Examples: 0.01, 1, 1.5
    private static final DecimalFormat DF_FORCE_DECIMALS = new DecimalFormat("0.00"); // Examples: 0.01, 1.00, 1.50

    /**
     * @param seconds The time in seconds to convert to ticks
     * @return the amount of ticks that most closely represents the specified time in seconds
     */
    public static int secondsToTicks(double seconds) {
        return (int) Math.round(seconds * TICKS_PER_SECOND);
    }

    /**
     * @param ticks The amount of ticks to convert to time in seconds
     * @return the time in seconds that equals the specified time in ticks
     */
    public static double ticksToSeconds(int ticks) {
        return ticks / (double) TICKS_PER_SECOND;
    }

    /**
     * Returns a String with the exact time in seconds that equals the specified time in ticks.
     * <p>
     * Examples: 0.01, 1, 1.5
     *
     * @param ticks The amount of ticks to convert to time in seconds
     * @return a String with the exact time in seconds that equals the specified time in ticks
     */
    public static String ticksToSecondsString(int ticks) {
        return ticksToSecondsString(ticks, false);
    }

    /**
     * Returns a String with the exact time in seconds that equals the specified time in ticks.
     * <p>
     * Examples: 0.01, 1, 1.5 or 0.01, 1.00, 1.50 with forceTwoDecimals
     *
     * @param ticks            The amount of ticks to convert to time in seconds
     * @param forceTwoDecimals If the resulting String should always contain exactly two decimals (e.g., 0.00 instead of 0)
     * @return a String with the exact time in seconds that equals the specified time in ticks
     */
    public static String ticksToSecondsString(int ticks, boolean forceTwoDecimals) {
        return (forceTwoDecimals ? DF_FORCE_DECIMALS : DF).format(ticksToSeconds(ticks));
    }
}
