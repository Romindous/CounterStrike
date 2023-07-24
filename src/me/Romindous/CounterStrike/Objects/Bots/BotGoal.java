package me.Romindous.CounterStrike.Objects.Bots;

import static me.Romindous.CounterStrike.Objects.Bots.ActType.DIRECT;
import static me.Romindous.CounterStrike.Objects.Bots.ActType.GO_SPOT;
import static me.Romindous.CounterStrike.Objects.Bots.ActType.NEAR_BOMB;
import static me.Romindous.CounterStrike.Objects.Bots.ActType.NEAR_SITE;
import static me.Romindous.CounterStrike.Objects.Bots.ActType.PICK_BOMB;
import static me.Romindous.CounterStrike.Objects.Bots.ActType.SITE_ACT;

import java.lang.ref.WeakReference;
import java.util.EnumSet;
import java.util.Map.Entry;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.FaceAttachable;
import org.bukkit.block.data.FaceAttachable.AttachedFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Monster;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.joml.Math;

import com.destroystokyo.paper.entity.Pathfinder;
import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;

import me.Romindous.CounterStrike.Main;
import me.Romindous.CounterStrike.Enums.GunType;
import me.Romindous.CounterStrike.Game.Arena.Team;
import me.Romindous.CounterStrike.Game.Defusal;
import me.Romindous.CounterStrike.Game.Invasion;
import me.Romindous.CounterStrike.Listeners.DmgLis;
import me.Romindous.CounterStrike.Objects.Shooter;
import me.Romindous.CounterStrike.Objects.Game.BtShooter;
import me.Romindous.CounterStrike.Objects.Game.GameState;
import me.Romindous.CounterStrike.Objects.Mobs.Mobber;
import net.minecraft.network.protocol.game.PacketPlayOutAnimation;
import ru.komiss77.modules.bots.BotManager;
import ru.komiss77.modules.world.AStarPath;
import ru.komiss77.modules.world.WXYZ;
import ru.komiss77.modules.world.XYZ;
import ru.komiss77.utils.ItemUtils;
import ru.komiss77.utils.LocationUtil;
import ru.komiss77.version.VM;

public class BotGoal implements Goal<Mob> {

    private static final int MAX_LIVE_TICKS = 1000000;
	private static final int SPOT_DST = 12;
	private static final int REACT = 4;
	private static final int ACT_TIME = 1;
	private static final double NEAR_DST_SQ = 8d;
	private static final BlockData bdt = crtBtnDt();
	private static final WeakReference<LivingEntity> nrf = new WeakReference<LivingEntity>(null);
    private static final GoalKey<Mob> key = GoalKey.of(Mob.class, new NamespacedKey(Main.plug, "bot"));
    
    private final BtShooter bot;
    private final Mob rplc;
	private final Pathfinder pth;
	private final AStarPath arp;
	
	private ActType act;
	private boolean hasBomb;
	private XYZ tLoc;
	private XYZ site;
	
	private int tick;
	private int agro;
	private int acttm;
    
    public BotGoal(final BtShooter bot, final Mob hs) {
        this.bot = bot;
        this.rplc = hs;
        this.arp = new AStarPath(rplc, 2000);
		this.pth = rplc.getPathfinder();
		this.tLoc = null;
		this.tick = Main.srnd.nextInt(16);
		this.hasBomb = false;
		this.site = null;
		this.act = null;
    }

	@Override
    public boolean shouldActivate() {
        return true;
    }
 
    @Override
    public boolean shouldStayActive() {
        return true;
    }
 
    @Override
    public void start() {
    }
 
    @Override
    public void stop() {
    }
    
