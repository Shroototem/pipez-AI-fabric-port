package com.shroototem.pipez.items;

import com.shroototem.pipez.DirectionalPosition;
import com.shroototem.pipez.Upgrade;
import com.shroototem.pipez.blocks.ModBlocks;
import com.shroototem.pipez.datacomponents.EnergyData;
import com.shroototem.pipez.datacomponents.FluidData;
import com.shroototem.pipez.datacomponents.GasData;
import com.shroototem.pipez.datacomponents.ItemData;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

public class ModItems {

    // Upgrade items
    public static final UpgradeItem BASIC_UPGRADE = Registry.register(BuiltInRegistries.ITEM,
            ResourceLocation.fromNamespaceAndPath("pipez", Upgrade.BASIC.getName() + "_upgrade"),
            new UpgradeItem(Upgrade.BASIC, new Item.Properties().setId(ResourceKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("pipez", "basic_upgrade")))));
    public static final UpgradeItem IMPROVED_UPGRADE = Registry.register(BuiltInRegistries.ITEM,
            ResourceLocation.fromNamespaceAndPath("pipez", Upgrade.IMPROVED.getName() + "_upgrade"),
            new UpgradeItem(Upgrade.IMPROVED, new Item.Properties().setId(ResourceKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("pipez", "improved_upgrade")))));
    public static final UpgradeItem ADVANCED_UPGRADE = Registry.register(BuiltInRegistries.ITEM,
            ResourceLocation.fromNamespaceAndPath("pipez", Upgrade.ADVANCED.getName() + "_upgrade"),
            new UpgradeItem(Upgrade.ADVANCED, new Item.Properties().setId(ResourceKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("pipez", "advanced_upgrade")))));
    public static final UpgradeItem ULTIMATE_UPGRADE = Registry.register(BuiltInRegistries.ITEM,
            ResourceLocation.fromNamespaceAndPath("pipez", Upgrade.ULTIMATE.getName() + "_upgrade"),
            new UpgradeItem(Upgrade.ULTIMATE, new Item.Properties().setId(ResourceKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("pipez", "ultimate_upgrade")))));
    public static final UpgradeItem INFINITY_UPGRADE = Registry.register(BuiltInRegistries.ITEM,
            ResourceLocation.fromNamespaceAndPath("pipez", Upgrade.INFINITY.getName() + "_upgrade"),
            new UpgradeItem(Upgrade.INFINITY, new Item.Properties().setId(ResourceKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("pipez", "infinity_upgrade")))));

    // Tool items
    public static final WrenchItem WRENCH = Registry.register(BuiltInRegistries.ITEM,
            ResourceLocation.fromNamespaceAndPath("pipez", "wrench"),
            new WrenchItem(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("pipez", "wrench")))));
    public static final FilterDestinationToolItem FILTER_DESTINATION_TOOL = Registry.register(BuiltInRegistries.ITEM,
            ResourceLocation.fromNamespaceAndPath("pipez", "filter_destination_tool"),
            new FilterDestinationToolItem(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("pipez", "filter_destination_tool")))));

    // Block items for pipes
    public static final BlockItem ITEM_PIPE = Registry.register(BuiltInRegistries.ITEM,
            ResourceLocation.fromNamespaceAndPath("pipez", "item_pipe"),
            new BlockItem(ModBlocks.ITEM_PIPE, new Item.Properties().setId(ResourceKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("pipez", "item_pipe"))).useBlockDescriptionPrefix()));
    public static final BlockItem FLUID_PIPE = Registry.register(BuiltInRegistries.ITEM,
            ResourceLocation.fromNamespaceAndPath("pipez", "fluid_pipe"),
            new BlockItem(ModBlocks.FLUID_PIPE, new Item.Properties().setId(ResourceKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("pipez", "fluid_pipe"))).useBlockDescriptionPrefix()));
    public static final BlockItem ENERGY_PIPE = Registry.register(BuiltInRegistries.ITEM,
            ResourceLocation.fromNamespaceAndPath("pipez", "energy_pipe"),
            new BlockItem(ModBlocks.ENERGY_PIPE, new Item.Properties().setId(ResourceKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("pipez", "energy_pipe"))).useBlockDescriptionPrefix()));
    public static final BlockItem UNIVERSAL_PIPE = Registry.register(BuiltInRegistries.ITEM,
            ResourceLocation.fromNamespaceAndPath("pipez", "universal_pipe"),
            new BlockItem(ModBlocks.UNIVERSAL_PIPE, new Item.Properties().setId(ResourceKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("pipez", "universal_pipe"))).useBlockDescriptionPrefix()));
    public static final BlockItem GAS_PIPE = Registry.register(BuiltInRegistries.ITEM,
            ResourceLocation.fromNamespaceAndPath("pipez", "gas_pipe"),
            new BlockItem(ModBlocks.GAS_PIPE, new Item.Properties().setId(ResourceKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("pipez", "gas_pipe"))).useBlockDescriptionPrefix()));

    // Data component types
    public static final DataComponentType<DirectionalPosition> DIRECTIONAL_POSITION_DATA_COMPONENT = Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE,
            ResourceLocation.fromNamespaceAndPath("pipez", "directional_position"),
            DataComponentType.<DirectionalPosition>builder().persistent(DirectionalPosition.CODEC).networkSynchronized(DirectionalPosition.STREAM_CODEC).build());

    public static final DataComponentType<ItemData> ITEM_DATA_COMPONENT = Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE,
            ResourceLocation.fromNamespaceAndPath("pipez", "item"),
            DataComponentType.<ItemData>builder().persistent(ItemData.CODEC).networkSynchronized(ItemData.STREAM_CODEC).build());
    public static final DataComponentType<FluidData> FLUID_DATA_COMPONENT = Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE,
            ResourceLocation.fromNamespaceAndPath("pipez", "fluid"),
            DataComponentType.<FluidData>builder().persistent(FluidData.CODEC).networkSynchronized(FluidData.STREAM_CODEC).build());
    public static final DataComponentType<GasData> GAS_DATA_COMPONENT = Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE,
            ResourceLocation.fromNamespaceAndPath("pipez", "gas"),
            DataComponentType.<GasData>builder().persistent(GasData.CODEC).networkSynchronized(GasData.STREAM_CODEC).build());
    public static final DataComponentType<EnergyData> ENERGY_DATA_COMPONENT = Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE,
            ResourceLocation.fromNamespaceAndPath("pipez", "energy"),
            DataComponentType.<EnergyData>builder().persistent(EnergyData.CODEC).networkSynchronized(EnergyData.STREAM_CODEC).build());

    public static void init() {
        // Triggers static initialization of this class, registering all items and data components
    }

}
