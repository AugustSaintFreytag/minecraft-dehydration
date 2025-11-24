package net.dehydration.item;

import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import eu.midnightdust.puddles.Puddles;
import me.shedaniel.math.Color;
import net.dehydration.block.AbstractCopperCauldronBlock;
import net.dehydration.block.CampfireCauldronBlock;
import net.dehydration.block.CopperCauldronBlock;
import net.dehydration.block.CopperLeveledCauldronBlock;
import net.dehydration.hydration.HydrationUtil;
import net.dehydration.misc.ThirstTooltipData;
import net.dehydration.mod.ModBlocks;
import net.dehydration.mod.ModConfig;
import net.dehydration.mod.ModSounds;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.AbstractCauldronBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CauldronBlock;
import net.minecraft.block.LeveledCauldronBlock;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.item.TooltipData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

// Thanks to Pois1x for the texture

public class LeatherFlaskItem extends Item {

	public static final String TAG_WATER = "leather_flask";

	// 0 = purified, 1 = contaminated/mixed, 2 dirty
	public static final String TAG_WATER_KIND = "purified_water";

	public final int maxFillLevel;

	public LeatherFlaskItem(int water, Settings settings) {
		super(settings);
		this.maxFillLevel = water;
	}

	@Override
	public ActionResult useOnBlock(ItemUsageContext context) {
		PlayerEntity player = context.getPlayer();
		ItemStack itemStack = context.getStack();
		BlockPos pos = context.getBlockPos();
		BlockState state = context.getWorld().getBlockState(pos);
		NbtCompound tags = itemStack.getNbt();

		if (state.getBlock() instanceof LeveledCauldronBlock || state.getBlock() instanceof CauldronBlock
				|| state.getBlock() instanceof CopperCauldronBlock || state.getBlock() instanceof CopperLeveledCauldronBlock
				|| state.getBlock() instanceof CampfireCauldronBlock) {

			// Empty flask
			if (player.isSneaking()) {
				if ((itemStack.hasNbt() && tags.getInt(TAG_WATER) > 0) || !itemStack.hasNbt()) {
					if (!player.getWorld().isClient()) {
						if (state.getBlock() instanceof AbstractCauldronBlock) {
							if (state.getBlock() instanceof LeveledCauldronBlock) {
								if (((LeveledCauldronBlock) state.getBlock()).isFull(state)) {
									return super.useOnBlock(context);
								}
								player.getWorld().setBlockState(pos, (BlockState) state.cycle(LeveledCauldronBlock.LEVEL));
							} else {
								player.getWorld().setBlockState(pos, Blocks.WATER_CAULDRON.getDefaultState());
								player.getWorld().emitGameEvent(null, GameEvent.BLOCK_CHANGE, pos);
							}
						} else if (state.getBlock() instanceof AbstractCopperCauldronBlock) {
							if (state.getBlock() instanceof CopperLeveledCauldronBlock) {
								if (((CopperLeveledCauldronBlock) state.getBlock()).isFull(state)) {
									return super.useOnBlock(context);
								}
								if (tags.getInt(TAG_WATER_KIND) != 0) {
									player.getWorld().setBlockState(pos, ModBlocks.COPPER_WATER_CAULDRON_BLOCK.getDefaultState()
											.with(CopperLeveledCauldronBlock.LEVEL, state.get(CopperLeveledCauldronBlock.LEVEL) + 1));
								} else {
									player.getWorld().setBlockState(pos, (BlockState) state.cycle(CopperLeveledCauldronBlock.LEVEL));
								}
							} else {
								if (tags.getInt(TAG_WATER_KIND) == 0) {
									player.getWorld().setBlockState(pos, ModBlocks.COPPER_PURIFIED_WATER_CAULDRON_BLOCK.getDefaultState());
								} else {
									player.getWorld().setBlockState(pos, ModBlocks.COPPER_WATER_CAULDRON_BLOCK.getDefaultState());
								}
								player.getWorld().emitGameEvent(null, GameEvent.BLOCK_CHANGE, pos);
							}
						} else {
							if (((CampfireCauldronBlock) state.getBlock()).isFull(state)) {
								return super.useOnBlock(context);
							}
							player.getWorld().setBlockState(pos, (BlockState) state.cycle(CampfireCauldronBlock.LEVEL));
						}

						player.getWorld().playSound((PlayerEntity) null, pos, ModSounds.EMPTY_FLASK_EVENT, SoundCategory.BLOCKS, 1.0F,
								1.0F);
						player.incrementStat(Stats.USE_CAULDRON);

						if (itemStack.hasNbt()) {
							tags.putInt(TAG_WATER, tags.getInt(TAG_WATER) - 1);
						} else {
							tags = new NbtCompound();
							tags.putInt(TAG_WATER, 1 + this.maxFillLevel);
							tags.putInt(TAG_WATER_KIND, 2);
						}

						itemStack.setNbt(tags);
					}

					return ActionResult.success(player.getWorld().isClient());
				}
			} else if (state.getBlock() instanceof LeveledCauldronBlock && state.get(LeveledCauldronBlock.LEVEL) > 0
					&& (!itemStack.hasNbt()
							|| tags.getInt(TAG_WATER) < 2 + this.maxFillLevel)) {
				// Fill up flask

				if (!player.getWorld().isClient()) {
					player.getWorld().playSound((PlayerEntity) null, pos, ModSounds.FILL_FLASK_EVENT, SoundCategory.BLOCKS, 1.0F, 1.0F);
					player.incrementStat(Stats.USE_CAULDRON);
					LeveledCauldronBlock.decrementFluidLevel(state, player.getWorld(), pos);
					tags.putInt(TAG_WATER, tags.getInt(TAG_WATER) + 1);
					tags.putInt(TAG_WATER_KIND, 2);
					itemStack.setNbt(tags);
				}

				return ActionResult.success(player.getWorld().isClient());
			}
		}

		return super.useOnBlock(context);
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack itemStack = user.getStackInHand(hand);
		NbtCompound tags = itemStack.getNbt();
		HitResult hitResult = raycast(world, user, RaycastContext.FluidHandling.SOURCE_ONLY);
		BlockPos blockPos = ((BlockHitResult) hitResult).getBlockPos();

		if (hitResult.getType() == HitResult.Type.BLOCK && world.canPlayerModifyAt(user, blockPos)
				&& world.getFluidState(blockPos).isIn(FluidTags.WATER) && itemStack.hasNbt()) {
			if (user.isSneaking() && tags.getInt(TAG_WATER) != 0) {
				tags.putInt(TAG_WATER, 0);
				world.playSound(user, user.getX(), user.getY(), user.getZ(), ModSounds.EMPTY_FLASK_EVENT, SoundCategory.NEUTRAL, 1.0F,
						1.0F);
				return TypedActionResult.consume(itemStack);
			}
			if (tags.getInt(TAG_WATER) < 2 + maxFillLevel) {
				int fillLevel = 2 + maxFillLevel;
				int waterPurity = 2;

				boolean isEmpty = tags.getInt(TAG_WATER) == 0;
				boolean isDirtyWater = tags.getInt(TAG_WATER_KIND) == 2;
				if (!isEmpty && !isDirtyWater) {
					waterPurity = 1;
				}

				if (FabricLoader.getInstance().isModLoaded("puddles")
						&& world.getBlockState(blockPos) == Puddles.Puddle.getDefaultState()) {
					if (!world.isClient()) {
						world.setBlockState(blockPos, Blocks.AIR.getDefaultState());
					}
					if (!isEmpty && !isDirtyWater) {
						fillLevel = 2;
						waterPurity = 0;
					}
				}

				boolean riverWater = world.getBiome(blockPos).isIn(BiomeTags.IS_RIVER);
				if (riverWater && (isEmpty || (!isEmpty && !isDirtyWater))) {
					waterPurity = 0;
				}

				world.playSound(user, user.getX(), user.getY(), user.getZ(), ModSounds.FILL_FLASK_EVENT, SoundCategory.NEUTRAL, 1.0F, 1.0F);
				tags.putInt(TAG_WATER_KIND, waterPurity);
				tags.putInt(TAG_WATER, fillLevel);
				return TypedActionResult.consume(itemStack);
			}
		}
		if (itemStack.hasNbt() && tags.getInt(TAG_WATER) == 0) {
			return TypedActionResult.pass(itemStack);
		} else {
			return ItemUsage.consumeHeldItem(world, user, hand);
		}
	}

