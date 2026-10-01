package com.proxpero.syntacticwizardry;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

public final class RandomSpellLootModifier extends LootModifier {
    public static final MapCodec<RandomSpellLootModifier> CODEC = RecordCodecBuilder.mapCodec(instance ->
            LootModifier.codecStart(instance).and(instance.group(
                    com.mojang.serialization.Codec.FLOAT.fieldOf("base_chance").forGetter(modifier -> modifier.baseChance),
                    com.mojang.serialization.Codec.FLOAT.fieldOf("second_tier_chance").forGetter(modifier -> modifier.secondTierChance),
                    com.mojang.serialization.Codec.FLOAT.fieldOf("third_tier_chance").forGetter(modifier -> modifier.thirdTierChance),
                    com.mojang.serialization.Codec.FLOAT.fieldOf("dungeon_chance").forGetter(modifier -> modifier.dungeonChance)
            )).apply(instance, RandomSpellLootModifier::new)
    );

    private final float baseChance;
    private final float secondTierChance;
    private final float thirdTierChance;
    private final float dungeonChance;

    public RandomSpellLootModifier(LootItemCondition[] conditions,
                                   float baseChance,
                                   float secondTierChance,
                                   float thirdTierChance,
                                   float dungeonChance) {
        super(conditions);
        this.baseChance = clampChance(baseChance);
        this.secondTierChance = clampChance(secondTierChance);
        this.thirdTierChance = clampChance(thirdTierChance);
        this.dungeonChance = clampChance(dungeonChance);
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        RandomSource random = context.getRandom();
        Entity entity = context.getParamOrNull(LootContextParams.THIS_ENTITY);

        if (entity instanceof Mob mob) {
            RandomSpellGenerator.Tier tier = mobTier(mob.getType());
            float chance = mobChance(tier);
            if (chance >= 1.0F || random.nextFloat() < chance) {
                ItemStack spell = RandomSpellGenerator.generate(tier, random);
                if (!spell.isEmpty()) generatedLoot.add(spell);
            }
            return generatedLoot;
        }

        ResourceLocation tableId = context.getQueriedLootTableId();
        if (isDungeonLootTable(tableId) && random.nextFloat() < dungeonChance) {
            ItemStack spell = RandomSpellGenerator.generate(randomDungeonTier(random), random);
            if (!spell.isEmpty()) generatedLoot.add(spell);
        }

        return generatedLoot;
    }

    private RandomSpellGenerator.Tier mobTier(EntityType<?> type) {
        if (type == EntityType.ELDER_GUARDIAN
                || type == EntityType.RAVAGER
                || type == EntityType.WITHER
                || type == EntityType.WARDEN) {
            return RandomSpellGenerator.Tier.MASTER;
        }

        if (type == EntityType.EVOKER
                || type == EntityType.GUARDIAN
                || type == EntityType.PIGLIN_BRUTE) {
            return RandomSpellGenerator.Tier.WIZARD;
        }

        if (type == EntityType.WITCH
                || type == EntityType.ENDERMAN
                || type == EntityType.PILLAGER
                || type == EntityType.BLAZE) {
            return RandomSpellGenerator.Tier.INITIATE;
        }

        return RandomSpellGenerator.Tier.APPRENTICE;
    }

    private float mobChance(RandomSpellGenerator.Tier tier) {
        return switch (tier) {
            case APPRENTICE -> baseChance;
            case INITIATE -> secondTierChance;
            case WIZARD -> thirdTierChance;
            case MASTER -> 1.0F;
        };
    }

    private static RandomSpellGenerator.Tier randomDungeonTier(RandomSource random) {
        float roll = random.nextFloat();
        if (roll < 0.60F) return RandomSpellGenerator.Tier.APPRENTICE;
        if (roll < 0.85F) return RandomSpellGenerator.Tier.INITIATE;
        if (roll < 0.97F) return RandomSpellGenerator.Tier.WIZARD;
        return RandomSpellGenerator.Tier.MASTER;
    }

    private static boolean isDungeonLootTable(ResourceLocation id) {
        if (id == null || !"minecraft".equals(id.getNamespace())) return false;
        String path = id.getPath();

        return path.equals("chests/simple_dungeon")
                || path.equals("chests/abandoned_mineshaft")
                || path.equals("chests/desert_pyramid")
                || path.equals("chests/jungle_temple")
                || path.equals("chests/woodland_mansion")
                || path.equals("chests/nether_bridge")
                || path.equals("chests/end_city_treasure")
                || path.equals("chests/ancient_city")
                || path.equals("chests/underwater_ruin_small")
                || path.equals("chests/underwater_ruin_big")
                || path.startsWith("chests/stronghold_")
                || path.startsWith("chests/bastion_")
                || path.startsWith("chests/trial_chambers/");
    }

    private static float clampChance(float value) {
        return Math.max(0.0F, Math.min(1.0F, value));
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
