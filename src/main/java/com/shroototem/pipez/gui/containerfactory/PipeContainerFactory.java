package com.shroototem.pipez.gui.containerfactory;

import com.shroototem.pipez.blocks.tileentity.UpgradeTileEntity;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;

public class PipeContainerFactory<T extends AbstractContainerMenu, U extends UpgradeTileEntity> {

    private final ContainerCreator<T, U> containerCreator;

    public PipeContainerFactory(ContainerCreator<T, U> containerCreator) {
        this.containerCreator = containerCreator;
    }

    @SuppressWarnings("unchecked")
    public T create(int windowId, Inventory inv, RegistryFriendlyByteBuf data) {
        BlockEntity te = inv.player.level().getBlockEntity(data.readBlockPos());
        Direction direction = data.readEnum(Direction.class);
        int index = data.readInt();
        try {
            return containerCreator.create(windowId, inv, (U) te, direction, index);
        } catch (ClassCastException e) {
            return null;
        }
    }

    public interface ContainerCreator<T extends AbstractContainerMenu, U extends BlockEntity> {
        T create(int windowId, Inventory inv, U tileEntity, Direction side, int index);
    }
}
