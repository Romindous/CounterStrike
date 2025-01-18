package me.Romindous.CounterStrike.Listeners;

import javax.annotation.Nullable;
import me.Romindous.CounterStrike.Enums.GameState;
import me.Romindous.CounterStrike.Enums.GunType;
import me.Romindous.CounterStrike.Enums.NadeType;
import me.Romindous.CounterStrike.Game.Arena;
import me.Romindous.CounterStrike.Game.Arena.Team;
import me.Romindous.CounterStrike.Game.Defusal;
import me.Romindous.CounterStrike.Game.Gungame;
import me.Romindous.CounterStrike.Game.Invasion;
import me.Romindous.CounterStrike.Main;
import me.Romindous.CounterStrike.Menus.ChosenSkinMenu;
import me.Romindous.CounterStrike.Objects.EntityShootAtEntityEvent;
import me.Romindous.CounterStrike.Objects.Game.BtShooter;
import me.Romindous.CounterStrike.Objects.Game.Mobber;
import me.Romindous.CounterStrike.Objects.Game.Nade;
import me.Romindous.CounterStrike.Objects.Game.PlShooter;
import me.Romindous.CounterStrike.Objects.Shooter;
import me.Romindous.CounterStrike.Objects.Skins.Quest;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Ostrov;
import ru.komiss77.enums.Stat;
import ru.komiss77.utils.EntityUtil;

public class DmgLis implements Listener {

	public static final String DEATH_SND = Ostrov.registries
		.SOUNDS.getKey(Sound.BLOCK_ENDER_CHEST_OPEN).asMinimalString();

