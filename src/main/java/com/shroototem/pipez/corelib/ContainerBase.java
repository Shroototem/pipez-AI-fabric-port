package com.shroototem.pipez.corelib;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;

public abstract class ContainerBase extends AbstractContainerMenu {

    @Nullable
    protected final Container inventory;

    protected ContainerBase(MenuType<?> type, int id, Inventory playerInventory, @Nullable Container inventory) {
        super(type, id);
        this.inventory = inventory;
    }

    public abstract int getInventorySize();

    public abstract int getInvOffset();

    protected void addPlayerInventorySlots() {
        int invOffset = getInvOffset();
        if (playerInventory() != null) {
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 9; j++) {
                    addSlot(new Slot(playerInventory(), j + i * 9 + 9, 8 + j * 18, 84 + i * 18 + invOffset));
                }
            }
            for (int k = 0; k < 9; k++) {
                addSlot(new Slot(playerInventory(), k, 8 + k * 18, 142 + invOffset));
            }
        }
    }

    @Nullable
    protected Inventory playerInventory() {
        return null;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            int invSize = getInventorySize();

            if (index < invSize) {
                if (!moveItemStackTo(itemstack1, invSize, slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(itemstack1, 0, invSize, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemstack;
    }

    @Override
    public boolean stillValid(Player player) {
        return inventory == null || inventory.stillValid(player);
    }
}
