package me.Romindous.CounterStrike.Objects.Game;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Husk;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.datafixers.util.Pair;

import me.Romindous.CounterStrike.Main;
import me.Romindous.CounterStrike.Enums.GameState;
import me.Romindous.CounterStrike.Enums.GunType;
import me.Romindous.CounterStrike.Enums.NadeType;
import me.Romindous.CounterStrike.Game.Arena;
import me.Romindous.CounterStrike.Game.Arena.Team;
import me.Romindous.CounterStrike.Game.Defusal;
import me.Romindous.CounterStrike.Objects.EntityShootAtEntityEvent;
import me.Romindous.CounterStrike.Objects.Shooter;
import me.Romindous.CounterStrike.Objects.Bots.BotGoal;
import me.Romindous.CounterStrike.Objects.Bots.BotManager;
import me.Romindous.CounterStrike.Objects.Bots.BotType;
import me.Romindous.CounterStrike.Objects.Loc.BrknBlck;
import me.Romindous.CounterStrike.Objects.Loc.WXYZ;
import me.Romindous.CounterStrike.Objects.Skins.GunSkin;
import me.Romindous.CounterStrike.Utils.Inventories;
import me.Romindous.CounterStrike.Utils.PacketUtils;
import net.minecraft.EnumChatFormat;
import net.minecraft.core.BaseBlockPosition;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.protocol.game.PacketPlayOutAnimation;
import net.minecraft.network.protocol.game.PacketPlayOutEntity;
import net.minecraft.network.protocol.game.PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook;
import net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy;
import net.minecraft.network.protocol.game.PacketPlayOutEntityEquipment;
import net.minecraft.network.protocol.game.PacketPlayOutEntityHeadRotation;
import net.minecraft.network.protocol.game.PacketPlayOutNamedEntitySpawn;
import net.minecraft.network.protocol.game.PacketPlayOutPlayerInfo;
import net.minecraft.network.protocol.game.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.phys.Vec3D;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.version.IServer;
import ru.komiss77.version.VM;

public class BtShooter extends EntityPlayer implements Shooter {
	
	private static int botID = 0;
	public static final EnumChatFormat clr = EnumChatFormat.h;
	
	public final World w;
	
	public WeakReference<LivingEntity> rplc;
	public Shooter tgt;
	public int rid;
	
	public boolean willBuy;
	
	public BtShooter(final Arena ar, final World w) {
		super(Main.ds, PacketUtils.getNMSWrld(w), new GameProfile(UUID.randomUUID(), "Bot_v" + String.valueOf(botID++)), null);
		name = fy().getName();
		shc = name.hashCode(); handSlot = 0;
		rclTm = 50; cldwn = 0; count = 0; shtTm = 0;
		money = 0; kills = 0; spwnrs = 0; deaths = 0;
		arena = ar; rid = -1; dead = false; this.w = w;
		willBuy = false; tgt = null;
		
		pss = new LinkedList<>();
		rplc = new WeakReference<LivingEntity>(null);
		inv = Bukkit.createInventory(null, InventoryType.PLAYER);
		final BotType bt = BotType.REGULAR;
    	final Pair<String, String> pr = bt.txs[Main.srnd.nextInt(bt.txs.length)];
    	if (pr != null) {
        	this.fy().getProperties().put("textures", new Property("textures", pr.getFirst(), pr.getSecond()));
    	}
		
		new BukkitRunnable() {
			public void run() {
				
				/*cldwn(Math.max(cldwn() - 1, 0));
				final ItemStack it = item(EquipmentSlot.HAND);
				final GunType gt = GunType.getGnTp(it);
				final Player p = getPlayer();
				if (gt == null || p == null) {
					return;
				}
				final boolean ps = ((Damageable)it.getItemMeta()).hasDamage();
				if (ps) {
					count(count() + 1);
					if ((count() & 0x3) == 0) {
						p.getWorld().playSound(p.getLocation(), Sound.BLOCK_CHAIN_FALL, 0.5f, 2f);
					} 
					Main.setDmg(it, it.getType().getMaxDurability() * count() / gt.rtm);
					if (count() >= gt.rtm) {
						p.getWorld().playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_NETHERITE, 1f, 2f);
						it.setAmount(gt.amo);
						count(0);
					} 
				} 
				final int cnt;
				if (shtTm() != 0) {
					shtTm(shtTm() - 1);
					cnt = count(count() + (ps ? 0 : 1));
					if (cnt % gt.cld == 0) {
						final int tr = cnt < rclTm() ? cnt : rclTm();
						if (it.getAmount() == 1) {
							if (ps) {
								return;
							}
							Main.setDmg(it, 0);
							count(0);
						} else {
							if (ps) {
								Main.setDmg(it, it.getType().getMaxDurability());
								count(0);
							} 
							it.setAmount(it.getAmount() - 1);
						} 
						cldwn(gt.cld);
						final boolean iw = (gt.snp && p.isSneaking());
						for (byte i = gt.brst == 0 ? 1 : gt.brst; i > 0; i--) {
							shoot(gt, !iw, tr);
						}
						Main.plyWrldSht(p.getLocation(), gt.snd);
						if (iw) {
							PacketUtils.fkHlmtClnt(p, p.getInventory().getHelmet());
							PacketUtils.zoom(p, false);
							p.setSneaking(false);
						}
						//p.setVelocity(p.getVelocity().subtract(p.getEyeLocation().getDirection().multiply(gt.kb)));
					}
				} else if ((cnt = count()) != 0 && !ps) {
					count(cnt > rclTm() + 1 ? rclTm() : (cnt - 2 < 0) ? 0 : (cnt - 2));
				}*/
			}
		}.runTaskTimer(Main.plug, 1L, 1L);
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
		final boolean ps = ((Damageable)it.getItemMeta()).hasDamage();
		if (gt != null) {
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
				Main.plyWrldSht(le.getLocation(), gt.snd);
				//p.setVelocity(p.getVelocity().subtract(p.getEyeLocation().getDirection().multiply(gt.kb)));
				return true;
			}
		}
		
