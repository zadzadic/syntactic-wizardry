package com.proxpero.syntacticwizardry.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.proxpero.syntacticwizardry.RunecasterItem;
import com.proxpero.syntacticwizardry.RunecasterRuneStorage;
import com.proxpero.syntacticwizardry.SpellComponentDefinition;
import com.proxpero.syntacticwizardry.SpellComponents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public abstract class RunecasterItemRenderMixin {
    @Inject(method = "renderItem", at = @At("TAIL"))
    private void syntacticwizardry$renderRunecasterRunes(
            LivingEntity entity,
            ItemStack stack,
            ItemDisplayContext displayContext,
            boolean leftHanded,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            CallbackInfo ci) {
        if (!(stack.getItem() instanceof RunecasterItem)) return;
        if (displayContext != ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
                && displayContext != ItemDisplayContext.FIRST_PERSON_LEFT_HAND) return;

        boolean left = displayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND;

        poseStack.pushPose();

        // Match the Runecaster's first-person model transform.
        poseStack.translate(1.13D / 16.0D, 3.2D / 16.0D, 1.13D / 16.0D);
        poseStack.mulPose(Axis.YP.rotationDegrees(left ? 90.0F : -90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(left ? -25.0F : 25.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(left ? 90.0F : -90.0F));
        poseStack.scale(0.68F, 0.68F, 0.68F);

        // Render the three currently active rune components directly on the visible shaft face.
        for (int slot = 0; slot < RunecasterRuneStorage.RUNE_SLOTS; slot++) {
            int type = RunecasterRuneStorage.activeType(stack, slot);
            SpellComponentDefinition definition = SpellComponents.byType(type);
            if (definition == null) continue;

            ItemStack icon = definition.createEditorIcon();
            if (icon.isEmpty()) continue;

            poseStack.pushPose();
            poseStack.translate(0.0D, 0.58D - slot * 0.34D, -0.084D);
            poseStack.mulPose(Axis.YP.rotationDegrees(left ? -90.0F : 90.0F));
            poseStack.mulPose(Axis.ZP.rotationDegrees(left ? 90.0F : -90.0F));
            poseStack.scale(0.23F, 0.23F, 0.23F);

            Minecraft.getInstance().getItemRenderer().renderStatic(
                    entity,
                    icon,
                    ItemDisplayContext.FIXED,
                    left,
                    poseStack,
                    bufferSource,
                    entity.level(),
                    packedLight,
                    OverlayTexture.NO_OVERLAY,
                    slot
            );
            poseStack.popPose();
        }

        poseStack.popPose();
    }
}
