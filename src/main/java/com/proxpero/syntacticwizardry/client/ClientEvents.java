package com.proxpero.syntacticwizardry.client;
import com.proxpero.syntacticwizardry.SyntacticWizardry;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
@EventBusSubscriber(modid=SyntacticWizardry.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class ClientEvents {
 @SubscribeEvent public static void registerScreens(RegisterMenuScreensEvent e){e.register(SyntacticWizardry.SCRIBES_LECTERN_MENU.get(),ScribesLecternScreen::new);}
}
