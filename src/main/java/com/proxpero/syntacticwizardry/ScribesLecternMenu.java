package com.proxpero.syntacticwizardry;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
public final class ScribesLecternMenu extends AbstractContainerMenu {
 public static final int WIDTH=320,HEIGHT=232;
 private static final int COMPONENT_ACTION_STRIDE=64,PROPERTY_CELL_STRIDE=1024,PROPERTY_KEY_STRIDE=64;
 public static final int ACTION_ADD_COMPONENT_BASE=100,ACTION_MOVE_BASE=10000,ACTION_CLEAR_BASE=20000,ACTION_SET_PROPERTY_BASE=30000,ACTION_WRITE=70000;
 private static final int SETTINGS_DATA_BASE=SpellPresentation.PLAN_DATA_SIZE;
 private final Container paper=new SimpleContainer(1);
 private final ContainerData spellData=new SimpleContainerData(SETTINGS_DATA_BASE+SpellPresentation.SETTINGS_DATA_SIZE);
 public ScribesLecternMenu(int id,Inventory inv){super(SyntacticWizardry.SCRIBES_LECTERN_MENU.get(),id);addSlot(new Slot(paper,0,20,25){@Override public boolean mayPlace(ItemStack s){return s.is(Items.PAPER);}});int sx=80,sy=204;for(int c=0;c<9;c++)addSlot(new Slot(inv,c,sx+c*18,sy));clearAllDefaults();addDataSlots(spellData);}
 public static int actionAddComponent(int cell,int type){return ACTION_ADD_COMPONENT_BASE+cell*COMPONENT_ACTION_STRIDE+type;}
 public static int actionMove(int from,int to){return ACTION_MOVE_BASE+from*SpellPresentation.CELLS+to;}
 public static int actionClear(int cell){return ACTION_CLEAR_BASE+cell;}
 public static int actionSetProperty(int cell,SpellPropertyKey key,int value){return ACTION_SET_PROPERTY_BASE+cell*PROPERTY_CELL_STRIDE+key.id()*PROPERTY_KEY_STRIDE+value;}
 private int off(int cell){return cell*SpellPresentation.STRIDE;}
 private int settingIndex(int cell,SpellPropertyKey key){return SETTINGS_DATA_BASE+cell*SpellPropertyKey.SETTING_COUNT+key.settingIndex();}
 public int typeAt(int cell){return valid(cell)?spellData.get(off(cell)):SpellPresentation.TYPE_EMPTY;}
 public int styleAt(int cell){return valid(cell)?Mth.clamp(spellData.get(off(cell)+1),0,SpellPresentation.STYLE_COUNT-1):SpellPresentation.STYLE_DEFAULT;}
 public int visualAt(int cell){return valid(cell)?Mth.clamp(spellData.get(off(cell)+2),0,SpellPresentation.VISUAL_COUNT-1):SpellPresentation.VISUAL_DEFAULT;}
 public int propertyValue(int cell,SpellPropertyKey key){if(!valid(cell))return key.isSetting()?SpellPresentation.settingDefault(key):0;if(key==SpellPropertyKey.STYLE)return styleAt(cell);if(key==SpellPropertyKey.VISUAL)return visualAt(cell);return SpellPresentation.clampSetting(key,spellData.get(settingIndex(cell,key)));}
 public int radiusAt(int cell){return propertyValue(cell,SpellPropertyKey.RADIUS);}
 public int damageKindAt(int cell){return propertyValue(cell,SpellPropertyKey.DAMAGE_KIND);}
 public int potenceAt(int cell){return propertyValue(cell,SpellPropertyKey.POTENCE);}
 public int boxWidthAt(int cell){return propertyValue(cell,SpellPropertyKey.WIDTH);}
 public int boxHeightAt(int cell){return propertyValue(cell,SpellPropertyKey.HEIGHT);}
 public int boxDepthAt(int cell){return propertyValue(cell,SpellPropertyKey.DEPTH);}
 public int targetTypeAt(int cell){return propertyValue(cell,SpellPropertyKey.TARGET_TYPE);}
 public SpellComponentDefinition definitionAt(int cell){return SpellComponents.byType(typeAt(cell));}
 public boolean hasAny(){for(int i=0;i<SpellPresentation.CELLS;i++)if(typeAt(i)!=SpellPresentation.TYPE_EMPTY)return true;return false;}
 public int[] snapshotPlan(){int[] values=SpellPresentation.emptyPlan();for(int i=0;i<SpellPresentation.PLAN_DATA_SIZE;i++)values[i]=spellData.get(i);return values;}
 public int[] snapshotSettings(){int[] values=SpellPresentation.emptySettings();for(int i=0;i<SpellPresentation.SETTINGS_DATA_SIZE;i++)values[i]=spellData.get(SETTINGS_DATA_BASE+i);return values;}
 private boolean valid(int cell){return cell>=0&&cell<SpellPresentation.CELLS;}
 private void clearAllDefaults(){for(int cell=0;cell<SpellPresentation.CELLS;cell++)resetCell(cell);}
 private void resetCell(int cell){int o=off(cell);spellData.set(o,SpellPresentation.TYPE_EMPTY);spellData.set(o+1,SpellPresentation.STYLE_DEFAULT);spellData.set(o+2,SpellPresentation.VISUAL_DEFAULT);for(SpellPropertyKey key:SpellPropertyKey.values())if(key.isSetting())spellData.set(settingIndex(cell,key),SpellPresentation.settingDefault(key));}
 private void setCellType(int cell,int type){if(!valid(cell))return;resetCell(cell);if(type==SpellPresentation.TYPE_EMPTY)return;SpellComponentDefinition definition=SpellComponents.byType(type);if(definition==null)return;int o=off(cell);spellData.set(o,type);spellData.set(o+1,definition.defaultStyle());spellData.set(o+2,definition.defaultVisual());for(SpellPropertyDefinition property:definition.settings())setPropertyValue(cell,property.key(),property.defaultValue());}
 private boolean supportsSetting(int cell,SpellPropertyKey key){SpellComponentDefinition definition=definitionAt(cell);if(definition==null)return false;for(SpellPropertyDefinition property:definition.settings())if(property.key()==key)return true;return false;}
 private void swap(int a,int b){if(!valid(a)||!valid(b)||a==b)return;for(int k=0;k<SpellPresentation.STRIDE;k++){int oa=off(a)+k,ob=off(b)+k,v=spellData.get(oa);spellData.set(oa,spellData.get(ob));spellData.set(ob,v);}for(SpellPropertyKey key:SpellPropertyKey.values())if(key.isSetting()){int ia=settingIndex(a,key),ib=settingIndex(b,key),v=spellData.get(ia);spellData.set(ia,spellData.get(ib));spellData.set(ib,v);}}
 private void setPropertyValue(int cell,SpellPropertyKey key,int value){if(!valid(cell))return;if(key==SpellPropertyKey.STYLE){if(typeAt(cell)!=SpellPresentation.TYPE_EMPTY)spellData.set(off(cell)+1,Mth.clamp(value,0,SpellPresentation.STYLE_COUNT-1));return;}if(key==SpellPropertyKey.VISUAL){if(typeAt(cell)!=SpellPresentation.TYPE_EMPTY)spellData.set(off(cell)+2,Mth.clamp(value,0,SpellPresentation.VISUAL_COUNT-1));return;}if(key.isSetting()&&supportsSetting(cell,key))spellData.set(settingIndex(cell,key),SpellPresentation.clampSetting(key,value));}
 @Override public boolean clickMenuButton(Player player,int id){
  if(id>=ACTION_ADD_COMPONENT_BASE&&id<ACTION_MOVE_BASE){int code=id-ACTION_ADD_COMPONENT_BASE,cell=code/COMPONENT_ACTION_STRIDE,type=code%COMPONENT_ACTION_STRIDE;if(valid(cell)&&SpellComponents.byType(type)!=null){setCellType(cell,type);return true;}}
  if(id>=ACTION_MOVE_BASE&&id<ACTION_CLEAR_BASE){int code=id-ACTION_MOVE_BASE,from=code/SpellPresentation.CELLS,to=code%SpellPresentation.CELLS;swap(from,to);return true;}
  if(id>=ACTION_CLEAR_BASE&&id<ACTION_SET_PROPERTY_BASE){setCellType(id-ACTION_CLEAR_BASE,SpellPresentation.TYPE_EMPTY);return true;}
  if(id>=ACTION_SET_PROPERTY_BASE&&id<ACTION_WRITE){int code=id-ACTION_SET_PROPERTY_BASE,cell=code/PROPERTY_CELL_STRIDE,rest=code%PROPERTY_CELL_STRIDE;SpellPropertyKey key=SpellPropertyKey.byId(rest/PROPERTY_KEY_STRIDE);int value=rest%PROPERTY_KEY_STRIDE;if(key!=null){setPropertyValue(cell,key,value);return true;}}
  if(id==ACTION_WRITE){if(!hasAny())return false;if(!player.getAbilities().instabuild){ItemStack paperStack=paper.getItem(0);if(!paperStack.is(Items.PAPER)||paperStack.isEmpty())return false;paperStack.shrink(1);paper.setChanged();}ItemStack written=WrittenSpellItem.create(snapshotPlan(),snapshotSettings());if(!player.addItem(written))player.drop(written,false);return true;}
  return false;
 }
 @Override public boolean stillValid(Player p){return true;}
 @Override public ItemStack quickMoveStack(Player p,int index){return ItemStack.EMPTY;}
}
