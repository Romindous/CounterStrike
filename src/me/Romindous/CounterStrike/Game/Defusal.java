package me.Romindous.CounterStrike.Game;

import me.Romindous.CounterStrike.Enums.GunType;
import me.Romindous.CounterStrike.Listeners.MainLis;
import me.Romindous.CounterStrike.Main;
import me.Romindous.CounterStrike.Objects.Game.*;
import me.Romindous.CounterStrike.Objects.Loc.BrknBlck;
import me.Romindous.CounterStrike.Objects.Shooter;
import me.Romindous.CounterStrike.Objects.Skins.Quest;
import me.Romindous.CounterStrike.Objects.Skins.SkinQuest;
import me.Romindous.CounterStrike.Utils.Inventories;
import me.Romindous.CounterStrike.Utils.PacketUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy;
import net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata;
import net.minecraft.network.protocol.game.PacketPlayOutSpawnEntity;
import net.minecraft.server.network.PlayerConnection;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.item.EntityItem;
import net.minecraft.world.level.World;
import org.bukkit.*;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
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
import ru.komiss77.ApiOstrov;
import ru.komiss77.enums.Stat;
import ru.komiss77.modules.bots.BotManager;
import ru.komiss77.modules.world.WXYZ;
import ru.komiss77.modules.world.XYZ;
import ru.komiss77.utils.ItemUtils;
import ru.komiss77.version.VM;

import java.util.Iterator;
import java.util.Map.Entry;

public class Defusal extends Arena {

	public final XYZ ast;
	public final XYZ bst;
	public final Inventory tms;
	private final EntityItem[] inds;
	private WXYZ bLoc;
	private Bomb bmb;
	private int TBns;
	private int CTBns;
	private final Team[] wns;
	public boolean indon;
	
	public Defusal(final String name, final byte min, final byte max, 
		final XYZ[] TSpawns, final XYZ[] CTSpawns, final XYZ[] spots, 
		final org.bukkit.World w, final XYZ ast, final XYZ bst, final byte wnr, 
		final boolean rnd, final boolean bots) {
		super(name, min, max, TSpawns, CTSpawns, spots, w, rnd, bots);
		this.ast = ast;
		this.bst = bst;
		wns = new Team[wnr * 2 - 1];
		bmb = null;
		TBns = 0;
		CTBns = 0;
		inds = new EntityItem[32];
		
		byte n = 0;
		final World wm = PacketUtils.getNMSWrld(w);
		final net.minecraft.world.item.ItemStack i = PacketUtils.getNMSIt(new ItemStack(Material.SMALL_AMETHYST_BUD));
		for (byte x = -2; x < 3; x++) {
			for (byte z = -2; z < 3; z++) {
				if (Math.abs(z) == 2 || Math.abs(x) == 2) {
					final EntityItem ait = new EntityItem(EntityTypes.ad, wm);
					ait.m(true);
					ait.e(true);
					ait.i(true);
					ait.a(i);
					ait.a(IChatBaseComponent.a("§4 "));
					ait.n(false);
					ait.setPosRaw(ast.x + x + 0.5d, ast.y, ast.z + z + 0.5d, false);
					inds[n] = ait; n++;
					final EntityItem bit = new EntityItem(EntityTypes.ad, wm);
					bit.m(true);
					bit.e(true);
					bit.i(true);
					bit.a(i);
					bit.a(IChatBaseComponent.a("§4 "));
					bit.n(false);
					bit.setPosRaw(bst.x + x + 0.5d, bst.y, bst.z + z + 0.5d, false);
					inds[n] = bit; n++;
				}
			}
		}
		
		this.tms = Bukkit.createInventory(null, 9, Component.text("§eВыбор Комманды"));
		this.tms.setContents(Inventories.fillTmInv());
		this.gst = GameState.WAITING;
		this.bLoc = null;
		ApiOstrov.sendArenaData(this.name, ru.komiss77.enums.GameState.ОЖИДАНИЕ, "§7[§5CS§7]", "§dКлассика", " ", "§7Игроков: §50§7/§5" + this.min, "", 0);
		Inventories.updtGm(this);
	}

	public BukkitTask getTask() {
		return tsk;
	}

