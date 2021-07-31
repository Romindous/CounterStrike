package me.Romindous.CounterStrike.Utils;

import me.Romindous.CounterStrike.Main;
import me.Romindous.CounterStrike.Game.Arena;
import me.Romindous.CounterStrike.Game.Arena.Team;
import me.Romindous.CounterStrike.Objects.Shooter;
import me.Romindous.CounterStrike.Objects.SmplLoc;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ConcurrentModificationException;
import java.util.Map.Entry;

import net.minecraft.EnumChatFormat;
import net.minecraft.core.BlockPosition;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.network.protocol.game.PacketPlayOutAbilities;
import net.minecraft.network.protocol.game.PacketPlayOutBlockBreakAnimation;
import net.minecraft.network.protocol.game.PacketPlayOutScoreboardTeam;
import net.minecraft.network.protocol.game.PacketPlayOutScoreboardTeam.a;
import net.minecraft.network.protocol.game.PacketPlayOutSetSlot;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.network.PlayerConnection;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.entity.player.PlayerAbilities;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.World;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.ScoreboardTeam;
import net.minecraft.world.scores.ScoreboardTeamBase.EnumNameTagVisibility;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import com.mojang.datafixers.util.Pair;

public class PacketUtils {
	
	public static void sendNmTg(final Pair<Shooter, Arena> pr, final String prf, final String sfx, final EnumChatFormat clr) {
		final EntityPlayer ep = getNMSPlr((Player) pr.getFirst().inv.getHolder());
		final Scoreboard sb = ep.getMinecraftServer().getScoreboard();
		final ScoreboardTeam st = sb.createTeam(ep.getName());
		st.setPrefix(IChatBaseComponent.a(prf));
		st.setSuffix(IChatBaseComponent.a(sfx));
		st.setColor(clr);
		final PacketPlayOutScoreboardTeam pt = PacketPlayOutScoreboardTeam.a(st);
		final PacketPlayOutScoreboardTeam crt = PacketPlayOutScoreboardTeam.a(st, true);
		final PacketPlayOutScoreboardTeam add = PacketPlayOutScoreboardTeam.a(st, ep.getName(), a.a);
		final PacketPlayOutScoreboardTeam modSm = PacketPlayOutScoreboardTeam.a(st, false);
		st.setNameTagVisibility(EnumNameTagVisibility.b);
		final PacketPlayOutScoreboardTeam modDf = PacketPlayOutScoreboardTeam.a(st, false);
		sb.removeTeam(st);
		if (pr.getSecond() == null) {
			for (final EntityHuman e : ep.getWorld().getPlayers()) {
				((EntityPlayer) e).b.sendPacket(pt);
				((EntityPlayer) e).b.sendPacket(crt);
				((EntityPlayer) e).b.sendPacket(add);
				((EntityPlayer) e).b.sendPacket(modSm);
			}
		} else {
			switch (pr.getSecond().gst) {
			case BEGINING:
			case WAITING:
			case FINISH:
				for (final EntityHuman e : ep.getWorld().getPlayers()) {
					((EntityPlayer) e).b.sendPacket(pt);
					((EntityPlayer) e).b.sendPacket(crt);
					((EntityPlayer) e).b.sendPacket(add);
					((EntityPlayer) e).b.sendPacket(modSm);
				}
				break;
			case BUYTIME:
			case ENDRND:
			case ROUND:
				final Team tm = pr.getSecond().shtrs.get(pr.getFirst());
				for (final Entry<Shooter, Team> e : pr.getSecond().shtrs.entrySet()) {
					final PlayerConnection pc = getNMSPlr((Player) e.getKey().inv.getHolder()).b;
					pc.sendPacket(pt);
					pc.sendPacket(crt);
					pc.sendPacket(add);
					if (e.getValue() == tm) {
						pc.sendPacket(modSm);
					} else {
						pc.sendPacket(modDf);
					}
				}
				break;
			}
		}
		//удаляет тиму final PacketPlayOutScoreboardTeam pt = PacketPlayOutScoreboardTeam.a(st);
		//создает тиму final PacketPlayOutScoreboardTeam pt = PacketPlayOutScoreboardTeam.a(st, true);
		//модифицирует final PacketPlayOutScoreboardTeam pt = PacketPlayOutScoreboardTeam.a(st, false);
		//добавляет игрока final PacketPlayOutScoreboardTeam pt = PacketPlayOutScoreboardTeam.a(st, p.getName(), a.a);
		//учирает игрока final PacketPlayOutScoreboardTeam pt = PacketPlayOutScoreboardTeam.a(st, p.getName(), a.b);
	}
	
	public static void sendRvsNmTg(final PlayerConnection pc, final Team rcvtm, final EntityPlayer tgt, final Team tgtm, final String prf, final String sfx, final EnumChatFormat clr) {
		final Scoreboard sb = tgt.getMinecraftServer().getScoreboard();
		final ScoreboardTeam st = sb.createTeam(tgt.getName());
		st.setPrefix(IChatBaseComponent.a(prf));
		st.setSuffix(IChatBaseComponent.a(sfx));
		st.setColor(clr);
		final PacketPlayOutScoreboardTeam pt = PacketPlayOutScoreboardTeam.a(st);
		final PacketPlayOutScoreboardTeam crt = PacketPlayOutScoreboardTeam.a(st, true);
		final PacketPlayOutScoreboardTeam add = PacketPlayOutScoreboardTeam.a(st, tgt.getName(), a.a);
		final PacketPlayOutScoreboardTeam modSm = PacketPlayOutScoreboardTeam.a(st, false);
		st.setNameTagVisibility(EnumNameTagVisibility.b);
		final PacketPlayOutScoreboardTeam modDf = PacketPlayOutScoreboardTeam.a(st, false);
		sb.removeTeam(st);
		pc.sendPacket(pt);
		pc.sendPacket(crt);
		pc.sendPacket(add);
		if (rcvtm == tgtm) {
			pc.sendPacket(modSm);
		} else {
			pc.sendPacket(modDf);
		}
	}
  
