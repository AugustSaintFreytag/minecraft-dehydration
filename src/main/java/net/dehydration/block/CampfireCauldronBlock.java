package net.dehydration.block;

import org.jetbrains.annotations.Nullable;

import net.dehydration.block.entity.CampfireCauldronEntity;
import net.dehydration.hydration.HydrationUtil;
import net.dehydration.item.LeatherFlaskItem;
import net.dehydration.mod.ModBlocks;
import net.dehydration.mod.ModItems;
import net.dehydration.mod.ModSounds;
import net.dehydration.utils.PlayerInventoryUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.event.GameEvent;

public class CampfireCauldronBlock extends Block implements BlockEntityProvider {
	public static final DirectionProperty FACING;
	public static final IntProperty LEVEL;
	private static final VoxelShape Z_BASE_SHAPE;
	private static final VoxelShape X_BASE_SHAPE;
	private static final VoxelShape CAULDRON_SHAPE;

	private static final int MAX_LEVEL = 4;

	public CampfireCauldronBlock(AbstractBlock.Settings settings) {
		super(settings);
		this.setDefaultState((BlockState) ((BlockState) this.stateManager.getDefaultState()).with(LEVEL, 0));
	}

	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new CampfireCauldronEntity(pos, state);
	}

	@Nullable @Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
		return checkType(type, ModBlocks.CAMPFIRE_CAULDRON_ENTITY,
				world.isClient() ? CampfireCauldronEntity::clientTick : CampfireCauldronEntity::serverTick);
	}

	@Override
	public BlockState getPlacementState(ItemPlacementContext itemPlacementContext) {
		return (BlockState) this.getDefaultState().with(FACING, itemPlacementContext.getHorizontalPlayerFacing().rotateYClockwise());
	}

	@Override
	public BlockState rotate(BlockState state, BlockRotation rotation) {
		return (BlockState) state.with(FACING, rotation.rotate((Direction) state.get(FACING)));
	}

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		Direction direction = (Direction) state.get(FACING);
		return direction.getAxis() == Direction.Axis.X ? X_BASE_SHAPE : Z_BASE_SHAPE;
	}

	@Override
	public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
		if (world.getBlockState(pos.down()).isIn(BlockTags.CAMPFIRES)) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
		ItemStack itemStack = player.getStackInHand(hand);
		CampfireCauldronEntity campfireCauldronEntity = (CampfireCauldronEntity) world.getBlockEntity(pos);

		if (itemStack.isEmpty()) {
			return ActionResult.PASS;
		}

		var level = state.get(LEVEL);
		var item = itemStack.getItem();

		// Empty Bucket
		if (item == Items.BUCKET) {
			if (level == MAX_LEVEL && !world.isClient()) {
				if (!player.isCreative()) {
					itemStack.decrement(1);
					PlayerInventoryUtil.giveItemStackToPlayer(player, new ItemStack(ModItems.PURIFIED_WATER_BUCKET));
				}

				this.setLevel(world, pos, state, 0);
				world.playSound((PlayerEntity) null, pos, SoundEvents.ITEM_BUCKET_FILL, SoundCategory.BLOCKS, 1.0F, 1.0F);
			}

			return ActionResult.success(world.isClient());
		}

		// Empty Bottle
		if (item == Items.GLASS_BOTTLE) {
			if (level > 0 && !world.isClient()) {
				if (!player.isCreative()) {
					itemStack.decrement(1);

					if (this.isPurifiedWater(world, pos)) {
						PlayerInventoryUtil.giveItemStackToPlayer(player, new ItemStack(ModItems.PURIFIED_WATER_BOTTLE));
					} else {
						PlayerInventoryUtil.giveItemStackToPlayer(player, PotionUtil.setPotion(new ItemStack(Items.POTION), Potions.WATER));
					}
				}

				world.playSound((PlayerEntity) null, pos, SoundEvents.ITEM_BOTTLE_FILL, SoundCategory.BLOCKS, 1.0F, 1.0F);
				this.setLevel(world, pos, state, level - 1);
			}

			return ActionResult.success(world.isClient());
		}

		// Water Bucket
		if (item == Items.WATER_BUCKET || item == ModItems.PURIFIED_WATER_BUCKET) {
			if (level < MAX_LEVEL && !world.isClient()) {
				if (!player.isCreative()) {
					player.setStackInHand(hand, new ItemStack(Items.BUCKET));
				}

				var isPurified = (item == ModItems.PURIFIED_WATER_BUCKET);
				campfireCauldronEntity.onFillingCauldron(isPurified);
				this.setLevel(world, pos, state, MAX_LEVEL);
				world.playSound((PlayerEntity) null, pos, SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.BLOCKS, 1.0F, 1.0F);
			}

			return ActionResult.success(world.isClient());
		}

		// Water Bottle
		if (item == ModItems.PURIFIED_WATER_BOTTLE || (item == Items.POTION && PotionUtil.getPotion(itemStack) == Potions.WATER)) {
			if (level < MAX_LEVEL && !world.isClient()) {
				if (!player.isCreative()) {
					itemStack.decrement(1);

					if (itemStack.isEmpty()) {
						var emptyBottleItemStack = new ItemStack(Items.GLASS_BOTTLE);
						PlayerInventoryUtil.giveItemStackToPlayer(player, emptyBottleItemStack);
					}
				}

				world.playSound((PlayerEntity) null, pos, SoundEvents.ITEM_BOTTLE_EMPTY, SoundCategory.BLOCKS, 1.0F, 1.0F);

				var isPurified = !HydrationUtil.isContaminatedItemStack(itemStack);
				campfireCauldronEntity.onFillingCauldron(isPurified);

				this.setLevel(world, pos, state, level + 1);
			}

			return ActionResult.success(world.isClient());
		}

			NbtCompound tags = itemStack.getNbt();
		if (item instanceof LeatherFlaskItem && level > 0) {
			if (tags != null && tags.getInt(LeatherFlaskItem.TAG_WATER) < 2 + ((LeatherFlaskItem) item).maxFillLevel) {
				if (this.isPurifiedWater(world, pos)) {
					if ((tags.getInt(LeatherFlaskItem.TAG_WATER_KIND) == 0 || tags.getInt(LeatherFlaskItem.TAG_WATER) == 0)) {
						tags.putInt(LeatherFlaskItem.TAG_WATER_KIND, 0);
					} else {
						tags.putInt(LeatherFlaskItem.TAG_WATER_KIND, 1);
					}
				} else {
					tags.putInt(LeatherFlaskItem.TAG_WATER_KIND, 2);
				}

				tags.putInt(LeatherFlaskItem.TAG_WATER, tags.getInt(LeatherFlaskItem.TAG_WATER) + 1);
				this.setLevel(world, pos, state, level - 1);
				world.playSound((PlayerEntity) null, pos, ModSounds.FILL_FLASK_EVENT, SoundCategory.NEUTRAL, 1.0F, 1.0F);

				return ActionResult.success(world.isClient());
			} else
				return ActionResult.PASS;

		} else {
			return ActionResult.PASS;
		}
	}

	public void setLevel(World world, BlockPos pos, BlockState state, int level) {
		world.setBlockState(pos, (BlockState) state.with(LEVEL, MathHelper.clamp(level, 0, MAX_LEVEL)), 2);
		world.updateComparators(pos, this);
	}

	@Override
	public void precipitationTick(BlockState state, World world, BlockPos pos, Biome.Precipitation precipitation) {
		if (world.getRandom().nextFloat() < 0.2f && world.isSkyVisible(pos) && world.getBiome(pos).value().getTemperature() >= 0.15F
				&& world.getBiome(pos).value().getTemperature() < 2F && precipitation == Biome.Precipitation.RAIN
				&& state.get(LEVEL) < MAX_LEVEL) {
			this.setLevel(world, pos, state, state.get(LEVEL) + 1);
			world.emitGameEvent((Entity) null, GameEvent.FLUID_PLACE, pos);
		}
	}

	@Override
	public boolean hasComparatorOutput(BlockState state) {
		return true;
	}

	@Override
	public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
		return (Integer) state.get(LEVEL);
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(LEVEL);
		builder.add(FACING);
	}

	@Override
	public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
		return false;
	}

	@Override @Environment(EnvType.CLIENT)
	public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
		if (random.nextInt(12) == 0 && this.isFireBurning(world, pos) && (Integer) state.get(LEVEL) > 0) {
			var pitch = random.nextFloat() * 0.3F + 0.5F;
			var isPurified = this.isPurifiedWater(world, pos);

			if (isPurified) {
				pitch += 0.35F;
			}

			world.playSound((double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.5D,
					ModSounds.CAULDRON_BUBBLE_EVENT, SoundCategory.BLOCKS, 0.5F, pitch, false);
		}
	}

	public boolean isFull(BlockState state) {
		return (Integer) state.get(LEVEL) == MAX_LEVEL;
	}

	public boolean isFireBurning(World world, BlockPos pos) {
		if (world.getBlockState(pos.down()).getBlock() instanceof CampfireBlock
				&& CampfireBlock.isLitCampfire(world.getBlockState(pos.down()))) {
			return true;
		} else
			return false;
	}

	public boolean isPurifiedWater(World world, BlockPos pos) {
		if (((CampfireCauldronEntity) world.getBlockEntity(pos) != null)) {
			return ((CampfireCauldronEntity) world.getBlockEntity(pos)).isPurified;
		} else
			return false;

	}

	@SuppressWarnings("unchecked")
	protected static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> checkType(BlockEntityType<A> givenType,
			BlockEntityType<E> expectedType, BlockEntityTicker<? super E> ticker) {
		return expectedType == givenType ? (BlockEntityTicker<A>) ticker : null;
	}

	static {
		LEVEL = IntProperty.of("level", 0, MAX_LEVEL);
		FACING = HorizontalFacingBlock.FACING;
		CAULDRON_SHAPE = VoxelShapes.union(createCuboidShape(11, 1, 4, 12, 6, 5), createCuboidShape(4, 0, 4, 12, 1, 12),
				createCuboidShape(3, 1, 4, 4, 6, 12), createCuboidShape(12, 1, 4, 13, 6, 12), createCuboidShape(4, 1, 12, 12, 6, 13),
				createCuboidShape(4, 1, 3, 12, 6, 4), createCuboidShape(4, 1, 4, 5, 6, 5), createCuboidShape(11, 1, 11, 12, 6, 12),
				Block.createCuboidShape(4, 1, 11, 5, 6, 12));
		Z_BASE_SHAPE = VoxelShapes.union(CAULDRON_SHAPE, createCuboidShape(7, -15, 0, 9, 14, 1), createCuboidShape(7, 14, -1, 9, 16, 1),
				createCuboidShape(7, 14, 15, 9, 16, 17), createCuboidShape(7, -15, 15, 9, 14, 16),
				Block.createCuboidShape(7, 14, 1, 9, 15, 15));
		X_BASE_SHAPE = VoxelShapes.union(CAULDRON_SHAPE, createCuboidShape(15, -15, 7, 16, 14, 9), createCuboidShape(15, 14, 7, 17, 16, 9),
				createCuboidShape(-1, 14, 7, 1, 16, 9), createCuboidShape(0, -15, 7, 1, 14, 9),
				Block.createCuboidShape(1, 14, 7, 15, 15, 9));
	}

}
