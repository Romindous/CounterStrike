package me.Romindous.CounterStrike.Listeners;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import com.mojang.datafixers.util.Pair;

import me.Romindous.CounterStrike.Main;
import me.Romindous.CounterStrike.Enums.GunType;
import me.Romindous.CounterStrike.Enums.NadeType;
import me.Romindous.CounterStrike.Game.Arena;
import me.Romindous.CounterStrike.Game.Defusal;
import me.Romindous.CounterStrike.Objects.Shooter;
import me.Romindous.CounterStrike.Utils.PacketUtils;

public class InventLis implements Listener {
   
	@EventHandler
	public void onOpen(final InventoryOpenEvent e) {
		e.setCancelled(e.getInventory().getHolder() != null && e.getPlayer().getGameMode() != GameMode.CREATIVE);
	}
	
	@EventHandler
	public void onSlot(final PlayerItemHeldEvent e) {
		final Player p = e.getPlayer();
		final Pair<Shooter, Arena> pr = Shooter.getPlShtrArena(p.getName());
		final Shooter sh = pr.getFirst();
		sh.is = false;
		sh.cnt = 0;
		final ItemStack it = sh.inv.getItem(e.getNewSlot());
		final GunType gt = GunType.getGnTp(it);
		if (gt != null && ((Damageable)it.getItemMeta()).hasDamage()) {
			sh.cnt = (short) ((it.getType().getMaxDurability() - ((Damageable)it.getItemMeta()).getDamage()) * gt.rtm / it.getType().getMaxDurability());
		}
		if (p.isSneaking()) {
			PacketUtils.fkHlmtClnt(p, sh.inv.getHelmet());
			PacketUtils.zoom(p, false);
			e.getPlayer().setSneaking(false);
		}
		
		if (it != null) {
			switch (it.getType()) {
			case GOLDEN_APPLE:
				if (pr.getSecond() != null && !((Defusal) pr.getSecond()).indon) {
					((Defusal) pr.getSecond()).indSts(PacketUtils.getNMSPlr(p).b);
				}
				return;
			default:
				break;
			}
		}
		
		if (p.getInventory().getItemInMainHand() != null) {
			switch (p.getInventory().getItemInMainHand().getType()) {
			case GOLDEN_APPLE:
				if (pr.getSecond() != null && ((Defusal) pr.getSecond()).indon) {
					((Defusal) pr.getSecond()).indSts(PacketUtils.getNMSPlr(p).b);
				}
				return;
			default:
				break;
			}
		}
	}
   
