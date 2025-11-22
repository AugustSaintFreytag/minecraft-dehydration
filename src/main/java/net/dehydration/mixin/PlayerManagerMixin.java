package net.dehydration.mixin;

import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.dehydration.access.HydrationManagerAccess;
import net.dehydration.mod.ModConfig;
import net.dehydration.network.HydrationServerPacket;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {

	@Inject(method = "onPlayerConnect", at = @At(value = "TAIL"))
	private void onPlayerConnectMixin(ClientConnection connection, ServerPlayerEntity player, CallbackInfo info) {
		var hydrationManager = ((HydrationManagerAccess) player).getHydrationManager();

		HydrationServerPacket.writeS2CExcludedSyncPacket(player, hydrationManager.hasThirst());
		HydrationServerPacket.writeS2CHydrationTemplateSyncPacket(player);
	}

	@Inject(method = "respawnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;onPlayerRespawned(Lnet/minecraft/server/network/ServerPlayerEntity;)V"), locals = LocalCapture.CAPTURE_FAILSOFT)
	private void respawnPlayerMixinAtInvoke(ServerPlayerEntity player, boolean alive, CallbackInfoReturnable<ServerPlayerEntity> info,
			BlockPos blockPos, float f, boolean bl, ServerWorld serverWorld, Optional<Vec3d> optional2, ServerWorld serverWorld2,
			ServerPlayerEntity serverPlayerEntity) {
		var hydrationManager = ((HydrationManagerAccess) player).getHydrationManager();
		HydrationServerPacket.writeS2CExcludedSyncPacket(serverPlayerEntity, hydrationManager.hasThirst());
	}

	@Inject(method = "respawnPlayer", at = @At("RETURN"), locals = LocalCapture.CAPTURE_FAILSOFT)
	private void respawnPlayerMixinAtReturn(ServerPlayerEntity player, boolean alive, CallbackInfoReturnable<ServerPlayerEntity> info) {
		var serverPlayerEntity = info.getReturnValue();
		var hydrationManager = ((HydrationManagerAccess) serverPlayerEntity).getHydrationManager();
		var defaultThirst = ModConfig.CONFIG.hydrationOnRespawn;

		hydrationManager.setHydrationLevel(defaultThirst);
		HydrationServerPacket.writeS2CHydrationTemplateSyncPacket(player);
	}
}
