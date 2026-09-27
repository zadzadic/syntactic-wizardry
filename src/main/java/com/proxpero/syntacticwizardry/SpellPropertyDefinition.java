package com.proxpero.syntacticwizardry;
import java.util.function.IntFunction;
public record SpellPropertyDefinition(SpellPropertyKey key,String label,SpellPropertyKind kind,int minValue,int maxValue,int defaultValue,IntFunction<String> valueLabel) {
 public String format(int value){return valueLabel.apply(value);}
}
