package me.Romindous.CounterStrike.Objects.Loc;

import me.Romindous.CounterStrike.Enums.TileSet;
import ru.komiss77.modules.world.XYZ;

public record PasteSet(TileSet set, byte rtt, XYZ loc) {

	@Override
	public String toString() {
		return set.toString() + " r-" + rtt + " loc-" + loc.toString();
	}

}
