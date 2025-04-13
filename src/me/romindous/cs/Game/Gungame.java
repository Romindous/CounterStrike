package me.romindous.cs.Game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import me.romindous.cs.Enums.GameState;
import me.romindous.cs.Enums.GameType;
import me.romindous.cs.Enums.GunType;
import me.romindous.cs.Enums.NadeType;
import me.romindous.cs.Listeners.MainLis;
import me.romindous.cs.Main;
import me.romindous.cs.Menus.ChosenSkinMenu;
import me.romindous.cs.Objects.Game.BtShooter;
import me.romindous.cs.Objects.Game.PlShooter;
import me.romindous.cs.Objects.Game.TripWire;
import me.romindous.cs.Objects.Loc.Broken;
import me.romindous.cs.Objects.Shooter;
import me.romindous.cs.Objects.Skins.Quest;
import me.romindous.cs.Utils.Inventories;
import me.romindous.cs.Utils.Utils;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import ru.komiss77.ApiOstrov;
import ru.komiss77.enums.Stat;
import ru.komiss77.modules.items.ItemBuilder;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.world.BVec;
import ru.komiss77.utils.ClassUtil;
import ru.komiss77.utils.ItemUtil;
import ru.komiss77.utils.TCUtil;

public class Gungame extends Arena {

	private final GunType[] guns;
	private final byte cycle;
	
	public Gungame(final String name, final byte min, final byte max,
	    final BVec[] TSps, final BVec[] CTSps, final BVec[] spots,
	    final World w, final boolean rnd, final boolean bots) {
		super(name, min, max, TSps, CTSps, spots, w, rnd, bots);
		this.guns = new GunType[GunType.values().length + 1];
		final List<GunType> gts = Arrays.asList(GunType.values());
		Collections.shuffle(gts);
		int i = 0;
		for (final GunType gt : gts) {
			guns[i] = gt;
			i++;
		}
		guns[GunType.values().length] = null;
		this.cycle = (byte) guns.length;
		this.gst = GameState.WAITING;
		updateData();
	}

