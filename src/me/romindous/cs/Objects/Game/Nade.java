package me.romindous.cs.Objects.Game;

import java.util.Objects;
import me.romindous.cs.Enums.GunType;
import me.romindous.cs.Enums.NadeType;
import me.romindous.cs.Game.Arena;
import me.romindous.cs.Listeners.DmgLis;
import me.romindous.cs.Main;
import me.romindous.cs.Objects.Loc.Info;
import me.romindous.cs.Objects.Shooter;
import org.bukkit.*;
import org.bukkit.block.BlockType;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import ru.komiss77.modules.world.BVec;
import ru.komiss77.utils.EntityUtil;
import ru.komiss77.utils.ItemUtil;
import ru.komiss77.utils.LocUtil;
import ru.komiss77.utils.NumUtil;
import ru.komiss77.version.Nms;

public class Nade {

	public static final int DECOY_DUR = 40;

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
				w.spawnParticle(Particle.EXPLOSION, loc, 1, 0d, 0d, 0d);
				w.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 2f, 1.5f);
				if (sh == null) {
					for (final Entity e : prj.getNearbyEntities(5d, 5d, 5d)) {
						if (e instanceof final Mob mb) {
							final double d = 12d - mb.getLocation().distanceSquared(loc) * 0.4d * (ItemUtil.isBlank(mb.getEquipment().getHelmet(), false) ? 1d : 0.4d);
							DmgLis.prcDmg(mb, Shooter.getShooter(mb, false), null, d, NadeType.FRAG.icn, 2,
									NadeType.nadeRwd, false, false, false, false, false);
						} else if (e instanceof final Player pl && pl.getGameMode() == GameMode.SURVIVAL) {
							final double d = 20d - e.getLocation().distanceSquared(loc) * 0.4d * (ItemUtil.isBlank(pl.getEquipment().getHelmet(), false) ? 1d : 0.4d);
							DmgLis.prcDmg(pl, Shooter.getShooter(pl, false), null, d, NadeType.FRAG.icn, 2,
									NadeType.nadeRwd, false, false, false, false, false);
						}
					}
				} else {
					for (final Entity e : prj.getNearbyEntities(5d, 5d, 5d)) {
                        switch (e) {
                            case final Mob mb -> {
                                final double d = 12d - mb.getLocation().distanceSquared(loc) * 0.4d * (mb.getEquipment().getChestplate() == null ? 1d : 0.4d);
                                DmgLis.prcDmg(mb, Shooter.getShooter(mb, false), sh, d, NadeType.FRAG.icn, 2,
                                    NadeType.nadeRwd, false, false, false, false, false);
                                if (sh instanceof PlShooter)
                                    EntityUtil.indicate(mb.getEyeLocation(), "§6" + (int) (d * 5.0d), (Player) dmgr);
                            }
                            case final Player pl when pl.getGameMode() == GameMode.SURVIVAL -> {
                                final double d = 20d - e.getLocation().distanceSquared(loc) * 0.4d * (pl.getInventory().getChestplate() == null ? 1d : 0.4d);
                                DmgLis.prcDmg(pl, Shooter.getShooter(pl, false), sh, d, NadeType.FRAG.icn, 2,
                                    NadeType.nadeRwd, false, false, false, false, false);
                                if (sh instanceof PlShooter)
                                    EntityUtil.indicate(pl.getEyeLocation(), "§6" + (int) (d * 5.0d), (Player) dmgr);
                            }
                            case null, default -> {}
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
				new BukkitRunnable() {
					int r = 0;
					@Override
					public void run() {
						if ((r++) == 3) {this.cancel(); return;}
						for (int x = X - r; x <= X + r; x++) {
							for (int y = Y - r; y <= Y + r; y++) {
								for (int z = Z - r; z <= Z + r; z++) {
									final int n = NumUtil.square(X - x)
										+ NumUtil.square(Y - y) + NumUtil.square(Z - z);
									if (NumUtil.square(r - 1) <= n && n <= r * r) {
										if (Nms.fastType(w, x, y, z).isAir()
											&& !Info.PASSABLE.contains(Nms.fastType(w, x, y - 1, z))) {
											final BVec bl = BVec.of(w, x, y, z, (byte) nt.time);
											bl.block(w).setType(Material.FIRE, false);
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
				new BukkitRunnable() {
					int r = 0;
					public void run() {
						if ((r++) == 4) {this.cancel(); return;}
						for (int x = X - r; x <= X + r; x++) {
							for (int y = Y - r; y <= Y + r; y++) {
								for (int z = Z - r; z <= Z + r; z++) {
									final int n = NumUtil.square(X - x)
										+ NumUtil.square(Y - y) + NumUtil.square(Z - z);
									if (NumUtil.square(r - 1) <= n && n <= r * r) {
										final BlockType bt = Nms.fastType(w, x, y, z);
										if (bt.isAir() || BlockType.FIRE.equals(bt)) {
											final BVec bl = BVec.of(w, x, y, z, (byte) nt.time);
											bl.block(w).setType(Material.POWDER_SNOW, false);
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
							if (!she.isDead() && Math.abs((px / pl - dx / dl) * (px / pl - dx / dl)
								+ (pz / pl - dz / dl) * (pz / pl - dz / dl)) < 1d) {
								if (LocUtil.trace(eloc, loc.toVector().subtract(eloc.toVector()),
									(bp, bd) -> bd.getMaterial().asBlockType().isOccluding()).endDst()) {
									if (!hit && sh != null && isOps(sh, she)) hit = true;
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
				final GunType gt = sh == null ? null : GunType.fast(sh.item(0));
				final GunType fgt = gt == null ? GunType.USP : gt;
				Main.decoys.add(BVec.of(w, X, Y, Z, (byte) (DECOY_DUR / fgt.cld), (byte) fgt.ordinal()));
				break;
		}
	}

	private boolean isOps(final Shooter sh, final Shooter she) {
		final Arena ar = sh.arena();
        return ar == null ? she.arena() == null
			: !Objects.equals(ar.shtrs.get(sh), ar.shtrs.get(she));
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
		sh.item(slot, ndi);
		final Nade nd = new Nade(sb, time);
		Main.nades.put(sb.getUniqueId(), nd);
		if (sh.arena() != null) {
			final Arena.Team tm = sh.arena().shtrs.get(sh);
			Nms.colorGlow(sb, tm.color(), sh.allyTest());
			Main.plyWrldSnd(le, sh.arena(), tm, nt.snd, 1.1f - Main.srnd.nextFloat() * 0.2f);
		} else Main.plyWrldSnd(le, "rand.nadethrow", 1.1f - Main.srnd.nextFloat() * 0.2f);
	}
}