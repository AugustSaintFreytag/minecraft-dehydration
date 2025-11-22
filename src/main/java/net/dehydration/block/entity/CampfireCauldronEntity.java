package net.dehydration.block.entity;

import net.dehydration.block.CampfireCauldronBlock;
import net.dehydration.mod.ModBlocks;
import net.dehydration.mod.ModConfig;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CampfireCauldronEntity extends BlockEntity {

	// Configuration

	private static final String IS_PURIFIED_NBT_KEY = "Purified";

	// State

	public boolean isPurified;

	private int ticker;

	// Init

	public CampfireCauldronEntity(BlockPos pos, BlockState state) {
		super(ModBlocks.CAMPFIRE_CAULDRON_ENTITY, pos, state);
	}

	// NBT

	@Override
	public void readNbt(NbtCompound tag) {
		super.readNbt(tag);
		this.isPurified = tag.getBoolean(IS_PURIFIED_NBT_KEY);
	}

	@Override
	public void writeNbt(NbtCompound tag) {
		super.writeNbt(tag);
		tag.putBoolean(IS_PURIFIED_NBT_KEY, isPurified);
	}

	@Override
	public void markDirty() {
		super.markDirty();
		sendUpdate();
	}

	// Tick

	public static void clientTick(World world, BlockPos pos, BlockState state, CampfireCauldronEntity blockEntity) {
		blockEntity.update();
	}

	public static void serverTick(World world, BlockPos pos, BlockState state, CampfireCauldronEntity blockEntity) {
		blockEntity.update();
	}

	// Update

	public void update() {
		CampfireCauldronBlock campfireCauldronBlock = (CampfireCauldronBlock) this.getCachedState().getBlock();
		if (campfireCauldronBlock.isFireBurning(world, pos) && this.getCachedState().get(CampfireCauldronBlock.LEVEL) > 0
				&& !this.isPurified) {
			this.ticker++;
			if (this.ticker >= ModConfig.CONFIG.waterBoilingTime) {
				this.isPurified = true;
				this.ticker = 0;
			}
		}
	}

	private void sendUpdate() {
		if (this.world != null) {
			BlockState state = this.world.getBlockState(this.pos);
			(this.world).updateListeners(this.pos, state, state, 3);
		}
	}

	public void onFillingCauldron() {
		onFillingCauldron(false);
	}

	public void onFillingCauldron(boolean isPurified) {
		this.isPurified = this.isPurified && isPurified;
		this.ticker = 0;
		this.markDirty();
	}

}
