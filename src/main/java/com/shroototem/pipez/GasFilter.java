package com.shroototem.pipez;

import com.mojang.serialization.Codec;
import com.shroototem.pipez.corelib.tag.Tag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import org.jetbrains.annotations.Nullable;
import java.util.UUID;

/**
 * Stub GasFilter for Fabric. Mekanism is not available on Fabric,
 * so this class exists only to allow references to compile.
 * The generic type parameter uses Void as a placeholder since
 * Chemical from Mekanism is not available.
 */
public class GasFilter extends Filter<GasFilter, Void> {

    // Stub codecs that will never actually be used since Mekanism is not on Fabric.
    // They are defined to satisfy the abstract method contracts from Filter.
    @SuppressWarnings("unchecked")
    public static final Codec<GasFilter> CODEC = codec(GasFilter.class, Codec.unit((Tag<Void>) null));

    @SuppressWarnings("unchecked")
    public static final StreamCodec<RegistryFriendlyByteBuf, GasFilter> STREAM_CODEC = streamCodec(GasFilter.class, StreamCodec.unit((Tag<Void>) null));

    public GasFilter(UUID id, @Nullable Tag<Void> tag, @Nullable CompoundTag metadata, boolean exactMetadata, @Nullable DirectionalPosition destination, boolean invert) {
        super(id, tag, metadata, exactMetadata, destination, invert);
    }

    public GasFilter() {
        this(UUID.randomUUID(), null, null, false, null, false);
    }

    @Override
    public Codec<GasFilter> getCodec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, GasFilter> getStreamCodec() {
        return STREAM_CODEC;
    }

}
