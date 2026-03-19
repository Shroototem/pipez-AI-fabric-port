package com.shroototem.pipez.blocks.tileentity.configuration;

import com.shroototem.pipez.blocks.tileentity.UpgradeTileEntity;
import com.shroototem.pipez.blocks.tileentity.types.PipeType;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

import java.util.function.Function;
import java.util.function.Supplier;

public class RedstoneModeCache extends CachedPipeConfiguration<UpgradeTileEntity.RedstoneMode> {

    public RedstoneModeCache(Supplier<NonNullList<ItemStack>> upgradeInventory, Function<PipeType<?, ?>, UpgradeTileEntity.RedstoneMode> defaultValue, Runnable onDirty) {
        super(upgradeInventory, defaultValue, onDirty);
    }

    @Override
    public UpgradeTileEntity.RedstoneMode get(PipeType<?, ?> pipeType, ItemStack stack) {
        return pipeType.getRedstoneMode(stack);
    }

    @Override
    public void set(PipeType<?, ?> pipeType, ItemStack stack, UpgradeTileEntity.RedstoneMode value) {
        pipeType.setRedstoneMode(stack, value);
    }

}
