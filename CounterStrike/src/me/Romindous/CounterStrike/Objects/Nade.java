		package me.Romindous.CounterStrike.Objects;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.ThrowableProjectile;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.mojang.datafixers.util.Pair;

import me.Romindous.CounterStrike.Main;
import me.Romindous.CounterStrike.Enums.GunType;
import me.Romindous.CounterStrike.Enums.NadeType;
import me.Romindous.CounterStrike.Game.Arena;
import me.Romindous.CounterStrike.Listeners.DmgLis;

public class Nade {
	
	public Snowball prj;
	public byte tm;
  
	public Nade(final Snowball prj, final byte tm) {
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
		switch (nt) {
			case FRAG:
				w.spawnParticle(Particle.EXPLOSION_HUGE, loc, 1, 0d, 0d, 0d);
				w.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 2f, 1.5f);
				for (final Entity e : prj.getNearbyEntities(5d, 5d, 5d)) {
					if (e instanceof Mob || (e instanceof Player && ((Player) e).getGameMode() == GameMode.SURVIVAL)) {
						final LivingEntity le = (LivingEntity) e;
						final double d = 20d - e.getLocation().distanceSquared(loc) * 0.25d * (le.getEquipment().getChestplate() == null ? 1d : 0.4d);
						final Pair<Shooter, Arena> pr = Shooter.getPlShtrArena(dmgr.getName());
						if (pr.getSecond() != null) {
							DmgLis.prcDmg(le, pr, d, le.getHealth() - d <= 0d ? 
								pr.getSecond().getShtrNm(dmgr.getName()) + " " + NadeType.FRAG.icn + " " + pr.getSecond().getShtrNm(e.getName()) 
								: null, (byte) 2, (short) (le.getName().equals(dmgr.getName()) ? 0 : 150));
						}
						Main.dmgArm(dmgr, le.getEyeLocation(), "§6" + String.valueOf((int)(d * 5.0f)));
					}
				}
				break;
				
			case FLAME:
				X = loc.getBlockX();
				Y = loc.getBlockY();
				Z = loc.getBlockZ();
				for (byte r = 1; r < 4; r++) {
					w.playSound(loc, Sound.ENTITY_BLAZE_SHOOT, 0.5F, 0.6F);
					for (int x = X - r; x <= X + r; x++) {
						for (int y = Y - r; y <= Y + r; y++) {
							for (int z = Z - r; z <= Z + r; z++) {
								int n = (X - x) * (X - x) + (Y - y) * (Y - y) + (Z - z) * (Z - z);
								if ((r - 1) * (r - 1) <= n && n <= r * r) {
									final Block b = w.getBlockAt(x, y, z);
									if (b.getType().isAir() && b.getRelative(BlockFace.DOWN).getType().isSolid()) {
										Main.ndBlks.put(b, (byte) (r - 1 << 2));
									}
								}
							}
						}
					}
				}
				break;
			case SMOKE:
				X = loc.getBlockX();
				Y = loc.getBlockY();
				Z = loc.getBlockZ();
				for (byte r = 1; r < 5; r++) {
					w.playSound(loc, Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5F, 0.4F);
					for (int x = X - r; x <= X + r; x++) {
						for (int y = Y - r; y <= Y + r; y++) {
							for (int z = Z - r; z <= Z + r; z++) {
								int n = (X - x) * (X - x) + (Y - y) * (Y - y) + (Z - z) * (Z - z);
								if ((r - 1) * (r - 1) <= n && n <= r * r) {
									final Block b = w.getBlockAt(x, y, z);
									if (b.getType().isAir()) {
										Main.ndBlks.put(b, (byte) (r - 1 << 2 ^ 0x1));
									} else if (b.getType() == Material.FIRE) {
										b.setType(Material.AIR, false);
										Main.ndBlks.remove(b);
										Main.ndBlks.put(b, (byte) (r - 1 << 2 ^ 0x1));
									}
								}
							}
						}
					}
				}
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
					if (dl < 20) {
						if (le instanceof Mob) {
							((Mob) le).setTarget(null);
							le.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 200, 1, true, false));
							le.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 200, 1, true, false));
						} else if (le.getType() == EntityType.PLAYER && ((HumanEntity) le).getGameMode() == GameMode.SURVIVAL && Math.abs((px / pl - dx / dl) * (px / pl - dx / dl) + (pz / pl - dz / dl) * (pz / pl - dz / dl)) < 1) {
							if (Main.rayThruAir(loc, eloc, 0.1F)) {
								if (dmgr != null) {
									dmgr.playSound(eloc, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 2f, 1f);
								}
								le.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 50, 1, true, false, false));
							}
						}
					}
				}
				break;
			case DECOY:
				loc.setYaw(160.0F);
				final GunType gt = dmgr == null ? GunType.USP : GunType.getGnTp(dmgr.getInventory().getItem(0));
				loc.setPitch(gt == null ? 10 : gt.ordinal());
				Main.dcs.add(loc);
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