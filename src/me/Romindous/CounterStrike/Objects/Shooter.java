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
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import me.Romindous.CounterStrike.Main;
import me.Romindous.CounterStrike.Enums.GameState;
import me.Romindous.CounterStrike.Enums.GunType;
import me.Romindous.CounterStrike.Game.Arena;
import me.Romindous.CounterStrike.Utils.PacketUtils;
import net.minecraft.core.BaseBlockPosition;
import net.minecraft.world.phys.Vec3D;
import ru.komiss77.version.VM;

public class Shooter {
	
	public final String nm;
	public final LinkedList<Vec3D> pss;
	public PlayerInventory inv;
	public Arena arena;
	public int shtTm;
	public int rclTm;
	public int cldwn;
	public byte dths;
	public short kls;
	public int count;
	public int money;
	private final int shc;
	
	public Shooter(final Player pl) {
		this.nm = pl.getName();
		this.inv = pl.getInventory();
		this.pss = new LinkedList<>();
		this.rclTm = 50;
		this.cldwn = 0;
		this.count = 0;
		this.shtTm = 0;
		this.shc = nm.hashCode();
		this.money = 0;
		this.kls = 0;
		this.dths = 0;
		this.arena = null;
		final Location loc = pl.getLocation();
		final Vec3D vc = new Vec3D(loc.getX(), loc.getY(), loc.getZ());
		this.pss.add(vc);
		this.pss.add(vc);
		this.pss.add(vc);
		this.pss.add(vc);
		this.pss.add(vc);
	}
  
	public static Shooter getShooter(final String nm) {
		final Shooter sh = Main.shtrs.get(nm);
		if (sh == null) {
			final Player p = Bukkit.getPlayer(nm);
			if (p == null) return null;
			final Shooter nvs = new Shooter(p);
			Main.shtrs.put(nm, nvs);
			return nvs;
		}
		return sh;
	}
	
