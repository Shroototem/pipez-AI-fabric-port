package com.shroototem.pipez.blocks;

import com.shroototem.pipez.blocks.tileentity.FluidPipeTileEntity;
import com.shroototem.pipez.gui.ExtractContainer;
import com.shroototem.pipez.gui.containerfactory.PipeContainerProvider;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class FluidPipeBlock extends PipeBlock {

    protected FluidPipeBlock(Properties properties) {
        super(properties);
    }

    @Override
    public boolean canConnectTo(Level world, BlockPos pos, Direction facing) {
        return FluidStorage.SIDED.find(world, pos.relative(facing), facing.getOpposite()) != null;
    }

    @Override
    public boolean isPipe(LevelAccessor world, BlockPos pos, Direction facing) {
        BlockState state = world.getBlockState(pos.relative(facing));
        return state.getBlock().equals(this);
    }

    @Override
    BlockEntity createTileEntity(BlockPos pos, BlockState state) {
        return new FluidPipeTileEntity(pos, state);
    }

    @Override
    public InteractionResult onPipeSideActivated(ItemStack stack, BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit, Direction direction) {
        BlockEntity tileEntity = worldIn.getBlockEntity(pos);
        if (tileEntity instanceof FluidPipeTileEntity && isExtracting(worldIn, pos, direction)) {
            if (worldIn.isClientSide()) {
                return InteractionResult.SUCCESS;
            }
            FluidPipeTileEntity pipe = (FluidPipeTileEntity) tileEntity;
            PipeContainerProvider.openGui(player, pipe, direction, -1, (i, playerInventory, playerEntity) -> new ExtractContainer(i, playerInventory, pipe, direction, -1));
            return InteractionResult.SUCCESS;
        }
        return super.onPipeSideActivated(stack, state, worldIn, pos, player, handIn, hit, direction);
    }
}
