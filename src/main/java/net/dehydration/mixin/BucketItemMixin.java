package net.dehydration.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.dehydration.utils.PlayerInventoryUtil;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BucketItem;
import net.minecraft.item.FluidModificationItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

@Mixin(BucketItem.class)
public abstract class BucketItemMixin extends Item implements FluidModificationItem {

	// Properties

	@Shadow
	private Fluid fluid;

	// Init

	public BucketItemMixin(Fluid fluid, Item.Settings settings) {
		super(settings);
		this.fluid = fluid;
	}

	// Use

	@Override
	public UseAction getUseAction(ItemStack stack) {
		return UseAction.DRINK;
	}

	@Override
	public int getMaxUseTime(ItemStack stack) {
		return 32;
	}

	@Inject(method = "use", at = @At("HEAD"), cancellable = true)
	public void dehydration$use(World world, PlayerEntity user, Hand hand,
			CallbackInfoReturnable<TypedActionResult<ItemStack>> callbackInfo) {
		if (this.fluid != Fluids.WATER) {
			return;
		}

		var itemStack = user.getStackInHand(hand);
		var targetBlockHitResult = checkTargetedBlock(world, user);

		if (targetBlockHitResult == null) {
			ItemUsage.consumeHeldItem(world, user, hand);
			callbackInfo.setReturnValue(TypedActionResult.success(itemStack, world.isClient()));
		}
	}

	@Override
	public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
		if (this.fluid != Fluids.WATER) {
			return ItemStack.EMPTY;
		}

		super.finishUsing(stack, world, user);

		if (!(user instanceof PlayerEntity player)) {
			return ItemStack.EMPTY;
		}

		if (user instanceof ServerPlayerEntity serverPlayer) {
			Criteria.CONSUME_ITEM.trigger(serverPlayer, stack);
			serverPlayer.incrementStat(Stats.USED.getOrCreateStat(this));
		}

		if (!player.isCreative()) {
			stack.decrement(1);
			PlayerInventoryUtil.giveItemStackToPlayer(player, new ItemStack(Items.BUCKET));
		}

		return stack;
	}

	// Behavior

	private BlockHitResult checkTargetedBlock(World world, PlayerEntity user) {
		var blockHitResult = BucketItem.raycast(world, user, RaycastContext.FluidHandling.NONE);

		if (blockHitResult.getType() == HitResult.Type.BLOCK) {
			return blockHitResult;
		}

		return null;
	}

}
