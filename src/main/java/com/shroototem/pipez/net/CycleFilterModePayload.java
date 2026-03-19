package com.shroototem.pipez.net;

import com.shroototem.pipez.PipezMod;
import com.shroototem.pipez.blocks.tileentity.types.PipeType;
import com.shroototem.pipez.gui.ExtractContainer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public record CycleFilterModePayload(int index) implements CustomPacketPayload {

    public static final Type<CycleFilterModePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(PipezMod.MODID, "cycle_filter_mode"));

    public static final StreamCodec<RegistryFriendlyByteBuf, CycleFilterModePayload> CODEC = StreamCodec.of(
            (buf, payload) -> buf.writeInt(payload.index),
            buf -> new CycleFilterModePayload(buf.readInt())
    );

    @Override
    public Type<CycleFilterModePayload> type() {
        return TYPE;
    }

    public static void handle(CycleFilterModePayload payload, ServerPlayNetworking.Context context) {
        ServerPlayer sender = context.player();
        context.server().execute(() -> {
            if (sender.containerMenu instanceof ExtractContainer extractContainer) {
                PipeType<?, ?> pipeType = extractContainer.getPipe().getPipeTypes()[payload.index];
                extractContainer.getPipe().setFilterMode(extractContainer.getSide(), pipeType, extractContainer.getPipe().getFilterMode(extractContainer.getSide(), pipeType).cycle());
            }
        });
    }
}
