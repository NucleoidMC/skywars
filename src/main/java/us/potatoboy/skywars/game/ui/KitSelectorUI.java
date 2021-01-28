package us.potatoboy.skywars.game.ui;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import us.potatoboy.skywars.game.SkyWarsWaiting;

import java.util.function.Consumer;

public final class KitSelectorUI implements NamedScreenHandlerFactory {
    private final Text title;
    private final Consumer<KitSelectorBuilder> builder;
    private final SkyWarsWaiting game;

    KitSelectorUI(Text title, Consumer<KitSelectorBuilder> builder, SkyWarsWaiting game) {
        this.title = title;
        this.builder = builder;
        this.game = game;
    }

    public static KitSelectorUI create(Text title, Consumer<KitSelectorBuilder> builder, SkyWarsWaiting game) {
        return new KitSelectorUI(title, builder, game);
    }

    public Text getDisplayName() {
        return this.title;
    }

    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        final ServerPlayerEntity serverPlayer = (ServerPlayerEntity)player;
        KitSelectorInventory inventory = new KitSelectorInventory(serverPlayer, game.participants.get(player), this.builder);
        return new GenericContainerScreenHandler(ScreenHandlerType.GENERIC_9X3, syncId, playerInventory, inventory, 3) {
            public ItemStack transferSlot(PlayerEntity player, int invSlot) {
                this.resendInventory();
                return ItemStack.EMPTY;
            }

            public ItemStack onSlotClick(int slot, int data, SlotActionType action, PlayerEntity player) {
                if (action != SlotActionType.SWAP && action != SlotActionType.THROW && action != SlotActionType.CLONE) {
                    return super.onSlotClick(slot, data, action, player);
                } else {
                    this.resendInventory();
                    return ItemStack.EMPTY;
                }
            }

            private void resendInventory() {
                serverPlayer.onHandlerRegistered(this, this.getStacks());
            }
        };
    }
}
