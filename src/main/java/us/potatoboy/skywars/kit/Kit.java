package us.potatoboy.skywars.kit;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import xyz.nucleoid.codecs.MoreCodecs;
import xyz.nucleoid.plasmid.api.util.ItemStackBuilder;

import java.util.ArrayList;
import java.util.List;

public class Kit {
    public static final Codec<Kit> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("kit_name").forGetter(kit -> kit.name),
            MoreCodecs.ITEM_STACK.fieldOf("icon").forGetter(kit -> kit.icon),
            Codec.list(MoreCodecs.ITEM_STACK).fieldOf("armor").forGetter(kit -> kit.armor),
            Codec.list(MoreCodecs.ITEM_STACK).fieldOf("items").forGetter(kit -> kit.items),
            Codec.list(Cooldown.CODEC).optionalFieldOf("cooldowns", new ArrayList<>()).forGetter(kit -> kit.cooldowns)
    ).apply(instance, Kit::new));

    public final String name;
    public final ItemStack icon;
    public final List<ItemStack> armor;
    public final List<ItemStack> items;
    public final List<Cooldown> cooldowns;

    public Kit(String name, ItemStack icon, List<ItemStack> armor, List<ItemStack> items, List<Cooldown> cooldowns) {
        this.name = name;
        this.icon = icon;
        this.armor = armor;
        this.items = items;
        this.cooldowns = cooldowns;
    }

    public MutableComponent displayName() {
        return Component.translatable("skywars.kit." + name);
    }

    public void equipPlayer(ServerPlayer player) {
        for (ItemStack itemStack : this.items) {
            player.getInventory().add(ItemStackBuilder.of(itemStack).build());
        }

        player.setItemSlot(EquipmentSlot.HEAD, ItemStackBuilder.of(this.armor.get(0)).build());
        player.setItemSlot(EquipmentSlot.CHEST, ItemStackBuilder.of(this.armor.get(1)).build());
        player.setItemSlot(EquipmentSlot.LEGS, ItemStackBuilder.of(this.armor.get(2)).build());
        player.setItemSlot(EquipmentSlot.FEET, ItemStackBuilder.of(this.armor.get(3)).build());

        for (Cooldown cooldown : cooldowns) {
            player.getCooldowns().addCooldown(cooldown.identifier, cooldown.durationSec * 20);
        }
    }

    public static class Cooldown {
        public static final Codec<Cooldown> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Identifier.CODEC.fieldOf("id").forGetter(cooldown -> cooldown.identifier),
                Codec.INT.fieldOf("duration").forGetter(cooldown -> cooldown.durationSec)
        ).apply(instance, Cooldown::new));

        public final Identifier identifier;
        public final int durationSec;

        public Cooldown(Identifier identifier, int durationSec) {
            this.identifier = identifier;
            this.durationSec = durationSec;
        }
    }
}
