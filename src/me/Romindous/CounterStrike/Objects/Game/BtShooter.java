package me.Romindous.CounterStrike.Objects.Game;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.function.Predicate;
import com.destroystokyo.paper.entity.ai.Goal;
import io.papermc.paper.math.BlockPosition;
import io.papermc.paper.math.Position;
import me.Romindous.CounterStrike.Enums.GameState;
import me.Romindous.CounterStrike.Enums.GameType;
import me.Romindous.CounterStrike.Enums.GunType;
import me.Romindous.CounterStrike.Enums.NadeType;
import me.Romindous.CounterStrike.Game.Arena;
import me.Romindous.CounterStrike.Game.Arena.Team;
import me.Romindous.CounterStrike.Game.Defusal;
import me.Romindous.CounterStrike.Game.Invasion;
import me.Romindous.CounterStrike.Main;
import me.Romindous.CounterStrike.Objects.Bots.BotGoal;
import me.Romindous.CounterStrike.Objects.EntityShootAtEntityEvent;
import me.Romindous.CounterStrike.Objects.Loc.BrknBlck;
import me.Romindous.CounterStrike.Objects.Mobs.Mobber;
import me.Romindous.CounterStrike.Objects.Shooter;
import me.Romindous.CounterStrike.Objects.Skins.GunSkin;
import me.Romindous.CounterStrike.Utils.Inventories;
import me.Romindous.CounterStrike.Utils.Utils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import ru.komiss77.Ostrov;
import ru.komiss77.modules.bots.Botter;
import ru.komiss77.modules.world.WXYZ;
import ru.komiss77.notes.Slow;
import ru.komiss77.objects.SortedList;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.ItemUtil;
import ru.komiss77.utils.LocUtil;
import ru.komiss77.version.Nms;

public class BtShooter implements Shooter, Botter.Extent {
	
	public Shooter tgtSh;

	public WeakReference<LivingEntity> tgtLe;
	public boolean willBuy;

	private final Predicate<Player> isAlly = pl -> {
		final PlShooter ps = Shooter.getPlShooter(pl.getName(), true);
		return ps.arena() != null && ps.arena().shtrs.get(ps) == arena().shtrs.get(this);
	};

	private final Botter own;
	
	public BtShooter(final Botter owner, final Arena ar) {
		rclTm = 50; cldwn = 0; count = 0; shtTm = 0;
		money = 0; kills = 0; spwnrs = 0; deaths = 0;
		arena = ar; own = owner; willBuy = false;
		tgtSh = null; tgtLe = new WeakReference<>(null);
		pss = new LinkedList<>();
	}

	public Botter own() {
		return own;
	}

	@Override
	public String name() {
		return own.name();
	}

	public void create(Botter bt) {
		bt.setTagVis(isAlly);
	}

	public void spawn(Botter bt, @Nullable LivingEntity le) {}
	public void remove(Botter bt) {}
	public void bug(Botter bt) {
		bt.remove();
	}

	@Override
	public void teleport(Botter botter, LivingEntity livingEntity) {

	}

	@Override
	public void hide(final Botter bt, final @Nullable LivingEntity mb) {
		if (arena == null) return;
		if (arena instanceof final Defusal df) {
			if (df.isBmbOn()) {
				final Shooter sh = df.getBomb().defusing;
				if (sh != null && sh.equals(this))
					df.getBomb().defusing = null;
			}
		} else if (arena instanceof final Invasion in) {
			for (final Mobber m : in.mbbrs.values()) {
				final Shooter sh = m.defusing;
				if (sh != null && sh.equals(this))
					m.defusing = null;
			}
		}
	}

	@Override
	public Goal<Mob> goal(final Botter bt, final Mob org) {
		return new BotGoal(this, org);
	}
	
	@Override
	public void damage(final Botter bt, final EntityDamageEvent e) {}

	@Override
	public void click(final Botter bt, final PlayerInteractAtEntityEvent e) {
		final Player p = e.getPlayer();
		if (isAlly.test(p) && p.isSneaking()
			&& e.getHand() == EquipmentSlot.HAND) {
			final PlayerInventory pinv = p.getInventory();
			final int slt = pinv.getHeldItemSlot();
			switch (slt) {
				case 0, 1, 3, 4:
					final ItemStack bit = item(slt);
					final ItemStack pit = pinv.getItem(slt);
					if (ItemUtil.compareItem(bit, pit, false)) break;
					own.world().playSound(p.getEyeLocation(), Sound.ITEM_ARMOR_EQUIP_IRON, 1f, 1.2f);
					own.world().playSound(getLoc().toLocation(own.world()), Sound.ITEM_ARMOR_EQUIP_IRON, 1f, 1.2f);
					item(slt, pit);
					pinv.setItem(slt, bit);
					willBuy = true;
					tryBuy();
				default: break;
			}
		}
	}

