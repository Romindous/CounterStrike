package me.Romindous.CounterStrike.Utils;

import java.util.Arrays;
import java.util.LinkedList;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import me.Romindous.CounterStrike.Main;
import me.Romindous.CounterStrike.Enums.GunType;
import me.Romindous.CounterStrike.Enums.NadeType;

import static me.Romindous.CounterStrike.Enums.GunType.*;
import static me.Romindous.CounterStrike.Enums.NadeType.*;

public class Inventories {
	
	public static Inventory TShop;
	public static Inventory CTShop;
	public static Inventory LBShop;
	
	public static ItemStack[] fillDefInv(final Block b, final byte sz) {
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
		for (byte i = 0; i < sz / 3; i++) {
			its[ls.remove(Main.srnd.nextInt(ls.size()))] = Main.mkItm(Material.STRING, "§8~-~-~", clr);
		}
		
		for (byte i = (byte) (its.length - 1); i >= 0; i--) {
			switch (i) {
			case 1:
			case 9:
				its[i] = Main.mkItm(Material.LIGHT_GRAY_STAINED_GLASS_PANE, "§8-=-=-=-", 1);
				break;
			case 0:
				its[i] = Main.mkItm(Material.BOWL, "§5§lРазрежте провода этого цвета!", clr);
				break;
			default:
				if (its[i] == null) {
					its[i] = Main.mkItm(Material.STRING, "§8~-~-~", chs[Main.srnd.nextInt(3)]);
				}
				break;
			}
		} 
		return its;
	}
	   
	public static void fillLbbInv() {
		LBShop = Bukkit.createInventory(null, 54, "§5§lТренировка");
		final ItemStack[] its = new ItemStack[54];
		its[AK47.slt] = Main.mkItm(Material.IRON_HOE, "§5AK-47", 1);
		its[P90.slt] = Main.mkItm(Material.GOLDEN_HOE, "§5P90", 1);
		its[AWP.slt] = Main.mkItm(Material.NETHERITE_AXE, "§5AWP", 1);
		its[MP5.slt] = Main.mkItm(Material.GOLDEN_AXE, "§5MP5", 1);
		its[NOVA.slt] = Main.mkItm(Material.WOODEN_AXE, "§5NOVA", 1);
		its[M4.slt] = Main.mkItm(Material.IRON_AXE, "§5M4A1", 1);
		its[SCAR.slt] = Main.mkItm(Material.NETHERITE_HOE, "§5SCAR", 1);
		its[NGV.slt] = Main.mkItm(Material.IRON_PICKAXE, "§5NEGEV", 1);
		its[SG13.slt] = Main.mkItm(Material.WOODEN_HOE, "§5SG-13", 1);
		its[31] = Main.mkItm(Material.BONE, "§fНож", 1);
		its[USP.slt] = Main.mkItm(Material.STONE_AXE, "§dUSP", 1);
		its[FRAG.slt] = Main.mkItm(Material.OAK_SAPLING, "§cОсколочная Граната", 1);
		its[FIRE.slt] = Main.mkItm(Material.ACACIA_SAPLING, "§6Огненная Граната", 1);
		its[FLASH.slt] = Main.mkItm(Material.BIRCH_SAPLING, "§8Свето-Шумовая Граната", 1);
		its[TP7.slt] = Main.mkItm(Material.STONE_HOE, "§dTP-7", 1);
		its[SMOKE.slt] = Main.mkItm(Material.DARK_OAK_SAPLING, "§7Дымовая Граната", 1);
		its[DECOY.slt] = Main.mkItm(Material.JUNGLE_SAPLING, "§2Отвлекающая Граната", 1);
		its[DGL.slt] = Main.mkItm(Material.STONE_PICKAXE, "§dDGL", 1);
		LBShop.setContents(its);
	}
	   
