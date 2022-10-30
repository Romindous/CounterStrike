package me.Romindous.CounterStrike.Objects;

import org.bukkit.configuration.ConfigurationSection;

import me.Romindous.CounterStrike.Enums.GameType;
import net.minecraft.core.BaseBlockPosition;

public class Setup {
	
	public final String nm;
	public final String wnm;
	public final byte min;
	public final byte max;
	public final boolean rndM;
	public final BaseBlockPosition A;
	public final BaseBlockPosition B;
	public final BaseBlockPosition[] ctSpawns;
	public final BaseBlockPosition[] tSpawns;
	public final boolean fin;
	public final GameType gt;
	
	public Setup(final ConfigurationSection ar) {
		nm = TripWire.loadMapSound(ar.getName());
		wnm = ar.getString("world");
		min = (byte) ar.getInt("min");
		max = (byte) ar.getInt("max");
		if (rndM = ar.getBoolean("rnd")) {
			ctSpawns = null;
			tSpawns = null;
			A = null;
			B = null;
		} else {
			final String[] tx = ar.getString("tspawns.x").split(":");
			final String[] ty = ar.getString("tspawns.y").split(":");
			final String[] tz = ar.getString("tspawns.z").split(":");
			tSpawns = new BaseBlockPosition[tx.length];
			for (byte i = (byte) (tx.length - 1); i >= 0; i--) {
				tSpawns[i] = new BaseBlockPosition(Integer.parseInt(tx[i]), Integer.parseInt(ty[i]), Integer.parseInt(tz[i]));
			}
			final String[] ctx = ar.getString("ctspawns.x").split(":");
			final String[] cty = ar.getString("ctspawns.y").split(":");
			final String[] ctz = ar.getString("ctspawns.z").split(":");
			ctSpawns = new BaseBlockPosition[ctx.length];
			for (byte i = (byte) (ctx.length - 1); i >= 0; i--) {
				ctSpawns[i] = new BaseBlockPosition(Integer.parseInt(ctx[i]), Integer.parseInt(cty[i]), Integer.parseInt(ctz[i]));
			}
			A = new BaseBlockPosition(ar.getInt("asite.x"), ar.getInt("asite.y"), ar.getInt("asite.z"));
			B = new BaseBlockPosition(ar.getInt("bsite.x"), ar.getInt("bsite.y"), ar.getInt("bsite.z"));
		}
		fin = ar.contains("fin");
		switch (ar.getString("type")) {
		case "gungame":
			gt = GameType.GUNGAME;
			break;
		case "invasion":
			gt = GameType.INVASION;
			break;
		case "defusal":
		default:
			gt = GameType.DEFUSAL;
			break;
		}
	}
}
