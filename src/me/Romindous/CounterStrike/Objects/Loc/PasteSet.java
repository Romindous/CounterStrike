package me.Romindous.CounterStrike.Objects.Loc;

import me.Romindous.CounterStrike.Objects.Map.TileSet;
import ru.komiss77.modules.world.XYZ;

public class PasteSet {
	
	public final TileSet ts;
	public final byte rtt;
	public final XYZ loc;
	
	public PasteSet(final TileSet ts, final byte rtt, final XYZ loc) {
		this.ts = ts;
		this.rtt = rtt;
		this.loc = loc;
	}
	
	@Override
	public String toString() {
		return ts.toString() + " r-" + rtt + " loc-" + loc.toString();
	}
}
