package me.Romindous.CounterStrike.Mobs;

import java.util.EnumSet;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Mob;

import com.destroystokyo.paper.entity.Pathfinder.PathResult;
import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;

import me.Romindous.CounterStrike.Main;
import me.Romindous.CounterStrike.Game.Invasion;
import net.minecraft.core.BaseBlockPosition;

public class GoalGoToSite implements Goal<Mob> {
	
    private final Mob mob;

    private final GoalKey<Mob> key;
	private final BaseBlockPosition ast;
	private final BaseBlockPosition bst;
	private int itr = 0;
    
    public GoalGoToSite(final Mob mob, final Invasion ar) {
        this.key = GoalKey.of(Mob.class, new NamespacedKey(Main.plug, "site"));
        this.mob = mob;
        this.ast = ar.ast == null ? null : ar.ast.da();
        this.bst = ar.bst == null || !ar.bst.bW() ? null : ar.bst.da();
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
    	itr = itr == 7 ? 0 : itr + 1;
    	if (((mob.getTicksLived() - itr) & 7) == 0 && ast != null) {
    		/*for (final Player p : mob.getWorld().getPlayers()) {
    			if (p.getGameMode() == GameMode.SURVIVAL && Main.rayThruAir(loc, p.getEyeLocation(), 0.2F)) {
    				mob.setTarget(p);
    				return;
    			}
    		}*/
    		final Location loc = mob.getEyeLocation();
			final Location pthTo;
    		if (bst == null) {
    			pthTo = new Location(mob.getWorld(), ast.u(), ast.v(), ast.w());
    		} else {
    			pthTo = Main.twoDisQuared(new BaseBlockPosition(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()), ast) < Main.twoDisQuared(new BaseBlockPosition(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()), bst) 
    				? new Location(mob.getWorld(), ast.u(), ast.v(), ast.w()) : new Location(mob.getWorld(), bst.u(), bst.v(), bst.w());
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
