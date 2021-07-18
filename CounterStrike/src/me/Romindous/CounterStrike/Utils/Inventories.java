package me.Romindous.CounterStrike.Utils;

import java.util.Arrays;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.Romindous.CounterStrike.Main;
 
 
public class Inventories {
	public static ItemStack[] fillDefInv(final Block b, final boolean bg) {
		final ItemStack[] its = new ItemStack[bg ? 54 : 27];
		for (byte i = (byte) (its.length - 1); i >= 0; i--) {
			switch (i) {
			case 3:
			case 5:
			case 13:
				its[i] = Main.mkItm(Material.LIGHT_GRAY_STAINED_GLASS_PANE, "§8-=-=-=-", 1);
				break;
			case 4:
				final ItemStack it = new ItemStack(Material.BOWL);
				final ItemMeta im = it.getItemMeta();
				im.setDisplayName("§5§lРазрежте провода этого цвета!");
				im.setCustomModelData(Integer.valueOf(11 + Main.srnd.nextInt(4)));
				im.setLore(Arrays.asList("§8Бомба на:", "§8" + b.getX(), "§8" + b.getY(), "§8" + b.getZ()));
				it.setItemMeta(im);
				its[i] = it;
				break;
			default:
				its[i] = Main.mkItm(Material.STRING, "§8~-~-~", 11 + Main.srnd.nextInt(4));
				break;
			} 
		} 
		return its;
	}
   
	public static ItemStack[] fillShpInv() {
		final ItemStack[] its = new ItemStack[54];
		its[1] = Main.mkItm(Material.IRON_HOE, "§5AK-47", 1);
		its[3] = Main.mkItm(Material.GOLDEN_HOE, "§5P90", 1);
		its[4] = Main.mkItm(Material.NETHERITE_AXE, "§5AWP", 1);
		its[5] = Main.mkItm(Material.GOLDEN_AXE, "§5MP5", 1);
		its[7] = Main.mkItm(Material.WOODEN_AXE, "§5NOVA", 1);
		its[9] = Main.mkItm(Material.IRON_AXE, "§5M4A1", 1);
		its[13] = Main.mkItm(Material.NETHERITE_HOE, "§5SCAR", 1);
		its[17] = Main.mkItm(Material.WOODEN_HOE, "§5SG-13", 1);
		its[28] = Main.mkItm(Material.SUGAR, "§9Растяжка", 1);
		its[31] = Main.mkItm(Material.BONE, "§fНож", 1);
		its[33] = Main.mkItm(Material.SHEARS, "§3Набор Для Разминировки", 1);
		its[35] = Main.mkItm(Material.STONE_AXE, "§dUSP", 1);
		its[36] = Main.mkItm(Material.OAK_SAPLING, "§cОсколочная Граната", 1);
		its[37] = Main.mkItm(Material.ACACIA_SAPLING, "§6Огненная Граната", 1);
		its[38] = Main.mkItm(Material.BIRCH_SAPLING, "§8Свето-Шумовая Граната", 1);
		its[44] = Main.mkItm(Material.STONE_HOE, "§dTP-7", 1);
		its[46] = Main.mkItm(Material.DARK_OAK_SAPLING, "§7Дымовая Граната", 1);
		its[47] = Main.mkItm(Material.JUNGLE_SAPLING, "§2Отвлекающая Граната", 1);
		its[52] = Main.mkItm(Material.STONE_PICKAXE, "§dDGL", 1);
		return its;
	}
}