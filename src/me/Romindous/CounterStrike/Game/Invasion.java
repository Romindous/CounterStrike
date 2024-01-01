package me.Romindous.CounterStrike.Game;

import me.Romindous.CounterStrike.Listeners.MainLis;
import me.Romindous.CounterStrike.Main;
import me.Romindous.CounterStrike.Objects.Game.*;
import me.Romindous.CounterStrike.Objects.Loc.BrknBlck;
import me.Romindous.CounterStrike.Objects.Mobs.Mobber;
import me.Romindous.CounterStrike.Objects.Shooter;
import me.Romindous.CounterStrike.Objects.Skins.Quest;
import me.Romindous.CounterStrike.Objects.Skins.SkinQuest;
import me.Romindous.CounterStrike.Utils.Inventories;
import me.Romindous.CounterStrike.Utils.PacketUtils;
import net.kyori.adventure.text.Component;
import net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy;
import org.bukkit.*;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.entity.Display.Billboard;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.Transformation;
import org.joml.Vector3f;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Ostrov;
import ru.komiss77.enums.Stat;
import ru.komiss77.modules.bots.BotManager;
import ru.komiss77.modules.world.XYZ;
import ru.komiss77.notes.Slow;
import ru.komiss77.utils.TCUtils;
import ru.komiss77.version.VM;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

public class Invasion extends Arena {
	
	public final HashMap<Integer, WeakReference<Mob>> TMbs;
	public final LinkedHashMap<Shooter, Mobber> dfs;
	public final HashMap<XYZ, Mobber> mbbrs;
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
		this.dfs = new LinkedHashMap<>();
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

	public BukkitTask getTask() {
		return tsk;
	}
	
	public static Invasion getMobInvasion(final int id) {
		for (final Arena ar : Main.actvarns.values()) {
			if (ar instanceof Invasion) {
				if (((Invasion) ar).TMbs.containsKey(id)) {
					return (Invasion) ar;
				}
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
				p.teleport(Main.getNrLoc(Main.rndElmt(CTSawns), w));
				p.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, tm * 20, 1));
				beginScore(sh, p);
				Main.shwHdPls(p);
				PacketUtils.sendNmTg(sh, "§7<§d" + name + "§7> ", " §7[-.-]", Team.CTs.clr);
				for (final Shooter s : shtrs.keySet()) {
					final Player pl = s.getPlayer();
					if (pl != null) {
						pl.sendMessage(Main.prf() + "§7Игрок §5" + sh.name() + " §7зашел на карту!");
						Main.chgSbdTm(pl.getScoreboard(), "amt", "", "§3" + shtrs.size() + " §7чел.");
						Main.chgSbdTm(pl.getScoreboard(), "sts", "", shtrs.size() == 1 ? "§bA" : "§bA §7и §6B");
						//Main.chgSbdTm(p.getScoreboard(), "tamt", "", "§4" + String.valueOf(getTmAmt(Team.Ts)) + (e.getValue() == Team.Ts ? " §7чел. §8✦ Вы" : " §7чел."));
						//Main.chgSbdTm(p.getScoreboard(), "ctamt", "", "§3" + String.valueOf(getTmAmt(Team.CTs)) + (e.getValue() == Team.CTs ? " §7чел. §8✦ Вы" : " §7чел."));
						PacketUtils.sendAcBr(pl, "§7Игроков: §5" + shtrs.size() + " §7из §5" + max + " §7(макс)!", 30);
					}
					PacketUtils.sendNmTg(PacketUtils.getNMSPl(p).c.h, s, "§7<§d" + name + "§7> ", " §7[-.-]", true, Team.CTs.clr);
				}
				break;
			case ROUND:
				shtrs.put(sh, Team.CTs);
				chngMn(sh, 250);
				addToGm(p, Team.CTs);
				gameScore(sh, p);
				cnt = Math.max(((isDay ? 160 : 80) - (ccl << 1)) / shtrs.size(), 1);
				PacketUtils.sendNmTg(sh, Team.CTs.icn + " ", " §7[0-0-0]", Team.CTs.clr);
				for (final Shooter s : shtrs.keySet()) {
					final Player pl = s.getPlayer();
					if (pl != null) {
						pl.sendMessage(Main.prf() + "§7Игрок §5" + sh.name() + " §7зашел играть!");
						Main.chgSbdTm(pl.getScoreboard(), "amt", "", "§3" + getTmAmt(Team.CTs, true, true) + " §7чел.");
						Main.chgSbdTm(pl.getScoreboard(), "bhp", "", "§6" + String.valueOf(bpc) + "%");
					} else {
						((BtShooter) s).updateAll(p);
					}
					PacketUtils.sendNmTg(PacketUtils.getNMSPl(p).c.h, s, Team.CTs.icn + " ", 
						" §7[" + s.kills() + "-" + s.spwnrs() + "-" + s.deaths() + "]", true, Team.CTs.clr);
				}
				
				if (shtrs.size() > 1) {
					bds.setViewRange(100f);
					bds.getLocation().getBlock().setType(Material.WARPED_PRESSURE_PLATE, false);
				}
				
				for (final TripWire tw : tws) {
					tw.shwNd(PacketUtils.getNMSPl(p).c);
				}
				
				for (final Mobber mb : mbbrs.values()) {
					VM.getNmsEntitygroup().colorGlow(mb.ind, '4', false);
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
			final String n = String.valueOf(MainLis.getPlaying());
			for (final Player pl : Bukkit.getOnlinePlayers()) {
				pl.sendPlayerListFooter(Component.text("§7Сейчас в игре: §d" + n + "§7 человек!"));
			}
		}
		return true;
	}

