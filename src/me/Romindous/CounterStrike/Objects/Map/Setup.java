package me.Romindous.CounterStrike.Objects.Map;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import me.Romindous.CounterStrike.Main;
import me.Romindous.CounterStrike.Enums.GameType;
import me.Romindous.CounterStrike.Objects.Game.TripWire;
import net.minecraft.core.BaseBlockPosition;

public class Setup {
	
	public final String nm;
	public final byte min;
	public final byte max;
	public final boolean rndM;
	public final BaseBlockPosition[] ctSpots;
	public final BaseBlockPosition[] tSpots;
	public final BaseBlockPosition[] ctSpawns;
	public final BaseBlockPosition[] tSpawns;
	public final BaseBlockPosition A;
	public final BaseBlockPosition B;
	public final Map<GameType, String> worlds;
	public final boolean bots;
	public final boolean fin;
	
	public Setup(final ConfigurationSection ar) {
		nm = TripWire.loadMapSound(ar.getName());
		min = (byte) ar.getInt("min");
		max = (byte) ar.getInt("max");
		final EnumMap<GameType, String> ws = new EnumMap<>(GameType.class);
		for (final GameType gt : GameType.values()) {
			ws.put(gt, ar.getString("world." + gt.toString()));
		}
		worlds = Collections.unmodifiableMap(ws);
		rndM = ar.getBoolean("rnd");
		if (rndM) {
			bots = false;
			ctSpots = null;
			tSpots = null;
			ctSpawns = null;
			tSpawns = null;
			A = null;
			B = null;
		} else {
			bots = ar.getBoolean("bots");
			final String[] ctPx = ar.getString("ctspots.x").split(":");
			final String[] ctPy = ar.getString("ctspots.y").split(":");
			final String[] ctPz = ar.getString("ctspots.z").split(":");
			ctSpots = new BaseBlockPosition[ctPx.length];
			for (byte i = (byte) (ctPx.length - 1); i >= 0; i--) {
				ctSpots[i] = new BaseBlockPosition(Integer.parseInt(ctPx[i]), Integer.parseInt(ctPy[i]), Integer.parseInt(ctPz[i]));
			}
			final String[] tPx = ar.getString("tspots.x").split(":");
			final String[] tPy = ar.getString("tspots.y").split(":");
			final String[] tPz = ar.getString("tspots.z").split(":");
			tSpots = new BaseBlockPosition[tPx.length];
			for (byte i = (byte) (tPx.length - 1); i >= 0; i--) {
				tSpots[i] = new BaseBlockPosition(Integer.parseInt(tPx[i]), Integer.parseInt(tPy[i]), Integer.parseInt(tPz[i]));
			}
			final String[] ctSx = ar.getString("ctspawns.x").split(":");
			final String[] ctSy = ar.getString("ctspawns.y").split(":");
			final String[] ctSz = ar.getString("ctspawns.z").split(":");
			ctSpawns = new BaseBlockPosition[ctSx.length];
			for (byte i = (byte) (ctSx.length - 1); i >= 0; i--) {
				ctSpawns[i] = new BaseBlockPosition(Integer.parseInt(ctSx[i]), Integer.parseInt(ctSy[i]), Integer.parseInt(ctSz[i]));
			}
			final String[] tSx = ar.getString("tspawns.x").split(":");
			final String[] tSy = ar.getString("tspawns.y").split(":");
			final String[] tSz = ar.getString("tspawns.z").split(":");
			tSpawns = new BaseBlockPosition[tSx.length];
			for (byte i = (byte) (tSx.length - 1); i >= 0; i--) {
				tSpawns[i] = new BaseBlockPosition(Integer.parseInt(tSx[i]), Integer.parseInt(tSy[i]), Integer.parseInt(tSz[i]));
			}
			A = new BaseBlockPosition(ar.getInt("asite.x"), ar.getInt("asite.y"), ar.getInt("asite.z"));
			B = new BaseBlockPosition(ar.getInt("bsite.x"), ar.getInt("bsite.y"), ar.getInt("bsite.z"));
		}
		fin = ar.getBoolean("fin");
	}
	
