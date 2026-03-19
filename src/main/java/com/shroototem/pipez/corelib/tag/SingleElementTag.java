package com.shroototem.pipez.corelib.tag;

import net.minecraft.resources.ResourceLocation;

import java.util.Collections;
import java.util.List;

/**
 * A tag that contains a single element.
 */
public class SingleElementTag<T> implements Tag<T> {

    private final ResourceLocation name;
    private final T element;

    public SingleElementTag(ResourceLocation name, T element) {
        this.name = name;
        this.element = element;
    }

    @Override
    public ResourceLocation getName() {
        return name;
    }

    public T getElement() {
        return element;
    }

    @Override
    public boolean contains(T element) {
        return this.element.equals(element);
    }

    @Override
    public List<T> getAll() {
        return Collections.singletonList(element);
    }

}
