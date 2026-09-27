package com.proxpero.syntacticwizardry;
public enum SpellPropertyKey {
 STYLE(0),
 VISUAL(1),
 RADIUS(2),
 DAMAGE_KIND(3),
 POTENCE(4);
 private final int id;
 SpellPropertyKey(int id){this.id=id;}
 public int id(){return id;}
 public static SpellPropertyKey byId(int id){
  for(SpellPropertyKey key:values())if(key.id==id)return key;
  return null;
 }
}
