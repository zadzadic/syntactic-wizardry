package com.proxpero.syntacticwizardry;
public final class VisualDurationSupport {
 public static final int DEFAULT_VISUAL_TICKS=8;
 private VisualDurationSupport(){}
 public static int durationTicksForShape(int[] plan,int[] settings,int shapeRow){
  int effectRow=shapeRow+1;
  if(effectRow<0||effectRow>=SpellPresentation.ROWS)return 0;
  int longest=0;
  for(int col=0;col<SpellPresentation.COLS;col++){
   int cell=effectRow*SpellPresentation.COLS+col;
   SpellComponentDefinition definition=SpellComponents.byType(SpellPresentation.typeAt(plan,cell));
   if(!SpellComponents.isEffect(definition))continue;
   longest=Math.max(longest,SpellComponents.attachedDurationTicks(plan,settings,effectRow,col));
  }
  return longest;
 }
 public static int lifetimeTicksForShape(int[] plan,int[] settings,int shapeRow){
  return Math.max(DEFAULT_VISUAL_TICKS,durationTicksForShape(plan,settings,shapeRow));
 }
}
