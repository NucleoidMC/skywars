package us.potatoboy.skywars.game.ui;

import eu.pb4.sgui.api.GuiHelpers;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.GuiInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import us.potatoboy.skywars.game.SkyWarsPlayer;
import us.potatoboy.skywars.game.SkyWarsWaiting;
import us.potatoboy.skywars.kit.Kit;
import us.potatoboy.skywars.kit.KitRegistry;
import us.potatoboy.skywars.kit.PlayerKitStorage;
import xyz.nucleoid.plasmid.api.util.PlayerRef;

import java.util.ArrayList;
import java.util.List;

public final class KitSelectorUI extends SimpleGui {
    private final SkyWarsPlayer playerData;
    private final SkyWarsWaiting game;
    private final List<Kit> kits;
    private final @Nullable GuiInterface prev;

    KitSelectorUI(ServerPlayerEntity player, SkyWarsPlayer data, SkyWarsWaiting game, List<Kit> kits) {
        super(getType(kits.size()), player, kits.size() > 53);
        this.prev = GuiHelpers.getCurrentGui(player);
        this.playerData = data;
        this.game = game;
        this.kits = kits;
        this.setTitle(Text.translatable("text.skywars.select_kit"));
    }

    private static ScreenHandlerType<?> getType(int size) {
        if (size <= 8) {
            return ScreenHandlerType.GENERIC_9X1;
        } else if (size <= 17) {
            return ScreenHandlerType.GENERIC_9X2;
        } else if (size <= 26) {
            return ScreenHandlerType.GENERIC_9X3;
        } else if (size <= 35) {
            return ScreenHandlerType.GENERIC_9X4;
        } else if (size <= 44) {
            return ScreenHandlerType.GENERIC_9X5;
        } else {
            return ScreenHandlerType.GENERIC_9X6;
        }
    }


    public static void openSelector(ServerPlayerEntity player, SkyWarsWaiting logic) {
        new KitSelectorUI(player, logic.participants.get(PlayerRef.of(player)), logic, logic.kits).open();
    }

    public static void openSelector(ServerPlayerEntity player, SkyWarsPlayer data, List<Identifier> kits) {
        var kitsList = new ArrayList<Kit>();

        for (Identifier id : kits) {
            Kit kit = KitRegistry.get(id);
            if (kit != null) {
                kitsList.add(kit);
            }
        }

        new KitSelectorUI(player, data, null, kitsList).open();
    }

    @Override
    public void onOpen() {
        int pos = 0;

        for (Kit kit : this.kits) {
            var icon = GuiElementBuilder.from(kit.icon);
            icon.setName(kit.displayName());
            icon.hideDefaultTooltip();
            icon.addLoreLine(Text.translatable("text.skywars.click_select").formatted(Formatting.GRAY));
            icon.addLoreLine(Text.translatable("text.skywars.click_preview").formatted(Formatting.GRAY));
            if (kit == this.playerData.selectedKit) {
                icon.addLoreLine(Text.translatable("text.skywars.selected").formatted(Formatting.GREEN));
                icon.glow();
            }

            icon.setCallback((index, clickType, action) -> {
                if (clickType.isLeft) {
                    this.player.playSoundToPlayer(SoundEvents.ITEM_BOOK_PAGE_TURN, SoundCategory.MASTER, 0.5f, 1);
                    PlayerKitStorage.get(player).selectedKit = KitRegistry.getId(kit);
                    changeKit(this.game, this.player, this.playerData, kit);
                } else if (clickType.isRight) {
                    this.player.playSoundToPlayer(SoundEvents.ITEM_BOOK_PAGE_TURN, SoundCategory.MASTER, 0.5f, 1);
                    new KitPreviewUI(this, kit).open();
                    this.close();
                }
                this.onOpen();
            });

            this.setSlot(pos, icon);
            pos++;
        }

        super.onOpen();
    }

    public static void changeKit(SkyWarsWaiting game, ServerPlayerEntity player, SkyWarsPlayer playerData, Kit kit) {
        playerData.selectedKit = kit;
    }

    @Override
    public void onClose() {
        if (this.prev != null) {
            this.prev.open();
        }
    }
}
