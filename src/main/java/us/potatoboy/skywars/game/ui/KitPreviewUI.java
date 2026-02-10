package us.potatoboy.skywars.game.ui;

import eu.pb4.sgui.api.GuiHelpers;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.GuiInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import us.potatoboy.skywars.kit.Kit;

public class KitPreviewUI extends SimpleGui {
    private final KitSelectorUI selectorUI;
    private final Kit kit;
    private final @Nullable GuiInterface prev;

    public KitPreviewUI(KitSelectorUI selectorUI, Kit kit) {
        super(MenuType.GENERIC_9x3, selectorUI.getPlayer(), false);
        this.prev = GuiHelpers.getCurrentGui(player);
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
                .setName(Component.translatable("text.skywars.return_selector").setStyle(Style.EMPTY.withItalic(false)))
                .setCallback((x, y, z) -> {
                    this.player.playSound(SoundEvents.BOOK_PAGE_TURN,0.5f, 1);
                    selectorUI.open();
                    this.close();

                })
        );

        super.onOpen();
    }

    @Override
    public void onClose() {
        if (this.prev != null) {
            this.prev.open();
        }
    }
}
