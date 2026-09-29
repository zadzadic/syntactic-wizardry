package com.proxpero.syntacticwizardry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.Tags;
public final class SpellProtectionService {
 private static final String KEY_KIND="syntacticwizardry_protection_kind";
 private static final String KEY_POTENCE="syntacticwizardry_protection_potence";
 private static final String KEY_EXPIRES="syntacticwizardry_protection_expires";
 private static final ThreadLocal<Integer> SPELL_DAMAGE_KIND=new ThreadLocal<>();
 public static final int BASE_DURATION_TICKS=30*20;
 private SpellProtectionService(){}
 public static void apply(ServerLevel level,LivingEntity target,int protectionKind,int potence,int durationExtensionTicks){
  if(level==null||target==null||!target.isAlive())return;
  CompoundTag data=target.getPersistentData();
  data.putInt(KEY_KIND,Mth.clamp(protectionKind,0,SpellPresentation.PROTECTION_KIND_COUNT-1));
  data.putInt(KEY_POTENCE,Mth.clamp(potence,SpellPresentation.POTENCE_MIN,SpellPresentation.POTENCE_MAX));
  data.putLong(KEY_EXPIRES,level.getGameTime()+BASE_DURATION_TICKS+Math.max(0,durationExtensionTicks));
 }
 public static void hurtSpell(ServerLevel level,LivingEntity target,int damageKind,float amount){
  if(level==null||target==null||amount<=0.0F)return;
  Integer previous=SPELL_DAMAGE_KIND.get();
  SPELL_DAMAGE_KIND.set(Mth.clamp(damageKind,0,SpellPresentation.DAMAGE_KIND_COUNT-1));
  try{target.hurt(level.damageSources().generic(),amount);}finally{
   if(previous==null)SPELL_DAMAGE_KIND.remove();else SPELL_DAMAGE_KIND.set(previous);
  }
 }
 public static float reduceIncomingDamage(ServerLevel level,LivingEntity target,DamageSource source,float amount){
  if(level==null||target==null||amount<=0.0F)return Math.max(0.0F,amount);
  CompoundTag data=target.getPersistentData();
  if(!data.contains(KEY_EXPIRES))return amount;
  long expires=data.getLong(KEY_EXPIRES);
  if(level.getGameTime()>=expires){clear(data);return amount;}
  int protectionKind=Mth.clamp(data.getInt(KEY_KIND),0,SpellPresentation.PROTECTION_KIND_COUNT-1);
  int protectedDamageKind=SpellPresentation.protectionDamageKind(protectionKind);
  if(protectedDamageKind>=0){
   int incomingKind=classifyIncoming(source);
   if(incomingKind!=protectedDamageKind)return amount;
  }
  int potence=Mth.clamp(data.getInt(KEY_POTENCE),SpellPresentation.POTENCE_MIN,SpellPresentation.POTENCE_MAX);
  float perPoint=protectionKind==SpellPresentation.PROTECTION_ALL?0.10F:0.20F;
  float reduction=Mth.clamp(perPoint*potence,0.0F,1.0F);
  return Math.max(0.0F,amount*(1.0F-reduction));
 }
 private static int classifyIncoming(DamageSource source){
  Integer spellKind=SPELL_DAMAGE_KIND.get();
  if(spellKind!=null)return spellKind;
  if(source==null)return -1;
  if(source.is(DamageTypeTags.IS_FIRE))return SpellPresentation.DAMAGE_FIRE;
  if(source.is(DamageTypeTags.IS_FREEZING))return SpellPresentation.DAMAGE_FROST;
  if(source.is(DamageTypeTags.IS_EXPLOSION)||source.is(DamageTypeTags.IS_LIGHTNING)||source.is(DamageTypes.SONIC_BOOM))return SpellPresentation.DAMAGE_FORCE;
  if(source.is(Tags.DamageTypes.IS_WITHER)||source.is(Tags.DamageTypes.IS_POISON)||source.is(DamageTypes.STARVE))return SpellPresentation.DAMAGE_ENTROPIC;
  if(source.is(Tags.DamageTypes.IS_MAGIC))return SpellPresentation.DAMAGE_ARCANE;
  if(source.is(Tags.DamageTypes.IS_PHYSICAL)||source.is(DamageTypeTags.IS_FALL))return SpellPresentation.DAMAGE_PHYSICAL;
  return -1;
 }
 private static void clear(CompoundTag data){data.remove(KEY_KIND);data.remove(KEY_POTENCE);data.remove(KEY_EXPIRES);}
}
