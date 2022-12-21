package me.Romindous.CounterStrike.Objects.Skins;

import me.Romindous.CounterStrike.Enums.GunType;

public class GunSkin {
	
	public final int chosen;
	public final int[] unlocked;
	
	public GunSkin() {
		this.chosen = GunType.defCMD;
		this.unlocked = new int[] {GunType.defCMD};
	}
	
	public GunSkin(final int chosen, final int... unlocked) {
		this.chosen = chosen;
		this.unlocked = unlocked;
	}
	
	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer(unlocked.length << 2);
		for (final int s : unlocked) {
			sb.append(":" + String.valueOf(s));
		}
		return chosen + ";" + (sb.isEmpty() ? String.valueOf(GunType.defCMD) : sb.substring(1));
	}
	
	public static GunSkin fromString(final String data) {
		int chs = parseInt(data.substring(0, data.indexOf(';')));
		final String[] has = data.substring(data.indexOf(';') + 1).split(":");
		final int[] ns = new int[has.length];
		for (int i = ns.length - 1; i >= 0; i--) {
			ns[i] = parseInt(has[i]);
		}
		return new GunSkin(chs, ns);
	}
	
	private static int parseInt(final String n) {
		try {
			return Integer.parseInt(n);
		} catch (final NumberFormatException e) {
			return 0;
		}
	}

	public boolean has(int mdl) {
		for (final int m : unlocked) {
			if (mdl == m) return true;
		}
		return false;
	}
}
