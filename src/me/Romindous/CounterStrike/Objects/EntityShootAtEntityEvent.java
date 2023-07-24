package me.Romindous.CounterStrike.Objects;

import java.util.EnumMap;
import java.util.Map;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import com.google.common.base.Function;

public class EntityShootAtEntityEvent extends EntityDamageByEntityEvent {

	private final boolean hst;
	public final boolean wb;
	public final boolean nscp;

	@SuppressWarnings("deprecation")
	private static final Map<DamageModifier, Double> mods = new EnumMap<DamageModifier, Double>(DamageModifier.class);
	@SuppressWarnings("deprecation")
	private static final Map<DamageModifier, Function<Double, Double>> fnks = new EnumMap<DamageModifier, Function<Double, Double>>(DamageModifier.class);
	
	public EntityShootAtEntityEvent(final LivingEntity dmgr, final LivingEntity ent, final double dmg, final boolean hst, final boolean wb, final boolean nscp) {
		super(dmgr, ent, DamageCause.ENTITY_ATTACK, makeDmgMods(dmg), makeDmgFnks(dmg), hst);
		this.hst = hst;
		this.wb = wb;
		this.nscp = nscp;
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
		return hst;
	}
}
