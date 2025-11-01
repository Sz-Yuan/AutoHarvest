package kite.autoharvest.util;

import kite.autoharvest.util.whitelist.itemWhiteList;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;

public class ItemRefillHelpermain {
    public static void refillHands() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) return;

        client.execute(() -> doRefillHands(client));
    }

    private static void doRefillHands(MinecraftClient client) {
        if (client.player == null || client.world == null) return;

        if (client.currentScreen != null && !(client.currentScreen instanceof InventoryScreen)) {
            return;
        }

        ClientPlayerEntity player = client.player;
        PlayerInventory inv = player.getInventory();

        refillHand(player, inv);
    }

    private static void refillHand(ClientPlayerEntity player, PlayerInventory inv) {
        var WHITELIST = itemWhiteList.WHITELIST;
        int hotbarSlot = inv.getSelectedSlot();
        ItemStack currentStack = player.getMainHandStack();

        if (currentStack.isEmpty() || !WHITELIST.contains(currentStack.getItem())) {
            return;
        }

        int sourcePlayerSlot = findMatchingStackInInventory(inv, currentStack, hotbarSlot);
        if (sourcePlayerSlot == -1) return;

        var handler = player.currentScreenHandler;

        int screenHotbarSlot = hotbarSlot < 9 ? hotbarSlot + 36 : hotbarSlot; // 快捷栏 0-8 → GUI 36-44

        int screenSourceSlot = sourcePlayerSlot;
        if (sourcePlayerSlot >= 0 && sourcePlayerSlot <= 8) {
            screenSourceSlot = sourcePlayerSlot + 36;
        }

        clickSlot(handler, screenSourceSlot);

        ItemStack cursorAfterPickup = handler.getCursorStack();
        if (cursorAfterPickup.isEmpty()) {
            return;
        }

        clickSlot(handler, screenHotbarSlot);

        if (!handler.getCursorStack().isEmpty()) {
            clickSlot(handler, screenSourceSlot);
        }
    }

    private static void clickSlot(net.minecraft.screen.ScreenHandler handler, int slotIndex) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        if (client.interactionManager != null) {
            client.interactionManager.clickSlot(
                    handler.syncId,
                    slotIndex,
                    0,
                    SlotActionType.PICKUP,
                    client.player
            );
        }
    }

    private static int findMatchingStackInInventory(PlayerInventory inv, ItemStack targetStack, int excludePlayerSlot) {
        for (int i = 0; i < 36; i++) {
            if (i == excludePlayerSlot) continue;
            ItemStack stack = inv.getStack(i);
            if (!stack.isEmpty() && ItemStack.areItemsEqual(stack, targetStack)) {
                return i;
            }
        }
        return -1;
    }
}
