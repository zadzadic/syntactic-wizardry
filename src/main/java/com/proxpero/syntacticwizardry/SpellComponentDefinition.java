package com.proxpero.syntacticwizardry;
import net.minecraft.world.item.ItemStack;
import java.util.List;
public interface SpellComponentDefinition {
 int typeId();
 String displayName();
 boolean isShape();
 int defaultStyle();
 int defaultVisual();
 List<Integer> styleOptions();
 boolean supportsVisuals();
 List<SpellPropertyDefinition> settings();
 ItemStack createEditorIcon();
 ComponentExecutionResult execute(SpellExecutionContext context);
}
