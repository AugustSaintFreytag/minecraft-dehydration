package net.dehydration.item;

import org.jetbrains.annotations.Nullable;

import net.dehydration.mod.ModFluids;
import net.dehydration.utils.PlayerInventoryUtil;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Block;
import net.minecraft.block.FluidFillable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.event.GameEvent;

public class PurifiedWaterBucketItem extends Item {

	public PurifiedWaterBucketItem(Settings settings) {
		super(settings);
	}

	// Interaction

	@Override
	public UseAction getUseAction(ItemStack stack) {
		return UseAction.DRINK;
	}

	@Override
	public int getMaxUseTime(ItemStack stack) {
		return 32;
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		var itemStack = user.getStackInHand(hand);
		var targetBlockHitResult = checkTargetedBlock(world, user);

		if (targetBlockHitResult == null) {
			ItemUsage.consumeHeldItem(world, user, hand);
			return TypedActionResult.success(itemStack, world.isClient());
		}

		var targetBlockPosition = targetBlockHitResult.getBlockPos();
		var targetBlockDirection = targetBlockHitResult.getSide();
		var adjacentBlockPosition = targetBlockPosition.offset(targetBlockDirection);

		if (!world.canPlayerModifyAt(user, targetBlockPosition)
				|| !user.canPlaceOn(adjacentBlockPosition, targetBlockDirection, itemStack)) {
			return TypedActionResult.fail(itemStack);
		}

		var blockState = world.getBlockState(targetBlockPosition);
		var fillableBlockPosition = blockState.getBlock() instanceof FluidFillable ? targetBlockPosition : adjacentBlockPosition;

		if (this.placeFluid(user, world, fillableBlockPosition, targetBlockHitResult)) {
			this.onEmptied(user, world, itemStack, fillableBlockPosition);

			if (user instanceof ServerPlayerEntity) {
				Criteria.PLACED_BLOCK.trigger((ServerPlayerEntity) user, fillableBlockPosition, itemStack);
			}

			user.incrementStat(Stats.USED.getOrCreateStat(this));
			return TypedActionResult.success(BucketItem.getEmptiedStack(itemStack, user), world.isClient());
		} else {
			return TypedActionResult.fail(itemStack);
		}
	}

	@Override
	public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
		super.finishUsing(stack, world, user);

		if (!(user instanceof PlayerEntity player)) {
			return ItemStack.EMPTY;
		}

		if (user instanceof ServerPlayerEntity serverPlayer) {
			Criteria.CONSUME_ITEM.trigger(serverPlayer, stack);
			serverPlayer.incrementStat(Stats.USED.getOrCreateStat(this));

			// var serverWorld = (ServerWorld) world;
			// serverWorld.playSound(null, serverPlayer.getBlockPos(),
			// SoundEvents.ENTITY_WANDERING_TRADER_DRINK_POTION, SoundCategory.PLAYERS,
			// 1.0F, 1.0F);
		}

		if (!player.isCreative()) {
			stack.decrement(1);
			PlayerInventoryUtil.giveItemStackToPlayer(player, new ItemStack(Items.BUCKET));
		}

		return stack;
	}

	public void onEmptied(PlayerEntity player, World world, ItemStack stack, BlockPos pos) {
	}

	@SuppressWarnings("deprecation")
	public boolean placeFluid(@Nullable PlayerEntity player, World world, BlockPos pos, @Nullable BlockHitResult hitResult) {

		var blockState = world.getBlockState(pos);
		var block = blockState.getBlock();
		var isBucketPlaceable = blockState.canBucketPlace(ModFluids.PURIFIED_WATER);
		var isFluidPlaceable = blockState.isAir() || isBucketPlaceable || block instanceof FluidFillable
				&& ((FluidFillable) ((Object) block)).canFillWithFluid(world, pos, blockState, ModFluids.PURIFIED_WATER);

		if (!isFluidPlaceable) {
			return hitResult != null && this.placeFluid(player, world, hitResult.getBlockPos().offset(hitResult.getSide()), null);
		}

		if (world.getDimension().ultrawarm()) {
			var i = pos.getX();
			var j = pos.getY();
			var k = pos.getZ();

			world.playSound(player, pos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.5f,
					2.6f + (world.random.nextFloat() - world.random.nextFloat()) * 0.8f);
			for (int l = 0; l < 8; ++l) {
				world.addParticle(ParticleTypes.LARGE_SMOKE, (double) i + Math.random(), (double) j + Math.random(),
						(double) k + Math.random(), 0.0, 0.0, 0.0);
			}

			return true;
		}

		if (block instanceof FluidFillable) {
			((FluidFillable) ((Object) block)).tryFillWithFluid(world, pos, blockState,
					((FlowableFluid) ModFluids.PURIFIED_WATER).getStill(false));

			this.playEmptyingSound(player, world, pos);
			return true;
		}

		if (!world.isClient() && isBucketPlaceable && !blockState.isLiquid()) {
			world.breakBlock(pos, true);
		}

		if (world.setBlockState(pos, ModFluids.PURIFIED_WATER.getDefaultState().getBlockState(),
				Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD) || blockState.getFluidState().isStill()) {
			this.playEmptyingSound(player, world, pos);
			return true;
		}

		return false;
	}

	protected void playEmptyingSound(@Nullable PlayerEntity player, WorldAccess world, BlockPos pos) {
		world.playSound(player, pos, SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.BLOCKS, 1.0f, 1.0f);
		world.emitGameEvent((Entity) player, GameEvent.FLUID_PLACE, pos);
	}

	// Behavior

	protected BlockHitResult checkTargetedBlock(World world, PlayerEntity user) {
		var blockHitResult = BucketItem.raycast(world, user, RaycastContext.FluidHandling.NONE);

		if (blockHitResult.getType() == HitResult.Type.BLOCK) {
			return blockHitResult;
		}

		return null;
	}

}
