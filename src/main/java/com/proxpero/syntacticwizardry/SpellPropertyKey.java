package com.proxpero.syntacticwizardry;
public enum SpellPropertyKey {
 STYLE(0,-1),
 VISUAL(1,-1),
 RADIUS(2,0),
 DAMAGE_KIND(3,1),
 POTENCE(4,2),
 WIDTH(5,3),
 HEIGHT(6,4),
 DEPTH(7,5),
 TARGET_TYPE(8,6);
 public static final int SETTING_COUNT=7;
 private final int id;
 private final int settingIndex;
 SpellPropertyKey(int id,int settingIndex){this.id=id;this.settingIndex=settingIndex;}
 public int id(){return id;}
 public int settingIndex(){return settingIndex;}
 public boolean isSetting(){return settingIndex>=0;}
 public static SpellPropertyKey byId(int id){for(SpellPropertyKey key:values())if(key.id==id)return key;return null;}
}
