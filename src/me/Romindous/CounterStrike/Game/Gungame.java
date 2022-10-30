package me.Romindous.CounterStrike.Game;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.attribute.Attribute;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Turtle;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import me.Romindous.CounterStrike.Main;
import me.Romindous.CounterStrike.Enums.GameState;
import me.Romindous.CounterStrike.Enums.GunType;
import me.Romindous.CounterStrike.Listeners.MainLis;
import me.Romindous.CounterStrike.Objects.BrknBlck;
import me.Romindous.CounterStrike.Objects.Shooter;
import me.Romindous.CounterStrike.Objects.TripWire;
import me.Romindous.CounterStrike.Utils.Inventories;
import me.Romindous.CounterStrike.Utils.PacketUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.core.BaseBlockPosition;
import net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy;
import ru.komiss77.ApiOstrov;
import ru.komiss77.enums.Stat;

public class Gungame extends Arena {
	
	public final Inventory tms;
	private final GunType[] guns;
	private final byte cycle;
	
	public Gungame(final String name, final byte min, final byte max, final BaseBlockPosition[] TSps, final BaseBlockPosition[] CTSps, final org.bukkit.World w, final boolean rnd) {
		super(name, min, max, TSps, CTSps, w, rnd);
		
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
		sh.kls = 0;
		sh.dths = 0;
		sh.money = 0;
		sh.arena = this;
		Main.nrmlzPl((Player) sh.inv.getHolder(), true);
		switch (gst) {
		case WAITING:
			shtrs.put(sh, Team.NA);
			editLr(tms.getItem(4), true, "§7✦ §7" + sh.nm);
			sh.inv.setItem(2, Main.mkItm(Material.NETHER_STAR, "§eВыбор Комманды", 1));
			sh.inv.setItem(6, Main.mkItm(Material.GHAST_TEAR, "§5Магазин", 1));
			sh.inv.setItem(8, Main.mkItm(Material.SLIME_BALL, "§cВыход", 1));
			if (shtrs.size() == min) {
				for (final Shooter s : shtrs.keySet()) {
					beginScore(s);
					final Player p = (Player) s.inv.getHolder();
					p.sendMessage(Main.prf() + "§7Игрок §5" + sh.nm + " §7зашел на карту!");
					PacketUtils.sendAcBr(p, "§7Игроков: §5" + shtrs.size() + " §7из §5" + max + " §7(макс)!", 30);
					if (!rnd) {
						p.teleport(Main.getNrLoc(Main.srnd.nextBoolean() ? TSps[Main.srnd.nextInt(TSps.length)] : CTSps[Main.srnd.nextInt(CTSps.length)], w));
					}
					PacketUtils.sendNmTg(s, "§7<§d" + name + "§7> ", " §7[-.-]", Team.NA.clr);
					p.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 600, 1));
					Main.shwHdPls(p);
				}
				cntBeg();
			} else {
				final int rm = min - shtrs.size();
				waitScore(sh, rm);
				PacketUtils.sendNmTg(sh, "§7<§d" + name + "§7> ", " §7[-.-]", Team.NA.clr);
				for (final Shooter s : shtrs.keySet()) {
					final Player p = (Player) s.inv.getHolder();
					p.sendMessage(Main.prf() + "§7Игрок §5" + sh.nm + " §7зашел на карту!");
					PacketUtils.sendAcBr(p, "§7Нужно еще §d" + rm + " §7игроков для начала!", 30);
					//Main.chgSbdTm(p.getScoreboard(), "tamt", "", "§4" + String.valueOf(getTmAmt(Team.Ts)) + (e.getValue() == Team.Ts ? " §7чел. §8✦ Вы" : " §7чел."));
					//Main.chgSbdTm(p.getScoreboard(), "ctamt", "", "§3" + String.valueOf(getTmAmt(Team.CTs)) + (e.getValue() == Team.CTs ? " §7чел. §8✦ Вы" : " §7чел."));
					Main.chgSbdTm(p.getScoreboard(), "rmn", "", "§5" + String.valueOf(rm) + (rm > 1 ? " §7игроков" : " §7игрокa"));
				}
			}
			break;
		case BEGINING:
			shtrs.put(sh, Team.NA);
			editLr(tms.getItem(4), true, "§7✦ §7" + sh.nm);
			sh.inv.setItem(2, Main.mkItm(Material.NETHER_STAR, "§eВыбор Комманды", 1));
			sh.inv.setItem(6, Main.mkItm(Material.GHAST_TEAR, "§5Магазин", 1));
			sh.inv.setItem(8, Main.mkItm(Material.SLIME_BALL, "§cВыход", 1));
			if (!rnd) {
				sh.inv.getHolder().teleport(Main.getNrLoc(Main.srnd.nextBoolean() ? TSps[Main.srnd.nextInt(TSps.length)] : CTSps[Main.srnd.nextInt(CTSps.length)], w));
			}
			sh.inv.getHolder().addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, tm * 20, 1));
			beginScore(sh);
			Main.shwHdPls((Player) sh.inv.getHolder());
			PacketUtils.sendNmTg(sh, "§7<§d" + name + "§7> ", " §7[-.-]", Team.NA.clr);
			for (final Entry<Shooter, Team> e : shtrs.entrySet()) {
				final Player p = (Player) e.getKey().inv.getHolder();
				p.sendMessage(Main.prf() + "§7Игрок §5" + sh.nm + " §7зашел на карту!");
				//Main.chgSbdTm(p.getScoreboard(), "tamt", "", "§4" + String.valueOf(getTmAmt(Team.Ts)) + (e.getValue() == Team.Ts ? " §7чел. §8✦ Вы" : " §7чел."));
				//Main.chgSbdTm(p.getScoreboard(), "ctamt", "", "§3" + String.valueOf(getTmAmt(Team.CTs)) + (e.getValue() == Team.CTs ? " §7чел. §8✦ Вы" : " §7чел."));
				PacketUtils.sendAcBr(p, "§7Игроков: §5" + shtrs.size() + " §7из §5" + max + " §7(макс)!", 30);
				PacketUtils.sendRvsNmTg(Main.ds.bh().a(sh.nm).networkManager, e.getValue(), Main.ds.bh().a(p.getName()), e.getValue(), "§7<§d" + name + "§7> ", " §7[-.-]", e.getValue().clr);
			}
			break;
		case BUYTIME:
		case ROUND:
		case ENDRND:
			final Team tm = getMinTm();
			final Player pl = (Player) sh.inv.getHolder();
			shtrs.put(sh, tm);
			addToTm(pl, tm);
			gameScore(sh);
			PacketUtils.sendNmTg(sh, tm.icn + " ", " §7[0-0]", tm.clr);
			for (final Entry<Shooter, Team> e : shtrs.entrySet()) {
				final Player p = (Player) e.getKey().inv.getHolder();
				p.sendMessage(Main.prf() + "§7Игрок §5" + sh.nm + " §7зашел играть за комманду " + tm.icn + "§7!");
				Main.chgSbdTm(p.getScoreboard(), "tamt", "", "§4" + String.valueOf(getTmAmt(Team.Ts, true)) + (e.getValue() == Team.Ts ? " §7чел. §8✦ Вы" : " §7чел."));
				Main.chgSbdTm(p.getScoreboard(), "ctamt", "", "§3" + String.valueOf(getTmAmt(Team.CTs, true)) + (e.getValue() == Team.CTs ? " §7чел. §8✦ Вы" : " §7чел."));
				PacketUtils.sendRvsNmTg(Main.ds.bh().a(pl.getName()).networkManager, tm, Main.ds.bh().a(p.getName()), e.getValue(), e.getValue().icn + " ", " §7[0-0]", e.getValue().clr);
			}
			for (final TripWire tw : tws) {
				if (tm == tw.tm) {
					tw.shwNd(Main.ds.bh().a(sh.nm).b);
				}
			}
			break;
		case FINISH:
			sh.inv.getHolder().sendMessage(Main.prf() + "§cЭта игра уже заканчивается!");
			Main.lobbyPl((Player) sh.inv.getHolder());
			return false;
		}
		Inventories.updtGm(this);
		final byte n = MainLis.getPlaying();
		for (final Player p : Bukkit.getOnlinePlayers()) {
			p.sendPlayerListFooter(Component.text("§7Сейчас в игре: §d" + String.valueOf(n) + "§7 человек!"));
		}
		return true;
	}

	public void addToTm(final Player p, final Team tm) {
		p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
		p.teleport(Main.getNrLoc(tm == Team.Ts ? TSps[Main.srnd.nextInt(TSps.length)] : CTSps[Main.srnd.nextInt(CTSps.length)], w).add(0d, 2d, 0d));
		Main.shwHdPls(p);
		if (gst == GameState.BUYTIME) {
			final PlayerInventory pinv = p.getInventory();
			p.setGameMode(GameMode.SURVIVAL);
			pinv.setItem(2, Main.mkItm(Material.BLAZE_ROD, "§fНож \u9298", 1));
			if (tm == Team.Ts) {
				pinv.setHelmet(Inventories.TShop.getItem(GunType.hlmtSlt).clone());
				pinv.setChestplate(Inventories.TShop.getItem(GunType.chstSlt).clone());
			} else {
				pinv.setHelmet(Inventories.CTShop.getItem(GunType.hlmtSlt).clone());
				pinv.setChestplate(Inventories.CTShop.getItem(GunType.chstSlt).clone());
			}
			p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 250, 250, true, false, false));
			p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 40, 2, true, false, false));
		} else {
			p.setGameMode(GameMode.SPECTATOR);
			p.getInventory().setItem(8, Main.mkItm(Material.REDSTONE, "§cВыход", 1));
		}
	}

	@Override
	public boolean rmvPl(final Shooter sh) {
		final Team tm = shtrs.remove(sh);
		if (tm == null) {
			sh.inv.getHolder().sendMessage(Main.prf() + "§cВы не находитесь в игре!");
			return false;
		}
		switch (tm) {
		case Ts:
			editLr(tms.getItem(1), false, "§4✦ §7" + sh.nm);
			break;
		case CTs:
			editLr(tms.getItem(7), false, "§3✦ §7" + sh.nm);
			break;
		case NA:
			editLr(tms.getItem(4), false, "§7✦ §7" + sh.nm);
			break;
		}
		sh.kls = 0;
		sh.dths = 0;
		sh.money = 0;
		switch (gst) {
		case WAITING:
			if (shtrs.size() > 0) {
				final int rm = min - shtrs.size();
				for (final Entry<Shooter, Team> e : shtrs.entrySet()) {
					final Player p = (Player) e.getKey().inv.getHolder();
					p.sendMessage(Main.prf() + "§7Игрок §5" + sh.nm + " §7вышел с карты!");
					PacketUtils.sendAcBr(p, "§7Нужно еще §d" + rm + " §7игроков для начала!", 30);
					final Scoreboard sb = p.getScoreboard();
					Main.chgSbdTm(p.getScoreboard(), "tamt", "", "§4" + String.valueOf(getTmAmt(Team.Ts, true)) + (e.getValue() == Team.Ts ? " §7чел. §8✦ Вы" : " §7чел."));
					Main.chgSbdTm(p.getScoreboard(), "ctamt", "", "§3" + String.valueOf(getTmAmt(Team.CTs, true)) + (e.getValue() == Team.CTs ? " §7чел. §8✦ Вы" : " §7чел."));
					Main.chgSbdTm(sb, "rmn", "", "§5" + String.valueOf(rm) + (rm > 1 ? " §7игроков" : " §7игрокa"));
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
					waitScore(s, min - 1);
					final Player pl = (Player) s.inv.getHolder();
					pl.teleport(Main.getNrLoc(Main.lobby, Main.lbbyW));
					pl.sendMessage(Main.prf() + "§7Игрок §5" + sh.nm + " §7вышел с карты,\n§7Слишком мало игроков для начала!");
					PacketUtils.sendAcBr(pl, "§7Игроков: §5" + shtrs.size() + " §7из §5" + max + " §7(макс)!", 30);
					pl.removePotionEffect(PotionEffectType.GLOWING);
				}
			} else {
				for (final Entry<Shooter, Team> e : shtrs.entrySet()) {
					final Player p = (Player) e.getKey().inv.getHolder();
					p.sendMessage(Main.prf() + "§7Игрок §5" + sh.nm + " §7вышел с карты!");
					PacketUtils.sendAcBr(p, "§7Игроков: §5" + shtrs.size() + " §7из §5" + max + " §7(макс)!", 30);
					Main.chgSbdTm(p.getScoreboard(), "tamt", "", "§4" + String.valueOf(getTmAmt(Team.Ts, true)) + (e.getValue() == Team.Ts ? " §7чел. §8✦ Вы" : " §7чел."));
					Main.chgSbdTm(p.getScoreboard(), "ctamt", "", "§3" + String.valueOf(getTmAmt(Team.CTs, true)) + (e.getValue() == Team.CTs ? " §7чел. §8✦ Вы" : " §7чел."));
				}
			}
			break;
		case BUYTIME:
		case ROUND:
			dropIts(sh.inv, sh.inv.getHolder().getLocation(), tm);
		case ENDRND:
			for (final TripWire tw : tws) {
				if (tm == tw.tm) {
					Main.ds.bh().a(sh.nm).b.a(new PacketPlayOutEntityDestroy(tw.eif.ae()));
				}
			}
			if (getTmAmt(Team.CTs, false) == 0 || getTmAmt(Team.Ts, false) == 0) {
				if (tsk != null) {
					tsk.cancel();
				}
				for (final Shooter s : shtrs.keySet()) {
					winScore(s, tm == Team.Ts);
					s.inv.getHolder().sendMessage(Main.prf() + "§7Игрок §5" + sh.nm + " §7вышел из игры!");
				}
				cntFnsh(tm.getOpst());
			} else {
				for (final Entry<Shooter, Team> e : shtrs.entrySet()) {
					final Player p = (Player) e.getKey().inv.getHolder();
					p.sendMessage(Main.prf() + "§7Игрок §5" + sh.nm + " §7вышел из игры!");
					Main.chgSbdTm(p.getScoreboard(), "tamt", "", "§4" + String.valueOf(getTmAmt(Team.Ts, true)) + (e.getValue() == Team.Ts ? " §7чел. §8✦ Вы" : " §7чел."));
					Main.chgSbdTm(p.getScoreboard(), "ctamt", "", "§3" + String.valueOf(getTmAmt(Team.CTs, true)) + (e.getValue() == Team.CTs ? " §7чел. §8✦ Вы" : " §7чел."));
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
		tm = 30;
		gst = GameState.BEGINING;
		ApiOstrov.sendArenaData(this.name, ru.komiss77.enums.GameState.СТАРТ, "§7[§5CS§7]", "§dЭстафета", " ", "§7Игроков: §5" + shtrs.size() + "§7/§5" + this.max, "", shtrs.size());
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
					cntPrep();
					if (rnd) {
						Main.mapBlds.get(name).placeSets(w, 5);
					}
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
			final Player p = (Player) sh.inv.getHolder();
			Main.nrmlzPl(p, true);
			gameScore(e.getKey());
			updateWeapon(sh, p);
			PacketUtils.sendNmTg(e.getKey(), e.getValue().icn + " ", " §7[" + sh.kls + "-" + sh.dths + "]", e.getValue().clr);
			p.setGameMode(GameMode.SURVIVAL);
			sh.inv.setItem(2, Main.mkItm(Material.BLAZE_ROD, "§fНож \u9298", 1));
			switch (e.getValue()) {
			case Ts:
				sh.inv.setHelmet(Inventories.TShop.getItem(GunType.hlmtSlt).clone());
				sh.inv.setChestplate(Inventories.TShop.getItem(GunType.chstSlt).clone());
				p.teleport(Main.getNrLoc(TSps[Main.srnd.nextInt(TSps.length)], w).add(0d, 2d, 0d));
				break;
			case CTs:
				sh.inv.setHelmet(Inventories.CTShop.getItem(GunType.hlmtSlt).clone());
				sh.inv.setChestplate(Inventories.CTShop.getItem(GunType.chstSlt).clone());
				p.teleport(Main.getNrLoc(CTSps[Main.srnd.nextInt(CTSps.length)], w).add(0d, 2d, 0d));
				break;
			case NA:
				break;
			}
			p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 250, 250, true, false, false));
			p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 40, 2, true, false, false));
			/*final Turtle tl = (Turtle) p.getWorld().spawnEntity(p.getLocation().add(0, 0.5d, 0), EntityType.TURTLE);
			tl.setRemoveWhenFarAway(false);
			tl.setPersistent(true);
			tl.setInvulnerable(true);
			tl.setCollidable(false);
			tl.setInvisible(true);
			tl.setSilent(true);
			tl.setBaby();
			tl.setGravity(false);
			tl.setTicksLived(1);
			tl.addPassenger(p);
			tl.setAI(true);*/
			PacketUtils.sendTtlSbTtl(p, "", "§l§eПодготовка Оружейни...", 30);
		}
		
		tsk = new BukkitRunnable() {
			@Override
			public void run() {
				final String t = getTime(tm, ChatColor.DARK_PURPLE);
				for (final LivingEntity le : w.getLivingEntities()) {
					le.setFireTicks(-1);
				}
				switch (tm) {
				case 3:
				case 2:
				case 1:
					for (final Shooter sh : shtrs.keySet()) {
						final Player p = (Player) sh.inv.getHolder();
						p.playSound(p.getLocation(), Sound.BLOCK_DISPENSER_FAIL, 0.8f, 1.2f);
						PacketUtils.sendSbTtl(p, "§d§l" + tm, 10);
						Main.chgSbdTm(p.getScoreboard(), "tm", t, "");
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
						Main.chgSbdTm(((Player) sh.inv.getHolder()).getScoreboard(), "tm", t, "");
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
			final Player p = (Player) e.getKey().inv.getHolder();
			//final Entity v = p.getVehicle();
			//v.eject(); v.remove();
			PacketUtils.sendSbTtl(p, (e.getValue() == Team.Ts ? "§4" : "§3") + "§lВперед", 30);
			Main.chgSbdTm(p.getScoreboard(), "gst", "", "§dБой");
			p.playSound(p.getLocation(), "cs.info." + (e.getValue() == Team.Ts ? "tfight" : "ctfight"), 10f, 1f);
			p.removePotionEffect(PotionEffectType.SLOW);
		}
		tsk = new BukkitRunnable() {
			@Override
			public void run() {
				switch (tm) {
				case 60:
					for (final Shooter sh : shtrs.keySet()) {
						PacketUtils.sendAcBr(((Player) sh.inv.getHolder()), "§7Осталась §d1 §7минута!", 30);
						Main.chgSbdTm(((Player) sh.inv.getHolder()).getScoreboard(), "tm", getTime(tm, ChatColor.LIGHT_PURPLE), "");
					}
					break;
				case 30:
					for (final Entry<Shooter, Team> e : shtrs.entrySet()) {
						final Player p = (Player) e.getKey().inv.getHolder();
						PacketUtils.sendAcBr(p, "§7Осталось §d" + tm + " §7секунд!", 30);
						p.playSound(p.getLocation(), "cs.info." + (e.getValue() == Team.Ts ? "t30sec" : "ct30sec"), 10f, 1f);
						Main.chgSbdTm(p.getScoreboard(), "tm", getTime(tm, ChatColor.LIGHT_PURPLE), "");
					}
					break;
				case 10:
					for (final Shooter sh : shtrs.keySet()) {
						PacketUtils.sendAcBr(((Player) sh.inv.getHolder()), "§7Осталось §d" + tm + " §7секунд!", 30);
						Main.chgSbdTm(((Player) sh.inv.getHolder()).getScoreboard(), "tm", getTime(tm, ChatColor.LIGHT_PURPLE), "");
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
						Main.chgSbdTm(((Player) sh.inv.getHolder()).getScoreboard(), "tm", getTime(tm, ChatColor.LIGHT_PURPLE), "");
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
		
		for (final BrknBlck b : brkn) {
			b.getBlock().setBlockData(b.bd, false);
		}
		brkn.clear();
		
		Shooter bt = null;
		Shooter bct = null;
		for (final Entry<Shooter, Team> e : shtrs.entrySet()) {

			switch (e.getValue()) {
			case Ts:
				if (bt == null || bt.kls < e.getKey().kls) {
					bt = e.getKey();
				}
				break;
			case CTs:
				if (bct == null || bct.kls < e.getKey().kls) {
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
			(bct == null ? "Никто" : bct.nm) + "\n          §7--=x=--\n§7Самый злобный террорист: §4" + 
			(bt == null ? "Никто" : bt.nm) + "\n§5=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-";
			st = "§4§lТеррористы §7выйграли!";
			break;
		case CTs:
			msg = " \n§7Победа в раунде: §3Спецназ\n \n§5=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-\n§7Лучший спецназовец: §3" + 
			(bct == null ? "Никто" : bct.nm) + "\n          §7--=x=--\n§7Самый злобный террорист: §4" + 
			(bt == null ? "Никто" : bt.nm) + "\n§5=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-";
			st = "§3§lСпецназ §7одержал победу!";
			break;
		default:
			msg = " ";
			st = " ";
			break;
		}
		
		for (final Entry<Shooter, Team> e : shtrs.entrySet()) {
			final Shooter sh = e.getKey();
			final Player p = (Player) sh.inv.getHolder();
			p.closeInventory();
			Main.chgSbdTm(p.getScoreboard(), "tm", "§d00:00", "");
			p.playSound(p.getLocation(), "cs.info." + (e.getValue() == Team.Ts ? "tfinish" : "ctfinish"), 10f, 1f);
			p.sendMessage(msg);
			PacketUtils.sendTtlSbTtl(p, "§5Финиш", st, 40);
			PacketUtils.sendNmTg(sh, "§7<§d" + name + "§7> ", " §7[" + sh.kls + "-" + sh.dths + "]", Team.NA.clr);
			ApiOstrov.moneyChange(sh.nm, sh.kls * 6, "Убийства");
			sh.inv.clear();
			winScore(sh, wn == Team.CTs);
			
			if (e.getValue() == wn) {
				ApiOstrov.addStat((Player) e.getKey().inv.getHolder(), Stat.CS_win);
				ApiOstrov.moneyChange(e.getKey().nm, 60, "Игра");
			} else {
				ApiOstrov.addStat((Player) e.getKey().inv.getHolder(), Stat.CS_loose);
			}
			ApiOstrov.addStat(p, Stat.CS_game);
		}
		
		for (final TripWire tw : tws) {
			tw.rmv(this);
		}
		
		tws.clear();
		tsk = new BukkitRunnable() {
			@Override
			public void run() {
				final String t = getTime(tm, ChatColor.DARK_PURPLE);
				if (tm == 0) {
					if (tsk != null) {
						tsk.cancel();
					}
					
					ApiOstrov.sendArenaData(name, ru.komiss77.enums.GameState.ОЖИДАНИЕ, "§7[§5CS§7]", "§dЭстафета", " ", "§7Игроков: §50§7/§5" + min, "", 0);
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
						final Player p = (Player) e.getKey().inv.getHolder();
						Main.chgSbdTm(p.getScoreboard(), "tm", t, "");
						if (e.getValue() == wn) {
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

	private void waitScore(final Shooter sh, final int rm) {
		final Scoreboard sb = Main.smg.getNewScoreboard();
		final Objective ob = sb.registerNewObjective("CS:GO", "", "§7[§5CS:GO§7]");
		ob.setDisplaySlot(DisplaySlot.SIDEBAR);
		ob.getScore("    ")
		.setScore(11);
		ob.getScore("§7Карта: §5" + name)
		.setScore(10);
		ob.getScore("§7Комманды:")
		.setScore(9);
		ob.getScore("§7=-=-=-=-=-=-=-=-")
		.setScore(8);
		Main.crtSbdTm(sb, "tamt", "", "§4\u9265 §7: ", "§4" + String.valueOf(getTmAmt(Team.Ts, true)) + " §7чел.");
		ob.getScore("§4\u9265 §7: ")
		.setScore(7);
		ob.getScore("   ")
		.setScore(6);
		Main.crtSbdTm(sb, "ctamt", "", "§3\u9264 §7: ", "§3" + String.valueOf(getTmAmt(Team.CTs, true)) + " §7чел.");
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
		((Player) sh.inv.getHolder()).setScoreboard(sb);
	}

	private void beginScore(final Shooter sh) {
		final Scoreboard sb = Main.smg.getNewScoreboard();
		final Objective ob = sb.registerNewObjective("CS:GO", "", "§7[§5CS:GO§7]");
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
		Main.crtSbdTm(sb, "tamt", "", "§4\u9265 §7: ", "§4" + String.valueOf(getTmAmt(Team.Ts, true)) + (shtrs.get(sh) == Team.Ts ? " §7чел. §8✦ Вы" : " §7чел."));
		ob.getScore("§4\u9265 §7: ")
		.setScore(7);
		ob.getScore("   ")
		.setScore(6);
		Main.crtSbdTm(sb, "ctamt", "", "§3\u9264 §7: ", "§3" + String.valueOf(getTmAmt(Team.CTs, true)) + (shtrs.get(sh) == Team.CTs ? " §7чел. §8✦ Вы" : " §7чел."));
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
		((Player) sh.inv.getHolder()).setScoreboard(sb);
	}
	
	private void gameScore(final Shooter sh) {
		final Scoreboard sb = Main.smg.getNewScoreboard();
		final org.bukkit.scoreboard.Team t = sb.registerNewTeam("ind");
		t.setColor(ChatColor.DARK_RED);
		final Objective ob = sb.registerNewObjective("CS:GO", "", "§7[§5CS:GO§7]");
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
		Main.crtSbdTm(sb, "tm", getTime(tm, ChatColor.LIGHT_PURPLE), " §7до конца!", "");
		ob.getScore(" §7до конца!")
		.setScore(8);
		ob.getScore("   ")
		.setScore(7);
		Main.crtSbdTm(sb, "tamt", "", "§4\u9265 §7: ", "§4" + String.valueOf(getTmAmt(Team.Ts, true)) + (shtrs.get(sh) == Team.Ts ? " §7чел. §8✦ Вы" : " §7чел."));
		ob.getScore("§4\u9265 §7: ")
		.setScore(6);
		Main.crtSbdTm(sb, "ctamt", "", "§3\u9264 §7: ", "§3" + String.valueOf(getTmAmt(Team.CTs, true)) + (shtrs.get(sh) == Team.CTs ? " §7чел. §8✦ Вы" : " §7чел."));
		ob.getScore("§3\u9264 §7: ")
		.setScore(5);
		ob.getScore("  ")
		.setScore(4);
		Main.crtSbdTm(sb, "prg", "", "§7Прогрес: ", "§d" + String.valueOf(sh.money) + "§7/§d" + String.valueOf(cycle));
		ob.getScore("§7Прогрес: ")
		.setScore(3);
		ob.getScore("§7=-=-=-=-=-=-=-")
		.setScore(2);
		ob.getScore(" ")
		.setScore(1);
		
		ob.getScore("§e   ostrov77.su")
		.setScore(0);
		((Player) sh.inv.getHolder()).setScoreboard(sb);
	}

	private int getTmProg(final Team tm) {
		int stage = 0;
		for (final Entry<Shooter, Team> e : shtrs.entrySet()) {
			if (e.getValue() == tm) {
				stage = Math.max(stage, e.getKey().money);
			}
		}
		return stage;
	}

	private void winScore(final Shooter sh, final boolean isCTWn) {
		final Scoreboard sb = Main.smg.getNewScoreboard();
		final Objective ob = sb.registerNewObjective("CS:GO", "", "§7[§5CS:GO§7]");
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
		ob.getScore("§4\u9265 §7: §4" + String.valueOf(getTmAmt(Team.Ts, true)) + (shtrs.get(sh) == Team.Ts ? " §7чел. §8✦ Вы" : " §7чел."))
		.setScore(6);
		ob.getScore("§3\u9264 §7: §3" + String.valueOf(getTmAmt(Team.CTs, true)) + (shtrs.get(sh) == Team.CTs ? " §7чел. §8✦ Вы" : " §7чел."))
		.setScore(5);
		ob.getScore("  ")
		.setScore(4);
		ob.getScore("§7Прогрес: §d" + sh.money + "§7/§d" + String.valueOf(cycle))
		.setScore(3);
		ob.getScore("§7=-=-=-=-=-=-=-")
		.setScore(2);
		ob.getScore(" ")
		.setScore(1);
		
		ob.getScore("§e   ostrov77.su")
		.setScore(0);
		((Player) sh.inv.getHolder()).setScoreboard(sb);
	}

	@Override
	public void killPl(final Shooter sh) {
		final Player p = (Player) sh.inv.getHolder();
		p.setHealth(p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());
		if (gst == GameState.ROUND) {
			addDth(sh);
			dropIts(sh.inv, p.getLocation(), shtrs.get(sh));
		} else {
			if (p.hasPotionEffect(PotionEffectType.GLOWING)) {
				Main.nrmlzPl(p, false);
				p.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, tm * 20, 1));
			} else {
				Main.nrmlzPl(p, false);
			}
		}
		p.teleport(Main.getNrLoc(shtrs.get(sh) == Team.Ts ? TSps[Main.srnd.nextInt(TSps.length)] : CTSps[Main.srnd.nextInt(CTSps.length)], w));
	}

	public void dropIts(final PlayerInventory inv, final Location loc, final Team tm) {
		ItemStack it = inv.getItem(3);
		if (it != null) {
			w.dropItemNaturally(loc, it);
		}
		it = inv.getItem(4);
		if (it != null) {
			if (it.getAmount() == 1) {
				w.dropItemNaturally(loc, it);
			} else {
				it.setAmount(1);
				w.dropItemNaturally(loc, it);
				w.dropItemNaturally(loc, it);
			}
		}
		inv.setItem(3, Main.air.clone());
		inv.setItem(4, Main.air.clone());
	}
	
	private void updateWeapon(final Shooter sh, final Player p) {
		if (sh.money == guns.length) {
			cntFnsh(shtrs.get(sh));
			return;
		}

		Main.chgSbdTm(p.getScoreboard(), "scr", "§l§4\u9265 §7: §4" + getTmProg(Team.Ts) + "§7/§4" + cycle, 
			"§l§3" + getTmProg(Team.CTs) + "§7/§3" + cycle + " §7: §3\u9264");
		final GunType gt = guns[sh.money];
		sh.inv.setItem(0, Main.air.clone());
		sh.inv.setItem(1, Main.air.clone());
		Main.chgSbdTm(p.getScoreboard(), "prg", "", "§d" + 
			String.valueOf(sh.money) + "§7/§d" + String.valueOf(cycle));
		if (gt == null) {
			PacketUtils.sendTtlSbTtl(p, "", "§7Последний §dцыкл §7оружия!", 20);
			return;
		}
		
		sh.inv.setItem(gt.prm ? 0 : 1, Inventories.CTShop.getItem(gt.slt).clone().asQuantity(gt.amo));
		PacketUtils.zoom(p, false);
		p.setNoDamageTicks(10);
	}

	@Override
	public void addKll(final Shooter sh) {
		sh.kls++;
		sh.money++;
		final Player p = (Player) sh.inv.getHolder();
		p.setHealth(p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());
		PacketUtils.sendNmTg(sh, shtrs.get(sh).icn + " ", " §7[" + sh.kls + "-" + sh.dths + "]", shtrs.get(sh).clr);
		ApiOstrov.addStat(p, Stat.CS_kill);
		updateWeapon(sh, p);
	}

	@Override
	public void addDth(final Shooter sh) {
		sh.dths++;
		sh.money -= sh.money == 0 ? 0 : 1;
		final Player p = (Player) sh.inv.getHolder();
		p.setHealth(p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());
		PacketUtils.sendNmTg(sh, shtrs.get(sh).icn + " ", " §7[" + sh.kls + "-" + sh.dths + "]", shtrs.get(sh).clr);
		ApiOstrov.addStat(p, Stat.CS_death);
		updateWeapon(sh, p);
	}
	
	public byte getTmAmt(final Team tm, final boolean alv) {
		byte n = 0;
		for (final Entry<Shooter, Team> e : shtrs.entrySet()) {
			if (e.getValue() == tm && (!alv || e.getKey().inv.getHolder().getGameMode() == GameMode.SURVIVAL)) {
				n++;
			}
		}
		return n;
	}

	@Override
	public void chngTm(final Shooter sh, final Team nv) {
		if (nv != null && ( (shtrs.get(sh) == nv.getOpst() && getTmAmt(nv, true) >= getTmAmt(nv.getOpst(), true)) || getTmAmt(nv, true) > getTmAmt(nv.getOpst(), true) )) {
			sh.inv.getHolder().sendMessage(Main.prf() + "§cВ этой комманде слишком много игроков!");
			return;
		}
		final Team tm = shtrs.replace(sh, nv);
		if (nv == Team.NA || tm != nv) {
			for (final Entry<Shooter, Team> e : shtrs.entrySet()) {
				Main.chgSbdTm(((Player) e.getKey().inv.getHolder()).getScoreboard(), "tamt", "", "§4" + String.valueOf(getTmAmt(Team.Ts, true)) + (e.getValue() == Team.Ts ? " §7чел. §8✦ Вы" : " §7чел."));
				Main.chgSbdTm(((Player) e.getKey().inv.getHolder()).getScoreboard(), "ctamt", "", "§3" + String.valueOf(getTmAmt(Team.CTs, true)) + (e.getValue() == Team.CTs ? " §7чел. §8✦ Вы" : " §7чел."));
			}
			PacketUtils.sendNmTg(sh, "§7<§d" + name + "§7> ", " §7[-.-]", nv.clr);
			switch (tm) {
			case Ts:
				editLr(tms.getItem(1), false, "§4✦ §7" + sh.nm);
				break;
			case CTs:
				editLr(tms.getItem(7), false, "§3✦ §7" + sh.nm);
				break;
			case NA:
				editLr(tms.getItem(4), false, "§7✦ §7" + sh.nm);
				break;
			}
			switch (nv) {
			case Ts:
				editLr(tms.getItem(1), true, "§4✦ §7" + sh.nm);
				break;
			case CTs:
				editLr(tms.getItem(7), true, "§3✦ §7" + sh.nm);
				break;
			case NA:
				editLr(tms.getItem(4), true, "§7✦ §7" + sh.nm);
				break;
			}
		}
	}

	private void editLr(final ItemStack it, final boolean add, final String lmnt) {
		final ItemMeta im = it.getItemMeta();
		final LinkedList<String> lr = new LinkedList<String>(im.getLore());
		if (add) {
			lr.add(lmnt);
		} else {
			lr.remove(lmnt);
		}
		im.setLore(lr);
		it.setItemMeta(im);
	}

	private void blncTms() {
		for (final Entry<Shooter, Team> e : shtrs.entrySet()) {
			if (e.getValue() == Team.NA) {
				e.setValue(getMinTm());
			}
		}
		for (final Entry<Shooter, Team> e : shtrs.entrySet()) {
			Main.chgSbdTm(((Player) e.getKey().inv.getHolder()).getScoreboard(), "tamt", "", "§4" + String.valueOf(getTmAmt(Team.Ts, true)) + (e.getValue() == Team.Ts ? " §7чел. §8✦ Вы" : " §7чел."));
			Main.chgSbdTm(((Player) e.getKey().inv.getHolder()).getScoreboard(), "ctamt", "", "§3" + String.valueOf(getTmAmt(Team.CTs, true)) + (e.getValue() == Team.CTs ? " §7чел. §8✦ Вы" : " §7чел."));
		}
	}

	private Team getMinTm() {
		final byte tn = getTmAmt(Team.Ts, false);
		final byte ctn = getTmAmt(Team.CTs, false);
		return tn < ctn ? Team.Ts : (tn == ctn ? (Main.srnd.nextBoolean() ? Team.Ts : Team.CTs) : Team.CTs);
	}
}