	public void addToGm(final Player p, final Team tm) {
		p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
		p.teleport(Main.getNrLoc(Main.rndElmt(CTSawns), w));
		Main.shwHdPls(p);
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
					end(this);
				} else {
					for (final Shooter s : shtrs.keySet()) {
						final Player pl = s.getPlayer();
						if (pl != null) {
							pl.sendMessage(Main.prf() + "§7Игрок §5" + sh.name() + " §7вышел с карты!");
							PacketUtils.sendAcBr(pl, "§7Игроков: §5" + shtrs.size() + " §7из §5" + max + " §7(макс)!", 30);
							Main.chgSbdTm(pl.getScoreboard(), "amt", "", "§3" + getTmAmt(Team.CTs, true, true) + " §7чел.");
						}
					}
				}
				break;
			case BUYTIME:
			case ROUND:
			case ENDRND:
				for (final TripWire tw : tws) {
					PacketUtils.getNMSPl(p).c.a(new PacketPlayOutEntityDestroy(tw.eif.af()));
				}
				final int sz = getTmAmt(Team.CTs, false, true);
				if (sz == 0) {
					final int rm = getTmAmt(Team.CTs, true, true);
					if (rm == 0 || getTmAmt(Team.CTs, false, false) == 0) {
						cntFnsh(false, "§7Все §3защитники §7убиты!", "mobwin");
					} else {
						for (final Shooter s : shtrs.keySet()) {
							final Player pl = s.getPlayer();
							if (pl != null) {
								Main.chgSbdTm(pl.getScoreboard(), "amt", "", "§3" + rm + " §7чел.");
							}
						}
					}
				} else if (sz == 1) {
					cnt = Math.max(((isDay ? 80 : 48) - ccl) / shtrs.size(), 1);
					for (final Shooter s : shtrs.keySet()) {
						final Player pl = s.getPlayer();
						if (pl != null) {
							pl.sendMessage(Main.prf() + "§7Игрок §5" + sh.name() + " §7вышел из игры!");
							Main.chgSbdTm(pl.getScoreboard(), "amt", "", "§3" + getTmAmt(Team.CTs, true, true) + " §7чел.");
							Main.chgSbdTm(pl.getScoreboard(), "bhp", "", "§7Не активна!");
						}
					}
					bds.setViewRange(0f);
					bds.getLocation().getBlock().setType(Material.AIR, false);
				} else {
					cnt = Math.max(((isDay ? 80 : 48) - ccl) / shtrs.size(), 1);
					for (final Shooter s : shtrs.keySet()) {
						final Player pl = s.getPlayer();
						if (pl != null) {
							pl.sendMessage(Main.prf() + "§7Игрок §5" + sh.name() + " §7вышел из игры!");
							Main.chgSbdTm(pl.getScoreboard(), "amt", "", "§3" + getTmAmt(Team.CTs, true, true) + " §7чел.");
						}
					}
				}
				break;
			case FINISH:
				if (shtrs.size() == 0) {
					this.tm = 1;
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
		tm = 10;
		gst = GameState.BEGINING;
		ApiOstrov.sendArenaData(this.name, ru.komiss77.enums.GameState.СТАРТ, "§7[§5CS§7]", 
			"§dВторжение", " ", "§7Игроков: §5" + shtrs.size() + "§7/§5" + this.min, "", shtrs.size());
		tsk = new BukkitRunnable() {
			@Override
			public void run() {
				switch (tm) {
				case 10, 5, 4:
					for (final Shooter sh : shtrs.keySet()) {
						final Player p = sh.getPlayer();
						if (p != null) {
							p.playSound(p.getLocation(), Sound.BLOCK_DISPENSER_FAIL, 0.8f, 1.2f);
							PacketUtils.sendSbTtl(p, "§5§l" + tm, 10);
							Main.chgSbdTm(p.getScoreboard(), "rmn", "", "§5" + String.valueOf(tm));
						}
					}
					break;
				case 3, 2, 1:
					for (final Shooter sh : shtrs.keySet()) {
						final Player p = sh.getPlayer();
						if (p != null) {
							p.playSound(p.getLocation(), Sound.BLOCK_DISPENSER_FAIL, 0.8f, 1.2f);
							PacketUtils.sendSbTtl(p, "§d§l" + tm, 10);
							Main.chgSbdTm(p.getScoreboard(), "rmn", "", "§5" + String.valueOf(tm));
						}
					}
					break;
				case 0:
					if (tsk != null) {
						tsk.cancel();
					}
					cntStrt();
					if (rnd) {
						Main.mapBlds.get(name).placeSets(w, 5);
					}
					break;
				default:
					for (final Shooter sh : shtrs.keySet()) {
						final Player p = sh.getPlayer();
						if (p != null) {
							Main.chgSbdTm(p.getScoreboard(), "rmn", "", "§5" + String.valueOf(tm));
						}
					}
					break;
				}
				tm--;
			}
		}.runTaskTimer(Main.plug, 20, 20);
	}

