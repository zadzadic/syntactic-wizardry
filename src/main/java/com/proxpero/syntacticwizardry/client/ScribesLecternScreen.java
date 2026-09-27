package com.proxpero.syntacticwizardry.client;
import com.proxpero.syntacticwizardry.ScribesLecternMenu;
import com.proxpero.syntacticwizardry.SpellPresentation;
import com.proxpero.syntacticwizardry.SyntacticWizardry;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
public final class ScribesLecternScreen extends AbstractContainerScreen<ScribesLecternMenu> {
 private enum EditorMode{COMPONENTS,STYLE,VISUAL}
 private static final int SELECTOR_X=175,SELECTOR_Y=43;
 private static final int GRID_X=20,GRID_Y=49,CELL=16,STEP=20;
 private static final int DRAG_NONE=-2,DRAG_SELECTOR=-1;
 private EditorMode editorMode=EditorMode.COMPONENTS;
 private int selectedCell=-1,dragSource=DRAG_NONE;
 private Button writeButton;
 public ScribesLecternScreen(ScribesLecternMenu menu,Inventory inv,Component title){super(menu,inv,title);imageWidth=ScribesLecternMenu.WIDTH;imageHeight=ScribesLecternMenu.HEIGHT;}
 @Override protected void init(){super.init();writeButton=addRenderableWidget(Button.builder(Component.literal("Write Spell"),b->sendAction(ScribesLecternMenu.ACTION_WRITE)).bounds(leftPos+20,topPos+171,120,18).build());}
 private void sendAction(int id){if(minecraft!=null&&minecraft.gameMode!=null)minecraft.gameMode.handleInventoryButtonClick(menu.containerId,id);}
 @Override protected void renderBg(GuiGraphics g,float partial,int mx,int my){
  int x=leftPos,y=topPos;
  g.fill(x,y,x+320,y+232,0xE0101826);
  panel(g,x+10,y+10,145,184,"Spell Workspace");
  panel(g,x+165,y+10,145,116,"Component Selector");
  panel(g,x+165,y+136,145,58,"Spell Preview");
  g.fill(x+75,y+199,x+247,y+227,0xC0182233);
  g.fill(x+19,y+24,x+37,y+42,0xFF335A88);
  renderWorkspace(g,x,y);
  renderSelector(g,x,y);
  renderPreview(g,x,y);
 }
 private void renderWorkspace(GuiGraphics g,int x,int y){
  for(int row=0;row<SpellPresentation.ROWS;row++)for(int col=0;col<SpellPresentation.COLS;col++){
   int cell=row*SpellPresentation.COLS+col,sx=x+GRID_X+col*STEP,sy=y+GRID_Y+row*STEP;
   g.fill(sx,sy,sx+CELL,sy+CELL,cell==selectedCell?0xFF4B7197:0x7030445E);
   if(menu.typeAt(cell)==SpellPresentation.TYPE_MISSILE)g.renderItem(SyntacticWizardry.MISSILE_SHAPE.get().getDefaultInstance(),sx,sy);
  }
  if(selectedCell>=0&&menu.typeAt(selectedCell)!=SpellPresentation.TYPE_EMPTY){
   buttonBox(g,x+20,y+149,55,17,"Style");
   buttonBox(g,x+82,y+149,55,17,"Visual");
  }
 }
 private void renderSelector(GuiGraphics g,int x,int y){
  if(editorMode==EditorMode.COMPONENTS){
   g.drawString(font,"Shapes",x+173,y+29,0xCFE5FF,false);
   g.fill(x+SELECTOR_X,y+SELECTOR_Y,x+SELECTOR_X+CELL,y+SELECTOR_Y+CELL,0xFF2A3B55);
   g.renderItem(SyntacticWizardry.MISSILE_SHAPE.get().getDefaultInstance(),x+SELECTOR_X,y+SELECTOR_Y);
   return;
  }
  buttonBox(g,x+174,y+29,40,15,"Back");
  if(editorMode==EditorMode.STYLE){
   g.drawString(font,"Style",x+220,y+33,0xCFE5FF,false);
   for(int i=0;i<SpellPresentation.STYLE_COUNT;i++){
    int oy=y+51+i*21;
    g.fill(x+174,oy,x+300,oy+18,selectedCell>=0&&menu.styleAt(selectedCell)==i?0xFF36587A:0xB02A3B55);
    g.drawString(font,SpellPresentation.styleName(i),x+181,oy+5,0xE8F3FF,false);
   }
  }else{
   g.drawString(font,"Visual",x+220,y+33,0xCFE5FF,false);
   for(int i=0;i<SpellPresentation.VISUAL_COUNT;i++){
    int col=i%3,row=i/3,ox=x+177+col*42,oy=y+52+row*34;
    g.fill(ox,oy,ox+CELL,oy+CELL,selectedCell>=0&&menu.visualAt(selectedCell)==i?0xFF5B86B8:0xFF2A3B55);
    g.renderItem(SpellPresentation.visualStack(i),ox,oy);
   }
  }
 }
 private void renderPreview(GuiGraphics g,int x,int y){
  int[] plan=menu.snapshotPlan();
  int root=SpellPresentation.firstOccupiedRow(plan);
  if(root<0)return;
  int depth=1;
  while(root+depth<SpellPresentation.ROWS&&SpellPresentation.rowHasComponents(plan,root+depth))depth++;
  long stageMs=1100L,totalMs=stageMs*depth,time=Util.getMillis()%totalMs;
  int activeRow=root+(int)(time/stageMs);
  double t=(time%stageMs)/(double)stageMs;
  int count=0;
  for(int col=0;col<SpellPresentation.COLS;col++)if(SpellPresentation.typeAt(plan,activeRow*SpellPresentation.COLS+col)!=SpellPresentation.TYPE_EMPTY)count++;
  int lane=0,left=x+174,top=y+154,w=126,h=32;
  for(int col=0;col<SpellPresentation.COLS;col++){
   int cell=activeRow*SpellPresentation.COLS+col;
   if(SpellPresentation.typeAt(plan,cell)!=SpellPresentation.TYPE_MISSILE)continue;
   int laneOffset=(lane-(count-1)/2)*5;lane++;
   double pyT=t;
   int px=left+(int)(pyT*(w-16));
   int py=previewY(top+h/2+laneOffset,pyT,SpellPresentation.styleAt(plan,cell))-8;
   g.renderItem(SpellPresentation.visualStack(SpellPresentation.visualAt(plan,cell)),px,py);
  }
  g.drawString(font,"Row "+(activeRow+1),x+274,y+181,0x9EB7CF,false);
 }
 private int previewY(int base,double t,int style){if(style==SpellPresentation.STYLE_ARC)return base-(int)(Math.sin(Math.PI*t)*10.0);if(style==SpellPresentation.STYLE_SPIRAL)return base+(int)(Math.sin(t*Math.PI*4.0)*6.0);return base;}
 private void panel(GuiGraphics g,int x,int y,int w,int h,String name){g.fill(x,y,x+w,y+h,0xC0202D42);g.renderOutline(x,y,w,h,0xFF5B86B8);g.drawString(font,name,x+7,y+6,0xE8F3FF,false);}
 private void buttonBox(GuiGraphics g,int x,int y,int w,int h,String text){g.fill(x,y,x+w,y+h,0xFF30445E);g.renderOutline(x,y,w,h,0xFF5B86B8);g.drawCenteredString(font,text,x+w/2,y+5,0xE8F3FF);}
 private boolean inside(double mx,double my,int x,int y,int w,int h){return mx>=leftPos+x&&mx<leftPos+x+w&&my>=topPos+y&&my<topPos+y+h;}
 private int workspaceCellAt(double mx,double my){
  for(int row=0;row<SpellPresentation.ROWS;row++)for(int col=0;col<SpellPresentation.COLS;col++){int x=GRID_X+col*STEP,y=GRID_Y+row*STEP;if(inside(mx,my,x,y,CELL,CELL))return row*SpellPresentation.COLS+col;}
  return -1;
 }
 @Override public boolean mouseClicked(double mx,double my,int button){
  if(button==1){int cell=workspaceCellAt(mx,my);if(cell>=0&&menu.typeAt(cell)!=SpellPresentation.TYPE_EMPTY){sendAction(ScribesLecternMenu.ACTION_CLEAR_BASE+cell);if(selectedCell==cell)selectedCell=-1;return true;}}
  if(button==0){
   if(selectedCell>=0&&menu.typeAt(selectedCell)!=SpellPresentation.TYPE_EMPTY&&inside(mx,my,20,149,55,17)){editorMode=EditorMode.STYLE;return true;}
   if(selectedCell>=0&&menu.typeAt(selectedCell)!=SpellPresentation.TYPE_EMPTY&&inside(mx,my,82,149,55,17)){editorMode=EditorMode.VISUAL;return true;}
   if(editorMode!=EditorMode.COMPONENTS&&inside(mx,my,174,29,40,15)){editorMode=EditorMode.COMPONENTS;return true;}
   if(editorMode==EditorMode.STYLE&&selectedCell>=0){
    for(int i=0;i<SpellPresentation.STYLE_COUNT;i++)if(inside(mx,my,174,51+i*21,126,18)){sendAction(ScribesLecternMenu.ACTION_STYLE_BASE+selectedCell*SpellPresentation.STYLE_COUNT+i);return true;}
   }
   if(editorMode==EditorMode.VISUAL&&selectedCell>=0){
    for(int i=0;i<SpellPresentation.VISUAL_COUNT;i++){int col=i%3,row=i/3;if(inside(mx,my,177+col*42,52+row*34,CELL,CELL)){sendAction(ScribesLecternMenu.ACTION_VISUAL_BASE+selectedCell*SpellPresentation.VISUAL_COUNT+i);return true;}}
   }
   if(editorMode==EditorMode.COMPONENTS&&inside(mx,my,SELECTOR_X,SELECTOR_Y,CELL,CELL)){dragSource=DRAG_SELECTOR;return true;}
   int cell=workspaceCellAt(mx,my);
   if(cell>=0&&menu.typeAt(cell)!=SpellPresentation.TYPE_EMPTY){selectedCell=cell;editorMode=EditorMode.COMPONENTS;dragSource=cell;return true;}
  }
  return super.mouseClicked(mx,my,button);
 }
 @Override public boolean mouseReleased(double mx,double my,int button){
  if(button==0&&dragSource!=DRAG_NONE){
   int target=workspaceCellAt(mx,my);
   if(target>=0){
    if(dragSource==DRAG_SELECTOR)sendAction(ScribesLecternMenu.ACTION_ADD_BASE+target);
    else if(dragSource!=target)sendAction(ScribesLecternMenu.ACTION_MOVE_BASE+dragSource*SpellPresentation.CELLS+target);
    selectedCell=target;
   }
   dragSource=DRAG_NONE;
   return true;
  }
  return super.mouseReleased(mx,my,button);
 }
 @Override public void render(GuiGraphics g,int mx,int my,float partial){
  if(writeButton!=null)writeButton.active=menu.hasAny();
  super.render(g,mx,my,partial);
  renderTooltip(g,mx,my);
  if(editorMode==EditorMode.COMPONENTS&&inside(mx,my,SELECTOR_X,SELECTOR_Y,CELL,CELL))g.renderTooltip(font,Component.literal("Missile"),mx,my);
  if(editorMode==EditorMode.VISUAL)for(int i=0;i<SpellPresentation.VISUAL_COUNT;i++){int col=i%3,row=i/3;if(inside(mx,my,177+col*42,52+row*34,CELL,CELL))g.renderTooltip(font,Component.literal(SpellPresentation.visualName(i)),mx,my);}
  if(dragSource!=DRAG_NONE)g.renderItem(SyntacticWizardry.MISSILE_SHAPE.get().getDefaultInstance(),mx-8,my-8);
 }
 @Override protected void renderLabels(GuiGraphics g,int mx,int my){}
}
