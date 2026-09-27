package com.proxpero.syntacticwizardry.client;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.proxpero.syntacticwizardry.SpellMissile;
import com.proxpero.syntacticwizardry.SpellPresentation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
public final class SpellMissileRenderer extends ThrownItemRenderer<SpellMissile> {
 private final ItemRenderer itemRenderer;
 public SpellMissileRenderer(EntityRendererProvider.Context context){super(context);this.itemRenderer=context.getItemRenderer();}
 private static boolean directional(int visual){return visual==SpellPresentation.VISUAL_SWORD||visual==SpellPresentation.VISUAL_AXE||visual==SpellPresentation.VISUAL_TRIDENT;}
 @Override public void render(SpellMissile entity,float yaw,float partialTick,PoseStack pose,MultiBufferSource buffers,int packedLight){
  int style=entity.presentationStyle();
  int visual=entity.presentationVisual();
  double age=entity.tickCount+partialTick;
  Vec3 physical=entity.getDeltaMovement();
  Vec3 offset=Vec3.ZERO;
  Vec3 tangent=physical;
  if(style==SpellPresentation.STYLE_ARC){
   double p=Math.min(age/11.0,1.0);
   offset=new Vec3(0.0,Math.sin(Math.PI*p)*0.8,0.0);
   double dy=Math.cos(Math.PI*p)*(Math.PI/11.0)*0.8;
   tangent=physical.add(0.0,dy,0.0);
  }else if(style==SpellPresentation.STYLE_SPIRAL&&physical.lengthSqr()>1.0E-6){
   Vec3 forward=physical.normalize();
   Vec3 side=forward.cross(new Vec3(0.0,1.0,0.0));
   if(side.lengthSqr()<1.0E-6)side=new Vec3(1.0,0.0,0.0);else side=side.normalize();
   Vec3 other=side.cross(forward).normalize();
   double phase=age*1.35;
   offset=side.scale(Math.cos(phase)*0.35).add(other.scale(Math.sin(phase)*0.35));
   Vec3 derivative=side.scale(-Math.sin(phase)*0.35*1.35).add(other.scale(Math.cos(phase)*0.35*1.35));
   tangent=physical.add(derivative);
  }
  pose.pushPose();
  pose.translate(offset.x,offset.y,offset.z);
  if(!directional(visual)){
   super.render(entity,yaw,partialTick,pose,buffers,packedLight);
   pose.popPose();
   return;
  }
  if(tangent.lengthSqr()<1.0E-6)tangent=new Vec3(0.0,0.0,1.0);
  tangent=tangent.normalize();
  Quaternionf face=new Quaternionf().rotationTo(0.0F,1.0F,0.0F,(float)tangent.x,(float)tangent.y,(float)tangent.z);
  pose.mulPose(face);
  pose.mulPose(Axis.ZP.rotationDegrees(45.0F));
  itemRenderer.renderStatic(entity.getItem(),ItemDisplayContext.GROUND,packedLight,OverlayTexture.NO_OVERLAY,pose,buffers,entity.level(),entity.getId());
  pose.popPose();
 }
}
