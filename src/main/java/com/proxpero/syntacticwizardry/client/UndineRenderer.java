package com.proxpero.syntacticwizardry.client;

import com.proxpero.syntacticwizardry.UndineEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/** Alpha-blended water, rather than cutout or an opaque mob texture. */
public final class UndineRenderer extends GeoEntityRenderer<UndineEntity> {
    public UndineRenderer(EntityRendererProvider.Context context) {
        super(context, new UndineModel());
        this.shadowRadius = 0.20F;
    }
    @Override
    public RenderType getRenderType(UndineEntity entity, ResourceLocation texture,
                                    MultiBufferSource buffers, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }
}
