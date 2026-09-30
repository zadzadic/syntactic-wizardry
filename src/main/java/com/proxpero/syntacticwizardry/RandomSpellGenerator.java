package com.proxpero.syntacticwizardry;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Produces ordinary Written Spell items for loot and other world-generation systems.
 * Composition is tier-driven, while component settings are randomized from each
 * component's existing property definitions.
 */
public final class RandomSpellGenerator {
    private RandomSpellGenerator() {}

    public enum Tier {
        APPRENTICE("Apprentice", 1, 4),
        INITIATE("Initiate", 2, 6),
        WIZARD("Wizard", 4, 8),
        MASTER("Master", 8, 10);

        private final String displayName;
        private final int minPotence;
        private final int maxPotence;

        Tier(String displayName, int minPotence, int maxPotence) {
            this.displayName = displayName;
            this.minPotence = minPotence;
            this.maxPotence = maxPotence;
        }

        public String displayName() { return displayName; }
        public int minPotence() { return minPotence; }
        public int maxPotence() { return maxPotence; }
    }

    public static ItemStack generate(Tier tier, RandomSource random) {
        if (tier == null) tier = Tier.APPRENTICE;
        if (random == null) random = RandomSource.create();

        Composition composition = compositionFor(tier, random);
        int[] plan = SpellPresentation.emptyPlan();
        int[] settings = SpellPresentation.emptySettings();

        List<SpellComponentDefinition> shapes = SpellComponents.shapes();
        List<SpellComponentDefinition> effects = SpellComponents.effects();
        if (shapes.isEmpty() || effects.isEmpty()) return ItemStack.EMPTY;

        List<PlacedComponent> core = new ArrayList<>(composition.shapes + composition.effects);
        List<SpellComponentDefinition> selectedEffects = new ArrayList<>(composition.effects);

        SpellComponentDefinition firstEffect = composition.effects == 1
                ? randomEntry(effects, random)
                : randomEffectExcludingDimensionalStorage(effects, random);
        TargetFamily targetFamily = targetFamily(firstEffect);
        selectedEffects.add(firstEffect);
        while (selectedEffects.size() < composition.effects) {
            selectedEffects.add(randomEffectForFamily(effects, targetFamily, true, random));
        }

        boolean requiresDirectPlayer = selectedEffects.stream()
                .anyMatch(definition -> definition.typeId() == SpellComponents.TYPE_DIMENSIONAL_STORAGE);

        int row = 0;
        SpellComponentDefinition previousShape = null;
        for (int i = 0; i < composition.shapes && row < SpellPresentation.ROWS; i++, row++) {
            boolean terminal = i == composition.shapes - 1;
            SpellComponentDefinition definition = terminal
                    ? randomTerminalShape(previousShape, shapes, targetFamily, requiresDirectPlayer, random)
                    : randomIntermediateShape(previousShape, shapes, requiresDirectPlayer && i == composition.shapes - 2, random);

            int cell = row * SpellPresentation.COLS;
            place(plan, settings, cell, definition, tier, composition, random);

            if (terminal && (definition.typeId() == SpellPresentation.TYPE_TARGET || definition.typeId() == SpellPresentation.TYPE_TOUCH)) {
                SpellPresentation.setSetting(
                        settings,
                        cell,
                        SpellPropertyKey.TARGET_TYPE,
                        targetFamily == TargetFamily.BLOCK ? SpellPresentation.TARGET_BLOCKS : SpellPresentation.TARGET_ENTITIES
                );
            }

            core.add(new PlacedComponent(row, cell, definition));
            previousShape = definition;
        }

        for (SpellComponentDefinition definition : selectedEffects) {
            if (row >= SpellPresentation.ROWS) break;
            int cell = row * SpellPresentation.COLS;
            place(plan, settings, cell, definition, tier, composition, random);
            core.add(new PlacedComponent(row, cell, definition));

            if (definition.typeId() == SpellComponents.TYPE_TEMPORARY_BLOCK) {
                SpellComponentDefinition duration = SpellComponents.byType(SpellComponents.TYPE_DURATION);
                if (duration != null) {
                    int durationCell = cell + 1;
                    place(plan, settings, durationCell, duration, tier, composition, random);
                    SpellPresentation.setSetting(
                            settings,
                            durationCell,
                            SpellPropertyKey.DURATION_SECONDS,
                            between(random, 15, 30)
                    );
                }
            }

            row++;
        }

        if (composition.modifiers > 0) {
            addModifiers(plan, settings, core, composition.modifiers, tier, composition, random);
        }

        String name = generateName(core, settings);
        return WrittenSpellItem.create(plan, settings, name);
    }

