package com.proxpero.syntacticwizardry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
public final class SpellExecutor {
 private SpellExecutor(){}
 public static void castRoot(ServerLevel level,Entity owner,int[] plan,int[] settings,Vec3 origin,Vec3 direction,Vec3 castYaw){
  int row=SpellPresentation.firstOccupiedRow(plan);
  if(row>=0)castRow(level,owner,plan,settings,row,ShapeResolution.point(origin,direction),true,normalizeYaw(castYaw));
 }
 private static void castRow(ServerLevel level,Entity owner,int[] plan,int[] settings,int row,ShapeResolution parent,boolean rootCast,Vec3 castYaw){
  if(row<0||row>=SpellPresentation.ROWS)return;
  boolean spawnedShape=false;
  for(int col=0;col<SpellPresentation.COLS;col++){
   int cell=row*SpellPresentation.COLS+col;
   SpellComponentDefinition definition=SpellComponents.byType(SpellPresentation.typeAt(plan,cell));
   if(definition==null)continue;
   ComponentExecutionResult result=definition.execute(new SpellExecutionContext(level,owner,plan,settings,row,cell,parent,rootCast,castYaw));
   if(result.spawnedShape())spawnedShape=true;
   for(ShapeResolution resolution:result.continuations())continueFrom(level,owner,plan,settings,row,resolution,castYaw);
  }
  if(!spawnedShape)continueFrom(level,owner,plan,settings,row,parent,castYaw);
 }
 public static void continueFrom(ServerLevel level,Entity owner,int[] plan,int[] settings,int resolvedRow,ShapeResolution resolution,Vec3 castYaw){
  int nextRow=resolvedRow+1;
  if(nextRow<SpellPresentation.ROWS&&SpellPresentation.rowHasComponents(plan,nextRow))castRow(level,owner,plan,settings,nextRow,resolution,false,normalizeYaw(castYaw));
 }
 public static Vec3 normalizeYaw(Vec3 direction){
  if(direction==null)return new Vec3(0.0,0.0,1.0);
  double horizontal=direction.x*direction.x+direction.z*direction.z;
  if(horizontal<=1.0E-12)return new Vec3(0.0,0.0,1.0);
  double scale=1.0/Math.sqrt(horizontal);
  return new Vec3(direction.x*scale,0.0,direction.z*scale);
 }
}
