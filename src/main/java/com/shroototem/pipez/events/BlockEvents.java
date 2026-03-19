package com.shroototem.pipez.events;

import com.shroototem.pipez.DirectionalPosition;
import com.shroototem.pipez.blocks.PipeBlock;
import com.shroototem.pipez.items.FilterDestinationToolItem;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class BlockEvents {

    public static InteractionResult onBlockClick(Player player, Level world, InteractionHand hand, BlockHitResult hitResult) {
        InteractionResult destResult = onDestinationToolClick(player, world, hand, hitResult);
        if (destResult.consumesAction()) {
            return destResult;
        }
        return onPipeClick(player, world, hand, hitResult);
    }

    private static InteractionResult onPipeClick(Player player, Level world, InteractionHand hand, BlockHitResult hitResult) {
        BlockState state = world.getBlockState(hitResult.getBlockPos());
        if (!(state.getBlock() instanceof PipeBlock pipe)) {
            return InteractionResult.PASS;
        }

        Direction side = pipe.getSelection(state, world, hitResult.getBlockPos(), player).getKey();
        InteractionResult result = pipe.onPipeSideForceActivated(state, world, hitResult.getBlockPos(), player, hand, hitResult, side);
        if (result.consumesAction()) {
            return result;
        }
        return InteractionResult.PASS;
    }

    private static InteractionResult onDestinationToolClick(Player player, Level world, InteractionHand hand, BlockHitResult hitResult) {
        ItemStack heldItem = player.getItemInHand(hand);
        if (!(heldItem.getItem() instanceof FilterDestinationToolItem)) {
            return InteractionResult.PASS;
        }

        BlockEntity te = world.getBlockEntity(hitResult.getBlockPos());
        if (te == null) {
            return InteractionResult.PASS;
        }

        BlockState blockState = world.getBlockState(hitResult.getBlockPos());
        if (blockState.getBlock() instanceof PipeBlock) {
            return InteractionResult.PASS;
        }

        FilterDestinationToolItem.setDestination(heldItem, new DirectionalPosition(hitResult.getBlockPos().immutable(), hitResult.getDirection()));
        player.displayClientMessage(Component.translatable("message.pipez.filter_destination_tool.destination.set"), true);
        return InteractionResult.SUCCESS;
    }
}
