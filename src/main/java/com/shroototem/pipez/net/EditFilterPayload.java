package com.shroototem.pipez.net;

import com.shroototem.pipez.Filter;
import com.shroototem.pipez.PipezMod;
import com.shroototem.pipez.gui.ExtractContainer;
import com.shroototem.pipez.gui.FilterContainer;
import com.shroototem.pipez.gui.containerfactory.FilterContainerProvider;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public record EditFilterPayload(CompoundTag filterTag, int index) implements CustomPacketPayload {

    public static final Type<EditFilterPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(PipezMod.MODID, "edit_filter_message"));

    public static final StreamCodec<RegistryFriendlyByteBuf, EditFilterPayload> CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeNbt(payload.filterTag);
                buf.writeInt(payload.index);
            },
            buf -> new EditFilterPayload(buf.readNbt(), buf.readInt())
    );

    public static EditFilterPayload create(Filter<?, ?> filter, int index) {
        return new EditFilterPayload(filter.toNbt(), index);
    }

    @Override
    public Type<EditFilterPayload> type() {
        return TYPE;
    }

    public static void handle(EditFilterPayload payload, ServerPlayNetworking.Context context) {
        ServerPlayer sender = context.player();
        context.server().execute(() -> {
            if (!(sender.containerMenu instanceof ExtractContainer extractContainer)) {
                return;
            }
            Filter<?, ?> filter = extractContainer.getPipe().getPipeTypes()[payload.index].createFilter();
            if (filter == null) {
                return;
            }
            filter = filter.fromNbt(payload.filterTag);
            Filter<?, ?> finalFilter = filter;
            FilterContainerProvider.openGui(sender, extractContainer.getPipe(), extractContainer.getSide(), finalFilter, payload.index, (id, playerInventory, playerEntity) -> new FilterContainer(id, playerInventory, extractContainer.getPipe(), extractContainer.getSide(), payload.index, finalFilter));
        });
    }
}
