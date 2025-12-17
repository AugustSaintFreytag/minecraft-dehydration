package net.dehydration.mod;

import net.dehydration.effect.HydrationEffect;
import net.dehydration.effect.ThirstEffect;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModEffects {

	public final static StatusEffect THIRST = new ThirstEffect(StatusEffectCategory.HARMFUL, 3062757);
	public final static StatusEffect HYDRATION = new HydrationEffect(StatusEffectCategory.BENEFICIAL, 3062757);

	public static void init() {
		Registry.register(Registries.STATUS_EFFECT, new Identifier("dehydration", "thirst"), THIRST);
		Registry.register(Registries.STATUS_EFFECT, new Identifier("dehydration", "hydration"), HYDRATION);
	}

}
