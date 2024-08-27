package me.Romindous.CounterStrike.Objects;

import java.util.function.Predicate;
import me.Romindous.CounterStrike.Enums.GunType;
import me.Romindous.CounterStrike.Game.Arena;
import me.Romindous.CounterStrike.Main;
import me.Romindous.CounterStrike.Objects.Game.BtShooter;
import me.Romindous.CounterStrike.Objects.Game.PlShooter;
import me.Romindous.CounterStrike.Objects.Skins.GunSkin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import ru.komiss77.modules.bots.BotManager;
import ru.komiss77.modules.bots.Botter;
import ru.komiss77.modules.world.WXYZ;
import ru.komiss77.notes.Slow;

public interface Shooter {

	int MAX_DST = 6;
	double TRC_STEP = 0.05d;
	double TRC_FCT = 1d / TRC_STEP;

	String name();
	
	void rotPss();
	Vector getLoc();
	Vector getLoc(final int dst);
	WXYZ getPos();
	
	Player getPlayer();
	LivingEntity getEntity();
	
	int shtTm();
	int shtTm(final int n);
	
	int rclTm();
	int rclTm(final int n);
	
	int cldwn();
	int cldwn(final int n);
	
	int kills();
	void killsI();
	void kills0();
	
	int deaths();
	void deathsI();
	void deaths0();
	
	int spwnrs();
	void spwnrsI();
	void spwnrs0();
	
	int money();
	int money(final int n);
	
	int count();
	int count(final int n);
	
	Arena arena();
	void arena(final Arena ar);

	ItemStack item(final EquipmentSlot slot);
	ItemStack item(final int slot);
	void item(final EquipmentSlot slot, final ItemStack it);
	void item(final int slot, final ItemStack it);
	Inventory inv();
	void clearInv();
	void drop(final Location loc);

	int getModel(final GunType gt);
	GunSkin getSkin(final GunType gt);
	boolean hasModel(final GunType gt, final int mdl);
	void giveModel(final GunType gt, final int cmd);
	void setModel(final GunType gt, final int cmd);

	void taq(final String pfx, final String sfx, final String afx);

	Predicate<Player> allyTest();
	
	@Override
	int hashCode();
	
	@Override
	boolean equals(final Object o);
	
	@Slow(priority = 3)
    void shoot(final GunType gt, final boolean dff, final int tr);
	
	static PlShooter getPlShooter(final String nm, final boolean crt) {
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
	
	static Shooter getShooter(final LivingEntity le, final boolean crt) {
		if (le.getType() == EntityType.PLAYER) {
			return getPlShooter(le.getName(), crt);
		} else {
			final Botter bh = BotManager.getBot(le.getEntityId());
            return bh == null ? null : bh.extent(BtShooter.class);
		}
	}

	void teleport(final LivingEntity le, final Location to);
	boolean isDead();
}
