package me.Romindous.CounterStrike.Game;

import java.util.List;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Turtle;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import com.mojang.datafixers.util.Pair;

import me.Romindous.CounterStrike.Main;
import me.Romindous.CounterStrike.Enums.GameState;
import me.Romindous.CounterStrike.Enums.GunType;
import me.Romindous.CounterStrike.Objects.Bomb;
import me.Romindous.CounterStrike.Objects.BrknBlck;
import me.Romindous.CounterStrike.Objects.Shooter;
import me.Romindous.CounterStrike.Objects.TripWire;
import me.Romindous.CounterStrike.Utils.PacketUtils;
import net.minecraft.core.BaseBlockPosition;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy;
import net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata;
import net.minecraft.network.protocol.game.PacketPlayOutSpawnEntity;
import net.minecraft.server.network.PlayerConnection;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.item.EntityItem;
import net.minecraft.world.level.World;

public class Defusal extends Arena {

	private BukkitTask tsk;
	public final BaseBlockPosition ast;
	public final BaseBlockPosition bst;
	private final EntityItem[] inds;
	private Bomb bmb;
	private int TBns;
	private int CTBns;
	private final Team[] wns;
	public boolean indon;
	
	public Defusal(final String name, final byte min, final byte max, final BaseBlockPosition[] TSps, final BaseBlockPosition[] CTSps, final org.bukkit.World w, final BaseBlockPosition ast, final BaseBlockPosition bst, final byte wnr) {
		super(name, min, max, TSps, CTSps, w);
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
					final EntityItem ait = new EntityItem(EntityTypes.Q, wm);
					ait.setInvulnerable(true);
					ait.setNoGravity(true);
					ait.setInvulnerable(true);
					ait.setGlowingTag(true);
					ait.setItemStack(i);
					ait.setCustomName(IChatBaseComponent.a("§4 "));
					ait.setCustomNameVisible(false);
					ait.setPosition(ast.getX() + x + 0.5d, ast.getY(), ast.getZ() + z + 0.5d);
					inds[n] = ait; n++;
					final EntityItem bit = new EntityItem(EntityTypes.Q, wm);
					bit.setInvulnerable(true);
					bit.setNoGravity(true);
					bit.setInvulnerable(true);
					bit.setGlowingTag(true);
					bit.setItemStack(i);
					bit.setCustomName(IChatBaseComponent.a("§4 "));
					bit.setCustomNameVisible(false);
					bit.setPosition(bst.getX() + x + 0.5d, bst.getY(), bst.getZ() + z + 0.5d);
					inds[n] = bit; n++;
				}
			}
		}
	}

	public BukkitTask getTask() {
		return tsk;
	}

	public boolean isBmbOn() {
		return bmb != null;
	}

	public static String getTime(final short t, final ChatColor cc) {
		return String.valueOf(cc) + (t / 60 > 9 ? t / 60 : "0" + (t / 60))
			+ "§7:"
			+ String.valueOf(cc) + (t % 60 > 9 ? t % 60 : "0" + (t % 60));
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
			shtrs.put(sh, Team.NA);
			sh.inv.setItem(4, Main.mkItm(Material.NETHER_STAR, "§eВыбор Комманды", 1));
			sh.inv.setItem(8, Main.mkItm(Material.GHAST_TEAR, "§5Магазин", 1));
			if (shtrs.size() == min) {
				for (final Shooter s : shtrs.keySet()) {
					beginScore(s);
					final Player p = (Player) s.inv.getHolder();
					p.sendMessage(Main.prf() + "§7Игрок §5" + sh.nm + " §7зашел на карту!");
					PacketUtils.sendAcBr(p, "§7Игроков: §5" + shtrs.size() + " §7из §5" + max + " §7(макс)!", 30);
					p.teleport(Main.getNrLoc(Main.srnd.nextBoolean() ? TSps[Main.srnd.nextInt(TSps.length)] : CTSps[Main.srnd.nextInt(CTSps.length)], w));
					PacketUtils.sendNmTg(new Pair<Shooter, Arena>(s, this), "§7<§d" + name + "§7> ", " §7[-.-]", Team.NA.clr);
					for (final Player pl : Bukkit.getOnlinePlayers()) {
						if (p.getWorld().getName().equals(pl.getWorld().getName())) {
							pl.showPlayer(Main.plug, p);
							p.showPlayer(Main.plug, pl);
						} else {
							pl.hidePlayer(Main.plug, p);
							p.hidePlayer(Main.plug, pl);
						}
					}
				}
				cntBeg();
			} else {
				final int rm = min - shtrs.size();
				waitScore(sh, rm);
				PacketUtils.sendNmTg(new Pair<Shooter, Arena>(sh, this), "§7<§d" + name + "§7> ", " §7[-.-]", Team.NA.clr);
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
			sh.inv.setItem(4, Main.mkItm(Material.NETHER_STAR, "§eВыбор Комманды", 1));
			sh.inv.setItem(8, Main.mkItm(Material.GHAST_TEAR, "§5Магазин", 1));
			sh.inv.getHolder().teleport(Main.getNrLoc(Main.srnd.nextBoolean() ? TSps[Main.srnd.nextInt(TSps.length)] : CTSps[Main.srnd.nextInt(CTSps.length)], w));
			beginScore(sh);
			PacketUtils.sendNmTg(new Pair<Shooter, Arena>(sh, this), "§7<§d" + name + "§7> ", " §7[-.-]", Team.NA.clr);
			for (final Shooter s : shtrs.keySet()) {
				s.inv.getHolder().sendMessage(Main.prf() + "§7Игрок §5" + sh.nm + " §7зашел на карту!");
				//Main.chgSbdTm(p.getScoreboard(), "tamt", "", "§4" + String.valueOf(getTmAmt(Team.Ts)) + (e.getValue() == Team.Ts ? " §7чел. §8✦ Вы" : " §7чел."));
				//Main.chgSbdTm(p.getScoreboard(), "ctamt", "", "§3" + String.valueOf(getTmAmt(Team.CTs)) + (e.getValue() == Team.CTs ? " §7чел. §8✦ Вы" : " §7чел."));
				PacketUtils.sendAcBr((Player) s.inv.getHolder(), "§7Игроков: §5" + shtrs.size() + " §7из §5" + max + " §7(макс)!", 30);
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
			chngMn(sh, 500 + (tm == Team.Ts ? TBns : CTBns));
			PacketUtils.sendNmTg(new Pair<Shooter, Arena>(sh, this), tm.icn + " ", " §7[0-0]", tm.clr);
			for (final Entry<Shooter, Team> e : shtrs.entrySet()) {
				final Player p = (Player) e.getKey().inv.getHolder();
				p.sendMessage(Main.prf() + "§7Игрок §5" + sh.nm + " §7зашел играть за комманду " + tm.icn + "§7!");
				Main.chgSbdTm(p.getScoreboard(), "tamt", "", "§4" + String.valueOf(getTmAmt(Team.Ts)) + (e.getValue() == Team.Ts ? " §7чел. §8✦ Вы" : " §7чел."));
				Main.chgSbdTm(p.getScoreboard(), "ctamt", "", "§3" + String.valueOf(getTmAmt(Team.CTs)) + (e.getValue() == Team.CTs ? " §7чел. §8✦ Вы" : " §7чел."));
				PacketUtils.sendRvsNmTg(PacketUtils.getNMSPlr(pl).b, tm, PacketUtils.getNMSPlr(p), e.getValue(), e.getValue().icn + " ", " §7[0-0]", e.getValue().clr);
			}
			for (final TripWire tw : tws) {
				if (tm == tw.tm) {
					tw.shwNd(PacketUtils.getNMSPlr((Player) sh.inv.getHolder()).b);
				}
			}
			for (final Player p : Bukkit.getOnlinePlayers()) {
				if (p.getWorld().getName().equals(pl.getWorld().getName())) {
					pl.showPlayer(Main.plug, p);
					p.showPlayer(Main.plug, pl);
				} else {
					pl.hidePlayer(Main.plug, p);
					p.hidePlayer(Main.plug, pl);
				}
			}
			break;
		case FINISH:
			sh.inv.getHolder().sendMessage(Main.prf() + "§cЭта игра уже заканчивается!");
			return false;
		}
		return true;
	}

	public void addToTm(final Player p, final Team tm) {
		p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
		p.teleport(Main.getNrLoc(tm == Team.Ts ? TSps[Main.srnd.nextInt(TSps.length)] : CTSps[Main.srnd.nextInt(CTSps.length)], w));
		if (gst == GameState.BUYTIME) {
			p.setGameMode(GameMode.SURVIVAL);
			p.getInventory().setItem(8, Main.mkItm(Material.GHAST_TEAR, "§5Магазин", 1));
			if (tm == Team.Ts) {
				p.getInventory().setItem(2, Main.mkItm(Material.BONE, "§fНож", 1));
			} else {
				p.getInventory().setItem(7, Main.mkItm(Material.GOLD_NUGGET, "§eКусачки", 1));
				p.getInventory().setItem(2, Main.mkItm(Material.BLAZE_ROD, "§fНож", 1));
			}
			final Turtle tl = (Turtle) p.getWorld().spawnEntity(p.getLocation(), EntityType.TURTLE);
			tl.setInvulnerable(true);
			tl.setCollidable(false);
			tl.setInvisible(true);
			tl.setSilent(true);
			tl.setBaby();
			tl.setGravity(false);
			tl.setTicksLived(1);
			tl.addPassenger(p);
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
					Main.chgSbdTm(p.getScoreboard(), "tamt", "", "§4" + String.valueOf(getTmAmt(Team.Ts)) + (e.getValue() == Team.Ts ? " §7чел. §8✦ Вы" : " §7чел."));
					Main.chgSbdTm(p.getScoreboard(), "ctamt", "", "§3" + String.valueOf(getTmAmt(Team.CTs)) + (e.getValue() == Team.CTs ? " §7чел. §8✦ Вы" : " §7чел."));
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
					s.inv.getHolder().teleport(Main.getNrLoc(Main.lobby, Main.lbbyW));
					s.inv.getHolder().sendMessage(Main.prf() + "§7Игрок §5" + sh.nm + " §7вышел с карты,\n§7Слишком мало игроков для начала!");
					PacketUtils.sendAcBr((Player) s.inv.getHolder(), "§7Игроков: §5" + shtrs.size() + " §7из §5" + max + " §7(макс)!", 30);
				}
			} else {
				for (final Entry<Shooter, Team> e : shtrs.entrySet()) {
					final Player p = (Player) e.getKey().inv.getHolder();
					p.sendMessage(Main.prf() + "§7Игрок §5" + sh.nm + " §7вышел с карты!");
					PacketUtils.sendAcBr(p, "§7Игроков: §5" + shtrs.size() + " §7из §5" + max + " §7(макс)!", 30);
					Main.chgSbdTm(p.getScoreboard(), "tamt", "", "§4" + String.valueOf(getTmAmt(Team.Ts)) + (e.getValue() == Team.Ts ? " §7чел. §8✦ Вы" : " §7чел."));
					Main.chgSbdTm(p.getScoreboard(), "ctamt", "", "§3" + String.valueOf(getTmAmt(Team.CTs)) + (e.getValue() == Team.CTs ? " §7чел. §8✦ Вы" : " §7чел."));
				}
			}
			break;
		case BUYTIME:
		case ROUND:
			dropIts(sh.inv, sh.inv.getHolder().getLocation(), tm);
		case ENDRND:
			for (final TripWire tw : tws) {
				if (tm == tw.tm) {
					PacketUtils.getNMSPlr((Player) sh.inv.getHolder()).b.sendPacket(new PacketPlayOutEntityDestroy(tw.eif.getId()));
				}
			}
			if (getTmAmt(Team.CTs) == 0 || getTmAmt(Team.Ts) == 0) {
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
					Main.chgSbdTm(p.getScoreboard(), "tamt", "", "§4" + String.valueOf(getTmAmt(Team.Ts)) + (e.getValue() == Team.Ts ? " §7чел. §8✦ Вы" : " §7чел."));
					Main.chgSbdTm(p.getScoreboard(), "ctamt", "", "§3" + String.valueOf(getTmAmt(Team.CTs)) + (e.getValue() == Team.CTs ? " §7чел. §8✦ Вы" : " §7чел."));
				}
			}
			break;
		case FINISH:
			if (shtrs.size() == 0) {
				this.tm = 1;
			}
			break;
		}
		Main.lobbyPl((Player) sh.inv.getHolder());
		return true;
	}

	//счетчик
	public void cntBeg() {
		tm = 10;
		gst = GameState.BEGINING;
		tsk = new BukkitRunnable() {
			@Override
			public void run() {
				switch (tm) {
				case 10:
				case 5:
				case 4:
					for (final Shooter sh : shtrs.keySet()) {
						PacketUtils.sendSbTtl((Player) sh.inv.getHolder(), "§5§l" + tm, 10);
						Main.chgSbdTm(((Player) sh.inv.getHolder()).getScoreboard(), "rmn", "", "§5" + String.valueOf(tm));
					}
					break;
				case 3:
				case 2:
				case 1:
					for (final Shooter sh : shtrs.keySet()) {
						PacketUtils.sendSbTtl((Player) sh.inv.getHolder(), "§d§l" + tm, 10);
						Main.chgSbdTm(((Player) sh.inv.getHolder()).getScoreboard(), "rmn", "", "§5" + String.valueOf(tm));
					}
					break;
				case 0:
					if (tsk != null) {
						tsk.cancel();
					}
					cntBuy(true);
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

	public void cntBuy(final boolean pstl) {
		tm = 10;
		TBns = getBns(Team.Ts);
		CTBns = getBns(Team.CTs);
		gst = GameState.BUYTIME;
		for (final TripWire tw : tws) {
			tw.rmv(this);
		}
		tws.clear();
		for (final Entity e : w.getEntitiesByClasses(Item.class, ArmorStand.class, Turtle.class)) {
			e.remove();
		}
		Shooter bc = null;
		if (pstl) {
			blncTms();
			swtchTms();
			for (final Entry<Shooter, Team> e : shtrs.entrySet()) {
				final Shooter sh = e.getKey();
				final Player p = (Player) sh.inv.getHolder();
				e.getKey().money = 0;
				Main.nrmlzPl(p, true);
				gameScore(e.getKey());
				PacketUtils.sendNmTg(new Pair<Shooter, Arena>(e.getKey(), this), e.getValue().icn + " ", " §7[" + sh.kls + "-" + sh.dths + "]", e.getValue().clr);
				sh.inv.setItem(8, Main.mkItm(Material.GHAST_TEAR, "§5Магазин", 1));
				switch (e.getValue()) {
				case Ts:
					p.teleport(Main.getNrLoc(TSps[Main.srnd.nextInt(TSps.length)], w));
					if (bc == null || e.getKey().kls > bc.kls) {
						 bc = e.getKey();
					}
					sh.inv.setItem(2, Main.mkItm(Material.BONE, "§fНож", 1));
					chngMn(e.getKey(), 500);
					break;
				case CTs:
					p.teleport(Main.getNrLoc(CTSps[Main.srnd.nextInt(CTSps.length)], w));
					sh.inv.setItem(7, Main.mkItm(Material.GOLD_NUGGET, "§eКусачки", 1));
					sh.inv.setItem(2, Main.mkItm(Material.BLAZE_ROD, "§fНож", 1));
					chngMn(e.getKey(), 500);
					break;
				case NA:
					break;
				}
				final Turtle tl = (Turtle) p.getWorld().spawnEntity(p.getLocation(), EntityType.TURTLE);
				tl.setInvulnerable(true);
				tl.setCollidable(false);
				tl.setInvisible(true);
				tl.setSilent(true);
				tl.setBaby();
				tl.setGravity(false);
				tl.setTicksLived(1);
				tl.addPassenger(p);
				tl.setAI(true);
				PacketUtils.sendTtlSbTtl(p, getRnd() == 0 ? "" : "§7Смена Ролей", "§l§6⛃§5Закупка§6⛃", 30);
			}
		} else {
			for (final Entry<Shooter, Team> e : shtrs.entrySet()) {
				final Shooter sh = e.getKey();
				final Player p = (Player) sh.inv.getHolder();
				if (p.getGameMode() == GameMode.SPECTATOR) {
					Main.nrmlzPl(p, true);
					gameScore(e.getKey());
					PacketUtils.sendNmTg(new Pair<Shooter, Arena>(e.getKey(), this), e.getValue().icn + " ", " §7[" + sh.kls + "-" + sh.dths + "]", e.getValue().clr);
					sh.inv.setItem(8, Main.mkItm(Material.GHAST_TEAR, "§5Магазин", 1));
					switch (e.getValue()) {
					case Ts:
						p.teleport(Main.getNrLoc(TSps[Main.srnd.nextInt(TSps.length)], w));
						if (bc == null || e.getKey().kls > bc.kls) {
							 bc = e.getKey();
						}
						sh.inv.setItem(2, Main.mkItm(Material.BONE, "§fНож", 1));
						chngMn(e.getKey(), 500 + TBns);
						break;
					case CTs:
						p.teleport(Main.getNrLoc(CTSps[Main.srnd.nextInt(CTSps.length)], w));
						sh.inv.setItem(7, Main.mkItm(Material.GOLD_NUGGET, "§eКусачки", 1));
						sh.inv.setItem(2, Main.mkItm(Material.BLAZE_ROD, "§fНож", 1));
						chngMn(e.getKey(), 500 + CTBns);
						break;
					case NA:
						break;
					}
				} else {
					Main.nrmlzPl(p, false);
					if (sh.inv.getItem(0) != null) {
						sh.inv.getItem(0).setAmount(GunType.getGnTp(sh.inv.getItem(0)).amo);
					}
					if (sh.inv.getItem(1) != null) {
						sh.inv.getItem(1).setAmount(GunType.getGnTp(sh.inv.getItem(1)).amo);
					}
					switch (e.getValue()) {
					case Ts:
						sh.inv.setItem(7, new ItemStack(Material.AIR));
						p.teleport(Main.getNrLoc(TSps[Main.srnd.nextInt(TSps.length)], w));
						if (bc == null || e.getKey().kls > bc.kls) {
							 bc = e.getKey();
						}
						chngMn(e.getKey(), 500 + TBns);
						break;
					case CTs:
						p.teleport(Main.getNrLoc(CTSps[Main.srnd.nextInt(CTSps.length)], w));
						chngMn(e.getKey(), 500 + CTBns);
						break;
					case NA:
						break;
					}
					Main.chgSbdTm(p.getScoreboard(), "scr", "§l§4\u9265 §7: §4" + getWns(Team.Ts), "§l§3" + getWns(Team.CTs) + " §7: §3\u9264");
					Main.chgSbdTm(p.getScoreboard(), "mn", "", "§d" + String.valueOf(e.getKey().money) + " §6⛃");
					Main.chgSbdTm(p.getScoreboard(), "gst", "", "§dЗакупка");
				}
				final Turtle tl = (Turtle) p.getWorld().spawnEntity(p.getLocation(), EntityType.TURTLE);
				tl.setInvulnerable(true);
				tl.setCollidable(false);
				tl.setInvisible(true);
				tl.setSilent(true);
				tl.setBaby();
				tl.setGravity(false);
				tl.setTicksLived(1);
				tl.addPassenger(p);
				tl.setAI(true);
				PacketUtils.sendSbTtl(p, "§l§6⛃§5Закупка§6⛃", 30);
			}
		} 
		if (noBmb()) {
			bmb = null;
			bc.inv.setItem(7, Main.mkItm(Material.GOLDEN_APPLE, "§4§lС-4 §c\u926e", 1, "§dПКМ §7- Заложить бомбу", "§7Можно установить на точку §5A §7или §5B"));
		}
		tsk = new BukkitRunnable() {
			@Override
			public void run() {
				final String t = getTime(tm, ChatColor.DARK_PURPLE);
				switch (tm) {
				case 3:
				case 2:
				case 1:
					for (final Shooter sh : shtrs.keySet()) {
						PacketUtils.sendSbTtl((Player) sh.inv.getHolder(), "§d§l" + tm, 10);
						Main.chgSbdTm(((Player) sh.inv.getHolder()).getScoreboard(), "tm", t, "");
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
		tm = 180;
		gst = GameState.ROUND;
		for (final Entry<Shooter, Team> e : shtrs.entrySet()) {
			final PlayerInventory inv = e.getKey().inv;
			final Entity v = inv.getHolder().getVehicle();
			v.eject(); v.remove();
			PacketUtils.sendSbTtl((Player) inv.getHolder(), (e.getValue() == Team.Ts ? "§4" : "§3") + "§lВперед", 30);
			Main.chgSbdTm(((Player) inv.getHolder()).getScoreboard(), "gst", "", "§dБой");
		}
		tsk = new BukkitRunnable() {
			@Override
			public void run() {
				final String t = getTime(tm, bmb == null || (tm & 1) == 0 ? ChatColor.LIGHT_PURPLE : ChatColor.RED);
				switch (tm) {
				case 60:
					for (final Shooter sh : shtrs.keySet()) {
						PacketUtils.sendAcBr(((Player) sh.inv.getHolder()), "§7Осталась §d1 §7минута!", 30);
						Main.chgSbdTm(((Player) sh.inv.getHolder()).getScoreboard(), "tm", t, "");
					}
					break;
				case 30:
				case 10:
					for (final Shooter sh : shtrs.keySet()) {
						PacketUtils.sendAcBr(((Player) sh.inv.getHolder()), "§7Осталось §d" + tm + " §7секунд!", 30);
						Main.chgSbdTm(((Player) sh.inv.getHolder()).getScoreboard(), "tm", t, "");
					}
					break;
				case 0:
					if (bmb == null) {
						rndWn(Team.CTs, "§9⌛", "§7§lВремя вышло, §3Спецназ §7победил!");
					} else {
						bmb.expld((Defusal) getArena());
						bmb = null;
						rndWn(Team.Ts, "§c\u9299", "§7§lБомба взорвалась, §4Террористы §7победили!");
					}
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

	private void swtchTms() {
		for (final Entry<Shooter, Team> e : shtrs.entrySet()) {
			e.setValue(e.getValue().getOpst());
			if (indon && e.getKey().inv.contains(Material.GOLDEN_APPLE)) {
				indSts(PacketUtils.getNMSPlr((Player) e.getKey().inv.getHolder()).b);
			}
		}
		for (byte i = (byte) (wns.length - 1); i >= 0; i--) {
			wns[i] = wns[i] == null ? null : wns[i].getOpst();
		}
	}

	private void rndWn(final Team tm, final String ttl, final String sbttl) {
		if (tsk != null) {
			tsk.cancel();
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
		
		if (bmb != null) {
			bmb.ttl.killEntity();
			bmb.sbttl.killEntity();
			bmb.getBlock().setType(Material.AIR, false);
			bmb = null;
		}
		
		addWn(tm);
		if (getWns(tm) == wns.length / 2 + 1) {
			for (final Shooter sh : shtrs.keySet()) {
				sh.inv.getHolder().closeInventory();
				sh.inv.getHolder().sendMessage(" \n§7Победа в раунде: " + (tm == Team.CTs ? "§3Спецназ" : "§4Террористы")
					+ "\n \n§5=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-\n§7Лучший спецназовец: §3" + bct.nm + 
					"\n          §7--=x=--\n§7Самый злобный террорист: §4" + bt.nm + "\n§5=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-");
			}
			cntFnsh(tm);
		} else {
			for (final Shooter sh : shtrs.keySet()) {
				final Player p = (Player) sh.inv.getHolder();
				p.closeInventory();
				Main.chgSbdTm(p.getScoreboard(), "tm", "§d00:00", "");
				PacketUtils.sendTtlSbTtl(p, ttl, sbttl, 30);
				p.sendMessage(" \n§7Победа в раунде: " + (tm == Team.CTs ? "§3Спецназ" : "§4Террористы")
					+ "\n \n§5=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-\n§7Лучший спецназовец: §3" + bct.nm + 
					"\n          §7--=x=--\n§7Самый злобный террорист: §4" + bt.nm + "\n§5=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-");
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
		for (final Entity e : w.getEntitiesByClasses(Item.class, ArmorStand.class, Turtle.class)) {
			e.remove();
		}
		if (bmb != null) {
			bmb.ttl.killEntity();
			bmb.sbttl.killEntity();
			bmb.getBlock().setType(Material.AIR, false);
			bmb = null;
		}
		final String st;
		switch (wn) {
		case Ts:
			st = "§4§lТеррористы §7выйграли!";
			break;
		case CTs:
			st = "§3§lСпецназ §7одержал победу!";
			break;
		default:
			st = " ";
			break;
		}
		for (final Entry<Shooter, Team> e : shtrs.entrySet()) {
			final Shooter sh = e.getKey();
			PacketUtils.sendTtlSbTtl((Player) sh.inv.getHolder(), "§5Финиш", st, 40);
			PacketUtils.sendNmTg(new Pair<Shooter, Arena>(sh, this), "§7<§d" + name + "§7> ", " §7[" + sh.kls + "-" + sh.dths + "]", Team.NA.clr);
			if (indon && sh.inv.contains(Material.GOLDEN_APPLE)) {
				indSts(PacketUtils.getNMSPlr((Player) sh.inv.getHolder()).b);
			}
			sh.inv.clear();
			winScore(sh, wn == Team.CTs);
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
		((Player) sh.inv.getHolder()).setScoreboard(sb);
	}

	private void beginScore(final Shooter sh) {
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
		((Player) sh.inv.getHolder()).setScoreboard(sb);
	}
	
	private void gameScore(final Shooter sh) {
		final Scoreboard sb = Main.smg.getNewScoreboard();
		final org.bukkit.scoreboard.Team t = sb.registerNewTeam("ind");
		t.setColor(ChatColor.DARK_RED);
		final Objective ob = sb.registerNewObjective("CS:GO", "", "§7[§5CS:GO§7]");
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
	}

	private void winScore(final Shooter sh, final boolean isCTWn) {
		final Scoreboard sb = Main.smg.getNewScoreboard();
		final Objective ob = sb.registerNewObjective("CS:GO", "", "§7[§5CS:GO§7]");
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
		ob.getScore("§4\u9265 §7: §4" + String.valueOf(getTmAmt(Team.Ts)) + (shtrs.get(sh) == Team.Ts ? " §7чел. §8✦ Вы" : " §7чел."))
		.setScore(6);
		ob.getScore("§3\u9264 §7: §3" + String.valueOf(getTmAmt(Team.CTs)) + (shtrs.get(sh) == Team.CTs ? " §7чел. §8✦ Вы" : " §7чел."))
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

	@Override
	public void killPl(final Shooter sh) {
		final Player p = (Player) sh.inv.getHolder();
		if (gst == GameState.ROUND) {
			addDth(sh);
			final Team tm = shtrs.get(sh);
			dropIts(sh.inv, p.getLocation(), tm);
			p.setGameMode(GameMode.SPECTATOR);
			final byte n = getTmAmt(tm);
			switch (tm) {
			case Ts:
				for (final Entry<Shooter, Team> e : shtrs.entrySet()) {
					Main.chgSbdTm(p.getScoreboard(), "tamt", "", "§4" + String.valueOf(n) + (e.getValue() == Team.Ts ? " §7чел. §8✦ Вы" : " §7чел."));
				}
				if (n == 0 && bmb == null) {
					rndWn(Team.CTs, "§9\u9264", "§7§lТеррористы были убиты!");
				}
				break;
			case CTs:
				for (final Entry<Shooter, Team> e : shtrs.entrySet()) {
					Main.chgSbdTm(p.getScoreboard(), "ctamt", "", "§3" + String.valueOf(n) + (e.getValue() == Team.CTs ? " §7чел. §8✦ Вы" : " §7чел."));
				}
				if (n == 0) {
					rndWn(Team.Ts, "§c\u9265", "§7§lСпецназ пал в бою!");
				}
				break;
			case NA:
				break;
			}
		} else {
			Main.nrmlzPl(p, false);
			p.teleport(Main.getNrLoc(Main.srnd.nextBoolean() ? TSps[Main.srnd.nextInt(TSps.length)] : CTSps[Main.srnd.nextInt(CTSps.length)], w));
		}
	}
	
	public void dropIts(final PlayerInventory inv, final Location loc, final Team tm) {
		ItemStack it = inv.getItem(0);
		if (it != null) {
			w.dropItemNaturally(loc, it);
		}
		it = inv.getItem(1);
		if (it != null) {
			w.dropItemNaturally(loc, it);
		}
		it = inv.getItem(3);
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
		it = inv.getItem(7);
		switch (tm) {
		case Ts:
			if (it != null) {
				//bomb dropped
				if (indon) {
					indSts(PacketUtils.getNMSPlr((Player) inv.getHolder()).b);
				}
				w.dropItemNaturally(loc, it);
			}
			break;
		case CTs:
			if (it != null && it.getType() == Material.SHEARS) {
				w.dropItemNaturally(loc, it);
			}
			break;
		case NA:
			break;
		}
		inv.clear();
	}

	@Override
	public void chngMn(final Shooter sh, final int n) {
		sh.money += n;
		PacketUtils.sendAcBr((Player) sh.inv.getHolder(), (n < 0 ? "§5" : "§d+") + String.valueOf(n) + " §6⛃", 20);
		Main.chgSbdTm(((Player) sh.inv.getHolder()).getScoreboard(), "mn", "", "§d" + String.valueOf(sh.money) + " §6⛃");
	}

	@Override
	public void addKll(final Shooter sh) {
		sh.kls++;
		PacketUtils.sendNmTg(new Pair<Shooter, Arena>(sh, this), shtrs.get(sh).icn + " ", " §7[" + sh.kls + "-" + sh.dths + "]", shtrs.get(sh).clr);
	}

	@Override
	public void addDth(final Shooter sh) {
		sh.dths++;
		PacketUtils.sendNmTg(new Pair<Shooter, Arena>(sh, this), shtrs.get(sh).icn + " ", " §7[" + sh.kls + "-" + sh.dths + "]", shtrs.get(sh).clr);
	}
	
	public byte getTmAmt(final Team tm) {
		byte n = 0;
		for (final Entry<Shooter, Team> e : shtrs.entrySet()) {
			if (e.getValue() == tm && e.getKey().inv.getHolder().getGameMode() == GameMode.SURVIVAL) {
				n++;
			}
		}
		return n;
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

	public void chngTm(final Shooter sh, final Team tm) {
		if (getTmAmt(tm) == 0 || getTmAmt(tm) < getTmAmt(tm.getOpst())) {
			shtrs.replace(sh, tm);
			for (final Entry<Shooter, Team> e : shtrs.entrySet()) {
				Main.chgSbdTm(((Player) e.getKey().inv.getHolder()).getScoreboard(), "tamt", "", "§4" + String.valueOf(getTmAmt(Team.Ts)) + (e.getValue() == Team.Ts ? " §7чел. §8✦ Вы" : " §7чел."));
				Main.chgSbdTm(((Player) e.getKey().inv.getHolder()).getScoreboard(), "ctamt", "", "§3" + String.valueOf(getTmAmt(Team.CTs)) + (e.getValue() == Team.CTs ? " §7чел. §8✦ Вы" : " §7чел."));
			}
		} else {
			sh.inv.getHolder().sendMessage(Main.prf() + "§cВ этой комманде слишком много игроков!");
		}
	}

	private void blncTms() {
		for (final Entry<Shooter, Team> e : shtrs.entrySet()) {
			if (e.getValue() == Team.NA) {
				e.setValue(getMinTm());
			}
		}
		for (final Entry<Shooter, Team> e : shtrs.entrySet()) {
			Main.chgSbdTm(((Player) e.getKey().inv.getHolder()).getScoreboard(), "tamt", "", "§4" + String.valueOf(getTmAmt(Team.Ts)) + (e.getValue() == Team.Ts ? " §7чел. §8✦ Вы" : " §7чел."));
			Main.chgSbdTm(((Player) e.getKey().inv.getHolder()).getScoreboard(), "ctamt", "", "§3" + String.valueOf(getTmAmt(Team.CTs)) + (e.getValue() == Team.CTs ? " §7чел. §8✦ Вы" : " §7чел."));
		}
	}

	public void defuse() {
		if (bmb == null) {
			Bukkit.broadcastMessage("no");
		} else {
			bmb.kllAs();
			bmb.getBlock().setType(Material.AIR);
			bmb = null;
			rndWn(Team.CTs, "§9\u926f", "§3§lБомба разминирована!");
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
			rndWn(Team.Ts, "§c\u9299", "§7Бомба взорвалась, §4§lТеррористы §7победили!");
		}
	}

	public void plant(final Block b) {
		if (bmb == null) {
			bmb = new Bomb(b);
			tm = 45;
			for (final Entry<Shooter, Team> e : shtrs.entrySet()) {
				e.getKey().inv.getHolder().closeInventory();
				PacketUtils.sendTtlSbTtl((Player) e.getKey().inv.getHolder(), "§4\u926e", "§c§lБомба установлена!", 30);
			}
		} else {
			Bukkit.broadcastMessage("no");
		}
	}

	public void indSts(final PlayerConnection pc) {
		if (indon) {
			for (final EntityItem e : inds) {
				pc.sendPacket(new PacketPlayOutEntityDestroy(e.getId()));
			}
			indon = false;
		} else {
			final org.bukkit.scoreboard.Team tm = pc.getPlayer().getScoreboard().getTeam("ind");
			for (final EntityItem e : inds) {
				tm.addEntry(e.getUniqueID().toString());
				pc.sendPacket(new PacketPlayOutSpawnEntity(e));
				pc.sendPacket(new PacketPlayOutEntityMetadata(e.getId(), e.getDataWatcher(), true));
			}
			indon = true;
		}
	}

	private Team getMinTm() {
		final byte tn = getTmAmt(Team.Ts);
		final byte ctn = getTmAmt(Team.CTs);
		return tn < ctn ? Team.Ts : (tn == ctn ? (Main.srnd.nextBoolean() ? Team.Ts : Team.CTs) : Team.CTs);
	}

	private int getWns(final Team tm) {
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
		byte t = 4;
		byte n = 0;
		boolean frst = true;
		for (byte i = (byte) (wns.length - 1); i >= 0; i--) {
			if (wns[i] != null) {
				if (wns[i] == tm) {
					if (frst) {
						return 500;
					}
					t--;
					if (t == 0) {
						return n * 200;
					}
				} else {
					if (frst) {
						frst = false;
					}
					n++;
					t--;
					if (t == 0) {
						return n * 200;
					}
				}
			}
		}
		return n * 200;
	}

	private boolean noBmb() {
		for (final Shooter sh : shtrs.keySet()) {
			if (sh.inv.contains(Material.GOLDEN_APPLE)) {
				return false;
			}
		}
		return true;
	}
}
