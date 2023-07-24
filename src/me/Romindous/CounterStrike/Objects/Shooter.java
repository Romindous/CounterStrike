package me.Romindous.CounterStrike.Objects;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import me.Romindous.CounterStrike.Main;
import me.Romindous.CounterStrike.Enums.GunType;
import me.Romindous.CounterStrike.Game.Arena;
import me.Romindous.CounterStrike.Objects.Game.PlShooter;
import me.Romindous.CounterStrike.Objects.Skins.GunSkin;
import ru.komiss77.modules.bots.BotEntity;
import ru.komiss77.modules.bots.BotManager;
import ru.komiss77.modules.world.WXYZ;
import ru.komiss77.notes.Slow;

public interface Shooter {

	public String name();
	
	public void rotPss();
	public Vector getLoc(final boolean dir);
	public WXYZ getPos();
	
	public Player getPlayer();
	public LivingEntity getEntity();
	
	public int shtTm();
	public int shtTm(final int n);
	
	public int rclTm();
	public int rclTm(final int n);
	
	public int cldwn();
	public int cldwn(final int n);
	
	public int kills();
	public void killsI();
	public void kills0();
	
	public int deaths();
	public void deathsI();
	public void deaths0();
	
	public int spwnrs();
	public void spwnrsI();
	public void spwnrs0();
	
	public int money();
	public int money(final int n);
	
	public int count();
	public int count(final int n);
	
	public Arena arena();
	public void arena(final Arena ar);

	public ItemStack item(final EquipmentSlot slot);
	public ItemStack item(final int slot);
	public void item(final ItemStack it, final EquipmentSlot slot);
	public void item(final ItemStack it, final int slot);
	public Inventory inv();
	public void clearInv();
	public void dropIts(final Location loc);

	public int getModel(final GunType gt);
	public GunSkin getSkin(final GunType gt);
	public boolean hasModel(final GunType gt, final int mdl);
	public void giveModel(final GunType gt, final int cmd);
	public void setModel(final GunType gt, final int cmd);
	
	@Override
	int hashCode();
	
	@Override
	boolean equals(final Object o);
	
	@Slow(priority = 4)
	public void shoot(final GunType gt, final boolean dff, final int tr);
	
	public static PlShooter getPlShooter(final String nm, final boolean crt) {
		final PlShooter sh = Main.shtrs.get(nm);
		if (sh == null && crt) {
			final Player p = Bukkit.getPlayer(nm);
			if (p == null) return null;
			final PlShooter nvs = new PlShooter(p);
			Main.shtrs.put(nm, nvs);
			return nvs;
		}
		return sh;
	}
	
	public static Shooter getShooter(final LivingEntity le, final boolean crt) {
		if (le.getType() == EntityType.PLAYER) {
			return getPlShooter(le.getName(), crt);
		} else {
			final BotEntity bh = BotManager.rIdBots.get(le.getEntityId());
			return bh != null && bh instanceof Shooter ? (Shooter) bh : null;
		}
	}

	public void teleport(final LivingEntity le, final Location to);
	public boolean isDead();
}
