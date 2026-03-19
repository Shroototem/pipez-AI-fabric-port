package com.shroototem.pipez.gui;

import com.shroototem.pipez.Filter;
import com.shroototem.pipez.blocks.tileentity.PipeLogicTileEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class FilterContainer extends AbstractContainerMenu implements IPipeContainer {

    private final PipeLogicTileEntity pipe;
    private final Direction side;
    private final int index;
    private final Filter<?, ?> filter;
    private final Inventory playerInventory;

    public FilterContainer(int id, Inventory playerInventory, PipeLogicTileEntity pipe, Direction side, int index, Filter<?, ?> filter) {
        super(Containers.FILTER, id);
        this.pipe = pipe;
        this.side = side;
        this.index = index;
        this.filter = filter;
        this.playerInventory = playerInventory;

        addPlayerInventorySlots();
    }

    protected void addPlayerInventorySlots() {
        int invOffset = 56;
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

    public int getIndex() {
        return index;
    }

    public Filter<?, ?> getFilter() {
        return filter;
    }

    @Override
    public Direction getSide() {
        return side;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return !pipe.isRemoved();
    }

}
