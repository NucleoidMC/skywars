package us.potatoboy.skywars.game.ui;

import eu.pb4.sgui.api.SguiUtils;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.GuiLike;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.Identifier;
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
    private final @Nullable GuiLike prev;

    KitSelectorUI(ServerPlayer player, SkyWarsPlayer data, SkyWarsWaiting game, List<Kit> kits) {
        super(getType(kits.size()), player, kits.size() > 53);
        this.prev = SguiUtils.getCurrentGui(player);
        this.playerData = data;
        this.game = game;
        this.kits = kits;
        this.setTitle(Component.translatable("text.skywars.select_kit"));
    }

    private static MenuType<?> getType(int size) {
        if (size <= 8) {
            return MenuType.GENERIC_9x1;
        } else if (size <= 17) {
            return MenuType.GENERIC_9x2;
        } else if (size <= 26) {
            return MenuType.GENERIC_9x3;
        } else if (size <= 35) {
            return MenuType.GENERIC_9x4;
        } else if (size <= 44) {
            return MenuType.GENERIC_9x5;
        } else {
            return MenuType.GENERIC_9x6;
        }
    }


    public static void openSelector(ServerPlayer player, SkyWarsWaiting logic) {
        new KitSelectorUI(player, logic.participants.get(PlayerRef.of(player)), logic, logic.kits).open();
    }

    public static void openSelector(ServerPlayer player, SkyWarsPlayer data, List<Identifier> kits) {
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
            icon.addLoreLine(Component.translatable("text.skywars.click_select").withStyle(ChatFormatting.GRAY));
            icon.addLoreLine(Component.translatable("text.skywars.click_preview").withStyle(ChatFormatting.GRAY));
            if (kit == this.playerData.selectedKit) {
                icon.addLoreLine(Component.translatable("text.skywars.selected").withStyle(ChatFormatting.GREEN));
                icon.glow();
            }

            icon.setCallback((index, clickType, action, gui) -> {
                if (clickType.isLeft) {
                    this.player.playSound(SoundEvents.BOOK_PAGE_TURN, 0.5f, 1);
                    PlayerKitStorage.get(player).selectedKit = KitRegistry.getId(kit);
                    changeKit(this.game, this.player, this.playerData, kit);
                } else if (clickType.isRight) {
                    this.player.playSound(SoundEvents.BOOK_PAGE_TURN, 0.5f, 1);
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

    public static void changeKit(SkyWarsWaiting game, ServerPlayer player, SkyWarsPlayer playerData, Kit kit) {
        playerData.selectedKit = kit;
    }

    @Override
    public void onManualClose() {
        if (this.prev != null) {
            this.prev.open();
        }
    }
}
