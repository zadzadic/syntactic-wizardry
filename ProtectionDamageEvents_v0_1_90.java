package com.proxpero.syntacticwizardry;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
@EventBusSubscriber(modid=SyntacticWizardry.MOD_ID,bus=EventBusSubscriber.Bus.GAME)
public final class ProtectionDamageEvents {
 private ProtectionDamageEvents(){}
 @SubscribeEvent public static void onDamage(LivingDamageEvent.Pre event){
  if(!(event.getEntity().level() instanceof ServerLevel level))return;
  float reduced=SpellProtectionService.reduceIncomingDamage(level,event.getEntity(),event.getSource(),event.getNewDamage());
  event.setNewDamage(reduced);
 }
}
