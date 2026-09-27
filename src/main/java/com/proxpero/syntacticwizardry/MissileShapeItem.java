package com.proxpero.syntacticwizardry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
public final class MissileShapeItem extends Item {
 public MissileShapeItem(Properties properties){super(properties);}
 @Override public InteractionResultHolder<ItemStack> use(Level level,Player player,InteractionHand hand){
  ItemStack stack=player.getItemInHand(hand);
  if(!level.isClientSide&&level instanceof ServerLevel server){
   int[] plan=SpellPresentation.emptyPlan();
   int[] radii=SpellPresentation.emptyRadii();
   int[] damageKinds=SpellPresentation.emptyDamageKinds();
   int[] potences=SpellPresentation.emptyPotences();
   SpellPresentation.setCell(plan,0,SpellPresentation.TYPE_MISSILE,SpellPresentation.STYLE_DEFAULT,SpellPresentation.VISUAL_DEFAULT);
   SpellExecutor.castRoot(server,player,plan,radii,damageKinds,potences,new Vec3(player.getX(),player.getEyeY()-0.1,player.getZ()),player.getLookAngle());
  }
  return InteractionResultHolder.sidedSuccess(stack,level.isClientSide());
 }
}
