package me.Romindous.CounterStrike.Objects;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class DmgdEnt {
	
	public final LivingEntity ent;
	public float dmg;
	public final Player dmgr;
	public final boolean hst;
	public boolean wb;
	public boolean nscp;
  
	public DmgdEnt(final LivingEntity ent, final float dmg, final Player dmgr, final boolean hst) {
		this.ent = ent;
		this.dmg = dmg;
		this.dmgr = dmgr;
		this.hst = hst;
		this.wb = false;
		this.nscp = false;
	}
}