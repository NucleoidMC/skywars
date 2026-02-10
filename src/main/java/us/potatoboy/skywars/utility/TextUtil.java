package us.potatoboy.skywars.utility;


import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import us.potatoboy.skywars.SkyWars;
import xyz.nucleoid.plasmid.api.game.common.team.GameTeam;

public class TextUtil {
    public static MutableComponent getText(String type, String path, Object... values) {
        return Component.translatable(Util.makeDescriptionId(type, Identifier.fromNamespaceAndPath(SkyWars.ID, path)), values);
    }

    public static MutableComponent getTeamText(GameTeam team) {
        return getText("general", "team", team.config().name()).setStyle(Style.EMPTY.withColor(team.config().dyeColor()));
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(SkyWars.ID, path);
    }
}
