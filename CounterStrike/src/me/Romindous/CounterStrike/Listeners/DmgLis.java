package me.Romindous.CounterStrike.Listeners;

import me.Romindous.CounterStrike.Main;
import me.Romindous.CounterStrike.Enums.GunType;
import me.Romindous.CounterStrike.Enums.NadeType;
import me.Romindous.CounterStrike.Game.Arena;
import me.Romindous.CounterStrike.Objects.EntityShootAtEntityEvent;
import me.Romindous.CounterStrike.Objects.Nade;
import me.Romindous.CounterStrike.Objects.Shooter;
import me.Romindous.CounterStrike.Utils.PacketUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.projectile.EntitySnowball;

import org.bukkit.EntityEffect;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.mojang.datafixers.util.Pair;

public class DmgLis implements Listener {
	
	@EventHandler
	public void onDmg(final EntityDamageEvent e) {
		switch (e.getCause()) {
		case ENTITY_ATTACK:
			if (e instanceof EntityDamageByEntityEvent) {
				final EntityDamageByEntityEvent ee = (EntityDamageByEntityEvent) e;
				if (e.getEntity() instanceof LivingEntity && !e.getEntity().isInvulnerable()) {
					final LivingEntity ent = (LivingEntity) e.getEntity();
					if (ee.getDamager().getType() == EntityType.PLAYER) {
						final Player dmgr = (Player) ee.getDamager();
						final Pair<Shooter, Arena> pr = Shooter.getPlShtrArena(dmgr.getName());
						//if player shoots living
						if (pr.getSecond() != null) {
							if (ee instanceof EntityShootAtEntityEvent) {
								//need to deal damage
								final EntityShootAtEntityEvent eee = (EntityShootAtEntityEvent) ee;
								final GunType gt = GunType.getGnTp(dmgr.getInventory().getItemInMainHand());
								if (ent.getHealth() - e.getDamage() <= 0) {
									dmgr.playSound(dmgr.getLocation(), Sound.BLOCK_ENDER_CHEST_OPEN, 1f, 2f);
									
									prcDmg(ent, pr, e.getDamage(), pr.getSecond().getShtrNm(dmgr.getName()) + " " + (eee.wb ? "§e\u9290" : "") + 
										(ent.getLocation().getBlock().getType() == Material.POWDER_SNOW ? "§7\u9292" : "") + 
										(ent.hasPotionEffect(PotionEffectType.BLINDNESS) ? "§b\u9293" : "") + 
										gt.icn + 
										(eee.nscp ? "§d\u9294" : "") + 
										(eee.hst ? "§6\u9291" : "") + 
										" " + pr.getSecond().getShtrNm(ent.getName()), (byte) 0, gt.rwd);
									
								} else {
									prcDmg(ent, pr, e.getDamage(), null, (byte) 0, gt.rwd);
								}
							} else {
								e.setCancelled(true);
								//if player hits living
								switch (dmgr.getInventory().getItemInMainHand().getType()) {
								case BONE:
								case BLAZE_ROD:
									if (ent.getNoDamageTicks() == 0) {
										e.setDamage(ent.getEquipment().getChestplate() == null ? 3d : 2d);
										prcDmg(ent, pr, e.getDamage(), ent.getHealth() - e.getDamage() <= 0 ? 
											pr.getSecond().getShtrNm(dmgr.getName()) + " §f\u9298 " + pr.getSecond().getShtrNm(ent.getName()) 
											: null, (byte) 5, (short) 300);
										Main.dmgArm(dmgr, ent.getEyeLocation(), "§6" + String.valueOf((int) e.getDamage() * 5));
									}
									break;
								default:
									break;
								}
							}
						} else {
							e.setCancelled(dmgr.getGameMode() != GameMode.CREATIVE);
							if (!(ee instanceof EntityShootAtEntityEvent) && e.getDamage() == 1d) {
								Main.dmgArm(dmgr, ent.getEyeLocation(), "§6" + String.valueOf((ent.getEquipment().getChestplate() == null ? 4 : 2) * 5));
							}
						}
					}
				}
			}
			break;
		case FIRE_TICK:
			e.getEntity().setFireTicks(0);
		case ENTITY_EXPLOSION:
			e.setCancelled(true);
			break;
		case FIRE:
			e.setCancelled(true);
			if (e.getEntity() instanceof LivingEntity && !e.getEntity().isInvulnerable() && e.getEntityType() == EntityType.PLAYER) {
				final LivingEntity ent = (LivingEntity) e.getEntity();
				final Pair<Shooter, Arena> pr = Shooter.getPlShtrArena(ent.getName());
				if (pr.getSecond() != null) {
					e.setDamage(2d);
					prcDmg(ent, pr, e.getDamage(), ent.getHealth() - e.getDamage() <= 0 ? 
						"§f\u9295 " + pr.getSecond().getShtrNm(ent.getName()) 
						: null, (byte) 5, (short) 0);
				}
			}
			break;
		case FALL:
			e.setCancelled(true);
			if (e.getEntity() instanceof LivingEntity && !e.getEntity().isInvulnerable() && e.getEntityType() == EntityType.PLAYER) {
				if (e.getDamage() > 2d) {
					final LivingEntity ent = (LivingEntity) e.getEntity();
					final Pair<Shooter, Arena> pr = Shooter.getPlShtrArena(ent.getName());
					if (pr.getSecond() != null) {
						prcDmg(ent, pr, e.getDamage(), ent.getHealth() - e.getDamage() <= 0 ? 
							"§f\u9296\u9297 " + pr.getSecond().getShtrNm(ent.getName()) 
							: null, (byte) 2, (short) 0);
					}
				}
			}
			break;
		default:
			break;
		}
	}
	