    private static Composition compositionFor(Tier tier, RandomSource random) {
        return switch (tier) {
            case APPRENTICE -> new Composition(1, 1, 0);

            case INITIATE -> {
                // Initiates are usually more complex, but simple high-potence spells remain possible.
                if (random.nextFloat() < 0.30F) yield new Composition(1, 1, 0);
                yield random.nextBoolean()
                        ? new Composition(2, 1, 0)
                        : new Composition(1, 2, 0);
            }

            case WIZARD -> {
                float roll = random.nextFloat();
                int shapes;
                int effects;
                if (roll < 0.55F) {
                    shapes = 2;
                    effects = 2;
                } else if (roll < 0.75F) {
                    shapes = 2;
                    effects = 1;
                } else if (roll < 0.95F) {
                    shapes = 1;
                    effects = 2;
                } else {
                    shapes = 1;
                    effects = 1;
                }
                int modifiers = random.nextFloat() < 0.70F ? 1 : 0;
                yield new Composition(shapes, effects, modifiers);
            }

            case MASTER -> {
                float roll = random.nextFloat();
                int shapes;
                int effects;
                if (roll < 0.35F) {
                    shapes = 3;
                    effects = 2;
                } else if (roll < 0.70F) {
                    shapes = 2;
                    effects = 3;
                } else if (roll < 0.85F) {
                    shapes = 2;
                    effects = 2;
                } else if (random.nextBoolean()) {
                    shapes = 3;
                    effects = 1;
                } else {
                    shapes = 1;
                    effects = 3;
                }
                int modifiers = random.nextFloat() < 0.85F ? 1 : 0;
                yield new Composition(shapes, effects, modifiers);
            }
        };
    }

    private static void place(int[] plan, int[] settings, int cell, SpellComponentDefinition definition,
                              Tier tier, Composition composition, RandomSource random) {
        int style = definition.defaultStyle();
        if (!definition.styleOptions().isEmpty()) {
            style = definition.styleOptions().get(random.nextInt(definition.styleOptions().size()));
        }

        int visual = definition.defaultVisual();
        if (definition.supportsVisuals()) visual = random.nextInt(SpellPresentation.VISUAL_COUNT);

        SpellPresentation.setCell(plan, cell, definition.typeId(), style, visual);
        randomizeSettings(settings, cell, definition, tier, composition, random);
    }

