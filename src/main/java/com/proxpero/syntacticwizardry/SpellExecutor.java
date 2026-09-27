package com.proxpero.syntacticwizardry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
public final class SpellExecutor {
 private SpellExecutor(){}
 public static void castRoot(ServerLevel level,Entity owner,int[] plan,Vec3 origin,Vec3 direction){
  int row=SpellPresentation.firstOccupiedRow(plan);
  if(row>=0)castRow(level,owner,plan,row,origin,direction);
 }
 public static void castRow(ServerLevel level,Entity owner,int[] plan,int row,Vec3 origin,Vec3 direction){
  if(row<0||row>=SpellPresentation.ROWS)return;
  Vec3 dir=direction.lengthSqr()>1.0E-8?direction.normalize():new Vec3(0.0,0.0,1.0);
  for(int col=0;col<SpellPresentation.COLS;col++){
   int cell=row*SpellPresentation.COLS+col;
   if(SpellPresentation.typeAt(plan,cell)!=SpellPresentation.TYPE_MISSILE)continue;
   SpellMissile missile=new SpellMissile(SyntacticWizardry.SPELL_MISSILE.get(),level);
   missile.prepare(owner,origin,dir,plan,row,cell);
   level.addFreshEntity(missile);
  }
 }
}
