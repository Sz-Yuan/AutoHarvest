package kite.autoharvest.util;

import kite.autoharvest.util.whitelist.itemWhiteList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;

public class ItemRefillHelpermain {
    public static void refillHands() {
        Minecraft client = Minecraft.getInstance();

        client.execute(() -> doRefillHands(client));
    }

    private static void doRefillHands(Minecraft client) {
        if (client.player == null || client.level == null) return;

        if (client.screen != null && !(client.screen instanceof InventoryScreen)) {
            return;
        }

        LocalPlayer player = client.player;
        Inventory inv = player.getInventory();

        refillHand(player, inv);
    }

    private static void refillHand(LocalPlayer player, Inventory inv) {
        var WHITELIST = itemWhiteList.WHITELIST;
        int hotbarSlot = inv.getSelectedSlot();
        ItemStack currentStack = player.getMainHandItem();

        if (currentStack.isEmpty() || !WHITELIST.contains(currentStack.getItem())) {
            return;
        }

        int sourcePlayerSlot = findMatchingStackInInventory(inv, currentStack, hotbarSlot);
        if (sourcePlayerSlot == -1) return;

        var handler = player.containerMenu;

        int screenHotbarSlot = hotbarSlot < 9 ? hotbarSlot + 36 : hotbarSlot; // 快捷栏 0-8 → GUI 36-44

        int screenSourceSlot = sourcePlayerSlot;
        if (sourcePlayerSlot >= 0 && sourcePlayerSlot <= 8) {
            screenSourceSlot = sourcePlayerSlot + 36;
        }

        clickSlot(handler, screenSourceSlot);

        ItemStack cursorAfterPickup = handler.getCarried();
        if (cursorAfterPickup.isEmpty()) {
            return;
        }

        clickSlot(handler, screenHotbarSlot);

        if (!handler.getCarried().isEmpty()) {
            clickSlot(handler, screenSourceSlot);
        }
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

    private static int findMatchingStackInInventory(Inventory inv, ItemStack targetStack, int excludePlayerSlot) {
        for (int i = 0; i < 36; i++) {
            if (i == excludePlayerSlot) continue;
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty() && ItemStack.isSameItem(stack, targetStack)) {
                return i;
            }
        }
        return -1;
    }
}
