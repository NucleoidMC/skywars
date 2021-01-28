package us.potatoboy.skywars.game.ui;

import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class KitSelectorBuilder {
    final List<KitSelectorEntry> elements = new ArrayList();

    public KitSelectorBuilder() {
    }

    public KitSelectorBuilder add(KitSelectorEntry entry) {
        this.elements.add(entry);
        return this;
    }
}
