package me.Romindous.CounterStrike.Objects;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Tripwire;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrowableProjectile;
import org.bukkit.inventory.ItemStack;

import me.Romindous.CounterStrike.Enums.NadeType;
import me.Romindous.CounterStrike.Utils.PacketUtils;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.projectile.EntitySnowball;
import net.minecraft.world.level.World;
 
public class TripWire {
	
	public final Block[] bs;
	public final String nm; 
	public NadeType nt;
   
	public TripWire(final Block[] bs, final String nm, final BlockFace bf, byte i) {
		final Tripwire tw = (Tripwire) Material.TRIPWIRE.createBlockData();
		tw.setAttached(true);
		tw.setFace(bf, true);
		tw.setFace(bf.getOppositeFace(), true);
		this.bs = new Block[i];
		for (i--; i >= 0; i--) {
			bs[i].setType(Material.TRIPWIRE, false);
			bs[i].setBlockData((BlockData)tw, false);
			this.bs[i] = bs[i];
		} 
		this.nm = nm;
		this.nt = null;
	}
	
	public void trgr(Player p, Location loc) {
		loc.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, loc, 10, 0.4d, 0.4d, 0.4d);
		loc.getWorld().playSound(loc, Sound.ENTITY_WITHER_BREAK_BLOCK, 2f, 0.8f);
		if (this.nt != null) {
			final Material m; 
			final World w = PacketUtils.getNMSWrld(p.getWorld());
			final EntitySnowball sb = new EntitySnowball(EntityTypes.aG, w);
			switch (this.nt) {
				case FRAG:
					m = Material.OAK_SAPLING;
					break;
				case FIRE:
					m = Material.ACACIA_SAPLING;
					break;
				case SMOKE:
					m = Material.DARK_OAK_SAPLING;
					break;
				case FLASH:
					m = Material.BIRCH_SAPLING;
					break;
				case DECOY:
					m = Material.JUNGLE_SAPLING;
					break;
				default:
					m = Material.ALLIUM;
					break;
			} 
			sb.setItem(PacketUtils.getNMSIt(new ItemStack(m)));
			sb.setPosition(loc.getX(), loc.getY(), loc.getZ());
			sb.setShooter(PacketUtils.getNMSPlr(Bukkit.getPlayer(this.nm)));
			w.addEntity(sb);
			Nade.expld((ThrowableProjectile) sb.getBukkitEntity(), Bukkit.getPlayer(this.nm));
		} 
	}
}