package me.Romindous.CounterStrike.Objects.Loc;

import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

import ru.komiss77.modules.world.WXYZ;
 
public class BrknBlck extends WXYZ {
	
	public final BlockData bd;
   
	public BrknBlck(final Block b) {
		super(b, 0);
		this.bd = b.getBlockData().clone();
	}
}