package net.dehydration.effect;

import net.dehydration.access.HydrationManagerAccess;
import net.dehydration.hydration.HydrationManager;
import net.dehydration.mod.ModConfig;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.player.PlayerEntity;

public class ThirstEffect extends StatusEffect {

	public ThirstEffect(StatusEffectCategory type, int color) {
		super(type, color);
	}

	@Override
	public void applyUpdateEffect(LivingEntity entity, int amplifier) {
		if (entity instanceof PlayerEntity playerEntity) {
			HydrationManager hydrationManager = ((HydrationManagerAccess) playerEntity).getHydrationManager();
			hydrationManager.addDehydration(0.01F * ModConfig.CONFIG.thirstEffectAmplitude * (float) (amplifier + 1));
		}
	}

	@Override
	public boolean canApplyUpdateEffect(int duration, int amplifier) {
		return true;
	}

}