    private static void randomizeSettings(int[] settings, int cell, SpellComponentDefinition definition,
                                          Tier tier, Composition composition, RandomSource random) {
        for (SpellPropertyDefinition property : definition.settings()) {
            int value;
            if (property.key() == SpellPropertyKey.POTENCE) {
                value = randomPotence(tier, composition, random);
                if (definition.typeId() == SpellComponents.TYPE_FLIGHT) value = Math.max(4, value);
            } else if (property.kind() == SpellPropertyKind.OPTIONS) {
                value = randomOptionValue(property, random);
            } else {
                value = randomPropertyValue(property, random);
            }
            SpellPresentation.setSetting(settings, cell, property.key(), value);
        }

        ensureOptionSelections(settings, cell, definition, random);

        // Alter should not accidentally generate a completely neutral Effect.
        if (definition.typeId() == SpellComponents.TYPE_RELATIVE) {
            SpellPresentation.setSetting(settings, cell, SpellPropertyKey.GRAVITY_MODE, SpellPresentation.RELATIVE_FRONT);
        }

        if (definition.typeId() == SpellComponents.TYPE_ALTER
                && SpellPresentation.settingAt(settings, cell, SpellPropertyKey.ALTER_STRENGTH) == SpellPresentation.ALTER_SETTING_DEFAULT
                && SpellPresentation.settingAt(settings, cell, SpellPropertyKey.ALTER_SPEED) == SpellPresentation.ALTER_SETTING_DEFAULT
                && SpellPresentation.settingAt(settings, cell, SpellPropertyKey.ALTER_TOUGHNESS) == SpellPresentation.ALTER_SETTING_DEFAULT) {
            SpellPropertyKey key = switch (random.nextInt(3)) {
                case 0 -> SpellPropertyKey.ALTER_STRENGTH;
                case 1 -> SpellPropertyKey.ALTER_SPEED;
                default -> SpellPropertyKey.ALTER_TOUGHNESS;
            };
            int value = random.nextBoolean()
                    ? Math.min(SpellPresentation.ALTER_SETTING_MAX, SpellPresentation.ALTER_SETTING_DEFAULT + 1)
                    : Math.max(SpellPresentation.ALTER_SETTING_MIN, SpellPresentation.ALTER_SETTING_DEFAULT - 1);
            SpellPresentation.setSetting(settings, cell, key, value);
        }
    }

    private static int randomPotence(Tier tier, Composition composition, RandomSource random) {
        int min = tier.minPotence();
        int max = tier.maxPotence();
        int first = between(random, min, max);

        // Simpler spells trend toward the upper end of the tier's Potence range.
        int complexity = composition.shapes + composition.effects + composition.modifiers;
        int tierMaximum = switch (tier) {
            case APPRENTICE -> 2;
            case INITIATE -> 3;
            case WIZARD -> 5;
            case MASTER -> 6;
        };
        if (complexity < tierMaximum) {
            return Math.max(first, between(random, min, max));
        }
        return first;
    }

    private static int randomOptionValue(SpellPropertyDefinition property, RandomSource random) {
        return between(random, property.minValue(), property.maxValue());
    }

    private static void ensureOptionSelections(int[] settings, int cell,
                                               SpellComponentDefinition definition,
                                               RandomSource random) {
        for (SpellPropertyDefinition property : definition.settings()) {
            if (property.kind() != SpellPropertyKind.OPTIONS) continue;
            int value = SpellPresentation.settingAt(settings, cell, property.key());
            if (value < property.minValue() || value > property.maxValue()) {
                SpellPresentation.setSetting(settings, cell, property.key(), randomOptionValue(property, random));
            }
        }
    }

    private static int randomPropertyValue(SpellPropertyDefinition property, RandomSource random) {
        int min = property.minValue();
        int max = property.maxValue();
        if (max <= min) return min;

        // Numeric dimensions and durations favor moderate values instead of always filling huge areas.
        return switch (property.kind()) {
            case STEPPER -> {
                int a = between(random, min, max);
                int b = between(random, min, max);
                yield Math.min(a, b);
            }
            case OPTIONS -> between(random, min, max);
        };
    }

    private static void addModifiers(int[] plan, int[] settings, List<PlacedComponent> core, int count,
                                     Tier tier, Composition composition, RandomSource random) {
        List<PlacedComponent> owners = new ArrayList<>();
        for (PlacedComponent placed : core) {
            if (!modifierChoices(placed.definition).isEmpty()) owners.add(placed);
        }
        if (owners.isEmpty()) return;

        for (int i = 0; i < count; i++) {
            PlacedComponent owner = randomEntry(owners, random);
            List<SpellComponentDefinition> choices = modifierChoices(owner.definition);
            if (choices.isEmpty()) continue;

            SpellComponentDefinition modifier = randomEntry(choices, random);
            int modifierCell = owner.row * SpellPresentation.COLS + 1 + i;
            if (modifierCell >= (owner.row + 1) * SpellPresentation.COLS) break;
            if (SpellPresentation.typeAt(plan, modifierCell) != SpellPresentation.TYPE_EMPTY) continue;
            place(plan, settings, modifierCell, modifier, tier, composition, random);
        }
    }

