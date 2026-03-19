package com.shroototem.pipez.utils;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

import java.util.Collections;
import java.util.Iterator;

/**
 * A dummy {@link Storage} for items that acts as a sink: accepts everything, stores nothing.
 * Used when a pipe is in extracting mode and needs to present an item storage to neighbors
 * without actually holding any items.
 */
public class DummyItemStorage implements Storage<ItemVariant> {

    public static final DummyItemStorage INSTANCE = new DummyItemStorage();

    @Override
    public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        return 0;
    }

    @Override
    public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        return 0;
    }

    @Override
    public Iterator<StorageView<ItemVariant>> iterator() {
        return Collections.emptyIterator();
    }
}
