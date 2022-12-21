package me.Romindous.CounterStrike.Objects.Loc;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import ru.komiss77.modules.world.XYZ;
 
public class WXYZ extends XYZ {

	public final World w;
   
	public WXYZ(final Block b, final int i) {
		super(b.getWorld().getName(), b.getX(), b.getY(), b.getZ());
		this.w = b.getWorld();
		this.pitch = i;
	}
	   
	public WXYZ(final Block b, final int i, final int j) {
		x = b.getX();
		y = b.getY();
		z = b.getZ();
		this.w = b.getWorld();
		this.pitch = i;
		this.yaw = j;
	}
	
	public WXYZ(int x, int y, int z, final World w, final int i) {
		this.x = x;
		this.y = y; 
		this.z = z;
		this.w = w;
		this.pitch = i;
	}
	
	public WXYZ(int x, int y, int z, final World w, final int i, final int j) {
		this.x = x;
		this.y = y; 
		this.z = z;
		this.w = w;
		this.pitch = i;
		this.yaw = j;
	}
   
	public Block getBlock() {
		return this.w.getBlockAt(x, y, z);
	}
	
	@Override
	public Location getCenterLoc() {
		return new Location(w, x + 0.5d, y + 0.5d, z + 0.5d);
	}
	
	@Override
	public boolean equals(final Object obj) {
		return super.equals(obj);
	}
	
	@Override
	public int hashCode() {
		return super.hashCode();
	}
}