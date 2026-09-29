package com.proxpero.syntacticwizardry.client;
import com.proxpero.syntacticwizardry.SyntacticWizardry;
import com.proxpero.syntacticwizardry.DimensionalStorageMenu;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
@EventBusSubscriber(modid=SyntacticWizardry.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class ClientEvents {
 @SubscribeEvent public static void registerScreens(RegisterMenuScreensEvent e){e.register(SyntacticWizardry.SCRIBES_LECTERN_MENU.get(),ScribesLecternScreen::new);e.register(SyntacticWizardry.DIMENSIONAL_STORAGE_MENU.get(),DimensionalStorageScreen::new);}
 @SubscribeEvent public static void registerRenderers(EntityRenderersEvent.RegisterRenderers e){
  e.registerEntityRenderer(SyntacticWizardry.SPELL_MISSILE.get(),SpellMissileRenderer::new);
  e.registerEntityRenderer(SyntacticWizardry.SPHERE_VISUAL.get(),SphereVisualRenderer::new);
  e.registerEntityRenderer(SyntacticWizardry.BOX_VISUAL.get(),BoxVisualRenderer::new);
  e.registerEntityRenderer(SyntacticWizardry.CONE_VISUAL.get(),ConeVisualRenderer::new);
  e.registerEntityRenderer(SyntacticWizardry.POINT_VISUAL.get(),PointVisualRenderer::new);
 }
}