    @Override
    public void tick() {
//		Bukkit.broadcast(Component.text("le-" + rplc.getName()));
		if (rplc == null || !rplc.isValid() || rplc.getTicksLived() > MAX_LIVE_TICKS) {
			return;
		}
		
		tick++;

		bot.rotPss();
		
		final Location loc = rplc.getLocation();
		final Location eyel = rplc.getEyeLocation();
		final Vector vc;
		
		bot.tryReload(rplc, eyel);
		final boolean rld = bot.tryReload(rplc, eyel);
		
		if (rplc.hasPotionEffect(PotionEffectType.SLOW) || //blind or buy
			rplc.hasPotionEffect(PotionEffectType.BLINDNESS)) {
			vc = eyel.getDirection();
			changeTLoc(null, null);
			
			if ((tick & 7) == 0) {
				bot.tryBuy();
			}
		} else {
			final Team tm = bot.arena().shtrs.get(bot);
			if ((tick & 3) == 0 && acttm == 0) {
				switch (bot.arena().getType()) {
				case DEFUSAL:
					if (bot.tgtSh == null || bot.tgtSh.isDead()) {//look for tgt
						bot.tgtSh = null; bot.tgtLe = nrf;
//						Bukkit.broadcast(Component.text("scaning for sh"));
						for (final Entry<Shooter, Team> en : bot.arena().shtrs.entrySet()) {
							if (!en.getKey().isDead() && en.getValue() != tm) {
								final LivingEntity le = en.getKey().getEntity();
								if (le != null && le.getEntityId() != rplc.getEntityId() && Main.rayThruAir(eyel, le.getEyeLocation().toVector(), 0.1d)) {
									bot.tgtLe = new WeakReference<LivingEntity>((bot.tgtSh = en.getKey()).getEntity());
									agro = 0;
									break;
								}
							}
						}
					} else {//track tgt
						agro+=4;
						final LivingEntity tgt = bot.tgtSh.getEntity();
						if (tgt == null || !Main.rayThruAir(eyel, tgt.getEyeLocation().toVector(), 0.1F)) {
							changeTLoc(bot.tgtSh.getPos(), DIRECT);
							bot.tgtLe = nrf;
							bot.tgtSh = null;
						}
					}
					break;
				case GUNGAME:
					if (bot.tgtSh == null || bot.tgtSh.isDead()) {//look for tgt
						bot.tgtSh = null; bot.tgtLe = nrf;
//						Bukkit.broadcast(Component.text("scaning for sh"));
						for (final Entry<Shooter, Team> en : bot.arena().shtrs.entrySet()) {
							if (!en.getKey().isDead()) {
								final LivingEntity le = en.getKey().getEntity();
								if (le != null && le.getEntityId() != rplc.getEntityId() && Main.rayThruAir(eyel, le.getEyeLocation().toVector(), 0.1d)) {
									bot.tgtLe = new WeakReference<LivingEntity>((bot.tgtSh = en.getKey()).getEntity());
									agro = 0;
									break;
								}
							}
						}
					} else {//track tgt
						agro+=4;
						final LivingEntity tgt = bot.tgtSh.getEntity();
						if (tgt == null || !Main.rayThruAir(eyel, bot.tgtSh.getEntity().getEyeLocation().toVector(), 0.1F)) {
							changeTLoc(bot.tgtSh.getPos(), DIRECT);
							bot.tgtLe = nrf;
							bot.tgtSh = null;
						}
					}
					break;
				case INVASION:
					if (bot.tgtLe.get() == null || !bot.tgtLe.get().isValid()) {//look for tgt
						bot.tgtLe = nrf;
//						Bukkit.broadcast(Component.text("scaning for mobs"));
						for (final Monster le : LocationUtil.getChEnts(bot.getPos(), SPOT_DST << 1, Monster.class)) {
							if (le.isValid() && le.getType() != rplc.getType() && Main.rayThruAir(eyel, le.getEyeLocation().toVector(), 0.1d)) {
								bot.tgtLe = new WeakReference<LivingEntity>(le);
								bot.tgtSh = null;
								agro = 0;
								break;
							}
						}
					} else {//track tgt
						agro+=4;
						final LivingEntity tle = bot.tgtLe.get();
						if (tle == null || !tle.isValid() || !Main.rayThruAir(eyel, tle.getEyeLocation().toVector(), 0.1F)) {
//							changeTLoc(bot.tgtSh.getPos(), DIRECT);
							bot.tgtLe = nrf;
							bot.tgtSh = null;
						}
					}
					break;
				}
			}

			final LivingEntity le = bot.tgtLe.get();
			if (le != null && le.isValid()) {//attack
//				Bukkit.broadcast(Component.text("attacking"));
				final Vector tgt = bot.tgtSh == null ? le.getLocation().toVector() : bot.tgtSh.getLoc(true);
				vc = tgt.subtract(loc.toVector());
				if (bot.getHandSlot() == 2) {
					if (vc.lengthSquared() < NEAR_DST_SQ) {
						changeTLoc(getStrfLoc(loc, vc, -2), DIRECT);
						BotManager.sendWrldPckts(VM.getNmsServer().toNMS(bot.w), new PacketPlayOutAnimation(bot, 0));
						if ((tick & 3) == 0) DmgLis.prcDmg(le, bot.tgtSh, bot, le.getEquipment().getChestplate() == null ? 3d : 2d, 
							"§f\u9298", 5, (short) GunType.knfRwd, false, false, false, false, false);
					} else {
						changeTLoc(getStrfLoc(loc, vc, 8), DIRECT);
					}
				} else {
					if (!rld && agro > REACT) {
						bot.tryShoot(rplc, eyel);
						bot.tryShoot(rplc, eyel);
					}
					changeTLoc(getStrfLoc(loc, vc, -2), DIRECT);
				}
			} else {//go places
				switch (bot.getHandSlot()) {
				case 2, 3, 4:
				default:
					bot.switchToGun();
					vc = eyel.getDirection();
					if ((tick & 7) == 0) {
						bot.tryBuy();
					}
					break;
				case 0, 1:
					//nades, knif
					if (ItemUtils.isBlank(bot.item(EquipmentSlot.HAND), false))
						bot.switchToGun();
					vc = eyel.getDirection();
					if ((tick & 7) == 0) {
						bot.tryBuy();
					}
					break;
				case 7:
					if (ItemUtils.isBlank(bot.item(7), false) || acttm == 0) {
						bot.switchToGun();
					} else if ((tick & 1) == 0) {
						bot.w.playSound(rplc.getLocation(), tm == Team.Ts ? 
						Sound.ENTITY_GENERIC_EAT : Sound.BLOCK_TRIPWIRE_CLICK_ON, 2f, 1f);
						BotManager.sendWrldPckts(VM.getNmsServer().toNMS(bot.w), new PacketPlayOutAnimation(bot, 0));
					}
					vc = tLoc == null ? eyel.getDirection() : new Vector(tLoc.x - eyel.getX(), tLoc.y - eyel.getY(), tLoc.z - eyel.getZ());
					break;
				}
				
				if (tLoc == null || (tick & 63) == 0 || act == DIRECT) {
					switch (bot.arena().getType()) {
					case DEFUSAL:
						final Defusal df = (Defusal) bot.arena();
						hasBomb = tm == Team.Ts && !ItemUtils.isBlank(bot.item(7), false);
						if (df.isBmbOn()) {
							if (site == null || site.distAbs(df.getBombLoc()) != 0) {
								site = df.getBombLoc();
								changeTLoc(Main.getNrPos(df.getClosestPos(site, 10)), NEAR_BOMB);
							} else {
								if (tm == Team.CTs) {
									if (df.gst == GameState.ROUND && site.distSq(bot.getPos()) < SPOT_DST) {
										if (acttm == 0) {
											bot.swapToSlot(7);
											acttm++;
											act = SITE_ACT;
										} else if (acttm == ACT_TIME) {
											bot.switchToGun();
											df.chngMn(bot, 250);
											df.defuse();
											acttm = 0;
										} else acttm++;
									} else {
										acttm = 0;
										changeTLoc(Main.getNrPos(site), SITE_ACT);
									}
								} else {
									changeTLoc(Main.getNrPos(df.getClosestPos(site, 20)), NEAR_BOMB);
								}
							}
						} else {
							final WXYZ bLoc = df.getBombPos();
							if (bLoc == null) {
								if (site == null) {
									site = Main.srnd.nextBoolean() ? df.ast : df.bst;
									changeTLoc(Main.getNrPos(df.getClosestPos(site, 10)), NEAR_SITE);
								} else {
									if (hasBomb) {
										if (df.gst == GameState.ROUND && (tick & 63) == 0 
											&& site.distSq(bot.getPos()) < SPOT_DST) {
											if (acttm == 0) {//start plant
												bot.swapToSlot(7);
												acttm++;
												act = SITE_ACT;
											} else if (acttm == ACT_TIME) {//planted
												final Block b = rplc.getLocation().getBlock();
												if (b.getType().isAir()) {
													bot.switchToGun();
													b.setBlockData(bdt, false);
													bot.item(Main.air.clone(), 7);
													df.chngMn(bot, 250);
													df.plant(b);
													changeTLoc(Main.getNrPos(df.getClosestPos(site, 10)), NEAR_BOMB);
													acttm = 0;
												}
											} else acttm++;
										} else {
											acttm = 0;
											changeTLoc(Main.getNrPos(site), NEAR_SITE);
										}
									} else {
										changeTLoc(Main.getNrPos(df.getClosestPos(site, 10)), NEAR_SITE);
									}
								}
							} else {
								changeTLoc(tm == Team.Ts ? bLoc : Main.getNrPos(df.getClosestPos(bLoc, 20)), PICK_BOMB);
							}
						}
						break;
					case GUNGAME:
						switch (act == null ? DIRECT : act) {
						case GO_SPOT:
							if (arp.isDone()) {
								changeTLoc(Main.getNrPos(bot.arena().getClosestPos(bot.getPos(), 20)), GO_SPOT);
							}
							break;
						case DIRECT:
							changeTLoc(Main.getNrPos(bot.arena().getClosestPos(bot.getPos(), 20)), GO_SPOT);
							break;
						default:
							break;
						}
						break;
					case INVASION:
						final Invasion in = (Invasion) bot.arena();
						if (in.isDay) {
							final Mobber mb;
							if (site == null) {
								mb = in.getRndMbbr(true);
								if (mb != null) site = new XYZ(mb.ind.getLocation());
							} else {
								final Mobber svm = in.mbbrs.get(site);
								if (svm == null) {
//									Bukkit.broadcast(Component.text("get close mb"));
									mb = in.getClsMbbr(bot.getPos(), true);
									if (mb != null) site = new XYZ(mb.ind.getLocation());
								} else mb = svm;
							}
							
							if (mb == null) {
								if (site == null) {
									site = new XYZ(in.bds.getViewRange() != 0f && Main.srnd.nextBoolean() 
											? in.bds.getLocation() : in.ads.getLocation());
									changeTLoc(Main.getNrPos(site), NEAR_SITE);
								}
							} else {
								if (in.gst == GameState.ROUND && (tick & 63) == 0 
									&& site.distSq(bot.getPos()) < SPOT_DST) {
									if (acttm == 0) {
										bot.swapToSlot(7);
										acttm++;
										act = SITE_ACT;
									} else if (acttm == ACT_TIME) {
										bot.switchToGun();
										in.addSpDfs(bot);
										in.chngMn(bot, 150);
										in.dieSpnr(mb);
										acttm = 0;
									} else acttm++;
								} else {
									acttm = 0;
									changeTLoc(Main.getNrPos(site), GO_SPOT);
								}
							}
						} else {
							if (acttm != 0) {
								bot.switchToGun();
								acttm = 0;
								site = null;
							}
							
							if (site == null || act != NEAR_SITE) {
								site = new XYZ(in.bds.getViewRange() != 0f && Main.srnd.nextBoolean() 
										? in.bds.getLocation() : in.ads.getLocation());
								changeTLoc(Main.getNrPos(site), NEAR_SITE);
							}
						}
						break;
					}
				}
			}
		}
		
		vc.normalize();
		
		bot.pickupIts(loc);
		
		bot.move(loc, vc, true);

		if (bot.tryJump(loc, rplc, vc)) {
			return;
		}
		
		vc.setY(0d);
		if (rplc.isInWater()) {
			rplc.setVelocity(rplc.getVelocity().setY(0.1d).add(vc.multiply(0.05d)));
		} else if (tLoc != null) {
			if (rplc.isOnGround()) {
				
				if (act == DIRECT) {
					pth.moveTo(tLoc.getCenterLoc(bot.w), 1.25d);
					return;
				}

//				pth.moveTo(tLoc.getCenterLoc(bot.w), 1.4d);
				arp.tickGo(1.4d);

			} else {
				if (pth.hasPath()) pth.stopPathfinding();
				rplc.setVelocity(rplc.getVelocity().add(vc.multiply(0.05d)));
			}
		}
    }
    
