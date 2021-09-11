package me.Romindous.CounterStrike.Objects;

import net.minecraft.core.BaseBlockPosition;
import org.bukkit.World;
import org.bukkit.block.Block;
 
public class SmplLoc extends BaseBlockPosition {

	public final World w;
	public short cnt;
   
	public SmplLoc(final Block b, final short i) {
		super(b.getX(), b.getY(), b.getZ());
		this.w = b.getWorld();
		this.cnt = i;
	}
	
	@Override
	public boolean equals(final Object o) {
		return super.equals(o);
	}
   
	public Block getBlock() {
		return this.w.getBlockAt(getX(), getY(), getZ());
	}
}