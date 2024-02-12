package me.Romindous.CounterStrike.Game;

import io.papermc.paper.math.Position;
import me.Romindous.CounterStrike.Enums.GameState;
import me.Romindous.CounterStrike.Enums.GameType;
import me.Romindous.CounterStrike.Listeners.MainLis;
import me.Romindous.CounterStrike.Main;
import me.Romindous.CounterStrike.Objects.Game.BtShooter;
import me.Romindous.CounterStrike.Objects.Game.PlShooter;
import me.Romindous.CounterStrike.Objects.Game.TripWire;
import me.Romindous.CounterStrike.Objects.Loc.BrknBlck;
import me.Romindous.CounterStrike.Objects.Mobs.Mobber;
import me.Romindous.CounterStrike.Objects.Shooter;
import me.Romindous.CounterStrike.Objects.Skins.Quest;
import me.Romindous.CounterStrike.Objects.Skins.SkinQuest;
import me.Romindous.CounterStrike.Utils.Inventories;
import me.Romindous.CounterStrike.Utils.PacketUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.*;
import org.bukkit.entity.Display.Billboard;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.joml.Vector3f;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Ostrov;
import ru.komiss77.enums.Stat;
import ru.komiss77.modules.bots.BotManager;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.world.XYZ;
import ru.komiss77.notes.Slow;
import ru.komiss77.utils.TCUtils;
import ru.komiss77.version.Nms;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

public class Invasion extends Arena {

	private final int KD_NIGHT = 60, KD_DAY = 120;
	private final String A_HP = "ahp", B_HP = "bhp";
	
	public final HashMap<Integer, WeakReference<Mob>> TMbs;
	public final HashMap<Integer, Mobber> mbbrs;
	public final TextDisplay ads, bds;
	
	public byte apc;
	public byte bpc;
	public byte ccl;
	public int cnt;
	public boolean isDay;
	
	public Invasion(final String name, final byte min, final byte max, 
		final XYZ[] TSps, final XYZ[] CTSps, final XYZ[] spots, final World w, 
		final XYZ ast, final XYZ bst, final boolean rnd, final boolean bots) {
		super(name, min, max, TSps, CTSps, spots, w, rnd, bots);
		this.TMbs = new HashMap<>();
		this.mbbrs = new HashMap<>();
		this.isDay = true;
		
		ads = w.spawn(ast.getCenterLoc(w), TextDisplay.class);
		ads.setPersistent(true);
		ads.setBillboard(Billboard.CENTER);
		ads.text(TCUtils.format("§7Точка §bA§7: §d100%"));
		ads.setShadowed(true);
		ads.setSeeThrough(true);
		ads.setViewRange(0f);
		final Transformation atr = ads.getTransformation();
		ads.setTransformation(new Transformation(atr.getTranslation(), 
			atr.getLeftRotation(), new Vector3f(1.6f, 1.6f, 1.6f), atr.getRightRotation()));
		//Transformation(Vector3f translation, AxisAngle4f leftRotation, Vector3f scale, AxisAngle4f rightRotation)
		
		bds = w.spawn(bst.getCenterLoc(w), TextDisplay.class);
		bds.setPersistent(true);
		bds.setBillboard(Billboard.CENTER);
		bds.text(TCUtils.format("§7Точка §6B§7: §d100%"));
		bds.setShadowed(true);
		bds.setSeeThrough(true);
		bds.setViewRange(0f);
		final Transformation btr = bds.getTransformation();
		bds.setTransformation(new Transformation(btr.getTranslation(), 
			btr.getLeftRotation(), new Vector3f(1.6f, 1.6f, 1.6f), btr.getRightRotation()));
		
		cntBeg();
	}
	
	public static Invasion getMobInvasion(final int id) {
		for (final Arena ar : Main.actvarns.values()) {
			if (ar instanceof Invasion && ((Invasion) ar).TMbs.containsKey(id)) {
				return (Invasion) ar;
			}
		}
		return null;
	}

