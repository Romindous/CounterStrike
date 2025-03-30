package me.Romindous.CounterStrike;

import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.*;
import java.util.Map.Entry;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.math.BlockPosition;
import me.Romindous.CounterStrike.Enums.GameType;
import me.Romindous.CounterStrike.Enums.GunType;
import me.Romindous.CounterStrike.Game.Arena;
import me.Romindous.CounterStrike.Game.Defusal;
import me.Romindous.CounterStrike.Game.Gungame;
import me.Romindous.CounterStrike.Game.Invasion;
import me.Romindous.CounterStrike.Listeners.*;
import me.Romindous.CounterStrike.Objects.Defusable;
import me.Romindous.CounterStrike.Objects.Game.Nade;
import me.Romindous.CounterStrike.Objects.Game.PlShooter;
import me.Romindous.CounterStrike.Objects.Map.MapBuilder;
import me.Romindous.CounterStrike.Objects.Map.Setup;
import me.Romindous.CounterStrike.Objects.Shooter;
import me.Romindous.CounterStrike.Utils.Inventories;
import me.Romindous.CounterStrike.Utils.Utils;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.World.Environment;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.BlockType;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Display;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.Vector3f;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Ostrov;
import ru.komiss77.Timer;
import ru.komiss77.enums.Game;
import ru.komiss77.enums.Stat;
import ru.komiss77.modules.games.GM;
import ru.komiss77.modules.items.ItemBuilder;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.world.BVec;
import ru.komiss77.modules.world.WorldManager;
import ru.komiss77.modules.world.WorldManager.Generator;
import ru.komiss77.utils.*;

import static org.bukkit.inventory.ItemType.LEATHER_CHESTPLATE;

public final class Main extends JavaPlugin implements Listener {

	public static Main plug;
	public static File folder;
	public static BVec lobby;
	public static ScoreboardManager smg;
	public static YamlConfiguration ars;
	public static final SecureRandom srnd = new SecureRandom();
	public static final LivingEntity[] emt = new LivingEntity[0];

	public static final ItemStack spy = Main.mkItm(ItemType.SPYGLASS, "§0О.О", Shooter.SCP_MDL);
	public static final ItemStack bmb = Main.mkItm(ItemType.GOLDEN_APPLE, "§4§lС*4 §c\u926e",
			Defusable.BOMB_MDL, "§dПКМ §7- Заложить бомбу", "§7Можно установить на точку §5A §7или §5B");
	public static final ItemStack tHelm = new ItemBuilder(ItemType.LEATHER_HELMET).name("§cШапка Террориста §f\u9267")
			.lore("§7Цена: §d" + Shooter.helmPrc + " §6⛃").color(Color.MAROON).build();
	public static final ItemStack tChest = new ItemBuilder(LEATHER_CHESTPLATE).color(Color.MAROON)
			.name("§cКуртка Террориста §f\u9266").lore("§7Цена: §d" + Shooter.chestPrc + " §6⛃").build();
	public static final ItemStack ctHelm = new ItemBuilder(ItemType.LEATHER_HELMET).name("§3Шлем Спецназа §f\u9267")
			.lore("§7Цена: §d" + Shooter.helmPrc + " §6⛃").color(Color.TEAL).build();
	public static final ItemStack ctChest = new ItemBuilder(LEATHER_CHESTPLATE).color(Color.TEAL)
			.name("§3Жилет Спецназа §f\u9266").lore("§7Цена: §d" + Shooter.chestPrc + " §6⛃").build();

	private static final HashMap<UUID, LivingEntity[]> wlents = new HashMap<>();

	public static final HashMap<String, Arena> actvarns = new HashMap<>();
	public static final HashMap<String, Setup> nnactvarns = new HashMap<>();
	public static final HashMap<String, PlShooter> shtrs = new HashMap<>();

	public static final Set<BVec> ndBlks = new HashSet<>();
	public static final ArrayList<BVec> cracks = new ArrayList<>();
	public static final HashMap<Player, BlockPosition> plnts = new HashMap<>();
	public static final HashMap<String, MapBuilder> mapBlds = new HashMap<>();
	public static final HashMap<UUID, Nade> nades = new HashMap<>();
	public static final ArrayList<BVec> decoys = new ArrayList<>();

	public void onEnable() {

		plug = this;
		folder = getDataFolder();
		smg = getServer().getScoreboardManager();

		loadConfigs();
		final PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new ChatLis(), this);
		pm.registerEvents(new DmgLis(), this);
		pm.registerEvents(new InterrLis(), this);
		pm.registerEvents(new MainLis(), this);
		pm.registerEvents(new InventLis(), this);

