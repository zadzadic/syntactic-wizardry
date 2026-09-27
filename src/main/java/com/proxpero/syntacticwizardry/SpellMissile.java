package com.proxpero.syntacticwizardry;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
public final class SpellMissile extends Snowball {
 private static final double MAX_DISTANCE_SQR=16.0*16.0;
 private double startX,startY,startZ;
 public SpellMissile(EntityType<? extends SpellMissile> type,Level level){super(type,level);markStart();}
 public SpellMissile(Level level,LivingEntity owner){this(SyntacticWizardry.SPELL_MISSILE.get(),level);setOwner(owner);setPos(owner.getX(),owner.getEyeY()-0.1,owner.getZ());markStart();}
 private void markStart(){startX=getX();startY=getY();startZ=getZ();}
 @Override public boolean isNoGravity(){return true;}
 @Override public void tick(){
  super.tick();
  double dx=getX()-startX,dy=getY()-startY,dz=getZ()-startZ;
  if(dx*dx+dy*dy+dz*dz>=MAX_DISTANCE_SQR)discard();
 }
 @Override protected void onHit(HitResult result){
  discard();
 }
}
