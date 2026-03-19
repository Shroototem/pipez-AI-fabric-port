package com.shroototem.pipez.blocks.tileentity.types;

import com.shroototem.pipez.Filter;
import com.shroototem.pipez.Upgrade;
import com.shroototem.pipez.blocks.ModBlocks;
import com.shroototem.pipez.blocks.tileentity.PipeLogicTileEntity;
import com.shroototem.pipez.blocks.tileentity.PipeTileEntity;
import com.shroototem.pipez.blocks.tileentity.UpgradeTileEntity;
import com.shroototem.pipez.config.PipezConfig;
import com.shroototem.pipez.corelib.Pair;
import com.shroototem.pipez.datacomponents.EnergyData;
import com.shroototem.pipez.items.ModItems;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import team.reborn.energy.api.EnergyStorage;

import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EnergyPipeType extends PipeType<Void, EnergyData> {

    public static final EnergyPipeType INSTANCE = new EnergyPipeType();

    @Override
    public BlockApiLookup<?, Direction> getCapability() {
        return EnergyStorage.SIDED;
    }

    @Nullable
    @Override
    public Filter<?, Void> createFilter() {
        return null;
    }

    @Override
    public boolean hasFilter() {
        return false;
    }

    @Override
    public String getTranslationKey() {
        return "tooltip.pipez.energy";
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(ModBlocks.ENERGY_PIPE);
    }

    @Override
    public Component getTransferText(@Nullable Upgrade upgrade) {
        return Component.translatable("tooltip.pipez.rate.energy", getRate(upgrade));
    }

    @Override
    public void tick(PipeLogicTileEntity tileEntity) {

    }

    public void pullEnergy(PipeLogicTileEntity tileEntity, Direction side) {
        if (!tileEntity.isExtracting(side)) {
            return;
        }
        if (!tileEntity.shouldWork(side, this)) {
            return;
        }
        PipeTileEntity.Connection extractingConnection = tileEntity.getExtractingConnection(side);
        if (extractingConnection == null) {
            return;
        }
        EnergyStorage energyStorage = extractingConnection.getEnergyHandler();
        if (energyStorage == null || energyStorage.getAmount() <= 0L) {
            return;
        }

        List<PipeTileEntity.Connection> connections = tileEntity.getSortedConnections(side, this);

        if (tileEntity.getDistribution(side, this).equals(UpgradeTileEntity.Distribution.ROUND_ROBIN)) {
            insertEqually(tileEntity, side, connections, energyStorage);
        } else {
            insertOrdered(tileEntity, side, connections, energyStorage);
        }
    }

    public long receive(PipeLogicTileEntity tileEntity, Direction side, long amount, TransactionContext transaction) {
        if (!tileEntity.isExtracting(side)) {
            return 0;
        }
        if (!tileEntity.shouldWork(side, this)) {
            return 0;
        }

        List<PipeTileEntity.Connection> connections = tileEntity.getSortedConnections(side, this);

        long maxTransfer = Math.min(getRate(tileEntity, side), amount);

        if (tileEntity.getDistribution(side, this).equals(UpgradeTileEntity.Distribution.ROUND_ROBIN)) {
            return receiveEqually(tileEntity, side, connections, maxTransfer, transaction);
        } else {
            return receiveOrdered(tileEntity, side, connections, maxTransfer, transaction);
        }
    }

    protected void insertEqually(PipeLogicTileEntity tileEntity, Direction side, List<PipeTileEntity.Connection> connections, EnergyStorage energyHandler) {
        if (connections.isEmpty()) {
            return;
        }
        long completeAmount = getRate(tileEntity, side);
        long energyToTransfer = completeAmount;

        int p = tileEntity.getRoundRobinIndex(side, this) % connections.size();

        List<EnergyStorage> destinations = new ArrayList<>(connections.size());
        for (int i = 0; i < connections.size(); i++) {
            int index = (i + p) % connections.size();

            PipeTileEntity.Connection connection = connections.get(index);
            EnergyStorage destination = connection.getEnergyHandler();
            try (Transaction simulated = Transaction.openOuter()) {
                if (destination != null && !isEnergyFull(destination) && destination.insert(1, simulated) >= 1) {
                    destinations.add(destination);
                }
            }
        }

        for (EnergyStorage destination : destinations) {
            long simulatedExtract;
            try (Transaction simulated = Transaction.openOuter()) {
                simulatedExtract = energyHandler.extract(Math.min(Math.max(completeAmount / destinations.size(), 1), energyToTransfer), simulated);
            }
            if (simulatedExtract > 0) {
                energyToTransfer -= moveEnergy(energyHandler, destination, simulatedExtract, null);
            }

            p = (p + 1) % connections.size();

            if (energyToTransfer <= 0) {
                break;
            }
        }

        tileEntity.setRoundRobinIndex(side, this, p);
    }

    protected long receiveEqually(PipeLogicTileEntity tileEntity, Direction side, List<PipeTileEntity.Connection> connections, long maxReceive, TransactionContext transaction) {
        if (connections.isEmpty() || maxReceive <= 0) {
            return 0;
        }
        if (tileEntity.pushRecursion()) {
            return 0;
        }
        long actuallyTransferred = 0;
        long energyToTransfer = maxReceive;
        int p = tileEntity.getRoundRobinIndex(side, this) % connections.size();

        List<Pair<EnergyStorage, Integer>> destinations = new ArrayList<>(connections.size());
        for (int i = 0; i < connections.size(); i++) {
            int index = (i + p) % connections.size();

            PipeTileEntity.Connection connection = connections.get(index);
            EnergyStorage destination = connection.getEnergyHandler();
            try (Transaction simulated = Transaction.openNested(transaction)) {
                if (destination != null && !isEnergyFull(destination) && destination.insert(maxReceive, simulated) >= 1) {
                    destinations.add(new Pair<>(destination, index));
                }
            }
        }

        for (Pair<EnergyStorage, Integer> destination : destinations) {
            long maxTransfer = Math.min(Math.max(maxReceive / destinations.size(), 1), energyToTransfer);
            long inserted = destination.getKey().insert(Math.min(maxTransfer, maxReceive), transaction);
            if (inserted > 0) {
                energyToTransfer -= inserted;
                actuallyTransferred += inserted;
            }

            p = destination.getValue() + 1;

            if (energyToTransfer <= 0) {
                break;
            }
        }

        tileEntity.setEnergyRoundRobinIndex(side, p, transaction);

        tileEntity.popRecursion();
        return actuallyTransferred;
    }

    protected void insertOrdered(PipeLogicTileEntity tileEntity, Direction side, List<PipeTileEntity.Connection> connections, EnergyStorage energyHandler) {
        long energyToTransfer = getRate(tileEntity, side);

        for (PipeTileEntity.Connection connection : connections) {
            if (energyToTransfer <= 0) {
                break;
            }
            EnergyStorage destination = connection.getEnergyHandler();
            if (destination == null || isEnergyFull(destination)) {
                continue;
            }
            long simulatedExtract;
            try (Transaction simulated = Transaction.openOuter()) {
                simulatedExtract = energyHandler.extract(energyToTransfer, simulated);
            }
            if (simulatedExtract > 0) {
                energyToTransfer -= moveEnergy(energyHandler, destination, simulatedExtract, null);
            }
        }
    }

    protected long receiveOrdered(PipeLogicTileEntity tileEntity, Direction side, List<PipeTileEntity.Connection> connections, long maxReceive, TransactionContext transaction) {
        if (tileEntity.pushRecursion()) {
            return 0;
        }
        long actuallyTransferred = 0;
        long energyToTransfer = maxReceive;

        for (PipeTileEntity.Connection connection : connections) {
            if (energyToTransfer <= 0) {
                break;
            }
            EnergyStorage destination = connection.getEnergyHandler();
            if (destination == null || isEnergyFull(destination)) {
                continue;
            }

            long extracted = destination.insert(Math.min(energyToTransfer, maxReceive), transaction);
            energyToTransfer -= extracted;
            actuallyTransferred += extracted;
        }
        tileEntity.popRecursion();
        return actuallyTransferred;
    }

    /**
     * Checks whether an energy storage is full.
     */
    private boolean isEnergyFull(EnergyStorage storage) {
        return storage.getAmount() >= storage.getCapacity();
    }

    /**
     * Moves energy from source to destination using the Fabric Transaction API.
     * This replaces NeoForge's EnergyHandlerUtil.move().
     */
    private long moveEnergy(EnergyStorage source, EnergyStorage destination, long maxAmount, @Nullable TransactionContext outerTransaction) {
        try (Transaction transaction = outerTransaction != null ? Transaction.openNested(outerTransaction) : Transaction.openOuter()) {
            long extracted = source.extract(maxAmount, transaction);
            if (extracted <= 0) {
                return 0;
            }
            long inserted = destination.insert(extracted, transaction);
            if (inserted <= 0) {
                return 0;
            }
            // If we inserted less than we extracted, we need to only extract what was inserted
            if (inserted < extracted) {
                // Abort and retry with the correct amount
                // Actually, the transaction system handles this; we just commit what we have
                // But we need to return only what was actually transferred
                transaction.abort();
                // Retry with the exact amount
                try (Transaction retryTx = outerTransaction != null ? Transaction.openNested(outerTransaction) : Transaction.openOuter()) {
                    long retryExtracted = source.extract(inserted, retryTx);
                    if (retryExtracted <= 0) {
                        return 0;
                    }
                    long retryInserted = destination.insert(retryExtracted, retryTx);
                    if (retryInserted > 0) {
                        retryTx.commit();
                        return retryInserted;
                    }
                    return 0;
                }
            }
            transaction.commit();
            return inserted;
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
            return PipezConfig.energyPipeAmount;
        }
        switch (upgrade) {
            case BASIC:
                return PipezConfig.energyPipeAmountBasic;
            case IMPROVED:
                return PipezConfig.energyPipeAmountImproved;
            case ADVANCED:
                return PipezConfig.energyPipeAmountAdvanced;
            case ULTIMATE:
                return PipezConfig.energyPipeAmountUltimate;
            case INFINITY:
            default:
                return Integer.MAX_VALUE;
        }
    }

    @Override
    public DataComponentType<EnergyData> getDataComponentType() {
        return ModItems.ENERGY_DATA_COMPONENT;
    }

    private static final EnergyData DEFAULT = new EnergyData(UpgradeTileEntity.FilterMode.WHITELIST, UpgradeTileEntity.RedstoneMode.IGNORED, UpgradeTileEntity.Distribution.ROUND_ROBIN, Collections.emptyList());

    @Override
    public EnergyData defaultData() {
        return DEFAULT;
    }
}
