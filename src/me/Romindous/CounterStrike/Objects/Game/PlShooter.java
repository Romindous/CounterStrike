package me.Romindous.CounterStrike.Objects.Game;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.function.Predicate;
import io.papermc.paper.math.BlockPosition;
import io.papermc.paper.math.Position;
import me.Romindous.CounterStrike.Enums.GameState;
import me.Romindous.CounterStrike.Enums.GunType;
import me.Romindous.CounterStrike.Game.Arena;
import me.Romindous.CounterStrike.Game.Arena.Team;
import me.Romindous.CounterStrike.Game.Defusal;
import me.Romindous.CounterStrike.Main;
import me.Romindous.CounterStrike.Objects.EntityShootAtEntityEvent;
import me.Romindous.CounterStrike.Objects.Loc.BrknBlck;
import me.Romindous.CounterStrike.Objects.Shooter;
import me.Romindous.CounterStrike.Objects.Skins.GunSkin;
import me.Romindous.CounterStrike.Utils.Utils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import ru.komiss77.Ostrov;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.world.WXYZ;
import ru.komiss77.notes.Slow;
import ru.komiss77.objects.SortedList;
import ru.komiss77.utils.EntityUtil;
import ru.komiss77.utils.FastMath;
import ru.komiss77.utils.ItemUtil;
import ru.komiss77.version.Nms;

public class PlShooter implements Shooter {

	private static final double MOB_TRC = 12d;

	private final Predicate<Player> isAlly = pl -> {
		if (arena() == null) return true;
		final Arena ar = arena();
		switch (ar.gst) {
            case WAITING, BEGINING, FINISH: return true;
			case BUYTIME, ROUND, ENDRND: break;
		}
		final PlShooter ps = Shooter.getPlShooter(pl.getName(), true);
		return ps.arena() == null || ps.arena().shtrs.get(ps) == arena().shtrs.get(this);
	};
	
