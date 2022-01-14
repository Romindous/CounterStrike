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
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.scheduler.BukkitRunnable;

import me.Romindous.CounterStrike.Main;
import me.Romindous.CounterStrike.Game.Arena.Team;
import me.Romindous.CounterStrike.Game.Defusal;
import me.Romindous.CounterStrike.Utils.PacketUtils;
import net.minecraft.core.BaseBlockPosition;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.decoration.EntityArmorStand;

public class Bomb extends BaseBlockPosition {

	public final World w;
	public final EntityArmorStand ttl;
	public final EntityArmorStand sbttl;
	
	public Bomb(final Block b) {
		super(b.getX(), b.getY(), b.getZ());
		w = b.getWorld();
		final net.minecraft.world.level.World wn = PacketUtils.getNMSWrld(w.getName());
		ttl = new EntityArmorStand(EntityTypes.c, wn);
		ttl.e(true);
		ttl.j(true);
		ttl.m(true);
		ttl.t(true);
		ttl.a(IChatBaseComponent.a("§l§4Бомба Поставлена!"));
		ttl.n(true);
		ttl.setPosRaw(u() + 0.5d, v() + 1d, w() + 0.5d, false);
		wn.addFreshEntity(ttl, SpawnReason.CUSTOM);
		
		sbttl = new EntityArmorStand(EntityTypes.c, wn);
		sbttl.e(true);
		sbttl.j(true);
		sbttl.m(true);
		sbttl.t(true);
		sbttl.a(IChatBaseComponent.a("§7Обезвредьте §eкусачками §7или §3спец. набором§7!"));
		sbttl.n(true);
		sbttl.setPosRaw(u() + 0.5d, v() + 0.5d, w() + 0.5d, false);
		wn.addFreshEntity(sbttl, SpawnReason.CUSTOM);
	}
	   
	public void expld(final Defusal ar) {
		kllAs();
		final Block b = w.getBlockAt(u(), v(), w());
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
			final int dx = loc.getBlockX() - X;
			final int dz = loc.getBlockZ() - Z;
			final double d = Math.max(200 - (dx * dx + dz * dz), 0) * 0.4d * (sh.inv.getChestplate() == null ? 1d : 0.4d);
			if (pl.getGameMode() == GameMode.SURVIVAL) {
				if (pl.getHealth() - d <= 0) {
					ar.addDth(sh);
					final Team tm = ar.shtrs.get(sh);
					ar.dropIts(sh.inv, loc, tm);
					pl.setGameMode(GameMode.SPECTATOR);
					for (final Player p : w.getPlayers()) {
						p.sendMessage("§c\u926e\u9299 " + ar.getShtrNm(sh.nm));
					}
				} else {
					pl.setHealth(pl.getHealth() - d);
					pl.playEffect(EntityEffect.HURT_EXPLOSION);
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

	public void kllAs() {
		ttl.a(RemovalReason.a);
		sbttl.a(RemovalReason.a);
	}

	public Block getBlock() {
		return w.getBlockAt(u(), v(), w());
	}

	public Location getLoc() {
		return new Location(w, u(), v(), w());
	}
}
