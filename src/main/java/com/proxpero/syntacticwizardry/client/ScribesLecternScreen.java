package com.proxpero.syntacticwizardry.client;
import com.proxpero.syntacticwizardry.ScribesLecternMenu;
import com.proxpero.syntacticwizardry.SpellComponentDefinition;
import com.proxpero.syntacticwizardry.SpellComponents;
import com.proxpero.syntacticwizardry.SpellPresentation;
import com.proxpero.syntacticwizardry.SpellManaCost;
import com.proxpero.syntacticwizardry.SpellPropertyDefinition;
import com.proxpero.syntacticwizardry.SpellPropertyKey;
import com.proxpero.syntacticwizardry.SpellPropertyKind;
import com.proxpero.syntacticwizardry.SyntacticWizardry;
import com.proxpero.syntacticwizardry.SphereShape;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import java.util.List;
import java.util.Arrays;
public final class ScribesLecternScreen extends AbstractContainerScreen<ScribesLecternMenu> {
 private enum PropertyTab{STYLE,VISUAL,SETTINGS}
 private enum SelectorTab{SHAPES,EFFECTS,MODIFIERS}
 private static final int SELECTOR_START_X=175,SELECTOR_GRID_Y=52,SELECTOR_COLS=5,SELECTOR_COL_STEP=24,SELECTOR_ROW_STEP=20;
 private static final int SELECTOR_VISIBLE_ROWS=3,SELECTOR_SCROLL_X=302,SELECTOR_SCROLL_Y=52,SELECTOR_SCROLL_W=5,SELECTOR_SCROLL_H=56;
 private static final int GRID_X=20,GRID_Y=47,CELL=16,STEP=18;
 private static final int NO_CELL=-1;
 private PropertyTab propertyTab=PropertyTab.SETTINGS;
 private SelectorTab selectorTab=SelectorTab.SHAPES;
 private final int[] selectorScrollRows=new int[SelectorTab.values().length];
 private boolean selectorScrollbarDragging=false;
 private int selectedCell=NO_CELL,dragSource=NO_CELL,dragNewType=SpellPresentation.TYPE_EMPTY;
 private SpellPropertyKey openOptionsProperty=null;
 private Button writeButton;
 private EditBox spellNameBox;
 private boolean costCacheValid=false;
 private int cachedPlanHash=0,cachedSettingsHash=0;
 private float cachedSpellCost=0.0F;
 public ScribesLecternScreen(ScribesLecternMenu menu,Inventory inv,Component title){super(menu,inv,title);imageWidth=ScribesLecternMenu.WIDTH;imageHeight=ScribesLecternMenu.HEIGHT;}
 @Override protected void init(){
  super.init();
  spellNameBox=new EditBox(font,leftPos+43,topPos+25,97,16,Component.literal("Spell Name"));
  spellNameBox.setMaxLength(ScribesLecternMenu.MAX_SPELL_NAME_LENGTH);
  spellNameBox.setHint(Component.literal("Spell Name"));
  addRenderableWidget(spellNameBox);
  writeButton=addRenderableWidget(Button.builder(Component.literal("Write Spell"),b->{syncSpellName();sendAction(ScribesLecternMenu.ACTION_WRITE);}).bounds(leftPos+20,topPos+174,120,15).build());
 }
 private void sendAction(int id){if(minecraft!=null&&minecraft.gameMode!=null)minecraft.gameMode.handleInventoryButtonClick(menu.containerId,id);}
 private void syncSpellName(){
  sendAction(ScribesLecternMenu.ACTION_NAME_RESET);
  if(spellNameBox==null)return;
  String value=spellNameBox.getValue();
  for(int i=0;i<value.length();i++)sendAction(ScribesLecternMenu.actionNameChar(value.charAt(i)));
 }
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
  g.drawString(font,"Cost: "+formatCost(currentSpellCost()),x+20,y+148,0xFFE8F2FF,false);
 }
 private float currentSpellCost(){
  int[] plan=menu.snapshotPlan(),settings=menu.snapshotSettings();
  int planHash=Arrays.hashCode(plan),settingsHash=Arrays.hashCode(settings);
  if(!costCacheValid||planHash!=cachedPlanHash||settingsHash!=cachedSettingsHash){
   cachedPlanHash=planHash;cachedSettingsHash=settingsHash;cachedSpellCost=SpellManaCost.calculate(plan,settings).spellCost();costCacheValid=true;
  }
  return cachedSpellCost;
 }
 private static String formatCost(float value){
  int rounded=Math.round(value);
  return Math.abs(value-rounded)<0.001F?Integer.toString(rounded):Float.toString(value);
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
  tabBox(g,x+170,y+30,40,16,"Shapes",selectorTab==SelectorTab.SHAPES);
  tabBox(g,x+212,y+30,40,16,"Effects",selectorTab==SelectorTab.EFFECTS);
  tabBox(g,x+254,y+30,51,16,"Modifiers",selectorTab==SelectorTab.MODIFIERS);
  List<SpellComponentDefinition> definitions=selectorDefinitions();
  int scroll=selectorScroll();
  g.enableScissor(x+170,y+49,x+300,y+121);
  for(int index=0;index<definitions.size();index++){
   int col=index%SELECTOR_COLS,row=index/SELECTOR_COLS;
   int visibleRow=row-scroll;
   int ox=x+SELECTOR_START_X+col*SELECTOR_COL_STEP,oy=y+SELECTOR_GRID_Y+visibleRow*SELECTOR_ROW_STEP;
   if(oy+CELL<y+49||oy>y+121)continue;
   g.fill(ox,oy,ox+CELL,oy+CELL,0xFF2A3B55);
   g.renderItem(definitions.get(index).createEditorIcon(),ox,oy);
  }
  g.disableScissor();
  renderSelectorScrollbar(g,x,y,definitions.size());
 }
 private List<SpellComponentDefinition> selectorDefinitions(){
  return switch(selectorTab){
   case SHAPES->SpellComponents.shapes();
   case EFFECTS->SpellComponents.effects();
   case MODIFIERS->SpellComponents.modifiers();
  };
 }
 private int selectorTotalRows(int count){return (count+SELECTOR_COLS-1)/SELECTOR_COLS;}
 private int selectorMaxScroll(int count){return Math.max(0,selectorTotalRows(count)-SELECTOR_VISIBLE_ROWS);}
 private int selectorScroll(){return selectorScrollRows[selectorTab.ordinal()];}
 private void setSelectorScroll(int value){
  int max=selectorMaxScroll(selectorDefinitions().size());
  selectorScrollRows[selectorTab.ordinal()]=Math.max(0,Math.min(max,value));
 }
 private void renderSelectorScrollbar(GuiGraphics g,int x,int y,int count){
  int max=selectorMaxScroll(count);
  if(max<=0)return;
  g.fill(x+SELECTOR_SCROLL_X,y+SELECTOR_SCROLL_Y,x+SELECTOR_SCROLL_X+SELECTOR_SCROLL_W,y+SELECTOR_SCROLL_Y+SELECTOR_SCROLL_H,0xFF1C293A);
  int totalRows=selectorTotalRows(count);
  int thumbH=Math.max(10,SELECTOR_SCROLL_H*SELECTOR_VISIBLE_ROWS/Math.max(1,totalRows));
  int travel=SELECTOR_SCROLL_H-thumbH;
  int thumbY=y+SELECTOR_SCROLL_Y+(max==0?0:travel*selectorScroll()/max);
  g.fill(x+SELECTOR_SCROLL_X,thumbY,x+SELECTOR_SCROLL_X+SELECTOR_SCROLL_W,thumbY+thumbH,0xFF5B86B8);
 }
 private void setSelectorScrollFromMouse(double my){
  int count=selectorDefinitions().size(),max=selectorMaxScroll(count);
  if(max<=0){setSelectorScroll(0);return;}
  int totalRows=selectorTotalRows(count);
  int thumbH=Math.max(10,SELECTOR_SCROLL_H*SELECTOR_VISIBLE_ROWS/Math.max(1,totalRows));
  int travel=Math.max(1,SELECTOR_SCROLL_H-thumbH);
  double local=my-(topPos+SELECTOR_SCROLL_Y)-thumbH/2.0;
  int row=(int)Math.round(Math.max(0.0,Math.min(travel,local))*max/travel);
  setSelectorScroll(row);
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
    renderStepper(g,x,rowY,property,menu.propertyValue(selectedCell,property.key()));
   }else{
    buttonBox(g,x+174,rowY,126,18,property.label()+": "+property.format(menu.propertyValue(selectedCell,property.key())));
   }
   rowY+=25;
  }
 }
 private void renderStepper(GuiGraphics g,int x,int y,SpellPropertyDefinition property,int value){
  g.drawString(font,property.label()+":",x+175,y+4,0xD9E9F7,false);
  buttonBox(g,x+245,y,16,16,"<");
  g.drawCenteredString(font,property.format(value),x+272,y+4,0xE8F3FF);
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
    drawBlockSpherePreview(g,left+28+laneOffset,top+h/2,menu.radiusAt(cell),menu.sphereHeightAt(cell),menu.sphereModeAt(cell),menu.visualAt(cell));
   }else if(type==SpellPresentation.TYPE_BOX){
    drawBlockBoxPreview(g,left+28+laneOffset,top+h/2,menu.boxWidthAt(cell),menu.boxHeightAt(cell),menu.boxDepthAt(cell),menu.visualAt(cell));
   }else if(type==SpellPresentation.TYPE_CONE){
    drawBlockConePreview(g,left+20+laneOffset,top+h/2,menu.boxWidthAt(cell),menu.boxHeightAt(cell),menu.boxDepthAt(cell),menu.visualAt(cell));
   }else if(type==SpellPresentation.TYPE_DAMAGE){
    int px=left+52+laneOffset,py=top+h/2-8;
    g.renderItem(SyntacticWizardry.DAMAGE_EFFECT.get().getDefaultInstance(),px,py);
    g.drawString(font,SpellPresentation.damageKindName(menu.damageKindAt(cell))+" "+menu.potenceAt(cell),px-5,py+17,0xB8CCE0,false);
   }else if(type==SpellPresentation.TYPE_DIG){
    int px=left+52+laneOffset,py=top+h/2-8;
    g.renderItem(SyntacticWizardry.DIG_EFFECT.get().getDefaultInstance(),px,py);
    g.drawString(font,"Dig "+menu.potenceAt(cell),px-5,py+17,0xB8CCE0,false);
   }else if(type==SpellComponents.TYPE_RUNE){
    int px=left+52+laneOffset,py=top+h/2-8;
    g.renderItem(SpellComponents.byType(type).createEditorIcon(),px,py);
    g.drawString(font,"Rune",px-3,py+17,0xB8CCE0,false);
   }
  }
  g.drawString(font,"Row "+(activeRow+1),x+274,y+181,0x9EB7CF,false);
 }
 private void drawBlockSpherePreview(GuiGraphics g,int cx,int cy,int radius,int height,int mode,int visual){
  int r=Math.min(radius,4),h=Math.min(height,4);
  float scale=Math.max(r,h)<=2?0.25F:0.18F;
  int spacing=Math.max(r,h)<=2?5:3;
  ItemStack stack=SpellPresentation.visualStack(visual);
  for(int dy=-h;dy<=h;dy++)for(int dx=-r;dx<=r;dx++)if(SphereShape.containsOffset(dx,dy,0,r,h,mode)){
   int px=cx+dx*spacing,py=cy+dy*spacing;
   g.pose().pushPose();
   g.pose().translate(px,py,0.0F);
   g.pose().scale(scale,scale,1.0F);
   g.renderItem(stack,-8,-8);
   g.pose().popPose();
  }
 }
 private void drawBlockBoxPreview(GuiGraphics g,int cx,int cy,int width,int height,int depth,int visual){
  int w=Math.min(width,4),h=Math.min(height,4),d=Math.min(depth,4);
  float scale=0.18F;
  int minX=-(w-1)/2,maxX=w/2,minY=-(h-1)/2,maxY=h/2,minZ=-(d-1)/2,maxZ=d/2;
  ItemStack stack=SpellPresentation.visualStack(visual);
  for(int y=maxY;y>=minY;y--)for(int z=minZ;z<=maxZ;z++)for(int x=minX;x<=maxX;x++){
   int px=cx+x*4+z*2;
   int py=cy-y*4-z*2;
   g.pose().pushPose();
   g.pose().translate(px,py,0.0F);
   g.pose().scale(scale,scale,1.0F);
   g.renderItem(stack,-8,-8);
   g.pose().popPose();
  }
 }
 private void drawBlockConePreview(GuiGraphics g,int cx,int cy,int width,int height,int depth,int visual){
  int d=Math.min(depth,5),w=Math.min(width,5),h=Math.min(height,5);
  ItemStack stack=SpellPresentation.visualStack(visual);
  for(int layer=0;layer<d;layer++){
   double scale=(layer+1)/(double)d;
   int lw=Math.max(1,(int)Math.ceil(w*scale));
   int lh=Math.max(1,(int)Math.ceil(h*scale));
   for(int yi=0;yi<lh;yi++)for(int xi=0;xi<lw;xi++){
    double xo=xi-(lw-1)/2.0,yo=yi-(lh-1)/2.0;
    int px=cx+layer*5+(int)Math.round(xo*3.0);
    int py=cy-(int)Math.round(yo*3.0);
    g.pose().pushPose();
    g.pose().translate(px,py,0.0F);
    g.pose().scale(0.18F,0.18F,1.0F);
    g.renderItem(stack,-8,-8);
    g.pose().popPose();
   }
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
  if(!inside(mx,my,170,49,130,72))return null;
  List<SpellComponentDefinition> definitions=selectorDefinitions();
  int scroll=selectorScroll();
  for(int index=0;index<definitions.size();index++){
   int col=index%SELECTOR_COLS,row=index/SELECTOR_COLS;
   int oy=SELECTOR_GRID_Y+(row-scroll)*SELECTOR_ROW_STEP;
   if(inside(mx,my,SELECTOR_START_X+col*SELECTOR_COL_STEP,oy,CELL,CELL))return definitions.get(index);
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
    if(inside(mx,my,170,30,40,16)){selectorTab=SelectorTab.SHAPES;selectorScrollbarDragging=false;return true;}
    if(inside(mx,my,212,30,40,16)){selectorTab=SelectorTab.EFFECTS;selectorScrollbarDragging=false;return true;}
    if(inside(mx,my,254,30,51,16)){selectorTab=SelectorTab.MODIFIERS;selectorScrollbarDragging=false;return true;}
    if(selectorMaxScroll(selectorDefinitions().size())>0&&inside(mx,my,SELECTOR_SCROLL_X,SELECTOR_SCROLL_Y,SELECTOR_SCROLL_W,SELECTOR_SCROLL_H)){
     selectorScrollbarDragging=true;setSelectorScrollFromMouse(my);return true;
    }
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
  if(button==0&&selectorScrollbarDragging){selectorScrollbarDragging=false;return true;}
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
 @Override public boolean mouseDragged(double mx,double my,int button,double dragX,double dragY){
  if(button==0&&selectorScrollbarDragging&&!hasSelection()){setSelectorScrollFromMouse(my);return true;}
  return super.mouseDragged(mx,my,button,dragX,dragY);
 }
 @Override public boolean mouseScrolled(double mx,double my,double scrollX,double scrollY){
  if(!hasSelection()&&inside(mx,my,170,49,137,72)){
   int before=selectorScroll();
   if(scrollY>0.0)setSelectorScroll(before-1);
   else if(scrollY<0.0)setSelectorScroll(before+1);
   return selectorScroll()!=before||selectorMaxScroll(selectorDefinitions().size())>0;
  }
  return super.mouseScrolled(mx,my,scrollX,scrollY);
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
