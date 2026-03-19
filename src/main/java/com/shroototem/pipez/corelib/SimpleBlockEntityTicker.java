package com.shroototem.pipez.corelib;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.state.BlockState;

public class SimpleBlockEntityTicker<T extends BlockEntity> implements BlockEntityTicker<T> {

    @Override
    public void tick(Level level, BlockPos pos, BlockState state, T blockEntity) {
        if (blockEntity instanceof ITickableBlockEntity tickable) {
            tickable.tick();
        }
    }
}
