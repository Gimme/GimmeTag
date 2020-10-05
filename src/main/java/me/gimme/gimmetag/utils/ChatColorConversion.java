package me.gimme.gimmetag.utils;

import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

public abstract class ChatColorConversion {
    private ChatColorConversion() {
    }

    /**
     * Returns the RGB value of the specified chat color.
     * <p>
     * Silently returns black if the specified chat color is not supported.
     *
     * @param chatColor the chat color to get the RGB value of
     * @return the RGB value of the specified chat color
     */
    public static int toRGB(@NotNull ChatColor chatColor) {
        switch (chatColor) {
            default:
            case BLACK:
                return 0x000000;
            case DARK_BLUE:
                return 0x0000AA;
            case DARK_GREEN:
                return 0x00AA00;
            case DARK_AQUA:
                return 0x00AAAA;
            case DARK_RED:
                return 0xAA0000;
            case DARK_PURPLE:
                return 0xAA00AA;
            case GOLD:
                return 0xFFAA00;
            case GRAY:
                return 0xAAAAAA;
            case DARK_GRAY:
                return 0x555555;
            case BLUE:
                return 0x5555FF;
            case GREEN:
                return 0x55FF55;
            case AQUA:
                return 0x55FFFF;
            case RED:
                return 0xFF5555;
            case LIGHT_PURPLE:
                return 0xFF55FF;
            case YELLOW:
                return 0xFFFF55;
            case WHITE:
                return 0xFFFFFF;
        }
    }
}
