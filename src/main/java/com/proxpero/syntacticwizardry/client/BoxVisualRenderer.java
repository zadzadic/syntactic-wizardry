package com.proxpero.syntacticwizardry.client;
import com.mojang.blaze3d.vertex.PoseStack;
import com.proxpero.syntacticwizardry.BoxVisualEntity;
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
public final class BoxVisualRenderer extends EntityRenderer<BoxVisualEntity> {
 private final ItemRenderer itemRenderer;
 public BoxVisualRenderer(EntityRendererProvider.Context context){
  super(context);
  this.itemRenderer=context.getItemRenderer();
  this.shadowRadius=0.0F;
 }
 @Override public ResourceLocation getTextureLocation(BoxVisualEntity entity){return TextureAtlas.LOCATION_BLOCKS;}
 @Override public void render(BoxVisualEntity entity,float yaw,float partialTick,PoseStack pose,MultiBufferSource buffers,int packedLight){
  ItemStack stack=entity.getItem();
  int width=SpellPresentation.readBoxVisualWidth(stack);
  int height=SpellPresentation.readBoxVisualHeight(stack);
  int depth=SpellPresentation.readBoxVisualDepth(stack);
  int minX=-(width-1)/2,maxX=width/2;
  int minY=-(height-1)/2,maxY=height/2;
  int minZ=-(depth-1)/2,maxZ=depth/2;
  int centerX=(int)Math.floor(entity.getX());
  int centerY=(int)Math.floor(entity.getY());
  int centerZ=(int)Math.floor(entity.getZ());
  double bx=centerX+0.5-entity.getX();
  double by=centerY+0.5-entity.getY();
  double bz=centerZ+0.5-entity.getZ();
  int seed=entity.getId()*31;
  for(int dy=minY;dy<=maxY;dy++)for(int dz=minZ;dz<=maxZ;dz++)for(int dx=minX;dx<=maxX;dx++){
   pose.pushPose();
   pose.translate(bx+dx,by+dy,bz+dz);
   itemRenderer.renderStatic(stack,ItemDisplayContext.GROUND,packedLight,OverlayTexture.NO_OVERLAY,pose,buffers,entity.level(),seed++);
   pose.popPose();
  }
  super.render(entity,yaw,partialTick,pose,buffers,packedLight);
 }
}
