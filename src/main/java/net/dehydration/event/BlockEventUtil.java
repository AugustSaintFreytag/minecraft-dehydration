package net.dehydration.event;

import net.dehydration.access.HydrationManagerAccess;
import net.dehydration.access.PlayerAccess;
import net.dehydration.hydration.HydrationUtil;
import net.dehydration.mod.ModConfig;
import net.dehydration.mod.ModSounds;
import net.dehydration.mod.ModTags;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;

public class BlockEventUtil {

	public static ActionResult handleUseBlockEvent(PlayerEntity player, World world, Hand hand,
			BlockHitResult originalHitResult) {
		// Initial checks for player state and held item
		if (player.isCreative() || player.isSpectator() || !player.isSneaking()) {
			return ActionResult.PASS;
		}

		if (!player.getMainHandStack().isEmpty()) {
			return ActionResult.PASS;
		}

		// Perform a new raycast to find the block to interact with for drinking
		var targetedHitResult = player.raycast(1.5D, 0.0F, true);
		if (targetedHitResult.getType() != HitResult.Type.BLOCK) {
			return ActionResult.PASS; // Not looking at a block close enough
		}

		var blockPos = ((BlockHitResult) targetedHitResult).getBlockPos();

		// Check if the player can modify the block and if it contains water
		if (!world.canPlayerModifyAt(player, blockPos) || !world.getFluidState(blockPos).isIn(FluidTags.WATER)) {
			return ActionResult.PASS;
		}

		// Check if the water is still or if drinking from flowing water is allowed by
		// config
		if (!world.getFluidState(blockPos).isStill() && !ModConfig.CONFIG.allowDrinkingFlowingWaterBlocks) {
			return ActionResult.PASS;
		}

		var hydrationManager = ((HydrationManagerAccess) player).getHydrationManager();

		if (!hydrationManager.isNotFull()) {
			// Player's hydration is already full, can bail
			return ActionResult.PASS;
		}

		var playerAccess = (PlayerAccess) player;
		var drinkTime = playerAccess.getDrinkTime();

		// Play drinking sound periodically on the client while drinking
		if (world.isClient() && drinkTime % 3 == 0) {
			player.playSound(SoundEvents.ENTITY_GENERIC_DRINK, 0.5f, world.getRandom().nextFloat() * 0.1f + 0.9f);
		}

		// If drinking duration is not yet met, continue drinking
		if (drinkTime <= 20) {
			playerAccess.setDrinkTime(drinkTime + 1);
			return ActionResult.PASS;
		}

		// Drinking duration met, complete the drinking action
		if (!world.isClient()) {
			// Server-side logic: modify block, add hydration, apply effects
			if (!ModConfig.CONFIG.allowDrinkingFlowingWaterBlocks && world.getFluidState(blockPos).isStill()) {
				var currentBlockState = world.getBlockState(blockPos);
				if (currentBlockState.contains(Properties.WATERLOGGED)) {
					world.setBlockState(blockPos, currentBlockState.with(Properties.WATERLOGGED, false));
				} else {
					// Assumes it's a full water block like Blocks.WATER if not waterloggable but
					// fluid is still water
					world.setBlockState(blockPos, Blocks.AIR.getDefaultState());
				}
			}

			hydrationManager.add(ModConfig.CONFIG.waterBlockHydrationValue);

			if (!world.getFluidState(blockPos).isIn(ModTags.PURIFIED_WATER)) {
				HydrationUtil.addDefaultThirstEffectToPlayer(player);
			}
		} else {
			// Client-side logic: play finish drinking sound
			world.playSound(player, player.getX(), player.getY(), player.getZ(),
					ModSounds.WATER_SIP_EVENT, SoundCategory.PLAYERS, 1.0F,
					0.9F + (world.getRandom().nextFloat() / 5F));
		}

		// Reset drink time
		playerAccess.setDrinkTime(0);
		return ActionResult.SUCCESS;
	}
}
