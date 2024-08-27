package me.Romindous.CounterStrike.Objects;

import java.util.EnumMap;
import java.util.Map;

import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import com.google.common.base.Function;

public class EntityShootAtEntityEvent extends EntityDamageByEntityEvent {

	private final boolean headshot;
	public final boolean wallbang;
	public final boolean noscope;
	public final boolean smoked;

	@SuppressWarnings("deprecation")
	private static final Map<DamageModifier, Double> mods = new EnumMap<DamageModifier, Double>(DamageModifier.class);
	@SuppressWarnings("deprecation")
	private static final Map<DamageModifier, Function<Double, Double>> fnks = new EnumMap<DamageModifier, Function<Double, Double>>(DamageModifier.class);
	
	public EntityShootAtEntityEvent(final LivingEntity dmgr, final LivingEntity ent,
		final double dmg, final boolean headshot, final boolean wallbang, final boolean noscope, final boolean smoked) {
		super(dmgr, ent, DamageCause.ENTITY_ATTACK,
			DamageSource.builder(DamageType.PLAYER_ATTACK).build(),
			makeDmgMods(dmg), makeDmgFnks(dmg), headshot);
		this.headshot = headshot;
		this.wallbang = wallbang;
		this.noscope = noscope;
		this.smoked = smoked;
	}

	@SuppressWarnings("deprecation")
	private static Map<DamageModifier, Double> makeDmgMods(final double dmg) {
		mods.put(DamageModifier.BASE, dmg); return mods;
	}

	@SuppressWarnings("deprecation")
	private static Map<DamageModifier, Function<Double, Double>> makeDmgFnks(final double dmg) {
		fnks.put(DamageModifier.BASE, d -> {return dmg;}); return fnks;
	}
	
	@Override
	public boolean isCritical() {
		return headshot;
	}
}
