package net.dehydration.api;

import net.dehydration.access.HydrationManagerAccess;
import net.dehydration.hydration.HydrationManager;
import net.dehydration.hydration.HydrationUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

/**
 * The {@link DehydrationAPI} interface, when implemented, allows you to access the main functions
 * provided by the API. The approach is very similar to ModMenu's API, with its {@code ModMenuApi}
 * interface.
 */
public interface DehydrationAPI {

	default void registerDrinkEvent() {
		DrinkEvent.EVENT.register(this::onDrink);
	}

	default void onDrink(ItemStack stack, PlayerEntity player) {
		HydrationManager hydrationManager = ((HydrationManagerAccess) player).getHydrationManager();

		int hydrationValue = getHydrationForItemStack(stack, player);
		hydrationManager.add(hydrationValue);
	}

	default int getHydrationForItemStack(ItemStack stack, PlayerEntity player) {
		return HydrationUtil.getHydrationValueForItemStack(stack);
	}

	default boolean getIsItemStackContaminated(ItemStack stack) {
		return HydrationUtil.isContaminatedItemStack(stack);
	}

}
