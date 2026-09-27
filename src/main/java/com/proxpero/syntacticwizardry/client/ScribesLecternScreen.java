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
import net.minecraft.world.item.ItemStack;
public final class ScribesLecternScreen extends AbstractContainerScreen<ScribesLecternMenu> {
 private enum EditorMode{COMPONENTS,STYLE,VISUAL}
 private static final int MISSILE_X=175,SPHERE_X=195,SELECTOR_Y=43;
 private static final int GRID_X=20,GRID_Y=47,CELL=16,STEP=18;
 private static final int NO_CELL=-1;
 private EditorMode editorMode=EditorMode.COMPONENTS;
 private int selectedCell=NO_CELL,dragSource=NO_CELL,dragNewType=SpellPresentation.TYPE_EMPTY;
 private Button writeButton;
 public ScribesLecternScreen(ScribesLecternMenu menu,Inventory inv,Component title){super(menu,inv,title);imageWidth=ScribesLecternMenu.WIDTH;imageHeight=ScribesLecternMenu.HEIGHT;}
 @Override protected void init(){super.init();writeButton=addRenderableWidget(Button.builder(Component.literal("Write Spell"),b->sendAction(ScribesLecternMenu.ACTION_WRITE)).bounds(leftPos+20,topPos+174,120,15).build());}
 private void sendAction(int id){if(minecraft!=null&&minecraft.gameMode!=null)minecraft.gameMode.handleInventoryButtonClick(menu.containerId,id);}
 private ItemStack shapeStack(int type){return type==SpellPresentation.TYPE_SPHERE?SyntacticWizardry.SPHERE_SHAPE.get().getDefaultInstance():SyntacticWizardry.MISSILE_SHAPE.get().getDefaultInstance();}
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
   int type=menu.typeAt(cell);
   if(type!=SpellPresentation.TYPE_EMPTY)g.renderItem(shapeStack(type),sx,sy);
  }
  if(selectedCell>=0&&menu.typeAt(selectedCell)!=SpellPresentation.TYPE_EMPTY){
   if(menu.typeAt(selectedCell)==SpellPresentation.TYPE_SPHERE){
    g.drawString(font,"Radius: "+menu.radiusAt(selectedCell),x+20,y+141,0xD9E9F7,false);
    buttonBox(g,x+99,y+138,16,13,"<");
    buttonBox(g,x+120,y+138,16,13,">");
   }
   buttonBox(g,x+20,y+154,55,16,"Style");
   buttonBox(g,x+82,y+154,55,16,"Visual");
  }
 }
 private void renderSelector(GuiGraphics g,int x,int y){
  if(editorMode==EditorMode.COMPONENTS){
   g.drawString(font,"Shapes",x+173,y+29,0xCFE5FF,false);
   g.fill(x+MISSILE_X,y+SELECTOR_Y,x+MISSILE_X+CELL,y+SELECTOR_Y+CELL,0xFF2A3B55);
   g.renderItem(SyntacticWizardry.MISSILE_SHAPE.get().getDefaultInstance(),x+MISSILE_X,y+SELECTOR_Y);
   g.fill(x+SPHERE_X,y+SELECTOR_Y,x+SPHERE_X+CELL,y+SELECTOR_Y+CELL,0xFF2A3B55);
   g.renderItem(SyntacticWizardry.SPHERE_SHAPE.get().getDefaultInstance(),x+SPHERE_X,y+SELECTOR_Y);
   return;
  }
  buttonBox(g,x+174,y+29,40,15,"Back");
  int selectedType=selectedCell>=0?menu.typeAt(selectedCell):SpellPresentation.TYPE_EMPTY;
  if(editorMode==EditorMode.STYLE){
   g.drawString(font,"Style",x+220,y+33,0xCFE5FF,false);
   int count=selectedType==SpellPresentation.TYPE_MISSILE?SpellPresentation.STYLE_COUNT:1;
   for(int i=0;i<count;i++){
    int oy=y+51+i*21;
    g.fill(x+174,oy,x+300,oy+18,selectedCell>=0&&menu.styleAt(selectedCell)==i?0xFF36587A:0xB02A3B55);
    g.drawString(font,SpellPresentation.styleName(i),x+181,oy+5,0xE8F3FF,false);
   }
  }else{
   g.drawString(font,"Visual",x+220,y+33,0xCFE5FF,false);
   int count=selectedType==SpellPresentation.TYPE_MISSILE?SpellPresentation.VISUAL_COUNT:1;
   for(int i=0;i<count;i++){
    int col=i%3,row=i/3,ox=x+177+col*42,oy=y+52+row*34;
    g.fill(ox,oy,ox+CELL,oy+CELL,selectedCell>=0&&menu.visualAt(selectedCell)==i?0xFF5B86B8:0xFF2A3B55);
    if(selectedType==SpellPresentation.TYPE_MISSILE)g.renderItem(SpellPresentation.visualStack(i),ox,oy);
    else g.renderItem(SyntacticWizardry.SPHERE_SHAPE.get().getDefaultInstance(),ox,oy);
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
   int type=SpellPresentation.typeAt(plan,cell);
   if(type==SpellPresentation.TYPE_EMPTY)continue;
   int laneOffset=(lane-(count-1)/2)*16;lane++;
   if(type==SpellPresentation.TYPE_MISSILE){
    int px=left+(int)(t*(w-16));
    int py=previewY(top+h/2+laneOffset/3,t,SpellPresentation.styleAt(plan,cell))-8;
    g.renderItem(SpellPresentation.visualStack(SpellPresentation.visualAt(plan,cell)),px,py);
   }else if(type==SpellPresentation.TYPE_SPHERE){
    drawBlockSpherePreview(g,left+20+laneOffset,top+h/2,menu.radiusAt(cell));
   }
  }
  g.drawString(font,"Row "+(activeRow+1),x+274,y+181,0x9EB7CF,false);
 }
 private void drawBlockSpherePreview(GuiGraphics g,int cx,int cy,int radius){
  int r=Math.min(radius,4);
  int size=Math.max(1,4-r/2);
  for(int dy=-r;dy<=r;dy++)for(int dx=-r;dx<=r;dx++)if(dx*dx+dy*dy<=r*r){
   int px=cx+dx*size,py=cy+dy*size;
   g.fill(px,py,px+size,py+size,0xCC77A9D8);
  }
 }
 private int previewY(int base,double t,int style){if(style==SpellPresentation.STYLE_ARC)return base-(int)(Math.sin(Math.PI*t)*10.0);if(style==SpellPresentation.STYLE_SPIRAL)return base+(int)(Math.sin(t*Math.PI*4.0)*6.0);return base;}
 private void panel(GuiGraphics g,int x,int y,int w,int h,String name){g.fill(x,y,x+w,y+h,0xC0202D42);g.renderOutline(x,y,w,h,0xFF5B86B8);g.drawString(font,name,x+7,y+6,0xE8F3FF,false);}
 private void buttonBox(GuiGraphics g,int x,int y,int w,int h,String text){g.fill(x,y,x+w,y+h,0xFF30445E);g.renderOutline(x,y,w,h,0xFF5B86B8);g.drawCenteredString(font,text,x+w/2,y+4,0xE8F3FF);}
 private boolean inside(double mx,double my,int x,int y,int w,int h){return mx>=leftPos+x&&mx<leftPos+x+w&&my>=topPos+y&&my<topPos+y+h;}
 private int workspaceCellAt(double mx,double my){
  for(int row=0;row<SpellPresentation.ROWS;row++)for(int col=0;col<SpellPresentation.COLS;col++){int x=GRID_X+col*STEP,y=GRID_Y+row*STEP;if(inside(mx,my,x,y,CELL,CELL))return row*SpellPresentation.COLS+col;}
  return NO_CELL;
 }
 private void beginNewDrag(int type){dragSource=NO_CELL;dragNewType=type;}
 private int draggedType(){return dragNewType!=SpellPresentation.TYPE_EMPTY?dragNewType:(dragSource>=0?menu.typeAt(dragSource):SpellPresentation.TYPE_EMPTY);}
 @Override public boolean mouseClicked(double mx,double my,int button){
  if(button==1){int cell=workspaceCellAt(mx,my);if(cell>=0&&menu.typeAt(cell)!=SpellPresentation.TYPE_EMPTY){sendAction(ScribesLecternMenu.ACTION_CLEAR_BASE+cell);if(selectedCell==cell)selectedCell=NO_CELL;return true;}}
  if(button==0){
   if(selectedCell>=0&&menu.typeAt(selectedCell)==SpellPresentation.TYPE_SPHERE){
    int current=menu.radiusAt(selectedCell);
    if(inside(mx,my,99,138,16,13)){int value=Math.max(SpellPresentation.RADIUS_MIN,current-1);sendAction(ScribesLecternMenu.ACTION_RADIUS_BASE+selectedCell*SpellPresentation.RADIUS_COUNT+(value-SpellPresentation.RADIUS_MIN));return true;}
    if(inside(mx,my,120,138,16,13)){int value=Math.min(SpellPresentation.RADIUS_MAX,current+1);sendAction(ScribesLecternMenu.ACTION_RADIUS_BASE+selectedCell*SpellPresentation.RADIUS_COUNT+(value-SpellPresentation.RADIUS_MIN));return true;}
   }
   if(selectedCell>=0&&menu.typeAt(selectedCell)!=SpellPresentation.TYPE_EMPTY&&inside(mx,my,20,154,55,16)){editorMode=EditorMode.STYLE;return true;}
   if(selectedCell>=0&&menu.typeAt(selectedCell)!=SpellPresentation.TYPE_EMPTY&&inside(mx,my,82,154,55,16)){editorMode=EditorMode.VISUAL;return true;}
   if(editorMode!=EditorMode.COMPONENTS&&inside(mx,my,174,29,40,15)){editorMode=EditorMode.COMPONENTS;return true;}
   if(editorMode==EditorMode.STYLE&&selectedCell>=0){
    int count=menu.typeAt(selectedCell)==SpellPresentation.TYPE_MISSILE?SpellPresentation.STYLE_COUNT:1;
    for(int i=0;i<count;i++)if(inside(mx,my,174,51+i*21,126,18)){sendAction(ScribesLecternMenu.ACTION_STYLE_BASE+selectedCell*SpellPresentation.STYLE_COUNT+i);return true;}
   }
   if(editorMode==EditorMode.VISUAL&&selectedCell>=0){
    int count=menu.typeAt(selectedCell)==SpellPresentation.TYPE_MISSILE?SpellPresentation.VISUAL_COUNT:1;
    for(int i=0;i<count;i++){int col=i%3,row=i/3;if(inside(mx,my,177+col*42,52+row*34,CELL,CELL)){sendAction(ScribesLecternMenu.ACTION_VISUAL_BASE+selectedCell*SpellPresentation.VISUAL_COUNT+i);return true;}}
   }
   if(editorMode==EditorMode.COMPONENTS&&inside(mx,my,MISSILE_X,SELECTOR_Y,CELL,CELL)){beginNewDrag(SpellPresentation.TYPE_MISSILE);return true;}
   if(editorMode==EditorMode.COMPONENTS&&inside(mx,my,SPHERE_X,SELECTOR_Y,CELL,CELL)){beginNewDrag(SpellPresentation.TYPE_SPHERE);return true;}
   int cell=workspaceCellAt(mx,my);
   if(cell>=0&&menu.typeAt(cell)!=SpellPresentation.TYPE_EMPTY){selectedCell=cell;editorMode=EditorMode.COMPONENTS;dragSource=cell;dragNewType=SpellPresentation.TYPE_EMPTY;return true;}
  }
  return super.mouseClicked(mx,my,button);
 }
 @Override public boolean mouseReleased(double mx,double my,int button){
  if(button==0&&(dragSource>=0||dragNewType!=SpellPresentation.TYPE_EMPTY)){
   int target=workspaceCellAt(mx,my);
   if(target>=0){
    if(dragNewType==SpellPresentation.TYPE_MISSILE)sendAction(ScribesLecternMenu.ACTION_ADD_MISSILE_BASE+target);
    else if(dragNewType==SpellPresentation.TYPE_SPHERE)sendAction(ScribesLecternMenu.ACTION_ADD_SPHERE_BASE+target);
    else if(dragSource!=target)sendAction(ScribesLecternMenu.ACTION_MOVE_BASE+dragSource*SpellPresentation.CELLS+target);
    selectedCell=target;
   }
   dragSource=NO_CELL;
   dragNewType=SpellPresentation.TYPE_EMPTY;
   return true;
  }
  return super.mouseReleased(mx,my,button);
 }
 @Override public void render(GuiGraphics g,int mx,int my,float partial){
  if(writeButton!=null)writeButton.active=menu.hasAny();
  super.render(g,mx,my,partial);
  renderTooltip(g,mx,my);
  if(editorMode==EditorMode.COMPONENTS&&inside(mx,my,MISSILE_X,SELECTOR_Y,CELL,CELL))g.renderTooltip(font,Component.literal("Missile"),mx,my);
  if(editorMode==EditorMode.COMPONENTS&&inside(mx,my,SPHERE_X,SELECTOR_Y,CELL,CELL))g.renderTooltip(font,Component.literal("Sphere"),mx,my);
  if(editorMode==EditorMode.VISUAL&&selectedCell>=0){int count=menu.typeAt(selectedCell)==SpellPresentation.TYPE_MISSILE?SpellPresentation.VISUAL_COUNT:1;for(int i=0;i<count;i++){int col=i%3,row=i/3;if(inside(mx,my,177+col*42,52+row*34,CELL,CELL))g.renderTooltip(font,Component.literal(menu.typeAt(selectedCell)==SpellPresentation.TYPE_MISSILE?SpellPresentation.visualName(i):"Default"),mx,my);}}
  int type=draggedType();
  if(type!=SpellPresentation.TYPE_EMPTY)g.renderItem(shapeStack(type),mx-8,my-8);
 }
 @Override protected void renderLabels(GuiGraphics g,int mx,int my){}
}