		getCommand("cs").setExecutor(new CSCmd());

		//Ostrov things

		//game stuff (5 tick)
		new BukkitRunnable() {
			public void run() {

				Main.cracks.removeIf(lc -> {
					final byte[] vals = lc.vals();
                    return vals.length == 0 || vals[0]-- == 0;
                });

				final Iterator<Entry<Player, BlockPosition>> pi = Main.plnts.entrySet().iterator();
				while (pi.hasNext()) {
					final Entry<Player, BlockPosition> e = pi.next();
					final Location loc = e.getKey().getLocation();
					if (e.getKey().getInventory().getHeldItemSlot() == 7 && loc.getBlockX() == e.getValue().blockX()
						&& loc.getBlockY() == e.getValue().blockY() && loc.getBlockZ() == e.getValue().blockZ()) {
						continue;
					}
					Utils.sendAcBr(e.getKey(), "§c§lВы вышли из режима установки");
					e.getKey().getInventory().setItem(7, bmb);
					pi.remove();
				}

				final Iterator<BVec> nbi = Main.ndBlks.iterator();
				while (nbi.hasNext()) {
					final BVec bl = nbi.next();
					final byte[] vals = bl.vals();
					final World w = bl.w();
					if (w == null) {nbi.remove(); continue;}
					if (vals.length == 0 || vals[0]-- == 0) {
						BlockUtil.set(bl.block(w), BlockType.AIR, false);
						nbi.remove();
					}
				}

				final Iterator<Nade> ni = Main.nades.values().iterator();
				while (ni.hasNext()) {
					final Nade nd = ni.next();
					if (nd.prj.isValid()) {
						if (nd.tm-- != 0) continue;
						nd.explode();
					}
					ni.remove();
				}
			}
		}.runTaskTimer(this, 5L, 5L);