	public PlShooter(final Player p) {
		name = p.getName();
		rclTm = 50; cldwn = 0; count = 0; shtTm = 0;
		money = 0; kills = 0; spwnrs = 0; deaths = 0;
		arena = null;

		pss = new LinkedList<>();
		skins = new EnumMap<>(GunType.class);
		inv = p.getInventory();
		final Oplayer op = PM.getOplayer(p);
		op.setTagVis(isAlly);
		final Map<String, String> data = op.mysqlData;
		Ostrov.async(() -> {
			for (final GunType gt : GunType.values()) {
				final String skn = data.get(gt.toString());
				final GunSkin gs;
				if (skn == null || skn.isEmpty()) {
					gs = new GunSkin();
					data.put(gt.toString(), gs.toString());
				} else {
					gs = GunSkin.fromString(skn);
				}
				
				skins.put(gt, gs.has(gs.chosen) || op.hasGroup("warior") 
					? gs : new GunSkin(GunType.defCMD, gs.unlocked));
			}
		});

		new BukkitRunnable() {
			public void run() {
				rotPss();
				
				cldwn = Math.max(cldwn - 1, 0);
				final ItemStack it = item(EquipmentSlot.HAND);
				final GunType gt = GunType.getGnTp(it);
				final Player p = getPlayer();
				if (gt == null || p == null) {
					return;
				}

//				p.sendMessage("2");
				final boolean ps = ((Damageable) it.getItemMeta()).hasDamage();
				if (ps) {
					count++;
					if ((count & 0x3) == 0) {
						p.getWorld().playSound(p.getLocation(), Sound.BLOCK_CHAIN_FALL, 0.5f, 2f);
					}
					if (count < gt.rtm) Main.setDmg(it, it.getType().getMaxDurability() * count / gt.rtm);
					else {
						Main.setDmg(it, it.getType().getMaxDurability());
						p.getWorld().playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_NETHERITE, 1f, 2f);
						it.setAmount(gt.amo);
						count = 0;
					} 
				}
				
				if (shtTm == 0) {
					if (count != 0 && !ps) {
						count = count > rclTm + 1 ? rclTm : (count - 2 < 0) ? 0 : (count - 2);
					}
				} else {
					shtTm--;
					if (!ps) count++;
					if (scope() && scope(p.isSneaking())) return;
					if (count % gt.cld == 0) {
						final int tr = count < rclTm ? count : rclTm;
						if (it.getAmount() == 1) {
							if (ps) return;
							count = 0;
							
							if (gt.snp && p.isSneaking()) {
								if (ItemUtil.isBlank(p.getInventory().getItemInOffHand(), false))
									p.getInventory().setItemInOffHand(Main.spy);
								scope(true);
								return;
							} else {
								Main.setDmg(it, 0);
							}
						} else {
							if (ps) {
								Main.setDmg(it, it.getType().getMaxDurability());
								count = 0;
							}

							if (gt.snp && p.isSneaking()) {
								if (ItemUtil.isBlank(p.getInventory().getItemInOffHand(), false))
									p.getInventory().setItemInOffHand(Main.spy);
								scope(true);
								return;
							} else {
								it.setAmount(it.getAmount() - 1);
							}
						} 
						cldwn = gt.cld;
//						p.sendMessage("3");
						for (int i = gt.brst == 0 ? 1 : gt.brst; i > 0; i--) {
							shoot(gt, true, tr);
						}
						p.setCooldown(gt.getMat(), gt.cld);
						Main.plyWrldSnd(p, gt.snd, 1f);
					}
				}
			}
		}.runTaskTimer(Main.plug, 1L, 1L);
	}
	
	@Override
	public Player getPlayer() {
		final HumanEntity p = inv.getHolder();
		return p == null ? Bukkit.getPlayer(name) : (Player) p;
	}
	
	@Override
	public LivingEntity getEntity() {
		return getPlayer();
	}
	
	private final String name;
	@Override
    public String name() {return name;}

	public final LinkedList<Vector> pss;
	public void rotPss() {
		if (isDead()) return;
		final Vector vc = getEntity().getLocation().toVector();
		if (pss.size() < MAX_DST) pss.add(vc);
		pss.poll(); pss.add(vc);
	}
	public Vector getLoc() {return pss.getLast().clone();}
	public Vector getLoc(final int dst) {
		return pss.get(Math.max(pss.size() - dst, 0)).clone();
	}
	
	public WXYZ getPos() {return new WXYZ(getEntity().getLocation());}
	
	private boolean scope;
	public boolean scope() {return scope;}
	public boolean scope(final boolean scp) {return scope=scp;}
	
	private int shtTm;
	public int shtTm() {return shtTm;}
	public int shtTm(final int n) {return shtTm=n;}
	
	private int rclTm;
	public int rclTm() {return rclTm;}
	public int rclTm(final int n) {return rclTm=n;}
	
	private int cldwn;
	public int cldwn() {return cldwn;}
	public int cldwn(final int n) {return cldwn=n;}
	
	private int kills;
	public int kills() {return kills;}
	public void killsI() {kills++;}
	public void kills0() {kills=0;}
	
	private int deaths;
	public int deaths() {return deaths;}
	public void deathsI() {deaths++;}
	public void deaths0() {deaths=0;}
	
	private int spwnrs;
	public int spwnrs() {return spwnrs;}
	public void spwnrsI() {spwnrs++;}
	public void spwnrs0() {spwnrs=0;}
	
	private int money;
	public int money() {return money;}
	public int money(final int n) {return money=n;}
	
	private int count;
	public int count() {return count;}
	public int count(final int n) {return count=n;}
	
	private Arena arena;
	public Arena arena() {return arena;}
	public void arena(final Arena ar) {arena = ar;}
	
	private PlayerInventory inv;
	public ItemStack item(final EquipmentSlot slot) {return inv.getItem(slot);}
	public ItemStack item(final int slot) {return inv.getItem(slot);}
	public void item(final EquipmentSlot slot, final ItemStack it) {
		final GunType gt = GunType.getGnTp(item(slot));
		if (gt != null && gt.snp) inv.setItemInOffHand(Main.air);
		inv.setItem(slot, it);
	}
	public void item(final int slot, final ItemStack it) {
		final GunType gt = GunType.getGnTp(item(slot));
		if (gt != null && gt.snp) inv.setItemInOffHand(Main.air);
		inv.setItem(slot, it);
	}
	public PlayerInventory inv() {return inv;}
	public void inv(final PlayerInventory i) {inv = i;}
	public void clearInv() {inv.clear();}
	
	public void drop(final Location loc) {
		final World w = loc.getWorld();
		ItemStack it = item(3);
		if (!ItemUtil.isBlank(it, false)) {
			w.dropItem(loc, it);
		}
		it = item(4);
		if (!ItemUtil.isBlank(it, false)) {
			if (it.getAmount() == 1) {
				w.dropItem(loc, it);
			} else {
				it.setAmount(1);
				w.dropItem(loc, it);
				w.dropItem(loc, it);
			}
		}
		
		if (arena() instanceof Defusal) {
			it = item(0);
			if (!ItemUtil.isBlank(it, false)) {
				it.setAmount(1);
				w.dropItem(loc, it);
			}
			it = item(1);
			if (!ItemUtil.isBlank(it, false)) {
				it.setAmount(1);
				w.dropItem(loc, it);
			}
		}
		
		it = item(7);
		if (!ItemUtil.isBlank(it, false)) {
			if (it.getType() == Main.bmb.getType()) {
				if (arena instanceof final Defusal df) {
                    //bomb dropped
					if (df.getTmAmt(Team.Ts, true, true) != 1) {
						df.dropBomb(w.dropItem(loc, Main.bmb));
					}
					if (df.indon) {
						df.indSts(getPlayer());
					}
				}
			} else if (it.getType() == Material.SHEARS) {
				w.dropItem(loc, it);
			}
		}
		clearInv();
	}

	private final EnumMap<GunType, GunSkin> skins;
	public int getModel(final GunType gt) {return skins.get(gt).chosen;}
	public GunSkin getSkin(final GunType gt) {return skins.get(gt);}
	public boolean hasModel(final GunType gt, final int mdl) {return skins.get(gt).has(mdl);}
	public boolean isDead() {return getPlayer().getGameMode() != GameMode.SURVIVAL;}

	@Override
	public void giveModel(final GunType gt, final int cmd) {
		final GunSkin gs = skins.get(gt);
		final GunSkin ns;
		if (gs == null) {
			skins.put(gt, ns = new GunSkin(cmd, GunType.defCMD, cmd));
			PM.getOplayer(name).mysqlData.put(gt.toString(), ns.toString());
			return;
		}	final int[] lst = gs.unlocked;
		final int [] nw = new int[lst.length + 1];
		nw[0] = cmd;
        System.arraycopy(lst, 0, nw, 1, lst.length);
        skins.put(gt, ns = new GunSkin(cmd, nw));
		PM.getOplayer(name).mysqlData.put(gt.toString(), ns.toString());
	}

	@Override
	public void setModel(final GunType gt, final int cmd) {
		final GunSkin gs = skins.get(gt);
		final GunSkin ns;
		if (gs == null) {
			skins.put(gt, ns = new GunSkin());
			PM.getOplayer(name).mysqlData.put(gt.toString(), ns.toString());
			return;
		}	skins.put(gt, ns = new GunSkin(cmd, gs.unlocked));
		//Bukkit.getPlayer(nm).sendMessage("hs-" + Arrays.toString(ns.unlocked) + ", sel-" + gs.chosen);
		PM.getOplayer(name).mysqlData.put(gt.toString(), ns.toString());
	}

	@Override
	public void taq(final String pfx, final String sfx, final String afx) {
		final Player pl = getPlayer();
		final Oplayer op = PM.getOplayer(pl);
		op.tabPrefix(pfx, pl);
		op.tabSuffix(sfx, pl);
		op.beforeName(afx, pl);
		op.tag(pfx, sfx);
	}

	@Override
	public Predicate<Player> allyTest() {
		return isAlly;
	}
	
	@Override
	public void teleport(final LivingEntity le, final Location to) {
		le.teleport(to);
		pss.clear();
		pss.add(to.toVector());
	}

	@Override
	@Slow(priority = 4)
	public void shoot(final GunType gt, final boolean dff, final int tr) {
		final LivingEntity ent = getEntity();
		final Location loc = ent.getEyeLocation();
		if (dff) {
			if (gt.brst == 0) {
				loc.setPitch(loc.getPitch() - tr * gt.yrcl + ((float) ent.getVelocity().getY() + (ent.isInWater() ? 0.005f : 0.0784f)) * 40f);
				loc.setYaw((Main.srnd.nextFloat() - 0.5f) * gt.xsprd * tr + loc.getYaw());
			} else {
				loc.setPitch((Main.srnd.nextFloat() - 0.5f) * gt.xsprd * rclTm() + loc.getPitch() - tr * 0.1F
					+ ((float) ent.getVelocity().getY() + (ent.isInWater() ? 0.005f : 0.0784f)) * 40f);
				loc.setYaw((Main.srnd.nextFloat() - 0.5f) * gt.xsprd * rclTm() + loc.getYaw());
			}
		}
		final double lkx = -Math.sin(Math.toRadians((180f - loc.getYaw())));
		final double lkz = -Math.cos(Math.toRadians((180f - loc.getYaw())));
		final Vector vec = loc.getDirection().normalize().multiply(TRC_STEP);
		final SortedList<TargetLe> shot = new SortedList<>();
		for (final LivingEntity e : Main.getLEs(loc.getWorld())) {
			if (!e.getType().isAlive() ||
				e.getEntityId() == ent.getEntityId()) continue;
			final Shooter sh = Shooter.getShooter(e, false);
			final Vector lc;
			if (sh == null) {
				lc = e.getLocation().toVector().subtract(
					e.getVelocity().setY(0d).multiply(MOB_TRC));
			} else {
				if (sh.isDead()) continue;
				lc = sh.getLoc(sh instanceof PlShooter || gt.snp ? 5 : 3);
			}
			final double dx = lc.getX() - loc.getX();
			final double dz = lc.getZ() - loc.getZ();
			final double ln = Math.sqrt(dx * dx + dz * dz);
			if (Math.sqrt(Math.pow(lkx - dx / ln, 2d) + Math.pow(lkz - dz / ln, 2d)) * ln < 0.4d) {
				final double pty = loc.getY() + Math.tan(Math.toRadians(-loc.getPitch())) * ln;
				if (pty > lc.getY() && pty < lc.getY() + e.getHeight()) {
					shot.add(new TargetLe(e, (int) (ln * TRC_FCT)));
				}
			}
		}

		float dmg = gt.dmg;
		final boolean brkBlks = arena() != null && arena().gst == GameState.ROUND;
		final HashSet<BlockPosition> wls = new HashSet<>();
		final World w = ent.getWorld();
		double x = TRC_FCT * vec.getX() + loc.getX();
		double y = TRC_FCT * vec.getY() + loc.getY();
		double z = TRC_FCT * vec.getZ() + loc.getZ();
		LivingEntity tgt;
		BoundingBox ebx;

		boolean smoke = false;
		int tit = shot.size() - 1;
		final int ln = gt.snp ? 2000 : 1400;
        for (int i = (int) TRC_FCT; i != ln; i++) {
            //ent.sendMessage("route" + i);
            x += vec.getX();
            y += vec.getY();
            z += vec.getZ();
            if ((i & 63) == 0) {
                w.spawnParticle(Particle.ASH, x, y, z, 1);
            }

            final Material mat = Nms.getFastMat(w, (int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z));
            final Block b;
            switch (mat) {
			case POWDER_SNOW:
				smoke = true;
				break;
			case OAK_LEAVES, ACACIA_LEAVES, BIRCH_LEAVES, JUNGLE_LEAVES, CHERRY_LEAVES,
			SPRUCE_LEAVES, DARK_OAK_LEAVES, MANGROVE_LEAVES, AZALEA_LEAVES,
			FLOWERING_AZALEA_LEAVES,

			GLASS, WHITE_STAINED_GLASS, GLASS_PANE,
			WHITE_STAINED_GLASS_PANE, FLOWER_POT, DECORATED_POT,

			DIAMOND_ORE, COAL_ORE, IRON_ORE, EMERALD_ORE:
				if (brkBlks) {
					b = w.getBlockAt((int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z));
					arena().brkn.add(new BrknBlck(b));
					w.playSound(b.getLocation(), Sound.BLOCK_SHROOMLIGHT_FALL, 2f, 0.8f);
					w.spawnParticle(Particle.BLOCK, b.getLocation().add(0.5d, 0.5d, 0.5d),
							40, 0.4d, 0.4d, 0.4d, b.getType().createBlockData());
					b.setType(Material.AIR, false);
					wls.add(Position.block(b.getX(), b.getY(), b.getZ()));
					dmg *= 0.5f;
				}
				break;
			case ACACIA_SLAB, BIRCH_SLAB, CRIMSON_SLAB, SPRUCE_SLAB, WARPED_SLAB, CHERRY_SLAB, BAMBOO_SLAB,
			DARK_OAK_SLAB, OAK_SLAB, JUNGLE_SLAB, PETRIFIED_OAK_SLAB, MANGROVE_SLAB, BAMBOO_MOSAIC_SLAB,

			ACACIA_STAIRS, BIRCH_STAIRS, CRIMSON_STAIRS, SPRUCE_STAIRS, CHERRY_STAIRS, BAMBOO_STAIRS,
			WARPED_STAIRS, DARK_OAK_STAIRS, OAK_STAIRS, JUNGLE_STAIRS, MANGROVE_STAIRS, BAMBOO_MOSAIC_STAIRS,

			ACACIA_PLANKS, BIRCH_PLANKS, CRIMSON_PLANKS, SPRUCE_PLANKS, CHERRY_PLANKS, BAMBOO_PLANKS,
			WARPED_PLANKS, DARK_OAK_PLANKS, OAK_PLANKS, JUNGLE_PLANKS, MANGROVE_PLANKS, BAMBOO_MOSAIC,

			ACACIA_TRAPDOOR, BIRCH_TRAPDOOR, CRIMSON_TRAPDOOR, DARK_OAK_TRAPDOOR, CHERRY_TRAPDOOR, BAMBOO_TRAPDOOR,
			JUNGLE_TRAPDOOR, MANGROVE_TRAPDOOR, OAK_TRAPDOOR, SPRUCE_TRAPDOOR, WARPED_TRAPDOOR,

			ACACIA_WOOD, BIRCH_WOOD, CRIMSON_HYPHAE, SPRUCE_WOOD, CHERRY_WOOD, BAMBOO_BLOCK,
			WARPED_HYPHAE, DARK_OAK_WOOD, OAK_WOOD, JUNGLE_WOOD, MANGROVE_WOOD,

			ACACIA_LOG, BIRCH_LOG, CRIMSON_STEM, SPRUCE_LOG, CHERRY_LOG, BAMBOO,
			WARPED_STEM, DARK_OAK_LOG, OAK_LOG, JUNGLE_LOG, MANGROVE_LOG,

			ACACIA_SIGN, ACACIA_WALL_SIGN, BIRCH_SIGN, BIRCH_WALL_SIGN, CRIMSON_SIGN, BAMBOO_SIGN,
			CRIMSON_WALL_SIGN, SPRUCE_SIGN, SPRUCE_WALL_SIGN, WARPED_SIGN, WARPED_WALL_SIGN,
			DARK_OAK_SIGN, DARK_OAK_WALL_SIGN, OAK_SIGN, CHERRY_SIGN, CHERRY_WALL_SIGN, BAMBOO_WALL_SIGN,
			OAK_WALL_SIGN, JUNGLE_SIGN, JUNGLE_WALL_SIGN, MANGROVE_SIGN, MANGROVE_WALL_SIGN,

			STRIPPED_ACACIA_WOOD, STRIPPED_BIRCH_WOOD, STRIPPED_CRIMSON_HYPHAE, STRIPPED_SPRUCE_WOOD,
			STRIPPED_WARPED_HYPHAE, STRIPPED_DARK_OAK_WOOD, STRIPPED_OAK_WOOD, STRIPPED_JUNGLE_WOOD,
			STRIPPED_MANGROVE_WOOD, STRIPPED_CHERRY_WOOD, STRIPPED_BAMBOO_BLOCK,

			STRIPPED_ACACIA_LOG, STRIPPED_BIRCH_LOG, STRIPPED_CRIMSON_STEM, STRIPPED_SPRUCE_LOG,
			STRIPPED_WARPED_STEM, STRIPPED_DARK_OAK_LOG, STRIPPED_OAK_LOG, STRIPPED_JUNGLE_LOG,
			STRIPPED_MANGROVE_LOG, STRIPPED_CHERRY_LOG,

			ACACIA_FENCE, BIRCH_FENCE, CRIMSON_FENCE, SPRUCE_FENCE, WARPED_FENCE, DARK_OAK_FENCE,
			OAK_FENCE, JUNGLE_FENCE, MANGROVE_FENCE, CHERRY_FENCE, BAMBOO_FENCE,

			ACACIA_FENCE_GATE, BIRCH_FENCE_GATE, CRIMSON_FENCE_GATE, CHERRY_FENCE_GATE, BAMBOO_FENCE_GATE,
			SPRUCE_FENCE_GATE, WARPED_FENCE_GATE, DARK_OAK_FENCE_GATE, OAK_FENCE_GATE, JUNGLE_FENCE_GATE, MANGROVE_FENCE_GATE,

			OAK_DOOR, ACACIA_DOOR, BIRCH_DOOR, CRIMSON_DOOR, DARK_OAK_DOOR, CHERRY_DOOR,
			JUNGLE_DOOR, MANGROVE_DOOR, WARPED_DOOR, SPRUCE_DOOR, BAMBOO_DOOR,

			OAK_HANGING_SIGN, ACACIA_HANGING_SIGN, BIRCH_HANGING_SIGN, CRIMSON_HANGING_SIGN, DARK_OAK_HANGING_SIGN, CHERRY_HANGING_SIGN,
			JUNGLE_HANGING_SIGN, MANGROVE_HANGING_SIGN, WARPED_HANGING_SIGN, SPRUCE_HANGING_SIGN, BAMBOO_HANGING_SIGN,

			OAK_WALL_HANGING_SIGN, ACACIA_WALL_HANGING_SIGN, BIRCH_WALL_HANGING_SIGN, CRIMSON_WALL_HANGING_SIGN,
			DARK_OAK_WALL_HANGING_SIGN, CHERRY_WALL_HANGING_SIGN, JUNGLE_WALL_HANGING_SIGN, MANGROVE_WALL_HANGING_SIGN,
			WARPED_WALL_HANGING_SIGN, SPRUCE_WALL_HANGING_SIGN, BAMBOO_WALL_HANGING_SIGN,

			BARREL, BEEHIVE, BEE_NEST, NOTE_BLOCK, JUKEBOX, CRAFTING_TABLE:
				b = w.getBlockAt((int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z));
				final BlockPosition bp = Position.block(b.getX(), b.getY(), b.getZ());
				if (wls.contains(bp)) break;
				if (b.getCollisionShape().overlaps(new BoundingBox().shift(adj(x - (int) x), adj(y - (int) y), adj(z - (int) z)))) {
					wls.add(bp);
					b.getWorld().spawnParticle(Particle.BLOCK, new Location(b.getWorld(), x, y, z), 4, 0.1d, 0.1d, 0.1d, b.getBlockData());
					Utils.crackBlock(new WXYZ(b));
					dmg *= 0.5f;
				}
			case AIR, CAVE_AIR, VOID_AIR,

			SEAGRASS, TALL_SEAGRASS, WEEPING_VINES, TWISTING_VINES,

			BLACK_CARPET, BLUE_CARPET, BROWN_CARPET, CYAN_CARPET, GRAY_CARPET,
			GREEN_CARPET, LIGHT_BLUE_CARPET, LIGHT_GRAY_CARPET, LIME_CARPET,
			MAGENTA_CARPET, MOSS_CARPET, ORANGE_CARPET, PINK_CARPET,
			PURPLE_CARPET, RED_CARPET, WHITE_CARPET, YELLOW_CARPET,

			WATER, IRON_BARS, CHAIN, STRUCTURE_VOID, COBWEB, SNOW,
			BARRIER, TRIPWIRE, LADDER, RAIL, POWERED_RAIL,
			DETECTOR_RAIL, ACTIVATOR_RAIL, CAMPFIRE, SOUL_CAMPFIRE:
				break;
			default:
				if (mat.isCollidable()) {
					/*final BoundingBox shift = new BoundingBox().shift(x - (int) x, y - (int) y, z - (int) z);
					ent.sendMessage("shift- " + shift.toString());
					for (final BoundingBox bs : b.getCollisionShape().getBoundingBoxes()) {
						ent.sendMessage("bs- " + bs.toString());
					}*/
					b = w.getBlockAt((int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z));
					if (b.getCollisionShape().overlaps(new BoundingBox().shift(adj(x - (int) x), adj(y - (int) y), adj(z - (int) z)))) {
						b.getWorld().spawnParticle(Particle.BLOCK, new Location(b.getWorld(), x, y, z), 10, 0.1d, 0.1d, 0.1d, b.getBlockData());
						Utils.crackBlock(new WXYZ(b));
						return;
					}
				}
            }

            for (; tit >= 0; tit--) {
                final TargetLe tle = shot.get(tit);
                if (i < tle.dst()) break;

                tgt = tle.le();
                ebx = tgt.getBoundingBox();
                if (tgt.getNoDamageTicks() != 0) continue;
                final String nm;
                final boolean hst = y - ebx.getMinY() > ebx.getHeight() * 0.75d;
                final EntityShootAtEntityEvent ese;
                final Location tlc = tgt.getEyeLocation();
                if (hst) {
                    dmg *= 2f * (tgt.getEquipment().getHelmet() == null ? 1f : 0.5f);
                    ese = new EntityShootAtEntityEvent(ent, tgt, dmg, true,
						wls.size() > 0, dff && gt.snp, smoke);
                    ese.callEvent();
                    nm = "<red>銑" + FastMath.abs((int) ((dmg - ese.getDamage()) * 5.0d));
                } else {
                    dmg *= tgt.getEquipment().getChestplate() == null ? 1f : 0.6f;
                    ese = new EntityShootAtEntityEvent(ent, tgt, dmg, false,
						wls.size() > 0, dff && gt.snp, smoke);
                    ese.callEvent();
                    nm = "<gold>" + FastMath.abs((int) ((dmg - ese.getDamage()) * 5.0d));
                }
                dmg = (float) ese.getDamage();

                if (ent instanceof final Player pl) {
                    if (hst) pl.playSound(loc, Sound.ENTITY_PLAYER_ATTACK_CRIT, 1f, 2f);
                    pl.playSound(loc, Sound.BLOCK_END_PORTAL_FRAME_FILL, 2f, 2f);
					EntityUtil.indicate(tlc, nm, pl);
                }

                if (dmg <= 0f) return;//dmg 0, end
            }
        }
	}

	private double adj(final double v) {
		return v < 0 ? v + 1d : v;
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}
	
	@Override
	public boolean equals(final Object o) {
		return o instanceof Shooter && ((Shooter) o).name().equals(name);
	}
}