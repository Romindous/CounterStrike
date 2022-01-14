package me.Romindous.CounterStrike.Game;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Turtle;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import com.mojang.datafixers.util.Pair;

import me.Romindous.CounterStrike.Main;
import me.Romindous.CounterStrike.Enums.GameState;
import me.Romindous.CounterStrike.Listeners.MainLis;
import me.Romindous.CounterStrike.Mobs.Mobber;
import me.Romindous.CounterStrike.Objects.BrknBlck;
import me.Romindous.CounterStrike.Objects.Shooter;
import me.Romindous.CounterStrike.Objects.TripWire;
import me.Romindous.CounterStrike.Utils.Inventories;
import me.Romindous.CounterStrike.Utils.PacketUtils;
import net.minecraft.core.BaseBlockPosition;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.projectile.EntityShulkerBullet;
import ru.komiss77.ApiOstrov;
import ru.komiss77.enums.Stat;

public class Invasion extends Arena {
	
	public final HashSet<UUID> TMbs;
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
	
	public Invasion(final String name, final byte min, final byte max, final BaseBlockPosition[] TSps, final BaseBlockPosition[] CTSps, final World w, final BaseBlockPosition ast, final BaseBlockPosition bst) {
		super(name, min, max, TSps, CTSps, w);
		this.TMbs = new HashSet<>();
		this.spnrs = new HashSet<>();
		this.dfs = new LinkedHashMap<>();
		this.sis = new Mobber[TSps.length];
		this.isDay = true;
		
		final net.minecraft.world.level.World wm = PacketUtils.getNMSWrld(w.getName());
		final EntityShulkerBullet as = new EntityShulkerBullet(EntityTypes.az, wm);
		as.m(true);
		as.e(true);
		as.j(true);
		as.d(true);
		as.a(IChatBaseComponent.a("§7Точка §bA§7: §d100%"));
		as.n(false);
		as.setPosRaw(ast.u() + 0.5d, ast.v() - 0.4d, ast.w() + 0.5d, false);
		wm.addFreshEntity(as, SpawnReason.CUSTOM);
		this.ast = as;
		final EntityShulkerBullet bs = new EntityShulkerBullet(EntityTypes.az, wm);
		bs.m(true);
		bs.e(true);
		bs.j(true);
		bs.d(true);
		bs.a(IChatBaseComponent.a("§7Точка §6B§7: §d100%"));
		bs.n(false);
		bs.setPosRaw(bst.u() + 0.5d, bst.v() - 0.4d, bst.w() + 0.5d, false);
		wm.addFreshEntity(bs, SpawnReason.CUSTOM);
		this.bst = bs;
		
		cntBeg();
	}

	public BukkitTask getTask() {
		return tsk;
	}
	
	public static Invasion getMobInvasion(final UUID mb) {
		for (final Arena ar : Main.actvarns) {
			if (ar instanceof Invasion) {
				if (((Invasion) ar).TMbs.contains(mb)) {
					return (Invasion) ar;
				}
			}
		}
		return null;
	}