	@Override
	public void death(Botter bt, EntityDeathEvent e) {
		Botter.Extent.super.death(bt, e);
	}

	public boolean tryReload(final LivingEntity le, final Location loc) {
		cldwn = Math.max(cldwn - 1, 0);
		final ItemStack it = item(EquipmentSlot.HAND);
		final GunType gt = GunType.getGnTp(it);
		if (gt == null) return false;
		if (it.hasItemMeta() && ((Damageable) it.getItemMeta()).hasDamage()) {
			count++;
			if ((count & 0x3) == 0) {
				le.getWorld().playSound(loc, Sound.BLOCK_CHAIN_FALL, 0.5f, 2f);
			}
			if (count < gt.rtm) Main.setDmg(it, it.getType().getMaxDurability() * count / gt.rtm);
			else {
				Main.setDmg(it, it.getType().getMaxDurability());
				le.getWorld().playSound(loc, Sound.ITEM_ARMOR_EQUIP_NETHERITE, 1f, 2f);
				it.setAmount(gt.amo);
				count = 0;
				item(EquipmentSlot.HAND, it);
				return false;
			}
			item(EquipmentSlot.HAND, it);
			return true;
		} 
		return false;
	}

	public boolean tryShoot(final LivingEntity le) {
		final ItemStack it = item(EquipmentSlot.HAND);
		final GunType gt = GunType.getGnTp(it);
		if (gt != null) {
			final boolean ps = ((Damageable) it.getItemMeta()).hasDamage();
			count = count + (ps ? 0 : 1);
			if (count % gt.cld == 0) {
				final int tr = count < rclTm ? count : rclTm;
				if (it.getAmount() == 1) {
					if (ps) return false;
					Main.setDmg(it, 0);
					count = 0;
				} else {
					if (ps) {
						Main.setDmg(it, it.getType().getMaxDurability());
						count = 0;
					}
					it.setAmount(it.getAmount() - 1);
				}
				item(EquipmentSlot.HAND, it);
				cldwn = gt.cld;
				final boolean iw = (gt.snp);
				for (byte i = gt.brst == 0 ? 1 : gt.brst; i > 0; i--) {
					shoot(gt, !iw, tr);
				}
				Main.plyWrldSnd(le, gt.snd, 1f);
				return true;
			}
		}
		return false;
	}
	
	public void switchToGun() {
		final GunType gt0 = GunType.getGnTp(item(0));
		if (gt0 != null) {
			own.swapToSlot(0);
			return;
		}
		final GunType gt1 = GunType.getGnTp(item(1));
		if (gt1 != null) {
			own.swapToSlot(1);
			return;
		}
		own.swapToSlot(2);
	}

	@Override
	public Player getPlayer() {return null;}

	@Override
	public LivingEntity getEntity() {
		return own.getEntity();
	}

	@Override
	public void teleport(final LivingEntity le, final Location to) {
		own.telespawn(le, to);
		own.tag(true);
		pss.clear();
		pss.add(to.toVector());
	}

	@Override
	public boolean isDead() {
		return own.isDead();
	}

	public final LinkedList<Vector> pss;

	public void rotPss() {
		final LivingEntity le = getEntity();
		if (le == null || !le.isValid()) return;
		final Vector vc = le.getLocation().toVector();
		if (pss.size() < MAX_DST) pss.add(vc);
		pss.poll(); pss.add(vc);
	}
	public Vector getLoc() {return pss.getLast().clone();}
	public Vector getLoc(final int dst) {
		return pss.get(Math.max(pss.size() - dst, 0)).clone();
	}

	@Override
	public WXYZ getPos() {
		return own.getPos();
	}

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

	@Override
	public ItemStack item(final EquipmentSlot slot) {
		return own.item(slot);
	}

	@Override
	public ItemStack item(final int slot) {
		return own.item(slot);
	}

	@Override
	public void item(final EquipmentSlot slot, final ItemStack it) {
		own.item(slot, it);
	}

	@Override
	public void item(final int slot, final ItemStack it) {
		own.item(slot, it);
	}

