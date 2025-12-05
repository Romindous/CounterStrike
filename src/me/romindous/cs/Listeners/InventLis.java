package me.romindous.cs.Listeners;

import io.papermc.paper.datacomponent.DataComponentTypes;
import me.romindous.cs.Enums.GunType;
import me.romindous.cs.Enums.NadeType;
import me.romindous.cs.Game.Arena;
import me.romindous.cs.Game.Defusal;
import me.romindous.cs.Main;
import me.romindous.cs.Objects.Game.PlShooter;
import me.romindous.cs.Objects.Shooter;
import me.romindous.cs.Objects.Skins.Quest;
import me.romindous.cs.Utils.Utils;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.PlayerInventory;
import ru.komiss77.Ostrov;
import ru.komiss77.modules.items.ItemBuilder;
import ru.komiss77.utils.ItemUtil;
import ru.komiss77.utils.TCUtil;

public class InventLis implements Listener {

	@EventHandler
	public void onOpen(final InventoryOpenEvent e) {
		final InventoryHolder hld = e.getInventory().getHolder();
		if (hld == null || hld instanceof Player) return;
		e.setCancelled(e.getPlayer().getGameMode() != GameMode.CREATIVE);
	}

	@EventHandler
	public void onSlot(final PlayerItemHeldEvent e) {
		final Player p = e.getPlayer();
		final PlShooter sh = Shooter.getPlShooter(p.getName(), true);
		sh.shtTm(0); sh.count(0);
		final ItemStack it = sh.item(e.getNewSlot());
		final ItemStack old = sh.item(e.getPreviousSlot());

		final ItemType bmbTp = Main.bmb.getType().asItemType();
		if (ItemUtil.is(it, bmbTp)) {
			if (sh.arena() instanceof final Defusal df
				&& !df.indon) df.indSts(p);
		}

		if (ItemUtil.is(old, bmbTp)) {
			if (sh.arena() instanceof final Defusal df
				&& df.indon) df.indSts(p);
		}

		final ItemType shpTp = ItemType.GHAST_TEAR;
		if (ItemUtil.is(it, shpTp) && sh.arena() != null)
			sh.arena().indSpawn(p, sh, true);

		if (ItemUtil.is(old, shpTp) && sh.arena() != null)
			sh.arena().indSpawn(p, sh, false);

		final GunType gt = GunType.get(it);
		if (gt == null) {
			Utils.spy(p, false, p.isSneaking());
			return;
		}
		p.getWorld().playSound(p.getLocation(), gt.prm
			? Sound.ITEM_ARMOR_EQUIP_IRON : Sound.ITEM_ARMOR_EQUIP_GOLD, 2f, 2f);
		final Integer dmg = it.getData(DataComponentTypes.DAMAGE);
		if (Main.hasDur(it)) sh.count(Main.maxDur(it) - dmg);
		Ostrov.sync(() -> Utils.spy(p, gt.snp, p.isSneaking()), 1);
    }

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
	public void onClick(final InventoryClickEvent e) {
		final HumanEntity p = e.getWhoClicked();
		final ItemStack it = e.getCurrentItem();
		switch (e.getAction()) {
		case CLONE_STACK, COLLECT_TO_CURSOR, MOVE_TO_OTHER_INVENTORY:
			e.setCancelled(true);
			return;
		}
		if (e.getClick() == ClickType.NUMBER_KEY) {
			e.setCancelled(true);
			return;
		}
		final String ttl = TCUtil.strip(e.getView().title());
		final Shooter sh;
		//p.sendMessage(ttl);
		switch (ttl) {
		case "Тренировка":
			if (e.getCurrentItem() != null) {
				e.setCancelled(true);
				if (e.getClickedInventory().getType() != InventoryType.PLAYER && it.getItemMeta().hasDisplayName()) {
					p.getWorld().playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_NETHERITE, 2f, 1.5f);
					final GunType gt = GunType.get(it);
					if (gt != null) {
						sh = Shooter.getPlShooter(p.getName(), true);
						final Quest shq = Quest.get(gt, sh.model(gt));
						sh.item(gt.prm ? 0 : 1, gt.item().name((shq == null ? "§5" + gt.name() :
								"§5" + gt.name() + " '" + shq.name + "'") + " " + gt.icn).amount(gt.amo)
							.maxDamage(gt.rtm).model(gt.skin(shq == null ? GunType.DEF_MDL : shq.model)).build());
						break;
					}
					final NadeType nt = NadeType.getNdTp(it);
					if (nt != null) {
						sh = Shooter.getPlShooter(p.getName(), true);
						if (nt.prm) sh.item(3, it);
						else addSetItm(sh, 4, it);
						break;
					}

					p.getInventory().setItem(7, it);
					break;
				}
			}
			break;
		case "Магазин Спецназа":
		case "Магазин Террористов":
			if (it != null) {
				sh = Shooter.getPlShooter(p.getName(), true);
				final ItemStack cp;
				final NadeType nt = NadeType.getNdTp(it);
				final GunType gt = GunType.get(it);
				e.setCancelled(true);
				if (e.getClickedInventory().getType() != InventoryType.PLAYER && it.getItemMeta().hasDisplayName()) {
					final PlayerInventory pi = p.getInventory();
					if (gt != null) {
						if (sh.money() - gt.prc < 0) {
							p.sendMessage(Main.prf() + "§cУ тебя не хватает денег для покупки этого!");
							((Player) p).playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_TURTLE, 1f, 2f);
						} else {
							sh.arena().chngMn(sh, -gt.prc);
							pi.setHeldItemSlot(gt.prm ? 0 : 1);
							final ItemStack oit = pi.getItem(gt.prm ? 0 : 1);
							if (!ItemUtil.isBlank(oit, false)) {
								Main.setDur(oit, 0);
								oit.setAmount(1);
								p.getWorld().dropItem(p.getLocation(), oit)
									.setInvulnerable(true);
							}
							pi.setHeldItemSlot(8);
							final Quest shq = Quest.get(gt, sh.model(gt));
							sh.item(gt.prm ? 0 : 1,
							gt.item().name((shq == null ? "§5" + gt.name() : "§5" + gt.name() + " '" + shq.name + "'") + " " + gt.icn)
							.amount(gt.amo).maxDamage(gt.rtm).model(gt.skin(shq == null ? GunType.DEF_MDL : shq.model)).build());
							p.getWorld().playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_NETHERITE, 1f, 1.4f);
						}
					} else if (nt != null) {
						if (nt.prm) {
							if (sh.money() - nt.prc < 0) {
								p.sendMessage(Main.prf() + "§cУ тебя не хватает денег для покупки этого!");
								((Player) p).playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_TURTLE, 1f, 2f);
							} else if (pi.getItem(NadeType.prmSlot) != null) {
								p.sendMessage(Main.prf() + "§cУ тебя уже есть граната в этом слоту!");
								((Player) p).playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_TURTLE, 1f, 2f);
							} else {
								sh.arena().chngMn(sh, -nt.prc);
								sh.item(NadeType.prmSlot, new ItemBuilder(it).deLore().build());
								p.getWorld().playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_DIAMOND, 1f, 1.4f);
							}
						} else {
							if (sh.money() - nt.prc < 0) {
								p.sendMessage(Main.prf() + "§cУ тебя не хватает денег для покупки этого!");
								((Player) p).playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_TURTLE, 1f, 2f);
							} else if (pi.getItem(NadeType.scdSlot) != null &&
								sh.item(NadeType.scdSlot).getType() != it.getType()) {
								p.sendMessage(Main.prf() + "§cУ тебя уже есть граната в этом слоту!");
								((Player) p).playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_TURTLE, 1f, 2f);
							} else {
								cp = new ItemBuilder(it).deLore().build();
								if (addSetItm(sh, NadeType.scdSlot, cp)) {
									sh.arena().chngMn(sh, -nt.prc);
									p.getWorld().playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_DIAMOND, 1f, 1.4f);
								} else {
									p.sendMessage(Main.prf() + "§cУ тебя уже есть 2 таких гранаты!");
								}
							}
						}
					} else {
						switch (it.getType()) {
						case SUGAR:
							if (sh.money() - Shooter.wirePrc < 0) {
								p.sendMessage(Main.prf() + "§cУ тебя не хватает денег для покупки этого!");
								((Player) p).playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_TURTLE, 1f, 2f);
							} else if (pi.getItem(3) != null) {
								p.sendMessage(Main.prf() + "§cУ тебя уже есть граната в этом слоту!");
								((Player) p).playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_TURTLE, 1f, 2f);
							} else {
								sh.arena().chngMn(sh, -Shooter.wirePrc);
								sh.item(3, new ItemBuilder(it).deLore().build());
								p.getWorld().playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_DIAMOND, 1f, 1.4f);
							}
							break;
						case SHEARS:
							if (sh.money() - Shooter.kitPrc < 0) {
								p.sendMessage(Main.prf() + "§cУ тебя не хватает денег для покупки этого!");
								((Player) p).playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_TURTLE, 1f, 2f);
							} else if (ItemUtil.is(pi.getItem(7), ItemType.SHEARS)) {
								p.sendMessage(Main.prf() + "§cУ тебя уже есть спец. набор!");
								((Player) p).playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_TURTLE, 1f, 2f);
							} else {
								sh.arena().chngMn(sh, -Shooter.kitPrc);
								sh.item(7, new ItemBuilder(it).deLore().build());
								p.getWorld().playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_DIAMOND, 1f, 1.4f);
							}
							break;
						case LEATHER_HELMET:
							if (sh.money() - Shooter.helmPrc < 0) {
								p.sendMessage(Main.prf() + "§cУ тебя не хватает денег для покупки этого!");
								((Player) p).playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_TURTLE, 1f, 2f);
							} else if (!ItemUtil.isBlank(pi.getHelmet(), false)) {
								p.sendMessage(Main.prf() + "§cУ тебя уже есть шлем!");
								((Player) p).playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_TURTLE, 1f, 2f);
							} else {
								sh.arena().chngMn(sh, -Shooter.helmPrc);
								pi.setHelmet(new ItemBuilder(it).deLore().build());
								p.getWorld().playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_IRON, 1f, 1.4f);
							}
							break;
						case LEATHER_CHESTPLATE:
							if (sh.money() - Shooter.chestPrc < 0) {
								p.sendMessage(Main.prf() + "§cУ тебя не хватает денег для покупки этого!");
								((Player) p).playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_TURTLE, 1f, 2f);
							} else if (!ItemUtil.isBlank(pi.getChestplate(), false)) {
								p.sendMessage(Main.prf() + "§cУ тебя уже есть нагрудник!");
								((Player) p).playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_TURTLE, 1f, 2f);
							} else {
								sh.arena().chngMn(sh, -Shooter.chestPrc);
								pi.setChestplate(new ItemBuilder(it).deLore().build());
								p.getWorld().playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_IRON, 1f, 1.4f);
							}
							break;
						default:
							break;
						}
					}
				}
			}
			break;
		default:
			e.setCancelled(p.getGameMode() != GameMode.CREATIVE);
			if (!ItemUtil.isBlank(it, true)
				&& p.getGameMode() == GameMode.SPECTATOR) {
				switch (it.getType()) {
					case SLIME_BALL:
						((Player) p).performCommand("cs leave");
						break;
					case NETHER_STAR:
						sh = Shooter.getPlShooter(p.getName(), true);
						final Arena ar = sh.arena();
						if (ar != null) {
							ar.teamInv.open((Player) p);
						}
						break;
					default:
						break;
				}
			}
			break;
		}
	}

	@EventHandler
	public void onDrag(final InventoryDragEvent e) {
		e.setCancelled(true);
	}

	private boolean addSetItm(final Shooter sh, final int slt, final ItemStack it) {
		final ItemStack s = sh.item(slt);
		if (s != null && s.getType() == it.getType()) {
			if (s.getAmount() == 1) {
				s.setAmount(2);
			} else {
				return false;
			}
		} else {
			sh.item(slt, it);
		}
		return true;
	}
}