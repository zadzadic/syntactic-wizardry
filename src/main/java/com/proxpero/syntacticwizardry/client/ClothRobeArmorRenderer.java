package com.proxpero.syntacticwizardry.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.proxpero.syntacticwizardry.ClothRobeArmorItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.DyedItemColor;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public final class ClothRobeArmorRenderer extends GeoArmorRenderer<ClothRobeArmorItem> {
    private static final int DEFAULT_CLOTH_RGB = 0xF3F0E8;

    public ClothRobeArmorRenderer() {
        super(new ClothRobeArmorModel());
    }

    @Override
    public void renderCubesOfBone(PoseStack poseStack, GeoBone bone, VertexConsumer buffer,
                                  int packedLight, int packedOverlay, int color) {
        int resolvedColor = 0xFFFFFFFF;

        if (bone.getName().startsWith("dye") && currentStack != null) {
            DyedItemColor dyed = currentStack.get(DataComponents.DYED_COLOR);
            int rgb = dyed == null ? DEFAULT_CLOTH_RGB : dyed.rgb();
            resolvedColor = 0xFF000000 | rgb;
        }

        super.renderCubesOfBone(poseStack, bone, buffer, packedLight, packedOverlay, resolvedColor);
    }
}
