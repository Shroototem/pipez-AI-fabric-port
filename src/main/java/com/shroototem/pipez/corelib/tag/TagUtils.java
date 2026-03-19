package com.shroototem.pipez.corelib.tag;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import org.jetbrains.annotations.Nullable;
import java.util.List;
import java.util.Optional;

/**
 * Utility class for creating Tag instances from registry entries and Minecraft tags.
 */
public class TagUtils {

    /**
     * Gets a Tag for an item, either a single item or an item tag (prefixed with #).
     */
    @Nullable
    public static Tag<Item> getItem(String name, boolean nullIfNotExists) {
        if (name.startsWith("#")) {
            ResourceLocation location = ResourceLocation.tryParse(name.substring(1));
            if (location == null) {
                return nullIfNotExists ? null : new SingleElementTag<>(ResourceLocation.withDefaultNamespace("air"), Items.AIR);
            }
            return getItemTag(location);
        } else {
            ResourceLocation location = ResourceLocation.tryParse(name);
            if (location == null) {
                return nullIfNotExists ? null : new SingleElementTag<>(ResourceLocation.withDefaultNamespace("air"), Items.AIR);
            }
            Optional<Holder.Reference<Item>> holder = BuiltInRegistries.ITEM.get(location);
            if (holder.isEmpty()) {
                return nullIfNotExists ? null : new SingleElementTag<>(ResourceLocation.withDefaultNamespace("air"), Items.AIR);
            }
            return new SingleElementTag<>(location, holder.get().value());
        }
    }

    /**
     * Gets a Tag representing a Minecraft item tag.
     */
    public static Tag<Item> getItemTag(ResourceLocation location) {
        TagKey<Item> tagKey = TagKey.create(BuiltInRegistries.ITEM.key(), location);
        return new RegistryTag<>(tagKey, BuiltInRegistries.ITEM, location);
    }

    /**
     * Gets a Tag for a fluid, either a single fluid or a fluid tag (prefixed with #).
     */
    @Nullable
    public static Tag<Fluid> getFluid(String name, boolean nullIfNotExists) {
        if (name.startsWith("#")) {
            ResourceLocation location = ResourceLocation.tryParse(name.substring(1));
            if (location == null) {
                return nullIfNotExists ? null : new SingleElementTag<>(ResourceLocation.withDefaultNamespace("empty"), Fluids.EMPTY);
            }
            return getFluidTag(location);
        } else {
            ResourceLocation location = ResourceLocation.tryParse(name);
            if (location == null) {
                return nullIfNotExists ? null : new SingleElementTag<>(ResourceLocation.withDefaultNamespace("empty"), Fluids.EMPTY);
            }
            Optional<Holder.Reference<Fluid>> holder = BuiltInRegistries.FLUID.get(location);
            if (holder.isEmpty()) {
                return nullIfNotExists ? null : new SingleElementTag<>(ResourceLocation.withDefaultNamespace("empty"), Fluids.EMPTY);
            }
            return new SingleElementTag<>(location, holder.get().value());
        }
    }

    /**
     * Gets a Tag representing a Minecraft fluid tag.
     */
    public static Tag<Fluid> getFluidTag(ResourceLocation location) {
        TagKey<Fluid> tagKey = TagKey.create(BuiltInRegistries.FLUID.key(), location);
        return new RegistryTag<>(tagKey, BuiltInRegistries.FLUID, location);
    }

    /**
     * A tag backed by a Minecraft registry tag.
     */
    private static class RegistryTag<T> implements Tag<T> {
        private final TagKey<T> tagKey;
        private final net.minecraft.core.Registry<T> registry;
        private final ResourceLocation name;

        public RegistryTag(TagKey<T> tagKey, net.minecraft.core.Registry<T> registry, ResourceLocation name) {
            this.tagKey = tagKey;
            this.registry = registry;
            this.name = name;
        }

        @Override
        public ResourceLocation getName() {
            return name;
        }

        @Override
        public boolean contains(T element) {
            return registry.get(tagKey)
                    .map(holders -> holders.stream().anyMatch(holder -> holder.value().equals(element)))
                    .orElse(false);
        }

        @Override
        public List<T> getAll() {
            Optional<HolderSet.Named<T>> holderSet = registry.get(tagKey);
            if (holderSet.isEmpty()) {
                return List.of();
            }
            return holderSet.get().stream().map(Holder::value).toList();
        }
    }

}
