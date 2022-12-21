package me.Romindous.CounterStrike.Listeners;

import org.bukkit.EntityEffect;
import org.bukkit.GameMode;
import org.bukkit.Location;
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
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
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
import me.Romindous.CounterStrike.Mobs.Mobber;
import me.Romindous.CounterStrike.Objects.EntityShootAtEntityEvent;
import me.Romindous.CounterStrike.Objects.Shooter;
import me.Romindous.CounterStrike.Objects.Game.BtShooter;
import me.Romindous.CounterStrike.Objects.Game.Nade;
import me.Romindous.CounterStrike.Objects.Skins.Quest;
import me.Romindous.CounterStrike.Objects.Skins.SkinQuest;
import ru.komiss77.ApiOstrov;
import ru.komiss77.enums.Stat;
import ru.komiss77.version.VM;

public class DmgLis implements Listener {
	
	@EventHandler
	public void onDmg(final EntityDamageEvent e) {
		switch (e.getCause()) {
		case ENTITY_ATTACK:
			if (e instanceof EntityDamageByEntityEvent) {
				final EntityDamageByEntityEvent ee = (EntityDamageByEntityEvent) e;
				if (e.getEntity() instanceof LivingEntity && ee.getDamager() instanceof LivingEntity && !e.getEntity().isInvulnerable()) {
					final LivingEntity ent = (LivingEntity) e.getEntity();
					final LivingEntity dmgr = (LivingEntity) ee.getDamager();
					final Shooter sh = Shooter.getShooter(dmgr, false);
					if (sh != null) {
						//if player shoots living
						if (sh.arena() == null) {
							if (!(ee instanceof EntityShootAtEntityEvent) && dmgr.getType() == EntityType.PLAYER) {
								e.setCancelled(((HumanEntity) dmgr).getGameMode() != GameMode.CREATIVE);
								Main.dmgArm((Player) dmgr, ent.getEyeLocation(), "§6" + String.valueOf((ent.getEquipment().getChestplate() == null ? 4 : 2) * 5));
							}
						} else {
							if (ee instanceof EntityShootAtEntityEvent) {
								//need to deal damage
								final EntityShootAtEntityEvent eee = (EntityShootAtEntityEvent) ee;
								final GunType gt = GunType.getGnTp(sh.item(EquipmentSlot.HAND));
								if (gt == null) return;
								if (eee.hst && dmgr.getType() == EntityType.PLAYER) {
									ApiOstrov.addStat((Player) dmgr, Stat.CS_hshot);
								}
								
								final Location loc = ent.getLocation();
								prcDmg(ent, Shooter.getShooter(ent, false), 
								sh, e.getDamage(), gt.icn, 0, gt.rwd, dmgr.hasPotionEffect(PotionEffectType.BLINDNESS), 
								VM.getNmsServer().getFastMat(loc.getWorld(), loc.getBlockX(), loc.getBlockY() + 1, loc.getBlockZ()) == Material.POWDER_SNOW, 
								eee.nscp, eee.hst, eee.wb);
							} else {
								e.setCancelled(true);
								//if player hits living
								final ItemStack hnd = sh.item(EquipmentSlot.HAND);
								switch (hnd.getType()) {
								case BONE:
								case BLAZE_ROD:
									if (ent.getNoDamageTicks() == 0) {
										e.setDamage(ent.getEquipment().getChestplate() == null ? 3d : 2d);
										prcDmg(ent, Shooter.getShooter(ent, false), sh, e.getDamage(), 
										"§f\u9298", 5, (short) GunType.knfRwd, false, false, false, false, false);
										if (dmgr.getType() == EntityType.PLAYER) {
											Main.dmgArm((Player) dmgr, ent.getEyeLocation(), "§6" + String.valueOf((int) e.getDamage() * 5));
										}
									}
									break;
								default:
									break;
								}
							}
						}
					} else if (ent instanceof Mob) {
						final Invasion ar = Invasion.getMobInvasion(ee.getDamager().getEntityId());
						if (ar != null && e.getEntityType() == EntityType.PLAYER) {
							e.setCancelled(true);
							e.setDamage(ent.getEquipment().getChestplate() == null ? e.getDamage() : e.getDamage() * 0.6);
							e.setDamage(ent.getEquipment().getHelmet() == null ? e.getDamage() : e.getDamage() * 0.8);
							prcDmg(ent, Shooter.getShooter(ent, false), null, e.getDamage(), Team.NA.clr + ee.getDamager().getName() + "§f\u929a", 5);
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
			if (e.getEntity() instanceof LivingEntity && !e.getEntity().isInvulnerable()) {
				final LivingEntity ent = (LivingEntity) e.getEntity();
				final Shooter sh = Shooter.getShooter(ent, false);
				if (sh != null) {
					if (sh.arena() != null) {
						prcDmg(ent, sh, null, e.getDamage(), "§f\u9295", 5);
					}
				} else if (ent instanceof Mob) {
					prcDmg(ent, null, null, e.getDamage(), "", 5);
				}
			}
			break;
		case FALL:
			e.setCancelled(true);
			if (e.getEntityType() == EntityType.PLAYER && !e.getEntity().isInvulnerable()) {
				if (e.getDamage() > 2d) {
					final LivingEntity ent = (LivingEntity) e.getEntity();
					final Shooter sh = Shooter.getShooter(ent, false);
					if (sh != null) {
						if (sh.arena() != null) {
							prcDmg(ent, sh, null, e.getDamage(), "§f\u9296\u9297", 2);
						}
					}
				}
			}
			break;
		default:
			break;
		}
	}

	public static void prcDmg(final LivingEntity target, final Shooter tgtsh, 
		final Shooter damager, final double dmg, final String icon, final int ndts) {
		prcDmg(target, tgtsh, damager, dmg, icon, ndts, (short) 0, false, false, false, false, false);
	}

	public static void prcDmg(final LivingEntity target, final Shooter tgtsh, 
		final Shooter damager, final double dmg, final String icon, final int ndts, 
		final short rwd, final boolean blind, final boolean smoked, 
		final boolean noscp, final boolean head, final boolean walled) {
		//target.sendMessage("hit-" + tgtsh + "," + damager + "," + dmg);
		final double health = target.getHealth() - dmg;
		if (health > 0d) {//if entity isnt dying
			if (tgtsh == null) {//target is not player
				target.setHealth(health);
				target.playEffect(EntityEffect.HURT);
				target.setNoDamageTicks(ndts);
				if (target instanceof Mob && damager != null) {
					((Mob) target).setTarget(damager.getEntity());
				}
			} else {//target is player
				if (tgtsh.arena() != null) {
					if (damager == null) {//damager is not player
						target.setHealth(health);
						target.playEffect(EntityEffect.HURT);
						target.setNoDamageTicks(ndts);
						if (tgtsh instanceof BtShooter) {
							((BtShooter) tgtsh).hurt();
						}
					} else {//damager is player
						switch (tgtsh.arena().gst) {
						case ROUND:
							if (!(tgtsh.arena() instanceof Gungame) && tgtsh.arena().isSmTm(damager, tgtsh)) {
								break;
							}
						case BEGINING:
							target.setHealth(health);
							target.playEffect(EntityEffect.HURT);
							target.setNoDamageTicks(ndts);
							if (tgtsh instanceof BtShooter) {
								((BtShooter) tgtsh).hurt();
							}
							break;
						default:
							break;
						}
					}
				}
			}
		} else {//if entity is dying
			if (tgtsh == null) {//target is not player
				final Invasion inv = Invasion.getMobInvasion(target.getEntityId());
				if (inv != null) {
					switch (inv.gst) {
					case ROUND:
						if (damager != null) {
							inv.addMbKll(damager);
							inv.chngMn(damager, rwd << Mobber.getMbPow(target.getType()) >> 2);
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
				final Arena ar = tgtsh.arena();
				if (ar != null) {
					if (damager == null) {//damager is not player
						final String klfd = icon + " " + ar.getShtrNm(tgtsh);
						switch (ar.gst) {
						case ROUND:
						case BEGINING:
							for (final Player p : target.getWorld().getPlayers()) {
								p.sendMessage(klfd);
							}
							target.getWorld().spawnParticle(Particle.SOUL, target.getLocation().add(0, 1d, 0), 40, 0.5D, 0.5D, 0.5D, 0.0D, null, false);
							target.getWorld().playSound(target.getLocation(), Sound.BLOCK_ENDER_CHEST_OPEN, 10f, 2f);
							ar.killSh(tgtsh);
							break;
						default:
							break;
						}
					} else {//damager is player
						final String klfd;
						switch (ar.gst) {
						case ROUND:
							if (!(ar instanceof Gungame) && ar.isSmTm(damager, tgtsh)) {
								break;
							}
							ar.addKll(damager);
							if (ar instanceof Defusal) {
								ar.chngMn(damager, rwd);
							}
						case BEGINING:
							klfd = ar.getShtrNm(damager) + " " + (walled ? "§e\u9290" : "") + 
								(smoked ? "§7\u9292" : "") + (blind ? "§b\u9293" : "") + 
								icon + (noscp ? "§d\u9294" : "") + (head ? "§6\u9291" : "") + 
								" " + ar.getShtrNm(tgtsh);
							for (final Player p : target.getWorld().getPlayers()) {
								p.sendMessage(klfd);
							}
							target.getWorld().spawnParticle(Particle.SOUL, target.getLocation().add(0, 1d, 0), 40, 0.5D, 0.5D, 0.5D, 0.0D, null, false);
							target.getWorld().playSound(target.getLocation(), Sound.BLOCK_ENDER_CHEST_OPEN, 10f, 2f);
							SkinQuest.tryCompleteQuest(damager, Quest.АЗИМОВ, smoked && blind && head ? 1 : 0);
							SkinQuest.tryCompleteQuest(damager, Quest.КРОВЬ, walled ? 1 : 0);
							SkinQuest.tryCompleteQuest(damager, Quest.ПАНК, (int) damager.getEntity().getHealth());
							ar.killSh(tgtsh);
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
			if (e.getHitEntity() != null || nt.hasPopFace(e.getHitBlockFace())) {
				Nade.expld(sb, (Player) sb.getShooter()); 
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
}