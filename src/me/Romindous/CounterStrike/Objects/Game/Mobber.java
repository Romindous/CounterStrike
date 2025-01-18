package me.Romindous.CounterStrike.Objects.Game;

import java.lang.ref.WeakReference;
import me.Romindous.CounterStrike.Game.Invasion;
import me.Romindous.CounterStrike.Main;
import me.Romindous.CounterStrike.Menus.DefuseMenu;
import me.Romindous.CounterStrike.Objects.Defusable;
import me.Romindous.CounterStrike.Objects.Mobs.GoalGoToSite;
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
import ru.komiss77.modules.world.XYZ;
import ru.komiss77.version.Nms;

public class Mobber extends Defusable {
	
	public static final BlockData spn = Material
		.SPAWNER.createBlockData();
	
	public static final BlockData dfs = Material
		.CRYING_OBSIDIAN.createBlockData();

	public final DefuseMenu inv;
	public final BlockDisplay ind;
	private final Invasion ar;
	private Shooter defusing;
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
		inv = new DefuseMenu(this).fillUp(mt.pow * 0.15f + 0.2f);
	}

	public Invasion arena() {
		return ar;
	}

	public Shooter defusing() {
		return defusing;
	}

	public void defusing(final Shooter sh) {
		defusing = sh;
	}

	public void spwnMb() {
        final Location loc = getCenterLoc();
		final Mob mb = (Mob) ar.w.spawnEntity(Main.getNrLoc(loc), mt.type, false);
		mb.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(mt.spd);
		mb.getAttribute(Attribute.MAX_HEALTH).setBaseValue(mt.hp);
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

	public void set(final MobType mt) {
		this.mt = mt;
		inv.fillUp(mt.pow * 0.15f + 0.2f);
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
				case STRAY -> NORM;
				case VINDICATOR -> DANG;
				case WITHER_SKELETON -> TERM;
				default -> WEAK;
			};
		}
	}
}
