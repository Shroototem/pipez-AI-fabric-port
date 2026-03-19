package com.shroototem.pipez.blocks.tileentity.types;

import com.shroototem.pipez.*;
import com.shroototem.pipez.blocks.ModBlocks;
import com.shroototem.pipez.blocks.tileentity.PipeLogicTileEntity;
import com.shroototem.pipez.blocks.tileentity.PipeTileEntity;
import com.shroototem.pipez.blocks.tileentity.UpgradeTileEntity;
import com.shroototem.pipez.config.PipezConfig;
import com.shroototem.pipez.datacomponents.FluidData;
import com.shroototem.pipez.items.ModItems;
import com.shroototem.pipez.utils.ComponentUtils;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;

import org.jetbrains.annotations.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class FluidPipeType extends PipeType<Fluid, FluidData> {

    public static final FluidPipeType INSTANCE = new FluidPipeType();

    @Override
    public BlockApiLookup<?, Direction> getCapability() {
        return FluidStorage.SIDED;
    }

    @Nullable
    @Override
    public Filter<?, Fluid> createFilter() {
        return new FluidFilter();
    }

    @Override
    public String getTranslationKey() {
        return "tooltip.pipez.fluid";
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(ModBlocks.FLUID_PIPE);
    }

    @Override
    public Component getTransferText(@Nullable Upgrade upgrade) {
        return Component.translatable("tooltip.pipez.rate.fluid", getRate(upgrade));
    }

    @Override
    public void tick(PipeLogicTileEntity tileEntity) {
        for (Direction side : Direction.values()) {
            if (!tileEntity.isExtracting(side)) {
                continue;
            }
            if (!tileEntity.shouldWork(side, this)) {
                continue;
            }
            PipeTileEntity.Connection extractingConnection = tileEntity.getExtractingConnection(side);
            if (extractingConnection == null) {
                continue;
            }
            Storage<FluidVariant> fluidHandler = extractingConnection.getFluidStorage();
            if (fluidHandler == null) {
                continue;
            }

            List<PipeTileEntity.Connection> connections = tileEntity.getSortedConnections(side, this);

            if (tileEntity.getDistribution(side, this).equals(UpgradeTileEntity.Distribution.ROUND_ROBIN)) {
                insertEqually(tileEntity, side, connections, fluidHandler);
            } else {
                insertOrdered(tileEntity, side, connections, fluidHandler);
            }
        }
    }

    protected void insertEqually(PipeLogicTileEntity tileEntity, Direction side, List<PipeTileEntity.Connection> connections, Storage<FluidVariant> fluidHandler) {
        if (connections.isEmpty()) {
            return;
        }
        int completeAmount = getRate(tileEntity, side);
        int mbToTransfer = completeAmount;
        boolean[] connectionsFull = new boolean[connections.size()];
        int p = tileEntity.getRoundRobinIndex(side, this) % connections.size();
        while (mbToTransfer > 0 && hasNotInserted(connectionsFull)) {
            PipeTileEntity.Connection connection = connections.get(p);
            Storage<FluidVariant> destination = connection.getFluidStorage();
            boolean hasInserted = false;
            if (destination != null && !connectionsFull[p]) {
                long moved = StorageUtil.move(fluidHandler, destination, resource -> {
                    return canInsert(tileEntity.getLevel().registryAccess(), connection, resource, tileEntity.getFilters(side, this)) != tileEntity.getFilterMode(side, this).equals(UpgradeTileEntity.FilterMode.BLACKLIST);
                }, Math.min(Math.max(completeAmount / getConnectionsNotFullCount(connectionsFull), 1), mbToTransfer), null);
                if (moved > 0) {
                    mbToTransfer -= (int) moved;
                    hasInserted = true;
                }
            }
            if (!hasInserted) {
                connectionsFull[p] = true;
            }
            p = (p + 1) % connections.size();
        }

        tileEntity.setRoundRobinIndex(side, this, p);
    }

    protected void insertOrdered(PipeLogicTileEntity tileEntity, Direction side, List<PipeTileEntity.Connection> connections, Storage<FluidVariant> fluidHandler) {
        int mbToTransfer = getRate(tileEntity, side);

        for (PipeTileEntity.Connection connection : connections) {
            Storage<FluidVariant> destination = connection.getFluidStorage();
            if (destination == null) {
                continue;
            }

            long moved = StorageUtil.move(fluidHandler, destination, resource -> {
                return canInsert(tileEntity.getLevel().registryAccess(), connection, resource, tileEntity.getFilters(side, this)) != tileEntity.getFilterMode(side, this).equals(UpgradeTileEntity.FilterMode.BLACKLIST);
            }, mbToTransfer, null);
            mbToTransfer -= (int) moved;

            if (mbToTransfer <= 0) {
                break;
            }
        }
    }

    private boolean canInsert(HolderLookup.Provider provider, PipeTileEntity.Connection connection, FluidVariant resource, List<Filter<?, ?>> filters) {
        for (Filter<?, Fluid> filter : filters.stream().map(filter -> (Filter<?, Fluid>) filter).filter(Filter::isInvert).filter(f -> matchesConnection(connection, f)).collect(Collectors.toList())) {
            if (matches(provider, filter, resource)) {
                return false;
            }
        }
        List<Filter<?, Fluid>> collect = filters.stream().map(filter -> (Filter<?, Fluid>) filter).filter(f -> !f.isInvert()).filter(f -> matchesConnection(connection, f)).collect(Collectors.toList());
        if (collect.isEmpty()) {
            return true;
        }
        for (Filter<?, Fluid> filter : collect) {
            if (matches(provider, filter, resource)) {
                return true;
            }
        }
        return false;
    }

    private boolean matches(HolderLookup.Provider provider, Filter<?, Fluid> filter, FluidVariant resource) {
        CompoundTag metadata = filter.getMetadata();
        if (metadata == null) {
            return filter.getTag() == null || filter.getTag().contains(resource.getFluid());
        }
        CompoundTag stackNBT = ComponentUtils.getTag(provider, resource.getComponents());
        if (filter.isExactMetadata()) {
            if (deepExactCompare(metadata, stackNBT)) {
                return filter.getTag() == null || filter.getTag().contains(resource.getFluid());
            } else {
                return false;
            }
        } else {
            if (stackNBT.isEmpty()) {
                return metadata.size() <= 0;
            }
            if (!deepFuzzyCompare(metadata, stackNBT)) {
                return false;
            }
            return filter.getTag() == null || filter.getTag().contains(resource.getFluid());
        }
    }

    private boolean hasNotInserted(boolean[] inventoriesFull) {
        for (boolean b : inventoriesFull) {
            if (!b) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getRate(@Nullable Upgrade upgrade) {
        if (upgrade == null) {
            return PipezConfig.fluidPipeAmount;
        }
        switch (upgrade) {
            case BASIC:
                return PipezConfig.fluidPipeAmountBasic;
            case IMPROVED:
                return PipezConfig.fluidPipeAmountImproved;
            case ADVANCED:
                return PipezConfig.fluidPipeAmountAdvanced;
            case ULTIMATE:
                return PipezConfig.fluidPipeAmountUltimate;
            case INFINITY:
            default:
                return Integer.MAX_VALUE;
        }
    }

    @Override
    public DataComponentType<FluidData> getDataComponentType() {
        return ModItems.FLUID_DATA_COMPONENT;
    }

    private static final FluidData DEFAULT = new FluidData(UpgradeTileEntity.FilterMode.WHITELIST, UpgradeTileEntity.RedstoneMode.IGNORED, UpgradeTileEntity.Distribution.ROUND_ROBIN, Collections.emptyList());

    @Override
    public FluidData defaultData() {
        return DEFAULT;
    }

}
