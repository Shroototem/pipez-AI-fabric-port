package com.shroototem.pipez.gui.containerfactory;

import com.shroototem.pipez.Filter;
import com.shroototem.pipez.blocks.tileentity.PipeLogicTileEntity;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class FilterContainerProvider implements ExtendedScreenHandlerFactory<FilterContainerData> {

    private final ContainerCreator container;
    private final PipeLogicTileEntity pipe;
    private final Direction direction;
    private final int index;
    private final Filter<?, ?> filter;

    public FilterContainerProvider(ContainerCreator container, PipeLogicTileEntity pipe, Direction direction, int index, Filter<?, ?> filter) {
        this.container = container;
        this.pipe = pipe;
        this.direction = direction;
        this.index = index;
        this.filter = filter;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(pipe.getBlockState().getBlock().getDescriptionId());
    }

    @Override
    public FilterContainerData getScreenOpeningData(ServerPlayer player) {
        return new FilterContainerData(pipe.getBlockPos(), direction, index, filter.toNbt());
    }

    public static void openGui(Player player, PipeLogicTileEntity tileEntity, Direction direction, Filter<?, ?> filter, int index, ContainerCreator containerCreator) {
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(new FilterContainerProvider(containerCreator, tileEntity, direction, index, filter));
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
