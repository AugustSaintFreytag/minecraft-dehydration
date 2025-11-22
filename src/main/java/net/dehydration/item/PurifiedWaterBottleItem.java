package net.dehydration.item;

import net.dehydration.utils.PlayerInventoryUtil;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;

public class PurifiedWaterBottleItem extends Item {

	// Init

	public PurifiedWaterBottleItem(Settings settings) {
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
		return ItemUsage.consumeHeldItem(world, user, hand);
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

			var serverWorld = (ServerWorld) world;
			serverWorld.playSound(null, serverPlayer.getBlockPos(), SoundEvents.ENTITY_WANDERING_TRADER_DRINK_POTION, SoundCategory.PLAYERS,
					1.0F, 1.0F);
		}

		if (!player.isCreative()) {
			stack.decrement(1);
			PlayerInventoryUtil.giveItemStackToPlayer(player, new ItemStack(Items.GLASS_BOTTLE));
		}

		return stack;
	}

}
