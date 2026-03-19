package com.shroototem.pipez;

import net.fabricmc.fabric.api.client.model.loading.v1.ExtraModelKey;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.UnbakedExtraModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.QuadCollection;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.Nullable;

public class ModelRegistry {

    public enum Model {
        ENERGY_PIPE_EXTRACT(ResourceLocation.fromNamespaceAndPath(PipezMod.MODID, "block/energy_pipe_extract")),
        FLUID_PIPE_EXTRACT(ResourceLocation.fromNamespaceAndPath(PipezMod.MODID, "block/fluid_pipe_extract")),
        GAS_PIPE_EXTRACT(ResourceLocation.fromNamespaceAndPath(PipezMod.MODID, "block/gas_pipe_extract")),
        ITEM_PIPE_EXTRACT(ResourceLocation.fromNamespaceAndPath(PipezMod.MODID, "block/item_pipe_extract")),
        UNIVERSAL_PIPE_EXTRACT(ResourceLocation.fromNamespaceAndPath(PipezMod.MODID, "block/universal_pipe_extract"));

        private final ResourceLocation resource;
        private final ExtraModelKey<QuadCollection> extraModelKey;

        Model(ResourceLocation rl) {
            resource = rl;
            extraModelKey = ExtraModelKey.create();
        }

        public ResourceLocation getResourceLocation() {
            return resource;
        }

        public ExtraModelKey<QuadCollection> getExtraModelKey() {
            return extraModelKey;
        }

        @Nullable
        public QuadCollection getQuadCollection() {
            Minecraft mc = Minecraft.getInstance();
            if (mc == null || mc.getModelManager() == null) {
                return null;
            }
            return mc.getModelManager().getModel(extraModelKey);
        }
    }

    public static void register() {
        ModelLoadingPlugin.register(pluginCtx -> {
            for (Model model : Model.values()) {
                final ResourceLocation rl = model.getResourceLocation();
                pluginCtx.addModel(model.getExtraModelKey(), new UnbakedExtraModel<QuadCollection>() {
                    @Override
                    public void resolveDependencies(ResolvableModel.Resolver resolver) {
                        resolver.markDependency(rl);
                    }

                    @Override
                    public QuadCollection bake(ModelBaker baker) {
                        ResolvedModel resolved = baker.getModel(rl);
                        return resolved.bakeTopGeometry(resolved.getTopTextureSlots(), baker, new ModelState() {});
                    }
                });
            }
        });
    }

    public static void invalidateAll() {
        // With ExtraModelKey, models are managed by the ModelManager and
        // automatically rebaked on resource reload. No manual invalidation needed.
    }
}
