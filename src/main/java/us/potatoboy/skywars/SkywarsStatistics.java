package us.potatoboy.skywars;

import us.potatoboy.skywars.utility.ComponentUtil;
import xyz.nucleoid.plasmid.api.game.stats.StatisticKey;

public class SkywarsStatistics {
    public static final StatisticKey<Integer> ARROWS_SHOT = StatisticKey.intKey(ComponentUtil.id("arrows_shot"));
    public static final StatisticKey<Integer> ARROWS_HIT = StatisticKey.intKey(ComponentUtil.id("arrows_hit"));
}
