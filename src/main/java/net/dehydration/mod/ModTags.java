package net.dehydration.mod;

import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public class ModTags {

	// Generics

	public static final TagKey<Item> NON_HYDRATING_ITEMS = TagKey.of(RegistryKeys.ITEM,
			new Identifier("dehydration", "non_hydrating_items"));

	// Drinks

	public static final TagKey<Item> HYDRATING_DRINKS_WEAK = TagKey.of(RegistryKeys.ITEM,
			new Identifier("dehydration", "hydrating_drinks_weak"));

	public static final TagKey<Item> HYDRATING_DRINKS_REGULAR = TagKey.of(RegistryKeys.ITEM,
			new Identifier("dehydration", "hydrating_drinks_regular"));

	public static final TagKey<Item> HYDRATING_DRINKS_STRONG = TagKey.of(RegistryKeys.ITEM,
			new Identifier("dehydration", "hydrating_drinks_strong"));

	// Food

	public static final TagKey<Item> HYDRATING_FOOD_WEAK = TagKey.of(RegistryKeys.ITEM,
			new Identifier("dehydration", "hydrating_food_weak"));

	public static final TagKey<Item> HYDRATING_FOOD_REGULAR = TagKey.of(RegistryKeys.ITEM,
			new Identifier("dehydration", "hydrating_food_regular"));

	public static final TagKey<Item> HYDRATING_FOOD_STRONG = TagKey.of(RegistryKeys.ITEM,
			new Identifier("dehydration", "hydrating_food_strong"));

	// Purified Water

	public static final TagKey<Fluid> PURIFIED_WATER = TagKey.of(RegistryKeys.FLUID, new Identifier("dehydration", "purified_water"));

	public static void init() {
	}

}
