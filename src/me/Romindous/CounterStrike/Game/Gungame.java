package me.Romindous.CounterStrike.Game;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
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
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import me.Romindous.CounterStrike.Main;
import me.Romindous.CounterStrike.Enums.GameState;
import me.Romindous.CounterStrike.Enums.GameType;
import me.Romindous.CounterStrike.Enums.GunType;
import me.Romindous.CounterStrike.Enums.NadeType;
import me.Romindous.CounterStrike.Listeners.MainLis;
import me.Romindous.CounterStrike.Objects.Shooter;
import me.Romindous.CounterStrike.Objects.Game.PlShooter;
import me.Romindous.CounterStrike.Objects.Game.TripWire;
import me.Romindous.CounterStrike.Objects.Loc.BrknBlck;
import me.Romindous.CounterStrike.Objects.Skins.Quest;
import me.Romindous.CounterStrike.Objects.Skins.SkinQuest;
import me.Romindous.CounterStrike.Utils.Inventories;
import me.Romindous.CounterStrike.Utils.PacketUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.core.BaseBlockPosition;
import net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy;
import ru.komiss77.ApiOstrov;
import ru.komiss77.enums.Stat;
import ru.komiss77.utils.ItemBuilder;

public class Gungame extends Arena {
	
	public final Inventory tms;
	private final GunType[] guns;
	private final byte cycle;
	
