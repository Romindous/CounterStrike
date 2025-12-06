package me.romindous.cs.Listeners;

import javax.annotation.Nullable;
import java.util.Set;
import me.romindous.cs.Enums.GameState;
import me.romindous.cs.Enums.NadeType;
import me.romindous.cs.Game.Arena;
import me.romindous.cs.Game.Defusal;
import me.romindous.cs.Game.Gungame;
import me.romindous.cs.Game.Invasion;
import me.romindous.cs.Main;
import me.romindous.cs.Menus.ChosenSkinMenu;
import me.romindous.cs.Objects.EntityShootAtEntityEvent;
import me.romindous.cs.Objects.Game.BtShooter;
import me.romindous.cs.Objects.Game.Mobber;
import me.romindous.cs.Objects.Game.Nade;
import me.romindous.cs.Objects.Game.PlShooter;
import me.romindous.cs.Objects.Shooter;
import me.romindous.cs.Objects.Skins.Quest;
import org.bukkit.GameMode;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.BlockType;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import ru.komiss77.ApiOstrov;
import ru.komiss77.boot.OStrap;
import ru.komiss77.enums.Stat;
import ru.komiss77.utils.BlockUtil;
import ru.komiss77.utils.EntityUtil;
import ru.komiss77.utils.ItemUtil;

public class DmgLis implements Listener {

    public static final DamageType KILL_TYPE = DamageType.GENERIC_KILL;
    public static final DamageSource KILL_SOURCE = DamageSource.builder(KILL_TYPE).build();
	public static final String DEATH_SND = OStrap.keyOf(Sound.BLOCK_ENDER_CHEST_OPEN).asMinimalString();
	private static final Set<DamageType> IGNORED = Set.of(DamageType.ON_FIRE, DamageType.FREEZE, DamageType.EXPLOSION, DamageType.PLAYER_EXPLOSION, DamageType.FIREWORKS);
	private static final Set<DamageType> TICKED = Set.of(DamageType.IN_FIRE, DamageType.CAMPFIRE, DamageType.DROWN, DamageType.DRAGON_BREATH, DamageType.INDIRECT_MAGIC);

