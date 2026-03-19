package com.shroototem.pipez.corelib;

import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class VoxelUtils {

    public static VoxelShape combine(VoxelShape... shapes) {
        VoxelShape result = Shapes.empty();
        for (VoxelShape shape : shapes) {
            result = Shapes.or(result, shape);
        }
        return result;
    }
}
