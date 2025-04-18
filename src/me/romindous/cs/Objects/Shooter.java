package me.romindous.cs.Objects;

import java.util.function.Predicate;
import me.romindous.cs.Enums.GunType;
import me.romindous.cs.Game.Arena;
import me.romindous.cs.Main;
import me.romindous.cs.Objects.Game.BtShooter;
import me.romindous.cs.Objects.Game.PlShooter;
import me.romindous.cs.Objects.Loc.Info;
import me.romindous.cs.Objects.Skins.GunSkin;
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
import ru.komiss77.modules.world.BVec;
import ru.komiss77.notes.Slow;
import ru.komiss77.objects.IntHashMap;

public interface Shooter {

	short helmPrc = 150;
	short chestPrc = 250;
	short wirePrc = 150;
	short kitPrc = 200;
	short knifRwd = 200;
	byte helmSlt = 40;
	byte chestSlt = 49;
	byte wireSlt = 29;
	byte kitSlt = 33;

	int MAX_DST = 6;
	int MAX_RCL = 50;

	double TRC_STEP = 0.05d;
	double TRC_FCT = 1d / TRC_STEP;

	String KNIFE = "knife", CTs = "cts", Ts = "ts", ROPE_MDL = "rope", KNIFE_MDL = KNIFE + "/" + CTs + "/classic", SHOP_MDL = "shop", SCP_MDL = "scope";

	String name();
	
	void rotPss();
	Vector getLoc();
	Vector getLoc(final int dst);
	BVec getPos();
	
	Player getPlayer();
	LivingEntity getEntity();
	
	int shtTm();
	void shtTm(final int n);
	
	int recoil();
	void rstRcl();
	
	int cldwn();
	void cldwn(final int n);
	
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
	void money(final int n);
	
	int count();
	void count(final int n);
	
	Arena arena();
	void arena(final Arena ar);

	ItemStack item(final EquipmentSlot slot);
	ItemStack item(final int slot);
	void item(final EquipmentSlot slot, final ItemStack it);
	void item(final int slot, final ItemStack it);
	Inventory inv();
	void clearInv();
	void drop(final Location loc);

	String model(final GunType gt);
	GunSkin skin(final GunType gt);
	boolean has(final GunType gt, final String mdl);
	void give(final GunType gt, final String mdl);
	void choose(final GunType gt, final String mdl);

	void taq(final String pfx, final String sfx, final String afx);

	Predicate<Player> allyTest();
	
	@Override
	int hashCode();
	
	@Override
	boolean equals(final Object o);
	
	@Slow(priority = 3)
    void shoot(final GunType gt, final boolean dff, final int tr, final IntHashMap<Info> blocks);
	
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
