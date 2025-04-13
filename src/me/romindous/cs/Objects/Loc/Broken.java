package me.romindous.cs.Objects.Loc;

import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

import ru.komiss77.modules.world.BVec;

public class Broken extends BVec {
	
	public final BlockData bd;
   
	public Broken(final Block bl) {
		super(bl.getX(), bl.getY(), bl.getZ());
		this.bd = bl.getBlockData();
	}
}