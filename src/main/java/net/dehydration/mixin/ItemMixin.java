package net.dehydration.mixin;

import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.dehydration.hydration.HydrationUtil;
import net.dehydration.misc.ThirstTooltipData;
import net.dehydration.mod.ModConfig;
import net.minecraft.client.item.TooltipData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

@Mixin(Item.class)
public class ItemMixin {

	@Inject(method = "finishUsing", at = @At(value = "HEAD"))
	private void finishUsingMixin(ItemStack stack, World world, LivingEntity user, CallbackInfoReturnable<ItemStack> info) {
		if (!(user instanceof PlayerEntity player)) {
			return;
		}

		HydrationUtil.addHydrationToPlayerForItemStack(player, stack);

		if (HydrationUtil.isContaminatedItemStack(stack)) {
			HydrationUtil.addDefaultThirstEffectToPlayer(player);
		}
	}

	@Inject(method = "getTooltipData", at = @At("HEAD"), cancellable = true)
	private void getTooltipDataMixin(ItemStack stack, CallbackInfoReturnable<Optional<TooltipData>> info) {
		if (!ModConfig.CONFIG.previewHydrationWhenHoldingItem) {
			return;
		}

		var hydrationValue = HydrationUtil.getHydrationValueForItemStack(stack);
		var isContaminated = HydrationUtil.isContaminatedItemStack(stack);

		if (hydrationValue > 0) {
			info.setReturnValue(Optional.of(new ThirstTooltipData(isContaminated ? 2 : 0, hydrationValue)));
		}
	}

}
