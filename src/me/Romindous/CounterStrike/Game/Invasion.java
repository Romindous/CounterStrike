package me.Romindous.CounterStrike.Game;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Item;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Turtle;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import me.Romindous.CounterStrike.Main;
import me.Romindous.CounterStrike.Listeners.MainLis;
import me.Romindous.CounterStrike.Objects.Shooter;
import me.Romindous.CounterStrike.Objects.Game.GameState;
import me.Romindous.CounterStrike.Objects.Game.GameType;
import me.Romindous.CounterStrike.Objects.Game.PlShooter;
import me.Romindous.CounterStrike.Objects.Game.TripWire;
import me.Romindous.CounterStrike.Objects.Loc.BrknBlck;
import me.Romindous.CounterStrike.Objects.Loc.Spot;
import me.Romindous.CounterStrike.Objects.Mobs.Mobber;
import me.Romindous.CounterStrike.Objects.Skins.Quest;
import me.Romindous.CounterStrike.Objects.Skins.SkinQuest;
import me.Romindous.CounterStrike.Utils.Inventories;
import me.Romindous.CounterStrike.Utils.PacketUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.core.BaseBlockPosition;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.projectile.EntityShulkerBullet;
import ru.komiss77.ApiOstrov;
import ru.komiss77.enums.Stat;

public class Invasion extends Arena {
	
	public final HashMap<Integer, WeakReference<Mob>> TMbs;
	public final HashSet<BaseBlockPosition> spnrs;
	public final LinkedHashMap<Shooter, Mobber> dfs;
	public final Mobber[] sis;
	public final EntityShulkerBullet ast;
	public final EntityShulkerBullet bst;
	
	public byte apc;
	public byte bpc;
	public byte ccl;
	public int cnt;
	public boolean isDay;
	
