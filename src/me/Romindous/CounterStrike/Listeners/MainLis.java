package me.Romindous.CounterStrike.Listeners;

import java.util.Random;
import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import me.Romindous.CounterStrike.CSCmd;
import me.Romindous.CounterStrike.Enums.GameState;
import me.Romindous.CounterStrike.Enums.GameType;
import me.Romindous.CounterStrike.Enums.GunType;
import me.Romindous.CounterStrike.Enums.NadeType;
import me.Romindous.CounterStrike.Game.Arena;
import me.Romindous.CounterStrike.Game.Arena.Team;
import me.Romindous.CounterStrike.Game.Defusal;
import me.Romindous.CounterStrike.Main;
import me.Romindous.CounterStrike.Objects.Defusable;
import me.Romindous.CounterStrike.Objects.Game.PlShooter;
import me.Romindous.CounterStrike.Objects.Shooter;
import me.Romindous.CounterStrike.Utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.potion.PotionEffectType;
import ru.komiss77.Ostrov;
import ru.komiss77.enums.Data;
import ru.komiss77.events.LocalDataLoadEvent;
import ru.komiss77.modules.player.PM;
import ru.komiss77.utils.ItemUtil;
import ru.komiss77.utils.TCUtil;

public class MainLis implements Listener {

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onData(final LocalDataLoadEvent e) {
		final Player p = e.getPlayer();
		Main.lobbyPl(p);
		final String title = switch (new Random().nextInt(4)) {
			case 0 -> "Добро пожаловать!";
			case 1 -> "Приятной игры!";
			case 2 -> "Желаем удачи!";
			case 3 -> "Развлекайтесь!";
			default -> "";
		};

		p.sendPlayerListHeaderAndFooter(TCUtil.form("§7<§5Counter Strike§7>\n" + title),
			TCUtil.form("§7Сейчас в игре: §d" + getPlaying() + "§7 человек!"));
		final String wa = PM.getOplayer(p).getDataString(Data.WANT_ARENA_JOIN);
//		p.sendMessage(" " + wa);
		if (!wa.isEmpty()) {
			if (Main.nnactvarns.containsKey(wa)) {
				final Arena ar;
				final Arena actv = Main.actvarns.get(wa);
				if (actv == null) {
					ar = Main.plug.crtArena(wa, GameType.DEFUSAL);
				} else ar = actv;
				Ostrov.sync(() -> {
					CSCmd.partyJoinMap(Shooter.getPlShooter(p.getName(), true), p, ar);
				}, 40);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onWorld(final PlayerChangedWorldEvent e) {
		final Player p = e.getPlayer();
		PM.getOplayer(p).tag.visible(true);
		for (final Player pl : Bukkit.getOnlinePlayers()) {
			if (p.getEntityId() == pl.getEntityId()) continue;
			pl.hidePlayer(Main.plug, p);
			p.hidePlayer(Main.plug, pl);
		}

		for (final Player pl : p.getWorld().getPlayers()) {
			if (p.getEntityId() == pl.getEntityId()) continue;
			pl.showPlayer(Main.plug, p);
			p.showPlayer(Main.plug, pl);
		}
	}
	
	public static int getPlaying() {
		int in = 0;
		for (final Arena ar : Main.actvarns.values()) {
			in += ar.shtrs.size();
		}
		return in;
	}

	@EventHandler
	public void onQuit(final PlayerQuitEvent e) {
		final PlShooter pr = Shooter.getPlShooter(e.getPlayer().getName(), false);
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
		final GunType gt = GunType.get(it);
		drop.setInvulnerable(true);
		if (gt != null) {
			it.setAmount(1);
			Main.setDur(it, 0);
			if (pr.arena() == null) drop.remove();
			else drop.setItemStack(it);
			pr.item(EquipmentSlot.HAND, ItemUtil.air);
			return;
		}

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
					if (ar.indon) {
						ar.indSts(p);
					}
					ar.dropBomb(drop);
				}
				break;
			case SHEARS:
				p.getInventory().setItem(7, Main.mkItm(ItemType.GOLD_NUGGET, "§eКусачки §f\u9268", Defusable.PLIERS_MDL));
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
   
	@EventHandler
	public void onPick(final EntityPickupItemEvent e) {
		if (e.getEntity() instanceof final Player pl) {
			final ItemStack it = e.getItem().getItemStack();
			final GunType gt = GunType.get(it);
			final NadeType nt = NadeType.getNdTp(it);
			e.setCancelled(pl.getGameMode() != GameMode.CREATIVE);
			final Shooter pr = Shooter.getPlShooter(pl.getName(), true);
			if (gt != null) {
				if (ItemUtil.isBlank(pr.item(gt.prm ? 0 : 1), false)) {
					pr.item(gt.prm ? 0 : 1, it);
					e.getItem().remove();
				} else {
					e.getItem().setPickupDelay(10);
				}
				return;
			}
			if (nt != null) {
				if (nt.prm) {
					if (ItemUtil.isBlank(pr.item(NadeType.prmSlot), false)) {
						pr.item(NadeType.prmSlot, it);
						e.getItem().remove();
					} else {
						e.getItem().setPickupDelay(10);
					}
				} else {
					final ItemStack i = pr.item(NadeType.scdSlot);
					if (ItemUtil.isBlank(i, false)) {
						pr.item(NadeType.scdSlot, it);
						e.getItem().remove();
					} else if (i.getType() == it.getType() && i.getAmount() == 1) {
						i.setAmount(2);
						e.getItem().remove();
					} else {
						e.getItem().setPickupDelay(10);
					}
				}
				return;
			}
			switch (it.getType()) {
				case GOLDEN_APPLE:
					final Defusal ar = (Defusal) pr.arena();
					if (ar != null && ar.shtrs.get(pr) == Team.Ts) {
						pr.item(7, it);
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
						if (!ItemUtil.is(pr.item(7), ItemType.SHEARS)) {
							pr.item(7, it);
							e.getItem().remove();
						}
					} else if (((HumanEntity) e.getEntity()).getGameMode() != GameMode.CREATIVE) {
						e.getItem().setPickupDelay(10);
					}
					break;
				case SUGAR:
					if (ItemUtil.isBlank(pr.item(3), false)) {
						pr.item(3, it);
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
	   
	@EventHandler
	public void onMerge(final ItemMergeEvent e) {
		e.setCancelled(true);
	}

	/*@EventHandler
	public void onOut(final EntityDismountEvent e) {
		e.setCancelled(e.getEntityType() != EntityType.PLAYER || ((HumanEntity) e.getEntity()).getGameMode() != GameMode.CREATIVE);
	}*/

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
		final ItemStack it = p.getInventory().getItemInMainHand();
		final GunType gt = GunType.fast(it);
		if (gt == null || !gt.snp) {
			if (e.isSneaking()) return;
			Utils.zoom(p, false);
			return;
		}
		Utils.zoom(p, e.isSneaking() && !Main.hasDur(it));
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