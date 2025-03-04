package me.Romindous.CounterStrike.Objects.Mobs;

import java.util.EnumSet;
import java.util.Map.Entry;
import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;
import me.Romindous.CounterStrike.Enums.GameState;
import me.Romindous.CounterStrike.Game.Arena.Team;
import me.Romindous.CounterStrike.Game.Invasion;
import me.Romindous.CounterStrike.Listeners.DmgLis;
import me.Romindous.CounterStrike.Main;
import me.Romindous.CounterStrike.Objects.Game.Mobber;
import me.Romindous.CounterStrike.Objects.Shooter;
import me.Romindous.CounterStrike.Utils.Utils;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.potion.PotionEffectType;
import ru.komiss77.modules.world.AStarPath;
import ru.komiss77.modules.world.BVec;
import ru.komiss77.version.Nms;

public class GoalGoToSite implements Goal<Mob> {
	
    private final Mob mob;
	private final Mobber.MobType mt;

    private final GoalKey<Mob> key;
	private final Invasion ar;
	private final AStarPath ap;
	private Shooter tgt;
	private int tick = 0;
    
    public GoalGoToSite(final Mob mob, final Invasion ar) {
        this.key = GoalKey.of(Mob.class, new NamespacedKey(Main.plug, "site"));
		this.mt = Mobber.MobType.get(mob.getType());
        this.mob = mob;
        this.ar = ar;
        this.tgt = null;
        
        ap = new AStarPath(mob, 1000, false);
        ap.setTgt(BVec.of(Main.srnd.nextBoolean() || ar.bds.getViewRange() == 0f
        	? ar.ads.getLocation() : ar.bds.getLocation()));
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
    public void tick() {
    	tick++;
    	if ((tick & 1) == 0 && ar.gst != GameState.FINISH && !mob.hasPotionEffect(PotionEffectType.BLINDNESS)) {
    		final Location eyel = mob.getEyeLocation();
    		final Location lc = mob.getEyeLocation();

			LivingEntity tle;
			if (tgt == null || tgt.isDead()) {//look for tgt
				tle = null;
				for (final Entry<Shooter, Team> en : ar.shtrs.entrySet()) {
					if (en.getKey().isDead()) continue;
					final LivingEntity le = en.getKey().getEntity();
					if (le != null && Utils.isSeen(eyel, le)) {
						tgt = Shooter.getShooter(le, false);
						Nms.setAggro(mob, true);
						tle = le;
						break;
					}
				}
			} else {
				tle = tgt.getEntity();
				if (tle == null || !Utils.isSeen(eyel, tle)) {
					Nms.setAggro(mob, false);
					tgt = null;
				}
			}
			
			final Location pthTo;
			if (tle == null || tgt == null || tgt.isDead()) {
				ap.tickGo(mt.spd);
			} else if ((tick & 7) == 0) {//attack
				pthTo = tle.getEyeLocation();
				if (lc.distanceSquared(pthTo) < 4d) {
					mob.swingMainHand();
					DmgLis.prcDmg(tle, tgt, null, mt.pow * 0.4d + 1d,
						Team.SPEC.clr + mob.getName() + " §f\u929a", 5);
				}
				
				mob.getPathfinder().moveTo(pthTo, mt.spd);
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
