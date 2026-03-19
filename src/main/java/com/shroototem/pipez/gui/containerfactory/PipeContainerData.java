package com.shroototem.pipez.gui.containerfactory;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record PipeContainerData(BlockPos pos, Direction side, int index) {

    public static final StreamCodec<RegistryFriendlyByteBuf, PipeContainerData> STREAM_CODEC = StreamCodec.of(
            (buf, data) -> {
                buf.writeBlockPos(data.pos);
                buf.writeEnum(data.side);
                buf.writeInt(data.index);
            },
            buf -> new PipeContainerData(buf.readBlockPos(), buf.readEnum(Direction.class), buf.readInt())
    );
}
