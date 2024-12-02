package us.potatoboy.skywars;

import us.potatoboy.skywars.utility.TextUtil;
import xyz.nucleoid.plasmid.api.game.stats.StatisticKey;

public class SkywarsStatistics {
    public static final StatisticKey<Integer> ARROWS_SHOT = StatisticKey.intKey(TextUtil.id("arrows_shot"));
    public static final StatisticKey<Integer> ARROWS_HIT = StatisticKey.intKey(TextUtil.id("arrows_hit"));
}