	public boolean isBmbOn() {
		return bmb != null;
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
				sh.item(Main.mkItm(Material.HEART_OF_THE_SEA, "§чБоторейка", 10), 4);
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
								pl.teleport(Main.getNrLoc(Main.srnd.nextBoolean() ? Main.rndElmt(TSpawns) : Main.rndElmt(CTSawns), w));
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
							Main.chgSbdTm(pl.getScoreboard(), "rmn", "", "§5" + rm + (rm > 1 ? " §7игроков" : " §7игрокa"));
						}
					}
				}
				break;
			case BEGINING:
				shtrs.put(sh, Team.NA);
				editLr(tms.getItem(4), true, "§7✦ §7" + sh.name());
				sh.item(Main.mkItm(Material.NETHER_STAR, "§eВыбор Комманды", 10), 2);
				sh.item(Main.mkItm(Material.HEART_OF_THE_SEA, "§чБоторейка", 10), 4);
				sh.item(Main.mkItm(Material.GHAST_TEAR, "§5Магазин", 10), 6);
				sh.item(Main.mkItm(Material.SLIME_BALL, "§cВыход", 10), 8);
				if (!rnd) {
					p.teleport(Main.getNrLoc(Main.srnd.nextBoolean() ? Main.rndElmt(TSpawns) : Main.rndElmt(CTSawns), w));
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
			case ENDRND:
				final Team tm = getMinTm();
				shtrs.put(sh, tm);
				addToTm(p, tm);
				gameScore(sh, p);
				chngMn(sh, (getRnd() & 3) == 0 ? 550 : 550 + (tm == Team.Ts ? TBns : CTBns));
				PacketUtils.sendNmTg(sh, tm.icn + " ", " §7[0-0]", tm.clr);
				for (final Entry<Shooter, Team> e : shtrs.entrySet()) {
					final Shooter s = e.getKey();
					final Team t = e.getValue();
					final Player pl = s.getPlayer();
					if (pl != null) {
						pl.sendMessage(Main.prf() + "§7Игрок §5" + sh.name() + " §7зашел играть за комманду " + tm.icn + "§7!");
						Main.chgSbdTm(pl.getScoreboard(), "tamt", "", "§4" + getTmAmt(Team.Ts, true, true) + (t == Team.Ts ? " §7чел. §8✦ Вы" : " §7чел."));
						Main.chgSbdTm(pl.getScoreboard(), "ctamt", "", "§3" + getTmAmt(Team.CTs, true, true) + (t == Team.CTs ? " §7чел. §8✦ Вы" : " §7чел."));
					} else {
						((BtShooter) s).updateAll(p);
					}
					PacketUtils.sendNmTg(PacketUtils.getNMSPl(p).c.h, s, e.getValue().icn + " ", " §7[" + s.kills() + "-" + s.deaths() + "]", t == tm, t.clr);
				}
				
				for (final TripWire tw : tws) {
					if (tm == tw.tm) {
						tw.shwNd(PacketUtils.getNMSPl(p).c);
					}
				}
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
		p.teleport(Main.getNrLoc(tm == Team.Ts ? Main.rndElmt(TSpawns) : Main.rndElmt(CTSawns), w).add(0d, 2d, 0d));
		Main.shwHdPls(p);
		if (gst == GameState.BUYTIME) {
			final PlayerInventory pinv = p.getInventory();
			p.setGameMode(GameMode.SURVIVAL);
			pinv.setItem(8, Main.mkItm(Material.GHAST_TEAR, "§5Магазин", 10));
			if (tm == Team.Ts) {
				pinv.setItem(2, Main.mkItm(Material.BLAZE_ROD, "§fНож \u9298", 10));
			} else {
				pinv.setItem(7, Main.mkItm(Material.GOLD_NUGGET, "§eКусачки §f\u9268", 10));
				pinv.setItem(2, Main.mkItm(Material.BLAZE_ROD, "§fНож \u9298", 10));
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
			tl.addPassenger(p);*/
		} else {
			p.setGameMode(GameMode.SPECTATOR);
			p.getInventory().setItem(8, Main.mkItm(Material.REDSTONE, "§cВыход", 10));
		}
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
							Main.chgSbdTm(pl.getScoreboard(), "tamt", "", "§4" + getTmAmt(Team.Ts, true, true) + (e.getValue() == Team.Ts ? " §7чел. §8✦ Вы" : " §7чел."));
							Main.chgSbdTm(pl.getScoreboard(), "ctamt", "", "§3" + getTmAmt(Team.CTs, true, true) + (e.getValue() == Team.CTs ? " §7чел. §8✦ Вы" : " §7чел."));
							Main.chgSbdTm(sb, "rmn", "", "§5" + rm + (rm > 1 ? " §7игроков" : " §7игрокa"));
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
						if (pl != null) {
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
							Main.chgSbdTm(pl.getScoreboard(), "tamt", "", "§4" + getTmAmt(Team.Ts, true, true) + (e.getValue() == Team.Ts ? " §7чел. §8✦ Вы" : " §7чел."));
							Main.chgSbdTm(pl.getScoreboard(), "ctamt", "", "§3" + getTmAmt(Team.CTs, true, true) + (e.getValue() == Team.CTs ? " §7чел. §8✦ Вы" : " §7чел."));
						}
					}
				}
				break;
			case BUYTIME:
			case ROUND:
				sh.dropIts(p.getLocation());
			case ENDRND:
				for (final TripWire tw : tws) {
					if (tm == tw.tm) {
						PacketUtils.getNMSPl(p).c.a(new PacketPlayOutEntityDestroy(tw.eif.af()));
					}
				}
				if (getTmAmt(Team.CTs, false, false) == 0 || getTmAmt(Team.Ts, false, false) == 0) {
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
							Main.chgSbdTm(pl.getScoreboard(), "tamt", "", "§4" + getTmAmt(Team.Ts, true, true) + (e.getValue() == Team.Ts ? " §7чел. §8✦ Вы" : " §7чел."));
							Main.chgSbdTm(pl.getScoreboard(), "ctamt", "", "§3" + getTmAmt(Team.CTs, true, true) + (e.getValue() == Team.CTs ? " §7чел. §8✦ Вы" : " §7чел."));
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
			Inventories.updtGm(this);
			Main.lobbyPl(p);
		}
		return true;
	}

	//счетчик
	public void cntBeg() {
		tm = 30;
		gst = GameState.BEGINING;
		ApiOstrov.sendArenaData(this.name, ru.komiss77.enums.GameState.СТАРТ, "§7[§5CS§7]", "§dКлассика", " ", "§7Игроков: §5" + shtrs.size() + "§7/§5" + this.max, "", shtrs.size());
		tsk = new BukkitRunnable() {
			@Override
			public void run() {
				switch (tm) {
				case 10, 5, 4:
					for (final Shooter sh : shtrs.keySet()) {
						final Player pl = sh.getPlayer();
						if (pl != null) {
							pl.playSound(pl.getLocation(), Sound.BLOCK_DISPENSER_FAIL, 0.8f, 1.2f);
							PacketUtils.sendSbTtl(pl, "§5§l" + tm, 10);
							Main.chgSbdTm(pl.getScoreboard(), "rmn", "", "§5" + String.valueOf(tm));
						}
					}
					break;
				case 3, 2, 1:
					for (final Shooter sh : shtrs.keySet()) {
						final Player pl = sh.getPlayer();
						if (pl != null) {
							pl.playSound(pl.getLocation(), Sound.BLOCK_DISPENSER_FAIL, 0.8f, 1.2f);
							PacketUtils.sendSbTtl(pl, "§d§l" + tm, 10);
							Main.chgSbdTm(pl.getScoreboard(), "rmn", "", "§5" + String.valueOf(tm));
						}
					}
					break;
				case 0:
					if (tsk != null) {
						tsk.cancel();
					}
					cntBuy(true);
					if (rnd) {
						Main.mapBlds.get(name).placeSets(w, 5);
					}
					break;
				default:
					for (final Shooter sh : shtrs.keySet()) {
						final Player pl = sh.getPlayer();
						if (pl != null) {
							Main.chgSbdTm(pl.getScoreboard(), "rmn", "", "§5" + String.valueOf(tm));
						}
					}
					break;
				}
				tm--;
			}
		}.runTaskTimer(Main.plug, 20, 20);
	}

	public void cntBuy(final boolean pstl) {
		tm = 10;
		TBns = getBns(Team.Ts);
		CTBns = getBns(Team.CTs);
		gst = GameState.BUYTIME;
		ApiOstrov.sendArenaData(this.name, ru.komiss77.enums.GameState.ЭКИПИРОВКА, "§7[§5CS§7]", "§dКлассика", " ", "§7Игроков: §5" + shtrs.size() + "§7/§5" + this.max, "", shtrs.size());
		Inventories.updtGm(this);
		for (final TripWire tw : tws) {
			tw.rmv(this);
		}
		tws.clear();
		for (final Entity e : w.getEntitiesByClasses(Item.class, ArmorStand.class, Turtle.class)) {
			e.remove();
		}
		
		Shooter bc = null;
		blncTms();
		if (pstl) {
			if (wns[0] != null) {
				swtchTms();
			}
			for (final Entry<Shooter, Team> e : shtrs.entrySet()) {
				final Shooter sh = e.getKey();
				sh.money(0);
				if (sh instanceof PlShooter) {
					final Player p = sh.getPlayer();
					Main.nrmlzPl(p, true);
					gameScore(sh, p);
					PacketUtils.sendTtlSbTtl(p, getRnd() == 0 ? "" : "§eСмена Ролей", "§l§6⛃§5Закупка§6⛃", 30);
				} else if (sh instanceof BtShooter) {
					((BtShooter) sh).willBuy = true;
					sh.clearInv();
				}
				switch (e.getValue()) {
				case Ts:
					sh.teleport(sh.getEntity(), Main.getNrLoc(Main.rndElmt(TSpawns), w));
					if (bc == null || sh.kills() > bc.kills()) {
						 bc = sh;
					}
					sh.item(Main.mkItm(Material.GHAST_TEAR, "§5Магазин", 10), 8);
					sh.item(Main.mkItm(Material.BLAZE_ROD, "§fНож \u9298", 10), 2);
					chngMn(sh, 550);
					break;
				case CTs:
					sh.teleport(sh.getEntity(), Main.getNrLoc(Main.rndElmt(CTSawns), w));
					sh.item(Main.mkItm(Material.GOLD_NUGGET, "§eКусачки §f\u9268", 10), 7);
					sh.item(Main.mkItm(Material.GHAST_TEAR, "§5Магазин", 10), 8);
					sh.item(Main.mkItm(Material.BLAZE_ROD, "§fНож \u9298", 10), 2);
					chngMn(sh, 550);
					break;
				case NA:
					break;
				}
				PacketUtils.sendNmTg(sh, e.getValue().icn + " ", " §7[" + sh.kills() + "-" + sh.deaths() + "]", e.getValue().clr);
				sh.getEntity().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 250, 250, true, false, false));
				sh.getEntity().addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 40, 2, true, false, false));
			}
		} else {
			for (final Entry<Shooter, Team> e : shtrs.entrySet()) {
				final Shooter sh = e.getKey();
				if (sh instanceof PlShooter) {
					final Player p = sh.getPlayer();
					if (p.getGameMode() == GameMode.SPECTATOR) {
						Main.nrmlzPl(p, true);
						gameScore(sh, p);
						sh.item(Main.mkItm(Material.GHAST_TEAR, "§5Магазин", 10), 8);
						switch (e.getValue()) {
						case Ts:
							sh.teleport(p, Main.getNrLoc(Main.rndElmt(TSpawns), w));
							if (bc == null || sh.kills() > bc.kills()) {
								 bc = sh;
							}
							sh.item(Main.mkItm(Material.BLAZE_ROD, "§fНож \u9298", 10), 2);
							chngMn(sh, 550 + TBns);
							break;
						case CTs:
							sh.teleport(p, Main.getNrLoc(Main.rndElmt(CTSawns), w));
							sh.item(Main.mkItm(Material.GOLD_NUGGET, "§eКусачки §f\u9268", 10), 7);
							sh.item(Main.mkItm(Material.BLAZE_ROD, "§fНож \u9298", 10), 2);
							chngMn(sh, 550 + CTBns);
							break;
						case NA:
							break;
						}
					} else {
						Main.nrmlzPl(p, false);
						final ItemStack it0 = sh.item(0);
						if (!ItemUtils.isBlank(it0, false)) {
							it0.setAmount(GunType.getGnTp(it0).amo);
							sh.item(it0, 0);
						}
						final ItemStack it1 = sh.item(1);
						if (!ItemUtils.isBlank(it1, false)) {
							it1.setAmount(GunType.getGnTp(it1).amo);
							sh.item(it1, 1);
						}
						switch (e.getValue()) {
						case Ts:
							if (!ItemUtils.isBlank(sh.item(7), false)) {
								sh.item(Main.air, 7);
								if (indon) {
									indSts(p);
								}
							}
							sh.teleport(p, Main.getNrLoc(Main.rndElmt(TSpawns), w));
							if (bc == null || sh.kills() > bc.kills()) {
								 bc = sh;
							}
							chngMn(sh, 550 + TBns);
							break;
						case CTs:
							sh.teleport(p, Main.getNrLoc(Main.rndElmt(CTSawns), w));
							chngMn(sh, 550 + CTBns);
							break;
						case NA:
							break;
						}
						Main.chgSbdTm(p.getScoreboard(), "scr", "§l§4\u9265 §7: §4" + getWns(Team.Ts), "§l§3" + getWns(Team.CTs) + " §7: §3\u9264");
						Main.chgSbdTm(p.getScoreboard(), "mn", "", "§d" + sh.money() + " §6⛃");
						Main.chgSbdTm(p.getScoreboard(), "gst", "", "§dЗакупка");
					}
					p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 250, 250, true, false, false));
					p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 40, 2, true, false, false));
					PacketUtils.sendNmTg(sh, e.getValue().icn + " ", " §7[" + sh.kills() + "-" + sh.deaths() + "]", e.getValue().clr);
					PacketUtils.sendSbTtl(p, "§l§6⛃§5Закупка§6⛃", 30);
				} else {
					((BtShooter) sh).willBuy = true;
					final ItemStack it0 = sh.item(0);
					if (!ItemUtils.isBlank(it0, false)) {
						it0.setAmount(GunType.getGnTp(it0).amo);
						sh.item(it0, 0);
					}
					final ItemStack it1 = sh.item(1);
					if (!ItemUtils.isBlank(it1, false)) {
						it1.setAmount(GunType.getGnTp(it1).amo);
						sh.item(it1, 1);
					}
					switch (e.getValue()) {
					case Ts:
						sh.teleport(sh.getEntity(), Main.getNrLoc(Main.rndElmt(TSpawns), w));
						sh.item(Main.mkItm(Material.BLAZE_ROD, "§fНож \u9298", 10), 2);
						chngMn(sh, 550 + TBns);
						if (bc == null || sh.kills() > bc.kills()) {
							 bc = sh;
						}
						break;
					case CTs:
						sh.teleport(sh.getEntity(), Main.getNrLoc(Main.rndElmt(CTSawns), w));
						sh.item(Main.mkItm(Material.GOLD_NUGGET, "§eКусачки §f\u9268", 10), 7);
						sh.item(Main.mkItm(Material.BLAZE_ROD, "§fНож \u9298", 10), 2);
						chngMn(sh, 550 + CTBns);
						break;
					case NA:
						break;
					}
					sh.getEntity().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 250, 250, true, false, false));
					sh.getEntity().addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 40, 2, true, false, false));
					PacketUtils.sendNmTg(sh, e.getValue().icn + " ", " §7[" + sh.kills() + "-" + sh.deaths() + "]", e.getValue().clr);
				}
			}
		}
		
		if (noBmb()) {
			bmb = null;
			bc.item(Main.bmb, 7);
			//Bukkit.broadcast(Component.text("bmb-" + bc.name()));
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

	private void swtchTms() {
		for (final Entry<Shooter, Team> e : shtrs.entrySet()) {
			e.setValue(e.getValue().getOpst());
			if (indon && e.getKey().inv().contains(Material.GOLDEN_APPLE)) {
				final Player p = e.getKey().getPlayer();
				if (p != null) {
					indSts(p);
				}
			}
		}
		
		for (byte i = (byte) (wns.length - 1); i >= 0; i--) {
			wns[i] = wns[i] == null ? null : wns[i].getOpst();
		}
	}

	private void cntRnd() {
		tm = 120;
		bLoc = null;
		gst = GameState.ROUND;
		ApiOstrov.sendArenaData(this.name, ru.komiss77.enums.GameState.ИГРА, "§7[§5CS§7]", "§dКлассика", " ", "§7Игроков: §5" + shtrs.size() + "§7/§5" + this.max, "", shtrs.size());
		Inventories.updtGm(this);
		for (final Entry<Shooter, Team> e : shtrs.entrySet()) {
			final Player p = e.getKey().getPlayer();
			if (p != null) {
				PacketUtils.sendSbTtl(p, (e.getValue() == Team.Ts ? "§4" : "§3") + "§lВперед", 30);
				Main.chgSbdTm(p.getScoreboard(), "gst", "", "§dБой");
				p.playSound(p.getLocation(), "cs.info." + (e.getValue() == Team.Ts ? "tfight" : "ctfight"), 10f, 1f);
				p.removePotionEffect(PotionEffectType.SLOW);
			}
		}
		tsk = new BukkitRunnable() {
			@Override
			public void run() {
				final String t;
				if (bmb == null || (tm > 10 && (tm & 1) == 0)) {
					t = getTime(tm, "§d");
				} else {
					t = getTime(tm, "§c");
					Main.plyWrldSnd(bmb.getCenterLoc(), "cs.rand.bmbbeep");
				}
				switch (tm) {
				case 60:
					for (final Shooter sh : shtrs.keySet()) {
						final Player p = sh.getPlayer();
						if (p != null) {
							PacketUtils.sendAcBr(p, "§7Осталась §d1 §7минута!", 30);
							Main.chgSbdTm(p.getScoreboard(), "tm", t, "");
						}
					}
					break;
				case 30:
					for (final Entry<Shooter, Team> e : shtrs.entrySet()) {
						final Player p = e.getKey().getPlayer();
						if (p != null) {
							PacketUtils.sendAcBr(p, "§7Осталось §d" + tm + " §7секунд!", 30);
							Main.chgSbdTm(p.getScoreboard(), "tm", t, "");
							p.playSound(p.getLocation(), "cs.info." + (e.getValue() == Team.Ts ? "t30sec" : "ct30sec"), 10f, 1f);
						}
					}
					break;
				case 10:
					for (final Shooter sh : shtrs.keySet()) {
						final Player p = sh.getPlayer();
						if (p != null) {
							PacketUtils.sendAcBr(p, "§7Осталось §d" + tm + " §7секунд!", 30);
							Main.chgSbdTm(p.getScoreboard(), "tm", t, "");
						}
					}
					break;
				case 0:
					if (bmb == null) {
						rndWn(Team.CTs, "§9⌛", "§7§lВремя вышло, §3Спецназ §7победил!", "notime");
					} else {
						bmb.expld((Defusal) getArena());
						bmb = null;
						rndWn(Team.Ts, "§c\u9299", "§7§lБомба взорвалась, §4Террористы §7победили!", "kaboom");
					}
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

	private void rndWn(final Team tm, final String ttl, final String sbttl, final String snd) {
		if (tsk != null) {
			tsk.cancel();
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
			final Player p = e.getKey().getPlayer();
			if (p != null) {
				if (e.getValue() == tm) {
					ApiOstrov.addStat(p, Stat.CS_win);
					SkinQuest.tryCompleteQuest(e.getKey(), Quest.ГРУЗЧИК, ApiOstrov.getStat(p, Stat.CS_win));
				} else {
					ApiOstrov.addStat(p, Stat.CS_loose);
				}
			}
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
		
		if (bmb != null) {
			bmb.title.remove();
			bmb.getBlock().setType(Material.AIR, false);
			bmb = null;
		}
		
		addWn(tm);
		if (getWns(tm) == wns.length / 2 + 1) {
			for (final Shooter sh : shtrs.keySet()) {
				final Player p = sh.getPlayer();
				if (p != null) {
					p.closeInventory();
					p.sendMessage(" \n§7Победа в раунде: " + (tm == Team.CTs ? "§3Спецназ" : "§4Террористы")
						+ "\n \n§5=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-\n§7Лучший спецназовец: §3" + bct.name() + 
						"\n          §7--=x=--\n§7Самый злобный террорист: §4" + bt.name() + "\n§5=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-");
				}
			}
			cntFnsh(tm);
		} else {
			for (final Entry<Shooter, Team> e : shtrs.entrySet()) {
				final Player p = e.getKey().getPlayer();
				if (p != null) {
					p.closeInventory();
					Main.chgSbdTm(p.getScoreboard(), "tm", "§d00:00", "");
					PacketUtils.sendTtlSbTtl(p, ttl, sbttl, 30);
					p.playSound(p.getLocation(), "cs.info." + (e.getValue() == Team.Ts ? "t" : "ct") + snd, 10f, 1f);
					p.sendMessage(" \n§7Победа в раунде: " + (tm == Team.CTs ? "§3Спецназ" : "§4Террористы")
						+ "\n \n§5=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-\n§7Лучший спецназовец: §3" + bct.name() + 
						"\n          §7--=x=--\n§7Самый злобный террорист: §4" + bt.name() + "\n§5=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-");
				}
			}
			gst = GameState.ENDRND;
			new BukkitRunnable() {
				@Override
				public void run() {
					if (tsk.isCancelled()) {
						cntBuy((getRnd() & 3) == 0);
					}
				}
			}.runTaskLater(Main.plug, 100);
		}
	}

	private void cntFnsh(final Team wn) {
		tm = 10;
		gst = GameState.FINISH;
		ApiOstrov.sendArenaData(this.name, ru.komiss77.enums.GameState.ФИНИШ, "§7[§5CS§7]", "§dКлассика", " ", "§7Игроков: §5" + shtrs.size() + "§7/§5" + this.max, "", shtrs.size());
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
		
		if (bmb != null) {
			bmb.title.remove();
			bmb.getBlock().setType(Material.AIR, false);
			bmb = null;
		}
		
		final String st = switch (wn) {
            case Ts -> "§4§lТеррористы §7выйграли!";
            case CTs -> "§3§lСпецназ §7одержал победу!";
            default -> " ";
        };

        for (final Entry<Shooter, Team> e : shtrs.entrySet()) {
			final Shooter sh = e.getKey();
			if (sh instanceof PlShooter) {
				final Player p = sh.getPlayer();
				p.closeInventory();
				p.playSound(p.getLocation(), "cs.info." + (e.getValue() == Team.Ts ? "tfinish" : "ctfinish"), 10f, 1f);
				PacketUtils.sendTtlSbTtl(p, "§5Финиш", st, 40);
				PacketUtils.sendNmTg(sh, "§7<§d" + name + "§7> ", " §7[" + sh.kills() + "-" + sh.deaths() + "]", Team.NA.clr);
				if (indon && sh.inv().contains(Material.GOLDEN_APPLE)) {
					indSts(p);
				}
				ApiOstrov.addStat(p, Stat.CS_game);
				SkinQuest.tryCompleteQuest(sh, Quest.ДУША, ApiOstrov.getStat(p, Stat.CS_game));
				sh.clearInv();
				winScore(sh, p, wn == Team.CTs);
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
				final String t = getTime(tm, "§5");
				if (tm == 0) {
					if (tsk != null) {
						tsk.cancel();
					}
					
					Arena.end(getArena());
				} else {
					final FireworkEffect fe = switch (wn) {
                        case Ts -> FireworkEffect.builder().with(Type.CREEPER)
							.withColor(Color.MAROON).withFlicker().build();
                        case CTs -> FireworkEffect.builder().with(Type.STAR)
							.withColor(Color.TEAL).withFlicker().build();
                        default -> null;
                    };

                    for (final Entry<Shooter, Team> e : shtrs.entrySet()) {
						final LivingEntity le = e.getKey().getEntity();
						if (le != null) {
							if (le instanceof Player) {
								Main.chgSbdTm(((Player) le).getScoreboard(), "tm", t, "");
							}
							if (e.getValue() == wn) {
								final Firework fw = (Firework) le.getWorld().spawnEntity(le.getEyeLocation(), EntityType.FIREWORK);
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

	private void waitScore(final Shooter sh, final Player pl, final int rm) {
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
		Main.crtSbdTm(sb, "tamt", "", "§4\u9265 §7: ", "§4" + getTmAmt(Team.Ts, true, true) + " §7чел.");
		ob.getScore("§4\u9265 §7: ")
		.setScore(7);
		ob.getScore("   ")
		.setScore(6);
		Main.crtSbdTm(sb, "ctamt", "", "§3\u9264 §7: ", "§3" + getTmAmt(Team.CTs, true, true) + " §7чел.");
		ob.getScore("§3\u9264 §7: ")
		.setScore(5);
		ob.getScore("§7-=-=-=-=-=-=-=-")
		.setScore(4);
		ob.getScore("  ")
		.setScore(3);
		Main.crtSbdTm(sb, "rmn", "", "§7Ждем еще ", "§5" + rm + (rm > 1 ? " §7игроков" : " §7игрокa"));
		ob.getScore("§7Ждем еще ")
		.setScore(2);
		ob.getScore(" ")
		.setScore(1);
		
		ob.getScore("§e   ostrov77.ru")
		.setScore(0);
		pl.setScoreboard(sb);
	}

	private void beginScore(final Shooter sh, final Player pl) {
		final Scoreboard sb = Main.smg.getNewScoreboard();
		final Objective ob = sb.registerNewObjective("CS:GO", Criteria.DUMMY, Component.text("§7[§5CS:GO§7]"));
//		final org.bukkit.scoreboard.Team bmb = sb.registerNewTeam("bmb");
//		bmb.prefix(Component.text("§4"));
//		bmb.color(NamedTextColor.DARK_RED);
		ob.setDisplaySlot(DisplaySlot.SIDEBAR);
		ob.getScore("    ")
		.setScore(11);
		ob.getScore("§7Карта: §5" + name)
		.setScore(10);
		ob.getScore("§7Комманды:")
		.setScore(9);
		ob.getScore("§7=-=-=-=-=-=-=-=-")
		.setScore(8);
		Main.crtSbdTm(sb, "tamt", "", "§4\u9265 §7: ", "§4" + getTmAmt(Team.Ts, true, true) + (shtrs.get(sh) == Team.Ts ? " §7чел. §8✦ Вы" : " §7чел."));
		ob.getScore("§4\u9265 §7: ")
		.setScore(7);
		ob.getScore("   ")
		.setScore(6);
		Main.crtSbdTm(sb, "ctamt", "", "§3\u9264 §7: ", "§3" + getTmAmt(Team.CTs, true, true) + (shtrs.get(sh) == Team.CTs ? " §7чел. §8✦ Вы" : " §7чел."));
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
		
		ob.getScore("§e   ostrov77.ru")
		.setScore(0);
		pl.setScoreboard(sb);
	}
	
	private void gameScore(final Shooter sh, final Player pl) {
		final Scoreboard sb = Main.smg.getNewScoreboard();
		final org.bukkit.scoreboard.Team t = sb.registerNewTeam("ind");
		t.color(NamedTextColor.DARK_RED);
		final Objective ob = sb.registerNewObjective("CS:GO", Criteria.DUMMY, Component.text("§7[§5CS:GO§7]"));
//		final org.bukkit.scoreboard.Team bmb = sb.registerNewTeam("bmb");
//		bmb.prefix(Component.text("§4"));
//		bmb.color(NamedTextColor.DARK_RED);
		ob.setDisplaySlot(DisplaySlot.SIDEBAR);
		Main.crtSbdTm(sb, "scr", "§l§4\u9265 §7: §4" + getWns(Team.Ts), " §7--=x=-- ", "§l§3" + getWns(Team.CTs) + " §7: §3\u9264");
		ob.getScore(" §7--=x=-- ")
		.setScore(13);
		ob.getScore("§7=-=-=-=-=-=-=-=-")
		.setScore(12);
		ob.getScore("§7Карта: §5" + name)
		.setScore(11);
		ob.getScore("    ")
		.setScore(10);
		Main.crtSbdTm(sb, "gst", "", "§7Cтадия: ", gst == GameState.BUYTIME ? "§5Закупка" : "§5Бой");
		ob.getScore("§7Cтадия: ")
		.setScore(9);
		Main.crtSbdTm(sb, "tm", getTime(tm, "§d"), " §7до конца!", "");
		ob.getScore(" §7до конца!")
		.setScore(8);
		ob.getScore("   ")
		.setScore(7);
		Main.crtSbdTm(sb, "tamt", "", "§4\u9265 §7: ", "§4" + getTmAmt(Team.Ts, true, true) + (shtrs.get(sh) == Team.Ts ? " §7чел. §8✦ Вы" : " §7чел."));
		ob.getScore("§4\u9265 §7: ")
		.setScore(6);
		Main.crtSbdTm(sb, "ctamt", "", "§3\u9264 §7: ", "§3" + getTmAmt(Team.CTs, true, true) + (shtrs.get(sh) == Team.CTs ? " §7чел. §8✦ Вы" : " §7чел."));
		ob.getScore("§3\u9264 §7: ")
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
		pl.setScoreboard(sb);
	}

	private void winScore(final Shooter sh, final Player pl, final boolean isCTWn) {
		final Scoreboard sb = Main.smg.getNewScoreboard();
		final Objective ob = sb.registerNewObjective("CS:GO", Criteria.DUMMY, Component.text("§7[§5CS:GO§7]"));
		ob.setDisplaySlot(DisplaySlot.SIDEBAR);
		if (isCTWn) {
			ob.getScore("§l§4\u9265 §7: §4" + getWns(Team.Ts) + " §7--=x=-- §l§3" + (wns.length / 2 + 1) + " §7: §3\u9264")
			.setScore(13);
		} else {
			ob.getScore("§l§4\u9265 §7: §4" + (wns.length / 2 + 1) + " §7--=x=-- §l§3" + getWns(Team.CTs) + " §7: §3\u9264")
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
		ob.getScore("§4\u9265 §7: §4" + getTmAmt(Team.Ts, true, true) + (shtrs.get(sh) == Team.Ts ? " §7чел. §8✦ Вы" : " §7чел."))
		.setScore(6);
		ob.getScore("§3\u9264 §7: §3" + getTmAmt(Team.CTs, true, true) + (shtrs.get(sh) == Team.CTs ? " §7чел. §8✦ Вы" : " §7чел."))
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
		pl.setScoreboard(sb);
	}

	@Override
	public void killSh(final Shooter sh) {
		final LivingEntity le = sh.getEntity();
		if (gst == GameState.ROUND) {
			addDth(sh);
			final Team tm = shtrs.get(sh);
			sh.dropIts(le.getLocation());
			if (sh instanceof PlShooter) {
				final Player p = sh.getPlayer();
				p.closeInventory();
				p.setGameMode(GameMode.SPECTATOR);
				final int n = getTmAmt(tm, true, true);
				switch (tm) {
				case Ts:
					for (final Entry<Shooter, Team> e : shtrs.entrySet()) {
						final Player pl = e.getKey().getPlayer();
						if (pl != null) {
							Main.chgSbdTm(pl.getScoreboard(), "tamt", "", "§4" + n + (e.getValue() == Team.Ts ? " §7чел. §8✦ Вы" : " §7чел."));
						}
					}
					if (n == 0 && bmb == null) {
						rndWn(Team.CTs, "§9\u9264", "§7§lТеррористы были убиты!", "winct");
					}
					break;
				case CTs:
					for (final Entry<Shooter, Team> e : shtrs.entrySet()) {
						final Player pl = e.getKey().getPlayer();
						if (pl != null) {
							Main.chgSbdTm(pl.getScoreboard(), "ctamt", "", "§3" + n + (e.getValue() == Team.CTs ? " §7чел. §8✦ Вы" : " §7чел."));
						}
					}
					if (n == 0) {
						rndWn(Team.Ts, "§c\u9265", "§7§lСпецназ пал в бою!", "wint");
					}
					break;
				case NA:
					break;
				}
			} else {
				((BtShooter) sh).die(le);
				final int n = getTmAmt(tm, true, true);
				switch (tm) {
				case Ts:
					for (final Entry<Shooter, Team> e : shtrs.entrySet()) {
						final Player pl = e.getKey().getPlayer();
						if (pl != null) {
							Main.chgSbdTm(pl.getScoreboard(), "tamt", "", "§4" + n + (e.getValue() == Team.Ts ? " §7чел. §8✦ Вы" : " §7чел."));
						}
					}
					if (n == 0 && bmb == null) {
						rndWn(Team.CTs, "§9\u9264", "§7§lТеррористы были убиты!", "winct");
					}
					break;
				case CTs:
					for (final Entry<Shooter, Team> e : shtrs.entrySet()) {
						final Player pl = e.getKey().getPlayer();
						if (pl != null) {
							Main.chgSbdTm(pl.getScoreboard(), "ctamt", "", "§3" + n + (e.getValue() == Team.CTs ? " §7чел. §8✦ Вы" : " §7чел."));
						}
					}
					if (n == 0) {
						rndWn(Team.Ts, "§c\u9265", "§7§lСпецназ пал в бою!", "wint");
					}
					break;
				case NA:
					break;
				}
			}
		} else {
			if (sh instanceof PlShooter) {
				final Player p = sh.getPlayer();
				if (p.hasPotionEffect(PotionEffectType.GLOWING)) {
					Main.nrmlzPl(p, false);
					p.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, tm * 20, 1));
				} else {
					Main.nrmlzPl(p, false);
				}
				if (!rnd) {
					le.teleport(Main.getNrLoc(Main.srnd.nextBoolean() ? Main.rndElmt(TSpawns) : Main.rndElmt(CTSawns), w));
				}
			}
		}
	}

	@Override
	public void addKll(final Shooter sh) {
		sh.killsI();
		PacketUtils.sendNmTg(sh, shtrs.get(sh).icn + " ", " §7[" + sh.kills() + "-" + sh.deaths() + "]", shtrs.get(sh).clr);
		final Player p = sh.getPlayer();
		if (p != null) {
			SkinQuest.tryCompleteQuest(sh, Quest.ВОЙ, sh.kills());
			ApiOstrov.addStat(p, Stat.CS_kill);
		}
	}

	@Override
	public void addDth(final Shooter sh) {
		sh.deathsI();
		PacketUtils.sendNmTg(sh, shtrs.get(sh).icn + " ", " §7[" + sh.kills() + "-" + sh.deaths() + "]", shtrs.get(sh).clr);
		final Player p = sh.getPlayer();
		if (p != null) {
			ApiOstrov.addStat(p, Stat.CS_death);
		}
	}

	public void addWn(final Team cts) {
		for (byte i = (byte) (wns.length - 1); i >= 0; i--) {
			if (wns[i] != null) {
				wns[i + 1] = cts;
				return;
			} else if (i == 0) {
				wns[i] = cts;
			}
		}
	}

    @Override
	public void chngTm(final Shooter sh, final Team nv) {
		if (nv != null && ( (shtrs.get(sh) == nv.getOpst() && getTmAmt(nv, true, true) >= getTmAmt(nv.getOpst(), true, true)) || getTmAmt(nv, true, true) > getTmAmt(nv.getOpst(), true, true) )) {
			final Player p = sh.getPlayer();
			if (p != null) {
				p.sendMessage(Main.prf() + "§cВ этой комманде слишком много игроков!");
			}
			return;
		}
		
		final Team tm = shtrs.replace(sh, nv);
		if (nv == Team.NA || tm != nv) {
			for (final Entry<Shooter, Team> e : shtrs.entrySet()) {
				final Player p = e.getKey().getPlayer();
				if (p != null) {
					Main.chgSbdTm(p.getScoreboard(), "tamt", "", "§4" + getTmAmt(Team.Ts, true, true) + (e.getValue() == Team.Ts ? " §7чел. §8✦ Вы" : " §7чел."));
					Main.chgSbdTm(p.getScoreboard(), "ctamt", "", "§3" + getTmAmt(Team.CTs, true, true) + (e.getValue() == Team.CTs ? " §7чел. §8✦ Вы" : " §7чел."));
				}
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
				editLr(tms.getItem(4), false, "§7✦ " + sh.name());
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
				editLr(tms.getItem(4), true, "§7✦ " + sh.name());
				break;
			}
		}
	}

	private void blncTms() {
		for (final Entry<Shooter, Team> e : shtrs.entrySet()) {
			if (e.getValue() == Team.NA) {
				e.setValue(getMinTm());
			}
		}
		
		if (botInv != null && bots) {
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
				Main.chgSbdTm(p.getScoreboard(), "tamt", "", "§4" + getTmAmt(Team.Ts, true, true) + (e.getValue() == Team.Ts ? " §7чел. §8✦ Вы" : " §7чел."));
				Main.chgSbdTm(p.getScoreboard(), "ctamt", "", "§3" + getTmAmt(Team.CTs, true, true) + (e.getValue() == Team.CTs ? " §7чел. §8✦ Вы" : " §7чел."));
			}
		}
	}

	public void defuse() {
		if (bmb == null) {
			Bukkit.broadcast(Component.text("no"));
		} else {
			bmb.title.remove();
			bmb.getBlock().setType(Material.AIR);
			bmb = null;
			rndWn(Team.CTs, "§9\u926f", "§3§lБомба разминирована!", "defuse");
		}
	}

	public void wrngWire() {
		tm -= 10;
		if (tm < 1) {
			if (tsk != null) {
				tsk.cancel();
			}
			bmb.expld(this);
			bmb = null;
			rndWn(Team.Ts, "§c\u9299", "§7§lБомба взорвалась, §4Террористы §7победили!", "kaboom");
		}
	}

	public void plant(final Block b) {
		if (bmb == null) {
			bmb = new Bomb(b);
			tm = 45;
			for (final Entry<Shooter, Team> e : shtrs.entrySet()) {
				final Player p = e.getKey().getPlayer();
				if (p != null) {
					p.closeInventory();
					PacketUtils.sendTtlSbTtl(p, "§4\u926e", "§c§lБомба установлена!", 30);
					p.playSound(p.getLocation(), "cs.info." + (e.getValue() == Team.Ts ? "tplant" : "ctplant"), 10f, 1f);
				}
			}
		} else {
			Bukkit.broadcast(Component.text("no"));
		}
	}

	public WXYZ getBombLoc() {
		return bmb;
	}

	public void indSts(final Player p) {
		final PlayerConnection pc = PacketUtils.getNMSPl(p).c;
		if (indon) {
			for (final EntityItem e : inds) {
				pc.a(new PacketPlayOutEntityDestroy(e.af()));
			}
			indon = false;
		} else {
			final org.bukkit.scoreboard.Team tm = pc.getCraftPlayer().getScoreboard().getTeam("ind");
			for (final EntityItem e : inds) {
				tm.addEntry(e.ct().toString());
				pc.a(new PacketPlayOutSpawnEntity(e));
				pc.a(new PacketPlayOutEntityMetadata(e.af(), e.aj().c()));
			}
			indon = true;
		}
	}
	
	public WXYZ getBombPos() {//bomb ITEM
		return bLoc;
	}
	
	public void dropBomb(final Item bmb) {
		final Location loc = bmb.getLocation();
		for (final Entry<Shooter, Team> n : shtrs.entrySet()) {
			final Player pl = n.getKey().getPlayer();
			if (pl != null) {
				pl.playSound(loc, "cs.info." + (n.getValue() == Team.Ts ? "tdropbmb" : "ctdropbmb"), 10f, 1f);
//				pl.getScoreboard().getTeam("bmb").addEntry(bmb.getUniqueId().toString());
				VM.getNmsEntitygroup().colorGlow(bmb, '4', true);
			}
		}
		bmb.setGlowing(true);
		bmb.setPersistent(true);
		bLoc = new WXYZ(bmb.getLocation().add(bmb.getVelocity().setY(0d).multiply(10d)));
	}
	
	public void pickBomb() {
		bLoc = null;
		for (final Entry<Shooter, Team> n : shtrs.entrySet()) {
			final Player pl = n.getKey().getPlayer();
			if (pl != null) {
				if (n.getValue() == Team.Ts) {
					pl.playSound(pl.getLocation(), "cs.info.tpkpbmb", 1f, 1f);
				}
			}
		}
	}

    private Team getMinTm() {
		final int tn = getTmAmt(Team.Ts, true, false);
		final int ctn = getTmAmt(Team.CTs, true, false);
		return tn < ctn ? Team.Ts : (tn == ctn ? (Main.srnd.nextBoolean() ? Team.Ts : Team.CTs) : Team.CTs);
	}

	public int getWns(final Team tm) {
		byte i = 0;
		for (final Team t : wns) {
			if (t == null) {
				return i;
			} else if (t == tm) {
				i++;
			}
		}
		return i;
	}
	
	private byte getRnd() {
		for (byte i = (byte) (wns.length - 1); i >= 0; i--) {
			if (wns[i] != null) {
				return (byte) (i + 1);
			}
		}
		return 0;
	}

	private int getBns(final Team tm) {
		int t = 4;
		int n = 0;
		boolean frst = true;
		for (int i = wns.length - 1; i >= 0; i--) {
			if (wns[i] != null) {
				if (wns[i] == tm) {
					if (frst) {
						n+=2;
						frst = false;
					}
				} else {
					if (frst) {
						frst = false;
					}
					n++;
				}
				if ((t--) == 0) {
					return n * 250;
				}
			}
		}
		return n * 250;
	}

	private boolean noBmb() {
		for (final Shooter sh : shtrs.keySet()) {
			if (sh.inv().contains(Material.GOLDEN_APPLE)) {
				return false;
			}
		}
		return true;
	}
}
