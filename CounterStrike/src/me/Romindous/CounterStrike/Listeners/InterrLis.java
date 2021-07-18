package me.Romindous.CounterStrike.Listeners;

import me.Romindous.CounterStrike.Main;
import me.Romindous.CounterStrike.Enums.GunType;
import me.Romindous.CounterStrike.Enums.NadeType;
import me.Romindous.CounterStrike.Objects.Nade;
import me.Romindous.CounterStrike.Objects.Shooter;
import me.Romindous.CounterStrike.Objects.SmplLoc;
import me.Romindous.CounterStrike.Objects.TripWire;
import me.Romindous.CounterStrike.Utils.Inventories;
import me.Romindous.CounterStrike.Utils.PacketUtils;

import java.util.HashSet;
import java.util.Iterator;
import net.minecraft.core.BlockPosition;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.metadata.FixedMetadataValue;

public class InterrLis implements Listener {
	
	public static final HashSet<LivingEntity> ents = new HashSet<>();
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onInter(final PlayerInteractEvent e) {
		final Block b;
		final ItemStack it = e.getItem();
		final GunType gt = GunType.getGnTp(it);
		final NadeType nt = NadeType.getNdTp(it);
		switch (e.getAction()) {
		case LEFT_CLICK_AIR:
		case LEFT_CLICK_BLOCK:
			if (gt != null) {
				e.setCancelled(true);
				Shooter sh = Shooter.getPlShtr(e.getPlayer().getName());
				if ((!sh.is || it.getAmount() == 1) && !((Damageable)it.getItemMeta()).hasDamage()) {
					Main.setDmg(it, 0);
					sh.cnt = 0;
				}
			}else if (nt != null) {
				e.getPlayer().setExp((e.getPlayer().getExp() == 1f) ? 0f : (e.getPlayer().getExp() + 0.5F));
			}
			break;
		case RIGHT_CLICK_AIR:
		case RIGHT_CLICK_BLOCK:
			e.setCancelled(true);
			final Player p = e.getPlayer();
			if (gt != null) {
   			   final Shooter sh = Shooter.getPlShtr(p.getName());
   			   final boolean hd = ((Damageable)it.getItemMeta()).hasDamage();
   			   if (!sh.is) {
   				   sh.is = true;
   				   if (sh.inv != p.getInventory()) {
   					   sh.inv = p.getInventory();
   				   }
   				   if ((sh.cnt == 0 || hd) && sh.cld == 0) {
   					   if (it.getAmount() == 1) {
   						   if (hd) {
   							   return;
   						   }
   						   Main.setDmg(it, 0);
   						   sh.cnt = 0;
   					   }else {
   						   if (hd) {
   							   Main.setDmg(it, it.getType().getMaxDurability());
   							   sh.cnt = 0;
   						   }
   						   it.setAmount(it.getAmount() - 1);
   					   }
   					   sh.cld = gt.cld;
   					   final boolean iw = gt.snp && p.isSneaking();
   					   if (iw) {
   						   PacketUtils.fkHlmtClnt(p, p.getInventory().getHelmet());
   						   PacketUtils.zoom(p, false);
   						   p.setSneaking(false);
   					   }
   					   for (byte i = gt.brst == 0 ? 1 : gt.brst; i > 0; i--) {
   						   sh.shoot(ents, gt, Main.plug, p, !iw);
   					   }
   					   p.setVelocity(p.getVelocity().subtract(p.getEyeLocation().getDirection().multiply(gt.kb)));
   				   }
   			   }
			}else if (nt != null) {
				b = e.getClickedBlock();
				if (b != null && b.getType() == Material.TRIPWIRE && !nt.prm) {
					e.setCancelled(true);
					for (final TripWire tn : Main.tws) {
						for (final Block bl : tn.bs) {
							if (b.getX() == bl.getX() && b.getY() == bl.getY() && b.getZ() == bl.getZ()) {
								tn.nt = nt;
								it.setAmount(it.getAmount() - 1);
								p.sendMessage(Main.prf() + "§7Вы зарядили растяжку гранатой!");
								p.getWorld().playSound(b.getLocation(), Sound.ITEM_CROSSBOW_LOADING_END, 1f, 1f);
								return;
							}
						}
					}
				}
				final Snowball sb = (Snowball) p.launchProjectile(Snowball.class);
				sb.setItem(it);
				it.setAmount(it.getAmount() - 1);
				p.getInventory().setItemInMainHand(it);
				sb.setVelocity(sb.getVelocity().multiply(0.75f * p.getExp() + 0.25f));
				Main.nades.add(new Nade(sb, nt.flrbnc ? (byte)(int)(10f * p.getExp() + 5f) : 40));
			}else if (it != null) {
				b = e.getClickedBlock();
				e.setCancelled((e.getPlayer().getGameMode() != GameMode.CREATIVE));
				switch (it.getType()) {
				case GHAST_TEAR:
					final Inventory inv = Bukkit.createInventory(p, 54, "§5Магазин");
					inv.setContents(Inventories.fillShpInv());
					p.openInventory(inv);
					break;
				case GOLDEN_APPLE:
					e.setCancelled(false);
					break;
				case GOLD_NUGGET:
				case SHEARS:
					final boolean bg = (it.getType() == Material.GOLD_NUGGET);
					if (b != null && b.getType() == Material.CRIMSON_BUTTON && Main.bmbs.contains(new BlockPosition(b.getX(), b.getY(), b.getZ()))) {
						e.setCancelled(true);
						final Inventory def = Bukkit.createInventory(p, bg ? 54 : 27, "§3§lРазминировка бомбы");
						p.getWorld().playSound(p.getLocation(), Sound.BLOCK_BEEHIVE_SHEAR, 2f, 0.5f);
						p.setMetadata("dfs", new FixedMetadataValue(Main.plug, b));
						def.setContents(Inventories.fillDefInv(b, bg));
						p.openInventory(def);
					}
					break;
				case SUGAR:
					if (b != null) {
						switch (e.getBlockFace()) {
						case EAST:
						case WEST:
						case NORTH:
						case SOUTH:
							if (crtTrpwr(b, e.getBlockFace(), p)) {
								it.setAmount(0);
							}
							break;
						default:
							p.sendMessage(Main.prf() + "§cРастяжку можно закреплять только на стены!");
							break;
						}
					}
					break;
				default:
					break;
				}
			}
			break;   
		case PHYSICAL:
			b = e.getClickedBlock();
			if (b != null && b.getType() == Material.TRIPWIRE) {
				e.setCancelled(true);
				final Iterator<TripWire> i = Main.tws.iterator();
				while (i.hasNext()) {
					final TripWire tn = i.next();
					for (Block bl : tn.bs) {
						if (b.getX() == bl.getX() && b.getY() == bl.getY() && b.getZ() == bl.getZ()) {
							for (final Block r : tn.bs) {
								r.setType(Material.AIR, false);
							}
							tn.trgr(e.getPlayer(), bl.getLocation().add(0.5d, 0d, 0.5d));
							i.remove();
							return;
						}
					}
				}
			}
			break;
		}
	}
   
