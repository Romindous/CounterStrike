package me.Romindous.CounterStrike.Listeners;

import me.Romindous.CounterStrike.Main;
import me.Romindous.CounterStrike.Enums.GunType;
import me.Romindous.CounterStrike.Enums.NadeType;
import me.Romindous.CounterStrike.Game.Arena;
import me.Romindous.CounterStrike.Game.Arena.Team;
import me.Romindous.CounterStrike.Game.Defusal;
import me.Romindous.CounterStrike.Objects.Shooter;
import me.Romindous.CounterStrike.Utils.PacketUtils;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
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

import com.mojang.datafixers.util.Pair;
 
public class MainLis implements Listener {
	
	@EventHandler
	public void onJoin(final PlayerJoinEvent e) {
		new BukkitRunnable() {
			@Override
			public void run() {
				Main.lobbyPl(e.getPlayer());
			}
		}.runTaskLater(Main.plug, 2);
	}
	
	@EventHandler
	public void onQuit(final PlayerQuitEvent e) {
		final Pair<Shooter, Arena> pr = Shooter.getPlShtrArena(e.getPlayer().getName());
		if (pr.getSecond() != null) {
			pr.getSecond().rmvPl(pr.getFirst()); 	
		}
	}
	
	@EventHandler
	public void onDrop(final PlayerDropItemEvent e) {
		final Player p = e.getPlayer();
		final ItemStack it = e.getItemDrop().getItemStack();
		if (GunType.getGnTp(it) != null) {
			e.getItemDrop().setItemStack(it);
			p.getInventory().setItemInMainHand(null);
		} else {
			switch (it.getType()) {
			case BONE:
			case BLAZE_ROD:
			case GOLD_NUGGET:
			case GHAST_TEAR:
				e.setCancelled(e.getPlayer().getGameMode() != GameMode.CREATIVE);
				break;
			case GOLDEN_APPLE:
				final Pair<Shooter, Arena> pr = Shooter.getPlShtrArena(p.getName());
				final Defusal ar = (Defusal) pr.getSecond();
				if (ar != null && ar.indon) {
					ar.indSts(PacketUtils.getNMSPlr(p).b);
				}
				break;
			case SHEARS:
				p.getInventory().setItemInMainHand(new ItemStack(Material.GOLD_NUGGET));
				break;
			default:
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
				final Pair<Shooter, Arena> pr = Shooter.getPlShtrArena(e.getEntity().getName());
				switch (it.getType()) {
				case GOLDEN_APPLE:
					if (pr.getSecond() != null && pr.getSecond().shtrs.get(pr.getFirst()) == Team.Ts) {
						inv.setItem(7, it);
						e.getItem().remove();
					} else if (((HumanEntity) e.getEntity()).getGameMode() != GameMode.CREATIVE) {
						e.getItem().setPickupDelay(10);
					}
					break;
				case SHEARS:
					if (pr.getSecond() != null && pr.getSecond().shtrs.get(pr.getFirst()) == Team.CTs) {
						if (inv.getItem(7) == null || inv.getItem(7).getType() == Material.GOLD_NUGGET) {
							inv.setItem(7, it);
							e.getItem().remove();
						}
					} else if (((HumanEntity) e.getEntity()).getGameMode() != GameMode.CREATIVE) {
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
}