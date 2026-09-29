package com.proxpero.syntacticwizardry;
import net.minecraft.server.level.ServerLevel;
public final class TemporaryBlockEffect {
 private TemporaryBlockEffect(){}
 public static void apply(ServerLevel level,ShapeResolution resolution,int durationTicks){
  if(level==null||resolution==null)return;
  TemporaryBlockRuntime.create(level,resolution.voxels(),durationTicks);
 }
}
