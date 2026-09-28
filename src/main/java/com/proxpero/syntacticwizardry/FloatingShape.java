package com.proxpero.syntacticwizardry;
import net.minecraft.world.phys.Vec3;
public final class FloatingShape {
 private FloatingShape(){}
 public static ShapeResolution resolve(Vec3 origin,Vec3 direction,Vec3 up,Vec3 surfaceNormal,int distance){
  return ShapeResolution.floating(origin,direction,up,surfaceNormal,distance);
 }
}
