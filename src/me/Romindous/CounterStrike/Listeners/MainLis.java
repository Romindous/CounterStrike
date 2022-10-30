package me.Romindous.CounterStrike.Listeners;

import me.Romindous.CounterStrike.Main;
import me.Romindous.CounterStrike.Enums.GameState;
import me.Romindous.CounterStrike.Enums.GunType;
import me.Romindous.CounterStrike.Enums.NadeType;
import me.Romindous.CounterStrike.Game.Arena;
import me.Romindous.CounterStrike.Game.Arena.Team;
import me.Romindous.CounterStrike.Game.Defusal;
import me.Romindous.CounterStrike.Objects.Shooter;
import me.Romindous.CounterStrike.Utils.PacketUtils;
import me.clip.deluxechat.events.DeluxeChatEvent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Ostrov;
import ru.komiss77.enums.Data;
import ru.komiss77.events.BungeeDataRecieved;
import ru.komiss77.modules.player.Oplayer;

import java.util.Random;
import java.util.Iterator;
import java.util.Map.Entry;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;
import org.spigotmc.event.entity.EntityDismountEvent;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import io.papermc.paper.event.player.AsyncChatEvent;
 
public class MainLis implements Listener {
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onBungee(final BungeeDataRecieved e) {
		final Oplayer op = e.getOplayer();
        final String wa = op.getDataString(Data.WANT_ARENA_JOIN);
        if (!wa.isEmpty()) {
        	if (Main.nnactvarns.containsKey(wa)) {
        		final Arena ar;
            	if (Main.actvarns.get(wa) == null) {
            		ar = Main.plug.crtArena(wa);
                } else {
                	ar = Main.actvarns.get(wa);
				}
            	new BukkitRunnable() {
					@Override
					public void run() {
						if (ApiOstrov.hasParty(e.getPlayer()) && ApiOstrov.isPartyLeader(e.getPlayer())) {
							for (final String pl : ApiOstrov.getPartyPlayers(e.getPlayer())) {
								//ApiOstrov.sendToServer(null, pl, wa);
							}
						} else {
							ar.addPl(Shooter.getShooter(op.nik));
						}
					}
				}.runTaskLater(Main.plug, 5);
        	}
        }
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onData(final PlayerJoinEvent e) {
		Ostrov.sync(() -> {
			Main.lobbyPl(e.getPlayer());
			final String title;
			switch (new Random().nextInt(4)) {
			case 0:
				title = "Добро пожаловать!";
				break;
			case 1:
				title = "Приятной игры!";
				break;
			case 2:
				title = "Желаем удачи!";
				break;
			case 3:
				title = "Развлекайтесь!";
				break;
			default:
				title = "";
				break;
			}
			e.getPlayer().sendPlayerListHeaderAndFooter(Component.text("§7<§5Counter Strike§7>\n" + title), 
				Component.text("§7Сейчас в игре: §d" + getPlaying() + "§7 человек!"));
			e.getPlayer().setResourcePack(Main.rplnk);
		}, 2);
	}
	
	public static byte getPlaying() {
		byte in = 0;
		for (final Arena ar : Main.actvarns.values()) {
			in += ar.shtrs.size();
		}
		return in;
	}

	@EventHandler
	public void onQuit(final PlayerQuitEvent e) {
		final Shooter pr = Shooter.getShooter(e.getPlayer().getName());
		if (pr.arena != null) {
			pr.arena.rmvPl(pr); 	
		}
	}
	
	@EventHandler
	public void onDrop(final PlayerDropItemEvent e) {
		final Player p = e.getPlayer();
		final Item drop = e.getItemDrop();
		final ItemStack it = drop.getItemStack();
		final Shooter pr = Shooter.getShooter(p.getName());
		if (GunType.getGnTp(it) != null) {
			if (pr.arena == null) {
				drop.remove();
				p.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
				return;
			}
			drop.setItemStack(it);
			p.getInventory().setItemInMainHand(null);
		} else {
			switch (it.getType()) {
			case BONE:
			case BLAZE_ROD:
			case NETHER_STAR:
			case SLIME_BALL:
			case GOLD_NUGGET:
			case GHAST_TEAR:
			case MAGMA_CREAM:
			case CAMPFIRE:
				e.setCancelled(e.getPlayer().getGameMode() != GameMode.CREATIVE);
				break;
			case GOLDEN_APPLE:
				final Defusal ar = (Defusal) pr.arena;
				if (ar != null) {
					for (final Entry<Shooter, Team> n : ar.shtrs.entrySet()) {
						((Player) n.getKey().inv.getHolder()).playSound(n.getKey().inv.getHolder().getLocation(), "cs.info." + (n.getValue() == Team.Ts ? "tdropbmb" : "ctdropbmb"), 1f, 1f);
					}
					if (ar.indon) {
						ar.indSts(Main.ds.bh().a(p.getName()).b);
					}
					
					if (ar.gst == GameState.ROUND) {
						for (final Shooter sh : ar.shtrs.keySet()) {
							((Player) sh.inv.getHolder()).getScoreboard().getTeam("bmb").addEntry(drop.getUniqueId().toString());
						}
						drop.setGlowing(true);
					}
				}
				break;
			case SHEARS:
				p.getInventory().setItem(7, Main.mkItm(Material.GOLD_NUGGET, "§eКусачки §f\u9268", 1));
				break;
			default:
				if (pr.arena == null) {
					drop.remove();
					p.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
					return;
				}
				break;
			}
		}
	}
   
	@EventHandler
	public void onPick(final EntityPickupItemEvent e) {
		if (e.getEntityType() == EntityType.PLAYER) {
			final PlayerInventory inv = ((Player) e.getEntity()).getInventory();
			final ItemStack it = e.getItem().getItemStack();
			final GunType gt = GunType.getGnTp(it);
			final NadeType nt = NadeType.getNdTp(it);
			e.setCancelled(((HumanEntity) e.getEntity()).getGameMode() != GameMode.CREATIVE);
			if (gt != null) {
				if (inv.getItem(gt.prm ? 0 : 1) == null) {
					inv.setItem(gt.prm ? 0 : 1, it);
					e.getItem().remove();
				} else {
					e.getItem().setPickupDelay(10);
				}
			} else if (nt != null) {
				if (nt.prm) {
					if (inv.getItem(3) == null) {
						inv.setItem(3, it);
						e.getItem().remove();
					} else {
						e.getItem().setPickupDelay(10);
					}
				} else {
					final ItemStack i = inv.getItem(4);
					if (i == null) {
						inv.setItem(4, it);
						e.getItem().remove();
					} else if (i.getType() == it.getType() && i.getAmount() == 1) {
						i.setAmount(2);
						e.getItem().remove();
					} else {
						e.getItem().setPickupDelay(10);
					}
				}
			} else if (e.getEntityType() == EntityType.PLAYER) {
				final Shooter pr = Shooter.getShooter(e.getEntity().getName());
				switch (it.getType()) {
				case GOLDEN_APPLE:
					final Defusal ar = (Defusal) pr.arena;
					if (ar != null && ar.shtrs.get(pr) == Team.Ts) {
						inv.setItem(7, it);
						e.getItem().remove();
						for (final Entry<Shooter, Team> n : ar.shtrs.entrySet()) {
							if (n.getValue() == Team.Ts) {
								((Player) n.getKey().inv.getHolder()).playSound(n.getKey().inv.getHolder().getLocation(), "cs.info.tpkpbmb", 1f, 1f);
							}
						}
						if (!ar.indon) {
							ar.indSts(Main.ds.bh().a(e.getEntity().getName()).b);
						}
					} else if (((HumanEntity) e.getEntity()).getGameMode() != GameMode.CREATIVE) {
						e.getItem().setPickupDelay(10);
					}
					break;
				case SHEARS:
					if (pr.arena != null && pr.arena.shtrs.get(pr) == Team.CTs) {
						if (inv.getItem(7) == null || inv.getItem(7).getType() == Material.GOLD_NUGGET) {
							inv.setItem(7, it);
							e.getItem().remove();
						}
					} else if (((HumanEntity) e.getEntity()).getGameMode() != GameMode.CREATIVE) {
						e.getItem().setPickupDelay(10);
					}
					break;
				case SUGAR:
					if (inv.getItem(3) == null) {
						inv.setItem(3, it);
						e.getItem().remove();
					} else {
						e.getItem().setPickupDelay(10);
					}
					break;
				default:
					e.setCancelled(false);
					break;
				}
			}
		}
	}
	   
	@EventHandler
	public void onMerge(final ItemMergeEvent e) {
		e.setCancelled(true);
	}
	   
	@EventHandler
	public void onOut(final EntityDismountEvent e) {
		e.setCancelled(e.getEntityType() != EntityType.PLAYER || ((HumanEntity) e.getEntity()).getGameMode() != GameMode.CREATIVE);
	}

	@EventHandler
	public void onSwap(final PlayerSwapHandItemsEvent e) {
		e.setCancelled(e.getPlayer().getGameMode() != GameMode.CREATIVE);
	}

	@EventHandler
	public void onJump(final PlayerJumpEvent e) {
		final Arena ar = Shooter.getShooter(e.getPlayer().getName()).arena;
		e.setCancelled(ar != null && ar.gst == GameState.BUYTIME);
	}
	
	@EventHandler
	public void onFood(final FoodLevelChangeEvent e) {
		e.setFoodLevel(19);
	}
   
	@EventHandler
	public void onExp(final PlayerExpChangeEvent e) {
		e.setAmount(0);
	}
   
	@EventHandler
	public void onBreak(final BlockBreakEvent e) {
		e.setCancelled((e.getPlayer().getGameMode() != GameMode.CREATIVE));
	}
   
	@EventHandler
	public void onShift(final PlayerToggleSneakEvent e) {
		final Player p = e.getPlayer();
		final GunType gt = GunType.getGnTp(p.getInventory().getItemInMainHand());
		if (gt != null && gt.snp) {
			if (e.isSneaking()) {
				PacketUtils.zoom(p, true);
			} else {
				PacketUtils.zoom(p, false);
			}
		}
	}
	
	@EventHandler
	public void onChat(final AsyncChatEvent e) {
		final String msg = ((TextComponent) e.message()).content();
		if (msg.startsWith("/")) {
			return;
		}
		final Player snd = e.getPlayer();
		final Shooter pr = Shooter.getShooter(snd.getName());
		final Arena ar = pr.arena;
		//если на арене
		if (ar == null) {
			return;
		} else {
			final Team tm = ar.shtrs.get(pr);
			switch (ar.gst) {
			case WAITING:
			case BEGINING:
			case FINISH:
				final Iterator<Audience> pl = e.viewers().iterator();
				while (pl.hasNext()) {
					final Audience rec = pl.next();
					if (rec instanceof Player) {
						sendSpigotMsg(Main.prf().replace('[', '<').replace(']', '>') + snd.getName() + " §7[§d" + ar.name + "§7] ≫ " + msg, (Player) rec);
						if (Main.eqlsCompStr(((Player) rec).getServer().motd(), snd.getServer().motd())) {
							pl.remove();
						}
					}
		        }
				return;
			case BUYTIME:
			case ROUND:
			case ENDRND:
				if (msg.startsWith("!")) {
					if (msg.length() > 1) {
						for (final Shooter sh : ar.shtrs.keySet()) {
							sendSpigotMsg("§7[Всем] " + tm.clr + 
								snd.getName() + " §7≫ " + msg.replaceFirst("!", ""), (Player) sh.inv.getHolder());
						}
					}
				} else {
					for (final Entry<Shooter, Team> n : ar.shtrs.entrySet()) {
						if (n.getValue() == tm) {
							sendSpigotMsg("§7[" + tm.icn + "§7] " + 
								snd.getName() + " §7≫ " + msg.replaceFirst("!", ""), (Player) n.getKey().inv.getHolder());
						}
					}
				}
				break;
			}
		}
        e.viewers().clear();
    }
	
	@EventHandler
    public void Dchat(final DeluxeChatEvent e) {
        final Arena ar = Shooter.getShooter(e.getPlayer().getName()).arena;
        if (ar != null) {
			switch (ar.gst) {
			case WAITING:
			case BEGINING:
			case FINISH:
	            e.getDeluxeFormat().setPrefix(Main.prf() + "§7<§5" + ar.name + "§7> ");
			case BUYTIME:
			case ROUND:
			case ENDRND:
	            e.setCancelled(true);
	            return;
			}
        }
        /*final Iterator<Player> recipients = e.getRecipients().iterator();
        while (recipients.hasNext()) {
            final Player recipient = recipients.next();
            if (!recipient.getWorld().getName().equalsIgnoreCase(p.getWorld().getName())) {
                recipients.remove();
            }
        }*/
    }
	
	public static void sendSpigotMsg(final String msg, final Player p) {
		p.spigot().sendMessage(new net.md_5.bungee.api.chat.TextComponent(msg));
	}
}