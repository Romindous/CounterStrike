package me.Romindous.CounterStrike.Objects;

import java.util.Set;
import java.util.function.Predicate;
import me.Romindous.CounterStrike.Enums.GunType;
import me.Romindous.CounterStrike.Game.Arena;
import me.Romindous.CounterStrike.Main;
import me.Romindous.CounterStrike.Objects.Game.BtShooter;
import me.Romindous.CounterStrike.Objects.Game.PlShooter;
import me.Romindous.CounterStrike.Objects.Skins.GunSkin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.BlockType;
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

	double TRC_STEP = 0.05d;
	double TRC_FCT = 1d / TRC_STEP;

	String KNIFE = "knife", CTs = "cts", Ts = "ts", ROPE_MDL = "rope", KNIFE_MDL = KNIFE + "/" + CTs + "/classic", SHOP_MDL = "shop";

	Set<BlockType> BREAKABLE = Set.of(BlockType.OAK_LEAVES, BlockType.ACACIA_LEAVES, BlockType.BIRCH_LEAVES, BlockType.JUNGLE_LEAVES, BlockType.CHERRY_LEAVES,
		BlockType.SPRUCE_LEAVES, BlockType.DARK_OAK_LEAVES, BlockType.MANGROVE_LEAVES, BlockType.AZALEA_LEAVES, BlockType.FLOWERING_AZALEA_LEAVES,
		BlockType.GLASS, BlockType.WHITE_STAINED_GLASS, BlockType.GLASS_PANE, BlockType.WHITE_STAINED_GLASS_PANE, BlockType.FLOWER_POT, BlockType.DECORATED_POT,
		BlockType.DIAMOND_ORE, BlockType.COAL_ORE, BlockType.IRON_ORE, BlockType.EMERALD_ORE);
	Set<BlockType> PASSABLE = Set.of(BlockType.AIR, BlockType.CAVE_AIR, BlockType.VOID_AIR,
		BlockType.SEAGRASS, BlockType.TALL_SEAGRASS, BlockType.WEEPING_VINES, BlockType.TWISTING_VINES,

		BlockType.BLACK_CARPET, BlockType.BLUE_CARPET, BlockType.BROWN_CARPET, BlockType.CYAN_CARPET, BlockType.GRAY_CARPET,
		BlockType.GREEN_CARPET, BlockType.LIGHT_BLUE_CARPET, BlockType.LIGHT_GRAY_CARPET, BlockType.LIME_CARPET,
		BlockType.MAGENTA_CARPET, BlockType.MOSS_CARPET, BlockType.ORANGE_CARPET, BlockType.PINK_CARPET,
		BlockType.PURPLE_CARPET, BlockType.RED_CARPET, BlockType.WHITE_CARPET, BlockType.YELLOW_CARPET,

		BlockType.WATER, BlockType.IRON_BARS, BlockType.CHAIN, BlockType.STRUCTURE_VOID, BlockType.COBWEB, BlockType.SNOW,
		BlockType.BARRIER, BlockType.TRIPWIRE, BlockType.LADDER, BlockType.RAIL, BlockType.POWERED_RAIL,
		BlockType.DETECTOR_RAIL, BlockType.ACTIVATOR_RAIL, BlockType.CAMPFIRE, BlockType.SOUL_CAMPFIRE);
	Set<BlockType> BANGABLE = Set.of(BlockType.ACACIA_SLAB, BlockType.BIRCH_SLAB, BlockType.CRIMSON_SLAB, BlockType.SPRUCE_SLAB, BlockType.WARPED_SLAB, BlockType.CHERRY_SLAB, BlockType.BAMBOO_SLAB,
		BlockType.DARK_OAK_SLAB, BlockType.OAK_SLAB, BlockType.JUNGLE_SLAB, BlockType.PETRIFIED_OAK_SLAB, BlockType.MANGROVE_SLAB, BlockType.BAMBOO_MOSAIC_SLAB, BlockType.PALE_OAK_SLAB,

		BlockType.ACACIA_STAIRS, BlockType.BIRCH_STAIRS, BlockType.CRIMSON_STAIRS, BlockType.SPRUCE_STAIRS, BlockType.CHERRY_STAIRS, BlockType.BAMBOO_STAIRS, BlockType.PALE_OAK_STAIRS,
		BlockType.WARPED_STAIRS, BlockType.DARK_OAK_STAIRS, BlockType.OAK_STAIRS, BlockType.JUNGLE_STAIRS, BlockType.MANGROVE_STAIRS, BlockType.BAMBOO_MOSAIC_STAIRS,

		BlockType.ACACIA_PLANKS, BlockType.BIRCH_PLANKS, BlockType.CRIMSON_PLANKS, BlockType.SPRUCE_PLANKS, BlockType.CHERRY_PLANKS, BlockType.BAMBOO_PLANKS, BlockType.PALE_OAK_PLANKS,
		BlockType.WARPED_PLANKS, BlockType.DARK_OAK_PLANKS, BlockType.OAK_PLANKS, BlockType.JUNGLE_PLANKS, BlockType.MANGROVE_PLANKS, BlockType.BAMBOO_MOSAIC,

		BlockType.ACACIA_TRAPDOOR, BlockType.BIRCH_TRAPDOOR, BlockType.CRIMSON_TRAPDOOR, BlockType.DARK_OAK_TRAPDOOR, BlockType.CHERRY_TRAPDOOR, BlockType.BAMBOO_TRAPDOOR,
		BlockType.JUNGLE_TRAPDOOR, BlockType.MANGROVE_TRAPDOOR, BlockType.OAK_TRAPDOOR, BlockType.SPRUCE_TRAPDOOR, BlockType.WARPED_TRAPDOOR, BlockType.PALE_OAK_TRAPDOOR,

		BlockType.ACACIA_WOOD, BlockType.BIRCH_WOOD, BlockType.CRIMSON_HYPHAE, BlockType.SPRUCE_WOOD, BlockType.CHERRY_WOOD, BlockType.BAMBOO_BLOCK,
		BlockType.WARPED_HYPHAE, BlockType.DARK_OAK_WOOD, BlockType.OAK_WOOD, BlockType.JUNGLE_WOOD, BlockType.MANGROVE_WOOD, BlockType.PALE_OAK_WOOD,

		BlockType.ACACIA_LOG, BlockType.BIRCH_LOG, BlockType.CRIMSON_STEM, BlockType.SPRUCE_LOG, BlockType.CHERRY_LOG, BlockType.BAMBOO,
		BlockType.WARPED_STEM, BlockType.DARK_OAK_LOG, BlockType.OAK_LOG, BlockType.JUNGLE_LOG, BlockType.MANGROVE_LOG, BlockType.PALE_OAK_LOG,

		BlockType.ACACIA_SIGN, BlockType.ACACIA_WALL_SIGN, BlockType.BIRCH_SIGN, BlockType.BIRCH_WALL_SIGN, BlockType.CRIMSON_SIGN, BlockType.BAMBOO_SIGN,
		BlockType.CRIMSON_WALL_SIGN, BlockType.SPRUCE_SIGN, BlockType.SPRUCE_WALL_SIGN, BlockType.WARPED_SIGN, BlockType.PALE_OAK_SIGN, BlockType.WARPED_WALL_SIGN,
		BlockType.DARK_OAK_SIGN, BlockType.DARK_OAK_WALL_SIGN, BlockType.OAK_SIGN, BlockType.CHERRY_SIGN, BlockType.CHERRY_WALL_SIGN, BlockType.BAMBOO_WALL_SIGN,
		BlockType.OAK_WALL_SIGN, BlockType.JUNGLE_SIGN, BlockType.JUNGLE_WALL_SIGN, BlockType.MANGROVE_SIGN, BlockType.MANGROVE_WALL_SIGN, BlockType.PALE_OAK_WALL_SIGN,

		BlockType.STRIPPED_ACACIA_WOOD, BlockType.STRIPPED_BIRCH_WOOD, BlockType.STRIPPED_CRIMSON_HYPHAE, BlockType.STRIPPED_SPRUCE_WOOD,
		BlockType.STRIPPED_WARPED_HYPHAE, BlockType.STRIPPED_DARK_OAK_WOOD, BlockType.STRIPPED_OAK_WOOD, BlockType.STRIPPED_JUNGLE_WOOD,
		BlockType.STRIPPED_MANGROVE_WOOD, BlockType.STRIPPED_CHERRY_WOOD, BlockType.STRIPPED_BAMBOO_BLOCK, BlockType.STRIPPED_PALE_OAK_WOOD,

		BlockType.STRIPPED_ACACIA_LOG, BlockType.STRIPPED_BIRCH_LOG, BlockType.STRIPPED_CRIMSON_STEM, BlockType.STRIPPED_SPRUCE_LOG,
		BlockType.STRIPPED_WARPED_STEM, BlockType.STRIPPED_DARK_OAK_LOG, BlockType.STRIPPED_OAK_LOG, BlockType.STRIPPED_JUNGLE_LOG,
		BlockType.STRIPPED_MANGROVE_LOG, BlockType.STRIPPED_CHERRY_LOG, BlockType.STRIPPED_PALE_OAK_LOG,

		BlockType.ACACIA_FENCE, BlockType.BIRCH_FENCE, BlockType.CRIMSON_FENCE, BlockType.SPRUCE_FENCE, BlockType.WARPED_FENCE, BlockType.DARK_OAK_FENCE,
		BlockType.OAK_FENCE, BlockType.JUNGLE_FENCE, BlockType.MANGROVE_FENCE, BlockType.CHERRY_FENCE, BlockType.BAMBOO_FENCE, BlockType.PALE_OAK_FENCE,

		BlockType.ACACIA_FENCE_GATE, BlockType.BIRCH_FENCE_GATE, BlockType.CRIMSON_FENCE_GATE, BlockType.CHERRY_FENCE_GATE, BlockType.BAMBOO_FENCE_GATE, BlockType.PALE_OAK_FENCE_GATE,
		BlockType.SPRUCE_FENCE_GATE, BlockType.WARPED_FENCE_GATE, BlockType.DARK_OAK_FENCE_GATE, BlockType.OAK_FENCE_GATE, BlockType.JUNGLE_FENCE_GATE, BlockType.MANGROVE_FENCE_GATE,

		BlockType.OAK_DOOR, BlockType.ACACIA_DOOR, BlockType.BIRCH_DOOR, BlockType.CRIMSON_DOOR, BlockType.DARK_OAK_DOOR, BlockType.CHERRY_DOOR,
		BlockType.JUNGLE_DOOR, BlockType.MANGROVE_DOOR, BlockType.WARPED_DOOR, BlockType.SPRUCE_DOOR, BlockType.BAMBOO_DOOR, BlockType.PALE_OAK_DOOR,

		BlockType.OAK_HANGING_SIGN, BlockType.ACACIA_HANGING_SIGN, BlockType.BIRCH_HANGING_SIGN, BlockType.CRIMSON_HANGING_SIGN, BlockType.DARK_OAK_HANGING_SIGN, BlockType.CHERRY_HANGING_SIGN,
		BlockType.JUNGLE_HANGING_SIGN, BlockType.MANGROVE_HANGING_SIGN, BlockType.WARPED_HANGING_SIGN, BlockType.SPRUCE_HANGING_SIGN, BlockType.BAMBOO_HANGING_SIGN, BlockType.PALE_OAK_HANGING_SIGN,

		BlockType.OAK_WALL_HANGING_SIGN, BlockType.ACACIA_WALL_HANGING_SIGN, BlockType.BIRCH_WALL_HANGING_SIGN, BlockType.CRIMSON_WALL_HANGING_SIGN,
		BlockType.DARK_OAK_WALL_HANGING_SIGN, BlockType.CHERRY_WALL_HANGING_SIGN, BlockType.JUNGLE_WALL_HANGING_SIGN, BlockType.MANGROVE_WALL_HANGING_SIGN,
		BlockType.WARPED_WALL_HANGING_SIGN, BlockType.SPRUCE_WALL_HANGING_SIGN, BlockType.BAMBOO_WALL_HANGING_SIGN, BlockType.PALE_OAK_WALL_HANGING_SIGN,

		BlockType.BARREL, BlockType.BEEHIVE, BlockType.BEE_NEST, BlockType.NOTE_BLOCK, BlockType.JUKEBOX, BlockType.CRAFTING_TABLE);

	String name();
	
	void rotPss();
	Vector getLoc();
	Vector getLoc(final int dst);
	WXYZ getPos();
	
	Player getPlayer();
	LivingEntity getEntity();
	
	int shtTm();
	void shtTm(final int n);
	
	int rclTm();
	void rclTm(final int n);
	
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
