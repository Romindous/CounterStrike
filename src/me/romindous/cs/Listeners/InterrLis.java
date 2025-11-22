package me.romindous.cs.Listeners;

import io.papermc.paper.event.player.PlayerStopUsingItemEvent;
import io.papermc.paper.math.Position;
import me.romindous.cs.Enums.GameState;
import me.romindous.cs.Enums.GunType;
import me.romindous.cs.Enums.NadeType;
import me.romindous.cs.Game.Arena;
import me.romindous.cs.Game.Arena.Team;
import me.romindous.cs.Game.Defusal;
import me.romindous.cs.Game.Invasion;
import me.romindous.cs.Main;
import me.romindous.cs.Menus.ChosenSkinMenu;
import me.romindous.cs.Objects.Game.*;
import me.romindous.cs.Objects.Loc.Info;
import me.romindous.cs.Objects.Map.MapManager;
import me.romindous.cs.Objects.Shooter;
import me.romindous.cs.Objects.Skins.Quest;
import me.romindous.cs.Utils.Inventories;
import me.romindous.cs.Utils.Utils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockType;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Ostrov;
import ru.komiss77.enums.Stat;
import ru.komiss77.modules.world.BVec;
import ru.komiss77.objects.IntHashMap;
import ru.komiss77.utils.EntityUtil;
import ru.komiss77.utils.ItemUtil;
import ru.komiss77.utils.ScreenUtil;
import ru.komiss77.utils.TCUtil;
import ru.komiss77.utils.inventory.SmartInventory;

