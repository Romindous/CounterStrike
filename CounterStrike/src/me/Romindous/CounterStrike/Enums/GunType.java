package me.Romindous.CounterStrike.Enums;

import org.bukkit.inventory.ItemStack;

public enum GunType {
	
	AWP((byte)6, (byte)20, 50.0F, 1.0F, 0.5F, (byte)1, 0.25F, true, (short)20, true, "§f鉰鉱"),
	AK47((byte)30, (byte)4, 5.0F, 0.1F, 0.08F, (byte)0, 0.08F, false, (short)20, true, "§f鉲鉳"),
	SCAR((byte)20, (byte)14, 5.0F, 0.5F, 0.25F, (byte)3, 0.05F, true, (short)20, true, "§f鉺鉻"),
	M4((byte)30, (byte)3, 4.0F, 0.05F, 0.048F, (byte)0, 0.05F, false, (short)20, true, "§f鉴鉵"),
	NGV((byte)60, (byte)4, 5.0F, 0.25F, 0.127F, (byte)0, 0.05F, false, (short)20, true, "§f鉾鉿"),
	P90((byte)36, (byte)3, 3.0F, 0.05F, 0.048F, (byte)0, 0.05F, false, (short)20, true, "§f鉸鉹"),
	MP5((byte)40, (byte)2, 2.0F, 0.15F, 0.08F, (byte)0, 0.02F, false, (short)20, true, "§f銃銄"),
	SG13((byte)8, (byte)20, 3.0F, 0.0F, 0.16F, (byte)8, 0.8F, false, (short)20, true, "§f鉶鉷"),
	NOVA((byte)10, (byte)12, 3.0F, 0.15F, 0.224F, (byte)6, 0.8F, false, (short)20, true, "§f鉼鉽"),
	TP7((byte)10, (byte)8, 3.0F, 0.05F, 0.016F, (byte)2, 0.05F, false, (short)20, false, "§f銀"),
	USP((byte)12, (byte)6, 4.0F, 0.05F, 0.016F, (byte)0, 0.05F, false, (short)20, false, "§f銁"),
	DGL((byte)8, (byte)12, 6.0F, 0.1F, 0.032F, (byte)0, 0.05F, false, (short)20, false, "§f銂");
	
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
	public final String icn;
	
	GunType(byte amo, byte cld, float dmg, float yrcl, float xsprd, byte brst, float kb, boolean snp, short rtm, boolean prm, String icn) {
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
		this.icn = icn;
	}
       
	public static GunType getGnTp(ItemStack it) {
		if (it == null) {
			return null;
		}
		switch (it.getType()) {
			case NETHERITE_AXE:
				return AWP;
			case IRON_HOE:
				return AK47;
			case NETHERITE_HOE:
				return SCAR;
			case IRON_AXE:
				return M4;
			case NETHERITE_PICKAXE:
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