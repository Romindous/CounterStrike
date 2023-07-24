package me.Romindous.CounterStrike.Objects.Game;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.bukkit.EntityEffect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Display.Billboard;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.util.Transformation;
import org.joml.Vector3f;

import io.papermc.paper.math.Position;
import me.Romindous.CounterStrike.Main;
import me.Romindous.CounterStrike.Game.Defusal;
import me.Romindous.CounterStrike.Objects.Shooter;
import net.kyori.adventure.text.Component;
import ru.komiss77.Ostrov;
import ru.komiss77.modules.world.WXYZ;
import ru.komiss77.modules.world.XYZ;
import ru.komiss77.utils.ItemUtils;
import ru.komiss77.utils.TCUtils;
import ru.komiss77.version.IServer;
import ru.komiss77.version.VM;

public class Bomb extends WXYZ {
	
	public final TextDisplay title;
	
	private static final Component bnm = TCUtils.format("§l§кБiмба Поставлена!")
		.appendNewline().append(TCUtils.format("§7Обезвредьте §eкусачками §7или §3спец. набором§7!"));
	
	public Bomb(final Block b) {
		super(b.getWorld(), b.getX(), b.getY(), b.getZ());
		title = w.spawn(getCenterLoc().add(0d, 1d, 0d), TextDisplay.class);
		title.setPersistent(true);
		title.setBillboard(Billboard.VERTICAL);
		title.text(bnm);
		title.setShadowed(true);
		title.setSeeThrough(true);
		title.setViewRange(100f);
		final Transformation atr = title.getTransformation();
		title.setTransformation(new Transformation(atr.getTranslation(), 
			atr.getLeftRotation(), new Vector3f(1.6f, 1.6f, 1.6f), atr.getRightRotation()));
	}
	   
	public void expld(final Defusal ar) {
		title.remove();
		final Block b = w.getBlockAt(x, y, z);
		b.setType(Material.AIR);
		final int X = b.getX();
		final int Y = b.getY();
		final int Z = b.getZ();
		final HashSet<XYZ> cls = new HashSet<>();
		b.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, b.getLocation(), 20, 5d, 5d, 5d);
		b.getWorld().playSound(b.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 5f, 0.8f);
		
		final IServer is = VM.getNmsServer();
		for (int x = -5; x < 6; x++) {
			for (int y = -5; y < 6; y++) {
				for (int z = -5; z < 6; z++) {
					final int bnd = x*x + y*y + z*z;
					if (bnd > 0 && is.getFastMat(w, X + x, Y + y, Z + z).isAir() && is.getFastMat(w, X + x, Y + y - 1, Z + z).isOccluding() && Main.srnd.nextInt(bnd) < 6) {
						for (final Player p : b.getWorld().getPlayers()) {
							p.sendBlockChange(new Location(w, X + x, Y + y, Z + z), Material.FIRE.createBlockData());
							cls.add(new XYZ("", x, y, z));
						} 
					} else if (is.getFastMat(w, X + x, Y + y, Z + z).isOccluding() && Main.srnd.nextInt(bnd) < 10) {
						for (final Player p : b.getWorld().getPlayers()) {
							p.sendBlockChange(new Location(w, X + x, Y + y, Z + z), Material.COAL_BLOCK.createBlockData());
							cls.add(new XYZ("", x, y, z));
						} 
					}
				} 
			} 
		}
		
		for (final Shooter sh : ar.shtrs.keySet()) {
			final LivingEntity le = sh.getEntity();
			if (sh.isDead() || le == null) continue;
			final Location loc = le.getLocation();
			final int dx = loc.getBlockX() - X;
			final int dz = loc.getBlockZ() - Z;
			final double d = Math.max(200 - (dx * dx + dz * dz), 0) * 0.4d * 
			(ItemUtils.isBlank(sh.item(EquipmentSlot.CHEST), false) ? 1d : 0.4d);
			if (le.getHealth() - d <= 0) {
				ar.addDth(sh);
				sh.dropIts(le.getLocation());
				if (sh instanceof PlShooter) {
					final Player p = sh.getPlayer();
					p.closeInventory();
					p.setGameMode(GameMode.SPECTATOR);
				} else {
					((BtShooter) sh).die(le);
				}
				for (final Player p : w.getPlayers()) {
					p.sendMessage("§c\u926e\u9299 " + ar.getShtrNm(sh));
				}
			} else {
				le.setHealth(le.getHealth() - d);
				le.playEffect(EntityEffect.HURT_EXPLOSION);
			}
		}
		
		Ostrov.async(() -> {
			final Map<Position, BlockData> bls = new HashMap<>();
			for (final XYZ bl : cls) {
				bls.put(bl.getCenterLoc(w), w.getBlockData(X + bl.x, Y + bl.y, Z + bl.z));
			}
			
			for (final Player p : w.getPlayers()) {
				p.sendMultiBlockChange(bls);
			}
		}, 200);
	}
}
