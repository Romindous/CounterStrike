package me.Romindous.CounterStrike.Listeners;

import java.util.Arrays;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import me.Romindous.CounterStrike.Main;
import me.Romindous.CounterStrike.Commands.CSCmd;
import me.Romindous.CounterStrike.Enums.GunType;
import me.Romindous.CounterStrike.Enums.NadeType;
import me.Romindous.CounterStrike.Game.Arena;
import me.Romindous.CounterStrike.Game.Defusal;
import me.Romindous.CounterStrike.Game.Invasion;
import me.Romindous.CounterStrike.Game.Arena.Team;
import me.Romindous.CounterStrike.Objects.Shooter;
import me.Romindous.CounterStrike.Utils.PacketUtils;
import ru.komiss77.ApiOstrov;
import ru.komiss77.enums.Stat;

public class InventLis implements Listener {
   
	@EventHandler
	public void onOpen(final InventoryOpenEvent e) {
		e.setCancelled(e.getInventory().getHolder() != null && e.getPlayer().getGameMode() != GameMode.CREATIVE);
	}
	
	@EventHandler
	public void onSlot(final PlayerItemHeldEvent e) {
		final Player p = e.getPlayer();
		final Shooter pr = Shooter.getShooter(p.getName());
		final Shooter sh = pr;
		sh.shtTm = 0;
		sh.count = 0;
		final ItemStack it = sh.inv.getItem(e.getNewSlot());
		final GunType gt = GunType.getGnTp(it);
		if (gt != null) {
			p.getWorld().playSound(p.getLocation(), gt.prm ? Sound.ITEM_ARMOR_EQUIP_IRON : Sound.ITEM_ARMOR_EQUIP_GOLD, 2, 2);
			if (((Damageable)it.getItemMeta()).hasDamage()) {
				sh.count = (short) ((it.getType().getMaxDurability() - ((Damageable)it.getItemMeta()).getDamage()) * gt.rtm / it.getType().getMaxDurability());
			}
		}
		if (p.isSneaking()) {
			PacketUtils.fkHlmtClnt(p, sh.inv.getHelmet());
			PacketUtils.zoom(p, false);
			e.getPlayer().setSneaking(false);
		}
		
		if (it != null) {
			switch (it.getType()) {
			case GOLDEN_APPLE:
				if (pr.arena != null && !((Defusal) pr.arena).indon) {
					((Defusal) pr.arena).indSts(Main.ds.bh().a(p.getName()).b);
				}
				return;
			default:
				break;
			}
		}
		
		if (p.getInventory().getItemInMainHand() != null) {
			switch (p.getInventory().getItemInMainHand().getType()) {
			case GOLDEN_APPLE:
				if (pr.arena != null && ((Defusal) pr.arena).indon) {
					((Defusal) pr.arena).indSts(Main.ds.bh().a(p.getName()).b);
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
		final ItemStack it = e.getCurrentItem();
		if (e.getClick() == ClickType.NUMBER_KEY) {
			e.setCancelled(true);
			return;
		}
		
		if (e.getView().getTitle().equalsIgnoreCase("§5§lТренировка")) {
			if (e.getCurrentItem() != null) {
				final ItemStack cp;
				e.setCancelled(true);
				if (e.getClickedInventory().getType() != InventoryType.PLAYER && it.getItemMeta().hasDisplayName()) {
					((Player) p).playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_NETHERITE, 2f, 1.5f);
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
			if (it != null) {
				final Shooter pr = Shooter.getShooter(p.getName());
				final ItemStack cp;
				final NadeType nt = NadeType.getNdTp(it);
				final GunType gt = GunType.getGnTp(it);
				e.setCancelled(true);
				if (e.getClickedInventory().getType() != InventoryType.PLAYER && it.getItemMeta().hasDisplayName()) {
					if (gt != null) {
						if (pr.money - gt.prc < 0) {
							p.sendMessage(Main.prf() + "§cУ вас не хватает денег для покупки этого!");
							((Player) p).playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_TURTLE, 1f, 2f);
						} else {
							pr.arena.chngMn(pr, -gt.prc);
							cp = it.clone();
							cp.setAmount(gt.amo);
							final ItemMeta im = cp.getItemMeta();
							im.setLore(Arrays.asList());
							cp.setItemMeta(im);
							p.getInventory().setHeldItemSlot(gt.prm ? 0 : 1);
							p.dropItem(false);
							p.getInventory().setHeldItemSlot(8);
							p.getInventory().setItem(gt.prm ? 0 : 1, cp);
							((Player) p).playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_NETHERITE, 2f, 1.5f);
						}
					} else if (nt != null) {
						if (nt.prm) {
							if (pr.money - nt.prc < 0) {
								p.sendMessage(Main.prf() + "§cУ вас не хватает денег для покупки этого!");
								((Player) p).playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_TURTLE, 1f, 2f);
							} else if (p.getInventory().getItem(3) != null) {
								p.sendMessage(Main.prf() + "§cУ вас уже есть граната в этом слоту!");
								((Player) p).playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_TURTLE, 1f, 2f);
							} else {
								pr.arena.chngMn(pr, -nt.prc);
								cp = it.clone();
								final ItemMeta im = cp.getItemMeta();
								im.setLore(Arrays.asList());
								cp.setItemMeta(im);
								p.getInventory().setItem(3, cp);
								((Player) p).playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_NETHERITE, 2f, 1.5f);
							}
						} else {
							if (pr.money - nt.prc < 0) {
								p.sendMessage(Main.prf() + "§cУ вас не хватает денег для покупки этого!");
								((Player) p).playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_TURTLE, 1f, 2f);
							} else if (p.getInventory().getItem(4) != null && p.getInventory().getItem(4).getType() != it.getType()) {
								p.sendMessage(Main.prf() + "§cУ вас уже есть граната в этом слоту!");
								((Player) p).playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_TURTLE, 1f, 2f);
							} else {
								cp = it.clone();
								final ItemMeta im = cp.getItemMeta();
								im.setLore(Arrays.asList());
								cp.setItemMeta(im);
								if (addSetItm(p.getInventory(), 4, cp)) {
									pr.arena.chngMn(pr, -nt.prc);
									((Player) p).playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_NETHERITE, 2f, 1.5f);
								} else {
									p.sendMessage(Main.prf() + "§cУ вас уже есть 2 таких гранаты!");
								}
							}
						}
					} else {
						switch (it.getType()) {
						case SUGAR:
							if (pr.money - GunType.twrPrc < 0) {
								p.sendMessage(Main.prf() + "§cУ вас не хватает денег для покупки этого!");
								((Player) p).playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_TURTLE, 1f, 2f);
							} else if (p.getInventory().getItem(3) != null) {
								p.sendMessage(Main.prf() + "§cУ вас уже есть граната в этом слоту!");
								((Player) p).playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_TURTLE, 1f, 2f);
							} else {
								pr.arena.chngMn(pr, -GunType.twrPrc);
								cp = it.clone();
								final ItemMeta im = cp.getItemMeta();
								im.setLore(Arrays.asList());
								cp.setItemMeta(im);
								p.getInventory().setItem(3, cp);
								((Player) p).playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_NETHERITE, 2f, 1.5f);
							}
							break;
						case SHEARS:
							if (pr.money - GunType.dfktPrc < 0) {
								p.sendMessage(Main.prf() + "§cУ вас не хватает денег для покупки этого!");
								((Player) p).playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_TURTLE, 1f, 2f);
							} else if (p.getInventory().getItem(7) != null && p.getInventory().getItem(7).getType() == Material.SHEARS) {
								p.sendMessage(Main.prf() + "§cУ вас уже есть спец. набор!");
								((Player) p).playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_TURTLE, 1f, 2f);
							} else {
								pr.arena.chngMn(pr, -GunType.dfktPrc);
								cp = it.clone();
								final ItemMeta im = cp.getItemMeta();
								im.setLore(Arrays.asList());
								cp.setItemMeta(im);
								p.getInventory().setItem(7, cp);
								((Player) p).playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_NETHERITE, 2f, 1.5f);
							}
							break;
						case LEATHER_HELMET:
							if (pr.money - GunType.hlmtPrc < 0) {
								p.sendMessage(Main.prf() + "§cУ вас не хватает денег для покупки этого!");
								((Player) p).playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_TURTLE, 1f, 2f);
							} else if (p.getInventory().getHelmet() != null) {
								p.sendMessage(Main.prf() + "§cУ вас уже есть шлем!");
								((Player) p).playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_TURTLE, 1f, 2f);
							} else {
								pr.arena.chngMn(pr, -GunType.hlmtPrc);
								cp = it.clone();
								final ItemMeta im = cp.getItemMeta();
								im.setLore(Arrays.asList());
								cp.setItemMeta(im);
								p.getInventory().setHelmet(cp);
								((Player) p).playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_IRON, 2f, 1.5f);
							}
							break;
						case LEATHER_CHESTPLATE:
							if (pr.money - GunType.chstPrc < 0) {
								p.sendMessage(Main.prf() + "§cУ вас не хватает денег для покупки этого!");
								((Player) p).playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_TURTLE, 1f, 2f);
							} else if (p.getInventory().getChestplate() != null) {
								p.sendMessage(Main.prf() + "§cУ вас уже есть нагрудник!");
								((Player) p).playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_TURTLE, 1f, 2f);
							} else {
								cp = it.clone();
								final ItemMeta im = cp.getItemMeta();
								im.setLore(Arrays.asList());
								cp.setItemMeta(im);
								pr.arena.chngMn(pr, -GunType.chstPrc);
								p.getInventory().setChestplate(cp);
								((Player) p).playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_IRON, 2f, 1.5f);
							}
							break;
						default:
							break;
						}
					}
				}
			}
		} else if (e.getView().getTitle().equalsIgnoreCase("§5§lВыбор Игры")) {
			e.setCancelled(true);
			if (it != null) {
				switch (it.getType()) {
				case GREEN_CONCRETE_POWDER:
				case YELLOW_CONCRETE_POWDER:
				case ORANGE_CONCRETE_POWDER:
				case PURPLE_CONCRETE_POWDER:
					if (it.hasItemMeta()) {
						p.getWorld().playSound(p.getLocation(), Sound.BLOCK_IRON_TRAPDOOR_CLOSE, 2f, 2f);
						final String nm = it.getItemMeta().getDisplayName().substring(2);
						final Arena ar = Main.actvarns.get(nm);
						if (ar == null) {
							if (Main.nnactvarns.containsKey(nm)) {
								CSCmd.partyJoinMap(Shooter.getShooter(p.getName()), (Player) p, Main.plug.crtArena(nm));
								
							} else {
								p.sendMessage(Main.prf() + "§cТакой карты не существует!");
							}
						} else {
							CSCmd.partyJoinMap(Shooter.getShooter(p.getName()), (Player) p, ar);
						}
					}
					break;
				case ENDER_EYE:
					if (it.hasItemMeta()) {
						p.getWorld().playSound(p.getLocation(), Sound.BLOCK_IRON_TRAPDOOR_CLOSE, 2f, 2f);
						final Arena ar = CSCmd.biggestArena();
						if (ar == null) {
							if (Main.nnactvarns.size() > 0) {
								CSCmd.partyJoinMap(Shooter.getShooter(p.getName()), (Player) p, Main.plug.crtArena((String) Main.rndElmt(Main.nnactvarns.keySet().toArray())));
							} else {
								p.sendMessage(Main.prf() + ChatColor.RED + "Ни одной карты еще не создано!");
							}
						} else {
							CSCmd.partyJoinMap(Shooter.getShooter(p.getName()), (Player) p, ar);
						}
					}
					break;
				default:
					break;
				}
			}
		} else if (e.getView().getTitle().equalsIgnoreCase("§eВыбор Комманды")) {
			e.setCancelled(true);
			final Shooter pr = Shooter.getShooter(p.getName());
			if (pr.arena != null && it != null) {
				switch (it.getType()) {
				case WARPED_NYLIUM:
					((Player) p).playSound(p.getLocation(), Sound.BLOCK_NETHER_GOLD_ORE_FALL, 2f, 2f);
					pr.arena.chngTm(pr, Team.CTs);
					break;
				case CRIMSON_NYLIUM:
					((Player) p).playSound(p.getLocation(), Sound.BLOCK_NETHER_GOLD_ORE_FALL, 2f, 2f);
					pr.arena.chngTm(pr, Team.Ts);
					break;
				case ENDER_EYE:
					((Player) p).playSound(p.getLocation(), Sound.BLOCK_NETHER_GOLD_ORE_FALL, 2f, 2f);
					pr.arena.chngTm(pr, Team.NA);
					break;
				default:
					break;
				}
			}
		} else if (e.getView().getTitle().equalsIgnoreCase("§3§lРазминировка Бомбы")) {
			e.setCancelled(true);
			if (it != null && it.getType() == Material.STRING) {
				final Shooter pr = Shooter.getShooter(p.getName());
				final Arena ar = pr.arena;
				if (it.getItemMeta().getCustomModelData() == e.getClickedInventory().getItem(0).getItemMeta().getCustomModelData()) {
					final ItemStack cp = it.clone();
					final ItemMeta im = it.getItemMeta();
					im.setCustomModelData(10);
					((Player) p).playSound(p.getLocation(), Sound.BLOCK_TRIPWIRE_CLICK_ON, 1f, 2f);
					it.setItemMeta(im);
					if (!e.getClickedInventory().contains(cp)) {
						((Player) p).getWorld().playSound(p.getLocation(), Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE, 2f, 2f);
						((Player) p).getWorld().playSound(p.getLocation(), Sound.BLOCK_IRON_TRAPDOOR_CLOSE, 2f, 2f);
						p.closeInventory();
						if (ar != null && ar instanceof Defusal && pr != null) {
							((Defusal) ar).defuse();
							ar.chngMn(pr, 250);
							ApiOstrov.addStat((Player) p, Stat.CS_bomb);
						}
					} 
				} else {
					if (ar != null && ar instanceof Defusal) {
						((Defusal) ar).wrngWire();
					}
					((Player) p).playSound(p.getLocation(), Sound.ENTITY_PLAYER_HURT_SWEET_BERRY_BUSH, 1f, 2f);
				} 
			}
		} else if (e.getView().getTitle().equalsIgnoreCase("§3§lОбезвреживание Спавнера")) {
			e.setCancelled(true);
			if (it != null && it.getType() == Material.STRING) {
				final Shooter pr = Shooter.getShooter(p.getName());
				if (it.getItemMeta().getCustomModelData() == e.getClickedInventory().getItem(0).getItemMeta().getCustomModelData()) {
					final ItemStack cp = it.clone();
					final ItemMeta im = it.getItemMeta();
					im.setCustomModelData(10);
					((Player) p).playSound(p.getLocation(), Sound.BLOCK_TRIPWIRE_CLICK_ON, 1f, 2f);
					it.setItemMeta(im);
					if (!e.getClickedInventory().contains(cp)) {
						((Player) p).getWorld().playSound(p.getLocation(), Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE, 2f, 2f);
						((Player) p).getWorld().playSound(p.getLocation(), Sound.BLOCK_IRON_TRAPDOOR_CLOSE, 2f, 2f);
						p.closeInventory();
						if (pr.arena != null && pr.arena instanceof Invasion && pr != null) {
							((Invasion) pr.arena).rmvSpnr(pr);
							ApiOstrov.addStat((Player) p, Stat.CS_spnrs);
						}
					} 
				} else {
					if (pr.arena != null && pr.arena instanceof Invasion && pr != null) {
						((Invasion) pr.arena).wrngWire(pr);
					}
					((Player) p).playSound(p.getLocation(), Sound.ENTITY_PLAYER_HURT_SWEET_BERRY_BUSH, 1f, 2f);
				} 
			}
		} else {
			e.setCancelled((p.getGameMode() != GameMode.CREATIVE));
			if (it != null && it.hasItemMeta() && it.getItemMeta().getDisplayName().equals("§сВыход")) {
				((Player) p).performCommand("cs leave");
			}
		} 
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