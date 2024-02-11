package me.Romindous.CounterStrike.Listeners;

import java.util.Arrays;

import me.Romindous.CounterStrike.Objects.Mobs.Mobber;
import org.bukkit.*;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import me.Romindous.CounterStrike.Main;
import me.Romindous.CounterStrike.Commands.CSCmd;
import me.Romindous.CounterStrike.Enums.GunType;
import me.Romindous.CounterStrike.Enums.NadeType;
import me.Romindous.CounterStrike.Game.Arena;
import me.Romindous.CounterStrike.Game.Arena.Team;
import me.Romindous.CounterStrike.Game.Defusal;
import me.Romindous.CounterStrike.Game.Invasion;
import me.Romindous.CounterStrike.Objects.Shooter;
import me.Romindous.CounterStrike.Objects.Game.PlShooter;
import me.Romindous.CounterStrike.Objects.Map.Setup;
import me.Romindous.CounterStrike.Objects.Map.TypeChoose;
import me.Romindous.CounterStrike.Objects.Skins.Quest;
import ru.komiss77.ApiOstrov;
import ru.komiss77.enums.Stat;
import ru.komiss77.modules.world.WXYZ;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.ItemUtils;
import ru.komiss77.utils.TCUtils;
import ru.komiss77.utils.inventory.SmartInventory;

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
		final GunType gt = GunType.getGnTp(it);
		if (gt != null) {
			p.getWorld().playSound(p.getLocation(), gt.prm ? Sound.ITEM_ARMOR_EQUIP_IRON : Sound.ITEM_ARMOR_EQUIP_GOLD, 2, 2);
			if (((Damageable)it.getItemMeta()).hasDamage()) {
				sh.count((it.getType().getMaxDurability() - ((Damageable)it.getItemMeta()).getDamage()) * gt.rtm / it.getType().getMaxDurability());
			}
		}
		
		if (p.isSneaking()) {
			sh.scope(false);
			if (!ItemUtils.isBlank(p.getInventory().getItemInOffHand(), false))
				p.getInventory().setItemInOffHand(Main.air);
		}
		
		if (it != null) {
			switch (it.getType()) {
			case GOLDEN_APPLE:
				if (sh.arena() != null && !((Defusal) sh.arena()).indon) {
					((Defusal) sh.arena()).indSts(p);
				}
				return;
			default:
				break;
			}
		}

        if (p.getInventory().getItemInMainHand().getType() == Material.GOLDEN_APPLE) {
            if (sh.arena() != null && ((Defusal) sh.arena()).indon) {
                ((Defusal) sh.arena()).indSts(p);
            }
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
		final String ttl = TCUtils.stripColor(e.getView().title());
		final Shooter sh;
		//p.sendMessage(ttl);
		switch (ttl) {
		case "Тренировка":
			if (e.getCurrentItem() != null) {
				e.setCancelled(true);
				if (e.getClickedInventory().getType() != InventoryType.PLAYER && it.getItemMeta().hasDisplayName()) {
					p.getWorld().playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_NETHERITE, 2f, 1.5f);
					final int mdl;
					final GunType gt;
					switch (TCUtils.toString(it.getItemMeta().displayName()).charAt(1)) {
					case '5':
						sh = Shooter.getPlShooter(p.getName(), true);
						gt = GunType.getGnTp(it);
						mdl = sh.getModel(gt);
						p.getInventory().setItem(0, new ItemBuilder(it.getType()).name((mdl == GunType.defCMD ? "§5" + gt.toString() : 
							"§5" + gt.toString() + " '" + Main.nrmlzStr(Quest.getQuest(gt, mdl).toString()) + "'") + " " + gt.icn)
						.setAmount(gt.amo).setModelData(mdl).build());
						break;
					case 'd':
						sh = Shooter.getPlShooter(p.getName(), true);
						gt = GunType.getGnTp(it);
						mdl = sh.getModel(gt);
						p.getInventory().setItem(1, new ItemBuilder(it.getType()).name((mdl == GunType.defCMD ? "§5" + gt.toString() : 
							"§5" + gt.toString() + " '" + Main.nrmlzStr(Quest.getQuest(gt, mdl).toString()) + "'") + " " + gt.icn)
						.setAmount(gt.amo).setModelData(mdl).build());
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
			break;
		case "Магазин Спецназа":
		case "Магазин Террористов":
			if (it != null) {
				sh = Shooter.getPlShooter(p.getName(), true);
				final ItemStack cp;
				final NadeType nt = NadeType.getNdTp(it);
				final GunType gt = GunType.getGnTp(it);
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
							final int mdl = sh.getModel(gt);
							p.getInventory().setItem(gt.prm ? 0 : 1, 
							new ItemBuilder(it.getType()).name((mdl == GunType.defCMD ? "§5" + gt.toString() : 
								"§5" + gt.toString() + " '" + Main.nrmlzStr(Quest.getQuest(gt, mdl).toString()) + "'") + " " + gt.icn)
							.setAmount(gt.amo).setModelData(mdl).build());
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
								cp = it.clone();
								final ItemMeta im = cp.getItemMeta();
								im.lore(Arrays.asList());
								cp.setItemMeta(im);
								p.getInventory().setItem(NadeType.prmSlot, cp);
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
								cp = it.clone();
								final ItemMeta im = cp.getItemMeta();
								im.lore(Arrays.asList());
								cp.setItemMeta(im);
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
							if (sh.money() - GunType.twrPrc < 0) {
								p.sendMessage(Main.prf() + "§cУ вас не хватает денег для покупки этого!");
								((Player) p).playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_TURTLE, 1f, 2f);
							} else if (p.getInventory().getItem(3) != null) {
								p.sendMessage(Main.prf() + "§cУ вас уже есть граната в этом слоту!");
								((Player) p).playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_TURTLE, 1f, 2f);
							} else {
								sh.arena().chngMn(sh, -GunType.twrPrc);
								cp = it.clone();
								final ItemMeta im = cp.getItemMeta();
								im.lore(Arrays.asList());
								cp.setItemMeta(im);
								p.getInventory().setItem(3, cp);
								p.getWorld().playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_DIAMOND, 1f, 1.4f);
							}
							break;
						case SHEARS:
							if (sh.money() - GunType.dfktPrc < 0) {
								p.sendMessage(Main.prf() + "§cУ вас не хватает денег для покупки этого!");
								((Player) p).playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_TURTLE, 1f, 2f);
							} else if (p.getInventory().getItem(7) != null && p.getInventory().getItem(7).getType() == Material.SHEARS) {
								p.sendMessage(Main.prf() + "§cУ вас уже есть спец. набор!");
								((Player) p).playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_TURTLE, 1f, 2f);
							} else {
								sh.arena().chngMn(sh, -GunType.dfktPrc);
								cp = it.clone();
								final ItemMeta im = cp.getItemMeta();
								im.lore(Arrays.asList());
								cp.setItemMeta(im);
								p.getInventory().setItem(7, cp);
								p.getWorld().playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_DIAMOND, 1f, 1.4f);
							}
							break;
						case LEATHER_HELMET:
							if (sh.money() - GunType.hlmtPrc < 0) {
								p.sendMessage(Main.prf() + "§cУ вас не хватает денег для покупки этого!");
								((Player) p).playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_TURTLE, 1f, 2f);
							} else if (p.getInventory().getHelmet() != null) {
								p.sendMessage(Main.prf() + "§cУ вас уже есть шлем!");
								((Player) p).playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_TURTLE, 1f, 2f);
							} else {
								sh.arena().chngMn(sh, -GunType.hlmtPrc);
								cp = it.clone();
								final ItemMeta im = cp.getItemMeta();
								im.lore(Arrays.asList());
								cp.setItemMeta(im);
								p.getInventory().setHelmet(cp);
								p.getWorld().playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_IRON, 1f, 1.4f);
							}
							break;
						case LEATHER_CHESTPLATE:
							if (sh.money() - GunType.chstPrc < 0) {
								p.sendMessage(Main.prf() + "§cУ вас не хватает денег для покупки этого!");
								((Player) p).playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_TURTLE, 1f, 2f);
							} else if (p.getInventory().getChestplate() != null) {
								p.sendMessage(Main.prf() + "§cУ вас уже есть нагрудник!");
								((Player) p).playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_TURTLE, 1f, 2f);
							} else {
								cp = it.clone();
								final ItemMeta im = cp.getItemMeta();
								im.lore(Arrays.asList());
								cp.setItemMeta(im);
								sh.arena().chngMn(sh, -GunType.chstPrc);
								p.getInventory().setChestplate(cp);
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
		case "Выбор Игры":
			e.setCancelled(true);
			if (it != null) {
				switch (it.getType()) {
				case GREEN_CONCRETE_POWDER:
				case YELLOW_CONCRETE_POWDER:
				case ORANGE_CONCRETE_POWDER:
				case PURPLE_CONCRETE_POWDER:
					if (it.hasItemMeta()) {
						p.getWorld().playSound(p.getLocation(), Sound.BLOCK_IRON_TRAPDOOR_CLOSE, 2f, 2f);
						final String nm = TCUtils.toString(it.getItemMeta().displayName()).substring(2);
						final Arena ar = Main.actvarns.get(nm);
						if (ar == null) {
							final Setup stp = Main.nnactvarns.get(nm);
							if (stp == null) {
								p.sendMessage(Main.prf() + "§cТакой карты не существует!");
								return;
							}
							SmartInventory.builder()
							.type(InventoryType.HOPPER)
	                        .id("Game "+p.getName())
	                        .provider(new TypeChoose(stp))
	                        .title("§d§l      Выбор Типа Игры")
	                        .build().open((Player) p);
						} else {
							CSCmd.partyJoinMap(Shooter.getPlShooter(p.getName(), true), (Player) p, ar);
						}
					}
					break;
				case ENDER_EYE:
					if (it.hasItemMeta()) {
						p.getWorld().playSound(p.getLocation(), Sound.BLOCK_IRON_TRAPDOOR_CLOSE, 2f, 2f);
						final Arena ar = CSCmd.biggestArena();
						if (ar == null) {
							if (Main.nnactvarns.size() > 0) {
								SmartInventory.builder()
								.type(InventoryType.HOPPER)
		                        .id("Game "+p.getName())
		                        .provider(new TypeChoose(ApiOstrov.rndElmt(Main.nnactvarns.values().toArray(new Setup[0]))))
		                        .title("§d§l      Выбор Типа Игры")
		                        .build().open((Player) p);
							} else {
								p.sendMessage(Main.prf() + "§cНи одной карты еще не создано!");
							}
						} else {
							CSCmd.partyJoinMap(Shooter.getPlShooter(p.getName(), true), (Player) p, ar);
						}
					}
					break;
				default:
					break;
				}
			}
			break;
		case "Выбор Комманды":
			e.setCancelled(true);
			sh = Shooter.getPlShooter(p.getName(), true);
			if (sh.arena() != null && it != null) {
				switch (it.getType()) {
				case WARPED_NYLIUM:
					((Player) p).playSound(p.getLocation(), Sound.BLOCK_NETHER_GOLD_ORE_FALL, 2f, 2f);
					sh.arena().chngTm(sh, Team.CTs);
					break;
				case CRIMSON_NYLIUM:
					((Player) p).playSound(p.getLocation(), Sound.BLOCK_NETHER_GOLD_ORE_FALL, 2f, 2f);
					sh.arena().chngTm(sh, Team.Ts);
					break;
				case ENDER_EYE:
					((Player) p).playSound(p.getLocation(), Sound.BLOCK_NETHER_GOLD_ORE_FALL, 2f, 2f);
					sh.arena().chngTm(sh, Team.SPEC);
					break;
				default:
					break;
				}
			}
			break;
		case "Разминировка Бомбы":
			e.setCancelled(true);
			if (it != null && it.getType() == Material.STRING) {
				sh = Shooter.getPlShooter(p.getName(), true);
				final Arena ar = sh.arena();
				final int cmd = it.getItemMeta().getCustomModelData();
				final Location eye = p.getEyeLocation();
				if (cmd == e.getClickedInventory().getItem(0).getItemMeta().getCustomModelData()) {
					final ItemStack cp = it.clone();
					final ItemMeta im = it.getItemMeta();
					im.setCustomModelData(10);
					ar.w.spawnParticle(Particle.ITEM_CRACK, eye.add(eye.getDirection()),
						8, 0d, 0d, 0d, 0.2d, p.getInventory().getItemInMainHand());
					ar.w.playSound(eye, Sound.BLOCK_TRIPWIRE_CLICK_ON, 1f, 2f);
					p.swingMainHand();
					it.setItemMeta(im);
					if (!e.getClickedInventory().contains(cp)) {
						ar.w.playSound(eye, Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE, 2f, 2f);
						ar.w.playSound(eye, Sound.BLOCK_IRON_TRAPDOOR_CLOSE, 2f, 2f);
						p.closeInventory();
						if (ar instanceof Defusal) {
							((Defusal) ar).defuse();
							ar.chngMn(sh, 250);
							ApiOstrov.addStat((Player) p, Stat.CS_bomb);
						}
					} 
				} else if (cmd != 10) {
					if (ar instanceof Defusal) {
						((Defusal) ar).wrngWire();
					}
					final ItemMeta im = it.getItemMeta();
					im.setCustomModelData(10);
					ar.w.playSound(eye, Sound.BLOCK_GLASS_BREAK, 2f, 2f);
					((Player) p).playSound(eye, Sound.BLOCK_TRIPWIRE_CLICK_ON, 1f, 2f);
					it.setItemMeta(im);
				}
			}
			break;
		case "Обезвреживание Спавнера":
			e.setCancelled(true);
			if (it != null && it.getType() == Material.STRING) {
				sh = Shooter.getPlShooter(p.getName(), true);
				final Arena ar = sh.arena();
				final int cmd = it.getItemMeta().getCustomModelData();
				final Location eye = p.getEyeLocation();
				if (cmd == e.getClickedInventory().getItem(0).getItemMeta().getCustomModelData()) {
					final ItemStack cp = it.clone();
					final ItemMeta im = it.getItemMeta();
					im.setCustomModelData(10);
					ar.w.spawnParticle(Particle.ITEM_CRACK, eye.add(eye.getDirection()),
						8, 0d, 0d, 0d, 0.2d, p.getInventory().getItemInMainHand());
					ar.w.playSound(eye, Sound.BLOCK_TRIPWIRE_CLICK_ON, 1f, 2f);
					p.swingMainHand();
					it.setItemMeta(im);
					if (!e.getClickedInventory().contains(cp)) {
						ar.w.playSound(eye, Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE, 2f, 2f);
						ar.w.playSound(eye, Sound.BLOCK_IRON_TRAPDOOR_CLOSE, 2f, 2f);
						p.closeInventory();
						if (ar instanceof Invasion) {
							((Invasion) sh.arena()).rmvSpnr(sh);
						}
					}
				} else if (cmd != 10) {
					if (ar instanceof Invasion) {
						((Invasion) sh.arena()).wrngWire(sh);
					}
					final ItemMeta im = it.getItemMeta();
					im.setCustomModelData(10);
					ar.w.playSound(eye, Sound.BLOCK_GLASS_BREAK, 2f, 2f);
					((Player) p).playSound(eye, Sound.BLOCK_TRIPWIRE_CLICK_ON, 1f, 2f);
					it.setItemMeta(im);
				} 
			}
			break;
		default:
			e.setCancelled(p.getGameMode() != GameMode.CREATIVE);
			if (!ItemUtils.isBlank(it, true)
				&& p.getGameMode() == GameMode.SPECTATOR) {
				switch (it.getType()) {
					case SLIME_BALL:
						((Player) p).performCommand("cs leave");
						break;
					case NETHER_STAR:
						((Player) p).performCommand("cs join");
						break;
					default:
						break;
				}
			}
			break;
		}
	}

	@EventHandler
	public void oClose(final InventoryCloseEvent e) {
		final HumanEntity p = e.getPlayer();
		final Shooter sh;
		switch (TCUtils.stripColor(e.getView().title())) {
			case "Разминировка Бомбы":
				sh = Shooter.getPlShooter(p.getName(), true);
				if (sh.arena() instanceof final Defusal df && df.isBmbOn()) {
					df.getBomb().defusing = null;
				}
				break;
			case "Обезвреживание Спавнера":
				sh = Shooter.getPlShooter(p.getName(), true);
				if (sh.arena() instanceof final Invasion in) {
					final Mobber mb = in.getClsMbbr(new WXYZ(p.getLocation()), true);
					if (mb != null) mb.defusing = null;
				}
				break;
			default:
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