	@Override
	public boolean addPl(final PlShooter sh) {
		sh.kills0();
		sh.spwnrs0();
		sh.deaths0();
		sh.money(0);
		sh.arena(this);
		final Player p = sh.getPlayer();
		Main.nrmlzPl(p, true);
		switch (gst) {
			case WAITING:
				chngTeam(sh, Team.SPEC);
				sh.item(2, new ItemBuilder(ItemType.NETHER_STAR).name("§eВыбор Комманды").build());
				sh.item(5, new ItemBuilder(ItemType.HEART_OF_THE_SEA).name("§чБоторейка").build());
				sh.item(6, Main.mkItm(ItemType.GHAST_TEAR, "§5Магазин", Shooter.SHOP_MDL));
				sh.item(8, new ItemBuilder(ItemType.SLIME_BALL).name("§cВыход").build());
				if (shtrs.size() == min) {
					for (final Entry<Shooter, Team> en : shtrs.entrySet()) {
						final Player pl = en.getKey().getPlayer();
						if (pl != null) {
							beginScore(pl, en.getValue());
							pl.sendMessage(Main.prf() + "§7Игрок §5" + sh.name() + " §7зашел на карту!");
							Utils.sendAcBr(pl, "§7Игроков: §5" + shtrs.size() + " §7из §5" + max + " §7(макс)!");
							if (!rnd) {
								pl.teleport(ClassUtil.rndElmt(spots).center(w));
							}
							pl.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 600, 1));
						}
					}
					cntBeg();
				} else {
					final int rm = min - shtrs.size();
					waitScore(p, rm);
					chngTeam(sh, Team.SPEC);
					for (final Shooter s : shtrs.keySet()) {
						final Player pl = s.getPlayer();
						if (pl != null) {
							pl.sendMessage(Main.prf() + "§7Игрок §5" + sh.name() + " §7зашел на карту!");
							Utils.sendAcBr(pl, "§7Нужно еще §d" + rm + " §7игроков для начала!");
							PM.getOplayer(pl).score.getSideBar().update(LIMIT, "§7Ждем еще §5" + rm + (rm > 1 ? " §7игроков" : " §7игрокa"));
						}
					}
				}
				break;
			case BEGINING:
				sh.item(2, new ItemBuilder(ItemType.NETHER_STAR).name("§eВыбор Комманды").build());
				sh.item(5, new ItemBuilder(ItemType.HEART_OF_THE_SEA).name("§чБоторейка").build());
				sh.item(6, Main.mkItm(ItemType.GHAST_TEAR, "§5Магазин", Shooter.SHOP_MDL));
				sh.item(8, new ItemBuilder(ItemType.SLIME_BALL).name("§cВыход").build());
				if (!rnd) {
					p.teleport(ClassUtil.rndElmt(spots).center(w));
				}
				p.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, time * 20, 1));
				beginScore(p, Team.SPEC);
				chngTeam(sh, Team.SPEC);
				for (final Entry<Shooter, Team> e : shtrs.entrySet()) {
					final Player pl = e.getKey().getPlayer();
					if (pl != null) {
						pl.sendMessage(Main.prf() + "§7Игрок §5" + sh.name() + " §7зашел на карту!");
						Utils.sendAcBr(pl, "§7Игроков: §5" + shtrs.size() + " §7из §5" + max + " §7(макс)!");
					}
				}
				break;
			case BUYTIME:
			case ROUND:
				if (!shtrs.containsKey(sh)) {//spec
					gameScore(sh, p);
					chngTeam(sh, Team.SPEC);
					p.setGameMode(GameMode.SPECTATOR);
					p.teleport(Main.getNrLoc(w, Main.srnd.nextBoolean() ? ClassUtil.rndElmt(TSpawns) : ClassUtil.rndElmt(CTSpawns)));
					sh.item(2, new ItemBuilder(ItemType.NETHER_STAR).name("§eВыбор Комманды").build());
					sh.item(8, new ItemBuilder(ItemType.SLIME_BALL).name("§cВыход").build());
					for (final Shooter s : shtrs.keySet()) {
						final Player pl = s.getPlayer();
						if (pl != null) {
							pl.sendMessage(Main.prf() + "§7Игрок §5" + sh.name() + " §7зашел посмотреть!");
						}
					}
					break;
				}
				addToTm(p, sh, getMinTm());
			case ENDRND:
				break;
			case FINISH:
				p.sendMessage(Main.prf() + "§cЭта игра уже заканчивается!");
				Main.lobbyPl(p);
				return false;
		}
		updateData();
		final Component tpl = TCUtil.form("§7Сейчас в игре: §d" + MainLis.getPlaying() + "§7 человек!");
		for (final Player pl : Bukkit.getOnlinePlayers()) {
			pl.sendPlayerListFooter(tpl);
		}
		return true;
	}

	public void addToTm(final Player p, final PlShooter sh, final Team tm) {
		p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
		p.teleport(ClassUtil.rndElmt(spots).center(w));
		final PlayerInventory pinv = p.getInventory();
		p.setGameMode(GameMode.SURVIVAL);
		pinv.setItem(2, Main.mkItm(ItemType.BLAZE_ROD, "§fНож \u9298", Shooter.KNIFE_MDL));
		if (tm == Team.Ts) {
			pinv.setHelmet(Inventories.TShop.getItem(Shooter.helmSlt).clone());
			pinv.setChestplate(Inventories.TShop.getItem(Shooter.chestSlt).clone());
		} else {
			pinv.setHelmet(Inventories.CTShop.getItem(Shooter.helmSlt).clone());
			pinv.setChestplate(Inventories.CTShop.getItem(Shooter.chestSlt).clone());
		}
		p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 240, 240, true, false, false));
		p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 40, 2, true, false, false));

		gameScore(sh, p);
		chngTeam(sh, tm);
		for (final Shooter s : shtrs.keySet()) {
			final Player pl = s.getPlayer();
			if (pl != null) {
				pl.sendMessage(Main.prf() + "§7Игрок §5" + sh.name() + " §7зашел играть за комманду " + tm.icn + "§7!");
			}
		}

		for (final TripWire tw : tws) {
			if (tm == tw.tm) tw.showNade(p);
		}
	}

	@Override
	public boolean rmvPl(final PlShooter sh) {
		sh.kills0();
		sh.spwnrs0();
		sh.deaths0();
		sh.money(0);
		final Team tm = shtrs.remove(sh);
		final Player p = sh.getPlayer();
		if (tm == null) {
			p.sendMessage(Main.prf() + "§cВы не находитесь в игре!");
			return false;
		}
		switch (gst) {
			case WAITING:
				if (shtrs.size() > 0) {
					final int rm = min - shtrs.size();
					for (final Entry<Shooter, Team> e : shtrs.entrySet()) {
						final Player pl = e.getKey().getPlayer();
						if (pl != null) {
							pl.sendMessage(Main.prf() + "§7Игрок §5" + sh.name() + " §7вышел с карты!");
							Utils.sendAcBr(pl, "§7Нужно еще §d" + rm + " §7игроков для начала!");
							final Scoreboard sb = pl.getScoreboard();
							PM.getOplayer(p).score.getSideBar()
									.update(T_AMT, Team.Ts.icn + " §7: " + getTmAmt(Team.Ts, true, true)
											+ (e.getValue() == Team.Ts ? " §7чел. §8✦ Ты" : " §7чел."))
									.update(CT_AMT, Team.CTs.icn + " §7: " + getTmAmt(Team.CTs, true, true)
											+ (e.getValue() == Team.CTs ? " §7чел. §8✦ Ты" : " §7чел."))
									.update(LIMIT, "§7Ждем еще §5" + rm + (rm > 1 ? " §7игроков" : " §7игрокa"));
						}
					}
				} else {
					this.end();
				}
				break;
			case BEGINING:
				if (shtrs.size() < min) {
					gst = GameState.WAITING;
					if (tsk != null) {
						tsk.cancel();
					}
					for (final Shooter s : shtrs.keySet()) {
						final Player pl = s.getPlayer();
						if (pl == null) {

						} else {
							waitScore(pl, min - 1);
							pl.teleport(Main.getNrLoc(Main.lobby()));
							pl.sendMessage(Main.prf() + "§7Игрок §5" + sh.name() + " §7вышел с карты,\n§7Слишком мало игроков для начала!");
							Utils.sendAcBr(pl, "§7Игроков: §5" + shtrs.size() + " §7из §5" + max + " §7(макс)!");
							pl.removePotionEffect(PotionEffectType.GLOWING);
						}
					}
				} else {
					for (final Entry<Shooter, Team> e : shtrs.entrySet()) {
						final Player pl = e.getKey().getPlayer();
						if (pl != null) {
							pl.sendMessage(Main.prf() + "§7Игрок §5" + sh.name() + " §7вышел с карты!");
							Utils.sendAcBr(pl, "§7Игроков: §5" + shtrs.size() + " §7из §5" + max + " §7(макс)!");
							PM.getOplayer(p).score.getSideBar()
									.update(T_AMT, Team.Ts.icn + " §7: " + getTmAmt(Team.Ts, true, true)
											+ (e.getValue() == Team.Ts ? " §7чел. §8✦ Ты" : " §7чел."))
									.update(CT_AMT, Team.CTs.icn + " §7: " + getTmAmt(Team.CTs, true, true)
											+ (e.getValue() == Team.CTs ? " §7чел. §8✦ Ты" : " §7чел."));
						}
					}
				}
				break;
			case BUYTIME:
			case ROUND:
				if (tm == Team.SPEC) {
					for (final Shooter s : shtrs.keySet()) {
						final Player pl = s.getPlayer();
						if (pl != null) {
							pl.sendMessage(Main.prf() + "§7Игрок §5" + sh.name() + " §7вышел из просмотра!");
						}
					}
					break;
				}

				if (getTmAmt(Team.CTs, false, true) == 0 || getTmAmt(Team.Ts, false, true) == 0) {
					if (tsk != null) {
						tsk.cancel();
					}
					for (final Shooter s : shtrs.keySet()) {
						final Player pl = s.getPlayer();
						if (pl != null) {
							winScore(s, pl, tm == Team.Ts);
							pl.sendMessage(Main.prf() + "§7Игрок §5" + sh.name() + " §7вышел из игры!");
						}
					}
					cntFnsh(tm.getOpst());
				} else {
					for (final Entry<Shooter, Team> e : shtrs.entrySet()) {
						final Player pl = e.getKey().getPlayer();
						if (pl != null) {
							pl.sendMessage(Main.prf() + "§7Игрок §5" + sh.name() + " §7вышел из игры!");
							PM.getOplayer(pl).score.getSideBar()
									.update(T_AMT, Team.Ts.icn + " §7: " + getTmAmt(Team.Ts, true, true)
											+ (e.getValue() == Team.Ts ? " §7чел. §8✦ Ты" : " §7чел."))
									.update(CT_AMT, Team.CTs.icn + " §7: " + getTmAmt(Team.CTs, true, true)
											+ (e.getValue() == Team.CTs ? " §7чел. §8✦ Ты" : " §7чел."));
						}
					}
				}
			case ENDRND:
				break;
			case FINISH:
				if (shtrs.size() == 0) {
					this.time = 1;
				}
				break;
		}
		updateData();
		Main.lobbyPl(p);
		return true;
	}

	//счетчик
	public void cntBeg() {
		time = 30;
		gst = GameState.BEGINING;
		final Arena ar = this;
		updateData();
		tsk = new BukkitRunnable() {
			@Override
			public void run() {
				final String rtm = "§7Начало через: §5" + String.valueOf(time);
				switch (time) {
				case 10, 5, 4:
					for (final Shooter sh : shtrs.keySet()) {
						final Player p = sh.getPlayer();
						if (p != null) {
							p.playSound(p.getLocation(), Sound.BLOCK_DISPENSER_FAIL, 0.8f, 1.2f);
							Utils.sendSbTtl(p, "§5§l" + time, 10);
							PM.getOplayer(p).score.getSideBar().update(LIMIT, rtm);
						}
					}
					break;
				case 3, 2, 1:
					for (final Shooter sh : shtrs.keySet()) {
						final Player p = sh.getPlayer();
						if (p != null) {
							p.playSound(p.getLocation(), Sound.BLOCK_DISPENSER_FAIL, 0.8f, 1.2f);
							Utils.sendSbTtl(p, "§d§l" + time, 10);
							PM.getOplayer(p).score.getSideBar().update(LIMIT, rtm);
						}
					}
					break;
				case 0:
					if (tsk != null) {
						tsk.cancel();
					}
					cntPrep();
					if (rnd) Main.mapBlds.get(name).placeSets(ar, 5);
					break;
				default:
					for (final Shooter sh : shtrs.keySet()) {
						final Player p = sh.getPlayer();
						if (p != null) {
							PM.getOplayer(p).score.getSideBar().update(LIMIT, rtm);
						}
					}
					break;
				}
				time--;
			}
		}.runTaskTimer(Main.plug, 20, 20);
	}

	public void cntPrep() {
		time = 10;
		gst = GameState.BUYTIME;
		for (final Entity e : w.getEntitiesByClasses(Item.class, ArmorStand.class, Turtle.class)) {
			e.remove();
		}
		updateData();
		final ArrayList<Shooter> sts = new ArrayList<>();
		for (final Entry<Shooter, Team> e : shtrs.entrySet()) {
			if (e.getValue() == Team.SPEC) sts.add(e.getKey());
		}
		for (final Shooter s : sts) chngTeam(s, getMinTm());
		blncTms();
		for (final Entry<Shooter, Team> e : shtrs.entrySet()) {
			final Shooter sh = e.getKey();
			final Player p = sh.getPlayer();
			if (p != null) {
				Main.nrmlzPl(p, true);
				gameScore(sh, p);
				p.setGameMode(GameMode.SURVIVAL);
				Utils.sendTtlSbTtl(p, "", "§l§eПодготовка Оружейни...", 30);
			}
			
			sh.teleport(sh.getEntity(), ClassUtil.rndElmt(spots).center(w));
			
			updateWeapon(sh);
			sh.taq(e.getValue().icn + " ", " §7[" + sh.kills() + "-" + sh.deaths() + "]", e.getValue().clr);
			sh.getEntity().addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS,
				240, 240, true, false, false));
			sh.getEntity().addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING,
				40, 2, true, false, false));
		}
		
		tsk = new BukkitRunnable() {
			@Override
			public void run() {
				final String rtm = getTime(time, "§5") + " §7до конца!";
//				for (final LivingEntity le : w.getLivingEntities()) {
//					le.setFireTicks(-1);
//				}
				switch (time) {
				case 3, 2, 1:
					for (final Shooter sh : shtrs.keySet()) {
						final Player p = sh.getPlayer();
						if (p != null) {
							p.playSound(p.getLocation(), Sound.BLOCK_DISPENSER_FAIL, 0.8f, 1.2f);
							Utils.sendSbTtl(p, "§d§l" + time, 10);
							PM.getOplayer(p).score.getSideBar().update(LIMIT, rtm);
						}
					}
					break;
				case 0:
					if (tsk != null) tsk.cancel();
					
					cntRnd();
					break;
				default:
					for (final Shooter sh : shtrs.keySet()) {
						final Player p = sh.getPlayer();
						if (p != null) {
							PM.getOplayer(p).score.getSideBar().update(LIMIT, rtm);
						}
					}
					break;
				}
				time--;
			}
		}.runTaskTimer(Main.plug, 20, 20);
	}

	private void cntRnd() {
		time = 360;
		gst = GameState.ROUND;
		updateData();

		for (final Entry<Shooter, Team> e : shtrs.entrySet()) {
			final LivingEntity le = e.getKey().getEntity();
			if (le == null) continue;
			le.removePotionEffect(PotionEffectType.SLOWNESS);
			if (le instanceof final Player p) {
				Utils.sendSbTtl(p, e.getValue().icn.substring(0, 2) + "§lВперед", 30);
				PM.getOplayer(p).score.getSideBar().update(STAGE, "§7Cтадия: §5Бой");
				p.playSound(p, "info." + e.getValue().goSnd, 10f, 1f);
			}
		}
		tsk = new BukkitRunnable() {
			@Override
			public void run() {
				final String rtm = getTime(time, "§d") + " §7до конца!";
				switch (time) {
				case 60:
					for (final Shooter sh : shtrs.keySet()) {
						final Player p = sh.getPlayer();
						if (p != null) {
							Utils.sendAcBr(p, "§7Осталась §d1 §7минута!");
							PM.getOplayer(p).score.getSideBar().update(LIMIT, rtm);
						}
					}
					break;
				case 30:
					for (final Entry<Shooter, Team> e : shtrs.entrySet()) {
						final Player p = e.getKey().getPlayer();
						if (p != null) {
							Utils.sendAcBr(p, "§7Осталось §d" + time + " §7секунд!");
							p.playSound(p.getLocation(), "info." + (e.getValue() == Team.Ts ? "t30sec" : "ct30sec"), 10f, 1f);
							PM.getOplayer(p).score.getSideBar().update(LIMIT, rtm);
						}
					}
					break;
				case 10:
					for (final Shooter sh : shtrs.keySet()) {
						final Player p = sh.getPlayer();
						if (p != null) {
							Utils.sendAcBr(p, "§7Осталось §d" + time + " §7секунд!");
							PM.getOplayer(p).score.getSideBar().update(LIMIT, rtm);
						}
					}
					break;
				case 0:
					final int tp = getTmProg(Team.Ts);
					final int ctp = getTmProg(Team.CTs);
					if (tp == ctp) cntFnsh(Main.srnd.nextBoolean() ? Team.CTs : Team.Ts);
					else cntFnsh(tp > ctp ? Team.Ts : Team.CTs);
					break;
				default:
					for (final Shooter sh : shtrs.keySet()) {
						final Player p = sh.getPlayer();
						if (p != null) {
							PM.getOplayer(p).score.getSideBar().update(LIMIT, rtm);
						}
					}
					break;
				}
				time--;
			}
		}.runTaskTimer(Main.plug, 20, 20);
	}

	private void cntFnsh(final Team wn) {
		if (tsk != null) tsk.cancel();
		time = 10;
		gst = GameState.FINISH;
		for (final Entity e : w.getEntitiesByClasses(Item.class, ArmorStand.class, Turtle.class)) {
			e.remove();
		}
		updateData();

		for (final Broken bb : brkn) {
			bb.block(w).setBlockData(bb.bd, false);
		}
		brkn.clear();

		for (final TripWire tw : tws) {
			tw.remove();
		}
		tws.clear();
		
		Shooter bt = null;
		Shooter bct = null;
		for (final Entry<Shooter, Team> e : shtrs.entrySet()) {
			switch (e.getValue()) {
			case Ts:
				if (bt == null || bt.kills() < e.getKey().kills()) {
					bt = e.getKey();
				}
				break;
			case CTs:
				if (bct == null || bct.kills() < e.getKey().kills()) {
					bct = e.getKey();
				}
				break;
			case SPEC:
				break;
			}
		}
		
		final String st;
		final String msg;
        st = switch (wn) {
            case Ts -> {
                msg = " \n§7Победа в раунде: §4Террористы\n \n§5=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-\n§7Лучший спецназовец: §3" +
                        (bct == null ? "Никто" : bct.name()) + "\n          §7-=x=-\n§7Самый злобный террорист: §4" +
                        (bt == null ? "Никто" : bt.name()) + "\n§5=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-";
                yield "§4§lТеррористы §7выйграли!";
            }
            case CTs -> {
                msg = " \n§7Победа в раунде: §3Спецназ\n \n§5=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-\n§7Лучший спецназовец: §3" +
                        (bct == null ? "Никто" : bct.name()) + "\n          §7-=x=-\n§7Самый злобный террорист: §4" +
                        (bt == null ? "Никто" : bt.name()) + "\n§5=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-";
                yield "§3§lСпецназ §7одержал победу!";
            }
            default -> {
                msg = " ";
                yield " ";
            }
        };
		
		for (final Entry<Shooter, Team> e : shtrs.entrySet()) {
			final Shooter sh = e.getKey();
			if (sh instanceof PlShooter) {
				final Player p = sh.getPlayer();
				p.closeInventory();
				PM.getOplayer(p).score.getSideBar().update(LIMIT, "§d00:00 §7до конца!");
				p.playSound(p, "info." + e.getValue().finSnd, 10f, 1f);
				p.sendMessage(msg);
				Utils.sendTtlSbTtl(p, "§5Финиш", st, 40);
				sh.taq("§7<§d" + name + "§7> ", " §7[" + sh.kills() + "-" + sh.deaths() + "]", Team.SPEC.clr);
				winScore(sh, p, wn == Team.CTs);
				if (e.getValue() == Team.SPEC) continue;
				
				if (e.getValue() == wn) {
					ApiOstrov.addStat(p, Stat.CS_win);
					ChosenSkinMenu.tryCompleteQuest(e.getKey(), Quest.ГРУЗЧИК, ApiOstrov.getStat(p, Stat.CS_win));
				} else {
					ApiOstrov.addStat(p, Stat.CS_loose);
				}
				ApiOstrov.addStat(p, Stat.CS_game);
				ChosenSkinMenu.tryCompleteQuest(sh, Quest.ДУША, ApiOstrov.getStat(p, Stat.CS_game));
				sh.clearInv();
			} else {
				((BtShooter) sh).own().remove();
			}
		}

		tsk = new BukkitRunnable() {
			@Override
			public void run() {
				final String rtm = getTime(time, "§5") + " §7до конца!";
				if (time == 0) {
					if (tsk != null) {
						tsk.cancel();
					}

					end();
				} else {
					final FireworkEffect fe = switch (wn) {
						case Ts -> FireworkEffect.builder().with(Type.CREEPER)
							.withColor(Color.MAROON).withFlicker().build();
						case CTs -> FireworkEffect.builder().with(Type.STAR)
							.withColor(Color.TEAL).withFlicker().build();
						case SPEC -> FireworkEffect.builder().with(Type.BURST)
							.withColor(Color.GRAY).withFlicker().build();
                    };
                    for (final Entry<Shooter, Team> e : shtrs.entrySet()) {
						final Player p = e.getKey().getPlayer();
						if (p != null) {
							PM.getOplayer(p).score.getSideBar().update(LIMIT, rtm);
							if (e.getValue() == wn) {
								final Firework fw = (Firework) p.getWorld().spawnEntity(p.getEyeLocation(), EntityType.FIREWORK_ROCKET);
								final FireworkMeta fm = fw.getFireworkMeta();
								fm.addEffect(fe);
								fw.setFireworkMeta(fm);
							}
						}
					}
				}
				time--;
			}
		}.runTaskTimer(Main.plug, 20, 20);
	}

	private void waitScore(final Player p, final int rm) {
		PM.getOplayer(p).score.getSideBar().reset().title("§7[§5CS:GO§7]")
			.add(" ")
			.add("§7Карта: §5" + name)
			.add("§7Режим: §d" + getType().name)
			.add("§7=-=-=-=-=-=-=-=-")
			.add("§7Комманды:")
			.add(T_AMT, Team.Ts.icn + " §7: " + getTmAmt(Team.Ts, true, true) + " §7чел.")
			.add(" ")
			.add(CT_AMT, Team.CTs.icn + " §7: " + getTmAmt(Team.CTs, true, true) + " §7чел.")
			.add("§7=-=-=-=-=-=-=-=-")
			.add(LIMIT, "§7Ждем еще §5" + rm + (rm > 1 ? " §7игроков" : " §7игрокa"))
			.add(" ")
			.add("§e   ostrov77.ru").build();
	}

	private void beginScore(final Player p, final Team tm) {
		PM.getOplayer(p).score.getSideBar().reset().title("§7[§5CS:GO§7]")
			.add(" ")
			.add("§7Карта: §5" + name)
			.add("§7Режим: §d" + getType().name)
			.add("§7=-=-=-=-=-=-=-")
			.add("§7Комманды:")
			.add(T_AMT, Team.Ts.icn + " §7: " + getTmAmt(Team.Ts, true, true)
				+ (tm == Team.Ts ? " §7чел. §8✦ Ты" : " §7чел."))
			.add(" ")
			.add(CT_AMT, Team.CTs.icn + " §7: " + getTmAmt(Team.CTs, true, true)
				+ (tm == Team.CTs ? " §7чел. §8✦ Ты" : " §7чел."))
			.add("§7=-=-=-=-=-=-=-")
			.add(LIMIT, "§7Начало через: §5" + String.valueOf(time))
			.add(" ")
			.add("§e   ostrov77.ru").build();
	}
	
	private void gameScore(final Shooter sh, final Player p) {
		final Team tm = shtrs.get(sh);
		PM.getOplayer(p).score.getSideBar().reset().title("§7[§5CS:GO§7]")
			.add(SCORE, Team.Ts.icn + " §7: " + Team.Ts.clr + getTmProg(Team.Ts) + "§7/" + Team.Ts.clr + cycle
				+ " §7-=x=- " + Team.CTs.clr + getTmProg(Team.CTs) + "§7/" + Team.CTs.clr + cycle + " §7: " + Team.CTs.icn)
			.add(" ")
			.add("§7Карта: §5" + name)
			.add("§7Режим: §d" + getType().name)
			.add("§7=-=-=-=-=-=-=-")
			.add(STAGE, "§7Cтадия: " + (gst == GameState.BUYTIME ? "§5Подготовка" : "§5Бой"))
			.add(LIMIT, getTime(time, "§d") + " §7до конца!")
			.add(T_AMT, Team.Ts.icn + " §7: " + getTmAmt(Team.Ts, true, true)
					+ (tm == Team.Ts ? " §7чел. §8✦ Ты" : " §7чел."))
			.add(" ")
			.add(CT_AMT, Team.CTs.icn + " §7: " + getTmAmt(Team.CTs, true, true)
					+ (tm == Team.CTs ? " §7чел. §8✦ Ты" : " §7чел."))
			.add("§7=-=-=-=-=-=-=-")
			.add(MONEY, "§7Прогрес: §d" + sh.money() + "§7/§d" + String.valueOf(cycle))
			.add(" ")
			.add("§e   ostrov77.ru").build();
	}

	private int getTmProg(final Team tm) {
		int stage = 0;
		for (final Entry<Shooter, Team> e : shtrs.entrySet()) {
			if (e.getValue() == tm) {
				stage = Math.max(stage, e.getKey().money());
			}
		}
		return stage;
	}

	private void winScore(final Shooter sh, final Player p, final boolean isCTWin) {
		final Team tm = shtrs.get(sh);
		PM.getOplayer(p).score.getSideBar().reset().title("§7[§5CS:GO§7]")
			.add(SCORE, isCTWin ? Team.Ts.icn + " §7: " + Team.Ts.clr + getTmProg(Team.Ts) + "§7/" + Team.Ts.clr + cycle
				+ " §7-=x=- " + Team.CTs.clr + cycle + "§7/" + Team.CTs.clr + cycle + " §7: " + Team.CTs.icn
				: Team.Ts.icn + " §7: " + Team.Ts.clr + cycle + "§7/" + Team.Ts.clr + cycle + " §7-=x=- "
				+ Team.CTs.clr + getTmProg(Team.CTs) + "§7/" + Team.CTs.clr + cycle + " §7: " + Team.CTs.icn)
			.add("§7Карта: §5" + name)
			.add("§7Режим: §d" + getType().name)
			.add("§7=-=-=-=-=-=-=-")
			.add("§7Cтадия: §dФиниш")
			.add(LIMIT, getTime(time, "§d") + " §7до конца!")
			.add(T_AMT, Team.Ts.icn + " §7: " + getTmAmt(Team.Ts, true, true)
				+ (tm == Team.Ts ? " §7чел. §8✦ Ты" : " §7чел."))
			.add(" ")
			.add(CT_AMT, Team.CTs.icn + " §7: " + getTmAmt(Team.CTs, true, true)
				+ (tm == Team.CTs ? " §7чел. §8✦ Ты" : " §7чел."))
			.add("§7=-=-=-=-=-=-=-")
			.add(MONEY, "§7Прогрес: §d" + sh.money() + "§7/§d" + String.valueOf(cycle))
			.add(" ")
			.add("§e   ostrov77.ru").build();
	}

	@Override
	public void killSh(final Shooter sh) {
		//Bukkit.broadcast(Component.text("ent-" + sh.getEntity().getEntityId() + ", sh-" + sh.name()));
		final LivingEntity le = sh.getEntity();
		if (gst == GameState.ROUND) {
			sh.drop(le.getLocation());
			addDth(sh);
			if (sh instanceof BtShooter) {
				((BtShooter) sh).own().hide(le);
			}
		} else if (sh instanceof PlShooter) {
			if (le.hasPotionEffect(PotionEffectType.GLOWING)) {
				Main.nrmlzPl((Player) le, false);
				le.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, time * 20, 1));
			} else {
				Main.nrmlzPl((Player) le, false);
			}
		}
		if (gst != GameState.FINISH) {
			final Location loc = rnd && gst != GameState.ROUND ?
				Main.lobby() : Main.getNrLoc(w, ClassUtil.rndElmt(spots));
			loc.getWorld().spawnParticle(Particle.PORTAL, loc, 200, 0.2D, 0.4D, 0.2D, 0.4D, null, false);
			sh.teleport(le, loc);
		}
	}
	
	private void updateWeapon(final Shooter sh) {
		if (sh.money() == guns.length) {
			cntFnsh(shtrs.get(sh));
			return;
		}
		
		sh.item(0, ItemUtil.air.clone()); sh.item(1, ItemUtil.air.clone());
		sh.item(2, Main.mkItm(ItemType.BLAZE_ROD, "§fНож \u9298", Shooter.KNIFE_MDL));
		sh.item(3, ItemUtil.air.clone()); sh.item(4, ItemUtil.air.clone());
		
		switch (shtrs.get(sh)) {
		case Ts:
			sh.item(EquipmentSlot.HEAD, Inventories.TShop.getItem(Shooter.helmSlt).clone());
			sh.item(EquipmentSlot.CHEST, Inventories.TShop.getItem(Shooter.chestSlt).clone());
			break;
		case CTs:
			sh.item(EquipmentSlot.HEAD, Inventories.CTShop.getItem(Shooter.helmSlt).clone());
			sh.item(EquipmentSlot.CHEST, Inventories.CTShop.getItem(Shooter.chestSlt).clone());
			break;
		default:
			break;
		}

		final GunType gt = guns[sh.money()];
		if (gt == null) {
			final Player p = sh.getPlayer();
			if (p != null) {
				Utils.sendTtlSbTtl(p, "", "§7Последний §dцыкл §7оружия!", 20);
			}
		} else {
			final Quest shq = Quest.get(gt, sh.model(gt));
			final NadeType nt = nadeFromPts(sh.money());
			sh.item(2, Main.mkItm(ItemType.BLAZE_ROD, "§fНож \u9298", Shooter.KNIFE_MDL));
			sh.item(gt.prm ? 0 : 1, gt.item().name((shq == null ? "§5" + gt.name() :
				"§5" + gt.name() + " '" + Main.nrmlzStr(shq.name()) + "'") + " " + gt.icn)
			.amount(gt.amo).maxDamage(gt.rtm).model(gt.skin(shq == null ? GunType.DEF_MDL : shq.model)).build());
			sh.item(nt.prm ? NadeType.prmSlot : NadeType.scdSlot, Inventories.CTShop.getItem(nt.slt).clone());
		}
		
		final LivingEntity le = sh.getEntity();
		le.setHealth(le.getAttribute(Attribute.MAX_HEALTH).getBaseValue());
		if (sh instanceof PlShooter) {
			final Player p = (Player) le;
			PM.getOplayer(p).score.getSideBar().update(SCORE,
				Team.Ts.icn + " §7: " + Team.Ts.clr + getTmProg(Team.Ts) + "§7/" + Team.Ts.clr + cycle
					+ " §7-=x=- " + Team.CTs.clr + getTmProg(Team.CTs) + "§7/" + Team.CTs.clr + cycle + " §7: " + Team.CTs.icn)
				.update(MONEY, "§7Прогрес: §d" + sh.money() + "§7/§d" + String.valueOf(cycle));
			p.setNoDamageTicks(10);
		} else {
			((BtShooter) sh).switchToGun();
		}
	}

	private NadeType nadeFromPts(final int pts) {
		final float cof = 10f / cycle;
        return switch (Main.srnd.nextInt((int) (cof * pts) + 1)) {
            case 7, 6, 5 -> NadeType.FLAME;
            case 4, 3 -> NadeType.SMOKE;
            case 2, 1 -> NadeType.FLASH;
            case 0 -> NadeType.DECOY;
            default -> NadeType.FRAG;
        };
	}

	@Override
	public void addKll(final Shooter sh) {
		sh.killsI();
		sh.money(sh.money() + 1);
		sh.taq(shtrs.get(sh).icn + " ", " §7[" + sh.kills() + "-" + sh.deaths() + "]", shtrs.get(sh).clr);
		updateWeapon(sh);
		final Player p = sh.getPlayer();
		if (p != null) {
			ApiOstrov.addStat(p, Stat.CS_kill);
			ChosenSkinMenu.tryCompleteQuest(sh, Quest.ЗЕМЛЯ, sh.kills() / Math.max(sh.deaths(), 1));
			ChosenSkinMenu.tryCompleteQuest(sh, Quest.ТОКСИК, sh.kills());
		}
	}

	@Override
	public void addDth(final Shooter sh) {
		sh.deathsI();
		sh.money(Math.max(sh.money() - 1, 0));
		sh.taq(shtrs.get(sh).icn + " ", " §7[" + sh.kills() + "-" + sh.deaths() + "]", shtrs.get(sh).clr);
		updateWeapon(sh);
		final Player p = sh.getPlayer();
		if (p != null) {
			ApiOstrov.addStat(p, Stat.CS_death);
		}
	}
	
	public int getTmAmt(final Team tm, final boolean bots, final boolean alv) {
		int n = 0;
		for (final Entry<Shooter, Team> e : shtrs.entrySet()) {
			if (e.getValue() == tm && (e.getKey() instanceof PlShooter || bots)) {
				n++;
			}
		}
		return n;
	}
	
	@Override
	public GameType getType() {
		return GameType.GUNGAME;
	}
}
