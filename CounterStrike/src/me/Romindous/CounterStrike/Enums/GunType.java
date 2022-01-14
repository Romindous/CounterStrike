package me.Romindous.CounterStrike.Enums;

import org.bukkit.inventory.ItemStack;

public enum GunType {
	
	AWP((byte)6, (byte)24, 50.0F, 1.0F, 0.5F, (byte)1, 0.08F, true, (short)60, true, (short)50, (short)1650, "§f鉰鉱", (byte)4, "cs.guns.awp"),
	AK47((byte)30, (byte)3, 5.5F, 0.15F, 0.08F, (byte)0, 0.04F, false, (short)50, true, (short)40, (short)1250, "§f鉲鉳", (byte)1, "cs.guns.ak47"),
	SCAR((byte)14, (byte)10, 7.0F, 0.5F, 0.14F, (byte)3, 0.05F, true, (short)40, true, (short)60, (short)1500, "§f鉺鉻", (byte)12, "cs.guns.scar"),
	M4((byte)30, (byte)2, 5.0F, 0.1F, 0.06F, (byte)0, 0.03F, false, (short)40, true, (short)30, (short)1300, "§f鉴鉵", (byte)9, "cs.guns.m4a1"),
	NGV((byte)60, (byte)3, 6.0F, 0.25F, 0.15F, (byte)1, 0.04F, false, (short)80, true, (short)60, (short)1350, "§f鉾鉿", (byte)14, "cs.guns.negev"),
	P90((byte)20, (byte)2, 4.0F, 0.05F, 0.05F, (byte)0, 0.02F, false, (short)30, true, (short)40, (short)1050, "§f鉸鉹", (byte)3, "cs.guns.p90"),
	MP5((byte)40, (byte)1, 1.5F, 0.15F, 0.12F, (byte)0, 0.01F, false, (short)40, true, (short)50, (short)900, "§f銃銄", (byte)5, "cs.guns.mp5"),
	SG13((byte)8, (byte)16, 2.5F, 0.0F, 0.16F, (byte)8, 0.08F, false, (short)40, true, (short)80, (short)750, "§f鉶鉷", (byte)17, "cs.guns.pump"),
	NOVA((byte)10, (byte)12, 3.0F, 0.2F, 0.25F, (byte)6, 0.08F, false, (short)50, true, (short)70, (short)600, "§f鉼鉽", (byte)7, "cs.guns.nova"),
	TP7((byte)6, (byte)10, 4.5F, 0.15F, 0.02F, (byte)2, 0.03F, false, (short)30, false, (short)80, (short)350, "§f銀", (byte)44, "cs.guns.tp7"),
	USP((byte)12, (byte)8, 7.5F, 0.05F, 0F, (byte)0, 0.02F, false, (short)20, false, (short)120, (short)150, "§f銁", (byte)35, "cs.guns.usp"),
	DGL((byte)8, (byte)14, 12.0F, 0.1F, 0F, (byte)0, 0.04F, false, (short)40, false, (short)100, (short)400, "§f銂", (byte)52, "cs.guns.deagle");
	
	public final byte amo;
	public final byte cld;
	public final float dmg;
	public final float yrcl;
	public final float xsprd;
	public final byte brst;
	public final float kb;
	public final boolean snp;
	public final short rtm;
	public final boolean prm;
	public final short rwd;
	public final short prc;
	public final String icn;
	public final byte slt;
	public final String snd;
	
	GunType(final byte amo, final byte cld, final float dmg, final float yrcl, final float xsprd, final byte brst, final float kb, final boolean snp, final short rtm, final boolean prm, final short rwd, final short prc, final String icn, final byte slt, final String snd) {
		this.amo = amo;
		this.cld = cld;
		this.dmg = dmg;
		this.yrcl = yrcl;
		this.xsprd = xsprd;
		this.brst = brst;
		this.kb = kb;
		this.snp = snp;
		this.rtm = rtm;
		this.prm = prm;
		this.rwd = rwd;
		this.prc = prc;
		this.icn = icn;
		this.slt = slt;
		this.snd = snd;
	}
       
	public static GunType getGnTp(final ItemStack it) {
		if (it == null) {
			return null;
		}
		switch (it.getType()) {
			case NETHERITE_AXE:
				return AWP;
			case NETHERITE_HOE:
				return SCAR;
			case IRON_HOE:
				return AK47;
			case IRON_AXE:
				return M4;
			case IRON_PICKAXE:
				return NGV;
			case GOLDEN_HOE:
				return P90;
			case GOLDEN_AXE:
				return MP5;
			case WOODEN_HOE:
				return SG13;
			case WOODEN_AXE:
				return NOVA;
			case STONE_HOE:
				return TP7;
			case STONE_AXE:
				return USP;
			case STONE_PICKAXE:
				return DGL;
			default:
				return null;
		}
	}
}