package me.romindous.cs.Objects.Bots;

import java.lang.ref.WeakReference;
import java.util.EnumSet;
import java.util.Map.Entry;
import com.destroystokyo.paper.entity.Pathfinder;
import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;
import me.romindous.cs.Enums.GameState;
import me.romindous.cs.Game.Arena;
import me.romindous.cs.Game.Arena.Team;
import me.romindous.cs.Game.Defusal;
import me.romindous.cs.Game.Invasion;
import me.romindous.cs.Listeners.DmgLis;
import me.romindous.cs.Main;
import me.romindous.cs.Objects.Game.Bomb;
import me.romindous.cs.Objects.Game.BtShooter;
import me.romindous.cs.Objects.Game.Mobber;
import me.romindous.cs.Objects.Shooter;
import me.romindous.cs.Utils.Utils;
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
import ru.komiss77.modules.bots.Botter;
import ru.komiss77.modules.world.AStarPath;
import ru.komiss77.modules.world.BVec;
import ru.komiss77.utils.ItemUtil;
import ru.komiss77.utils.LocUtil;

import static me.romindous.cs.Objects.Bots.BotGoal.ActType.*;

public class BotGoal implements Goal<Mob> {

    private static final int MAX_LIVE_TICKS = 1000000;

	private static final int SHIFT_TGT = 2;
	private static final int SHIFT_ACT = 4;

	private static final int DEL_TGT = (1 << SHIFT_TGT) - 1;
	private static final int DEL_ACT = (1 << SHIFT_ACT) - 1;

	private static final int SPOT_DST_SQ = 16;
	private static final int REACT = 1;
	private static final int ACT_TIME = 50;
	private static final int KNIFE_KD = 5;
	private static final double NEAR_DST_SQ = 8d;
	private static final BlockData bdt = crtBtnDt();
	private static final WeakReference<LivingEntity> nrf = new WeakReference<>(null);
    private static final GoalKey<Mob> key = GoalKey.of(Mob.class, new NamespacedKey(Main.plug, "bot"));

    private final BtShooter bsh;
    private final Mob rplc;
	private final Pathfinder pth;
	private final AStarPath arp;
	private final int reactTime;
//	private final boolean crazy;
	
	private ActType act;
	private BVec tLoc;
	private BVec site;
	
	private int tick;
	private int agro;
	private int acttm;
    