	@Override
	public Inventory inv() {
		return own.inv();
	}

	@Override
	public void clearInv() {
		own.clearInv();
	}

	@Override
	public void drop(final Location loc) {
		own.drop(loc);
	}

	public int getModel(final GunType gt) {return GunType.defCMD;}
	public GunSkin getSkin(final GunType gt) {return new GunSkin();}
	public boolean hasModel(final GunType gt, final int mdl) {return true;}
	public void giveModel(final GunType gt, final int cmd) {}
	public void setModel(final GunType gt, final int cmd) {}

	@Override
	public void taq(final String pfx, final String sfx, final String afx) {
		own.tab(pfx, afx, sfx);
		own.tag(pfx, afx, sfx);
	}

	@Override
	public Predicate<Player> allyTest() {
		return isAlly;
	}

	@Override
	@Slow(priority = 4)
	public void shoot(final GunType gt, final boolean dff, final int tr) {
		final LivingEntity ent = getEntity();
		if (ent == null) return;
		final Location loc = ent.getEyeLocation();
		if (tgtSh != null) {
			loc.setDirection(tgtSh.getLoc().subtract(getLoc()));
		} else {
			final LivingEntity le = tgtLe.get();
			if (le != null) {
				loc.setDirection(le.getLocation().toVector().subtract(getLoc()));
			}
		}
		if (dff) {
			loc.setPitch(loc.getPitch() + ((float) ent.getVelocity().getY() + (ent.isInWater() ? 0.005f : 0.0784f)) * 40f + Main.srnd.nextFloat() * 8f);
			if (gt.brst == 0) {
				loc.setYaw((Main.srnd.nextFloat() - 0.5f) * gt.xsprd * tr * 4f + loc.getYaw());
			} else {
				loc.setYaw((Main.srnd.nextFloat() - 0.5f) * gt.xsprd * rclTm + loc.getYaw());
			}
		} else {
			loc.setPitch(loc.getPitch() + ((float) ent.getVelocity().getY() + (ent.isInWater() ? 0.005f : 0.0784f)) * 40f + Main.srnd.nextFloat() * 4f);
			loc.setYaw((Main.srnd.nextFloat() - 0.5f) * gt.xsprd * rclTm * 0.1f + loc.getYaw());
		}
		final double lkx = -Math.sin(Math.toRadians((180f - loc.getYaw())));
		final double lkz = -Math.cos(Math.toRadians((180f - loc.getYaw())));
		final Vector vec = loc.getDirection().normalize().multiply(TRC_STEP);
		final SortedList<TargetLe> shot = new SortedList<>();
		for (final LivingEntity e : Main.getLEs(loc.getWorld())) {
			if (!e.getType().isAlive() ||
				e.getEntityId() == ent.getEntityId()) continue;
			final Shooter sh = Shooter.getShooter(e, false);
//				final Vector lc = sh.getLoc(sh instanceof PlShooter || gt.snp ? 4 : 2);
			if (sh != null && sh.isDead()) continue;
			final BoundingBox ebx = e.getBoundingBox();
			final double dx = ebx.getCenterX() - loc.getX();
			final double dz = ebx.getCenterZ() - loc.getZ();
			final double ln = Math.sqrt(dx * dx + dz * dz);
			if (Math.sqrt(Math.pow(lkx - dx / ln, 2d) + Math.pow(lkz - dz / ln, 2d)) * ln < 0.4d) {
				final double pty = loc.getY() + Math.tan(Math.toRadians(-loc.getPitch())) * ln;
				if (pty < ebx.getMaxY() && pty > ebx.getMinY()) {
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
				final boolean hst = y - ebx.getMinY() > ebx.getHeight() * 0.75d;
				final EntityShootAtEntityEvent ese;
				if (hst) {
					dmg *= 2f * (tgt.getEquipment().getHelmet() == null ? 1f : 0.5f);
					ese = new EntityShootAtEntityEvent(ent, tgt, dmg, true,
						wls.size() > 0, dff && gt.snp, smoke);
					ese.callEvent();
				} else {
					dmg *= tgt.getEquipment().getChestplate() == null ? 1f : 0.6f;
					ese = new EntityShootAtEntityEvent(ent, tgt, dmg, false,
						wls.size() > 0, dff && gt.snp, smoke);
					ese.callEvent();
				}
				dmg = (float) ese.getDamage();
				if (dmg <= 0f) return;//dmg 0, end
			}
		}
	}

	private double adj(final double v) {
		return v < 0 ? v + 1d : v;
	}
	
	@Override
	public void drop(final Botter bt, final Location loc) {
		ItemStack it = item(3);
		if (!ItemUtil.isBlank(it, false)) {
			own.world().dropItem(loc, it);
		}
		it = item(4);
		if (!ItemUtil.isBlank(it, false)) {
			if (it.getAmount() == 1) {
				own.world().dropItem(loc, it);
			} else {
				it.setAmount(1);
				own.world().dropItem(loc, it);
				own.world().dropItem(loc, it);
			}
		}
		
		if (arena() instanceof Defusal) {
			it = item(0);
			if (!ItemUtil.isBlank(it, false)) {
				it.setAmount(1);
				own.world().dropItem(loc, it);
			}
			it = item(1);
			if (!ItemUtil.isBlank(it, false)) {
				it.setAmount(1);
				own.world().dropItem(loc, it);
			}
		}
		
		it = item(7);
		if (!ItemUtil.isBlank(it, false)) {
			if (it.getType() == Main.bmb.getType()) {
				if (arena instanceof final Defusal df) {
                    //bomb dropped
					if (df.getTmAmt(Team.Ts, true, true) != 1) {
						df.dropBomb(own.world().dropItem(loc, Main.bmb));
					}
				}
			} else if (it.getType() == Material.SHEARS) {
				own.world().dropItem(loc, it);
			}
		}
		clearInv();
	}
	
	@Override
	public void pickup(final Botter bt, final Location loc) {
		for (final Item it : LocUtil.getChEnts(loc, 4d, Item.class, it -> {return it.getPickupDelay() == 0;})) {
			final ItemStack is = it.getItemStack();
			final GunType gt = GunType.getGnTp(is);
			if (gt == null) {
				final NadeType nt = NadeType.getNdTp(is);
				if (nt == null) {
					if (ItemUtil.isBlank(is, false)) return;
					switch (is.getType()) {
					case GOLDEN_APPLE:
						if (arena().shtrs.get(this) == Team.Ts) {
							item(7, is);
							((Defusal) arena()).pickBomb();
							it.remove();
							own.world().playSound(loc, Sound.ITEM_ARMOR_EQUIP_DIAMOND, 1f, 1.4f);
						} else {
							it.setPickupDelay(40);
						}
						break;
					case SHEARS:
						if (arena().shtrs.get(this) == Team.CTs) {
							item(7, is);
							it.remove();
							own.world().playSound(loc, Sound.ITEM_ARMOR_EQUIP_DIAMOND, 1f, 1.4f);
						} else {
							it.setPickupDelay(40);
						}
						break;
					default:
						break;
					}
					return;
				}
				final int slt = nt.prm ? NadeType.prmSlot : NadeType.scdSlot;
				final ItemStack eqp = item(slt);
				final NadeType ownNade = NadeType.getNdTp(eqp);
				if (ownNade == null) {
					item(slt, is);
					it.remove();
					this.own.world().playSound(loc, Sound.ITEM_ARMOR_EQUIP_DIAMOND, 1f, 1.4f);
				}
				return;
			}
			final int slt = gt.prm ? 0 : 1;
			final ItemStack eqp = item(slt);
			final GunType ownGun = GunType.getGnTp(eqp);
			if (ownGun == null) {
				item(slt, is.asQuantity(gt.amo));
				it.remove();
				own.world().playSound(loc, Sound.ITEM_ARMOR_EQUIP_NETHERITE, 1f, 1.4f);
			} else if (gt.prc > ownGun.prc) {
				own.world().dropItem(loc, eqp);
				item(slt, is.asQuantity(gt.amo));
				it.remove();
				own.world().playSound(loc, Sound.ITEM_ARMOR_EQUIP_NETHERITE, 1f, 1.4f);
			}
			switchToGun();
		}
	}

	public void tryBuy() {
//		Bukkit.broadcast(Component.text("mon " + money));
		switch (arena.getType()) {
		case INVASION:
			if (!((Invasion) arena).isDay) return;
			break;
		case DEFUSAL:
			if (!willBuy || arena.gst != GameState.BUYTIME) return;
			willBuy = false;
			break;
		case GUNGAME:
			return;
		}
		
		final Team tm = arena.shtrs.get(this);
		final boolean chs = Main.srnd.nextBoolean();
		if (!chs && tm == Team.CTs) buyItem(GunType.kitSlt, GunType.kitPrc, 7, tm);
		if (money < 1000) {//save
			buyItem(GunType.helmSlt, GunType.helmPrc, EquipmentSlot.HEAD, tm);
			if (chs) buyItem(GunType.chestSlt, GunType.chestPrc, EquipmentSlot.CHEST, tm);
			
			if (!buyItem(GunType.DGL.slt, GunType.DGL.prc, 1, tm)) {
				if (!buyItem(GunType.TP7.slt, GunType.TP7.prc, 1, tm)) {
					buyItem(GunType.USP.slt, GunType.USP.prc, 1, tm);
				}
			}

		} else {//buy up
			buyItem(GunType.helmSlt, GunType.helmPrc, EquipmentSlot.HEAD, tm);
			buyItem(GunType.chestSlt, GunType.chestPrc, EquipmentSlot.CHEST, tm);
			
			final GunType top = chs ? GunType.AWP : GunType.SCAR;
			if (!buyItem(top.slt, top.prc, 0, tm)) {
				final GunType gud = chs ? GunType.M4 : GunType.AK47;
				if (!buyItem(gud.slt, gud.prc, 0, tm)) {
					final GunType mid = chs ? GunType.P90 : GunType.MP5;
					if (!buyItem(mid.slt, mid.prc, 0, tm)) {
						final GunType low = chs ? GunType.SG13 : GunType.NOVA;
						buyItem(low.slt, low.prc, 0, tm);
					}
				}
			}
			
			if (arena.getType() != GameType.INVASION) {
				if (chs) {
					buyItem(NadeType.FRAG.slt, NadeType.FRAG.prc, NadeType.prmSlot, tm);
					buyItem(NadeType.FLASH.slt, NadeType.FLASH.prc, NadeType.scdSlot, tm);
				} else buyItem(NadeType.FLAME.slt, NadeType.FLAME.prc, NadeType.prmSlot, tm);
			}
		}
		
		switchToGun();
	}
	
	private boolean buyItem(final byte slot, final short prc, final EquipmentSlot to, final Team tm) {
		final ItemStack eq = item(to);
		final Inventory inv = switch (tm) {
            case Ts -> Inventories.TShop;
            case CTs -> Inventories.CTShop;
            case SPEC -> Inventories.LBShop;
        };
        final ItemStack it = inv.getItem(slot);
		if (ItemUtil.isBlank(it, false)) {
			Ostrov.log_warn("Tried to buy null item at slot " + slot);
			return false;
		}
		final GunType gt = GunType.getGnTp(it);
		if (gt == null) {
			final NadeType nt = NadeType.getNdTp(it);
			if (nt == null) {//armor?
                if (!ItemUtil.isBlank(eq, false) || prc > money) return false;
                money -= prc;
                item(to, it.clone());
                own.world().playSound(getLoc().toLocation(own.world()), Sound.ITEM_ARMOR_EQUIP_IRON, 1f, 1.4f);
                return true;
            }
			//nades
			if (prc > money) return false;
			final NadeType ownNade = NadeType.getNdTp(eq);
			if (ownNade != null) {
				if (ownNade.prc >= prc) return false;
				own.world().dropItem(getLoc().toLocation(own.world()), eq);
			}
			money -= prc;
			item(to, it.clone());
			own.world().playSound(getLoc().toLocation(own.world()), Sound.ITEM_ARMOR_EQUIP_DIAMOND, 1f, 1.4f);
			return true;
		}
		//guns
		if (prc > money) return false;
		final GunType ownGun = GunType.getGnTp(eq);
		if (ownGun != null) {
			if (ownGun.prc >= prc) return false;
			own.world().dropItem(getLoc().toLocation(own.world()), eq.asOne());
		}
		money -= prc;
		item(to, new ItemBuilder(it.getType()).name("§5" + gt.name() + " " + gt.icn)
			.amount(gt.amo).modelData(GunType.defCMD).build());
		own.world().playSound(getLoc().toLocation(own.world()), Sound.ITEM_ARMOR_EQUIP_NETHERITE, 1f, 1.4f);
		return true;
    }
	
	private boolean buyItem(final byte slot, final short prc, final int to, final Team tm) {
		final ItemStack eq = item(to);
		final Inventory inv = switch (tm) {
			case Ts -> Inventories.TShop;
			case CTs -> Inventories.CTShop;
			case SPEC -> Inventories.LBShop;
		};
		final ItemStack it = inv.getItem(slot);
		if (ItemUtil.isBlank(it, false)) {
			Ostrov.log_warn("Tried to buy null item at slot " + slot);
			return false;
		}
		final GunType gt = GunType.getGnTp(it);
		if (gt == null) {
			final NadeType nt = NadeType.getNdTp(it);
			if (nt == null) {//armor?
				if (!ItemUtil.isBlank(eq, false) || prc > money) return false;
				money -= prc;
				item(to, it.clone());
				own.world().playSound(getLoc().toLocation(own.world()), Sound.ITEM_ARMOR_EQUIP_IRON, 1f, 1.4f);
				return true;
			}
			//nades
			if (prc > money) return false;
			final NadeType ownNade = NadeType.getNdTp(eq);
			if (ownNade != null) {
				if (ownNade.prc >= prc) return false;
				own.world().dropItem(getLoc().toLocation(own.world()), eq);
			}
			money -= prc;
			item(to, it.clone());
			own.world().playSound(getLoc().toLocation(own.world()), Sound.ITEM_ARMOR_EQUIP_DIAMOND, 1f, 1.4f);
			return true;
		}
		//guns
		if (prc > money) return false;
		final GunType ownGun = GunType.getGnTp(eq);
		if (ownGun != null) {
			if (ownGun.prc >= prc) return false;
			own.world().dropItem(getLoc().toLocation(own.world()), eq.asOne());
		}
		money -= prc;
		item(to, new ItemBuilder(it.getType()).name("§5" + gt.name() + " " + gt.icn)
				.amount(gt.amo).modelData(GunType.defCMD).build());
		own.world().playSound(getLoc().toLocation(own.world()), Sound.ITEM_ARMOR_EQUIP_NETHERITE, 1f, 1.4f);
		return true;
		/*final ItemStack eq = item(to);
		final GunType gt = GunType.getGnTp(eq);
		final ItemStack it;
		if (gt == null) {
			if (money < prc) {
				it = null;
			} else {
				money -= prc;
                it = switch (tm) {
                    case CTs -> Inventories.CTShop.getItem(slot);
                    default -> Inventories.TShop.getItem(slot);
                };
			}
		} else if (gt.prc < prc) {
			if (money < prc) {
				it = null;
			} else {
				money -= prc;
				world.dropItem(getLoc().toLocation(world), eq);
                it = switch (tm) {
                    case CTs -> Inventories.CTShop.getItem(slot);
                    default -> Inventories.TShop.getItem(slot);
                };
			}
		} else {
			it = null;
		}

		if (ItemUtil.isBlank(it, false)) return false;
		final GunType ngt = GunType.getGnTp(it);
		if (ngt == null) {
			item(it.clone(), to);
			world.playSound(getLoc().toLocation(world), Sound.ITEM_ARMOR_EQUIP_DIAMOND, 1f, 1.4f);
		} else {
			item(new ItemBuilder(it.getType()).name("§5" + ngt.toString() + " " + ngt.icn)
				.amount(ngt.amo).modelData(GunType.defCMD).build(), to);
			world.playSound(getLoc().toLocation(world), Sound.ITEM_ARMOR_EQUIP_NETHERITE, 1f, 1.4f);
		}
		return true;*/
	}

	public boolean tryNade(final LivingEntity tgt) {
		final NadeType pnt = NadeType.getNdTp(item(NadeType.prmSlot));
		if (pnt == null) {
			final NadeType snt = NadeType.getNdTp(item(NadeType.scdSlot));
			if (snt == null) return false;
			final Vector dir = getNadeVec(tgt.getLocation()
				.toVector().subtract(getLoc()), 1d);
			dir.setY(dir.getY() * 1.25d);
			Nade.launch(getEntity(), this, dir, snt.time << 1, NadeType.scdSlot);
			return true;
		}

		Nade.launch(getEntity(), this, getNadeVec(tgt.getLocation()
			.toVector().subtract(getLoc()), 1.25d), pnt.time << 1, NadeType.prmSlot);
		return true;
	}

	private Vector getNadeVec(final Vector dst, final double spd) {
		final double DlnSq = dst.lengthSquared() * 0.01d / spd;
		if (dst.getY() > -DlnSq) dst.setY((dst.getY() + 1d) * DlnSq);
		return dst.normalize().multiply(spd);
	}

	/*@Override
	public int hashCode() {
		return super.hashCode();
	}

	@Override
	public boolean equals(final Object o) {
		return o instanceof Shooter && ((Shooter) o).name().equals(fR().getName());
	}*/
}
