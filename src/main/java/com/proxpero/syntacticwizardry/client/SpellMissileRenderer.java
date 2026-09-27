package com.proxpero.syntacticwizardry.client;
import com.mojang.blaze3d.vertex.PoseStack;
import com.proxpero.syntacticwizardry.SpellMissile;
import com.proxpero.syntacticwizardry.SpellPresentation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.world.phys.Vec3;
public final class SpellMissileRenderer extends ThrownItemRenderer<SpellMissile> {
 public SpellMissileRenderer(EntityRendererProvider.Context context){super(context);}
 @Override public void render(SpellMissile entity,float yaw,float partialTick,PoseStack pose,MultiBufferSource buffers,int packedLight){
  pose.pushPose();
  int style=entity.presentationStyle();
  double age=entity.tickCount+partialTick;
  if(style==SpellPresentation.STYLE_ARC){
   double p=Math.min(age/11.0,1.0);
   pose.translate(0.0,Math.sin(Math.PI*p)*0.8,0.0);
  }else if(style==SpellPresentation.STYLE_SPIRAL){
   Vec3 forward=entity.getDeltaMovement();
   if(forward.lengthSqr()>1.0E-6){
    forward=forward.normalize();
    Vec3 side=forward.cross(new Vec3(0.0,1.0,0.0));
    if(side.lengthSqr()<1.0E-6)side=new Vec3(1.0,0.0,0.0);else side=side.normalize();
    Vec3 other=side.cross(forward).normalize();
    double phase=age*1.35;
    Vec3 off=side.scale(Math.cos(phase)*0.35).add(other.scale(Math.sin(phase)*0.35));
    pose.translate(off.x,off.y,off.z);
   }
  }
  super.render(entity,yaw,partialTick,pose,buffers,packedLight);
  pose.popPose();
 }
}