	public void save(final YamlConfiguration conf) {
		if (!fin) return;
		conf.set("arenas." + nm, null);
		final ConfigurationSection ar = conf.createSection("arenas." + nm);
		ar.set("min", min);
		ar.set("max", max);
		ar.set("fin", fin);
		ar.set("rnd", rndM);
		ar.set("bots", bots);
		for (final GameType gt : GameType.values()) {
			final String w = worlds.get(gt);
			if (w != null) ar.set("world." + gt.toString(), w);
		}
		if (A != null) {
			ar.set("asite.x", A.u());
			ar.set("asite.y", A.v());
			ar.set("asite.z", A.w());
		}
		if (B != null) {
			ar.set("bsite.x", B.u());
			ar.set("bsite.y", B.v());
			ar.set("bsite.z", B.w());
		}
		if (ctSpots != null) {
			final StringBuffer xs = new StringBuffer();
			final StringBuffer ys = new StringBuffer();
			final StringBuffer zs = new StringBuffer();
			for (final BaseBlockPosition l : ctSpots) {
				xs.append(":" + l.u());
				ys.append(":" + l.v());
				zs.append(":" + l.w());
			}
			ar.set("ctspots.x", xs.substring(1));
			ar.set("ctspots.y", ys.substring(1));
			ar.set("ctspots.z", zs.substring(1));
		}
		if (tSpots != null) {
			final StringBuffer xs = new StringBuffer();
			final StringBuffer ys = new StringBuffer();
			final StringBuffer zs = new StringBuffer();
			for (final BaseBlockPosition l : tSpots) {
				xs.append(":" + l.u());
				ys.append(":" + l.v());
				zs.append(":" + l.w());
			}
			ar.set("tspots.x", xs.substring(1));
			ar.set("tspots.y", ys.substring(1));
			ar.set("tspots.z", zs.substring(1));
		}
		if (ctSpawns != null) {
			final StringBuffer xs = new StringBuffer();
			final StringBuffer ys = new StringBuffer();
			final StringBuffer zs = new StringBuffer();
			for (final BaseBlockPosition l : ctSpawns) {
				xs.append(":" + l.u());
				ys.append(":" + l.v());
				zs.append(":" + l.w());
			}
			ar.set("ctspawns.x", xs.substring(1));
			ar.set("ctspawns.y", ys.substring(1));
			ar.set("ctspawns.z", zs.substring(1));
		}
		if (tSpawns != null) {
			final StringBuffer xs = new StringBuffer();
			final StringBuffer ys = new StringBuffer();
			final StringBuffer zs = new StringBuffer();
			for (final BaseBlockPosition l : tSpawns) {
				xs.append(":" + l.u());
				ys.append(":" + l.v());
				zs.append(":" + l.w());
			}
			ar.set("tspawns.x", xs.substring(1));
			ar.set("tspawns.y", ys.substring(1));
			ar.set("tspawns.z", zs.substring(1));
		}
		try {
			conf.save(new File(Main.plug.getDataFolder() + File.separator + "arenas.yml"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public Setup(final String name) {
		nm = name; min = 2; max = 2; rndM = false;
		ctSpots = null; tSpots = null; ctSpawns = null; tSpawns = null;
		A = null; B = null; fin = false; bots = false;
		worlds = Collections.unmodifiableMap(
			new EnumMap<>(GameType.class));
	}
	
	public Setup(final String name,
		final byte min, final byte max, 
		final boolean rndM, final boolean fin, final boolean bots,
		final BaseBlockPosition[] ctSpots, final BaseBlockPosition[] tSpots, 
		final BaseBlockPosition[] ctSpawns, final BaseBlockPosition[] tSpawns, 
		final BaseBlockPosition A, final BaseBlockPosition B, 
		final Map<GameType, String> worlds) {
		this.nm = name;
		this.min = min;
		this.max = max;
		this.rndM = rndM;
		this.ctSpots = ctSpots;
		this.tSpots = tSpots;
		this.ctSpawns = ctSpawns;
		this.tSpawns = tSpawns;
		this.A = A;
		this.B = B;
		this.fin = fin;
		this.bots = bots;
		this.worlds = worlds;
	}
	
	public boolean isReady() {
		if (nm == null || min < 1 || max < min) return false;
		return rndM || (A != null && B != null && 
		(ctSpots != null && ctSpots.length > 1) && (tSpots != null && tSpots.length > 1) && 
		(ctSpawns != null && ctSpawns.length > 1) && (tSpawns != null && tSpawns.length > 1));
	}

	public void delete(final boolean hard) {
		Main.nnactvarns.remove(nm);
		if (hard) {
			Main.ars.set("arenas." + nm, null);
			try {
				Main.ars.save(new File(Main.plug.getDataFolder() + File.separator + "arenas.yml"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
