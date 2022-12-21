package me.Romindous.CounterStrike.Enums;

import java.util.EnumSet;
import java.util.Iterator;

import org.bukkit.Material;

public enum TileType {
	
	WALL(true, Material.POLISHED_ANDESITE, -1, 0),
	OPEN(true, Material.POLISHED_ANDESITE, 0, 0),
	BOX(true, Material.POLISHED_ANDESITE, 1, 0),
	UPSTS(false, Material.POLISHED_ANDESITE, 1, 0),
	DWNSTS(false, Material.POLISHED_ANDESITE, 1, 0),
	HGSTS(true, Material.POLISHED_ANDESITE, 0, 1),
	HGBOX(true, Material.POLISHED_ANDESITE, 0, 2),
	HGWALL(true, Material.POLISHED_ANDESITE, 0, 3);
	//=-=-=-=-
	//VERY IMPORTANT - besides (0, 0), there can be no other same-number noises, like (1,1), since that will create impossible cases
	//=-=-=-=-
	public final boolean generate;
	public final Material floorMat;
	public final int[] noise;
	
	public static final EnumSet<TileType> gns = getGens(EnumSet.allOf(TileType.class));
	
	private TileType(final boolean generate, final Material floor, final int... noise) {
		this.noise = noise;
		this.floorMat = floor;
		this.generate = generate;
	}
	
	public boolean canPlaceNear(final TileType near, final int dst) {
		final int[] fst = noise;
		final int[] scd = near.noise;
		final int len = fst.length;
		if (len != scd.length) {
			return false;
		}
		//Bukkit.getConsoleSender().sendMessage("comparing t-" + Arrays.toString(ns) + " to t-" + Arrays.toString(tt.ns) + " ");
		int difference = 0;
		for (int i = 0; i < len; i++) {
			difference += Math.abs(fst[i] - scd[i]);
		}
		//Bukkit.getConsoleSender().sendMessage("df-" + df + " dst-" + dst);
		return difference <= dst;
	}
	
	private static EnumSet<TileType> getGens(final EnumSet<TileType> all) {
		final Iterator<TileType> it = all.iterator();
		while (it.hasNext()) {
			if (!it.next().generate) it.remove();
		}
		return all;
	}

	public static int getNoiseDiff() {
		int min = 0;
		int max = 0;
		for (final TileType tile : gns) {
			for (final int n : tile.noise) {
				min = n < min ? n : min;
				max = n > max ? n : max;
			}
		}
		return max - min + 1;
	}
}
