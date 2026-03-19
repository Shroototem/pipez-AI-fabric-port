package com.shroototem.pipez.corelib.tag;

import net.minecraft.resources.ResourceLocation;

import java.util.List;

/**
 * A tag abstraction that can represent either a single element or a Minecraft tag group.
 */
public interface Tag<T> {

    ResourceLocation getName();

    boolean contains(T element);

    List<T> getAll();

}
