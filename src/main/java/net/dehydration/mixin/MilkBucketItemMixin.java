package net.dehydration.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MilkBucketItem;
import net.minecraft.world.World;

@Mixin(MilkBucketItem.class)
public abstract class MilkBucketItemMixin extends Item {

	public MilkBucketItemMixin(Settings settings) {
		super(settings);
	}

	@Inject(method = "finishUsing", at = @At("HEAD"))
	private void finishUsingMixin(ItemStack stack, World world, LivingEntity user, CallbackInfoReturnable<ItemStack> info) {
		super.finishUsing(stack, world, user);
	}

}
