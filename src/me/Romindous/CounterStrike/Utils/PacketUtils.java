package me.Romindous.CounterStrike.Utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ConcurrentModificationException;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import me.Romindous.CounterStrike.Main;
import me.Romindous.CounterStrike.Game.Arena.Team;
import me.Romindous.CounterStrike.Objects.Shooter;
import me.Romindous.CounterStrike.Objects.Game.PlShooter;
import net.minecraft.EnumChatFormat;
import net.minecraft.commands.arguments.ArgumentAnchor.Anchor;
import net.minecraft.core.BlockPosition;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.network.protocol.game.PacketPlayOutAbilities;
import net.minecraft.network.protocol.game.PacketPlayOutBlockBreakAnimation;
import net.minecraft.network.protocol.game.PacketPlayOutLookAt;
import net.minecraft.network.protocol.game.PacketPlayOutScoreboardTeam;
import net.minecraft.network.protocol.game.PacketPlayOutScoreboardTeam.a;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.server.network.PlayerConnection;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.entity.player.PlayerAbilities;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.ScoreboardTeam;
import net.minecraft.world.scores.ScoreboardTeamBase.EnumNameTagVisibility;
import ru.komiss77.modules.world.WXYZ;
import ru.komiss77.version.VM;

public class PacketUtils {
	
