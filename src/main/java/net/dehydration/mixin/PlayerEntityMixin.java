package net.dehydration.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.dehydration.access.HydrationManagerAccess;
import net.dehydration.access.PlayerAccess;
import net.dehydration.hydration.HydrationManager;
import net.dehydration.mod.ModConfig;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity implements HydrationManagerAccess, PlayerAccess {

	private HydrationManager hydrationManager = new HydrationManager();

	@Override
	public HydrationManager getHydrationManager() {
		return this.hydrationManager;
	}

	@Shadow
	protected HungerManager hungerManager = new HungerManager();

	@Shadow
	private int sleepTimer;

	private int drinkTime = 0;

	public PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
		super(entityType, world);
	}

	@Inject(method = "Lnet/minecraft/entity/player/PlayerEntity;tick()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/HungerManager;update(Lnet/minecraft/entity/player/PlayerEntity;)V", shift = Shift.AFTER))
	private void tickMixin(CallbackInfo info) {
		if (this.hydrationManager.hasThirst()) {
			this.hydrationManager.update((PlayerEntity) (Object) this);
		}
	}

	@Inject(method = "Lnet/minecraft/entity/player/PlayerEntity;tickMovement()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerInventory;updateItems()V", shift = Shift.BEFORE))
	private void tickMovementMixin(CallbackInfo info) {
		if (this.getWorld().getDifficulty() == Difficulty.PEACEFUL
				&& this.getWorld().getGameRules().getBoolean(GameRules.NATURAL_REGENERATION)
				&& this.hydrationManager.hasThirst()) {
			PlayerEntity player = (PlayerEntity) (Object) this;

			this.hydrationManager.update(player);

			if (this.hydrationManager.isNotFull() && this.age % 10 == 0) {
				this.hydrationManager.setHydrationLevel(this.hydrationManager.getHydrationLevel() + 1);
			}
		}
	}

	@Inject(method = "readCustomDataFromNbt", at = @At(value = "TAIL"))
	private void readCustomDataFromTagMixin(NbtCompound tag, CallbackInfo info) {
		this.hydrationManager.readNbt(tag);
	}

	@Inject(method = "writeCustomDataToNbt", at = @At(value = "TAIL"))
	private void writeCustomDataToTagMixin(NbtCompound tag, CallbackInfo info) {
		this.hydrationManager.writeNbt(tag);
	}

	@Inject(method = "Lnet/minecraft/entity/player/PlayerEntity;addExhaustion(F)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/HungerManager;addExhaustion(F)V", shift = Shift.AFTER))
	private void addExhaustionMixin(float exhaustion, CallbackInfo info) {
		if (this.hydrationManager.hasThirst()) {
			if (ModConfig.CONFIG.increasedDehydrationInHotBiomes && this.getWorld().getDimension().ultrawarm()) {
				exhaustion *= ModConfig.CONFIG.increasedDehydrationFactor;
			}
			this.hydrationManager.addDehydration(exhaustion / ModConfig.CONFIG.dehydrationExhaustionRate);
		}
	}

	@Inject(method = "Lnet/minecraft/entity/player/PlayerEntity;wakeUp(ZZ)V", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/player/PlayerEntity;sleepTimer:I"))
	private void wakeUpMixin(boolean bl, boolean updateSleepingPlayers, CallbackInfo info) {
		if (this.getWorld().isClient()) {
			return;
		}

		var thirstConsumption = ModConfig.CONFIG.dehydrationAfterSleeping;
		var hungerConsumption = ModConfig.CONFIG.hungerWhenSleeping;

		if (this.hydrationManager.hasThirst() && thirstConsumption > 0) {
			var thirstLevel = this.hydrationManager.getHydrationLevel();
			this.hydrationManager
					.setHydrationLevel(thirstLevel >= thirstConsumption ? thirstLevel - thirstConsumption : 0);
		}

		if (hungerConsumption > 0) {
			var hungerLevel = this.hungerManager.getFoodLevel();
			this.hungerManager.setFoodLevel(hungerLevel >= hungerConsumption ? hungerLevel - hungerConsumption : 0);
		}
	}

	@Override
	public void setDrinkTime(int time) {
		this.drinkTime = time;
	}

	@Override
	public int getDrinkTime() {
		return this.drinkTime;
	}

}
