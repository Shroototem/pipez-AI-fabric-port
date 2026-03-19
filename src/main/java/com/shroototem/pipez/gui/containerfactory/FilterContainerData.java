package com.shroototem.pipez.gui.containerfactory;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record FilterContainerData(BlockPos pos, Direction side, int index, CompoundTag filterTag) {

    public static final StreamCodec<RegistryFriendlyByteBuf, FilterContainerData> STREAM_CODEC = StreamCodec.of(
            (buf, data) -> {
                buf.writeBlockPos(data.pos);
                buf.writeEnum(data.side);
                buf.writeInt(data.index);
                buf.writeNbt(data.filterTag);
            },
            buf -> new FilterContainerData(buf.readBlockPos(), buf.readEnum(Direction.class), buf.readInt(), buf.readNbt())
    );
}