	private void cntStrt() {
		ccl = 0;
		tm = 180;
		apc = 100;
		bpc = 100;
		isDay = false;
		gst = GameState.ROUND;
		for (final Entity e : w.getEntitiesByClasses(Item.class, ArmorStand.class, Turtle.class)) {
			e.remove();
		}
		
		if (botInv != null && bots) {
			for (int i = (max >> 1) - shtrs.size(); i > 0; i--) {
				shtrs.put(BotManager.createBot(name, BtShooter.class, () -> new BtShooter(this)), Team.CTs);
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
			
			sh.teleport(le, Main.getNrLoc(Main.rndElmt(CTSawns), w));
			sh.item(Main.mkItm(Material.GOLD_NUGGET, "§eКусачки §f\u9268", 10), 7);
			sh.item(Main.mkItm(Material.BLAZE_ROD, "§fНож \u9298", 10), 2);
			PacketUtils.sendNmTg(sh, Team.CTs.icn + " ", " §7[" + sh.kills() + 
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
			new Mobber(spots[i], this).runTaskTimer(Main.plug, 10L, 10L);
		}
		swpDayNght();
	}

	private void swpDayNght() {
		if (tsk != null) {
			tsk.cancel();
		}
		if (isDay) {
			//night
			isDay = false;
			tm = (short) (ccl * 20 + 60);
			gst = GameState.ROUND;
			ApiOstrov.sendArenaData(this.name, ru.komiss77.enums.GameState.ИГРА, "§7[§5CS§7]", "§dВторжение", " ", "§7Игроков: §5" + shtrs.size() + "§7/§5" + this.max, "", shtrs.size());
			Inventories.updtGm(this);
//			final ArrayList<Player> pls = new ArrayList<>();
			for (final Shooter sh : shtrs.keySet()) {
				sh.item(Main.air, 8);
				final Player p = sh.getPlayer();
				if (p != null) {
					PacketUtils.sendTtlSbTtl(p, "§4Ночь", "§7Крепитесь и защищайте точки!", 50);
					Main.chgSbdTm(p.getScoreboard(), "gst", "", "§5Ночь");
					p.playSound(p.getLocation(), "cs.info.night", 10f, 1f);
				}
			}
			
			cnt = Math.max((80 - (ccl << 1)) / shtrs.size(), 1);
			Ostrov.async(() -> ApiOstrov.shuffle(spots));
			for (int i = spots.length >> 1; i > 0; i--) {
				final Mobber mb = mbbrs.get(spots[i]);
				if (mb == null) {
					new Mobber(spots[i], this).runTaskTimer(Main.plug, 10L, 10L);
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
			gst = GameState.ROUND;
			tm = (short) (ccl * 10 + 60);
			cnt = Math.max((160 - (ccl << 1)) / shtrs.size(), 1);
			ccl++;
			ApiOstrov.sendArenaData(this.name, ru.komiss77.enums.GameState.ЭКИПИРОВКА, "§7[§5CS§7]", 
				"§dВторжение", " ", "§7Игроков: §5" + shtrs.size() + "§7/§5" + this.max, "", shtrs.size());
			Inventories.updtGm(this);
			for (final Shooter sh : shtrs.keySet()) {
				chngMn(sh, 250);
				sh.item(Main.mkItm(Material.GHAST_TEAR, "§5Магазин", 10), 8);
				final LivingEntity le = sh.getEntity();
				if (sh instanceof PlShooter) {
					final Player p = (Player) le;
					if (p.getGameMode() == GameMode.SPECTATOR) {
						p.setGameMode(GameMode.SURVIVAL);
						p.teleport(Main.getNrLoc(Main.rndElmt(CTSawns), w));
					}
					PacketUtils.sendTtlSbTtl(p, "§3День", "§7Закупайтесь и ломайте спавнеры!", 50);
					Main.chgSbdTm(p.getScoreboard(), "gst", "", "§dДень");
					p.playSound(p.getLocation(), "cs.info.day", 10f, 1f);
				} else {
					if (sh.isDead()) {
						sh.teleport(le, Main.getNrLoc(Main.rndElmt(CTSawns), w));
					}
				}
				sh.getEntity().addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, tm * 20 + 10, 1));
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
				final String tt = (isDay ? "§dДень §7(" : "§5Ночь §7(") + getTime(tm, isDay ? "§d" : "§5") + "§7)";
				switch (tm) {
				case 60:
					for (final Shooter sh : shtrs.keySet()) {
						final Player p = sh.getPlayer();
						if (p != null) {
							PacketUtils.sendAcBr(p, "§7Осталась §d1 §7минута!", 30);
							Main.chgSbdTm(p.getScoreboard(), "gst", "", tt);
						}
					}
					break;
				case 30:
					for (final Shooter sh : shtrs.keySet()) {
						final Player p = sh.getPlayer();
						if (p != null) {
							PacketUtils.sendAcBr(p, "§7Осталось §d" + tm + " §7секунд!", 30);
							Main.chgSbdTm(p.getScoreboard(), "gst", "", tt);
							p.playSound(p.getLocation(), "cs.info." + (isDay ? "day30sec" : "nit30sec"), 10f, 1f);
						}
					}
					break;
				case 10:
					for (final Shooter sh : shtrs.keySet()) {
						final Player p = sh.getPlayer();
						if (p != null) {
							PacketUtils.sendAcBr(p, "§7Осталось §d" + tm + " §7секунд!", 30);
							Main.chgSbdTm(p.getScoreboard(), "gst", "", tt);
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
							Main.chgSbdTm(p.getScoreboard(), "gst", "", tt);
						}
					}
					break;
				}
				tm--;
			}
		}.runTaskTimer(Main.plug, 20, 20);
	}

	private void cntFnsh(final boolean CTwn, final String sbt, final String snd) {
		tm = 10;
		gst = GameState.FINISH;
		ApiOstrov.sendArenaData(this.name, ru.komiss77.enums.GameState.ФИНИШ, "§7[§5CS§7]", 
			"§dВторжение", " ", "§7Игроков: §5" + shtrs.size() + "§7/§5" + this.min, "", shtrs.size());
		Inventories.updtGm(this);
		for (final Entity e : w.getEntitiesByClasses(Item.class, ArmorStand.class, Turtle.class)) {
			e.remove();
		}
		
		final Iterator<BrknBlck> bi = brkn.iterator();
		while (bi.hasNext()) {
			final BrknBlck b = bi.next();
			b.getBlock().setBlockData(b.bd, false);
			bi.remove();
		}
		
		Shooter tp = null;
		for (final Shooter sh : shtrs.keySet()) {
			if (tp == null || tp.kills() < sh.kills()) {
				tp = sh;
			}
		}
		final String ttl = CTwn ? "§3Победа!" : "§4Поражение!";
		for (final Shooter sh : shtrs.keySet()) {
			if (sh instanceof PlShooter) {
				final Player p = sh.getPlayer();
				p.closeInventory();
				p.playSound(p.getLocation(), "cs.info." + snd, 10f, 1f);
				PacketUtils.sendTtlSbTtl(p, ttl, sbt, 50);
				PacketUtils.sendNmTg(sh, "§7<§d" + name + "§7> ", " §7[" + sh.kills() + 
					"-" + sh.spwnrs() + "-" + sh.deaths() + "]", Team.NA.clr);
				p.sendMessage(" \n§7Финиш: " + ttl
					+ "\n§7=-=-=-=-=-=-=-=-=-=-=-=-=-=-"
					+ "\n§7Самый кровожадный защитник:"
					+ "\n§3" + tp.name() + " §7(§5" + tp.kills() + " §7Убийств)"
					+ "\n \n§7Ваши убийства: §d" + sh.kills()
					+ "\n§7=-=-=-=-=-=-=-=-=-=-=-=-=-=-");
				ApiOstrov.addStat(p, Stat.CS_game);
				SkinQuest.tryCompleteQuest(sh, Quest.ДУША, ApiOstrov.getStat(p, Stat.CS_game));
				sh.inv().clear();
				winScore(sh, p, CTwn);
			} else {
				((BtShooter) sh).remove();
			}
		}
		
		final Iterator<TripWire> ti = tws.iterator();
		while (bi.hasNext()) {
			ti.next().rmv(this);
			bi.remove();
		}
		
		endSps();
		if (tsk != null) tsk.cancel();
		tsk = new BukkitRunnable() {
			@Override
			public void run() {
				final String t = getTime(tm, "§5");
				if (tm == 0) {
					if (tsk != null) {
						tsk.cancel();
					}
					
					end(getArena());
				} else {
					final FireworkEffect fe = FireworkEffect.builder().with(Type.STAR).withColor(Color.TEAL).withFlicker().build();
					for (final Shooter sh : shtrs.keySet()) {
						final Player p = sh.getPlayer();
						if (p != null) {
							Main.chgSbdTm(p.getScoreboard(), "tm", t, "");
							if (CTwn) {
								final Firework fw = (Firework) p.getWorld().spawnEntity(p.getEyeLocation(), EntityType.FIREWORK);
								final FireworkMeta fm = fw.getFireworkMeta();
								fm.addEffect(fe);
								fw.setFireworkMeta(fm);
							}
						}
					}
				}
				tm--;
			}
		}.runTaskTimer(Main.plug, 20, 20);
	}

	private void beginScore(final Shooter sh, final Player p) {
		final Scoreboard sb = Main.smg.getNewScoreboard();
		final Objective ob = sb.registerNewObjective("CS:GO", Criteria.DUMMY, Component.text("§7[§5CS:GO§7]"));
		ob.setDisplaySlot(DisplaySlot.SIDEBAR);
		ob.getScore("    ")
		.setScore(8);
		ob.getScore("§7Карта: §5" + name)
		.setScore(7);
		ob.getScore("   ")
		.setScore(6);
		Main.crtSbdTm(sb, "amt", "", "§7Игроков: ", "§3" + shtrs.size() + " §7чел.");
		ob.getScore("§7Игроков: ")
		.setScore(5);
		Main.crtSbdTm(sb, "sts", "", "§7Точки: ", shtrs.size() == 1 ? "§bA" : "§bA §7и §6B");
		ob.getScore("§7Точки: ")
		.setScore(4);
		ob.getScore("  ")
		.setScore(3);
		Main.crtSbdTm(sb, "rmn", "", "§7Начало через: ", "§5" + String.valueOf(tm));
		ob.getScore("§7Начало через: ")
		.setScore(2);
		ob.getScore(" ")
		.setScore(1);
		
		ob.getScore("§e   ostrov77.ru")
		.setScore(0);
		p.setScoreboard(sb);
	}
	
	private void gameScore(final Shooter sh, final Player p) {
		final Scoreboard sb = Main.smg.getNewScoreboard();
		final Objective ob = sb.registerNewObjective("CS:GO", Criteria.DUMMY, Component.text("§7[§5CS:GO§7]"));
		ob.setDisplaySlot(DisplaySlot.SIDEBAR);
		ob.getScore("§7=-=-=-=-=-=-=-=-")
		.setScore(13);
		ob.getScore("§7Карта: §5" + name)
		.setScore(12);
		ob.getScore("     ")
		.setScore(11);
		Main.crtSbdTm(sb, "gst", "", "§7Cтадия: ", (isDay ? "§dДень §7(" : "§5Ночь §7(") + getTime(tm, isDay ? "§d" : "§5") + "§7)");
		ob.getScore("§7Cтадия: ")
		.setScore(10);
		ob.getScore("    ")
		.setScore(9);
		Main.crtSbdTm(sb, "ahp", "", "§7Точка §bA §7: ", "§b" + String.valueOf(apc) + "%");
		ob.getScore("§7Точка §bA §7: ")
		.setScore(8);
		Main.crtSbdTm(sb, "bhp", "", "§7Точка §6B §7: ", shtrs.size() == 1 ? "§7Не активна!" : "§6" + String.valueOf(bpc) + "%");
		ob.getScore("§7Точка §6B §7: ")
		.setScore(7);
		ob.getScore("   ")
		.setScore(6);
		Main.crtSbdTm(sb, "amt", "", "§7Защитников: ", "§3" + getTmAmt(Team.CTs, true, true) + " §7чел.");
		ob.getScore("§7Защитников: ")
		.setScore(5);
		ob.getScore("  ")
		.setScore(4);
		Main.crtSbdTm(sb, "mn", "", "§7Монет: ", "§d" + sh.money() + " §6⛃");
		ob.getScore("§7Монет: ")
		.setScore(3);
		ob.getScore("§7=-=-=-=-=-=-=-")
		.setScore(2);
		ob.getScore(" ")
		.setScore(1);
		
		ob.getScore("§e   ostrov77.ru")
		.setScore(0);
		p.setScoreboard(sb);
		for (final Mobber m : mbbrs.values()) {
			VM.getNmsEntitygroup().colorGlow(m.ind, '4', false);
		}
		VM.getNmsEntitygroup().colorGlow(ads, 'b', false);
		VM.getNmsEntitygroup().colorGlow(ads, '6', false);
	}

	private void winScore(final Shooter sh, final Player p, final boolean isCTWn) {
		final Scoreboard sb = Main.smg.getNewScoreboard();
		final Objective ob = sb.registerNewObjective("CS:GO", Criteria.DUMMY, Component.text("§7[§5CS:GO§7]"));
		ob.setDisplaySlot(DisplaySlot.SIDEBAR);
		ob.getScore(isCTWn ? "§3Защитники §7победили!" : "§4Захватчики §7победили!")
		.setScore(12);
		ob.getScore("§7=-=-=-=-=-=-=-=-")
		.setScore(11);
		ob.getScore("§7Карта: §5" + name)
		.setScore(10);
		ob.getScore("    ")
		.setScore(9);
		ob.getScore("§7Cтадия: §dФиниш")
		.setScore(8);
		Main.crtSbdTm(sb, "tm", "§5" + String.valueOf(tm), " §7до конца!", "");
		ob.getScore(" §7до конца!")
		.setScore(7);
		ob.getScore("   ")
		.setScore(6);
		Main.crtSbdTm(sb, "amt", "", "§7Защитников: ", "§3" + getTmAmt(Team.CTs, true, true) + " §7чел.");
		ob.getScore("§7Защитников: ")
		.setScore(5);
		ob.getScore("  ")
		.setScore(4);
		ob.getScore("§7Монет: §d" + sh.money() + " §6⛃")
		.setScore(3);
		ob.getScore("§7=-=-=-=-=-=-=-")
		.setScore(2);
		ob.getScore(" ")
		.setScore(1);
		
		ob.getScore("§e   ostrov77.ru")
		.setScore(0);
		p.setScoreboard(sb);
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
					Main.chgSbdTm(pl.getScoreboard(), "amt", "", "§3" + rm + " §7чел.");
				}
			}
			if (rm == 0) {
				cntFnsh(false, "§7Все §3защитники §7убиты!", "mobwin");
			}
		} else {
			if (gst == GameState.ROUND) {
				addDth(sh);
			} else if (le.hasPotionEffect(PotionEffectType.GLOWING)) {
				le.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, tm * 20, 1));
			}
			if (sh instanceof PlShooter) {
				final Player p = (Player) le;
				Main.nrmlzPl(p, false);
				p.closeInventory();
			}
			sh.teleport(le, Main.getNrLoc(Main.rndElmt(CTSawns), w));
		}
	}

	public void addMbKll(final Shooter sh) {
		sh.killsI();
		PacketUtils.sendNmTg(sh, Team.CTs.icn + " ", " §7[" + sh.kills() + 
			"-" + sh.spwnrs() + "-" + sh.deaths() + "]", Team.CTs.clr);
		final Player p = sh.getPlayer();
		if (p != null) {
			SkinQuest.tryCompleteQuest(sh, Quest.ДЮНА, sh.kills());
			ApiOstrov.addStat(p, Stat.CS_mobs);
		}
	}

	public void addSpDfs(final Shooter sh) {
		sh.spwnrsI();
		PacketUtils.sendNmTg(sh, Team.CTs.icn + " ", " §7[" + sh.kills() + 
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
		PacketUtils.sendNmTg(sh, Team.CTs.icn + " ", " §7[" + sh.kills() + 
			"-" + sh.spwnrs() + "-" + sh.deaths() + "]", Team.CTs.clr);
		final Player p = sh.getPlayer();
		if (p != null) {
			ApiOstrov.addStat(p, Stat.CS_death);
		}
	}
	
	public void rmvSpnr(final Shooter pl) {
		final Mobber mb = dfs.remove(pl);
		addSpDfs(pl);
		chngMn(pl, 150);
		if (mb != null) dieSpnr(mb);
	}
	
	public void dieSpnr(final Mobber mb) {
		mb.setDef();
		for (final Entry<Shooter, Mobber> e : dfs.entrySet()) {
			final Player p = e.getKey().getPlayer();
			if (p != null) {
				if (e.getValue().ind.getEntityId() == mb.ind.getEntityId()) {
					p.closeInventory();
				}
			}
		}
		
		for (final Mobber m : mbbrs.values()) {
			if (m.ind.isGlowing()) return;
		}
		cntFnsh(true, "§7Все §4спавнеры §7обезврежены!", "despawner");
	}

	public void wrngWire(final Shooter sh) {
		for (final Entry<Shooter, Mobber> e : dfs.entrySet()) {
			if (e.getKey().equals(sh)) {
				e.getValue().spwnMb();
			}
		}
	}
	
	public void endSps() {
		for (final Mobber m : mbbrs.values()) {
			if (m != null) {
				m.ind.remove();
				m.ind.getLocation().getBlock().setType(Material.AIR, false);
				m.cancel();
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
						Main.chgSbdTm(p.getScoreboard(), "ahp", "", "§b" + String.valueOf(apc) + "%");
					}
				}
			} else {
				final Block b = ads.getLocation().getBlock();
				b.getWorld().playSound(b.getLocation(), Sound.BLOCK_CONDUIT_DEACTIVATE, 40f, 2f);
				cntFnsh(false, "§7Точка §bA §7разрушена!", "sitebreak");
				b.setType(Material.AIR);
				final int X = b.getX();
				final int Y = b.getY();
				final int Z = b.getZ();
				final HashSet<Block> cls = new HashSet<>();
				b.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, b.getLocation(), 20, 5d, 5d, 5d);
				b.getWorld().playSound(b.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 5f, 0.8f);
				for (int x = X - 5; x <= X + 5; x++) {
					for (int y = Y - 5; y <= Y + 5; y++) {
						for (int z = Z - 5; z <= Z + 5; z++) {
							final Block f = b.getRelative(X - x, Y - y, Z - z);
							final int bnd = (X - x) * (X - x) + (Y - y) * (Y - y) + (Z - z) * (Z - z);
							if (bnd > 0 && f.getType().isAir() && f.getRelative(BlockFace.DOWN).getType().isOccluding() && Main.srnd.nextInt(bnd) < 4) {
								for (final Player p : b.getWorld().getPlayers()) {
									p.sendBlockChange(f.getLocation(), Material.FIRE.createBlockData());
									cls.add(f);
								} 
							} else if (f.getType().isOccluding() && Main.srnd.nextInt(bnd) < 6) {
								for (final Player p : b.getWorld().getPlayers()) {
									p.sendBlockChange(f.getLocation(), Material.COAL_BLOCK.createBlockData());
									cls.add(f);
								} 
							} 
						} 
					} 
				}
				new BukkitRunnable() {
					public void run() {
						for (final Block b : cls) {
							for (final Player p : b.getWorld().getPlayers()) {
								p.sendBlockChange(b.getLocation(), b.getBlockData());
							}
						} 
					}
				}.runTaskLater(Main.plug, 200L);
			}
		} else {
			bpc -= dmg;
			if (bpc > 0) {
				bds.text(TCUtils.format("§7Точка §6B§7: §d" + bpc + "%"));
				for (final Shooter sh : shtrs.keySet()) {
					final Player p = sh.getPlayer();
					if (p != null) {
						p.playSound(bds.getLocation(), Sound.BLOCK_SCULK_SENSOR_CLICKING, 40f, 1f);
						PacketUtils.sendSbTtl(p, "§6B §7атакована : §6" + (bpc + dmg) + " §7=-> §6" + bpc, 30);
						Main.chgSbdTm(p.getScoreboard(), "bhp", "", "§6" + String.valueOf(bpc) + "%");
					}
				}
			} else {
				final Block b = bds.getLocation().getBlock();
				b.getWorld().playSound(b.getLocation(), Sound.BLOCK_CONDUIT_DEACTIVATE, 40f, 2f);
				cntFnsh(false, "§7Точка §6B §7разрушена!", "sitebreak");
				b.setType(Material.AIR);
				final int X = b.getX();
				final int Y = b.getY();
				final int Z = b.getZ();
				final HashSet<Block> cls = new HashSet<>();
				b.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, b.getLocation(), 20, 5d, 5d, 5d);
				b.getWorld().playSound(b.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 5f, 0.8f);
				for (int x = X - 5; x <= X + 5; x++) {
					for (int y = Y - 5; y <= Y + 5; y++) {
						for (int z = Z - 5; z <= Z + 5; z++) {
							final Block f = b.getRelative(X - x, Y - y, Z - z);
							final int bnd = (X - x) * (X - x) + (Y - y) * (Y - y) + (Z - z) * (Z - z);
							if (bnd > 0 && f.getType().isAir() && f.getRelative(BlockFace.DOWN).getType().isOccluding() && Main.srnd.nextInt(bnd) < 4) {
								for (final Player p : b.getWorld().getPlayers()) {
									p.sendBlockChange(f.getLocation(), Material.FIRE.createBlockData());
									cls.add(f);
								} 
							} else if (f.getType().isOccluding() && Main.srnd.nextInt(bnd) < 6) {
								for (final Player p : b.getWorld().getPlayers()) {
									p.sendBlockChange(f.getLocation(), Material.COAL_BLOCK.createBlockData());
									cls.add(f);
								} 
							} 
						} 
					} 
				}
				new BukkitRunnable() {
					public void run() {
						for (final Block b : cls) {
							for (final Player p : b.getWorld().getPlayers()) {
								p.sendBlockChange(b.getLocation(), b.getBlockData());
							}
						} 
					}
				}.runTaskLater(Main.plug, 200L);
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
		for (final Entry<XYZ, Mobber> en : mbbrs.entrySet()) {
			if (alive && !en.getValue().ind.isGlowing()) continue;
			final int d = to.distSq(en.getKey());
			if (d < dd) {
				m = en.getValue();
				dd = d;
			}
		}
		return m;
	}
	
	public Mobber getRndMbbr(final boolean alive) {
		final Mobber m = mbbrs.get(ApiOstrov.rndElmt(spots));
		return m == null || (alive && !m.ind.isGlowing()) ? null : m;
	}
}
