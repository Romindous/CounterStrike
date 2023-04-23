package me.Romindous.CounterStrike.Objects.Map;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.util.Vector;

import me.Romindous.CounterStrike.Main;
import me.Romindous.CounterStrike.Game.Arena.Team;
import me.Romindous.CounterStrike.Objects.Game.GameType;
import me.Romindous.CounterStrike.Objects.Game.TripWire;
import me.Romindous.CounterStrike.Objects.Loc.Spot;
import net.minecraft.core.BaseBlockPosition;

public class Setup {
	
	public final String nm;
	public final byte min;
	public final byte max;
	public final boolean rndM;
	public final Spot[] spots;
	public final BaseBlockPosition[] ctSpawns;
	public final BaseBlockPosition[] tSpawns;
	public final BaseBlockPosition A;
	public final BaseBlockPosition B;
	public final Map<GameType, String> worlds;
	public final boolean bots;
	public final boolean fin;
	
	private static final int MAX_REL = 20;
	
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
			spots = null;
			ctSpawns = null;
			tSpawns = null;
			A = null;
			B = null;
		} else {
			bots = ar.getBoolean("bots");
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
			final ConfigurationSection spc = ar.getConfigurationSection("spots");
			if (spc == null) {
				this.spots = null;
			} else {
				final ArrayList<Spot> sps = new ArrayList<>();
				int ix = 0;
				for (final String s : spc.getKeys(false)) {
					final String[] ss = s.split(",");
					sps.add(new Spot(Main.parseInt(ss[0]), Main.parseInt(ss[1]), Main.parseInt(ss[2]), 
						ix, Team.valueOf(ss[3].substring(2)), readIntArr(spc.getString(s), ":")));
					ix++;
				}
				this.spots = sps.toArray(new Spot[sps.size()]);
			}
			A = new BaseBlockPosition(ar.getInt("asite.x"), ar.getInt("asite.y"), ar.getInt("asite.z"));
			B = new BaseBlockPosition(ar.getInt("bsite.x"), ar.getInt("bsite.y"), ar.getInt("bsite.z"));
		}
		fin = ar.getBoolean("fin");
	}
	
	private int[] readIntArr(final String arr, final String rgx) {
		final String[] els = arr.split(rgx);
		final int[] nar = new int[els.length];
		for (int i = 0; i < els.length; i++) {
			nar[i] = Main.parseInt(els[i]);
		}
		return nar;
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
		if (spots != null) {
			final ConfigurationSection spc = ar.createSection("spots");
			for (final Spot sp : spots) {
				final StringBuffer sb = new StringBuffer();
				for (int i : sp.relPos) {
					sb.append(":" + i);
				}
				spc.set(sp.toString(), sb.isEmpty() ? "" : sb.substring(1));
			}
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
		spots = null; ctSpawns = null; tSpawns = null;
		A = null; B = null; fin = false; bots = false;
		worlds = Collections.unmodifiableMap(
			new EnumMap<>(GameType.class));
	}
	
	public Setup(final String name, final byte min, final byte max, 
		final boolean rndM, final boolean fin, final boolean bots,
		final Spot[] spots, final BaseBlockPosition[] ctSpawns, final BaseBlockPosition[] tSpawns, 
		final BaseBlockPosition A, final BaseBlockPosition B, final Map<GameType, String> worlds) {
		this.nm = name;
		this.min = min;
		this.max = max;
		this.rndM = rndM;
		this.spots = spots;
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
		return rndM || (A != null && B != null && hasSpot(Team.Ts) && hasSpot(Team.CTs) &&
		(ctSpawns != null && ctSpawns.length > 1) && (tSpawns != null && tSpawns.length > 1));
	}

	public boolean hasSpot(final Team t) {
		if (spots == null) return false;
		for (final Spot sp : spots) {
			if (sp.tm == t) return true;
		}
		return false;
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

	public Spot[] linkSpots(final World w) {
		if (!bots) return spots;
		final int ln = spots.length;
		final Spot[] nsp = new Spot[ln];
		for (int i = 0; i < ln; i++) {
			nsp[i] = new Spot(spots[i], i, ln);
		}
		
		/*for (final Spot sp : nsp) {//take 1 spot
			final HashSet<Spot> done = new HashSet<>();
			done.add(sp);//itself
			sp.setPosFrom(sp, 0);
			//Bukkit.broadcast(Component.text("start"));
			for (int stp = 1; stp < MAX_REL && done.size() < ln; stp++) {//link steps
				final HashSet<Spot> add = new HashSet<>();
				for (final Spot fr : done) {//link spots per step
					for (final Spot to : nsp) {
						if (!done.contains(to) && !add.contains(to)) {
							if (sp.getPosFrom(to) == 0) {
								if (Main.rayThruSoft(new Location(w, fr.u() + 0.5d, fr.v() + 0.5d, fr.w() + 0.5d), 
									new Vector(to.u() + 0.5d, to.v() + 0.5d, to.w() + 0.5d), 0.6d)) {
									//Bukkit.broadcast(Component.text("dst-"+(sp.getPosFrom(fr) + 1)+",sp-"+sp.toString()+",to-"+to.toString()));
									sp.setPosFrom(to, sp.getPosFrom(fr) + 1);//() -> * -> * -> ()
									to.setPosFrom(sp, sp.getPosFrom(fr) + 1);//() <- * <- * <- ()
									if (stp != 1) {
										fr.setPosFrom(to, 1);//* -> * -> () -> ()
										to.setPosFrom(fr, 1);//* <- * <- () <- ()
									}
									add.add(to);
								}
							} else {
								add.add(to);
							}
						}
					}
				}
				done.addAll(add);
			}
		}*/
		
		//raytracing
		for (final Spot sp : nsp) {//take 1 spot
			sp.setPosFrom(sp, 0);//self
			for (final Spot to : nsp) {//look at others
				if (sp.noPosFrom(to)) {
					if (Main.rayThruSoft(new Location(w, sp.u() + 0.5d, sp.v() + 0.5d, sp.w() + 0.5d), 
							new Vector(to.u() + 0.5d, to.v() + 0.5d, to.w() + 0.5d), 0.6d)) {
						sp.setPosFrom(to, 1);
					}
				}
			}
		}
		
		//magic
		for (int stp = 2; stp < MAX_REL; stp++) {//link steps
			for (final Spot sp : nsp) {//take 1 spot
				for (final Spot to : nsp) {//look at others
					if (sp.noPosFrom(to)) {
						
						int min = MAX_REL;
						for (final Spot nr : nsp) {//look at near
							if (to.getPosFrom(nr) == 1) {
								if (sp.noPosFrom(nr) || min < sp.getPosFrom(nr)) continue;
								min = sp.getPosFrom(nr);
							}
						}
						
						if (min == MAX_REL) continue;
						sp.setPosFrom(to, min + 1);
						to.setPosFrom(sp, min + 1);
					}
				}
			}
		}
		
		//filling
		for (final Spot sp : nsp) {//take 1 spot
			for (final Spot os : nsp) {//link leftover spots
				if (sp.noPosFrom(os)) {
					sp.setPosFrom(os, MAX_REL);
					os.setPosFrom(sp, MAX_REL);
				}
			}
			/*final StringBuffer sb = new StringBuffer();
			for (int i : sp.relPos) {
				sb.append(":" + i);
			}
			Bukkit.broadcast(Component.text(sp.toString() + "-" + sb.substring(1)));*/
		}
		
		return nsp;
	}
}