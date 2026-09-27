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
  lifespan=8;
 }
 public SphereVisualEntity(Level level,Vec3 origin,int radius,int style,int visual){
  this(SyntacticWizardry.SPHERE_VISUAL.get(),level);
  setPos(origin.x,origin.y,origin.z);
  setItem(SpellPresentation.sphereVisualStack(style,visual,radius));
  setDeltaMovement(Vec3.ZERO);
 }
 @Override public void tick(){
  double x=getX(),y=getY(),z=getZ();
  super.tick();
  setPos(x,y,z);
  setDeltaMovement(Vec3.ZERO);
 }
}
