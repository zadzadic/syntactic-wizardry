package com.proxpero.syntacticwizardry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
public final class WrittenSpellItem extends Item {
 public WrittenSpellItem(Properties properties){super(properties);}
 public static ItemStack create(int style,int visual){ItemStack stack=new ItemStack(SyntacticWizardry.WRITTEN_SPELL.get());SpellPresentation.writeState(stack,style,visual);return stack;}
 @Override public InteractionResultHolder<ItemStack> use(Level level,Player player,InteractionHand hand){
  ItemStack stack=player.getItemInHand(hand);
  if(!level.isClientSide && level instanceof ServerLevel){
   SpellMissile missile=new SpellMissile(level,player);
   missile.configure(SpellPresentation.readStyle(stack),SpellPresentation.readVisual(stack));
   missile.shootFromRotation(player,player.getXRot(),player.getYRot(),0.0F,1.5F,1.0F);
   level.addFreshEntity(missile);
  }
  return InteractionResultHolder.sidedSuccess(stack,level.isClientSide());
 }
}
