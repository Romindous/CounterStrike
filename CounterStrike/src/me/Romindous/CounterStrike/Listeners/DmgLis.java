package me.Romindous.CounterStrike.Listeners;

import java.util.Iterator;
import java.util.UUID;

import org.bukkit.EntityEffect;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
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

import me.Romindous.CounterStrike.Main;
import me.Romindous.CounterStrike.Enums.GunType;
import me.Romindous.CounterStrike.Enums.NadeType;
import me.Romindous.CounterStrike.Game.Arena;
import me.Romindous.CounterStrike.Game.Invasion;
import me.Romindous.CounterStrike.Objects.EntityShootAtEntityEvent;
import me.Romindous.CounterStrike.Objects.Nade;
import me.Romindous.CounterStrike.Objects.Shooter;
import me.Romindous.CounterStrike.Utils.PacketUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.projectile.EntitySnowball;
import ru.komiss77.ApiOstrov;
import ru.komiss77.enums.Stat;

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
								if (eee.hst) {
									ApiOstrov.addStat(dmgr, Stat.CS_hshot);
								}
								
								prcDmg(ent, pr, e.getDamage(), ent.getHealth() - e.getDamage() <= 0 ? pr.getSecond().getShtrNm(dmgr.getName()) + " " + (eee.wb ? "§e\u9290" : "") + 
									(ent.getLocation().getBlock().getType() == Material.POWDER_SNOW ? "§7\u9292" : "") + 
									(dmgr.hasPotionEffect(PotionEffectType.BLINDNESS) ? "§b\u9293" : "") + 
									gt.icn + 
									(eee.nscp ? "§d\u9294" : "") + 
									(eee.hst ? "§6\u9291" : "") + 
									" " + pr.getSecond().getShtrNm(ent.getName()) : null, (byte) 0, gt.rwd);
								
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
					} else {
						final Invasion ar = Invasion.getMobInvasion(ee.getDamager().getUniqueId());
						if (ar != null && e.getEntityType() == EntityType.PLAYER) {
							e.setCancelled(true);
							for (final Shooter sh : ar.shtrs.keySet()) {
								if (sh.nm.equals(ent.getName())) {
									e.setDamage(ent.getEquipment().getChestplate() == null ? e.getDamage() : e.getDamage() * 0.6);
									e.setDamage(ent.getEquipment().getHelmet() == null ? e.getDamage() : e.getDamage() * 0.8);
									prcDmg(ent, new Pair<Shooter, Arena>(sh, ar), e.getDamage(), ent.getHealth() - e.getDamage() <= 0 ? 
										ar.getShtrNm(ee.getDamager().getName()) + " §f\u929a " + ar.getShtrNm(ent.getName()) 
										: null, (byte) 5, (short) 0);
								}
							}
						}
					}
				}
			}
			break;
		case FIRE_TICK:
			e.getEntity().setFireTicks(0);
		case ENTITY_EXPLOSION:
		case FREEZE:
			e.setCancelled(true);
			break;
		case FIRE:
			e.setCancelled(true);
			if ((e.getEntity() instanceof Mob || e.getEntity() instanceof Player) && !e.getEntity().isInvulnerable()) {
				final LivingEntity ent = (LivingEntity) e.getEntity();
				final Arena ar = e.getEntityType() == EntityType.PLAYER ? Shooter.getPlShtrArena(ent.getName()).getSecond() : Invasion.getMobInvasion(e.getEntity().getUniqueId());
				if (ar != null) {
					e.setDamage(2d);
					prcDmg(ent, new Pair<Shooter, Arena>(null, ar), e.getDamage(), ent.getHealth() - e.getDamage() <= 0 ? 
						"§f\u9295 " + ar.getShtrNm(ent.getName()) 
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
					if (pr.getFirst() != null && !pr.getFirst().nm.equals(ent.getName()) && pr.getSecond().isSmTm(pr.getFirst(), ent.getName())) {
						break;
					}
				case BEGINING:
					((HumanEntity) ent).closeInventory();
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
				if (ent instanceof Mob && pr.getFirst() != null) {
					((Mob) ent).setTarget(pr.getFirst().inv.getHolder());
				}
			}
		} else {
        	ent.getWorld().spawnParticle(Particle.SOUL, ent.getLocation().add(0, ent.getBoundingBox().getHeight() * 0.5d, 0), 40, 0.5D, 0.5D, 0.5D, 0.0D, null, false);
        	ent.getWorld().playSound(ent.getLocation(), Sound.BLOCK_ENDER_CHEST_OPEN, 10f, 2f);
			final Arena ar = pr.getSecond();
			if (ent.getType() == EntityType.PLAYER) {
				switch (ar.gst) {
				case ROUND:
					if (!pr.getFirst().nm.equals(ent.getName()) && ar.isSmTm(pr.getFirst(), ent.getName())) {
						break;
					}
					if (klrwd != 0) {
						ar.addKll(pr.getFirst());
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
			} else if (ar instanceof Invasion) {
				final Iterator<UUID> it = ((Invasion) ar).TMbs.iterator();
				while (it.hasNext()) {
					if (it.next().equals(ent.getUniqueId())) {
						switch (ar.gst) {
						case ROUND:
							if (klrwd != 0) {
								((Invasion) ar).addMbKll(pr.getFirst());
								ar.chngMn(pr.getFirst(), klrwd * getMbRwd(ent.getName().charAt(0)) / 100);
							}
							it.remove();
						case BEGINING:
							ent.remove();
						default:
							break;
						}
					}
				}
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
				e.setCancelled(true);
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
	
	public static short getMbRwd(final char l) {
		switch (l) {
		case 'Z':
			return 15;
		case 'S':
			return 20;
		case 'V':
			return 30;
		case 'P':
			return 45;
		default:
			return 0;
		}
	}
}