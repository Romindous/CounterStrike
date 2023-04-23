package me.Romindous.CounterStrike.Listeners;

import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

import me.Romindous.CounterStrike.Main;
import me.Romindous.CounterStrike.Enums.GunType;
import me.Romindous.CounterStrike.Enums.NadeType;
import me.Romindous.CounterStrike.Game.Arena;
import me.Romindous.CounterStrike.Game.Arena.Team;
import me.Romindous.CounterStrike.Game.Defusal;
import me.Romindous.CounterStrike.Game.Gungame;
import me.Romindous.CounterStrike.Game.Invasion;
import me.Romindous.CounterStrike.Objects.Shooter;
import me.Romindous.CounterStrike.Objects.Game.GameState;
import me.Romindous.CounterStrike.Objects.Game.Nade;
import me.Romindous.CounterStrike.Objects.Game.TripWire;
import me.Romindous.CounterStrike.Objects.Map.MapManager;
import me.Romindous.CounterStrike.Objects.Mobs.Mobber;
import me.Romindous.CounterStrike.Objects.Skins.Quest;
import me.Romindous.CounterStrike.Objects.Skins.SkinQuest;
import me.Romindous.CounterStrike.Utils.Inventories;
import me.Romindous.CounterStrike.Utils.PacketUtils;
import net.kyori.adventure.text.Component;
import net.minecraft.core.BaseBlockPosition;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.monster.EntityShulker;
import net.minecraft.world.level.World;
import ru.komiss77.ApiOstrov;
import ru.komiss77.enums.Stat;
import ru.komiss77.utils.inventory.SmartInventory;