    private static List<SpellComponentDefinition> modifierChoices(SpellComponentDefinition owner) {
        List<SpellComponentDefinition> result = new ArrayList<>();
        if (owner == null) return result;

        if (owner.isShape()) {
            addModifier(result, SpellComponents.TYPE_DURATION);
            addModifier(result, SpellComponents.TYPE_STREAM);

            if (owner.typeId() == SpellPresentation.TYPE_MISSILE || owner.typeId() == SpellComponents.TYPE_CHAIN) {
                addModifier(result, SpellComponents.TYPE_RANGE);
                addModifier(result, SpellComponents.TYPE_SPLIT);
                addModifier(result, SpellComponents.TYPE_RICOCHET);
                addModifier(result, SpellComponents.TYPE_PIERCING);
                addModifier(result, SpellComponents.TYPE_HOMING);
            }
        } else if (SpellComponents.isEffect(owner)) {
            addModifier(result, SpellComponents.TYPE_DURATION);
            addModifier(result, SpellComponents.TYPE_CHANNEL);
            addModifier(result, SpellComponents.TYPE_EXCLUDE_CASTER);
            if (owner.supportsBlockInteraction()) addModifier(result, SpellComponents.TYPE_BLOCK_INTERACTION);
        }
        return result;
    }

    private static void addModifier(List<SpellComponentDefinition> result, int type) {
        SpellComponentDefinition definition = SpellComponents.byType(type);
        if (definition != null && SpellComponents.isModifier(definition)) result.add(definition);
    }

    private static String generateName(List<PlacedComponent> core, int[] settings) {
        SpellComponentDefinition firstShape = null;
        SpellComponentDefinition firstEffect = null;
        int effectCell = -1;

        for (PlacedComponent placed : core) {
            if (firstShape == null && placed.definition.isShape()) firstShape = placed.definition;
            if (firstEffect == null && SpellComponents.isEffect(placed.definition)) {
                firstEffect = placed.definition;
                effectCell = placed.cell;
            }
        }

        String shapeName = firstShape == null ? "Spell" : firstShape.displayName();
        if (firstEffect == null) return shapeName;

        if (firstEffect.typeId() == SpellPresentation.TYPE_DAMAGE && effectCell >= 0) {
            return SpellPresentation.damageKindName(SpellPresentation.damageKindAt(settings, effectCell)) + " " + shapeName;
        }
        return firstEffect.displayName() + " " + shapeName;
    }

    private static SpellComponentDefinition randomIntermediateShape(SpellComponentDefinition previousShape,
                                                                  List<SpellComponentDefinition> shapes,
                                                                  boolean forbidTarget,
                                                                  RandomSource random) {
        List<SpellComponentDefinition> candidates = new ArrayList<>();
        for (SpellComponentDefinition shape : shapes) {
            if (previousShape != null && isFirstShapeOnly(shape)) continue;
            if (shape.typeId() == SpellPresentation.TYPE_TARGET) continue;
            candidates.add(shape);
        }
        return candidates.isEmpty() ? randomEntry(shapes, random) : randomEntry(candidates, random);
    }

