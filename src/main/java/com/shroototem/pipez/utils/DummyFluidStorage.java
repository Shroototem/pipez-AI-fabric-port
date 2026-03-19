package com.shroototem.pipez.utils;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

import java.util.Collections;
import java.util.Iterator;

/**
 * A dummy {@link Storage} for fluids that acts as a sink: accepts everything, stores nothing.
 * Used when a pipe is in extracting mode and needs to present a fluid storage to neighbors
 * without actually holding any fluids.
 */
public class DummyFluidStorage implements Storage<FluidVariant> {

    public static final DummyFluidStorage INSTANCE = new DummyFluidStorage();

    @Override
    public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        return 0;
    }

    @Override
    public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        return 0;
    }

    @Override
    public Iterator<StorageView<FluidVariant>> iterator() {
        return Collections.emptyIterator();
    }
}