	@Override
	public boolean addPl(final Shooter sh) {
		sh.kills0();
		sh.spwnrs0();
		sh.deaths0();
		sh.money(0);
		sh.arena(this);
		final Player p = sh.getPlayer();
		if (p != null) {
			Main.nrmlzPl(p, true);
			switch (gst) {
			case WAITING:
			case BEGINING:
				shtrs.put(sh, Team.CTs);
				sh.item(Main.mkItm(Material.HEART_OF_THE_SEA, "§чБоторейка", 10), 4);
				sh.item(Main.mkItm(Material.GHAST_TEAR, "§5Магазин", 10), 6);
				sh.item(Main.mkItm(Material.SLIME_BALL, "§cВыход", 10), 8);
				if (!rnd) p.teleport(Main.getNrLoc(ApiOstrov.rndElmt(CTSawns), w));
				p.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, time * 20, 1));
				beginScore(p);
				for (final Shooter s : shtrs.keySet()) {
					final Player pl = s.getPlayer();
					if (pl != null) {
						pl.sendMessage(Main.prf() + "§7Игрок §5" + sh.name() + " §7зашел на карту!");
						PM.getOplayer(pl).score.getSideBar().update(CT_AMT, "§7Защитников: "
							+ Team.CTs.clr + getTmAmt(Team.CTs, true, true) + " §7чел.");
						PacketUtils.sendAcBr(pl, "§7Игроков: §5" + shtrs.size() + " §7из §5" + max + " §7(макс)!");
					}
				}
				break;
			case ROUND:
				if (!shtrs.containsKey(sh)) {//spec
					shtrs.put(sh, Team.SPEC);
					gameScore(sh, p);
					p.setGameMode(GameMode.SPECTATOR);
					p.teleport(Main.getNrLoc(Main.srnd.nextBoolean() ? ApiOstrov.rndElmt(TSpawns) : ApiOstrov.rndElmt(CTSawns), w));
					sh.item(Main.mkItm(Material.NETHER_STAR, "§eВыбор Комманды", 10), 2);
					sh.item(Main.mkItm(Material.SLIME_BALL, "§cВыход", 10), 8);
					for (final Entry<Shooter, Team> e : shtrs.entrySet()) {
						final Shooter s = e.getKey();
						final Player pl = s.getPlayer();
						if (pl != null) {
							pl.sendMessage(Main.prf() + "§7Игрок §5" + sh.name() + " §7зашел посмотреть!");
						}
					}
					break;
				}

				shtrs.put(sh, Team.CTs);
				chngMn(sh, 250);
				addToGm(p, Team.CTs);
				gameScore(sh, p);
				final int pls = getPlaying(true, false);
				cnt = Math.max(((isDay ? KD_DAY : KD_NIGHT) - (ccl << 1)) / pls, 1);
				for (final Shooter s : shtrs.keySet()) {
					final Player pl = s.getPlayer();
					if (pl != null) {
						pl.sendMessage(Main.prf() + "§7Игрок §5" + sh.name() + " §7зашел играть!");
						PM.getOplayer(pl).score.getSideBar()
							.update(CT_AMT, "§7Защитников: " + Team.CTs.clr
								+ getTmAmt(Team.CTs, true, true) + " §7чел.")
							.update(B_HP, "§7Точка §6B §7: §6" + String.valueOf(bpc) + "%");
					}
				}
				
				if (pls > 1) {
					bds.setViewRange(100f);
					bds.getLocation().getBlock().setType(Material.WARPED_PRESSURE_PLATE, false);
				}
				
				for (final TripWire tw : tws) {
					tw.showNade(p);
				}
				
				for (final Mobber mb : mbbrs.values()) {
					if (mb.isAlive()) Nms.colorGlow(mb.ind, NamedTextColor.DARK_RED, false);
				}
				break;
			case BUYTIME:
			case ENDRND:
			case FINISH:
				p.sendMessage(Main.prf() + "§cЭта игра уже заканчивается!");
				Main.lobbyPl(p);
				return false;
			}
			Inventories.updtGm(this);
			final Component tpl = TCUtils.format("§7Сейчас в игре: §d" + MainLis.getPlaying() + "§7 человек!");
			for (final Player pl : Bukkit.getOnlinePlayers()) {
				pl.sendPlayerListFooter(tpl);
			}
		}
		return true;
	}

	public void addToGm(final Player p, final Team tm) {
		p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
		p.teleport(Main.getNrLoc(ApiOstrov.rndElmt(CTSawns), w));
		p.getInventory().setItem(8, Main.mkItm(Material.GHAST_TEAR, "§5Магазин", 10));
		p.getInventory().setItem(7, Main.mkItm(Material.GOLD_NUGGET, "§eКусачки §f\u9268", 10));
		p.getInventory().setItem(2, Main.mkItm(Material.BLAZE_ROD, "§fНож \u9298", 10));
		p.setGameMode(isDay ? GameMode.SURVIVAL : GameMode.SPECTATOR);
	}

	@Override
	public boolean rmvPl(final Shooter sh) {
		sh.kills0();
		sh.spwnrs0();
		sh.deaths0();
		sh.money(0);
		final Team tm = shtrs.remove(sh);
		final Player p = sh.getPlayer();
		if (p != null) {
			if (tm == null) {
				p.sendMessage(Main.prf() + "§cВы не находитесь в игре!");
				return false;
			}
			switch (gst) {
			case WAITING:
			case BEGINING:
				if (shtrs.size() == 0) {
					if (tsk != null) {
						tsk.cancel();
					}
					endSps();
					this.end();
				} else {
					for (final Shooter s : shtrs.keySet()) {
						final Player pl = s.getPlayer();
						if (pl != null) {
							pl.sendMessage(Main.prf() + "§7Игрок §5" + sh.name() + " §7вышел с карты!");
							PacketUtils.sendAcBr(pl, "§7Игроков: §5" + shtrs.size() + " §7из §5" + max + " §7(макс)!");
							PM.getOplayer(pl).score.getSideBar().update(CT_AMT, "§7Защитников: "
								+ Team.CTs.clr + getTmAmt(Team.CTs, true, true) + " §7чел.");
						}
					}
				}
				break;
			case BUYTIME:
			case ROUND:
			case ENDRND:
				if (tm == Team.SPEC) {
					for (final Shooter s : shtrs.keySet()) {
						final Player pl = s.getPlayer();
						if (pl != null) {
							pl.sendMessage(Main.prf() + "§7Игрок §5" + sh.name() + " §7вышел из просмотра!");
						}
					}
					break;
				}

				final int pls = getPlaying(true, false),
					sz = getTmAmt(Team.CTs, false, true);
				if (sz == 0) {
					final int rm = getTmAmt(Team.CTs, true, true);
					if (rm == 0 || getTmAmt(Team.CTs, false, false) == 0) {
						cntFnsh(false, "§7Все §3защитники §7убиты!", "mobwin");
					} else {
						for (final Shooter s : shtrs.keySet()) {
							final Player pl = s.getPlayer();
							if (pl != null) {
								PM.getOplayer(pl).score.getSideBar().update(CT_AMT,
									"§7Защитников: " + Team.CTs.clr + rm + " §7чел.");
							}
						}
					}
				} else if (sz == 1) {
					cnt = Math.max(((isDay ? KD_DAY : KD_NIGHT) - (ccl << 1)) / pls, 1);
					for (final Shooter s : shtrs.keySet()) {
						final Player pl = s.getPlayer();
						if (pl != null) {
							pl.sendMessage(Main.prf() + "§7Игрок §5" + sh.name() + " §7вышел из игры!");
							PM.getOplayer(pl).score.getSideBar()
								.update(CT_AMT, "§7Защитников: " + Team.CTs.clr
									+ getTmAmt(Team.CTs, true, true) + " §7чел.")
								.update(B_HP, "§7Точка §6B §7: §8Не активна!");
						}
					}
					bds.setViewRange(0f);
					bds.getLocation().getBlock().setType(Material.AIR, false);
				} else {
					cnt = Math.max(((isDay ? KD_DAY : KD_NIGHT) - (ccl << 1)) / pls, 1);
					for (final Shooter s : shtrs.keySet()) {
						final Player pl = s.getPlayer();
						if (pl != null) {
							pl.sendMessage(Main.prf() + "§7Игрок §5" + sh.name() + " §7вышел из игры!");
							PM.getOplayer(pl).score.getSideBar()
								.update(CT_AMT, "§7Защитников: " + Team.CTs.clr
									+ getTmAmt(Team.CTs, true, true) + " §7чел.");
						}
					}
				}
				break;
			case FINISH:
				if (shtrs.size() == 0) {
					this.time = 1;
				}
				break;
			}
		}
		Inventories.updtGm(this);
		Main.lobbyPl(p);
		return true;
	}

	//счетчик
	public void cntBeg() {
		time = 10;
		gst = GameState.BEGINING;
		final Arena ar = this;
		ApiOstrov.sendArenaData(this.name, ru.komiss77.enums.GameState.СТАРТ, "§7[§5CS§7]", 
			"§dВторжение", " ", "§7Игроков: §5" + shtrs.size() + "§7/§5" + this.min, "", shtrs.size());
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
							PacketUtils.sendSbTtl(p, "§5§l" + time, 10);
							PM.getOplayer(p).score.getSideBar().update(LIMIT, rtm);
						}
					}
					break;
				case 3, 2, 1:
					for (final Shooter sh : shtrs.keySet()) {
						final Player p = sh.getPlayer();
						if (p != null) {
							p.playSound(p.getLocation(), Sound.BLOCK_DISPENSER_FAIL, 0.8f, 1.2f);
							PacketUtils.sendSbTtl(p, "§d§l" + time, 10);
							PM.getOplayer(p).score.getSideBar().update(LIMIT, rtm);
						}
					}
					break;
				case 0:
					if (tsk != null) {
						tsk.cancel();
					}
					cntStrt();
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

	private void cntStrt() {
		ccl = 0;
		time = 180;
		apc = 100;
		bpc = 100;
		isDay = false;
		gst = GameState.ROUND;
		for (final Entity e : w.getEntitiesByClasses(Item.class, ArmorStand.class, Turtle.class)) {
			e.remove();
		}
		
		if (botInv != null && bots) {
			for (int i = (max >> 1) - shtrs.size(); i > 0; i--) {
				shtrs.put(BotManager.createBot("Bot-v" + botID++, BtShooter.class, nm -> new BtShooter(nm, this)), Team.CTs);
			}
		}
		
		for (final Shooter sh : shtrs.keySet()) {
			sh.money(0);
			final LivingEntity le = sh.getEntity();
			if (sh instanceof PlShooter) {
				Main.nrmlzPl((Player) le, true);
				gameScore(sh, (Player) le);
			} else if (sh instanceof BtShooter) {
				((BtShooter) sh).willBuy = true;
			}
			
			sh.teleport(le, Main.getNrLoc(ApiOstrov.rndElmt(CTSawns), w));
			sh.item(Main.mkItm(Material.GOLD_NUGGET, "§eКусачки §f\u9268", 10), 7);
			sh.item(Main.mkItm(Material.BLAZE_ROD, "§fНож \u9298", 10), 2);
			sh.setTabTag(Team.CTs.icn + " ", " §7[" + sh.kills() +
				"-" + sh.spwnrs() + "-" + sh.deaths() + "]", Team.CTs.clr);
		}
		
		ads.setViewRange(100f);
		ads.getLocation().getBlock().setType(Material.WARPED_PRESSURE_PLATE, false);
		if (shtrs.size() > 1) {
			bds.setViewRange(100f);
			bds.getLocation().getBlock().setType(Material.WARPED_PRESSURE_PLATE, false);
		}
		
		Ostrov.async(() -> ApiOstrov.shuffle(spots));
		for (int i = spots.length >> 1; i >= 0; i--) {
			new Mobber(spots[i], this);
		}
		Nms.colorGlow(ads, NamedTextColor.AQUA, false);
		Nms.colorGlow(bds, NamedTextColor.GOLD, false);
		swpDayNght();
		Inventories.updtGm(this);
	}

	private void swpDayNght() {
		if (tsk != null) {
			tsk.cancel();
		}
		final int pls = getPlaying(true, false);
		gst = GameState.ROUND;
		if (isDay) {
			//night
			isDay = false;
			time = (short) (ccl * 20 + 60);
			cnt = Math.max((KD_NIGHT - (ccl << 1)) / pls, 1);
			ApiOstrov.sendArenaData(this.name, ru.komiss77.enums.GameState.ИГРА, "§7[§5CS§7]", "§dВторжение", " ", "§7Игроков: §5" + pls + "§7/§5" + this.max, "", pls);
//			final ArrayList<Player> pls = new ArrayList<>();
			for (final Shooter sh : shtrs.keySet()) {
				sh.item(Main.air, 8);
				final Player p = sh.getPlayer();
				if (p != null) {
					PacketUtils.sendTtlSbTtl(p, "§4Ночь", "§7Крепитесь и защищайте точки!", 50);
					PM.getOplayer(p).score.getSideBar().update(STAGE, "§7Cтадия: §5Ночь §7(" + getTime(time, "§5") + "§7)");
					p.playSound(p.getLocation(), "cs.info.night", 10f, 1f);
				}
			}

			Ostrov.async(() -> ApiOstrov.shuffle(spots));
			for (int i = spots.length >> 1; i > 0; i--) {
				final Mobber mb = mbbrs.get(spots[i].getSLoc());
				if (mb == null) {
					new Mobber(spots[i], this);
					continue;
				}
				
				mb.ind.setGlowing(true);
				if (mb.getType() == Material.SPAWNER) {
					switch (mb.et) {
					case ZOMBIE_VILLAGER:
						mb.et = EntityType.STRAY;
						break;
					case STRAY:
						mb.et = EntityType.PILLAGER;
						break;
					case PILLAGER:
						mb.et = EntityType.PIGLIN_BRUTE;
						break;
					default:
						break;
					}
				} else {
					mb.setSpwn();
					mb.et = EntityType.ZOMBIE_VILLAGER;
				}
			}
		} else {
			//day
			isDay = true;
			time = (short) (ccl * 10 + 60);
			cnt = Math.max((KD_DAY - (ccl << 1)) / pls, 1);
			ccl++;
			ApiOstrov.sendArenaData(this.name, ru.komiss77.enums.GameState.ЭКИПИРОВКА, "§7[§5CS§7]", 
				"§dВторжение", " ", "§7Игроков: §5" + pls + "§7/§5" + this.max, "", pls);
			Inventories.updtGm(this);
			for (final Entry<Shooter, Team> e : shtrs.entrySet()) {
				final Shooter sh = e.getKey();
				final LivingEntity le = sh.getEntity();
				if (sh instanceof PlShooter) {
					final Player p = (Player) le;
					PacketUtils.sendTtlSbTtl(p, "§3День", "§7Закупайтесь и ломайте спавнеры!", 80);
					PM.getOplayer(p).score.getSideBar().update(STAGE, "§7Cтадия: §dДень §7(" + getTime(time, "§d") + "§7)");
					p.playSound(p.getLocation(), "cs.info.day", 10f, 1f);
					if (e.getValue() == Team.SPEC) continue;

					if (p.getGameMode() == GameMode.SPECTATOR) {
						p.setGameMode(GameMode.SURVIVAL);
						p.teleport(Main.getNrLoc(ApiOstrov.rndElmt(CTSawns), w));
					}
				} else {
					if (sh.isDead()) {
						sh.teleport(le, Main.getNrLoc(ApiOstrov.rndElmt(CTSawns), w));
					}
				}
				chngMn(sh, 250);
				sh.item(Main.mkItm(Material.GHAST_TEAR, "§5Магазин", 10), 8);
				sh.getEntity().addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, time * 20 + 10, 1));
			}
			
			for (final WeakReference<Mob> mbr : TMbs.values()) {
				final Mob mb = mbr.get();
				if (mb != null) {
					mb.setFireTicks(10);
					mb.damage(100d);
				}
			}
			TMbs.clear();
		}
		
		tsk = new BukkitRunnable() {
			@Override
			public void run() {
				if (w.getTime() % 24000 != (isDay ? 6000L : 18000L)) {
					w.setTime(w.getTime() + 1000L);
				}
				for (final Mobber mb : mbbrs.values()) {
					if (mb.isAlive() && mb.getType() == Material.SPAWNER
							&& Main.srnd.nextInt(cnt) == 0 && gst == GameState.ROUND) {
						mb.spwnMb();
					}
				}
				final String ttrm = "§7Cтадия: " + (isDay ? "§dДень §7(" + getTime(time, "§d") : "§5Ночь §7(" + getTime(time, "§5")) + "§7)";
				switch (time) {
				case 60:
					for (final Shooter sh : shtrs.keySet()) {
						final Player p = sh.getPlayer();
						if (p != null) {
							PacketUtils.sendAcBr(p, "§7Осталась §d1 §7минута!");
							PM.getOplayer(p).score.getSideBar().update(STAGE, ttrm);
						}
					}
					break;
				case 30:
					for (final Shooter sh : shtrs.keySet()) {
						final Player p = sh.getPlayer();
						if (p != null) {
							PacketUtils.sendAcBr(p, "§7Осталось §d" + time + " §7секунд!");
							PM.getOplayer(p).score.getSideBar().update(STAGE, ttrm);
							p.playSound(p.getLocation(), "cs.info." + (isDay ? "day30sec" : "nit30sec"), 10f, 1f);
						}
					}
					break;
				case 10:
					for (final Shooter sh : shtrs.keySet()) {
						final Player p = sh.getPlayer();
						if (p != null) {
							PacketUtils.sendAcBr(p, "§7Осталось §d" + time + " §7секунд!");
							PM.getOplayer(p).score.getSideBar().update(STAGE, ttrm);
						}
					}
					break;
				case 0:
					swpDayNght();
					break;
				default:
					for (final Shooter sh : shtrs.keySet()) {
						final Player p = sh.getPlayer();
						if (p != null) {
							PM.getOplayer(p).score.getSideBar().update(STAGE, ttrm);
						}
					}
					break;
				}
				time--;
			}
		}.runTaskTimer(Main.plug, 20, 20);
	}

	private void cntFnsh(final boolean CTwn, final String sbt, final String snd) {
		if (tsk != null) tsk.cancel();
		time = 10;
		gst = GameState.FINISH;
		ApiOstrov.sendArenaData(this.name, ru.komiss77.enums.GameState.ФИНИШ, "§7[§5CS§7]", 
			"§dВторжение", " ", "§7Игроков: §5" + shtrs.size() + "§7/§5" + this.min, "", shtrs.size());
		Inventories.updtGm(this);
		for (final Entity e : w.getEntitiesByClasses(Item.class, ArmorStand.class, Turtle.class)) {
			e.remove();
		}

		for (final BrknBlck bb : brkn) {
			bb.getBlock().setBlockData(bb.bd, false);
		}
		brkn.clear();

		for (final TripWire tw : tws) {
			tw.remove();
		}
		tws.clear();
		
		Shooter tp = null;
		for (final Shooter sh : shtrs.keySet()) {
			if (tp == null || tp.kills() < sh.kills()) {
				tp = sh;
			}
		}
		final String ttl = CTwn ? "§3Победа!" : "§4Поражение!";
		for (final Entry<Shooter, Team> e : shtrs.entrySet()) {
			final Shooter sh = e.getKey();
			if (sh instanceof PlShooter) {
				final Player p = sh.getPlayer();
				p.closeInventory();
				p.playSound(p.getLocation(), "cs.info." + snd, 10f, 1f);
				PacketUtils.sendTtlSbTtl(p, ttl, sbt, 50);
				sh.setTabTag("§7<§d" + name + "§7> ", " §7[" + sh.kills() +
					"-" + sh.spwnrs() + "-" + sh.deaths() + "]", Team.SPEC.clr);
				p.sendMessage(" \n§7Финиш: " + ttl
					+ "\n§7=-=-=-=-=-=-=-=-=-=-=-=-=-=-"
					+ "\n§7Самый кровожадный защитник:"
					+ "\n§3" + tp.name() + " §7(§5" + tp.kills() + " §7Убийств)"
					+ "\n \n§7Ваши убийства: §d" + sh.kills()
					+ "\n§7=-=-=-=-=-=-=-=-=-=-=-=-=-=-");
				if (e.getValue() == Team.SPEC) continue;

				ApiOstrov.addStat(p, Stat.CS_game);
				SkinQuest.tryCompleteQuest(sh, Quest.ДУША, ApiOstrov.getStat(p, Stat.CS_game));
				sh.inv().clear();
				winScore(sh, p, CTwn);
			} else {
				((BtShooter) sh).remove();
			}
		}
		
		endSps();
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
					final FireworkEffect fe = FireworkEffect.builder().with(Type.STAR).withColor(Color.TEAL).withFlicker().build();
					for (final Shooter sh : shtrs.keySet()) {
						final Player p = sh.getPlayer();
						if (p != null) {
							PM.getOplayer(p).score.getSideBar().update(LIMIT, rtm);
							if (CTwn) {
								final Firework fw = (Firework) p.getWorld().spawnEntity(p.getEyeLocation(), EntityType.FIREWORK);
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

	private void beginScore(final Player p) {
		PM.getOplayer(p).score.getSideBar().reset().title("§7[§5CS:GO§7]")
			.add(" ")
			.add("§7Карта: §5" + name)
			.add("§7Режим: §dВторжение")
			.add("§7=-=-=-=-=-=-=-=-")
			.add(T_AMT, "§7Точки: " + (shtrs.size() == 1 ? "§bA" : "§bA §7и §6B"))
			.add(" ")
			.add(CT_AMT, "§7Защитников: " + Team.CTs.clr + shtrs.size() + " §7чел. §8✦ Ты")
			.add("§7=-=-=-=-=-=-=-=-")
			.add(LIMIT, "§7Начало через: §5" + String.valueOf(time))
			.add(" ")
			.add("§e   ostrov77.ru").build();
	}
	
	private void gameScore(final Shooter sh, final Player p) {
		PM.getOplayer(p).score.getSideBar().reset().title("§7[§5CS:GO§7]")
			.add(" ")
			.add("§7Карта: §5" + name)
			.add("§7Режим: §dВторжение")
			.add("§7=-=-=-=-=-=-=-=-")
			.add(STAGE, "§7Cтадия: " + (isDay ? "§dДень §7(" + getTime(time, "§d") 
					: "§5Ночь §7(" + getTime(time, "§5")) + "§7)")
			.add(" ")
			.add(A_HP, "§7Точка §bA §7: §b" + String.valueOf(apc) + "%")
			.add(B_HP, "§7Точка §6B §7: " + (getTmAmt(Team.CTs, true, false) == 1 ? "§7Не активна!" : "§6" + String.valueOf(bpc) + "%"))
			.add(" ")
			.add(CT_AMT, "§7Защитников: " + Team.CTs.clr + getTmAmt(Team.CTs, true, true) + " §7чел. §8✦ Ты")
			.add("§7=-=-=-=-=-=-=-=-")
			.add(MONEY, "§7Монет: §d" + sh.money() + " §6⛃")
			.add(" ")
			.add("§e   ostrov77.ru").build();
	}

	private void winScore(final Shooter sh, final Player p, final boolean isCTWn) {
		PM.getOplayer(p).score.getSideBar().reset().title("§7[§5CS:GO§7]")
			.add(isCTWn ? "§3Защитники §7победили!" : "§4Захватчики §7победили!")
			.add(" ")
			.add("§7Карта: §5" + name)
			.add("§7Режим: §dВторжение")
			.add("§7=-=-=-=-=-=-=-=-")
			.add("§7Cтадия: §dФиниш")
			.add(LIMIT, getTime(time, "§d") + " §7до конца!")
			.add(" ")
			.add(CT_AMT, "§7Защитников: " + Team.CTs.clr + getTmAmt(Team.CTs, true, true) + " §7чел. §8✦ Ты")
			.add("§7=-=-=-=-=-=-=-=-")
			.add(MONEY, "§7Монет: §d" + sh.money() + " §6⛃")
			.add(" ")
			.add("§e   ostrov77.ru").build();
	}
	
	
	@Override
	public int getTmAmt(final Team tm, final boolean bots, final boolean alv) {
		int n = 0;
		for (final Shooter sh : shtrs.keySet()) {
			if (alv && sh.isDead()) continue;
			if (sh instanceof PlShooter || bots) {
				n++;
			}
		}
		return n;
	}

	@Override
	public void killSh(final Shooter sh) {
		final LivingEntity le = sh.getEntity();
		if (gst == GameState.ROUND && !isDay) {
			addDth(sh);
			if (sh instanceof PlShooter) {
				final Player pl = (Player) le;
				pl.setGameMode(GameMode.SPECTATOR);
				pl.closeInventory();
			} else if (sh instanceof BtShooter) {
				((BtShooter) sh).die(le);
			}
			
			final int rm = getTmAmt(Team.CTs, true, true);
			for (final Shooter s : shtrs.keySet()) {
				final Player pl = s.getPlayer();
				if (pl != null) {
					PM.getOplayer(pl).score.getSideBar().update(CT_AMT,
						"§7Защитников: " + Team.CTs.clr + rm + " §7чел.");
				}
			}
			if (rm == 0) {
				cntFnsh(false, "§7Все §3защитники §7убиты!", "mobwin");
			}
		} else {
			if (gst == GameState.ROUND) {
				addDth(sh);
			} else if (le.hasPotionEffect(PotionEffectType.GLOWING)) {
				le.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, time * 20, 1));
			}
			if (sh instanceof PlShooter) {
				final Player p = (Player) le;
				Main.nrmlzPl(p, false);
				p.closeInventory();
			}
			final Location loc = rnd && gst != GameState.ROUND ?
				Main.lobby.getCenterLoc() : Main.getNrLoc(ApiOstrov.rndElmt(CTSawns), w);
			sh.teleport(le, loc);
		}
	}

	public void addMbKll(final Shooter sh) {
		sh.killsI();
		sh.setTabTag(Team.CTs.icn + " ", " §7[" + sh.kills() +
			"-" + sh.spwnrs() + "-" + sh.deaths() + "]", Team.CTs.clr);
		final Player p = sh.getPlayer();
		if (p != null) {
			SkinQuest.tryCompleteQuest(sh, Quest.ДЮНА, sh.kills());
			ApiOstrov.addStat(p, Stat.CS_mobs);
		}
	}

	public void addSpDfs(final Shooter sh) {
		sh.spwnrsI();
		sh.setTabTag(Team.CTs.icn + " ", " §7[" + sh.kills() +
			"-" + sh.spwnrs() + "-" + sh.deaths() + "]", Team.CTs.clr);
		final Player p = sh.getPlayer();
		if (p != null) {
			SkinQuest.tryCompleteQuest(sh, Quest.ЛГБТ, sh.spwnrs());
			ApiOstrov.addStat(p, Stat.CS_spnrs);
		}
	}

	@Override
	public void addDth(final Shooter sh) {
		sh.deathsI();
		sh.setTabTag(Team.CTs.icn + " ", " §7[" + sh.kills() +
			"-" + sh.spwnrs() + "-" + sh.deaths() + "]", Team.CTs.clr);
		final Player p = sh.getPlayer();
		if (p != null) {
			ApiOstrov.addStat(p, Stat.CS_death);
		}
	}
	
	public void rmvSpnr(final Shooter sh) {
		final Mobber mb = getClsMbbr(sh.getPos(), true);
		if (mb != null) {
			mb.defusing = null;
			chngMn(sh, 150);
			addSpDfs(sh);
			dieSpnr(mb);
		}
	}
	
	public void dieSpnr(final Mobber mb) {
		mb.setDef();
		for (final Mobber m : mbbrs.values()) {
			if (m.isAlive()) return;
		}
		cntFnsh(true, "§7Все §4спавнеры §7обезврежены!", "despawner");
	}

	public void wrngWire(final Shooter sh) {
		final Mobber mb = getClsMbbr(sh.getPos(), true);
		if (mb != null) mb.spwnMb();
	}
	
	public void endSps() {
		for (final Mobber m : mbbrs.values()) {
			if (m != null) {
				m.ind.remove();
				m.getBlock().setType(Material.AIR, false);
			}
		}
		ads.getLocation().getBlock().setType(Material.AIR, false);
		ads.remove();
		bds.getLocation().getBlock().setType(Material.AIR, false);
		bds.remove();
	}

	public void hrtSt(final boolean isA, final byte dmg) {
		if (isA) {
			apc -= dmg;
			if (apc > 0) {
				ads.text(TCUtils.format("§7Точка §bA§7: §d" + apc + "%"));
				final Location lc = ads.getLocation();
				w.spawnParticle(Particle.SOUL_FIRE_FLAME, lc, 24, 0.2d, 0.4d, 0.2d, 0.1d);
				for (final Shooter sh : shtrs.keySet()) {
					final Player p = sh.getPlayer();
					if (p != null) {
						p.playSound(lc, Sound.BLOCK_SCULK_SENSOR_CLICKING, 40f, 1f);
						PacketUtils.sendSbTtl(p, "§bA §7атакована : §b" + (apc + dmg) + " §7=-> §b" + apc, 30);
						PM.getOplayer(p).score.getSideBar().update(A_HP, "§7Точка §bA §7: §b" + String.valueOf(apc) + "%");
					}
				}
			} else {
				final Block b = ads.getLocation().getBlock();
				b.getWorld().playSound(b.getLocation(), Sound.BLOCK_CONDUIT_DEACTIVATE, 40f, 2f);
				cntFnsh(false, "§7Точка §bA §7разрушена!", "sitebreak");
				b.setType(Material.AIR,false);
				final int X = b.getX();
				final int Y = b.getY();
				final int Z = b.getZ();
				final HashSet<XYZ> cls = new HashSet<>();
				b.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, b.getLocation(), 20, 5d, 5d, 5d);
				b.getWorld().playSound(b.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 5f, 0.8f);
				for (int x = -5; x < 6; x++) {
					for (int y = -5; y < 6; y++) {
						for (int z = -5; z < 6; z++) {
							final int bnd = x*x + y*y + z*z;
							if (bnd > 0 && Nms.getFastMat(w, X + x, Y + y, Z + z).isAir() && Nms.getFastMat(w, X + x, Y + y - 1, Z + z).isOccluding() && Main.srnd.nextInt(bnd) < 6) {
								for (final Player p : b.getWorld().getPlayers()) {
									p.sendBlockChange(new Location(w, X + x, Y + y, Z + z), Material.FIRE.createBlockData());
									cls.add(new XYZ("", X + x, Y + y, Z + z));
								}
							} else if (Nms.getFastMat(w, X + x, Y + y, Z + z).isOccluding() && Main.srnd.nextInt(bnd) < 10) {
								for (final Player p : b.getWorld().getPlayers()) {
									p.sendBlockChange(new Location(w, X + x, Y + y, Z + z), Material.COAL_BLOCK.createBlockData());
									cls.add(new XYZ("", X + x, Y + y, Z + z));
								}
							}
						}
					}
				}

				Ostrov.async(() -> {
					final Map<Position, BlockData> bls = new HashMap<>();
					for (final XYZ bl : cls) {
						bls.put(bl.getCenterLoc(w), w.getBlockData(bl.x, bl.y, bl.z));
					}

					for (final Player p : w.getPlayers()) {
						p.sendMultiBlockChange(bls);
					}
				}, 200);
			}
		} else {
			bpc -= dmg;
			if (bpc > 0) {
				bds.text(TCUtils.format("§7Точка §6B§7: §d" + bpc + "%"));
				final Location lc = bds.getLocation();
				w.spawnParticle(Particle.SOUL_FIRE_FLAME, lc, 24, 0.2d, 0.4d, 0.2d, 0.1d);
				for (final Shooter sh : shtrs.keySet()) {
					final Player p = sh.getPlayer();
					if (p != null) {
						p.playSound(lc, Sound.BLOCK_SCULK_SENSOR_CLICKING, 40f, 1f);
						PacketUtils.sendSbTtl(p, "§6B §7атакована : §6" + (bpc + dmg) + " §7=-> §6" + bpc, 30);
						PM.getOplayer(p).score.getSideBar().update(B_HP, "§7Точка §6B §7: §6" + String.valueOf(bpc) + "%");
					}
				}
			} else {
				final Block b = bds.getLocation().getBlock();
				b.getWorld().playSound(b.getLocation(), Sound.BLOCK_CONDUIT_DEACTIVATE, 40f, 2f);
				cntFnsh(false, "§7Точка §6B §7разрушена!", "sitebreak");
				b.setType(Material.AIR,false);
				final int X = b.getX();
				final int Y = b.getY();
				final int Z = b.getZ();
				final HashSet<XYZ> cls = new HashSet<>();
				b.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, b.getLocation(), 20, 5d, 5d, 5d);
				b.getWorld().playSound(b.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 5f, 0.8f);

				for (int x = -5; x < 6; x++) {
					for (int y = -5; y < 6; y++) {
						for (int z = -5; z < 6; z++) {
							final int bnd = x*x + y*y + z*z;
							if (bnd > 0 && Nms.getFastMat(w, X + x, Y + y, Z + z).isAir() && Nms.getFastMat(w, X + x, Y + y - 1, Z + z).isOccluding() && Main.srnd.nextInt(bnd) < 6) {
								for (final Player p : b.getWorld().getPlayers()) {
									p.sendBlockChange(new Location(w, X + x, Y + y, Z + z), Material.FIRE.createBlockData());
									cls.add(new XYZ("", X + x, Y + y, Z + z));
								}
							} else if (Nms.getFastMat(w, X + x, Y + y, Z + z).isOccluding() && Main.srnd.nextInt(bnd) < 10) {
								for (final Player p : b.getWorld().getPlayers()) {
									p.sendBlockChange(new Location(w, X + x, Y + y, Z + z), Material.COAL_BLOCK.createBlockData());
									cls.add(new XYZ("", X + x, Y + y, Z + z));
								}
							}
						}
					}
				}

				Ostrov.async(() -> {
					final Map<Position, BlockData> bls = new HashMap<>();
					for (final XYZ bl : cls) {
						bls.put(bl.getCenterLoc(w), w.getBlockData(bl.x, bl.y, bl.z));
					}

					for (final Player p : w.getPlayers()) {
						p.sendMultiBlockChange(bls);
					}
				}, 200);
			}
		}
	}
	
	@Override
	public GameType getType() {
		return GameType.INVASION;
	}
	
	@Slow(priority = 2)
	public Mobber getClsMbbr(final XYZ to, final boolean alive) {
		Mobber m = null;
		int dd = Integer.MAX_VALUE;
		for (final Mobber mb : mbbrs.values()) {
			if (alive && !mb.isAlive()) continue;
			final int d = to.distSq(mb);
			if (d < dd) {
				m = mb;
				dd = d;
			}
		}
		return m;
	}
	
	public Mobber getRndMbbr(final boolean alive) {
		final Mobber m = mbbrs.get(ApiOstrov.rndElmt(spots).getSLoc());
		return m == null || (alive && !m.isAlive()) ? null : m;
	}
}
