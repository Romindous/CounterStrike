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
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.server.network.PlayerConnection;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.entity.player.PlayerAbilities;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.ScoreboardTeam;
import net.minecraft.world.scores.ScoreboardTeamBase.EnumNameTagVisibility;
import ru.komiss77.version.VM;

import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import com.mojang.datafixers.util.Pair;

public class PacketUtils {
	
	public static void sendNmTg(final Pair<Shooter, Arena> pr, final String prf, final String sfx, final EnumChatFormat clr) {
		final EntityPlayer ep = Main.ds.bg().a(pr.getFirst().nm);
		if (ep == null) {
			return;
		}
		final Scoreboard sb = ep.c.aE();
		final ScoreboardTeam st = sb.g(pr.getFirst().nm);
		st.b(IChatBaseComponent.a(prf));
		st.c(IChatBaseComponent.a(sfx));
		st.a(clr);
		final PacketPlayOutScoreboardTeam pt = PacketPlayOutScoreboardTeam.a(st);
		final PacketPlayOutScoreboardTeam crt = PacketPlayOutScoreboardTeam.a(st, true);
		final PacketPlayOutScoreboardTeam add = PacketPlayOutScoreboardTeam.a(st, pr.getFirst().nm, a.a);
		final PacketPlayOutScoreboardTeam modSm = PacketPlayOutScoreboardTeam.a(st, false);
		st.a(EnumNameTagVisibility.b);
		final PacketPlayOutScoreboardTeam modDf = PacketPlayOutScoreboardTeam.a(st, false);
		sb.d(st);
		if (pr.getSecond() == null) {
			for (final EntityHuman e : ep.t.z()) {
				((EntityPlayer) e).b.a(pt);
				((EntityPlayer) e).b.a(crt);
				((EntityPlayer) e).b.a(add);
				((EntityPlayer) e).b.a(modSm);
			}
		} else {
			switch (pr.getSecond().gst) {
			case BEGINING:
			case WAITING:
			case FINISH:
				for (final EntityHuman e : ep.t.z()) {
					((EntityPlayer) e).b.a(pt);
					((EntityPlayer) e).b.a(crt);
					((EntityPlayer) e).b.a(add);
					((EntityPlayer) e).b.a(modSm);
				}
				break;
			case BUYTIME:
			case ENDRND:
			case ROUND:
				final Team tm = pr.getSecond().shtrs.get(pr.getFirst());
				for (final Entry<Shooter, Team> e : pr.getSecond().shtrs.entrySet()) {
					final PlayerConnection pc = Main.ds.bg().a(e.getKey().nm).b;
					pc.a(pt);
					pc.a(crt);
					pc.a(add);
					if (e.getValue() == tm) {
						pc.a(modSm);
					} else {
						pc.a(modDf);
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
		final Scoreboard sb = tgt.c.aE();
		final ScoreboardTeam st = sb.g(tgt.displayName);
		st.b(IChatBaseComponent.a(prf));
		st.c(IChatBaseComponent.a(sfx));
		st.a(clr);
		final PacketPlayOutScoreboardTeam pt = PacketPlayOutScoreboardTeam.a(st);
		final PacketPlayOutScoreboardTeam crt = PacketPlayOutScoreboardTeam.a(st, true);
		final PacketPlayOutScoreboardTeam add = PacketPlayOutScoreboardTeam.a(st, tgt.displayName, a.a);
		final PacketPlayOutScoreboardTeam modSm = PacketPlayOutScoreboardTeam.a(st, false);
		st.a(EnumNameTagVisibility.b);
		final PacketPlayOutScoreboardTeam modDf = PacketPlayOutScoreboardTeam.a(st, false);
		sb.d(st);
		pc.a(pt);
		pc.a(crt);
		pc.a(add);
		if (rcvtm == tgtm) {
			pc.a(modSm);
		} else {
			pc.a(modDf);
		}
	}
  
	public static String v;
  
	public static void sendTtlSbTtl(final Player p, final String ttl, final String sbttl, final int tm) {
		final PlayerConnection pc = Main.ds.bg().a(p.getName()).b;
		pc.a(new ClientboundSetTitleTextPacket(IChatBaseComponent.a(ttl)));
		pc.a(new ClientboundSetSubtitleTextPacket(IChatBaseComponent.a(sbttl)));
		pc.a(new ClientboundSetTitlesAnimationPacket(2, tm, 20));
	}
  
	public static void sendTtl(final Player p, final String ttl, final int tm) {
		final PlayerConnection pc = Main.ds.bg().a(p.getName()).b;
		pc.a(new ClientboundSetTitleTextPacket(IChatBaseComponent.a(ttl)));
		pc.a(new ClientboundSetSubtitleTextPacket(IChatBaseComponent.a(" ")));
		pc.a(new ClientboundSetTitlesAnimationPacket(2, tm, 20));
	}
  
	public static void sendSbTtl(final Player p, final String sbttl, final int tm) {
		final PlayerConnection pc = Main.ds.bg().a(p.getName()).b;
		pc.a(new ClientboundSetTitleTextPacket(IChatBaseComponent.a(" ")));
		pc.a(new ClientboundSetSubtitleTextPacket(IChatBaseComponent.a(sbttl)));
		pc.a(new ClientboundSetTitlesAnimationPacket(2, tm, 20));
	}
  
	public static void sendAcBr(final Player p, final String msg, final int tm) {
		final PlayerConnection pc = Main.ds.bg().a(p.getName()).b;
		pc.a(new ClientboundSetActionBarTextPacket(IChatBaseComponent.a(msg)));
		pc.a(new ClientboundSetTitlesAnimationPacket(2, tm, 20));
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
			Main.ds.bg().a(p.getName()).b.a(pab);
			fkHlmtClnt(p, in ? Main.cp : p.getInventory().getHelmet());
		}
		catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}
    
    public static WorldServer getNMSWrld(final String nm) {
  		for (final WorldServer w : Main.ds.R.values()) {
  			if (w.N.e.a.equals(nm)) {
  				return w;
  			}
  		}
  		return null;
  	}

	
	public static net.minecraft.world.item.ItemStack getNMSIt(final org.bukkit.inventory.ItemStack it) {
		try {
			return (net.minecraft.world.item.ItemStack) getCrftClss("inventory.CraftItemStack").getMethod("asNMSCopy", it.getClass()).invoke(null, it);
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
 		final ItemStack ii;
 		if (it == null) {
 			ii = new ItemStack(Material.AIR);
 		} else {
			if (it.getType() == Material.LEATHER_HELMET) {
				final Color clr = ((LeatherArmorMeta) it.getItemMeta()).getColor();
				if (clr == Color.RED) {
					ii = Main.thlmt;
				} else if (clr == Color.TEAL) {
					ii = Main.cthlmt;
				} else {
					ii = new ItemStack(Material.LEATHER_HELMET);
				}
			} else {
	 			ii = it;
			}
		}
 		VM.getNmsServer().sendFakeEquip(p, 5, ii);
 	}
  
 	public static void blkCrckClnt(final EntityPlayer ep, final SmplLoc bl) {
 		final PacketPlayOutBlockBreakAnimation pb = new PacketPlayOutBlockBreakAnimation(Main.srnd.nextInt(1000) + 10000, new BlockPosition(bl.x, bl.y, bl.z), getNxtStg(bl));
 		for (final EntityHuman e : Main.ds.bg().j) {
 			((EntityPlayer) e).b.a(pb);
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