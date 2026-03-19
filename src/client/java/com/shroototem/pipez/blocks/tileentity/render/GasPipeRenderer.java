package com.shroototem.pipez.blocks.tileentity.render;

import com.shroototem.pipez.ModelRegistry.Model;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class GasPipeRenderer extends PipeRenderer {

    public GasPipeRenderer(BlockEntityRendererProvider.Context renderer) {
        super(renderer);
    }

    @Override
    Model getModel() {
        return Model.GAS_PIPE_EXTRACT;
    }
}
