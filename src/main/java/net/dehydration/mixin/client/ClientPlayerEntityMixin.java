package net.dehydration.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.authlib.GameProfile;

import net.dehydration.access.HydrationManagerAccess;
import net.dehydration.hydration.HydrationManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;

@Environment(EnvType.CLIENT) @Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin extends AbstractClientPlayerEntity {

	public ClientPlayerEntityMixin(ClientWorld world, GameProfile profile) {
		super(world, profile);
	}

	@Inject(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;setSprinting(Z)V", shift = Shift.AFTER))
	public void tickMovementMixin(CallbackInfo info) {
		HydrationManager hydrationManager = ((HydrationManagerAccess) this).getHydrationManager();
		if (hydrationManager.hasThirst() && !this.isCreative() && hydrationManager.getHydrationLevel() < 6) {
			this.setSprinting(false);
		}
	}

}
