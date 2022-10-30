package me.Romindous.CounterStrike.Utils;

import java.util.Arrays;
import java.util.LinkedList;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import me.Romindous.CounterStrike.Main;
import me.Romindous.CounterStrike.Enums.GunType;
import me.Romindous.CounterStrike.Game.Arena;
import me.Romindous.CounterStrike.Game.Defusal;
import me.Romindous.CounterStrike.Game.Invasion;
import me.Romindous.CounterStrike.Objects.Setup;
import static org.bukkit.Material.*;
import static me.Romindous.CounterStrike.Enums.GunType.*;
import static me.Romindous.CounterStrike.Enums.NadeType.*;

public class Inventories {
	
	public static Inventory TShop;
	public static Inventory CTShop;
	public static Inventory LBShop;
	public static Inventory GmInv;

	public static void fillGmInv() {
		GmInv = Bukkit.createInventory(null, 54, "§5§lВыбор Игры");
		Bukkit.getConsoleSender().sendMessage(Main.nnactvarns.size() + " maps");
		final ItemStack[] its = new ItemStack[54];
		for (byte i = 0; i < 54; i++) {
			its[i] = Main.mkItm(LIGHT_GRAY_STAINED_GLASS_PANE, "§8-=-=-=-", 1);
		}
		its[4] = Main.mkItm(ENDER_EYE, "§5Быстрый Поиск", 1);
		byte dfs = 9;
		byte ggs = 27;
		byte ivs = 45;
		for (final Setup stp : Main.nnactvarns.values()) {
			Bukkit.getConsoleSender().sendMessage("Looking at " + stp.nm);
			if (stp.fin) {
				final Arena ar = Main.actvarns.get(stp.nm);
				switch (stp.gt) {
				case DEFUSAL:
					if (dfs < 27) {
						if (ar == null) {
							its[dfs] = Main.mkItm(GREEN_CONCRETE_POWDER, "§d" + stp.nm, 1, "§2Ожидание", "§7Игроков: §20§7/§2" + stp.min, " ", "§7Режим: §dКлассика");
						} else {
							switch (ar.gst) {
							case WAITING:
								its[dfs] = Main.mkItm(GREEN_CONCRETE_POWDER, "§d" + stp.nm, 1, "§2Ожидание", "§7Игроков: §2" + ar.shtrs.size() + "§7/§2" + ar.min, " ", "§7Режим: §dКлассика");
								break;
							case BEGINING:
								its[dfs] = Main.mkItm(YELLOW_CONCRETE_POWDER, "§d" + stp.nm, 1, "§eНачало", "§7Игроков: §e" + ar.shtrs.size() + "§7/§e" + ar.max, " ", "§7Режим: §dКлассика");
								break;
							case BUYTIME:
								its[dfs] = Main.mkItm(ORANGE_CONCRETE_POWDER, "§d" + stp.nm, 1, "§6Закупка", "§7Игроков: §6" + ar.shtrs.size() + "§7/§6" + ar.max, " ", "§7Режим: §dКлассика");
								break;
							case ROUND:
							case ENDRND:
								its[dfs] = Main.mkItm(ORANGE_CONCRETE_POWDER, "§d" + stp.nm, 1, "§6Бой", "§7Игроков: §6" + ar.shtrs.size() + "§7/§6" + ar.max, " ", "§7Режим: §dКлассика");
								break;
							case FINISH:
								its[dfs] = Main.mkItm(PURPLE_CONCRETE_POWDER, "§d" + stp.nm, 1, "§5Финиш", "§7Игроков: §5" + ar.shtrs.size() + "§7/§5" + ar.max, " ", "§7Режим: §dКлассика");
								break;
							}
						}
						dfs++;
					}
					break;
				case GUNGAME:
					if (ggs < 45) {
						if (ar == null) {
							its[ggs] = Main.mkItm(GREEN_CONCRETE_POWDER, "§d" + stp.nm, 1, "§2Ожидание", "§7Игроков: §20§7/§2" + stp.min, " ", "§7Режим: §dЭстафета");
						} else {
							switch (ar.gst) {
							case WAITING:
								its[ggs] = Main.mkItm(GREEN_CONCRETE_POWDER, "§d" + stp.nm, 1, "§2Ожидание", "§7Игроков: §2" + ar.shtrs.size() + "§7/§2" + ar.min, " ", "§7Режим: §dЭстафета");
								break;
							case BEGINING:
								its[ggs] = Main.mkItm(YELLOW_CONCRETE_POWDER, "§d" + stp.nm, 1, "§eНачало", "§7Игроков: §e" + ar.shtrs.size() + "§7/§e" + ar.max, " ", "§7Режим: §dЭстафета");
								break;
							case BUYTIME:
								its[ggs] = Main.mkItm(ORANGE_CONCRETE_POWDER, "§d" + stp.nm, 1, "§6Подготовка", "§7Игроков: §6" + ar.shtrs.size() + "§7/§6" + ar.max, " ", "§7Режим: §dЭстафета");
								break;
							case ROUND:
							case ENDRND:
								its[ggs] = Main.mkItm(ORANGE_CONCRETE_POWDER, "§d" + stp.nm, 1, "§6Бой", "§7Игроков: §6" + ar.shtrs.size() + "§7/§6" + ar.max, " ", "§7Режим: §dЭстафета");
								break;
							case FINISH:
								its[ggs] = Main.mkItm(PURPLE_CONCRETE_POWDER, "§d" + stp.nm, 1, "§5Финиш", "§7Игроков: §5" + ar.shtrs.size() + "§7/§5" + ar.max, " ", "§7Режим: §dЭстафета");
								break;
							}
						}
						its[ggs] = Main.mkItm(GREEN_CONCRETE_POWDER, "§d" + stp.nm, 1, "§2Ожидание", "§7Игроков: §20§7/§2" + stp.min, " ", "§7Режим: §dЭстафета");
						ggs++;
					}
					break;
				case INVASION:
					if (ivs < 54) {
						if (ar == null) {
							its[ivs] = Main.mkItm(GREEN_CONCRETE_POWDER, "§d" + stp.nm, 1, "§2Ожидание", "§7Игроков: §20§7/§2" + stp.min, " ", "§7Режим: §dВторжение");
						} else {
							switch (ar.gst) {
							case WAITING:
								its[ivs] = Main.mkItm(GREEN_CONCRETE_POWDER, "§d" + stp.nm, 1, "§2Ожидание", "§7Игроков: §2" + ar.shtrs.size() + "§7/§2" + ar.min, " ", "§7Режим: §dВторжение");
								break;
							case BEGINING:
								its[ivs] = Main.mkItm(YELLOW_CONCRETE_POWDER, "§d" + stp.nm, 1, "§eНачало", "§7Игроков: §e" + ar.shtrs.size() + "§7/§e" + ar.max, " ", "§7Режим: §dВторжение");
								break;
							case BUYTIME:
								its[ivs] = Main.mkItm(ORANGE_CONCRETE_POWDER, "§d" + stp.nm, 1, "§6Закупка", "§7Игроков: §6" + ar.shtrs.size() + "§7/§6" + ar.max, " ", "§7Режим: §dВторжение");
								break;
							case ROUND:
							case ENDRND:
								its[ivs] = Main.mkItm(ORANGE_CONCRETE_POWDER, "§d" + stp.nm, 1, "§6Бой", "§7Игроков: §6" + ar.shtrs.size() + "§7/§6" + ar.max, " ", "§7Режим: §dВторжение");
								break;
							case FINISH:
								its[ivs] = Main.mkItm(PURPLE_CONCRETE_POWDER, "§d" + stp.nm, 1, "§5Финиш", "§7Игроков: §5" + ar.shtrs.size() + "§7/§5" + ar.max, " ", "§7Режим: §dВторжение");
								break;
							}
						}
						ivs++;
					}
					break;
				default:
					break;
				}
			}
		}
		GmInv.setContents(its);
	}

