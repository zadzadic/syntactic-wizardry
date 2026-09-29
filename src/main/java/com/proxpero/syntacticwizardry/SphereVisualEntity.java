package com.proxpero.syntacticwizardry;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
public final class SphereVisualEntity extends ItemEntity {
 public SphereVisualEntity(EntityType<? extends SphereVisualEntity> type,Level level){
  super(type,level);
  setNoGravity(true);
  setNeverPickUp();
  setInvulnerable(true);
  lifespan=Integer.MAX_VALUE;
 }
 public SphereVisualEntity(Level level,Vec3 origin,int radius,int height,int mode,int style,int visual,int lifetimeTicks){
  this(SyntacticWizardry.SPHERE_VISUAL.get(),level);
  setPos(origin.x,origin.y,origin.z);
  setItem(SpellPresentation.sphereVisualStack(style,visual,radius,height,mode,lifetimeTicks));
  setDeltaMovement(Vec3.ZERO);
 }
 public SphereVisualEntity(Level level,Vec3 origin,int radius,int height,int mode,int style,int visual){this(level,origin,radius,height,mode,style,visual,VisualDurationSupport.DEFAULT_VISUAL_TICKS);}
 public SphereVisualEntity(Level level,Vec3 origin,int radius,int style,int visual){this(level,origin,radius,radius,SpellPresentation.SPHERE_FULL,style,visual,VisualDurationSupport.DEFAULT_VISUAL_TICKS);}
 @Override public void tick(){
  double x=getX(),y=getY(),z=getZ();
  lifespan=getItem().isEmpty()?Integer.MAX_VALUE:SpellPresentation.readVisualLifetime(getItem());
  super.tick();
  setPos(x,y,z);
  setDeltaMovement(Vec3.ZERO);
 }
}
