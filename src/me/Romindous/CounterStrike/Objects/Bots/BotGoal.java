package me.Romindous.CounterStrike.Objects.Bots;

import static me.Romindous.CounterStrike.Objects.Bots.ActType.*;

import java.util.EnumSet;
import java.util.Map.Entry;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.FaceAttachable;
import org.bukkit.block.data.FaceAttachable.AttachedFace;
import org.bukkit.entity.Husk;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.destroystokyo.paper.entity.Pathfinder;
import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;

import me.Romindous.CounterStrike.Main;
import me.Romindous.CounterStrike.Game.Arena;
import me.Romindous.CounterStrike.Game.Arena.Team;
import me.Romindous.CounterStrike.Game.Defusal;
import me.Romindous.CounterStrike.Game.Gungame;
import me.Romindous.CounterStrike.Objects.Shooter;
import me.Romindous.CounterStrike.Objects.Game.BtShooter;
import me.Romindous.CounterStrike.Objects.Game.GameState;
import me.Romindous.CounterStrike.Objects.Loc.Spot;
import me.Romindous.CounterStrike.Utils.Inventories;
import net.minecraft.core.BaseBlockPosition;

public class BotGoal implements Goal<Husk> {

    public static final double DIF_POINT_DST = 20d;
    public static final int MAX_LIVE_TICKS = 1000000;
    public static final int NEXT_SPOT_TICKS = 120;
	private static final int SPOT_DST = 12;
	private static final int REACT = 8;
	private static final BlockData bdt = crtBtnDt();
    
    private final GoalKey<Husk> key;
    private final BtShooter bot;
    private final Mob rplc;
	private final Pathfinder pth;
	private final Team tm;
	
	private ActType act;
	private boolean direct;
	private Vector tLoc;
	private Spot pathTo;
	private Spot next;
	private int strX;
	private int strZ;

	private boolean hasBomb;
	private BaseBlockPosition site;
	
	private int tick;
	private int agro;
    
    public BotGoal(final BtShooter bot, final Husk hs) {
        this.key = GoalKey.of(Husk.class, new NamespacedKey(Main.plug, "bot"));
        this.tm = bot.arena().shtrs.get(bot);
        this.bot = bot;
        this.rplc = hs;
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
    	bot.die(null);
		bot.remove(true);
    }
    
