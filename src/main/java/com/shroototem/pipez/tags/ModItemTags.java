package com.shroototem.pipez.tags;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class ModItemTags {

    public static final String MODID = "pipez";

    public static final TagKey<Item> WRENCHES_TAG = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "wrenches"));
    public static final TagKey<Item> WRENCH_TAG = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "tools/wrench"));
    public static final TagKey<Item> TOOLS_TAG = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "tools"));
    public static final TagKey<Item> UPGRADES_TAG = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MODID, "upgrades"));

}
