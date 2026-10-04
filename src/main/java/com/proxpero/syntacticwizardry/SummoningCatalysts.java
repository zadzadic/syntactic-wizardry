package com.proxpero.syntacticwizardry;

import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.LinkedHashMap;
import java.util.Map;

public final class SummoningCatalysts {
    private static final Map<Item, EntityType<? extends Mob>> EXACT = new LinkedHashMap<>();

    static {
        put(Items.AMETHYST_SHARD, EntityType.ALLAY);
        put(Items.ARMADILLO_SCUTE, EntityType.ARMADILLO);
        put(Items.AXOLOTL_BUCKET, EntityType.AXOLOTL);
        put(Items.GLOW_BERRIES, EntityType.BAT);
        put(Items.HONEYCOMB, EntityType.BEE);
        put(Items.BLAZE_ROD, EntityType.BLAZE);
        put(Items.BROWN_MUSHROOM, EntityType.BOGGED);
        put(Items.BREEZE_ROD, EntityType.BREEZE);
        put(Items.CACTUS, EntityType.CAMEL);
        put(Items.COOKED_COD, EntityType.CAT);
        put(Items.STRING, EntityType.CAVE_SPIDER);
        put(Items.CHICKEN, EntityType.CHICKEN);
        put(Items.COD, EntityType.COD);
        put(Items.BEEF, EntityType.COW);
        put(Items.GUNPOWDER, EntityType.CREEPER);
        put(Items.HEART_OF_THE_SEA, EntityType.DOLPHIN);
        put(Items.CHEST, EntityType.DONKEY);
        put(Items.TRIDENT, EntityType.DROWNED);
        put(Items.WET_SPONGE, EntityType.ELDER_GUARDIAN);
        put(Items.DRAGON_BREATH, EntityType.ENDER_DRAGON);
        put(Items.ENDER_PEARL, EntityType.ENDERMAN);
        put(Items.ENDER_EYE, EntityType.ENDERMITE);
        put(Items.BOOK, EntityType.EVOKER);
        put(Items.SWEET_BERRIES, EntityType.FOX);
        put(Items.OCHRE_FROGLIGHT, EntityType.FROG);
        put(Items.GHAST_TEAR, EntityType.GHAST);
        put(Items.ZOMBIE_HEAD, EntityType.GIANT);
        put(Items.GLOW_INK_SAC, EntityType.GLOW_SQUID);
        put(Items.GOAT_HORN, EntityType.GOAT);
        put(Items.PRISMARINE_SHARD, EntityType.GUARDIAN);
        put(Items.CRIMSON_FUNGUS, EntityType.HOGLIN);
        put(Items.LEATHER_HORSE_ARMOR, EntityType.HORSE);
        put(Items.SAND, EntityType.HUSK);
        put(Items.BOW, EntityType.ILLUSIONER);
        put(Items.IRON_INGOT, EntityType.IRON_GOLEM);
        put(Items.HAY_BLOCK, EntityType.LLAMA);
        put(Items.MAGMA_CREAM, EntityType.MAGMA_CUBE);
        put(Items.MUSHROOM_STEW, EntityType.MOOSHROOM);
        put(Items.GOLDEN_CARROT, EntityType.MULE);
        put(Items.COOKED_SALMON, EntityType.OCELOT);
        put(Items.BAMBOO, EntityType.PANDA);
        put(Items.MELON_SEEDS, EntityType.PARROT);
        put(Items.PHANTOM_MEMBRANE, EntityType.PHANTOM);
        put(Items.PORKCHOP, EntityType.PIG);
        put(Items.GOLD_INGOT, EntityType.PIGLIN);
        put(Items.GOLDEN_AXE, EntityType.PIGLIN_BRUTE);
        put(Items.ARROW, EntityType.PILLAGER);
        put(Items.PACKED_ICE, EntityType.POLAR_BEAR);
        put(Items.PUFFERFISH, EntityType.PUFFERFISH);
        put(Items.RABBIT, EntityType.RABBIT);
        put(Items.SADDLE, EntityType.RAVAGER);
        put(Items.SALMON, EntityType.SALMON);
        put(Items.MUTTON, EntityType.SHEEP);
        put(Items.SHULKER_SHELL, EntityType.SHULKER);
        put(Items.STONE, EntityType.SILVERFISH);
        put(Items.BONE, EntityType.SKELETON);
        put(Items.BONE_BLOCK, EntityType.SKELETON_HORSE);
        put(Items.SLIME_BALL, EntityType.SLIME);
        put(Items.TORCHFLOWER_SEEDS, EntityType.SNIFFER);
        put(Items.CARVED_PUMPKIN, EntityType.SNOW_GOLEM);
        put(Items.SPIDER_EYE, EntityType.SPIDER);
        put(Items.INK_SAC, EntityType.SQUID);
        put(Items.BLUE_ICE, EntityType.STRAY);
        put(Items.WARPED_FUNGUS, EntityType.STRIDER);
        put(Items.TADPOLE_BUCKET, EntityType.TADPOLE);
        put(Items.BLUE_CARPET, EntityType.TRADER_LLAMA);
        put(Items.TROPICAL_FISH, EntityType.TROPICAL_FISH);
        put(Items.TURTLE_SCUTE, EntityType.TURTLE);
        put(Items.IRON_SWORD, EntityType.VEX);
        put(Items.EMERALD, EntityType.VILLAGER);
        put(Items.IRON_AXE, EntityType.VINDICATOR);
        put(Items.LEAD, EntityType.WANDERING_TRADER);
        put(Items.ECHO_SHARD, EntityType.WARDEN);
        put(Items.GLASS_BOTTLE, EntityType.WITCH);
        put(Items.NETHER_STAR, EntityType.WITHER);
        put(Items.COAL, EntityType.WITHER_SKELETON);
        put(Items.COOKED_BEEF, EntityType.WOLF);
        put(Items.WARPED_WART_BLOCK, EntityType.ZOGLIN);
        put(Items.ROTTEN_FLESH, EntityType.ZOMBIE);
        put(Items.IRON_HORSE_ARMOR, EntityType.ZOMBIE_HORSE);
        put(Items.GOLDEN_APPLE, EntityType.ZOMBIE_VILLAGER);
        put(Items.GOLD_NUGGET, EntityType.ZOMBIFIED_PIGLIN);
    }

    private SummoningCatalysts() {}

    public static EntityType<? extends Mob> resolve(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;
        if (stack.is(ItemTags.WOOL)) return EntityType.SHEEP;
        return EXACT.get(stack.getItem());
    }

    public static Map<Item, EntityType<? extends Mob>> exactEntries() {
        return Map.copyOf(EXACT);
    }

    private static void put(Item item, EntityType<? extends Mob> type) {
        EXACT.put(item, type);
    }
}
