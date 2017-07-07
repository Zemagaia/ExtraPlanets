package com.mjr.extraplanets.planets.Eris.worldgen;

import java.util.Random;

import micdoodle8.mods.galacticraft.core.blocks.BlockTier1TreasureChest;
import micdoodle8.mods.galacticraft.core.world.gen.dungeon.DungeonConfiguration;
import micdoodle8.mods.galacticraft.core.world.gen.dungeon.RoomTreasure;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;

import com.mjr.extraplanets.blocks.ExtraPlanets_Blocks;
import com.mjr.extraplanets.blocks.treasureChest.T10TreasureChest;

public class RoomTreasureEris extends RoomTreasure {
	public RoomTreasureEris() {
	}

	public RoomTreasureEris(DungeonConfiguration configuration, Random rand, int blockPosX, int blockPosZ, EnumFacing entranceDir) {
		super(configuration, rand, blockPosX, blockPosZ, rand.nextInt(4) + 6, configuration.getRoomHeight(), rand.nextInt(4) + 6, entranceDir);
	}

	public RoomTreasureEris(DungeonConfiguration configuration, Random rand, int blockPosX, int blockPosZ, int sizeX, int sizeY, int sizeZ, EnumFacing entranceDir) {
		super(configuration, rand, blockPosX, blockPosZ, sizeX, sizeY, sizeZ, entranceDir);
	}

	@Override
	public boolean addComponentParts(World worldIn, Random random, StructureBoundingBox boundingBox) {
		for (int i = 0; i <= this.sizeX; i++) {
			for (int j = 0; j <= this.sizeY; j++) {
				for (int k = 0; k <= this.sizeZ; k++) {
					if (i == 0 || i == this.sizeX || j == 0 || j == this.sizeY || k == 0 || k == this.sizeZ) {
						boolean placeBlock = true;
						if (getDirection().getAxis() == EnumFacing.Axis.Z) {
							int start = (this.boundingBox.maxX - this.boundingBox.minX) / 2 - 1;
							int end = (this.boundingBox.maxX - this.boundingBox.minX) / 2 + 1;
							if (i > start && i <= end && j < 3 && j > 0) {
								if (getDirection() == EnumFacing.SOUTH && k == 0) {
									placeBlock = false;
								} else if (getDirection() == EnumFacing.NORTH && k == this.sizeZ) {
									placeBlock = false;
								}
							}
						} else {
							int start = (this.boundingBox.maxZ - this.boundingBox.minZ) / 2 - 1;
							int end = (this.boundingBox.maxZ - this.boundingBox.minZ) / 2 + 1;
							if (k > start && k <= end && j < 3 && j > 0) {
								if (getDirection() == EnumFacing.EAST && i == 0) {
									placeBlock = false;
								} else if (getDirection() == EnumFacing.WEST && i == this.sizeX) {
									placeBlock = false;
								}
							}
						}
						if (placeBlock) {
							this.setBlockState(worldIn, this.configuration.getBrickBlock(), i, j, k, boundingBox);
						} else {
							this.setBlockState(worldIn, Blocks.air.getDefaultState(), i, j, k, boundingBox);
						}
					} else if ((i == 1 && k == 1) || (i == 1 && k == this.sizeZ - 1) || (i == this.sizeX - 1 && k == 1) || (i == this.sizeX - 1 && k == this.sizeZ - 1)) {
						this.setBlockState(worldIn, Blocks.glowstone.getDefaultState(), i, j, k, boundingBox);
					} else if (i == this.sizeX / 2 && j == 1 && k == this.sizeZ / 2) {
						this.setBlockState(worldIn, ExtraPlanets_Blocks.treasureChestTier10.getDefaultState().withProperty(BlockTier1TreasureChest.FACING, this.getDirection().getOpposite()), i, j, k, boundingBox);
					} else {
						this.setBlockState(worldIn, Blocks.air.getDefaultState(), i, j, k, boundingBox);
					}
				}
			}
		}

		return true;
	}
}