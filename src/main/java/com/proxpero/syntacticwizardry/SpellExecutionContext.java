package com.proxpero.syntacticwizardry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
public record SpellExecutionContext(ServerLevel level,Entity owner,int[] plan,int[] radii,int[] damageKinds,int[] potences,int row,int cell,ShapeResolution parent) {
 public int style(){return SpellPresentation.styleAt(plan,cell);}
 public int visual(){return SpellPresentation.visualAt(plan,cell);}
 public int radius(){return SpellPresentation.radiusAt(radii,cell);}
 public int damageKind(){return SpellPresentation.damageKindAt(damageKinds,cell);}
 public int potence(){return SpellPresentation.potenceAt(potences,cell);}
}
