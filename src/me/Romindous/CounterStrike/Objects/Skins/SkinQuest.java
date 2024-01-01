package me.Romindous.CounterStrike.Objects.Skins;

import java.util.Arrays;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import me.Romindous.CounterStrike.Main;
import me.Romindous.CounterStrike.Enums.GunType;
import me.Romindous.CounterStrike.Objects.Shooter;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;

public class SkinQuest implements InventoryProvider {

	public static final ItemStack icon = new ItemBuilder(Material.CLOCK).name("§6ВыБери Обшивку").build();
	public static final ItemStack clear = new ItemBuilder(Material.MUSIC_DISC_PIGSTEP).name("§eСброс Обшивок").build();
	
	@Override
	public void init(final Player pl, final InventoryContent its) {
		final Oplayer op = PM.getOplayer(pl);
		final Shooter sh = Shooter.getPlShooter(op.nik, true);
		pl.playSound(pl.getLocation(), Sound.BLOCK_BEEHIVE_SHEAR, 1f, 0.6f);
		
		final Inventory inv = its.getInventory();
		inv.setContents(fillSkinInv(op.hasGroup("warior")));
		
		its.set(13, ClickableItem.empty(icon));
		its.set(40, ClickableItem.of(clear, e -> {
			for (final GunType gt : GunType.values()) {
				sh.setModel(gt, GunType.defCMD);
			}
			reopen(pl, its);
		}));
		
		for (final GunType gt : GunType.values()) {
			final int shm = sh.getModel(gt);
			final int oslt = (gt.ordinal() >> 1) * 9 + ((gt.ordinal() & 1) == 0 ? 1 : 5);
			for (int i = 0; i != 3; i++) {
				final Quest q = Quest.getQuest(gt, GunType.defCMD + i);
				final ItemStack gn;
				if (q == null) {
					if (i == 0) {
						if (shm == GunType.defCMD) {
							gn = new ItemBuilder(gt.getMat())
							.name("§5" + gt.toString()).setModelData(GunType.defCMD + i)
							.addLore(Arrays.asList("§7Ванильная общивка снаряжения!"))
							.addEnchant(Enchantment.BINDING_CURSE).build();
							its.set(oslt + i, ClickableItem.empty(gn));
						} else {
							gn = new ItemBuilder(gt.getMat())
							.name("§5" + gt.toString()).setModelData(GunType.defCMD + i)
							.addLore(Arrays.asList("§7Ванильная общивка снаряжения!", " ", "§6Клик §7==> Выбор")).build();
							its.set(oslt + i, ClickableItem.of(gn, e -> {
								pl.playSound(pl.getLocation(), Sound.ITEM_ARMOR_EQUIP_NETHERITE, 2f, 1.5f);
								sh.setModel(gt, GunType.defCMD);
								reopen(pl, its);
							}));
						}
					}
				} else {
					if (shm == q.cmd) {
						gn = new ItemBuilder(gt.getMat()).setModelData(GunType.defCMD + i)
						.name("§5" + gt.toString() + " '" + Main.nrmlzStr(q.toString()) + "'")
						.addLore(Arrays.asList("§7Для получения:", q.msg, " ", "§eКлик §7==> Сброс"))
						.addEnchant(Enchantment.BINDING_CURSE).build();
						its.set(oslt + i, ClickableItem.of(gn, e -> {
							pl.playSound(pl.getLocation(), Sound.ITEM_ARMOR_EQUIP_NETHERITE, 2f, 1.5f);
							sh.setModel(gt, GunType.defCMD);
							reopen(pl, its);
						}));
					} else {
						if (sh.hasModel(gt, i + GunType.defCMD) || op.hasGroup("warior")) {
							gn = new ItemBuilder(gt.getMat()).setModelData(GunType.defCMD + i)
							.name("§5" + gt.toString() + " '" + Main.nrmlzStr(q.toString()) + "'")
							.addLore(Arrays.asList("§7Для получения:", q.msg, " ", "§6Клик §7==> Выбор")).build();
							its.set(oslt + i, ClickableItem.of(gn, e -> {
								pl.playSound(pl.getLocation(), Sound.ITEM_ARMOR_EQUIP_NETHERITE, 2f, 1.5f);
								sh.setModel(gt, q.cmd);
								reopen(pl, its);
							}));
						} else {
							gn = new ItemBuilder(Material.GRAY_DYE).setModelData(GunType.defCMD + i)
							.name("§5" + gt.toString() + " '" + Main.nrmlzStr(q.toString()) + "'")
							.addLore(Arrays.asList("§7Для получения:", q.msg)).build();
							//its.set(oslt + i, ClickableItem.empty(gn));
							its.set(oslt + i, ClickableItem.empty(gn));
						}
					}
				}
			}
		}
	}
	
	private static ItemStack[] fillSkinInv(final boolean donate) {
		final ItemStack[] its = new ItemStack[54];
		final ItemStack rl = new ItemBuilder(donate ? Material.POWERED_RAIL : Material.ACTIVATOR_RAIL).name("§0.").build();
		for (int r = 0; r != 3; r++) {
			for (int c = 0; c != 6; c++) {
				its[(r << 2) + c * 9] = rl;
			}
		}

		final ItemStack blnk = new ItemBuilder(Material.LIGHT_GRAY_STAINED_GLASS_PANE).name("§0.").build();
		for (int i = its.length - 1; i >= 0; i--) {
			if (its[i] == null) its[i] = blnk;
		}
		return its;
	}
	
	public static void tryCompleteQuest(final Shooter sh, final Quest qst, final int stat) {
		if (sh.hasModel(qst.gun, qst.cmd)) return;
		switch (qst) {
		case ВОЙ, ДУША, ДЮНА, ЗЕМЛЯ, ЛГБТ, ОКЕАН, ТОКСИК, ГРУЗЧИК, ЛАТУНЬ:
			if (stat < qst.stat) return;
			break;
		case ПАНК:
			if (stat > qst.stat) return;
			break;
		case КРОВЬ, АЗИМОВ:
			if (stat == 0) return;
			break;
		}
		
		sh.giveModel(qst.gun, qst.cmd);
		final Player p = sh.getPlayer();
		p.sendMessage("§8=-=-=-=-=-=-=-\n§7Поздравляю! Скин оружия\n§5" + 
		qst.gun.toString() + " §7-§d " + Main.nrmlzStr(qst.toString()) + 
		"\n§7теперь есть в твоем наборе!\n\n§7Можешь §eвыбрать §7его в лобби!\n§8=-=-=-=-=-=-=-=-=-");
		p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
	}

}