	public static void updtGm(final Arena ar) {
		byte n = 0;
		for (final ItemStack it : GmInv.getContents()) {
			if (it != null && it.hasItemMeta() && it.getItemMeta().getDisplayName().substring(2).equals(ar.name)) {
				final String md;
				if (ar instanceof Defusal) {
					md = "Классика";
				} else if (ar instanceof Invasion) {
					md = "Вторжение";
				} else {
					md = "Эстафета";
				} 
				switch (ar.gst) {
				case WAITING:
					GmInv.setItem(n, Main.mkItm(GREEN_CONCRETE_POWDER, "§d" + ar.name, 1, "§2Ожидание", "§7Игроков: §2" + ar.shtrs.size() + "§7/§2" + ar.min, " ", "§7Режим: §d" + md));
					break;
				case BEGINING:
					GmInv.setItem(n, Main.mkItm(YELLOW_CONCRETE_POWDER, "§d" + ar.name, 1, "§eНачало", "§7Игроков: §e" + ar.shtrs.size() + "§7/§e" + ar.max, " ", "§7Режим: §d" + md));
					break;
				case BUYTIME:
					GmInv.setItem(n, Main.mkItm(ORANGE_CONCRETE_POWDER, "§d" + ar.name, 1, "§6Закупка", "§7Игроков: §6" + ar.shtrs.size() + "§7/§6" + ar.max, " ", "§7Режим: §d" + md));
					break;
				case ROUND:
				case ENDRND:
					GmInv.setItem(n, Main.mkItm(ORANGE_CONCRETE_POWDER, "§d" + ar.name, 1, "§6Бой", "§7Игроков: §6" + ar.shtrs.size() + "§7/§6" + ar.max, " ", "§7Режим: §d" + md));
					break;
				case FINISH:
					GmInv.setItem(n, Main.mkItm(PURPLE_CONCRETE_POWDER, "§d" + ar.name, 1, "§5Финиш", "§7Игроков: §5" + ar.shtrs.size() + "§7/§5" + ar.max, " ", "§7Режим: §d" + md));
					break;
				}
			}
			n++;
		}
	}

