package me.Romindous.CounterStrike.Listeners;

import me.Romindous.CounterStrike.Main;
import me.Romindous.CounterStrike.Enums.GunType;
import me.Romindous.CounterStrike.Enums.NadeType;
import me.Romindous.CounterStrike.Objects.EntityShootAtEntityEvent;
import me.Romindous.CounterStrike.Objects.Nade;
import me.Romindous.CounterStrike.Utils.PacketUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.projectile.EntitySnowball;

import org.bukkit.Bukkit;
import org.bukkit.EntityEffect;
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

public class DmgLis implements Listener {
	
	@EventHandler
	public void onDmg(final EntityDamageEvent e) {
		Bukkit.broadcastMessage(e.toString());
		switch (e.getCause()) {
		case ENTITY_ATTACK:
			if (e instanceof EntityDamageByEntityEvent) {
				final EntityDamageByEntityEvent ee = (EntityDamageByEntityEvent) e;
				if (e.getEntity() instanceof LivingEntity) {
					final LivingEntity ent = (LivingEntity) e.getEntity();
					if (ee.getDamager().getType() == EntityType.PLAYER) {
						final Player dmgr = (Player) ee.getDamager();
						//if player shoots living
						if (ee instanceof EntityShootAtEntityEvent) {
							//need to deal damage
							final EntityShootAtEntityEvent eee = (EntityShootAtEntityEvent) ee;
							dmgr.playSound(dmgr.getLocation(), Sound.BLOCK_END_PORTAL_FRAME_FILL, 1f, 2f);
							if (ent.getHealth() - e.getDamage() < 0) {
								dmgr.playSound(dmgr.getLocation(), Sound.BLOCK_ENDER_CHEST_OPEN, 1f, 2f);
								
								final String klfd = dmgr.getName() + " " + (eee.wb ? "§e\u9290" : "") + 
										((ent.getLocation().getBlock().getType() == Material.POWDER_SNOW) ? "§7\u9292" : "") + 
										(ent.hasPotionEffect(PotionEffectType.BLINDNESS) ? "§b\u9293" : "") + 
										(GunType.getGnTp(dmgr.getInventory().getItemInMainHand())).icn + 
										(eee.nscp ? "§d\u9294" : "") + 
										(eee.hst ? "§6\u9291" : "") + 
										" §f" + ent.getName();
								for (final Player p : Bukkit.getOnlinePlayers()) {
									p.sendMessage(klfd);
								}
								if (ent.getType() == EntityType.PLAYER) {
									Main.killPl((Player) ent);
								} else {
									ent.remove();
								} 
							} else {
								ent.setHealth(ent.getHealth() - e.getDamage());
								ent.playEffect(EntityEffect.HURT);
								ent.setNoDamageTicks(0);
							} 
						} else {
						//if player hits living
							
						}
					}
				}
			}
			break;
		case ENTITY_EXPLOSION:
			e.setCancelled(true);
			break;
		default:
			break;
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
					Nade.expld(sb, (Player) sb.getShooter()); return;
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
					nv.setNoGravity(true); break;
				} 
				vec.setY(-vec.getY());
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