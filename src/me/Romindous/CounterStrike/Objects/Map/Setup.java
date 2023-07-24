package me.Romindous.CounterStrike.Objects.Map;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import me.Romindous.CounterStrike.Main;
import me.Romindous.CounterStrike.Objects.Game.GameType;
import me.Romindous.CounterStrike.Objects.Game.TripWire;
import ru.komiss77.modules.world.XYZ;

public class Setup {
	
	public final String nm;
	public final byte min;
	public final byte max;
	public final boolean rndM;
	public final XYZ[] tSpawns;
	public final XYZ[] ctSpawns;
	public final XYZ[] spots;
	public final XYZ A;
	public final XYZ B;
//	public final XYZ bot;
//	public final XYZ top;
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
			tSpawns = null;
			ctSpawns = null;
			spots = null;
			A = null;
			B = null;
//			bot = null;
//			top = null;
		} else {
			bots = ar.getBoolean("bots");
			
			final List<XYZ> tSps = ar.getStringList("tspawns").stream().map(XYZ::fromString).toList();
			tSpawns = tSps.toArray(new XYZ[tSps.size()]);
			
			final List<XYZ> ctSps = ar.getStringList("ctspawns").stream().map(XYZ::fromString).toList();
			ctSpawns = ctSps.toArray(new XYZ[ctSps.size()]);
			
			final List<XYZ> tPss = ar.getStringList("spots").stream().map(XYZ::fromString).toList();
			spots = tPss.toArray(new XYZ[tPss.size()]);
			
			A = XYZ.fromString(ar.getString("asite"));
			B = XYZ.fromString(ar.getString("bsite"));
			
//			bot = XYZ.fromString(ar.getString("bot"));
//			top = XYZ.fromString(ar.getString("top"));
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
		
		if (A != null) ar.set("asite", A.toString());
		if (B != null) ar.set("bsite", B.toString());
		
//		if (bot != null) ar.set("bot", bot.toString());
//		if (top != null) ar.set("top", top.toString());
		
		if (tSpawns != null) ar.set("tspawns", 
			Arrays.asList(tSpawns).stream().map(XYZ::toString).toList());
		
		if (ctSpawns != null) ar.set("ctspawns", 
			Arrays.asList(ctSpawns).stream().map(XYZ::toString).toList());
		
		if (spots != null) ar.set("spots", 
			Arrays.asList(spots).stream().map(XYZ::toString).toList());
		
		try {
			conf.save(new File(Main.plug.getDataFolder() + File.separator + "arenas.yml"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public Setup(final String name) {
		nm = name; min = 2; max = 2; rndM = false;
		tSpawns = null; ctSpawns = null; spots = null; A = null; B = null; 
		fin = false; bots = false; worlds = Collections.unmodifiableMap(
			new EnumMap<>(GameType.class));
	}
	
	public Setup(final String name, final byte min, final byte max, 
		final boolean rndM, final boolean fin, final boolean bots,
		final XYZ[] tSpawns, final XYZ[] ctSpawns, final XYZ[] spots, 
		final XYZ A, final XYZ B, final Map<GameType, String> worlds) {
		this.nm = name;
		this.min = min;
		this.max = max;
		this.rndM = rndM;
		this.tSpawns = tSpawns;
		this.ctSpawns = ctSpawns;
		this.spots = spots;
		this.A = A;
		this.B = B;
//		this.bot = bot;
//		this.top = top;
		this.fin = fin;
		this.bots = bots;
		this.worlds = worlds;
	}
	
	public boolean isReady() {
		if (nm == null || min < 1 || max < min) return false;
		return rndM || (A != null && B != null && 
		(ctSpawns != null && ctSpawns.length > 1) && 
		(tSpawns != null && tSpawns.length > 1));
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

	/*
	public Spot[] linkSpots(final World w) {
		if (!bots) return spots;
		final int ln = spots.length;
		final Spot[] nsp = new Spot[ln];
		for (int i = 0; i < ln; i++) {
			nsp[i] = new Spot(spots[i], i, ln);
		}
		
//		for (final Spot sp : nsp) {//take 1 spot
//			final HashSet<Spot> done = new HashSet<>();
//			done.add(sp);//itself
//			sp.setPosFrom(sp, 0);
//			//Bukkit.broadcast(Component.text("start"));
//			for (int stp = 1; stp < MAX_REL && done.size() < ln; stp++) {//link steps
//				final HashSet<Spot> add = new HashSet<>();
//				for (final Spot fr : done) {//link spots per step
//					for (final Spot to : nsp) {
//						if (!done.contains(to) && !add.contains(to)) {
//							if (sp.getPosFrom(to) == 0) {
//								if (Main.rayThruSoft(new Location(w, fr.u() + 0.5d, fr.v() + 0.5d, fr.w() + 0.5d), 
//									new Vector(to.u() + 0.5d, to.v() + 0.5d, to.w() + 0.5d), 0.6d)) {
//									//Bukkit.broadcast(Component.text("dst-"+(sp.getPosFrom(fr) + 1)+",sp-"+sp.toString()+",to-"+to.toString()));
//									sp.setPosFrom(to, sp.getPosFrom(fr) + 1);//() -> * -> * -> ()
//									to.setPosFrom(sp, sp.getPosFrom(fr) + 1);//() <- * <- * <- ()
//									if (stp != 1) {
//										fr.setPosFrom(to, 1);//* -> * -> () -> ()
//										to.setPosFrom(fr, 1);//* <- * <- () <- ()
//									}
//									add.add(to);
//								}
//							} else {
//								add.add(to);
//							}
//						}
//					}
//				}
//				done.addAll(add);
//			}
//		}
		
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
//			final StringBuffer sb = new StringBuffer();
//			for (int i : sp.relPos) {
//				sb.append(":" + i);
//			}
//			Bukkit.broadcast(Component.text(sp.toString() + "-" + sb.substring(1)));
		}
		
		return nsp;
	}*/
}