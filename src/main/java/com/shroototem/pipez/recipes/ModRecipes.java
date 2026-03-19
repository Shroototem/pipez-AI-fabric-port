package com.shroototem.pipez.recipes;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class ModRecipes {

    public static final CopyComponentsRecipe.Serializer COPY_NBT = Registry.register(BuiltInRegistries.RECIPE_SERIALIZER,
            ResourceLocation.fromNamespaceAndPath("pipez", "copy_components"),
            new CopyComponentsRecipe.Serializer());

    public static final ClearComponentsRecipe.Serializer CLEAR_NBT = Registry.register(BuiltInRegistries.RECIPE_SERIALIZER,
            ResourceLocation.fromNamespaceAndPath("pipez", "clear_components"),
            new ClearComponentsRecipe.Serializer());

    public static void init() {
        // Triggers static initialization of this class, registering all recipe serializers
    }

}
