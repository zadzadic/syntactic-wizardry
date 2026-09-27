package com.proxpero.syntacticwizardry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import java.util.List;
public final class FloatingShape {
 private FloatingShape(){}
 public static ShapeResolution resolve(Vec3 origin,Vec3 direction,int distance){
  int d=Math.max(SpellPresentation.DISTANCE_MIN,Math.min(SpellPresentation.DISTANCE_MAX,distance));
  Vec3 dir=direction.lengthSqr()>1.0E-8?direction.normalize():new Vec3(0.0,0.0,1.0);
  Vec3 target=origin.add(dir.scale(d));
  return new ShapeResolution(target,dir,List.of(BlockPos.containing(target)),null);
 }
}
