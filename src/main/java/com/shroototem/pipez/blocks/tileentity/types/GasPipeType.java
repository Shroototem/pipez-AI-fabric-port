package com.shroototem.pipez.blocks.tileentity.types;

import com.shroototem.pipez.Filter;
import com.shroototem.pipez.GasFilter;
import com.shroototem.pipez.Upgrade;
import com.shroototem.pipez.blocks.ModBlocks;
import com.shroototem.pipez.blocks.tileentity.PipeLogicTileEntity;
import com.shroototem.pipez.blocks.tileentity.UpgradeTileEntity;
import com.shroototem.pipez.config.PipezConfig;
import com.shroototem.pipez.datacomponents.GasData;
import com.shroototem.pipez.items.ModItems;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;
import java.util.Collections;

/**
 * Gas pipe type stub for Fabric.
 * Mekanism is not available on Fabric, so this is a no-op implementation.
 * The pipe type is kept so that Universal Pipes can still reference it
 * and the data component / upgrade system remains consistent.
 */
public class GasPipeType extends PipeType<Void, GasData> {

    public static final GasPipeType INSTANCE = new GasPipeType();

    @Override
    public BlockApiLookup<?, Direction> getCapability() {
        // No gas capability on Fabric; return null
        return null;
    }

    @Override
    public Filter<?, Void> createFilter() {
        return null;
    }

    @Override
    public String getTranslationKey() {
        return "tooltip.pipez.gas";
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(ModBlocks.GAS_PIPE);
    }

    @Override
    public Component getTransferText(@Nullable Upgrade upgrade) {
        return Component.translatable("tooltip.pipez.rate.gas", getRate(upgrade));
    }

    @Override
    public void tick(PipeLogicTileEntity tileEntity) {
        // No-op: Mekanism is not available on Fabric
    }

    @Override
    public boolean hasFilter() {
        return false;
    }

    @Override
    public int getRate(@Nullable Upgrade upgrade) {
        if (upgrade == null) {
            return PipezConfig.gasPipeAmount;
        }
        switch (upgrade) {
            case BASIC:
                return PipezConfig.gasPipeAmountBasic;
            case IMPROVED:
                return PipezConfig.gasPipeAmountImproved;
            case ADVANCED:
                return PipezConfig.gasPipeAmountAdvanced;
            case ULTIMATE:
                return PipezConfig.gasPipeAmountUltimate;
            case INFINITY:
            default:
                return Integer.MAX_VALUE;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public BlockApiLookup<?, Direction>[] getCapabilities() {
        // No capabilities for gas on Fabric
        return new BlockApiLookup[0];
    }

    @Override
    public DataComponentType<GasData> getDataComponentType() {
        return ModItems.GAS_DATA_COMPONENT;
    }

    private static final GasData DEFAULT = new GasData(UpgradeTileEntity.FilterMode.WHITELIST, UpgradeTileEntity.RedstoneMode.IGNORED, UpgradeTileEntity.Distribution.ROUND_ROBIN, Collections.emptyList());

    @Override
    public GasData defaultData() {
        return DEFAULT;
    }

}
