package net.dehydration.block.entity;

import java.util.Map;
import java.util.function.Predicate;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.dehydration.block.CopperLeveledCauldronBlock;
import net.dehydration.item.LeatherFlask;
import net.dehydration.mod.ModBlocks;
import net.dehydration.mod.ModItems;
import net.dehydration.mod.ModSounds;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public interface CopperCauldronBehavior {
	Map<Item, CopperCauldronBehavior> EMPTY_COPPER_CAULDRON_BEHAVIOR = createMap();
	Map<Item, CopperCauldronBehavior> WATER_COPPER_CAULDRON_BEHAVIOR = createMap();
	Map<Item, CopperCauldronBehavior> POWDER_SNOW_COPPER_CAULDRON_BEHAVIOR = createMap();
	Map<Item, CopperCauldronBehavior> PURIFIED_WATER_COPPER_CAULDRON_BEHAVIOR = createMap();
	CopperCauldronBehavior FILL_WITH_WATER = (state, world, pos, player, hand, stack) -> {
		return fillCauldron(world, pos, player, hand, stack,
				(BlockState) ModBlocks.COPPER_WATER_CAULDRON_BLOCK.getDefaultState()
						.with(CopperLeveledCauldronBlock.LEVEL, 3),
				SoundEvents.ITEM_BUCKET_EMPTY);
	};

	CopperCauldronBehavior FILL_WITH_POWDER_SNOW = (state, world, pos, player, hand, stack) -> {
		return fillCauldron(world, pos, player, hand, stack,
				(BlockState) ModBlocks.COPPER_POWDERED_CAULDRON_BLOCK.getDefaultState()
						.with(CopperLeveledCauldronBlock.LEVEL, 3),
				SoundEvents.ITEM_BUCKET_EMPTY_POWDER_SNOW);
	};

	static Object2ObjectOpenHashMap<Item, CopperCauldronBehavior> createMap() {
		return Util.make(new Object2ObjectOpenHashMap<Item, CopperCauldronBehavior>(), (map) -> {
			map.defaultReturnValue((state, world, pos, player, hand, stack) -> {
				return ActionResult.PASS;
			});
		});
	}

	ActionResult interact(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack);

	static void registerBehavior() {
		EMPTY_COPPER_CAULDRON_BEHAVIOR.put(ModItems.PURIFIED_WATER_BUCKET, (state, world, pos, player, hand, stack) -> {
			return fillCauldron(world, pos, player, hand, stack,
					(BlockState) ModBlocks.COPPER_PURIFIED_WATER_CAULDRON_BLOCK.getDefaultState()
							.with(CopperLeveledCauldronBlock.LEVEL, 3),
					SoundEvents.ITEM_BUCKET_EMPTY);
		});
		EMPTY_COPPER_CAULDRON_BEHAVIOR.put(Items.POTION, (state, world, pos, player, hand, stack) -> {
			if (PotionUtil.getPotion(stack) == ModItems.PURIFIED_WATER) {
				if (!world.isClient()) {
					Item item = stack.getItem();
					player.setStackInHand(hand,
							ItemUsage.exchangeStack(stack, player, new ItemStack(Items.GLASS_BOTTLE)));
					player.incrementStat(Stats.FILL_CAULDRON);
					player.incrementStat(Stats.USED.getOrCreateStat(item));
					world.setBlockState(pos, ModBlocks.COPPER_PURIFIED_WATER_CAULDRON_BLOCK.getDefaultState());
					world.playSound((PlayerEntity) null, pos, SoundEvents.ITEM_BOTTLE_EMPTY, SoundCategory.BLOCKS, 1.0F,
							1.0F);
					world.emitGameEvent((Entity) null, GameEvent.FLUID_PLACE, pos);
				}
				return ActionResult.success(world.isClient());
			} else if (PotionUtil.getPotion(stack) == Potions.WATER) {
				if (!world.isClient()) {
					Item item = stack.getItem();
					player.setStackInHand(hand,
							ItemUsage.exchangeStack(stack, player, new ItemStack(Items.GLASS_BOTTLE)));
					player.incrementStat(Stats.FILL_CAULDRON);
					player.incrementStat(Stats.USED.getOrCreateStat(item));
					world.setBlockState(pos, ModBlocks.COPPER_WATER_CAULDRON_BLOCK.getDefaultState());
					world.playSound((PlayerEntity) null, pos, SoundEvents.ITEM_BOTTLE_EMPTY, SoundCategory.BLOCKS, 1.0F,
							1.0F);
					world.emitGameEvent((Entity) null, GameEvent.FLUID_PLACE, pos);
				}
				return ActionResult.success(world.isClient());
			} else
				return ActionResult.PASS;
		});
		for (int i = 0; i < ModItems.FLASK_ITEM_LIST.size(); i++) {
			EMPTY_COPPER_CAULDRON_BEHAVIOR.put(ModItems.FLASK_ITEM_LIST.get(i),
					(state, world, pos, player, hand, stack) -> {
						if (LeatherFlask.isFlaskEmpty(stack)) {
							return ActionResult.PASS;
						} else if (!world.isClient()) {
							Item item = stack.getItem();
							NbtCompound nbt;
							if (stack.hasNbt()) {
								nbt = stack.getNbt();
								nbt.putInt(LeatherFlask.TAG_WATER, nbt.getInt(LeatherFlask.TAG_WATER) - 1);
							} else {
								nbt = new NbtCompound();
								nbt.putInt(LeatherFlask.TAG_WATER, 1 + ((LeatherFlask) item).maxFillLevel);
								nbt.putInt(LeatherFlask.TAG_WATER_KIND, 0);

							}
							stack.setNbt(nbt);
							if (nbt.getInt(LeatherFlask.TAG_WATER_KIND) == 0)
								world.setBlockState(pos,
										ModBlocks.COPPER_PURIFIED_WATER_CAULDRON_BLOCK.getDefaultState());
							else
								world.setBlockState(pos, ModBlocks.COPPER_WATER_CAULDRON_BLOCK.getDefaultState());

							player.incrementStat(Stats.USE_CAULDRON);
							player.incrementStat(Stats.USED.getOrCreateStat(item));

							world.playSound((PlayerEntity) null, pos, SoundEvents.ITEM_BOTTLE_EMPTY,
									SoundCategory.BLOCKS, 1.0F, 1.0F);
							world.emitGameEvent((Entity) null, GameEvent.FLUID_PLACE, pos);
						}
						return ActionResult.success(world.isClient());

					});
		}
		registerBucketBehavior(EMPTY_COPPER_CAULDRON_BEHAVIOR);
		WATER_COPPER_CAULDRON_BEHAVIOR.put(Items.BUCKET, (state, world, pos, player, hand, stack) -> {
			return emptyCauldron(state, world, pos, player, hand, stack, new ItemStack(Items.WATER_BUCKET),
					(statex) -> {
						return (Integer) statex.get(CopperLeveledCauldronBlock.LEVEL) == 3;
					}, SoundEvents.ITEM_BUCKET_FILL);
		});
		WATER_COPPER_CAULDRON_BEHAVIOR.put(Items.GLASS_BOTTLE, (state, world, pos, player, hand, stack) -> {
			if (!world.isClient()) {
				Item item = stack.getItem();
				player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player,
						PotionUtil.setPotion(new ItemStack(Items.POTION), Potions.WATER)));
				player.incrementStat(Stats.USE_CAULDRON);
				player.incrementStat(Stats.USED.getOrCreateStat(item));
				CopperLeveledCauldronBlock.decrementFluidLevel(state, world, pos);
				world.playSound((PlayerEntity) null, pos, SoundEvents.ITEM_BOTTLE_FILL, SoundCategory.BLOCKS, 1.0F,
						1.0F);
				world.emitGameEvent((Entity) null, GameEvent.FLUID_PICKUP, pos);
			}

			return ActionResult.success(world.isClient());
		});
		WATER_COPPER_CAULDRON_BEHAVIOR.put(Items.POTION, (state, world, pos, player, hand, stack) -> {
			if ((Integer) state.get(CopperLeveledCauldronBlock.LEVEL) != 3
					&& PotionUtil.getPotion(stack) == Potions.WATER) {
				if (!world.isClient()) {
					player.setStackInHand(hand,
							ItemUsage.exchangeStack(stack, player, new ItemStack(Items.GLASS_BOTTLE)));
					player.incrementStat(Stats.USE_CAULDRON);
					player.incrementStat(Stats.USED.getOrCreateStat(stack.getItem()));
					world.setBlockState(pos, (BlockState) state.cycle(CopperLeveledCauldronBlock.LEVEL));
					world.playSound((PlayerEntity) null, pos, SoundEvents.ITEM_BOTTLE_EMPTY, SoundCategory.BLOCKS, 1.0F,
							1.0F);
					world.emitGameEvent((Entity) null, GameEvent.FLUID_PLACE, pos);
				}

				return ActionResult.success(world.isClient());
			} else {
				return ActionResult.PASS;
			}
		});
		registerBucketBehavior(WATER_COPPER_CAULDRON_BEHAVIOR);
		POWDER_SNOW_COPPER_CAULDRON_BEHAVIOR.put(Items.BUCKET, (state, world, pos, player, hand, stack) -> {
			return emptyCauldron(state, world, pos, player, hand, stack, new ItemStack(Items.POWDER_SNOW_BUCKET),
					(statex) -> {
						return (Integer) statex.get(CopperLeveledCauldronBlock.LEVEL) == 3;
					}, SoundEvents.ITEM_BUCKET_FILL_POWDER_SNOW);
		});
		registerBucketBehavior(POWDER_SNOW_COPPER_CAULDRON_BEHAVIOR);

		PURIFIED_WATER_COPPER_CAULDRON_BEHAVIOR.put(Items.BUCKET, (state, world, pos, player, hand, stack) -> {
			return emptyCauldron(state, world, pos, player, hand, stack, new ItemStack(ModItems.PURIFIED_WATER_BUCKET),
					(statex) -> {
						return (Integer) statex.get(CopperLeveledCauldronBlock.LEVEL) == 3;
					}, SoundEvents.ITEM_BUCKET_FILL);
		});
		PURIFIED_WATER_COPPER_CAULDRON_BEHAVIOR.put(Items.GLASS_BOTTLE, (state, world, pos, player, hand, stack) -> {
			if (!world.isClient()) {
				Item item = stack.getItem();
				player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player,
						PotionUtil.setPotion(new ItemStack(Items.POTION), ModItems.PURIFIED_WATER)));
				player.incrementStat(Stats.USE_CAULDRON);
				player.incrementStat(Stats.USED.getOrCreateStat(item));
				CopperLeveledCauldronBlock.decrementFluidLevel(state, world, pos);
				world.playSound((PlayerEntity) null, pos, SoundEvents.ITEM_BOTTLE_FILL, SoundCategory.BLOCKS, 1.0F,
						1.0F);
				world.emitGameEvent((Entity) null, GameEvent.FLUID_PICKUP, pos);
			}

			return ActionResult.success(world.isClient());
		});
		PURIFIED_WATER_COPPER_CAULDRON_BEHAVIOR.put(Items.POTION, (state, world, pos, player, hand, stack) -> {
			if ((Integer) state.get(CopperLeveledCauldronBlock.LEVEL) != 3
					&& PotionUtil.getPotion(stack) == ModItems.PURIFIED_WATER) {
				if (!world.isClient()) {
					player.setStackInHand(hand,
							ItemUsage.exchangeStack(stack, player, new ItemStack(Items.GLASS_BOTTLE)));
					player.incrementStat(Stats.USE_CAULDRON);
					player.incrementStat(Stats.USED.getOrCreateStat(stack.getItem()));
					world.setBlockState(pos, (BlockState) state.cycle(CopperLeveledCauldronBlock.LEVEL));
					world.playSound((PlayerEntity) null, pos, SoundEvents.ITEM_BOTTLE_EMPTY, SoundCategory.BLOCKS, 1.0F,
							1.0F);
					world.emitGameEvent((Entity) null, GameEvent.FLUID_PLACE, pos);
				}

				return ActionResult.success(world.isClient());
			} else {
				return ActionResult.PASS;
			}
		});
		for (int i = 0; i < ModItems.FLASK_ITEM_LIST.size(); i++) {
			PURIFIED_WATER_COPPER_CAULDRON_BEHAVIOR.put(ModItems.FLASK_ITEM_LIST.get(i),
					(state, world, pos, player, hand, stack) -> {
						if (!world.isClient()) {
							boolean playerIsSneaking = player.isSneaking();
							Item item = stack.getItem();

							NbtCompound nbt;
							if (!stack.hasNbt()) {
								nbt = new NbtCompound();
								nbt.putInt(LeatherFlask.TAG_WATER, 2 + ((LeatherFlask) item).maxFillLevel);
								nbt.putInt(LeatherFlask.TAG_WATER_KIND, 0);
							} else
								nbt = stack.getNbt();
							stack.setNbt(nbt);

							// Fill cauldron
							if (playerIsSneaking) {
								if (!LeatherFlask.isFlaskEmpty(stack))
									if (!((CopperLeveledCauldronBlock) state.getBlock()).isFull(state)) {
										nbt.putInt(LeatherFlask.TAG_WATER, nbt.getInt(LeatherFlask.TAG_WATER) - 1);
										if (nbt.getInt(LeatherFlask.TAG_WATER_KIND) == 0)
											world.setBlockState(pos,
													(BlockState) state.cycle(CopperLeveledCauldronBlock.LEVEL));
										else
											world.setBlockState(pos,
													ModBlocks.COPPER_WATER_CAULDRON_BLOCK.getDefaultState().with(
															CopperLeveledCauldronBlock.LEVEL,
															state.get(CopperLeveledCauldronBlock.LEVEL) + 1));
										world.playSound((PlayerEntity) null, pos, ModSounds.EMPTY_FLASK_EVENT,
												SoundCategory.BLOCKS, 1.0F, 1.0F);
										world.emitGameEvent((Entity) null, GameEvent.FLUID_PLACE, pos);
									} else
										return ActionResult.CONSUME;
							} else {
								// Fill flask
								if (!LeatherFlask.isFlaskFull(stack)) {
									nbt.putInt(LeatherFlask.TAG_WATER, nbt.getInt(LeatherFlask.TAG_WATER) + 1);
									if (nbt.getInt(LeatherFlask.TAG_WATER_KIND) != 0)
										nbt.putInt(LeatherFlask.TAG_WATER_KIND, 1);

									CopperLeveledCauldronBlock.decrementFluidLevel(state, world, pos);
									world.playSound((PlayerEntity) null, pos, ModSounds.FILL_FLASK_EVENT,
											SoundCategory.BLOCKS, 1.0F, 1.0F);
									world.emitGameEvent((Entity) null, GameEvent.FLUID_PICKUP, pos);
								} else
									return ActionResult.CONSUME;

							}
							stack.setNbt(nbt);
							player.incrementStat(Stats.USE_CAULDRON);
							player.incrementStat(Stats.USED.getOrCreateStat(item));
						}

						return ActionResult.success(world.isClient());
					});
		}
		registerBucketBehavior(PURIFIED_WATER_COPPER_CAULDRON_BEHAVIOR);
	}

	static void registerBucketBehavior(Map<Item, CopperCauldronBehavior> behavior) {
		behavior.put(Items.WATER_BUCKET, FILL_WITH_WATER);
		behavior.put(Items.POWDER_SNOW_BUCKET, FILL_WITH_POWDER_SNOW);
	}

	static ActionResult emptyCauldron(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand,
			ItemStack stack, ItemStack output, Predicate<BlockState> predicate,
			SoundEvent soundEvent) {
		if (!predicate.test(state)) {
			return ActionResult.PASS;
		} else {
			if (!world.isClient()) {
				Item item = stack.getItem();
				player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, output));
				player.incrementStat(Stats.USE_CAULDRON);
				player.incrementStat(Stats.USED.getOrCreateStat(item));
				world.setBlockState(pos, ModBlocks.COPPER_CAULDRON_BLOCK.getDefaultState());
				world.playSound((PlayerEntity) null, pos, soundEvent, SoundCategory.BLOCKS, 1.0F, 1.0F);
				world.emitGameEvent((Entity) null, GameEvent.FLUID_PICKUP, pos);
			}

			return ActionResult.success(world.isClient());
		}
	}

	static ActionResult fillCauldron(World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack,
			BlockState state, SoundEvent soundEvent) {
		if (!world.isClient()) {
			Item item = stack.getItem();
			player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, new ItemStack(Items.BUCKET)));
			player.incrementStat(Stats.FILL_CAULDRON);
			player.incrementStat(Stats.USED.getOrCreateStat(item));
			world.setBlockState(pos, state);
			world.playSound((PlayerEntity) null, pos, soundEvent, SoundCategory.BLOCKS, 1.0F, 1.0F);
			world.emitGameEvent((Entity) null, GameEvent.FLUID_PLACE, pos);
		}

		return ActionResult.success(world.isClient());
	}
}
