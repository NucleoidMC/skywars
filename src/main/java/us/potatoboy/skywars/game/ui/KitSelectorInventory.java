package us.potatoboy.skywars.game.ui;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.MathHelper;
import us.potatoboy.skywars.game.SkyWarsPlayer;

import java.util.Arrays;
import java.util.function.Consumer;

public final class KitSelectorInventory implements Inventory {
    private static final int WIDTH = 9;
    private static final int PADDING = 0;
    private static final int PADDED_WIDTH = 0;
    private final KitSelectorEntry[] elements = new KitSelectorEntry[this.size()];
    private final ServerPlayerEntity player;
    private final SkyWarsPlayer participant;
    private final Consumer<KitSelectorBuilder> builder;

    KitSelectorInventory(ServerPlayerEntity player, SkyWarsPlayer participant, Consumer<KitSelectorBuilder> builder) {
        this.player = player;
        this.participant = participant;
        this.builder = builder;
        this.buildGrid();
    }

    private void buildGrid() {
        KitSelectorBuilder builder = new KitSelectorBuilder();
        this.builder.accept(builder);
        this.buildGrid((KitSelectorEntry[])builder.elements.toArray(new KitSelectorEntry[0]));
    }

    private void buildGrid(KitSelectorEntry[] elements) {
        Arrays.fill(this.elements, (Object)null);
        int rows = MathHelper.ceil((double)elements.length / 9.0D);

        for(int row = 0; row < rows; ++row) {
            KitSelectorEntry[] resolved = this.resolveRow(elements, row);
            int minColumn = (9 - resolved.length) / 2;

            for(int column = 0; column < resolved.length; ++column) {
                KitSelectorEntry element = resolved[column];
                this.elements[column + minColumn + row * 9] = element;
            }
        }

    }

    private KitSelectorEntry[] resolveRow(KitSelectorEntry[] elements, int row) {
        int minId = 2147483647;
        int maxId = -2147483648;
        int rowStart = row * 9;
        int rowEnd = Math.min(rowStart + 9, elements.length);

        for(int idx = rowStart; idx < rowEnd; ++idx) {
            if (elements[idx] != null) {
                if (idx < minId) {
                    minId = idx;
                }

                if (idx > maxId) {
                    maxId = idx;
                }
            }
        }

        KitSelectorEntry[] resolved = new KitSelectorEntry[maxId - minId + 1];
        System.arraycopy(elements, minId, resolved, 0, resolved.length);
        return resolved;
    }

    public int size() {
        return 27;
    }

    public boolean isEmpty() {
        return false;
    }

    public int getMaxCountPerStack() {
        return 1;
    }

    public ItemStack getStack(int index) {
        KitSelectorEntry element = this.elements[index];
        return element == null ? ItemStack.EMPTY : element.createIcon(participant);
    }

    public ItemStack removeStack(int index, int count) {
        this.handleElementClick(index);
        return ItemStack.EMPTY;
    }

    public ItemStack removeStack(int index) {
        this.handleElementClick(index);
        return ItemStack.EMPTY;
    }

    private void handleElementClick(int index) {
        this.player.inventory.setCursorStack(ItemStack.EMPTY);
        this.player.updateCursorStack();
        KitSelectorEntry element = this.elements[index];
        if (element != null) {
            element.onClick(this.player);
        }

        this.buildGrid();
        this.player.onHandlerRegistered(this.player.currentScreenHandler, this.player.currentScreenHandler.getStacks());
    }

    public void setStack(int slot, ItemStack stack) {
    }

    public void markDirty() {
    }

    public boolean canPlayerUse(PlayerEntity player) {
        return true;
    }

    public void clear() {
    }
}
