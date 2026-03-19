package com.shroototem.pipez.net;

import com.shroototem.pipez.PipezMod;
import com.shroototem.pipez.gui.ExtractContainer;
import com.shroototem.pipez.gui.FilterContainer;
import com.shroototem.pipez.gui.containerfactory.PipeContainerProvider;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public record OpenExtractPayload(int index) implements CustomPacketPayload {

    public static final Type<OpenExtractPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(PipezMod.MODID, "open_extract"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenExtractPayload> CODEC = StreamCodec.of(
            (buf, payload) -> buf.writeInt(payload.index),
            buf -> new OpenExtractPayload(buf.readInt())
    );

    @Override
    public Type<OpenExtractPayload> type() {
        return TYPE;
    }

    public static void handle(OpenExtractPayload payload, ServerPlayNetworking.Context context) {
        ServerPlayer sender = context.player();
        context.server().execute(() -> {
            if (sender.containerMenu instanceof FilterContainer filterContainer) {
                PipeContainerProvider.openGui(sender, filterContainer.getPipe(), filterContainer.getSide(), payload.index, (id, playerInventory, playerEntity) -> new ExtractContainer(id, playerInventory, filterContainer.getPipe(), filterContainer.getSide(), payload.index));
            }
        });
    }
}
