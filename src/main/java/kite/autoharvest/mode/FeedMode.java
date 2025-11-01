package kite.autoharvest.mode;

import kite.autoharvest.config.AutoHarvestConfig;
import kite.autoharvest.mode.animal.Animals;
import kite.autoharvest.util.*;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.lang.reflect.Method;
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
        Long lastInteract = INTERACT_COOLDOWN.get(entity.getUuid());
        if (lastInteract == null) return false;
        return (System.currentTimeMillis() - lastInteract) < COOLDOWN_MS();
    }

    private void markAsInteracted(Entity entity) {
        INTERACT_COOLDOWN.put(entity.getUuid(), System.currentTimeMillis());
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
        try {
            Method isBabyMethod = entity.getClass().getMethod("isBaby");
            boolean isBaby = (boolean) isBabyMethod.invoke(entity);
            if (isBaby) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    @Override
    public void tick() {
        var BREEDABLE_WHITELIST = Animals.BREEDABLE_WHITELIST;
        cleanupCooldownCache();

        ClientWorld world = BoxUtil.getWorld();
        ClientPlayerEntity player = BoxUtil.getPlayer();
        if (world == null || player == null) return;

        Vec3d playerPos = BoxUtil.getPlayerPos();
        if (playerPos == null) return;
        ItemStack mainHand = player.getMainHandStack();
        ItemStack offHand = player.getOffHandStack();
        boolean holdingShears = mainHand.isOf(Items.SHEARS) || offHand.isOf(Items.SHEARS);

        // 如果手持剪刀：仅剪羊毛，跳过喂食
        if (holdingShears) {
            double radius = AutoHarvestConfig.getInstance().getRadius();
            Box searchBox = BoxUtil.createSearchBox(playerPos, radius);
            List<Entity> sheepList = world.getOtherEntities(player, searchBox, entity ->
                    entity instanceof SheepEntity && ((SheepEntity) entity).isShearable()
            );
            sheepList.sort(Comparator.comparingDouble(e -> e.squaredDistanceTo(player)));

            for (Entity sheep : sheepList) {
                Hand hand = mainHand.isOf(Items.SHEARS) ? Hand.MAIN_HAND : Hand.OFF_HAND;
                InteractionHelper.interactEntity(player, sheep, hand);
                return;
            }
            return;
        }

        boolean enableRefill = AutoHarvestConfig.enableRefill();
        boolean isCreativeOrSpectator = player.isCreative() || player.isSpectator();

        if (!isCreativeOrSpectator && enableRefill) {
            ItemStack main = player.getMainHandStack();
            ItemStack off = player.getOffHandStack();

            if (!main.isEmpty() && isBreedItem(main.getItem()) && main.getCount() < 64) {
                ItemRefillHelpermain.refillHands();
            }
            if (!off.isEmpty() && isBreedItem(off.getItem()) && off.getCount() < 64) {
                ItemRefillHelperoff.refillOffHand();
            }
        }

        double radius = AutoHarvestConfig.getInstance().getRadius();
        Box searchBox = BoxUtil.createSearchBox(playerPos, radius);

        List<Entity> entities = world.getOtherEntities(player, searchBox, entity -> {
            Set<Item> foods = BREEDABLE_WHITELIST.get(entity.getClass());
            return foods != null && canBreed(entity) && !isOnCooldown(entity);
        });

        entities.sort(Comparator.comparingDouble(e -> e.squaredDistanceTo(player)));

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

    private boolean tryFeedEntity(ClientPlayerEntity player, Entity target, Set<Item> validFoods) {
        ItemStack main = player.getMainHandStack();
        ItemStack off = player.getOffHandStack();

        if (!main.isEmpty() && validFoods.contains(main.getItem())) {
            InteractionHelper.interactEntity(player, target, Hand.MAIN_HAND);
            return true;
        }
        if (!off.isEmpty() && validFoods.contains(off.getItem())) {
            InteractionHelper.interactEntity(player, target, Hand.OFF_HAND);
            return true;
        }

        Item bestFood = findBestFood(player, validFoods);
        if (bestFood == null) {
            return false;
        }

        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (!stack.isEmpty() && stack.getItem() == bestFood) {
                player.getInventory().setSelectedSlot(i);
                InteractionHelper.interactEntity(player, target, Hand.MAIN_HAND);
                return true;
            }
        }

        return false;
    }

    private Item findBestFood(ClientPlayerEntity player, Set<Item> validFoods) {
        if (validFoods.contains(player.getMainHandStack().getItem())) {
            return player.getMainHandStack().getItem();
        }
        if (validFoods.contains(player.getOffHandStack().getItem())) {
            return player.getOffHandStack().getItem();
        }

        int currentSlot = player.getInventory().getSelectedSlot();
        int bestSlot = -1;
        int minDistance = Integer.MAX_VALUE;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (!stack.isEmpty() && validFoods.contains(stack.getItem())) {
                int distance = Math.abs(i - currentSlot);
                if (distance < minDistance) {
                    minDistance = distance;
                    bestSlot = i;
                }
            }
        }

        return bestSlot != -1 ? player.getInventory().getStack(bestSlot).getItem() : null;
    }

    @Override
    public String getName() {
        return Text.translatable("autoharvest.mode.feed").getString();
    }

    @Override
    public void onDisable() {
        // 留空
    }
}