	@Override
	public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
		PlayerEntity player = user instanceof PlayerEntity ? (PlayerEntity) user : null;
		NbtCompound tags = stack.getNbt();

		if (player == null) {
			return stack;
		}

		if (tags == null || tags.getInt(TAG_WATER) == 0) {
			return stack;
		}

		if (player instanceof ServerPlayerEntity) {
			Criteria.CONSUME_ITEM.trigger((ServerPlayerEntity) player, stack);
		}

		player.incrementStat(Stats.USED.getOrCreateStat(this));

		if (player.isCreative() || player.isSpectator()) {
			return stack;
		}

		if (!stack.hasNbt()) {
			tags = new NbtCompound();

			tags.putInt(TAG_WATER, 2 + maxFillLevel);
			tags.putInt(TAG_WATER_KIND, 0);
			stack.setNbt(tags);
		}

		tags.putInt(TAG_WATER, tags.getInt(TAG_WATER) - 1);

		HydrationUtil.addHydrationToPlayer(player, ModConfig.CONFIG.flaskHydrationValue);

		if (world.isClient()) {
			return stack;
		}

		if (tags.getInt(TAG_WATER_KIND) == 2 || tags.getInt(TAG_WATER_KIND) == 1) {
			HydrationUtil.addDefaultThirstEffectToPlayer(player);
		}