	@EventHandler
	public void onDmg(final EntityDamageEvent e) {
		final DamageSource ds = e.getDamageSource();
		final DamageType dt = ds.getDamageType();
		if (!(e.getEntity() instanceof final LivingEntity tgt)
            || e.getEntity().isInvulnerable() || KILL_TYPE.equals(dt)) return;
		if (IGNORED.contains(dt)) {
			e.setDamage(0d);
			e.getEntity().setFireTicks(0);
			e.setCancelled(true);
			return;
		}
		double dmg = e.getDamage();
		if (DamageType.PLAYER_ATTACK.equals(dt)) {
			if (!(ds.getCausingEntity() instanceof final LivingEntity dmgr)) return;
			final Shooter sh = Shooter.getShooter(dmgr, false);
			if (sh == null) return;
			if (sh.arena() == null) {
				if (!(e instanceof EntityShootAtEntityEvent) && dmgr instanceof final HumanEntity he) {
					e.setCancelled(he.getGameMode() != GameMode.CREATIVE);
					EntityUtil.indicate(tgt.getEyeLocation(), "§6"
						+ (ItemUtil.isBlank(tgt.getEquipment().getChestplate(), false) ? dmg : dmg * 0.5d) * 5, (Player) dmgr);
				}
				return;
			}
			if (e instanceof final EntityShootAtEntityEvent eee) {
				//need to deal damage
				if (eee.gun == null) return;
				if (eee.isCritical() && dmgr instanceof final Player pl) ApiOstrov.addStat(pl, Stat.CS_hshot);
				eee.setDamage(prcDmg(tgt, Shooter.getShooter(tgt, false), sh, e.getDamage(),
					eee.gun.icn, 0, eee.gun.rwd, dmgr.hasPotionEffect(PotionEffectType.BLINDNESS),
					eee.smoked, eee.noscope, eee.isCritical(), eee.wallbang));
				return;
			}
			e.setCancelled(true);
			final ItemStack hnd = sh.item(EquipmentSlot.HAND);
			switch (hnd.getType()) {
				case BONE:
				case BLAZE_ROD:
					if (tgt.getNoDamageTicks() != 0) return;
					final double knfDmg = ItemUtil.isBlank(tgt.getEquipment().getChestplate(), false) ? 5d : 3d;
					e.setDamage(knfDmg);
					prcDmg(tgt, Shooter.getShooter(tgt, false), sh, knfDmg, "§f\u9298", 5,
						Shooter.knifRwd, false, false, false, false, false);
					if (dmgr.getType() == EntityType.PLAYER)
						EntityUtil.indicate(tgt.getEyeLocation(),
							"§6" + (int) knfDmg * 5, (Player) dmgr);
					break;
				default:
					break;
			}
			return;
		}
		if (DamageType.FALL.equals(dt)) {
			e.setDamage(0d);
			e.setCancelled(true);
            if (e.getEntityType() != EntityType.PLAYER || dmg < 2.5d) return;
            final Shooter sh = Shooter.getShooter(tgt, false);
            if (sh == null || sh.arena() == null) return;
            prcDmg(tgt, sh, null, dmg, "§f\u9296\u9297", 2);
            return;
		}
		if (TICKED.contains(dt)) {
			e.setDamage(1d);
			dmg = e.getDamage();
			e.setCancelled(true);
			if (tgt.getNoDamageTicks() != 0) return;
			final Shooter sh = Shooter.getShooter(tgt, false);
			if (sh == null) {
				if (!(tgt instanceof Mob)) return;
				prcDmg(tgt, null, null, dmg, "", 2);
				return;
			}
			if (sh.arena() == null || sh.arena().gst == GameState.BUYTIME) return;
			prcDmg(tgt, sh, null, sh instanceof PlShooter
				? dmg : dmg * 0.2d, "§f\u9295", 2);
		}
		e.setDamage(0d);
		e.setCancelled(true);
//		Ostrov.log_warn(tgt.getName() + " was damaged by " + dt.key().asMinimalString());
		if (e.getEntityType() != EntityType.PLAYER) return;
		final Shooter sh = Shooter.getShooter(tgt, false);
		if (sh == null || sh.arena() == null) return;
		prcDmg(tgt, sh, null, dmg, "§f乂", 2);
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
//                        Bukkit.getOnlinePlayers().forEach(p -> p.sendMessage("killed " + target.getName()));
						inv.TMbs.remove(target.getEntityId());
                        target.getWorld().spawnParticle(Particle.SOUL, EntityUtil.center(target),
                            40, 0.5D, 0.5D, 0.5D, 0.0D, null, false);
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
							ChosenSkinMenu.tryCompleteQuest(damager, Quest.CYBER, smoked && blind && head ? 1 : 0);
							ChosenSkinMenu.tryCompleteQuest(damager, Quest.SCULK, walled ? 1 : 0);
							final LivingEntity dle = damager.getEntity();
							if (dle != null) ChosenSkinMenu.tryCompleteQuest(damager, Quest.ACID, (int) dle.getHealth());
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
        if (e.getEntityType() != EntityType.SNOWBALL) return;
        final Snowball sb = (Snowball) e.getEntity();
        final Nade nd = Main.nades.remove(sb.getUniqueId());
        final NadeType nt = NadeType.getNdTp(sb.getItem());
        if (nd == null || nt == null) return;
        if (e.getHitEntity() != null) {
            e.setCancelled(true);
            nd.explode();
            return;
        }

        if (nt.hasPopFace(e.getHitBlockFace())) {
            nd.tm=nd.tm>>1;
            if (nt.prm) {
                e.setCancelled(true);
                nd.explode();
                return;
            }
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
            if (nt == NadeType.SMOKE && BlockUtil.is(sb.getLocation().getBlock(), BlockType.FIRE)) {
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