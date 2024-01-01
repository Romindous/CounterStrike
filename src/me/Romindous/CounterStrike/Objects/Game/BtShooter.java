package me.Romindous.CounterStrike.Objects.Game;

import com.destroystokyo.paper.entity.ai.Goal;
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
import me.Romindous.CounterStrike.Objects.Shooter;
import me.Romindous.CounterStrike.Objects.Skins.GunSkin;
import me.Romindous.CounterStrike.Utils.Inventories;
import me.Romindous.CounterStrike.Utils.PacketUtils;
import net.minecraft.core.BaseBlockPosition;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import ru.komiss77.modules.bots.BotEntity;
import ru.komiss77.modules.world.WXYZ;
import ru.komiss77.notes.Slow;
import ru.komiss77.utils.FastMath;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.ItemUtils;
import ru.komiss77.utils.LocationUtil;
import ru.komiss77.version.IServer;
import ru.komiss77.version.VM;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.TreeMap;

public class BtShooter extends BotEntity implements Shooter {
	
	private static int botID = 0;
	
	public Shooter tgtSh;
	public WeakReference<LivingEntity> tgtLe;
	public boolean willBuy;
	
	public BtShooter(final Arena ar) {
		super("Bot-v" + botID++, ar.w);
		rclTm = 50; cldwn = 0; count = 0; shtTm = 0;
		money = 0; kills = 0; spwnrs = 0; deaths = 0;
		arena = ar; rid = -1; willBuy = false;
		tgtSh = null; tgtLe = new WeakReference<>(null);
		pss = new LinkedList<>();
//		setGoal(mb -> new BotGoal(this, mb));
//		onDamage(e -> {});
//		tagVisIf(pl -> {
//			final Team tm = ar.shtrs.get(Shooter.getPlShooter(pl.getName(), true));
//			return tm != null && ar.shtrs.get(this) == tm;
//		});
	}
	
	@Override
	public Goal<Mob> getGoal(final Mob org) {
		return new BotGoal(this, org);
	}
	
	@Override
	public void onDamage(final EntityDamageEvent e) {}
	
	@Override
	public boolean isTagVisFor(final Player p) {
		final Team tm = arena.shtrs.get(Shooter.getPlShooter(p.getName(), true));
		return tm != null && arena.shtrs.get(this) == tm;
	}
	
	public boolean tryReload(final LivingEntity le, final Location loc) {
		cldwn = Math.max(cldwn - 1, 0);
		final ItemStack it = item(EquipmentSlot.HAND);
		final GunType gt = GunType.getGnTp(it);
		if (gt == null) return false;
		if (it.hasItemMeta() && ((Damageable) it.getItemMeta()).hasDamage()) {
			count++;
			if ((count & 0x3) == 0) {
				le.getWorld().playSound(le.getLocation(), Sound.BLOCK_CHAIN_FALL, 0.5f, 2f);
			} 
			Main.setDmg(it, it.getType().getMaxDurability() * count / gt.rtm);
			if (count >= gt.rtm) {
				le.getWorld().playSound(le.getLocation(), Sound.ITEM_ARMOR_EQUIP_NETHERITE, 1f, 2f);
				it.setAmount(gt.amo);
				count = 0;
				item(it, EquipmentSlot.HAND);
				return false;
			}
			item(it, EquipmentSlot.HAND);
			return true;
		} 
		return false;
	}

