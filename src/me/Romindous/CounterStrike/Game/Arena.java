package me.Romindous.CounterStrike.Game;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Difficulty;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;

import me.Romindous.CounterStrike.Main;
import me.Romindous.CounterStrike.Enums.GameState;
import me.Romindous.CounterStrike.Enums.GameType;
import me.Romindous.CounterStrike.Objects.Shooter;
import me.Romindous.CounterStrike.Objects.Game.PlShooter;
import me.Romindous.CounterStrike.Objects.Game.TripWire;
import me.Romindous.CounterStrike.Objects.Loc.BrknBlck;
import me.Romindous.CounterStrike.Objects.Skins.Quest;
import me.Romindous.CounterStrike.Objects.Skins.SkinQuest;
import me.Romindous.CounterStrike.Utils.Inventories;
import me.Romindous.CounterStrike.Utils.PacketUtils;
import net.minecraft.EnumChatFormat;
import net.minecraft.core.BaseBlockPosition;
import net.minecraft.core.BlockPosition;
import ru.komiss77.ApiOstrov;

public class Arena {
	
	public final HashMap<Shooter, Team> shtrs = new HashMap<>();
	public final HashSet<BrknBlck> brkn = new HashSet<>();
	public final LinkedList<TripWire> tws = new LinkedList<>();
	public final String name;
	public final World w;
	public final byte min;
	public final byte max;
	protected final BaseBlockPosition[] TSps;
	protected final BaseBlockPosition[] CTSps;
	protected final BaseBlockPosition[] TPss;
	protected final BaseBlockPosition[] CTPss;
	public final boolean bots;
	public final boolean rnd;
	protected BukkitTask tsk;
	protected short tm;
	public GameState gst;
	
	public Arena(final String name, final byte min, final byte max, 
		final BaseBlockPosition[] TSps, final BaseBlockPosition[] CTSps, final World w, 
		final BaseBlockPosition[] TPss, final BaseBlockPosition[] CTPss, final boolean rnd, final boolean bots) {
		this.w = w;
		w.setTime(6000L);
		w.setDifficulty(Difficulty.EASY);
		w.setGameRule(GameRule.DO_MOB_SPAWNING, false);
		w.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
		w.setGameRule(GameRule.NATURAL_REGENERATION, false);
		this.gst = GameState.WAITING;
		this.TSps = TSps;
		this.CTSps = CTSps;
		this.TPss = TPss;
		this.CTPss = CTPss;
		this.name = name;
		this.bots = bots;
		this.rnd = rnd;
		this.min = min;
		this.max = max;
		this.tm = 0;
	}

	protected Arena getArena() {
		return this;
	}
	
//	public static Arena getPlArena(final Shooter pl) {
//		for (final Arena ar : Main.actvarns) {
//			if (ar.shtrs.keySet().contains(pl)) {
//				return ar;
//			}
//		}
//		if (Main.shtrs.contains(pl)) {
//			return null;
//		}
//		return null;
//	}
	
	public enum Team {
		
		Ts(EnumChatFormat.e, "§4\u9265"),
		CTs(EnumChatFormat.d, "§3\u9264"),
		NA(EnumChatFormat.h, "");
		
		public final String icn;
		public final EnumChatFormat clr;
		
		Team(final EnumChatFormat clr, final String icn) {
			this.clr = clr;
			this.icn = icn;
		}
		
		public Team getOpst() {
			switch (this) {
			case Ts:
				return CTs;
			case CTs:
				return Ts;
			case NA:
				return NA;
			}
			return NA;
		}
	}

	public static void end(Arena ar) {
		ar.gst = GameState.WAITING;
		ApiOstrov.sendArenaData(ar.name, ru.komiss77.enums.GameState.ОЖИДАНИЕ, "§7[§5CS§7]", "§8Не Выбрано", " ", "§7Игроков: §50§7/§5" + ar.min, "", 0);
		Main.actvarns.remove(ar.name);
		for (final Shooter sh : ar.shtrs.keySet()) {
			if (sh instanceof PlShooter) {
				Main.lobbyPl(sh.getPlayer());
			}
		}
		ar.shtrs.clear();
		Inventories.updtGm(ar);
		for (final Entity e : ar.w.getEntities()) {
			if (e.getType() != EntityType.PLAYER) {
				e.remove();
			}
		}
		
		if (ar.rnd) {
			Bukkit.getConsoleSender().sendMessage("removeing arena");
			Main.mapBlds.remove(ar.name).remove(ar.w, 3);
		}
		ar = null;
	}