	public Invasion(final String name, final byte min, final byte max, final BaseBlockPosition[] TSps, final BaseBlockPosition[] CTSps, final World w, final BaseBlockPosition ast, final BaseBlockPosition bst, final Spot[] spots, final boolean rnd, final boolean bots) {
		super(name, min, max, TSps, CTSps, w, spots, rnd, bots);
		this.TMbs = new HashMap<>();
		this.spnrs = new HashSet<>();
		this.dfs = new LinkedHashMap<>();
		this.sis = new Mobber[TPss.length >> 1];
		this.isDay = true;
		
		final net.minecraft.world.level.World wm = PacketUtils.getNMSWrld(w);
		final EntityShulkerBullet as = new EntityShulkerBullet(EntityTypes.aC, wm);
		as.m(true);
		as.e(true);
		as.j(true);
		as.d(true);
		as.b(IChatBaseComponent.a("§7Точка §bA§7: §d100%"));
		as.n(false);
		as.setPosRaw(ast.u() + 0.5d, ast.v() - 0.4d, ast.w() + 0.5d, false);
		wm.addFreshEntity(as, SpawnReason.CUSTOM);
		this.ast = as;
		final EntityShulkerBullet bs = new EntityShulkerBullet(EntityTypes.aC, wm);
		bs.m(true);
		bs.e(true);
		bs.j(true);
		bs.d(true);
		bs.b(IChatBaseComponent.a("§7Точка §6B§7: §d100%"));
		bs.n(false);
		bs.setPosRaw(bst.u() + 0.5d, bst.v() - 0.4d, bst.w() + 0.5d, false);
		wm.addFreshEntity(bs, SpawnReason.CUSTOM);
		this.bst = bs;
		
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
		if (p == null) {
			
		} else {
			Main.nrmlzPl(p, true);
			switch (gst) {
			case WAITING:
			case BEGINING:
				shtrs.put(sh, Team.CTs);
				sh.item(Main.mkItm(Material.GHAST_TEAR, "§5Магазин", 10), 6);
				sh.item(Main.mkItm(Material.SLIME_BALL, "§cВыход", 10), 8);
				p.teleport(Main.getNrLoc(Main.rndElmt(CTSps), w));
				p.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, tm * 20, 1));
				beginScore(sh, p);
				Main.shwHdPls(p);
				PacketUtils.sendNmTg(sh, "§7<§d" + name + "§7> ", " §7[-.-]", Team.CTs.clr);
				for (final Shooter s : shtrs.keySet()) {
					final Player pl = s.getPlayer();
					if (pl == null) {
						
					} else {
						pl.sendMessage(Main.prf() + "§7Игрок §5" + sh.name() + " §7зашел на карту!");
						Main.chgSbdTm(pl.getScoreboard(), "amt", "", "§3" + shtrs.size() + " §7чел.");
						Main.chgSbdTm(pl.getScoreboard(), "sts", "", shtrs.size() == 1 ? "§bA" : "§bA §7и §6B");
						//Main.chgSbdTm(p.getScoreboard(), "tamt", "", "§4" + String.valueOf(getTmAmt(Team.Ts)) + (e.getValue() == Team.Ts ? " §7чел. §8✦ Вы" : " §7чел."));
						//Main.chgSbdTm(p.getScoreboard(), "ctamt", "", "§3" + String.valueOf(getTmAmt(Team.CTs)) + (e.getValue() == Team.CTs ? " §7чел. §8✦ Вы" : " §7чел."));
						PacketUtils.sendAcBr(pl, "§7Игроков: §5" + shtrs.size() + " §7из §5" + max + " §7(макс)!", 30);
						PacketUtils.sendNmTg(PacketUtils.getNMSPl(p).networkManager, pl, "§7<§d" + name + "§7> ", " §7[-.-]", true, Team.CTs.clr);
					}
				}
				break;
			case ROUND:
				shtrs.put(sh, Team.CTs);
				chngMn(sh, 250);
				addToGm(p, Team.CTs);
				gameScore(sh, p);
				cnt = Math.max(((isDay ? 80 : 48) - ccl) / shtrs.size(), 1);
				PacketUtils.sendNmTg(sh, Team.CTs.icn + " ", " §7[0-0-0]", Team.CTs.clr);
				for (final Shooter s : shtrs.keySet()) {
					final Player pl = s.getPlayer();
					if (pl == null) {
						
					} else {
						pl.sendMessage(Main.prf() + "§7Игрок §5" + sh.name() + " §7зашел играть!");
						Main.chgSbdTm(pl.getScoreboard(), "amt", "", "§3" + getAlive() + " §7чел.");
						Main.chgSbdTm(pl.getScoreboard(), "bhp", "", "§6" + String.valueOf(bpc) + "%");
						PacketUtils.sendNmTg(PacketUtils.getNMSPl(p).networkManager, pl, Team.CTs.icn + " ", " §7[0-0-0]", true, Team.CTs.clr);
					}
				}
				if (shtrs.size() > 1) {
					bst.i(true);
					bst.n(true);
					Main.getBBlock(bst.da().c(0, 1, 0), w).setType(Material.WARPED_PRESSURE_PLATE, false);
				}
				for (final TripWire tw : tws) {
					tw.shwNd(PacketUtils.getNMSPl(p).b);
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
		p.teleport(Main.getNrLoc(Main.rndElmt(CTSps), w));
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
		if (p == null) {
			
		} else {
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
							Main.chgSbdTm(pl.getScoreboard(), "amt", "", "§3" + getAlive() + " §7чел.");
						}
					}
				}
				break;
			case BUYTIME:
			case ROUND:
			case ENDRND:
				for (final TripWire tw : tws) {
					PacketUtils.getNMSPl(p).b.a(new PacketPlayOutEntityDestroy(tw.eif.ae()));
				}
				final int sz = getAmt(true, true);
				if (sz == 0) {
					if (tsk != null) {
						tsk.cancel();
					}
					endSps();
					end(this);
				} else if (sz == 1) {
					cnt = Math.max(((isDay ? 80 : 48) - ccl) / shtrs.size(), 1);
					for (final Shooter s : shtrs.keySet()) {
						final Player pl = s.getPlayer();
						if (pl != null) {
							pl.sendMessage(Main.prf() + "§7Игрок §5" + sh.name() + " §7вышел из игры!");
							Main.chgSbdTm(pl.getScoreboard(), "amt", "", "§3" + getAlive() + " §7чел.");
							Main.chgSbdTm(pl.getScoreboard(), "bhp", "", "§7Не активна!");
						}
					}
					bst.i(false);
					bst.n(false);
					Main.getBBlock(bst.da().c(0, 1, 0), w).setType(Material.AIR, false);
				} else {
					cnt = Math.max(((isDay ? 80 : 48) - ccl) / shtrs.size(), 1);
					for (final Shooter s : shtrs.keySet()) {
						final Player pl = s.getPlayer();
						if (pl != null) {
							pl.sendMessage(Main.prf() + "§7Игрок §5" + sh.name() + " §7вышел из игры!");
							Main.chgSbdTm(pl.getScoreboard(), "amt", "", "§3" + getAlive() + " §7чел.");
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
		ApiOstrov.sendArenaData(this.name, ru.komiss77.enums.GameState.СТАРТ, "§7[§5CS§7]", "§dВторжение", " ", "§7Игроков: §5" + shtrs.size() + "§7/§5" + this.min, "", shtrs.size());
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
		setSpwnrs();
		for (final Shooter sh : shtrs.keySet()) {
			sh.money(0);
			final Player p = sh.getPlayer();
			if (p == null) {
				
			} else {
				Main.nrmlzPl(p, true);
				gameScore(sh, p);
				p.teleport(Main.getNrLoc(Main.rndElmt(CTSps), w));
				PacketUtils.sendNmTg(sh, Team.CTs.icn + " ", " §7[" + sh.kills() + 
					"-" + sh.spwnrs() + "-" + sh.deaths() + "]", Team.CTs.clr);
				sh.item(Main.mkItm(Material.GOLD_NUGGET, "§eКусачки §f\u9268", 10), 7);
				sh.item(Main.mkItm(Material.BLAZE_ROD, "§fНож \u9298", 10), 2);
			}
		}
		ast.i(true);
		ast.n(true);
		Main.getBBlock(ast.da().c(0, 1, 0), w).setType(Material.WARPED_PRESSURE_PLATE, false);
		if (shtrs.size() > 1) {
			bst.i(true);
			bst.n(true);
			Main.getBBlock(bst.da().c(0, 1, 0), w).setType(Material.WARPED_PRESSURE_PLATE, false);
		}
		swpDayNght();
	}

	private void setSpwnrs() {
		if (shtrs.size() == 0) {
			if (tsk != null) {
				tsk.cancel();
			}
			endSps();
			end(this);
		} else {
			cnt = Math.max((80 - ccl) / shtrs.size(), 1);
			final net.minecraft.world.level.World wm = PacketUtils.getNMSWrld(w);
			for (int i = sis.length - 1; i >= 0; i--) {
				sis[i] = new Mobber(TPss[i], wm, this);
				sis[i].runTaskTimer(Main.plug, 10L, 10L);
			}
		}
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
			cnt = Math.max((48 - ccl) / shtrs.size(), 1);
			ApiOstrov.sendArenaData(this.name, ru.komiss77.enums.GameState.ИГРА, "§7[§5CS§7]", "§dВторжение", " ", "§7Игроков: §5" + shtrs.size() + "§7/§5" + this.max, "", shtrs.size());
			Inventories.updtGm(this);
			final ArrayList<Player> pls = new ArrayList<>();
			for (final Shooter sh : shtrs.keySet()) {
				sh.item(Main.air, 8);
				final Player p = sh.getPlayer();
				if (p != null) {
					PacketUtils.sendTtlSbTtl(p, "§4Ночь", "§7Крепитесь и защищайте точки!", 50);
					Main.chgSbdTm(p.getScoreboard(), "gst", "", "§5Ночь");
					p.playSound(p.getLocation(), "cs.info.night", 10f, 1f);
					pls.add(p);
				}
			}
			for (final Mobber mb : sis) {
				mb.ind.i(true);
				final Block b = Main.getBBlock(mb.ind.da(), w);
				final CreatureSpawner cs;
				if (b.getType() == Material.SPAWNER) {
					cs = (CreatureSpawner) b.getState();
					switch (cs.getSpawnedType()) {
					case ZOMBIE_VILLAGER:
						cs.setSpawnedType(EntityType.STRAY);
						break;
					case STRAY:
						cs.setSpawnedType(EntityType.VINDICATOR);
						break;
					case VINDICATOR:
						cs.setSpawnedType(EntityType.PIGLIN_BRUTE);
						break;
					default:
						break;
					}
				} else {
					b.setType(Material.SPAWNER, false);
					cs = (CreatureSpawner) b.getState();
					cs.setSpawnedType(EntityType.ZOMBIE_VILLAGER);
				}
				cs.setSpawnCount(0);
				cs.update();
				final Location lc = new Location(b.getWorld(), b.getX(), b.getY() - 1, b.getZ());
				for (final Player p : pls) {
					p.sendBlockChange(lc, Mobber.stnd);
				}
			}
		} else {
			//day
			isDay = true;
			gst = GameState.ROUND;
			tm = (short) (ccl * 10 + 60);
			cnt = Math.max((80 - ccl) / shtrs.size(), 1);
			ccl++;
			ApiOstrov.sendArenaData(this.name, ru.komiss77.enums.GameState.ЭКИПИРОВКА, "§7[§5CS§7]", "§dВторжение", " ", "§7Игроков: §5" + shtrs.size() + "§7/§5" + this.max, "", shtrs.size());
			Inventories.updtGm(this);
			for (final Shooter sh : shtrs.keySet()) {
				chngMn(sh, 250);
				final Player p = sh.getPlayer();
				if (p == null) {
					
				} else {
					if (p.getGameMode() == GameMode.SPECTATOR) {
						p.setGameMode(GameMode.SURVIVAL);
						p.teleport(Main.getNrLoc(Main.rndElmt(CTSps), w));
					}
					p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, tm * 20 + 10, 1));
					sh.item(Main.mkItm(Material.GHAST_TEAR, "§5Магазин", 10), 8);
					PacketUtils.sendTtlSbTtl(p, "§3День", "§7Закупайтесь и ломайте спавнеры!", 50);
					Main.chgSbdTm(p.getScoreboard(), "gst", "", "§dДень");
					p.playSound(p.getLocation(), "cs.info.day", 10f, 1f);
				}
			}
			for (final Monster mn : w.getEntitiesByClass(Monster.class)) {
				mn.setFireTicks(10);
				mn.damage(100);
			}
		}
		
