package us.potatoboy.skywars.game;

import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import us.potatoboy.skywars.SkyWars;
import us.potatoboy.skywars.game.ui.KitSelectorBuilder;
import us.potatoboy.skywars.game.ui.KitSelectorEntry;
import us.potatoboy.skywars.game.ui.KitSelectorUI;
import us.potatoboy.skywars.kit.Kit;
import us.potatoboy.skywars.kit.KitRegistry;

import java.util.Collection;
import java.util.stream.Collectors;

public class SkyWarsKitSelector {
    public static void openSelector(ServerPlayerEntity player, SkyWarsPlayer participant, SkyWarsWaiting game) {
        KitSelectorUI selector = KitSelectorUI.create(new TranslatableText("skywars.text.select_kit"), ui -> {
            ui.add(KitSelectorEntry.ofIcon(Items.BARRIER, null)
                    .withName(new TranslatableText("skywars.kit.none"))
                    .onUse(p -> {
                        selectKit(game, player, participant, null);
                    }));

            game.config.kits.ifLeft(kits -> {
                addKits(kits.stream().map(identifier -> KitRegistry.get(identifier)).collect(Collectors.toList()), ui, player, participant, game);
            });

            game.config.kits.ifRight(allKits -> {
                if (allKits) {
                    addKits(KitRegistry.getKITS().values(), ui, player, participant, game);
                }
            });
        }, game);

        player.openHandledScreen(selector);
    }

    private static void addKits(Collection<Kit> kits, KitSelectorBuilder ui, ServerPlayerEntity player, SkyWarsPlayer participant, SkyWarsWaiting game) {
        for (Kit kit : kits) {

            if (kit != null) {
                ui.add(KitSelectorEntry.ofIcon(kit.icon, kit)
                        .withName(new TranslatableText("skywars.kit." + kit.name))
                        .onUse(p -> {
                            selectKit(game, player, participant, kit);
                        }));
            }
        }
    }

    public static void selectKit(SkyWarsWaiting game, ServerPlayerEntity player, SkyWarsPlayer participant, Kit kit) {
        participant.selectedKit = kit;
        SkyWars.KIT_STORAGE.putPlayerKit(player.getUuid(), KitRegistry.getId(kit));
    }
}
