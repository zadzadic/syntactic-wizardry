package com.proxpero.syntacticwizardry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
public record SpellExecutionContext(ServerLevel level,Entity owner,int[] plan,int[] settings,int row,int cell,ShapeResolution parent,boolean rootCast) {
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
 public Vec3 areaOrigin(){
  if(!rootCast)return parent.origin();
  Vec3 dir=parent.direction();
  double horizontal=dir.x*dir.x+dir.z*dir.z;
  if(horizontal<=1.0E-8)return parent.origin();
  double scale=1.0/Math.sqrt(horizontal);
  return parent.origin().add(dir.x*scale,0.0,dir.z*scale);
 }
}
