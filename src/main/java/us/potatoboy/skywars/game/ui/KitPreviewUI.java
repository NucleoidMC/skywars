package us.potatoboy.skywars.game.ui;

import eu.pb4.sgui.api.SguiUtils;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.GuiLike;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import us.potatoboy.skywars.kit.Kit;

public class KitPreviewUI extends SimpleGui {
    private final KitSelectorUI selectorUI;
    private final Kit kit;
    private final @Nullable GuiLike prev;

    public KitPreviewUI(KitSelectorUI selectorUI, Kit kit) {
        super(MenuType.GENERIC_9x3, selectorUI.getPlayer(), false);
        this.prev = SguiUtils.getCurrentGui(player);
        this.selectorUI = selectorUI;
        this.kit = kit;
        this.setTitle(kit.displayName());
    }

    @Override
    public void onOpen() {
        int pos = 0;
        for (ItemStack itemStack : this.kit.getItems()) {
            this.setSlot(pos++, itemStack.copy());
        }

        pos = 0;

        for (ItemStack itemStack : this.kit.getArmor()) {
            this.setSlot(9 + pos, itemStack.copy());
            pos++;
        }

        this.setSlot(this.size - 1, new GuiElementBuilder(Items.BARRIER)
                .setName(Component.translatable("text.skywars.return_selector").setStyle(Style.EMPTY.withItalic(false)))
                .setCallback((index, clickType, action, gui) -> {
                    this.player.playSound(SoundEvents.BOOK_PAGE_TURN,0.5f, 1);
                    selectorUI.open();
                    this.close();
                })
        );

        super.onOpen();
    }

    @Override
    public void onManualClose() {
        if (this.prev != null) {
            this.prev.open();
        }
    }
}
