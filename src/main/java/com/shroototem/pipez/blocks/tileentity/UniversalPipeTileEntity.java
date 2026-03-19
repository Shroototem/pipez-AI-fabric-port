package com.shroototem.pipez.blocks.tileentity;

import com.shroototem.pipez.blocks.tileentity.types.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class UniversalPipeTileEntity extends PipeLogicTileEntity {

    public UniversalPipeTileEntity(BlockPos pos, BlockState state) {
        super(ModTileEntities.UNIVERSAL_PIPE, new PipeType[]{ItemPipeType.INSTANCE, FluidPipeType.INSTANCE, EnergyPipeType.INSTANCE}, pos, state);
    }

}
