package com.shroototem.pipez.gui;

import com.shroototem.pipez.Filter;
import com.shroototem.pipez.PipezMod;
import com.shroototem.pipez.blocks.tileentity.PipeLogicTileEntity;
import com.shroototem.pipez.gui.containerfactory.FilterContainerData;
import com.shroototem.pipez.gui.containerfactory.PipeContainerData;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntity;

public class Containers {

    public static final MenuType<ExtractContainer> EXTRACT = Registry.register(
            BuiltInRegistries.MENU,
            ResourceLocation.fromNamespaceAndPath(PipezMod.MODID, "extract"),
            new ExtendedScreenHandlerType<>(
                    (syncId, inv, data) -> {
                        BlockEntity te = inv.player.level().getBlockEntity(data.pos());
                        if (te instanceof PipeLogicTileEntity pipe) {
                            return new ExtractContainer(syncId, inv, pipe, data.side(), data.index());
                        }
                        return null;
                    },
                    PipeContainerData.STREAM_CODEC
            )
    );

    public static final MenuType<FilterContainer> FILTER = Registry.register(
            BuiltInRegistries.MENU,
            ResourceLocation.fromNamespaceAndPath(PipezMod.MODID, "filter"),
            new ExtendedScreenHandlerType<>(
                    (syncId, inv, data) -> {
                        BlockEntity te = inv.player.level().getBlockEntity(data.pos());
                        if (te instanceof PipeLogicTileEntity pipe) {
                            Filter<?, ?> filter = pipe.getPipeTypes()[data.index()].createFilter();
                            if (filter != null) {
                                filter = filter.fromNbt(data.filterTag());
                            }
                            return new FilterContainer(syncId, inv, pipe, data.side(), data.index(), filter);
                        }
                        return null;
                    },
                    FilterContainerData.STREAM_CODEC
            )
    );

    public static void init() {
        // Class loading triggers registration
    }
}
