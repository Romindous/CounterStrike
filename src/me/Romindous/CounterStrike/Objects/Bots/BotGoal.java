package me.Romindous.CounterStrike.Objects.Bots;

import com.destroystokyo.paper.entity.Pathfinder;
import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;
import me.Romindous.CounterStrike.Enums.GameState;
import me.Romindous.CounterStrike.Enums.GunType;
import me.Romindous.CounterStrike.Game.Arena.Team;
import me.Romindous.CounterStrike.Game.Defusal;
import me.Romindous.CounterStrike.Game.Invasion;
import me.Romindous.CounterStrike.Listeners.DmgLis;
import me.Romindous.CounterStrike.Main;
import me.Romindous.CounterStrike.Objects.Game.Bomb;
import me.Romindous.CounterStrike.Objects.Game.BtShooter;
import me.Romindous.CounterStrike.Objects.Mobs.Mobber;
import me.Romindous.CounterStrike.Objects.Shooter;
import net.minecraft.network.protocol.game.PacketPlayOutAnimation;
import org.bukkit.*;
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
import ru.komiss77.modules.world.AStarPath;
import ru.komiss77.modules.world.WXYZ;
import ru.komiss77.modules.world.XYZ;
import ru.komiss77.utils.ItemUtils;
import ru.komiss77.utils.LocationUtil;
import ru.komiss77.version.Nms;

import java.lang.ref.WeakReference;
import java.util.EnumSet;
import java.util.Map.Entry;

import static me.Romindous.CounterStrike.Objects.Bots.BotGoal.ActType.*;

public class BotGoal implements Goal<Mob> {

    private static final int MAX_LIVE_TICKS = 1000000;

	private static final int SHIFT_TGT = 2;
	private static final int SHIFT_ACT = 6;

	private static final int DEL_TGT = (1 << SHIFT_TGT) - 1;
	private static final int DEL_ACT = (1 << SHIFT_ACT) - 1;

	private static final int SPOT_DST_SQ = 16;
	private static final int REACT = 2;
	private static final int ACT_TIME = 80;
	private static final int KNIFE_KD = 5;
	private static final double NEAR_DST_SQ = 8d;
	private static final BlockData bdt = crtBtnDt();
	private static final WeakReference<LivingEntity> nrf = new WeakReference<>(null);
    private static final GoalKey<Mob> key = GoalKey.of(Mob.class, new NamespacedKey(Main.plug, "bot"));
    
    private final BtShooter bot;
    private final Mob rplc;
	private final Pathfinder pth;
	private final AStarPath arp;
	private final int reactTime;
//	private final boolean crazy;
	
	private ActType act;
	private XYZ tLoc;
	private XYZ site;
	
	private int tick;
	private int agro;
	private int acttm;
    
