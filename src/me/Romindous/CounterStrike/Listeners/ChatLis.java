package me.Romindous.CounterStrike.Listeners;

import java.util.Map.Entry;
import me.Romindous.CounterStrike.Game.Arena;
import me.Romindous.CounterStrike.Game.Arena.Team;
import me.Romindous.CounterStrike.Objects.Shooter;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import ru.komiss77.ApiOstrov;
import ru.komiss77.enums.Stat;
import ru.komiss77.events.ChatPrepareEvent;
import ru.komiss77.listener.ChatLst;
import ru.komiss77.modules.player.Perm;
import ru.komiss77.utils.StringUtil;
import ru.komiss77.utils.TCUtil;

public class ChatLis implements Listener {
	
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChat(final ChatPrepareEvent e) {
		final Player p = e.getPlayer();
		final Shooter pr = Shooter.getPlShooter(p.getName(), true);
		final Arena ar = pr.arena();

		final String msg = Perm.canColorChat(e.getOplayer())
			? e.getMessage().replace('&', '§') : e.getMessage();
		e.showLocal(true);
		e.showSelf(false);
    	if (ar == null) {
			final Component modMsg = TCUtil.form("§7{§5" + StringUtil.toSigFigs(
				(double) ApiOstrov.getStat(p, Stat.CS_kill) / (double) ApiOstrov.getStat(p, Stat.CS_death), (byte) 2) + "§7} "
				+ ChatLst.NIK_COLOR + p.getName() + " §7[§5ЛОББИ§7] <gray><i>≫</i> " + msg);
			for (final Audience au : e.viewers()) {
				au.sendMessage(modMsg);
			}
			p.sendMessage(modMsg);
    	} else {
			final Component modMsg;
			final Team tm = ar.shtrs.get(pr);
			switch (ar.gst) {
			case WAITING:
			case BEGINING:
			case FINISH:
				modMsg = TCUtil.form(tm.clr + p.getName() + " §7[§d" + ar.name + "§7] <gray><i>≫</i> " + msg);
				for (final Audience au : e.viewers()) {
					au.sendMessage(modMsg);
				}
				p.sendMessage(modMsg);
				break;
			case BUYTIME:
			case ROUND:
			case ENDRND:
				if (msg.startsWith("!")) {
					if (msg.length() > 1) {
						modMsg = TCUtil.form("§7[Всем] " + tm.clr + p.getName() + " <gray><i>≫</i> " + msg.substring(1));
						for (final Shooter sh : ar.shtrs.keySet()) {
							final Player pl = sh.getPlayer();
							if (pl != null) {
								pl.sendMessage(modMsg);
								pl.playSound(pl.getLocation(), Sound.BLOCK_GRINDSTONE_USE, 1f, 1.4f);
							}
						}
					}
				} else {
					modMsg = TCUtil.form("§7[" + tm.icn + "§7] " + p.getName() + " <gray><i>≫</i> " + msg);
					for (final Entry<Shooter, Team> n : ar.shtrs.entrySet()) {
						if (n.getValue() == tm) {
							final Player pl = n.getKey().getPlayer();
							if (pl != null) {
								pl.sendMessage(modMsg);
								pl.playSound(pl.getLocation(), Sound.BLOCK_GRINDSTONE_USE, 1f, 2f);
							}
						}
					}
				}
				break;
			}
		}
		e.viewers().clear();
    }
}
