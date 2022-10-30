package me.Romindous.CounterStrike.Enums;

import org.bukkit.inventory.ItemStack;

public enum NadeType {
	
	FRAG(true, true, (short)360, "§f鉬", (byte)36),
	FLAME(false, true, (short)240, "§f鉫", (byte)37),
	SMOKE(true, false, (short)100, "§f鉪", (byte)46),
	FLASH(true, false, (short)60, "§f鉩", (byte)38),
	DECOY(true, false, (short)20, "§f鉭", (byte)47);
	
	public final boolean flrbnc;
	public final boolean prm;
	public final short prc;
	public final String icn;
	public final byte slt;
	
	NadeType(final boolean flrbnc, final boolean prm, final short prc, final String icn, final byte slt) {
		this.flrbnc = flrbnc;
		this.prm = prm;
		this.prc = prc;
		this.icn = icn;
		this.slt = slt;
	}
  
	public static NadeType getNdTp(final ItemStack it) {
		if (it == null) {
			return null;
		}
		switch (it.getType()) {
			case OAK_SAPLING:
				return FRAG;
			case ACACIA_SAPLING:
				return FLAME;
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