		return stack;
	}

	@Override
	public int getMaxUseTime(ItemStack stack) {
		return 32;
	}

	@Override
	public UseAction getUseAction(ItemStack stack) {
		if (!isFlaskEmpty(stack)) {
			return UseAction.DRINK;
		}

		return UseAction.NONE;
	}

	@Override
	public void onCraft(ItemStack stack, World world, PlayerEntity player) {
		if (!stack.hasNbt()) {
			NbtCompound tags = new NbtCompound();
			tags.putInt(TAG_WATER, 0);
			stack.setNbt(tags);
		}
	}

	@Override @Environment(EnvType.CLIENT)
	public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
		NbtCompound tags = stack.getNbt();

		if (tags != null) {
			tooltip.add(Text.translatable("item.dehydration.leather_flask.rations.tooltip", tags.getInt(TAG_WATER), maxFillLevel + 2)
					.formatted(Formatting.GRAY));

			if (ModConfig.CONFIG.renderHydrationValueInItemTooltip && tags.getInt(TAG_WATER) > 0) {
				String string = "dirty";

				if (tags.getInt(TAG_WATER_KIND) == 1) {
					string = "impurified";
				} else if (tags.getInt(TAG_WATER_KIND) == 0) {
					string = "purified";
				}

				tooltip.add(Text.translatable("item.dehydration.leather_flask.rations.tooltip3." + string));
			} else {
				tooltip.add(
						Text.translatable("item.dehydration.leather_flask.capacity.tooltip", maxFillLevel + 2).formatted(Formatting.GRAY));
			}
		}

		super.appendTooltip(stack, world, tooltip, context);
	}

	@Override
	public Optional<TooltipData> getTooltipData(ItemStack stack) {
		if (ModConfig.CONFIG.previewHydrationWhenHoldingItem) {
			if (stack.hasNbt() && stack.getNbt().contains(TAG_WATER)) {
				if (stack.getNbt().getInt(TAG_WATER) == 0) {
					return Optional.empty();
				}
				return Optional.of(new ThirstTooltipData(stack.getNbt().getInt(TAG_WATER_KIND),
						stack.getNbt().getInt(TAG_WATER) * ModConfig.CONFIG.flaskHydrationValue));
			}
			return Optional.of(new ThirstTooltipData(0, (2 + this.maxFillLevel) * ModConfig.CONFIG.flaskHydrationValue));
		}

		return super.getTooltipData(stack);
	}

	public static void fillFlask(ItemStack itemStack, int quench) {
		NbtCompound nbt = new NbtCompound();

		if (!itemStack.hasNbt()) {
			nbt.putInt(TAG_WATER, 0);
			nbt.putInt(TAG_WATER_KIND, 0);
		} else {
			nbt = itemStack.getNbt().copy();
			if (nbt.getInt(TAG_WATER) == 0) {
				nbt.putInt(TAG_WATER_KIND, 0);
			}
		}

		int hydrationValue = nbt.getInt(TAG_WATER) + quench;
		int addition = ((LeatherFlaskItem) itemStack.getItem()).maxFillLevel;

		nbt.putInt(TAG_WATER, hydrationValue > 2 + addition ? 2 + addition : hydrationValue);
		itemStack.setNbt(nbt);
	}

	@Override
	public boolean isItemBarVisible(ItemStack stack) {
		return !isFlaskEmpty(stack);
	}

	@Override
	public int getItemBarStep(ItemStack stack) {
		if (!stack.hasNbt()) {
			return super.getItemBarStep(stack);
		}

		var item = (LeatherFlaskItem) stack.getItem();

		var maxFillLevel = item.maxFillLevel + 2;
		var fillLevel = stack.getNbt().getInt(TAG_WATER);
		var step = Math.round(13f - (maxFillLevel - fillLevel) * 13f / maxFillLevel);

		return step;
	}

	@Override
	public boolean allowNbtUpdateAnimation(PlayerEntity player, Hand hand, ItemStack oldStack, ItemStack newStack) {
		NbtCompound oldNbt = null;
		NbtCompound newNbt = null;

		if (oldStack.getNbt() != null) {
			oldNbt = oldStack.getNbt().copy();
			oldNbt.remove(TAG_WATER);
			oldNbt.remove(TAG_WATER_KIND);
		}

		if (newStack.getNbt() != null) {
			newNbt = newStack.getNbt().copy();
			newNbt.remove(TAG_WATER);
			newNbt.remove(TAG_WATER_KIND);
		}

		if (oldNbt == null && newNbt != null) {
			return true;
		}

		if (oldNbt != null && newNbt == null) {
			return true;
		}

		if (oldNbt == null && newNbt == null) {
			return false;
		}

		return oldNbt == null || oldNbt.equals(null);
	}

	@Override
	public int getItemBarColor(ItemStack stack) {
		if (!stack.hasNbt()) {
			return super.getItemBarColor(stack);
		}

		var fillKind = stack.getNbt().getInt(TAG_WATER_KIND);

		switch (fillKind) {
		case 0:
			// Purified Water
			return Color.ofRGB(83, 237, 245).getColor();
		case 1:
			// Contaminated Water
			return Color.ofRGB(255, 221, 158).getColor();
		case 2:
		default:
			// Dirty Water
			return Color.ofRGB(255, 191, 150).getColor();
		}
	}

	public static int getMaxFlaskFillLevel(ItemStack stack) {
		var item = (LeatherFlaskItem) stack.getItem();
		var maxFillLevel = item.maxFillLevel + 2;

		return maxFillLevel;
	}

	public static boolean isFlaskEmpty(ItemStack stack) {
		NbtCompound tags = stack.getNbt();

		if (tags == null) {
			return true;
		}

		if (tags.getInt(TAG_WATER) != 0) {
			return false;
		}

		return true;
	}

	public static boolean isFlaskFull(ItemStack stack) {
		NbtCompound tags = stack.getNbt();

		if (tags == null) {
			return false;
		}

		var fillLevel = tags.getInt(TAG_WATER);
		var maxFillLevel = getMaxFlaskFillLevel(stack);

		return fillLevel == maxFillLevel;
	}

	public static boolean isFlaskContaminated(ItemStack stack) {
		NbtCompound tags = stack.getNbt();

		if (tags == null) {
			return false;
		}

		int waterKind = tags.getInt(TAG_WATER_KIND);
		return waterKind == 1 || waterKind == 2;
	}

	public static boolean isFlaskPartiallyContaminated(ItemStack stack) {
		NbtCompound tags = stack.getNbt();

		if (tags == null) {
			return false;
		}

		return tags.getInt(TAG_WATER_KIND) == 1;
	}

}
