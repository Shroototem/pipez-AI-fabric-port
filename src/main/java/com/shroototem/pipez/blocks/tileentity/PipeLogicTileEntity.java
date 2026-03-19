package com.shroototem.pipez.blocks.tileentity;

import com.shroototem.pipez.blocks.tileentity.types.EnergyPipeType;
import com.shroototem.pipez.blocks.tileentity.types.FluidPipeType;
import com.shroototem.pipez.blocks.tileentity.types.ItemPipeType;
import com.shroototem.pipez.blocks.tileentity.types.PipeType;
import com.shroototem.pipez.utils.DummyFluidStorage;
import com.shroototem.pipez.utils.DummyItemStorage;
import com.shroototem.pipez.utils.PipeEnergyStorage;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import team.reborn.energy.api.EnergyStorage;

import org.jetbrains.annotations.Nullable;

public abstract class PipeLogicTileEntity extends UpgradeTileEntity {

    protected PipeType<?, ?>[] types;
    protected final int[][] rrIndex;

    protected PipeEnergyStorage[] energyStorages;

    private int recursionDepth;

    private final SnapshotParticipant<Integer>[] energyRRIndexParticipants;

    public PipeLogicTileEntity(BlockEntityType<?> tileEntityTypeIn, PipeType<?, ?>[] types, BlockPos pos, BlockState state) {
        super(tileEntityTypeIn, pos, state);
        this.types = types;
        rrIndex = new int[Direction.values().length][types.length];
        energyStorages = new PipeEnergyStorage[Direction.values().length];
        energyRRIndexParticipants = new SnapshotParticipant[Direction.values().length];
        for (Direction direction : Direction.values()) {
            final int dirIndex = direction.get3DDataValue();
            energyRRIndexParticipants[dirIndex] = new SnapshotParticipant<>() {

                @Override
                protected Integer createSnapshot() {
                    return rrIndex[dirIndex][getIndex(EnergyPipeType.INSTANCE)];
                }

                @Override
                protected void readSnapshot(Integer snapshot) {
                    rrIndex[dirIndex][getIndex(EnergyPipeType.INSTANCE)] = snapshot;
                }
            };
        }
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public <T> T onRegisterCapability(BlockApiLookup<T, Direction> lookup, @Nullable Direction side) {
        if (side == null) {
            return null;
        }
        if (!isExtracting(side)) {
            return null;
        }
        if (lookup == EnergyStorage.SIDED && hasType(EnergyPipeType.INSTANCE)) {
            if (energyStorages[side.get3DDataValue()] == null) {
                energyStorages[side.get3DDataValue()] = new PipeEnergyStorage(this, side);
            }
            return (T) energyStorages[side.get3DDataValue()];
        } else if (lookup == FluidStorage.SIDED && hasType(FluidPipeType.INSTANCE)) {
            return (T) DummyFluidStorage.INSTANCE;
        } else if (lookup == ItemStorage.SIDED && hasType(ItemPipeType.INSTANCE)) {
            return (T) DummyItemStorage.INSTANCE;
        }
        return null;
    }

    public boolean hasType(PipeType<?, ?> type) {
        for (PipeType<?, ?> t : types) {
            if (t == type) {
                return true;
            }
        }
        return false;
    }

    public int getRoundRobinIndex(Direction direction, PipeType<?, ?> pipeType) {
        return rrIndex[direction.get3DDataValue()][getIndex(pipeType)];
    }

    public void setEnergyRoundRobinIndex(Direction direction, int value, TransactionContext transaction) {
        energyRRIndexParticipants[direction.get3DDataValue()].updateSnapshots(transaction);
        rrIndex[direction.get3DDataValue()][getIndex(EnergyPipeType.INSTANCE)] = value;
    }

    public void setRoundRobinIndex(Direction direction, PipeType<?, ?> pipeType, int value) {
        rrIndex[direction.get3DDataValue()][getIndex(pipeType)] = value;
    }

    public boolean isEnabled(Direction side, PipeType<?, ?> pipeType) {
        UpgradeTileEntity.RedstoneMode redstoneMode = getRedstoneMode(side, pipeType);
        return redstoneMode != UpgradeTileEntity.RedstoneMode.ALWAYS_OFF;
    }

    public int getPreferredPipeIndex(Direction side) {
        for (int i = 0; i < types.length; i++) {
            if (isEnabled(side, types[i])) {
                return i;
            }
        }
        return 0;
    }

    public boolean shouldWork(Direction side, PipeType<?, ?> pipeType) {
        RedstoneMode redstoneMode = getRedstoneMode(side, pipeType);
        if (redstoneMode.equals(RedstoneMode.ALWAYS_OFF)) {
            return false;
        } else if (redstoneMode.equals(RedstoneMode.OFF_WHEN_POWERED)) {
            return !isRedstonePowered();
        } else if (redstoneMode.equals(RedstoneMode.ON_WHEN_POWERED)) {
            return isRedstonePowered();
        } else {
            return true;
        }
    }

    public boolean isRedstonePowered() {
        return level.hasNeighborSignal(worldPosition);
    }

    public PipeType<?, ?>[] getPipeTypes() {
        return types;
    }

    public int getIndex(PipeType<?, ?> pipeType) {
        for (int i = 0; i < getPipeTypes().length; i++) {
            PipeType<?, ?> type = getPipeTypes()[i];
            if (type == pipeType) {
                return i;
            }
        }
        return 0;
    }

    @Override
    public void tick() {
        super.tick();

        if (level.isClientSide()) {
            return;
        }

        for (PipeType<?, ?> type : getPipeTypes()) {
            type.tick(this);
        }

        if (hasType(EnergyPipeType.INSTANCE)) {
            for (Direction side : Direction.values()) {
                if (isExtracting(side)) {
                    if (energyStorages[side.get3DDataValue()] != null) {
                        energyStorages[side.get3DDataValue()].tick();
                    }
                }
            }
        }
    }

    public void invalidateCapabilities() {
        // On Fabric, there is no level.invalidateCapabilities(pos).
        // We just manage the energy storage caches manually.
        if (!hasType(EnergyPipeType.INSTANCE)) {
            return;
        }
        for (Direction dir : Direction.values()) {
            if (!isExtracting(dir)) {
                //TODO Check if this causes issues when reloading or other edge cases
                energyStorages[dir.get3DDataValue()] = null;
            }
        }
    }

    @Override
    public void setExtracting(Direction side, boolean extracting) {
        super.setExtracting(side, extracting);
        invalidateCapabilities();
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        invalidateCapabilities();
    }

    @Override
    public void setRemoved() {
        invalidateCapabilities();
        super.setRemoved();
    }

    @Override
    public boolean canInsert(Level level, Connection connection) {
        for (PipeType<?, ?> type : types) {
            for (BlockApiLookup<?, Direction> lookup : type.getCapabilities()) {
                if (connection.getCapability(lookup) != null) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean pushRecursion() {
        if (recursionDepth >= 1) {
            return true;
        }
        recursionDepth++;
        return false;
    }

    public void popRecursion() {
        recursionDepth--;
    }

}
