package me.Romindous.CounterStrike.Objects;

import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
 
public class BrknBlck extends SmplLoc {
	
	public final BlockData bd;
   
	public BrknBlck(final Block b) {
		super(b.getX(), b.getY(), b.getZ(), b.getWorld(), (short) 0);
		this.bd = b.getBlockData().clone();
	}
	
	@Override
	public boolean equals(final Object o) {
		return super.equals(o);
	}
}