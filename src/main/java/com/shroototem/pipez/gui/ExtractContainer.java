package com.shroototem.pipez.gui;

import com.shroototem.pipez.blocks.tileentity.PipeLogicTileEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ExtractContainer extends AbstractContainerMenu implements IPipeContainer {

    private final PipeLogicTileEntity pipe;
    private final Direction side;
    private final int index;
    private final Inventory playerInventory;

    public ExtractContainer(int id, Inventory playerInventory, PipeLogicTileEntity pipe, Direction side, int index) {
        super(Containers.EXTRACT, id);
        this.pipe = pipe;
        this.side = side;
        this.index = index;
        this.playerInventory = playerInventory;

        addSlot(new UpgradeSlot(pipe.getUpgradeInventory(), side.get3DDataValue(), 9, 81));

        addPlayerInventorySlots();
    }

    protected void addPlayerInventorySlots() {
        int invOffset = 30;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18 + invOffset));
            }
        }
        for (int k = 0; k < 9; k++) {
            addSlot(new Slot(playerInventory, k, 8 + k * 18, 142 + invOffset));
        }
    }

    @Override
    public PipeLogicTileEntity getPipe() {
        return pipe;
    }

    @Override
    public Direction getSide() {
        return side;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotIndex);

        if (slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            if (slotIndex < 1) {
                if (!moveItemStackTo(itemstack1, 1, slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(itemstack1, 0, 1, false)) {
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
        return !pipe.isRemoved();
    }
}
