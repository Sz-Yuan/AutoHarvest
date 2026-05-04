package kite.autoharvest.util;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Set;
import java.util.function.Predicate;

public class ItemSlotHelper {

    public static int findNearestSlot(LocalPlayer player, Predicate<ItemStack> matcher, boolean useCircularDistance) {
        int currentSlot = player.getInventory().getSelectedSlot();
        int bestSlot = -1;
        int minDistance = Integer.MAX_VALUE;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && matcher.test(stack)) {
                int distance = calculateDistance(i, currentSlot, useCircularDistance);
                if (distance < minDistance) {
                    minDistance = distance;
                    bestSlot = i;
                }
            }
        }

        return bestSlot;
    }

    public static int findNearestSlot(LocalPlayer player, Item targetItem) {
        return findNearestSlot(player, targetItem, false);
    }

    public static int findNearestSlot(LocalPlayer player, Item targetItem, boolean useCircularDistance) {
        return findNearestSlot(player, stack -> stack.getItem() == targetItem, useCircularDistance);
    }

    public static int findNearestSlot(LocalPlayer player, Set<Item> allowedItems) {
        return findNearestSlot(player, allowedItems, false);
    }

    public static int findNearestSlot(LocalPlayer player, Set<Item> allowedItems, boolean useCircularDistance) {
        return findNearestSlot(player, stack -> allowedItems.contains(stack.getItem()), useCircularDistance);
    }

    public static Item findNearestItem(LocalPlayer player, Set<Item> allowedItems) {
        int slot = findNearestSlot(player, allowedItems);
        return slot != -1 ? player.getInventory().getItem(slot).getItem() : null;
    }

    public static boolean hasItem(LocalPlayer player, Set<Item> allowedItems) {
        if (allowedItems.contains(player.getMainHandItem().getItem())) return true;
        if (allowedItems.contains(player.getOffhandItem().getItem())) return true;
        return findNearestSlot(player, allowedItems) != -1;
    }

    private static int calculateDistance(int slot1, int slot2, boolean circular) {
        int diff = Math.abs(slot1 - slot2);
        if (circular) {
            return Math.min(diff, 9 - diff);
        }
        return diff;
    }
}