    private static SpellComponentDefinition randomTerminalShape(SpellComponentDefinition previousShape,
                                                              List<SpellComponentDefinition> shapes,
                                                              TargetFamily family,
                                                              boolean requiresDirectPlayer,
                                                              RandomSource random) {
        List<SpellComponentDefinition> candidates = new ArrayList<>();
        for (SpellComponentDefinition shape : shapes) {
            if (previousShape != null && previousShape.typeId() == SpellPresentation.TYPE_TARGET) continue;
            if (previousShape != null && isFirstShapeOnly(shape)) continue;

            int type = shape.typeId();
            if (requiresDirectPlayer) {
                if (type == SpellComponents.TYPE_SELF
                        || type == SpellPresentation.TYPE_TARGET
                        || type == SpellPresentation.TYPE_TOUCH
                        || type == SpellComponents.TYPE_RUNE) {
                    candidates.add(shape);
                }
                continue;
            }

            if (family == TargetFamily.BLOCK) {
                if (type == SpellPresentation.TYPE_TARGET
                        || type == SpellPresentation.TYPE_TOUCH
                        || isAreaShape(shape)) {
                    candidates.add(shape);
                }
            } else {
                if (type == SpellComponents.TYPE_SELF
                        || type == SpellPresentation.TYPE_TARGET
                        || type == SpellPresentation.TYPE_TOUCH
                        || type == SpellComponents.TYPE_RUNE
                        || isAreaShape(shape)) {
                    candidates.add(shape);
                }
            }
        }
        return candidates.isEmpty() ? randomEntry(shapes, random) : randomEntry(candidates, random);
    }

    private static boolean isFirstShapeOnly(SpellComponentDefinition shape) {
        if (shape == null) return false;
        int type = shape.typeId();
        return type == SpellPresentation.TYPE_TOUCH
                || type == SpellPresentation.TYPE_MISSILE;
    }

    private static boolean isAreaShape(SpellComponentDefinition shape) {
        if (shape == null) return false;
        int type = shape.typeId();
        return type == SpellPresentation.TYPE_SPHERE
                || type == SpellPresentation.TYPE_BOX
                || type == SpellPresentation.TYPE_CONE;
    }

    private static TargetFamily targetFamily(SpellComponentDefinition effect) {
        int type = effect.typeId();
        return type == SpellPresentation.TYPE_DIG
                || type == SpellComponents.TYPE_TEMPORARY_BLOCK
                || type == SpellComponents.TYPE_MARK
                ? TargetFamily.BLOCK
                : TargetFamily.ENTITY;
    }

    private static SpellComponentDefinition randomEffectForFamily(List<SpellComponentDefinition> effects,
                                                                  TargetFamily family,
                                                                  boolean excludeDimensionalStorage,
                                                                  RandomSource random) {
        List<SpellComponentDefinition> candidates = new ArrayList<>();
        for (SpellComponentDefinition effect : effects) {
            if (excludeDimensionalStorage && effect.typeId() == SpellComponents.TYPE_DIMENSIONAL_STORAGE) continue;
            if (targetFamily(effect) == family) candidates.add(effect);
        }
        return candidates.isEmpty() ? randomEffectExcludingDimensionalStorage(effects, random) : randomEntry(candidates, random);
    }

    private static SpellComponentDefinition randomEffectExcludingDimensionalStorage(List<SpellComponentDefinition> effects,
                                                                                     RandomSource random) {
        List<SpellComponentDefinition> candidates = new ArrayList<>();
        for (SpellComponentDefinition effect : effects) {
            if (effect.typeId() != SpellComponents.TYPE_DIMENSIONAL_STORAGE) candidates.add(effect);
        }
        return candidates.isEmpty() ? randomEntry(effects, random) : randomEntry(candidates, random);
    }

    private static int between(RandomSource random, int min, int max) {
        return min + random.nextInt(max - min + 1);
    }

    private static <T> T randomEntry(List<T> values, RandomSource random) {
        return values.get(random.nextInt(values.size()));
    }

    private enum TargetFamily { ENTITY, BLOCK }

    private record Composition(int shapes, int effects, int modifiers) {}
    private record PlacedComponent(int row, int cell, SpellComponentDefinition definition) {}
}
