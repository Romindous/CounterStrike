package me.Romindous.CounterStrike.Objects.Bots;

import java.util.EnumSet;
import java.util.Map.Entry;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Husk;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.destroystokyo.paper.entity.Pathfinder;
import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;

import me.Romindous.CounterStrike.Main;
import me.Romindous.CounterStrike.Game.Arena.Team;
import me.Romindous.CounterStrike.Game.Defusal;
import me.Romindous.CounterStrike.Objects.Shooter;
import me.Romindous.CounterStrike.Objects.Game.BtShooter;
import me.Romindous.CounterStrike.Utils.Inventories;
import net.minecraft.core.BaseBlockPosition;


public class BotGoal implements Goal<Husk> {
    
    public static final int MAX_LIVE_TICKS = 1000000;
	private static final int BOMB_DST = 200;
	private static final int ACT_DST = 10;
    
    private final GoalKey<Husk> key;
    private final BtShooter bot;
    private final Mob rplc;
	private final Pathfinder pth;
	private final Team tm;
	
	private Vector tLoc;
	private int strX;
	private int strZ;

	private boolean hasBomb;
	private BaseBlockPosition site;
	
	private boolean isActSite;
	
	private int tick;
    
    public BotGoal(final BtShooter bot, final Husk hs) {
        this.key = GoalKey.of(Husk.class, new NamespacedKey(Main.plug, "bot"));
        this.tm = bot.arena().shtrs.get(bot);
        this.bot = bot;
        this.rplc = hs;
		this.pth = rplc.getPathfinder();
		this.tLoc = null;
		this.tick = Main.srnd.nextInt(16);
		this.isActSite = false;
		this.hasBomb = false;
		this.site = null;
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
			bot.tryBuy();
			tick++;
			
			//Bukkit.broadcast(Component.text("le-" + rplc.getName()));
			final Location loc = rplc.getLocation();
			final Location eyel = rplc.getEyeLocation();
			final Vector vc;
			
			bot.tryReload(rplc, eyel);
			final boolean rld = bot.tryReload(rplc, eyel);
			
			if (!rplc.hasPotionEffect(PotionEffectType.SLOW) && //blind or buy
				!rplc.hasPotionEffect(PotionEffectType.BLINDNESS)) {
				if (bot.tgt == null || bot.tgt.isDead()) {//look for tgt
					if ((tick & 3) == 0) {
						final Team tm = bot.arena().shtrs.get(bot);
						for (final Entry<Shooter, Team> en : bot.arena().shtrs.entrySet()) {
							if (en.getKey().isDead() || en.getValue() == tm) continue;
							final LivingEntity le = en.getKey().getEntity();
							if (le != null && Main.rayThruAir(eyel, le.getEyeLocation().toVector(), 0.1F)) {
								bot.tgt = Shooter.getShooter(le, false);
								break;
							}
						}
					}
				} else {
					if ((tick & 7) == 0) {
						final Vector pos = bot.tgt.getPos();
						if (!Main.rayThruAir(eyel, pos, 0.1F)) {
							tLoc = pos;
							bot.tgt = null;
						}
					}
				}

				if (isActSite) {//is busy
					if ((tick & 3) == 0) {
						bot.w.playSound(rplc.getLocation(), Sound.ENTITY_GENERIC_EAT, 2f, 1f);
					}
				} else {
					bot.switchToGun();
				}
				
				if (bot.tgt == null || bot.tgt.isDead()) {//go places
					if (isActSite) {//is busy
						if ((tick & 3) == 0) {
							if (bot.arena().getTime() > 100) isActSite = false;
							bot.w.playSound(rplc.getLocation(), tm == Team.Ts ? 
							Sound.ENTITY_GENERIC_EAT : Sound.BLOCK_TRIPWIRE_CLICK_ON, 2f, 1f);
						}
						vc = tLoc == null ? Main.getNrVec(bot.da()) : tLoc.subtract(eyel.toVector());
					} else {
						bot.switchToGun();
						vc = eyel.getDirection();
					}
					
					if (tLoc == null || (tick & 127) == 0) {
						hasBomb = !Inventories.isBlankItem(bot.item(7), false) && tm == Team.Ts;
						switch (bot.arena().getType()) {
						case DEFUSAL:
							final Defusal df = (Defusal) bot.arena();
							if (df.isBmbOn()) {
								if (site == null || !site.equals(df.getBombLoc())) {
									site = df.getBombLoc();
									tLoc = Main.getNrVec(df.getClosestPos(site, 1, true));
								} else {
									final int dst = df.distSq(site, bot.da());
									if (tm == Team.CTs && dst < BOMB_DST) {
										tLoc = Main.getNrVec(site);
										if (isActSite) {//defused
											isActSite = false;
											df.chngMn(bot, 250);
											df.defuse();
										} else if (dst < ACT_DST) {//start defuse
											isActSite = true;
											bot.swapToSlot(7);
										}
									} else {
										tLoc = Main.getNrVec(df.getClosestPos(site, 2, true));
									}
								}
							} else {
								if (site == null) {
									site = Main.srnd.nextBoolean() ? df.ast : df.bst;
									tLoc = Main.getNrVec(df.getClosestPos(site, 1, false));
								} else {
									final int dst = df.distSq(site, bot.da());
									if (hasBomb && dst < BOMB_DST) {
										tLoc = Main.getNrVec(site);
										if (isActSite) {//planted
											final Block b = rplc.getLocation().getBlock();
											if (b.getType().isAir()) {
												isActSite = false;
												b.setType(Material.CRIMSON_BUTTON, false);
												df.chngMn(bot, 250);
												df.plant(b);
												tLoc = Main.getNrVec(df.getClosestPos(site, 1, true));
											}
										} else if (dst < ACT_DST) {//start plant
											isActSite = true;
											bot.swapToSlot(7);
										}
									} else {
										tLoc = Main.getNrVec(df.getClosestPos(site, 1, true));
									}
								}
							}
							break;
						case GUNGAME:
							break;
						case INVASION:
							break;
						}
					}
				} else {//shoot
					vc = bot.tgt.getPos().subtract(loc.toVector());
					if (!rld) {
						bot.tryShoot(rplc, eyel);
						bot.tryShoot(rplc, eyel);
					}
					tLoc = getStrfLoc(loc, vc);
				}
			} else {
				vc = eyel.getDirection();
				
				tLoc = null;
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
					pth.moveTo(tLoc.toLocation(bot.w), 1.4d);
				} else {
					if (pth.hasPath()) pth.stopPathfinding(); 
					rplc.setVelocity(rplc.getVelocity().add(vc.multiply(0.05d)));
				}
			}
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
}