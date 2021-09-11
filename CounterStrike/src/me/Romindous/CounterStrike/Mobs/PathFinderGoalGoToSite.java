package me.Romindous.CounterStrike.Mobs;

import java.util.EnumSet;

import me.Romindous.CounterStrike.Main;
import me.Romindous.CounterStrike.Enums.GameState;
import net.minecraft.core.BaseBlockPosition;
import net.minecraft.core.BlockPosition;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.goal.PathfinderGoal;

public class PathFinderGoalGoToSite extends PathfinderGoal {

	private final GameMob a; //entity
	
	private double c; //x
	private double d; //y
	private double e; //z
	
	private final double f; //speed
	
	public PathFinderGoalGoToSite(final GameMob a, final double spd) {
		this.a = a;
		this.f = spd;
		this.a(EnumSet.of(Type.a));
	}
	
	//runs every tick (event checker)
	@Override
	public boolean a() {
		if (a.ar == null || a.ar.gst != GameState.ROUND || a.getMot().getY() < -0.1d) {
			return false;
		}
		
		if (a.hasEffect(MobEffects.o)) {
			final BaseBlockPosition loc = a.getChunkCoordinates();
			this.c = loc.getX() + (Main.srnd.nextBoolean() ? 1 : -1);
			this.d = loc.getY() + (Main.srnd.nextBoolean() ? 1 : -1);
			this.e = loc.getZ() + (Main.srnd.nextBoolean() ? 1 : -1);
			return true;
		}
		
		final BlockPosition loc = this.a.ar.ast.getChunkCoordinates().a(this.a.getChunkCoordinates(), false) 
				> 
				this.a.ar.bst.getChunkCoordinates().a(this.a.getChunkCoordinates(), false) && this.a.ar.bst.isCurrentlyGlowing() 
				? this.a.ar.bst.getChunkCoordinates() : this.a.ar.ast.getChunkCoordinates();
		this.c = loc.getX() + 0.5d;
		this.d = loc.getY() + 0.5d;
		this.e = loc.getZ() + 0.5d;
		return true;
	}
	
	//running after return true and if b() is true
	public void c() {
		if (this.a.getNavigation().k() == null || !this.a.getNavigation().k().m().equals(new BlockPosition(c, d, e))) {
			this.a.getNavigation().a(this.c,this.d,this.e,this.f);
		}
	}
	
	//runs after c()
	public boolean b() {
		return !this.a.getNavigation().m();
	}
	
	//runs when b() is false
	public void d() {
		//
	}

}
