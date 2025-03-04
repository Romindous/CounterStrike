package me.Romindous.CounterStrike.Utils;

import me.Romindous.CounterStrike.Enums.GunType;
import me.Romindous.CounterStrike.Main;
import me.Romindous.CounterStrike.Objects.Defusable;
import me.Romindous.CounterStrike.Objects.Shooter;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import static me.Romindous.CounterStrike.Enums.GunType.DEF_MDL;
import static me.Romindous.CounterStrike.Enums.GunType.GUNS;
import static me.Romindous.CounterStrike.Enums.NadeType.*;
import static org.bukkit.inventory.ItemType.*;

public class Inventories {

	public static Inventory TShop;
	public static Inventory CTShop;
	public static Inventory LBShop;
	
	/*public static ItemStack[] fillDfsInv(final byte size) {
		final ItemStack[] its = new ItemStack[size];

		final Integer[] slts = new Integer[size - 3];
		for (int i = 0; i != slts.length ; i++) {
			slts[i] = i;
		}
		slts[0] = size - 3;
		slts[1] = size - 2;
		slts[9] = size - 1;
		
		final int clr = 11 + Main.srnd.nextInt(4);
		final int[] chs = new int[] {clr == 11 ? 12 : 11, clr <= 12 ? 13 : 12, clr <= 13 ? 14 : 13};
		
		final LinkedList<Integer> ls = new LinkedList<>(Arrays.asList(slts));
		for (int i = 0; i < size / 3; i++) {
			its[ls.remove(Main.srnd.nextInt(ls.size()))] = Main.mkItm(STRING, "§8~-~-~", clr);
		}
		
		for (int i = 0; i != its.length; i++) {
			switch (i) {
			case 1:
			case 9:
				its[i] = Main.mkItm(LIGHT_GRAY_STAINED_GLASS_PANE, "§8-=-=-=-", "");
				break;
			case 0:
				its[i] = Main.mkItm(BOWL, "§5§lРазрежте провода этого цвета!", clr);
				break;
			default:
				if (its[i] == null) {
					its[i] = Main.mkItm(STRING, "§8~-~-~", chs[Main.srnd.nextInt(3)]);
				}
				break;
			}
		} 
		return its;
	}

	public static ItemStack[] fillDfSpInv(final Mobber mb, final byte sz, final boolean bg) {
		final ItemStack[] its = new ItemStack[sz];
		
		final Byte[] slts = new Byte[sz - 3];
		for (byte i = (byte) (slts.length - 1); i >= 0; i--) {
			slts[i] = i;
		}
		slts[0] = (byte) (sz - 3);
		slts[1] = (byte) (sz - 2);
		slts[9] = (byte) (sz - 1);
		
		final int clr = 11 + Main.srnd.nextInt(4);
		final int[] chs = new int[] {clr == 11 ? 12 : 11, clr <= 12 ? 13 : 12, clr <= 13 ? 14 : 13};
		
		final LinkedList<Byte> ls = new LinkedList<Byte>(Arrays.asList(slts));
		for (byte i = (byte) ((bg ? 12 : 8) * (mb.mt.pow + 1)); i >= 0; i--) {
			its[ls.remove(Main.srnd.nextInt(ls.size()))] = Main.mkItm(STRING, "§8~-~-~", clr);
		}
		
		for (byte i = (byte) (its.length - 1); i >= 0; i--) {
			switch (i) {
			case 1:
			case 9:
				its[i] = Main.mkItm(LIGHT_GRAY_STAINED_GLASS_PANE, "§8-=-=-=-", DEF_MDL);
				break;
			case 0:
				its[i] = Main.mkItm(BOWL, "§5§lРазрежте провода этого цвета!", clr);
				break;
			default:
				if (its[i] == null) {
					its[i] = Main.mkItm(STRING, "§8~-~-~", chs[Main.srnd.nextInt(3)]);
				}
				break;
			}
		} 
		return its;
	}*/

	public static void fillLbbInv() {
		LBShop = Bukkit.createInventory(null, 54, Component.text("§5§lТренировка"));
		final ItemStack[] its = new ItemStack[54];
		for (final GunType gt : GunType.values()) {
			its[gt.slt] = gt.item().name("§5" + gt.name() + " " + gt.icn)
				.model(Key.key(GUNS + "/" + gt.name + "/" + DEF_MDL)).build();
		}
		its[FRAG.slt] = Main.mkItm(OAK_SAPLING, "§cОсколочная Граната " + FRAG.icn, FRAG.skin());
		its[FLAME.slt] = Main.mkItm(ACACIA_SAPLING, "§6Огненная Граната " + FLAME.icn, FLAME.skin());
		its[FLASH.slt] = Main.mkItm(BIRCH_SAPLING, "§8Свето-Шумовая Граната " + FLASH.icn, FLASH.skin());
		its[SMOKE.slt] = Main.mkItm(DARK_OAK_SAPLING, "§7Дымовая Граната " + SMOKE.icn, SMOKE.skin());
		its[DECOY.slt] = Main.mkItm(JUNGLE_SAPLING, "§2Отвлекающая Граната " + DECOY.icn, DECOY.skin());
		LBShop.setContents(its);
	}

