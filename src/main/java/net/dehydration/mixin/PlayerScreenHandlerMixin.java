package net.dehydration.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.dehydration.item.LeatherFlaskItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.slot.CraftingResultSlot;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;

@Mixin(PlayerScreenHandler.class)
public class PlayerScreenHandlerMixin {

	@Inject(method = "quickMove", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;copy()Lnet/minecraft/item/ItemStack;"), locals = LocalCapture.CAPTURE_FAILSOFT)
	public void onCraftedLeatherFlask(PlayerEntity player, int lhsSlot, CallbackInfoReturnable<ItemStack> callbackInfo,
			ItemStack lhsItemStack, Slot rhsSlot, ItemStack rhsItemStack) {
		if (player instanceof ServerPlayerEntity && rhsSlot instanceof CraftingResultSlot) {
			if (rhsItemStack.getItem() instanceof LeatherFlaskItem && !rhsItemStack.hasNbt()) {
				var nbt = new NbtCompound();

				nbt.putInt(LeatherFlaskItem.TAG_WATER, 0);
				rhsItemStack.setNbt(nbt);
			}
		}
	}
}
