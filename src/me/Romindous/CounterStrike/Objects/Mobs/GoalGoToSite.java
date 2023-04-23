package me.Romindous.CounterStrike.Objects.Mobs;

import java.util.EnumSet;
import java.util.Map.Entry;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.destroystokyo.paper.entity.Pathfinder.PathResult;
import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;

import me.Romindous.CounterStrike.Main;
import me.Romindous.CounterStrike.Game.Arena.Team;
import me.Romindous.CounterStrike.Listeners.DmgLis;
import me.Romindous.CounterStrike.Game.Invasion;
import me.Romindous.CounterStrike.Objects.Shooter;
import me.Romindous.CounterStrike.Objects.Game.GameState;
import net.minecraft.core.BaseBlockPosition;

public class GoalGoToSite implements Goal<Mob> {
	
    private final Mob mob;

    private final GoalKey<Mob> key;
	private final Invasion ar;
	private Shooter tgt;
	private int tick = 0;
    
    public GoalGoToSite(final Mob mob, final Invasion ar) {
        this.key = GoalKey.of(Mob.class, new NamespacedKey(Main.plug, "site"));
        this.mob = mob;
        this.ar = ar;
        this.tgt = null;
    }
 
    @Override
    public boolean shouldActivate() {
        return true;
    }
 
    @Override
    public boolean shouldStayActive() {
        return !mob.isDead();//shouldActivate();
    }
 
    @Override
    public void start() {
    }
 
    @Override
    public void stop() {
    }
    
    @Override
    public void tick() {
    	if (((tick++) & 3) == 0 && ar.gst != GameState.FINISH && !mob.hasPotionEffect(PotionEffectType.BLINDNESS)) {
    		final Location eyel = mob.getEyeLocation();
    		final Location lc = mob.getEyeLocation();
    		
			if (tgt == null || tgt.isDead()) {//look for tgt
				for (final Entry<Shooter, Team> en : ar.shtrs.entrySet()) {
					if (en.getKey().isDead()) continue;
					final LivingEntity le = en.getKey().getEntity();
					if (le != null && Main.rayThruAir(eyel, le.getEyeLocation().toVector(), 0.1F)) {
						tgt = Shooter.getShooter(le, false);
						break;
					}
				}
			} else {
				final Vector pos = tgt.getPos(true);
				if (!Main.rayThruAir(eyel, pos, 0.1F)) {
					tgt = null;
				}
			}
			
			final Location pthTo;
			if (tgt == null || tgt.isDead()) {
				final BaseBlockPosition ap = ar.ast.da();
	    		if (ar.bst.bW()) {
	    			final BaseBlockPosition bp = ar.bst.da();
	    			pthTo = Main.twoDisQuared(new BaseBlockPosition(lc.getBlockX(), lc.getBlockY(), lc.getBlockZ()), ap) < Main.twoDisQuared(new BaseBlockPosition(lc.getBlockX(), lc.getBlockY(), lc.getBlockZ()), bp) 
	    				? new Location(mob.getWorld(), ap.u(), ap.v(), ap.w()) : new Location(mob.getWorld(), bp.u(), bp.v(), bp.w());
	    		} else {
	    			pthTo = new Location(mob.getWorld(), ap.u(), ap.v(), ap.w());
				}
			} else {//attack
				final LivingEntity le = tgt.getEntity();
				final Location ll = (pthTo = le.getLocation());
				//Bukkit.getLogger().info("" + loc.distanceSquared(ll));
				if (lc.distanceSquared(ll) < 4d) {
					DmgLis.prcDmg(le, tgt, null, Mobber.getMbPow(mob.getType()) * 0.4d + 1d, Team.NA.clr + mob.getName() + " §f\u929a", 5);
				}
			}
			//Bukkit.getLogger().info(pthTo.toString());
    		final PathResult pth = mob.getPathfinder().findPath(pthTo);
    		if (pth != null) {
    			mob.getPathfinder().moveTo(pth);
			}
    	}
    }

	@Override
	public GoalKey<Mob> getKey() {
		return key;
	}

	@Override
	public EnumSet<GoalType> getTypes() {
        return EnumSet.of(GoalType.MOVE);
	}
}
