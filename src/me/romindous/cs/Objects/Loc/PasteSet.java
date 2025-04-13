package me.romindous.cs.Objects.Loc;

import me.romindous.cs.Enums.TileSet;
import ru.komiss77.modules.world.BVec;

public record PasteSet(TileSet set, byte rtt, BVec loc) {
	public String toString() {
		return set.toString() + " r-" + rtt + " loc-" + loc.toString();
	}
}
