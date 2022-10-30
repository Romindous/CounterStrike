package me.Romindous.CounterStrike.Listeners;

import org.bukkit.EntityEffect;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
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

import me.Romindous.CounterStrike.Main;
import me.Romindous.CounterStrike.Enums.GunType;
import me.Romindous.CounterStrike.Enums.NadeType;
import me.Romindous.CounterStrike.Game.Arena;
import me.Romindous.CounterStrike.Game.Arena.Team;
import me.Romindous.CounterStrike.Game.Defusal;
import me.Romindous.CounterStrike.Game.Gungame;
import me.Romindous.CounterStrike.Game.Invasion;
import me.Romindous.CounterStrike.Objects.EntityShootAtEntityEvent;
import me.Romindous.CounterStrike.Objects.Nade;
import me.Romindous.CounterStrike.Objects.Shooter;
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
						final Shooter sh = Shooter.getShooter(dmgr.getName());
						//if player shoots living
						if (sh.arena != null) {
							if (ee instanceof EntityShootAtEntityEvent) {
								//need to deal damage
								final EntityShootAtEntityEvent eee = (EntityShootAtEntityEvent) ee;
								final GunType gt = GunType.getGnTp(dmgr.getInventory().getItemInMainHand());
								if (eee.hst) {
									ApiOstrov.addStat(dmgr, Stat.CS_hshot);
								}
								
								if (e.getEntityType() == EntityType.PLAYER) {
									final Shooter tgt = Shooter.getShooter(ent.getName());
									prcDmg(ent, tgt, sh, e.getDamage(), 
									ent.getHealth() - e.getDamage() <= 0 ? sh.arena.getShtrNm(sh) + " " + (eee.wb ? "§e\u9290" : "") + 
									(ent.getLocation().getBlock().getType() == Material.POWDER_SNOW ? "§7\u9292" : "") + 
									(dmgr.hasPotionEffect(PotionEffectType.BLINDNESS) ? "§b\u9293" : "") + 
									gt.icn + 
									(eee.nscp ? "§d\u9294" : "") + 
									(eee.hst ? "§6\u9291" : "") + 
									" " + sh.arena.getShtrNm(tgt) : null, 
										(byte) 0, gt.rwd);
								} else {
									prcDmg(ent, null, sh, e.getDamage(), 
									ent.getHealth() - e.getDamage() <= 0 ? sh.arena.getShtrNm(sh) + " " + (eee.wb ? "§e\u9290" : "") + 
									(ent.getLocation().getBlock().getType() == Material.POWDER_SNOW ? "§7\u9292" : "") + 
									(dmgr.hasPotionEffect(PotionEffectType.BLINDNESS) ? "§b\u9293" : "") + 
									gt.icn + 
									(eee.nscp ? "§d\u9294" : "") + 
									(eee.hst ? "§6\u9291" : "") + 
									" " + Team.NA.clr + ent.getName() : null, 
										(byte) 0, gt.rwd);
								}
								
								
							} else {
								e.setCancelled(true);
								//if player hits living
								switch (dmgr.getInventory().getItemInMainHand().getType()) {
								case BONE:
								case BLAZE_ROD:
									if (ent.getNoDamageTicks() == 0) {
										e.setDamage(ent.getEquipment().getChestplate() == null ? 3d : 2d);
										
										if (e.getEntityType() == EntityType.PLAYER) {
											final Shooter tgt = Shooter.getShooter(ent.getName());
											prcDmg(ent, tgt, sh, e.getDamage(), 
											ent.getHealth() - e.getDamage() <= 0 ? sh.arena.getShtrNm(sh) + " §f\u9298 " + sh.arena.getShtrNm(tgt) : null, 
												(byte) 5, (short) 300);
											Main.dmgArm(dmgr, ent.getEyeLocation(), "§6" + String.valueOf((int) e.getDamage() * 5));
										} else {
											prcDmg(ent, null, sh, e.getDamage(), 
											ent.getHealth() - e.getDamage() <= 0 ? sh.arena.getShtrNm(sh) + " §f\u9298 " + Team.NA.clr + ent.getName() : null, 
												(byte) 5, (short) 300);
											Main.dmgArm(dmgr, ent.getEyeLocation(), "§6" + String.valueOf((int) e.getDamage() * 5));
										}
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
						final Invasion ar = Invasion.getMobInvasion(ee.getDamager().getEntityId());
						if (ar != null && e.getEntityType() == EntityType.PLAYER) {
							e.setCancelled(true);
							
							e.setDamage(ent.getEquipment().getChestplate() == null ? e.getDamage() : e.getDamage() * 0.6);
							e.setDamage(ent.getEquipment().getHelmet() == null ? e.getDamage() : e.getDamage() * 0.8);
							final Shooter tgt = Shooter.getShooter(ent.getName());
							prcDmg(ent, tgt, null, e.getDamage(), 
							ent.getHealth() - e.getDamage() <= 0 ? Team.NA.clr + ee.getDamager().getName() + " §f\u929a " + ar.getShtrNm(tgt) : null, 
								(byte) 5, (short) 0);
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
			e.setDamage(2d);
			e.setCancelled(true);
			if (!e.getEntity().isInvulnerable()) {
				final LivingEntity ent = (LivingEntity) e.getEntity();
				if (e.getEntityType() == EntityType.PLAYER) {
					final Shooter sh = Shooter.getShooter(ent.getName());
					if (sh.arena != null) {
						if (sh.arena != null) {
							prcDmg(ent, sh, null, e.getDamage(), 
							ent.getHealth() - e.getDamage() <= 0 ? "§f\u9295 " + sh.arena.getShtrNm(sh) : null, 
								(byte) 5, (short) 0);
						}
					}
				} else if (ent instanceof Mob) {
					prcDmg(ent, null, null, e.getDamage(), 
					ent.getHealth() - e.getDamage() <= 0 ? "" : null, 
						(byte) 5, (short) 0);
				}
			}
			break;
		case FALL:
			e.setCancelled(true);
			if (e.getEntityType() == EntityType.PLAYER && !e.getEntity().isInvulnerable()) {
				if (e.getDamage() > 2d) {
					final LivingEntity ent = (LivingEntity) e.getEntity();
					final Shooter sh = Shooter.getShooter(ent.getName());
					if (sh.arena != null) {
						prcDmg(ent, sh, null, e.getDamage(), 
						ent.getHealth() - e.getDamage() <= 0 ? "§f\u9296\u9297 " + sh.arena.getShtrNm(sh) : null, 
							(byte) 2, (short) 0);
					}
				}
			}
			break;
		default:
			break;
		}
	}

	public static void prcDmg(final LivingEntity target, final Shooter tgtsh, final Shooter damager, final double dmg, final String klfd, final byte dts, final short klrwd) {

		if (klfd == null) {//if entity isnt dying
			if (tgtsh == null) {//target is not player
				target.setHealth(target.getHealth() - dmg);
				target.playEffect(EntityEffect.HURT);
				target.setNoDamageTicks(dts);
				if (target instanceof Mob && damager != null) {
					((Mob) target).setTarget(damager.inv.getHolder());
				}
			} else {//target is player
				if (tgtsh.arena != null) {
					if (damager == null) {//damager is not player
						target.setHealth(target.getHealth() - dmg);
						target.playEffect(EntityEffect.HURT);
						target.setNoDamageTicks(dts);
					} else {//damager is player
						switch (tgtsh.arena.gst) {
						case ROUND:
							if (!(tgtsh.arena instanceof Gungame) && tgtsh.arena.isSmTm(damager, tgtsh)) {
								break;
							}
						case BEGINING:
							target.setHealth(target.getHealth() - dmg);
							target.playEffect(EntityEffect.HURT);
							target.setNoDamageTicks(dts);
							break;
						default:
							break;
						}
					}
				}
			}
		} else {
			if (tgtsh == null) {//target is not player
				final Invasion inv = Invasion.getMobInvasion(target.getEntityId());
				if (inv != null) {
					switch (inv.gst) {
					case ROUND:
						if (damager != null) {
							inv.addMbKll(damager);
							inv.chngMn(damager, klrwd * getMbRwd(target.getName().charAt(0)) / 100);
						}
					case BEGINING:
						inv.TMbs.remove(target.getEntityId());
						target.getWorld().spawnParticle(Particle.SOUL, target.getLocation().add(0, 1d, 0), 40, 0.5D, 0.5D, 0.5D, 0.0D, null, false);
						target.getWorld().playSound(target.getLocation(), Sound.BLOCK_ENDER_CHEST_OPEN, 10f, 2f);
						target.remove();
					default:
						break;
					}
				}
			} else {//target is player
				final Arena ar = tgtsh.arena;
				if (ar != null) {
					if (damager == null) {//damager is not player
						switch (ar.gst) {
						case ROUND:
						case BEGINING:
							for (final Player p : target.getWorld().getPlayers()) {
								p.sendMessage(klfd);
							}
							target.getWorld().spawnParticle(Particle.SOUL, target.getLocation().add(0, 1d, 0), 40, 0.5D, 0.5D, 0.5D, 0.0D, null, false);
							target.getWorld().playSound(target.getLocation(), Sound.BLOCK_ENDER_CHEST_OPEN, 10f, 2f);
							ar.killPl(tgtsh);
							break;
						default:
							break;
						}
					} else {//damager is player
						switch (ar.gst) {
						case ROUND:
							if (!(ar instanceof Gungame) && ar.isSmTm(damager, tgtsh)) {
								break;
							}
							ar.addKll(damager);
							if (ar instanceof Defusal) {
								ar.chngMn(damager, klrwd);
							}
						case BEGINING:
							for (final Player p : target.getWorld().getPlayers()) {
								p.sendMessage(klfd);
							}
							target.getWorld().spawnParticle(Particle.SOUL, target.getLocation().add(0, 1d, 0), 40, 0.5D, 0.5D, 0.5D, 0.0D, null, false);
							target.getWorld().playSound(target.getLocation(), Sound.BLOCK_ENDER_CHEST_OPEN, 10f, 2f);
							ar.killPl(tgtsh);
							break;
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
			final Snowball sb = (Snowball) e.getEntity();
			final NadeType nt = NadeType.getNdTp(sb.getItem());
			if (nt == null || !(sb.getShooter() instanceof LivingEntity)) {
				return;
			}
			if (e.getHitEntity() != null) {
				Nade.expld(sb, (Player) e.getEntity().getShooter());
				e.setCancelled(true);
				return;
			} 
			final Vector vec = sb.getVelocity();
			final Snowball nv = (Snowball) sb.getWorld().spawnEntity(sb.getLocation(), EntityType.SNOWBALL);
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
					nv.setGravity(false);
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
       
			nv.setItem(sb.getItem());
			vec.multiply(0.6F);
			nv.setVelocity(vec);
			nv.setShooter(sb.getShooter());
			Nade.chngNd(sb, nv);
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