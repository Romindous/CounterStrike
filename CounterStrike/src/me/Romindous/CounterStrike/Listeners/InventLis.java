package me.Romindous.CounterStrike.Listeners;

import java.util.List;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import me.Romindous.CounterStrike.Main;
import me.Romindous.CounterStrike.Enums.GunType;
import me.Romindous.CounterStrike.Objects.Shooter;
import me.Romindous.CounterStrike.Objects.SmplLoc;
import me.Romindous.CounterStrike.Utils.PacketUtils;
import net.minecraft.core.BlockPosition;

public class InventLis implements Listener {
   
	@EventHandler
	public void onOpen(final InventoryOpenEvent e) {
		
	}
	
	@EventHandler
	public void onSlot(final PlayerItemHeldEvent e) {
		final Player p = e.getPlayer();
		final Shooter sh = Shooter.getPlShtr(p.getName());
		sh.is = false;
		sh.cnt = 0;
		final ItemStack it = sh.inv.getItem(e.getNewSlot());
		final GunType gt = GunType.getGnTp(it);
		if (gt != null && ((Damageable)it.getItemMeta()).hasDamage()) {
			sh.cnt = (short) ((it.getType().getMaxDurability() - ((Damageable)it.getItemMeta()).getDamage()) * gt.rtm / it.getType().getMaxDurability());
		}
		if (p.isSneaking()) {
			PacketUtils.fkHlmtClnt(p, sh.inv.getHelmet());
			PacketUtils.zoom(p, false);
			e.getPlayer().setSneaking(false);
		} 
	}
   
	@EventHandler
	public void onClick(final InventoryClickEvent e) {
		if (e.getView().getTitle().equalsIgnoreCase("§5Магазин")) {
			if (e.getCurrentItem() != null) {
				final ItemStack it = e.getCurrentItem();
				final ItemStack cp;
				e.setCancelled(true);
				if (e.getClickedInventory().getType() != InventoryType.PLAYER && it.getItemMeta().hasDisplayName()) {
					switch (it.getItemMeta().getDisplayName().charAt(1)) {
					case '5':
						cp = it.clone();
						cp.setAmount((GunType.getGnTp(it)).amo);
						e.getWhoClicked().getInventory().setItem(0, cp);
						break;
					case 'd':
						cp = it.clone();
						cp.setAmount((GunType.getGnTp(it)).amo);
						e.getWhoClicked().getInventory().setItem(1, cp);
						break;
					case 'f':
						e.getWhoClicked().getInventory().setItem(2, it);
						break;
					case '6':
					case '9':
					case 'c':
						e.getWhoClicked().getInventory().setItem(3, it);
						break;
					case '2':
					case '7':
					case '8':
						addSetItm(e.getWhoClicked().getInventory(), 4, it);
						break;
					case '3':
						e.getWhoClicked().getInventory().setItem(7, it);
						break;
					}
				}
			} 
		} else if (e.getView().getTitle().equalsIgnoreCase("§3§lРазминировка бомбы")) {
			e.setCancelled(true);
			final ItemStack it = e.getCurrentItem();
			if (it != null && it.getType() == Material.STRING) {
				final Player p = (Player) e.getWhoClicked();
				if (it.getItemMeta().getCustomModelData() == e.getClickedInventory().getItem(4).getItemMeta().getCustomModelData()) {
					final ItemStack cp = it.clone();
					final ItemMeta im = it.getItemMeta();
					if (Main.srnd.nextBoolean()) {
						im.setCustomModelData(Integer.valueOf(10));
						p.playSound(p.getLocation(), Sound.BLOCK_TRIPWIRE_CLICK_ON, 1f, 2f);
					} else {
						im.setCustomModelData(Integer.valueOf(11 + Main.srnd.nextInt(4)));
						p.playSound(p.getLocation(), Sound.BLOCK_LODESTONE_PLACE, 1f, 2f);
					} 
					it.setItemMeta(im);
					if (!e.getClickedInventory().contains(cp)) {
						p.playSound(p.getLocation(), Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE, 1f, 2f);
						p.playSound(p.getLocation(), Sound.BLOCK_IRON_TRAPDOOR_CLOSE, 1f, 2f);
						p.closeInventory();
						final List<String> lr = e.getClickedInventory().getItem(4).getItemMeta().getLore();
						final Block b = p.getWorld().getBlockAt(Integer.parseInt(lr.get(1).substring(2)), Integer.parseInt(lr.get(2).substring(2)), Integer.parseInt(lr.get(3).substring(2)));
						b.setType(Material.AIR);
						Main.bmbs.remove(new BlockPosition(b.getX(), b.getY(), b.getZ()));
						PacketUtils.sendTtlSbTtl(p, "§9鉨", "§3§lThe bomb has been defused!", 50);
					} 
				} else {
					final List<String> lr = e.getClickedInventory().getItem(4).getItemMeta().getLore();
					for (final SmplLoc s : Main.bmbs) {
						if (s.getX() == Integer.parseInt(lr.get(1).substring(2)) && s.getY() == Integer.parseInt(lr.get(2).substring(2)) && s.getZ() == Integer.parseInt(lr.get(3).substring(2))) {
							s.cnt = (short) (s.cnt - 20);
							if (s.cnt <= 0) {
								s.expldBmb();
							}
						} 
					} 
					p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_HURT_SWEET_BERRY_BUSH, 1f, 2f);
				} 
			} 
		} else {
			e.setCancelled((e.getWhoClicked().getGameMode() != GameMode.CREATIVE));
		} 
	}
   
	private void addSetItm(final PlayerInventory inv, final int slt, final ItemStack it) {
		final ItemStack s = inv.getItem(slt);
		if (s != null && s.getType() == it.getType()) {
			s.setAmount(s.getAmount() == 1 ? (s.getAmount() + 1) : s.getAmount());
		} else {
			inv.setItem(slt, it);
		} 
	}
}