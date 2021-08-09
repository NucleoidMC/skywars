package us.potatoboy.skywars.utility;


import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import us.potatoboy.skywars.SkyWars;
import xyz.nucleoid.plasmid.game.common.team.GameTeam;

public class TextUtil {
    public static MutableText getText(String type, String path, Object... values) {
        return new TranslatableText(Util.createTranslationKey(type, new Identifier(SkyWars.ID, path)), values);
    }

    public static MutableText getTeamText(GameTeam team) {
        return getText("general", "team", team.display()).setStyle(Style.EMPTY.withColor(team.color()));
    }

    public static Identifier id(String path) {
        return new Identifier(SkyWars.ID, path);
    }
}
