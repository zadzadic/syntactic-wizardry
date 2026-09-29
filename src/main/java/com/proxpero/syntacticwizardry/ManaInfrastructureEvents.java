package com.proxpero.syntacticwizardry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

@EventBusSubscriber(modid=SyntacticWizardry.MOD_ID,bus=EventBusSubscriber.Bus.GAME)
public final class ManaInfrastructureEvents {
    private ManaInfrastructureEvents(){}
    @SubscribeEvent public static void onRightClick(PlayerInteractEvent.RightClickBlock event){
        if(LegacyBuilderBridge.invokeBoolean("com.arcane.magic.builder.BuilderLinkSupport", "onRightClick", event))return;
        Level level=event.getLevel();BlockPos pos=event.getPos();BlockState state=level.getBlockState(pos);ItemStack held=event.getItemStack();
        if(held.isEmpty()&&level instanceof ServerLevel server){
            BlockPos center=PowerCoreSupport.centerFromPart(level,pos,state);
            if(center!=null&&PowerCoreSupport.isValid(server,center)){
                int output=ManaGridSupport.coreOutput(server,center),capacity=ManaGridSupport.coreStorageCapacity(server,center),stored=(int)Math.floor(ManaGridSupport.coreStored(server,center)+1.0E-4D);
                event.getEntity().displayClientMessage(Component.literal("Power Core: "+output+" Mana/s, "+stored+"/"+capacity+" stored."),false);cancel(event,level);
            }
            return;
        }
        if(!held.is(SyntacticWizardry.WAND.get()))return;
        boolean relevant=PylonSupport.isRawStructure(level,pos)||PowerCoreSupport.isRawStructure(level,pos)||PylonSupport.isPylonPart(state)||PowerCoreSupport.isCorePart(state);
        if(!relevant)return;
        cancel(event,level);
        if(!(level instanceof ServerLevel server)||!(event.getEntity() instanceof ServerPlayer player))return;
        if(PowerCoreSupport.isRawStructure(level,pos)){
            PowerCoreSupport.form(server,pos);BlockPos center=pos.below();WandBindingService.bindCore(held,server.dimension().location(),center);player.getInventory().setChanged();message(player,"Power Core formed. Wand attuned to Power Core.");return;
        }
        if(PylonSupport.isRawStructure(level,pos)){
            PylonSupport.form(server,pos);PylonNetworkData network=PylonNetworkData.get(server);network.registerPylon(server,pos);
            if(network.isActivePylon(server,pos)){
                HighManaClaimSavedData data=HighManaCompassService.data(player);data.claim(player.getUUID(),new HighManaClaimSavedData.Claim(server.dimension().location(),pos.getX()>>4,pos.getZ()>>4));HighManaCompassService.syncTarget(player);message(player,"Pylon formed and High Mana Zone tapped.");
            }else if(HighManaZones.isNaturallyHighMana(server,pos.getX()>>4,pos.getZ()>>4))message(player,"Pylon formed. Another Pylon is already active in this High Mana Zone.");
            else message(player,"Pylon formed, but this chunk is not a High Mana Zone.");
            return;
        }
        BlockPos core=PowerCoreSupport.centerFromPart(level,pos,state);
        if(core!=null&&PowerCoreSupport.isValid(server,core)){
            WandBindingService.bindCore(held,server.dimension().location(),core);player.getInventory().setChanged();message(player,"Wand attuned to Power Core.");return;
        }
        BlockPos pylon=PylonSupport.middleFromPart(pos,state);
        if(pylon!=null&&PylonSupport.isValid(server,pylon)){
            WandBindingService.Binding binding=WandBindingService.get(held);
            if(binding==null||!"core".equals(binding.type())){message(player,"Attune the wand to a Power Core first.");return;}
            if(!server.dimension().location().equals(binding.dimension())){message(player,"The attuned Power Core is in another dimension.");return;}
            if(!PowerCoreSupport.isValid(server,binding.position())){WandBindingService.clear(held);player.getInventory().setChanged();message(player,"The attuned Power Core no longer exists.");return;}
            boolean linked=PylonNetworkData.get(server).link(server,pylon,binding.position());message(player,linked?"Pylon linked to Power Core.":"Pylon is already linked to this Power Core.");
        }
    }
    @SubscribeEvent public static void onBreak(BlockEvent.BreakEvent event){
        if(!(event.getLevel() instanceof ServerLevel level))return;BlockPos pos=event.getPos();BlockState state=event.getState();
        BlockPos pylon=PylonSupport.middleFromPart(pos,state);
        if(pylon!=null){PylonNetworkData.get(level).unregisterPylon(level,pylon);PylonSupport.clear(level,pylon);return;}
        BlockPos core=PowerCoreSupport.centerFromPart(level,pos,state);
        if(core!=null){PylonNetworkData.get(level).unregisterCore(level,core);PowerCoreSupport.clear(level,core);}
    }
    @SubscribeEvent public static void onLevelTick(LevelTickEvent.Post event){
        LegacyBuilderBridge.invoke("com.arcane.magic.builder.BuilderLinkSupport", "tick", event);
        if(!(event.getLevel() instanceof ServerLevel level))return;long time=level.getGameTime();if(time%5L!=0L)return;
        PylonNetworkData network=PylonNetworkData.get(level);
        for(BlockPos pylon:network.activePylons(level))glow(level,pylon,0.65D);
        for(BlockPos core:network.linkedCores(level))glow(level,core.above(),0.55D);
    }
    private static void glow(ServerLevel level,BlockPos pos,double y){level.sendParticles(ParticleTypes.END_ROD,pos.getX()+0.5D,pos.getY()+y,pos.getZ()+0.5D,2,0.15D,0.35D,0.15D,0.01D);}
    private static void message(Player player,String text){player.displayClientMessage(Component.literal(text),true);}
    private static void cancel(PlayerInteractEvent.RightClickBlock event,Level level){event.setCanceled(true);event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide()));}
}
