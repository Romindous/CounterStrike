package me.Romindous.CounterStrike.Game;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Turtle;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import me.Romindous.CounterStrike.Main;
import me.Romindous.CounterStrike.Enums.GunType;
import me.Romindous.CounterStrike.Enums.NadeType;
import me.Romindous.CounterStrike.Listeners.MainLis;
import me.Romindous.CounterStrike.Objects.Shooter;
import me.Romindous.CounterStrike.Objects.Game.BtShooter;
import me.Romindous.CounterStrike.Objects.Game.GameState;
import me.Romindous.CounterStrike.Objects.Game.GameType;
import me.Romindous.CounterStrike.Objects.Game.PlShooter;
import me.Romindous.CounterStrike.Objects.Game.TripWire;
import me.Romindous.CounterStrike.Objects.Loc.BrknBlck;
import me.Romindous.CounterStrike.Objects.Skins.Quest;
import me.Romindous.CounterStrike.Objects.Skins.SkinQuest;
import me.Romindous.CounterStrike.Utils.Inventories;
import me.Romindous.CounterStrike.Utils.PacketUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy;
import ru.komiss77.ApiOstrov;
import ru.komiss77.enums.Stat;
import ru.komiss77.modules.bots.BotManager;
import ru.komiss77.modules.world.XYZ;
import ru.komiss77.utils.ItemBuilder;

public class Gungame extends Arena {
	
	public final Inventory tms;
	private final GunType[] guns;
	private final byte cycle;
	
	public Gungame(final String name, final byte min, final byte max, 
		final XYZ[] TSps, final XYZ[] CTSps, final XYZ[] spots, 
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
		this.tms = Bukkit.createInventory(null, 9, Component.text("§eВыбор Комманды"));
		this.tms.setContents(Inventories.fillTmInv());
		this.gst = GameState.WAITING;
		ApiOstrov.sendArenaData(this.name, ru.komiss77.enums.GameState.ОЖИДАНИЕ, "§7[§5CS§7]", "§dЭстафета", " ", "§7Игроков: §50§7/§5" + this.min, "", 0);
		Inventories.updtGm(this);
	}

