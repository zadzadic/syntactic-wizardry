package com.proxpero.syntacticwizardry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
@Mod(SyntacticWizardry.MOD_ID)
public final class SyntacticWizardry {
 public static final String MOD_ID="syntacticwizardry";
 private static final DeferredRegister<CreativeModeTab> CREATIVE_TABS=DeferredRegister.create(Registries.CREATIVE_MODE_TAB,MOD_ID);
 public static final DeferredHolder<CreativeModeTab,CreativeModeTab> SYNTACTIC_WIZARDRY_TAB=CREATIVE_TABS.register("syntactic_wizardry",()->CreativeModeTab.builder().title(Component.translatable("itemGroup.syntacticwizardry")).icon(()->Items.AMETHYST_SHARD.getDefaultInstance()).displayItems((parameters,output)->{}).build());
 public SyntacticWizardry(IEventBus modEventBus){CREATIVE_TABS.register(modEventBus);}
}
