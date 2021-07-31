package me.Romindous.CounterStrike.Game;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.bukkit.Difficulty;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import me.Romindous.CounterStrike.Main;
import me.Romindous.CounterStrike.Enums.GameState;
import me.Romindous.CounterStrike.Objects.BrknBlck;
import me.Romindous.CounterStrike.Objects.Shooter;
import me.Romindous.CounterStrike.Objects.TripWire;
import net.minecraft.EnumChatFormat;
import net.minecraft.core.BaseBlockPosition;

public class Arena {
	
	public final LinkedHashMap<Shooter, Team> shtrs = new LinkedHashMap<>();
	public final HashSet<BrknBlck> brkn = new HashSet<>();
	public final HashSet<TripWire> tws = new HashSet<>();
	public final String name;
	public final World w;
	public final byte min;
	public final byte max;
	protected final BaseBlockPosition[] TSps;
	protected final BaseBlockPosition[] CTSps;
	protected short tm;
	public GameState gst;
	
	public Arena(final String name, final byte min, final byte max, final BaseBlockPosition[] TSps, final BaseBlockPosition[] CTSps, final World w) {
		this.w = w;
		w.setDifficulty(Difficulty.PEACEFUL);
		this.gst = GameState.WAITING;
		this.TSps = TSps;
		this.CTSps = CTSps;
		this.name = name;
		this.min = min;
		this.max = max;
		this.tm = 0;
	}

	protected Arena getArena() {
		return this;
	}
	
	public Shooter getShtr(final String nm) {
		for (final Shooter s : shtrs.keySet()) {
			if (s.nm.equals(nm)) {
				return s;
			}
		}
		return null;
	}
	
	public static Arena getPlArena(final Shooter pl) {
		for (final Arena ar : Main.actvarns) {
			if (ar.shtrs.keySet().contains(pl)) {
				return ar;
			}
		}
		if (Main.shtrs.contains(pl)) {
			return null;
		}
		return null;
	}
	
	public static Arena getNameArena(final String nm) {
		for (final Arena ar : Main.actvarns) {
			if (ar.name.equals(nm)) {
				return ar;
			}
		}
		return null;
	}
	
	public enum Team {
		
		Ts(EnumChatFormat.e, "§4\u9265"),
		CTs(EnumChatFormat.d, "§3\u9264"),
		NA(EnumChatFormat.h, "");
		
		protected final String icn;
		protected final EnumChatFormat clr;
		
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
		Main.actvarns.remove(ar);
		for (final Shooter sh : ar.shtrs.keySet()) {
			Main.lobbyPl((Player) sh.inv.getHolder());
		}
		//ApiOstrov.sendArenaData(ar.name, ru.komiss77.enums.GameState.ОЖИДАНИЕ, "§7[§5CS§7]", "§dОжидание", " ", "§7Игроков: §50§7/§5" + ar.min, "", 0);
		ar = null;
	}

	public boolean addPl(final Shooter sh) {
		sh.inv.getHolder().sendMessage(Main.prf() + "Что то пошло не так...");
		return false;
	}

	public boolean rmvPl(final Shooter sh) {
		sh.inv.getHolder().sendMessage(Main.prf() + "Что то пошло не так...");
		return false;
	}

	public void killPl(final Shooter sh) {
		sh.inv.getHolder().sendMessage(Main.prf() + "Что то пошло не так...");
	}

	public void chngMn(final Shooter sh, final int n) {
		sh.money += n;
	}

	public void addKll(final Shooter sh) {
		sh.kls++;
	}

	public void addDth(final Shooter sh) {
		sh.dths++;
	}
	
	public String getShtrNm(final String nm) {
		final Team tm = shtrs.get(getShtr(nm));
		if (tm == null) {
			return "§f" + nm;
		}
		switch (tm) {
		case Ts:
			return "§4" + nm;
		case CTs:
			return "§3" + nm;
		case NA:
		default:
			return "§f" + nm;
		}
	}

	public boolean isSmTm(final Shooter org, final String cmp) {
		for (final Entry<Shooter, Team> e : shtrs.entrySet()) {
			if (e.getKey().nm.equals(cmp)) {
				return shtrs.get(org) == e.getValue();
			}
		}
		return true;
	}

	public boolean canOpnShp(final Location loc, final Team tm) {
		switch (tm) {
		case Ts:
			for (final BaseBlockPosition b : TSps) {
				if (Math.abs(loc.getBlockX() - b.getX()) < 3 && Math.abs(loc.getBlockZ() - b.getZ()) < 3) {
					return true;
				}
			}
			break;
		case CTs:
			for (final BaseBlockPosition b : CTSps) {
				if (Math.abs(loc.getBlockX() - b.getX()) < 3 && Math.abs(loc.getBlockZ() - b.getZ()) < 3) {
					return true;
				}
			}
			break;
		case NA:
			break;
		}
		return false;
	}
}
