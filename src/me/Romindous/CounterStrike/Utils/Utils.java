package me.Romindous.CounterStrike.Utils;

import java.util.ConcurrentModificationException;
import me.Romindous.CounterStrike.Main;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import ru.komiss77.modules.world.WXYZ;
import ru.komiss77.utils.ScreenUtil;

public class Utils {

    private static int id = 0;

    public static void sendTtlSbTtl(final Player p, final String ttl, final String sbttl, final int tm) {
        ScreenUtil.sendTitleDirect(p, ttl, sbttl, 4, tm, 20);
    }

    public static void sendTtl(final Player p, final String ttl, final int tm) {
        ScreenUtil.sendTitleDirect(p, ttl, "", 4, tm, 20);
    }

    public static void sendSbTtl(final Player p, final String sbttl, final int tm) {
        ScreenUtil.sendTitleDirect(p, "", sbttl, 4, tm, 20);
    }

    public static void sendAcBr(final Player p, final String msg) {
        ScreenUtil.sendActionBarDirect(p, msg);
    }

    public static void crackBlock(final WXYZ bl) {
        final Location loc = bl.getCenterLoc();
        final float stg = getNxtStg(bl);
        bl.w.getPlayers().forEach(p -> p.sendBlockDamage(loc, stg,
            (id == 1000 ? id = 0 : id++) + 10000));
    }

    private static final int MAX_CRACK = 7;
    private static final float CRACK_DEL = 0.8f / MAX_CRACK;
    public static float getNxtStg(final WXYZ bl) {
        try {
            for (final WXYZ s : Main.cracks) {
                if (s.distAbs(bl) == 0) {
                    if (s.yaw != MAX_CRACK) s.yaw++;
                    return s.yaw * CRACK_DEL;
                }
            }
        } catch (ConcurrentModificationException e) {
            return 0.4f;
        }
        bl.yaw = 1;
        Main.cracks.add(bl);
        return 0.1f;
    }
 	
	/*public static void sendRecoil(final PlShooter sh, final Location rot) {
		final Location lc = rot.add(rot.getDirection().multiply(40d));
		Nms.sendPacket(sh.getPlayer(), new PacketPlayOutLookAt(Anchor.b, lc.getX(), lc.getY(), lc.getZ()));
	}*/
}