	private static final Method getWrld = mkGet(".CraftWorld");
	private static final Method getPl = mkGet(".entity.CraftPlayer");
	private static final Method getLE = mkGet(".entity.CraftLivingEntity");
	private static final Field fov = mkFld(PacketPlayOutAbilities.class.getDeclaredFields()[9]);
	private static Field mkFld(final Field fld) {
		fld.setAccessible(true);
		return fld;
	}
	private static Method mkGet(final String pth) {
		try {
			return Class.forName(Bukkit.getServer().getClass().getPackageName() + pth).getDeclaredMethod("getHandle");
		} catch (NoSuchMethodException | SecurityException | ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private static int id = 0;
	
	public static void sendNmTg(final Shooter of, final String prf, final String sfx, final EnumChatFormat clr) {
		final EntityLiving ep = getNMSLE(of.getEntity());
		if (ep == null) return;
		final Scoreboard sb = ep.cI().aF();
		final ScoreboardTeam st = sb.g(of.name());
		st.b(IChatBaseComponent.a(prf));
		st.c(IChatBaseComponent.a(sfx));
		st.a(clr);
		final PacketPlayOutScoreboardTeam pt = PacketPlayOutScoreboardTeam.a(st);
		final PacketPlayOutScoreboardTeam crt = PacketPlayOutScoreboardTeam.a(st, true);
		final PacketPlayOutScoreboardTeam add = PacketPlayOutScoreboardTeam.a(st, of.name(), a.a);
		final PacketPlayOutScoreboardTeam modSm = PacketPlayOutScoreboardTeam.a(st, false);
		st.a(EnumNameTagVisibility.b);
		final PacketPlayOutScoreboardTeam modDf = PacketPlayOutScoreboardTeam.a(st, false);
		sb.d(st);
		if (of.arena() == null) {
			for (final EntityHuman e : ep.dI().v()) {
				final NetworkManager nm = ((EntityPlayer) e).c.h;
				nm.a(pt); nm.a(crt); nm.a(add); nm.a(modSm);
			}
		} else {
			switch (of.arena().gst) {
			case WAITING:
				for (final EntityHuman e : ep.dI().v()) {
					final NetworkManager nm = ((EntityPlayer) e).c.h;
					nm.a(pt); nm.a(crt); nm.a(add); nm.a(modSm);
				}
				break;
			case BEGINING, FINISH:
				for (final Shooter sh : of.arena().shtrs.keySet()) {
					if (sh instanceof PlShooter) {
						final NetworkManager nm = getNMSPl(sh.getPlayer()).c.h;
						nm.a(pt); nm.a(crt); nm.a(add); nm.a(modSm);
					}
				}
				break;
			case BUYTIME, ENDRND, ROUND:
				final Team tm = of.arena().shtrs.get(of);
				for (final Entry<Shooter, Team> e : of.arena().shtrs.entrySet()) {
					if (e.getKey() instanceof PlShooter) {
						final NetworkManager nm = getNMSPl(e.getKey().getPlayer()).c.h;
						nm.a(pt); nm.a(crt); nm.a(add); nm.a(e.getValue() == tm ? modSm : modDf);
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
	
	public static void sendNmTg(final NetworkManager to, final Shooter of, final String prf, 
			final String sfx, final boolean show, final EnumChatFormat clr) {
		final Scoreboard sb = VM.getNmsServer().toNMS().aF();
		final ScoreboardTeam st = sb.g(of.name());
		st.b(IChatBaseComponent.a(prf));
		st.c(IChatBaseComponent.a(sfx));
		st.a(clr);
		final PacketPlayOutScoreboardTeam pt = PacketPlayOutScoreboardTeam.a(st);
		final PacketPlayOutScoreboardTeam crt = PacketPlayOutScoreboardTeam.a(st, true);
		final PacketPlayOutScoreboardTeam add = PacketPlayOutScoreboardTeam.a(st, of.name(), a.a);
		final PacketPlayOutScoreboardTeam modSm = PacketPlayOutScoreboardTeam.a(st, false);
		st.a(EnumNameTagVisibility.b);
		final PacketPlayOutScoreboardTeam modDf = PacketPlayOutScoreboardTeam.a(st, false);
		sb.d(st); to.a(pt); to.a(crt); to.a(add); to.a(show ? modSm : modDf);
	}
  
	public static void sendTtlSbTtl(final Player p, final String ttl, final String sbttl, final int tm) {
		final PlayerConnection pc = getNMSPl(p).c;
		pc.a(new ClientboundSetTitleTextPacket(IChatBaseComponent.a(ttl)));
		pc.a(new ClientboundSetSubtitleTextPacket(IChatBaseComponent.a(sbttl)));
		pc.a(new ClientboundSetTitlesAnimationPacket(2, tm, 20));
	}
  
	public static void sendTtl(final Player p, final String ttl, final int tm) {
		final PlayerConnection pc = getNMSPl(p).c;
		pc.a(new ClientboundSetTitleTextPacket(IChatBaseComponent.a(ttl)));
		pc.a(new ClientboundSetSubtitleTextPacket(IChatBaseComponent.a(" ")));
		pc.a(new ClientboundSetTitlesAnimationPacket(2, tm, 20));
	}
  
	public static void sendSbTtl(final Player p, final String sbttl, final int tm) {
		final PlayerConnection pc = getNMSPl(p).c;
		pc.a(new ClientboundSetTitleTextPacket(IChatBaseComponent.a(" ")));
		pc.a(new ClientboundSetSubtitleTextPacket(IChatBaseComponent.a(sbttl)));
		pc.a(new ClientboundSetTitlesAnimationPacket(2, tm, 20));
	}
  
	public static void sendAcBr(final Player p, final String msg, final int tm) {
		final PlayerConnection pc = getNMSPl(p).c;
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
			//final PotionEffect slw = p.getPotionEffect(PotionEffectType.SLOW);
			if (in) {
				fov.setFloat(pab, 256f);
				getNMSPl(p).c.a(pab);
				fkHlmtClnt(p, Main.cp);
			} else {
				fov.setFloat(pab, 0.1f);
				getNMSPl(p).c.a(pab);
				fkHlmtClnt(p, p.getInventory().getHelmet());
			}
		}
		catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	
	public static net.minecraft.world.item.ItemStack getNMSIt(final ItemStack it) {
		return net.minecraft.world.item.ItemStack.fromBukkitCopy(it);
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
    
    public static WorldServer getNMSWrld(final World w) {
		try {
			return (WorldServer) getWrld.invoke(w);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			return null;
		}
  	}
    
    public static EntityLiving getNMSLE(final LivingEntity le) {
		try {
			return (EntityLiving) getLE.invoke(le);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			return null;
		}
  	}
    
    public static EntityPlayer getNMSPl(final Player p) {
		try {
			return (EntityPlayer) getPl.invoke(p);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			return null;
		}
  	}
  
 	public static void blkCrckClnt(final WXYZ bl) {
 		final PacketPlayOutBlockBreakAnimation pb = new PacketPlayOutBlockBreakAnimation((id == 1000 ? (id = 0) : id++) + 10000, new BlockPosition(bl.x, bl.y, bl.z), getNxtStg(bl));
 		for (final EntityHuman e : getNMSWrld(bl.w).v()) {
 			((EntityPlayer) e).c.a(pb);
 		}
 	}
  
 	public static int getNxtStg(final WXYZ bl) {
 		try {
 	 		for (final WXYZ s : Main.ckracks) {
 	 			if (s.equals(bl)) {
 	 				return (s.pitch = 640 ^ ((s.pitch & 0x7) == 7 ? 7 : (s.pitch + 1 & 0x7))) & 0x7;
 	 			} 
 	 		} 
 		} catch (ConcurrentModificationException e) {
 	 		return 3;
 		}
 		Main.ckracks.add(bl);
 		return 0;
 	}
 	
	public static void sendRecoil(final PlShooter sh, final Location rot) {
		final Location lc = rot.add(rot.getDirection().multiply(40d));
		getNMSPl(sh.getPlayer()).c.a(new PacketPlayOutLookAt(Anchor.b, lc.getX(), lc.getY(), lc.getZ()));
	}
}