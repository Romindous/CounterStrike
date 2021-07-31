package me.Romindous.CounterStrike.Objects;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

import net.minecraft.core.BaseBlockPosition;
 
public class BrknBlck extends BaseBlockPosition {

	public final World w;
	public final BlockData bd;
   
	public BrknBlck(final Block b) {
		super(b.getX(), b.getY(), b.getZ());
		this.w = b.getWorld();
		this.bd = b.getBlockData().clone();
	}
	
	@Override
	public boolean equals(final Object o) {
		return super.equals(o);
	}
   
	public Block getBlock() {
		return this.w.getBlockAt(getX(), getY(), getZ());
	}
}