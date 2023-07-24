package me.Romindous.CounterStrike.Objects.Game;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.ThrowableProjectile;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import me.Romindous.CounterStrike.Main;
import me.Romindous.CounterStrike.Enums.GunType;
import me.Romindous.CounterStrike.Enums.NadeType;
import me.Romindous.CounterStrike.Listeners.DmgLis;
import me.Romindous.CounterStrike.Objects.Shooter;
import ru.komiss77.modules.world.WXYZ;
import ru.komiss77.version.IServer;
import ru.komiss77.version.VM;

public class Nade {
	
	public Snowball prj;
	public int tm;
  
	public Nade(final Snowball prj, final int tm) {
		this.prj = prj;
		this.tm = tm;
	}
	
	public static void expld(final ThrowableProjectile prj, final Player dmgr) {
		final NadeType nt = NadeType.getNdTp(prj.getItem());
		final Location loc = prj.getLocation();
		final World w = loc.getWorld();
		final int X;
		final int Y;
		final int Z;
		final IServer sv;
		switch (nt) {
			case FRAG:
				w.spawnParticle(Particle.EXPLOSION_HUGE, loc, 1, 0d, 0d, 0d);
				w.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 2f, 1.5f);
				final Shooter sh = Shooter.getPlShooter(dmgr.getName(), true);
				for (final Entity e : prj.getNearbyEntities(5d, 5d, 5d)) {
					if (e instanceof Mob) {
						final LivingEntity le = (LivingEntity) e;
						final double d = 12d - e.getLocation().distanceSquared(loc) * 0.4d * (le.getEquipment().getChestplate() == null ? 1d : 0.4d);
						if (sh.arena() != null) {
							DmgLis.prcDmg(le, Shooter.getShooter(le, false), sh, d, NadeType.FRAG.icn, 2, NadeType.nadeRwd, false, false, false, false, false);
						}
						Main.dmgInd(dmgr, le.getEyeLocation(), "§6" + String.valueOf((int)(d * 5.0f)));
					} else if (e instanceof Player && ((Player) e).getGameMode() == GameMode.SURVIVAL) {
						final Player pl = (Player) e;
						final double d = 20d - e.getLocation().distanceSquared(loc) * 0.4d * (pl.getInventory().getChestplate() == null ? 1d : 0.4d);
						if (sh.arena() != null) {
							DmgLis.prcDmg(pl, Shooter.getShooter(pl, false), sh, d, NadeType.FRAG.icn, 2, NadeType.nadeRwd, false, false, false, false, false);
						}
						Main.dmgInd(dmgr, pl.getEyeLocation(), "§6" + String.valueOf((int)(d * 5.0f)));
					}
				}
				break;
			case FLAME:
				X = loc.getBlockX();
				Y = loc.getBlockY();
				Z = loc.getBlockZ();
				sv = VM.getNmsServer();
				w.playSound(loc, Sound.BLOCK_GLASS_BREAK, 2f, 0.8f);
				w.playSound(loc, Sound.ENTITY_BLAZE_SHOOT, 0.5F, 0.6F);
				new BukkitRunnable() {int r = 0;
					@Override
					public void run() {
						if ((r++) == 3) {this.cancel(); return;}
						for (int x = X - r; x <= X + r; x++) {
							for (int y = Y - r; y <= Y + r; y++) {
								for (int z = Z - r; z <= Z + r; z++) {
									int n = (X - x) * (X - x) + (Y - y) * (Y - y) + (Z - z) * (Z - z);
									if ((r - 1) * (r - 1) <= n && n <= r * r) {
										if (sv.getFastMat(w, x, y, z).isAir() && sv.getFastMat(w, x, y - 1, z).isCollidable()) {
											final WXYZ bl = new WXYZ(w, x, y, z, nt.time << 1);
											bl.getBlock().setType(Material.FIRE, false);
											Main.ndBlks.remove(bl);
											Main.ndBlks.add(bl);
										}
									}
								}
							}
						}
					}
				}.runTaskTimer(Main.plug, 2, 4);
				break;
			case SMOKE:
				X = loc.getBlockX();
				Y = loc.getBlockY();
				Z = loc.getBlockZ();
				sv = VM.getNmsServer();
				w.playSound(loc, Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5F, 0.4F);
				new BukkitRunnable() {int r = 0;
					public void run() {
						if ((r++) == 4) {this.cancel(); return;}
						for (int x = X - r; x <= X + r; x++) {
							for (int y = Y - r; y <= Y + r; y++) {
								for (int z = Z - r; z <= Z + r; z++) {
									int n = (X - x) * (X - x) + (Y - y) * (Y - y) + (Z - z) * (Z - z);
									if ((r - 1) * (r - 1) <= n && n <= r * r) {
										switch (sv.getFastMat(w, x, y, z)) {
										case FIRE, AIR, CAVE_AIR:
											final WXYZ bl = new WXYZ(w, x, y, z, nt.time << 2);
											bl.getBlock().setType(Material.POWDER_SNOW, false);
											Main.ndBlks.remove(bl);
											Main.ndBlks.add(bl);
										default:
											break;
										}
									}
								}
							}
						}
					}
				}.runTaskTimer(Main.plug, 2, 4);
				break;
			case FLASH:
				final Firework fw = (Firework) w.spawnEntity(loc, EntityType.FIREWORK);
				final FireworkMeta fm = fw.getFireworkMeta();
				fm.addEffect(FireworkEffect.builder().withColor(Color.WHITE).with(FireworkEffect.Type.BURST).build());
				fw.setFireworkMeta(fm);
				fw.detonate();
				for (final LivingEntity le : w.getLivingEntities()) {
					final Location eloc = le.getEyeLocation();
					double px = -Math.sin(Math.toRadians(eloc.getYaw()));
					double pz = Math.cos(Math.toRadians(eloc.getYaw()));
					double pl = Math.sqrt(px * px + pz * pz);
					double dx = loc.getX() - eloc.getX();
					double dz = loc.getZ() - eloc.getZ();
					double dl = Math.sqrt(dx * dx + dz * dz);
					if (dl < 32d) {
						final Shooter she = Shooter.getShooter(le, false);
						if (she != null) {
							if (!she.isDead() && Math.abs((px / pl - dx / dl) * (px / pl - dx / dl) + (pz / pl - dz / dl) * (pz / pl - dz / dl)) < 1) {
								if (Main.rayThruAir(loc, eloc.toVector(), 0.1F)) {
									if (dmgr != null && dmgr.getEntityId() != le.getEntityId()) {
										dmgr.playSound(eloc, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 4f, 1f);
									}
									le.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, (40 - (int) dl) << 2, 2, true, false, false));
								}
							}
						} else if (le instanceof Mob) {
							switch (le.getType()) {
							case SHULKER:
								return;
							default:
								((Mob) le).setTarget(null);
								le.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 200, 1, true, false));
								le.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 200, 1, true, false));
								break;
							}
						}
					}
				}
				break;
			case DECOY:
				X = loc.getBlockX();
				Y = loc.getBlockY();
				Z = loc.getBlockZ();
				final GunType gt = dmgr == null ? GunType.USP : GunType.getGnTp(dmgr.getInventory().getItem(0));
				loc.setPitch(gt == null ? 10 : gt.ordinal());
				Main.decoys.add(new WXYZ(w, X, Y, Z, 160, gt == null ? GunType.USP.ordinal() : gt.ordinal()));
				break;
		}
		prj.remove();
	}

	public static void chngNd(final Snowball sb, final Snowball nv) {
		for (final Nade n : Main.nades) {
			if (n.prj.getUniqueId().equals(sb.getUniqueId())) {
				n.prj = nv; 
			}
		}
	}
}