	@EventHandler
	public void onDmg(final EntityDamageEvent e) {
		switch (e.getCause()) {
		case ENTITY_ATTACK:
			if (e instanceof final EntityDamageByEntityEvent ee) {
                if (e.getEntity() instanceof final LivingEntity ent
					&& ee.getDamager() instanceof final LivingEntity dmgr
					&& !e.getEntity().isInvulnerable()) {
                    final Shooter sh = Shooter.getShooter(dmgr, false);
					if (sh != null) {
						//if player shoots living
						if (sh.arena() == null) {
							if (!(ee instanceof EntityShootAtEntityEvent) && dmgr.getType() == EntityType.PLAYER) {
								e.setCancelled(((HumanEntity) dmgr).getGameMode() != GameMode.CREATIVE);
								EntityUtil.indicate(ent.getEyeLocation(), "§6"
									+ (ent.getEquipment().getChestplate() == null ? 4 : 2) * 5, (Player) dmgr);
							}
						} else {
							if (ee instanceof final EntityShootAtEntityEvent eee) {
								//need to deal damage
                                final GunType gt = GunType.get(sh.item(EquipmentSlot.HAND));
								if (gt == null) return;
								if (eee.isCritical() && dmgr.getType() == EntityType.PLAYER) {
									ApiOstrov.addStat((Player) dmgr, Stat.CS_hshot);
								}

								eee.setDamage(prcDmg(ent, Shooter.getShooter(ent, false), 
								sh, e.getDamage(), gt.icn, 0, gt.rwd, dmgr.hasPotionEffect(PotionEffectType.BLINDNESS),
									eee.smoked, eee.noscope, eee.isCritical(), eee.wallbang));
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
										"§f\u9298", 5, Shooter.knifRwd, false, false, false, false, false);
										if (dmgr.getType() == EntityType.PLAYER) {
											EntityUtil.indicate(ent.getEyeLocation(), "§6" + (int) e.getDamage() * 5, (Player) dmgr);
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
							prcDmg(ent, Shooter.getShooter(ent, false), null, e.getDamage(),
								Team.SPEC.clr + ee.getDamager().getName() + "§f\u929a", 5);
						}
					}
				}
			}
			break;
		case FIRE_TICK:
			e.getEntity().setFireTicks(0);
		case BLOCK_EXPLOSION:
		case ENTITY_EXPLOSION:
		case FREEZE:
			e.setCancelled(true);
			break;
		case FIRE:
			e.setDamage(2d);
			e.setCancelled(true);
			if (e.getEntity() instanceof final LivingEntity ent && !e.getEntity().isInvulnerable()) {
                final Shooter sh = Shooter.getShooter(ent, false);
				if (sh != null) {
					if (sh.arena() != null && sh.arena().gst != GameState.BUYTIME) {
						prcDmg(ent, sh, null, sh instanceof PlShooter ? e.getDamage() : e.getDamage() * 0.1d, "§f\u9295", 5);
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

	public static double prcDmg(final LivingEntity target, final Shooter tgtsh, 
		final Shooter damager, final double dmg, final String icon, final int ndts) {
		return prcDmg(target, tgtsh, damager, dmg, icon, ndts, (short) 0,
			false, false, false, false, false);
	}

	public static double prcDmg(final LivingEntity target, final @Nullable Shooter tgtsh, final @Nullable Shooter damager,
		final double dmg, final String icon, final int ndts, final short rwd, final boolean blind, final boolean smoked,
		final boolean noscp, final boolean head, final boolean walled) {
		//target.sendMessage("hit-" + tgtsh + "," + damager + "," + dmg);
		final double health = target.getHealth() - dmg;
		if (health > 0d) {//if entity isnt dying
			if (tgtsh == null) {//target is not player
				target.setHealth(health);
				target.playHurtAnimation(target.getBodyYaw());
				target.setNoDamageTicks(ndts);
				target.playHurtAnimation(target.getYaw());
				if (target instanceof Mob && damager != null) {
					((Mob) target).setTarget(damager.getEntity());
				}
			} else {//target is player
				if (tgtsh.arena() != null) {
					if (damager == null) {//damager is not player
						target.setHealth(health);
						target.setNoDamageTicks(ndts);
						if (tgtsh instanceof BtShooter) {
							((BtShooter) tgtsh).own().hurt(target);
						} else {
							target.playHurtAnimation(target.getYaw());
							((Player) target).playSound(target, Sound.BLOCK_MUDDY_MANGROVE_ROOTS_FALL, 2f, 2f);
							target.getWorld().playSound(target.getLocation(), Sound.ENTITY_GENERIC_HURT, 1f, 1.2f);
						}
					} else {//damager is player
						switch (tgtsh.arena().gst) {
						case ROUND:
							if (!(tgtsh.arena() instanceof Gungame) && tgtsh.arena().isSmTm(damager, tgtsh)) {
								break;
							}
						case BEGINING:
							target.setHealth(health);
							target.setNoDamageTicks(ndts);
							if (tgtsh instanceof BtShooter) {
								((BtShooter) tgtsh).own().hurt(target);
							} else {
								target.playHurtAnimation(target.getYaw());
								((Player) target).playSound(target, Sound.BLOCK_MUDDY_MANGROVE_ROOTS_FALL, 2f, 2f);
								target.getWorld().playSound(target.getLocation(), Sound.ENTITY_GENERIC_HURT, 1f, 1.2f);
							}
							break;
						default:
							break;
						}
					}
				}
			}
			return 0d;
		} else {//if entity is dying
			if (tgtsh == null) {//target is not player
				final Invasion inv = Invasion.getMobInvasion(target.getEntityId());
				if (inv != null) {
					switch (inv.gst) {
					case ROUND:
						if (damager != null) {
							inv.addMbKll(damager);
							inv.chngMn(damager, rwd << Mobber.MobType.get(target.getType()).pow >> 2);
						}
					case BEGINING:
						inv.TMbs.remove(target.getEntityId());
						target.getWorld().spawnParticle(Particle.SOUL, target.getLocation()
							.add(0, 1d, 0), 40, 0.5D, 0.5D, 0.5D, 0.0D, null, false);
						Main.plyWrldSnd(target, DEATH_SND, 2f);
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
							target.getWorld().spawnParticle(Particle.SOUL, target.getLocation()
								.add(0, 1d, 0), 40, 0.5D, 0.5D, 0.5D, 0.0D, null, false);
							Main.plyWrldSnd(target, DEATH_SND, 2f);
							ar.killSh(tgtsh);
							break;
						default:
							break;
						}
					} else {//damager is player
						final String klfd;
						switch (ar.gst) {
						case ROUND:
							if (ar.isSmTm(damager, tgtsh) && !(ar instanceof Gungame)) {
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
							target.getWorld().spawnParticle(Particle.SOUL, target.getLocation()
								.add(0, 1d, 0), 40, 0.5D, 0.5D, 0.5D, 0.0D, null, false);
							Main.plyWrldSnd(target, DEATH_SND, 2f);
							ChosenSkinMenu.tryCompleteQuest(damager, Quest.АЗИМОВ, smoked && blind && head ? 1 : 0);
							ChosenSkinMenu.tryCompleteQuest(damager, Quest.КРОВЬ, walled ? 1 : 0);
							final LivingEntity dle = damager.getEntity();
							if (dle != null) ChosenSkinMenu.tryCompleteQuest(damager, Quest.ПАНК, (int) dle.getHealth());
							ar.killSh(tgtsh);
							break;
						default:
							break;
						}
					}
				}
			}
			return -health;
		}
	}
   
	@EventHandler
	public void onHit(final ProjectileHitEvent e) {
		if (e.getEntityType() == EntityType.SNOWBALL) {
			final Snowball sb = (Snowball) e.getEntity();
			final Nade nd = Main.nades.remove(sb.getUniqueId());
			final NadeType nt = NadeType.getNdTp(sb.getItem());
			if (nd == null || nt == null) return;
			if (e.getHitEntity() != null || nt.hasPopFace(e.getHitBlockFace())) {
				e.setCancelled(true);
				nd.explode();
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
					nd.explode();
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

			vec.multiply(0.6F);
			nv.setVelocity(vec);
			nd.chngNd(nv);
		}
	}
}