package com.shroototem.pipez.corelib;

import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ItemListInventory implements Container {

    private final NonNullList<ItemStack> items;
    private final Runnable onChanged;

    public ItemListInventory(NonNullList<ItemStack> items, Runnable onChanged) {
        this.items = items;
        this.onChanged = onChanged;
    }

    @Override
    public int getContainerSize() {
        return items.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack stack = items.get(slot);
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack split = stack.split(amount);
        if (stack.isEmpty()) {
            items.set(slot, ItemStack.EMPTY);
        }
        setChanged();
        return split;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack stack = items.get(slot);
        items.set(slot, ItemStack.EMPTY);
        return stack;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        setChanged();
    }

    @Override
    public void setChanged() {
        if (onChanged != null) {
            onChanged.run();
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void clearContent() {
        for (int i = 0; i < items.size(); i++) {
            items.set(i, ItemStack.EMPTY);
        }
        setChanged();
    }
}
