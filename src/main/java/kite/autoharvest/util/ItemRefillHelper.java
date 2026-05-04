package kite.autoharvest.util;

import kite.autoharvest.util.whitelist.itemWhiteList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;

public class ItemRefillHelper {

    private static final int OFF_HAND_SCREEN_SLOT = 45;

    public static void refillMainHand() {
        Minecraft client = Minecraft.getInstance();
        client.execute(() -> doRefill(client, HandType.MAIN_HAND));
    }

    public static void refillOffHand() {
        Minecraft client = Minecraft.getInstance();
        client.execute(() -> doRefill(client, HandType.OFF_HAND));
    }

    private static void doRefill(Minecraft client, HandType handType) {
        if (client.player == null || client.level == null) return;

        if (client.screen != null && !(client.screen instanceof InventoryScreen)) {
            return;
        }

        LocalPlayer player = client.player;
        Inventory inv = player.getInventory();

        refill(player, inv, handType);
    }

    private static void refill(LocalPlayer player, Inventory inv, HandType handType) {
        var WHITELIST = itemWhiteList.WHITELIST;

        ItemStack targetStack;
        int excludeSlot = -1;

        if (handType == HandType.MAIN_HAND) {
            targetStack = player.getMainHandItem();
            excludeSlot = inv.getSelectedSlot();
        } else {
            targetStack = player.getOffhandItem();
        }

        if (targetStack.isEmpty() || !WHITELIST.contains(targetStack.getItem())) {
            return;
        }

        int sourcePlayerSlot = findMatchingStackInInventory(inv, targetStack, excludeSlot);
        if (sourcePlayerSlot == -1) return;

        var handler = player.containerMenu;

        int screenSourceSlot = convertToScreenSlot(sourcePlayerSlot);
        int screenTargetSlot = handType == HandType.MAIN_HAND
                ? convertToScreenSlot(inv.getSelectedSlot())
                : OFF_HAND_SCREEN_SLOT;

        clickSlot(handler, screenSourceSlot);

        ItemStack cursorAfterPickup = handler.getCarried();
        if (cursorAfterPickup.isEmpty()) {
            return;
        }

        clickSlot(handler, screenTargetSlot);

        if (!handler.getCarried().isEmpty()) {
            clickSlot(handler, screenSourceSlot);
        }
    }

    private static int convertToScreenSlot(int playerSlot) {
        if (playerSlot >= 0 && playerSlot <= 8) {
            return playerSlot + 36;
        }
        return playerSlot;
    }

    private static void clickSlot(AbstractContainerMenu handler, int slotIndex) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;

        if (client.gameMode != null) {
            client.gameMode.handleContainerInput(
                    handler.containerId,
                    slotIndex,
                    0,
                    ContainerInput.PICKUP,
                    client.player
            );
        }
    }

    private static int findMatchingStackInInventory(Inventory inv, ItemStack targetStack, int excludeSlot) {
        for (int i = 0; i < 36; i++) {
            if (i == excludeSlot) continue;
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty() && ItemStack.isSameItem(stack, targetStack)) {
                return i;
            }
        }
        return -1;
    }

    private enum HandType {
        MAIN_HAND,
        OFF_HAND
    }
}
