package com.proxpero.syntacticwizardry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
public final class SpellProtectionService {
 private static final String KEY_KIND="syntacticwizardry_protection_kind";
 private static final String KEY_POTENCE="syntacticwizardry_protection_potence";
 private static final String KEY_EXPIRES="syntacticwizardry_protection_expires";
 public static final int BASE_DURATION_TICKS=30*20;
 private SpellProtectionService(){}
 public static void apply(ServerLevel level,LivingEntity target,int protectionKind,int potence,int durationExtensionTicks){
  if(level==null||target==null||!target.isAlive())return;
  CompoundTag data=target.getPersistentData();
  data.putInt(KEY_KIND,Mth.clamp(protectionKind,0,SpellPresentation.PROTECTION_KIND_COUNT-1));
  data.putInt(KEY_POTENCE,Mth.clamp(potence,SpellPresentation.POTENCE_MIN,SpellPresentation.POTENCE_MAX));
  data.putLong(KEY_EXPIRES,level.getGameTime()+BASE_DURATION_TICKS+Math.max(0,durationExtensionTicks));
 }
 public static float reduceDamage(ServerLevel level,LivingEntity target,int damageKind,float amount){
  if(level==null||target==null||amount<=0.0F)return Math.max(0.0F,amount);
  CompoundTag data=target.getPersistentData();
  if(!data.contains(KEY_EXPIRES))return amount;
  long expires=data.getLong(KEY_EXPIRES);
  if(level.getGameTime()>=expires){clear(data);return amount;}
  int kind=Mth.clamp(data.getInt(KEY_KIND),0,SpellPresentation.PROTECTION_KIND_COUNT-1);
  int protectedDamageKind=SpellPresentation.protectionDamageKind(kind);
  if(protectedDamageKind>=0&&protectedDamageKind!=damageKind)return amount;
  int potence=Mth.clamp(data.getInt(KEY_POTENCE),SpellPresentation.POTENCE_MIN,SpellPresentation.POTENCE_MAX);
  float perPoint=kind==SpellPresentation.PROTECTION_ALL?0.10F:0.20F;
  float reduction=Mth.clamp(perPoint*potence,0.0F,1.0F);
  return Math.max(0.0F,amount*(1.0F-reduction));
 }
 private static void clear(CompoundTag data){data.remove(KEY_KIND);data.remove(KEY_POTENCE);data.remove(KEY_EXPIRES);}
}
