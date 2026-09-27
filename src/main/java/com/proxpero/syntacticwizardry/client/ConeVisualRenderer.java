package com.proxpero.syntacticwizardry.client;
import com.mojang.blaze3d.vertex.PoseStack;
import com.proxpero.syntacticwizardry.ConeShape;
import com.proxpero.syntacticwizardry.ConeVisualEntity;
import com.proxpero.syntacticwizardry.SpellPresentation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
public final class ConeVisualRenderer extends EntityRenderer<ConeVisualEntity> {
 private final ItemRenderer itemRenderer;
 public ConeVisualRenderer(EntityRendererProvider.Context context){
  super(context);
  this.itemRenderer=context.getItemRenderer();
  this.shadowRadius=0.0F;
 }
 @Override public ResourceLocation getTextureLocation(ConeVisualEntity entity){return TextureAtlas.LOCATION_BLOCKS;}
 @Override public void render(ConeVisualEntity entity,float yaw,float partialTick,PoseStack pose,MultiBufferSource buffers,int packedLight){
  ItemStack stack=entity.getItem();
  Vec3 origin=new Vec3(entity.getX(),entity.getY(),entity.getZ());
  Vec3 direction=SpellPresentation.readConeVisualDirection(stack);
  int width=SpellPresentation.readConeVisualWidth(stack);
  int height=SpellPresentation.readConeVisualHeight(stack);
  int depth=SpellPresentation.readConeVisualDepth(stack);
  int seed=entity.getId()*31;
  for(BlockPos block:ConeShape.voxels(origin,direction,width,height,depth)){
   pose.pushPose();
   pose.translate(block.getX()+0.5-entity.getX(),block.getY()+0.5-entity.getY(),block.getZ()+0.5-entity.getZ());
   itemRenderer.renderStatic(stack,ItemDisplayContext.GROUND,packedLight,OverlayTexture.NO_OVERLAY,pose,buffers,entity.level(),seed++);
   pose.popPose();
  }
  super.render(entity,yaw,partialTick,pose,buffers,packedLight);
 }
}
