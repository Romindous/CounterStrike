package me.Romindous.CounterStrike.Listeners;

import java.util.Map.Entry;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import io.papermc.paper.event.player.AsyncChatEvent;
import me.Romindous.CounterStrike.Main;
import me.Romindous.CounterStrike.Game.Arena;
import me.Romindous.CounterStrike.Game.Arena.Team;
import me.Romindous.CounterStrike.Objects.Shooter;
import me.Romindous.CounterStrike.Objects.Game.PlShooter;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import ru.komiss77.ApiOstrov;
import ru.komiss77.enums.Stat;
import ru.komiss77.events.ChatPrepareEvent;
import ru.komiss77.utils.TCUtils;

public class ChatLis implements Listener {
	
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChat(final ChatPrepareEvent e) {
        final Player p = e.getPlayer();
        final PlShooter sh = Shooter.getPlShooter(p.getName(), true);
		e.showLocal(false);
    	if (sh.arena() == null) {
            final Component c = TCUtils.format("§7(§5" + ApiOstrov.toSigFigs(
        		(float) ApiOstrov.getStat(p, Stat.CS_kill) / (float) ApiOstrov.getStat(p, Stat.CS_death), (byte) 2) + "§7) ");
            e.setSenderGameInfo(c);
            e.setViewerGameInfo(c);
    	} else {
			switch (sh.arena().gst) {
			case BUYTIME:
			case ENDRND:
			case ROUND:
	    		e.sendProxy(false);
	    		return;
			case WAITING:
			case BEGINING:
			case FINISH:
	            final Component c = TCUtils.format("§7[§5" + sh.arena().name + "§7] ");
                e.setSenderGameInfo(c);
                e.setViewerGameInfo(c);
				break;
			}
		}
    }
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
	public void onAChat(final AsyncChatEvent e) {
		final Player snd = e.getPlayer();
		final Shooter pr = Shooter.getPlShooter(snd.getName(), true);
		final String msg = TCUtils.toString(e.message());
		final Arena ar = pr.arena();
		//если на арене
		if (ar == null) {
			for (final Audience au : e.viewers()) {
				au.sendMessage(TCUtils.format(Main.prf().replace('[', '<').replace(']', '>') + snd.getName() + " §7[§5ЛОББИ§7] §o≫ §7" + msg));
			}
		} else {
			final Team tm = ar.shtrs.get(pr);
			switch (ar.gst) {
			case WAITING:
			case BEGINING:
			case FINISH:
				for (final Audience au : e.viewers()) {
					au.sendMessage(TCUtils.format(Main.prf().replace('[', '<').replace(']', '>') + snd.getName() + " §7[§d" + ar.name + "§7] §o≫ §7" + msg));
				}
				break;
			case BUYTIME:
			case ROUND:
			case ENDRND:
				if (msg.startsWith("!")) {
					if (msg.length() > 1) {
						for (final Shooter sh : ar.shtrs.keySet()) {
							final Player p = sh.getPlayer();
							if (p != null) {
								p.sendMessage(TCUtils.format("§7[Всем] " + tm.clr + 
									snd.getName() + " §7§o≫ §7" + msg.substring(1)));
								p.playSound(p.getLocation(), Sound.BLOCK_GRINDSTONE_USE, 1f, 1.4f);
							}
						}
					}
				} else {
					for (final Entry<Shooter, Team> n : ar.shtrs.entrySet()) {
						if (n.getValue() == tm) {
							final Player p = n.getKey().getPlayer();
							if (p != null) {
								p.sendMessage(TCUtils.format("§7[" + tm.icn + "§7] " + 
									snd.getName() + " §7§o≫ §7" + msg));
								p.playSound(p.getLocation(), Sound.BLOCK_GRINDSTONE_USE, 1f, 2f);
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
