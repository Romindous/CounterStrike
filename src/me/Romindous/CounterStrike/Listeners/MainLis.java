package me.Romindous.CounterStrike.Listeners;

import java.util.Random;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffectType;
import org.spigotmc.event.entity.EntityDismountEvent;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;

import me.Romindous.CounterStrike.Main;
import me.Romindous.CounterStrike.Enums.GunType;
import me.Romindous.CounterStrike.Enums.NadeType;
import me.Romindous.CounterStrike.Game.Arena;
import me.Romindous.CounterStrike.Game.Arena.Team;
import me.Romindous.CounterStrike.Game.Defusal;
import me.Romindous.CounterStrike.Objects.Shooter;
import me.Romindous.CounterStrike.Objects.Game.GameState;
import net.kyori.adventure.text.Component;
import ru.komiss77.enums.Data;
import ru.komiss77.events.BungeeDataRecieved;
import ru.komiss77.events.LocalDataLoadEvent;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.utils.ItemUtils;
 
public class MainLis implements Listener {
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onBungee(final BungeeDataRecieved e) {
		final Oplayer op = e.getOplayer();
        final String wa = op.getDataString(Data.WANT_ARENA_JOIN);
        if (!wa.isEmpty()) {/*
        	if (Main.nnactvarns.containsKey(wa)) {
        		final Arena ar;
            	if (Main.actvarns.get(wa) == null) {
            		ar = Main.plug.crtArena(wa, GameType.DEFUSAL);
                } else {
                	ar = Main.actvarns.get(wa);
				}
            	new BukkitRunnable() {
					@Override
					public void run() {
						if (ApiOstrov.hasParty(e.getPlayer()) && ApiOstrov.isPartyLeader(e.getPlayer())) {
							for (final String pl : ApiOstrov.getPartyPlayers(e.getPlayer())) {
								pl.charAt(0);
							}
						} else {
							ar.addPl(Shooter.getPlShooter(op.nik, true));
						}
					}
				}.runTaskLater(Main.plug, 5);
        	}*/
        }
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onData(final LocalDataLoadEvent e) {
		Main.lobbyPl(e.getPlayer());
		final String title = switch (new Random().nextInt(4)) {
            case 0 -> "Добро пожаловать!";
            case 1 -> "Приятной игры!";
            case 2 -> "Желаем удачи!";
            case 3 -> "Развлекайтесь!";
            default -> "";
        };

        e.getPlayer().sendPlayerListHeaderAndFooter(Component.text("§7<§5Counter Strike§7>\n" + title),
			Component.text("§7Сейчас в игре: §d" + getPlaying() + "§7 человек!"));
	}
	
	public static byte getPlaying() {
		int in = 0;
		for (final Arena ar : Main.actvarns.values()) {
			in += ar.shtrs.size();
		}
		return (byte) in;
	}

	@EventHandler
	public void onQuit(final PlayerQuitEvent e) {
		final Shooter pr = Shooter.getPlShooter(e.getPlayer().getName(), true);
		if (pr.arena() != null) {
			pr.arena().rmvPl(pr); 	
		}
	}
	
	@EventHandler
	public void onDrop(final PlayerDropItemEvent e) {
		final Player p = e.getPlayer();
		final Item drop = e.getItemDrop();
		final ItemStack it = drop.getItemStack();
		final Shooter pr = Shooter.getPlShooter(p.getName(), true);
		final GunType gt = GunType.getGnTp(it);
		if (gt != null) {
			if (pr.arena() == null) drop.remove();
			else drop.setItemStack(it);
			p.getInventory().setItemInMainHand(Main.air);
			if (gt.snp) p.getInventory().setItemInOffHand(Main.air);
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
			case CRIMSON_BUTTON:
			case TOTEM_OF_UNDYING:
				e.setCancelled(e.getPlayer().getGameMode() != GameMode.CREATIVE);
				break;
			case GOLDEN_APPLE:
				final Defusal ar = (Defusal) pr.arena();
				if (ar != null) {
					ar.dropBomb(drop);
					if (ar.indon) {
						ar.indSts(p);
					}
				}
				break;
			case SHEARS:
				p.getInventory().setItem(7, Main.mkItm(Material.GOLD_NUGGET, "§eКусачки §f\u9268", 10));
				break;
			default:
				if (pr.arena() == null) {
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
					if (inv.getItem(NadeType.prmSlot) == null) {
						inv.setItem(NadeType.prmSlot, it);
						e.getItem().remove();
					} else {
						e.getItem().setPickupDelay(10);
					}
				} else {
					final ItemStack i = inv.getItem(4);
					if (i == null) {
						inv.setItem(NadeType.scdSlot, it);
						e.getItem().remove();
					} else if (i.getType() == it.getType() && i.getAmount() == 1) {
						i.setAmount(2);
						e.getItem().remove();
					} else {
						e.getItem().setPickupDelay(10);
					}
				}
			} else if (e.getEntityType() == EntityType.PLAYER) {
				final Shooter pr = Shooter.getPlShooter(e.getEntity().getName(), true);
				switch (it.getType()) {
				case GOLDEN_APPLE:
					final Defusal ar = (Defusal) pr.arena();
					if (ar != null && ar.shtrs.get(pr) == Team.Ts) {
						pr.item(it, 7);
						e.getItem().remove();
						ar.pickBomb();
						if (!ar.indon && ((Player) e.getEntity()).getInventory().getHeldItemSlot() == 7) {
							ar.indSts((Player) e.getEntity());
						}
					} else if (((HumanEntity) e.getEntity()).getGameMode() != GameMode.CREATIVE) {
						e.getItem().setPickupDelay(10);
					}
					break;
				case SHEARS:
					if (pr.arena() != null && pr.arena().shtrs.get(pr) == Team.CTs) {
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
		final Arena ar = Shooter.getPlShooter(e.getPlayer().getName(), true).arena();
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
		if (gt != null && gt.snp && !e.isSneaking()) {
			Shooter.getPlShooter(p.getName(), true).scope(false);
			if (!ItemUtils.isBlank(p.getInventory().getItemInOffHand(), false))
				p.getInventory().setItemInOffHand(Main.air);
		}
	}
	
	@EventHandler
	public void onTgt(final EntityTargetEvent e) {
		if (e.getEntity() instanceof final Mob mb) {
            if (mb.hasPotionEffect(PotionEffectType.BLINDNESS)) {
				mb.setTarget(null);
				e.setTarget(null);
			}
		}
	}
}