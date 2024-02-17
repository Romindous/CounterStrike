package me.Romindous.CounterStrike.Enums;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public enum GunType {
	
	AWP(	6, 	24, 	50.0F, 	1.0F, 	0.5F, 	1, 0.08F, 	true, 60, true, 50, 1650, "§f鉰鉱", 4, "cs.guns.awp"),
	AK47(	30, 	3, 	6.0F, 	0.15F, 0.08F, 	0, 0.04F, 	false, 50, true, 40, 1250, "§f鉲鉳", 1, "cs.guns.ak47"),
	SCAR(	14, 	10, 	7.0F, 	0.5F, 	0.14F, 	3, 0.05F, 	true, 40, true, 60, 1500, "§f鉺鉻", 12, "cs.guns.scar"),
	M4(		30, 	2, 	5.0F, 	0.1F, 	0.06F, 	0, 0.03F, 	false, 40, true, 30, 1300, "§f鉴鉵", 9, "cs.guns.m4a1"),
	NGV(	60, 	3, 	2.5F, 	0.25F, 0.15F, 	2, 0.04F, 	false, 80, true, 60, 1350, "§f鉾鉿", 14, "cs.guns.negev"),
	P90(	20, 	2, 	4.0F, 	0.05F, 0.05F, 	0, 0.02F, 	false, 30, true, 40, 1050, "§f鉸鉹", 3, "cs.guns.p90"),
	MP5(	40, 	1, 	1.5F, 	0.15F, 0.12F, 	0, 0.01F, 	false, 40, true, 50, 900, "§f銃銄", 5, "cs.guns.mp5"),
	SG13(	8, 	16, 	2.5F, 	0.0F, 	0.16F, 	8, 0.08F, 	false, 40, true, 80, 750, "§f鉶鉷", 17, "cs.guns.pump"),
	NOVA(	10, 	12, 	3.0F, 	0.2F, 	0.25F, 	6, 0.08F, 	false, 50, true, 70, 600, "§f鉼鉽", 7, "cs.guns.nova"),
	TP7(	6, 	8, 	5.5F, 	0.15F, 0.025F, 	2, 0.03F, 	false, 30, false, 80, 350, "§f銀", 44, "cs.guns.tp7"),
	USP(	12, 	4, 	4.5F, 	0.1F, 	0.04F, 	0, 0.02F, 	false, 20, false, 120, 150, "§f銁", 35, "cs.guns.usp"),
	DGL(	8, 	12, 	13.0F, 	0.1F, 	0F, 		0, 0.04F, 	false, 40, false, 100, 400, "§f銂", 52, "cs.guns.deagle");
	
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

	public static final short helmPrc = 150;
	public static final short chestPrc = 250;
	public static final short wirePrc = 150;
	public static final short kitPrc = 200;
	
	public static final short knifRwd = 200;
	
	public static final byte helmSlt = 40;
	public static final byte chestSlt = 49;
	public static final byte wireSlt = 29;
	public static final byte kitSlt = 33;
	
	
	public static final byte defCMD = 10;
	
	GunType(final int amo, final int cld, final float dmg, final float yrcl, final float xsprd, 
		final int brst, final float kb, final boolean snp, final int rtm, final boolean prm, 
		final int rwd, final int prc, final String icn, final int slt, final String snd) {
		this.amo = (byte) amo;
		this.cld = (byte) cld;
		this.dmg = dmg;
		this.yrcl = yrcl;
		this.xsprd = xsprd;
		this.brst = (byte) brst;
		this.kb = kb;
		this.snp = snp;
		this.rtm = (short) rtm;
		this.prm = prm;
		this.rwd = (short) rwd;
		this.prc = (short) prc;
		this.icn = icn;
		this.slt = (byte) slt;
		this.snd = snd;
	}
       
	public static GunType getGnTp(final ItemStack it) {
		if (it == null) {
			return null;
		}
        return switch (it.getType()) {
            case NETHERITE_AXE -> AWP;
            case NETHERITE_HOE -> SCAR;
            case IRON_HOE -> AK47;
            case IRON_AXE -> M4;
            case IRON_PICKAXE -> NGV;
            case GOLDEN_HOE -> P90;
            case GOLDEN_AXE -> MP5;
            case WOODEN_HOE -> SG13;
            case WOODEN_AXE -> NOVA;
            case STONE_HOE -> TP7;
            case STONE_AXE -> USP;
            case STONE_PICKAXE -> DGL;
            default -> null;
        };
	}

	public Material getMat() {
        return switch (this) {
            case AWP -> Material.NETHERITE_AXE;
            case SCAR -> Material.NETHERITE_HOE;
            case AK47 -> Material.IRON_HOE;
            case M4 -> Material.IRON_AXE;
            case NGV -> Material.IRON_PICKAXE;
            case P90 -> Material.GOLDEN_HOE;
            case MP5 -> Material.GOLDEN_AXE;
            case SG13 -> Material.WOODEN_HOE;
            case NOVA -> Material.WOODEN_AXE;
            case TP7 -> Material.STONE_HOE;
            case USP -> Material.STONE_AXE;
            case DGL -> Material.STONE_PICKAXE;
        };
	}
}