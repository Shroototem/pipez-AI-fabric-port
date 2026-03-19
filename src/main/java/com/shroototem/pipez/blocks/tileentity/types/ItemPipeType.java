package com.shroototem.pipez.blocks.tileentity.types;

import com.shroototem.pipez.*;
import com.shroototem.pipez.blocks.ModBlocks;
import com.shroototem.pipez.blocks.tileentity.PipeLogicTileEntity;
import com.shroototem.pipez.blocks.tileentity.PipeTileEntity;
import com.shroototem.pipez.blocks.tileentity.UpgradeTileEntity;
import com.shroototem.pipez.config.PipezConfig;
import com.shroototem.pipez.datacomponents.ItemData;
import com.shroototem.pipez.items.ModItems;
import com.shroototem.pipez.utils.ComponentUtils;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ItemPipeType extends PipeType<Item, ItemData> {

    public static final ItemPipeType INSTANCE = new ItemPipeType();

    @Override
    public BlockApiLookup<?, Direction> getCapability() {
        return ItemStorage.SIDED;
    }

    @Override
    public Filter<?, Item> createFilter() {
        return new ItemFilter();
    }

    @Override
    public UpgradeTileEntity.Distribution getDefaultDistribution() {
        return UpgradeTileEntity.Distribution.NEAREST;
    }

    @Override
    public String getTranslationKey() {
        return "tooltip.pipez.item";
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(ModBlocks.ITEM_PIPE);
    }

    @Override
    public Component getTransferText(@Nullable Upgrade upgrade) {
        return Component.translatable("tooltip.pipez.rate.item", getRate(upgrade), getSpeed(upgrade));
    }

    @Override
    public void tick(PipeLogicTileEntity tileEntity) {
        for (Direction side : Direction.values()) {
            if (tileEntity.getLevel().getGameTime() % getSpeed(tileEntity, side) != 0) {
                continue;
            }
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
            Storage<ItemVariant> itemHandler = extractingConnection.getItemStorage();
            if (itemHandler == null) {
                continue;
            }

            List<PipeTileEntity.Connection> connections = tileEntity.getSortedConnections(side, this);

            if (tileEntity.getDistribution(side, this).equals(UpgradeTileEntity.Distribution.ROUND_ROBIN)) {
                insertEqually(tileEntity, side, connections, itemHandler);
            } else {
                insertOrdered(tileEntity, side, connections, itemHandler);
            }
        }
    }

    protected void insertEqually(PipeLogicTileEntity tileEntity, Direction side, List<PipeTileEntity.Connection> connections, Storage<ItemVariant> itemHandler) {
        if (connections.isEmpty()) {
            return;
        }
        int itemsToTransfer = getRate(tileEntity, side);
        boolean[] inventoriesFull = new boolean[connections.size()];
        int p = tileEntity.getRoundRobinIndex(side, this) % connections.size();
        while (itemsToTransfer > 0 && hasNotInserted(inventoriesFull)) {
            PipeTileEntity.Connection connection = connections.get(p);
            Storage<ItemVariant> destination = connection.getItemStorage();
            boolean hasInserted = false;
            if (destination != null && !inventoriesFull[p]) {
                long moved = StorageUtil.move(itemHandler, destination, resource -> {
                    return shouldTransfer(tileEntity.getLevel().registryAccess(), connection, resource.toStack(), tileEntity.getFilters(side, this), tileEntity.getFilterMode(side, this));
                }, 1, null);
                if (moved > 0) {
                    hasInserted = true;
                    itemsToTransfer -= (int) moved;
                }
            }
            if (!hasInserted) {
                inventoriesFull[p] = true;
            }
            p = (p + 1) % connections.size();
        }

        tileEntity.setRoundRobinIndex(side, this, p);
    }

    protected void insertOrdered(PipeLogicTileEntity tileEntity, Direction side, List<PipeTileEntity.Connection> connections, Storage<ItemVariant> itemHandler) {
        int itemsToTransfer = getRate(tileEntity, side);

        for (PipeTileEntity.Connection connection : connections) {
            Storage<ItemVariant> destination = connection.getItemStorage();
            if (destination == null) {
                continue;
            }
            long moved = StorageUtil.move(itemHandler, destination, resource -> {
                return shouldTransfer(tileEntity.getLevel().registryAccess(), connection, resource.toStack(), tileEntity.getFilters(side, this), tileEntity.getFilterMode(side, this));
            }, itemsToTransfer, null);

            itemsToTransfer -= (int) moved;

            if (itemsToTransfer <= 0) {
                break;
            }
        }
    }

    private boolean shouldTransfer(HolderLookup.Provider provider, PipeTileEntity.Connection connection, ItemStack stack, List<Filter<?, ?>> filters, UpgradeTileEntity.FilterMode filterMode) {
        boolean isBlacklist = filterMode == UpgradeTileEntity.FilterMode.BLACKLIST;

        // Step 1: Check inverted filters matching this connection — reject if any match
        for (Filter<?, Item> filter : filters.stream()
                .map(f -> (Filter<?, Item>) f)
                .filter(Filter::isInvert)
                .filter(f -> matchesConnection(connection, f))
                .collect(Collectors.toList())) {
            if (matches(provider, filter, stack)) {
                return false;
            }
        }

        // Step 2: Non-inverted filters matching this connection
        List<Filter<?, Item>> collect = filters.stream()
                .map(f -> (Filter<?, Item>) f)
                .filter(f -> !f.isInvert())
                .filter(f -> matchesConnection(connection, f))
                .collect(Collectors.toList());

        if (!collect.isEmpty()) {
            boolean itemMatches = collect.stream().anyMatch(f -> matches(provider, f, stack));
            return itemMatches != isBlacklist;
        }

        // Step 3: No filters match this connection
        if (!isBlacklist) {
            // WHITELIST: check if item is destined for a specific other connection
            for (Filter<?, ?> f : filters) {
                Filter<?, Item> filter = (Filter<?, Item>) f;
                if (!filter.isInvert() && filter.getDestination() != null) {
                    if (matches(provider, filter, stack)) {
                        return false;
                    }
                }
            }
        }
        // BLACKLIST with no matching filters: allow (no blacklist applies here)
        // No restriction: allow
        return true;
    }

    private boolean matches(HolderLookup.Provider provider, Filter<?, Item> filter, ItemStack stack) {
        CompoundTag metadata = filter.getMetadata();
        if (metadata == null) {
            return filter.getTag() == null || filter.getTag().contains(stack.getItem());
        }
        CompoundTag stackNBT = ComponentUtils.getTag(provider, stack);
        if (filter.isExactMetadata()) {
            if (deepExactCompare(metadata, stackNBT)) {
                return filter.getTag() == null || filter.getTag().contains(stack.getItem());
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
            return filter.getTag() == null || filter.getTag().contains(stack.getItem());
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

    public int getSpeed(PipeLogicTileEntity tileEntity, Direction direction) {
        return getSpeed(tileEntity.getUpgrade(direction));
    }

    public int getSpeed(@Nullable Upgrade upgrade) {
        if (upgrade == null) {
            return PipezConfig.itemPipeSpeed;
        }
        switch (upgrade) {
            case BASIC:
                return PipezConfig.itemPipeSpeedBasic;
            case IMPROVED:
                return PipezConfig.itemPipeSpeedImproved;
            case ADVANCED:
                return PipezConfig.itemPipeSpeedAdvanced;
            case ULTIMATE:
                return PipezConfig.itemPipeSpeedUltimate;
            case INFINITY:
            default:
                return 1;
        }
    }

    @Override
    public int getRate(@Nullable Upgrade upgrade) {
        if (upgrade == null) {
            return PipezConfig.itemPipeAmount;
        }
        switch (upgrade) {
            case BASIC:
                return PipezConfig.itemPipeAmountBasic;
            case IMPROVED:
                return PipezConfig.itemPipeAmountImproved;
            case ADVANCED:
                return PipezConfig.itemPipeAmountAdvanced;
            case ULTIMATE:
                return PipezConfig.itemPipeAmountUltimate;
            case INFINITY:
            default:
                return Integer.MAX_VALUE;
        }
    }

    @Override
    public DataComponentType<ItemData> getDataComponentType() {
        return ModItems.ITEM_DATA_COMPONENT;
    }

    private static final ItemData DEFAULT = new ItemData(UpgradeTileEntity.FilterMode.WHITELIST, UpgradeTileEntity.RedstoneMode.IGNORED, UpgradeTileEntity.Distribution.NEAREST, Collections.emptyList());

    @Override
    public ItemData defaultData() {
        return DEFAULT;
    }

}
