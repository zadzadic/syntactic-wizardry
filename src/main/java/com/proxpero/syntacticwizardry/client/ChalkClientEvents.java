package com.proxpero.syntacticwizardry.client;

import com.proxpero.syntacticwizardry.ChalkItem;
import com.proxpero.syntacticwizardry.ChalkRegistry;
import com.proxpero.syntacticwizardry.ChalkRuneBlock;
import com.proxpero.syntacticwizardry.SyntacticWizardry;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

@EventBusSubscriber(modid=SyntacticWizardry.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class ChalkClientEvents {
    private ChalkClientEvents(){}

    @SubscribeEvent
    public static void registerBlockColors(RegisterColorHandlersEvent.Block event){
        if(ChalkRegistry.block()==null) return;
        event.register((state,level,pos,tintIndex)->tintIndex==0
                ? state.getValue(ChalkRuneBlock.COLOR).getTextureDiffuseColor()
                : 0xFFFFFF,ChalkRegistry.block());
    }

    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event){
        Item[] items=ChalkRegistry.items();
        if(items.length==0) return;
        event.register((stack,tintIndex)->{
            if(tintIndex!=0 || !(stack.getItem() instanceof ChalkItem chalk)) return 0xFFFFFF;
            return chalk.color().getTextureDiffuseColor();
        },items);
    }
}
