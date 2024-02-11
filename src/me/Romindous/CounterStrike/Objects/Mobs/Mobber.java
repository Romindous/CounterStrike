package me.Romindous.CounterStrike.Objects.Mobs;

import me.Romindous.CounterStrike.Game.Invasion;
import me.Romindous.CounterStrike.Main;
import me.Romindous.CounterStrike.Objects.Shooter;
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
import ru.komiss77.modules.world.WXYZ;
import ru.komiss77.modules.world.XYZ;
import ru.komiss77.version.Nms;

import java.lang.ref.WeakReference;

public class Mobber extends WXYZ {
	
	public static final BlockData spn = Material
		.SPAWNER.createBlockData();
	
	public static final BlockData dfs = Material
		.CRYING_OBSIDIAN.createBlockData();
	
	public final BlockDisplay ind;
	private final Invasion ar;
	public Shooter defusing;
	public EntityType et;
  
	public Mobber(final XYZ loc, final Invasion ar) {
        super(ar.w, loc);
        this.ar = ar;
		ind = ar.w.spawn(new Location(ar.w, loc.x, loc.y, loc.z), BlockDisplay.class);
		ind.setGravity(false);
		ind.setViewRange(100f);
//		ind.setDisplayWidth(1.2f);
//		ind.setDisplayHeight(1.2f);
		ind.setBillboard(Billboard.FIXED);
		setSpwn();
		et = EntityType.ZOMBIE_VILLAGER;
		
		final Block b = getBlock();
		b.setType(Material.SPAWNER, false);
		
		final CreatureSpawner cs = (CreatureSpawner) b.getState();
		cs.setSpawnedType(et);
		cs.setSpawnCount(0);
		cs.update();

		ar.mbbrs.put(this.getSLoc(), this);
	}

	public void spwnMb() {
		final float hlth = switch (et) {
			case PIGLIN_BRUTE -> 12f;
			case PILLAGER -> 8f;
			case STRAY -> 10f;
            default -> 8f;
        };

        final Location loc = getCenterLoc();
		final Mob mb = (Mob) ar.w.spawnEntity(Main.getNrLoc(loc), et, false);
		mb.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(getMbSpd(et));
		mb.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(hlth);
		mb.setHealth(hlth);
        //Bukkit.getMobGoals().removeAllGoals(mb);
        Bukkit.getMobGoals().removeAllGoals(mb);
        Bukkit.getMobGoals().addGoal(mb, 0, new GoalGoToSite(mb, ar));
		ar.TMbs.put(mb.getEntityId(), new WeakReference<>(mb));
		ar.w.spawnParticle(Particle.SOUL, loc, 40, 0.6D, 0.6D, 0.6D, 0.0D, null, false);
		
		if (mb instanceof PiglinBrute) {
			((PiglinBrute) mb).setImmuneToZombification(true);
		}
	}
	
	public static double getMbSpd(final EntityType e) {
        return switch (e) {
			case PIGLIN_BRUTE -> 0.52d;
			case PILLAGER -> 0.55d;
			case STRAY -> 0.52d;
            default -> 0.50d;
        };
	}
	
	public static int getMbPow(final EntityType e) {
        return switch (e) {
			case PIGLIN_BRUTE -> 3;
			case PILLAGER -> 2;
			case STRAY -> 1;
            default -> 0;
        };
	}

	public Material getType() {
		return ind.getBlock().getMaterial();
	}

	public boolean isAlive() {
		return ind.isGlowing();
	}

	public void setSpwn() {
		ind.setBlock(spn);
		Nms.colorGlow(ind, '4', false);
	}

	public void setDef() {
		ind.setBlock(dfs);
		ind.setGlowing(false);
		defusing = null;
	}
}
