package me.Romindous.CounterStrike.Mobs;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.scheduler.BukkitRunnable;

import me.Romindous.CounterStrike.Main;
import me.Romindous.CounterStrike.Game.Invasion;
import net.minecraft.core.BaseBlockPosition;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.monster.EntityMonster;
import net.minecraft.world.entity.monster.EntityShulker;
import net.minecraft.world.level.World;

public class Mobber extends BukkitRunnable {
	
	public final EntityShulker ind;
	private final Invasion ar;
  
	public Mobber(final BaseBlockPosition loc, final World w, final Invasion ar) {
		this.ar = ar;
		ind = new EntityShulker(EntityTypes.ay, w);
		ind.setInvulnerable(true);
		ind.setNoGravity(true);
		ind.setInvisible(true);
		ind.setSilent(true);
		ind.setGlowingTag(true);
		ind.setPosition(loc.getX() + 0.5d, loc.getY(), loc.getZ() + 0.5d);
		ind.setNoAI(true);
		ind.collides = false;
		ind.addEffect(new MobEffect(MobEffects.n, 100000, 1, true, false));
		w.addEntity(ind);
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
		final Block b = Main.getBBlock(ind.getChunkCoordinates(), ind.getWorld().getWorld());
		if (ind.isCurrentlyGlowing() && b.getType() == Material.SPAWNER && Main.srnd.nextInt(ar.cnt) == 0) {
			spwnMb(b);
		}
	}

	public void spwnMb(final Block b) {
		final EntityTypes<? extends EntityMonster> et;
		final float hlth;
		switch (((CreatureSpawner) b.getState()).getSpawnedType()) {
		case ZOMBIE_VILLAGER:
		default:
			et = EntityTypes.bg;
			hlth = 8f;
			break;
		case STRAY:
			et = EntityTypes.aK;
			hlth = 10f;
			break;
		case VINDICATOR:
			et = EntityTypes.aW;
			hlth = 8f;
			break;
		case PIGLIN_BRUTE:
			et = EntityTypes.ap;
			hlth = 12f;
			break;
		}
		ar.TMbs.add(new GameMob(et, ind.getChunkCoordinates(), ind.getWorld(), ar, hlth).getUniqueID());
		b.getWorld().spawnParticle(Particle.SOUL, b.getLocation(), 40, 0.6D, 0.6D, 0.6D, 0.0D, null, false);
	}
}