	public Gungame(final String name, final byte min, final byte max, 
		final BaseBlockPosition[] TSps, final BaseBlockPosition[] CTSps, final org.bukkit.World w, 
		final BaseBlockPosition[] TPss, final BaseBlockPosition[] CTPss, final boolean rnd, final boolean bots) {
		super(name, min, max, TSps, CTSps, w, TPss, CTPss, rnd, bots);
		
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
		if (p == null) {
			
		} else {
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
						if (pl == null) {
							
						} else {
							beginScore(s, pl);
							pl.sendMessage(Main.prf() + "§7Игрок §5" + sh.name() + " §7зашел на карту!");
							PacketUtils.sendAcBr(pl, "§7Игроков: §5" + shtrs.size() + " §7из §5" + max + " §7(макс)!", 30);
							if (!rnd) {
								pl.teleport(Main.getNrLoc(Main.srnd.nextBoolean() ? Main.rndElmt(TPss) : Main.rndElmt(CTPss), w));
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
						if (pl == null) {
							
						} else {
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
					p.teleport(Main.getNrLoc(Main.srnd.nextBoolean() ? Main.rndElmt(TPss) : Main.rndElmt(CTPss), w));
				}
				p.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, tm * 20, 1));
				beginScore(sh, p);
				Main.shwHdPls(p);
				PacketUtils.sendNmTg(sh, "§7<§d" + name + "§7> ", " §7[-.-]", Team.NA.clr);
				for (final Entry<Shooter, Team> e : shtrs.entrySet()) {
					final Player pl = e.getKey().getPlayer();
					if (pl == null) {
						
					} else {
						pl.sendMessage(Main.prf() + "§7Игрок §5" + sh.name() + " §7зашел на карту!");
						//Main.chgSbdTm(p.getScoreboard(), "tamt", "", "§4" + String.valueOf(getTmAmt(Team.Ts)) + (e.getValue() == Team.Ts ? " §7чел. §8✦ Вы" : " §7чел."));
						//Main.chgSbdTm(p.getScoreboard(), "ctamt", "", "§3" + String.valueOf(getTmAmt(Team.CTs)) + (e.getValue() == Team.CTs ? " §7чел. §8✦ Вы" : " §7чел."));
						PacketUtils.sendAcBr(pl, "§7Игроков: §5" + shtrs.size() + " §7из §5" + max + " §7(макс)!", 30);
						PacketUtils.sendNmTg(PacketUtils.getNMSPl(p).networkManager, pl, "§7<§d" + name + "§7> ", " §7[-.-]", true, e.getValue().clr);
					}
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
					final Player pl = e.getKey().getPlayer();
					if (pl == null) {
						
					} else {
						pl.sendMessage(Main.prf() + "§7Игрок §5" + sh.name() + " §7зашел играть за комманду " + tm.icn + "§7!");
						Main.chgSbdTm(pl.getScoreboard(), "tamt", "", "§4" + String.valueOf(getTmAmt(Team.Ts)) + (e.getValue() == Team.Ts ? " §7чел. §8✦ Вы" : " §7чел."));
						Main.chgSbdTm(pl.getScoreboard(), "ctamt", "", "§3" + String.valueOf(getTmAmt(Team.CTs)) + (e.getValue() == Team.CTs ? " §7чел. §8✦ Вы" : " §7чел."));
						PacketUtils.sendNmTg(PacketUtils.getNMSPl(p).networkManager, pl, e.getValue().icn + " ", " §7[0-0]", e.getValue() == tm, e.getValue().clr);
					}
				}
				for (final TripWire tw : tws) {
					if (tm == tw.tm) {
						tw.shwNd(PacketUtils.getNMSPl(p).b);
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
		p.teleport(Main.getNrLoc(tm == Team.Ts ? Main.rndElmt(TPss) : Main.rndElmt(CTPss), w).add(0d, 2d, 0d));
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
		if (p == null) {
			
		} else {
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
						if (pl == null) {
							
						} else {
							pl.sendMessage(Main.prf() + "§7Игрок §5" + sh.name() + " §7вышел с карты!");
							PacketUtils.sendAcBr(pl, "§7Нужно еще §d" + rm + " §7игроков для начала!", 30);
							final Scoreboard sb = pl.getScoreboard();
							Main.chgSbdTm(pl.getScoreboard(), "tamt", "", "§4" + String.valueOf(getTmAmt(Team.Ts)) + (e.getValue() == Team.Ts ? " §7чел. §8✦ Вы" : " §7чел."));
							Main.chgSbdTm(pl.getScoreboard(), "ctamt", "", "§3" + String.valueOf(getTmAmt(Team.CTs)) + (e.getValue() == Team.CTs ? " §7чел. §8✦ Вы" : " §7чел."));
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
							pl.teleport(Main.getNrLoc(Main.lobby, Main.lbbyW));
							pl.sendMessage(Main.prf() + "§7Игрок §5" + sh.name() + " §7вышел с карты,\n§7Слишком мало игроков для начала!");
							PacketUtils.sendAcBr(pl, "§7Игроков: §5" + shtrs.size() + " §7из §5" + max + " §7(макс)!", 30);
							pl.removePotionEffect(PotionEffectType.GLOWING);
						}
					}
				} else {
					for (final Entry<Shooter, Team> e : shtrs.entrySet()) {
						final Player pl = e.getKey().getPlayer();
						if (pl == null) {
							
						} else {
							pl.sendMessage(Main.prf() + "§7Игрок §5" + sh.name() + " §7вышел с карты!");
							PacketUtils.sendAcBr(pl, "§7Игроков: §5" + shtrs.size() + " §7из §5" + max + " §7(макс)!", 30);
							Main.chgSbdTm(pl.getScoreboard(), "tamt", "", "§4" + String.valueOf(getTmAmt(Team.Ts)) + (e.getValue() == Team.Ts ? " §7чел. §8✦ Вы" : " §7чел."));
							Main.chgSbdTm(pl.getScoreboard(), "ctamt", "", "§3" + String.valueOf(getTmAmt(Team.CTs)) + (e.getValue() == Team.CTs ? " §7чел. §8✦ Вы" : " §7чел."));
						}
					}
				}
				break;
			case BUYTIME:
			case ROUND:
				for (final TripWire tw : tws) {
					if (tm == tw.tm) {
						PacketUtils.getNMSPl(p).b.a(new PacketPlayOutEntityDestroy(tw.eif.ae()));
					}
				}
				if (getTmAmt(Team.CTs) == 0 || getTmAmt(Team.Ts) == 0) {
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
						if (pl == null) {
							
						} else {
							pl.sendMessage(Main.prf() + "§7Игрок §5" + sh.name() + " §7вышел из игры!");
							Main.chgSbdTm(pl.getScoreboard(), "tamt", "", "§4" + String.valueOf(getTmAmt(Team.Ts)) + (e.getValue() == Team.Ts ? " §7чел. §8✦ Вы" : " §7чел."));
							Main.chgSbdTm(pl.getScoreboard(), "ctamt", "", "§3" + String.valueOf(getTmAmt(Team.CTs)) + (e.getValue() == Team.CTs ? " §7чел. §8✦ Вы" : " §7чел."));
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
						if (p == null) {
							
						} else {
							p.playSound(p.getLocation(), Sound.BLOCK_DISPENSER_FAIL, 0.8f, 1.2f);
							PacketUtils.sendSbTtl(p, "§5§l" + tm, 10);
							Main.chgSbdTm(p.getScoreboard(), "rmn", "", "§5" + String.valueOf(tm));
						}
					}
					break;
				case 3, 2, 1:
					for (final Shooter sh : shtrs.keySet()) {
						final Player p = sh.getPlayer();
						if (p == null) {
							
						} else {
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
						if (p == null) {
							
						} else {
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
			if (p == null) {
				
			} else {
				Main.nrmlzPl(p, true);
				gameScore(sh, p);
				updateWeapon(sh, p);
				PacketUtils.sendNmTg(e.getKey(), e.getValue().icn + " ", " §7[" + sh.kills() + "-" + sh.deaths() + "]", e.getValue().clr);
				p.setGameMode(GameMode.SURVIVAL);
				sh.item(Main.mkItm(Material.BLAZE_ROD, "§fНож \u9298", 10), 2);
				switch (e.getValue()) {
				case Ts:
					sh.item(Inventories.TShop.getItem(GunType.hlmtSlt).clone(), EquipmentSlot.HEAD);
					sh.item(Inventories.TShop.getItem(GunType.chstSlt).clone(), EquipmentSlot.CHEST);
					p.teleport(Main.getNrLoc(Main.rndElmt(TPss), w).add(0d, 2d, 0d));
					break;
				case CTs:
					sh.item(Inventories.CTShop.getItem(GunType.hlmtSlt).clone(), EquipmentSlot.HEAD);
					sh.item(Inventories.CTShop.getItem(GunType.chstSlt).clone(), EquipmentSlot.CHEST);
					p.teleport(Main.getNrLoc(Main.rndElmt(CTPss), w).add(0d, 2d, 0d));
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
		}
		
		tsk = new BukkitRunnable() {
			@Override
			public void run() {
				final String t = getTime(tm, ChatColor.DARK_PURPLE);
				for (final LivingEntity le : w.getLivingEntities()) {
					le.setFireTicks(-1);
				}
				switch (tm) {
				case 3, 2, 1:
					for (final Shooter sh : shtrs.keySet()) {
						final Player p = sh.getPlayer();
						if (p == null) {
							
						} else {
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
						if (p == null) {
							
						} else {
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
			final Player p = e.getKey().getPlayer();
			if (p == null) {
				
			} else {
				//final Entity v = p.getVehicle();
				//v.eject(); v.remove();
				PacketUtils.sendSbTtl(p, (e.getValue() == Team.Ts ? "§4" : "§3") + "§lВперед", 30);
				Main.chgSbdTm(p.getScoreboard(), "gst", "", "§dБой");
				p.playSound(p.getLocation(), "cs.info." + (e.getValue() == Team.Ts ? "tfight" : "ctfight"), 10f, 1f);
				p.removePotionEffect(PotionEffectType.SLOW);
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
							Main.chgSbdTm(p.getScoreboard(), "tm", getTime(tm, ChatColor.LIGHT_PURPLE), "");
						}
					}
					break;
				case 30:
					for (final Entry<Shooter, Team> e : shtrs.entrySet()) {
						final Player p = e.getKey().getPlayer();
						if (p != null) {
							PacketUtils.sendAcBr(p, "§7Осталось §d" + tm + " §7секунд!", 30);
							p.playSound(p.getLocation(), "cs.info." + (e.getValue() == Team.Ts ? "t30sec" : "ct30sec"), 10f, 1f);
							Main.chgSbdTm(p.getScoreboard(), "tm", getTime(tm, ChatColor.LIGHT_PURPLE), "");
						}
					}
					break;
				case 10:
					for (final Shooter sh : shtrs.keySet()) {
						final Player p = sh.getPlayer();
						if (p != null) {
							PacketUtils.sendAcBr(p, "§7Осталось §d" + tm + " §7секунд!", 30);
							Main.chgSbdTm(p.getScoreboard(), "tm", getTime(tm, ChatColor.LIGHT_PURPLE), "");
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
							Main.chgSbdTm(p.getScoreboard(), "tm", getTime(tm, ChatColor.LIGHT_PURPLE), "");
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
		
		for (final BrknBlck b : brkn) {
			b.getBlock().setBlockData(b.bd, false);
		}
		brkn.clear();
		
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
			final Player p = sh.getPlayer();
			if (p != null) {
				p.closeInventory();
				Main.chgSbdTm(p.getScoreboard(), "tm", "§d00:00", "");
				p.playSound(p.getLocation(), "cs.info." + (e.getValue() == Team.Ts ? "tfinish" : "ctfinish"), 10f, 1f);
				p.sendMessage(msg);
				PacketUtils.sendTtlSbTtl(p, "§5Финиш", st, 40);
				PacketUtils.sendNmTg(sh, "§7<§d" + name + "§7> ", " §7[" + sh.kills() + "-" + sh.deaths() + "]", Team.NA.clr);
				ApiOstrov.moneyChange(sh.name(), sh.kills() * 6, "Убийства");
				sh.inv().clear();
				winScore(sh, p, wn == Team.CTs);
				
				if (e.getValue() == wn) {
					ApiOstrov.addStat(p, Stat.CS_win);
					SkinQuest.tryCompleteQuest(e.getKey(), Quest.ГРУЗЧИК, ApiOstrov.getStat(p, Stat.CS_win));
					ApiOstrov.moneyChange(e.getKey().name(), 60, "Игра");
				} else {
					ApiOstrov.addStat(p, Stat.CS_loose);
				}
				ApiOstrov.addStat(p, Stat.CS_game);
				SkinQuest.tryCompleteQuest(sh, Quest.ДУША, ApiOstrov.getStat(p, Stat.CS_game));
			}
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
						if (p == null) {
							
						} else {
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
		final Objective ob = sb.registerNewObjective("CS:GO", "", Component.text("§7[§5CS:GO§7]"));
		ob.setDisplaySlot(DisplaySlot.SIDEBAR);
		ob.getScore("    ")
		.setScore(11);
		ob.getScore("§7Карта: §5" + name)
		.setScore(10);
		ob.getScore("§7Комманды:")
		.setScore(9);
		ob.getScore("§7=-=-=-=-=-=-=-=-")
		.setScore(8);
		Main.crtSbdTm(sb, "tamt", "", "§4\u9265 §7: ", "§4" + String.valueOf(getTmAmt(Team.Ts)) + " §7чел.");
		ob.getScore("§4\u9265 §7: ")
		.setScore(7);
		ob.getScore("   ")
		.setScore(6);
		Main.crtSbdTm(sb, "ctamt", "", "§3\u9264 §7: ", "§3" + String.valueOf(getTmAmt(Team.CTs)) + " §7чел.");
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
		final Objective ob = sb.registerNewObjective("CS:GO", "", Component.text("§7[§5CS:GO§7]"));
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
		Main.crtSbdTm(sb, "tamt", "", "§4\u9265 §7: ", "§4" + String.valueOf(getTmAmt(Team.Ts)) + (shtrs.get(sh) == Team.Ts ? " §7чел. §8✦ Вы" : " §7чел."));
		ob.getScore("§4\u9265 §7: ")
		.setScore(7);
		ob.getScore("   ")
		.setScore(6);
		Main.crtSbdTm(sb, "ctamt", "", "§3\u9264 §7: ", "§3" + String.valueOf(getTmAmt(Team.CTs)) + (shtrs.get(sh) == Team.CTs ? " §7чел. §8✦ Вы" : " §7чел."));
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
		final Objective ob = sb.registerNewObjective("CS:GO", "", Component.text("§7[§5CS:GO§7]"));
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
		Main.crtSbdTm(sb, "tamt", "", "§4\u9265 §7: ", "§4" + String.valueOf(getTmAmt(Team.Ts)) + (shtrs.get(sh) == Team.Ts ? " §7чел. §8✦ Вы" : " §7чел."));
		ob.getScore("§4\u9265 §7: ")
		.setScore(6);
		Main.crtSbdTm(sb, "ctamt", "", "§3\u9264 §7: ", "§3" + String.valueOf(getTmAmt(Team.CTs)) + (shtrs.get(sh) == Team.CTs ? " §7чел. §8✦ Вы" : " §7чел."));
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
		final Objective ob = sb.registerNewObjective("CS:GO", "", Component.text("§7[§5CS:GO§7]"));
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
		ob.getScore("§4\u9265 §7: §4" + String.valueOf(getTmAmt(Team.Ts)) + (shtrs.get(sh) == Team.Ts ? " §7чел. §8✦ Вы" : " §7чел."))
		.setScore(6);
		ob.getScore("§3\u9264 §7: §3" + String.valueOf(getTmAmt(Team.CTs)) + (shtrs.get(sh) == Team.CTs ? " §7чел. §8✦ Вы" : " §7чел."))
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
		final LivingEntity le = sh.getEntity();
		le.setHealth(le.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());
		if (gst == GameState.ROUND) {
			addDth(sh);
			sh.dropIts(le.getLocation(), shtrs.get(sh), false);
		} else if (sh instanceof PlShooter) {
			if (le.hasPotionEffect(PotionEffectType.GLOWING)) {
				Main.nrmlzPl((Player) le, false);
				le.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, tm * 20, 1));
			} else {
				Main.nrmlzPl((Player) le, false);
			}
		}
		sh.teleport(le, Main.getNrLoc(shtrs.get(sh) == Team.Ts ? Main.rndElmt(TPss) : Main.rndElmt(CTPss), w));
	}
	
	private void updateWeapon(final Shooter sh, final Player p) {
		if (sh.money() == guns.length) {
			cntFnsh(shtrs.get(sh));
			return;
		}

		Main.chgSbdTm(p.getScoreboard(), "scr", "§l§4\u9265 §7: §4" + getTmProg(Team.Ts) + "§7/§4" + cycle, 
			"§l§3" + getTmProg(Team.CTs) + "§7/§3" + cycle + " §7: §3\u9264");
		sh.item(Main.air.clone(), 0);
		sh.item(Main.air.clone(), 1);
		sh.item(Main.air.clone(), 3);
		sh.item(Main.air.clone(), 4);
		Main.chgSbdTm(p.getScoreboard(), "prg", "", "§d" + 
			String.valueOf(sh.money()) + "§7/§d" + String.valueOf(cycle));
		final GunType gt = guns[sh.money()];
		if (gt == null) {
			PacketUtils.sendTtlSbTtl(p, "", "§7Последний §dцыкл §7оружия!", 20);
			return;
		}

		final int mdl = sh.getModel(gt);
		final NadeType nt = nadeFromPts(sh.money());
		sh.item(new ItemBuilder(gt.getMat()).name(mdl == GunType.defCMD ? "§5" + gt.toString() : 
			"§5" + gt.toString() + " '" + Main.nrmlzStr(Quest.getQuest(gt, mdl).toString()) + "'")
		.setAmount(gt.amo).setModelData(mdl).build(), gt.prm ? 0 : 1);
		sh.item(Inventories.CTShop.getItem(nt.slt).clone(), nt.prm ? 3 : 4);
		PacketUtils.zoom(p, false);
		p.setNoDamageTicks(10);
		
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
		final Player p = sh.getPlayer();
		if (p != null) {
			p.setHealth(p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());
			ApiOstrov.addStat(p, Stat.CS_kill);
			SkinQuest.tryCompleteQuest(sh, Quest.ЗЕМЛЯ, sh.kills() / Math.max(sh.deaths(), 1));
			SkinQuest.tryCompleteQuest(sh, Quest.ТОКСИК, sh.kills());
			updateWeapon(sh, p);
		}
	}

	@Override
	public void addDth(final Shooter sh) {
		sh.deathsI();
		sh.money(Math.max(sh.money() - 1, 0));
		PacketUtils.sendNmTg(sh, shtrs.get(sh).icn + " ", " §7[" + sh.kills() + "-" + sh.deaths() + "]", shtrs.get(sh).clr);
		final Player p = sh.getPlayer();
		if (p != null) {
			p.setHealth(p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());
			ApiOstrov.addStat(p, Stat.CS_death);
			updateWeapon(sh, p);
		}
	}
	
	public byte getTmAmt(final Team tm) {
		byte n = 0;
		for (final Entry<Shooter, Team> e : shtrs.entrySet()) {
			if (e.getValue() == tm) {
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
			if (nv != null && ( (shtrs.get(sh) == nv.getOpst() && getTmAmt(nv) >= getTmAmt(nv.getOpst())) || getTmAmt(nv) > getTmAmt(nv.getOpst()) )) {
				p.sendMessage(Main.prf() + "§cВ этой комманде слишком много игроков!");
				return;
			}
			final Team tm = shtrs.replace(sh, nv);
			if (nv == Team.NA || tm != nv) {
				for (final Entry<Shooter, Team> e : shtrs.entrySet()) {
					Main.chgSbdTm(p.getScoreboard(), "tamt", "", "§4" + String.valueOf(getTmAmt(Team.Ts)) + (e.getValue() == Team.Ts ? " §7чел. §8✦ Вы" : " §7чел."));
					Main.chgSbdTm(p.getScoreboard(), "ctamt", "", "§3" + String.valueOf(getTmAmt(Team.CTs)) + (e.getValue() == Team.CTs ? " §7чел. §8✦ Вы" : " §7чел."));
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
		for (final Entry<Shooter, Team> e : shtrs.entrySet()) {
			final Player p = e.getKey().getPlayer();
			if (p == null) {
				
			} else {
				Main.chgSbdTm(p.getScoreboard(), "tamt", "", "§4" + String.valueOf(getTmAmt(Team.Ts)) + (e.getValue() == Team.Ts ? " §7чел. §8✦ Вы" : " §7чел."));
				Main.chgSbdTm(p.getScoreboard(), "ctamt", "", "§3" + String.valueOf(getTmAmt(Team.CTs)) + (e.getValue() == Team.CTs ? " §7чел. §8✦ Вы" : " §7чел."));
			}
		}
	}

	private Team getMinTm() {
		final byte tn = getTmAmt(Team.Ts);
		final byte ctn = getTmAmt(Team.CTs);
		return tn < ctn ? Team.Ts : (tn == ctn ? (Main.srnd.nextBoolean() ? Team.Ts : Team.CTs) : Team.CTs);
	}
}