	public static void fillTsInv() {
		TShop = Bukkit.createInventory(null, 54, "§c§lМагазин Террористов");
		final ItemStack[] its = new ItemStack[54];
		its[AK47.slt] = Main.mkItm(Material.IRON_HOE, "§5AK-47", 1, "§7Цена: §d" + GunType.getGnTp(new ItemStack(Material.IRON_HOE)).prc + " §6⛃");
		its[P90.slt] = Main.mkItm(Material.GOLDEN_HOE, "§5P90", 1, "§7Цена: §d" + GunType.getGnTp(new ItemStack(Material.GOLDEN_HOE)).prc + " §6⛃");
		its[AWP.slt] = Main.mkItm(Material.NETHERITE_AXE, "§5AWP", 1, "§7Цена: §d" + GunType.getGnTp(new ItemStack(Material.NETHERITE_AXE)).prc + " §6⛃");
		its[MP5.slt] = Main.mkItm(Material.GOLDEN_AXE, "§5MP5", 1, "§7Цена: §d" + GunType.getGnTp(new ItemStack(Material.GOLDEN_AXE)).prc + " §6⛃");
		its[NOVA.slt] = Main.mkItm(Material.WOODEN_AXE, "§5NOVA", 1, "§7Цена: §d" + GunType.getGnTp(new ItemStack(Material.WOODEN_AXE)).prc + " §6⛃");
		its[M4.slt] = Main.mkItm(Material.IRON_AXE, "§5M4A1", 1, "§7Цена: §d" + GunType.getGnTp(new ItemStack(Material.IRON_AXE)).prc + " §6⛃");
		its[SCAR.slt] = Main.mkItm(Material.NETHERITE_HOE, "§5SCAR", 1, "§7Цена: §d" + GunType.getGnTp(new ItemStack(Material.NETHERITE_HOE)).prc + " §6⛃");
		its[NGV.slt] = Main.mkItm(Material.IRON_PICKAXE, "§5NEGEV", 1, "§7Цена: §d" + GunType.getGnTp(new ItemStack(Material.IRON_PICKAXE)).prc + " §6⛃");
		its[SG13.slt] = Main.mkItm(Material.WOODEN_HOE, "§5SG-13", 1, "§7Цена: §d" + GunType.getGnTp(new ItemStack(Material.WOODEN_HOE)).prc + " §6⛃");
		its[28] = Main.mkItm(Material.SUGAR, "§9Растяжка", 1, "§7Цена: §d" + Main.twrPrc + " §6⛃");
		its[USP.slt] = Main.mkItm(Material.STONE_AXE, "§dUSP", 1, "§7Цена: §d" + GunType.getGnTp(new ItemStack(Material.STONE_AXE)).prc + " §6⛃");
		its[FRAG.slt] = Main.mkItm(Material.OAK_SAPLING, "§cОсколочная Граната", 1, "§7Цена: §d" + NadeType.getNdTp(new ItemStack(Material.OAK_SAPLING)).prc + " §6⛃");
		its[FIRE.slt] = Main.mkItm(Material.ACACIA_SAPLING, "§6Огненная Граната", 1, "§7Цена: §d" + NadeType.getNdTp(new ItemStack(Material.ACACIA_SAPLING)).prc + " §6⛃");
		its[FLASH.slt] = Main.mkItm(Material.BIRCH_SAPLING, "§8Свето-Шумовая Граната", 1, "§7Цена: §d" + NadeType.getNdTp(new ItemStack(Material.BIRCH_SAPLING)).prc + " §6⛃");
		its[40] = new ItemStack(Material.LEATHER_HELMET);
		final LeatherArmorMeta hm = (LeatherArmorMeta) its[40].getItemMeta();
		hm.setColor(Color.RED);
		hm.setDisplayName("§cШапка Террориста");
		hm.setLore(Arrays.asList("§7Цена: §d" + Main.hlmtPrc + " §6⛃"));
		its[40].setItemMeta(hm);
		its[49] = new ItemStack(Material.LEATHER_CHESTPLATE);
		final LeatherArmorMeta cm = (LeatherArmorMeta) its[49].getItemMeta();
		cm.setColor(Color.RED);
		cm.setDisplayName("§cКуртка Террориста");
		cm.setLore(Arrays.asList("§7Цена: §d" + Main.chstPrc + " §6⛃"));
		its[49].setItemMeta(cm);
		its[TP7.slt] = Main.mkItm(Material.STONE_HOE, "§dTP-7", 1, "§7Цена: §d" + GunType.getGnTp(new ItemStack(Material.STONE_HOE)).prc + " §6⛃");
		its[SMOKE.slt] = Main.mkItm(Material.DARK_OAK_SAPLING, "§7Дымовая Граната", 1, "§7Цена: §d" + NadeType.getNdTp(new ItemStack(Material.DARK_OAK_SAPLING)).prc + " §6⛃");
		its[DECOY.slt] = Main.mkItm(Material.JUNGLE_SAPLING, "§2Отвлекающая Граната", 1, "§7Цена: §d" + NadeType.getNdTp(new ItemStack(Material.JUNGLE_SAPLING)).prc + " §6⛃");
		its[DGL.slt] = Main.mkItm(Material.STONE_PICKAXE, "§dDGL", 1, "§7Цена: §d" + GunType.getGnTp(new ItemStack(Material.STONE_PICKAXE)).prc + " §6⛃");
		TShop.setContents(its);
	}
	   
