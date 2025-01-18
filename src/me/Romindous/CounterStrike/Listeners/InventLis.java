package me.Romindous.CounterStrike.Listeners;

import io.papermc.paper.datacomponent.DataComponentTypes;
import me.Romindous.CounterStrike.Enums.GunType;
import me.Romindous.CounterStrike.Enums.NadeType;
import me.Romindous.CounterStrike.Game.Arena;
import me.Romindous.CounterStrike.Game.Defusal;
import me.Romindous.CounterStrike.Main;
import me.Romindous.CounterStrike.Objects.Game.PlShooter;
import me.Romindous.CounterStrike.Objects.Shooter;
import me.Romindous.CounterStrike.Objects.Skins.Quest;
import org.bukkit.GameMode;
import org.bukkit.Material;
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
		sh.shtTm(0);
		sh.count(0);
		final ItemStack it = sh.item(e.getNewSlot());
		final GunType gt = GunType.get(it);
		if (gt != null) {
			p.getWorld().playSound(p.getLocation(), gt.prm ? Sound.ITEM_ARMOR_EQUIP_IRON : Sound.ITEM_ARMOR_EQUIP_GOLD, 2, 2);
			final Integer dmg = it.getData(DataComponentTypes.DAMAGE);
			if (dmg != null) sh.count(Main.maxDur(it) - dmg);
		}

		if (p.isSneaking()) {
			sh.scope(false);
			if (!ItemUtil.isBlank(p.getInventory().getItemInOffHand(), false))
				p.getInventory().setItemInOffHand(Main.air);
		}

		if (ItemUtil.is(it, ItemType.GOLDEN_APPLE)) {
			if (sh.arena() instanceof final Defusal df
				&& !df.indon) df.indSts(p);
		}

		if (ItemUtil.is(p.getInventory().getItemInMainHand(), ItemType.GOLDEN_APPLE)) {
			if (sh.arena() instanceof final Defusal df
				&& df.indon) df.indSts(p);
		}
    }

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
	public void onClick(final InventoryClickEvent e) {
		final HumanEntity p = e.getWhoClicked();
		final ItemStack it = e.getCurrentItem();
		switch (e.getAction()) {
		case CLONE_STACK, COLLECT_TO_CURSOR, MOVE_TO_OTHER_INVENTORY:
			e.setCancelled(true);
			return;
		default:
			break;
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
						p.getInventory().setItem(gt.prm ? 0 : 1, new ItemBuilder(gt.type()).name((shq == null ? "§5" + gt.name() :
								"§5" + gt.name() + " '" + Main.nrmlzStr(shq.name()) + "'") + " " + gt.icn)
							.amount(gt.amo).maxDamage(gt.rtm).model(gt.skin(shq == null ? GunType.DEF_MDL : shq.model)).build());
						break;
					}
					final NadeType nt = NadeType.getNdTp(it);
					if (nt != null) {
						if (nt.prm) p.getInventory().setItem(3, it);
						else addSetItm(p.getInventory(), 4, it);
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
					if (gt != null) {
						if (sh.money() - gt.prc < 0) {
							p.sendMessage(Main.prf() + "§cУ вас не хватает денег для покупки этого!");
							((Player) p).playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_TURTLE, 1f, 2f);
						} else {
							sh.arena().chngMn(sh, -gt.prc);
							p.getInventory().setHeldItemSlot(gt.prm ? 0 : 1);
							p.dropItem(false);
							p.getInventory().setHeldItemSlot(8);
							final Quest shq = Quest.get(gt, sh.model(gt));
							p.getInventory().setItem(gt.prm ? 0 : 1,
							new ItemBuilder(gt.type()).name((shq == null ? "§5" + gt.name() :
									"§5" + gt.name() + " '" + Main.nrmlzStr(shq.name()) + "'") + " " + gt.icn)
							.amount(gt.amo).maxDamage(gt.rtm).model(gt.skin(shq == null ? GunType.DEF_MDL : shq.model)).build());
							p.getWorld().playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_NETHERITE, 1f, 1.4f);
						}
					} else if (nt != null) {
						if (nt.prm) {
							if (sh.money() - nt.prc < 0) {
								p.sendMessage(Main.prf() + "§cУ вас не хватает денег для покупки этого!");
								((Player) p).playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_TURTLE, 1f, 2f);
							} else if (p.getInventory().getItem(NadeType.prmSlot) != null) {
								p.sendMessage(Main.prf() + "§cУ вас уже есть граната в этом слоту!");
								((Player) p).playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_TURTLE, 1f, 2f);
							} else {
								sh.arena().chngMn(sh, -nt.prc);
								p.getInventory().setItem(NadeType.prmSlot, new ItemBuilder(it).deLore().build());
								p.getWorld().playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_DIAMOND, 1f, 1.4f);
							}
						} else {
							if (sh.money() - nt.prc < 0) {
								p.sendMessage(Main.prf() + "§cУ вас не хватает денег для покупки этого!");
								((Player) p).playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_TURTLE, 1f, 2f);
							} else if (p.getInventory().getItem(NadeType.scdSlot) != null &&
								p.getInventory().getItem(NadeType.scdSlot).getType() != it.getType()) {
								p.sendMessage(Main.prf() + "§cУ вас уже есть граната в этом слоту!");
								((Player) p).playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_TURTLE, 1f, 2f);
							} else {
								cp = new ItemBuilder(it).deLore().build();
								if (addSetItm(p.getInventory(), NadeType.scdSlot, cp)) {
									sh.arena().chngMn(sh, -nt.prc);
									p.getWorld().playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_DIAMOND, 1f, 1.4f);
								} else {
									p.sendMessage(Main.prf() + "§cУ вас уже есть 2 таких гранаты!");
								}
							}
						}
					} else {
						switch (it.getType()) {
						case SUGAR:
							if (sh.money() - Shooter.wirePrc < 0) {
								p.sendMessage(Main.prf() + "§cУ вас не хватает денег для покупки этого!");
								((Player) p).playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_TURTLE, 1f, 2f);
							} else if (p.getInventory().getItem(3) != null) {
								p.sendMessage(Main.prf() + "§cУ вас уже есть граната в этом слоту!");
								((Player) p).playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_TURTLE, 1f, 2f);
							} else {
								sh.arena().chngMn(sh, -Shooter.wirePrc);
								p.getInventory().setItem(3, new ItemBuilder(it).deLore().build());
								p.getWorld().playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_DIAMOND, 1f, 1.4f);
							}
							break;
						case SHEARS:
							if (sh.money() - Shooter.kitPrc < 0) {
								p.sendMessage(Main.prf() + "§cУ вас не хватает денег для покупки этого!");
								((Player) p).playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_TURTLE, 1f, 2f);
							} else if (p.getInventory().getItem(7) != null && p.getInventory().getItem(7).getType() == Material.SHEARS) {
								p.sendMessage(Main.prf() + "§cУ вас уже есть спец. набор!");
								((Player) p).playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_TURTLE, 1f, 2f);
							} else {
								sh.arena().chngMn(sh, -Shooter.kitPrc);
								p.getInventory().setItem(7, new ItemBuilder(it).deLore().build());
								p.getWorld().playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_DIAMOND, 1f, 1.4f);
							}
							break;
						case LEATHER_HELMET:
							if (sh.money() - Shooter.helmPrc < 0) {
								p.sendMessage(Main.prf() + "§cУ вас не хватает денег для покупки этого!");
								((Player) p).playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_TURTLE, 1f, 2f);
							} else if (p.getInventory().getHelmet() != null) {
								p.sendMessage(Main.prf() + "§cУ вас уже есть шлем!");
								((Player) p).playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_TURTLE, 1f, 2f);
							} else {
								sh.arena().chngMn(sh, -Shooter.helmPrc);
								p.getInventory().setHelmet(new ItemBuilder(it).deLore().build());
								p.getWorld().playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_IRON, 1f, 1.4f);
							}
							break;
						case LEATHER_CHESTPLATE:
							if (sh.money() - Shooter.chestPrc < 0) {
								p.sendMessage(Main.prf() + "§cУ вас не хватает денег для покупки этого!");
								((Player) p).playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_TURTLE, 1f, 2f);
							} else if (p.getInventory().getChestplate() != null) {
								p.sendMessage(Main.prf() + "§cУ вас уже есть нагрудник!");
								((Player) p).playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_TURTLE, 1f, 2f);
							} else {
								sh.arena().chngMn(sh, -Shooter.chestPrc);
								p.getInventory().setChestplate(new ItemBuilder(it).deLore().build());
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

	private boolean addSetItm(final PlayerInventory inv, final int slt, final ItemStack it) {
		final ItemStack s = inv.getItem(slt);
		if (s != null && s.getType() == it.getType()) {
			if (s.getAmount() == 1) {
				s.setAmount(2);
			} else {
				return false;
			}
		} else {
			inv.setItem(slt, it);
		}
		return true;
	}
}