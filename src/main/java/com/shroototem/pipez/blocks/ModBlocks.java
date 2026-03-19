package com.shroototem.pipez.blocks;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class ModBlocks {

    public static final ItemPipeBlock ITEM_PIPE = Registry.register(BuiltInRegistries.BLOCK,
            ResourceLocation.fromNamespaceAndPath("pipez", "item_pipe"),
            new ItemPipeBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("pipez", "item_pipe")))));
    public static final FluidPipeBlock FLUID_PIPE = Registry.register(BuiltInRegistries.BLOCK,
            ResourceLocation.fromNamespaceAndPath("pipez", "fluid_pipe"),
            new FluidPipeBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("pipez", "fluid_pipe")))));
    public static final EnergyPipeBlock ENERGY_PIPE = Registry.register(BuiltInRegistries.BLOCK,
            ResourceLocation.fromNamespaceAndPath("pipez", "energy_pipe"),
            new EnergyPipeBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("pipez", "energy_pipe")))));
    public static final UniversalPipeBlock UNIVERSAL_PIPE = Registry.register(BuiltInRegistries.BLOCK,
            ResourceLocation.fromNamespaceAndPath("pipez", "universal_pipe"),
            new UniversalPipeBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("pipez", "universal_pipe")))));
    public static final GasPipeBlock GAS_PIPE = Registry.register(BuiltInRegistries.BLOCK,
            ResourceLocation.fromNamespaceAndPath("pipez", "gas_pipe"),
            new GasPipeBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("pipez", "gas_pipe")))));

    public static void init() {
        // Triggers static initialization of this class, registering all blocks
    }

}
