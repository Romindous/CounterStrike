package me.Romindous.CounterStrike.Objects;

import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class EntityShootAtEntityEvent extends EntityDamageByEntityEvent {

	public final boolean hst;
	public final boolean wb;
	public final boolean nscp;
	
	public EntityShootAtEntityEvent(final DmgdEnt de) {
		super(de.dmgr, de.ent, DamageCause.ENTITY_ATTACK, de.dmg);
		this.hst = de.hst;
		this.wb = de.wb;
		this.nscp = de.nscp;
	}

}