	@EventHandler
	public void onClick(final InventoryClickEvent e) {
		final HumanEntity p = e.getWhoClicked();
		if (e.getView().getTitle().equalsIgnoreCase("§5§lТренировка")) {
			if (e.getCurrentItem() != null) {
				final ItemStack it = e.getCurrentItem();
				final ItemStack cp;
				e.setCancelled(true);
				if (e.getClickedInventory().getType() != InventoryType.PLAYER && it.getItemMeta().hasDisplayName()) {
					switch (it.getItemMeta().getDisplayName().charAt(1)) {
					case '5':
						cp = it.clone();
						cp.setAmount((GunType.getGnTp(it)).amo);
						p.getInventory().setItem(0, cp);
						break;
					case 'd':
						cp = it.clone();
						cp.setAmount((GunType.getGnTp(it)).amo);
						p.getInventory().setItem(1, cp);
						break;
					case 'f':
						p.getInventory().setItem(2, it);
						break;
					case '6':
					case 'c':
						p.getInventory().setItem(3, it);
						break;
					case '2':
					case '7':
					case '8':
						addSetItm(p.getInventory(), 4, it);
						break;
					case '3':
						p.getInventory().setItem(7, it);
						break;
					}
				}
			}
		} else if (e.getView().getTitle().equalsIgnoreCase("§c§lМагазин Террористов") || e.getView().getTitle().equalsIgnoreCase("§9§lМагазин Спецназа")) {
			if (e.getCurrentItem() != null) {
				final Pair<Shooter, Arena> pr = Shooter.getPlShtrArena(p.getName());
				final ItemStack it = e.getCurrentItem();
				final ItemStack cp;
				final NadeType nt = NadeType.getNdTp(it);
				final GunType gt = GunType.getGnTp(it);
				e.setCancelled(true);
				if (e.getClickedInventory().getType() != InventoryType.PLAYER && it.getItemMeta().hasDisplayName()) {
					if (gt != null) {
						if (pr.getFirst().money - gt.prc < 0) {
							p.sendMessage(Main.prf() + "§cУ вас не хватает денег для покупки этого!");
						} else {
							pr.getSecond().chngMn(pr.getFirst(), -gt.prc);
							cp = it.clone();
							cp.setAmount(gt.amo);
							p.getInventory().setHeldItemSlot(gt.prm ? 0 : 1);
							p.dropItem(false);
							p.getInventory().setHeldItemSlot(8);
							p.getInventory().setItem(gt.prm ? 0 : 1, cp);
						}
					} else if (nt != null) {
						if (nt.prm) {
							if (pr.getFirst().money - nt.prc < 0) {
								p.sendMessage(Main.prf() + "§cУ вас не хватает денег для покупки этого!");
							} else if (p.getInventory().getItem(3) != null) {
								p.sendMessage(Main.prf() + "§cУ вас уже есть граната в этом слоту!");
							} else {
								pr.getSecond().chngMn(pr.getFirst(), -nt.prc);
								p.getInventory().setItem(3, it);
							}
						} else {
							if (pr.getFirst().money - nt.prc < 0) {
								p.sendMessage(Main.prf() + "§cУ вас не хватает денег для покупки этого!");
							} else if (p.getInventory().getItem(4) != null && p.getInventory().getItem(4).getType() != it.getType()) {
								p.sendMessage(Main.prf() + "§cУ вас уже есть граната в этом слоту!");
							} else {
								pr.getSecond().chngMn(pr.getFirst(), -nt.prc);
								addSetItm(p.getInventory(), 4, it);
							}
						}
					} else {
						switch (it.getType()) {
						case SUGAR:
							if (pr.getFirst().money - Main.twrPrc < 0) {
								p.sendMessage(Main.prf() + "§cУ вас не хватает денег для покупки этого!");
							} else if (p.getInventory().getItem(3) != null) {
								p.sendMessage(Main.prf() + "§cУ вас уже есть граната в этом слоту!");
							} else {
								pr.getSecond().chngMn(pr.getFirst(), -Main.twrPrc);
								p.getInventory().setItem(3, it);
							}
							break;
						case SHEARS:
							if (pr.getFirst().money - Main.dfktPrc < 0) {
								p.sendMessage(Main.prf() + "§cУ вас не хватает денег для покупки этого!");
							} else if (p.getInventory().getItem(7) != null && p.getInventory().getItem(7).getType() == Material.SHEARS) {
								p.sendMessage(Main.prf() + "§cУ вас уже есть спец. набор!");
							} else {
								pr.getSecond().chngMn(pr.getFirst(), -Main.dfktPrc);
								p.getInventory().setItem(7, it);
							}
							break;
						case LEATHER_HELMET:
							if (pr.getFirst().money - Main.hlmtPrc < 0) {
								p.sendMessage(Main.prf() + "§cУ вас не хватает денег для покупки этого!");
							} else if (p.getInventory().getHelmet() != null) {
								p.sendMessage(Main.prf() + "§cУ вас уже есть шлем!");
							} else {
								pr.getSecond().chngMn(pr.getFirst(), -Main.hlmtPrc);
								p.getInventory().setHelmet(it);
							}
							break;
						case LEATHER_CHESTPLATE:
							if (pr.getFirst().money - Main.chstPrc < 0) {
								p.sendMessage(Main.prf() + "§cУ вас не хватает денег для покупки этого!");
							} else if (p.getInventory().getChestplate() != null) {
								p.sendMessage(Main.prf() + "§cУ вас уже есть нагрудник!");
							} else {
								pr.getSecond().chngMn(pr.getFirst(), -Main.chstPrc);
								p.getInventory().setChestplate(it);
							}
							break;
						default:
							break;
						}
					}
				}
			}
		} else if (e.getView().getTitle().equalsIgnoreCase("§3§lРазминировка бомбы")) {
			e.setCancelled(true);
			final ItemStack it = e.getCurrentItem();
			if (it != null && it.getType() == Material.STRING) {
				final Arena ar = Shooter.getPlShtrArena(p.getName()).getSecond();
				if (it.getItemMeta().getCustomModelData() == e.getClickedInventory().getItem(0).getItemMeta().getCustomModelData()) {
					final ItemStack cp = it.clone();
					final ItemMeta im = it.getItemMeta();
					if (Main.srnd.nextBoolean()) {
						im.setCustomModelData(Integer.valueOf(10));
						((Player) p).playSound(p.getLocation(), Sound.BLOCK_TRIPWIRE_CLICK_ON, 1f, 2f);
					} else {
						im.setCustomModelData(Integer.valueOf(11 + Main.srnd.nextInt(4)));
						((Player) p).playSound(p.getLocation(), Sound.BLOCK_LODESTONE_PLACE, 1f, 2f);
					} 
					it.setItemMeta(im);
					if (!e.getClickedInventory().contains(cp)) {
						((Player) p).playSound(p.getLocation(), Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE, 1f, 2f);
						((Player) p).playSound(p.getLocation(), Sound.BLOCK_IRON_TRAPDOOR_CLOSE, 1f, 2f);
						p.closeInventory();
						if (ar != null && ar instanceof Defusal) {
							((Defusal) ar).defuse();
						}
					} 
				} else {
					if (ar != null && ar instanceof Defusal) {
						((Defusal) ar).wrngWire();
					}
					((Player) p).playSound(p.getLocation(), Sound.ENTITY_PLAYER_HURT_SWEET_BERRY_BUSH, 1f, 2f);
				} 
			} 
		} else {
			e.setCancelled((p.getGameMode() != GameMode.CREATIVE));
		} 
	}
   
	private void addSetItm(final PlayerInventory inv, final int slt, final ItemStack it) {
		final ItemStack s = inv.getItem(slt);
		if (s != null && s.getType() == it.getType()) {
			s.setAmount(s.getAmount() == 1 ? (s.getAmount() + 1) : s.getAmount());
		} else {
			inv.setItem(slt, it);
		} 
	}
}