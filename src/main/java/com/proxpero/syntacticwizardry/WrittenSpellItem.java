package com.proxpero.syntacticwizardry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
public final class WrittenSpellItem extends Item {
 public WrittenSpellItem(Properties properties){super(properties);}
 public static ItemStack create(int[] plan){ItemStack stack=new ItemStack(SyntacticWizardry.WRITTEN_SPELL.get());SpellPresentation.writePlan(stack,plan);return stack;}
 @Override public InteractionResultHolder<ItemStack> use(Level level,Player player,InteractionHand hand){
  ItemStack stack=player.getItemInHand(hand);
  if(!level.isClientSide&&level instanceof ServerLevel server){
   Vec3 origin=new Vec3(player.getX(),player.getEyeY()-0.1,player.getZ());
   SpellExecutor.castRoot(server,player,SpellPresentation.readPlan(stack),origin,player.getLookAngle());
  }
  return InteractionResultHolder.sidedSuccess(stack,level.isClientSide());
 }
}