    @Override
    public void tick() {
		if (rplc == null || !rplc.isValid() || rplc.getTicksLived() > MAX_LIVE_TICKS) {
	    	bot.die(null);
			bot.remove(true);
			return;
		} else {
			bot.rotPss();
			if (((tick++) & 7) == 0) {
				bot.tryBuy();
			}
			
			//Bukkit.broadcast(Component.text("le-" + rplc.getName()));
			final Location loc = rplc.getLocation();
			final Location eyel = rplc.getEyeLocation();
			final Vector vc;
			
			bot.tryReload(rplc, eyel);
			final boolean rld = bot.tryReload(rplc, eyel);
			
			if (rplc.hasPotionEffect(PotionEffectType.SLOW) || //blind or buy
				rplc.hasPotionEffect(PotionEffectType.BLINDNESS)) {
				vc = eyel.getDirection();
				changeTLoc(null, null);
			} else {
				if (bot.tgt == null || bot.tgt.isDead()) {//look for tgt
					if ((tick & 3) == 0) {
						for (final Entry<Shooter, Team> en : bot.arena().shtrs.entrySet()) {
							if (!en.getKey().isDead() && (en.getValue() != tm || bot.arena() instanceof Gungame)) {
								final LivingEntity le = en.getKey().getEntity();
								if (le != null && le.getEntityId() != rplc.getEntityId() && Main.rayThruAir(eyel, le.getEyeLocation().toVector(), 0.1d)) {
									bot.tgt = en.getKey();
									agro = 0;
									break;
								}
							}
						}
					}
				} else {
					agro++;
					if ((tick & 7) == 0) {
						final Vector pos = bot.tgt.getPos(true);
						if (!Main.rayThruAir(eyel, pos, 0.1F)) {
							changeTLoc(pos, DIRECT);
							bot.tgt = null;
						}
					}
				}
				
				if (bot.tgt == null || bot.tgt.isDead()) {//go places
					switch (bot.getHandSlot()) {
					case 2, 3, 4:
					default:
						bot.switchToGun();
						vc = eyel.getDirection();
						break;
					case 0, 1:
						//nades, knif
						if (Inventories.isBlankItem(bot.item(EquipmentSlot.HAND), false))
							bot.switchToGun();
						vc = eyel.getDirection();
						break;
					case 7:
						if (Inventories.isBlankItem(bot.item(7), false)) {
							bot.switchToGun();
						} else if ((tick & 1) == 0) {
							bot.w.playSound(rplc.getLocation(), tm == Team.Ts ? 
							Sound.ENTITY_GENERIC_EAT : Sound.BLOCK_TRIPWIRE_CLICK_ON, 2f, 1f);
						}
						vc = tLoc == null ? eyel.getDirection() : tLoc.clone().subtract(eyel.toVector());
						break;
					}
					
					final boolean isActSite = bot.getHandSlot() == 7;
					
					if (tLoc == null || (tick & 63) == 0) {
						switch (bot.arena().getType()) {
						case DEFUSAL:
							final Defusal df = (Defusal) bot.arena();
							hasBomb = tm == Team.Ts && !Inventories.isBlankItem(bot.item(7), false);
							if (df.isBmbOn()) {
								if (site == null || !site.equals(df.getBombLoc())) {
									site = df.getBombLoc();
									changeTLoc(Main.getNrVec(df.getClosestPos(site, 1, true)), NEAR_BOMB);
								} else {
									final int dst = Arena.distSq(site, bot.da());
									if (tm == Team.CTs) {
										if (isActSite && df.gst == GameState.ROUND) {//defused
											bot.switchToGun();
											df.chngMn(bot, 250);
											df.defuse();
										} else if (dst < SPOT_DST) {//start defuse
											bot.swapToSlot(7);
										} else {
											changeTLoc(Main.getNrVec(site), DEFUSE);
										}
									} else {
										changeTLoc(Main.getNrVec(df.getClosestPos(site, 2, true)), NEAR_BOMB);
									}
								}
							} else {
								final Vector bLoc = df.getBombPos();
								if (bLoc == null) {
									if (site == null) {
										site = Main.srnd.nextBoolean() ? df.ast : df.bst;
										changeTLoc(Main.getNrVec(df.getClosestPos(site, 1, false)), NEAR_SITE);
									} else {
										final int dst = Arena.distSq(site, bot.da());
										if (hasBomb) {
											if (isActSite && df.gst == GameState.ROUND) {//planted
												final Block b = rplc.getLocation().getBlock();
												if (b.getType().isAir()) {
													bot.switchToGun();
													b.setBlockData(bdt, false);
													bot.item(Main.air.clone(), 7);
													df.chngMn(bot, 250);
													df.plant(b);
													changeTLoc(Main.getNrVec(df.getClosestPos(site, 1, true)), NEAR_BOMB);
												}
											} else if (dst < SPOT_DST) {//start plant
												bot.swapToSlot(7);
											} else {
												changeTLoc(Main.getNrVec(site), PLANT);
											}
										} else {
											changeTLoc(Main.getNrVec(df.getClosestPos(site, 1, true)), NEAR_SITE);
										}
									}
								} else {
									changeTLoc(tm == Team.Ts ? bLoc : Main.getNrVec(df.getClosestPos(
										new BaseBlockPosition(bLoc.getBlockX(), bLoc.getBlockY(), bLoc.getBlockZ()), 2, true)), PICK_BOMB);
								}
							}
							break;
						case GUNGAME:
							switch (act == null ? DIRECT : act) {
							case GO_CTS:
								if (bot.arena().getClosePos(bot.w, bot.da(), false, true).tm == Team.CTs) {
									changeTLoc(Main.getNrVec(bot.arena().getClosestPos(bot.da(), 2, false)), GO_TS);
								}
								break;
							case GO_TS:
								if (bot.arena().getClosePos(bot.w, bot.da(), false, true).tm == Team.Ts) {
									changeTLoc(Main.getNrVec(bot.arena().getClosestPos(bot.da(), 2, true)), GO_CTS);
								}
								break;
							case DIRECT:
								if (Main.srnd.nextBoolean()) {
									changeTLoc(Main.getNrVec(bot.arena().getClosestPos(bot.da(), 2, true)), GO_CTS);
								} else {
									changeTLoc(Main.getNrVec(bot.arena().getClosestPos(bot.da(), 2, false)), GO_TS);
								}
								break;
							default:
								break;
							}
							break;
						case INVASION:
							break;
						}
					}
				} else {//shoot
					vc = bot.tgt.getPos(true).subtract(loc.toVector());
					if (!rld && agro > REACT) {
						bot.tryShoot(rplc, eyel);
						bot.tryShoot(rplc, eyel);
					}
					tLoc = getStrfLoc(loc, vc);
				}
			}
			
			vc.normalize();
			
			bot.pickupIts(loc, rplc);
			
			bot.move(loc, vc, true);

			if (bot.tryJump(loc, rplc, vc)) {
				return;
			}
			
			vc.setY(0d);
			if (rplc.isInWater()) {
				rplc.setVelocity(rplc.getVelocity().setY(0.1d).add(vc.multiply(0.05d)));
			} else if (tLoc != null) {
				if (rplc.isOnGround()) {
					
					if (direct) {
						pth.moveTo(tLoc.toLocation(bot.w), 1.4d);
						return;
					}
					
					if (pathTo == null) {
						//closest spot to tLoc
						pathTo = bot.arena().getClosePos(bot.w, new BaseBlockPosition(
							tLoc.getBlockX(), tLoc.getBlockY(), tLoc.getBlockZ()), true, true);
						return;
					}
					
					if (next == null) {
						//closest spot to bot
						next = bot.arena().getClosePos(bot.w, bot.da(), true, true);
						return;
					}
					
					if (Arena.distSq(bot.da(), next) < SPOT_DST) {
						final Spot sp = next;
						final int rl = sp.getPosFrom(pathTo);
						if (rl == 0) {
							changeTLoc(tLoc, DIRECT);
							return;
						}
						next = null; //agro = 0;
						for (final Spot s : bot.arena().getPoss()) {
							if (s.getPosFrom(pathTo) == rl - 1 && s.getPosFrom(sp) == 1) {
								next = s;
							}
						}
						
//						if (tm == Team.Ts) {
//							Bukkit.broadcast(Component.text(bot.name() + " §9 pt-" + pathTo.toString() + " next " + rl + " to " + (next == null ? "null" : next.getPosFrom(pathTo))));
//						}
						
						if (next == null) return;
					}
					
					pth.moveTo(Main.getNrLoc(next, bot.w), 1.5d);

				} else {
					if (pth.hasPath()) pth.stopPathfinding(); 
					rplc.setVelocity(rplc.getVelocity().add(vc.multiply(0.05d)));
				}
			}
		}
    }
    
