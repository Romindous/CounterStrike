package me.Romindous.CounterStrike.Listeners;

import me.Romindous.CounterStrike.Main;
import me.Romindous.CounterStrike.Enums.GunType;
import me.Romindous.CounterStrike.Enums.NadeType;
import me.Romindous.CounterStrike.Enums.GameState;
import me.Romindous.CounterStrike.Game.Arena;
import me.Romindous.CounterStrike.Game.Arena.Team;
import me.Romindous.CounterStrike.Game.Defusal;
import me.Romindous.CounterStrike.Objects.Nade;
import me.Romindous.CounterStrike.Objects.Shooter;
import me.Romindous.CounterStrike.Objects.TripWire;
import me.Romindous.CounterStrike.Utils.Inventories;
import me.Romindous.CounterStrike.Utils.PacketUtils;

import java.util.HashSet;
import java.util.Iterator;

import net.minecraft.core.BaseBlockPosition;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
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
import com.mojang.datafixers.util.Pair;

public class InterrLis implements Listener {
	
	public static final HashSet<LivingEntity> ents = new HashSet<>();
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onInter(final PlayerInteractEvent e) {
		final Block b;
		final ItemStack it = e.getItem();
		final GunType gt = GunType.getGnTp(it);
		final NadeType nt = NadeType.getNdTp(it);
		final Pair<Shooter, Arena> pr;
		switch (e.getAction()) {
		case LEFT_CLICK_AIR:
		case LEFT_CLICK_BLOCK:
			if (gt != null) {
				e.setCancelled(true);
				Shooter sh = Shooter.getPlShtrArena(e.getPlayer().getName()).getFirst();
				if ((!sh.is || it.getAmount() == 1) && !((Damageable)it.getItemMeta()).hasDamage()) {
					Main.setDmg(it, 0);
					sh.cnt = 0;
				}
			} else if (nt != null) {
				e.getPlayer().setExp((e.getPlayer().getExp() == 1f) ? 0f : (e.getPlayer().getExp() + 0.5F));
			}
			break;
		case RIGHT_CLICK_AIR:
		case RIGHT_CLICK_BLOCK:
			e.setCancelled(true);
			final Player p = e.getPlayer();
			if (gt != null) {
				final Shooter sh = Shooter.getPlShtrArena(p.getName()).getFirst();
				final boolean hd = ((Damageable)it.getItemMeta()).hasDamage();
				if (!sh.is) {
					sh.is = true;
					if ((sh.cnt == 0 || hd) && sh.cld == 0) {
						if (it.getAmount() == 1) {
							if (hd) {
								return;
							}
							Main.setDmg(it, 0);
							sh.cnt = 0;
						} else {
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
			} else if (nt != null) {
				pr = Shooter.getPlShtrArena(p.getName());
				if (pr.getSecond() != null) {
					switch (pr.getSecond().gst) {
					case BUYTIME:
					case ENDRND:
					case FINISH:
						return;
					default:
						break;
					}
				}
				b = e.getClickedBlock();
				if (b != null && b.getType() == Material.TRIPWIRE && !nt.prm) {
					e.setCancelled(true);
					for (final Arena ar : Main.actvarns) {
						for (final TripWire tn : ar.tws) {
							for (final Block bl : tn.bs) {
								if (b.getX() == bl.getX() && b.getY() == bl.getY() && b.getZ() == bl.getZ()) {
									if (ar.name.equals(pr.getSecond().name) && ar.shtrs.get(pr.getFirst()) == tn.tm) {
										tn.chngNt(pr.getFirst().inv.getItemInMainHand(), ar);
										it.setAmount(it.getAmount() - 1);
										p.sendMessage(Main.prf() + "§7Вы зарядили растяжку гранатой!");
										p.getWorld().playSound(b.getLocation(), Sound.ITEM_CROSSBOW_LOADING_END, 1f, 1f);
										return;
									}
								}
							}
						}
					}
				}
				final Snowball sb = (Snowball) p.launchProjectile(Snowball.class);
				sb.setItem(it);
				it.setAmount(it.getAmount() - 1);
				p.getInventory().setItemInMainHand(it);
				sb.setVelocity(sb.getVelocity().multiply(0.75f * p.getExp() + 0.25f));
				Main.nades.add(new Nade(sb, (byte) (nt.flrbnc ? 10f * p.getExp() + 5f : 40)));
			} else if (it != null) {
				b = e.getClickedBlock();
				e.setCancelled((e.getPlayer().getGameMode() != GameMode.CREATIVE));
				switch (it.getType()) {
				case GHAST_TEAR:
					pr = Shooter.getPlShtrArena(p.getName());
					if (pr.getSecond() == null) {
						p.openInventory(Inventories.LBShop);
					} else {
						final Team tm = pr.getSecond().shtrs.get(pr.getFirst());
						switch (pr.getSecond().gst) {
						case BEGINING:
						case WAITING:
							p.openInventory(Inventories.LBShop);
							break;
						case BUYTIME:
							switch (tm) {
							case Ts:
								p.openInventory(Inventories.TShop);
								break;
							case CTs:
								p.openInventory(Inventories.CTShop);
								break;
							case NA:
								p.openInventory(Inventories.LBShop);
								break;
							}
							break;
						case ROUND:
							if (pr.getSecond() instanceof Defusal && ((Defusal) pr.getSecond()).isBmbOn()) {
								PacketUtils.sendAcBr(p, "§c§lВремя закупки вышло, бомба постовлена!", 30);
							} else if (pr.getSecond().canOpnShp(p.getLocation(), tm)) {
								switch (tm) {
								case Ts:
									p.openInventory(Inventories.TShop);
									break;
								case CTs:
									p.openInventory(Inventories.CTShop);
									break;
								case NA:
									p.openInventory(Inventories.LBShop);
									break;
								}
							} else {
								PacketUtils.sendAcBr(p, "§c§lЗакупатся можно только на спавне!", 30);
							}
							break;
						case ENDRND:
						case FINISH:
							break;
						}
					}
					break;
				case GOLDEN_APPLE:
				case CRIMSON_BUTTON:
					e.setCancelled(false);
					break;
				case STICK:
					final Inventory df = Bukkit.createInventory(null, 54, "§3§lРазминировка бомбы");
					p.getWorld().playSound(p.getLocation(), Sound.BLOCK_BEEHIVE_SHEAR, 2f, 0.5f);
					df.setContents(Inventories.fillDefInv(b, (byte) 54));
					p.openInventory(df);
					break;
				case GOLD_NUGGET:
				case SHEARS:
					final boolean bg = it.getType() == Material.GOLD_NUGGET;
					if (b != null && b.getType() == Material.CRIMSON_BUTTON) {
						e.setCancelled(true);
						final Inventory def = Bukkit.createInventory(null, bg ? 54 : 27, "§3§lРазминировка бомбы");
						p.getWorld().playSound(p.getLocation(), Sound.BLOCK_BEEHIVE_SHEAR, 2f, 0.5f);
						def.setContents(Inventories.fillDefInv(b, (byte) (bg ? 54 : 27)));
						p.openInventory(def);
					}
					break;
				case SUGAR:
					if (b != null) {
						pr = Shooter.getPlShtrArena(p.getName());
						if (pr.getSecond() != null) {
							switch (pr.getSecond().gst) {
							case BUYTIME:
							case ENDRND:
							case FINISH:
								return;
							default:
								break;
							}
						}
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
				for (final Arena ar : Main.actvarns) {
					final Iterator<TripWire> i = ar.tws.iterator();
					while (i.hasNext()) {
						final TripWire tn = i.next();
						for (final Block bl : tn.bs) {
							if (b.getX() == bl.getX() && b.getY() == bl.getY() && b.getZ() == bl.getZ()) {
								pr = Shooter.getPlShtrArena(e.getPlayer().getName());
								if (ar.name.equals(pr.getSecond().name) && ar.shtrs.get(pr.getFirst()) != tn.tm) {
									for (final Block r : tn.bs) {
										r.setType(Material.AIR, false);
									}
									tn.hdNdAll(ar);
									tn.trgr(e.getPlayer(), bl.getLocation().add(0.5d, 0d, 0.5d));
									i.remove();
								}
								return;
							}
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
		final Arena ar = Shooter.getPlShtrArena(p.getName()).getSecond();
		if (Main.plnts.containsKey(p) && ar != null && ar instanceof Defusal && ar.gst == GameState.ROUND) {
			if (Math.abs(e.getBlockPlaced().getY() - p.getLocation().getBlockY()) < 3) {
				Main.plnts.remove(p);
				((Defusal) ar).plant(e.getBlockPlaced());
				((Defusal) ar).indSts(PacketUtils.getNMSPlr(p).b);
			} else {
            	PacketUtils.sendAcBr(p, "§c§lУ вас не дотягиваются руки!", 20);
            	e.setCancelled(true);
			}
			return;
		}
		e.setCancelled((p.getGameMode() != GameMode.CREATIVE));
	}
   
	@EventHandler
	public void onEat(final PlayerItemConsumeEvent e) {
		e.setCancelled(true);
		final Player p = e.getPlayer();
		final Arena ar = Shooter.getPlShtrArena(p.getName()).getSecond();
		if (e.getItem().getType() == Material.GOLDEN_APPLE && ar != null && ar instanceof Defusal && ar.gst == GameState.ROUND) {
			if (canPlcBmb(p.getLocation(), (Defusal) ar)) {
				p.getInventory().setItemInMainHand(new ItemStack(Material.CRIMSON_BUTTON));
				Main.plnts.put(p, new BaseBlockPosition(p.getLocation().getBlockX(), p.getLocation().getBlockY(), p.getLocation().getBlockZ()));
				PacketUtils.sendAcBr(p, "§d§lВыбирите место для установки бомбы...", 30);
			} else {
            	PacketUtils.sendAcBr(p, "§c§lБомбу можно ставить только на точках!", 20);
			}
		}
	}
   
	private boolean canPlcBmb(final Location loc, final Defusal ar) {
		return (Math.abs(loc.getX() - ar.ast.getX()) < 3 && Math.abs(loc.getY() - ar.ast.getY()) < 3 && Math.abs(loc.getZ() - ar.ast.getZ()) < 3) 
			|| 
			(Math.abs(loc.getX() - ar.bst.getX()) < 3 && Math.abs(loc.getY() - ar.bst.getY()) < 3 && Math.abs(loc.getZ() - ar.bst.getZ()) < 3);
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
						final Pair<Shooter, Arena> pr = Shooter.getPlShtrArena(p.getName());
						if (pr.getSecond() != null) {
							pr.getSecond().tws.add(new TripWire(twbs, pr, bf, i));
							p.getWorld().playSound(b.getLocation(), Sound.ITEM_CROSSBOW_QUICK_CHARGE_1, 2f, 1f);
							p.sendMessage(Main.prf() + "§7Растяжка поставлена, \n§7можете прицепить на нее гранату!");
							return true;
						}
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