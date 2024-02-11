package me.Romindous.CounterStrike.Enums;

import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public enum NadeType {
	
	FRAG(true, 6, 300, "§f鉬", "frag", 36),
	FLAME(true, 36, 250, "§f鉫", "flame", 37, BlockFace.UP),
	SMOKE(false, 14, 100, "§f鉪", "smoke", 46),
	FLASH(false, 4, 50, "§f鉩", "flash", 38, BlockFace.WEST, BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH),
	DECOY(false, 10, 20, "§f鉭", "decoy", 47);
	
	public final boolean prm;
	public final short time;
	public final short prc;
	public final String icn;
	public final String snd;
	public final byte slt;
	public final BlockFace[] popOn;

	public static final byte prmSlot = 3;
	public static final byte scdSlot = 4;
	public static final short nadeRwd = 160;
	
	NadeType(final boolean prm, final int time, final int prc, final String icn,
		final String snd, final int slt, final BlockFace... popOn) {
		this.time = (short) time;
		this.prm = prm;
		this.prc = (short) prc;
		this.icn = icn;
		this.snd = snd;
		this.slt = (byte) slt;
		this.popOn = popOn;
	}
  
	public static @Nullable NadeType getNdTp(final ItemStack it) {
		if (it == null) return null;
        return switch (it.getType()) {
            case OAK_SAPLING -> FRAG;
            case ACACIA_SAPLING -> FLAME;
            case DARK_OAK_SAPLING -> SMOKE;
            case BIRCH_SAPLING -> FLASH;
            case JUNGLE_SAPLING -> DECOY;
            default -> null;
        };
	}
	
	public boolean hasPopFace(final BlockFace bf) {
		for (final BlockFace f : popOn)
			if (bf == f) return true;
		return false;
	}
}