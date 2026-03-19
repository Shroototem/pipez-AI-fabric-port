package com.shroototem.pipez.net;

import com.shroototem.pipez.Filter;
import com.shroototem.pipez.PipezMod;
import com.shroototem.pipez.blocks.tileentity.types.PipeType;
import com.shroototem.pipez.gui.ExtractContainer;
import com.shroototem.pipez.gui.IPipeContainer;
import com.shroototem.pipez.gui.containerfactory.PipeContainerProvider;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.Optional;

public record UpdateFilterPayload(CompoundTag filterTag, int index) implements CustomPacketPayload {

    public static final Type<UpdateFilterPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(PipezMod.MODID, "update_filter"));

    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateFilterPayload> CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeNbt(payload.filterTag);
                buf.writeInt(payload.index);
            },
            buf -> new UpdateFilterPayload(buf.readNbt(), buf.readInt())
    );

    public static UpdateFilterPayload create(Filter<?, ?> filter, int index) {
        return new UpdateFilterPayload(filter.toNbt(), index);
    }

    @Override
    public Type<UpdateFilterPayload> type() {
        return TYPE;
    }

    public static void handle(UpdateFilterPayload payload, ServerPlayNetworking.Context context) {
        ServerPlayer sender = context.player();
        context.server().execute(() -> {
            if (!(sender.containerMenu instanceof IPipeContainer pipeContainer)) {
                return;
            }
            PipeType<?, ?>[] pipeTypes = pipeContainer.getPipe().getPipeTypes();
            if (payload.index >= pipeTypes.length) {
                return;
            }
            PipeType<?, ?> pipeType = pipeTypes[payload.index];
            Filter<?, ?> filter = pipeType.createFilter();
            if (filter == null) {
                return;
            }
            Filter<?, ?> finalFilter = filter.fromNbt(payload.filterTag);

            List<Filter<?, ?>> filters = pipeContainer.getPipe().getFilters(pipeContainer.getSide(), pipeType);

            Optional<Filter<?, ?>> editFilter = filters.stream().filter(f1 -> finalFilter.getId().equals(f1.getId())).findFirst();
            if (editFilter.isPresent()) {
                int idx = filters.indexOf(editFilter.get());
                if (idx >= 0) {
                    filters.set(idx, finalFilter);
                } else {
                    filters.add(finalFilter);
                }
            } else {
                filters.add(finalFilter);
            }
            pipeContainer.getPipe().setFilters(pipeContainer.getSide(), pipeType, filters);

            PipeContainerProvider.openGui(sender, pipeContainer.getPipe(), pipeContainer.getSide(), payload.index, (id, playerInventory, playerEntity) -> new ExtractContainer(id, playerInventory, pipeContainer.getPipe(), pipeContainer.getSide(), payload.index));
        });
    }
}