	public boolean addPl(final Shooter sh) {
		final Player p = sh.getPlayer();
		if (p != null) {
			p.sendMessage(Main.prf() + "Что то пошло не так...");
		}
		return false;
	}

	public boolean rmvPl(final Shooter sh) {
		final Player p = sh.getPlayer();
		if (p != null) {
			p.sendMessage(Main.prf() + "Что то пошло не так...");
		}
		return false;
	}

	public void killSh(final Shooter sh) {
		final Player p = sh.getPlayer();
		if (p != null) {
			p.sendMessage(Main.prf() + "Что то пошло не так...");
		}
	}
	
	public void chngMn(final Shooter sh, final int n) {
		sh.money(sh.money() + n);
		final Player p = sh.getPlayer();
		if (p == null) return;
		SkinQuest.tryCompleteQuest(sh, Quest.ЛАТУНЬ, sh.money());
		PacketUtils.sendAcBr(p, (n < 0 ? "§5" : "§d+") + String.valueOf(n) + " §6⛃", 20);
		Main.chgSbdTm(p.getScoreboard(), "mn", "", "§d" + String.valueOf(sh.money()) + " §6⛃");
	}

	public void addKll(final Shooter sh) {
		sh.killsI();
	}

	public void addDth(final Shooter sh) {
		sh.deathsI();
	}
	
	public String getShtrNm(final Shooter sh) {
		final Team tm = sh == null ? null : shtrs.get(sh);
		return (tm == null ? Team.NA.clr : tm.clr) + sh.name();
	}

	public boolean isSmTm(final Shooter org, final Shooter cmp) {
		if (cmp.arena() == null || org.arena() == null || !org.arena().name.equals(cmp.arena().name)) return true;
		final Team tm = shtrs.get(org);
		return tm == null ? false : tm == shtrs.get(cmp);
	}

	public boolean canOpnShp(final Location loc, final Team tm) {
		switch (tm) {
		case Ts:
			for (final BaseBlockPosition b : TSps) {
				if (Math.abs(loc.getBlockX() - b.u()) < 3 && Math.abs(loc.getBlockZ() - b.w()) < 3) {
					return true;
				}
			}
			break;
		case CTs:
			for (final BaseBlockPosition b : CTSps) {
				if (Math.abs(loc.getBlockX() - b.u()) < 3 && Math.abs(loc.getBlockZ() - b.w()) < 3) {
					return true;
				}
			}
			break;
		case NA:
			break;
		}
		return false;
	}

	public void chngTm(final Shooter sh, final Team nv) {
		return;
	}
	
	public GameType getType() {
		return GameType.DEFUSAL;
	}

	public BaseBlockPosition getClosestPos(final BaseBlockPosition loc, final int part, final boolean CTps) {
		final List<BaseBlockPosition> bbps = Arrays.asList(CTps ? CTPss : TPss);
		Collections.shuffle(bbps, Main.srnd);
		int bbi = 0;
		int dd = Integer.MAX_VALUE;
		for (int i = bbps.size() >> part; i > 0; i--) {
			final int d = distSq(loc, bbps.get(i));
			if (d < dd) {
				bbi = i;
				dd = d;
			}
		}
		return bbps.get(bbi);
	}

	public int distSq(final BaseBlockPosition from, final BaseBlockPosition to) {
		final int dx = from.u() - to.u(), dy = from.v() - to.v(), dz = from.w() - to.w();
		return dx * dx + dy * dy + dz * dz;
		
	}

	public static String getTime(final short t, final ChatColor cc) {
		return String.valueOf(cc) + (t / 60 > 9 ? t / 60 : "0" + (t / 60))
			+ "§7:"
			+ String.valueOf(cc) + (t % 60 > 9 ? t % 60 : "0" + (t % 60));
	}

	public short getTime() {
		return tm;
	}

	protected static void editLr(final ItemStack it, final boolean add, final String lmnt) {
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
}