	public static void fillCTsInv() {
		CTShop = Bukkit.createInventory(null, 54, "§9§lМагазин Спецназа");
		final ItemStack[] its = new ItemStack[54];
		its[AK47.slt] = Main.mkItm(Material.IRON_HOE, "§5AK-47", 1, "§7Цена: §d" + GunType.getGnTp(new ItemStack(Material.IRON_HOE)).prc + " §6⛃");
		its[P90.slt] = Main.mkItm(Material.GOLDEN_HOE, "§5P90", 1, "§7Цена: §d" + GunType.getGnTp(new ItemStack(Material.GOLDEN_HOE)).prc + " §6⛃");
		its[AWP.slt] = Main.mkItm(Material.NETHERITE_AXE, "§5AWP", 1, "§7Цена: §d" + GunType.getGnTp(new ItemStack(Material.NETHERITE_AXE)).prc + " §6⛃");
		its[MP5.slt] = Main.mkItm(Material.GOLDEN_AXE, "§5MP5", 1, "§7Цена: §d" + GunType.getGnTp(new ItemStack(Material.GOLDEN_AXE)).prc + " §6⛃");
		its[NOVA.slt] = Main.mkItm(Material.WOODEN_AXE, "§5NOVA", 1, "§7Цена: §d" + GunType.getGnTp(new ItemStack(Material.WOODEN_AXE)).prc + " §6⛃");
		its[M4.slt] = Main.mkItm(Material.IRON_AXE, "§5M4A1", 1, "§7Цена: §d" + GunType.getGnTp(new ItemStack(Material.IRON_AXE)).prc + " §6⛃");
		its[SCAR.slt] = Main.mkItm(Material.NETHERITE_HOE, "§5SCAR", 1, "§7Цена: §d" + GunType.getGnTp(new ItemStack(Material.NETHERITE_HOE)).prc + " §6⛃");
		its[NGV.slt] = Main.mkItm(Material.IRON_PICKAXE, "§5NEGEV", 1, "§7Цена: §d" + GunType.getGnTp(new ItemStack(Material.IRON_PICKAXE)).prc + " §6⛃");
		its[SG13.slt] = Main.mkItm(Material.WOODEN_HOE, "§5SG-13", 1, "§7Цена: §d" + GunType.getGnTp(new ItemStack(Material.WOODEN_HOE)).prc + " §6⛃");
		its[28] = Main.mkItm(Material.SUGAR, "§9Растяжка", 1, "§7Цена: §d" + Main.twrPrc + " §6⛃");
		its[33] = Main.mkItm(Material.SHEARS, "§3Набор Для Разминировки", 1, "§7Цена: §d" + Main.dfktPrc + " §6⛃");
		its[USP.slt] = Main.mkItm(Material.STONE_AXE, "§dUSP", 1, "§7Цена: §d" + GunType.getGnTp(new ItemStack(Material.STONE_AXE)).prc + " §6⛃");
		its[FRAG.slt] = Main.mkItm(Material.OAK_SAPLING, "§cОсколочная Граната", 1, "§7Цена: §d" + NadeType.getNdTp(new ItemStack(Material.OAK_SAPLING)).prc + " §6⛃");
		its[FIRE.slt] = Main.mkItm(Material.ACACIA_SAPLING, "§6Огненная Граната", 1, "§7Цена: §d" + NadeType.getNdTp(new ItemStack(Material.ACACIA_SAPLING)).prc + " §6⛃");
		its[FLASH.slt] = Main.mkItm(Material.BIRCH_SAPLING, "§8Свето-Шумовая Граната", 1, "§7Цена: §d" + NadeType.getNdTp(new ItemStack(Material.BIRCH_SAPLING)).prc + " §6⛃");
		its[40] = new ItemStack(Material.LEATHER_HELMET);
		final LeatherArmorMeta hm = (LeatherArmorMeta) its[40].getItemMeta();
		hm.setColor(Color.TEAL);
		hm.setDisplayName("§3Шлем Спецназа");
		hm.setLore(Arrays.asList("§7Цена: §d" + Main.hlmtPrc + " §6⛃"));
		its[40].setItemMeta(hm);
		its[49] = new ItemStack(Material.LEATHER_CHESTPLATE);
		final LeatherArmorMeta cm = (LeatherArmorMeta) its[49].getItemMeta();
		cm.setColor(Color.TEAL);
		cm.setDisplayName("§3Жилет Спецназа");
		cm.setLore(Arrays.asList("§7Цена: §d" + Main.chstPrc + " §6⛃"));
		its[49].setItemMeta(cm);
		its[TP7.slt] = Main.mkItm(Material.STONE_HOE, "§dTP-7", 1, "§7Цена: §d" + GunType.getGnTp(new ItemStack(Material.STONE_HOE)).prc + " §6⛃");
		its[SMOKE.slt] = Main.mkItm(Material.DARK_OAK_SAPLING, "§7Дымовая Граната", 1, "§7Цена: §d" + NadeType.getNdTp(new ItemStack(Material.DARK_OAK_SAPLING)).prc + " §6⛃");
		its[DECOY.slt] = Main.mkItm(Material.JUNGLE_SAPLING, "§2Отвлекающая Граната", 1, "§7Цена: §d" + NadeType.getNdTp(new ItemStack(Material.JUNGLE_SAPLING)).prc + " §6⛃");
		its[DGL.slt] = Main.mkItm(Material.STONE_PICKAXE, "§dDGL", 1, "§7Цена: §d" + GunType.getGnTp(new ItemStack(Material.STONE_PICKAXE)).prc + " §6⛃");
		CTShop.setContents(its);
	}
}