package com.shroototem.pipez;

import com.shroototem.pipez.blocks.tileentity.ModTileEntities;
import com.shroototem.pipez.blocks.tileentity.render.*;
import com.shroototem.pipez.gui.Containers;
import com.shroototem.pipez.gui.ExtractScreen;
import com.shroototem.pipez.gui.FilterScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.world.level.block.entity.BlockEntityType;

@SuppressWarnings("unchecked")
public class PipezClientMod implements ClientModInitializer {

    private static <E extends BlockEntity, S extends BlockEntityRenderState> void registerRenderer(
            BlockEntityType<?> type, BlockEntityRendererProvider<?, ?> provider) {
        BlockEntityRendererRegistry.register((BlockEntityType<E>) type, (BlockEntityRendererProvider<? super E, ? super S>) provider);
    }

    private static BlockEntityRendererProvider<?, ?> pipeProvider(java.util.function.Function<BlockEntityRendererProvider.Context, PipeRenderer> factory) {
        return (BlockEntityRendererProvider<BlockEntity, BlockEntityRenderState>) (ctx -> (BlockEntityRenderer<BlockEntity, BlockEntityRenderState>) (BlockEntityRenderer<?, ?>) factory.apply(ctx));
    }

    @Override
    public void onInitializeClient() {
        // Register extra models for pipe extract overlays
        ModelRegistry.register();

        // Register block entity renderers
        registerRenderer(ModTileEntities.ITEM_PIPE, pipeProvider(ItemPipeRenderer::new));
        registerRenderer(ModTileEntities.FLUID_PIPE, pipeProvider(FluidPipeRenderer::new));
        registerRenderer(ModTileEntities.ENERGY_PIPE, pipeProvider(EnergyPipeRenderer::new));
        registerRenderer(ModTileEntities.GAS_PIPE, pipeProvider(GasPipeRenderer::new));
        registerRenderer(ModTileEntities.UNIVERSAL_PIPE, pipeProvider(UniversalPipeRenderer::new));

        // Register menu screens
        MenuScreens.register(Containers.EXTRACT, ExtractScreen::new);
        MenuScreens.register(Containers.FILTER, FilterScreen::new);
    }
}
