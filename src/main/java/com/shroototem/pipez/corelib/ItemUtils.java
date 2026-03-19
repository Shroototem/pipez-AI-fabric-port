package com.shroototem.pipez.corelib;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class ItemUtils {

    public static void readInventory(ValueInput input, String key, NonNullList<ItemStack> items) {
        ValueInput.TypedInputList<ItemStack> list = input.listOrEmpty(key, ItemStack.OPTIONAL_CODEC);
        int index = 0;
        for (ItemStack stack : list) {
            if (index < items.size()) {
                items.set(index, stack);
            }
            index++;
        }
    }

    public static void saveInventory(ValueOutput output, String key, NonNullList<ItemStack> items) {
        ValueOutput.TypedOutputList<ItemStack> list = output.list(key, ItemStack.OPTIONAL_CODEC);
        for (ItemStack stack : items) {
            list.add(stack);
        }
    }
}
