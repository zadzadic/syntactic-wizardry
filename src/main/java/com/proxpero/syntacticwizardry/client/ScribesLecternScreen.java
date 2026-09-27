package com.proxpero.syntacticwizardry.client;
import com.proxpero.syntacticwizardry.ScribesLecternMenu;
import com.proxpero.syntacticwizardry.SpellComponentDefinition;
import com.proxpero.syntacticwizardry.SpellComponents;
import com.proxpero.syntacticwizardry.SpellPresentation;
import com.proxpero.syntacticwizardry.SpellPropertyDefinition;
import com.proxpero.syntacticwizardry.SpellPropertyKey;
import com.proxpero.syntacticwizardry.SpellPropertyKind;
import com.proxpero.syntacticwizardry.SyntacticWizardry;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import java.util.List;
public final class ScribesLecternScreen extends AbstractContainerScreen<ScribesLecternMenu> {
 private enum PropertyTab{STYLE,VISUAL,SETTINGS}
 private static final int SELECTOR_START_X=175,SHAPE_Y=43,EFFECT_Y=82;
 private static final int GRID_X=20,GRID_Y=47,CELL=16,STEP=18;
 private static final int NO_CELL=-1;
 private PropertyTab propertyTab=PropertyTab.SETTINGS;
 private int selectedCell=NO_CELL,dragSource=NO_CELL,dragNewType=SpellPresentation.TYPE_EMPTY;
 private SpellPropertyKey openOptionsProperty=null;
 private Button writeButton;
 public ScribesLecternScreen(ScribesLecternMenu menu,Inventory inv,Component title){super(menu,inv,title);imageWidth=ScribesLecternMenu.WIDTH;imageHeight=ScribesLecternMenu.HEIGHT;}
 @Override protected void init(){super.init();writeButton=addRenderableWidget(Button.builder(Component.literal("Write Spell"),b->sendAction(ScribesLecternMenu.ACTION_WRITE)).bounds(leftPos+20,topPos+174,120,15).build());}
 private void sendAction(int id){if(minecraft!=null&&minecraft.gameMode!=null)minecraft.gameMode.handleInventoryButtonClick(menu.containerId,id);}
 private ItemStack componentStack(int type){SpellComponentDefinition definition=SpellComponents.byType(type);return definition!=null?definition.createEditorIcon():ItemStack.EMPTY;}
 private boolean hasSelection(){return selectedCell>=0&&menu.typeAt(selectedCell)!=SpellPresentation.TYPE_EMPTY;}
 private SpellComponentDefinition selectedDefinition(){return hasSelection()?menu.definitionAt(selectedCell):null;}
 @Override protected void renderBg(GuiGraphics g,float partial,int mx,int my){
  int x=leftPos,y=topPos;
  g.fill(x,y,x+320,y+232,0xE0101826);
  panel(g,x+10,y+10,145,184,"Spell Workspace");
  panel(g,x+165,y+10,145,116,hasSelection()?"Properties: "+menu.definitionAt(selectedCell).displayName():"Component Selector");
  panel(g,x+165,y+136,145,58,"Spell Preview");
  g.fill(x+75,y+199,x+247,y+227,0xC0182233);
  g.fill(x+19,y+24,x+37,y+42,0xFF335A88);
  renderWorkspace(g,x,y);
  renderRightPanel(g,x,y);
  renderPreview(g,x,y);
 }
 private void renderWorkspace(GuiGraphics g,int x,int y){
  for(int row=0;row<SpellPresentation.ROWS;row++)for(int col=0;col<SpellPresentation.COLS;col++){
   int cell=row*SpellPresentation.COLS+col,sx=x+GRID_X+col*STEP,sy=y+GRID_Y+row*STEP;
   g.fill(sx,sy,sx+CELL,sy+CELL,cell==selectedCell?0xFF4B7197:0x7030445E);
   int type=menu.typeAt(cell);
   if(type!=SpellPresentation.TYPE_EMPTY)g.renderItem(componentStack(type),sx,sy);
  }
 }
 private void renderRightPanel(GuiGraphics g,int x,int y){
  if(!hasSelection()){renderComponentSelector(g,x,y);return;}
  buttonBox(g,x+291,y+15,14,12,"<");
  tabBox(g,x+170,y+30,40,16,"Style",propertyTab==PropertyTab.STYLE);
  tabBox(g,x+212,y+30,40,16,"Visual",propertyTab==PropertyTab.VISUAL);
  tabBox(g,x+254,y+30,51,16,"Settings",propertyTab==PropertyTab.SETTINGS);
  if(propertyTab==PropertyTab.STYLE)renderStyleTab(g,x,y);
  else if(propertyTab==PropertyTab.VISUAL)renderVisualTab(g,x,y);
  else renderSettingsTab(g,x,y);
 }
 private void renderComponentSelector(GuiGraphics g,int x,int y){
  g.drawString(font,"Shapes",x+173,y+29,0xCFE5FF,false);
  int index=0;
  for(SpellComponentDefinition definition:SpellComponents.shapes()){
   int ox=x+SELECTOR_START_X+index*20;
   g.fill(ox,y+SHAPE_Y,ox+CELL,y+SHAPE_Y+CELL,0xFF2A3B55);
   g.renderItem(definition.createEditorIcon(),ox,y+SHAPE_Y);
   index++;
  }
  g.drawString(font,"Effects",x+173,y+68,0xCFE5FF,false);
  index=0;
  for(SpellComponentDefinition definition:SpellComponents.effects()){
   int ox=x+SELECTOR_START_X+index*20;
   g.fill(ox,y+EFFECT_Y,ox+CELL,y+EFFECT_Y+CELL,0xFF2A3B55);
   g.renderItem(definition.createEditorIcon(),ox,y+EFFECT_Y);
   index++;
  }
 }
 private void renderStyleTab(GuiGraphics g,int x,int y){
  SpellComponentDefinition definition=selectedDefinition();
  if(definition==null||definition.styleOptions().isEmpty()){g.drawCenteredString(font,"Not applicable",x+237,y+73,0x9EB7CF);return;}
  List<Integer> styles=definition.styleOptions();
  for(int i=0;i<styles.size();i++){
   int styleId=styles.get(i),oy=y+52+i*21;
   g.fill(x+174,oy,x+300,oy+18,menu.styleAt(selectedCell)==styleId?0xFF36587A:0xB02A3B55);
   g.drawString(font,SpellPresentation.styleName(styleId),x+181,oy+5,0xE8F3FF,false);
  }
 }
 private void renderVisualTab(GuiGraphics g,int x,int y){
  SpellComponentDefinition definition=selectedDefinition();
  if(definition==null||!definition.supportsVisuals()){g.drawCenteredString(font,"Not applicable",x+237,y+73,0x9EB7CF);return;}
  for(int i=0;i<SpellPresentation.VISUAL_COUNT;i++){
   int col=i%3,row=i/3,ox=x+177+col*42,oy=y+55+row*34;
   g.fill(ox,oy,ox+CELL,oy+CELL,menu.visualAt(selectedCell)==i?0xFF5B86B8:0xFF2A3B55);
   g.renderItem(SpellPresentation.visualStack(i),ox,oy);
  }
 }
 private void renderSettingsTab(GuiGraphics g,int x,int y){
  SpellComponentDefinition definition=selectedDefinition();
  if(definition==null||definition.settings().isEmpty()){g.drawCenteredString(font,"No settings",x+237,y+73,0x9EB7CF);return;}
  if(openOptionsProperty!=null){renderOptionMenu(g,x,y);return;}
  int rowY=y+55;
  for(SpellPropertyDefinition property:definition.settings()){
   if(property.kind()==SpellPropertyKind.STEPPER){
    renderStepper(g,x,rowY,property.label(),menu.propertyValue(selectedCell,property.key()));
   }else{
    buttonBox(g,x+174,rowY,126,18,property.label()+": "+property.format(menu.propertyValue(selectedCell,property.key())));
   }
   rowY+=25;
  }
 }
 private void renderStepper(GuiGraphics g,int x,int y,String label,int value){
  g.drawString(font,label+":",x+175,y+4,0xD9E9F7,false);
  buttonBox(g,x+245,y,16,16,"<");
  g.drawCenteredString(font,Integer.toString(value),x+272,y+4,0xE8F3FF);
  buttonBox(g,x+284,y,16,16,">");
 }
 private void renderOptionMenu(GuiGraphics g,int x,int y){
  SpellPropertyDefinition property=findSetting(openOptionsProperty);
  if(property==null)return;
  int count=property.maxValue()-property.minValue()+1;
  for(int i=0;i<count;i++){
   int value=property.minValue()+i;
   int col=i%2,row=i/2,ox=x+174+col*64,oy=y+51+row*18;
   g.fill(ox,oy,ox+61,oy+15,menu.propertyValue(selectedCell,property.key())==value?0xFF36587A:0xB02A3B55);
   g.drawCenteredString(font,property.format(value),ox+30,oy+4,0xE8F3FF);
  }
 }
 private SpellPropertyDefinition findSetting(SpellPropertyKey key){
  SpellComponentDefinition definition=selectedDefinition();
  if(definition==null)return null;
  for(SpellPropertyDefinition property:definition.settings())if(property.key()==key)return property;
  return null;
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
    drawBlockSpherePreview(g,left+28+laneOffset,top+h/2,menu.radiusAt(cell),menu.visualAt(cell));
   }else if(type==SpellPresentation.TYPE_DAMAGE){
    int px=left+52+laneOffset,py=top+h/2-8;
    g.renderItem(SyntacticWizardry.DAMAGE_EFFECT.get().getDefaultInstance(),px,py);
    g.drawString(font,SpellPresentation.damageKindName(menu.damageKindAt(cell))+" "+menu.potenceAt(cell),px-5,py+17,0xB8CCE0,false);
   }
  }
  g.drawString(font,"Row "+(activeRow+1),x+274,y+181,0x9EB7CF,false);
 }
 private void drawBlockSpherePreview(GuiGraphics g,int cx,int cy,int radius,int visual){
  int r=Math.min(radius,4);
  float scale=r<=2?0.25F:0.18F;
  int spacing=r<=2?5:3;
  ItemStack stack=SpellPresentation.visualStack(visual);
  for(int dy=-r;dy<=r;dy++)for(int dx=-r;dx<=r;dx++)if(dx*dx+dy*dy<=r*r){
   int px=cx+dx*spacing,py=cy+dy*spacing;
   g.pose().pushPose();
   g.pose().translate(px,py,0.0F);
   g.pose().scale(scale,scale,1.0F);
   g.renderItem(stack,-8,-8);
   g.pose().popPose();
  }
 }
 private int previewY(int base,double t,int style){if(style==SpellPresentation.STYLE_ARC)return base-(int)(Math.sin(Math.PI*t)*10.0);if(style==SpellPresentation.STYLE_SPIRAL)return base+(int)(Math.sin(t*Math.PI*4.0)*6.0);return base;}
 private void panel(GuiGraphics g,int x,int y,int w,int h,String name){g.fill(x,y,x+w,y+h,0xC0202D42);g.renderOutline(x,y,w,h,0xFF5B86B8);g.drawString(font,name,x+7,y+6,0xE8F3FF,false);}
 private void tabBox(GuiGraphics g,int x,int y,int w,int h,String text,boolean active){g.fill(x,y,x+w,y+h,active?0xFF466B91:0xFF30445E);g.renderOutline(x,y,w,h,0xFF5B86B8);g.drawCenteredString(font,text,x+w/2,y+4,0xE8F3FF);}
 private void buttonBox(GuiGraphics g,int x,int y,int w,int h,String text){g.fill(x,y,x+w,y+h,0xFF30445E);g.renderOutline(x,y,w,h,0xFF5B86B8);g.drawCenteredString(font,text,x+w/2,y+4,0xE8F3FF);}
 private boolean inside(double mx,double my,int x,int y,int w,int h){return mx>=leftPos+x&&mx<leftPos+x+w&&my>=topPos+y&&my<topPos+y+h;}
 private int workspaceCellAt(double mx,double my){
  for(int row=0;row<SpellPresentation.ROWS;row++)for(int col=0;col<SpellPresentation.COLS;col++){int x=GRID_X+col*STEP,y=GRID_Y+row*STEP;if(inside(mx,my,x,y,CELL,CELL))return row*SpellPresentation.COLS+col;}
  return NO_CELL;
 }
 private SpellComponentDefinition selectorComponentAt(double mx,double my){
  int index=0;
  for(SpellComponentDefinition definition:SpellComponents.shapes()){
   if(inside(mx,my,SELECTOR_START_X+index*20,SHAPE_Y,CELL,CELL))return definition;
   index++;
  }
  index=0;
  for(SpellComponentDefinition definition:SpellComponents.effects()){
   if(inside(mx,my,SELECTOR_START_X+index*20,EFFECT_Y,CELL,CELL))return definition;
   index++;
  }
  return null;
 }
 private void beginNewDrag(int type){dragSource=NO_CELL;dragNewType=type;}
 private int draggedType(){return dragNewType!=SpellPresentation.TYPE_EMPTY?dragNewType:(dragSource>=0?menu.typeAt(dragSource):SpellPresentation.TYPE_EMPTY);}
 private void selectCell(int cell){selectedCell=cell;propertyTab=PropertyTab.SETTINGS;openOptionsProperty=null;}
 private void closeProperties(){selectedCell=NO_CELL;openOptionsProperty=null;}
 @Override public boolean mouseClicked(double mx,double my,int button){
  if(button==1){
   int cell=workspaceCellAt(mx,my);
   if(cell>=0&&menu.typeAt(cell)!=SpellPresentation.TYPE_EMPTY){sendAction(ScribesLecternMenu.actionClear(cell));if(selectedCell==cell)closeProperties();return true;}
  }
  if(button==0){
   if(hasSelection()){
    if(inside(mx,my,291,15,14,12)){closeProperties();return true;}
    if(inside(mx,my,170,30,40,16)){propertyTab=PropertyTab.STYLE;openOptionsProperty=null;return true;}
    if(inside(mx,my,212,30,40,16)){propertyTab=PropertyTab.VISUAL;openOptionsProperty=null;return true;}
    if(inside(mx,my,254,30,51,16)){propertyTab=PropertyTab.SETTINGS;openOptionsProperty=null;return true;}
    SpellComponentDefinition definition=selectedDefinition();
    if(definition!=null&&propertyTab==PropertyTab.STYLE&&!definition.styleOptions().isEmpty()){
      List<Integer> styles=definition.styleOptions();
      for(int i=0;i<styles.size();i++)if(inside(mx,my,174,52+i*21,126,18)){sendAction(ScribesLecternMenu.actionSetProperty(selectedCell,SpellPropertyKey.STYLE,styles.get(i)));return true;}
    }
    if(definition!=null&&propertyTab==PropertyTab.VISUAL&&definition.supportsVisuals()){
      for(int i=0;i<SpellPresentation.VISUAL_COUNT;i++){int col=i%3,row=i/3;if(inside(mx,my,177+col*42,55+row*34,CELL,CELL)){sendAction(ScribesLecternMenu.actionSetProperty(selectedCell,SpellPropertyKey.VISUAL,i));return true;}}
    }
    if(definition!=null&&propertyTab==PropertyTab.SETTINGS&&!definition.settings().isEmpty()){
      if(openOptionsProperty!=null){
        SpellPropertyDefinition property=findSetting(openOptionsProperty);
        if(property!=null){
         int count=property.maxValue()-property.minValue()+1;
         for(int i=0;i<count;i++){
          int value=property.minValue()+i;
          int col=i%2,row=i/2;
          if(inside(mx,my,174+col*64,51+row*18,61,15)){sendAction(ScribesLecternMenu.actionSetProperty(selectedCell,property.key(),value));openOptionsProperty=null;return true;}
         }
        }
      }else{
        int rowY=55;
        for(SpellPropertyDefinition property:definition.settings()){
         if(property.kind()==SpellPropertyKind.STEPPER){
          int current=menu.propertyValue(selectedCell,property.key());
          if(inside(mx,my,245,rowY,16,16)){sendAction(ScribesLecternMenu.actionSetProperty(selectedCell,property.key(),Math.max(property.minValue(),current-1)));return true;}
          if(inside(mx,my,284,rowY,16,16)){sendAction(ScribesLecternMenu.actionSetProperty(selectedCell,property.key(),Math.min(property.maxValue(),current+1)));return true;}
         }else if(inside(mx,my,174,rowY,126,18)){
          openOptionsProperty=property.key();
          return true;
         }
         rowY+=25;
        }
      }
    }
   }else{
    SpellComponentDefinition selector=selectorComponentAt(mx,my);
    if(selector!=null){beginNewDrag(selector.typeId());return true;}
   }
   int cell=workspaceCellAt(mx,my);
   if(cell>=0&&menu.typeAt(cell)!=SpellPresentation.TYPE_EMPTY){selectCell(cell);dragSource=cell;dragNewType=SpellPresentation.TYPE_EMPTY;return true;}
   if(cell>=0&&hasSelection()){closeProperties();return true;}
  }
  return super.mouseClicked(mx,my,button);
 }
 @Override public boolean mouseReleased(double mx,double my,int button){
  if(button==0&&(dragSource>=0||dragNewType!=SpellPresentation.TYPE_EMPTY)){
   int target=workspaceCellAt(mx,my);
   if(target>=0){
    if(dragNewType!=SpellPresentation.TYPE_EMPTY)sendAction(ScribesLecternMenu.actionAddComponent(target,dragNewType));
    else if(dragSource!=target)sendAction(ScribesLecternMenu.actionMove(dragSource,target));
    selectCell(target);
   }
   dragSource=NO_CELL;dragNewType=SpellPresentation.TYPE_EMPTY;
   return true;
  }
  return super.mouseReleased(mx,my,button);
 }
 @Override public void render(GuiGraphics g,int mx,int my,float partial){
  if(writeButton!=null)writeButton.active=menu.hasAny();
  super.render(g,mx,my,partial);
  renderTooltip(g,mx,my);
  if(!hasSelection()){
   SpellComponentDefinition selector=selectorComponentAt(mx,my);
   if(selector!=null)g.renderTooltip(font,Component.literal(selector.displayName()),mx,my);
  }
  if(hasSelection()&&propertyTab==PropertyTab.VISUAL&&selectedDefinition()!=null&&selectedDefinition().supportsVisuals())for(int i=0;i<SpellPresentation.VISUAL_COUNT;i++){int col=i%3,row=i/3;if(inside(mx,my,177+col*42,55+row*34,CELL,CELL))g.renderTooltip(font,Component.literal(SpellPresentation.visualName(i)),mx,my);}
  int type=draggedType();if(type!=SpellPresentation.TYPE_EMPTY)g.renderItem(componentStack(type),mx-8,my-8);
 }
 @Override protected void renderLabels(GuiGraphics g,int mx,int my){}
}