	public static String v;
  
	public static void sendTtlSbTtl(final Player p, final String ttl, final String sbttl, final int tm) {
		final PlayerConnection pc = getNMSPlr(p).b;
		pc.sendPacket(new ClientboundSetTitleTextPacket(IChatBaseComponent.a(ttl)));
		pc.sendPacket(new ClientboundSetSubtitleTextPacket(IChatBaseComponent.a(sbttl)));
		pc.sendPacket(new ClientboundSetTitlesAnimationPacket(2, tm, 20));
	}
  
	public static void sendTtl(final Player p, final String ttl, final int tm) {
		final PlayerConnection pc = getNMSPlr(p).b;
		pc.sendPacket(new ClientboundSetTitleTextPacket(IChatBaseComponent.a(ttl)));
		pc.sendPacket(new ClientboundSetSubtitleTextPacket(IChatBaseComponent.a(" ")));
		pc.sendPacket(new ClientboundSetTitlesAnimationPacket(2, tm, 20));
	}
  
	public static void sendSbTtl(final Player p, final String sbttl, final int tm) {
		final PlayerConnection pc = getNMSPlr(p).b;
		pc.sendPacket(new ClientboundSetTitleTextPacket(IChatBaseComponent.a(" ")));
		pc.sendPacket(new ClientboundSetSubtitleTextPacket(IChatBaseComponent.a(sbttl)));
		pc.sendPacket(new ClientboundSetTitlesAnimationPacket(2, tm, 20));
	}
  
	public static void sendAcBr(final Player p, final String msg, final int tm) {
		final PlayerConnection pc = getNMSPlr(p).b;
		pc.sendPacket(new ClientboundSetActionBarTextPacket(IChatBaseComponent.a(msg)));
		pc.sendPacket(new ClientboundSetTitlesAnimationPacket(2, tm, 20));
	}
  
	public static void zoom(final Player p, final boolean in) {
		try {
			final PlayerAbilities pa = new PlayerAbilities();
			pa.a = p.getGameMode() == GameMode.CREATIVE;
			pa.b = p.isFlying();
			pa.c = p.getGameMode() == GameMode.CREATIVE;
			pa.d = p.getGameMode() == GameMode.CREATIVE;
			pa.e = true;
			pa.f = 0.05f;
			final PacketPlayOutAbilities pab = new PacketPlayOutAbilities(pa);
			final Field fov = pab.getClass().getDeclaredFields()[9];
			fov.setAccessible(true);
			fov.setFloat(pab, in ? 100f : 0.1f);
			getNMSPlr(p).b.sendPacket(pab);
			fkHlmtClnt(p, in ? Main.cp : p.getInventory().getHelmet());
		}
		catch (IllegalAccessException e) {
			e.printStackTrace();
		} 
	}
  
	public static EntityPlayer getNMSPlr(final Player p) {
		try {
			return (EntityPlayer)p.getClass().getMethod("getHandle").invoke(p);
		} catch (IllegalAccessException|IllegalArgumentException|InvocationTargetException|NoSuchMethodException|SecurityException e) {
			e.printStackTrace();
			
			return null;
		} 
	}
	public static World getNMSWrld(final org.bukkit.World w) {
	    try {
	    	return (World) w.getClass().getMethod("getHandle").invoke(w);
	    } catch (IllegalAccessException|InvocationTargetException|NoSuchMethodException e) {
	    	e.printStackTrace();
	    	return null;
	    } 
	}
	
	public static ItemStack getNMSIt(final org.bukkit.inventory.ItemStack it) {
		try {
			return (ItemStack) getCrftClss("inventory.CraftItemStack").getMethod("asNMSCopy", it.getClass()).invoke(null, it);
		} catch (IllegalAccessException|InvocationTargetException|NoSuchMethodException e) {
			e.printStackTrace();
			return null;
		} 
	}
	
 	public static Class<?> getCrftClss(final String cls) {
 		try {
 			return Class.forName("org.bukkit.craftbukkit." + v + "." + cls);
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 			return null;
 		} 
 	}
  
 	public static void fkHlmtClnt(final Player p, final org.bukkit.inventory.ItemStack it) {
 		final PacketPlayOutSetSlot ps = new PacketPlayOutSetSlot(-2, 10, 39, getNMSIt(it == null ? new org.bukkit.inventory.ItemStack(Material.AIR) : it));
 		getNMSPlr(p).b.sendPacket(ps);
 	}
  
 	public static void blkCrckClnt(final EntityPlayer ep, final SmplLoc bl) {
 		final PacketPlayOutBlockBreakAnimation pb = new PacketPlayOutBlockBreakAnimation(Main.srnd.nextInt(1000) + 10000, new BlockPosition(bl), getNxtStg(bl));
 		for (final EntityHuman e : ep.getWorld().getPlayers()) {
 			((EntityPlayer) e).b.sendPacket(pb);
 		}
 	}
  
 	public static int getNxtStg(final SmplLoc bl) {
 		try {
 	 		for (final SmplLoc s : Main.crckd) {
 	 			if (s.equals(bl)) {
 	 				s.cnt = (short) (640 ^ ((s.cnt & 0x7) == 7 ? 7 : (s.cnt + 1 & 0x7)));
 	 				return s.cnt & 0x7;
 	 			} 
 	 		} 
 		} catch (ConcurrentModificationException e) {
 	 		return 3;
 		}
 		Main.crckd.add(bl);
 		return 0;
 	}
}