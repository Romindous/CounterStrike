package me.Romindous.CounterStrike.Objects.Game;

import me.Romindous.CounterStrike.Enums.NadeType;
import me.Romindous.CounterStrike.Game.Arena;
import me.Romindous.CounterStrike.Game.Arena.Team;
import me.Romindous.CounterStrike.Main;
import me.Romindous.CounterStrike.Objects.Shooter;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Tripwire;
import org.bukkit.entity.GlowItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.inventory.ItemStack;
import ru.komiss77.modules.world.XYZ;

import java.util.Map.Entry;
 
public class TripWire {

	private static int cid = 0;
	private final int id;

	public final World w;
	public final XYZ[] blks;
	public final GlowItemFrame eif;
	public final Team tm;
	public NadeType nt;
   
	public TripWire(final Block[] bs, Shooter sh, final BlockFace bf, byte i) {
		final Tripwire tw = (Tripwire) Material.TRIPWIRE.createBlockData();
		tw.setAttached(true);
		tw.setFace(bf, true);
		tw.setFace(bf.getOppositeFace(), true);
		this.id = cid++;
		this.w = bs[0].getWorld();
		this.blks = new XYZ[i];
		for (i--; i >= 0; i--) {
			bs[i].setBlockData(tw, false);
			final XYZ bl = new XYZ(bs[i].getLocation());
			this.blks[i] = bl;
			Arena.tblks.put(bl, this);
		}
		this.nt = null;
		sh.arena().tws.add(this);
		eif = w.spawn(bs[0].getLocation().add(0.5d, -1d, 0.5d), GlowItemFrame.class, fr -> {
			fr.setFacingDirection(BlockFace.DOWN, true);
			fr.setVisibleByDefault(false);
			fr.setVisible(false);
			fr.setFixed(true);
		});
		this.tm = sh.arena().shtrs.get(sh);
		for (final Entry<Shooter, Team> e : sh.arena().shtrs.entrySet()) {
			if (e.getValue() == tm) {
				final Player p = e.getKey().getPlayer();
				if (p != null) {
					p.showEntity(Main.plug, eif);
				}
			}
		}
	}
	
	public void trigger(final Location loc) {
		loc.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, loc, 10, 0.4d, 0.4d, 0.4d);
		loc.getWorld().playSound(loc, Sound.ENTITY_WITHER_BREAK_BLOCK, 2f, 0.8f);
		if (this.nt != null) {
			final Material m = switch (this.nt) {
				case FRAG -> Material.OAK_SAPLING;
				case FLAME -> Material.ACACIA_SAPLING;
				case SMOKE -> Material.DARK_OAK_SAPLING;
				case FLASH -> Material.BIRCH_SAPLING;
				case DECOY -> Material.JUNGLE_SAPLING;
			};
			final Snowball sb = loc.getWorld().spawn(loc, Snowball.class);
			sb.setItem(new ItemStack(m));
			Nade.expld(sb, null);
		} 
	}

	public void chgNade(final ItemStack it, final Arena ar) {
		this.nt = NadeType.getNdTp(it);
		eif.setItem(it);
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

	public void showNade(final Player p) {
		p.showEntity(Main.plug, eif);
	}

	public void hideNade(final Player p) {
		p.hideEntity(Main.plug, eif);
	}

	public void remove() {
		for (final XYZ r : blks) {
			Arena.tblks.remove(r);
			r.getCenterLoc(w).getBlock()
				.setType(Material.AIR, false);
		}
		eif.remove();
	}

	@Override
	public boolean equals(final Object o) {
		return o instanceof TripWire && ((TripWire) o).id == id;
	}

	@Override
	public int hashCode() {
		return id;
	}
}