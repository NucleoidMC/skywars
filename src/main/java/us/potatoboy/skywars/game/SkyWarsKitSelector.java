package us.potatoboy.skywars.game;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import us.potatoboy.skywars.game.ui.KitSelectorEntry;
import us.potatoboy.skywars.game.ui.KitSelectorUI;
import us.potatoboy.skywars.kit.Kit;
import us.potatoboy.skywars.kit.KitRegistry;

public class SkyWarsKitSelector {
    public static void openSelector(ServerPlayerEntity player, SkyWarsPlayer dtmPlayer, SkyWarsWaiting game) {
        KitSelectorUI selector = KitSelectorUI.create(new TranslatableText("skywars.text.select_kit"), ui -> {
            game.config.kits.ifLeft(kits -> {
                for (Identifier id : kits) {
                    Kit kit = KitRegistry.get(id);

                    if (kit != null) {
                        ui.add(KitSelectorEntry.ofIcon(kit.icon, kit)
                                .withName(new TranslatableText("skywars.kit." + kit.name))
                                .onUse(p -> {
                                    changeKit(game, player, dtmPlayer, kit);
                                }));
                    }
                }
            });

            game.config.kits.ifRight(allKits -> {
                if (allKits) {
                    for (Kit kit : KitRegistry.getKITS().values()) {

                        if (kit != null) {
                            ui.add(KitSelectorEntry.ofIcon(kit.icon, kit)
                                    .withName(new TranslatableText("skywars.kit." + kit.name))
                                    .onUse(p -> {
                                        changeKit(game, player, dtmPlayer, kit);
                                    }));
                        }
                    }
                }
            });
        }, game);

        player.openHandledScreen(selector);
    }

    public static void changeKit(SkyWarsWaiting game, ServerPlayerEntity player, SkyWarsPlayer participant, Kit kit) {
        participant.selectedKit = kit;

    }
}
