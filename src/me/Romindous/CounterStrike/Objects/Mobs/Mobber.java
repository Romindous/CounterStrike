package me.Romindous.CounterStrike.Objects.Mobs;

import java.lang.ref.WeakReference;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display.Billboard;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;
import org.bukkit.entity.PiglinBrute;
import org.bukkit.scheduler.BukkitRunnable;

import me.Romindous.CounterStrike.Main;
import me.Romindous.CounterStrike.Game.Invasion;
import me.Romindous.CounterStrike.Objects.Game.GameState;
import ru.komiss77.modules.world.XYZ;
import ru.komiss77.version.VM;

public class Mobber extends BukkitRunnable {
	
	public static final BlockData spn = Material
		.SPAWNER.createBlockData();
	
	public static final BlockData dfs = Material
		.CRYING_OBSIDIAN.createBlockData();
	
	public final BlockDisplay ind;
	private final Invasion ar;
	public EntityType et;
  
	public Mobber(final XYZ loc, final Invasion ar) {
		this.ar = ar;
		final Location lc = new Location(ar.w, loc.x, loc.y, loc.z);
		ind = ar.w.spawn(lc, BlockDisplay.class);
		ind.setGravity(false);
		ind.setViewRange(100f);
//		ind.setDisplayWidth(1.2f);
//		ind.setDisplayHeight(1.2f);
		ind.setBillboard(Billboard.FIXED);
		setSpwn();
		et = EntityType.ZOMBIE_VILLAGER;
		
		final Block b = lc.getBlock();
		b.setType(Material.SPAWNER, false);
		
		final CreatureSpawner cs = (CreatureSpawner) b.getState();
		cs.setSpawnedType(et);
		cs.setSpawnCount(0);
		cs.update();
		
//		for (final Player p : ar.w.getPlayers()) {
//			p.sendBlockChange(lc, stnd);
//		}
		ar.mbbrs.put(loc, this);
	}

	@Override
	public void run() {
		if (ind.isGlowing() && ind.getBlock().getMaterial() == Material.SPAWNER && 
			Main.srnd.nextInt(ar.cnt) == 0 && ar != null && ar.gst == GameState.ROUND) {
			spwnMb();
		}
	}

	public void spwnMb() {
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
		final Location loc = ind.getLocation();
		final Mob mb = (Mob) ar.w.spawnEntity(Main.getNrLoc(loc), et, false);
		mb.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(getMbSpd(et));
		mb.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(hlth);
		mb.setHealth(hlth);
        //Bukkit.getMobGoals().removeAllGoals(mb);
        Bukkit.getMobGoals().removeAllGoals(mb);
        Bukkit.getMobGoals().addGoal(mb, 0, new GoalGoToSite(mb, ar));
		ar.TMbs.put(mb.getEntityId(), new WeakReference<Mob>(mb));
		ar.w.spawnParticle(Particle.SOUL, loc, 40, 0.6D, 0.6D, 0.6D, 0.0D, null, false);
		
		if (mb instanceof PiglinBrute) {
			((PiglinBrute) mb).setImmuneToZombification(true);
		}
	}
	
	public static double getMbSpd(final EntityType e) {
		switch (e) {
		case ZOMBIE_VILLAGER:
		default:
			return 0.4d;
		case STRAY:
			return 0.45d;
		case VINDICATOR:
			return 0.55d;
		case PIGLIN_BRUTE:
			return 0.5d;
		}
	}
	
	public static int getMbPow(final EntityType e) {
		switch (e) {
		case ZOMBIE_VILLAGER:
		default:
			return 0;
		case STRAY:
			return 1;
		case VINDICATOR:
			return 2;
		case PIGLIN_BRUTE:
			return 3;
		}
	}

	public Material getType() {
		return ind.getBlock().getMaterial();
	}

	public void setSpwn() {
		ind.setBlock(spn);
		VM.getNmsEntitygroup().colorGlow(ind, '4', false);
	}

	public void setDef() {
		ind.setBlock(dfs);
		ind.setGlowing(false);
	}
}
