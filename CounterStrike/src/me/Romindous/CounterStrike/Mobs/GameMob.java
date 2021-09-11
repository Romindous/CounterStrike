package me.Romindous.CounterStrike.Mobs;

import me.Romindous.CounterStrike.Main;
import me.Romindous.CounterStrike.Game.Invasion;
import net.minecraft.core.BaseBlockPosition;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.entity.ai.goal.PathfinderGoalFloat;
import net.minecraft.world.entity.ai.goal.PathfinderGoalMeleeAttack;
import net.minecraft.world.entity.ai.goal.target.PathfinderGoalNearestAttackableTarget;
import net.minecraft.world.entity.monster.EntityMonster;
import net.minecraft.world.entity.monster.EntitySkeletonStray;
import net.minecraft.world.entity.monster.EntityVindicator;
import net.minecraft.world.entity.monster.EntityZombieVillager;
import net.minecraft.world.entity.monster.piglin.EntityPiglinBrute;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.World;

public class GameMob extends EntityMonster {
	
	final Invasion ar;

	public GameMob(final EntityTypes<? extends EntityMonster> et, final BaseBlockPosition loc, final World w, final Invasion ar, final float hlth) {
		super(et, w);
		this.setPosition((Main.srnd.nextBoolean() ? -1 : 1) + loc.getX() + 0.5d, loc.getY() + 0.1d, (Main.srnd.nextBoolean() ? -1 : 1) + loc.getZ() + 0.5d);
		this.setSlot(EnumItemSlot.a, new ItemStack(Items.a));
		this.setHealth(hlth);
		w.addEntity(this);
		this.ar = ar;
	}
	
	@Override
	public void initPathfinder() {
		
		this.bP.a(0, new PathfinderGoalFloat(this));
		this.bP.a(2, new PathfinderGoalMeleeAttack(this, getMbSpd(this), true));
		this.bP.a(3, new PathFinderGoalGoToSite(this, getMbSpd(this)));
		
		this.bQ.a(3, new PathfinderGoalNearestAttackableTarget<EntityHuman>(this, EntityHuman.class, true));
	}
	
	public double getMbSpd(final Entity e) {
		if (e instanceof EntityZombieVillager) {
			return 2d;
		} else if (e instanceof EntitySkeletonStray) {
			return 1.6d;
		} else if (e instanceof EntityVindicator) {
			return 1d;
		} else if (e instanceof EntityPiglinBrute) {
			return 0.8d;
		} else {
			return 1d;
		}
	}
}
