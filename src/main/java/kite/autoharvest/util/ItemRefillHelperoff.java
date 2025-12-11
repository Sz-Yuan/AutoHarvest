package kite.autoharvest.util;

import kite.autoharvest.util.whitelist.itemWhiteList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;

public class ItemRefillHelperoff {

    public static void refillOffHand() {
        Minecraft client = Minecraft.getInstance();

        client.execute(() -> doRefillOffHand(client));
    }

    private static void doRefillOffHand(Minecraft client) {
        if (client.player == null || client.level == null) return;

        if (client.screen != null && !(client.screen instanceof InventoryScreen)) {
            return;
        }

        LocalPlayer player = client.player;
        Inventory inv = player.getInventory();

        refillOffHand(player, inv);
    }

    private static void refillOffHand(LocalPlayer player, Inventory inv) {
        var WHITELIST = itemWhiteList.WHITELIST;

        ItemStack offHandStack = player.getOffhandItem();

        if (offHandStack.isEmpty() || !WHITELIST.contains(offHandStack.getItem())) {
            return;
        }

        int sourcePlayerSlot = findMatchingStackInInventory(inv, offHandStack);
        if (sourcePlayerSlot == -1) return;

        var handler = player.containerMenu;

        final int OFF_HAND_SCREEN_SLOT = 45;

        int screenSourceSlot = sourcePlayerSlot;
        if (sourcePlayerSlot >= 0 && sourcePlayerSlot <= 8) {
            screenSourceSlot = sourcePlayerSlot + 36; // 快捷栏映射到 36-44
        }

        clickSlot(handler, screenSourceSlot);

        ItemStack cursorAfterPickup = handler.getCarried();
        if (cursorAfterPickup.isEmpty()) {
            return;
        }

        clickSlot(handler, OFF_HAND_SCREEN_SLOT);

        if (!handler.getCarried().isEmpty()) {
            clickSlot(handler, screenSourceSlot);
        }
    }

    private static void clickSlot(AbstractContainerMenu handler, int slotIndex) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;

        if (client.gameMode != null) {
            client.gameMode.handleInventoryMouseClick(
                    handler.containerId,
                    slotIndex,
                    0,
                    ClickType.PICKUP,
                    client.player
            );
        }
    }

    private static int findMatchingStackInInventory(Inventory inv, ItemStack targetStack) {
        for (int i = 0; i < 36; i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty() && ItemStack.isSameItem(stack, targetStack)) {
                return i;
            }
        }
        return -1;
    }
}