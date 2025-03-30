package me.Romindous.CounterStrike.Enums;

import java.util.List;
import java.util.Locale;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ChargedProjectiles;
import io.papermc.paper.datacomponent.item.UseCooldown;
import net.kyori.adventure.key.Key;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;
import ru.komiss77.boot.OStrap;
import ru.komiss77.modules.items.ItemBuilder;

public enum GunType {
	
	AWP(	6, 	24, 	50.0F, 	1.0F, 	0.5F, 	1, 0.08F, 	true, 60, true, 50, 1650, "§f鉰鉱", 4),
	SCAR(	14, 	10, 	5.5F, 	0.5F, 	0.10F, 	3, 0.05F, 	true, 40, true, 60, 1500, "§f鉺鉻", 12),
	NGV(	60, 	3, 	4.0F, 	0.25F, 0.15F, 	2, 0.04F, 	false, 80, true, 60, 1350, "§f鉾鉿", 14),
	M4A1(	30, 	2, 	5.0F, 	0.1F, 	0.05F, 	0, 0.03F, 	false, 40, true, 30, 1300, "§f鉴鉵", 9),
	AK47(	30, 	3, 	6.0F, 	0.15F, 0.08F, 	0, 0.04F, 	false, 50, true, 40, 1250, "§f鉲鉳", 1),
	P90(	20, 	2, 	4.0F, 	0.05F, 0.06F, 	0, 0.02F, 	false, 30, true, 40, 1050, "§f鉸鉹", 3),
	MP5(	40, 	1, 	1.5F, 	0.15F, 0.12F, 	0, 0.01F, 	false, 40, true, 50, 900, "§f銃銄", 5),
	NOVA(	10, 	12, 	3.0F, 	0.2F, 	0.20F, 	6, 0.08F, 	false, 50, true, 70, 600, "§f鉼鉽", 7),
	SG13(	8, 	16, 	2.5F, 	0.0F, 	0.16F, 	8, 0.08F, 	false, 40, true, 80, 750, "§f鉶鉷", 17),
	DGL(	8, 	12, 	12.8F, 	0.1F, 	0.025F, 	0, 0.04F, 	false, 40, false, 100, 400, "§f銂", 52),
	TP9(	6, 	8, 	4.0F, 	0.15F, 0.02F, 	2, 0.03F, 	false, 30, false, 80, 250, "§f銀", 44),
	USP(	12, 	4, 	4.5F, 	0.1F, 	0.04F, 	0, 0.02F, 	false, 20, false, 120, 150, "§f銁", 35);

	public final byte amo;
	public final byte cld;
	public final float dmg;
	
	public final float yrcl;
	public final float xsprd;
	
	public final byte brst;
	public final float kb;

	public final String name;
	public final boolean snp;
	public final short rtm;
	public final boolean prm;
	public final short rwd;
	public final short prc;
	public final String icn;
	public final byte slt;
	public final String snd;

	public static final String GUNS = "gun";
	public static final NamespacedKey KEY = OStrap.key(GUNS);
	public static final String DEF_MDL = "default";
	public static final ItemType TYPE = ItemType.CROSSBOW;

	GunType(final int amo, final int cld, final float dmg, final float yrcl, final float xsprd, 
		final int brst, final float kb, final boolean snp, final int rtm, final boolean prm, 
		final int rwd, final int prc, final String icn, final int slt) {
		this.name = name().toLowerCase(Locale.ENGLISH);
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
		this.snd = GUNS + "." + name;
	}

	public Key skin(final String mdl) {
		return Key.key(GUNS + "/" + name + "/" + mdl);
	}

	/*public ItemType type() {
		return switch (this) {
			case AWP -> ItemType.NETHERITE_AXE;
			case SCAR -> ItemType.NETHERITE_HOE;
			case AK47 -> ItemType.IRON_HOE;
			case M4A1 -> ItemType.IRON_AXE;
			case NGV -> ItemType.IRON_PICKAXE;
			case P90 -> ItemType.GOLDEN_HOE;
			case MP5 -> ItemType.GOLDEN_AXE;
			case SG13 -> ItemType.WOODEN_HOE;
			case NOVA -> ItemType.WOODEN_AXE;
			case TP9 -> ItemType.STONE_HOE;
			case USP -> ItemType.STONE_AXE;
			case DGL -> ItemType.STONE_PICKAXE;
		};
	}*/

	private static final GunType[] VALS = values();
	public static final ChargedProjectiles CHARGE = ChargedProjectiles
		.chargedProjectiles(List.of(ItemType.FIRE_CHARGE.createItemStack()));
	public static final int ENCD = 100;

	public ItemBuilder item() {
		return new ItemBuilder(TYPE).data(KEY, ordinal()).set(DataComponentTypes.CHARGED_PROJECTILES, CHARGE)
			.hide(DataComponentTypes.CHARGED_PROJECTILES, DataComponentTypes.INTANGIBLE_PROJECTILE)
			.set(DataComponentTypes.USE_COOLDOWN, UseCooldown.useCooldown(ordinal() + ENCD)
				.cooldownGroup(skin(DEF_MDL)).build());
	}

	@Nullable
	public static GunType get(final ItemStack it) {
		if (it == null) return null;
		final Integer or = it.getPersistentDataContainer().get(KEY, PersistentDataType.INTEGER);
        return or == null || or >= VALS.length ? null : VALS[or];
    }

	@Nullable
	public static GunType fast(final ItemStack it) {
		if (it == null) return null;
		final UseCooldown usCd = it.getData(DataComponentTypes.USE_COOLDOWN);
        if (usCd == null) return null;
		final int enc = (int) usCd.seconds() - ENCD;
        return enc < 0 || enc >= VALS.length ? null : VALS[enc];
    }
}