package com.shroototem.pipez.blocks.tileentity;

import com.shroototem.pipez.blocks.ModBlocks;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import team.reborn.energy.api.EnergyStorage;

public class ModTileEntities {

    public static final BlockEntityType<ItemPipeTileEntity> ITEM_PIPE = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath("pipez", "item_pipe"),
            FabricBlockEntityTypeBuilder.create(ItemPipeTileEntity::new, ModBlocks.ITEM_PIPE).build()
    );

    public static final BlockEntityType<FluidPipeTileEntity> FLUID_PIPE = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath("pipez", "fluid_pipe"),
            FabricBlockEntityTypeBuilder.create(FluidPipeTileEntity::new, ModBlocks.FLUID_PIPE).build()
    );

    public static final BlockEntityType<EnergyPipeTileEntity> ENERGY_PIPE = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath("pipez", "energy_pipe"),
            FabricBlockEntityTypeBuilder.create(EnergyPipeTileEntity::new, ModBlocks.ENERGY_PIPE).build()
    );

    public static final BlockEntityType<GasPipeTileEntity> GAS_PIPE = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath("pipez", "gas_pipe"),
            FabricBlockEntityTypeBuilder.create(GasPipeTileEntity::new, ModBlocks.GAS_PIPE).build()
    );

    public static final BlockEntityType<UniversalPipeTileEntity> UNIVERSAL_PIPE = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath("pipez", "universal_pipe"),
            FabricBlockEntityTypeBuilder.create(UniversalPipeTileEntity::new, ModBlocks.UNIVERSAL_PIPE).build()
    );

    public static void init() {
        // Triggers static initialization, registering all block entity types.
        // Register capabilities for all pipe types.
        registerPipeCapabilities(ITEM_PIPE);
        registerPipeCapabilities(FLUID_PIPE);
        registerPipeCapabilities(ENERGY_PIPE);
        registerPipeCapabilities(GAS_PIPE);
        registerPipeCapabilities(UNIVERSAL_PIPE);
    }

    private static <T extends PipeLogicTileEntity> void registerPipeCapabilities(BlockEntityType<T> type) {
        ItemStorage.SIDED.registerForBlockEntity((blockEntity, direction) ->
                blockEntity.onRegisterCapability(ItemStorage.SIDED, direction), type);
        FluidStorage.SIDED.registerForBlockEntity((blockEntity, direction) ->
                blockEntity.onRegisterCapability(FluidStorage.SIDED, direction), type);
        EnergyStorage.SIDED.registerForBlockEntity((blockEntity, direction) ->
                blockEntity.onRegisterCapability(EnergyStorage.SIDED, direction), type);
    }

    public static void clientSetup() {
        // Block entity renderers are registered on the client side in PipezClientMod.
    }

}
