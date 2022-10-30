package me.Romindous.CounterStrike.Objects;

import org.bukkit.World;
import org.bukkit.block.Block;

import net.minecraft.core.BaseBlockPosition;
 
public class SmplLoc {

	public final World w;
	public final int x;
	public final int y;
	public final int z;
	public short cnt;
   
	public SmplLoc(final Block b, final short i) {
		x = b.getX();
		y = b.getY(); 
		z = b.getZ();
		this.w = b.getWorld();
		this.cnt = i;
	}
	
	public SmplLoc(int x, int y, int z, final World w, final short i) {
		this.x = x;
		this.y = y; 
		this.z = z;
		this.w = w;
		this.cnt = i;
	}

	@Override
	public boolean equals(final Object o) {
		if (o instanceof SmplLoc) {
			final SmplLoc ol = (SmplLoc) o;
			return x == ol.x && y == ol.y && z == ol.z && w.getName().equals(ol.w.getName());
		} else if (o instanceof BaseBlockPosition) {
			final BaseBlockPosition ol = (BaseBlockPosition) o;
			return x == ol.u() && y == ol.v() && z == ol.w();
		}
		
		return false;
	}
   
	public Block getBlock() {
		return this.w.getBlockAt(x, y, z);
	}
}