    private void changeTLoc(final Vector nlc, final ActType at) {
    	direct = at == DIRECT;
    	if (at != act || direct || (tick & 255) == 0) {
        	act = at;
        	pathTo = null;
        	next = null;
        	tLoc = nlc;
    	}
    }
	
	private Vector getStrfLoc(final Location from, final Vector vc) {
		if (Main.srnd.nextBoolean()) {
			final BlockFace bf = getVec4Face(vc).getOppositeFace();
			if (bf.getModX() == 0) {
				return new Vector(from.getX() + (strX = Main.srnd.nextBoolean() ? 2 : -2), 
				from.getY(), from.getZ() + (strZ = (Main.srnd.nextBoolean() ? 1 : 2) * bf.getModZ()));
			} else {
				return new Vector(from.getX() + (strX = (Main.srnd.nextBoolean() ? 1 : 2) * bf.getModX()), 
				from.getY(), from.getZ() + (strZ = Main.srnd.nextBoolean() ? 2 : -2));
			}
		}
		return new Vector(from.getX() + strX, from.getY(), strZ);
	}

	private BlockFace getVec4Face(final Vector vc) {
		if (Math.abs(vc.getX()) > Math.abs(vc.getZ())) {
			return vc.getX() > 0 ? BlockFace.EAST : BlockFace.WEST;
		} else {
			return vc.getZ() > 0 ? BlockFace.SOUTH : BlockFace.NORTH;
		}
	}

	@Override
    public GoalKey<Husk> getKey() {
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