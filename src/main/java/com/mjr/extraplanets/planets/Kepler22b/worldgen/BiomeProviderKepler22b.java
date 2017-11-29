package com.mjr.extraplanets.planets.Kepler22b.worldgen;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeCache;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.IntCache;

import com.mjr.extraplanets.planets.Kepler22b.worldgen.biome.BiomeGenBaseKepler22b;
import com.mjr.extraplanets.planets.Kepler22b.worldgen.layer.GenLayerKepler22b;

public class BiomeProviderKepler22b extends BiomeProvider {
	private GenLayer unzoomedBiomes;
	private GenLayer zoomedBiomes;
	private BiomeCache biomeCache;
	private List<Biome> biomesToSpawn;

	protected BiomeProviderKepler22b() {
		this.biomeCache = new BiomeCache(this);
		this.biomesToSpawn = new ArrayList<Biome>();
		this.biomesToSpawn.add(BiomeGenBaseKepler22b.kepler22bPlains);
		this.biomesToSpawn.add(BiomeGenBaseKepler22b.kepler22bBlueForest);
		this.biomesToSpawn.add(BiomeGenBaseKepler22b.kepler22bPurpleForest);
		this.biomesToSpawn.add(BiomeGenBaseKepler22b.kepler22bRedForest);
		this.biomesToSpawn.add(BiomeGenBaseKepler22b.kepler22bYellowForest);
		this.biomesToSpawn.add(BiomeGenBaseKepler22b.kepler22bRedDesert);
	}

	public BiomeProviderKepler22b(long seed) {
		this();
		GenLayer[] agenlayer;
		agenlayer = GenLayerKepler22b.makeTheWorld(seed);
		this.unzoomedBiomes = agenlayer[0];
		this.zoomedBiomes = agenlayer[1];
	}

	public BiomeProviderKepler22b(World world) {
		this(world.getSeed());
	}

	public Biome getBiome() {
		return BiomeGenBaseKepler22b.kepler22bPlains;
	}

	@Override
	public List<Biome> getBiomesToSpawnIn() {
		return this.biomesToSpawn;
	}

	@Override
	public Biome getBiome(BlockPos pos, Biome biomeGen) {
		Biome biome = this.biomeCache.getBiome(pos.getX(), pos.getZ(), biomeGen);

		if (biome == null) {
			return BiomeGenBaseKepler22b.kepler22bPlains;
		}
		return biome;
	}

	@Override
	public float getTemperatureAtHeight(float par1, int par2) {
		return par1;
	}

	@Override
	public Biome[] getBiomesForGeneration(Biome[] par1ArrayOfBiome, int x, int z, int length, int width) {
		int[] arrayOfInts = this.unzoomedBiomes.getInts(x, z, length, width);

		if (par1ArrayOfBiome == null || par1ArrayOfBiome.length < length * width) {
			par1ArrayOfBiome = new Biome[length * width];
		}
		for (int i = 0; i < length * width; i++) {
			if (arrayOfInts[i] >= 0) {
				par1ArrayOfBiome[i] = Biome.getBiome(arrayOfInts[i]);
			} else {
				par1ArrayOfBiome[i] = BiomeGenBaseKepler22b.kepler22bPlains;
			}
		}
		return par1ArrayOfBiome;
	}

    @Override
    public Biome[] getBiomes(@Nullable Biome[] oldBiomeList, int x, int z, int width, int depth)
    {
        return getBiomes(oldBiomeList, x, z, width, depth, true);
    }

    @Override
    public Biome[] getBiomes(@Nullable Biome[] listToReuse, int x, int z, int width, int length, boolean cacheFlag)
    {
        IntCache.resetIntCache();

        if (listToReuse == null || listToReuse.length < length * width)
        {
            listToReuse = new Biome[width * length];
        }

        if (cacheFlag && width == 16 && length == 16 && (x & 15) == 0 && (z & 15) == 0)
        {
            Biome[] cached = this.biomeCache.getCachedBiomes(x, z);
            System.arraycopy(cached, 0, listToReuse, 0, width * length);
            return listToReuse;
        }

        int[] zoomed = zoomedBiomes.getInts(x, z, width, length);

        for (int i = 0; i < width * length; ++i)
        {
            if (zoomed[i] >= 0)
            {
                listToReuse[i] = Biome.getBiome(zoomed[i]);
            }
            else
            {
                listToReuse[i] = this.getBiome();
            }
        }

        return listToReuse;
    }

	@Override
	public boolean areBiomesViable(int par1, int par2, int par3, List<Biome> par4List) {
		int i = par1 - par3 >> 2;
		int j = par2 - par3 >> 2;
		int k = par1 + par3 >> 2;
		int l = par2 + par3 >> 2;
		int i1 = k - i + 1;
		int j1 = l - j + 1;
		int[] ai = this.unzoomedBiomes.getInts(i, j, i1, j1);

		for (int k1 = 0; k1 < i1 * j1; k1++) {
			Biome biomegenbase = Biome.getBiome(ai[k1]);

			if (!par4List.contains(biomegenbase)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public BlockPos findBiomePosition(int par1, int par2, int par3, List<Biome> par4List, Random par5Random) {
		int i = par1 - par3 >> 2;
		int j = par2 - par3 >> 2;
		int k = par1 + par3 >> 2;
		int l = par2 + par3 >> 2;
		int i1 = k - i + 1;
		int j1 = l - j + 1;
		int[] ai = this.unzoomedBiomes.getInts(i, j, i1, j1);
		BlockPos chunkposition = null;
		int k1 = 0;

		for (int l1 = 0; l1 < ai.length; l1++) {
			int i2 = i + l1 % i1 << 2;
			int j2 = j + l1 / i1 << 2;
			Biome biomegenbase = Biome.getBiome(ai[l1]);

			if (par4List.contains(biomegenbase) && (chunkposition == null || par5Random.nextInt(k1 + 1) == 0)) {
				chunkposition = new BlockPos(i2, 0, j2);
				k1++;
			}
		}
		return chunkposition;
	}

	@Override
	public void cleanupCache() {
		this.biomeCache.cleanupCache();
	}
}