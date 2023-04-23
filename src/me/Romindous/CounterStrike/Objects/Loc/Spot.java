package me.Romindous.CounterStrike.Objects.Loc;

import org.bukkit.World;
import org.bukkit.block.Block;

import me.Romindous.CounterStrike.Game.Arena.Team;
import net.minecraft.core.BaseBlockPosition;

public class Spot extends BaseBlockPosition {
	
	public final Team tm;
	public final int[] relPos;
	private final int index;

	/*public Spot(final Block b, final int ix, final Team tm, final int[] rp) {
		super(b.getX(), b.getY(), b.getZ());
		this.tm = tm;
		relPos = rp;
		index = ix;
	}*/

	public Spot(final Spot noRp, final int ix, final int rpl) {
		super(noRp.u(), noRp.v(), noRp.w());
		this.tm = noRp.tm;
		relPos = new int[rpl];
		index = ix;
	}

	public Spot(final int x, final int y, final int z, final int ix, final Team tm, final int[] rp) {
		super(x, y, z);
		this.tm = tm;
		relPos = rp;
		index = ix;
	}
	
	public void setPosFrom(final Spot sp, final int ps) {
		relPos[sp.index] = ps;
	}
	
	public int getPosFrom(final Spot sp) {
		return relPos[sp.index];
	}
	
	public boolean noPosFrom(final Spot sp) {
		return !sp.equals(this) && relPos[sp.index] == 0;
	}

	public Block getBlock(final World w) {
		return w.getBlockAt(u(), v(), w());
	}
	
	@Override
	public String toString() {
		return u()+","+v()+","+w()+","+tm.clr+tm.toString()+","+index;
	}
}
