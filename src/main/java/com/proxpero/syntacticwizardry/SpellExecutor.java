package com.proxpero.syntacticwizardry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
public final class SpellExecutor {
 private SpellExecutor(){}
 public static void castRoot(ServerLevel level,Entity owner,int[] plan,int[] radii,int[] damageKinds,int[] potences,Vec3 origin,Vec3 direction){
  int row=SpellPresentation.firstOccupiedRow(plan);
  if(row>=0)castRow(level,owner,plan,radii,damageKinds,potences,row,ShapeResolution.point(origin,direction));
 }
 public static void castRow(ServerLevel level,Entity owner,int[] plan,int[] radii,int[] damageKinds,int[] potences,int row,ShapeResolution parent){
  if(row<0||row>=SpellPresentation.ROWS)return;
  boolean spawnedShape=false;
  for(int col=0;col<SpellPresentation.COLS;col++){
   int cell=row*SpellPresentation.COLS+col;
   SpellComponentDefinition definition=SpellComponents.byType(SpellPresentation.typeAt(plan,cell));
   if(definition==null)continue;
   ComponentExecutionResult result=definition.execute(new SpellExecutionContext(level,owner,plan,radii,damageKinds,potences,row,cell,parent));
   if(result.spawnedShape())spawnedShape=true;
   for(ShapeResolution resolution:result.continuations())continueFrom(level,owner,plan,radii,damageKinds,potences,row,resolution);
  }
  if(!spawnedShape)continueFrom(level,owner,plan,radii,damageKinds,potences,row,parent);
 }
 public static void continueFrom(ServerLevel level,Entity owner,int[] plan,int[] radii,int[] damageKinds,int[] potences,int resolvedRow,ShapeResolution resolution){
  int nextRow=resolvedRow+1;
  if(nextRow<SpellPresentation.ROWS&&SpellPresentation.rowHasComponents(plan,nextRow))castRow(level,owner,plan,radii,damageKinds,potences,nextRow,resolution);
 }
}
