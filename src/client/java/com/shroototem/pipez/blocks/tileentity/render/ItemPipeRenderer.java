package com.shroototem.pipez.blocks.tileentity.render;

import com.shroototem.pipez.ModelRegistry.Model;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class ItemPipeRenderer extends PipeRenderer {

    public ItemPipeRenderer(BlockEntityRendererProvider.Context renderer) {
        super(renderer);
    }

    @Override
    Model getModel() {
        return Model.ITEM_PIPE_EXTRACT;
    }
}