	public boolean tryShoot(final LivingEntity le, final Location loc) {
		final ItemStack it = item(EquipmentSlot.HAND);
		final GunType gt = GunType.getGnTp(it);
		if (gt != null) {
			final boolean ps = ((Damageable)it.getItemMeta()).hasDamage();
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
				item(it, EquipmentSlot.HAND);
				cldwn = gt.cld;
				final boolean iw = (gt.snp);
				for (byte i = gt.brst == 0 ? 1 : gt.brst; i > 0; i--) {
					shoot(gt, !iw, tr);
				}
				Main.plyWrldSnd(le.getLocation(), gt.snd);
				return true;
			}
		}
		return false;
	}
	
	public void switchToGun() {
		final GunType gt0 = GunType.getGnTp(item(0));
		if (gt0 != null) {
			swapToSlot(0);
			return;
		}
		final GunType gt1 = GunType.getGnTp(item(1));
		if (gt1 != null) {
			swapToSlot(1);
			return;
		}
		swapToSlot(2);
	}

	@Override
	public Player getPlayer() {return null;}
	
	@Override
	public LivingEntity getEntity() {
		return super.getEntity();
	}

	@Override
	public void teleport(final LivingEntity le, final Location to) {
		super.telespawn(to, le);
	}

	public final LinkedList<Vector> pss;
	public void rotPss() {
		final LivingEntity le = getEntity();
		if (le == null || !le.isValid()) return;
		pss.poll();
		pss.add(le.getLocation().toVector());
	}
	
	public Vector getLoc(final boolean dir) 
	{return dir ? pss.getLast().clone() : pss.getFirst().clone();}
	
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
	
	public int getModel(final GunType gt) {return GunType.defCMD;}
	public GunSkin getSkin(final GunType gt) {return new GunSkin();}
	public boolean hasModel(final GunType gt, final int mdl) {return true;}
	public void giveModel(final GunType gt, final int cmd) {}
	public void setModel(final GunType gt, final int cmd) {}

	@Override
	@Slow(priority = 4)
	public void shoot(final GunType gt, final boolean dff, final int tr) {
		final LivingEntity ent = getEntity();
		if (ent == null) return;
		final Location loc = ent.getEyeLocation();
		if (tgtSh != null) {
			loc.setDirection(tgtSh.getLoc(true).subtract(ent.getLocation().toVector()));
		} else {
			final LivingEntity le = tgtLe.get();
			if (le != null) {
				loc.setDirection(le.getLocation().toVector().subtract(ent.getLocation().toVector()));
			}
		}
		//Bukkit.broadcast(Component.text("tgt-" + tgt.getPos().toString() + "\n\nent-" + ent.getLocation().toVector().toString()));
		if (dff) {
			loc.setPitch(loc.getPitch() + ((float) ent.getVelocity().getY() + (ent.isInWater() ? 0.005f : 0.0784f)) * 40f + Main.srnd.nextFloat() * 8f);
			if (gt.brst == 0) {
				loc.setYaw((Main.srnd.nextFloat() - 0.5f) * gt.xsprd * tr * 4f + loc.getYaw());
			} else {
				//loc.setPitch((Main.srnd.nextFloat() - 0.5f) * gt.xsprd * rclTm() + loc.getPitch() - tr * 0.1F + ((float) ent.getVelocity().getY() + (ent.isInWater() ? 0.005f : 0.0784f)) * 40f);
				loc.setYaw((Main.srnd.nextFloat() - 0.5f) * gt.xsprd * rclTm + loc.getYaw());
			}
		} else {
			loc.setPitch(loc.getPitch() + ((float) ent.getVelocity().getY() + (ent.isInWater() ? 0.005f : 0.0784f)) * 40f + Main.srnd.nextFloat() * 4f);
			loc.setYaw((Main.srnd.nextFloat() - 0.5f) * gt.xsprd * rclTm * 0.1f + loc.getYaw());
		}
		final double lkx = -Math.sin(Math.toRadians((180f - loc.getYaw())));
		final double lkz = -Math.cos(Math.toRadians((180f - loc.getYaw())));
		final TreeMap<Integer, LivingEntity> shot = new TreeMap<Integer, LivingEntity>();
		for (final LivingEntity e : Main.getWLnts(loc.getWorld().getUID())) {
			final BoundingBox ebx;
			final double dx;
			final double dz;
			switch (e.getType()) {
			case PLAYER:
			case HUSK:
				if (e.getEntityId() == ent.getEntityId()) continue;
				final Shooter sh = Shooter.getShooter(e, false);
				if (sh == null || sh.isDead()) {
					continue;
				}
				final Vector lc = sh.getLoc(false);
				ebx = new BoundingBox(lc.getX(), lc.getY(), lc.getZ(), lc.getX(), lc.getY() + 
				(sh instanceof PlShooter && e.isSneaking() ? 1.5d : 1.9d), lc.getZ());
				dx = lc.getX() - loc.getX();
				dz = lc.getZ() - loc.getZ();
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
					shot.put(FastMath.square((int) dx) + FastMath.square((int) dz), e);
				}
			}
		}
		
		final Vector vec = loc.getDirection().normalize().multiply(0.05d);
		float dmg = gt.dmg;
		/*new BukkitRunnable() {
			public void run() {*/
		int i = gt.snp ? 1600 : 1200;
		final boolean brkBlks = arena() != null && arena().gst == GameState.ROUND;
		final HashSet<BaseBlockPosition> wls = new HashSet<>();
		final World w = ent.getWorld();
		double x = 20d * vec.getX() + loc.getX();
		double y = 20d * vec.getY() + loc.getY();
		double z = 20d * vec.getZ() + loc.getZ();
		final IServer is = VM.getNmsServer();
		LivingEntity tgt;
		BoundingBox ebx;
		
		lp : while(true) {
			//ent.sendMessage("route" + i);
			x += vec.getX();
			y += vec.getY();
			z += vec.getZ();
			if ((i & 63) == 0) {
				w.spawnParticle(Particle.ASH, x, y, z, 1);
			}
			
			final Material mat = is.getFastMat(w, (int)Math.floor(x), (int)Math.floor(y), (int)Math.floor(z));
			final Block b;
			switch(mat) {
			case OAK_LEAVES, ACACIA_LEAVES, BIRCH_LEAVES, JUNGLE_LEAVES, 
			SPRUCE_LEAVES, DARK_OAK_LEAVES, MANGROVE_LEAVES, AZALEA_LEAVES,
			FLOWERING_AZALEA_LEAVES, 
			
			GLASS, WHITE_STAINED_GLASS, GLASS_PANE, 
			WHITE_STAINED_GLASS_PANE, DIAMOND_ORE, 
			COAL_ORE, IRON_ORE, EMERALD_ORE:
				if (brkBlks) {
					b = w.getBlockAt((int)Math.floor(x), (int)Math.floor(y), (int)Math.floor(z));
					arena().brkn.add(new BrknBlck(b));
					w.playSound(b.getLocation(), Sound.BLOCK_SHROOMLIGHT_FALL, 2f, 0.8f);
					w.spawnParticle(Particle.BLOCK_CRACK, b.getLocation().add(0.5d, 0.5d, 0.5d), 40, 0.4d, 0.4d, 0.4d, b.getType().createBlockData());
					b.setType(Material.AIR, false);
					wls.add(new BaseBlockPosition(b.getX(), b.getY(), b.getZ()));
					dmg *= 0.5f;
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
			
			OAK_DOOR, ACACIA_DOOR, BIRCH_DOOR, CRIMSON_DOOR, DARK_OAK_DOOR, 
			JUNGLE_DOOR, MANGROVE_DOOR, WARPED_DOOR, SPRUCE_DOOR, 
			
			BARREL, BEEHIVE, BEE_NEST, NOTE_BLOCK, JUKEBOX, CRAFTING_TABLE:
				b = w.getBlockAt((int)Math.floor(x), (int)Math.floor(y), (int)Math.floor(z));
				if (b.getBoundingBox().contains(x, y, z) && wls.add(new BaseBlockPosition(b.getX(), b.getY(), b.getZ()))) {
					PacketUtils.blkCrckClnt(new WXYZ(b, 640));
					dmg *= 0.5f;
				}
			case AIR, CAVE_AIR, VOID_AIR,
			
			SEAGRASS, TALL_SEAGRASS, WEEPING_VINES, TWISTING_VINES, 
			
			BLACK_CARPET, BLUE_CARPET, BROWN_CARPET, CYAN_CARPET, GRAY_CARPET, 
			GREEN_CARPET, LIGHT_BLUE_CARPET, LIGHT_GRAY_CARPET, LIME_CARPET, 
			MAGENTA_CARPET, MOSS_CARPET, ORANGE_CARPET, PINK_CARPET, 
			PURPLE_CARPET, RED_CARPET, WHITE_CARPET, YELLOW_CARPET, 
			
			WATER, IRON_BARS, CHAIN, STRUCTURE_VOID, COBWEB, SNOW, 
			POWDER_SNOW, BARRIER, TRIPWIRE, LADDER, RAIL, POWERED_RAIL, 
			DETECTOR_RAIL, ACTIVATOR_RAIL, CAMPFIRE, SOUL_CAMPFIRE:
				break;
			default:
				if (mat.isCollidable()) {
					b = w.getBlockAt((int)Math.floor(x), (int)Math.floor(y), (int)Math.floor(z));
					if (b.getBoundingBox().contains(x, y, z)) {
						b.getWorld().spawnParticle(Particle.BLOCK_CRACK, new Location(b.getWorld(), x, y, z), 10, 0.1d, 0.1d, 0.1d, b.getBlockData());
						PacketUtils.blkCrckClnt(new WXYZ(b, 640));
						break lp;
					}
				}
			}
			
			while (shot.size() != 0 && dmg > 0f) {
				tgt = shot.firstEntry().getValue();
				ebx = tgt.getBoundingBox();
				final Shooter sh = Shooter.getShooter(tgt, false);
				final boolean nr; 
				if (sh == null) {
					nr = Math.pow(x - ebx.getCenterX(), 2d) + Math.pow(z - ebx.getCenterZ(), 2d) < 0.2d;
				} else {
					final Vector vc = sh.getLoc(false);
					nr = Math.pow(x - vc.getX(), 2d) + Math.pow(z - vc.getZ(), 2d) < 0.2d;
				}
				
				if (nr) {
					shot.pollFirstEntry();
					if (tgt.getNoDamageTicks() == 0) {
						final String nm;
						final boolean hst = y - ebx.getMinY() > ebx.getHeight() * 0.75d;
						final EntityShootAtEntityEvent ese;
						if (hst) {
							dmg *= 2f * (tgt.getEquipment().getHelmet() == null ? 1f : 0.5f);
							ese = new EntityShootAtEntityEvent(ent, tgt, dmg,
								true, wls.size() > 0, dff && gt.snp);
							ese.callEvent();
							nm = "§c銑" + FastMath.absInt((int) ((dmg - ese.getDamage()) * 5.0d));
						} else {
							dmg *= tgt.getEquipment().getChestplate() == null ? 1f : 0.6f;
							ese = new EntityShootAtEntityEvent(ent, tgt, dmg,
								false, wls.size() > 0, dff && gt.snp);
							ese.callEvent();
							nm = "§6" + FastMath.absInt((int) ((dmg - ese.getDamage()) * 5.0d));
						}
						dmg = (float) ese.getDamage();
						
						if (ent instanceof final Player pl) {
                            if (hst) pl.playSound(loc, Sound.ENTITY_PLAYER_ATTACK_CRIT, 1f, 2f);
							pl.playSound(loc, Sound.BLOCK_END_PORTAL_FRAME_FILL, 2f, 2f);
							Main.dmgInd(pl, tgt.getEyeLocation(), nm);
						}
					}
				} else break;
			}

			if ((i--) < 0) {
				break;
			}
		}
	}
	
	@Override
	public void dropIts(final Location loc) {
		final World w = loc.getWorld();
		ItemStack it = item(3);
		if (!ItemUtils.isBlank(it, false)) {
			w.dropItem(loc, it);
		}
		it = item(4);
		if (!ItemUtils.isBlank(it, false)) {
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
			if (!ItemUtils.isBlank(it, false)) {
				w.dropItem(loc, it);
			}
			it = item(1);
			if (!ItemUtils.isBlank(it, false)) {
				w.dropItem(loc, it);
			}
		}
		
		it = item(7);
		if (!ItemUtils.isBlank(it, false)) {
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
	
	@Override
	public void pickupIts(final Location loc) {
		for (final Item it : LocationUtil.getChEnts(loc, 4d, Item.class, it -> {return it.getPickupDelay() == 0;})) {
			final ItemStack is = it.getItemStack();
			final GunType gt = GunType.getGnTp(is);
			if (gt == null) {
				final NadeType nt = NadeType.getNdTp(is);
				if (nt == null) {
					if (ItemUtils.isBlank(is, false)) return;
					switch (is.getType()) {
					case GOLDEN_APPLE:
						if (arena().shtrs.get(this) == Team.Ts) {
							item(is, 7);
							((Defusal) arena()).pickBomb();
							it.remove();
							w.playSound(loc, Sound.ITEM_ARMOR_EQUIP_DIAMOND, 1f, 1.4f);
						} else {
							it.setPickupDelay(40);
						}
						break;
					case SHEARS:
						if (arena().shtrs.get(this) == Team.CTs) {
							item(is, 7);
							it.remove();
							w.playSound(loc, Sound.ITEM_ARMOR_EQUIP_DIAMOND, 1f, 1.4f);
						} else {
							it.setPickupDelay(40);
						}
						break;
					default:
						break;
					}
					return;
				}
				final int slt = nt.prm ? 3 : 4;
				final ItemStack eqp = item(slt);
				final NadeType own = NadeType.getNdTp(eqp);
				if (own == null) {
					item(is, slt);
					it.remove();
					w.playSound(loc, Sound.ITEM_ARMOR_EQUIP_DIAMOND, 1f, 1.4f);
				}
				return;
			}
			final int slt = gt.prm ? 0 : 1;
			final ItemStack eqp = item(slt);
			final GunType own = GunType.getGnTp(eqp);
			if (own == null) {
				item(is.asQuantity(gt.amo), slt);
				it.remove();
				w.playSound(loc, Sound.ITEM_ARMOR_EQUIP_NETHERITE, 1f, 1.4f);
			} else if (gt.prc > own.prc) {
				w.dropItem(loc, eqp);
				item(is.asQuantity(gt.amo), slt);
				it.remove();
				w.playSound(loc, Sound.ITEM_ARMOR_EQUIP_NETHERITE, 1f, 1.4f);
			}
			switchToGun();
		}
	}

	public void tryBuy() {
//		Bukkit.broadcast(Component.text("mon " + money));
		if (!willBuy) return;
		switch (arena.getType()) {
		case INVASION:
			if (!((Invasion) arena).isDay) return;
			break;
		case DEFUSAL:
			if (arena.gst != GameState.BUYTIME) return;
			willBuy = false;
			break;
		case GUNGAME:
			return;
		}
		
		final Team tm = arena.shtrs.get(this);
		if (money < 1000) {//save
			tryBuyItem(GunType.hlmtSlt, GunType.hlmtPrc, EquipmentSlot.HEAD, tm);
			if (Main.srnd.nextBoolean()) tryBuyItem(GunType.chstSlt, GunType.chstPrc, EquipmentSlot.CHEST, tm);
			else if (Main.srnd.nextBoolean()) tryBuyItem(NadeType.FLASH.slt, NadeType.FLASH.prc, NadeType.scdSlot, tm);
			
			if (!tryBuyItem(GunType.DGL.slt, GunType.DGL.prc, 1, tm)) {
				if (!tryBuyItem(GunType.TP7.slt, GunType.TP7.prc, 1, tm)) {
					tryBuyItem(GunType.USP.slt, GunType.USP.prc, 1, tm);
				}
			}
			
		} else {//not save
			tryBuyItem(GunType.hlmtSlt, GunType.hlmtPrc, EquipmentSlot.HEAD, tm);
			tryBuyItem(GunType.chstSlt, GunType.chstPrc, EquipmentSlot.CHEST, tm);
			
			final GunType top = Main.srnd.nextBoolean() ? GunType.AWP : GunType.SCAR;
			if (!tryBuyItem(top.slt, top.prc, 0, tm)) {
				final GunType gud = Main.srnd.nextBoolean() ? GunType.M4 : GunType.AK47;
				if (!tryBuyItem(gud.slt, gud.prc, 0, tm)) {
					final GunType mid = Main.srnd.nextBoolean() ? GunType.P90 : GunType.MP5;
					if (!tryBuyItem(mid.slt, mid.prc, 0, tm)) {
						final GunType low = Main.srnd.nextBoolean() ? GunType.SG13 : GunType.NOVA;
						tryBuyItem(low.slt, low.prc, 0, tm);
					}
				}
			}
			
			if (tryBuyItem(NadeType.FRAG.slt, NadeType.FRAG.prc, NadeType.prmSlot, tm))
				tryBuyItem(NadeType.FLASH.slt, NadeType.FLASH.prc, NadeType.scdSlot, tm);
		}
		
		switchToGun();
	}
	
	private boolean tryBuyItem(final byte slot, final short prc, final EquipmentSlot to, final Team tm) {
		final ItemStack eq = item(to);
		final Inventory inv = switch (tm) {
            case CTs -> Inventories.CTShop;
            default -> Inventories.TShop;
        };
        final ItemStack it = inv.getItem(slot);
		final GunType gt = GunType.getGnTp(it);
		if (gt == null) {
			final NadeType nt = NadeType.getNdTp(it);
			if (nt == null) {
				if (ItemUtils.isBlank(eq, false) && prc <= money) {//armor?
					money -= prc;
					item(it.clone(), to);
					w.playSound(getEntity().getLocation(), Sound.ITEM_ARMOR_EQUIP_IRON, 1f, 1.4f);
					return true;
				}
			} else if (prc <= money) {//nades
				final NadeType own = NadeType.getNdTp(eq);
				money -= prc;
				if (own != null) {
					if (own.prc > prc) return false;
					w.dropItem(getEntity().getLocation(), eq);
				}
				item(it.clone(), to);
				w.playSound(getEntity().getLocation(), Sound.ITEM_ARMOR_EQUIP_DIAMOND, 1f, 1.4f);
				return true;
			}
		} else if (prc <= money) {//guns
			final GunType own = GunType.getGnTp(eq);
			if (own != null) {
				if (own.prc > prc) return false;
				w.dropItem(getEntity().getLocation(), eq.asOne());
			}
			money -= prc;
			item(new ItemBuilder(it.getType()).name("§5" + gt.toString() + " " + gt.icn)
				.setAmount(gt.amo).setModelData(GunType.defCMD).build(), to);
			w.playSound(getEntity().getLocation(), Sound.ITEM_ARMOR_EQUIP_NETHERITE, 1f, 1.4f);
			return true;
		}
		return false;
	}
	
	private boolean tryBuyItem(final byte slot, final short prc, final int to, final Team tm) {
		final ItemStack eq = item(to);
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
				w.dropItem(getEntity().getLocation(), eq);
                it = switch (tm) {
                    case CTs -> Inventories.CTShop.getItem(slot);
                    default -> Inventories.TShop.getItem(slot);
                };
			}
		} else {
			it = null;
		}
		
		if (ItemUtils.isBlank(it, false)) return false;
		final GunType ngt = GunType.getGnTp(it);
		if (ngt == null) {
			item(it.clone(), to);
			w.playSound(getEntity().getLocation(), Sound.ITEM_ARMOR_EQUIP_DIAMOND, 1f, 1.4f);
		} else {
			item(new ItemBuilder(it.getType()).name("§5" + ngt.toString() + " " + ngt.icn)
				.setAmount(ngt.amo).setModelData(GunType.defCMD).build(), to);
			w.playSound(getEntity().getLocation(), Sound.ITEM_ARMOR_EQUIP_NETHERITE, 1f, 1.4f);
		}
		return true;
	}
	
	@Override
	public int hashCode() {
		return fM().getName().hashCode();
	}
	
	@Override
	public boolean equals(final Object o) {
		return o instanceof Shooter && ((Shooter) o).name().equals(fM().getName());
	}
}
