package net.dehydration.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.authlib.GameProfile;

import net.dehydration.access.HydrationManagerAccess;
import net.dehydration.access.ServerPlayerAccess;
import net.dehydration.hydration.HydrationManager;
import net.dehydration.network.HydrationServerPacket;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity implements ServerPlayerAccess {

	private HydrationManager hydrationManager = ((HydrationManagerAccess) this).getHydrationManager();
	private int syncedThirstLevel = -99999999;
	public int compatSync = 0;

	public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
		super(world, pos, yaw, gameProfile);
	}

	@Inject(method = "playerTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;tick()V", shift = Shift.AFTER))
	public void playerTickMixin(CallbackInfo info) {
		if (this.syncedThirstLevel != this.hydrationManager.getHydrationLevel() && this.hydrationManager.hasThirst()) {
			HydrationServerPacket.writeS2CThirstUpdatePacket((ServerPlayerEntity) (Object) this);
			this.syncedThirstLevel = hydrationManager.getHydrationLevel();
		}
		if (compatSync > 0) {
			compatSync--;

			if (compatSync == 1) {
				HydrationServerPacket.writeS2CExcludedSyncPacket((ServerPlayerEntity) (Object) this, hydrationManager.hasThirst());
			}
		}
	}

	@Inject(method = "Lnet/minecraft/server/network/ServerPlayerEntity;copyFrom(Lnet/minecraft/server/network/ServerPlayerEntity;Z)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;setHealth(F)V"))
	public void copyFromMixinOne(ServerPlayerEntity oldPlayer, boolean alive, CallbackInfo info) {
		this.hydrationManager.setHydrationLevel(((HydrationManagerAccess) oldPlayer).getHydrationManager().getHydrationLevel());
	}

	@Inject(method = "Lnet/minecraft/server/network/ServerPlayerEntity;copyFrom(Lnet/minecraft/server/network/ServerPlayerEntity;Z)V", at = @At(value = "TAIL"))
	public void copyFromMixinTwo(ServerPlayerEntity oldPlayer, boolean alive, CallbackInfo info) {
		this.syncedThirstLevel = -1;
	}

	@Inject(method = "Lnet/minecraft/server/network/ServerPlayerEntity;teleport(Lnet/minecraft/server/world/ServerWorld;DDDFF)V", at = @At("TAIL"))
	private void teleportMixin(ServerWorld targetWorld, double x, double y, double z, float yaw, float pitch, CallbackInfo info) {
		this.syncedThirstLevel = -1;
	}

	@Nullable @Inject(method = "moveToWorld", at = @At(value = "FIELD", target = "Lnet/minecraft/server/network/ServerPlayerEntity;syncedFoodLevel:I", ordinal = 0))
	private void moveToWorldMixin(ServerWorld destination, CallbackInfoReturnable<Entity> info) {
		this.syncedThirstLevel = -1;
	}

	@Override
	public void compatSync() {
		compatSync = 5;
	}

}