    public BotGoal(final BtShooter bot, final Mob hs) {
        this.bot = bot;
        this.rplc = hs;
        this.arp = new AStarPath(rplc, 2000, 1f);
		this.pth = rplc.getPathfinder();
		this.reactTime = Main.srnd.nextInt(REACT) + REACT;
//		this.crazy = Main.srnd.nextBoolean();

		this.tick = reactTime << 1;
		this.tLoc = null;
		this.site = null;
		this.act = DIRECT;
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
			
			if ((tick & DEL_TGT) == 0) {
				bot.tryBuy();
			}
		} else {
			final Team tm = bot.arena().shtrs.get(bot);
			if ((tick & DEL_TGT) == 0 && acttm == 0) {
				final LivingEntity tle;
				switch (bot.arena().getType()) {
				case DEFUSAL:
					if (bot.tgtSh == null || bot.tgtSh.isDead()) {//look for tgt
						bot.tgtSh = null; bot.tgtLe = nrf;
//						Bukkit.broadcast(Component.text("scaning for sh"));
						for (final Entry<Shooter, Team> en : bot.arena().shtrs.entrySet()) {
							if (!en.getKey().isDead() && en.getValue() != tm) {
								final LivingEntity le = en.getKey().getEntity();
								if (le != null && le.getEntityId() != rplc.getEntityId() && LocationUtil.rayThruAir(eyel, le.getEyeLocation().toVector(), 0.1d)) {
									bot.tgtLe = new WeakReference<>((bot.tgtSh = en.getKey()).getEntity());
									agro = 0;
									break;
								}
							}
						}
					} else {//track tgt
						agro+=SHIFT_TGT;
						tle = bot.tgtSh.getEntity();
						if (tle == null || !LocationUtil.rayThruAir(eyel, tle.getEyeLocation().toVector(), 0.1F)) {
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
								if (le != null && le.getEntityId() != rplc.getEntityId() && LocationUtil.rayThruAir(eyel, le.getEyeLocation().toVector(), 0.1d)) {
									bot.tgtLe = new WeakReference<>((bot.tgtSh = en.getKey()).getEntity());
									agro = 0;
									break;
								}
							}
						}
					} else {//track tgt
						agro+=SHIFT_TGT;
						tle = bot.tgtSh.getEntity();
						if (tle == null || !LocationUtil.rayThruAir(eyel, tle.getEyeLocation().toVector(), 0.1F)) {
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
						tle = LocationUtil.getClsChEnt(bot.getPos(), SPOT_DST_SQ, Monster.class, le ->
							le.isValid() && le.getType() != rplc.getType() && LocationUtil.rayThruAir(eyel, le.getEyeLocation().toVector(), 0.1d));
						if (tle != null && acttm == 0) {
							bot.tgtLe = new WeakReference<>(tle);
							bot.tgtSh = null;
							agro = 0;
						}
					} else {//track tgt
						agro+=SHIFT_TGT;
						tle = bot.tgtLe.get();
						if (tle == null || !tle.isValid() || !LocationUtil.rayThruAir(eyel, tle.getEyeLocation().toVector(), 0.1F)) {
							changeTLoc(new WXYZ(tle.getLocation()), DIRECT);
							bot.tgtLe = nrf;
							bot.tgtSh = null;
						}
					}
					break;
				}
			}

			final LivingEntity le = bot.tgtLe.get();
			if (le != null && le.isValid()) {//attack
				final Vector tgt = bot.tgtSh == null ? le.getLocation().toVector() : bot.tgtSh.getLoc();
				vc = tgt.subtract(loc.toVector());
				switch (bot.getHandSlot()) {
				case 2:
					if (vc.lengthSquared() < NEAR_DST_SQ) {
						if ((tick & DEL_TGT) == 0) changeTLoc(getStrfLoc(loc, vc, -2), DIRECT);
						if (tick % KNIFE_KD == 0) {
							Nms.sendWorldPacket(bot.world, new PacketPlayOutAnimation(bot, 0));
							DmgLis.prcDmg(le, bot.tgtSh, bot, le.getEquipment().getChestplate() == null ? 3d : 2d,
									"§f\u9298", 5, GunType.knfRwd, false, false, false, false, false);
						}
					} else {
						if ((tick & DEL_TGT) == 0) changeTLoc(getStrfLoc(loc, vc, 8), DIRECT);
					}
					break;
				case 0, 1:
					if (!rld && agro > reactTime) {
						bot.tryShoot(rplc, eyel);
						bot.tryShoot(rplc, eyel);
					}

					if ((tick & DEL_TGT) == 0) changeTLoc(getStrfLoc(loc, vc, -2), DIRECT);
					break;
				case 7:
					acttm = 0;
					bot.switchToGun();
					break;
				default:
					bot.switchToGun();
					break;
                }
			} else {//go places
				final Location dir = arp.getNextLoc();
				switch (bot.getHandSlot()) {
				default://nades, knif
					bot.switchToGun();
					vc = dir == null ? eyel.getDirection() : dir.subtract(loc).toVector().setY(0d);
					if ((tick & DEL_TGT) == 0) bot.tryBuy();
					break;
				case 0, 1:
					if (ItemUtils.isBlank(bot.item(EquipmentSlot.HAND), false))
						bot.switchToGun();
					vc = dir == null ? eyel.getDirection() : dir.subtract(loc).toVector().setY(0d);
					if ((tick & DEL_TGT) == 0) bot.tryBuy();
					break;
				case 7:
					vc = site == null ? (dir == null ? eyel.getDirection() : dir.subtract(loc).toVector().setY(0d))
						: new Vector(site.x - eyel.getX() + 0.5d, site.y - eyel.getY() + 0.5d, site.z - eyel.getZ() + 0.5d);
					if (acttm == 0) {
						bot.switchToGun();
					} else if ((tick & DEL_TGT) == 0) {
						bot.world.playSound(eyel, tm == Team.Ts ? Sound.ENTITY_GENERIC_EAT : Sound.BLOCK_TRIPWIRE_CLICK_ON, 2f, 1f);
						bot.world.spawnParticle(Particle.ITEM_CRACK, eyel.clone().add(vc.multiply(0.4d)),
							12, 0d, 0d, 0d, 0.1d, bot.item(EquipmentSlot.HAND));
						Nms.sendWorldPacket(bot.world, new PacketPlayOutAnimation(bot, 0));
					}
					break;
				}

				switch (bot.arena().getType()) {
				case DEFUSAL:
					final Defusal df = (Defusal) bot.arena();
					if (df.isBmbOn()) {
						if (site == null || act == NEAR_SITE) {
							site = df.getBomb();
							changeTLoc(Main.getNrPos(df.getClosestPos(site, 10)), NEAR_BOMB);
						} else {
							final boolean tAct = (tick & DEL_ACT) == 0;
							if (!arp.hasTgt() || tLoc == null || act == DIRECT || tAct) {
								if (tm == Team.CTs && site instanceof final Bomb bmb) {//cts go defuse
									if (df.gst == GameState.ROUND && act == SITE_ACT
										&& site.distSq(bot.getPos()) < SPOT_DST_SQ) {
										if (acttm == 0) {
											if (bmb.defusing == null) {//can defuse
												bot.swapToSlot(7);
												acttm += DEL_ACT;
												bmb.defusing = bot;
											} else {//defend defuser
												changeTLoc(Main.getNrPos(site), SITE_ACT);
											}
										} else if (tAct) {
											if (acttm > ACT_TIME << (bot.item(EquipmentSlot.HAND)
												.getType() == Material.SHEARS ? 0 : 1)) {//defused
												df.w.playSound(eyel, Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE, 2f, 2f);
												df.w.playSound(eyel, Sound.BLOCK_IRON_TRAPDOOR_CLOSE, 2f, 2f);
												bot.switchToGun();
												df.chngMn(bot, 250);
												df.defuse();
												acttm = 0;
											} else acttm += DEL_ACT;
										}
									} else {
										acttm = 0;
										changeTLoc(Main.getNrPos(site), SITE_ACT);
										final Shooter ds = bmb.defusing;
										if (bot.equals(ds)) bmb.defusing = null;
									}
								} else {//set go defend
									changeTLoc(Main.getNrPos(df.getClosestPos(site, 10)), NEAR_BOMB);
								}
							}
						}
					} else {
						final WXYZ bLoc = df.getBombDrop();
						if (bLoc == null) {//if bomb not dropped
							if (site == null) {
								site = Main.srnd.nextBoolean() ? df.ast : df.bst;
								changeTLoc(Main.getNrPos(df.getClosestPos(site, 10)), NEAR_SITE);
							} else {
								final boolean tAct = (tick & DEL_ACT) == 0;
								if (!arp.hasTgt() || tLoc == null || act == DIRECT || tAct) {
									if (tm == Team.Ts) {//set go plant
										if (ItemUtils.isBlank(bot.item(7), false)) {
											changeTLoc(Main.getNrPos(df.getClosestPos(site, 10)), NEAR_SITE);
										} else {//has bomb
											if (df.gst == GameState.ROUND && act == SITE_ACT
												&& site.distSq(bot.getPos()) < SPOT_DST_SQ) {
												if (acttm == 0) {//start plant
													bot.swapToSlot(7);
													acttm += DEL_ACT;
												} else if (tAct) {
													if (acttm > ACT_TIME) {//planted
														final Block b = rplc.getLocation().getBlock();
														if (b.getType().isAir()) {
															bot.switchToGun();
															b.setBlockData(bdt, false);
															bot.item(Main.air.clone(), 7);
															df.chngMn(bot, 250);
															df.plant(b);
															changeTLoc(Main.getNrPos(df.getClosestPos(site, 10)), NEAR_SITE);
															acttm = 0;
														} else {//if obstructed
															changeTLoc(Main.getNrPos(site), SITE_ACT);
														}
													} else acttm += DEL_ACT;
												}
											} else {
												acttm = 0;
												changeTLoc(Main.getNrPos(site), SITE_ACT);
											}
										}
									} else {//cts go to cites
										changeTLoc(Main.getNrPos(df.getClosestPos(site, 20)), NEAR_SITE);
									}
								}
							}
						} else if (act != PICK_BOMB) {//if bomb dropped
							changeTLoc(tm == Team.Ts ? bLoc : Main.getNrPos(df.getClosestPos(bLoc, 10)), PICK_BOMB);
						}
					}
					break;
				case GUNGAME:
					switch (act) {
						case GO_SPOT:
							if (arp.hasTgt()) break;
						case DIRECT:
							changeTLoc(Main.getNrPos(bot.arena().getClosestPos(bot.getPos(), 20)), GO_SPOT);
							break;
						default:
							break;
					}
					break;
				case INVASION:
					final Invasion in = (Invasion) bot.arena();
					if (site == null) {
						if (in.isDay) {
							final Mobber mb = in.getRndMbbr(true);
							if (mb != null) {
								site = mb;
								changeTLoc(Main.getNrPos(site), GO_SPOT);
							}
						} else {
							site = new XYZ(in.bds.getViewRange() == 0f || Main.srnd.nextBoolean()
								? in.ads.getLocation() : in.bds.getLocation());
							changeTLoc(Main.getNrPos(site), NEAR_SITE);
						}
					} else {
						if (site instanceof final Mobber mb) {
							if ((in.isDay || act == SITE_ACT) && mb.isAlive()) {
								final boolean tAct = (tick & DEL_ACT) == 0;
//								Bukkit.broadcast(TCUtils.format("ac-" + act.name() + ", t-" + tAct));
								if (!arp.hasTgt() || tLoc == null || act == DIRECT || tAct) {
									if (in.gst == GameState.ROUND && site.distSq(bot.getPos()) < SPOT_DST_SQ) {
										if (acttm == 0) {
											if (mb.defusing == null) {//can defuse
												bot.swapToSlot(7);
												acttm += DEL_ACT;
												mb.defusing = bot;
												act = SITE_ACT;
											}
										} else if (tAct) {
//											Bukkit.broadcast(TCUtils.format("a-" + acttm));
											if (acttm > ACT_TIME << (bot.item(EquipmentSlot.HAND)
													.getType() == Material.SHEARS ? 0 : 1)) {//defused
												in.w.playSound(eyel, Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE, 2f, 2f);
												in.w.playSound(eyel, Sound.BLOCK_IRON_TRAPDOOR_CLOSE, 2f, 2f);
												bot.switchToGun();
												in.addSpDfs(bot);
												in.chngMn(bot, 150);
												in.dieSpnr(mb);
												site = null;
												acttm = 0;
											} else acttm += DEL_ACT;
										}
									} else {
										acttm = 0;
										changeTLoc(Main.getNrPos(site), GO_SPOT);
										final Shooter ds = mb.defusing;
										if (bot.equals(ds)) mb.defusing = null;
									}
								}
							} else {
								bot.switchToGun();
								acttm = 0;
								site = null;
							}
						} else {
							if (in.isDay) {
								site = null;
							} else if (act != GO_SPOT) {
								acttm = 0;
								changeTLoc(Main.getNrPos(site), GO_SPOT);
							}
						}
					}
					break;
				}
			}
		}
		
