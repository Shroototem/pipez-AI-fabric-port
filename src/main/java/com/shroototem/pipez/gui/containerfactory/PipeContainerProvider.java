package com.shroototem.pipez.gui.containerfactory;

import com.shroototem.pipez.blocks.tileentity.UpgradeTileEntity;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class PipeContainerProvider implements ExtendedScreenHandlerFactory<PipeContainerData> {

    private final ContainerCreator container;
    private final UpgradeTileEntity tileEntity;
    private final Direction direction;
    private final int index;

    public PipeContainerProvider(ContainerCreator container, UpgradeTileEntity tileEntity, Direction direction, int index) {
        this.container = container;
        this.tileEntity = tileEntity;
        this.direction = direction;
        this.index = index;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(tileEntity.getBlockState().getBlock().getDescriptionId());
    }

    @Override
    public PipeContainerData getScreenOpeningData(ServerPlayer player) {
        return new PipeContainerData(tileEntity.getBlockPos(), direction, index);
    }

    public static void openGui(Player player, UpgradeTileEntity tileEntity, Direction direction, int index, ContainerCreator containerCreator) {
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(new PipeContainerProvider(containerCreator, tileEntity, direction, index));
        }
    }

    @Override
    public AbstractContainerMenu createMenu(int i, Inventory playerInventory, Player playerEntity) {
        return container.create(i, playerInventory, playerEntity);
    }

    public interface ContainerCreator {
        AbstractContainerMenu create(int i, Inventory playerInventory, Player playerEntity);
    }
}
