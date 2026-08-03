package kite.autoharvest.mode.animal;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.animal.armadillo.Armadillo;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.animal.bee.Bee;
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.animal.cow.MushroomCow;
import net.minecraft.world.entity.animal.equine.Donkey;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.entity.animal.equine.Llama;
import net.minecraft.world.entity.animal.equine.TraderLlama;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.entity.animal.feline.Ocelot;
import net.minecraft.world.entity.animal.fox.Fox;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.entity.animal.panda.Panda;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.animal.rabbit.Rabbit;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.animal.sniffer.Sniffer;
import net.minecraft.world.entity.animal.turtle.Turtle;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.monster.Strider;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Animals {
    public static final Map<Class<? extends Entity>, Set<Item>> BREEDABLE_WHITELIST = new HashMap<>();

    private static Set<Item> getFlowerItems() {
        Set<Item> flowers = new HashSet<>();
        for (Item item : BuiltInRegistries.ITEM) {
            if (new ItemStack(item).is(ItemTags.BEE_FOOD)) {
                flowers.add(item);
            }
        }
        return flowers;
    }

    static {
        Set<Item> wheatFeeders = Set.of(Items.WHEAT);
        BREEDABLE_WHITELIST.put(Cow.class, wheatFeeders);
        BREEDABLE_WHITELIST.put(Sheep.class, wheatFeeders);
        BREEDABLE_WHITELIST.put(Goat.class, wheatFeeders);
        BREEDABLE_WHITELIST.put(MushroomCow.class, wheatFeeders);
        BREEDABLE_WHITELIST.put(Pig.class, Set.of(Items.CARROT, Items.POTATO, Items.BEETROOT));
        BREEDABLE_WHITELIST.put(Chicken.class, Set.of(
                Items.WHEAT_SEEDS,
                Items.PUMPKIN_SEEDS,
                Items.MELON_SEEDS,
                Items.BEETROOT_SEEDS,
                Items.TORCHFLOWER_SEEDS,
                Items.PITCHER_POD
        ));
        BREEDABLE_WHITELIST.put(Horse.class, Set.of(Items.GOLDEN_CARROT));
        BREEDABLE_WHITELIST.put(Donkey.class, Set.of(Items.GOLDEN_CARROT));
        BREEDABLE_WHITELIST.put(Llama.class, Set.of(Items.HAY_BLOCK));
        BREEDABLE_WHITELIST.put(TraderLlama.class, Set.of(Items.HAY_BLOCK));
        BREEDABLE_WHITELIST.put(Wolf.class, Set.of(
                Items.BEEF, Items.COOKED_BEEF,
                Items.PORKCHOP, Items.COOKED_PORKCHOP,
                Items.CHICKEN, Items.COOKED_CHICKEN,
                Items.RABBIT, Items.COOKED_RABBIT,
                Items.MUTTON, Items.COOKED_MUTTON,
                Items.ROTTEN_FLESH
        ));
        BREEDABLE_WHITELIST.put(Cat.class, Set.of(Items.COD, Items.SALMON));
        BREEDABLE_WHITELIST.put(Ocelot.class, Set.of(Items.COD, Items.SALMON));
        BREEDABLE_WHITELIST.put(Rabbit.class, Set.of(Items.DANDELION, Items.CARROT, Items.GOLDEN_CARROT));
        BREEDABLE_WHITELIST.put(Fox.class, Set.of(Items.SWEET_BERRIES, Items.GLOW_BERRIES));
        BREEDABLE_WHITELIST.put(Panda.class, Set.of(Items.BAMBOO));
        BREEDABLE_WHITELIST.put(Sniffer.class, Set.of(Items.MOSS_BLOCK));
        BREEDABLE_WHITELIST.put(Hoglin.class, Set.of(Items.CRIMSON_FUNGUS));
        BREEDABLE_WHITELIST.put(Bee.class, getFlowerItems());
        BREEDABLE_WHITELIST.put(Strider.class, Set.of(Items.WARPED_FUNGUS));
        BREEDABLE_WHITELIST.put(Camel.class, Set.of(Items.CACTUS));
        BREEDABLE_WHITELIST.put(Frog.class, Set.of(Items.SLIME_BALL));
        BREEDABLE_WHITELIST.put(Turtle.class, Set.of(Items.SEAGRASS));
        BREEDABLE_WHITELIST.put(Armadillo.class, Set.of(Items.SPIDER_EYE));
        BREEDABLE_WHITELIST.put(Axolotl.class, Set.of(Items.TROPICAL_FISH_BUCKET));
        BREEDABLE_WHITELIST.put(Allay.class,Set.of(Items.AMETHYST_SHARD));
    }
}
