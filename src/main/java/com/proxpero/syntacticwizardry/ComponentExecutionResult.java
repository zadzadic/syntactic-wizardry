package com.proxpero.syntacticwizardry;
import java.util.List;
public record ComponentExecutionResult(boolean spawnedShape,List<ShapeResolution> continuations) {
 public static final ComponentExecutionResult NONE=new ComponentExecutionResult(false,List.of());
 public static ComponentExecutionResult spawned(){return new ComponentExecutionResult(true,List.of());}
 public static ComponentExecutionResult resolved(ShapeResolution resolution){return new ComponentExecutionResult(true,List.of(resolution));}
}
