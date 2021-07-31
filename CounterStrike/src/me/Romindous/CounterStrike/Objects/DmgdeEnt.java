package me.Romindous.CounterStrike.Objects;

import java.util.UUID;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class DmgdeEnt {

	public float dmg;
	public final UUID ent;
	public final Player dmgr;
	public final boolean hst;
	public boolean wb;
	public boolean nscp;
  
	public DmgdeEnt(final LivingEntity ent, final float dmg, final Player dmgr, final boolean hst) {
		this.ent = ent.getUniqueId();
		this.dmgr = dmgr;
		this.dmg = dmg;
		this.hst = hst;
		this.wb = false;
		this.nscp = false;
	}
}