public class InterrLis implements Listener {

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEntIntr(final EntityInteractEvent e) {
		final Block b = e.getBlock();
		if (e.getEntity() instanceof Mob && b != null) {
			e.setCancelled(true);
			final Invasion ar = Invasion.getMobInvasion(e.getEntity().getEntityId());
			if (ar != null && ar.gst == GameState.ROUND) {
				switch (b.getType()) {
				case WARPED_PRESSURE_PLATE:
					if (ar.ast.da().equals(new BaseBlockPosition(b.getX(), b.getY() - 1, b.getZ()))) {
						ar.hrtSt(true, e.getEntity().getName().charAt(0));
						e.getEntity().remove();
					} else if (ar.bst.da().equals(new BaseBlockPosition(b.getX(), b.getY() - 1, b.getZ()))) {
						ar.hrtSt(false, e.getEntity().getName().charAt(0));
						e.getEntity().remove();
					}
					break;
				case TRIPWIRE:
					final Iterator<TripWire> i = ar.tws.iterator();
					while (i.hasNext()) {
						final TripWire tn = i.next();
						for (final Block bl : tn.bs) {
							if (b.getX() == bl.getX() && b.getY() == bl.getY() && b.getZ() == bl.getZ()) {
								e.setCancelled(true);
								for (final Block r : tn.bs) {
									r.setType(Material.AIR, false);
								}
								tn.hdNdAll(ar);
								tn.trgr(bl.getLocation().add(0.5d, 0d, 0.5d));
								i.remove();
								return;
							}
						}
					}
					break;
				default:
					break;
				}
			}
		}
		if (e.getEntityType() != EntityType.PLAYER && e.getBlock() != null && e.getBlock().getType() == Material.WARPED_PRESSURE_PLATE) {
			e.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onInter(final PlayerInteractEvent e) {
		final Block b;
		final ItemStack it = e.getItem();
		final GunType gt = GunType.getGnTp(it);
		final NadeType nt = NadeType.getNdTp(it);
		final Shooter sh;
		switch (e.getAction()) {
		case LEFT_CLICK_AIR:
		case LEFT_CLICK_BLOCK:
			if (gt != null) {
				e.setCancelled(true);
				sh = Shooter.getPlShooter(e.getPlayer().getName(), true);
				if (!((Damageable)it.getItemMeta()).hasDamage()) {
					Main.setDmg(it, 0);
					sh.shtTm(0);
					sh.count(0);
				}
			} else if (nt != null) {
				e.getPlayer().setExp((e.getPlayer().getExp() == 1f) ? 0f : (e.getPlayer().getExp() + 0.5F > 1f ? 1f : e.getPlayer().getExp() + 0.5F));
			}
			break;
		case RIGHT_CLICK_AIR:
		case RIGHT_CLICK_BLOCK:
			e.setCancelled(true);
			final Player p = e.getPlayer();
			if (gt != null) {
				sh = Shooter.getPlShooter(p.getName(), true);
				final boolean hd = ((Damageable)it.getItemMeta()).hasDamage();
				if (sh.shtTm() == 0) {
					if ((sh.count() == 0 || hd) && sh.cldwn() == 0) {
						final int tr = sh.count() < sh.rclTm() ? sh.count() : sh.rclTm();
						if (it.getAmount() == 1) {
							if (hd) {
								return;
							}
							Main.setDmg(it, 0);
							sh.count(0);
						} else {
							if (hd) {
								Main.setDmg(it, it.getType().getMaxDurability());
								sh.count(0);
							}
							it.setAmount(it.getAmount() - 1);
						}
						sh.cldwn(gt.cld);
						final boolean iw = gt.snp && p.isSneaking();
						for (byte i = gt.brst == 0 ? 1 : gt.brst; i > 0; i--) {
							sh.shoot(gt, !iw, tr);
						}
						Main.plyWrldSht(p.getLocation(), gt.snd);
						if (iw) {
							PacketUtils.fkHlmtClnt(p, p.getInventory().getHelmet());
							PacketUtils.zoom(p, false);
							p.setSneaking(false);
						}
						//p.setVelocity(p.getVelocity().subtract(p.getEyeLocation().getDirection().multiply(gt.kb)));
					}
				}
				sh.shtTm(5);
			} else if (nt != null) {
				sh = Shooter.getPlShooter(p.getName(), true);
				if (sh.arena() != null) {
					switch (sh.arena().gst) {
					case BUYTIME:
					case ENDRND:
					case FINISH:
						return;
					default:
						break;
					}
				}
				b = e.getClickedBlock();
				if (b != null && b.getType() == Material.TRIPWIRE && !nt.prm) {
					e.setCancelled(true);
					final Arena ar = Main.actvarns.get(sh.arena().name);
					if (ar != null) {
						final Team tm = ar.shtrs.get(sh);
						for (final TripWire tn : ar.tws) {
							if (tm == tn.tm) {
								for (final Block bl : tn.bs) {
									if (b.getX() == bl.getX() && b.getY() == bl.getY() && b.getZ() == bl.getZ()) {
										tn.chngNt(sh.getPlayer().getInventory().getItemInMainHand(), ar);
										it.setAmount(it.getAmount() - 1);
										p.sendMessage(Main.prf() + "§7Вы зарядили растяжку гранатой!");
										p.getWorld().playSound(b.getLocation(), Sound.ITEM_CROSSBOW_LOADING_END, 1f, 1f);
										return;
									}
								}
							}
						}
					}
				}
				final Snowball sb = (Snowball) p.launchProjectile(Snowball.class);
				sb.setItem(it);
				it.setAmount(it.getAmount() - 1);
				p.getInventory().setItemInMainHand(it);
				sb.setVelocity(sb.getVelocity().multiply(0.75f * p.getExp() + 0.25f));
				Main.nades.add(new Nade(sb, (int) (nt.time * p.getExp()) + 4));
				Main.plyWrldSht(p.getLocation(), "cs.rand.nadethrow");
			} else {
				e.setCancelled(p.getGameMode() != GameMode.CREATIVE);
				if (it != null) {
					b = e.getClickedBlock();
					switch (it.getType()) {
					case GHAST_TEAR:
						sh = Shooter.getPlShooter(p.getName(), true);
						if (sh.arena() == null) {
							p.playSound(p.getLocation(), Sound.BLOCK_BEEHIVE_SHEAR, 2f, 2f);
							p.openInventory(Inventories.LBShop);
						} else {
							final Team tm = sh.arena().shtrs.get(sh);
							final Inventory inv;
							switch (sh.arena().gst) {
							case BEGINING:
							case WAITING:
								p.playSound(p.getLocation(), Sound.BLOCK_BEEHIVE_SHEAR, 2f, 2f);
								p.openInventory(Inventories.LBShop);
								break;
							case BUYTIME:
								p.playSound(p.getLocation(), Sound.BLOCK_BEEHIVE_SHEAR, 2f, 2f);
								switch (tm) {
								case Ts:
									inv = Inventories.TShop;
									if (Inventories.isBlankItem(inv.getItem(4), false)) {
										Inventories.fillTsInv();
									}
									break;
								case CTs:
									inv = Inventories.CTShop;
									if (Inventories.isBlankItem(inv.getItem(4), false)) {
										Inventories.fillCTsInv();
									}
									break;
								case NA:
								default:
									inv = Inventories.LBShop;
									if (Inventories.isBlankItem(inv.getItem(4), false)) {
										Inventories.fillLbbInv();
									}
									break;
								}
								p.openInventory(inv);
								break;
							case ROUND:
								if (sh.arena() instanceof Defusal && ((Defusal) sh.arena()).isBmbOn()) {
									PacketUtils.sendAcBr(p, "§c§lВремя закупки вышло, бомба постовлена!", 30);
								} else if (sh.arena().canOpnShp(p.getLocation(), tm)) {
									switch (tm) {
									case Ts:
										inv = Inventories.TShop;
										if (Inventories.isBlankItem(inv.getItem(4), false)) {
											Inventories.fillTsInv();
										}
										break;
									case CTs:
										inv = Inventories.CTShop;
										if (Inventories.isBlankItem(inv.getItem(4), false)) {
											Inventories.fillCTsInv();
										}
										break;
									case NA:
									default:
										inv = Inventories.LBShop;
										if (Inventories.isBlankItem(inv.getItem(4), false)) {
											Inventories.fillLbbInv();
										}
										break;
									}
									p.openInventory(inv);
								} else {
									PacketUtils.sendAcBr(p, "§c§lЗакупатся можно только на спавне!", 30);
								}
								break;
							case ENDRND:
							case FINISH:
								break;
							}
						}
						break;
					case GOLDEN_APPLE:
					case CRIMSON_BUTTON:
						e.setCancelled(false);
						break;
					case STICK:
						final World w = PacketUtils.getNMSWrld(p.getWorld());
						final EntityShulker ind = new EntityShulker(EntityTypes.aB, w);
						ind.setPosRaw(p.getLocation().getX() + 0.5d, p.getLocation().getY(), p.getLocation().getZ() + 0.5d, false);
						w.addFreshEntity(ind, SpawnReason.CUSTOM);
						break;
					case GOLD_NUGGET:
					case SHEARS:
						final boolean bg = it.getType() == Material.GOLD_NUGGET;
						sh = Shooter.getPlShooter(p.getName(), true);
						if (b != null && sh.arena() != null && p.getGameMode() == GameMode.SURVIVAL) {
							e.setCancelled(true);
							if (b.getType() == Material.CRIMSON_BUTTON) {
								final Inventory def = Bukkit.createInventory(null, bg ? 54 : 27, Component.text("§3§lРазминировка Бомбы"));
								p.getWorld().playSound(b.getLocation(), Sound.BLOCK_BEEHIVE_SHEAR, 2f, 0.5f);
								def.setContents(Inventories.fillDfsInv(b, (byte) (bg ? 54 : 27)));
								p.openInventory(def);
							} else if (b.getType() == Material.SPAWNER && sh.arena() instanceof Invasion) {
								for (final Mobber m : ((Invasion) sh.arena()).sis) {
									if (m.ind.da().equals(new BaseBlockPosition(b.getX(), b.getY(), b.getZ()))) {
										final Inventory def = Bukkit.createInventory(null, 54, "§3§lОбезвреживание Спавнера");
										p.getWorld().playSound(b.getLocation(), Sound.BLOCK_BEEHIVE_SHEAR, 2f, 0.5f);
										def.setContents(Inventories.fillDfSpInv(b, (byte) 54, bg));
										p.openInventory(def);
										((Invasion) sh.arena()).dfs.put(sh, m);
									}
								}
							}
						}
						break;
					case SUGAR:
						if (b != null) {
							sh = Shooter.getPlShooter(p.getName(), true);
							if (sh.arena() != null) {
								switch (sh.arena().gst) {
								case BUYTIME:
								case ENDRND:
								case FINISH:
									return;
								default:
									break;
								}
							}
							switch (e.getBlockFace()) {
							case EAST:
							case WEST:
							case NORTH:
							case SOUTH:
								if (crtTrpwr(b, e.getBlockFace(), p)) {
									it.setAmount(0);
								}
								break;
							default:
								p.sendMessage(Main.prf() + "§cРастяжку можно закреплять только на стены!");
								break;
							}
						}
						break;
					case CAMPFIRE:
						if (it.hasItemMeta() && it.getItemMeta().getDisplayName().equals("§dВыбор Игры")) {
							final Inventory inv = Inventories.GmInv;
							if (Inventories.isBlankItem(inv.getItem(4), false)) {
								Inventories.fillGmInv();
							}
							p.playSound(p.getLocation(), Sound.BLOCK_BEEHIVE_ENTER, 1f, 2f);
							p.openInventory(inv);
						}
						break;
					case TOTEM_OF_UNDYING:
						if (it.hasItemMeta() && it.getItemMeta().getDisplayName().equals("§eВыбор Обшивки")) {
							SmartInventory.builder().size(6, 9)
                            .id("Skins "+p.getName())
                            .title("§6Выберите Обшивку")
                            .provider(new SkinQuest())
                            .build().open(p);
						}
						break;
					case NETHER_STAR:
						if (it.hasItemMeta() && it.getItemMeta().getDisplayName().equals("§eВыбор Комманды")) {
							sh = Shooter.getPlShooter(p.getName(), true);
							p.playSound(p.getLocation(), Sound.BLOCK_CONDUIT_ATTACK_TARGET, 1f, 2f);
							if (sh.arena() != null) {
								if (sh.arena() instanceof Defusal) {
									p.openInventory(((Defusal) sh.arena()).tms);
								} else if (sh.arena() instanceof Gungame) {
									p.openInventory(((Gungame) sh.arena()).tms);
								}
							}
						}
						break;
					case EMERALD:
						if (MapManager.edits.containsKey(p.getUniqueId())) {
							SmartInventory.builder().size(3, 9)
	                        .id("Map "+p.getName()).title("§d§lРедактор Карты")
	                        .provider(MapManager.edits.get(p.getUniqueId()))
	                        .build().open(p);
						}
						break;
					case MAGMA_CREAM:
						if (it.hasItemMeta() && it.getItemMeta().getDisplayName().equals("§4Выход в Лобби")) {
							ApiOstrov.sendToServer(p, "lobby1", "");
						}
						break;
					case SLIME_BALL:
						if (it.hasItemMeta() && it.getItemMeta().getDisplayName().equals("§cВыход")) {
							final Arena ar = Shooter.getPlShooter(p.getName(), true).arena();
							if (ar == null) {
								p.sendMessage(Main.prf() + "§cВы не находитесь в игре!");
							} else {
								ar.rmvPl(Shooter.getPlShooter(p.getName(), true));
							}
						}
						break;
					default:
						break;
					}
				}
			}
			break;   
		case PHYSICAL:
			b = e.getClickedBlock();
			if (b != null) {
				switch (b.getType()) {
				case TRIPWIRE:
					sh = Shooter.getPlShooter(e.getPlayer().getName(), true);
					final Arena ar = sh.arena();
					final Iterator<TripWire> i = ar.tws.iterator();
					while (i.hasNext()) {
						final TripWire tn = i.next();
						for (final Block bl : tn.bs) {
							if (b.getX() == bl.getX() && b.getY() == bl.getY() && b.getZ() == bl.getZ()) {
								e.setCancelled(true);
								if (ar.name.equals(sh.arena().name) && ar.shtrs.get(sh) != tn.tm) {
									for (final Block r : tn.bs) {
										r.setType(Material.AIR, false);
									}
									tn.hdNdAll(ar);
									tn.trgr(bl.getLocation().add(0.5d, 0d, 0.5d));
									i.remove();
								}
								return;
							}
						}
					}
					break;
				case WARPED_PRESSURE_PLATE:
					e.setCancelled(true);
					break;
				default:
					break;
				}
			}
			break;
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlc(final BlockPlaceEvent e) {
		final Player p = e.getPlayer();
		final Shooter sh = Shooter.getPlShooter(p.getName(), true);
		final Arena ar = sh.arena();
		if (Main.plnts.containsKey(p) && ar != null && ar instanceof Defusal && ar.gst == GameState.ROUND) {
			if (Math.abs(e.getBlockPlaced().getY() - p.getLocation().getBlockY()) < 3) {
				Main.plnts.remove(p);
				((Defusal) ar).plant(e.getBlockPlaced());
				((Defusal) ar).indSts(p);
				ar.chngMn(sh, 250);
				ApiOstrov.addStat(p, Stat.CS_bomb);
				SkinQuest.tryCompleteQuest(sh, Quest.ОКЕАН, ApiOstrov.getStat(p, Stat.CS_bomb));
            	e.setCancelled(false);
			} else {
            	PacketUtils.sendAcBr(p, "§c§lУ вас не дотягиваются руки!", 20);
            	e.setCancelled(true);
			}
			return;
		}
		e.setCancelled((p.getGameMode() != GameMode.CREATIVE));
	}
   
	@EventHandler
	public void onEat(final PlayerItemConsumeEvent e) {
		e.setCancelled(true);
		final Player p = e.getPlayer();
		final Arena ar = Shooter.getPlShooter(p.getName(), true).arena();
		if (e.getItem().getType() == Material.GOLDEN_APPLE && ar != null && ar instanceof Defusal && ar.gst == GameState.ROUND) {
			if (canPlcBmb(p.getLocation(), (Defusal) ar)) {
				p.getInventory().setItemInMainHand(new ItemStack(Material.CRIMSON_BUTTON));
				Main.plnts.put(p, new BaseBlockPosition(p.getLocation().getBlockX(), p.getLocation().getBlockY(), p.getLocation().getBlockZ()));
				PacketUtils.sendAcBr(p, "§d§lВыбирите место для установки бомбы...", 30);
			} else {
            	PacketUtils.sendAcBr(p, "§c§lБомбу можно ставить только на точках!", 20);
			}
		}
	}
   
	private boolean canPlcBmb(final Location loc, final Defusal ar) {
		return (Math.abs(loc.getX() - ar.ast.u()) < 3 && Math.abs(loc.getY() - ar.ast.v()) < 3 && Math.abs(loc.getZ() - ar.ast.w()) < 3) 
			|| 
			(Math.abs(loc.getX() - ar.bst.u()) < 3 && Math.abs(loc.getY() - ar.bst.v()) < 3 && Math.abs(loc.getZ() - ar.bst.w()) < 3);
	}

	public boolean crtTrpwr(final Block b, final BlockFace bf, final Player p) {
		if (b.getType().isSolid()) {
			final byte r = 5;
			final Block[] twbs = new Block[r];
			for (byte i = 0; i < r + 1; i++) {
				final Block t = b.getRelative(bf, i + 1);
				if (t.getType().isAir()) {
					if (i == r) {
						p.sendMessage(Main.prf() + "§cРастяжка не может тянутся настолько далеко!");
						return false;
					}
					twbs[i] = t;
				}else {
					if (i == 0) {
						p.sendMessage(Main.prf() + "§cСтавьте растяжку на открытое место!");
						return false;
					}
					if (t.getType().isOccluding()) {
						final Shooter sh = Shooter.getPlShooter(p.getName(), true);
						if (sh.arena() != null) {
							sh.arena().tws.add(new TripWire(twbs, sh, bf, i));
							p.getWorld().playSound(b.getLocation(), Sound.ITEM_CROSSBOW_QUICK_CHARGE_1, 2f, 1f);
							p.sendMessage(Main.prf() + "§7Растяжка поставлена, \n§7можете прицепить на нее гранату!");
							return true;
						}
					}
					p.sendMessage(Main.prf() + "§cРастяжку можно крепить только на целые блоки!");
					return false;
				}
			}
			return false;
		}
		p.sendMessage(Main.prf() + "§cРастяжку можно крепить только на целые блоки!");
		return false;
	}
}