package com.proxpero.syntacticwizardry.client;

import com.proxpero.syntacticwizardry.CarvingStationMenu;
import com.proxpero.syntacticwizardry.RunestoneItem;
import com.proxpero.syntacticwizardry.SpellComponentDefinition;
import com.proxpero.syntacticwizardry.SpellComponents;
import com.proxpero.syntacticwizardry.SpellPropertyDefinition;
import com.proxpero.syntacticwizardry.SpellPropertyKey;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import java.util.List;

public final class CarvingStationScreen extends AbstractContainerScreen<CarvingStationMenu> {
    private static final int GRID_X=104,GRID_Y=48,CELL=16,STEP=18;
    private int dragType=0;
    private int dragSourceCell=-1;
    private boolean effects=false;
    private int selectedCell=-1;
    private Button carveButton,formButton;

    public CarvingStationScreen(CarvingStationMenu menu,Inventory inv,Component title){
        super(menu,inv,title);imageWidth=CarvingStationMenu.WIDTH;imageHeight=CarvingStationMenu.HEIGHT;
    }

    @Override protected void init(){
        super.init();
        carveButton=addRenderableWidget(Button.builder(Component.literal("Carve"),b->send(CarvingStationMenu.ACTION_CARVE)).bounds(leftPos+104,topPos+174,90,16).build());
        formButton=addRenderableWidget(Button.builder(Component.literal("Form Runestone"),b->send(CarvingStationMenu.ACTION_FORM)).bounds(leftPos+18,topPos+84,90,16).build());
    }

    private void send(int id){if(minecraft!=null&&minecraft.gameMode!=null)minecraft.gameMode.handleInventoryButtonClick(menu.containerId,id);}
    private List<SpellComponentDefinition> choices(){return effects?SpellComponents.effects():SpellComponents.shapes();}

    @Override protected void renderBg(GuiGraphics g,float partial,int mx,int my){
        int x=leftPos,y=topPos;
        g.fill(x,y,x+320,y+232,0xE0101826);
        panel(g,x+10,y+10,84,184,"Runestone");
        separator(g,x+96,y+10,y+194);
        panel(g,x+100,y+10,110,184,"Carving Grid");
        separator(g,x+212,y+10,y+194);
        panel(g,x+216,y+10,94,184,selectedCell>=0?"Properties":"Components");
        g.drawString(font,"Ingredients",x+18,y+49,0xFFE8F2FF,false);
        g.drawString(font,"Any order",x+18,y+91,0xFF9EB7CF,false);
        slotFrame(g,x+19,y+24);
        slotFrame(g,x+19,y+60);
        slotFrame(g,x+43,y+60);
        slotFrame(g,x+67,y+60);
        for(int c=0;c<9;c++)slotFrame(g,x+79+c*18,y+203);
        int slots=menu.slots();
        if(slots>0){
            g.drawString(font,"Slots: "+slots,x+18,y+110,0xFFE8F2FF,false);
            g.drawString(font,menu.carved()?"LOCKED":"Uncarved",x+18,y+124,menu.carved()?0xFFFF7777:0xFF9FE6A0,false);
            int totalWidth=slots*STEP-2;
            int startX=x+154-totalWidth/2;
            int sy=y+GRID_Y+STEP;
            for(int cell=0;cell<slots;cell++){
                int sx=startX+cell*STEP;
                gridCell(g,sx,sy);
                if(cell==selectedCell)g.renderOutline(sx-1,sy-1,CELL+2,CELL+2,0xFFB8E8FF);
                int type=menu.typeAt(cell);
                if(type!=0){
                    SpellComponentDefinition d=SpellComponents.byType(type);
                    if(d!=null)g.renderItem(d.createEditorIcon(),sx,sy);
                }
            }
        }else g.drawCenteredString(font,"Insert Runestone",x+154,y+92,0xFF9EB7CF);
        if(selectedCell>=0&&selectedCell<slots&&menu.typeAt(selectedCell)!=0){
            renderProperties(g,x,y);
        }else{
            if(selectedCell>=0)selectedCell=-1;
            tab(g,x+220,y+30,40,"Shapes",!effects);
            tab(g,x+264,y+30,40,"Effects",effects);
            List<SpellComponentDefinition> list=choices();
            for(int i=0;i<list.size()&&i<20;i++){
                int col=i%4,row=i/4,sx=x+220+col*21,sy=y+52+row*21;
                g.fill(sx,sy,sx+16,sy+16,0xFF2A3B55);g.renderItem(list.get(i).createEditorIcon(),sx,sy);
            }
        }
        carveButton.active=slots>0&&!menu.carved();
        formButton.active=menu.runestone().isEmpty();
    }

