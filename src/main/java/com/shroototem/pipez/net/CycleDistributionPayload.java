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

public record CycleDistributionPayload(int index) implements CustomPacketPayload {

    public static final Type<CycleDistributionPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(PipezMod.MODID, "cycle_distribution"));

    public static final StreamCodec<RegistryFriendlyByteBuf, CycleDistributionPayload> CODEC = StreamCodec.of(
            (buf, payload) -> buf.writeInt(payload.index),
            buf -> new CycleDistributionPayload(buf.readInt())
    );

    @Override
    public Type<CycleDistributionPayload> type() {
        return TYPE;
    }

    public static void handle(CycleDistributionPayload payload, ServerPlayNetworking.Context context) {
        ServerPlayer sender = context.player();
        context.server().execute(() -> {
            if (sender.containerMenu instanceof ExtractContainer extractContainer) {
                PipeType<?, ?> pipeType = extractContainer.getPipe().getPipeTypes()[payload.index];
                extractContainer.getPipe().setDistribution(extractContainer.getSide(), pipeType, extractContainer.getPipe().getDistribution(extractContainer.getSide(), pipeType).cycle());
            }
        });
    }
}
