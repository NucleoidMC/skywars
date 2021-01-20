package us.potatoboy.skywars.game;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import xyz.nucleoid.plasmid.widget.SidebarWidget;

import java.util.HashMap;
import java.util.Map;

public class SkyWarsSidebar {
    private final SkyWarsActive game;
    public final HashMap<SkyWarsPlayer, SidebarWidget> sidebars = new HashMap<>();

    SkyWarsSidebar(SkyWarsActive game) {
        this.game = game;

        for (Object2ObjectMap.Entry<ServerPlayerEntity, SkyWarsPlayer> entry : Object2ObjectMaps.fastIterable(game.participants)) {
            SidebarWidget scoreboard = new SidebarWidget(game.gameSpace, new LiteralText("SkyWars").formatted(Formatting.BOLD));

            scoreboard.addPlayer(entry.getKey());

            sidebars.put(entry.getValue(), scoreboard);
        }
    }

    public void update(long time) {
        for (Map.Entry<SkyWarsPlayer, SidebarWidget> entry : sidebars.entrySet()) {
            SidebarWidget sidebar = entry.getValue();
            SkyWarsPlayer participant = entry.getKey();

            sidebar.set(content -> {
                long ticksUntilEnd = game.stageManager.finishTime - time;
                long ticksUntilRefill = game.stageManager.refillTime - time;
                content.writeLine(formatTime(ticksUntilEnd, "Armageddon"));
                if (game.stageManager.refills <= game.config.refills) {
                    content.writeLine("");
                    content.writeLine(formatTime(ticksUntilRefill, "Next Refill"));
                }
                content.writeLine("");
                content.writeLine("");

                content.writeLine("Kills: " + Formatting.GREEN + participant.kills);
            });
        }
    }

    public static String formatTime(long ticksUntilEnd, String label) {
        long secondsUntilEnd = ticksUntilEnd / 20;

        long minutes = secondsUntilEnd / 60;
        long seconds = secondsUntilEnd % 60;

        return String.format("%s: %s%02d:%02d", label, Formatting.GREEN, minutes, seconds);
    }

    public void close() {
        for (SidebarWidget sidebar : sidebars.values()) {
            sidebar.close();
        }
    }
}
