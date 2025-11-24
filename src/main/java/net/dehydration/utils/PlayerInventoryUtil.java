package net.dehydration.utils;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public final class PlayerInventoryUtil {

	public static void giveItemStackToPlayer(PlayerEntity player, ItemStack itemStack) {
		var activeHand = player.getActiveHand();

		if (activeHand != null && player.getStackInHand(activeHand).isEmpty()) {
			player.setStackInHand(activeHand, itemStack);
			return;
		}

		if (!player.getInventory().insertStack(itemStack)) {
			player.dropItem(itemStack, false);
		}
	}

}
