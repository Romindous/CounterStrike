package me.Romindous.CounterStrike.Listeners;

import me.Romindous.CounterStrike.Main;
import me.Romindous.CounterStrike.Enums.GunType;
import me.Romindous.CounterStrike.Utils.PacketUtils;

import org.bukkit.GameMode;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
 
public class MainLis implements Listener {
	@EventHandler
	public void onDrop(final PlayerDropItemEvent e) {
		final ItemStack it = e.getPlayer().getInventory().getItemInMainHand();
		if (GunType.getGnTp(it) != null) {
			e.getItemDrop().setItemStack(it);
			e.getPlayer().getInventory().setItemInMainHand(null);
		} 
	}
   
	@EventHandler
	public void onPick(final EntityPickupItemEvent e) {
		final GunType gt = GunType.getGnTp(e.getItem().getItemStack());
		if (gt != null && e.getEntityType() == EntityType.PLAYER) {
			e.setCancelled(true);
			final PlayerInventory inv = ((Player) e.getEntity()).getInventory();
			if (inv.getItem(gt.prm ? 0 : 1) == null) {
				inv.setItem(gt.prm ? 0 : 1, e.getItem().getItemStack());
				e.getItem().remove();
				return;
			} 
			e.getItem().setPickupDelay(10);
		} 
	}
   
	@EventHandler
	public void onSwap(final PlayerSwapHandItemsEvent e) {
		e.setCancelled((e.getPlayer().getGameMode() != GameMode.CREATIVE));
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
				PacketUtils.fkHlmtClnt(p, Main.cp);
				PacketUtils.zoom(p, true);
			} else {
				PacketUtils.fkHlmtClnt(p, p.getInventory().getHelmet());
				PacketUtils.zoom(p, false);
			}
		}
	}
}