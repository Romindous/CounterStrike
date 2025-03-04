package me.Romindous.CounterStrike.Objects.Loc;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

import ru.komiss77.modules.world.BVec;
import ru.komiss77.version.Nms;

public class Broken extends BVec {
	
	public final BlockData bd;
   
	public Broken(final Block bl) {
		super(bl.getX(), bl.getY(), bl.getZ());
		this.bd = bl.getBlockData();
	}
}