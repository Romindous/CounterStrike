package me.Romindous.CounterStrike.Objects.Mobs;

import me.Romindous.CounterStrike.Game.Invasion;
import me.Romindous.CounterStrike.Main;
import me.Romindous.CounterStrike.Objects.Shooter;
import net.kyori.adventure.text.format.NamedTextColor;
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
	public MobType mt;
  
	public Mobber(final XYZ loc, final Invasion ar) {
        super(ar.w, loc);
        this.ar = ar;
		ind = ar.w.spawn(new Location(ar.w, loc.x, loc.y, loc.z), BlockDisplay.class);
		ind.setGravity(false);
		ind.setViewRange(100f);
		ind.setBillboard(Billboard.FIXED);
		setSpwn();
		mt = MobType.WEAK;
		
		final Block b = getBlock();
		b.setType(Material.SPAWNER, false);
		
		final CreatureSpawner cs = (CreatureSpawner) b.getState();
		cs.setSpawnedType(mt.type);
		cs.setSpawnCount(0);
		cs.update();

		ar.mbbrs.put(this.getSLoc(), this);
	}

	public void spwnMb() {

        final Location loc = getCenterLoc();
		final Mob mb = (Mob) ar.w.spawnEntity(Main.getNrLoc(loc), mt.type, false);
		mb.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(mt.spd);
		mb.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(mt.hp);
		mb.setHealth(mt.hp);
        //Bukkit.getMobGoals().removeAllGoals(mb);
        Bukkit.getMobGoals().removeAllGoals(mb);
        Bukkit.getMobGoals().addGoal(mb, 0, new GoalGoToSite(mb, ar));
		ar.TMbs.put(mb.getEntityId(), new WeakReference<>(mb));
		ar.w.spawnParticle(Particle.SOUL, loc, 40, 0.6D, 0.6D, 0.6D, 0.0D, null, false);
		
		if (mb instanceof PiglinBrute) {
			((PiglinBrute) mb).setImmuneToZombification(true);
		}
	}

	public Material getType() {
		return ind.getBlock().getMaterial();
	}

	public boolean isAlive() {
		return ind.isGlowing();
	}

	public void setSpwn() {
		ind.setBlock(spn);
		Nms.colorGlow(ind, NamedTextColor.DARK_RED, false);
	}

	public void setDef() {
		ind.setBlock(dfs);
		ind.setGlowing(false);
		defusing = null;
	}

	public enum MobType {
		WEAK(EntityType.ZOMBIE_VILLAGER, 10d, 0.50d),
		NORM(EntityType.STRAY, 14d, 0.54d),
		DANG(EntityType.VINDICATOR, 8d, 0.58d),
		TERM(EntityType.WITHER_SKELETON, 16d, 0.58d);

		public final EntityType type;
		public final double hp;
		public final double spd;
		public final int pow;

		MobType(final EntityType type, final double hp, final double spd) {
			this.pow = ordinal();
            this.type = type;
			this.hp = hp;
			this.spd = spd;
        }

		public static MobType get(final EntityType type) {
			return switch (type) {
				default -> WEAK;
				case STRAY -> NORM;
				case VINDICATOR -> DANG;
				case WITHER_SKELETON -> TERM;
			};
		}
	}
}
