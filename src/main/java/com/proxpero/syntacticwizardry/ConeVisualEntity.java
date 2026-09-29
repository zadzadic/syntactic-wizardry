package com.proxpero.syntacticwizardry;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
public final class ConeVisualEntity extends ItemEntity {
 public ConeVisualEntity(EntityType<? extends ConeVisualEntity> type,Level level){
  super(type,level);
  setNoGravity(true);
  setNeverPickUp();
  setInvulnerable(true);
  lifespan=Integer.MAX_VALUE;
 }
 public ConeVisualEntity(Level level,Vec3 origin,Vec3 direction,int width,int height,int depth,int style,int visual,int lifetimeTicks){
  this(SyntacticWizardry.CONE_VISUAL.get(),level);
  setPos(origin.x,origin.y,origin.z);
  setItem(SpellPresentation.coneVisualStack(style,visual,width,height,depth,direction,lifetimeTicks));
  setDeltaMovement(Vec3.ZERO);
 }
 public ConeVisualEntity(Level level,Vec3 origin,Vec3 direction,int width,int height,int depth,int style,int visual){this(level,origin,direction,width,height,depth,style,visual,VisualDurationSupport.DEFAULT_VISUAL_TICKS);}
 @Override public void tick(){
  double x=getX(),y=getY(),z=getZ();
  lifespan=getItem().isEmpty()?Integer.MAX_VALUE:SpellPresentation.readVisualLifetime(getItem());
  super.tick();
  setPos(x,y,z);
  setDeltaMovement(Vec3.ZERO);
 }
}