		//shooting mechanics (1 tick)
		new BukkitRunnable() {
			public void run() {

				final int tick = (int) Timer.tickTime();
				final Iterator<BVec> li = Main.decoys.iterator();
				while (li.hasNext()) {
					final BVec loc = li.next();
					final World w = loc.w();
					if (w == null) {li.remove(); continue;}
					final byte[] vals = loc.vals();
					if (vals.length != 2) {li.remove(); continue;}
					w.spawnParticle(Particle.SOUL, loc.center(w),
						2, 0.2D, 0.2D, 0.2D, 0.0D, null, false);
					final GunType gt = GunType.values()[vals[1]];
					if ((tick & 31) > 15 && tick % gt.cld == 0) {
						if (vals[0]-- == 0) {li.remove(); continue;}
						Main.plyWrldSnd(loc.center(w), gt.snd, 1f);
					}
				}

				for (final Entry<UUID, LivingEntity[]> we : Main.wlents.entrySet()) {
					we.setValue(Bukkit.getWorld(we.getKey()).getLivingEntities().toArray(emt));
				}
			}
		}.runTaskTimer(this, 1L, 1L);
	}

	public void onDisable() {
		for (final PlShooter sh : shtrs.values()) {
			if (sh.arena() != null) sh.arena().rmvPl(sh);
		}
	}

	public static Location lobby() {
		final World w = lobby.w();
		return lobby.center(w == null ? Bukkit.getWorlds().getFirst() : w);
	}

	public void loadConfigs() {
		try {
			for (final PlShooter sh : shtrs.values()) {
				if (sh.arena() != null) sh.arena().rmvPl(sh);
			}

			File file = new File(getDataFolder() + File.separator + "config.yml");
			if (!file.exists() || !getConfig().contains("spawns")) {
				getServer().getConsoleSender().sendMessage("Config for CS not found, creating a new one...");
				getConfig().options().copyDefaults(true);
				getConfig().save(file);
			}
			//значения из конфига
			//tbl = getConfig().getString("setup.table");
			//арены
			file = new File(getDataFolder() + File.separator + "arenas.yml");
			file.createNewFile();
			ars = YamlConfiguration.loadConfiguration(file);

			nnactvarns.clear();
			if (!ars.contains("arenas")) {
				ars.createSection("arenas");
				ars.save(file);
			} else {
				for(final String s : ars.getConfigurationSection("arenas").getKeys(false)) {
					final Setup stp = new Setup(ars.getConfigurationSection("arenas." + s));
					if (stp.fin) {
						for (final String wn : stp.worlds.values()) {
							if (wn != null && !wn.isEmpty()) {
								final World w = WorldManager.load(getServer().getConsoleSender(), wn, Environment.NORMAL, Generator.Empty);
								if (w != null) {
									w.setGameRule(GameRule.RANDOM_TICK_SPEED, 0);
									w.setGameRule(GameRule.MOB_GRIEFING, false);
								}
							}
						}
						GM.sendArenaData(Game.CS, stp.nm, ru.komiss77.enums.GameState.ОЖИДАНИЕ, shtrs.size(), "§7Тип: §8Не Выбран",
							"§5=-=-=-=-=-=-", "§7Нужно: §d" + stp.min + " §7чел.", "");
						nnactvarns.put(stp.nm, stp);
					}
				}
			}

			final World w;
			if (ars.contains("lobby")) {
				final BVec lb = BVec.parse(ars.getString("lobby"));
				final World tw = lb.w();
				lobby = lb.w(w = tw == null ? Bukkit.getWorlds().getFirst() : tw);
			} else w = Bukkit.getWorlds().getFirst();

			wlents.clear();
			wlents.put(w.getUID(), emt);

			Inventories.fillLbbInv();
			Inventories.fillTsInv();
			Inventories.fillCTsInv();
		}
		catch (IOException | NullPointerException e) {
			e.printStackTrace();
		}
	}

	public static void lobbyPl(final Player p) {
		nrmlzPl(p, true);
		final PlShooter sh = Shooter.getPlShooter(p.getName(), true);
		sh.arena(null);
		sh.inv(p.getInventory());
		if (p.isInsideVehicle()) {
			p.getVehicle().remove();
		}
		p.teleport(getNrLoc(Main.lobby()));
		p.getInventory().setItem(2, new ItemBuilder(ItemType.CAMPFIRE).name("§dВыбор Игры").build());
		p.getInventory().setItem(5, new ItemBuilder(ItemType.TOTEM_OF_UNDYING).name("§eВыбор Обшивки").build());
		p.getInventory().setItem(6, Main.mkItm(ItemType.GHAST_TEAR, "§5Магазин", Shooter.SHOP_MDL));
		p.getInventory().setItem(8, new ItemBuilder(ItemType.MAGMA_CREAM).name("§4Выход в Лобби").build());
		p.getInventory().setHeldItemSlot(0);
		lobbyScore(p);
		final Component tpl = TCUtil.form("§7Сейчас в игре: §d" + MainLis.getPlaying() + "§7 человек!");
		for (final Player pl : Bukkit.getOnlinePlayers()) {
			pl.sendPlayerListFooter(tpl);
			if (p.getWorld().getUID().equals(pl.getWorld().getUID())) {
				pl.showPlayer(Main.plug, p);
				p.showPlayer(Main.plug, pl);
			} else {
				pl.hidePlayer(Main.plug, p);
				p.hidePlayer(Main.plug, pl);
			}
		}
		sh.taq("§7<§5ЛОББИ§7> ", " §7[-.-]", Arena.Team.SPEC.clr);
	}

	public static void lobbyScore(final Player p) {
		PM.getOplayer(p).score.getSideBar().reset().title("§7[§5CS:GO§7]")
			.add(" ")
			.add("§7Карта: §dЛОББИ")
			.add("§7=-=-=-=-=-=-")
			.add("§5Игр §7всего: " + ApiOstrov.getStat(p, Stat.CS_game))
			.add(" ")
			.add("§7Раундов:")
			.add(Arena.Team.CTs.clr + "Выйграно§7: " + ApiOstrov.getStat(p, Stat.CS_win))
			.add(Arena.Team.Ts.clr + "Проиграно§7: " + ApiOstrov.getStat(p, Stat.CS_loose))
			.add("§7=-=-=-=-=-=-")
			.add("§7(§dК§7/§dД§7): " + StringUtil.toSigFigs((double) ApiOstrov.getStat(p, Stat.CS_kill)
				/ (double) ApiOstrov.getStat(p, Stat.CS_death), (byte) 2))
			.add(" ")
			.add("§e   ostrov77.ru").build();
	}

	public Main getPlug() {
		return this;
	}

	public static void dmgInd(final Player p, final Location loc, final String nm) {
		final float lc = Math.max((float) p.getEyeLocation().distanceSquared(loc) * 0.006f, 0.6f);
		final TextDisplay tds = loc.getWorld().spawn(loc.clone().add(0.5f - srnd.nextFloat(),
			lc * 0.001f + srnd.nextFloat(), 0.5f - srnd.nextFloat()), TextDisplay.class, td -> {
			final Transformation tm = td.getTransformation();
			td.setTransformation(new Transformation(tm.getTranslation(),
				tm.getLeftRotation(), new Vector3f(lc, lc, lc), tm.getRightRotation()));
			td.setBackgroundColor(Color.fromARGB(0));
			td.setBillboard(Display.Billboard.VERTICAL);
			td.setVisibleByDefault(false);
			td.text(TCUtil.form(nm));
			td.setSeeThrough(true);
			td.setShadowed(true);
		});
		p.showEntity(plug, tds);
		Ostrov.sync(() -> tds.remove(), 24);
	}

	public static ItemStack mkItm(final ItemType mt, final String nm, final String mdl, final String... lr) {
		return new ItemBuilder(mt).name(nm).model(Key.key(mdl.toLowerCase())).lore(Arrays.asList(lr)).build();
	}

	public static void setDur(final ItemStack it, final int d) {
		final int max = maxDur(it);
		if (d == max) {
			it.unsetData(DataComponentTypes.DAMAGE);
			return;
		}
		it.setData(DataComponentTypes.DAMAGE, Math.max(0, max - d));
	}

	public static boolean hasDur(final ItemStack it) {
		final Integer dmg = it.getData(DataComponentTypes.DAMAGE);
		return dmg != null && dmg != 0;
	}

	public static int maxDur(final ItemStack it) {
		final Integer max = it.getData(DataComponentTypes.MAX_DAMAGE);
		return max == null ? it.getType().asItemType().getMaxDurability() : max;
	}

	public static String prf() {
		return "§7[§5CS§7] ";
	}

	public static void nrmlzPl(final Player p, final boolean clrinv) {
		p.setFireTicks(0);
		for (final PotionEffect ef : p.getActivePotionEffects()) {
			p.removePotionEffect(ef.getType());
		}
		p.getAttribute(Attribute.MAX_HEALTH).setBaseValue(20);
		p.setHealth(20);
		p.setExp(0);
		p.setLevel(0);
		p.setGameMode(GameMode.SURVIVAL);
		p.closeInventory();
		if (clrinv) {
			p.getInventory().clear();
		}
	}

	public Arena crtArena(final String nm, final GameType gt) {
		final Setup stp = nnactvarns.get(nm);
		final String wnm = stp.worlds.get(gt);
		final World w = wnm == null ? null : (getServer().getWorld(wnm) == null ? WorldManager.load(
			getServer().getConsoleSender(), wnm, Environment.NORMAL, Generator.Empty) : getServer().getWorld(wnm));
		if (w == null) return null;
		final Arena ar;
		if (stp.rnd) {
			final MapBuilder mb = new MapBuilder(stp, gt);

			mb.build(new Location(w, 0d, 11d, 0d));
			switch (gt) {
				case INVASION:
					ar = new Invasion(stp.nm, stp.min, stp.max, mb.getCTSpawns(), mb.getTSpawns(), mb.getSpots(), w, mb.getASite(), mb.getBSite(), true, stp.bots);
					actvarns.put(nm, ar);
					break;
				case GUNGAME:
					ar = new Gungame(stp.nm, stp.min, stp.max, mb.getCTSpawns(), mb.getTSpawns(), mb.getSpots(), w, true, stp.bots);
					actvarns.put(nm, ar);
					break;
				case DEFUSAL:
				default:
					ar = new Defusal(stp.nm, stp.min, stp.max, mb.getCTSpawns(), mb.getTSpawns(), mb.getSpots(), w, mb.getASite(), mb.getBSite(), (byte) 6, true, stp.bots);
					actvarns.put(nm, ar);
					break;
			}
			mapBlds.put(nm, mb);
		} else {
//			mar = new Area(nm, stp.bot, stp.top, w);
//			if (stp.bots) Ostrov.async(() -> mar.loadPos());
			switch (gt) {
				case INVASION:
					ar = new Invasion(stp.nm, stp.min, stp.max, stp.tSpawns, stp.ctSpawns, stp.spots, w, stp.A, stp.B, false, stp.bots);
					actvarns.put(nm, ar);
					break;
				case GUNGAME:
					ar = new Gungame(stp.nm, stp.min, stp.max, stp.tSpawns, stp.ctSpawns, stp.spots, w, false, stp.bots);
					actvarns.put(nm, ar);
					break;
				case DEFUSAL:
				default:
					ar = new Defusal(stp.nm, stp.min, stp.max, stp.tSpawns, stp.ctSpawns, stp.spots, w, stp.A, stp.B, (byte) 6, false, stp.bots);
					actvarns.put(nm, ar);
					break;
			}
		}
		return ar;
	}

	public static Location getNrLoc(final Location loc) {
		return loc.add(Main.srnd.nextBoolean() ? -1d : 1d, 0.1d, Main.srnd.nextBoolean() ? -1d : 1d);
	}

	public static Location getNrLoc(final World w, final BVec loc) {
		return new Location(w, (Main.srnd.nextBoolean() ? -1 : 1) + loc.x + 0.5d, loc.y + 0.1d, (Main.srnd.nextBoolean() ? -1 : 1) + loc.z + 0.5d);
	}

	public static Vector getNrVec(final BVec loc) {
		return new Vector((Main.srnd.nextBoolean() ? -1 : 1) + loc.x + 0.5d, loc.y + 0.1d, (Main.srnd.nextBoolean() ? -1 : 1) + loc.z + 0.5d);
	}

	public static BVec getNrPos(final BVec loc) {
		return loc.add(Main.srnd.nextBoolean() ? -1 : 1, 0, Main.srnd.nextBoolean() ? -1 : 1);
	}

	public static final int RESONATE = 2000;
	public static void plyWrldSnd(final Location org, final String snd, final float pt) {
		Ostrov.async(() -> {
			for (final Player p : org.getWorld().getPlayers()) {
				p.playSound(org, snd, (float) (RESONATE / ((int) p.getLocation().distanceSquared(org) + 1)), pt);
			}
		});
	}

	public static void plyWrldSnd(final LivingEntity src, final String snd, final float pt) {
		final Location org = src.getEyeLocation();
		final int id = src.getEntityId();
		Ostrov.async(() -> {
			for (final Player p : org.getWorld().getPlayers()) {
				if (p.getEntityId() == id) p.playSound(Sound.sound(Key.key(snd), Sound.Source.PLAYER, (float) RESONATE, pt), Sound.Emitter.self());
				else p.playSound(org, snd, (float) (RESONATE / ((int) p.getLocation().distanceSquared(org) + 1)), pt);
			}
		});
	}

	public static void plyWrldSnd(final LivingEntity src, final Arena ar, final Arena.Team tm, final String snd, final float pt) {
		final Location org = src.getEyeLocation();
		final int id = src.getEntityId();
		Ostrov.async(() -> {
			for (final Entry<Shooter, Arena.Team> en : ar.shtrs.entrySet()) {
				final Player p = en.getKey().getPlayer();
				if (p == null || en.getValue() != tm) continue;
				if (p.getEntityId() == id) p.playSound(Sound.sound(Key.key(snd), Sound.Source.PLAYER, (float) RESONATE, pt), Sound.Emitter.self());
				else p.playSound(src, snd, (float) (RESONATE / ((int) p.getLocation().distanceSquared(org) + 1)), pt);
			}
		});
	}

	public static String nrmlzStr(final String s) {
		final char[] ss = s.toLowerCase().toCharArray();
		ss[0] = Character.toUpperCase(ss[0]);
		for (byte i = (byte) (ss.length - 1); i > 0; i--) {
			switch (ss[i]) {
				case '_':
					ss[i] = ' ';
				case ' ':
					ss[i + 1] = Character.toUpperCase(ss[i + 1]);
					break;
				default:
					break;
			}
		}
		return String.valueOf(ss);
	}
	
	/*public static void shwHdPls(final Player p) {
		for (final Player pl : Bukkit.getOnlinePlayers()) {
			if (p.getWorld().getUID().equals(pl.getWorld().getUID())) {
				pl.showPlayer(Main.plug, p);
				p.showPlayer(Main.plug, pl);
			} else {
				pl.hidePlayer(Main.plug, p);
				p.hidePlayer(Main.plug, pl);
			}
		}
	}*/

	public static LivingEntity[] getLEs(final World w) {
		final LivingEntity[] arr = wlents.get(w.getUID());
		return arr == null ? emt : arr;
	}

	public static void addLEWorld(final World w) {
		wlents.put(w.getUID(), emt);
	}

	public static void delLEWorld(final World w) {
		wlents.remove(w.getUID());
	}

	public static int parseInt(final String n) {
		try {
			return Integer.parseInt(n);
		} catch (final NumberFormatException e) {
			return 0;
		}
	}

	public static int[] shuffle(final int[] ar) {
		int chs = ar.length >> 2;
		for (int i = ar.length - 1; i > chs; i--) {
			final int ni = srnd.nextInt(i);
			final int ne = ar[ni];
			ar[ni] = ar[i];
			ar[i] = ne;
			chs += ((chs-ni) >> 31) + 1;
		}
		return ar;
	}
}