	public static ItemStack[] fillTmInv() {
		final ItemStack[] its = new ItemStack[9];
		for (byte i = 0; i < 9; i++) {
			its[i] = Main.mkItm(LIGHT_GRAY_STAINED_GLASS_PANE, "§8-=-=-=-", 1);
		}
		its[1] = Main.mkItm(CRIMSON_NYLIUM, "§4§lТеррористы", 1, " ");
		its[4] = Main.mkItm(ENDER_EYE, "§5§lСлучайная", 1, " ");
		its[7] = Main.mkItm(WARPED_NYLIUM, "§3§lСпецназ", 1, " ");
		return its;
	}
	
	public static ItemStack[] fillDfsInv(final Block b, final byte sz) {
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
			its[ls.remove(Main.srnd.nextInt(ls.size()))] = Main.mkItm(STRING, "§8~-~-~", clr);
		}
		
		for (byte i = (byte) (its.length - 1); i >= 0; i--) {
			switch (i) {
			case 1:
			case 9:
				its[i] = Main.mkItm(LIGHT_GRAY_STAINED_GLASS_PANE, "§8-=-=-=-", 1);
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

	public static ItemStack[] fillDfSpInv(final Block b, final byte sz, final boolean bg) {
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
		for (byte i = (byte) ((bg ? 4 : 3) * getSpnrDf((CreatureSpawner) b.getState()) + 8); i >= 0; i--) {
			its[ls.remove(Main.srnd.nextInt(ls.size()))] = Main.mkItm(STRING, "§8~-~-~", clr);
		}
		
		for (byte i = (byte) (its.length - 1); i >= 0; i--) {
			switch (i) {
			case 1:
			case 9:
				its[i] = Main.mkItm(LIGHT_GRAY_STAINED_GLASS_PANE, "§8-=-=-=-", 1);
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
	   
	private static int getSpnrDf(final CreatureSpawner sp) {
		switch (sp.getSpawnedType()) {
		case ZOMBIE_VILLAGER:
		default:
			return 1;
		case STRAY:
			return 2;
		case VINDICATOR:
			return 3;
		case PIGLIN_BRUTE:
			return 4;
		}
	}

	public static void fillLbbInv() {
		LBShop = Bukkit.createInventory(null, 54, "§5§lТренировка");
		final ItemStack[] its = new ItemStack[54];
		its[AK47.slt] = Main.mkItm(IRON_HOE, "§5AK-47 " + AK47.icn, 1);
		its[P90.slt] = Main.mkItm(GOLDEN_HOE, "§5P90 " + P90.icn, 1);
		its[AWP.slt] = Main.mkItm(NETHERITE_AXE, "§5AWP " + AWP.icn, 1);
		its[MP5.slt] = Main.mkItm(GOLDEN_AXE, "§5MP5 " + MP5.icn, 1);
		its[NOVA.slt] = Main.mkItm(WOODEN_AXE, "§5NOVA " + NOVA.icn, 1);
		its[M4.slt] = Main.mkItm(IRON_AXE, "§5M4A1 " + M4.icn, 1);
		its[SCAR.slt] = Main.mkItm(NETHERITE_HOE, "§5SCAR " + SCAR.icn, 1);
		its[NGV.slt] = Main.mkItm(IRON_PICKAXE, "§5NEGEV " + NGV.icn, 1);
		its[SG13.slt] = Main.mkItm(WOODEN_HOE, "§5SG-13 " + SG13.icn, 1);
		its[USP.slt] = Main.mkItm(STONE_AXE, "§dUSP " + USP.icn, 1);
		its[FRAG.slt] = Main.mkItm(OAK_SAPLING, "§cОсколочная Граната " + FRAG.icn, 1);
		its[FLAME.slt] = Main.mkItm(ACACIA_SAPLING, "§6Огненная Граната " + FLAME.icn, 1);
		its[FLASH.slt] = Main.mkItm(BIRCH_SAPLING, "§8Свето-Шумовая Граната " + FLASH.icn, 1);
		its[TP7.slt] = Main.mkItm(STONE_HOE, "§dTP-7 " + TP7.icn, 1);
		its[SMOKE.slt] = Main.mkItm(DARK_OAK_SAPLING, "§7Дымовая Граната " + SMOKE.icn, 1);
		its[DECOY.slt] = Main.mkItm(JUNGLE_SAPLING, "§2Отвлекающая Граната " + DECOY.icn, 1);
		its[DGL.slt] = Main.mkItm(STONE_PICKAXE, "§dDGL " + DGL.icn, 1);
		LBShop.setContents(its);
	}
	   
	public static void fillTsInv() {
		TShop = Bukkit.createInventory(null, 54, "§c§lМагазин Террористов");
		final ItemStack[] its = new ItemStack[54];
		its[AK47.slt] = Main.mkItm(IRON_HOE, "§5AK-47 " + AK47.icn, 1, "§7Цена: §d" + AK47.prc + " §6⛃");
		its[P90.slt] = Main.mkItm(GOLDEN_HOE, "§5P90 " + P90.icn, 1, "§7Цена: §d" + P90.prc + " §6⛃");
		its[AWP.slt] = Main.mkItm(NETHERITE_AXE, "§5AWP " + AWP.icn, 1, "§7Цена: §d" + AWP.prc + " §6⛃");
		its[MP5.slt] = Main.mkItm(GOLDEN_AXE, "§5MP5 " + MP5.icn, 1, "§7Цена: §d" + MP5.prc + " §6⛃");
		its[NOVA.slt] = Main.mkItm(WOODEN_AXE, "§5NOVA " + NOVA.icn, 1, "§7Цена: §d" + NOVA.prc + " §6⛃");
		its[M4.slt] = Main.mkItm(IRON_AXE, "§5M4A1 " + M4.icn, 1, "§7Цена: §d" + M4.prc + " §6⛃");
		its[SCAR.slt] = Main.mkItm(NETHERITE_HOE, "§5SCAR " + SCAR.icn, 1, "§7Цена: §d" + SCAR.prc + " §6⛃");
		its[NGV.slt] = Main.mkItm(IRON_PICKAXE, "§5NEGEV " + NGV.icn, 1, "§7Цена: §d" + NGV.prc + " §6⛃");
		its[SG13.slt] = Main.mkItm(WOODEN_HOE, "§5SG-13 " + SG13.icn, 1, "§7Цена: §d" + SG13.prc + " §6⛃");
		its[GunType.twrSlt] = Main.mkItm(SUGAR, "§9Растяжка §f\u929b\u929c", 1, "§7Цена: §d" + GunType.twrPrc + " §6⛃");
		its[USP.slt] = Main.mkItm(STONE_AXE, "§dUSP " + USP.icn, 1, "§7Цена: §d" + USP.prc + " §6⛃");
		its[FRAG.slt] = Main.mkItm(OAK_SAPLING, "§cОсколочная Граната " + FRAG.icn, 1, "§7Цена: §d" + FRAG.prc + " §6⛃");
		its[FLAME.slt] = Main.mkItm(ACACIA_SAPLING, "§6Огненная Граната " + FLAME.icn, 1, "§7Цена: §d" + FLAME.prc + " §6⛃");
		its[FLASH.slt] = Main.mkItm(BIRCH_SAPLING, "§8Свето-Шумовая Граната " + FLASH.icn, 1, "§7Цена: §d" + FLASH.prc + " §6⛃");
		its[GunType.hlmtSlt] = Main.thlmt.clone();
		its[GunType.chstSlt] = new ItemStack(LEATHER_CHESTPLATE);
		final LeatherArmorMeta cm = (LeatherArmorMeta) its[49].getItemMeta();
		cm.setColor(Color.RED);
		cm.setDisplayName("§cКуртка Террориста §f\u9266");
		cm.setLore(Arrays.asList("§7Цена: §d" + GunType.chstPrc + " §6⛃"));
		its[GunType.chstSlt].setItemMeta(cm);
		its[TP7.slt] = Main.mkItm(STONE_HOE, "§dTP-7 " + TP7.icn, 1, "§7Цена: §d" + TP7.prc + " §6⛃");
		its[SMOKE.slt] = Main.mkItm(DARK_OAK_SAPLING, "§7Дымовая Граната " + SMOKE.icn, 1, "§7Цена: §d" + SMOKE.prc + " §6⛃");
		its[DECOY.slt] = Main.mkItm(JUNGLE_SAPLING, "§2Отвлекающая Граната " + DECOY.icn, 1, "§7Цена: §d" + DECOY.prc + " §6⛃");
		its[DGL.slt] = Main.mkItm(STONE_PICKAXE, "§dDGL " + DGL.icn, 1, "§7Цена: §d" + DGL.prc + " §6⛃");
		TShop.setContents(its);
	}
	   
	public static void fillCTsInv() {
		CTShop = Bukkit.createInventory(null, 54, "§9§lМагазин Спецназа");
		final ItemStack[] its = new ItemStack[54];
		its[AK47.slt] = Main.mkItm(IRON_HOE, "§5AK-47 " + AK47.icn, 1, "§7Цена: §d" + AK47.prc + " §6⛃");
		its[P90.slt] = Main.mkItm(GOLDEN_HOE, "§5P90 " + P90.icn, 1, "§7Цена: §d" + P90.prc + " §6⛃");
		its[AWP.slt] = Main.mkItm(NETHERITE_AXE, "§5AWP " + AWP.icn, 1, "§7Цена: §d" + AWP.prc + " §6⛃");
		its[MP5.slt] = Main.mkItm(GOLDEN_AXE, "§5MP5 " + MP5.icn, 1, "§7Цена: §d" + MP5.prc + " §6⛃");
		its[NOVA.slt] = Main.mkItm(WOODEN_AXE, "§5NOVA " + NOVA.icn, 1, "§7Цена: §d" + NOVA.prc + " §6⛃");
		its[M4.slt] = Main.mkItm(IRON_AXE, "§5M4A1 " + M4.icn, 1, "§7Цена: §d" + M4.prc + " §6⛃");
		its[SCAR.slt] = Main.mkItm(NETHERITE_HOE, "§5SCAR " + SCAR.icn, 1, "§7Цена: §d" + SCAR.prc + " §6⛃");
		its[NGV.slt] = Main.mkItm(IRON_PICKAXE, "§5NEGEV " + NGV.icn, 1, "§7Цена: §d" + NGV.prc + " §6⛃");
		its[SG13.slt] = Main.mkItm(WOODEN_HOE, "§5SG-13 " + SG13.icn, 1, "§7Цена: §d" + SG13.prc + " §6⛃");
		its[GunType.twrSlt] = Main.mkItm(SUGAR, "§9Растяжка §f\u929b\u929c", 1, "§7Цена: §d" + GunType.twrPrc + " §6⛃");
		its[GunType.dfktSlt] = Main.mkItm(SHEARS, "§3Набор Для Разминировки §f\u9268", 1, "§7Цена: §d" + GunType.dfktPrc + " §6⛃");
		its[USP.slt] = Main.mkItm(STONE_AXE, "§dUSP " + USP.icn, 1, "§7Цена: §d" + USP.prc + " §6⛃");
		its[FRAG.slt] = Main.mkItm(OAK_SAPLING, "§cОсколочная Граната " + FRAG.icn, 1, "§7Цена: §d" + FRAG.prc + " §6⛃");
		its[FLAME.slt] = Main.mkItm(ACACIA_SAPLING, "§6Огненная Граната " + FLAME.icn, 1, "§7Цена: §d" + FLAME.prc + " §6⛃");
		its[FLASH.slt] = Main.mkItm(BIRCH_SAPLING, "§8Свето-Шумовая Граната " + FLASH.icn, 1, "§7Цена: §d" + FLASH.prc + " §6⛃");
		its[GunType.hlmtSlt] = Main.cthlmt.clone();
		its[GunType.chstSlt] = new ItemStack(LEATHER_CHESTPLATE);
		final LeatherArmorMeta cm = (LeatherArmorMeta) its[49].getItemMeta();
		cm.setColor(Color.TEAL);
		cm.setDisplayName("§3Жилет Спецназа §f\u9266");
		cm.setLore(Arrays.asList("§7Цена: §d" + GunType.chstPrc + " §6⛃"));
		its[GunType.chstSlt].setItemMeta(cm);
		its[TP7.slt] = Main.mkItm(STONE_HOE, "§dTP-7 " + TP7.icn, 1, "§7Цена: §d" + TP7.prc + " §6⛃");
		its[SMOKE.slt] = Main.mkItm(DARK_OAK_SAPLING, "§7Дымовая Граната " + SMOKE.icn, 1, "§7Цена: §d" + SMOKE.prc + " §6⛃");
		its[DECOY.slt] = Main.mkItm(JUNGLE_SAPLING, "§2Отвлекающая Граната " + DECOY.icn, 1, "§7Цена: §d" + DECOY.prc + " §6⛃");
		its[DGL.slt] = Main.mkItm(STONE_PICKAXE, "§dDGL " + DGL.icn, 1, "§7Цена: §d" + DGL.prc + " §6⛃");
		CTShop.setContents(its);
	}
}