	public static void fillTsInv() {
		TShop = Bukkit.createInventory(null, 54, Component.text("§c§lМагазин Террористов"));
		final ItemStack[] its = new ItemStack[54];
		for (final GunType gt : GunType.values()) {
			its[gt.slt] = gt.item().name("§5" + gt.name() + " " + gt.icn)
				.model(Key.key(GUNS + "/" + gt.name + "/" + DEF_MDL)).lore("§7Цена: §d" + gt.prc + " §6⛃").build();
		}
		its[Shooter.wireSlt] = Main.mkItm(SUGAR, "§9Растяжка §f\u929b\u929c", Shooter.ROPE_MDL, "§7Цена: §d" + Shooter.wirePrc + " §6⛃");
		its[FRAG.slt] = Main.mkItm(OAK_SAPLING, "§cОсколочная Граната " + FRAG.icn, FRAG.skin(), "§7Цена: §d" + FRAG.prc + " §6⛃");
		its[FLAME.slt] = Main.mkItm(ACACIA_SAPLING, "§6Огненная Граната " + FLAME.icn, FLAME.skin(), "§7Цена: §d" + FLAME.prc + " §6⛃");
		its[FLASH.slt] = Main.mkItm(BIRCH_SAPLING, "§8Свето-Шумовая Граната " + FLASH.icn, FLASH.skin(), "§7Цена: §d" + FLASH.prc + " §6⛃");
		its[SMOKE.slt] = Main.mkItm(DARK_OAK_SAPLING, "§7Дымовая Граната " + SMOKE.icn, SMOKE.skin(), "§7Цена: §d" + SMOKE.prc + " §6⛃");
		its[DECOY.slt] = Main.mkItm(JUNGLE_SAPLING, "§2Отвлекающая Граната " + DECOY.icn, DECOY.skin(), "§7Цена: §d" + DECOY.prc + " §6⛃");
		its[Shooter.chestSlt] = Main.tChest.clone();
		its[Shooter.helmSlt] = Main.tHelm.clone();
		TShop.setContents(its);
	}
	   
	public static void fillCTsInv() {
		CTShop = Bukkit.createInventory(null, 54, Component.text("§9§lМагазин Спецназа"));
		final ItemStack[] its = new ItemStack[54];
		for (final GunType gt : GunType.values()) {
			its[gt.slt] = gt.item().name("§5" + gt.name() + " " + gt.icn)
				.model(Key.key(GUNS + "/" + gt.name + "/" + DEF_MDL)).lore("§7Цена: §d" + gt.prc + " §6⛃").build();
		}
		its[Shooter.wireSlt] = Main.mkItm(SUGAR, "§9Растяжка §f\u929b\u929c", Shooter.ROPE_MDL, "§7Цена: §d" + Shooter.wirePrc + " §6⛃");
		its[Shooter.kitSlt] = Main.mkItm(SHEARS, "§3Набор Для Разминировки §f\u9268", Defusable.KIT_MDL, "§7Цена: §d" + Shooter.kitPrc + " §6⛃");
		its[FRAG.slt] = Main.mkItm(OAK_SAPLING, "§cОсколочная Граната " + FRAG.icn, FRAG.skin(), "§7Цена: §d" + FRAG.prc + " §6⛃");
		its[FLAME.slt] = Main.mkItm(ACACIA_SAPLING, "§6Огненная Граната " + FLAME.icn, FLAME.skin(), "§7Цена: §d" + FLAME.prc + " §6⛃");
		its[FLASH.slt] = Main.mkItm(BIRCH_SAPLING, "§8Свето-Шумовая Граната " + FLASH.icn, FLASH.skin(), "§7Цена: §d" + FLASH.prc + " §6⛃");
		its[SMOKE.slt] = Main.mkItm(DARK_OAK_SAPLING, "§7Дымовая Граната " + SMOKE.icn, SMOKE.skin(), "§7Цена: §d" + SMOKE.prc + " §6⛃");
		its[DECOY.slt] = Main.mkItm(JUNGLE_SAPLING, "§2Отвлекающая Граната " + DECOY.icn, DECOY.skin(), "§7Цена: §d" + DECOY.prc + " §6⛃");
		its[Shooter.helmSlt] = Main.ctHelm.clone();
		its[Shooter.chestSlt] = Main.ctChest.clone();
		CTShop.setContents(its);
	}
}