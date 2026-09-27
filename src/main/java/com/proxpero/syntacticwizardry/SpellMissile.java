package com.proxpero.syntacticwizardry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
public final class SpellMissile extends Snowball {
 private static final double MAX_DISTANCE_SQR=16.0*16.0;
 private double startX,startY,startZ;
 public SpellMissile(EntityType<? extends SpellMissile> type,Level level){super(type,level);}
 public SpellMissile(Level level,LivingEntity owner){this(SyntacticWizardry.SPELL_MISSILE.get(),level);setOwner(owner);setPos(owner.getX(),owner.getEyeY()-0.1,owner.getZ());markStart();}
 public void prepare(Entity owner,Vec3 origin,Vec3 direction,int[] plan,int[] radii,int[] damageKinds,int[] potences,int row,int cell){
  setOwner(owner);
  setPos(origin.x,origin.y,origin.z);
  setItem(SpellPresentation.projectileStack(plan,radii,damageKinds,potences,row,cell));
  markStart();
  Vec3 dir=direction.lengthSqr()>1.0E-8?direction.normalize():new Vec3(0.0,0.0,1.0);
  shoot(dir.x,dir.y,dir.z,1.5F,0.0F);
 }
 public int presentationStyle(){return SpellPresentation.readStyle(getItem());}
 public int presentationVisual(){return SpellPresentation.readVisual(getItem());}
 private void markStart(){startX=getX();startY=getY();startZ=getZ();}
 @Override public boolean isNoGravity(){return true;}
 @Override public void tick(){
  super.tick();
  if(!level().isClientSide){
   double dx=getX()-startX,dy=getY()-startY,dz=getZ()-startZ;
   if(dx*dx+dy*dy+dz*dz>=MAX_DISTANCE_SQR)discard();
  }
 }
 @Override protected void onHit(HitResult result){
  if(!level().isClientSide&&level() instanceof ServerLevel server){
   int[] plan=SpellPresentation.readPlan(getItem());
   int[] radii=SpellPresentation.readRadii(getItem());
   int[] damageKinds=SpellPresentation.readDamageKinds(getItem());
   int[] potences=SpellPresentation.readPotences(getItem());
   Entity hit=result instanceof EntityHitResult entityHit?entityHit.getEntity():null;
   ShapeResolution resolved=ShapeResolution.impact(result.getLocation(),getDeltaMovement(),hit);
   SpellExecutor.continueFrom(server,getOwner(),plan,radii,damageKinds,potences,SpellPresentation.readRow(getItem()),resolved);
  }
  discard();
 }
}
