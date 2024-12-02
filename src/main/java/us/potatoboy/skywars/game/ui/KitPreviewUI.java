package us.potatoboy.skywars.game.ui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import us.potatoboy.skywars.kit.Kit;

public class KitPreviewUI extends SimpleGui {
    private final KitSelectorUI selectorUI;
    private final Kit kit;

    public KitPreviewUI(KitSelectorUI selectorUI, Kit kit) {
        super(ScreenHandlerType.GENERIC_9X3, selectorUI.getPlayer(), false);
        this.selectorUI = selectorUI;
        this.kit = kit;
        this.setTitle(kit.displayName());
    }

    @Override
    public void onOpen() {
        int pos = 0;

        for (ItemStack itemStack : this.kit.items) {
            this.setSlot(pos++, itemStack.copy());
        }

        pos = 0;

        for (ItemStack itemStack : this.kit.armor) {
            this.setSlot(9 + pos, itemStack.copy());
            pos++;
        }

        this.setSlot(this.size - 1, new GuiElementBuilder(Items.BARRIER)
                .setName(Text.translatable("text.skywars.return_selector").setStyle(Style.EMPTY.withItalic(false)))
                .setCallback((x, y, z) -> {
                    this.player.playSound(SoundEvents.ITEM_BOOK_PAGE_TURN, 0.5f, 1);
                    selectorUI.open();
                    this.close();

                })
        );

        super.onOpen();
    }
}
