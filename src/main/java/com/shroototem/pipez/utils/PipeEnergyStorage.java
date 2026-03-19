package com.shroototem.pipez.utils;

import com.shroototem.pipez.blocks.tileentity.PipeLogicTileEntity;
import com.shroototem.pipez.blocks.tileentity.types.EnergyPipeType;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.Direction;
import team.reborn.energy.api.EnergyStorage;

public class PipeEnergyStorage implements EnergyStorage {

    protected PipeLogicTileEntity pipe;
    protected Direction side;
    protected long lastReceived;

    public PipeEnergyStorage(PipeLogicTileEntity pipe, Direction side) {
        this.pipe = pipe;
        this.side = side;
    }

    public void tick() {
        if (pipe.getLevel().getGameTime() - lastReceived > 1) {
            EnergyPipeType.INSTANCE.pullEnergy(pipe, side);
        }
    }

    @Override
    public long insert(long maxAmount, TransactionContext transaction) {
        lastReceived = pipe.getLevel().getGameTime();
        return EnergyPipeType.INSTANCE.receive(pipe, side, maxAmount, transaction);
    }

    @Override
    public long extract(long maxAmount, TransactionContext transaction) {
        return 0;
    }

    @Override
    public long getAmount() {
        return 0;
    }

    @Override
    public long getCapacity() {
        return Long.MAX_VALUE;
    }
}
