package net.dehydration.misc;

import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;

public class PotionItemUtil {

	public static boolean isBrewedPotionItemStack(ItemStack stack) {
		Potion potion = PotionUtil.getPotion(stack);
		return isBrewedPotionItem(potion);
	}

	public static boolean isBrewedPotionItem(Potion potion) {
		if (potion != Potions.EMPTY && potion != Potions.WATER) {
			return true;
		}

		return false;
	}

}
