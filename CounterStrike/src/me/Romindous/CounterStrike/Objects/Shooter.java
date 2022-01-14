package me.Romindous.CounterStrike.Objects;

import java.util.HashSet;
import java.util.LinkedList;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import com.mojang.datafixers.util.Pair;

import me.Romindous.CounterStrike.Main;
import me.Romindous.CounterStrike.Enums.GameState;
import me.Romindous.CounterStrike.Enums.GunType;
import me.Romindous.CounterStrike.Game.Arena;
import me.Romindous.CounterStrike.Utils.PacketUtils;
import net.minecraft.core.BaseBlockPosition;
import net.minecraft.world.phys.Vec3D;

public class Shooter {
	
	public final String nm;
	public final LinkedList<Vec3D> pss;
	public PlayerInventory inv;
	public byte stm;
	public byte rctm;
	public byte cld;
	public byte dths;
	public short kls;
	public short cnt;
	public short money;
	private final int shc;
	
	public Shooter(final Player pl) {
		this.nm = pl.getName();
		this.inv = pl.getInventory();
		this.pss = new LinkedList<>();
		this.rctm = 50;
		this.cld = 0;
		this.cnt = 0;
		this.stm = 0;
		this.shc = nm.hashCode();
		this.money = 0;
		this.kls = 0;
		this.dths = 0;
		final Location loc = pl.getLocation();
		final Vec3D vc = new Vec3D(loc.getX(), loc.getY(), loc.getZ());
		this.pss.add(vc);
		this.pss.add(vc);
		this.pss.add(vc);
		this.pss.add(vc);
		this.pss.add(vc);
	}
  
	public static Pair<Shooter, Arena> getPlShtrArena(final String nm) {
		for (final Arena ar : Main.actvarns) {
			for (final Shooter s : ar.shtrs.keySet()) {
				if (s.nm.equals(nm)) {
					return new Pair<Shooter, Arena>(s, ar);
				}
			}
		}
		for (final Shooter s : Main.shtrs) {
			if (s.nm.equals(nm)) {
				return new Pair<Shooter, Arena>(s, null);
			}
		}
		final Shooter sh = new Shooter(Bukkit.getPlayer(nm));
		Main.shtrs.add(sh);
		return new Pair<Shooter, Arena>(sh, null);
	}
	
