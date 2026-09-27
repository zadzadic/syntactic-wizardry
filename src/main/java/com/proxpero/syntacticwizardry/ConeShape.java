package com.proxpero.syntacticwizardry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import java.util.LinkedHashSet;
import java.util.List;
public final class ConeShape {
 private ConeShape(){}
 public static List<BlockPos> voxels(Vec3 origin,Vec3 direction,int width,int height,int depth){
  int w=clamp(width),h=clamp(height),d=clamp(depth);
  Vec3 forward=direction.lengthSqr()>1.0E-8?direction.normalize():new Vec3(0.0,0.0,1.0);
  Vec3 reference=Math.abs(forward.y)>0.95?new Vec3(0.0,0.0,1.0):new Vec3(0.0,1.0,0.0);
  Vec3 right=forward.cross(reference).normalize();
  Vec3 up=right.cross(forward).normalize();
  LinkedHashSet<BlockPos> out=new LinkedHashSet<>();
  for(int layer=0;layer<d;layer++){
   double scale=(layer+1)/(double)d;
   int layerWidth=Math.max(1,(int)Math.ceil(w*scale));
   int layerHeight=Math.max(1,(int)Math.ceil(h*scale));
   Vec3 center=origin.add(forward.scale(layer));
   for(int yi=0;yi<layerHeight;yi++){
    double yOffset=yi-(layerHeight-1)/2.0;
    for(int xi=0;xi<layerWidth;xi++){
     double xOffset=xi-(layerWidth-1)/2.0;
     Vec3 point=center.add(right.scale(xOffset)).add(up.scale(yOffset));
     out.add(BlockPos.containing(point));
    }
   }
  }
  return List.copyOf(out);
 }
 private static int clamp(int value){return Math.max(SpellPresentation.DIMENSION_MIN,Math.min(SpellPresentation.DIMENSION_MAX,value));}
}
