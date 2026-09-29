package com.proxpero.syntacticwizardry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
public record SpellExecutionContext(ServerLevel level,Entity owner,int[] plan,int[] settings,int row,int cell,ShapeResolution parent,boolean rootCast,Vec3 castYaw,int activeDurationTicks,boolean blockInteraction) {
 public SpellExecutionContext(ServerLevel level,Entity owner,int[] plan,int[] settings,int row,int cell,ShapeResolution parent,boolean rootCast,Vec3 castYaw){this(level,owner,plan,settings,row,cell,parent,rootCast,castYaw,0,false);}
 public int style(){return SpellPresentation.styleAt(plan,cell);}
 public int visual(){return SpellPresentation.visualAt(plan,cell);}
 public int setting(SpellPropertyKey key){return SpellPresentation.settingAt(settings,cell,key);}
 public int radius(){return setting(SpellPropertyKey.RADIUS);}
 public int sphereHeight(){return setting(SpellPropertyKey.HEIGHT);}
 public int sphereMode(){return setting(SpellPropertyKey.SPHERE_MODE);}
 public int damageKind(){return setting(SpellPropertyKey.DAMAGE_KIND);}
 public int potence(){return setting(SpellPropertyKey.POTENCE);}
 public int width(){return setting(SpellPropertyKey.WIDTH);}
 public int height(){return setting(SpellPropertyKey.HEIGHT);}
 public int depth(){return setting(SpellPropertyKey.DEPTH);}
 public int targetType(){return setting(SpellPropertyKey.TARGET_TYPE);}
 public int distance(){return setting(SpellPropertyKey.DISTANCE);}
 public int moveMode(){return setting(SpellPropertyKey.MOVE_MODE);}
 public int impactDirection(){return setting(SpellPropertyKey.IMPACT_DIRECTION);}
 public int siphonResource(){return setting(SpellPropertyKey.SIPHON_RESOURCE);}
 public int siphonMode(){return setting(SpellPropertyKey.SIPHON_MODE);}
 public int durationSeconds(){return setting(SpellPropertyKey.DURATION_SECONDS);}
 public int gravityMode(){return setting(SpellPropertyKey.GRAVITY_MODE);}
 public int protectionKind(){return setting(SpellPropertyKey.PROTECTION_KIND);}
 public int relativeDirection(){return setting(SpellPropertyKey.GRAVITY_MODE);}
 public int alterStrength(){return setting(SpellPropertyKey.ALTER_STRENGTH)-SpellPresentation.ALTER_SETTING_DEFAULT;}
 public int alterSpeed(){return setting(SpellPropertyKey.ALTER_SPEED)-SpellPresentation.ALTER_SETTING_DEFAULT;}
 public int alterToughness(){return setting(SpellPropertyKey.ALTER_TOUGHNESS)-SpellPresentation.ALTER_SETTING_DEFAULT;}
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
