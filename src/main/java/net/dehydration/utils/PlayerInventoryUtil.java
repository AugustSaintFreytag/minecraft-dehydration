package net.dehydration.utils;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public final class PlayerInventoryUtil {

	public static void giveItemStackToPlayer(PlayerEntity player, ItemStack itemStack) {
		if (!player.getInventory().insertStack(itemStack)) {
			player.dropItem(itemStack, false);
		}
	}

}
