package com.shroototem.pipez.net;

import com.shroototem.pipez.Filter;
import com.shroototem.pipez.PipezMod;
import com.shroototem.pipez.blocks.tileentity.types.PipeType;
import com.shroototem.pipez.gui.ExtractContainer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.UUID;

public record RemoveFilterPayload(UUID filter, int index) implements CustomPacketPayload {

    public static final Type<RemoveFilterPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(PipezMod.MODID, "remove_filter"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RemoveFilterPayload> CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeUUID(payload.filter);
                buf.writeInt(payload.index);
            },
            buf -> new RemoveFilterPayload(buf.readUUID(), buf.readInt())
    );

    @Override
    public Type<RemoveFilterPayload> type() {
        return TYPE;
    }

    public static void handle(RemoveFilterPayload payload, ServerPlayNetworking.Context context) {
        ServerPlayer sender = context.player();
        context.server().execute(() -> {
            if (sender.containerMenu instanceof ExtractContainer extractContainer) {
                PipeType<?, ?> pipeType = extractContainer.getPipe().getPipeTypes()[payload.index];
                List<Filter<?, ?>> filters = extractContainer.getPipe().getFilters(extractContainer.getSide(), pipeType);
                filters.removeIf(f -> f.getId().equals(payload.filter));
                extractContainer.getPipe().setFilters(extractContainer.getSide(), pipeType, filters);
            }
        });
    }
}
