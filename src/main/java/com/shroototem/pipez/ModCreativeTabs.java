package com.shroototem.pipez;

import com.shroototem.pipez.blocks.ModBlocks;
import com.shroototem.pipez.items.ModItems;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class ModCreativeTabs {

    public static final CreativeModeTab TAB_PIPEZ = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB,
            ResourceLocation.fromNamespaceAndPath("pipez", "pipez"),
            FabricItemGroup.builder()
                    .icon(() -> new ItemStack(ModBlocks.ITEM_PIPE))
                    .displayItems((features, output) -> {
                        output.accept(new ItemStack(ModBlocks.ITEM_PIPE));
                        output.accept(new ItemStack(ModBlocks.FLUID_PIPE));
                        output.accept(new ItemStack(ModBlocks.ENERGY_PIPE));
                        output.accept(new ItemStack(ModBlocks.UNIVERSAL_PIPE));
                        output.accept(new ItemStack(ModBlocks.GAS_PIPE));

                        output.accept(new ItemStack(ModItems.BASIC_UPGRADE));
                        output.accept(new ItemStack(ModItems.IMPROVED_UPGRADE));
                        output.accept(new ItemStack(ModItems.ADVANCED_UPGRADE));
                        output.accept(new ItemStack(ModItems.ULTIMATE_UPGRADE));
                        output.accept(new ItemStack(ModItems.INFINITY_UPGRADE));

                        output.accept(new ItemStack(ModItems.WRENCH));
                        output.accept(new ItemStack(ModItems.FILTER_DESTINATION_TOOL));
                    })
                    .title(Component.translatable("itemGroup.pipez"))
                    .build());

    public static void init() {
        // Triggers static initialization of this class, registering the creative tab
    }

}
