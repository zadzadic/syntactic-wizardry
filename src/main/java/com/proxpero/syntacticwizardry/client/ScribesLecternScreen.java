package com.proxpero.syntacticwizardry.client;
import com.proxpero.syntacticwizardry.ScribesLecternMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
public final class ScribesLecternScreen extends AbstractContainerScreen<ScribesLecternMenu> {
 public ScribesLecternScreen(ScribesLecternMenu menu,Inventory inv,Component title){super(menu,inv,title);imageWidth=ScribesLecternMenu.WIDTH;imageHeight=ScribesLecternMenu.HEIGHT;}
 @Override protected void init(){super.init(); addRenderableWidget(Button.builder(Component.literal("Write Spell"),b->{}).bounds(leftPos+20,topPos+112,120,20).build());}
 @Override protected void renderBg(GuiGraphics g,float partial,int mx,int my){
  int x=leftPos,y=topPos;
  g.fill(x,y,x+320,y+232,0xE0101826);
  panel(g,x+10,y+10,145,130,"Spell Workspace");
  panel(g,x+165,y+10,145,80,"Component Selector");
  panel(g,x+165,y+100,145,40,"Spell Preview");
  g.fill(x+10,y+145,x+310,y+227,0xC0182233);
  g.drawString(font,"Inventory",x+16,y+148,0xDDEBFF,false);
  g.fill(x+19,y+24,x+37,y+42,0xFF335A88);
 }
 private void panel(GuiGraphics g,int x,int y,int w,int h,String name){g.fill(x,y,x+w,y+h,0xC0202D42);g.renderOutline(x,y,w,h,0xFF5B86B8);g.drawString(font,name,x+7,y+6,0xE8F3FF,false);}
 @Override public void render(GuiGraphics g,int mx,int my,float partial){super.render(g,mx,my,partial);renderTooltip(g,mx,my);}
 @Override protected void renderLabels(GuiGraphics g,int mx,int my){}
}
