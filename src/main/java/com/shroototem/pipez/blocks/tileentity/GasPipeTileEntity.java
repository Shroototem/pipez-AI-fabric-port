package com.shroototem.pipez.blocks.tileentity;

import com.shroototem.pipez.blocks.tileentity.types.PipeType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Stub for GasPipeTileEntity - Mekanism is not available on Fabric.
 * This class exists to maintain registry compatibility but has no gas pipe types.
 * It extends PipeLogicTileEntity with an empty type array.
 */
public class GasPipeTileEntity extends PipeLogicTileEntity {

    public GasPipeTileEntity(BlockPos pos, BlockState state) {
        super(ModTileEntities.GAS_PIPE, new PipeType[]{}, pos, state);
    }

}
