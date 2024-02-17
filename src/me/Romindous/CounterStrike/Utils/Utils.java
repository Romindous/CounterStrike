package me.Romindous.CounterStrike.Utils;

import me.Romindous.CounterStrike.Main;
import net.minecraft.core.BlockPosition;
import net.minecraft.network.protocol.game.PacketPlayOutBlockBreakAnimation;
import org.bukkit.entity.Player;
import ru.komiss77.ApiOstrov;
import ru.komiss77.modules.world.WXYZ;
import ru.komiss77.version.Nms;

import java.util.ConcurrentModificationException;

public class Utils {
	
	private static int id = 0;
  
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