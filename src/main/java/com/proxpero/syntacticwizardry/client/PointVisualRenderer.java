package com.proxpero.syntacticwizardry.client;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.proxpero.syntacticwizardry.PointVisualEntity;
import com.proxpero.syntacticwizardry.SpellPresentation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
public final class PointVisualRenderer extends EntityRenderer<PointVisualEntity> {
 private final ItemRenderer itemRenderer;
 public PointVisualRenderer(EntityRendererProvider.Context context){super(context);this.itemRenderer=context.getItemRenderer();this.shadowRadius=0.0F;}
 private static boolean directional(int visual){return visual==SpellPresentation.VISUAL_SWORD||visual==SpellPresentation.VISUAL_AXE||visual==SpellPresentation.VISUAL_TRIDENT;}
 @Override public ResourceLocation getTextureLocation(PointVisualEntity entity){return TextureAtlas.LOCATION_BLOCKS;}
 @Override public void render(PointVisualEntity entity,float yaw,float partialTick,PoseStack pose,MultiBufferSource buffers,int packedLight){
  ItemStack stack=entity.getItem();int style=SpellPresentation.readStyle(stack);int visual=SpellPresentation.readVisual(stack);Vec3 forward=SpellPresentation.readPointVisualDirection(stack);double age=entity.tickCount+partialTick;Vec3 offset=Vec3.ZERO;Vec3 tangent=forward;
  if(style==SpellPresentation.STYLE_ARC){double p=(age%20.0)/20.0;offset=new Vec3(0.0,Math.sin(Math.PI*p)*0.8,0.0);double dy=Math.cos(Math.PI*p)*(Math.PI/20.0)*0.8;tangent=forward.add(0.0,dy,0.0);}
  else if(style==SpellPresentation.STYLE_SPIRAL){Vec3 side=forward.cross(new Vec3(0.0,1.0,0.0));if(side.lengthSqr()<1.0E-6)side=new Vec3(1.0,0.0,0.0);else side=side.normalize();Vec3 other=side.cross(forward).normalize();double phase=age*0.35;offset=side.scale(Math.cos(phase)*0.35).add(other.scale(Math.sin(phase)*0.35));Vec3 derivative=side.scale(-Math.sin(phase)*0.35*0.35).add(other.scale(Math.cos(phase)*0.35*0.35));tangent=forward.add(derivative);}
  pose.pushPose();pose.translate(offset.x,offset.y,offset.z);if(directional(visual)){if(tangent.lengthSqr()<1.0E-6)tangent=forward;Quaternionf face=new Quaternionf().rotationTo(0.0F,1.0F,0.0F,(float)tangent.x,(float)tangent.y,(float)tangent.z);pose.mulPose(face);pose.mulPose(Axis.ZP.rotationDegrees(45.0F));}itemRenderer.renderStatic(stack,ItemDisplayContext.GROUND,packedLight,OverlayTexture.NO_OVERLAY,pose,buffers,entity.level(),entity.getId());pose.popPose();super.render(entity,yaw,partialTick,pose,buffers,packedLight);
 }
}
