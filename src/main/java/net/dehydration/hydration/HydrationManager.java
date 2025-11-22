package net.dehydration.hydration;

import net.dehydration.mod.ModConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.Difficulty;

public class HydrationManager {

	// Damage Type

	public static final RegistryKey<DamageType> THIRST = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, new Identifier("dehydration", "thirst"));

	// Properties

	public float dehydration;

	private boolean hasThirst = true;

	private int dehydrationTimer;

	private int hydrationLevel = 20;

	// Modifications

	public void add(int hydration) {
		this.hydrationLevel = Math.max(0, Math.min(hydration + this.hydrationLevel, 20));
	}

	public void update(PlayerEntity player) {
		Difficulty difficulty = player.getWorld().getDifficulty();

		if (this.dehydration > 4.0F) {
			this.dehydration -= 4.0F;
			if (difficulty != Difficulty.PEACEFUL) {
				this.hydrationLevel = Math.max(this.hydrationLevel - 1, 0);
			}
		}

		if (this.hydrationLevel <= 0) {
			++this.dehydrationTimer;

			if (this.dehydrationTimer >= 90) {
				if (ModConfig.CONFIG.dehydrationDepletionDamage > 0 && player.getHealth() > 10.0F || difficulty == Difficulty.HARD
						|| (player.getHealth() > 1.0F && difficulty == Difficulty.NORMAL)) {
					player.damage(createDamageSource(player), ModConfig.CONFIG.dehydrationDepletionDamage);
				}

				this.dehydrationTimer = 0;
			}
		} else {
			this.dehydrationTimer = 0;
		}

		if (!player.isCreative() && ModConfig.CONFIG.extraDehydrationEffects) {
			if (hydrationLevel == 2 && !player.hasStatusEffect(StatusEffects.HASTE)) {
				player.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, 409, 0, false, false, false));
			}

			if (hydrationLevel == 0 && player.getHungerManager().getFoodLevel() == 0
					&& !player.hasStatusEffect(StatusEffects.MINING_FATIGUE)) {
				player.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 409, 2, false, false, false));
			}
		}

	}

	// NBT

	public void readNbt(NbtCompound tag) {
		if (tag.contains("ThirstLevel", 99)) {
			this.hydrationLevel = tag.getInt("ThirstLevel");
			this.dehydrationTimer = tag.getInt("ThirstTickTimer");
			this.dehydration = tag.getFloat("ThirstExhaustionLevel");
			this.hasThirst = tag.getBoolean("HasThirst");
		}
	}

	public void writeNbt(NbtCompound tag) {
		tag.putInt("ThirstLevel", this.hydrationLevel);
		tag.putInt("ThirstTickTimer", this.dehydrationTimer);
		tag.putFloat("ThirstExhaustionLevel", this.dehydration);
		tag.putBoolean("HasThirst", this.hasThirst);
	}

	// Accessors

	public int getHydrationLevel() {
		return this.hydrationLevel;
	}

	public boolean isNotFull() {
		return this.hydrationLevel < 20;
	}

	public void addDehydration(float dehydration) {
		this.dehydration = Math.min(this.dehydration + dehydration, 40.0F);
	}

	public void setHydrationLevel(int thirstLevel) {
		this.hydrationLevel = thirstLevel;
	}

	public boolean hasThirst() {
		return this.hasThirst;
	}

	public void setThirst(boolean canHaveThirst) {
		this.hasThirst = canHaveThirst;
	}

	private DamageSource createDamageSource(Entity entity) {
		return entity.getDamageSources().create(THIRST, null);
	}

}
