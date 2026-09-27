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
 private static final int MISSILE_X=175,MISSILE_Y=43;
 private EditorMode editorMode=EditorMode.COMPONENTS;
 private boolean workspaceMissileSelected;
 private Button writeButton;
 public ScribesLecternScreen(ScribesLecternMenu menu,Inventory inv,Component title){super(menu,inv,title);imageWidth=ScribesLecternMenu.WIDTH;imageHeight=ScribesLecternMenu.HEIGHT;}
 @Override protected void init(){
  super.init();
  writeButton=addRenderableWidget(Button.builder(Component.literal("Write Spell"),b->sendAction(ScribesLecternMenu.ACTION_WRITE)).bounds(leftPos+20,topPos+166,120,20).build());
 }
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
  if(!menu.hasMissile()){g.drawString(font,"Add a Shape to begin.",x+20,y+52,0xA9B8C8,false);return;}
  int rx=x+19,ry=y+50;
  g.fill(rx,ry,rx+126,ry+23,workspaceMissileSelected?0xFF36587A:0xB02A3B55);
  g.renderItem(SyntacticWizardry.MISSILE_SHAPE.get().getDefaultInstance(),rx+3,ry+3);
  g.drawString(font,"Missile",rx+25,ry+7,0xE8F3FF,false);
  if(workspaceMissileSelected){
   buttonBox(g,x+20,y+82,55,18,"Style");
   buttonBox(g,x+82,y+82,55,18,"Visual");
   g.drawString(font,"Style: "+SpellPresentation.styleName(menu.style()),x+20,y+108,0xBED4EA,false);
   g.drawString(font,"Visual: "+SpellPresentation.visualName(menu.visual()),x+20,y+121,0xBED4EA,false);
  }
 }
 private void renderSelector(GuiGraphics g,int x,int y){
  if(editorMode==EditorMode.COMPONENTS){
   g.drawString(font,"Shapes",x+173,y+29,0xCFE5FF,false);
   int sx=x+MISSILE_X,sy=y+MISSILE_Y;
   g.fill(sx-1,sy-1,sx+17,sy+17,0xFF2A3B55);
   g.renderItem(SyntacticWizardry.MISSILE_SHAPE.get().getDefaultInstance(),sx,sy);
   return;
  }
  buttonBox(g,x+174,y+29,40,15,"Back");
  if(editorMode==EditorMode.STYLE){
   g.drawString(font,"Style",x+220,y+33,0xCFE5FF,false);
   for(int i=0;i<3;i++){
    int oy=y+51+i*21;
    g.fill(x+174,oy,x+300,oy+18,menu.style()==i?0xFF36587A:0xB02A3B55);
    g.drawString(font,SpellPresentation.styleName(i),x+181,oy+5,0xE8F3FF,false);
   }
  }else{
   g.drawString(font,"Visual",x+220,y+33,0xCFE5FF,false);
   for(int i=0;i<6;i++){
    int col=i%3,row=i/3,ox=x+177+col*42,oy=y+52+row*34;
    g.fill(ox-2,oy-2,ox+20,oy+20,menu.visual()==i?0xFF5B86B8:0xFF2A3B55);
    g.renderItem(SpellPresentation.visualStack(i),ox,oy);
   }
  }
 }
 private void renderPreview(GuiGraphics g,int x,int y){
  if(!menu.hasMissile())return;
  int left=x+174,top=y+154,w=126,h=32;
  for(int i=0;i<=18;i++){
   double t=i/18.0;
   int px=left+(int)(t*(w-4));
   int py=previewY(top,h,t,menu.style());
   g.fill(px,py,px+1,py+1,0x806FA7D8);
  }
  double t=(Util.getMillis()%1600L)/1600.0;
  int px=left+(int)(t*(w-16));
  int py=previewY(top,h,t,menu.style())-8;
  g.renderItem(SpellPresentation.visualStack(menu.visual()),px,py);
 }
 private int previewY(int top,int h,double t,int style){
  int base=top+h/2;
  if(style==SpellPresentation.STYLE_ARC)return base-(int)(Math.sin(Math.PI*t)*12.0);
  if(style==SpellPresentation.STYLE_SPIRAL)return base+(int)(Math.sin(t*Math.PI*4.0)*7.0);
  return base;
 }
 private void panel(GuiGraphics g,int x,int y,int w,int h,String name){g.fill(x,y,x+w,y+h,0xC0202D42);g.renderOutline(x,y,w,h,0xFF5B86B8);g.drawString(font,name,x+7,y+6,0xE8F3FF,false);}
 private void buttonBox(GuiGraphics g,int x,int y,int w,int h,String text){g.fill(x,y,x+w,y+h,0xFF30445E);g.renderOutline(x,y,w,h,0xFF5B86B8);g.drawCenteredString(font,text,x+w/2,y+5,0xE8F3FF);}
 private boolean inside(double mx,double my,int x,int y,int w,int h){return mx>=leftPos+x&&mx<leftPos+x+w&&my>=topPos+y&&my<topPos+y+h;}
 @Override public boolean mouseClicked(double mx,double my,int button){
  if(button==0){
   if(editorMode==EditorMode.COMPONENTS&&inside(mx,my,MISSILE_X,MISSILE_Y,16,16)){sendAction(ScribesLecternMenu.ACTION_ADD_MISSILE);workspaceMissileSelected=false;return true;}
   if(menu.hasMissile()&&inside(mx,my,19,50,126,23)){workspaceMissileSelected=true;editorMode=EditorMode.COMPONENTS;return true;}
   if(workspaceMissileSelected&&inside(mx,my,20,82,55,18)){editorMode=EditorMode.STYLE;return true;}
   if(workspaceMissileSelected&&inside(mx,my,82,82,55,18)){editorMode=EditorMode.VISUAL;return true;}
   if(editorMode!=EditorMode.COMPONENTS&&inside(mx,my,174,29,40,15)){editorMode=EditorMode.COMPONENTS;return true;}
   if(editorMode==EditorMode.STYLE){
    for(int i=0;i<3;i++)if(inside(mx,my,174,51+i*21,126,18)){sendAction(ScribesLecternMenu.ACTION_STYLE_BASE+i);return true;}
   }
   if(editorMode==EditorMode.VISUAL){
    for(int i=0;i<6;i++){int col=i%3,row=i/3;if(inside(mx,my,177+col*42,52+row*34,20,20)){sendAction(ScribesLecternMenu.ACTION_VISUAL_BASE+i);return true;}}
   }
  }
  return super.mouseClicked(mx,my,button);
 }
 @Override public void render(GuiGraphics g,int mx,int my,float partial){
  if(writeButton!=null)writeButton.active=menu.hasMissile();
  super.render(g,mx,my,partial);
  renderTooltip(g,mx,my);
  if(editorMode==EditorMode.COMPONENTS&&inside(mx,my,MISSILE_X,MISSILE_Y,16,16))g.renderTooltip(font,Component.literal("Missile"),mx,my);
  if(editorMode==EditorMode.VISUAL)for(int i=0;i<6;i++){int col=i%3,row=i/3;if(inside(mx,my,177+col*42,52+row*34,20,20))g.renderTooltip(font,Component.literal(SpellPresentation.visualName(i)),mx,my);}
 }
 @Override protected void renderLabels(GuiGraphics g,int mx,int my){}
}
