package me.Romindous.CounterStrike.Objects.Map;

import java.io.File;
import java.io.IOException;
import java.util.*;
import me.Romindous.CounterStrike.Enums.GameType;
import me.Romindous.CounterStrike.Main;
import me.Romindous.CounterStrike.Objects.Game.TripWire;
import net.kyori.adventure.key.Key;
import org.bukkit.block.BlockType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import ru.komiss77.boot.OStrap;
import ru.komiss77.modules.world.BVec;

public class Setup {
	
	public final String nm;
	public final byte min;
	public final byte max;
	public final boolean rnd;
	public final BVec[] tSpawns;
	public final BVec[] ctSpawns;
	public final BVec[] spots;
	public final BVec A;
	public final BVec B;
	public final BVec dims;
	public final BlockType ceil;
//	public final XYZ bot;
//	public final XYZ top;
	public final Map<GameType, String> worlds;
	public final boolean bots;
	public final boolean fin;

	public static final String sep = "_";
	
	public Setup(final ConfigurationSection ar) {
		nm = TripWire.loadMapSound(ar.getName());
		min = (byte) ar.getInt("min");
		max = (byte) ar.getInt("max");
		final EnumMap<GameType, String> ws = new EnumMap<>(GameType.class);
		for (final GameType gt : GameType.values()) {
			ws.put(gt, ar.getString("world." + gt.toString()));
		}
		worlds = Collections.unmodifiableMap(ws);
		rnd = ar.getBoolean("rnd");
		if (rnd) {
			bots = ar.getBoolean("bots");
			tSpawns = null;
			ctSpawns = null;
			spots = null;
			A = null;
			B = null;
			final BVec dm = BVec.parse(ar.getString("dims"));
			dims = dm == null ? MapBuilder.dftMapDims : dm;
			final String cl = ar.getString("ceil");
			ceil = cl == null ? BlockType.STONE : OStrap.get(Key.key(cl.toLowerCase()), BlockType.STONE);
//			bot = null;
//			top = null;
		} else {
			dims = null;
			ceil = null;
			bots = ar.getBoolean("bots");
			
			final List<BVec> tSps = ar.getStringList("tspawns").stream().map(BVec::parse).toList();
			tSpawns = tSps.toArray(new BVec[0]);
			
			final List<BVec> ctSps = ar.getStringList("ctspawns").stream().map(BVec::parse).toList();
			ctSpawns = ctSps.toArray(new BVec[0]);
			
			final List<BVec> tPss = ar.getStringList("spots").stream().map(BVec::parse).toList();
			spots = tPss.toArray(new BVec[0]);
			
			A = BVec.parse(ar.getString("asite"));
			B = BVec.parse(ar.getString("bsite"));
			
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
		ar.set("fin", true);
		ar.set("rnd", rnd);
		ar.set("bots", bots);
		ar.set("dims", dims == null ? null : dims.toString());
		ar.set("ceil", ceil == null ? null : ceil.key().asMinimalString());
		for (final GameType gt : GameType.values()) {
			final String w = worlds.get(gt);
			if (w != null) ar.set("world." + gt.toString(), w);
		}
		
		if (A != null) ar.set("asite", A.toString());
		if (B != null) ar.set("bsite", B.toString());
		
//		if (bot != null) ar.set("bot", bot.toString());
//		if (top != null) ar.set("top", top.toString());
		
		if (tSpawns != null) ar.set("tspawns", 
			Arrays.stream(tSpawns).map(BVec::toString).toList());
		
		if (ctSpawns != null) ar.set("ctspawns", 
			Arrays.stream(ctSpawns).map(BVec::toString).toList());
		
		if (spots != null) ar.set("spots", 
			Arrays.stream(spots).map(BVec::toString).toList());
		
		try {
			conf.save(new File(Main.plug.getDataFolder() + File.separator + "arenas.yml"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public Setup(final String name) {
		nm = name; min = 2; max = 2; rnd = false; dims = MapBuilder.dftMapDims;
		ceil = BlockType.STONE; tSpawns = null; ctSpawns = null; spots = null;
		worlds = Collections.unmodifiableMap(new EnumMap<>(GameType.class));
		A = null; B = null; fin = false; bots = false;
	}
	
	public Setup(final String name, final byte min, final byte max, 
		final boolean rndM, final boolean fin, final boolean bots, final BVec dims,
		final BlockType ceil, final BVec[] tSpawns, final BVec[] ctSpawns, final BVec[] spots,
		final BVec A, final BVec B, final Map<GameType, String> worlds) {
		this.nm = name;
		this.min = min;
		this.max = max;
		this.rnd = rndM;
		this.dims = dims;
		this.ceil = ceil;
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
		return rnd || (A != null && B != null &&
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
}