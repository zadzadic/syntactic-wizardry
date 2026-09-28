package com.proxpero.syntacticwizardry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
public record SpellExecutionContext(ServerLevel level,Entity owner,int[] plan,int[] settings,int row,int cell,ShapeResolution parent,boolean rootCast,Vec3 castYaw) {
 public int style(){return SpellPresentation.styleAt(plan,cell);}
 public int visual(){return SpellPresentation.visualAt(plan,cell);}
 public int setting(SpellPropertyKey key){return SpellPresentation.settingAt(settings,cell,key);}
 public int radius(){return setting(SpellPropertyKey.RADIUS);}
 public int damageKind(){return setting(SpellPropertyKey.DAMAGE_KIND);}
 public int potence(){return setting(SpellPropertyKey.POTENCE);}
 public int width(){return setting(SpellPropertyKey.WIDTH);}
 public int height(){return setting(SpellPropertyKey.HEIGHT);}
 public int depth(){return setting(SpellPropertyKey.DEPTH);}
 public int targetType(){return setting(SpellPropertyKey.TARGET_TYPE);}
 public int distance(){return setting(SpellPropertyKey.DISTANCE);}
 public boolean hasSurfaceFrame(){return parent.surfaceNormal()!=null;}
 public Vec3 shapeDirection(){
  if(hasSurfaceFrame())return parent.surfaceNormal();
  return rootCast?parent.direction():SpellExecutor.normalizeYaw(castYaw);
 }
 public Vec3 shapeUp(){return hasSurfaceFrame()?parent.up():new Vec3(0.0,1.0,0.0);}
 public Vec3 boxDirection(){return hasSurfaceFrame()?parent.surfaceNormal():SpellExecutor.normalizeYaw(castYaw);}
 public Vec3 inheritedSurfaceNormal(){return parent.surfaceNormal();}
 public Vec3 areaOrigin(){
  if(!rootCast)return parent.origin();
  Vec3 yaw=SpellExecutor.normalizeYaw(castYaw);
  return parent.origin().add(yaw.x,0.0,yaw.z);
 }
}
