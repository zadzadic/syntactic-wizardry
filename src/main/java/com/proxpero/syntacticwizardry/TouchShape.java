package com.proxpero.syntacticwizardry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
public final class TouchShape {
 private static final double RANGE=5.0;
 private TouchShape(){}
 public static ShapeResolution resolve(SpellExecutionContext context){
  ServerLevel level=context.level();
  Vec3 start=context.parent().origin();
  Vec3 direction=context.shapeDirection().lengthSqr()>1.0E-8?context.shapeDirection().normalize():new Vec3(0.0,0.0,1.0);
  Vec3 end=start.add(direction.scale(RANGE));
  BlockHitResult hit=level.clip(new ClipContext(start,end,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,context.owner()));
  if(hit.getType()==HitResult.Type.MISS)return null;
  return ShapeResolution.block(hit.getLocation(),direction,hit.getBlockPos());
 }
}
