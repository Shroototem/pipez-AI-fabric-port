package com.shroototem.pipez.gui;

import com.shroototem.pipez.blocks.tileentity.PipeLogicTileEntity;
import net.minecraft.core.Direction;

public interface IPipeContainer {

    PipeLogicTileEntity getPipe();

    Direction getSide();

}
