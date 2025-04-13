package me.romindous.cs.Objects.Game;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import io.papermc.paper.math.Position;
import me.romindous.cs.Game.Defusal;
import me.romindous.cs.Main;
import me.romindous.cs.Menus.DefuseMenu;
import me.romindous.cs.Objects.Defusable;
import me.romindous.cs.Objects.Shooter;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Display.Billboard;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.util.Transformation;
import org.joml.Vector3f;
import ru.komiss77.Ostrov;
import ru.komiss77.modules.world.BVec;
import ru.komiss77.utils.ItemUtil;
import ru.komiss77.utils.NumUtil;
import ru.komiss77.utils.TCUtil;
import ru.komiss77.version.Nms;

public class Bomb extends Defusable {

	public static final int WIRE_TIME = 5;

	public final DefuseMenu inv;
	public final TextDisplay title;
	private final Defusal ar;
	private Shooter defusing;
	
	private static final Component bnm = TCUtil.form("§l§кБiмба Поставлена!")
		.appendNewline().append(TCUtil.form("§7Обезвредьте §eкусачками §7или §3спец. набором§7!"));
	
	public Bomb(final Block b, final Defusal df) {
		super(b); ar = df;
		title = ar.w.spawn(center(ar.w).add(0d, 1d, 0d), TextDisplay.class);
		title.setPersistent(true);
		title.setBillboard(Billboard.VERTICAL);
		title.text(bnm);
		title.setShadowed(true);
		title.setSeeThrough(true);
		title.setViewRange(100f);
		final Transformation atr = title.getTransformation();
		title.setTransformation(new Transformation(atr.getTranslation(), 
			atr.getLeftRotation(), new Vector3f(1.6f, 1.6f, 1.6f), atr.getRightRotation()));
		defusing = null;
		inv = new DefuseMenu(this).fillUp(0.4f);
	}

	public Defusal arena() {
		return ar;
	}

	public Shooter defusing() {
		return defusing;
	}

	public void defusing(final Shooter sh) {
		defusing = sh;
	}
	   
	public void expld() {
		title.remove();
		final Block b = block(ar.w);
		b.setType(Material.AIR,false);
		final int X = b.getX();
		final int Y = b.getY();
		final int Z = b.getZ();
		final HashSet<BVec> cls = new HashSet<>();
		b.getWorld().spawnParticle(Particle.EXPLOSION, b.getLocation(), 20, 5d, 5d, 5d);
		b.getWorld().playSound(b.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 5f, 0.8f);

		for (int x = -5; x < 6; x++) {
			for (int y = -5; y < 6; y++) {
				for (int z = -5; z < 6; z++) {
					final int bnd = x*x + y*y + z*z;
					if (bnd > 0 && Nms.fastType(ar.w, X + x, Y + y, Z + z).isAir() && Nms.fastType(ar.w, X + x, Y + y - 1, Z + z).isOccluding() && Main.srnd.nextInt(bnd) < 6) {
						for (final Player p : b.getWorld().getPlayers()) {
							p.sendBlockChange(new Location(ar.w, X + x, Y + y, Z + z), Material.FIRE.createBlockData());
							cls.add(BVec.of(X + x, Y + y, Z + z));
						} 
					} else if (Nms.fastType(ar.w, X + x, Y + y, Z + z).isOccluding() && Main.srnd.nextInt(bnd) < 10) {
						for (final Player p : b.getWorld().getPlayers()) {
							p.sendBlockChange(new Location(ar.w, X + x, Y + y, Z + z), Material.COAL_BLOCK.createBlockData());
							cls.add(BVec.of(X + x, Y + y, Z + z));
						}
					}
				} 
			} 
		}
		
		for (final Shooter sh : ar.shtrs.keySet()) {
			final LivingEntity le = sh.getEntity();
			if (sh.isDead() || le == null) continue;
			final Location loc = le.getLocation();
			final int dSq = NumUtil.square(loc.getBlockX() - X)
				+ NumUtil.square(loc.getBlockZ() - Z);
			final int idm = NumUtil.sqrt(dSq) * 200 / dSq; if (idm == 0) continue;
			final double d = idm * (ItemUtil.isBlank(sh.item(EquipmentSlot.CHEST), false) ? 1d : 0.4d);
			if (le.getHealth() - d <= 0) {
				ar.addDth(sh);
				sh.drop(le.getLocation());
				if (sh instanceof PlShooter) {
					final Player p = sh.getPlayer();
					p.closeInventory();
					p.setGameMode(GameMode.SPECTATOR);
				} else {
					((BtShooter) sh).own().hide(le);
				}
				for (final Player p : ar.w.getPlayers()) {
					p.sendMessage("§c\u926e\u9299 " + ar.getShtrNm(sh));
				}
			} else {
				le.setHealth(le.getHealth() - d);
				le.playHurtAnimation(le.getBodyYaw());
			}
		}
		
		Ostrov.async(() -> {
			final Map<Position, BlockData> bls = new HashMap<>();
			for (final BVec bl : cls) {
				bls.put(bl.center(ar.w), ar.w.getBlockData(bl.x, bl.y, bl.z));
			}
			
			for (final Player p : ar.w.getPlayers()) {
				p.sendMultiBlockChange(bls);
			}
		}, 200);
	}
}