	public void shoot(final GunType gt, final Main plug, final Player pl, final boolean dff, final int tr) {
		final Location loc = pl.getEyeLocation();
		if (dff) {
			if (gt.brst == 0) {
				loc.setPitch(loc.getPitch() - tr * gt.yrcl + ((float) pl.getVelocity().getY() + (pl.isInWater() ? 0.005f : 0.0784f)) * 40f);
				loc.setYaw((Main.srnd.nextFloat() - 0.5f) * gt.xsprd * tr + loc.getYaw());
			} else {
				loc.setPitch((Main.srnd.nextFloat() - 0.5f) * gt.xsprd * this.rclTm + loc.getPitch() - tr * 0.1F + ((float) pl.getVelocity().getY() + (pl.isInWater() ? 0.005f : 0.0784f)) * 40f);
				loc.setYaw((Main.srnd.nextFloat() - 0.5f) * gt.xsprd * this.rclTm + loc.getYaw());
			} 
		}
		final double lkx = -Math.sin(Math.toRadians((180f - loc.getYaw())));
		final double lkz = -Math.cos(Math.toRadians((180f - loc.getYaw())));
		
		LivingEntity srch = null;
		Location prp = null;
		boolean h = false;
		
		for (final LivingEntity e : Main.getWLnts(loc.getWorld().getUID())) {
			final BoundingBox ebx;
			final double dx;
			final double dz;
			switch (e.getType()) {
			case PLAYER:
				if (((HumanEntity) e).getGameMode() == GameMode.SURVIVAL && !e.getName().equals(pl.getName())) {
					final Vec3D lc = getShooter(e.getName()).pss.peek();
					ebx = new BoundingBox(lc.c, lc.d, lc.e, lc.c, lc.d + (((Player) e).isSneaking() ? 1.5d : 1.9d), lc.e);
					dx = lc.c - loc.getX();
					dz = lc.e - loc.getZ();
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
				int i = gt.snp ? 1600 : 1200;
				final boolean brkBlks = arena != null && arena.gst == GameState.ROUND;
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
					
					final Material mat = VM.getNmsServer().getFastMat(w, (int)Math.floor(x), (int)Math.floor(y), (int)Math.floor(z));
					final Block b;
					switch(mat) {
					case OAK_LEAVES, ACACIA_LEAVES, BIRCH_LEAVES, JUNGLE_LEAVES, 
					SPRUCE_LEAVES, DARK_OAK_LEAVES, MANGROVE_LEAVES, AZALEA_LEAVES,
					FLOWERING_AZALEA_LEAVES, 
					
					GLASS, WHITE_STAINED_GLASS, 
					WHITE_STAINED_GLASS_PANE, DIAMOND_ORE, COAL_ORE, IRON_ORE, EMERALD_ORE:
						if (brkBlks) {
							b = w.getBlockAt((int)Math.floor(x), (int)Math.floor(y), (int)Math.floor(z));
							arena.brkn.add(new BrknBlck(b));
							w.playSound(b.getLocation(), Sound.BLOCK_SHROOMLIGHT_FALL, 2f, 0.8f);
							w.spawnParticle(Particle.BLOCK_CRACK, b.getLocation().add(0.5d, 0.5d, 0.5d), 40, 0.4d, 0.4d, 0.4d, b.getType().createBlockData());
							b.setType(Material.AIR, false);
							wls.add(new BaseBlockPosition(b.getX(), b.getY(), b.getZ()));
						}
						break;
					case ACACIA_SLAB, BIRCH_SLAB, CRIMSON_SLAB, SPRUCE_SLAB, WARPED_SLAB, 
					DARK_OAK_SLAB, OAK_SLAB, JUNGLE_SLAB, PETRIFIED_OAK_SLAB, MANGROVE_SLAB, 
					
					ACACIA_STAIRS, BIRCH_STAIRS, CRIMSON_STAIRS, SPRUCE_STAIRS, 
					WARPED_STAIRS, DARK_OAK_STAIRS, OAK_STAIRS, JUNGLE_STAIRS, MANGROVE_STAIRS, 
					
					ACACIA_PLANKS, BIRCH_PLANKS, CRIMSON_PLANKS, SPRUCE_PLANKS, 
					WARPED_PLANKS, DARK_OAK_PLANKS, OAK_PLANKS, JUNGLE_PLANKS, MANGROVE_PLANKS, 
					
					ACACIA_TRAPDOOR, BIRCH_TRAPDOOR, CRIMSON_TRAPDOOR, DARK_OAK_TRAPDOOR, 
					JUNGLE_TRAPDOOR, MANGROVE_TRAPDOOR, OAK_TRAPDOOR, SPRUCE_TRAPDOOR, WARPED_TRAPDOOR, 
					
					ACACIA_WOOD, BIRCH_WOOD, CRIMSON_HYPHAE, SPRUCE_WOOD, 
					WARPED_HYPHAE, DARK_OAK_WOOD, OAK_WOOD, JUNGLE_WOOD, MANGROVE_WOOD, 
					
					ACACIA_LOG, BIRCH_LOG, CRIMSON_STEM, SPRUCE_LOG, 
					WARPED_STEM, DARK_OAK_LOG, OAK_LOG, JUNGLE_LOG, MANGROVE_LOG, 
					
					ACACIA_SIGN, ACACIA_WALL_SIGN, BIRCH_SIGN, BIRCH_WALL_SIGN, CRIMSON_SIGN, 
					CRIMSON_WALL_SIGN, SPRUCE_SIGN, SPRUCE_WALL_SIGN, WARPED_SIGN, 
					WARPED_WALL_SIGN, DARK_OAK_SIGN, DARK_OAK_WALL_SIGN, OAK_SIGN, 
					OAK_WALL_SIGN, JUNGLE_SIGN, JUNGLE_WALL_SIGN, MANGROVE_SIGN, MANGROVE_WALL_SIGN, 
					
					STRIPPED_ACACIA_WOOD, STRIPPED_BIRCH_WOOD, STRIPPED_CRIMSON_HYPHAE, STRIPPED_SPRUCE_WOOD, 
					STRIPPED_WARPED_HYPHAE, STRIPPED_DARK_OAK_WOOD, STRIPPED_OAK_WOOD, STRIPPED_JUNGLE_WOOD, 
					STRIPPED_MANGROVE_WOOD, 
					
					STRIPPED_ACACIA_LOG, STRIPPED_BIRCH_LOG, STRIPPED_CRIMSON_STEM, STRIPPED_SPRUCE_LOG, 
					STRIPPED_WARPED_STEM, STRIPPED_DARK_OAK_LOG, STRIPPED_OAK_LOG, STRIPPED_JUNGLE_LOG, 
					STRIPPED_MANGROVE_LOG, 
					
					ACACIA_FENCE, BIRCH_FENCE, CRIMSON_FENCE, SPRUCE_FENCE, WARPED_FENCE, DARK_OAK_FENCE, 
					OAK_FENCE, JUNGLE_FENCE, MANGROVE_FENCE, ACACIA_FENCE_GATE, BIRCH_FENCE_GATE, CRIMSON_FENCE_GATE, 
					SPRUCE_FENCE_GATE, WARPED_FENCE_GATE, DARK_OAK_FENCE_GATE, OAK_FENCE_GATE, JUNGLE_FENCE_GATE, MANGROVE_FENCE_GATE,
					
					BARREL, BEEHIVE, BEE_NEST:
						b = w.getBlockAt((int)Math.floor(x), (int)Math.floor(y), (int)Math.floor(z));
						if (b.getBoundingBox().contains(x, y, z)) {
							wls.add(new BaseBlockPosition(b.getX(), b.getY(), b.getZ()));
						}
					case AIR, CAVE_AIR, VOID_AIR,
					
					SEAGRASS, TALL_SEAGRASS, WEEPING_VINES, TWISTING_VINES, 
					
					BLACK_CARPET, BLUE_CARPET, BROWN_CARPET, CYAN_CARPET, GRAY_CARPET, 
					GREEN_CARPET, LIGHT_BLUE_CARPET, LIGHT_GRAY_CARPET, LIME_CARPET, 
					MAGENTA_CARPET, MOSS_CARPET, ORANGE_CARPET, PINK_CARPET, 
					PURPLE_CARPET, RED_CARPET, WHITE_CARPET, YELLOW_CARPET, 
					
					WATER, IRON_BARS, CHAIN, STRUCTURE_VOID, COBWEB, SNOW, 
					POWDER_SNOW, BARRIER, TRIPWIRE, LADDER, RAIL, POWERED_RAIL, 
					DETECTOR_RAIL, ACTIVATOR_RAIL:
						break;
					default:
						if (mat.isCollidable()) {
							b = w.getBlockAt((int)Math.floor(x), (int)Math.floor(y), (int)Math.floor(z));
							if (b.getBoundingBox().contains(x, y, z)) {
								b.getWorld().spawnParticle(Particle.BLOCK_CRACK, new Location(b.getWorld(), x, y, z), 10, 0.1d, 0.1d, 0.1d, b.getBlockData());
								PacketUtils.blkCrckClnt(Main.ds.bh().a(pl.getName()), new SmplLoc(b, (short)640));
								return;
							}
						}
					}
					
					if (ex && Math.pow(x - el.getX(), 2d) + Math.pow(z - el.getZ(), 2d) < 0.2d) {
						/*new BukkitRunnable() {
							@Override
							public void run() {*/
								if (pl.isValid() && tgt.isValid() && tgt.getNoDamageTicks() == 0) {
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