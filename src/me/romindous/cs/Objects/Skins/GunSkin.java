package me.romindous.cs.Objects.Skins;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import me.romindous.cs.Enums.GunType;

public class GunSkin {
	
	public final String chosen;
	public final Set<String> unlocked;
	
	public GunSkin() {
		this(GunType.DEF_MDL, new String[] {GunType.DEF_MDL});
	}
	
	public GunSkin(final String chosen, final String... unlocked) {
		this(chosen, new HashSet<>(Arrays.asList(unlocked)));
	}

	public GunSkin(final String chosen, final Set<String> unlocked) {
		this.chosen = chosen; this.unlocked = unlocked;
	}
	
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder(unlocked.size() << 2);
		for (final String s : unlocked) sb.append(":").append(s);
		return chosen + ";" + (sb.isEmpty() ? GunType.DEF_MDL : sb.substring(1));
	}
	
	public static GunSkin fromString(final String data) {
		final String chs = data.substring(0, data.indexOf(';'));
		final String[] has = data.substring(data.indexOf(';') + 1).split(":");
		return new GunSkin(chs, has);
	}

	public boolean has(final String mdl) {
		return unlocked.contains(mdl);
	}
}
