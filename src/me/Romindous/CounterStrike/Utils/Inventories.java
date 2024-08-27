package me.Romindous.CounterStrike.Utils;

import me.Romindous.CounterStrike.Enums.GunType;
import me.Romindous.CounterStrike.Main;
import me.Romindous.CounterStrike.Objects.Mobs.Mobber;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import ru.komiss77.utils.TCUtil;

import java.util.Arrays;
import java.util.LinkedList;

import static me.Romindous.CounterStrike.Enums.GunType.*;
import static me.Romindous.CounterStrike.Enums.NadeType.*;
import static org.bukkit.Material.*;

public class Inventories {
	
	public static Inventory TShop;
	public static Inventory CTShop;
	public static Inventory LBShop;
	
	public static ItemStack[] fillDfsInv(final byte size) {
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
				its[i] = Main.mkItm(LIGHT_GRAY_STAINED_GLASS_PANE, "§8-=-=-=-", 10);
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
				its[i] = Main.mkItm(LIGHT_GRAY_STAINED_GLASS_PANE, "§8-=-=-=-", 10);
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

	public static void fillLbbInv() {
		LBShop = Bukkit.createInventory(null, 54, Component.text("§5§lТренировка"));
		final ItemStack[] its = new ItemStack[54];
		its[AK47.slt] = Main.mkItm(IRON_HOE, "§5AK-47 " + AK47.icn, 10);
		its[P90.slt] = Main.mkItm(GOLDEN_HOE, "§5P90 " + P90.icn, 10);
		its[AWP.slt] = Main.mkItm(NETHERITE_AXE, "§5AWP " + AWP.icn, 10);
		its[MP5.slt] = Main.mkItm(GOLDEN_AXE, "§5MP5 " + MP5.icn, 10);
		its[NOVA.slt] = Main.mkItm(WOODEN_AXE, "§5NOVA " + NOVA.icn, 10);
		its[M4.slt] = Main.mkItm(IRON_AXE, "§5M4A1 " + M4.icn, 10);
		its[SCAR.slt] = Main.mkItm(NETHERITE_HOE, "§5SCAR " + SCAR.icn, 10);
		its[NGV.slt] = Main.mkItm(IRON_PICKAXE, "§5NEGEV " + NGV.icn, 10);
		its[SG13.slt] = Main.mkItm(WOODEN_HOE, "§5SG-13 " + SG13.icn, 10);
		its[USP.slt] = Main.mkItm(STONE_AXE, "§dUSP " + USP.icn, 10);
		its[FRAG.slt] = Main.mkItm(OAK_SAPLING, "§cОсколочная Граната " + FRAG.icn, 10);
		its[FLAME.slt] = Main.mkItm(ACACIA_SAPLING, "§6Огненная Граната " + FLAME.icn, 10);
		its[FLASH.slt] = Main.mkItm(BIRCH_SAPLING, "§8Свето-Шумовая Граната " + FLASH.icn, 10);
		its[TP7.slt] = Main.mkItm(STONE_HOE, "§dTP-7 " + TP7.icn, 10);
		its[SMOKE.slt] = Main.mkItm(DARK_OAK_SAPLING, "§7Дымовая Граната " + SMOKE.icn, 10);
		its[DECOY.slt] = Main.mkItm(JUNGLE_SAPLING, "§2Отвлекающая Граната " + DECOY.icn, 10);
		its[DGL.slt] = Main.mkItm(STONE_PICKAXE, "§dDGL " + DGL.icn, 10);
		LBShop.setContents(its);
	}
	   
	public static void fillTsInv() {
		TShop = Bukkit.createInventory(null, 54, Component.text("§c§lМагазин Террористов"));
		final ItemStack[] its = new ItemStack[54];
		its[AK47.slt] = Main.mkItm(IRON_HOE, "§5AK-47 " + AK47.icn, 10, "§7Цена: §d" + AK47.prc + " §6⛃");
		its[P90.slt] = Main.mkItm(GOLDEN_HOE, "§5P90 " + P90.icn, 10, "§7Цена: §d" + P90.prc + " §6⛃");
		its[AWP.slt] = Main.mkItm(NETHERITE_AXE, "§5AWP " + AWP.icn, 10, "§7Цена: §d" + AWP.prc + " §6⛃");
		its[MP5.slt] = Main.mkItm(GOLDEN_AXE, "§5MP5 " + MP5.icn, 10, "§7Цена: §d" + MP5.prc + " §6⛃");
		its[NOVA.slt] = Main.mkItm(WOODEN_AXE, "§5NOVA " + NOVA.icn, 10, "§7Цена: §d" + NOVA.prc + " §6⛃");
		its[M4.slt] = Main.mkItm(IRON_AXE, "§5M4A1 " + M4.icn, 10, "§7Цена: §d" + M4.prc + " §6⛃");
		its[SCAR.slt] = Main.mkItm(NETHERITE_HOE, "§5SCAR " + SCAR.icn, 10, "§7Цена: §d" + SCAR.prc + " §6⛃");
		its[NGV.slt] = Main.mkItm(IRON_PICKAXE, "§5NEGEV " + NGV.icn, 10, "§7Цена: §d" + NGV.prc + " §6⛃");
		its[SG13.slt] = Main.mkItm(WOODEN_HOE, "§5SG-13 " + SG13.icn, 10, "§7Цена: §d" + SG13.prc + " §6⛃");
		its[GunType.wireSlt] = Main.mkItm(SUGAR, "§9Растяжка §f\u929b\u929c", 10, "§7Цена: §d" + GunType.wirePrc + " §6⛃");
		its[USP.slt] = Main.mkItm(STONE_AXE, "§dUSP " + USP.icn, 10, "§7Цена: §d" + USP.prc + " §6⛃");
		its[FRAG.slt] = Main.mkItm(OAK_SAPLING, "§cОсколочная Граната " + FRAG.icn, 10, "§7Цена: §d" + FRAG.prc + " §6⛃");
		its[FLAME.slt] = Main.mkItm(ACACIA_SAPLING, "§6Огненная Граната " + FLAME.icn, 10, "§7Цена: §d" + FLAME.prc + " §6⛃");
		its[FLASH.slt] = Main.mkItm(BIRCH_SAPLING, "§8Свето-Шумовая Граната " + FLASH.icn, 10, "§7Цена: §d" + FLASH.prc + " §6⛃");
		its[GunType.helmSlt] = Main.thelm.clone();
		its[GunType.chestSlt] = new ItemStack(LEATHER_CHESTPLATE);
		final LeatherArmorMeta cm = (LeatherArmorMeta) its[49].getItemMeta();
		cm.setColor(Color.RED);
		cm.displayName(TCUtil.form("§cКуртка Террориста §f\u9266"));
		cm.lore(Arrays.asList(TCUtil.form("§7Цена: §d" + GunType.chestPrc + " §6⛃")));
		its[GunType.chestSlt].setItemMeta(cm);
		its[TP7.slt] = Main.mkItm(STONE_HOE, "§dTP-7 " + TP7.icn, 10, "§7Цена: §d" + TP7.prc + " §6⛃");
		its[SMOKE.slt] = Main.mkItm(DARK_OAK_SAPLING, "§7Дымовая Граната " + SMOKE.icn, 10, "§7Цена: §d" + SMOKE.prc + " §6⛃");
		its[DECOY.slt] = Main.mkItm(JUNGLE_SAPLING, "§2Отвлекающая Граната " + DECOY.icn, 10, "§7Цена: §d" + DECOY.prc + " §6⛃");
		its[DGL.slt] = Main.mkItm(STONE_PICKAXE, "§dDGL " + DGL.icn, 10, "§7Цена: §d" + DGL.prc + " §6⛃");
		TShop.setContents(its);
	}
	   
	public static void fillCTsInv() {
		CTShop = Bukkit.createInventory(null, 54, Component.text("§9§lМагазин Спецназа"));
		final ItemStack[] its = new ItemStack[54];
		its[AK47.slt] = Main.mkItm(IRON_HOE, "§5AK-47 " + AK47.icn, 10, "§7Цена: §d" + AK47.prc + " §6⛃");
		its[P90.slt] = Main.mkItm(GOLDEN_HOE, "§5P90 " + P90.icn, 10, "§7Цена: §d" + P90.prc + " §6⛃");
		its[AWP.slt] = Main.mkItm(NETHERITE_AXE, "§5AWP " + AWP.icn, 10, "§7Цена: §d" + AWP.prc + " §6⛃");
		its[MP5.slt] = Main.mkItm(GOLDEN_AXE, "§5MP5 " + MP5.icn, 10, "§7Цена: §d" + MP5.prc + " §6⛃");
		its[NOVA.slt] = Main.mkItm(WOODEN_AXE, "§5NOVA " + NOVA.icn, 10, "§7Цена: §d" + NOVA.prc + " §6⛃");
		its[M4.slt] = Main.mkItm(IRON_AXE, "§5M4A1 " + M4.icn, 10, "§7Цена: §d" + M4.prc + " §6⛃");
		its[SCAR.slt] = Main.mkItm(NETHERITE_HOE, "§5SCAR " + SCAR.icn, 10, "§7Цена: §d" + SCAR.prc + " §6⛃");
		its[NGV.slt] = Main.mkItm(IRON_PICKAXE, "§5NEGEV " + NGV.icn, 10, "§7Цена: §d" + NGV.prc + " §6⛃");
		its[SG13.slt] = Main.mkItm(WOODEN_HOE, "§5SG-13 " + SG13.icn, 10, "§7Цена: §d" + SG13.prc + " §6⛃");
		its[GunType.wireSlt] = Main.mkItm(SUGAR, "§9Растяжка §f\u929b\u929c", 10, "§7Цена: §d" + GunType.wirePrc + " §6⛃");
		its[GunType.kitSlt] = Main.mkItm(SHEARS, "§3Набор Для Разминировки §f\u9268", 10, "§7Цена: §d" + GunType.kitPrc + " §6⛃");
		its[USP.slt] = Main.mkItm(STONE_AXE, "§dUSP " + USP.icn, 10, "§7Цена: §d" + USP.prc + " §6⛃");
		its[FRAG.slt] = Main.mkItm(OAK_SAPLING, "§cОсколочная Граната " + FRAG.icn, 10, "§7Цена: §d" + FRAG.prc + " §6⛃");
		its[FLAME.slt] = Main.mkItm(ACACIA_SAPLING, "§6Огненная Граната " + FLAME.icn, 10, "§7Цена: §d" + FLAME.prc + " §6⛃");
		its[FLASH.slt] = Main.mkItm(BIRCH_SAPLING, "§8Свето-Шумовая Граната " + FLASH.icn, 10, "§7Цена: §d" + FLASH.prc + " §6⛃");
		its[GunType.helmSlt] = Main.cthelm.clone();
		its[GunType.chestSlt] = new ItemStack(LEATHER_CHESTPLATE);
		final LeatherArmorMeta cm = (LeatherArmorMeta) its[49].getItemMeta();
		cm.setColor(Color.TEAL);
		cm.displayName(TCUtil.form("§3Жилет Спецназа §f\u9266"));
		cm.lore(Arrays.asList(TCUtil.form("§7Цена: §d" + GunType.chestPrc + " §6⛃")));
		its[GunType.chestSlt].setItemMeta(cm);
		its[TP7.slt] = Main.mkItm(STONE_HOE, "§dTP-7 " + TP7.icn, 10, "§7Цена: §d" + TP7.prc + " §6⛃");
		its[SMOKE.slt] = Main.mkItm(DARK_OAK_SAPLING, "§7Дымовая Граната " + SMOKE.icn, 10, "§7Цена: §d" + SMOKE.prc + " §6⛃");
		its[DECOY.slt] = Main.mkItm(JUNGLE_SAPLING, "§2Отвлекающая Граната " + DECOY.icn, 10, "§7Цена: §d" + DECOY.prc + " §6⛃");
		its[DGL.slt] = Main.mkItm(STONE_PICKAXE, "§dDGL " + DGL.icn, 10, "§7Цена: §d" + DGL.prc + " §6⛃");
		CTShop.setContents(its);
	}
}