	public BukkitTask getTask() {
		return tsk;
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
				shtrs.put(sh, Team.NA);
				editLr(tms.getItem(4), true, "§7✦ §7" + sh.name());
				sh.item(Main.mkItm(Material.NETHER_STAR, "§eВыбор Комманды", 10), 2);
				sh.item(Main.mkItm(Material.GHAST_TEAR, "§5Магазин", 10), 6);
				sh.item(Main.mkItm(Material.SLIME_BALL, "§cВыход", 10), 8);
				if (shtrs.size() == min) {
					for (final Shooter s : shtrs.keySet()) {
						final Player pl = s.getPlayer();
						if (pl != null) {
							beginScore(s, pl);
							pl.sendMessage(Main.prf() + "§7Игрок §5" + sh.name() + " §7зашел на карту!");
							PacketUtils.sendAcBr(pl, "§7Игроков: §5" + shtrs.size() + " §7из §5" + max + " §7(макс)!", 30);
							if (!rnd) {
								pl.teleport(Main.rndElmt(spots).getCenterLoc(w));
							}
							PacketUtils.sendNmTg(s, "§7<§d" + name + "§7> ", " §7[-.-]", Team.NA.clr);
							pl.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 600, 1));
							Main.shwHdPls(pl);
						}
					}
					cntBeg();
				} else {
					final int rm = min - shtrs.size();
					waitScore(sh, p, rm);
					PacketUtils.sendNmTg(sh, "§7<§d" + name + "§7> ", " §7[-.-]", Team.NA.clr);
					for (final Shooter s : shtrs.keySet()) {
						final Player pl = s.getPlayer();
						if (pl != null) {
							pl.sendMessage(Main.prf() + "§7Игрок §5" + sh.name() + " §7зашел на карту!");
							PacketUtils.sendAcBr(pl, "§7Нужно еще §d" + rm + " §7игроков для начала!", 30);
							//Main.chgSbdTm(p.getScoreboard(), "tamt", "", "§4" + String.valueOf(getTmAmt(Team.Ts)) + (e.getValue() == Team.Ts ? " §7чел. §8✦ Вы" : " §7чел."));
							//Main.chgSbdTm(p.getScoreboard(), "ctamt", "", "§3" + String.valueOf(getTmAmt(Team.CTs)) + (e.getValue() == Team.CTs ? " §7чел. §8✦ Вы" : " §7чел."));
							Main.chgSbdTm(pl.getScoreboard(), "rmn", "", "§5" + String.valueOf(rm) + (rm > 1 ? " §7игроков" : " §7игрокa"));
						}
					}
				}
				break;
			case BEGINING:
				shtrs.put(sh, Team.NA);
				editLr(tms.getItem(4), true, "§7✦ §7" + sh.name());
				sh.item(Main.mkItm(Material.NETHER_STAR, "§eВыбор Комманды", 10), 2);
				sh.item(Main.mkItm(Material.GHAST_TEAR, "§5Магазин", 10), 6);
				sh.item(Main.mkItm(Material.SLIME_BALL, "§cВыход", 10), 8);
				if (!rnd) {
					p.teleport(Main.rndElmt(spots).getCenterLoc(w));
				}
				p.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, tm * 20, 1));
				beginScore(sh, p);
				Main.shwHdPls(p);
				PacketUtils.sendNmTg(sh, "§7<§d" + name + "§7> ", " §7[-.-]", Team.NA.clr);
				for (final Entry<Shooter, Team> e : shtrs.entrySet()) {
					final Player pl = e.getKey().getPlayer();
					if (pl != null) {
						pl.sendMessage(Main.prf() + "§7Игрок §5" + sh.name() + " §7зашел на карту!");
						PacketUtils.sendAcBr(pl, "§7Игроков: §5" + shtrs.size() + " §7из §5" + max + " §7(макс)!", 30);
					}
					PacketUtils.sendNmTg(PacketUtils.getNMSPl(p).c.h, e.getKey(), "§7<§d" + name + "§7> ", " §7[-.-]", true, e.getValue().clr);
				}
				break;
			case BUYTIME:
			case ROUND:
				final Team tm = getMinTm();
				shtrs.put(sh, tm);
				addToTm(p, tm);
				gameScore(sh, p);
				PacketUtils.sendNmTg(sh, tm.icn + " ", " §7[0-0]", tm.clr);
				for (final Entry<Shooter, Team> e : shtrs.entrySet()) {
					final Shooter s = e.getKey();
					final Team t = e.getValue();
					final Player pl = s.getPlayer();
					if (pl != null) {
						pl.sendMessage(Main.prf() + "§7Игрок §5" + sh.name() + " §7зашел играть за комманду " + tm.icn + "§7!");
						Main.chgSbdTm(pl.getScoreboard(), "tamt", "", "§4" + String.valueOf(getTmAmt(Team.Ts, true, true)) + (t == Team.Ts ? " §7чел. §8✦ Вы" : " §7чел."));
						Main.chgSbdTm(pl.getScoreboard(), "ctamt", "", "§3" + String.valueOf(getTmAmt(Team.CTs, true, true)) + (t == Team.CTs ? " §7чел. §8✦ Вы" : " §7чел."));
					} else {
						((BtShooter) s).updateAll(PacketUtils.getNMSPl(p).c.h);
					}
					PacketUtils.sendNmTg(PacketUtils.getNMSPl(p).c.h, s, e.getValue().icn + " ", " §7[" + s.kills() + "-" + s.deaths() + "]", t == tm, t.clr);
				}
				for (final TripWire tw : tws) {
					if (tm == tw.tm) {
						tw.shwNd(PacketUtils.getNMSPl(p).c);
					}
				}
			case ENDRND:
				break;
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

	public void addToTm(final Player p, final Team tm) {
		p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
		p.teleport(Main.rndElmt(spots).getCenterLoc(w));
		Main.shwHdPls(p);
		final PlayerInventory pinv = p.getInventory();
		p.setGameMode(GameMode.SURVIVAL);
		pinv.setItem(2, Main.mkItm(Material.BLAZE_ROD, "§fНож \u9298", 10));
		if (tm == Team.Ts) {
			pinv.setHelmet(Inventories.TShop.getItem(GunType.hlmtSlt).clone());
			pinv.setChestplate(Inventories.TShop.getItem(GunType.chstSlt).clone());
		} else {
			pinv.setHelmet(Inventories.CTShop.getItem(GunType.hlmtSlt).clone());
			pinv.setChestplate(Inventories.CTShop.getItem(GunType.chstSlt).clone());
		}
		p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 250, 250, true, false, false));
		p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 40, 2, true, false, false));
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
			switch (tm) {
			case Ts:
				editLr(tms.getItem(1), false, "§4✦ §7" + sh.name());
				break;
			case CTs:
				editLr(tms.getItem(7), false, "§3✦ §7" + sh.name());
				break;
			case NA:
				editLr(tms.getItem(4), false, "§7✦ §7" + sh.name());
				break;
			}
			switch (gst) {
			case WAITING:
				if (shtrs.size() > 0) {
					final int rm = min - shtrs.size();
					for (final Entry<Shooter, Team> e : shtrs.entrySet()) {
						final Player pl = e.getKey().getPlayer();
						if (pl != null) {
							pl.sendMessage(Main.prf() + "§7Игрок §5" + sh.name() + " §7вышел с карты!");
							PacketUtils.sendAcBr(pl, "§7Нужно еще §d" + rm + " §7игроков для начала!", 30);
							final Scoreboard sb = pl.getScoreboard();
							Main.chgSbdTm(pl.getScoreboard(), "tamt", "", "§4" + String.valueOf(getTmAmt(Team.Ts, true, true)) + (e.getValue() == Team.Ts ? " §7чел. §8✦ Вы" : " §7чел."));
							Main.chgSbdTm(pl.getScoreboard(), "ctamt", "", "§3" + String.valueOf(getTmAmt(Team.CTs, true, true)) + (e.getValue() == Team.CTs ? " §7чел. §8✦ Вы" : " §7чел."));
							Main.chgSbdTm(sb, "rmn", "", "§5" + String.valueOf(rm) + (rm > 1 ? " §7игроков" : " §7игрокa"));
						}
					}
				} else {
					Arena.end(this);
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
							waitScore(s, pl, min - 1);
							pl.teleport(Main.getNrLoc(Main.lobby));
							pl.sendMessage(Main.prf() + "§7Игрок §5" + sh.name() + " §7вышел с карты,\n§7Слишком мало игроков для начала!");
							PacketUtils.sendAcBr(pl, "§7Игроков: §5" + shtrs.size() + " §7из §5" + max + " §7(макс)!", 30);
							pl.removePotionEffect(PotionEffectType.GLOWING);
						}
					}
				} else {
					for (final Entry<Shooter, Team> e : shtrs.entrySet()) {
						final Player pl = e.getKey().getPlayer();
						if (pl != null) {
							pl.sendMessage(Main.prf() + "§7Игрок §5" + sh.name() + " §7вышел с карты!");
							PacketUtils.sendAcBr(pl, "§7Игроков: §5" + shtrs.size() + " §7из §5" + max + " §7(макс)!", 30);
							Main.chgSbdTm(pl.getScoreboard(), "tamt", "", "§4" + String.valueOf(getTmAmt(Team.Ts, true, true)) + (e.getValue() == Team.Ts ? " §7чел. §8✦ Вы" : " §7чел."));
							Main.chgSbdTm(pl.getScoreboard(), "ctamt", "", "§3" + String.valueOf(getTmAmt(Team.CTs, true, true)) + (e.getValue() == Team.CTs ? " §7чел. §8✦ Вы" : " §7чел."));
						}
					}
				}
				break;
			case BUYTIME:
			case ROUND:
				for (final TripWire tw : tws) {
					if (tm == tw.tm) {
						PacketUtils.getNMSPl(p).c.a(new PacketPlayOutEntityDestroy(tw.eif.af()));
					}
				}
				if (getTmAmt(Team.CTs, false, true) == 0 || getTmAmt(Team.Ts, false, true) == 0) {
					if (tsk != null) {
						tsk.cancel();
					}
					for (final Shooter s : shtrs.keySet()) {
						final Player pl = s.getPlayer();
						if (pl == null) {
							
						} else {
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
							Main.chgSbdTm(pl.getScoreboard(), "tamt", "", "§4" + String.valueOf(getTmAmt(Team.Ts, true, true)) + (e.getValue() == Team.Ts ? " §7чел. §8✦ Вы" : " §7чел."));
							Main.chgSbdTm(pl.getScoreboard(), "ctamt", "", "§3" + String.valueOf(getTmAmt(Team.CTs, true, true)) + (e.getValue() == Team.CTs ? " §7чел. §8✦ Вы" : " §7чел."));
						}
					}
				}
			case ENDRND:
				break;
			case FINISH:
				if (shtrs.size() == 0) {
					this.tm = 1;
				}
				break;
			}
			Inventories.updtGm(this);
			Main.lobbyPl(p);
		}
		return true;
	}

	//счетчик
	public void cntBeg() {
		tm = 30;
		gst = GameState.BEGINING;
		ApiOstrov.sendArenaData(this.name, ru.komiss77.enums.GameState.СТАРТ, "§7[§5CS§7]", "§dЭстафета", " ", "§7Игроков: §5" + shtrs.size() + "§7/§5" + this.max, "", shtrs.size());
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
					cntPrep();
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

	public void cntPrep() {
		tm = 10;
		gst = GameState.BUYTIME;
		ApiOstrov.sendArenaData(this.name, ru.komiss77.enums.GameState.ЭКИПИРОВКА, "§7[§5CS§7]", "§dЭстафета", " ", "§7Игроков: §5" + shtrs.size() + "§7/§5" + this.max, "", shtrs.size());
		Inventories.updtGm(this);
		for (final TripWire tw : tws) {
			tw.rmv(this);
		}
		tws.clear();
		for (final Entity e : w.getEntitiesByClasses(Item.class, ArmorStand.class, Turtle.class)) {
			e.remove();
		}
		blncTms();
		for (final Entry<Shooter, Team> e : shtrs.entrySet()) {
			final Shooter sh = e.getKey();
			final Player p = sh.getPlayer();
			if (p != null) {
				Main.nrmlzPl(p, true);
				gameScore(sh, p);
				p.setGameMode(GameMode.SURVIVAL);
				PacketUtils.sendTtlSbTtl(p, "", "§l§eПодготовка Оружейни...", 30);
			}
			
			sh.teleport(sh.getEntity(), Main.rndElmt(spots).getCenterLoc(w));
			
			updateWeapon(sh);
			PacketUtils.sendNmTg(e.getKey(), e.getValue().icn + " ", " §7[" + sh.kills() + "-" + sh.deaths() + "]", e.getValue().clr);
			sh.getEntity().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 250, 250, true, false, false));
			sh.getEntity().addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 40, 2, true, false, false));
		}
		
		tsk = new BukkitRunnable() {
			@Override
			public void run() {
				final String t = getTime(tm, "§5");
				for (final LivingEntity le : w.getLivingEntities()) {
					le.setFireTicks(-1);
				}
				switch (tm) {
				case 3, 2, 1:
					for (final Shooter sh : shtrs.keySet()) {
						final Player p = sh.getPlayer();
						if (p != null) {
							p.playSound(p.getLocation(), Sound.BLOCK_DISPENSER_FAIL, 0.8f, 1.2f);
							PacketUtils.sendSbTtl(p, "§d§l" + tm, 10);
							Main.chgSbdTm(p.getScoreboard(), "tm", t, "");
						}
					}
					break;
				case 0:
					if (tsk != null) {
						tsk.cancel();
					}
					
					cntRnd();
					break;
				default:
					for (final Shooter sh : shtrs.keySet()) {
						final Player p = sh.getPlayer();
						if (p != null) {
							Main.chgSbdTm(p.getScoreboard(), "tm", t, "");
						}
					}
					break;
				}
				tm--;
			}
		}.runTaskTimer(Main.plug, 20, 20);
	}

	private void cntRnd() {
		tm = 360;
		gst = GameState.ROUND;
		ApiOstrov.sendArenaData(this.name, ru.komiss77.enums.GameState.ИГРА, "§7[§5CS§7]", "§dЭстафета", " ", "§7Игроков: §5" + shtrs.size() + "§7/§5" + this.max, "", shtrs.size());
		Inventories.updtGm(this);
		for (final Entry<Shooter, Team> e : shtrs.entrySet()) {
			final LivingEntity le = e.getKey().getEntity();
			if (le == null) continue;
			le.removePotionEffect(PotionEffectType.SLOW);
			if (le instanceof Player) {
				final Player p = (Player) le;
				PacketUtils.sendSbTtl(p, (e.getValue() == Team.Ts ? "§4" : "§3") + "§lВперед", 30);
				Main.chgSbdTm(p.getScoreboard(), "gst", "", "§dБой");
				p.playSound(p.getLocation(), "cs.info." + (e.getValue() == Team.Ts ? "tfight" : "ctfight"), 10f, 1f);
			}
		}
		tsk = new BukkitRunnable() {
			@Override
			public void run() {
				switch (tm) {
				case 60:
					for (final Shooter sh : shtrs.keySet()) {
						final Player p = sh.getPlayer();
						if (p != null) {
							PacketUtils.sendAcBr(p, "§7Осталась §d1 §7минута!", 30);
							Main.chgSbdTm(p.getScoreboard(), "tm", getTime(tm, "§d"), "");
						}
					}
					break;
				case 30:
					for (final Entry<Shooter, Team> e : shtrs.entrySet()) {
						final Player p = e.getKey().getPlayer();
						if (p != null) {
							PacketUtils.sendAcBr(p, "§7Осталось §d" + tm + " §7секунд!", 30);
							p.playSound(p.getLocation(), "cs.info." + (e.getValue() == Team.Ts ? "t30sec" : "ct30sec"), 10f, 1f);
							Main.chgSbdTm(p.getScoreboard(), "tm", getTime(tm, "§d"), "");
						}
					}
					break;
				case 10:
					for (final Shooter sh : shtrs.keySet()) {
						final Player p = sh.getPlayer();
						if (p != null) {
							PacketUtils.sendAcBr(p, "§7Осталось §d" + tm + " §7секунд!", 30);
							Main.chgSbdTm(p.getScoreboard(), "tm", getTime(tm, "§d"), "");
						}
					}
					break;
				case 0:
					final int tp = getTmProg(Team.Ts);
					final int ctp = getTmProg(Team.CTs);
					if (tp == ctp) {
						cntFnsh(Main.srnd.nextBoolean() ? Team.CTs : Team.Ts);
					} else if (tp > ctp) {
						cntFnsh(Team.Ts);
					} else {
						cntFnsh(Team.CTs);
					}
					break;
				default:
					for (final Shooter sh : shtrs.keySet()) {
						final Player p = sh.getPlayer();
						if (p != null) {
							Main.chgSbdTm(p.getScoreboard(), "tm", getTime(tm, "§d"), "");
						}
					}
					break;
				}
				tm--;
			}
		}.runTaskTimer(Main.plug, 20, 20);
	}

	private void cntFnsh(final Team wn) {
		if (tsk != null) {
			tsk.cancel();
		}
		tm = 10;
		gst = GameState.FINISH;
		ApiOstrov.sendArenaData(this.name, ru.komiss77.enums.GameState.ФИНИШ, "§7[§5CS§7]", "§dЭстафета", " ", "§7Игроков: §5" + shtrs.size() + "§7/§5" + this.max, "", shtrs.size());
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
			case NA:
				break;
			}
		}
		
		final String st;
		final String msg;
		switch (wn) {
		case Ts:
			msg = " \n§7Победа в раунде: §4Террористы\n \n§5=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-\n§7Лучший спецназовец: §3" + 
			(bct == null ? "Никто" : bct.name()) + "\n          §7--=x=--\n§7Самый злобный террорист: §4" + 
			(bt == null ? "Никто" : bt.name()) + "\n§5=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-";
			st = "§4§lТеррористы §7выйграли!";
			break;
		case CTs:
			msg = " \n§7Победа в раунде: §3Спецназ\n \n§5=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-\n§7Лучший спецназовец: §3" + 
			(bct == null ? "Никто" : bct.name()) + "\n          §7--=x=--\n§7Самый злобный террорист: §4" + 
			(bt == null ? "Никто" : bt.name()) + "\n§5=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-";
			st = "§3§lСпецназ §7одержал победу!";
			break;
		default:
			msg = " ";
			st = " ";
			break;
		}
		
		for (final Entry<Shooter, Team> e : shtrs.entrySet()) {
			final Shooter sh = e.getKey();
			if (sh instanceof PlShooter) {
				final Player p = sh.getPlayer();
				p.closeInventory();
				Main.chgSbdTm(p.getScoreboard(), "tm", "§d00:00", "");
				p.playSound(p.getLocation(), "cs.info." + (e.getValue() == Team.Ts ? "tfinish" : "ctfinish"), 10f, 1f);
				p.sendMessage(msg);
				PacketUtils.sendTtlSbTtl(p, "§5Финиш", st, 40);
				PacketUtils.sendNmTg(sh, "§7<§d" + name + "§7> ", " §7[" + sh.kills() + "-" + sh.deaths() + "]", Team.NA.clr);
				sh.inv().clear();
				winScore(sh, p, wn == Team.CTs);
				
				if (e.getValue() == wn) {
					ApiOstrov.addStat(p, Stat.CS_win);
					SkinQuest.tryCompleteQuest(e.getKey(), Quest.ГРУЗЧИК, ApiOstrov.getStat(p, Stat.CS_win));
				} else {
					ApiOstrov.addStat(p, Stat.CS_loose);
				}
				ApiOstrov.addStat(p, Stat.CS_game);
				SkinQuest.tryCompleteQuest(sh, Quest.ДУША, ApiOstrov.getStat(p, Stat.CS_game));
			} else {
				((BtShooter) sh).remove();
			}
		}
		
		final Iterator<TripWire> ti = tws.iterator();
		while (bi.hasNext()) {
			ti.next().rmv(this);
			bi.remove();
		}
		
		tsk = new BukkitRunnable() {
			@Override
			public void run() {
				final String t = getTime(tm, "§d");
				if (tm == 0) {
					if (tsk != null) {
						tsk.cancel();
					}
					
					Arena.end(getArena());
				} else {
					final FireworkEffect fe;
					switch (wn) {
					case Ts:
						fe = FireworkEffect.builder().with(Type.CREEPER).withColor(Color.MAROON).withFlicker().build();
						break;
					case CTs:
						fe = FireworkEffect.builder().with(Type.STAR).withColor(Color.TEAL).withFlicker().build();
						break;
					case NA:
					default:
						fe = null;
						break;
					}
					for (final Entry<Shooter, Team> e : shtrs.entrySet()) {
						final Player p = e.getKey().getPlayer();
						if (p != null) {
							Main.chgSbdTm(p.getScoreboard(), "tm", t, "");
							if (e.getValue() == wn) {
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

	private void waitScore(final Shooter sh, final Player p, final int rm) {
		final Scoreboard sb = Main.smg.getNewScoreboard();
		final Objective ob = sb.registerNewObjective("CS:GO", Criteria.DUMMY, Component.text("§7[§5CS:GO§7]"));
		ob.setDisplaySlot(DisplaySlot.SIDEBAR);
		ob.getScore("    ")
		.setScore(11);
		ob.getScore("§7Карта: §5" + name)
		.setScore(10);
		ob.getScore("§7Комманды:")
		.setScore(9);
		ob.getScore("§7=-=-=-=-=-=-=-=-")
		.setScore(8);
		Main.crtSbdTm(sb, "tamt", "", "§4\u9265 §7: ", "§4" + String.valueOf(getTmAmt(Team.Ts, true, true)) + " §7чел.");
		ob.getScore("§4\u9265 §7: ")
		.setScore(7);
		ob.getScore("   ")
		.setScore(6);
		Main.crtSbdTm(sb, "ctamt", "", "§3\u9264 §7: ", "§3" + String.valueOf(getTmAmt(Team.CTs, true, true)) + " §7чел.");
		ob.getScore("§3\u9264 §7: ")
		.setScore(5);
		ob.getScore("§7-=-=-=-=-=-=-=-")
		.setScore(4);
		ob.getScore("  ")
		.setScore(3);
		Main.crtSbdTm(sb, "rmn", "", "§7Ждем еще ", "§5" + String.valueOf(rm) + (rm > 1 ? " §7игроков" : " §7игрокa"));
		ob.getScore("§7Ждем еще ")
		.setScore(2);
		ob.getScore(" ")
		.setScore(1);
		
		ob.getScore("§e   ostrov77.su")
		.setScore(0);
		p.setScoreboard(sb);
	}

	private void beginScore(final Shooter sh, final Player p) {
		final Scoreboard sb = Main.smg.getNewScoreboard();
		final Objective ob = sb.registerNewObjective("CS:GO", Criteria.DUMMY, Component.text("§7[§5CS:GO§7]"));
		final org.bukkit.scoreboard.Team bmb = sb.registerNewTeam("bmb");
		bmb.prefix(Component.text("§4"));
		bmb.color(NamedTextColor.DARK_RED);
		ob.setDisplaySlot(DisplaySlot.SIDEBAR);
		ob.getScore("    ")
		.setScore(11);
		ob.getScore("§7Карта: §5" + name)
		.setScore(10);
		ob.getScore("§7Комманды:")
		.setScore(9);
		ob.getScore("§7=-=-=-=-=-=-=-=-")
		.setScore(8);
		Main.crtSbdTm(sb, "tamt", "", "§4\u9265 §7: ", "§4" + String.valueOf(getTmAmt(Team.Ts, true, true)) + (shtrs.get(sh) == Team.Ts ? " §7чел. §8✦ Вы" : " §7чел."));
		ob.getScore("§4\u9265 §7: ")
		.setScore(7);
		ob.getScore("   ")
		.setScore(6);
		Main.crtSbdTm(sb, "ctamt", "", "§3\u9264 §7: ", "§3" + String.valueOf(getTmAmt(Team.CTs, true, true)) + (shtrs.get(sh) == Team.CTs ? " §7чел. §8✦ Вы" : " §7чел."));
		ob.getScore("§3\u9264 §7: ")
		.setScore(5);
		ob.getScore("§7=-=-=-=-=-=-=-")
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
		final Objective ob = sb.registerNewObjective("CS:GO", Criteria.DUMMY, Component.text("§7[§5CS:GO§7]"));
		final org.bukkit.scoreboard.Team bmb = sb.registerNewTeam("bmb");
		bmb.prefix(Component.text("§4"));
		bmb.color(NamedTextColor.DARK_RED);
		ob.setDisplaySlot(DisplaySlot.SIDEBAR);
		Main.crtSbdTm(sb, "scr", "§l§4\u9265 §7: §4" + getTmProg(Team.Ts) + "§7/§4" + cycle, 
			" §7--=x=-- ", "§l§3" + getTmProg(Team.CTs) + "§7/§3" + cycle + " §7: §3\u9264");
		ob.getScore(" §7--=x=-- ")
		.setScore(13);
		ob.getScore("§7=-=-=-=-=-=-=-=-")
		.setScore(12);
		ob.getScore("§7Карта: §5" + name)
		.setScore(11);
		ob.getScore("    ")
		.setScore(10);
		Main.crtSbdTm(sb, "gst", "", "§7Cтадия: ", gst == GameState.BUYTIME ? "§5Подготовка" : "§5Бой");
		ob.getScore("§7Cтадия: ")
		.setScore(9);
		Main.crtSbdTm(sb, "tm", getTime(tm, "§d"), " §7до конца!", "");
		ob.getScore(" §7до конца!")
		.setScore(8);
		ob.getScore("   ")
		.setScore(7);
		Main.crtSbdTm(sb, "tamt", "", "§4\u9265 §7: ", "§4" + String.valueOf(getTmAmt(Team.Ts, true, true)) + (shtrs.get(sh) == Team.Ts ? " §7чел. §8✦ Вы" : " §7чел."));
		ob.getScore("§4\u9265 §7: ")
		.setScore(6);
		Main.crtSbdTm(sb, "ctamt", "", "§3\u9264 §7: ", "§3" + String.valueOf(getTmAmt(Team.CTs, true, true)) + (shtrs.get(sh) == Team.CTs ? " §7чел. §8✦ Вы" : " §7чел."));
		ob.getScore("§3\u9264 §7: ")
		.setScore(5);
		ob.getScore("  ")
		.setScore(4);
		Main.crtSbdTm(sb, "prg", "", "§7Прогрес: ", "§d" + String.valueOf(sh.money()) + "§7/§d" + String.valueOf(cycle));
		ob.getScore("§7Прогрес: ")
		.setScore(3);
		ob.getScore("§7=-=-=-=-=-=-=-")
		.setScore(2);
		ob.getScore(" ")
		.setScore(1);
		
		ob.getScore("§e   ostrov77.su")
		.setScore(0);
		p.setScoreboard(sb);
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

	private void winScore(final Shooter sh, final Player p, final boolean isCTWn) {
		final Scoreboard sb = Main.smg.getNewScoreboard();
		final Objective ob = sb.registerNewObjective("CS:GO", Criteria.DUMMY, Component.text("§7[§5CS:GO§7]"));
		ob.setDisplaySlot(DisplaySlot.SIDEBAR);
		if (isCTWn) {
			ob.getScore("§l§4\u9265 §7: §4" + getTmProg(Team.Ts) + "§7/§4" + cycle + " §7--=x=-- §l§3" + cycle + "§7/§3" + cycle + " §7: §3\u9264")
			.setScore(13);
		} else {
			ob.getScore("§l§4\u9265 §7: §4" + cycle + "§7/§4" + cycle + " §7--=x=-- §l§3" + getTmProg(Team.CTs) + "§7/§3" + cycle + " §7: §3\u9264")
			.setScore(13);
		}
		ob.getScore("§7=-=-=-=-=-=-=-=-")
		.setScore(12);
		ob.getScore("§7Карта: §5" + name)
		.setScore(11);
		ob.getScore("    ")
		.setScore(10);
		ob.getScore("§7Cтадия: §dФиниш")
		.setScore(9);
		Main.crtSbdTm(sb, "tm", "§5" + String.valueOf(tm), " §7до конца!", "");
		ob.getScore(" §7до конца!")
		.setScore(8);
		ob.getScore("   ")
		.setScore(7);
		ob.getScore("§4\u9265 §7: §4" + String.valueOf(getTmAmt(Team.Ts, true, true)) + (shtrs.get(sh) == Team.Ts ? " §7чел. §8✦ Вы" : " §7чел."))
		.setScore(6);
		ob.getScore("§3\u9264 §7: §3" + String.valueOf(getTmAmt(Team.CTs, true, true)) + (shtrs.get(sh) == Team.CTs ? " §7чел. §8✦ Вы" : " §7чел."))
		.setScore(5);
		ob.getScore("  ")
		.setScore(4);
		ob.getScore("§7Прогрес: §d" + sh.money() + "§7/§d" + String.valueOf(cycle))
		.setScore(3);
		ob.getScore("§7=-=-=-=-=-=-=-")
		.setScore(2);
		ob.getScore(" ")
		.setScore(1);
		
		ob.getScore("§e   ostrov77.su")
		.setScore(0);
		p.setScoreboard(sb);
	}

	@Override
	public void killSh(final Shooter sh) {
		//Bukkit.broadcast(Component.text("ent-" + sh.getEntity().getEntityId() + ", sh-" + sh.name()));
		final LivingEntity le = sh.getEntity();
		if (gst == GameState.ROUND) {
			sh.dropIts(le.getLocation());
			addDth(sh);
			if (sh instanceof BtShooter) {
				((BtShooter) sh).die(le);
			}
		} else if (sh instanceof PlShooter) {
			if (le.hasPotionEffect(PotionEffectType.GLOWING)) {
				Main.nrmlzPl((Player) le, false);
				le.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, tm * 20, 1));
			} else {
				Main.nrmlzPl((Player) le, false);
			}
		}
		if (gst != GameState.FINISH) {
			final Location loc = Main.rndElmt(spots).getCenterLoc(w);
			loc.getWorld().spawnParticle(Particle.PORTAL, loc, 200, 0.2D, 0.4D, 0.2D, 0.4D, null, false);
			sh.teleport(le, loc);
		}
	}
	
	private void updateWeapon(final Shooter sh) {
		if (sh.money() == guns.length) {
			cntFnsh(shtrs.get(sh));
			return;
		}
		
		sh.item(Main.air.clone(), 0); sh.item(Main.air.clone(), 1);
		sh.item(Main.mkItm(Material.BLAZE_ROD, "§fНож \u9298", 10), 2);
		sh.item(Main.air.clone(), 3); sh.item(Main.air.clone(), 4);
		
		switch (shtrs.get(sh)) {
		case Ts:
			sh.item(Inventories.TShop.getItem(GunType.hlmtSlt).clone(), EquipmentSlot.HEAD);
			sh.item(Inventories.TShop.getItem(GunType.chstSlt).clone(), EquipmentSlot.CHEST);
			//Bukkit.broadcast(Component.text("gv-" + Inventories.TShop.getItem(GunType.chstSlt).getType().toString() + " to-" + sh.getEntity().getEntityId()));
			break;
		case CTs:
			sh.item(Inventories.CTShop.getItem(GunType.hlmtSlt).clone(), EquipmentSlot.HEAD);
			sh.item(Inventories.CTShop.getItem(GunType.chstSlt).clone(), EquipmentSlot.CHEST);
			//Bukkit.broadcast(Component.text("gv-" + Inventories.CTShop.getItem(GunType.chstSlt).getType().toString() + " to-" + sh.getEntity().getEntityId()));
			break;
		default:
			break;
		}

		final GunType gt = guns[sh.money()];
		if (gt == null) {
			final Player p = sh.getPlayer();
			if (p != null) {
				PacketUtils.sendTtlSbTtl(p, "", "§7Последний §dцыкл §7оружия!", 20);
			}
		} else {
			final int mdl = sh.getModel(gt);
			final NadeType nt = nadeFromPts(sh.money());
			sh.item(Main.mkItm(Material.BLAZE_ROD, "§fНож \u9298", 10), 2);
			sh.item(new ItemBuilder(gt.getMat()).name(mdl == GunType.defCMD ? "§5" + gt.toString() : 
				"§5" + gt.toString() + " '" + Main.nrmlzStr(Quest.getQuest(gt, mdl).toString()) + "'")
			.setAmount(gt.amo).setModelData(mdl).build(), gt.prm ? 0 : 1);
			sh.item(Inventories.CTShop.getItem(nt.slt).clone(), nt.prm ? NadeType.prmSlot : NadeType.scdSlot);
			//Bukkit.broadcast(Component.text("guns-" + sh.getEntity().getEntityId()));
		}
		
		final LivingEntity le = sh.getEntity();
		le.setHealth(le.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());
		if (sh instanceof PlShooter) {
			final Player p = (Player) le;
			Main.chgSbdTm(p.getScoreboard(), "scr", "§l§4\u9265 §7: §4" + getTmProg(Team.Ts) + "§7/§4" + cycle, 
				"§l§3" + getTmProg(Team.CTs) + "§7/§3" + cycle + " §7: §3\u9264");
			Main.chgSbdTm(p.getScoreboard(), "prg", "", "§d" + String.valueOf(sh.money()) + "§7/§d" + String.valueOf(cycle));
			PacketUtils.zoom(p, false);
			p.setNoDamageTicks(10);
		} else if (sh instanceof BtShooter) {
			((BtShooter) sh).switchToGun();
		}
	}

	private NadeType nadeFromPts(final int pts) {
		final float cof = 10f / cycle;
		switch (Main.srnd.nextInt((int) (cof * pts) + 1)) {
		default:
		case 10, 9, 8:
			return NadeType.FRAG;
		case 7, 6, 5:
			return NadeType.FLAME;
		case 4, 3:
			return NadeType.SMOKE;
		case 2, 1:
			return NadeType.FLASH;
		case 0:
			return NadeType.DECOY;
		}
	}

	@Override
	public void addKll(final Shooter sh) {
		sh.killsI();
		sh.money(sh.money() + 1);
		PacketUtils.sendNmTg(sh, shtrs.get(sh).icn + " ", " §7[" + sh.kills() + "-" + sh.deaths() + "]", shtrs.get(sh).clr);
		updateWeapon(sh);
		final Player p = sh.getPlayer();
		if (p != null) {
			ApiOstrov.addStat(p, Stat.CS_kill);
			SkinQuest.tryCompleteQuest(sh, Quest.ЗЕМЛЯ, sh.kills() / Math.max(sh.deaths(), 1));
			SkinQuest.tryCompleteQuest(sh, Quest.ТОКСИК, sh.kills());
		}
	}

	@Override
	public void addDth(final Shooter sh) {
		sh.deathsI();
		sh.money(Math.max(sh.money() - 1, 0));
		PacketUtils.sendNmTg(sh, shtrs.get(sh).icn + " ", " §7[" + sh.kills() + "-" + sh.deaths() + "]", shtrs.get(sh).clr);
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
	public void chngTm(final Shooter sh, final Team nv) {
		final Player p = sh.getPlayer();
		if (p == null) {
			
		} else {
			if (nv != null && ( (shtrs.get(sh) == nv.getOpst() && getTmAmt(nv, true, true) >= 
				getTmAmt(nv.getOpst(), true, true)) || getTmAmt(nv, true, true) > getTmAmt(nv.getOpst(), true, true) )) {
				p.sendMessage(Main.prf() + "§cВ этой комманде слишком много игроков!");
				return;
			}
			final Team tm = shtrs.replace(sh, nv);
			if (nv == Team.NA || tm != nv) {
				for (final Entry<Shooter, Team> e : shtrs.entrySet()) {
					Main.chgSbdTm(p.getScoreboard(), "tamt", "", "§4" + String.valueOf(getTmAmt(Team.Ts, true, true)) + (e.getValue() == Team.Ts ? " §7чел. §8✦ Вы" : " §7чел."));
					Main.chgSbdTm(p.getScoreboard(), "ctamt", "", "§3" + String.valueOf(getTmAmt(Team.CTs, true, true)) + (e.getValue() == Team.CTs ? " §7чел. §8✦ Вы" : " §7чел."));
				}
				PacketUtils.sendNmTg(sh, "§7<§d" + name + "§7> ", " §7[-.-]", nv.clr);
				switch (tm) {
				case Ts:
					editLr(tms.getItem(1), false, "§4✦ §7" + sh.name());
					break;
				case CTs:
					editLr(tms.getItem(7), false, "§3✦ §7" + sh.name());
					break;
				case NA:
					editLr(tms.getItem(4), false, "§7✦ §7" + sh.name());
					break;
				}
				switch (nv) {
				case Ts:
					editLr(tms.getItem(1), true, "§4✦ §7" + sh.name());
					break;
				case CTs:
					editLr(tms.getItem(7), true, "§3✦ §7" + sh.name());
					break;
				case NA:
					editLr(tms.getItem(4), true, "§7✦ §7" + sh.name());
					break;
				}
			}
		}
	}
	
	@Override
	public GameType getType() {
		return GameType.GUNGAME;
	}

	private void blncTms() {
		for (final Entry<Shooter, Team> e : shtrs.entrySet()) {
			if (e.getValue() == Team.NA) {
				e.setValue(getMinTm());
			}
		}
		
		if (bots) {
			int tmMx = max >> 1;
//			shtrs.put(new BtShooter(this, w), Team.CTs);
			
			int tPls = 0, ctPls = 0;
			final Iterator<Entry<Shooter, Team>> it = shtrs.entrySet().iterator();
			while (it.hasNext()) {
				final Entry<Shooter, Team> en = it.next();
				switch (en.getValue()) {
				case Ts:
					if (++tPls > tmMx) {
						if (en.getKey() instanceof BtShooter) {
							it.remove();
							((BtShooter) en.getKey()).remove();
						} else {
							tmMx = tPls;
						}
					}
					break;
				case CTs:
					if (++ctPls > tmMx) {
						if (en.getKey() instanceof BtShooter) {
							it.remove();
							((BtShooter) en.getKey()).remove();
						} else {
							tmMx = ctPls;
						}
					}
					break;
				case NA:
					break;
				}
			}
			
			for (int i = tPls; i < tmMx; i++) {
				shtrs.put(BotManager.createBot(name, BtShooter.class, () -> new BtShooter(this)), Team.Ts);
			}
			
			for (int i = ctPls; i < tmMx; i++) {
				shtrs.put(BotManager.createBot(name, BtShooter.class, () -> new BtShooter(this)), Team.CTs);
			}
		}
		
		for (final Entry<Shooter, Team> e : shtrs.entrySet()) {
			final Player p = e.getKey().getPlayer();
			if (p != null) {
				Main.chgSbdTm(p.getScoreboard(), "tamt", "", "§4" + String.valueOf(getTmAmt(Team.Ts, true, true)) + (e.getValue() == Team.Ts ? " §7чел. §8✦ Вы" : " §7чел."));
				Main.chgSbdTm(p.getScoreboard(), "ctamt", "", "§3" + String.valueOf(getTmAmt(Team.CTs, true, true)) + (e.getValue() == Team.CTs ? " §7чел. §8✦ Вы" : " §7чел."));
			}
		}
	}

	private Team getMinTm() {
		final int tn = getTmAmt(Team.Ts, true, true);
		final int ctn = getTmAmt(Team.CTs, true, true);
		return tn < ctn ? Team.Ts : (tn == ctn ? (Main.srnd.nextBoolean() ? Team.Ts : Team.CTs) : Team.CTs);
	}
}
