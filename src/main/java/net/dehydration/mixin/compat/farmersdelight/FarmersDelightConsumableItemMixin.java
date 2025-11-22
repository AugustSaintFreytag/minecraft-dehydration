package net.dehydration.mixin.compat.farmersdelight;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import vectorwing.farmersdelight.common.item.ConsumableItem;

@Mixin(ConsumableItem.class)
public abstract class FarmersDelightConsumableItemMixin extends Item {

	public FarmersDelightConsumableItemMixin(Settings settings) {
		super(settings);
	}

	@Inject(method = "finishUsing", at = @At("RETURN"))
	private void mixinFinishUsing(ItemStack stack, World level, LivingEntity consumer, CallbackInfoReturnable<ItemStack> callbackInfo) {
		if (!level.isClient) {
			return;
		}

		if (!this.isFood()) {
			super.finishUsing(stack, level, consumer);
		}
	}

}
