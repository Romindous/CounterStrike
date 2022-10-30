package me.Romindous.CounterStrike.Objects;

import me.Romindous.CounterStrike.Enums.TileSet;
import net.minecraft.core.BaseBlockPosition;

public class PasteSet {
	
	public final TileSet ts;
	public final byte rtt;
	public final BaseBlockPosition loc;
	
	public PasteSet(final TileSet ts, final byte rtt, final BaseBlockPosition loc) {
		this.ts = ts;
		this.rtt = rtt;
		this.loc = loc;
	}
	
	@Override
	public String toString() {
		return ts.toString() + " r-" + rtt + " loc-" + loc.toString();
	}
}
