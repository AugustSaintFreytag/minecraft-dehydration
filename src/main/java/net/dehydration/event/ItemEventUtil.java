package net.dehydration.event;

import net.dehydration.Mod;
import net.dehydration.hydration.HydrationUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class ItemEventUtil {

	public static TypedActionResult<ItemStack> handleUseItemEvent(PlayerEntity player, World world, Hand hand) {
		var itemStack = player.getStackInHand(hand);
		var isUsingItem = player.isUsingItem();
		var itemUseTimeLeft = player.getItemUseTimeLeft();
		var itemUseTime = player.getItemUseTime();
		var playerItemUseTime = itemStack.getMaxUseTime();

		Mod.LOGGER.info("Using item '" + itemStack.getTranslationKey() + "', client: " + world.isClient + ", using: " + isUsingItem
				+ ", use time left: " + itemUseTimeLeft + ", item use time: " + itemUseTime + ", player item use time: " + playerItemUseTime
				+ ".");

		if (!(isUsingItem && itemUseTimeLeft == 0)) {
			return TypedActionResult.pass(itemStack);
		}

		int itemHydrationValue = HydrationUtil.getHydrationValueForItemStack(itemStack);

		if (world.isClient) {
			Mod.LOGGER.info("Item finished using (client-side): " + itemStack.getTranslationKey() + " with hydration value "
					+ itemHydrationValue + ".");
			return TypedActionResult.pass(itemStack);
		}

		Mod.LOGGER.info("Item finished using (server-side): " + itemStack.getTranslationKey() + " with hydration value "
				+ itemHydrationValue + ".");

		return TypedActionResult.pass(itemStack);
	}

}
