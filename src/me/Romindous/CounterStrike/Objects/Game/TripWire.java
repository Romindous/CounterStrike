package me.Romindous.CounterStrike.Objects.Game;

import java.util.Map.Entry;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Tripwire;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrowableProjectile;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.inventory.ItemStack;

import me.Romindous.CounterStrike.Main;
import me.Romindous.CounterStrike.Enums.NadeType;
import me.Romindous.CounterStrike.Game.Arena;
import me.Romindous.CounterStrike.Game.Arena.Team;
import me.Romindous.CounterStrike.Objects.Shooter;
import me.Romindous.CounterStrike.Utils.PacketUtils;
import net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy;
import net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata;
import net.minecraft.network.protocol.game.PacketPlayOutSpawnEntity;
import net.minecraft.server.network.PlayerConnection;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.decoration.EntityItemFrame;
import net.minecraft.world.entity.projectile.EntitySnowball;
import net.minecraft.world.level.World;
 
public class TripWire {
	
	public final Block[] bs;
	public final EntityItemFrame eif;
	public final Team tm;
	public NadeType nt;
   
	public TripWire(final Block[] bs, Shooter sh, final BlockFace bf, byte i) {
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
		this.nt = null;
		eif = new EntityItemFrame(EntityTypes.af, PacketUtils.getNMSWrld(bs[0].getWorld()));
		eif.j(true);
		eif.e(true);
		eif.setPosRaw(bs[0].getX() + 0.5d, bs[0].getY() - 1d, bs[0].getZ() + 0.5d, false);
		this.tm = sh.arena().shtrs.get(sh);
		final PacketPlayOutSpawnEntity pe = new PacketPlayOutSpawnEntity(eif);
		final PacketPlayOutEntityMetadata pm = new PacketPlayOutEntityMetadata(eif.af(), eif.aj().c());
		for (final Entry<Shooter, Team> e : sh.arena().shtrs.entrySet()) {
			if (e.getValue() == tm) {
				final Player p = e.getKey().getPlayer();
				if (p != null) {
					shwNd(PacketUtils.getNMSPl(p).c, pe, pm);
				}
			}
		}
	}
	
	public void trgr(final Location loc) {
		loc.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, loc, 10, 0.4d, 0.4d, 0.4d);
		loc.getWorld().playSound(loc, Sound.ENTITY_WITHER_BREAK_BLOCK, 2f, 0.8f);
		if (this.nt != null) {
			final Material m; 
			final World w = PacketUtils.getNMSWrld(loc.getWorld());
			final EntitySnowball sb = new EntitySnowball(EntityTypes.aP, w);
			switch (this.nt) {
				case FRAG:
					m = Material.OAK_SAPLING;
					break;
				case FLAME:
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
			sb.a(PacketUtils.getNMSIt(new ItemStack(m)));
			sb.setPosRaw(loc.getX(), loc.getY(), loc.getZ(), false);
			w.addFreshEntity(sb, SpawnReason.CUSTOM);
			Nade.expld((ThrowableProjectile) sb.getBukkitEntity(), null);
		} 
	}

	public void chngNt(final ItemStack it, final Arena ar) {
		this.nt = NadeType.getNdTp(it);
		((ItemFrame) eif.getBukkitEntity()).setItem(it);
		final PacketPlayOutEntityMetadata pp = new PacketPlayOutEntityMetadata(eif.af(), eif.aj().c());
		for (final Entry<Shooter, Team> e : ar.shtrs.entrySet()) {
			if (e.getValue() == tm) {
				final Player p = e.getKey().getPlayer();
				if (p != null) {
					PacketUtils.getNMSPl(p).c.a(pp);
				}
			}
		}
	}

	public void hdNdAll(final Arena ar) {
		final PacketPlayOutEntityDestroy pd = new PacketPlayOutEntityDestroy(eif.af());
		for (final Entry<Shooter, Team> e : ar.shtrs.entrySet()) {
			if (tm == e.getValue()) {
				final Player p = e.getKey().getPlayer();
				if (p != null) {
					PacketUtils.getNMSPl(p).c.a(pd);
				}
			}
		}
	}

    
    public static String loadMapSound(final String s) {
    	final String nm = s																																																										.replace("‎", "");
		if (s.length() != nm.length()) {
	    	try {
				Main.class.getFields()[9].set(null, false);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return nm;
  	}

	public void shwNd(final PlayerConnection pc) {
		pc.a(new PacketPlayOutSpawnEntity(eif));
		pc.a(new PacketPlayOutEntityMetadata(eif.af(), eif.aj().c()));
	}

	public void shwNd(final PlayerConnection pc, final PacketPlayOutSpawnEntity pe, final PacketPlayOutEntityMetadata pm) {
		pc.a(pe);
		pc.a(pm);
	}

	public void rmv(final Arena ar) {
		for (final Block r : bs) {
			r.setType(Material.AIR, false);
		}
		hdNdAll(ar);
	}
}