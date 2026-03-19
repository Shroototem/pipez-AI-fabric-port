package com.shroototem.pipez.blocks.tileentity.render;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.Direction;

public class PipeRenderState extends BlockEntityRenderState {

    public boolean[] extracting = new boolean[Direction.values().length];

}
