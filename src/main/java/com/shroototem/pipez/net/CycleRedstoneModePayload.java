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

public record CycleRedstoneModePayload(int index) implements CustomPacketPayload {

    public static final Type<CycleRedstoneModePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(PipezMod.MODID, "cycle_redstone_mode"));

    public static final StreamCodec<RegistryFriendlyByteBuf, CycleRedstoneModePayload> CODEC = StreamCodec.of(
            (buf, payload) -> buf.writeInt(payload.index),
            buf -> new CycleRedstoneModePayload(buf.readInt())
    );

    @Override
    public Type<CycleRedstoneModePayload> type() {
        return TYPE;
    }

    public static void handle(CycleRedstoneModePayload payload, ServerPlayNetworking.Context context) {
        ServerPlayer sender = context.player();
        context.server().execute(() -> {
            if (sender.containerMenu instanceof ExtractContainer extractContainer) {
                PipeType<?, ?> pipeType = extractContainer.getPipe().getPipeTypes()[payload.index];
                extractContainer.getPipe().setRedstoneMode(extractContainer.getSide(), pipeType, extractContainer.getPipe().getRedstoneMode(extractContainer.getSide(), pipeType).cycle());
            }
        });
    }
}
