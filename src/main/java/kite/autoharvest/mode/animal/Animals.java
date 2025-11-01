package kite.autoharvest.mode.animal;

import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.HoglinEntity;
import net.minecraft.entity.passive.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.ItemTags;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Animals {
    public static final Map<Class<? extends Entity>, Set<Item>> BREEDABLE_WHITELIST = new HashMap<>();

    private static Set<Item> getFlowerItems() {
        Set<Item> flowers = new HashSet<>();
        for (Item item : Registries.ITEM) {
            if (new ItemStack(item).isIn(ItemTags.FLOWERS)) {
                flowers.add(item);
            }
        }
        return flowers;
    }

    static {
        Set<Item> wheatFeeders = Set.of(Items.WHEAT);
        BREEDABLE_WHITELIST.put(CowEntity.class, wheatFeeders);
        BREEDABLE_WHITELIST.put(SheepEntity.class, wheatFeeders);
        BREEDABLE_WHITELIST.put(GoatEntity.class, wheatFeeders);
        BREEDABLE_WHITELIST.put(MooshroomEntity.class, wheatFeeders);
        BREEDABLE_WHITELIST.put(PigEntity.class, Set.of(Items.CARROT, Items.POTATO, Items.BEETROOT));
        BREEDABLE_WHITELIST.put(ChickenEntity.class, Set.of(
                Items.WHEAT_SEEDS,
                Items.PUMPKIN_SEEDS,
                Items.MELON_SEEDS,
                Items.BEETROOT_SEEDS,
                Items.TORCHFLOWER_SEEDS,
                Items.PITCHER_POD
        ));
        BREEDABLE_WHITELIST.put(HorseEntity.class, Set.of(Items.GOLDEN_CARROT));
        BREEDABLE_WHITELIST.put(DonkeyEntity.class, Set.of(Items.GOLDEN_CARROT));
        BREEDABLE_WHITELIST.put(LlamaEntity.class, Set.of(Items.HAY_BLOCK));
        BREEDABLE_WHITELIST.put(TraderLlamaEntity.class, Set.of(Items.HAY_BLOCK));
        BREEDABLE_WHITELIST.put(WolfEntity.class, Set.of(
                Items.BEEF, Items.COOKED_BEEF,
                Items.PORKCHOP, Items.COOKED_PORKCHOP,
                Items.CHICKEN, Items.COOKED_CHICKEN,
                Items.RABBIT, Items.COOKED_RABBIT,
                Items.MUTTON, Items.COOKED_MUTTON,
                Items.ROTTEN_FLESH
        ));
        BREEDABLE_WHITELIST.put(CatEntity.class, Set.of(Items.COD, Items.SALMON));
        BREEDABLE_WHITELIST.put(OcelotEntity.class, Set.of(Items.COD, Items.SALMON));
        BREEDABLE_WHITELIST.put(RabbitEntity.class, Set.of(Items.DANDELION, Items.CARROT, Items.GOLDEN_CARROT));
        BREEDABLE_WHITELIST.put(FoxEntity.class, Set.of(Items.SWEET_BERRIES, Items.GLOW_BERRIES));
        BREEDABLE_WHITELIST.put(PandaEntity.class, Set.of(Items.BAMBOO));
        BREEDABLE_WHITELIST.put(SnifferEntity.class, Set.of(Items.MOSS_BLOCK));
        BREEDABLE_WHITELIST.put(HoglinEntity.class, Set.of(Items.CRIMSON_FUNGUS));
        BREEDABLE_WHITELIST.put(BeeEntity.class, getFlowerItems());
        BREEDABLE_WHITELIST.put(StriderEntity.class, Set.of(Items.WARPED_FUNGUS));
        BREEDABLE_WHITELIST.put(CamelEntity.class, Set.of(Items.CACTUS));
        BREEDABLE_WHITELIST.put(FrogEntity.class, Set.of(Items.SLIME_BALL));
        BREEDABLE_WHITELIST.put(TurtleEntity.class, Set.of(Items.SEAGRASS));
        BREEDABLE_WHITELIST.put(ArmadilloEntity.class, Set.of(Items.SPIDER_EYE));
        BREEDABLE_WHITELIST.put(AxolotlEntity.class, Set.of(Items.TROPICAL_FISH_BUCKET));
    }
}