    public BotGoal(final BtShooter bsh, final Mob hs) {
        this.bsh = bsh;
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
    public void tick() {
//		Bukkit.broadcast(Component.text("le-" + rplc.getName()));
		if (rplc == null || !rplc.isValid() || rplc.getTicksLived() > MAX_LIVE_TICKS) {
			return;
		}
		
		tick++;

		bsh.rotPss();
		
		final Location loc = rplc.getLocation();
		final Location eyel = rplc.getEyeLocation();
		final Vector vc;
		
		bsh.tryReload(rplc, eyel);
		final boolean rld = bsh.tryReload(rplc, eyel);
		
		if (rplc.hasPotionEffect(PotionEffectType.SLOWNESS)) {//buy
			vc = eyel.getDirection();
			changeTLoc(null);
			
			if ((tick & DEL_TGT) == 0) {
				bsh.tryBuy();
			}

			bsh.own().pickup(loc);
			bsh.own().move(loc, vc);
			return;
		}

		final Team tm = bsh.arena().shtrs.get(bsh);
		if ((tick & DEL_TGT) == 0 && acttm == 0) {
			final LivingEntity tle;
			switch (bsh.arena().getType()) {
				case DEFUSAL:
					if (bsh.tgtSh == null || bsh.tgtSh.isDead()) {//look for tgt
						bsh.tgtLe = nrf; bsh.tgtSh = null;
//						Bukkit.broadcast(Component.text("scaning for sh"));
						for (final Entry<Shooter, Team> en : bsh.arena().shtrs.entrySet()) {
							if (!en.getKey().isDead() && en.getValue() != tm) {
								final LivingEntity le = en.getKey().getEntity();
								if (le != null && le.getEntityId() != rplc.getEntityId() && Utils.isSeen(eyel, le)) {
									if (Main.srnd.nextBoolean()) bsh.tryNade(le);
									bsh.tgtLe = new WeakReference<>(le);
									bsh.tgtSh = en.getKey();
									agro = 0;
									break;
								}
							}
						}
					} else {//track tgt
						agro+=SHIFT_TGT;
						tle = bsh.tgtSh.getEntity();
						if (tle == null || !tle.isValid() || !Utils.isSeen(eyel, tle)) {
							changeTLoc(bsh.tgtSh.getPos());
							bsh.tgtLe = nrf;
							bsh.tgtSh = null;
						}
					}
					break;
				case GUNGAME:
					if (bsh.tgtSh == null || bsh.tgtSh.isDead()) {//look for tgt
						bsh.tgtSh = null; bsh.tgtLe = nrf;
//						Bukkit.broadcast(Component.text("scaning for sh"));
						for (final Entry<Shooter, Team> en : bsh.arena().shtrs.entrySet()) {
							if (!en.getKey().isDead()) {
								final LivingEntity le = en.getKey().getEntity();
								if (le != null && le.getEntityId() != rplc.getEntityId() && Utils.isSeen(eyel, le)) {
									if (Main.srnd.nextBoolean()) bsh.tryNade(le);
									bsh.tgtLe = new WeakReference<>(le);
									bsh.tgtSh = en.getKey();
									agro = 0;
									break;
								}
							}
						}
					} else {//track tgt
						agro+=SHIFT_TGT;
						tle = bsh.tgtSh.getEntity();
						if (tle == null || !tle.isValid() || !Utils.isSeen(eyel, tle)) {
							changeTLoc(null);
							bsh.tgtLe = nrf;
							bsh.tgtSh = null;
						}
					}
					break;
				case INVASION:
					if (bsh.tgtLe.get() == null || !bsh.tgtLe.get().isValid()) {//look for tgt
						bsh.tgtLe = nrf;
//						Bukkit.broadcast(Component.text("scaning for mobs"));
						tle = LocUtil.getClsChEnt(bsh.getPos(), SPOT_DST_SQ, Monster.class, le ->
							le.getType() != rplc.getType() && Utils.isSeen(eyel, le));
						if (tle != null && acttm == 0) {
							bsh.tgtLe = new WeakReference<>(tle);
							bsh.tgtSh = null;
							agro = 0;
						}
					} else {//track tgt
						agro+=SHIFT_TGT;
						tle = bsh.tgtLe.get();
						if (tle == null || !tle.isValid() || !Utils.isSeen(eyel, tle)) {
							changeTLoc(null);
							bsh.tgtLe = nrf;
							bsh.tgtSh = null;
						}
					}
					break;
			}
		}

		final LivingEntity le = bsh.tgtLe.get();
		if (le != null && le.isValid()) {//attack
			final Vector tgt = bsh.tgtSh == null ? le.getLocation().toVector() : bsh.tgtSh.getLoc();
			vc = tgt.subtract(loc.toVector());
			if (rplc.hasPotionEffect(PotionEffectType.BLINDNESS)) {//blind
				if ((tick & DEL_TGT) == 0) changeTLoc(getStrfLoc(loc, vc, -2));

				if (tLoc != null) {
					if (act == DIRECT) {
						pth.moveTo(tLoc.center(bsh.own().world()), Botter.SPEED_SLOW);
					} else {
						arp.tickGo(Botter.SPEED_RUN);
					}
				}

				bsh.own().move(loc, vc);
				return;
			} else switch (bsh.own().getHandSlot()) {
                case 2:
                    if (vc.lengthSquared() < NEAR_DST_SQ) {
                        if ((tick & DEL_TGT) == 0) changeTLoc(getStrfLoc(loc, vc, -2));
                        if (tick % KNIFE_KD == 0) {
                            bsh.own().swingHand(true);
                            DmgLis.prcDmg(le, bsh.tgtSh, bsh, le.getEquipment().getChestplate() == null ? 3d : 2d,
                                "§f\u9298", 5, Shooter.knifRwd, false, false, false, false, false);
                        }
                    } else {
                        if ((tick & DEL_TGT) == 0) changeTLoc(getStrfLoc(loc, vc, 8));
                    }
                    break;
                case 0, 1:
                    if (!rld && agro > reactTime) {
                        bsh.tryShoot(rplc);
                        bsh.tryShoot(rplc);
                    }

                    if ((tick & DEL_TGT) == 0) {
						changeTLoc(getStrfLoc(loc, vc,
							vc.lengthSquared() < NEAR_DST_SQ ? -2 : 8));
					}
                    break;
                case 7:
                    acttm = 0;
                    bsh.switchToGun();
                    break;
                default:
                    bsh.switchToGun();
                    break;
            }
		} else {//go places
			final Location dir = arp.getNextLoc(rplc.getWorld());
			switch (bsh.own().getHandSlot()) {
				case 0, 1:
					if (ItemUtil.isBlank(bsh.item(EquipmentSlot.HAND), false))
						bsh.switchToGun();
					vc = dir == null ? eyel.getDirection() : dir.subtract(loc).toVector().setY(0d);
					if ((tick & DEL_TGT) == 0) bsh.tryBuy();
					break;
				case 7:
					vc = site == null ? (dir == null ? eyel.getDirection() : dir.subtract(loc).toVector().setY(0d))
						: new Vector(site.x - eyel.getX() + 0.5d, site.y - eyel.getY() + 0.5d, site.z - eyel.getZ() + 0.5d);
					if (acttm == 0) {
						bsh.switchToGun();
					} else if ((tick & DEL_TGT) == 0) {
						bsh.own().world().playSound(eyel, tm == Team.Ts ? Sound.ENTITY_GENERIC_EAT : Sound.BLOCK_TRIPWIRE_CLICK_ON, 2f, 1f);
						bsh.own().world().spawnParticle(Particle.ITEM, eyel.clone().add(vc.multiply(0.4d)),
							12, 0d, 0d, 0d, 0.1d, bsh.item(EquipmentSlot.HAND));
						bsh.own().swingHand(true);
					}
					break;
				default://nades, knif
					bsh.switchToGun();
					vc = dir == null ? eyel.getDirection() : dir.subtract(loc).toVector().setY(0d);
					if ((tick & DEL_TGT) == 0) bsh.tryBuy();
					break;
			}

			switch (bsh.arena().getType()) {
				case DEFUSAL:
					final Defusal df = (Defusal) bsh.arena();
					if (df.isBmbOn()) {
						if (site == null || act == NEAR_SITE) {
							site = df.getBomb();
							changeTLoc(site, NEAR_BOMB, df, 10);
						} else {
							final boolean tAct = (tick & DEL_ACT) == 0;
							if (!arp.hasTgt() || tLoc == null || act == DIRECT || tAct) {
								if (tm == Team.CTs && site instanceof final Bomb bmb) {//cts go defuse
									if (df.gst == GameState.ROUND && act == SITE_ACT
										&& site.distSq(bsh.getPos()) < SPOT_DST_SQ) {
										if (acttm == 0) {
											if (bmb.defusing() == null) {//can defuse
												bsh.swapTo(7);
												acttm += DEL_ACT;
												bmb.defusing(bsh);
											} else {//defend defuser
												changeTLoc(site, SITE_ACT, false);
											}
										} else if (tAct) {
											if (acttm > ACT_TIME << (bsh.item(EquipmentSlot.HAND)
												.getType() == Material.SHEARS ? 0 : 1)) {//defused
												df.w.playSound(eyel, Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE, 2f, 2f);
												df.w.playSound(eyel, Sound.BLOCK_IRON_TRAPDOOR_CLOSE, 2f, 2f);
												bsh.switchToGun();
												df.chngMn(bsh, 250);
												df.defuse();
												acttm = 0;
											} else acttm += DEL_ACT;
										}
									} else {
										acttm = 0;
										changeTLoc(site, SITE_ACT, false);
										final Shooter ds = bmb.defusing();
										if (bsh.equals(ds)) bmb.defusing(null);
									}
								} else {//set go defend
									site = null;
								}
							}
						}
					} else {
						final BVec bLoc = df.getBombDrop();
						if (bLoc == null) {//if bomb not dropped
							if (site == null) {
								site = Main.srnd.nextBoolean() ? df.ast : df.bst;
								changeTLoc(site, NEAR_SITE, df, 10);
							} else {
								final boolean tAct = (tick & DEL_ACT) == 0;
								if (!arp.hasTgt() || tLoc == null || act == DIRECT || tAct) {
									if (tm == Team.Ts) {//set go plant
										if (ItemUtil.isBlank(bsh.item(7), false)) {
											changeTLoc(site, NEAR_SITE, bsh.arena(), 10);
										} else {//has bomb
											if (df.gst == GameState.ROUND && act == SITE_ACT
												&& site.distSq(bsh.getPos()) < SPOT_DST_SQ) {
												if (acttm == 0) {//start plant
													bsh.swapTo(7);
													acttm += DEL_ACT;
												} else if (tAct) {
													if (acttm > ACT_TIME) {//planted
														final Block b = rplc.getLocation().getBlock();
														if (b.getType().isAir()) {
															bsh.switchToGun();
															b.setBlockData(bdt, false);
															bsh.item(7, ItemUtil.air.clone());
															df.chngMn(bsh, 250);
															df.plant(b);
															changeTLoc(site, NEAR_SITE, df, 10);
															acttm = 0;
														} else {//if obstructed
															changeTLoc(site, SITE_ACT, false);
														}
													} else acttm += DEL_ACT;
												}
											} else {
												acttm = 0;
												changeTLoc(site, SITE_ACT, true);
											}
										}
									} else {//cts go to cites
										changeTLoc(site, NEAR_SITE, df, 20);
									}
								}
							}
						} else {//if bomb dropped
							if (tm == Team.Ts) {
								changeTLoc(bLoc, PICK_BOMB, false);
							} else {
								changeTLoc(bLoc, PICK_BOMB, df, 10);
							}
						}
					}
					break;
				case GUNGAME:
					switch (act) {
						case GO_SPOT:
							if (arp.hasTgt()) break;
						case DIRECT:
							changeTLoc(bsh.getPos(), GO_SPOT, bsh.arena(), 20);
							break;
						default:
							break;
					}
					break;
				case INVASION:
					final Invasion in = (Invasion) bsh.arena();
					if (site == null) {
						if (in.isDay) {
							final Mobber mb = in.getRndMbbr(true);
							if (mb != null) {
								site = mb;
								changeTLoc(site, GO_SPOT, true);
							}
						} else {
							site = BVec.of(in.bds.getViewRange() == 0f || Main.srnd.nextBoolean()
								? in.ads.getLocation() : in.bds.getLocation());
							changeTLoc(site, NEAR_SITE, true);
						}
					} else {
						if (site instanceof final Mobber mb) {
							if ((in.isDay || act == SITE_ACT) && mb.isAlive()) {
								final boolean tAct = (tick & DEL_ACT) == 0;
//								Bukkit.broadcast(TCUtil.form("ac-" + act.name() + ", t-" + tAct));
								if (!arp.hasTgt() || tLoc == null || act == DIRECT || tAct) {
									if (in.gst == GameState.ROUND && site.distSq(bsh.getPos()) < SPOT_DST_SQ) {
										if (acttm == 0) {
											if (mb.defusing() == null) {//can defuse
												bsh.swapTo(7);
												acttm += DEL_ACT;
												mb.defusing(bsh);
												act = SITE_ACT;
											}
										} else if (tAct) {
//											Bukkit.broadcast(TCUtil.form("a-" + acttm));
											if (acttm > ACT_TIME << (bsh.item(EquipmentSlot.HAND)
												.getType() == Material.SHEARS ? 0 : 1)) {//defused
												in.w.playSound(eyel, Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE, 2f, 2f);
												in.w.playSound(eyel, Sound.BLOCK_IRON_TRAPDOOR_CLOSE, 2f, 2f);
												bsh.switchToGun();
												in.addSpDfs(bsh);
												in.chngMn(bsh, 150);
												in.dieSpnr(mb);
												site = null;
												acttm = 0;
											} else acttm += DEL_ACT;
										}
									} else {
										acttm = 0;
										changeTLoc(site, GO_SPOT, true);
										final Shooter ds = mb.defusing();
										if (bsh.equals(ds)) mb.defusing(null);
									}
								}
							} else {
								bsh.switchToGun();
								acttm = 0;
								site = null;
							}
						} else {
							if (in.isDay) {
								site = null;
							} else if (act != GO_SPOT) {
								acttm = 0;
								changeTLoc(site, GO_SPOT, true);
							}
						}
					}
					break;
			}
		}

		if (tLoc != null) {
			if (act == DIRECT) {
				pth.moveTo(tLoc.center(bsh.own().world()), Botter.SPEED_SLOW);
			} else {
                arp.tickGo(Botter.SPEED_RUN);
            }
		}

		bsh.own().pickup(loc);
		bsh.own().move(loc, vc);
    }

	private void changeTLoc(final BVec nlc) {
		if (nlc == null) {
			arp.delTgt();
			tLoc = null;
			act = DIRECT;
			return;
		}

		tLoc = nlc;
		act = DIRECT;
	}

	private void changeTLoc(final BVec nlc, final ActType at, final boolean limit) {
		if (nlc == null) {
			arp.delTgt();
			tLoc = null;
			act = DIRECT;
			return;
		}

		if (at == DIRECT) {
			tLoc = nlc;
			act = DIRECT;
			return;
		}

		if (at == act && arp.hasTgt() && tLoc != null && (limit || (tick & DEL_ACT) != 0)) return;
		arp.setTgt(Main.getNrPos(nlc).w(bsh.own().world()));
		tLoc = nlc;
		act = at;
	}

	private void changeTLoc(final BVec nlc, final ActType at, final Arena ar, final int dst) {
		if (nlc == null) {
			arp.delTgt();
			tLoc = null;
			act = DIRECT;
			return;
		}

		if (at == DIRECT) {
			tLoc = nlc;
			act = DIRECT;
			return;
		}

		if (at == act && arp.hasTgt() && tLoc != null) return;
        arp.setTgt(ar.getClosestPos(nlc, dst).w(bsh.own().world()));
		tLoc = nlc;
		act = at;
	}
	
	private BVec getStrfLoc(final Location from, final Vector vc, final int ds) {
		final int rx = Main.srnd.nextInt(Math.max(1, Math.abs(ds))), rz = Main.srnd.nextInt(Math.max(1, Math.abs(ds)));
		return BVec.of(from.getWorld(), (vc.getBlockX()>>31 ^ ds>>31) * (rx<<1) + rx + (int) from.getX(),
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