    private void renderProperties(GuiGraphics g,int x,int y){
        SpellComponentDefinition definition=menu.definitionAt(selectedCell);
        if(definition==null)return;
        g.fill(x+220,y+30,x+304,y+46,0xB02A3B55);
        g.drawCenteredString(font,"< Back",x+262,y+34,0xFFE8F2FF);
        g.drawCenteredString(font,definition.displayName(),x+262,y+51,0xFFB8D7F0);
        if(definition.settings().isEmpty()){
            g.drawCenteredString(font,"No properties",x+262,y+76,0xFF9EB7CF);
            return;
        }
        int rowY=y+66;
        for(SpellPropertyDefinition property:definition.settings()){
            int value=menu.propertyValue(selectedCell,property.key());
            g.drawString(font,property.label(),x+220,rowY,0xFFD9E9F7,false);
            buttonBox(g,x+220,rowY+10,14,14,"<");
            g.drawCenteredString(font,property.format(value),x+262,rowY+13,0xFFE8F3FF);
            buttonBox(g,x+290,rowY+10,14,14,">");
            rowY+=27;
        }
    }

    private void buttonBox(GuiGraphics g,int x,int y,int w,int h,String text){
        g.fill(x,y,x+w,y+h,0xFF30445E);
        g.renderOutline(x,y,w,h,0xFF5B86B8);
        g.drawCenteredString(font,text,x+w/2,y+3,0xFFE8F3FF);
    }

    private void panel(GuiGraphics g,int x,int y,int w,int h,String title){
        g.fill(x,y,x+w,y+h,0xB01A2637);
        g.fill(x,y,x+w,y+1,0xFF6B8198);
        g.fill(x,y+h-1,x+w,y+h,0xFF6B8198);
        g.fill(x,y,x+1,y+h,0xFF6B8198);
        g.fill(x+w-1,y,x+w,y+h,0xFF6B8198);
        g.drawString(font,title,x+6,y+6,0xFFE8F2FF,false);
    }
    private void separator(GuiGraphics g,int x,int y1,int y2){
        g.fill(x,y1,x+1,y2,0xFF7E94AA);
        g.fill(x+1,y1,x+2,y2,0x6030445E);
    }
    private void slotFrame(GuiGraphics g,int x,int y){
        g.fill(x,y,x+18,y+18,0xFF6B8198);
        g.fill(x+1,y+1,x+17,y+17,0xFF111A26);
        g.fill(x+2,y+2,x+16,y+16,0xFF27374A);
    }
    private void gridCell(GuiGraphics g,int x,int y){
        g.fill(x,y,x+CELL,y+CELL,0xFF6B8198);
        g.fill(x+1,y+1,x+CELL-1,y+CELL-1,0x7030445E);
    }
    private void tab(GuiGraphics g,int x,int y,int w,String text,boolean active){g.fill(x,y,x+w,y+16,active?0xFF36587A:0xB02A3B55);g.drawCenteredString(font,text,x+w/2,y+4,0xFFE8F2FF);}

