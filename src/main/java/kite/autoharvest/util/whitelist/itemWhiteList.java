package kite.autoharvest.util.whitelist;

import kite.autoharvest.mode.animal.Animals;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class itemWhiteList {
    public static final Set<Item> WHITELIST;

    static {
        Set<Item> combined = new HashSet<>(Arrays.asList(
                Items.WHEAT_SEEDS,
                Items.CARROT,
                Items.POTATO,
                Items.BEETROOT_SEEDS,
                Items.PUMPKIN_SEEDS,
                Items.MELON_SEEDS,
                Items.SUGAR_CANE,
                Items.BAMBOO,
                Items.NETHER_WART,
                Items.BONE_MEAL,
                Items.COCOA_BEANS,
                Items.SWEET_BERRIES
        ));
        for (Set<Item> items : Animals.BREEDABLE_WHITELIST.values()) {
            combined.addAll(items);
        }
        WHITELIST = Collections.unmodifiableSet(combined);
    }
}
