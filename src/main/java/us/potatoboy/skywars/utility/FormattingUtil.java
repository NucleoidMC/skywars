package us.potatoboy.skywars.utility;

import net.minecraft.text.*;
import net.minecraft.util.Formatting;

public class FormattingUtil {
    public static final Style PREFIX_STYLE = Style.EMPTY.withColor(TextColor.fromRgb(0x858585));
    public static final Style PREFIX_SCOREBOARD_STYLE = Style.EMPTY.withColor(TextColor.fromRgb(0xd9d9d9));

    public static final Style GENERAL_STYLE = Style.EMPTY.withColor(Formatting.WHITE);
    public static final Style WIN_STYLE = Style.EMPTY.withColor(Formatting.GOLD);
    public static final Style DEATH_STYLE = Style.EMPTY.withColor(TextColor.fromRgb(0xbfbfbf));

    public static final String GENERAL_PREFIX = "»";
    public static final String DEATH_PREFIX = "☠";
    public static final String PICKAXE_PREFIX = "⛏";
    public static final String HEALTH_PREFIX = "✚";
    public static final String SUN_PREFIX = "☀";
    public static final String UMBRELLA_PREFIX = "☂";
    public static final String CLOUD_PREFIX = "☁";
    public static final String MUSIC_PREFIX = "♫";
    public static final String HEART_PREFIX = "♥";
    public static final String STAR_PREFIX = "★";
    public static final String DOT_PREFIX = "•";
    public static final String TIME_PREFIX = "⌚";
    public static final String HOURGLASS_PREFIX = "⌛";
    public static final String FLAG_PREFIX = "⚐";
    public static final String COMET_PREFIX = "☄";
    public static final String SWORD_PREFIX = "🗡";
    public static final String BOW_PREFIX = "🏹";

    public static final String CHECKMARK = "✔";
    public static final String X = "✘";


    public static MutableText format(String prefix, Style style, Text message) {
        return Text.literal(prefix + " ").setStyle(PREFIX_STYLE).append(message.copy().fillStyle(style));
    }

    public static MutableText format(String prefix, Text message) {
        return Text.literal(prefix + " ").setStyle(PREFIX_STYLE).append(message.copy());
    }

    public static MutableText formatScoreboard(String prefix, Style style, Text message) {
        return Text.literal(prefix + " ").setStyle(PREFIX_SCOREBOARD_STYLE).append(message.copy().fillStyle(style));
    }

    public static MutableText formatScoreboard(String prefix, Text message) {
        return Text.literal(prefix + " ").setStyle(PREFIX_SCOREBOARD_STYLE).append(message.copy());
    }
}
