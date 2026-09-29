package com.proxpero.syntacticwizardry.client;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.proxpero.syntacticwizardry.SphereVisualEntity;
import com.proxpero.syntacticwizardry.SpellPresentation;
import com.proxpero.syntacticwizardry.SphereShape;
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
public final class SphereVisualRenderer extends EntityRenderer<SphereVisualEntity> {
 private final ItemRenderer itemRenderer;
 public SphereVisualRenderer(EntityRendererProvider.Context context){
  super(context);
  this.itemRenderer=context.getItemRenderer();
  this.shadowRadius=0.0F;
 }
 private static boolean directional(int visual){
  return visual==SpellPresentation.VISUAL_SWORD||visual==SpellPresentation.VISUAL_AXE||visual==SpellPresentation.VISUAL_TRIDENT;
 }
 @Override public ResourceLocation getTextureLocation(SphereVisualEntity entity){return TextureAtlas.LOCATION_BLOCKS;}
 @Override public void render(SphereVisualEntity entity,float yaw,float partialTick,PoseStack pose,MultiBufferSource buffers,int packedLight){
  ItemStack stack=entity.getItem();
  int radius=SpellPresentation.readSphereVisualRadius(stack);
  int height=SpellPresentation.readSphereVisualHeight(stack);
  int mode=SpellPresentation.readSphereVisualMode(stack);
  int style=SpellPresentation.readStyle(stack);
  int visual=SpellPresentation.readVisual(stack);
  int centerX=(int)Math.floor(entity.getX());
  int centerY=(int)Math.floor(entity.getY());
  int centerZ=(int)Math.floor(entity.getZ());
  double bx=centerX+0.5-entity.getX();
  double by=centerY+0.5-entity.getY();
  double bz=centerZ+0.5-entity.getZ();
  int seed=entity.getId()*31;
  for(int dy=-height;dy<=height;dy++)for(int dz=-radius;dz<=radius;dz++)for(int dx=-radius;dx<=radius;dx++){
   if(!SphereShape.containsOffset(dx,dy,dz,radius,height,mode))continue;
   pose.pushPose();
   pose.translate(bx+dx,by+dy,bz+dz);
   if(directional(visual)&&(style==SpellPresentation.STYLE_INNER||style==SpellPresentation.STYLE_OUTER)){
    Vec3 radial=new Vec3(dx,dy,dz);
    if(radial.lengthSqr()>1.0E-8){
     Vec3 dir=style==SpellPresentation.STYLE_INNER?radial.scale(-1.0).normalize():radial.normalize();
     Quaternionf face=new Quaternionf().rotationTo(0.0F,1.0F,0.0F,(float)dir.x,(float)dir.y,(float)dir.z);
     pose.mulPose(face);
     pose.mulPose(Axis.ZP.rotationDegrees(45.0F));
    }
   }
   itemRenderer.renderStatic(stack,ItemDisplayContext.GROUND,packedLight,OverlayTexture.NO_OVERLAY,pose,buffers,entity.level(),seed++);
   pose.popPose();
  }
  super.render(entity,yaw,partialTick,pose,buffers,packedLight);
 }
}
