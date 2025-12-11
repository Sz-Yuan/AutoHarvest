package kite.autoharvest.mode;

import kite.autoharvest.config.AutoHarvestConfig;
import kite.autoharvest.mode.animal.Animals;
import kite.autoharvest.util.*;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class FeedMode implements AutoMode {


    private static final Map<UUID, Long> INTERACT_COOLDOWN = new HashMap<>();

    //受限于无法准确获取服务器中生物的繁殖准确冷却时间，使用交互冷却优化性能
    private static long COOLDOWN_MS() {
        return AutoHarvestConfig.coolDown();
    }

    private static long lastCleanupTime = 0;
    private static final long CLEANUP_INTERVAL_MS = 5_000L;

    private boolean isOnCooldown(Entity entity) {
        Long lastInteract = INTERACT_COOLDOWN.get(entity.getUUID());
        if (lastInteract == null) return false;
        return (System.currentTimeMillis() - lastInteract) < COOLDOWN_MS();
    }

    private void markAsInteracted(Entity entity) {
        INTERACT_COOLDOWN.put(entity.getUUID(), System.currentTimeMillis());
    }

    private void cleanupCooldownCache() {
        long now = System.currentTimeMillis();
        if (now - lastCleanupTime < CLEANUP_INTERVAL_MS) {
            return;
        }
        INTERACT_COOLDOWN.entrySet().removeIf(entry -> now - entry.getValue() >= COOLDOWN_MS());
        lastCleanupTime = now;
    }

    private boolean canBreed(Entity entity) {
        if (entity instanceof Animal animal) {
            return !animal.isBaby();
        }
        return false;
    }

    @Override
    public void tick() {
        var BREEDABLE_WHITELIST = Animals.BREEDABLE_WHITELIST;
        cleanupCooldownCache();

        ClientLevel world = BoxUtil.getWorld();
        LocalPlayer player = BoxUtil.getPlayer();
        if (world == null || player == null) return;

        Vec3 playerPos = BoxUtil.getPlayerPos();
        if (playerPos == null) return;
        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();
        boolean holdingShears = mainHand.is(Items.SHEARS) || offHand.is(Items.SHEARS);

        // 如果手持剪刀：仅剪羊毛，跳过喂食
        if (holdingShears) {
            double radius = AutoHarvestConfig.getInstance().getRadius();
            AABB searchBox = BoxUtil.createSearchBox(playerPos, radius);
            List<Entity> sheepList = world.getEntities(player, searchBox, entity ->
                    entity instanceof Sheep && ((Sheep) entity).readyForShearing()
            );
            sheepList.sort(Comparator.comparingDouble(e -> e.distanceToSqr(player)));

            for (Entity sheep : sheepList) {
                InteractionHand hand = mainHand.is(Items.SHEARS) ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
                InteractionHelper.interactEntity(player, sheep, hand);
                return;
            }
            return;
        }

        boolean enableRefill = AutoHarvestConfig.enableRefill();
        boolean isCreativeOrSpectator = player.isCreative() || player.isSpectator();

        if (!isCreativeOrSpectator && enableRefill) {
            ItemStack main = player.getMainHandItem();
            ItemStack off = player.getOffhandItem();

            if (!main.isEmpty() && isBreedItem(main.getItem()) && main.getCount() < 64) {
                ItemRefillHelpermain.refillHands();
            }
            if (!off.isEmpty() && isBreedItem(off.getItem()) && off.getCount() < 64) {
                ItemRefillHelperoff.refillOffHand();
            }
        }

        double radius = AutoHarvestConfig.getInstance().getRadius();
        AABB searchBox = BoxUtil.createSearchBox(playerPos, radius);

        List<Entity> entities = world.getEntities(player, searchBox, entity -> {
            Set<Item> foods = BREEDABLE_WHITELIST.get(entity.getClass());
            return foods != null && canBreed(entity) && !isOnCooldown(entity);
        });

        entities.sort(Comparator.comparingDouble(e -> e.distanceToSqr(player)));

        for (Entity target : entities) {
            Set<Item> validFoods = BREEDABLE_WHITELIST.get(target.getClass());
            if (validFoods == null || validFoods.isEmpty()) continue;
            if (!canBreed(target)) continue;

            if (tryFeedEntity(player, target, validFoods)) {
                markAsInteracted(target);
                return;
            }
        }
    }

    private boolean isBreedItem(Item item) {
        var BREEDABLE_WHITELIST = Animals.BREEDABLE_WHITELIST;
        for (Set<Item> foods : BREEDABLE_WHITELIST.values()) {
            if (foods != null && foods.contains(item)) {
                return true;
            }
        }
        return false;
    }

    private boolean tryFeedEntity(LocalPlayer player, Entity target, Set<Item> validFoods) {
        ItemStack main = player.getMainHandItem();
        ItemStack off = player.getOffhandItem();

        if (!main.isEmpty() && validFoods.contains(main.getItem())) {
            InteractionHelper.interactEntity(player, target, InteractionHand.MAIN_HAND);
            return true;
        }
        if (!off.isEmpty() && validFoods.contains(off.getItem())) {
            InteractionHelper.interactEntity(player, target, InteractionHand.OFF_HAND);
            return true;
        }

        Item bestFood = findBestFood(player, validFoods);
        if (bestFood == null) {
            return false;
        }

        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.getItem() == bestFood) {
                player.getInventory().setSelectedSlot(i);
                InteractionHelper.interactEntity(player, target, InteractionHand.MAIN_HAND);
                return true;
            }
        }

        return false;
    }

    private Item findBestFood(LocalPlayer player, Set<Item> validFoods) {
        if (validFoods.contains(player.getMainHandItem().getItem())) {
            return player.getMainHandItem().getItem();
        }
        if (validFoods.contains(player.getOffhandItem().getItem())) {
            return player.getOffhandItem().getItem();
        }

        int currentSlot = player.getInventory().getSelectedSlot();
        int bestSlot = -1;
        int minDistance = Integer.MAX_VALUE;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && validFoods.contains(stack.getItem())) {
                int distance = Math.abs(i - currentSlot);
                if (distance < minDistance) {
                    minDistance = distance;
                    bestSlot = i;
                }
            }
        }

        return bestSlot != -1 ? player.getInventory().getItem(bestSlot).getItem() : null;
    }

    @Override
    public String getName() {
        return Component.translatable("autoharvest.mode.feed").getString();
    }

    @Override
    public void onDisable() {
        // 留空
    }
}