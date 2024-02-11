package me.Romindous.CounterStrike.Utils;

import me.Romindous.CounterStrike.Main;
import net.minecraft.core.BlockPosition;
import net.minecraft.network.protocol.game.PacketPlayOutBlockBreakAnimation;
import org.bukkit.entity.Player;
import ru.komiss77.ApiOstrov;
import ru.komiss77.modules.world.WXYZ;
import ru.komiss77.version.Nms;

import java.util.ConcurrentModificationException;

public class PacketUtils {
	
	private static int id = 0;
	
	/*public static void sendNmTg(final Shooter of, final String prf, final String sfx, final EnumChatFormat clr) {
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
						nm.a(pt); nm.a(crt); nm.a(add); nm.a(e.getValue() == Team.SPEC || e.getValue() == tm ? modSm : modDf);
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
	}*/
	
	/*public static void sendNmTg(final Player to, final Shooter of, final String prf,
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
		final PlayerConnection pc = PacketUtils.getNMSPl(to).c;
		sb.d(st); pc.a(pt); pc.a(crt); pc.a(add); pc.a(show ? modSm : modDf);
	}*/
  
	public static void sendTtlSbTtl(final Player p, final String ttl, final String sbttl, final int tm) {
		ApiOstrov.sendTitleDirect(p, ttl, sbttl, 4, tm, 20);
	}
  
	public static void sendTtl(final Player p, final String ttl, final int tm) {
		ApiOstrov.sendTitleDirect(p, ttl, "", 4, tm, 20);
	}
  
	public static void sendSbTtl(final Player p, final String sbttl, final int tm) {
		ApiOstrov.sendTitleDirect(p, "", sbttl, 4, tm, 20);
	}
  
	public static void sendAcBr(final Player p, final String msg) {
		ApiOstrov.sendActionBarDirect(p, msg);
	}
  
 	/*public static void fkHlmtClnt(final Player p, final org.bukkit.inventory.ItemStack it) {
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
 		VM.server().sendFakeEquip(p, 5, ii);
 	}*/
  
 	public static void blkCrckClnt(final WXYZ bl) {
 		Nms.sendWorldPackets(bl.w, new PacketPlayOutBlockBreakAnimation(
				(id == 1000 ? (id = 0) : id++) + 10000,
			new BlockPosition(bl.x, bl.y, bl.z), getNxtStg(bl)));
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
 	
	/*public static void sendRecoil(final PlShooter sh, final Location rot) {
		final Location lc = rot.add(rot.getDirection().multiply(40d));
		Nms.sendPacket(sh.getPlayer(), new PacketPlayOutLookAt(Anchor.b, lc.getX(), lc.getY(), lc.getZ()));
	}*/
}