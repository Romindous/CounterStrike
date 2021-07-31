package me.Romindous.CounterStrike.Objects;

import java.util.HashSet;
import org.bukkit.EntityEffect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.mojang.datafixers.util.Pair;

import me.Romindous.CounterStrike.Main;
import me.Romindous.CounterStrike.Game.Arena;
import me.Romindous.CounterStrike.Game.Arena.Team;
import me.Romindous.CounterStrike.Game.Defusal;
import me.Romindous.CounterStrike.Listeners.DmgLis;
import me.Romindous.CounterStrike.Utils.PacketUtils;
import net.minecraft.core.BaseBlockPosition;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.decoration.EntityArmorStand;

public class Bomb extends BaseBlockPosition {

	public final World w;
	public final EntityArmorStand ttl;
	public final EntityArmorStand sbttl;
	
	public Bomb(final Block b) {
		super(b.getX(), b.getY(), b.getZ());
		w = b.getWorld();
		final net.minecraft.world.level.World wn = PacketUtils.getNMSWrld(w);
		ttl = new EntityArmorStand(EntityTypes.c, wn);
		ttl.setNoGravity(true);
		ttl.setInvisible(true);
		ttl.setInvulnerable(true);
		ttl.setMarker(true);
		ttl.setCustomName(IChatBaseComponent.a("§l§4Бомба Поставлена!"));
		ttl.setCustomNameVisible(true);
		ttl.setPosition(getX() + 0.5d, getY() + 1d, getZ() + 0.5d);
		wn.addEntity(ttl);
		
		sbttl = new EntityArmorStand(EntityTypes.c, wn);
		sbttl.setNoGravity(true);
		sbttl.setInvisible(true);
		sbttl.setInvulnerable(true);
		sbttl.setMarker(true);
		sbttl.setCustomName(IChatBaseComponent.a("§7Обезвредьте §eкусачками §7или §3спец. набором§7!"));
		sbttl.setCustomNameVisible(true);
		sbttl.setPosition(getX() + 0.5d, getY() + 0.5d, getZ() + 0.5d);
		wn.addEntity(sbttl);
	}
	   
	public void expld(final Defusal ar) {
		kllAs();
		final Block b = w.getBlockAt(getX(), getY(), getZ());
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
		for (final Shooter sh : ar.shtrs.keySet()) {
			final HumanEntity pl = sh.inv.getHolder();
			final Location loc = pl.getLocation();
			final int dx = 50 - loc.getBlockX() + X > 0 ? 50 - loc.getBlockX() + X : 0;
			final int dz = 50 - loc.getBlockZ() + Z > 0 ? 50 - loc.getBlockZ() + Z : 0;
			final double d = (dx * dx + dz * dz) * 0.01d;
			if (pl.getHealth() - d <= 0) {
				ar.addDth(sh);
				final Team tm = ar.shtrs.get(sh);
				ar.dropIts(sh.inv, loc, tm);
				pl.setGameMode(GameMode.SPECTATOR);
			} else {
				pl.setHealth(pl.getHealth() - d);
				pl.playEffect(EntityEffect.HURT_EXPLOSION);
			}
			DmgLis.prcDmg(sh.inv.getHolder(), new Pair<Shooter, Arena>(sh, ar), d, sh.inv.getHolder().getHealth() - d <= 0d ? 
				"§c\u926e\u9299 " + ar.getShtrNm(sh.nm) 
				: null, (byte) 2, (short) 0);
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

	public void kllAs() {
		ttl.killEntity();
		sbttl.killEntity();
	}

	public Block getBlock() {
		return w.getBlockAt(getX(), getY(), getZ());
	}
}
