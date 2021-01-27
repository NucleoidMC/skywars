package us.potatoboy.skywars.kit;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import xyz.nucleoid.plasmid.game.player.GameTeam;
import xyz.nucleoid.plasmid.util.ItemStackBuilder;

import java.util.List;

public class Kit {
    public static final Codec<Kit> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("kit_name").forGetter(kit -> kit.name),
            ItemStack.CODEC.fieldOf("icon").forGetter(kit -> kit.icon),
            Codec.list(ItemStack.CODEC).fieldOf("armor").forGetter(kit -> kit.armor),
            Codec.list(ItemStack.CODEC).fieldOf("items").forGetter(kit -> kit.items)
    ).apply(instance, Kit::new));

    public final String name;
    public final ItemStack icon;
    public final List<ItemStack> armor;
    public final List<ItemStack> items;

    public Kit(String name, ItemStack icon, List<ItemStack> armor, List<ItemStack> items) {
        this.name = name;
        this.icon = icon;
        this.armor = armor;
        this.items = items;
    }


    public void equipPlayer(ServerPlayerEntity player) {
        for (ItemStack itemStack : this.items) {
            player.inventory.insertStack(ItemStackBuilder.of(itemStack).build());
        }

        player.equipStack(EquipmentSlot.HEAD, ItemStackBuilder.of(this.armor.get(0)).build());
        player.equipStack(EquipmentSlot.CHEST, ItemStackBuilder.of(this.armor.get(1)).build());
        player.equipStack(EquipmentSlot.LEGS, ItemStackBuilder.of(this.armor.get(2)).build());
        player.equipStack(EquipmentSlot.FEET, ItemStackBuilder.of(this.armor.get(3)).build());
    }
}