	@EventHandler
	public void onPlc(final BlockPlaceEvent e) {
		final Player p = e.getPlayer();
		if (Main.plnts.containsKey(p)) {
			Main.bmbs.add(new SmplLoc(e.getBlock(), (short) 40));
			Main.plnts.remove(p);
			PacketUtils.sendSbTtl(p, "§c§lThe bomb has been planted!", 50);
			return;
		}
		e.setCancelled((p.getGameMode() != GameMode.CREATIVE));
	}
   
	@EventHandler
	public void onEat(final PlayerItemConsumeEvent e) {
		final Player p = e.getPlayer();
		if (e.getItem().getType() == Material.GOLDEN_APPLE) {
			e.setCancelled(true);
			p.getInventory().setItemInMainHand(new ItemStack(Material.CRIMSON_BUTTON));
			Main.plnts.put(p, p.getLocation());
			PacketUtils.sendAcBr(p, "§d§lВыбирите место для установки бомбы...", 30);
		}
	}
   
	public boolean crtTrpwr(final Block b, final BlockFace bf, final Player p) {
		if (b.getType().isSolid()) {
			final byte r = 5;
			final Block[] twbs = new Block[r];
			for (byte i = 0; i < r + 1; i++) {
				final Block t = b.getRelative(bf, i + 1);
				if (t.getType().isAir()) {
					if (i == r) {
						p.sendMessage(Main.prf() + "§cРастяжка не может тянутся настолько далеко!");
						return false;
					}
					twbs[i] = t;
				}else {
					if (i == 0) {
						p.sendMessage(Main.prf() + "§cСтавьте растяжку на открытое место!");
						return false;
					}
					if (t.getType().isOccluding()) {
						Main.tws.add(new TripWire(twbs, p.getName(), bf, i));
						p.getWorld().playSound(b.getLocation(), Sound.ITEM_CROSSBOW_QUICK_CHARGE_1, 2f, 1f);
						p.sendMessage(Main.prf() + "§7Растяжка поставлена, \n§7можете прицепить на нее гранату!");
						return true;
					}
					p.sendMessage(Main.prf() + "§cРастяжку можно крепить только на целые блоки!");
					return false;
				}
			}
			return false;
		}
		p.sendMessage(Main.prf() + "§cРастяжку можно крепить только на целые блоки!");
		return false;
	}
}