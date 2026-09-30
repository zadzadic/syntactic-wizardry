package com.proxpero.syntacticwizardry.client;

import com.proxpero.syntacticwizardry.ClothRobeArmorItem;
import com.proxpero.syntacticwizardry.SyntacticWizardry;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedItemGeoModel;

public final class ClothRobeArmorModel extends DefaultedItemGeoModel<ClothRobeArmorItem> {
    private static final ResourceLocation MODEL =
            ResourceLocation.fromNamespaceAndPath(SyntacticWizardry.MOD_ID, "geo/cloth_robe_armor.geo.json");
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(SyntacticWizardry.MOD_ID, "textures/models/armor/cloth_robe.png");
    private static final ResourceLocation ANIMATION =
            ResourceLocation.fromNamespaceAndPath(SyntacticWizardry.MOD_ID, "animations/cloth_robe.animation.json");

    public ClothRobeArmorModel() {
        super(ResourceLocation.fromNamespaceAndPath(SyntacticWizardry.MOD_ID, "armor/cloth_robe"));
    }

    @Override
    public ResourceLocation getModelResource(ClothRobeArmorItem item) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(ClothRobeArmorItem item) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(ClothRobeArmorItem item) {
        return ANIMATION;
    }
}