		/*if (count != 0 && !ps) {
			count = count > rclTm + 1 ? rclTm : Math.max(count - 2, 0);
		}*/
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
		final LivingEntity le = rplc.get();
		return le == null || !le.isValid() ? null : le;
	}
	
	private final String name;
	public String name() {return name;}

	public final LinkedList<Vector> pss;
	public void rotPss() {
		final LivingEntity le = getEntity();
		if (le == null || !le.isValid()) return;
		pss.poll();
		pss.add(le.getLocation().toVector());
	}
	public Vector getPos() {return pss.peek().clone();}
	
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
	
	private Inventory inv;
	public ItemStack item(final EquipmentSlot slot) {
		final LivingEntity mb = getEntity();
		if (mb == null) return null;
		return mb.getEquipment().getItem(slot);
	}
	public ItemStack item(final int slot) {return inv.getItem(slot);}
	
	private int handSlot;
	public void swapToSlot(final int slot) {
		handSlot = slot;
		final LivingEntity mb = getEntity();
		if (mb != null) mb.getEquipment().setItem(EquipmentSlot.HAND, item(slot));
		updateEqp();
	}
	public void item(final ItemStack it, final EquipmentSlot slot) {
		final LivingEntity mb = getEntity();
		if (mb != null) mb.getEquipment().setItem(slot, it);
		if (slot == EquipmentSlot.HAND) inv.setItem(handSlot, it);
		updateEqp();
	}
	public void item(final ItemStack it, final int slot) {
		inv.setItem(slot, it);
		if (slot == handSlot) {
			final LivingEntity mb = getEntity();
			if (mb != null) mb.getEquipment().setItem(EquipmentSlot.HAND, item(slot));
			updateEqp();
		}
	}
	public Inventory inv() {return inv;}
	public void clearInv() {
		inv.clear();
		final LivingEntity mb = getEntity();
		if (mb != null) mb.getEquipment().clear();
		updateEqp();
	}
	
	public void dropIts(final Location loc, final Team tm, final boolean guns) {
		final World w = loc.getWorld();
		ItemStack it = inv.getItem(3);
		if (!Inventories.isBlankItem(it, false)) {
			w.dropItem(loc, it);
		}
		it = inv.getItem(4);
		if (!Inventories.isBlankItem(it, false)) {
			if (it.getAmount() == 1) {
				w.dropItem(loc, it);
			} else {
				it.setAmount(1);
				w.dropItem(loc, it);
				w.dropItem(loc, it);
			}
		}
		if (guns) {
			it = inv.getItem(0);
			if (!Inventories.isBlankItem(it, false)) {
				w.dropItem(loc, it);
			}
			it = inv.getItem(1);
			if (!Inventories.isBlankItem(it, false)) {
				w.dropItem(loc, it);
			}
		}
		it = inv.getItem(7);
		switch (tm) {
		case Ts:
			if (!Inventories.isBlankItem(it, false) && arena instanceof Defusal) {
				final Defusal df = (Defusal) arena;
				//bomb dropped
				if (df.indon) {
					df.indSts(getPlayer());
				}
				final Item drop = w.dropItem(loc, Main.bmb);
				if (df.getTmAmt(Team.Ts, true, true) != 1) {
					for (final Entry<Shooter, Team> n : df.shtrs.entrySet()) {
						final Player pl = n.getKey().getPlayer();
						if (pl != null) {
							pl.playSound(loc, "cs.info." + (n.getValue() == Team.Ts ? "tdropbmb" : "ctdropbmb"), 10f, 1f);
							pl.getScoreboard().getTeam("bmb").addEntry(drop.getUniqueId().toString());
						}
					}
					drop.setGlowing(true);
				}
			}
			break;
		case CTs:
			if (it != null && it.getType() == Material.SHEARS) {
				w.dropItem(loc, it);
			}
			break;
		case NA:
			break;
		}
		inv.clear();
	}
	
	public int getModel(final GunType gt) {return GunType.defCMD;}
	public GunSkin getSkin(final GunType gt) {return new GunSkin();}
	public boolean hasModel(final GunType gt, final int mdl) {return true;}
	public void giveModel(final GunType gt, final int cmd) {}
	public void setModel(final GunType gt, final int cmd) {}
	
	public void hurt() {BotManager.sendWrldPckts(this.s, new PacketPlayOutAnimation(this, 1));}
	
	@Override
	public void teleport(final LivingEntity le, final Location to) {
		BotManager.sendWrldPckts(this.s, 
			new PacketPlayOutEntityDestroy(this.ae()), 
			new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.e, this));
		
		if (le == null || !le.isValid() || dead) {
			BotManager.npcs.remove(rid);
			dead = false;
			final Husk hs = (Husk) w.spawnEntity(to, EntityType.HUSK, false);
			this.rplc = new WeakReference<LivingEntity>(hs);
			this.rid = hs.getEntityId();
			hs.setSilent(true);
			hs.setPersistent(true);
			hs.setRemoveWhenFarAway(false);
			Bukkit.getMobGoals().removeAllGoals(hs);
			Bukkit.getMobGoals().addGoal(hs, 0, new BotGoal(this, hs));
			BotManager.npcs.put(rid, this);
			hs.teleportAsync(to);
	    	this.setPosRaw(to.getX(), to.getY(), to.getZ(), true);
		} else {
			le.teleportAsync(to);
	    	this.setPosRaw(to.getX(), to.getY(), to.getZ(), true);
		}
		
		BotManager.sendWrldPckts(this.s, 
			new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.a, this), 
			new PacketPlayOutNamedEntitySpawn(this),
			new PacketPlayOutEntityDestroy(rid));
		swapToSlot(0);
		willBuy = true; tgt = null;
	}

	@Override
	public void shoot(final GunType gt, final boolean dff, final int tr) {
		final LivingEntity ent = getEntity();
		if (ent == null) return;
		final Location loc = ent.getEyeLocation();
		loc.setDirection(tgt.getPos().subtract(ent.getLocation().toVector()));
		//Bukkit.broadcast(Component.text("tgt-" + tgt.getPos().toString() + "\n\nent-" + ent.getLocation().toVector().toString()));
		if (dff) {
			loc.setPitch(loc.getPitch() + ((float) ent.getVelocity().getY() + (ent.isInWater() ? 0.005f : 0.0784f)) * 40f + Main.srnd.nextFloat() * 8f);
			if (gt.brst == 0) {
				loc.setYaw((Main.srnd.nextFloat() - 0.5f) * gt.xsprd * tr * 4f + loc.getYaw());
			} else {
				//loc.setPitch((Main.srnd.nextFloat() - 0.5f) * gt.xsprd * rclTm() + loc.getPitch() - tr * 0.1F + ((float) ent.getVelocity().getY() + (ent.isInWater() ? 0.005f : 0.0784f)) * 40f);
				loc.setYaw((Main.srnd.nextFloat() - 0.5f) * gt.xsprd * rclTm + loc.getYaw());
			}
		}
		final double lkx = -Math.sin(Math.toRadians((180f - loc.getYaw())));
		final double lkz = -Math.cos(Math.toRadians((180f - loc.getYaw())));
		LivingEntity srch = null;
		Vector prp = null;
		boolean h = false;
		for (final LivingEntity e : Main.getWLnts(loc.getWorld().getUID())) {
			final BoundingBox ebx;
			final double dx;
			final double dz;
			switch (e.getType()) {
			case PLAYER:
				if (((HumanEntity) e).getGameMode() != GameMode.SURVIVAL) continue;
			case HUSK:
				if (e.getEntityId() == ent.getEntityId()) continue;
				final Shooter sh = Shooter.getShooter(e, false);
				if (sh == null) continue;
				final Vector lc = sh.getPos();
				ebx = new BoundingBox(lc.getX(), lc.getY(), lc.getZ(), lc.getX(), lc.getY() + 
				(sh instanceof PlShooter && ((Player) e).isSneaking() ? 1.5d : 1.9d), lc.getZ());
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
					srch = e;
					prp = new Vector(ebx.getCenterX(), ebx.getCenterY(), ebx.getCenterZ());
					h = pty - ebx.getMinY() > ebx.getHeight() * 0.75d;
					break;
				} 
			} 
		}
		
		final Vector vec = loc.getDirection().normalize().multiply(0.05d);
		final boolean ex = srch != null;
		final boolean hst = h;
		final LivingEntity tgt = srch;
		final Vector el = ex ? prp : null;
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
				
				while(true) {
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
					
					GLASS, WHITE_STAINED_GLASS, 
					WHITE_STAINED_GLASS_PANE, DIAMOND_ORE, COAL_ORE, IRON_ORE, EMERALD_ORE:
						if (brkBlks) {
							b = w.getBlockAt((int)Math.floor(x), (int)Math.floor(y), (int)Math.floor(z));
							arena().brkn.add(new BrknBlck(b));
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
					
					BARREL, BEEHIVE, BEE_NEST, NOTE_BLOCK, JUKEBOX:
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
								if (ent instanceof Player) PacketUtils.blkCrckClnt(PacketUtils.getNMSPl((Player) ent), new WXYZ(b, 640));
								return;
							}
						}
					}
					
					if (ex && Math.pow(x - el.getX(), 2d) + Math.pow(z - el.getZ(), 2d) < 0.2d) {
						/*new BukkitRunnable() {
							@Override
							public void run() {*/
								if (/*pl.isValid() && tgt.isValid() && */tgt.getNoDamageTicks() == 0) {
									double dmg = gt.dmg * Math.pow(0.5d, wls.size());
									final String nm;
									if (hst) {
										dmg *= 2f * (tgt.getEquipment().getHelmet() == null ? 1f : 0.5f);
										nm = "§c銑 " + String.valueOf((int)(dmg * 5.0F));
									} else {
										dmg *= tgt.getEquipment().getChestplate() == null ? 1f : 0.6f;
										nm = "§6" + String.valueOf((int)(dmg * 5.0f));
									}
									if (ent instanceof Player) {
										final Player pl = (Player) ent;
										if (hst) pl.playSound(loc, Sound.ENTITY_PLAYER_ATTACK_CRIT, 1f, 2f);
										pl.playSound(loc, Sound.BLOCK_END_PORTAL_FRAME_FILL, 2f, 2f);
										Main.dmgArm(pl, tgt.getEyeLocation(), nm);
									}
									Bukkit.getPluginManager().callEvent(new EntityShootAtEntityEvent(ent, tgt, dmg, hst, wls.size() > 0, dff && gt.snp));
								}
							/*}
						}.runTask(plug);*/
						return;
					}

					if ((i--) < 0) {
						return;
					}
				}
			/*}
		}.runTaskAsynchronously(plug);*/
	}

	public boolean tryJump(final Location loc, final LivingEntity rplc, final Vector vc) {
		if (rplc.isOnGround()) {
			double lx = loc.getX(), lz = loc.getZ();
			final int dHt = 3;
			final WXYZ blc = new WXYZ(0, loc.getBlockY() - dHt, 0, w, 0);
			//Bukkit.broadcast(Component.text("1"));
			final int tHt = dHt + 3;
			for (int i = 0; i < 5; i++) {
				lx += vc.getX(); lz += vc.getZ();
				blc.x = (int) Math.floor(lx); blc.z = (int) Math.floor(lz);
				if (checkIfPass(blc, BlockFace.UP, tHt, false) != tHt) {
					if (i == 0) break;
					blc.y += tHt;
					final int upY = tHt - checkIfPass(blc, BlockFace.DOWN, tHt + 2, false);
					//Bukkit.broadcast(Component.text("u=" + upY));
					//Bukkit.broadcast(Component.text("i=" + i));
					if (upY < 4) {
						/*final Vector vec = upY > 2 ? 
							vc.clone().multiply((i) * 0.16d + (upY) * 0.1d - 0.02d).setY(0.42d) :
							vc.clone().multiply((i) * 0.16d + (upY) * 0.1d - 0.02d).setY(0.42d);*/
						rplc.setVelocity(upY > 2 ? vc.clone().multiply((i) * 0.15d + (upY) * 0.14d - 0.08d).setY(0.42d) 
								: vc.clone().multiply((i) * 0.15d + (upY) * 0.12d - 0.08d).setY(0.42d));
						return true;
					}
				}
			}
		}
		return false;
	}

	public int checkIfPass(final WXYZ tst, final BlockFace bf, final int num, final boolean inv) {
		for (int i = 0; i < num; i++) {
			
			final boolean cll = VM.getNmsServer().getFastMat(tst.w, tst.x + (bf.getModX() * i), 
				tst.y + (bf.getModY() * i), tst.z + (bf.getModZ() * i)).isCollidable();
			if (inv ? !cll : cll) return i;
		}
		return num;
	}

	public void move(final Location loc, final Vector vc, final boolean look) {
		if (look) {
			loc.setDirection(vc);
		}
		final Vec3D ps = this.cY();
		this.b(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
		//loc.getWorld().playSound(loc, Sound.ENTITY_SHEEP_STEP, 1f, 1.2f);
		final Vector dl = new Vector(loc.getX() - ps.c, loc.getY() - ps.d, loc.getZ() - ps.e);
		BotManager.sendWrldPckts(this.s, 
			new PacketPlayOutEntityHeadRotation(this, (byte) (loc.getYaw() * 256 / 360)), 
			new PacketPlayOutRelEntityMoveLook(this.ae(), (short) (dl.getX() * 4096), (short) (dl.getY() * 4096), (short) (dl.getZ() * 4096), (byte) (loc.getYaw() * 256 / 360), (byte) (loc.getPitch() * 256 / 360), false));
	}

	public void pickupIts(final Location loc, final LivingEntity le) {
		for (final Item it : w.getEntitiesByClass(Item.class)) {
			//rplc.getWorld().getPlayers().get(0).sendMessage(loc.distanceSquared(it.getLocation()) + "");
			if (loc.distanceSquared(it.getLocation()) < 4d) {
				final ItemStack is = it.getItemStack();
				final GunType gt = GunType.getGnTp(is);
				if (gt == null) {
					final NadeType nt = NadeType.getNdTp(is);
					if (nt == null) {
						if (Inventories.isBlankItem(is, false)) return;
						switch (is.getType()) {
						case GOLDEN_APPLE:
							if (arena().shtrs.get(this) == Team.Ts) {
								item(is, 7);
								it.remove();
							}
							w.playSound(loc, Sound.ITEM_ARMOR_EQUIP_DIAMOND, 1f, 1.4f);
							BotManager.sendWrldPckts(this.s, new PacketPlayOutAnimation(this, 0), 
								new PacketPlayOutEntityEquipment(this.ae(), updateIts()));
							break;
						case SHEARS:
							if (arena().shtrs.get(this) == Team.CTs) {
								item(is, 7);
								it.remove();
							}
							w.playSound(loc, Sound.ITEM_ARMOR_EQUIP_DIAMOND, 1f, 1.4f);
							BotManager.sendWrldPckts(this.s, new PacketPlayOutAnimation(this, 0), 
								new PacketPlayOutEntityEquipment(this.ae(), updateIts()));
							break;
						default:
							break;
						}
						return;
					}
					final int slt = nt.prm ? 0 : 1;
					final ItemStack eqp = item(slt);
					final NadeType own = NadeType.getNdTp(eqp);
					if (own == null) {
						item(is, slt);
						it.remove();
						w.playSound(loc, Sound.ITEM_ARMOR_EQUIP_DIAMOND, 1f, 1.4f);
						BotManager.sendWrldPckts(this.s, new PacketPlayOutAnimation(this, 0), 
							new PacketPlayOutEntityEquipment(this.ae(), updateIts()));
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
					BotManager.sendWrldPckts(this.s, new PacketPlayOutAnimation(this, 0), 
						new PacketPlayOutEntityEquipment(this.ae(), updateIts()));
				} else if (gt.prc > own.prc) {
					w.dropItem(loc, eqp);
					item(is.asQuantity(gt.amo), slt);
					it.remove();
					w.playSound(loc, Sound.ITEM_ARMOR_EQUIP_NETHERITE, 1f, 1.4f);
					BotManager.sendWrldPckts(this.s, new PacketPlayOutAnimation(this, 0), 
						new PacketPlayOutEntityEquipment(this.ae(), updateIts()));
				}
			}
		}
	}

	private List<Pair<EnumItemSlot, net.minecraft.world.item.ItemStack>> updateIts() {
		final LivingEntity le = getEntity();
		final EnumItemSlot[] eis = EnumItemSlot.values();
		if (le == null) {
			@SuppressWarnings("unchecked")
			final Pair<EnumItemSlot, net.minecraft.world.item.ItemStack>[] its = (Pair<EnumItemSlot, net.minecraft.world.item.ItemStack>[]) new Pair<?, ?>[6];
			for (int i = its.length - 1; i >= 0; i--) {
				its[i] = Pair.of(eis[i], net.minecraft.world.item.ItemStack.fromBukkitCopy(Main.air));
			}
			return Arrays.asList(its);
		}
		final EquipmentSlot[] ess = EquipmentSlot.values();
		final EntityEquipment eq = le.getEquipment();
		@SuppressWarnings("unchecked")
		final Pair<EnumItemSlot, net.minecraft.world.item.ItemStack>[] its = (Pair<EnumItemSlot, net.minecraft.world.item.ItemStack>[]) new Pair<?, ?>[6];
		for (int i = its.length - 1; i >= 0; i--) {
			final ItemStack it = eq.getItem(ess[i]);
			its[i] = Pair.of(eis[i], net.minecraft.world.item.ItemStack.fromBukkitCopy(it == null ? Main.air : it));
		}
		return Arrays.asList(its);
	}

	private void updateEqp() {
		BotManager.sendWrldPckts(this.s, new PacketPlayOutEntityEquipment(this.ae(), updateIts()));
	}

	public void updateAll(final NetworkManager nm) {
		nm.getPlayer().getBukkitEntity().sendMessage("updated");
		nm.a(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.a, this));
		nm.a(new PacketPlayOutNamedEntitySpawn(this));
		nm.a(new PacketPlayOutEntityHeadRotation(this, (byte) (this.getBukkitYaw() * 256 / 360)));
		nm.a(new PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook(this.ae(), (short) 0, (short) 0, (short) 0, (byte) 0, (byte) 0, false));
		nm.a(new PacketPlayOutEntityDestroy(rid));
		nm.a(new PacketPlayOutEntityEquipment(this.ae(), updateIts()));
		/*final Scoreboard sb = this.c.aF();
		final ScoreboardTeam st = sb.g(name);
		//st.b(IChatBaseComponent.a("§7"));
		st.a(clr);
		nm.a(PacketPlayOutScoreboardTeam.a(st));
		nm.a(PacketPlayOutScoreboardTeam.a(st, true));
		nm.a(PacketPlayOutScoreboardTeam.a(st, name, PacketPlayOutScoreboardTeam.a.a));
		nm.a(PacketPlayOutScoreboardTeam.a(st, false));
		sb.d(st);*/
	}

	public void remove(final boolean npc) {
		if (npc) {
			BotManager.npcs.remove(rid);
		}
		final LivingEntity le = getEntity();
		if (le != null) {
			le.remove();
		}
		BotManager.sendWrldPckts(this.s, 
			new PacketPlayOutEntityDestroy(this.ae()), 
			new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.e, this));
		this.a(RemovalReason.a);
	}
	
	private boolean dead;
	public boolean isDead() {return dead;}
	public void die(final LivingEntity le) {
		dead = true;
		if (le != null) {
			BotManager.npcs.remove(rid);
			le.remove();
		}
		BotManager.sendWrldPckts(this.s, 
		new PacketPlayOutEntityDestroy(this.ae()), 
		new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.e, this));
	}

	public void tryBuy() {
		if (!willBuy || arena.gst != GameState.BUYTIME) return;
		willBuy = false;
		final Team tm = arena.shtrs.get(this);
		if (money < 1000) {//save
			tryBuyItem(GunType.hlmtSlt, GunType.hlmtPrc, EquipmentSlot.HEAD, tm);
			if (Main.srnd.nextBoolean()) {
				tryBuyItem(GunType.chstSlt, GunType.chstPrc, EquipmentSlot.CHEST, tm);
			}
			
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
			
			//drop mechanic?
		}
	}
	
	private boolean tryBuyItem(final byte slot, final short prc, final EquipmentSlot to, final Team tm) {
		final ItemStack eq = item(to);
		final GunType gt = GunType.getGnTp(eq);
		final ItemStack it;
		if (gt == null) {
			if (money < prc) {
				it = null;
			} else {
				money -= prc;
				switch (tm) {
				case CTs:
					it = Inventories.CTShop.getItem(slot);
					break;
				case Ts:
				default:
					it = Inventories.TShop.getItem(slot);
					break;
				}
			}
		} else if (gt.prc < prc) {
			if (money < prc) {
				it = null;
			} else {
				money -= prc;
				w.dropItem(getEntity().getLocation(), eq);
				switch (tm) {
				case CTs:
					it = Inventories.CTShop.getItem(slot);
					break;
				case Ts:
				default:
					it = Inventories.TShop.getItem(slot);
					break;
				}
			}
		} else {
			it = null;
		}
		
		if (Inventories.isBlankItem(it, false)) return false;
		final GunType ngt = GunType.getGnTp(it);
		if (ngt == null) {
			item(it.clone(), to);
			w.playSound(getEntity().getLocation(), Sound.ITEM_ARMOR_EQUIP_IRON, 1f, 1.4f);
		} else {
			item(new ItemBuilder(it.getType()).name("§5" + ngt.toString())
				.setAmount(ngt.amo).setModelData(GunType.defCMD).build(), to);
			w.playSound(getEntity().getLocation(), Sound.ITEM_ARMOR_EQUIP_NETHERITE, 1f, 1.4f);
		}
		return true;
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
				switch (tm) {
				case CTs:
					it = Inventories.CTShop.getItem(slot);
					break;
				case Ts:
				default:
					it = Inventories.TShop.getItem(slot);
					break;
				}
			}
		} else if (gt.prc < prc) {
			if (money < prc) {
				it = null;
			} else {
				money -= prc;
				w.dropItem(getEntity().getLocation(), eq);
				switch (tm) {
				case CTs:
					it = Inventories.CTShop.getItem(slot);
					break;
				case Ts:
				default:
					it = Inventories.TShop.getItem(slot);
					break;
				}
			}
		} else {
			it = null;
		}
		
		if (Inventories.isBlankItem(it, false)) return false;
		final GunType ngt = GunType.getGnTp(it);
		if (ngt == null) {
			item(it.clone(), to);
			w.playSound(getEntity().getLocation(), Sound.ITEM_ARMOR_EQUIP_IRON, 1f, 1.4f);
		} else {
			item(new ItemBuilder(it.getType()).name("§5" + ngt.toString())
				.setAmount(ngt.amo).setModelData(GunType.defCMD).build(), to);
			w.playSound(getEntity().getLocation(), Sound.ITEM_ARMOR_EQUIP_NETHERITE, 1f, 1.4f);
		}
		return true;
	}

	private final int shc;
	@Override
	public int hashCode() {
		return shc;
	}
	
	@Override
	public boolean equals(final Object o) {
		return o instanceof Shooter ? ((Shooter) o).name().equals(name) : false;
	}
}