	public void shoot(final GunType gt, final Main plug, final Player pl, final boolean dff, final short tr) {
		final Location loc = pl.getEyeLocation();
		if (dff) {
			if (gt.brst == 0) {
				loc.setPitch(loc.getPitch() - tr * gt.yrcl + ((float) pl.getVelocity().getY() + (pl.isInWater() ? 0.005f : 0.0784f)) * 40f);
				loc.setYaw((Main.srnd.nextFloat() - 0.5f) * gt.xsprd * tr + loc.getYaw());
			} else {
				loc.setPitch((Main.srnd.nextFloat() - 0.5f) * gt.xsprd * this.rctm + loc.getPitch() - tr * 0.1F + ((float) pl.getVelocity().getY() + (pl.isInWater() ? 0.005f : 0.0784f)) * 40f);
				loc.setYaw((Main.srnd.nextFloat() - 0.5f) * gt.xsprd * this.rctm + loc.getYaw());
			} 
		}
		final double lkx = -Math.sin(Math.toRadians((180f - loc.getYaw())));
		final double lkz = -Math.cos(Math.toRadians((180f - loc.getYaw())));
		
		LivingEntity srch = null;
		Location prp = null;
		boolean h = false;
		
		for (final LivingEntity e : Main.getWLnts(loc.getWorld().getName())) {
			final BoundingBox ebx;
			final double dx;
			final double dz;
			switch (e.getType()) {
			case PLAYER:
				if (((HumanEntity) e).getGameMode() == GameMode.SURVIVAL && !e.getName().equals(pl.getName())) {
					final Vec3D lc = Shooter.getPlShtrArena(e.getName()).getFirst().pss.peek();
					if (((Player) e).isSneaking()) {
						ebx = new BoundingBox(lc.b, lc.c, lc.d, lc.b, lc.c + 1.5d, lc.d);
					} else {
						ebx = new BoundingBox(lc.b, lc.c, lc.d, lc.b, lc.c + 1.9d, lc.d);
					}
					dx = lc.b - loc.getX();
					dz = lc.d - loc.getZ();
				} else {
					continue;
				}
				break;
			case ARMOR_STAND:
			case TURTLE:
				continue;
			default:
				ebx = e.getBoundingBox();
				dx = ebx.getCenterX() - loc.getX();
				dz = ebx.getCenterZ() - loc.getZ();
				break;
			}
			final double ln = Math.sqrt(dx * dx + dz * dz);
			if (Math.sqrt(Math.pow(lkx - dx / ln, 2d) + Math.pow(lkz - dz / ln, 2d)) * ln < 0.4d) {
				final double pty = loc.getY() + Math.tan(Math.toRadians(-loc.getPitch())) * ln;
				if (pty < ebx.getMaxY() && pty > ebx.getMinY()) {
					srch = e;
					prp = new Location(loc.getWorld(), ebx.getCenterX(), ebx.getCenterY(), ebx.getCenterZ());
					h = pty - ebx.getMinY() > ebx.getHeight() * 0.75d;
					break;
				} 
			} 
		} 
		
		final Vector vec = loc.getDirection().normalize().multiply(0.05d);
		final boolean ex = srch != null;
		final boolean hst = h;
		final LivingEntity tgt = srch;
		final Location el = ex ? prp : null;
		/*new BukkitRunnable() {
			public void run() {*/
				int i = 800;
				final HashSet<BaseBlockPosition> wls = new HashSet<>();
				final World w = pl.getWorld();
				double x = 20d * vec.getX() + loc.getX();
				double y = 20d * vec.getY() + loc.getY();
				double z = 20d * vec.getZ() + loc.getZ();
				
				while(true) {
					x += vec.getX();
					y += vec.getY();
					z += vec.getZ();
					if ((i & 31) == 0) {
						pl.spawnParticle(Particle.ASH, x, y, z, 1);
					}

					final Block b = w.getBlockAt((int)Math.floor(x), (int)Math.floor(y), (int)Math.floor(z));
					switch(b.getType()) {
					case OAK_LEAVES:
					case ACACIA_LEAVES:
					case BIRCH_LEAVES:
					case JUNGLE_LEAVES:
					case SPRUCE_LEAVES:
					case DARK_OAK_LEAVES:
					case GLASS:
					case WHITE_STAINED_GLASS:
					case WHITE_STAINED_GLASS_PANE:
					case DIAMOND_ORE:
					case COAL_ORE:
					case IRON_ORE:
					case EMERALD_ORE:
						final Arena ar = getPlShtrArena(pl.getName()).getSecond();
						if (ar != null && ar.gst == GameState.ROUND) {
							ar.brkn.add(new BrknBlck(b));
							new BukkitRunnable() {
								@Override
								public void run() {
									b.setType(Material.AIR, false);
								}
							}.runTask(plug);
						}
						wls.add(new BaseBlockPosition(b.getX(), b.getY(), b.getZ()));
						break;
					case ACACIA_SLAB:
					case BIRCH_SLAB:
					case CRIMSON_SLAB:
					case SPRUCE_SLAB:
					case WARPED_SLAB:
					case DARK_OAK_SLAB:
					case OAK_SLAB:
					case JUNGLE_SLAB:
					case PETRIFIED_OAK_SLAB:
					case ACACIA_STAIRS:
					case BIRCH_STAIRS:
					case CRIMSON_STAIRS:
					case SPRUCE_STAIRS:
					case WARPED_STAIRS:
					case DARK_OAK_STAIRS:
					case OAK_STAIRS:
					case JUNGLE_STAIRS:
					case ACACIA_PLANKS:
					case BIRCH_PLANKS:
					case CRIMSON_PLANKS:
					case SPRUCE_PLANKS:
					case WARPED_PLANKS:
					case DARK_OAK_PLANKS:
					case OAK_PLANKS:
					case JUNGLE_PLANKS:
					case ACACIA_WOOD:
					case BIRCH_WOOD:
					case CRIMSON_HYPHAE:
					case SPRUCE_WOOD:
					case WARPED_HYPHAE:
					case DARK_OAK_WOOD:
					case OAK_WOOD:
					case JUNGLE_WOOD:
					case ACACIA_LOG:
					case BIRCH_LOG:
					case CRIMSON_STEM:
					case SPRUCE_LOG:
					case WARPED_STEM:
					case DARK_OAK_LOG:
					case OAK_LOG:
					case JUNGLE_LOG:
					case ACACIA_SIGN:
					case ACACIA_WALL_SIGN:
					case BIRCH_SIGN:
					case BIRCH_WALL_SIGN:
					case CRIMSON_SIGN:
					case CRIMSON_WALL_SIGN:
					case SPRUCE_SIGN:
					case SPRUCE_WALL_SIGN:
					case WARPED_SIGN:
					case WARPED_WALL_SIGN:
					case DARK_OAK_SIGN:
					case DARK_OAK_WALL_SIGN:
					case OAK_SIGN:
					case OAK_WALL_SIGN:
					case JUNGLE_SIGN:
					case JUNGLE_WALL_SIGN:
					case STRIPPED_ACACIA_WOOD:
					case STRIPPED_BIRCH_WOOD:
					case STRIPPED_CRIMSON_HYPHAE:
					case STRIPPED_SPRUCE_WOOD:
					case STRIPPED_WARPED_HYPHAE:
					case STRIPPED_DARK_OAK_WOOD:
					case STRIPPED_OAK_WOOD:
					case STRIPPED_JUNGLE_WOOD:
					case STRIPPED_ACACIA_LOG:
					case STRIPPED_BIRCH_LOG:
					case STRIPPED_CRIMSON_STEM:
					case STRIPPED_SPRUCE_LOG:
					case STRIPPED_WARPED_STEM:
					case STRIPPED_DARK_OAK_LOG:
					case STRIPPED_OAK_LOG:
					case STRIPPED_JUNGLE_LOG:
					case ACACIA_FENCE:
					case BIRCH_FENCE:
					case CRIMSON_FENCE:
					case SPRUCE_FENCE:
					case WARPED_FENCE:
					case DARK_OAK_FENCE:
					case OAK_FENCE:
					case JUNGLE_FENCE:
					case ACACIA_FENCE_GATE:
					case BIRCH_FENCE_GATE:
					case CRIMSON_FENCE_GATE:
					case SPRUCE_FENCE_GATE:
					case WARPED_FENCE_GATE:
					case DARK_OAK_FENCE_GATE:
					case OAK_FENCE_GATE:
					case JUNGLE_FENCE_GATE:
					case BARREL:
						if (b.getBoundingBox().contains(x, y, z)) {
							wls.add(new BaseBlockPosition(b.getX(), b.getY(), b.getZ()));
						}
					case AIR:
					case CAVE_AIR:
					case GRASS:
					case FERN:
					case RAIL:
					case CRIMSON_ROOTS:
					case WARPED_ROOTS:
					case NETHER_SPROUTS:
					case RED_MUSHROOM:
					case BROWN_MUSHROOM:
					case CRIMSON_FUNGUS:
					case WARPED_FUNGUS:
					case DEAD_BUSH:
					case WATER:
					case TALL_GRASS:
					case IRON_BARS:
					case CHAIN:
					case STRUCTURE_VOID:
					case POWDER_SNOW:
					case BARRIER:
					case GLOW_LICHEN:
					case TRIPWIRE:
						break;
					default:
						if (b.getBoundingBox().contains(x, y, z)) {
							b.getWorld().spawnParticle(Particle.BLOCK_CRACK, new Location(b.getWorld(), x, y, z), 10, 0.1d, 0.1d, 0.1d, b.getBlockData());
							PacketUtils.blkCrckClnt(Main.ds.bg().a(pl.getName()), new SmplLoc(b, (short)640));
							return;
						}
					}
					
					if (ex && Math.pow(x - el.getX(), 2d) + Math.pow(z - el.getZ(), 2d) < 0.2d) {
						/*new BukkitRunnable() {
							@Override
							public void run() {*/
								if (pl.isValid() && tgt.isValid()) {
									double dmg = gt.dmg * Math.pow(0.5d, wls.size());
									final String nm;
									if (hst) {
										pl.playSound(pl.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1f, 2f);
										dmg *= 2f * (tgt.getEquipment().getHelmet() == null ? 1f : 0.5f);
										nm = "§c銑 " + String.valueOf((int)(dmg * 5.0F));
									} else {
										dmg *= tgt.getEquipment().getChestplate() == null ? 1f : 0.6f;
										nm = "§6" + String.valueOf((int)(dmg * 5.0f));
									}
									pl.playSound(pl.getLocation(), Sound.BLOCK_END_PORTAL_FRAME_FILL, 2f, 2f);
									Bukkit.getPluginManager().callEvent(new EntityShootAtEntityEvent(pl, tgt, dmg, hst, wls.size() > 0, dff && gt.snp));
									Main.dmgArm(pl, tgt.getEyeLocation(), nm);
								}
							/*}
						}.runTask(plug);*/
						return;
					}

					if (i < 0) {
						return;
					}

					i--;
				}
			/*}
		}.runTaskAsynchronously(plug);*/
	}
	
	@Override
	public int hashCode() {
		return shc;
	}
	
	@Override
	public boolean equals(final Object o) {
		return o instanceof Shooter ? ((Shooter) o).nm.equals(nm) : false;
	}
}