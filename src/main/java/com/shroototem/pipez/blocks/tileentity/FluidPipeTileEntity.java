package com.shroototem.pipez.blocks.tileentity;

import com.shroototem.pipez.blocks.tileentity.types.FluidPipeType;
import com.shroototem.pipez.blocks.tileentity.types.PipeType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class FluidPipeTileEntity extends PipeLogicTileEntity {

    public FluidPipeTileEntity(BlockPos pos, BlockState state) {
        super(ModTileEntities.FLUID_PIPE, new PipeType[]{FluidPipeType.INSTANCE}, pos, state);
    }

}
