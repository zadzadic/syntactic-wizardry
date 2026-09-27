package com.proxpero.syntacticwizardry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
public final class SpellExecutor {
 private SpellExecutor(){}
 public static void castRoot(ServerLevel level,Entity owner,int[] plan,int[] radii,Vec3 origin,Vec3 direction){
  int row=SpellPresentation.firstOccupiedRow(plan);
  if(row>=0)castRow(level,owner,plan,radii,row,ShapeResolution.point(origin,direction));
 }
 public static void castRoot(ServerLevel level,Entity owner,int[] plan,Vec3 origin,Vec3 direction){castRoot(level,owner,plan,SpellPresentation.emptyRadii(),origin,direction);}
 public static void castRow(ServerLevel level,Entity owner,int[] plan,int[] radii,int row,ShapeResolution parent){
  if(row<0||row>=SpellPresentation.ROWS)return;
  for(int col=0;col<SpellPresentation.COLS;col++){
   int cell=row*SpellPresentation.COLS+col;
   int type=SpellPresentation.typeAt(plan,cell);
   if(type==SpellPresentation.TYPE_MISSILE){
    SpellMissile missile=new SpellMissile(SyntacticWizardry.SPELL_MISSILE.get(),level);
    missile.prepare(owner,parent.origin(),parent.direction(),plan,radii,row,cell);
    level.addFreshEntity(missile);
   }else if(type==SpellPresentation.TYPE_SPHERE){
    ShapeResolution resolved=ShapeResolution.sphere(parent.origin(),parent.direction(),SpellPresentation.radiusAt(radii,cell));
    continueFrom(level,owner,plan,radii,row,resolved);
   }
  }
 }
 public static void continueFrom(ServerLevel level,Entity owner,int[] plan,int[] radii,int resolvedRow,ShapeResolution resolution){
  int nextRow=resolvedRow+1;
  if(nextRow<SpellPresentation.ROWS&&SpellPresentation.rowHasComponents(plan,nextRow))castRow(level,owner,plan,radii,nextRow,resolution);
 }
}