    private void changeTLoc(final XYZ nlc, final ActType at) {
    	if (at == DIRECT) {
        	tLoc = nlc;
        	act = DIRECT;
    		return;
    	}
    	
    	if (at != act || arp.isDone()) {
        	if (nlc != null) arp.setTgt(new WXYZ(bot.w, nlc));
        	tLoc = nlc;
        	act = at;
    	}
    }
	
	private WXYZ getStrfLoc(final Location from, final Vector vc, final int ds) {
		final int rx = Main.srnd.nextInt(Math.max(1, Math.abs(ds))), rz = Main.srnd.nextInt(Math.max(1, Math.abs(ds)));
		return new WXYZ(from.getWorld(), (vc.getBlockX()>>31 ^ ds>>31) * (rx<<1) + rx + (int) from.getX(), 
			from.getBlockY(), (vc.getBlockZ()>>31 ^ ds>>31) * (rz<<1) + rz + (int) from.getZ());
	}

	@Override
    public GoalKey<Mob> getKey() {
        return key;
    }
    
    @Override
    public EnumSet<GoalType> getTypes() {
        return EnumSet.of(GoalType.MOVE, GoalType.LOOK);
    }
 
    private static BlockData crtBtnDt() {
    	final BlockData dt = Material.CRIMSON_BUTTON.createBlockData();
    	((FaceAttachable) dt).setAttachedFace(AttachedFace.FLOOR);
		return dt;
	}
}