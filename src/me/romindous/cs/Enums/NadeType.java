package me.romindous.cs.Enums;

import java.util.Locale;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public enum NadeType {
	
	FRAG(true, 6, 200, "§f鉬", 36),
	FLAME(true, 20, 150, "§f鉫", 37, BlockFace.UP),
	SMOKE(false, 28, 80, "§f鉪", 46),
	FLASH(false, 4, 50, "§f鉩", 38, BlockFace.WEST, BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH),
	DECOY(false, 10, 20, "§f鉭", 47);

	public final String name;
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
	public static final String NADES = "nade";
	
	NadeType(final boolean prm, final int time, final int prc, final String icn, final int slt, final BlockFace... popOn) {
		this.name = name().toLowerCase(Locale.ENGLISH);
		this.time = (short) time;
		this.prm = prm;
		this.prc = (short) prc;
		this.icn = icn;
		this.snd = NADES + "." + name;
		this.slt = (byte) slt;
		this.popOn = popOn;
	}

	public boolean hasPopFace(final BlockFace bf) {
		for (final BlockFace f : popOn)
			if (bf == f) return true;
		return false;
	}

	public String skin() {
		return NADES + "/" + name;
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
}