package me.Romindous.CounterStrike.Enums;

import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public enum NadeType {
	
	FRAG(true, (short)6, (short)300, "§f鉬", (byte)36),
	FLAME(true, (short)36, (short)250, "§f鉫", (byte)37, BlockFace.UP),
	SMOKE(false, (short)14, (short)100, "§f鉪", (byte)46),
	FLASH(false, (short)4, (short)50, "§f鉩", (byte)38, BlockFace.WEST, BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH),
	DECOY(false, (short)10, (short)20, "§f鉭", (byte)47);
	
	public final boolean prm;
	public final short time;
	public final short prc;
	public final String icn;
	public final byte slt;
	public final BlockFace[] popOn;

	public static final byte prmSlot = 3;
	public static final byte scdSlot = 4;
	public static final short nadeRwd = 160;
	
	NadeType(final boolean prm, final short time, final short prc, final String icn, final byte slt, final BlockFace... popOn) {
		this.time = time;
		this.prm = prm;
		this.prc = prc;
		this.icn = icn;
		this.slt = slt;
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