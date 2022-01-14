package me.Romindous.CounterStrike.Objects;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class EntityShootAtEntityEvent extends EntityDamageByEntityEvent {

	public final boolean hst;
	public final boolean wb;
	public final boolean nscp;
	
	public EntityShootAtEntityEvent(final Player dmgr, final LivingEntity ent, final double dmg, final boolean hst, final boolean wb, final boolean nscp) {
		super(dmgr, ent, DamageCause.ENTITY_ATTACK, dmg);
		this.hst = hst;
		this.wb = wb;
		this.nscp = nscp;
	}

}
