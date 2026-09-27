package com.proxpero.syntacticwizardry.client;
import com.proxpero.syntacticwizardry.ScribesLecternMenu;
import com.proxpero.syntacticwizardry.SyntacticWizardry;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
public final class ScribesLecternScreen extends AbstractContainerScreen<ScribesLecternMenu> {
 private static final int MISSILE_X=175;
 private static final int MISSILE_Y=43;
 private boolean missileSelected;
 public ScribesLecternScreen(ScribesLecternMenu menu,Inventory inv,Component title){super(menu,inv,title);imageWidth=ScribesLecternMenu.WIDTH;imageHeight=ScribesLecternMenu.HEIGHT;}
 @Override protected void init(){super.init(); addRenderableWidget(Button.builder(Component.literal("Write Spell"),b->{}).bounds(leftPos+20,topPos+166,120,20).build());}
 @Override protected void renderBg(GuiGraphics g,float partial,int mx,int my){
  int x=leftPos,y=topPos;
  g.fill(x,y,x+320,y+232,0xE0101826);
  panel(g,x+10,y+10,145,184,"Spell Workspace");
  panel(g,x+165,y+10,145,116,"Component Selector");
  panel(g,x+165,y+136,145,58,"Spell Preview");
  g.fill(x+75,y+199,x+247,y+227,0xC0182233);
  g.fill(x+19,y+24,x+37,y+42,0xFF335A88);
  g.drawString(font,"Shapes",x+173,y+29,0xCFE5FF,false);
  int sx=x+MISSILE_X,sy=y+MISSILE_Y;
  g.fill(sx-1,sy-1,sx+17,sy+17,missileSelected?0xFF5B86B8:0xFF2A3B55);
  ItemStack missile=SyntacticWizardry.MISSILE_SHAPE.get().getDefaultInstance();
  g.renderItem(missile,sx,sy);
  if(missileSelected){
   g.fill(x+19,y+51,x+145,y+71,0x802A3B55);
   g.renderItem(missile,x+21,y+53);
   g.drawString(font,"Missile",x+42,y+57,0xE8F3FF,false);
  }
 }
 private void panel(GuiGraphics g,int x,int y,int w,int h,String name){g.fill(x,y,x+w,y+h,0xC0202D42);g.renderOutline(x,y,w,h,0xFF5B86B8);g.drawString(font,name,x+7,y+6,0xE8F3FF,false);}
 private boolean overMissile(double mx,double my){int sx=leftPos+MISSILE_X,sy=topPos+MISSILE_Y;return mx>=sx&&mx<sx+16&&my>=sy&&my<sy+16;}
 @Override public boolean mouseClicked(double mx,double my,int button){if(button==0&&overMissile(mx,my)){missileSelected=true;return true;}return super.mouseClicked(mx,my,button);}
 @Override public void render(GuiGraphics g,int mx,int my,float partial){super.render(g,mx,my,partial);renderTooltip(g,mx,my);if(overMissile(mx,my))g.renderTooltip(font,Component.literal("Missile"),mx,my);}
 @Override protected void renderLabels(GuiGraphics g,int mx,int my){}
}
