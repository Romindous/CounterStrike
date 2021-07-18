package me.Romindous.CounterStrike.Objects;

import java.util.HashSet;
import net.minecraft.core.BlockPosition;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.Romindous.CounterStrike.Main;
 
public class SmplLoc extends BlockPosition {
	
	public final World w;
	public short cnt;
   
	public SmplLoc(final Block b, final short i) {
		super(b.getX(), b.getY(), b.getZ());
		this.w = b.getWorld();
		this.cnt = i;
	}
	
	@Override
	public boolean equals(final Object o) {
		return super.equals(o);
	}
   
	public Block getBlock() {
		return this.w.getBlockAt(getX(), getY(), getZ());
	}
   
	public void expldBmb() {
		Main.bmbs.remove(this);
		final Block b = this.w.getBlockAt(getX(), getY(), getZ());
		b.setType(Material.AIR);
		final int X = b.getX();
		final int Y = b.getY();
		final int Z = b.getZ();
		final HashSet<Block> cls = new HashSet<>();
		b.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, b.getLocation(), 20, 5d, 5d, 5d);
		b.getWorld().playSound(b.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 5f, 0.8f);
		for (int x = X - 5; x <= X + 5; x++) {
			for (int y = Y - 5; y <= Y + 5; y++) {
				for (int z = Z - 5; z <= Z + 5; z++) {
					final Block f = b.getRelative(X - x, Y - y, Z - z);
					final int bnd = (X - x) * (X - x) + (Y - y) * (Y - y) + (Z - z) * (Z - z);
					if (bnd > 0 && f.getType().isAir() && f.getRelative(BlockFace.DOWN).getType().isOccluding() && Main.srnd.nextInt(bnd) < 4) {
						for (final Player p : b.getWorld().getPlayers()) {
							p.sendBlockChange(f.getLocation(), Material.FIRE.createBlockData());
							cls.add(f);
						} 
					} else if (f.getType().isOccluding() && Main.srnd.nextInt(bnd) < 6) {
						for (final Player p : b.getWorld().getPlayers()) {
							p.sendBlockChange(f.getLocation(), Material.COAL_BLOCK.createBlockData());
							cls.add(f);
						} 
					} 
				} 
			} 
		} 
		new BukkitRunnable() {
			public void run() {
				for (final Block b : cls) {
					for (final Player p : b.getWorld().getPlayers()) {
						p.sendBlockChange(b.getLocation(), b.getBlockData());
					}
				} 
			}
		}.runTaskLater(Main.plug, 200L);
	}
}