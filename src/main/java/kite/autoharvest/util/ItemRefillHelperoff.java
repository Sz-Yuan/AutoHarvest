package kite.autoharvest.util;

import kite.autoharvest.util.whitelist.itemWhiteList;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;

public class ItemRefillHelperoff {

    public static void refillOffHand() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) return;

        client.execute(() -> doRefillOffHand(client));
    }

    private static void doRefillOffHand(MinecraftClient client) {
        if (client.player == null || client.world == null) return;

        if (client.currentScreen != null && !(client.currentScreen instanceof InventoryScreen)) {
            return;
        }

        ClientPlayerEntity player = client.player;
        PlayerInventory inv = player.getInventory();

        refillOffHand(player, inv);
    }

    private static void refillOffHand(ClientPlayerEntity player, PlayerInventory inv) {
        var WHITELIST = itemWhiteList.WHITELIST;

        ItemStack offHandStack = player.getOffHandStack();

        if (offHandStack.isEmpty() || !WHITELIST.contains(offHandStack.getItem())) {
            return;
        }

        int sourcePlayerSlot = findMatchingStackInInventory(inv, offHandStack);
        if (sourcePlayerSlot == -1) return;

        var handler = player.currentScreenHandler;

        final int OFF_HAND_SCREEN_SLOT = 45;

        int screenSourceSlot = sourcePlayerSlot;
        if (sourcePlayerSlot >= 0 && sourcePlayerSlot <= 8) {
            screenSourceSlot = sourcePlayerSlot + 36; // 快捷栏映射到 36-44
        }

        clickSlot(handler, screenSourceSlot);

        ItemStack cursorAfterPickup = handler.getCursorStack();
        if (cursorAfterPickup.isEmpty()) {
            return;
        }

        clickSlot(handler, OFF_HAND_SCREEN_SLOT);

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

    private static int findMatchingStackInInventory(PlayerInventory inv, ItemStack targetStack) {
        for (int i = 0; i < 36; i++) {
            ItemStack stack = inv.getStack(i);
            if (!stack.isEmpty() && ItemStack.areItemsEqual(stack, targetStack)) {
                return i;
            }
        }
        return -1;
    }
}