	public static void prcDmg(final LivingEntity ent, final Pair<Shooter, Arena> pr, final double dmg, final String klfd, final byte dts, final short klrwd) {
		if (klfd == null) { 
			if (ent.getType() == EntityType.PLAYER) {
				switch (pr.getSecond().gst) {
				case ROUND:
					if (!pr.getFirst().nm.equals(ent.getName()) && pr.getSecond().isSmTm(pr.getFirst(), ent.getName())) {
						break;
					}
				case BEGINING:
					ent.setHealth(ent.getHealth() - dmg);
					ent.playEffect(EntityEffect.HURT);
					ent.setNoDamageTicks(dts);
					break;
				default:
					break;
				}
			} else {
				ent.setHealth(ent.getHealth() - dmg);
				ent.playEffect(EntityEffect.HURT);
				ent.setNoDamageTicks(dts);
			}
		} else {
			if (ent.getType() == EntityType.PLAYER) {
				final Arena ar = pr.getSecond();
				switch (ar.gst) {
				case ROUND:
					if (!pr.getFirst().nm.equals(ent.getName()) && ar.isSmTm(pr.getFirst(), ent.getName())) {
						break;
					}
					if (klrwd != 0) {
						pr.getSecond().addKll(pr.getFirst());
						ar.chngMn(pr.getFirst(), klrwd);
					}
				case BEGINING:
					for (final Player p : ent.getWorld().getPlayers()) {
						p.sendMessage(klfd);
					}
					ar.killPl(ar.getShtr(ent.getName()));
					break;
				default:
					break;
				}
			} else {
				for (final Player p : ent.getWorld().getPlayers()) {
					p.sendMessage(klfd);
				}
				ent.remove();
			}
		}
	}
   
	@EventHandler
	public void onHit(final ProjectileHitEvent e) {
		if (e.getEntityType() == EntityType.SNOWBALL) {
			final Snowball sb = (Snowball)e.getEntity();
			final NadeType nt = NadeType.getNdTp(sb.getItem());
			if (nt == null) {
				return;
			}
			if (e.getHitEntity() != null) {
				Nade.expld(sb, (Player) e.getEntity().getShooter());
				return;
			} 
			Vector vec = sb.getVelocity();
			EntitySnowball nv = new EntitySnowball(EntityTypes.aG, PacketUtils.getNMSWrld(sb.getWorld()));
			switch (e.getHitBlockFace()) {
			case NORTH:
			case SOUTH:
				vec.setZ(-vec.getZ());
				break;
			case EAST:
			case WEST:
				vec.setX(-vec.getX());
				break;
			case UP:
				if (!nt.flrbnc) {
					sb.getWorld().playSound(sb.getLocation(), Sound.BLOCK_GLASS_BREAK, 2f, 0.8f);
					Nade.expld(sb, (Player) sb.getShooter()); 
					return;
				} 
				if (nt == NadeType.SMOKE && sb.getLocation().getBlock().getType() == Material.FIRE) {
					sb.getWorld().playSound(sb.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 2f, 0.8f);
					Nade.expld(sb, (Player) sb.getShooter());
					return;
				} 
				if (vec.length() < 0.3D) {
					vec.setX(0);
					vec.setY(0);
					vec.setZ(0);
					nv.setNoGravity(true);
				} else {
					vec.setY(-vec.getY());
				}
				break;
         
			case DOWN:
				vec.setY(-vec.getY());
				break;
			default:
				break;
			} 
       
			nv.setItem(PacketUtils.getNMSIt(sb.getItem()));
			nv.setPosition(sb.getLocation().getX(), sb.getLocation().getY(), sb.getLocation().getZ());
			vec.multiply(0.6F);
			nv.setMot(vec.getX(), vec.getY(), vec.getZ());
			nv.setShooter((Entity)PacketUtils.getNMSPlr((Player) sb.getShooter()));
			PacketUtils.getNMSWrld(sb.getWorld()).addEntity(nv);
			Nade.chngNd(sb, (Snowball) nv.getBukkitEntity());
			sb.remove();
		}
	}
}