public class InterrLis implements Listener {

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEntIntr(final EntityInteractEvent e) {
		final Block b = e.getBlock();
        if (e.getEntity() instanceof Player) return;
        e.setCancelled(true);
        if (!(e.getEntity() instanceof final Mob mb)) return;
        final Invasion ar = Invasion.getMobInvasion(mb.getEntityId());
        if (ar == null || ar.gst != GameState.ROUND) return;
        final BlockType bt = b.getType().asBlockType();
        if (bt == BlockType.WARPED_PRESSURE_PLATE) {
            if (BVec.of(ar.ads.getLocation()).distAbs(BVec.of(b)) == 0) {
                e.setCancelled(true);
                ar.hrtSt(true, (byte) (2 + Mobber.MobType.get(e.getEntityType()).pow << 1));
                mb.getWorld().spawnParticle(Particle.SOUL, EntityUtil.center(mb),
                    40, 0.5D, 0.5D, 0.5D, 0.0D, null, false);
                mb.remove();
            } else if (BVec.of(ar.bds.getLocation()).distAbs(BVec.of(b)) == 0) {
                e.setCancelled(true);
                ar.hrtSt(false, (byte) (2 + Mobber.MobType.get(e.getEntityType()).pow << 1));
                mb.getWorld().spawnParticle(Particle.SOUL, EntityUtil.center(mb),
                    40, 0.5D, 0.5D, 0.5D, 0.0D, null, false);
                mb.remove();
            }
        } else if (bt == BlockType.TRIPWIRE) {
            final TripWire tw = Arena.tblks.get(BVec.of(b.getLocation()));
            if (tw == null) return;
            e.setCancelled(true);
            if (tw.tm != Team.CTs) return;
            tw.remove();
            ar.tws.remove(tw);
            tw.trigger(b.getLocation().add(0.5d, 0d, 0.5d));
        }
    }
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onStop(final PlayerStopUsingItemEvent e) {
		final ItemStack it = e.getItem();
        final Player p = e.getPlayer();
        if (!ItemUtil.is(it, ItemType.SPYGLASS)) return;
        final PlShooter sh = Shooter.getPlShooter(p.getName(), true);
        final ItemStack mh = p.getInventory().getItemInMainHand();
        if (!p.isSneaking() || sh.shtTm() != 0) return;
		final GunType gt = GunType.fast(mh);
		if (gt == null || !gt.snp) return;
        final boolean hd = Main.hasDur(mh);
        if ((sh.count() != 0 && !hd) || sh.cldwn() != 0) {
			if (mh.getAmount() == 1 && hd) return;
			Ostrov.sync(() -> {
				final GunType ngt = GunType.fast(p.getInventory().getItemInMainHand());
				Utils.spy(p, ngt != null && ngt.snp, p.isSneaking() && !Main.hasDur(mh));
			}, 2);
			return;
		}
        if (mh.getAmount() == 1) {
            if (hd) return;
            sh.count(0);
            Main.setDur(mh, 0);
        } else {
            if (hd) {
                Main.setDur(mh, Main.maxDur(mh));
                sh.count(0);
            }

            mh.setAmount(mh.getAmount() - 1);
        }
		sh.cldwn(gt.cld);
		final int tr = sh.count() < sh.recoil() ? sh.count() : sh.recoil();
        if (gt.brst == 0) sh.shoot(gt, false, tr, new IntHashMap<>());
		else {
			final IntHashMap<Info> info = new IntHashMap<>();
			for (int i = gt.brst; i != 0; i--) sh.shoot(gt, false, tr, info);
		}
		if (gt.cld > 2) p.setCooldown(mh, gt.cld - 1);
		Main.plyWrldSnd(p, gt.snd, 1.1f - Main.srnd.nextFloat() * 0.2f);
		Ostrov.sync(() -> {
			final GunType ngt = GunType.fast(p.getInventory().getItemInMainHand());
			Utils.spy(p, ngt != null && ngt.snp, p.isSneaking() && !Main.hasDur(mh));
		}, 2);
    }
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onInter(final PlayerInteractEvent e) {
		final ItemStack it = e.getItem();
		final Player pl = e.getPlayer();
		final NadeType nt = NadeType.getNdTp(it);
		final GunType gt = GunType.fast(it);
		final PlShooter sh;
		final Block b;
		switch (e.getAction()) {
		case LEFT_CLICK_AIR:
		case LEFT_CLICK_BLOCK:
			if (gt != null) {
				e.setCancelled(true);
				if (!Main.hasDur(it) && it.getAmount() != gt.amo) {
					sh = Shooter.getPlShooter(pl.getName(), true);
					Utils.spy(pl, gt.snp, pl.isSneaking());
					Main.setDur(it, 0);
					sh.shtTm(0);
					sh.count(0);
				}
			} else if (nt != null) {
				pl.setExp((pl.getExp() == 1f) ? 0f :
					(pl.getExp() + 0.5F > 1f ? 1f : pl.getExp() + 0.5F));
			}
			break;
		case RIGHT_CLICK_AIR:
		case RIGHT_CLICK_BLOCK:
            if (gt != null) {
				sh = Shooter.getPlShooter(pl.getName(), true);
				e.setUseInteractedBlock(Result.DENY);
				e.setUseItemInHand(Result.DENY);
				if (gt.snp && pl.isSneaking()) return;
				final boolean hd = Main.hasDur(it);
				if (sh.shtTm() == 0) {
					if ((sh.count() == 0 || hd) && sh.cldwn() == 0) {
						final boolean one = it.getAmount() == 1;
						if (one) {
							if (hd) return;
							Main.setDur(it, 0);
						} else {
							if (hd) {
								Main.setDur(it, Main.maxDur(it));
								sh.count(0);
							}
							
							it.setAmount(it.getAmount() - 1);
						}
						sh.cldwn(gt.cld);
						final int tr = sh.count() < sh.recoil() ? sh.count() : sh.recoil();
						final boolean scp = gt.snp && pl.isSneaking();
						if (gt.brst == 0) sh.shoot(gt, !scp, tr, new IntHashMap<>());
						else {
							final IntHashMap<Info> info = new IntHashMap<>();
							for (int i = gt.brst; i != 0; i--) sh.shoot(gt, !scp, tr, info);
						}
						if (one) sh.count(0);
						if (gt.cld > 2) pl.setCooldown(it, gt.cld - 1);
						Main.plyWrldSnd(pl, gt.snd, 1.1f - Main.srnd.nextFloat() * 0.2f);
					}
				}
				sh.shtTm(5);
			} else if (nt != null) {
				sh = Shooter.getPlShooter(pl.getName(), true);
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
					final Arena ar = sh.arena();
					if (ar != null) {
						final TripWire tw = Arena.tblks.get(BVec.of(b.getLocation()));
						if (tw != null) {
							e.setCancelled(true);
							if (tw.tm == ar.shtrs.get(sh)) {
								if (tw.chgNade(sh.getPlayer().getInventory().getItemInMainHand(), ar)) {
									it.setAmount(it.getAmount() - 1);
									pl.sendMessage(Main.prf() + "§7Вы зарядили растяжку гранатой!");
									pl.getWorld().playSound(b.getLocation(), Sound.ITEM_CROSSBOW_LOADING_END, 1f, 1f);
								} else Utils.sendAcBr(pl, "§c§lТакая граната уже привязана!");
								return;
							}
						}
					}
				}

				final float exp = pl.getExp();
				Nade.launch(pl, sh, pl.getEyeLocation().getDirection().multiply(exp + 0.4f),
					(int) (exp * 14f) + 4, pl.getInventory().getHeldItemSlot());
			} else {
				e.setUseInteractedBlock(pl.getGameMode() == GameMode.CREATIVE ? Result.ALLOW : Result.DENY);
				if (it != null) {
					b = e.getClickedBlock();
					switch (it.getType()) {
					case SPYGLASS:
						e.setUseItemInHand(pl.isSneaking() ? Result.ALLOW : Result.DENY);
						break;
					case GHAST_TEAR:
						sh = Shooter.getPlShooter(pl.getName(), true);
						if (sh.arena() == null) {
							pl.playSound(pl.getLocation(), Sound.BLOCK_BEEHIVE_SHEAR, 2f, 2f);
							pl.openInventory(Inventories.LBShop);
						} else {
							final Team tm = sh.arena().shtrs.get(sh);
							final Inventory inv;
							switch (sh.arena().gst) {
							case BEGINING:
							case WAITING:
								pl.playSound(pl.getLocation(), Sound.BLOCK_BEEHIVE_SHEAR, 2f, 2f);
								pl.openInventory(Inventories.LBShop);
								break;
							case BUYTIME:
								pl.playSound(pl.getLocation(), Sound.BLOCK_BEEHIVE_SHEAR, 2f, 2f);
								switch (tm) {
								case Ts:
									inv = Inventories.TShop;
									if (ItemUtil.isBlank(inv.getItem(4), false)) {
										Inventories.fillTsInv();
									}
									break;
								case CTs:
									inv = Inventories.CTShop;
									if (ItemUtil.isBlank(inv.getItem(4), false)) {
										Inventories.fillCTsInv();
									}
									break;
								case SPEC:
								default:
									inv = Inventories.LBShop;
									if (ItemUtil.isBlank(inv.getItem(4), false)) {
										Inventories.fillLbbInv();
									}
									break;
								}
								pl.openInventory(inv);
								break;
							case ROUND:
								if (sh.arena() instanceof Defusal && ((Defusal) sh.arena()).isBmbOn()) {
									Utils.sendAcBr(pl, "§c§lВремя закупки вышло, бомба постовлена!");
								} else if (sh.arena().canOpnShp(pl.getLocation(), tm)) {
									switch (tm) {
									case Ts:
										inv = Inventories.TShop;
										if (ItemUtil.isBlank(inv.getItem(4), false)) {
											Inventories.fillTsInv();
										}
										break;
									case CTs:
										inv = Inventories.CTShop;
										if (ItemUtil.isBlank(inv.getItem(4), false)) {
											Inventories.fillCTsInv();
										}
										break;
									case SPEC:
									default:
										inv = Inventories.LBShop;
										if (ItemUtil.isBlank(inv.getItem(4), false)) {
											Inventories.fillLbbInv();
										}
										break;
									}
									pl.openInventory(inv);
								} else {
									Utils.sendAcBr(pl, "§c§lЗакупатся можно только на спавне!");
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
					case GOLD_NUGGET:
					case SHEARS:
						final boolean kit = ItemUtil.is(it, ItemType.SHEARS);
						sh = Shooter.getPlShooter(pl.getName(), true);
                        if (b != null && sh.arena() != null && pl.getGameMode() == GameMode.SURVIVAL) {
                            e.setCancelled(true);
                            final Arena ar = sh.arena();
                            if (BlockType.CRIMSON_BUTTON.equals(b.getType().asBlockType())) {
                                if (ar instanceof final Defusal df) {
                                    final Bomb bmb = df.getBomb();
                                    if (bmb == null) break;
                                    if (bmb.defusing() == null) {
                                        bmb.defusing(sh);
                                        bmb.inv.open(pl, kit, true);
                                    } else {
                                        ScreenUtil.sendActionBarDirect(pl, "§c§lБомбу уже обезвреживают!");
                                    }
                                }
                            } else if (BlockType.SPAWNER.equals(b.getType().asBlockType())) {
                                if (ar instanceof final Invasion in && ar.gst == GameState.ROUND) {
                                    final Mobber mb = in.mbbrs.get(BVec.of(b).thin());
                                    if (mb == null || !mb.isAlive()) break;
                                    if (mb.defusing() == null) {
                                        mb.defusing(sh);
                                        mb.inv.open(pl, kit, true);
                                    } else {
                                        ScreenUtil.sendActionBarDirect(pl, "§c§lЭтот спавнер уже обезвреживают!");
                                    }
                                }
                            }
                        }
                        break;
                        case SUGAR:
						if (b != null) {
							sh = Shooter.getPlShooter(pl.getName(), true);
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
								if (crtTrpwr(b, e.getBlockFace(), pl)) {
									it.setAmount(0);
								}
								break;
							default:
								pl.sendMessage(Main.prf() + "§cРастяжку можно закреплять только на стены!");
								break;
							}
						}
						break;
					case CAMPFIRE:
						if (it.hasItemMeta() && TCUtil.strip(it.getItemMeta().displayName()).equals("Выбор Игры")) {
							Arena.gameInv.open(pl);
						}
						break;
					case TOTEM_OF_UNDYING:
						if (it.hasItemMeta() && TCUtil.strip(it.getItemMeta().displayName()).equals("Выбор Обшивки")) {
							ChosenSkinMenu.open(pl);
						}
						break;
					case NETHER_STAR:
						if (it.hasItemMeta() && TCUtil.strip(it.getItemMeta().displayName()).equals("Выбор Комманды")) {
							pl.playSound(pl.getLocation(), Sound.BLOCK_CONDUIT_ATTACK_TARGET, 1f, 2f);
							sh = Shooter.getPlShooter(pl.getName(), true);
							if (sh.arena() != null) {
								sh.arena().teamInv.open(pl);
							}
						}
						break;
					case EMERALD:
						if (MapManager.edits.containsKey(pl.getUniqueId())) {
							SmartInventory.builder().size(3, 9)
	                        .id("Map "+ pl.getName()).title("§d§lРедактор Карты")
	                        .provider(MapManager.edits.get(pl.getUniqueId()))
	                        .build().open(pl);
						}
						break;
					case HEART_OF_THE_SEA:
						if (it.hasItemMeta() && TCUtil.strip(it.getItemMeta().displayName()).equals("Боторейка")) {
							sh = Shooter.getPlShooter(pl.getName(), true);
							if (sh.arena() != null && sh.arena().botInv != null) {
								pl.playSound(pl.getLocation(), Sound.BLOCK_BREWING_STAND_BREW, 1f, 1.6f);
								sh.arena().botInv.open(pl);
							}
						}
						break;
					case MAGMA_CREAM:
						if (it.hasItemMeta() && TCUtil.strip(it.getItemMeta().displayName()).equals("Выход в Лобби")) {
							ApiOstrov.sendToServer(pl, "lobby1", "");
						}
						break;
					case SLIME_BALL:
						if (it.hasItemMeta() && TCUtil.strip(it.getItemMeta().displayName()).equals("Выход")) {
							final Arena ar = Shooter.getPlShooter(pl.getName(), true).arena();
							if (ar == null) {
								pl.sendMessage(Main.prf() + "§cВы не находитесь в игре!");
							} else {
								ar.rmvPl(Shooter.getPlShooter(pl.getName(), true));
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
            if (b == null) break;
            final BlockType bt = b.getType().asBlockType();
            if (bt == BlockType.TRIPWIRE) {
                sh = Shooter.getPlShooter(pl.getName(), true);
                final Arena ar = sh.arena();
                if (ar != null) {
                    final TripWire tw = Arena.tblks.get(BVec.of(b.getLocation()));
                    if (tw != null) {
                        e.setCancelled(true);
                        if (ar.name.equals(sh.arena().name) && ar.shtrs.get(sh) != tw.tm) {
                            tw.remove(); ar.tws.remove(tw);
                            tw.trigger(b.getLocation().add(0.5d, 0d, 0.5d));
                        }
                    }
                }
            } else if (bt == BlockType.WARPED_PRESSURE_PLATE || bt == BlockType.FARMLAND) {
                e.setCancelled(true);
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
				ChosenSkinMenu.tryCompleteQuest(sh, Quest.SHARD, ApiOstrov.getStat(p, Stat.CS_bomb));
            	e.setCancelled(false);
			} else {
            	Utils.sendAcBr(p, "§c§lУ тебя не дотягиваются руки!");
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
				Main.plnts.put(p, Position.block(p.getLocation().getBlockX(), p.getLocation().getBlockY(), p.getLocation().getBlockZ()));
				Utils.sendAcBr(p, "§d§lВыбирите место для установки бомбы...");
			} else {
            	Utils.sendAcBr(p, "§c§lБомбу можно ставить только на точках!");
			}
		}
	}
   
	private boolean canPlcBmb(final Location loc, final Defusal ar) {
		return (Math.abs(loc.getX() - ar.ast.x) < 3 && Math.abs(loc.getY() - ar.ast.y) < 3 && Math.abs(loc.getZ() - ar.ast.z) < 3) 
			|| 
			(Math.abs(loc.getX() - ar.bst.x) < 3 && Math.abs(loc.getY() - ar.bst.y) < 3 && Math.abs(loc.getZ() - ar.bst.z) < 3);
	}

	public boolean crtTrpwr(final Block b, final BlockFace bf, final Player p) {
		if (b.getType().isSolid()) {
			final int r = 5;
			final Block[] twbs = new Block[r];
			for (byte i = 0; true; i++) {
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
							new TripWire(twbs, sh, bf, i);
							p.getWorld().playSound(b.getLocation(), Sound.ITEM_CROSSBOW_QUICK_CHARGE_1, 2f, 1f);
							p.sendMessage(Main.prf() + "§7Растяжка поставлена, \n§7можете прицепить на нее гранату!");
							return true;
						}
					}
					p.sendMessage(Main.prf() + "§cРастяжку можно крепить только на целые блоки!");
					return false;
				}
			}
		}
		p.sendMessage(Main.prf() + "§cРастяжку можно крепить только на целые блоки!");
		return false;
	}
}