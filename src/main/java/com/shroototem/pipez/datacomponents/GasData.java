package com.shroototem.pipez.datacomponents;

import com.mojang.serialization.Codec;
import com.shroototem.pipez.Filter;
import com.shroototem.pipez.blocks.tileentity.UpgradeTileEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;

/**
 * Gas data component for gas pipe upgrades.
 * On Fabric, Mekanism is not available, so this uses Void as the type parameter
 * and null filter codecs (no gas filters). The structure is kept for data compatibility.
 */
public class GasData extends AbstractPipeTypeData<Void> {

    public static final Codec<GasData> CODEC = codec(GasData.class, null);
    public static final StreamCodec<RegistryFriendlyByteBuf, GasData> STREAM_CODEC = streamCodec(GasData.class, null);

    public GasData(UpgradeTileEntity.FilterMode filterMode, UpgradeTileEntity.RedstoneMode redstoneMode, UpgradeTileEntity.Distribution distribution, List<Filter<?, Void>> filters) {
        super(filterMode, redstoneMode, distribution, filters);
    }

    @Override
    public GasDataBuilder builder() {
        return new GasDataBuilder(this);
    }

    public static class GasDataBuilder extends PipeTypeDataBuilder<GasData, GasDataBuilder, Void> {

        public GasDataBuilder(GasData data) {
            super(data);
        }

        @Override
        public GasData build() {
            return new GasData(filterMode, redstoneMode, distribution, filters);
        }

    }

}
