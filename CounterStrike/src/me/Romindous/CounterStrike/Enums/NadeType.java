package me.Romindous.CounterStrike.Enums;

import org.bukkit.inventory.ItemStack;

public enum NadeType {
	FRAG(true, true, "§f鉬"),
	FIRE(false, true, "§f鉫"),
	SMOKE(true, false, "§f鉪"),
	FLASH(true, false, "§f鉩"),
	DECOY(true, false, "§f鉭");
	
	public final boolean flrbnc;
	public final boolean prm;
	public final String icn;
	
	NadeType(boolean flrbnc, boolean prm, String icn) {
		this.flrbnc = flrbnc;
		this.prm = prm;
		this.icn = icn;
	}
  
	public static NadeType getNdTp(ItemStack it) {
		if (it == null) {
			return null;
		}
		switch (it.getType()) {
			case OAK_SAPLING:
				return FRAG;
			case ACACIA_SAPLING:
				return FIRE;
			case DARK_OAK_SAPLING:
				return SMOKE;
			case BIRCH_SAPLING:
				return FLASH;
			case JUNGLE_SAPLING:
				return DECOY;
			default:
				return null;
		}
	}
}