		vc.normalize();
		
		bot.pickupIts(loc);
		
		vc.setY(0d);

//		Bukkit.broadcast(TCUtils.format("move " + tLoc));
		bot.move(loc, vc, true);

		if (rplc.isInWater()) {
			rplc.setVelocity(rplc.getVelocity().setY(0.1d).add(vc.multiply(0.05d)));
		} else if (tLoc != null) {
			if (act == DIRECT) {
				pth.moveTo(tLoc.getCenterLoc(bot.world), 1.4d);
			} else {
                arp.tickGo(1.5d);
            }
		}
    }
    
    private void changeTLoc(final XYZ nlc, final ActType at) {
    	if (at == DIRECT) {
        	tLoc = nlc;
        	act = DIRECT;
    		return;
    	}
    	
    	if (at == act && arp.hasTgt() && tLoc != null) return;
//		Bukkit.broadcast(TCUtils.format(bot.name() + ", " + nlc + ", " + at + ", " + v + ", "
//			+ bot.name() + ", " + !arp.hasTgt() + ", " + tLoc + ", " + act + ", " + ((tick & DEL_ACT) == 0)));
		if (nlc != null) arp.setTgt(new WXYZ(bot.world, nlc));
		tLoc = nlc;
		act = at;
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

	protected enum ActType {
		SITE_ACT,
		NEAR_SITE,
		NEAR_BOMB,
		PICK_BOMB,
		DIRECT,
		GO_SPOT,
	}
}