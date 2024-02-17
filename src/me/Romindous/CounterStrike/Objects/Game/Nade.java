package me.Romindous.CounterStrike.Objects.Game;

import me.Romindous.CounterStrike.Enums.GunType;
import me.Romindous.CounterStrike.Enums.NadeType;
import me.Romindous.CounterStrike.Game.Arena;
import me.Romindous.CounterStrike.Listeners.DmgLis;
import me.Romindous.CounterStrike.Main;
import me.Romindous.CounterStrike.Objects.Shooter;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import ru.komiss77.modules.world.WXYZ;
import ru.komiss77.utils.LocationUtil;
import ru.komiss77.version.Nms;

public class Nade {
	
	public Snowball prj;
	public int tm;
  
	public Nade(final Snowball prj, final int tm) {
		this.prj = prj;
		this.tm = tm;
	}
	
	public void explode() {
		final LivingEntity dmgr = prj.getShooter() instanceof LivingEntity
			? ((LivingEntity) prj.getShooter()) : null;
		final Shooter sh = dmgr == null ? null :
			Shooter.getShooter(dmgr, false);
		prj.remove();

		final NadeType nt = NadeType.getNdTp(prj.getItem());
		if (nt == null) return;
		final Location loc = prj.getLocation();
		final World w = loc.getWorld();
		final int X, Y, Z;
		switch (nt) {
			case FRAG:
				w.spawnParticle(Particle.EXPLOSION_HUGE, loc, 1, 0d, 0d, 0d);
				w.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 2f, 1.5f);
				if (sh == null) {
					for (final Entity e : prj.getNearbyEntities(5d, 5d, 5d)) {
						if (e instanceof final Mob mb) {
							final double d = 12d - mb.getLocation().distanceSquared(loc) * 0.4d * (mb.getEquipment().getChestplate() == null ? 1d : 0.4d);
							DmgLis.prcDmg(mb, Shooter.getShooter(mb, false), null, d, NadeType.FRAG.icn, 2,
									NadeType.nadeRwd, false, false, false, false, false);
						} else if (e instanceof final Player pl && pl.getGameMode() == GameMode.SURVIVAL) {
							final double d = 20d - e.getLocation().distanceSquared(loc) * 0.4d * (pl.getInventory().getChestplate() == null ? 1d : 0.4d);
							DmgLis.prcDmg(pl, Shooter.getShooter(pl, false), null, d, NadeType.FRAG.icn, 2,
									NadeType.nadeRwd, false, false, false, false, false);
						}
					}
				} else {
					for (final Entity e : prj.getNearbyEntities(5d, 5d, 5d)) {
						if (e instanceof final Mob mb) {
							final double d = 12d - mb.getLocation().distanceSquared(loc) * 0.4d * (mb.getEquipment().getChestplate() == null ? 1d : 0.4d);
							DmgLis.prcDmg(mb, Shooter.getShooter(mb, false), sh, d, NadeType.FRAG.icn, 2,
									NadeType.nadeRwd, false, false, false, false, false);
							if (sh instanceof PlShooter) Main.dmgInd((Player) dmgr, mb.getEyeLocation(), "§6" + (int) (d * 5.0f));
						} else if (e instanceof final Player pl && pl.getGameMode() == GameMode.SURVIVAL) {
							final double d = 20d - e.getLocation().distanceSquared(loc) * 0.4d * (pl.getInventory().getChestplate() == null ? 1d : 0.4d);
							DmgLis.prcDmg(pl, Shooter.getShooter(pl, false), sh, d, NadeType.FRAG.icn, 2,
									NadeType.nadeRwd, false, false, false, false, false);
							if (sh instanceof PlShooter) Main.dmgInd((Player) dmgr, pl.getEyeLocation(), "§6" + (int) (d * 5.0f));
						}
					}
				}
				break;
			case FLAME:
				X = loc.getBlockX();
				Y = loc.getBlockY();
				Z = loc.getBlockZ();
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
										if (Nms.getFastMat(w, x, y, z).isAir() && Nms.getFastMat(w, x, y - 1, z).isCollidable()) {
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
				w.playSound(loc, Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5F, 0.4F);
				new BukkitRunnable() {int r = 0;
					public void run() {
						if ((r++) == 4) {this.cancel(); return;}
						for (int x = X - r; x <= X + r; x++) {
							for (int y = Y - r; y <= Y + r; y++) {
								for (int z = Z - r; z <= Z + r; z++) {
									int n = (X - x) * (X - x) + (Y - y) * (Y - y) + (Z - z) * (Z - z);
									if ((r - 1) * (r - 1) <= n && n <= r * r) {
										switch (Nms.getFastMat(w, x, y, z)) {
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
				final Firework fw = w.spawn(loc, Firework.class, pf -> {
					final FireworkMeta fm = pf.getFireworkMeta();
					fm.addEffect(FireworkEffect.builder().withColor(Color.WHITE).with(FireworkEffect.Type.BURST).build());
					pf.setFireworkMeta(fm);
				});
				fw.detonate();
				boolean hit = false;
				for (final LivingEntity le : Main.getLEs(w)) {
					final Location eloc = le.getEyeLocation();
					final double px = -Math.sin(Math.toRadians(eloc.getYaw()));
					final double pz = Math.cos(Math.toRadians(eloc.getYaw()));
					final double pl = Math.sqrt(px * px + pz * pz);
					final double dx = loc.getX() - eloc.getX();
					final double dz = loc.getZ() - eloc.getZ();
					final double dl = Math.sqrt(dx * dx + dz * dz);
					final int dur = (40 - (int) dl) << 2;
					if (dl < 40d) {
						final int id = dmgr == null ? -1 : dmgr.getEntityId();
						final Shooter she = Shooter.getShooter(le, false);
						if (she != null) {
							if (!she.isDead() && Math.abs((px / pl - dx / dl) * (px / pl - dx / dl) + (pz / pl - dz / dl) * (pz / pl - dz / dl)) < 1) {
								if (LocationUtil.rayThruAir(loc, eloc.toVector(), 0.1F)) {
									if (le.getEntityId() != id && !hit) hit = true;
									le.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS,
										dur, 2, true, false, false));
								}
							}
						} else if (le instanceof Mob) {
							if (le.getType() == EntityType.SHULKER) {
								continue;
							}
							((Mob) le).setTarget(null);
							if (le.getEntityId() != id && !hit) hit = true;
							le.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 200, 1, true, false));
							le.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 200, 1, true, false));
						}
					}
				}
				if (hit && sh instanceof PlShooter) sh.getPlayer().playSound(loc, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 4f, 1f);
				break;
			case DECOY:
				X = loc.getBlockX(); Y = loc.getBlockY(); Z = loc.getBlockZ();
				final GunType gt = sh == null ? null : GunType.getGnTp(sh.item(0));
				Main.decoys.add(new WXYZ(w, X, Y, Z, 160, gt == null ? GunType.USP.ordinal() : gt.ordinal()));
				break;
		}
	}

	public void chngNd(final Snowball nv) {
		Main.nades.put(nv.getUniqueId(), this);
		nv.setItem(prj.getItem());
		nv.setShooter(prj.getShooter());
		prj.remove();
		prj = nv;

		if (nv.getShooter() instanceof final LivingEntity le) {
			final Shooter sh = Shooter.getPlShooter(le.getName(), false);
			if (sh != null && sh.arena() != null) {
				final Arena.Team tm = sh.arena().shtrs.get(sh);
				Nms.colorGlow(nv, tm.color(), sh.allyTest());
			}
		}
	}

	public static void launch(final LivingEntity le, final Shooter sh,
	  	final Vector dir, final int time, final int slot) {
		final Snowball sb = le.launchProjectile(Snowball.class, dir);
		final ItemStack ndi = sh.item(slot);
		final NadeType nt = NadeType.getNdTp(ndi);
		if (nt == null) return;
		sb.setItem(ndi);
		ndi.setAmount(ndi.getAmount() - 1);
		sh.item(ndi, slot);
		final Nade nd = new Nade(sb, time);
		Main.nades.put(sb.getUniqueId(), nd);
		if (sh.arena() != null) {
			final Arena.Team tm = sh.arena().shtrs.get(sh);
			Nms.colorGlow(sb, tm.color(), sh.allyTest());
			Main.plyWrldSnd(le, sh.arena(), tm, nt.snd, 1f);
		} else Main.plyWrldSnd(le, "cs.rand.nadethrow", 1f);
	}
}