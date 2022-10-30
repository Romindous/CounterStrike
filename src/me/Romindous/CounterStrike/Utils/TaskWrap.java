package me.Romindous.CounterStrike.Utils;

import org.bukkit.scheduler.BukkitTask;

public class TaskWrap {
	
	public BukkitTask tsk;

	public TaskWrap() {
		this.tsk = null;
	}

	public void cancel() {
		if (tsk != null && !tsk.isCancelled()) {
			tsk.cancel();
		}
	}
}