    private int gridCellAt(double mx,double my){
        int slots=menu.slots();
        if(slots<=0)return -1;
        int x=leftPos,y=topPos;
        int totalWidth=slots*STEP-2;
        int startX=x+154-totalWidth/2;
        int sy=y+GRID_Y+STEP;
        for(int cell=0;cell<slots;cell++){
            int sx=startX+cell*STEP;
            if(mx>=sx&&mx<sx+CELL&&my>=sy&&my<sy+CELL)return cell;
        }
        return -1;
    }

    private void clearDrag(){
        dragType=0;
        dragSourceCell=-1;
    }

    @Override public boolean mouseClicked(double mx,double my,int button){
        int x=leftPos,y=topPos;

        if(button==1){
            int cell=gridCellAt(mx,my);
            if(cell>=0&&!menu.carved()){
                send(CarvingStationMenu.actionClear(cell));
                if(selectedCell==cell)selectedCell=-1;
                return true;
            }
        }

        if(button==0){
            if(selectedCell>=0){
                if(mx>=x+220&&mx<x+304&&my>=y+30&&my<y+46){
                    selectedCell=-1;clearDrag();return true;
                }
                SpellComponentDefinition definition=menu.definitionAt(selectedCell);
                if(definition!=null&&!menu.carved()){
                    int rowY=y+66;
                    for(SpellPropertyDefinition property:definition.settings()){
                        int current=menu.propertyValue(selectedCell,property.key());
                        if(mx>=x+220&&mx<x+234&&my>=rowY+10&&my<rowY+24){
                            send(CarvingStationMenu.actionSetProperty(selectedCell,property.key(),Math.max(property.minValue(),current-1)));
                            return true;
                        }
                        if(mx>=x+290&&mx<x+304&&my>=rowY+10&&my<rowY+24){
                            send(CarvingStationMenu.actionSetProperty(selectedCell,property.key(),Math.min(property.maxValue(),current+1)));
                            return true;
                        }
                        rowY+=27;
                    }
                }
            }else{
                if(my>=y+30&&my<y+46){
                    if(mx>=x+220&&mx<x+260){effects=false;clearDrag();return true;}
                    if(mx>=x+264&&mx<x+304){effects=true;clearDrag();return true;}
                }

                List<SpellComponentDefinition> list=choices();
                for(int i=0;i<list.size()&&i<20;i++){
                    int col=i%4,row=i/4,sx=x+220+col*21,sy=y+52+row*21;
                    if(mx>=sx&&mx<sx+16&&my>=sy&&my<sy+16){
                        dragType=list.get(i).typeId();
                        dragSourceCell=-1;
                        return true;
                    }
                }
            }

            int cell=gridCellAt(mx,my);
            if(cell>=0&&!menu.carved()){
                int type=menu.typeAt(cell);
                if(type!=0){
                    selectedCell=cell;
                    dragType=type;
                    dragSourceCell=cell;
                    return true;
                }
            }
        }

        return super.mouseClicked(mx,my,button);
    }

    @Override public boolean mouseReleased(double mx,double my,int button){
        if(button==0&&dragType!=0){
            int target=gridCellAt(mx,my);
            if(target>=0&&!menu.carved()){
                if(dragSourceCell>=0){
                    if(target!=dragSourceCell)send(CarvingStationMenu.actionMove(dragSourceCell,target));
                }else{
                    send(CarvingStationMenu.actionAdd(target,dragType));
                }
                selectedCell=target;
            }
            clearDrag();
            return true;
        }
        return super.mouseReleased(mx,my,button);
    }

    @Override public void render(GuiGraphics g,int mx,int my,float partialTick){
        super.render(g,mx,my,partialTick);
        if(dragType!=0){
            SpellComponentDefinition definition=SpellComponents.byType(dragType);
            if(definition!=null)g.renderItem(definition.createEditorIcon(),mx-8,my-8);
        }
        renderTooltip(g,mx,my);
    }

    @Override protected void renderLabels(GuiGraphics g,int mx,int my){}
}
