package com.proxpero.syntacticwizardry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import java.util.List;
public final class WrittenSpellItem extends Item {
 public static final int CAST_COOLDOWN_TICKS=20;
 public static final int SUSTAINED_MANA_INTERVAL_TICKS=10;
 public WrittenSpellItem(Properties properties){super(properties);}
 public static ItemStack create(int[] plan,int[] settings){return create(plan,settings,SpellManaCost.DEFAULT_DISCOUNT,"");}
 public static ItemStack create(int[] plan,int[] settings,String name){return create(plan,settings,SpellManaCost.DEFAULT_DISCOUNT,name);}
 public static ItemStack create(int[] plan,int[] settings,float discount){return create(plan,settings,discount,"");}
 public static ItemStack create(int[] plan,int[] settings,float discount,String name){
  ItemStack stack=SpellPresentation.stack(SyntacticWizardry.WRITTEN_SPELL.get());
  SpellPresentation.writePlan(stack,plan,settings);
  SpellPresentation.writeManaCosts(stack,SpellManaCost.calculate(plan,settings,discount));
  String cleaned=sanitizeName(name);
  if(!cleaned.isEmpty())stack.set(DataComponents.CUSTOM_NAME,Component.literal(cleaned));
  return stack;
 }

 @Override public void appendHoverText(ItemStack stack,TooltipContext context,List<Component> tooltipComponents,TooltipFlag tooltipFlag){
  super.appendHoverText(stack,context,tooltipComponents,tooltipFlag);
  String chain=describePlan(SpellPresentation.readPlan(stack));
  if(!chain.isEmpty())tooltipComponents.add(Component.literal(chain));
  tooltipComponents.add(Component.literal("Cost: "+formatCost(SpellPresentation.readSpellCost(stack))));
  float sustained=SpellPresentation.readSustainedCost(stack);
  if(sustained>0.0F)tooltipComponents.add(Component.literal("Sustain: "+formatCost(sustained)+" / 0.5s"));
 }
 private static String describePlan(int[] plan){
  StringBuilder result=new StringBuilder();
  for(int row=0;row<SpellPresentation.ROWS;row++){
   StringBuilder rowText=new StringBuilder();
   for(int col=0;col<SpellPresentation.COLS;col++){
    int type=SpellPresentation.typeAt(plan,row*SpellPresentation.COLS+col);
    if(type==SpellPresentation.TYPE_EMPTY)continue;
    if(rowText.length()>0)rowText.append(" + ");
    rowText.append(SpellPresentation.componentName(type));
   }
   if(rowText.length()==0)continue;
   if(result.length()>0)result.append(" -> ");
   result.append(rowText);
  }
  return result.toString();
 }
 private static String formatCost(float value){
  int rounded=Math.round(value);
  return Math.abs(value-rounded)<0.001F?Integer.toString(rounded):Float.toString(value);
 }
 private static String sanitizeName(String name){
  if(name==null)return "";
  String stripped=name.strip();
  StringBuilder result=new StringBuilder();
  for(int i=0;i<stripped.length()&&result.length()<ScribesLecternMenu.MAX_SPELL_NAME_LENGTH;i++){
   char value=stripped.charAt(i);
   if(!Character.isISOControl(value))result.append(value);
  }
  return result.toString();
 }
 @Override public InteractionResultHolder<ItemStack> use(Level level,Player player,InteractionHand hand){
  ItemStack stack=player.getItemInHand(hand);
  SpellPresentation.ensureManaCosts(stack);
  int[] plan=SpellPresentation.readPlan(stack);
  int[] settings=SpellPresentation.readSettings(stack);
  boolean sustained=SpellComponents.requiresHeldUse(plan);
  if(player.getCooldowns().isOnCooldown(this))return InteractionResultHolder.fail(stack);
  if(sustained)player.startUsingItem(hand);
  if(!level.isClientSide&&level instanceof ServerLevel server){
   float rawSpellCost=SpellPresentation.readSpellCost(stack);
   float spellCost=RobeArmorSupport.discountedManaCost(player,rawSpellCost);
   if(!player.isCreative()&&!ManaService.tryConsume(player,spellCost,rawSpellCost)){
    if(sustained)player.stopUsingItem();
    player.displayClientMessage(Component.literal("Not enough Mana. Need "+formatCost(spellCost)+", have "+formatCost(ManaService.getMana(player))+"."),true);
    return InteractionResultHolder.fail(stack);
   }
   player.getCooldowns().addCooldown(this,CAST_COOLDOWN_TICKS);
   ChannelRuntime.begin(player);
   StreamRuntime.begin(player);
   Vec3 origin=new Vec3(player.getX(),player.getEyeY()-0.1,player.getZ());
   SpellExecutor.castRoot(server,player,plan,settings,origin,player.getLookAngle(),yawFacing(player.getYRot()));
  }
  return sustained?InteractionResultHolder.consume(stack):InteractionResultHolder.sidedSuccess(stack,level.isClientSide());
 }
 @Override public int getUseDuration(ItemStack stack,LivingEntity entity){return SpellComponents.requiresHeldUse(SpellPresentation.readPlan(stack))?72000:0;}
 @Override public UseAnim getUseAnimation(ItemStack stack){return UseAnim.NONE;}
 @Override public void onUseTick(Level level,LivingEntity entity,ItemStack stack,int remainingUseDuration){
  if(level.isClientSide)return;
  SpellPresentation.ensureManaCosts(stack);
  int elapsed=getUseDuration(stack,entity)-remainingUseDuration;
  if(elapsed>0&&elapsed%SUSTAINED_MANA_INTERVAL_TICKS==0&&entity instanceof Player player&&!player.isCreative()){
   float rawSustainedCost=SpellPresentation.readSustainedCost(stack);
   float sustainedCost=RobeArmorSupport.discountedManaCost(player,rawSustainedCost);
   if(sustainedCost>0.0F&&!ManaService.tryConsume(player,sustainedCost,rawSustainedCost)){
    entity.stopUsingItem();
    ChannelRuntime.clear(entity);
    StreamRuntime.clear(entity);
    return;
   }
  }
  ChannelRuntime.tick(entity);
  StreamRuntime.tick(entity);
 }
 @Override public void releaseUsing(ItemStack stack,Level level,LivingEntity entity,int timeLeft){if(!level.isClientSide){ChannelRuntime.clear(entity);StreamRuntime.clear(entity);}}
 private static Vec3 yawFacing(float yawDegrees){
  double radians=Math.toRadians(yawDegrees);
  return new Vec3(-Math.sin(radians),0.0,Math.cos(radians));
 }
}
