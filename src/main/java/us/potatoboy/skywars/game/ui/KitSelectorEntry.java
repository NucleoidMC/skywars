package us.potatoboy.skywars.game.ui;

import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import us.potatoboy.skywars.game.SkyWarsPlayer;
import us.potatoboy.skywars.kit.Kit;
import xyz.nucleoid.plasmid.util.ItemStackBuilder;

import java.util.function.Consumer;

public final class KitSelectorEntry {
    private final ItemStackBuilder icon;
    private final Kit kit;
    private Consumer<ServerPlayerEntity> useAction;

    private KitSelectorEntry(ItemStack icon, Kit kit) {
        this.icon = ItemStackBuilder.of(icon);
        this.kit = kit;
    }

    public static KitSelectorEntry ofIcon(ItemStack icon, Kit kit) {
        return new KitSelectorEntry(icon, kit);
    }

    public static KitSelectorEntry ofIcon(ItemConvertible icon, Kit kit) {
        return new KitSelectorEntry(new ItemStack(icon), kit);
    }


    public KitSelectorEntry withName(Text name) {
        this.icon.setName(name);
        return this;
    }

    public KitSelectorEntry addLore(Text lore) {
        this.icon.addLore(lore);
        return this;
    }

    public KitSelectorEntry onUse(Consumer<ServerPlayerEntity> action) {
        this.useAction = action;
        return this;
    }

    ItemStack createIcon(SkyWarsPlayer participant) {
        ItemStack icon = this.icon.build().copy();
        icon.getTag().putByte("HideFlags", (byte) 95);

        Style style = Style.EMPTY.withItalic(false).withColor(Formatting.GOLD);

        Text name = icon.getName().shallowCopy().setStyle(style);
        icon.setCustomName(name);

        if (participant.selectedKit == kit) {
            icon.addEnchantment(null, 0);

            CompoundTag display = icon.getOrCreateSubTag("display");
            ListTag loreList = new ListTag();
            display.put("Lore", loreList);
            loreList.add(StringTag.of(Text.Serializer.toJson(new TranslatableText("skywars.text.selected"))));
        }

        return icon;
    }

    void onClick(ServerPlayerEntity player) {
        SoundEvent sound;
        if (this.useAction != null) {
            this.useAction.accept(player);
        }
        sound = SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP;
        icon.addEnchantment(Enchantments.UNBREAKING, 1);

        player.playSound(sound, SoundCategory.MASTER, 1.0F, 1.0F);
    }
}
