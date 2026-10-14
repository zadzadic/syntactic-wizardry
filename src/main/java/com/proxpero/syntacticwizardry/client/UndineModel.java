package com.proxpero.syntacticwizardry.client;

import com.proxpero.syntacticwizardry.SyntacticWizardry;
import com.proxpero.syntacticwizardry.UndineEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public final class UndineModel extends GeoModel<UndineEntity> {
    private static ResourceLocation asset(String path) {
        return ResourceLocation.fromNamespaceAndPath(SyntacticWizardry.MOD_ID, path);
    }
    @Override public ResourceLocation getModelResource(UndineEntity entity) {
        return asset("geo/undine.geo.json");
    }
    @Override public ResourceLocation getTextureResource(UndineEntity entity) {
        return asset("textures/entity/undine.png");
    }
    @Override public ResourceLocation getAnimationResource(UndineEntity entity) {
        return asset("animations/undine.animation.json");
    }
}