		tsk = new BukkitRunnable() {
			@Override
			public void run() {
				if (w.getTime() % 24000 != (isDay ? 6000L : 18000L)) {
					w.setTime(w.getTime() + 1000L);
				}
				final String tt = (isDay ? "§dДень §7(" : "§5Ночь §7(") + getTime(tm, isDay ? ChatColor.LIGHT_PURPLE : ChatColor.DARK_PURPLE) + "§7)";
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
		ApiOstrov.sendArenaData(this.name, ru.komiss77.enums.GameState.ФИНИШ, "§7[§5CS§7]", "§dВторжение", " ", "§7Игроков: §5" + shtrs.size() + "§7/§5" + this.min, "", shtrs.size());
		Inventories.updtGm(this);
		for (final Entity e : w.getEntitiesByClasses(Item.class, ArmorStand.class, Turtle.class)) {
			e.remove();
		}
		for (final BrknBlck b : brkn) {
			b.getBlock().setBlockData(b.bd, false);
		}
		Shooter tp = null;
		for (final Shooter sh : shtrs.keySet()) {
			if (tp == null || tp.kills() < sh.kills()) {
				tp = sh;
			}
		}
		final String ttl = CTwn ? "§3Победа!" : "§4Поражение!";
		for (final Shooter sh : shtrs.keySet()) {
			final Player p = sh.getPlayer();
			if (p != null) {
				p.closeInventory();
				p.playSound(p.getLocation(), "cs.info." + snd, 10f, 1f);
				PacketUtils.sendTtlSbTtl(p, ttl, sbt, 50);
				PacketUtils.sendNmTg(sh, "§7<§d" + name + "§7> ", " §7[" + sh.kills() + 
					"-" + sh.spwnrs() + "-" + sh.deaths() + "]", Team.NA.clr);
				ApiOstrov.moneyChange(sh.name(), sh.kills() >> 2, "Убийства");
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
			}
		}
		for (final TripWire tw : tws) {
			tw.rmv(this);
		}
		tws.clear();
		endSps();
		if (tsk != null) {
			tsk.cancel();
		}
		tsk = new BukkitRunnable() {
			@Override
			public void run() {
				final String t = getTime(tm, ChatColor.DARK_PURPLE);
				if (tm == 0) {
					if (tsk != null) {
						tsk.cancel();
					}
					
					end(getArena());
				} else {
					final FireworkEffect fe = FireworkEffect.builder().with(Type.STAR).withColor(Color.TEAL).withFlicker().build();
					for (final Shooter sh : shtrs.keySet()) {
						final Player p = sh.getPlayer();
						if (p == null) {
							
						} else {
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
		final Objective ob = sb.registerNewObjective("CS:GO", "", Component.text("§7[§5CS:GO§7]"));
		ob.setDisplaySlot(DisplaySlot.SIDEBAR);
		ob.getScore("    ")
		.setScore(8);
		ob.getScore("§7Карта: §5" + name)
		.setScore(7);
		ob.getScore("   ")
		.setScore(6);
		Main.crtSbdTm(sb, "amt", "", "§7Игроков: ", "§3" + String.valueOf(shtrs.size()) + " §7чел.");
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
		
		ob.getScore("§e   ostrov77.su")
		.setScore(0);
		p.setScoreboard(sb);
	}
	
	private void gameScore(final Shooter sh, final Player p) {
		final Scoreboard sb = Main.smg.getNewScoreboard();
		final org.bukkit.scoreboard.Team t = sb.registerNewTeam("ind");
		t.color(NamedTextColor.DARK_RED);
		final org.bukkit.scoreboard.Team a = sb.registerNewTeam("ast");
		a.color(NamedTextColor.AQUA);
		final org.bukkit.scoreboard.Team b = sb.registerNewTeam("bst");
		b.color(NamedTextColor.GOLD);
		final Objective ob = sb.registerNewObjective("CS:GO", "", Component.text("§7[§5CS:GO§7]"));
		ob.setDisplaySlot(DisplaySlot.SIDEBAR);
		ob.getScore("§7=-=-=-=-=-=-=-=-")
		.setScore(13);
		ob.getScore("§7Карта: §5" + name)
		.setScore(12);
		ob.getScore("     ")
		.setScore(11);
		Main.crtSbdTm(sb, "gst", "", "§7Cтадия: ", (isDay ? "§dДень §7(" : "§5Ночь §7(") + getTime(tm, isDay ? ChatColor.LIGHT_PURPLE : ChatColor.DARK_PURPLE) + "§7)");
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
		Main.crtSbdTm(sb, "amt", "", "§7Защитников: ", "§3" + getAlive() + " §7чел.");
		ob.getScore("§7Защитников: ")
		.setScore(5);
		ob.getScore("  ")
		.setScore(4);
		Main.crtSbdTm(sb, "mn", "", "§7Монет: ", "§d" + String.valueOf(sh.money()) + " §6⛃");
		ob.getScore("§7Монет: ")
		.setScore(3);
		ob.getScore("§7=-=-=-=-=-=-=-")
		.setScore(2);
		ob.getScore(" ")
		.setScore(1);
		
		ob.getScore("§e   ostrov77.su")
		.setScore(0);
		p.setScoreboard(sb);
		for (final Mobber m : sis) {
			t.addEntry(m.ind.cp().toString());
		}
		a.addEntry(ast.cp().toString());
		b.addEntry(bst.cp().toString());
	}

	private void winScore(final Shooter sh, final Player p, final boolean isCTWn) {
		final Scoreboard sb = Main.smg.getNewScoreboard();
		final Objective ob = sb.registerNewObjective("CS:GO", "", Component.text("§7[§5CS:GO§7]"));
		ob.setDisplaySlot(DisplaySlot.SIDEBAR);
		if (isCTWn) {
			ob.getScore("§3Защитники §7победили!")
			.setScore(12);
		} else {
			ob.getScore("§4Захватчики §7победили!")
			.setScore(12);
		}
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
		Main.crtSbdTm(sb, "amt", "", "§7Защитников: ", "§3" + getAlive() + " §7чел.");
		ob.getScore("§7Защитников: ")
		.setScore(5);
		ob.getScore("  ")
		.setScore(4);
		ob.getScore("§7Монет: §d" + String.valueOf(sh.money()) + " §6⛃")
		.setScore(3);
		ob.getScore("§7=-=-=-=-=-=-=-")
		.setScore(2);
		ob.getScore(" ")
		.setScore(1);
		
		ob.getScore("§e   ostrov77.su")
		.setScore(0);
		p.setScoreboard(sb);
	}

	private byte getAlive() {
		byte n = 0;
		for (final Shooter sh : shtrs.keySet()) {
			final Player p = sh.getPlayer();
			if (p == null) {
				
			} else if (p.getGameMode() == GameMode.SURVIVAL) {
				n++;
			}
		}
		return n;
	}

	@Override
	public void killSh(final Shooter sh) {
		final Player p = sh.getPlayer();
		if (p == null) {
			
		} else {
			//p.sendMessage("1");
			p.closeInventory();
			if (gst == GameState.ROUND && !isDay) {
				addDth(sh);
				p.setGameMode(GameMode.SPECTATOR);
				final byte rm = getAlive();
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
					Main.nrmlzPl(p, false);
				} else if (p.hasPotionEffect(PotionEffectType.GLOWING)) {
					Main.nrmlzPl(p, false);
					p.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, tm * 20, 1));
				} else {
					Main.nrmlzPl(p, false);
				}
				p.teleport(Main.getNrLoc(Main.rndElmt(CTSps), w));
			}
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
			PacketUtils.sendNmTg(sh, Team.CTs.icn + " ", " §7[" + sh.kills() + 
				"-" + sh.spwnrs() + "-" + sh.deaths() + "]", Team.CTs.clr);
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
		if (mb != null) {
			Main.getBBlock(mb.ind.da(), w).setType(Material.CRYING_OBSIDIAN, false);
			mb.ind.i(false);
		}
		for (final Entry<Shooter, Mobber> e : dfs.entrySet()) {
			final Player p = e.getKey().getPlayer();
			if (p != null) {
				if (e.getValue().ind.da().equals(mb.ind.da())) {
					p.closeInventory();
				}
			}
		}
		for (final Mobber m : sis) {
			if (m.ind.bW()) {
				return;
			}
		}
		cntFnsh(true, "§7Все §4спавнеры §7обезврежены!", "despawner");
	}

	public void wrngWire(final Shooter sh) {
		for (final Entry<Shooter, Mobber> e : dfs.entrySet()) {
			if (e.getKey().equals(sh)) {
				e.getValue().spwnMb(Main.getBBlock(e.getValue().ind.da(), w));
			}
		}
	}
	
	public void endSps() {
		for (final Mobber m : sis) {
			if (m != null) {
				Main.getBBlock(m.ind.da(), w).setType(Material.AIR, false);
				m.ind.a(RemovalReason.a);
				m.cancel();
			}
		}
		Main.getBBlock(ast.da().c(0, 1, 0), w).setType(Material.AIR, false);
		ast.a(RemovalReason.a);
		Main.getBBlock(bst.da().c(0, 1, 0), w).setType(Material.AIR, false);
		bst.a(RemovalReason.a);
	}

	public void hrtSt(final boolean isA, final char l) {
		final byte dmg;
		switch (l) {
		case 'Z':
		default:
			dmg = 5;
			break;
		case 'S':
			dmg = 8;
			break;
		case 'V':
			dmg = 12;
			break;
		case 'P':
			dmg = 15;
			break;
		}
		if (isA) {
			apc -= dmg;
			if (apc > 0) {
				ast.getBukkitEntity().setCustomName("§7Точка §bA§7: §d" + apc + "%");
				for (final Shooter sh : shtrs.keySet()) {
					final Player p = sh.getPlayer();
					if (p != null) {
						p.playSound(ast.getBukkitEntity().getLocation(), Sound.BLOCK_SCULK_SENSOR_CLICKING, 40f, 1f);
						PacketUtils.sendSbTtl(p, "§bA §7атакована : §b" + (apc + dmg) + " §7=-> §b" + apc, 30);
						Main.chgSbdTm(p.getScoreboard(), "ahp", "", "§b" + String.valueOf(apc) + "%");
					}
				}
			} else {
				final Block b = Main.getBBlock(ast.da().c(0, 1, 0), w);
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
				bst.getBukkitEntity().setCustomName("§7Точка §6B§7: §d" + bpc + "%");
				for (final Shooter sh : shtrs.keySet()) {
					final Player p = sh.getPlayer();
					if (p != null) {
						p.playSound(bst.getBukkitEntity().getLocation(), Sound.BLOCK_SCULK_SENSOR_CLICKING, 40f, 1f);
						PacketUtils.sendSbTtl(p, "§6B §7атакована : §6" + (bpc + dmg) + " §7=-> §6" + bpc, 30);
						Main.chgSbdTm(p.getScoreboard(), "bhp", "", "§6" + String.valueOf(bpc) + "%");
					}
				}
			} else {
				final Block b = Main.getBBlock(bst.da().c(0, 1, 0), w);
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
	
	public int getAmt(final boolean bots, final boolean alv) {
		int n = 0;
		for (final Entry<Shooter, Team> e : shtrs.entrySet()) {
			if (alv && e.getKey().isDead()) continue;
			if (e.getKey() instanceof PlShooter || bots) {
				n++;
			} 
		}
		return n;
	}
	
	@Override
	public GameType getType() {
		return GameType.INVASION;
	}
}
