package me.Romindous.CounterStrike.Mobs;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;
import org.bukkit.entity.PiglinBrute;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityPotionEffectEvent.Cause;
import org.bukkit.scheduler.BukkitRunnable;

import me.Romindous.CounterStrike.Main;
import me.Romindous.CounterStrike.Enums.GameState;
import me.Romindous.CounterStrike.Game.Invasion;
import net.minecraft.core.BaseBlockPosition;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.monster.EntityShulker;
import net.minecraft.world.entity.monster.EntitySkeletonStray;
import net.minecraft.world.entity.monster.EntityVindicator;
import net.minecraft.world.entity.monster.EntityZombieVillager;
import net.minecraft.world.entity.monster.piglin.EntityPiglinBrute;
import net.minecraft.world.level.World;

public class Mobber extends BukkitRunnable {
	
	public final EntityShulker ind;
	private final Invasion ar;
  
	public Mobber(final BaseBlockPosition loc, final World w, final Invasion ar) {
		this.ar = ar;
		ind = new EntityShulker(EntityTypes.ay, w);
		ind.m(true);
		ind.e(true);
		ind.j(true);
		ind.d(true);
		ind.i(true);
		ind.setPosRaw(loc.u() + 0.5d, loc.v(), loc.w() + 0.5d, false);
		ind.s(true);
		ind.collides = false;
		ind.addEffect(new MobEffect(MobEffects.n, 100000, 1, true, false), Cause.UNKNOWN);
		w.addFreshEntity(ind, SpawnReason.CUSTOM);
		final Block b = Main.getBBlock(loc, w.getWorld());
		b.setType(Material.SPAWNER, false);
		final CreatureSpawner cs = (CreatureSpawner) b.getState();
		cs.setSpawnedType(EntityType.ZOMBIE_VILLAGER);
		cs.setSpawnCount(0);
		cs.update();
		/*
		final Shulker sh = (Shulker) w.getWorld().spawnEntity(b.getLocation().add(0.5d, 0d, 0.5d), EntityType.SHULKER);
		sh.setInvulnerable(true);
		sh.setGravity(false);
		sh.setInvisible(true);
		sh.setSilent(true);
		sh.setGlowing(true);
		sh.setCollidable(false);
		sh.setAI(false);sh.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 1000000, 1));
		Bukkit.broadcastMessage(sh.getLocation().toString());
		ind = (EntityShulker) w.getMinecraftWorld().getEntity(sh.getUniqueId());
		*/
	}

	@Override
	public void run() {
		final Block b = Main.getBBlock(ind.cW(), ind.t.getWorld());
		if (ind.bT() && b.getType() == Material.SPAWNER && Main.srnd.nextInt(ar.cnt) == 0 && ar != null && ar.gst == GameState.ROUND) {
			spwnMb(b);
		}
	}

	public void spwnMb(final Block b) {
		final EntityType et = ((CreatureSpawner) b.getState()).getSpawnedType();
		final float hlth;
		switch (et) {
		case ZOMBIE_VILLAGER:
		default:
			hlth = 8f;
			break;
		case STRAY:
			hlth = 10f;
			break;
		case VINDICATOR:
			hlth = 8f;
			break;
		case PIGLIN_BRUTE:
			hlth = 12f;
			break;
		}
		final Mob mb = (Mob) ar.w.spawnEntity(Main.getNrLoc(ind.cW(), ar.w), et, false);
		mb.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(getMbSpd(et));
		mb.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(hlth);
		mb.setHealth(hlth);
        //Bukkit.getMobGoals().removeAllGoals(mb);
        Bukkit.getMobGoals().addGoal(mb, 5, new GoalGoToSite(mb, ar));
		ar.TMbs.add(mb.getUniqueId());
		b.getWorld().spawnParticle(Particle.SOUL, b.getLocation(), 40, 0.6D, 0.6D, 0.6D, 0.0D, null, false);
		
		if (mb instanceof PiglinBrute) {
			((PiglinBrute) mb).setImmuneToZombification(true);
		}
	}
	
	public double getMbSpd(final EntityType e) {
		switch (e) {
		case STRAY:
			return 0.25d;
		case PIGLIN_BRUTE:
			return 0.3d;
		case VINDICATOR:
			return 0.35d;
		case ZOMBIE_VILLAGER:
		default:
			return 0.25d;
		}
	}
}
