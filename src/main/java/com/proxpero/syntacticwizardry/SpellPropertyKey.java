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
 TARGET_TYPE(8,6),
 DISTANCE(9,7),
 MOVE_MODE(10,8),
 IMPACT_DIRECTION(11,9),
 SIPHON_RESOURCE(12,10),
 SIPHON_MODE(13,11),
 DURATION_SECONDS(14,12),
 GRAVITY_MODE(15,13),
 SPHERE_MODE(16,14),
 PROTECTION_KIND(17,15),
 ALTER_STRENGTH(18,16),
 ALTER_SPEED(19,17),
 ALTER_TOUGHNESS(20,18),
 TELEPORT_MODE(21,19),
 RANGE_VALUE(22,20),
 SPLIT_PATTERN(23,21);
 public static final int SETTING_COUNT=22;
 private final int id;
 private final int settingIndex;
 SpellPropertyKey(int id,int settingIndex){this.id=id;this.settingIndex=settingIndex;}
 public int id(){return id;}
 public int settingIndex(){return settingIndex;}
 public boolean isSetting(){return settingIndex>=0;}
 public static SpellPropertyKey byId(int id){for(SpellPropertyKey key:values())if(key.id==id)return key;return null;}
}
