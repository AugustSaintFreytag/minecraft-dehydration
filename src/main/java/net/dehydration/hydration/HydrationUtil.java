package net.dehydration.hydration;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import net.dehydration.access.HydrationManagerAccess;
import net.dehydration.item.LeatherFlaskItem;
import net.dehydration.item.PurifiedWaterBottleItem;
import net.dehydration.item.PurifiedWaterBucketItem;
import net.dehydration.misc.PotionItemUtil;
import net.dehydration.mixin.accessor.BucketItemAccessor;
import net.dehydration.mod.ModConfig;
import net.dehydration.mod.ModEffects;
import net.dehydration.mod.ModTags;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PotionItem;
import net.minecraft.item.SplashPotionItem;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.UseAction;

public class HydrationUtil {

	// Properties

	private static final Map<TagKey<Item>, Integer> tagToThirstQuenchMap = new HashMap<>();

	static {
		tagToThirstQuenchMap.put(ModTags.NON_HYDRATING_ITEMS, 0);
		tagToThirstQuenchMap.put(ModTags.HYDRATING_DRINKS_WEAK, ModConfig.CONFIG.drinksWeakHydrationValue);
		tagToThirstQuenchMap.put(ModTags.HYDRATING_DRINKS_REGULAR, ModConfig.CONFIG.drinksRegularHydrationValue);
		tagToThirstQuenchMap.put(ModTags.HYDRATING_DRINKS_STRONG, ModConfig.CONFIG.drinksStrongHydrationValue);
		tagToThirstQuenchMap.put(ModTags.HYDRATING_FOOD_WEAK, ModConfig.CONFIG.foodWeakHydrationValue);
		tagToThirstQuenchMap.put(ModTags.HYDRATING_FOOD_REGULAR, ModConfig.CONFIG.foodRegularHydrationValue);
		tagToThirstQuenchMap.put(ModTags.HYDRATING_FOOD_STRONG, ModConfig.CONFIG.foodStrongHydrationValue);
	}

	// Data

	public static int getHydrationValueForItemStack(ItemStack stack) {
		for (Map.Entry<TagKey<Item>, Integer> entry : tagToThirstQuenchMap.entrySet()) {
			if (stack.isIn(entry.getKey())) {
				return entry.getValue();
			}
		}

		var useAction = stack.getItem().getUseAction(stack);

		if (useAction != UseAction.DRINK && useAction != UseAction.EAT) {
			return 0;
		}

		if (isKnownItemStack(stack)) {
			return getItemHydrationValueForKnownItemStack(stack);
		}

		if (ModConfig.CONFIG.enableAutoInferredHydrationValues) {
			return getAutoInferredHydrationValueForItemStack(stack);
		}

		return 0;
	}

	// Known Items

	private static boolean isKnownItemStack(ItemStack stack) {
		var item = stack.getItem();
		return item instanceof PurifiedWaterBottleItem || item instanceof PurifiedWaterBucketItem || item instanceof LeatherFlaskItem
				|| item instanceof PotionItem;
	}

	private static int getItemHydrationValueForKnownItemStack(ItemStack stack) {
		var item = stack.getItem();

		if (item instanceof PurifiedWaterBucketItem) {
			return ModConfig.CONFIG.drinksStrongHydrationValue;
		}

		if (item instanceof PurifiedWaterBottleItem) {
			return ModConfig.CONFIG.drinksRegularHydrationValue;
		}

		if (item instanceof PotionItem && !(item instanceof SplashPotionItem)) {
			// Return weak hydration value for all brewed potions.
			return ModConfig.CONFIG.drinksWeakHydrationValue;
		}

		if (item instanceof LeatherFlaskItem) {
			// Check if flask is empty, return no hydration value if not filled.
			if (LeatherFlaskItem.isFlaskEmpty(stack)) {
				return 0;
			}

			return ModConfig.CONFIG.flaskHydrationValue;
		}

		return 0;
	}

	// Auto Inference

	private static int getAutoInferredHydrationValueForItemStack(ItemStack stack) {
		var item = stack.getItem();
		var itemUseAction = item.getUseAction(stack);
		var itemHasDrinkUseAction = itemUseAction == UseAction.DRINK;
		var itemHasEatUseAction = itemUseAction == UseAction.EAT;

		var key = item.getTranslationKey();

		if (!item.isFood() && !itemHasDrinkUseAction) {
			return 0;
		}

		// Weak Drinks

		if (itemHasDrinkUseAction && matchesAny(key, "milk", "milkshake", "smoothie", "hot_cocoa")) {
			return ModConfig.CONFIG.drinksWeakHydrationValue;
		}

		// Regular Drinks

		if (itemHasDrinkUseAction && matchesAny(key, "water", "tea", "coffee") && !matchesAny(key, "tea_leaves", "coffee_beans", "cake")) {
			return ModConfig.CONFIG.drinksRegularHydrationValue;
		}

		// Strong Drinks

		if (itemHasDrinkUseAction && matchesAny(key, "juice", "cider")) {
			return ModConfig.CONFIG.drinksStrongHydrationValue;
		}

		// Weak Food

		if (itemHasEatUseAction && matchesAny(key, "popsicle", "ice_cream", "sorbet")) {
			return ModConfig.CONFIG.foodWeakHydrationValue;
		}

		// Regular Food

		if (itemHasEatUseAction && matchesAny(key, "soup", "stew")) {
			return ModConfig.CONFIG.foodRegularHydrationValue;
		}

		return 0;
	}

	private static boolean matches(String key, String component) {
		var regex = "_" + component + "|" + component + "_";
		var pattern = Pattern.compile(regex);
		var matcher = pattern.matcher(key);

		return matcher.find();
	}

	private static boolean matchesAny(String key, String... components) {
		for (var component : components) {
			if (matches(key, component)) {
				return true;
			}
		}

		return false;
	}

	// Effects

	public static boolean isContaminatedItemStack(ItemStack stack) {
		var item = stack.getItem();

		if (item instanceof LeatherFlaskItem) {
			return LeatherFlaskItem.isFlaskContaminated(stack);
		}

		if (item instanceof PotionItem && !(item instanceof SplashPotionItem)) {
			return !PotionItemUtil.isBrewedPotionItemStack(stack);
		}

		if (item instanceof PurifiedWaterBottleItem || item instanceof PurifiedWaterBucketItem) {
			return false;
		}

		if (item instanceof BucketItem) {
			var fluid = ((BucketItemAccessor) (Object) item).dehydration$getFluid();

			if (fluid == Fluids.WATER) {
				return true;
			}
		}

		return false;
	}

	public static void addHydrationToPlayerForItemStack(PlayerEntity player, ItemStack stack) {
		var hydrationValue = getHydrationValueForItemStack(stack);
		addHydrationToPlayer(player, hydrationValue);
	}

	public static void addHydrationToPlayer(PlayerEntity player, int value) {
		if (value <= 0) {
			return;
		}

		var hydrationManager = ((HydrationManagerAccess) player).getHydrationManager();
		hydrationManager.add(value);
	}

	public static void addDefaultThirstEffectToPlayer(PlayerEntity player) {
		player.addStatusEffect(new StatusEffectInstance(ModEffects.THIRST, ModConfig.CONFIG.thirstEffectDuration, 1, false, false, true));
	}

}