	@Override
	public boolean addPl(final Shooter sh) {
		sh.kls = 0;
		sh.dths = 0;
		sh.money = 0;
		Main.shtrs.remove(sh);
		Main.nrmlzPl((Player) sh.inv.getHolder(), true);
		switch (gst) {
		case WAITING:
		case BEGINING:
			shtrs.put(sh, Team.CTs);
			sh.inv.setItem(6, Main.mkItm(Material.GHAST_TEAR, "§5Магазин", 1));
			sh.inv.setItem(8, Main.mkItm(Material.SLIME_BALL, "§cВыход", 1));
			sh.inv.getHolder().teleport(Main.getNrLoc(CTSps[Main.srnd.nextInt(CTSps.length)], w));
			sh.inv.getHolder().addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, tm * 20, 1));
			beginScore(sh);
			Main.shwHdPls((Player) sh.inv.getHolder());
			PacketUtils.sendNmTg(new Pair<Shooter, Arena>(sh, this), "§7<§d" + name + "§7> ", " §7[-.-]", Team.CTs.clr);
			for (final Shooter s : shtrs.keySet()) {
				final Player p = (Player) s.inv.getHolder();
				p.sendMessage(Main.prf() + "§7Игрок §5" + sh.nm + " §7зашел на карту!");
				Main.chgSbdTm(p.getScoreboard(), "amt", "", "§3" + shtrs.size() + " §7чел.");
				Main.chgSbdTm(p.getScoreboard(), "sts", "", shtrs.size() == 1 ? "§bA" : "§bA §7и §6B");
				//Main.chgSbdTm(p.getScoreboard(), "tamt", "", "§4" + String.valueOf(getTmAmt(Team.Ts)) + (e.getValue() == Team.Ts ? " §7чел. §8✦ Вы" : " §7чел."));
				//Main.chgSbdTm(p.getScoreboard(), "ctamt", "", "§3" + String.valueOf(getTmAmt(Team.CTs)) + (e.getValue() == Team.CTs ? " §7чел. §8✦ Вы" : " §7чел."));
				PacketUtils.sendAcBr(p, "§7Игроков: §5" + shtrs.size() + " §7из §5" + max + " §7(макс)!", 30);
				PacketUtils.sendRvsNmTg(Main.ds.bg().a(sh.nm).b, Team.CTs, Main.ds.bg().a(p.getName()), Team.CTs, "§7<§d" + name + "§7> ", " §7[-.-]", Team.CTs.clr);
			}
			break;
		case ROUND:
			final Player pl = (Player) sh.inv.getHolder();
			shtrs.put(sh, Team.CTs);
			addToGm(pl, Team.CTs);
			gameScore(sh);
			cnt = Math.max(((isDay ? 80 : 48) - ccl) / shtrs.size(), 1);
			PacketUtils.sendNmTg(new Pair<Shooter, Arena>(sh, this), Team.CTs.icn + " ", " §7[0-0]", Team.CTs.clr);
			for (final Shooter s : shtrs.keySet()) {
				final Player p = (Player) s.inv.getHolder();
				p.sendMessage(Main.prf() + "§7Игрок §5" + sh.nm + " §7зашел играть!");
				Main.chgSbdTm(p.getScoreboard(), "amt", "", "§3" + getAlive() + " §7чел.");
				Main.chgSbdTm(p.getScoreboard(), "bhp", "", "§6" + String.valueOf(bpc) + "%");
				PacketUtils.sendRvsNmTg(Main.ds.bg().a(pl.getName()).b, Team.CTs, Main.ds.bg().a(p.getName()), Team.CTs, Team.CTs.icn + " ", " §7[0-0]", Team.CTs.clr);
			}
			if (shtrs.size() > 1) {
				bst.i(true);
				bst.n(true);
				Main.getBBlock(bst.cW().c(0, 1, 0), w).setType(Material.WARPED_PRESSURE_PLATE, false);
			}
			for (final TripWire tw : tws) {
				tw.shwNd(Main.ds.bg().a(sh.nm).b);
			}
			break;
		case BUYTIME:
		case ENDRND:
		case FINISH:
			sh.inv.getHolder().sendMessage(Main.prf() + "§cЭта игра уже заканчивается!");
			Main.lobbyPl((Player) sh.inv.getHolder());
			return false;
		}
		Inventories.updtGm(this);
		final byte n = MainLis.getPlaying();
		for (final Player p : Bukkit.getOnlinePlayers()) {
			p.setPlayerListFooter("§7Сейчас в игре: §d" + String.valueOf(n) + "§7 человек!");
		}
		return true;
	}

	public void addToGm(final Player p, final Team tm) {
		p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
		p.teleport(Main.getNrLoc(CTSps[Main.srnd.nextInt(CTSps.length)], w));
		Main.shwHdPls(p);
		p.getInventory().setItem(8, Main.mkItm(Material.GHAST_TEAR, "§5Магазин", 1));
		p.getInventory().setItem(7, Main.mkItm(Material.GOLD_NUGGET, "§eКусачки §f\u9268", 1));
		p.getInventory().setItem(2, Main.mkItm(Material.BLAZE_ROD, "§fНож \u9298", 1));
		p.setGameMode(isDay ? GameMode.SURVIVAL : GameMode.SPECTATOR);
	}

	@Override
	public boolean rmvPl(final Shooter sh) {
		final Team tm = shtrs.remove(sh);
		if (tm == null) {
			sh.inv.getHolder().sendMessage(Main.prf() + "§cВы не находитесь в игре!");
			return false;
		}
		sh.kls = 0;
		sh.dths = 0;
		sh.money = 0;
		switch (gst) {
		case WAITING:
		case BEGINING:
			if (shtrs.size() == 0) {
				if (tsk != null) {
					tsk.cancel();
				}
				endSps();
				ApiOstrov.sendArenaData(name, ru.komiss77.enums.GameState.ОЖИДАНИЕ, "§7[§5CS§7]", "§dВторжение", " ", "§7Игроков: §50§7/§5" + min, "", 0);
				end(this);
			} else {
				for (final Shooter s : shtrs.keySet()) {
					final Player p = (Player) s.inv.getHolder();
					p.sendMessage(Main.prf() + "§7Игрок §5" + sh.nm + " §7вышел с карты!");
					PacketUtils.sendAcBr(p, "§7Игроков: §5" + shtrs.size() + " §7из §5" + max + " §7(макс)!", 30);
					Main.chgSbdTm(p.getScoreboard(), "amt", "", "§3" + getAlive() + " §7чел.");
				}
			}
			break;
		case BUYTIME:
		case ROUND:
		case ENDRND:
			for (final TripWire tw : tws) {
				Main.ds.bg().a(sh.nm).b.a(new PacketPlayOutEntityDestroy(tw.eif.ae()));
			}
			if (shtrs.size() == 0) {
				if (tsk != null) {
					tsk.cancel();
				}
				endSps();
				ApiOstrov.sendArenaData(name, ru.komiss77.enums.GameState.ОЖИДАНИЕ, "§7[§5CS§7]", "§dВторжение", " ", "§7Игроков: §50§7/§5" + min, "", 0);
				end(this);
			} else if (shtrs.size() == 1) {
				cnt = Math.max(((isDay ? 80 : 48) - ccl) / shtrs.size(), 1);
				for (final Shooter s : shtrs.keySet()) {
					final Player p = (Player) s.inv.getHolder();
					p.sendMessage(Main.prf() + "§7Игрок §5" + sh.nm + " §7вышел из игры!");
					Main.chgSbdTm(p.getScoreboard(), "amt", "", "§3" + getAlive() + " §7чел.");
					Main.chgSbdTm(p.getScoreboard(), "bhp", "", "§7Не активна!");
				}
				bst.i(false);
				bst.n(false);
				Main.getBBlock(bst.cW().c(0, 1, 0), w).setType(Material.AIR, false);
			} else {
				cnt = Math.max(((isDay ? 80 : 48) - ccl) / shtrs.size(), 1);
				for (final Shooter s : shtrs.keySet()) {
					final Player p = (Player) s.inv.getHolder();
					p.sendMessage(Main.prf() + "§7Игрок §5" + sh.nm + " §7вышел из игры!");
					Main.chgSbdTm(p.getScoreboard(), "amt", "", "§3" + getAlive() + " §7чел.");
				}
			}
			break;
		case FINISH:
			if (shtrs.size() == 0) {
				this.tm = 1;
			}
			break;
		}
		Inventories.updtGm(this);
		Main.lobbyPl((Player) sh.inv.getHolder());
		return true;
	}

	//счетчик
	public void cntBeg() {
		tm = 10;
		gst = GameState.BEGINING;
		ApiOstrov.sendArenaData(this.name, ru.komiss77.enums.GameState.СТАРТ, "§7[§5CS§7]", "§dВторжение", " ", "§7Игроков: §50§7/§5" + this.min, "", shtrs.size());
		tsk = new BukkitRunnable() {
			@Override
			public void run() {
				switch (tm) {
				case 10:
				case 5:
				case 4:
					for (final Shooter sh : shtrs.keySet()) {
						final Player p = (Player) sh.inv.getHolder();
						p.playSound(p.getLocation(), Sound.BLOCK_DISPENSER_FAIL, 0.8f, 1.2f);
						PacketUtils.sendSbTtl(p, "§5§l" + tm, 10);
						Main.chgSbdTm(p.getScoreboard(), "rmn", "", "§5" + String.valueOf(tm));
					}
					break;
				case 3:
				case 2:
				case 1:
					for (final Shooter sh : shtrs.keySet()) {
						final Player p = (Player) sh.inv.getHolder();
						p.playSound(p.getLocation(), Sound.BLOCK_DISPENSER_FAIL, 0.8f, 1.2f);
						PacketUtils.sendSbTtl(p, "§d§l" + tm, 10);
						Main.chgSbdTm(p.getScoreboard(), "rmn", "", "§5" + String.valueOf(tm));
					}
					break;
				case 0:
					if (tsk != null) {
						tsk.cancel();
					}
					cntStrt();
					break;
				default:
					for (final Shooter sh : shtrs.keySet()) {
						Main.chgSbdTm(((Player) sh.inv.getHolder()).getScoreboard(), "rmn", "", "§5" + String.valueOf(tm));
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
			final Player p = (Player) sh.inv.getHolder();
			sh.money = 0;
			Main.nrmlzPl(p, true);
			gameScore(sh);
			p.teleport(Main.getNrLoc(CTSps[Main.srnd.nextInt(CTSps.length)], w));
			PacketUtils.sendNmTg(new Pair<Shooter, Arena>(sh, this), Team.CTs.icn + " ", " §7[" + sh.kls + "-" + sh.dths + "]", Team.CTs.clr);
			sh.inv.setItem(7, Main.mkItm(Material.GOLD_NUGGET, "§eКусачки §f\u9268", 1));
			sh.inv.setItem(2, Main.mkItm(Material.BLAZE_ROD, "§fНож \u9298", 1));
		}
		ast.i(true);
		ast.n(true);
		Main.getBBlock(ast.cW().c(0, 1, 0), w).setType(Material.WARPED_PRESSURE_PLATE, false);
		if (shtrs.size() > 1) {
			bst.i(true);
			bst.n(true);
			Main.getBBlock(bst.cW().c(0, 1, 0), w).setType(Material.WARPED_PRESSURE_PLATE, false);
		}
		swpDayNght();
	}

	private void setSpwnrs() {
		if (shtrs.size() == 0) {
			if (tsk != null) {
				tsk.cancel();
			}
			endSps();
			ApiOstrov.sendArenaData(name, ru.komiss77.enums.GameState.ОЖИДАНИЕ, "§7[§5CS§7]", "§dВторжение", " ", "§7Игроков: §50§7/§5" + min, "", 0);
			end(this);
		} else {
			cnt = Math.max((80 - ccl) / shtrs.size(), 1);
			final net.minecraft.world.level.World wm = PacketUtils.getNMSWrld(w.getName());
			for (byte i = (byte) (sis.length - 1); i >= 0; i--) {
				sis[i] = new Mobber(TSps[i], wm, this);
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
			ApiOstrov.sendArenaData(this.name, ru.komiss77.enums.GameState.ИГРА, "§7[§5CS§7]", "§dВторжение", " ", "§7Игроков: §50§7/§5" + this.max, "", shtrs.size());
			Inventories.updtGm(this);
			for (final Shooter sh : shtrs.keySet()) {
				final Player p = (Player) sh.inv.getHolder();
				sh.inv.setItem(8, new ItemStack(Material.AIR));
				PacketUtils.sendTtlSbTtl(p, "§4Ночь", "§7Крепитесь и защищайте точки!", 50);
				Main.chgSbdTm(p.getScoreboard(), "gst", "", "§5Ночь");
				p.playSound(p.getLocation(), "cs.info.night", 10f, 1f);
			}
			for (final Mobber mb : sis) {
				mb.ind.i(true);
				final Block b = Main.getBBlock(mb.ind.cW(), w);
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
			}
		} else {
			//day
			isDay = true;
			gst = GameState.ROUND;
			tm = (short) (ccl * 10 + 60);
			cnt = Math.max((80 - ccl) / shtrs.size(), 1);
			ccl++;
			ApiOstrov.sendArenaData(this.name, ru.komiss77.enums.GameState.ЭКИПИРОВКА, "§7[§5CS§7]", "§dВторжение", " ", "§7Игроков: §50§7/§5" + this.max, "", shtrs.size());
			Inventories.updtGm(this);
			for (final Shooter sh : shtrs.keySet()) {
				chngMn(sh, 250);
				final Player p = (Player) sh.inv.getHolder();
				if (p.getGameMode() == GameMode.SPECTATOR) {
					p.setGameMode(GameMode.SURVIVAL);
					p.teleport(Main.getNrLoc(CTSps[Main.srnd.nextInt(CTSps.length)], w));
				}
				p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, tm * 20 + 10, 1));
				sh.inv.setItem(8, Main.mkItm(Material.GHAST_TEAR, "§5Магазин", 1));
				PacketUtils.sendTtlSbTtl(p, "§3День", "§7Закупайтесь и ломайте спавнеры!", 50);
				Main.chgSbdTm(p.getScoreboard(), "gst", "", "§dДень");
				p.playSound(p.getLocation(), "cs.info.day", 10f, 1f);
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
						PacketUtils.sendAcBr((Player) sh.inv.getHolder(), "§7Осталась §d1 §7минута!", 30);
						Main.chgSbdTm(((Player) sh.inv.getHolder()).getScoreboard(), "gst", "", tt);
					}
					break;
				case 30:
					for (final Shooter sh : shtrs.keySet()) {
						final Player p = (Player) sh.inv.getHolder();
						PacketUtils.sendAcBr(p, "§7Осталось §d" + tm + " §7секунд!", 30);
						Main.chgSbdTm(p.getScoreboard(), "gst", "", tt);
						p.playSound(p.getLocation(), "cs.info." + (isDay ? "day30sec" : "nit30sec"), 10f, 1f);
					}
					break;
				case 10:
					for (final Shooter sh : shtrs.keySet()) {
						PacketUtils.sendAcBr((Player) sh.inv.getHolder(), "§7Осталось §d" + tm + " §7секунд!", 30);
						Main.chgSbdTm(((Player) sh.inv.getHolder()).getScoreboard(), "gst", "", tt);
					}
					break;
				case 0:
					swpDayNght();
					break;
				default:
					for (final Shooter sh : shtrs.keySet()) {
						Main.chgSbdTm(((Player) sh.inv.getHolder()).getScoreboard(), "gst", "", tt);
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
		ApiOstrov.sendArenaData(this.name, ru.komiss77.enums.GameState.ФИНИШ, "§7[§5CS§7]", "§dВторжение", " ", "§7Игроков: §50§7/§5" + this.min, "", shtrs.size());
		Inventories.updtGm(this);
		for (final Entity e : w.getEntitiesByClasses(Item.class, ArmorStand.class, Turtle.class)) {
			e.remove();
		}
		for (final BrknBlck b : brkn) {
			b.getBlock().setBlockData(b.bd, false);
		}
		Shooter tp = null;
		for (final Shooter sh : shtrs.keySet()) {
			if (tp == null || tp.kls < sh.kls) {
				tp = sh;
			}
		}
		final String ttl = CTwn ? "§3Победа!" : "§4Поражение!";
		for (final Shooter sh : shtrs.keySet()) {
			final Player p = (Player) sh.inv.getHolder();
			p.closeInventory();
			p.playSound(p.getLocation(), "cs.info." + snd, 10f, 1f);
			PacketUtils.sendTtlSbTtl(p, ttl, sbt, 50);
			PacketUtils.sendNmTg(new Pair<Shooter, Arena>(sh, this), "§7<§d" + name + "§7> ", " §7[" + sh.kls + "-" + sh.dths + "]", Team.NA.clr);
			ApiOstrov.moneyChange(sh.nm, sh.kls, "Убийства");
			p.sendMessage(" \n§7Финиш: " + ttl
				+ "\n§7=-=-=-=-=-=-=-=-=-=-=-=-=-=-"
				+ "\n§7Самый кровожадный защитник:"
				+ "\n§3" + tp.nm + " §7(§5" + tp.kls + " §7Убийств)"
				+ "\n \n§7Ваши убийства: §d" + sh.kls
				+ "\n§7=-=-=-=-=-=-=-=-=-=-=-=-=-=-");
			ApiOstrov.addStat(p, Stat.CS_game);
			sh.inv.clear();
			winScore(sh, CTwn);
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
					
					ApiOstrov.sendArenaData(name, ru.komiss77.enums.GameState.ОЖИДАНИЕ, "§7[§5CS§7]", "§dВторжение", " ", "§7Игроков: §50§7/§5" + min, "", 0);
					end(getArena());
				} else {
					final FireworkEffect fe = FireworkEffect.builder().with(Type.STAR).withColor(Color.TEAL).withFlicker().build();
					for (final Shooter sh : shtrs.keySet()) {
						final Player p = (Player) sh.inv.getHolder();
						Main.chgSbdTm(p.getScoreboard(), "tm", t, "");
						if (CTwn) {
							final Firework fw = (Firework) p.getWorld().spawnEntity(p.getEyeLocation(), EntityType.FIREWORK);
							final FireworkMeta fm = fw.getFireworkMeta();
							fm.addEffect(fe);
							fw.setFireworkMeta(fm);
						}
					}
				}
				tm--;
			}
		}.runTaskTimer(Main.plug, 20, 20);
	}

	private void beginScore(final Shooter sh) {
		final Scoreboard sb = Main.smg.getNewScoreboard();
		final Objective ob = sb.registerNewObjective("CS:GO", "", "§7[§5CS:GO§7]");
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
		((Player) sh.inv.getHolder()).setScoreboard(sb);
	}
	
	private void gameScore(final Shooter sh) {
		final Scoreboard sb = Main.smg.getNewScoreboard();
		final org.bukkit.scoreboard.Team t = sb.registerNewTeam("ind");
		t.setColor(ChatColor.DARK_RED);
		final org.bukkit.scoreboard.Team a = sb.registerNewTeam("ast");
		a.setColor(ChatColor.AQUA);
		final org.bukkit.scoreboard.Team b = sb.registerNewTeam("bst");
		b.setColor(ChatColor.GOLD);
		final Objective ob = sb.registerNewObjective("CS:GO", "", "§7[§5CS:GO§7]");
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
		Main.crtSbdTm(sb, "mn", "", "§7Монет: ", "§d" + String.valueOf(sh.money) + " §6⛃");
		ob.getScore("§7Монет: ")
		.setScore(3);
		ob.getScore("§7=-=-=-=-=-=-=-")
		.setScore(2);
		ob.getScore(" ")
		.setScore(1);
		
		ob.getScore("§e   ostrov77.su")
		.setScore(0);
		((Player) sh.inv.getHolder()).setScoreboard(sb);
		for (final Mobber m : sis) {
			t.addEntry(m.ind.cm().toString());
		}
		a.addEntry(ast.cm().toString());
		b.addEntry(bst.cm().toString());
	}

	private void winScore(final Shooter sh, final boolean isCTWn) {
		final Scoreboard sb = Main.smg.getNewScoreboard();
		final Objective ob = sb.registerNewObjective("CS:GO", "", "§7[§5CS:GO§7]");
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
		ob.getScore("§7Монет: §d" + String.valueOf(sh.money) + " §6⛃")
		.setScore(3);
		ob.getScore("§7=-=-=-=-=-=-=-")
		.setScore(2);
		ob.getScore(" ")
		.setScore(1);
		
		ob.getScore("§e   ostrov77.su")
		.setScore(0);
		((Player) sh.inv.getHolder()).setScoreboard(sb);
	}

	private byte getAlive() {
		byte n = 0;
		for (final Shooter sh : shtrs.keySet()) {
			if (sh.inv.getHolder().getGameMode() == GameMode.SURVIVAL) {
				n++;
			}
		}
		return n;
	}

	@Override
	public void killPl(final Shooter sh) {
		final Player p = (Player) sh.inv.getHolder();
		p.closeInventory();
		if (gst == GameState.ROUND && !isDay) {
			addDth(sh);
			p.setGameMode(GameMode.SPECTATOR);
			final byte rm = getAlive();
			for (final Shooter s : shtrs.keySet()) {
				Main.chgSbdTm(((Player) s.inv.getHolder()).getScoreboard(), "amt", "", "§3" + rm + " §7чел.");
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
			p.teleport(Main.getNrLoc(CTSps[Main.srnd.nextInt(CTSps.length)], w));
		}
	}

	public void addMbKll(final Shooter sh) {
		sh.kls++;
		PacketUtils.sendNmTg(new Pair<Shooter, Arena>(sh, this), Team.CTs.icn + " ", " §7[" + sh.kls + "-" + sh.dths + "]", Team.CTs.clr);
		ApiOstrov.addStat((Player) sh.inv.getHolder(), Stat.CS_mobs);
	}

	@Override
	public void addDth(final Shooter sh) {
		sh.dths++;
		PacketUtils.sendNmTg(new Pair<Shooter, Arena>(sh, this), Team.CTs.icn + " ", " §7[" + sh.kls + "-" + sh.dths + "]", Team.CTs.clr);
		ApiOstrov.addStat((Player) sh.inv.getHolder(), Stat.CS_death);
	}
	
	public void rmvSpnr(final Shooter pl) {
		final Mobber mb = dfs.remove(pl);
		chngMn(pl, 150);
		if (mb != null) {
			Main.getBBlock(mb.ind.cW(), w).setType(Material.CRYING_OBSIDIAN, false);
			mb.ind.i(false);
		}
		for (final Entry<Shooter, Mobber> e : dfs.entrySet()) {
			if (e.getValue().ind.cW().equals(mb.ind.cW())) {
				e.getKey().inv.getHolder().closeInventory();
			}
		}
		for (final Mobber m : sis) {
			if (m.ind.bT()) {
				return;
			}
		}
		cntFnsh(true, "§7Все §4спавнеры §7обезврежены!", "despawner");
	}

	public void wrngWire(final Shooter sh) {
		for (final Entry<Shooter, Mobber> e : dfs.entrySet()) {
			if (e.getKey().equals(sh)) {
				e.getValue().spwnMb(Main.getBBlock(e.getValue().ind.cW(), w));
			}
		}
	}
	
	public void endSps() {
		for (final Mobber m : sis) {
			if (m != null) {
				Main.getBBlock(m.ind.cW(), w).setType(Material.AIR, false);
				m.ind.a(RemovalReason.a);
				m.cancel();
			}
		}
		Main.getBBlock(ast.cW().c(0, 1, 0), w).setType(Material.AIR, false);
		ast.a(RemovalReason.a);
		Main.getBBlock(bst.cW().c(0, 1, 0), w).setType(Material.AIR, false);
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
				ast.a(IChatBaseComponent.a("§7Точка §bA§7: §d" + apc + "%"));
				for (final Shooter sh : shtrs.keySet()) {
					final Player p = (Player) sh.inv.getHolder();
					p.playSound(ast.getBukkitEntity().getLocation(), Sound.BLOCK_SCULK_SENSOR_CLICKING, 40f, 1f);
					PacketUtils.sendSbTtl(p, "§bA §7атакована : §b" + (apc + dmg) + " §7=-> §b" + apc, 30);
					Main.chgSbdTm(p.getScoreboard(), "ahp", "", "§b" + String.valueOf(apc) + "%");
				}
			} else {
				final Block b = Main.getBBlock(ast.cW().c(0, 1, 0), w);
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
				bst.a(IChatBaseComponent.a("§7Точка §6B§7: §d" + bpc + "%"));
				for (final Shooter sh : shtrs.keySet()) {
					final Player p = (Player) sh.inv.getHolder();
					p.playSound(ast.getBukkitEntity().getLocation(), Sound.BLOCK_SCULK_SENSOR_CLICKING, 40f, 1f);
					PacketUtils.sendSbTtl(p, "§6B §7атакована : §6" + (bpc + dmg) + " §7=-> §6" + bpc, 30);
					Main.chgSbdTm(p.getScoreboard(), "bhp", "", "§6" + String.valueOf(bpc) + "%");
				}
			} else {
				final Block b = Main.getBBlock(bst.cW().c(0, 1, 0), w);
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
}
