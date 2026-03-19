package com.shroototem.pipez;

import com.shroototem.pipez.blocks.ModBlocks;
import com.shroototem.pipez.blocks.tileentity.ModTileEntities;
import com.shroototem.pipez.config.PipezConfig;
import com.shroototem.pipez.events.BlockEvents;
import com.shroototem.pipez.gui.Containers;
import com.shroototem.pipez.items.ModItems;
import com.shroototem.pipez.net.*;
import com.shroototem.pipez.recipes.ModRecipes;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PipezMod implements ModInitializer {

    public static final String MODID = "pipez";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    @Override
    public void onInitialize() {
        // Load config
        PipezConfig.load();

        // Register blocks, items, block entities, menus, recipes, creative tabs
        ModBlocks.init();
        ModItems.init();
        ModTileEntities.init();
        Containers.init();
        ModRecipes.init();
        ModCreativeTabs.init();

        // Register networking payloads (C2S)
        PayloadTypeRegistry.playC2S().register(CycleDistributionPayload.TYPE, CycleDistributionPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(CycleFilterModePayload.TYPE, CycleFilterModePayload.CODEC);
        PayloadTypeRegistry.playC2S().register(CycleRedstoneModePayload.TYPE, CycleRedstoneModePayload.CODEC);
        PayloadTypeRegistry.playC2S().register(UpdateFilterPayload.TYPE, UpdateFilterPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(RemoveFilterPayload.TYPE, RemoveFilterPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(EditFilterPayload.TYPE, EditFilterPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(OpenExtractPayload.TYPE, OpenExtractPayload.CODEC);

        // Register server-side handlers
        ServerPlayNetworking.registerGlobalReceiver(CycleDistributionPayload.TYPE, CycleDistributionPayload::handle);
        ServerPlayNetworking.registerGlobalReceiver(CycleFilterModePayload.TYPE, CycleFilterModePayload::handle);
        ServerPlayNetworking.registerGlobalReceiver(CycleRedstoneModePayload.TYPE, CycleRedstoneModePayload::handle);
        ServerPlayNetworking.registerGlobalReceiver(UpdateFilterPayload.TYPE, UpdateFilterPayload::handle);
        ServerPlayNetworking.registerGlobalReceiver(RemoveFilterPayload.TYPE, RemoveFilterPayload::handle);
        ServerPlayNetworking.registerGlobalReceiver(EditFilterPayload.TYPE, EditFilterPayload::handle);
        ServerPlayNetworking.registerGlobalReceiver(OpenExtractPayload.TYPE, OpenExtractPayload::handle);

        // Register events
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) ->
                BlockEvents.onBlockClick(player, world, hand, hitResult)
        );

        LOGGER.info("Pipez mod initialized!");
    }
}
