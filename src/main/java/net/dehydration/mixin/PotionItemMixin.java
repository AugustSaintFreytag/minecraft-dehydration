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
import net.minecraft.item.Items;
import net.minecraft.item.PotionItem;
import net.minecraft.item.ThrowablePotionItem;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

@Mixin(PotionItem.class)
public abstract class PotionItemMixin extends Item {

	public PotionItemMixin(Settings settings) {
		super(settings);
	}

	@Inject(method = "use", at = @At("HEAD"), cancellable = true)
	private void useMixin(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> info) {
		BlockHitResult hitResult = Item.raycast(world, user, RaycastContext.FluidHandling.SOURCE_ONLY);
		if (((HitResult) hitResult).getType() == HitResult.Type.BLOCK) {
			var blockPos = hitResult.getBlockPos();

			if (world.canPlayerModifyAt(user, blockPos) && world.getFluidState(blockPos).isIn(FluidTags.WATER)) {
				world.playSound(user, user.getX(), user.getY(), user.getZ(), SoundEvents.ITEM_BOTTLE_EMPTY, SoundCategory.NEUTRAL, 1.0f,
						1.0f);
				info.setReturnValue(TypedActionResult.success(new ItemStack(Items.GLASS_BOTTLE), world.isClient()));
			}
		}
	}

	@Inject(method = "finishUsing", at = @At(value = "HEAD"))
	public void finishUsingMixin(ItemStack stack, World world, LivingEntity user, CallbackInfoReturnable<ItemStack> info) {
		if (!(user instanceof PlayerEntity player) || world.isClient()) {
			return;
		}

		HydrationUtil.addHydrationToPlayerForItemStack(player, stack);

		if (HydrationUtil.isContaminatedItemStack(stack)) {
			HydrationUtil.addDefaultThirstEffectToPlayer(player);
		}
	}

	@Override
	public Optional<TooltipData> getTooltipData(ItemStack stack) {
		if (!ModConfig.CONFIG.previewHydrationWhenHoldingItem || (stack.getItem() instanceof ThrowablePotionItem)) {
			return Optional.empty();
		}

		var hydrationValue = HydrationUtil.getHydrationValueForItemStack(stack);

		if (HydrationUtil.isContaminatedItemStack(stack)) {
			return Optional.of(new ThirstTooltipData(2, hydrationValue));
		}

		return Optional.of(new ThirstTooltipData(0, hydrationValue));

	}

}
