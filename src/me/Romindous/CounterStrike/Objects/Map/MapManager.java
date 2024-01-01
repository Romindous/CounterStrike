package me.Romindous.CounterStrike.Objects.Map;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;

import me.Romindous.CounterStrike.Main;
import me.Romindous.CounterStrike.Objects.Game.GameType;
import ru.komiss77.modules.world.XYZ;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InputButton;
import ru.komiss77.utils.inventory.InputButton.InputType;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;

public class MapManager implements InventoryProvider {
	
	public static final HashMap<UUID, MapManager> edits = new HashMap<>();
	
	public Setup stp;
	
	public MapManager(final String name) {
		final Setup ar = Main.nnactvarns.get(name);
		stp = ar == null ? new Setup(name) : 
		new Setup(ar.nm, ar.min, ar.max, ar.rndM, ar.fin, ar.bots, 
		ar.tSpawns, ar.ctSpawns, ar.spots, ar.A, ar.B, ar.worlds);
	}
	
	@Override
	public void init(final Player pl, final InventoryContent its) {
		edits.put(pl.getUniqueId(), this);
		
		its.set(4, new InputButton(InputType.ANVILL, //имя
			new ItemBuilder(Material.GLOBE_BANNER_PATTERN).name("§5" + stp.nm).addLore(Arrays.asList("§dКлик §7- изменить имя")).build(), "Карта", nm -> {
			stp = new Setup(nm, stp.min, stp.max, stp.rndM, stp.fin, stp.bots, 
				stp.tSpawns, stp.ctSpawns, stp.spots, stp.A, stp.B, stp.worlds);
			reopen(pl, its);
		}));
		
		its.set(7, ClickableItem.of(new ItemBuilder(Material.RABBIT_HIDE).name("§7Минимум Игроков")
			.setAmount(stp.min).addLore(Arrays.asList("§dЛКМ §7= +1", "§cПКМ §7= -1")).build(), e -> {
			switch (e.getClick()) {
			case RIGHT, SHIFT_RIGHT:
				if (stp.min == 1) return;
				stp = new Setup(stp.nm, (byte) (stp.min - 1), stp.max, stp.rndM, stp.fin, stp.bots, 
					stp.tSpawns, stp.ctSpawns, stp.spots, stp.A, stp.B, stp.worlds);
				pl.playSound(pl.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 1f, 0.6f);
				break;
			default:
				stp = new Setup(stp.nm, (byte) (stp.min + 1), stp.min == stp.max ? (byte) (stp.max + 1) : stp.max, 
					stp.rndM, stp.fin, stp.bots, stp.tSpawns, stp.ctSpawns, stp.spots, stp.A, stp.B, stp.worlds);
				pl.playSound(pl.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 1f, 1.4f);
				break;
			}
			reopen(pl, its);
		}));
		
		its.set(8, ClickableItem.of(new ItemBuilder(Material.LEATHER).name("§7Максимум Игроков")
			.setAmount(stp.max).addLore(Arrays.asList("§dЛКМ §7= +1", "§cПКМ §7= -1")).build(), e -> {
			switch (e.getClick()) {
			case RIGHT, SHIFT_RIGHT:
				if (stp.max == 1) return;
				stp = new Setup(stp.nm, stp.min == stp.max ? (byte) (stp.min - 1) : stp.min, (byte) (stp.max - 1), 
						stp.rndM, stp.fin, stp.bots, stp.tSpawns, stp.ctSpawns, stp.spots, stp.A, stp.B, stp.worlds);
				pl.playSound(pl.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 1f, 0.6f);
				break;
			default:
				stp = new Setup(stp.nm, stp.min, (byte) (stp.max + 1), stp.rndM, stp.fin, 
					stp.bots, stp.tSpawns, stp.ctSpawns, stp.spots, stp.A, stp.B, stp.worlds);
				pl.playSound(pl.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 1f, 1.4f);
				break;
			}
			reopen(pl, its);
		}));
		
		if (stp.rndM) {
			its.set(5, ClickableItem.of(new ItemBuilder(Material.POTION).name("§7Рандом: §aВкл").addLore(Arrays.asList("§dКлик §7- Выкл")).build(), e -> {
				stp = new Setup(stp.nm, stp.min, stp.max, false, stp.fin, stp.bots, 
					stp.tSpawns, stp.ctSpawns, stp.spots, stp.A, stp.B, stp.worlds);
				pl.playSound(pl.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 1f, 1.4f);
				reopen(pl, its);
			}));
			
			if (stp.bots) {
				its.set(10, ClickableItem.of(new ItemBuilder(Material.FERMENTED_SPIDER_EYE).name("§7Боты: §aВкл").addLore(Arrays.asList("§dКлик §7- Выкл")).build(), e -> {
					stp = new Setup(stp.nm, stp.min, stp.max, stp.rndM, stp.fin, false, 
						stp.tSpawns, stp.ctSpawns, stp.spots, stp.A, stp.B, stp.worlds);
					pl.playSound(pl.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 1f, 1.4f);
					reopen(pl, its);
				}));
			} else {
				its.set(10, ClickableItem.of(new ItemBuilder(Material.SPIDER_EYE).name("§7Боты: §cВыкл").addLore(Arrays.asList("§dКлик §7- Вкл")).build(), e -> {
					stp = new Setup(stp.nm, stp.min, stp.max, stp.rndM, stp.fin, true, 
						stp.tSpawns, stp.ctSpawns, stp.spots, stp.A, stp.B, stp.worlds);
					pl.playSound(pl.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 1f, 1.4f);
					reopen(pl, its);
				}));
			}
			
			its.set(0, new InputButton(InputType.ANVILL, new ItemBuilder(Material.ENDER_EYE)
				.name("§7Мир для §5Классики§7: " + stp.worlds.get(GameType.DEFUSAL))
				.addLore(Arrays.asList("§dКлик §7- изменить")).build(), "Карта", nm -> {
				final EnumMap<GameType, String> worlds = new EnumMap<>(GameType.class);
				worlds.putAll(stp.worlds);
				worlds.put(GameType.DEFUSAL, nm);
				stp = new Setup(stp.nm, stp.min, stp.max, stp.rndM, stp.fin, stp.bots, 
					stp.tSpawns, stp.ctSpawns, stp.spots, stp.A, stp.B, worlds);
				pl.playSound(pl.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 1f, 1.4f);
				reopen(pl, its);
			}));
			
			its.set(1, new InputButton(InputType.ANVILL, new ItemBuilder(Material.ENDER_PEARL)
				.name("§7Мир для §5Эстафеты§7: " + stp.worlds.get(GameType.GUNGAME))
				.addLore(Arrays.asList("§dКлик §7- изменить")).build(), "Карта", nm -> {
				final EnumMap<GameType, String> worlds = new EnumMap<>(GameType.class);
				worlds.putAll(stp.worlds);
				worlds.put(GameType.GUNGAME, nm);
				stp = new Setup(stp.nm, stp.min, stp.max, stp.rndM, stp.fin, stp.bots, 
					stp.tSpawns, stp.ctSpawns, stp.spots, stp.A, stp.B, worlds);
				pl.playSound(pl.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 1f, 1.4f);
				reopen(pl, its);
			}));
			
			its.set(2, new InputButton(InputType.ANVILL, new ItemBuilder(Material.END_PORTAL_FRAME)
				.name("§7Мир для §5Вторжения§7: " + stp.worlds.get(GameType.INVASION))
				.addLore(Arrays.asList("§dКлик §7- изменить")).build(), "Карта", nm -> {
				final EnumMap<GameType, String> worlds = new EnumMap<>(GameType.class);
				worlds.putAll(stp.worlds);
				worlds.put(GameType.INVASION, nm);
				stp = new Setup(stp.nm, stp.min, stp.max, stp.rndM, stp.fin, stp.bots, 
					stp.tSpawns, stp.ctSpawns, stp.spots, stp.A, stp.B, worlds);
				pl.playSound(pl.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 1f, 1.4f);
				reopen(pl, its);
			}));

        } else {
			its.set(5, ClickableItem.of(new ItemBuilder(Material.GLASS_BOTTLE).name("§7Рандом: §cВыкл").addLore(Arrays.asList("§dКлик §7- Вкл")).build(), e -> {
				stp = new Setup(stp.nm, stp.min, stp.max, true, stp.fin, stp.bots, 
					stp.tSpawns, stp.ctSpawns, stp.spots, stp.A, stp.B, stp.worlds);
				pl.playSound(pl.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 1f, 1.4f);
				reopen(pl, its);
			}));
			
			final List<String> plr = new ArrayList<>();
			plr.add("§dЛКМ §7- добавить");
			plr.add("§cПКМ §7- удалить");
			plr.add("§eШифт+ЛКМ §7- показать");
			plr.add("§eШифт+ПКМ §7- убрать показ");
			plr.add(" ");
			final boolean sps = stp.spots != null && stp.spots.length != 0;
			if (sps) {
				for (int i = stp.spots.length; i > 0; i--) {
					plr.add("§7" + i + ") " + stp.spots[i-1].toString().substring(1));
				}
			} else {
				plr.add("§7Точек еще нет...");
			}
			its.set(15, ClickableItem.of(new ItemBuilder(Material.PURPLE_DYE).name("§7Точки Карты").addLore(plr).build(), e -> {
				switch (e.getClick()) {
				case SHIFT_LEFT:
					if (sps) {
						final World w = pl.getWorld();
						for (final XYZ lc : stp.spots) {
							w.getBlockAt(lc.x, lc.y, lc.z).setType(Material.PURPLE_STAINED_GLASS, false);
						}
					}
					pl.playSound(pl.getLocation(), Sound.BLOCK_BELL_USE, 1f, 1f);
					break;
				case SHIFT_RIGHT:
					if (sps) {
						final World w = pl.getWorld();
						for (final XYZ lc : stp.spots) {
							w.getBlockAt(lc.x, lc.y, lc.z).setType(Material.AIR, false);
						}
					}
					pl.playSound(pl.getLocation(), Sound.BLOCK_BELL_USE, 1f, 1f);
					break;
				case RIGHT:
					if (stp.spots == null) return;
					pl.sendMessage(Main.prf() + "Убрана предыдущая точка (" + (stp.spots.length-1) + ")");
					stp.spots[stp.spots.length - 1].getCenterLoc(pl.getWorld()).getBlock().setType(Material.AIR, false);
					stp = new Setup(stp.nm, stp.min, stp.max, stp.rndM, stp.fin, stp.bots, 
						stp.tSpawns, stp.ctSpawns, rmv(stp.spots), stp.A, stp.B, stp.worlds);
					pl.playSound(pl.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 1f, 0.6f);
					reopen(pl, its);
					return;
				default:
					final Location loc = pl.getLocation();
					stp = new Setup(stp.nm, stp.min, stp.max, stp.rndM, stp.fin, stp.bots, stp.tSpawns, stp.ctSpawns, 
						add(stp.spots, new XYZ("", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ())), stp.A, stp.B, stp.worlds);
					loc.getBlock().setType(Material.PURPLE_STAINED_GLASS, false);
					pl.sendMessage(Main.prf() + "Точка поставлена на " + 
					new XYZ("", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()).toString().substring(1) + " (" + stp.spots.length + ")");
					pl.playSound(pl.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 1f, 1.4f);
					break;
				}
				pl.closeInventory();
			}));
			
			final List<String> tslr = new ArrayList<>();
			tslr.add("§dЛКМ §7- добавить");
			tslr.add("§cПКМ §7- удалить");
			tslr.add("§eШифт+ЛКМ §7- показать");
			tslr.add("§eШифт+ПКМ §7- убрать показ");
			tslr.add(" ");
			if (stp.tSpawns == null) {
				tslr.add("§7Точек еще нет...");
			} else {
				for (int i = stp.tSpawns.length; i > 0; i--) {
					tslr.add("§7" + i + ") " + stp.tSpawns[i-1].toString().substring(1));
				}
			}
			its.set(16, ClickableItem.of(new ItemBuilder(Material.REDSTONE_BLOCK).name("§7Спавны §4Террористов").addLore(tslr).build(), e -> {
				switch (e.getClick()) {
				case SHIFT_LEFT:
					if (stp.tSpawns != null) {
						final World w = pl.getWorld();
						for (final XYZ lc : stp.tSpawns) {
							w.getBlockAt(lc.x, lc.y, lc.z).setType(Material.RED_CONCRETE, false);
						}
					}
					pl.playSound(pl.getLocation(), Sound.BLOCK_BELL_USE, 1f, 1f);
					break;
				case SHIFT_RIGHT:
					if (stp.tSpawns != null) {
						final World w = pl.getWorld();
						for (final XYZ lc : stp.tSpawns) {
							w.getBlockAt(lc.x, lc.y, lc.z).setType(Material.AIR, false);
						}
					}
					pl.playSound(pl.getLocation(), Sound.BLOCK_BELL_USE, 1f, 1f);
					break;
				case RIGHT:
					if (stp.tSpawns == null) return;
					pl.sendMessage(Main.prf() + "Убран предыдущий спавн (" + (stp.tSpawns.length-1) + ")");
					final XYZ lst = stp.tSpawns[stp.tSpawns.length - 1];
					pl.getWorld().getBlockAt(lst.x, lst.y, lst.z).setType(Material.AIR, false);
					stp = new Setup(stp.nm, stp.min, stp.max, stp.rndM, stp.fin, stp.bots, 
						rmv(stp.tSpawns), stp.ctSpawns, stp.spots, stp.A, stp.B, stp.worlds);
					pl.playSound(pl.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 1f, 0.6f);
					reopen(pl, its);
					return;
				default:
					final Location loc = pl.getLocation();
					loc.getBlock().setType(Material.RED_CONCRETE, false);
					stp = new Setup(stp.nm, stp.min, stp.max, stp.rndM, stp.fin, stp.bots, 
						add(stp.tSpawns, new XYZ("", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ())), 
						stp.ctSpawns, stp.spots, stp.A, stp.B, stp.worlds);
					pl.sendMessage(Main.prf() + "Спавн поставлен на " + 
					new XYZ("", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()).toString().substring(1) + " (" + stp.tSpawns.length + ")");
					pl.playSound(pl.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 1f, 1.4f);
					break;
				}
				pl.closeInventory();
			}));
			
			final List<String> ctslr = new ArrayList<>();
			ctslr.add("§dЛКМ §7- добавить");
			ctslr.add("§cПКМ §7- удалить");
			ctslr.add("§eШифт+ЛКМ §7- показать");
			ctslr.add("§eШифт+ПКМ §7- убрать показ");
			ctslr.add(" ");
			if (stp.ctSpawns == null) {
				ctslr.add("§7Точек еще нет...");
			} else {
				for (int i = stp.ctSpawns.length; i > 0; i--) {
					ctslr.add("§7" + i + ") " + stp.ctSpawns[i-1].toString().substring(1));
				}
			}
			its.set(17, ClickableItem.of(new ItemBuilder(Material.LAPIS_BLOCK).name("§7Спавны §3Спецназа").addLore(ctslr).build(), e -> {
				switch (e.getClick()) {
				case SHIFT_LEFT:
					if (stp.ctSpawns != null) {
						final World w = pl.getWorld();
						for (final XYZ lc : stp.ctSpawns) {
							w.getBlockAt(lc.x, lc.y, lc.z).setType(Material.BLUE_CONCRETE, false);
						}
					}
					pl.playSound(pl.getLocation(), Sound.BLOCK_BELL_USE, 1f, 1f);
					break;
				case SHIFT_RIGHT:
					if (stp.ctSpawns != null) {
						final World w = pl.getWorld();
						for (final XYZ lc : stp.ctSpawns) {
							w.getBlockAt(lc.x, lc.y, lc.z).setType(Material.AIR, false);
						}
					}
					pl.playSound(pl.getLocation(), Sound.BLOCK_BELL_USE, 1f, 1f);
					break;
				case RIGHT:
					if (stp.ctSpawns == null) return;
					pl.sendMessage(Main.prf() + "Убран предыдущий спавн (" + (stp.ctSpawns.length-1) + ")");
					final XYZ lst = stp.ctSpawns[stp.ctSpawns.length - 1];
					pl.getWorld().getBlockAt(lst.x, lst.y, lst.z).setType(Material.AIR, false);
					stp = new Setup(stp.nm, stp.min, stp.max, stp.rndM, stp.fin, stp.bots, 
						stp.tSpawns, rmv(stp.ctSpawns), stp.spots, stp.A, stp.B, stp.worlds);
					pl.playSound(pl.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 1f, 0.6f);
					reopen(pl, its);
					return;
				default:
					final Location loc = pl.getLocation();
					loc.getBlock().setType(Material.BLUE_CONCRETE, false);
					stp = new Setup(stp.nm, stp.min, stp.max, stp.rndM, stp.fin, stp.bots, stp.tSpawns, 
						add(stp.ctSpawns, new XYZ("", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ())), 
						stp.spots, stp.A, stp.B, stp.worlds);
					pl.sendMessage(Main.prf() + "Спавн поставлен на " + 
					new XYZ("", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()).toString().substring(1) + " (" + stp.ctSpawns.length + ")");
					pl.playSound(pl.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 1f, 1.4f);
					break;
				}
				pl.closeInventory();
			}));
			
			its.set(25, ClickableItem.of(new ItemBuilder(Material.DIAMOND_BLOCK).name("§7Точка §bA")
				.addLore(Arrays.asList("§dКлик §7- поставить", "§eШифт+Клик §7- показать", stp.A == null ? 
					"§7Не поставлена..." : "§7Точка: " + stp.A.toString().substring(1))).build(), e -> {
				switch (e.getClick()) {
				case SHIFT_LEFT, SHIFT_RIGHT:
					if (stp.A != null) {
						pl.teleport(stp.A.getCenterLoc(pl.getWorld()));
						pl.sendMessage(Main.prf() + "Тут точка §bA");
					}
					break;
				default:
					final Location loc = pl.getLocation();
					stp = new Setup(stp.nm, stp.min, stp.max, stp.rndM, stp.fin, stp.bots, stp.tSpawns, stp.ctSpawns, 
						stp.spots, new XYZ("", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()), 
						stp.B, stp.worlds);
					pl.sendMessage(Main.prf() + "Точка §bA §7теперь на " + stp.A.toString().substring(1));
					pl.playSound(pl.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 1f, 1.4f);
					break;
				}
				pl.closeInventory();
			}));
			
			its.set(26, ClickableItem.of(new ItemBuilder(Material.GOLD_BLOCK).name("§7Точка §6B")
				.addLore(Arrays.asList("§dКлик §7- поставить", "§eШифт+Клик §7- показать", stp.B == null ? 
					"§7Не поставлена..." : "§7Точка: " + stp.B.toString().substring(1))).build(), e -> {
				switch (e.getClick()) {
				case SHIFT_LEFT, SHIFT_RIGHT:
					if (stp.B != null) {
						pl.teleport(stp.B.getCenterLoc(pl.getWorld()));
						pl.sendMessage(Main.prf() + "Тут точка §6B");
					}
					break;
				default:
					final Location loc = pl.getLocation();
					stp = new Setup(stp.nm, stp.min, stp.max, stp.rndM, stp.fin, stp.bots, stp.tSpawns, stp.ctSpawns, 
						stp.spots, stp.A, new XYZ("", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()), 
						stp.worlds);
					pl.sendMessage(Main.prf() + "Точка §6B §7теперь на " + stp.B.toString().substring(1));
					pl.playSound(pl.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 1f, 1.4f);
					break;
				}
				pl.closeInventory();
			}));
			
			/*its.set(12, ClickableItem.of(new ItemBuilder(Material.RAW_IRON_BLOCK).name("§7Нижняя Точка Карты")
				.addLore(Arrays.asList("§dКлик §7- поставить", "§eШифт+Клик §7- показать", stp.bot == null ? 
					"§7Не поставлена..." : "§7Точка: " + stp.bot.toString().substring(1))).build(), e -> {
				switch (e.getClick()) {
				case SHIFT_LEFT, SHIFT_RIGHT:
					if (stp.B != null) {
						pl.teleport(stp.bot.getCenterLoc(pl.getWorld()));
						pl.sendMessage(Main.prf() + "Тут нижняя точка!");
					}
					break;
				default:
					final Location loc = pl.getLocation();
					stp = new Setup(stp.nm, stp.min, stp.max, stp.rndM, stp.fin, stp.bots, stp.tSpawns, stp.ctSpawns, 
						stp.spots, stp.A, stp.B, new XYZ("", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()), stp.top, stp.worlds);
					pl.sendMessage(Main.prf() + "Нижняя точка теперь на " + stp.bot.toString().substring(1));
					pl.playSound(pl.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 1f, 1.4f);
					break;
				}
				pl.closeInventory();
			}));
			
			its.set(13, ClickableItem.of(new ItemBuilder(Material.RAW_GOLD_BLOCK).name("§7Верхняя Точка Карты")
				.addLore(Arrays.asList("§dКлик §7- поставить", "§eШифт+Клик §7- показать", stp.top == null ? 
					"§7Не поставлена..." : "§7Точка: " + stp.top.toString().substring(1))).build(), e -> {
				switch (e.getClick()) {
				case SHIFT_LEFT, SHIFT_RIGHT:
					if (stp.B != null) {
						pl.teleport(stp.top.getCenterLoc(pl.getWorld()));
						pl.sendMessage(Main.prf() + "Тут верхняя точка!");
					}
					break;
				default:
					final Location loc = pl.getLocation();
					stp = new Setup(stp.nm, stp.min, stp.max, stp.rndM, stp.fin, stp.bots, stp.tSpawns, stp.ctSpawns, 
						stp.spots, stp.A, stp.B, stp.bot, new XYZ("", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()), stp.worlds);
					pl.sendMessage(Main.prf() + "Верхняя точка теперь на " + stp.top.toString().substring(1));
					pl.playSound(pl.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 1f, 1.4f);
					break;
				}
				pl.closeInventory();
			}));*/
			
			if (stp.bots) {
				its.set(10, ClickableItem.of(new ItemBuilder(Material.FERMENTED_SPIDER_EYE).name("§7Боты: §aВкл").addLore(Arrays.asList("§dКлик §7- Выкл")).build(), e -> {
					stp = new Setup(stp.nm, stp.min, stp.max, stp.rndM, stp.fin, false, 
						stp.tSpawns, stp.ctSpawns, stp.spots, stp.A, stp.B, stp.worlds);
					pl.playSound(pl.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 1f, 1.4f);
					reopen(pl, its);
				}));
			} else {
				its.set(10, ClickableItem.of(new ItemBuilder(Material.SPIDER_EYE).name("§7Боты: §cВыкл").addLore(Arrays.asList("§dКлик §7- Вкл")).build(), e -> {
					stp = new Setup(stp.nm, stp.min, stp.max, stp.rndM, stp.fin, true, 
						stp.tSpawns, stp.ctSpawns, stp.spots, stp.A, stp.B, stp.worlds);
					pl.playSound(pl.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 1f, 1.4f);
					reopen(pl, its);
				}));
			}
			
			its.set(0, new InputButton(InputType.ANVILL, new ItemBuilder(Material.ENDER_EYE)
				.name("§7Мир для §5Классики§7: " + stp.worlds.get(GameType.DEFUSAL))
				.addLore(Arrays.asList("§dКлик §7- изменить")).build(), "Карта", nm -> {
				final EnumMap<GameType, String> worlds = new EnumMap<>(GameType.class);
				worlds.putAll(stp.worlds);
				worlds.put(GameType.DEFUSAL, nm);
				stp = new Setup(stp.nm, stp.min, stp.max, stp.rndM, stp.fin, stp.bots, 
					stp.tSpawns, stp.ctSpawns, stp.spots, stp.A, stp.B, worlds);
				pl.playSound(pl.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 1f, 1.4f);
				reopen(pl, its);
			}));
			
			its.set(1, new InputButton(InputType.ANVILL, new ItemBuilder(Material.ENDER_PEARL)
				.name("§7Мир для §5Эстафеты§7: " + stp.worlds.get(GameType.GUNGAME))
				.addLore(Arrays.asList("§dКлик §7- изменить")).build(), "Карта", nm -> {
				final EnumMap<GameType, String> worlds = new EnumMap<>(GameType.class);
				worlds.putAll(stp.worlds);
				worlds.put(GameType.GUNGAME, nm);
				stp = new Setup(stp.nm, stp.min, stp.max, stp.rndM, stp.fin, stp.bots, 
					stp.tSpawns, stp.ctSpawns, stp.spots, stp.A, stp.B, worlds);
				pl.playSound(pl.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 1f, 1.4f);
				reopen(pl, its);
			}));
			
			its.set(2, new InputButton(InputType.ANVILL, new ItemBuilder(Material.END_PORTAL_FRAME)
				.name("§7Мир для §5Вторжения§7: " + stp.worlds.get(GameType.INVASION))
				.addLore(Arrays.asList("§dКлик §7- изменить")).build(), "Карта", nm -> {
				final EnumMap<GameType, String> worlds = new EnumMap<>(GameType.class);
				worlds.putAll(stp.worlds);
				worlds.put(GameType.INVASION, nm);
				stp = new Setup(stp.nm, stp.min, stp.max, stp.rndM, stp.fin, stp.bots, 
					stp.tSpawns, stp.ctSpawns, stp.spots, stp.A, stp.B, worlds);
				pl.playSound(pl.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 1f, 1.4f);
				reopen(pl, its);
			}));

        }

        if (stp.isReady()) {
            stp = new Setup(stp.nm, stp.min, stp.max, stp.rndM, true, stp.bots,
                stp.tSpawns, stp.ctSpawns, stp.spots, stp.A, stp.B, stp.worlds);
            its.set(22, ClickableItem.of(new ItemBuilder(Material.KNOWLEDGE_BOOK).name("§aГотово").addLore(Arrays.asList("§7Закрыть редактор!")).build(), e -> {
                pl.playSound(pl.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
                pl.sendMessage(Main.prf() + "Карта §d" + stp.nm + " §7сохранена!");
                Main.nnactvarns.put(stp.nm, stp);
                edits.remove(pl.getUniqueId());
                stp.save(Main.ars);
                pl.closeInventory();
            }));
        } else {
            stp = new Setup(stp.nm, stp.min, stp.max, stp.rndM, false, stp.bots,
                stp.tSpawns, stp.ctSpawns, stp.spots, stp.A, stp.B, stp.worlds);
            its.set(22, ClickableItem.empty(new ItemBuilder(Material.GRAY_DYE).name("§cНе Готово").addLore(Arrays.asList("§7Какие-то поля пустые!")).build()));
        }

        its.set(18, new InputButton(InputType.ANVILL, new ItemBuilder(Material.BARRIER)
        .name("§4Удалить").addLore(Arrays.asList("§7Невозвратимо!")).build(), stp.nm, e -> {
            pl.playSound(pl.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 1f, 1f);
            pl.sendMessage(Main.prf() + "Карта §5" + stp.nm + " §7удалена!");
            edits.remove(pl.getUniqueId());
            stp.delete(true);
            pl.closeInventory();
        }));
    }

	/*private static ItemStack[] fillSkinInv(final boolean donate) {
		final ItemStack[] its = new ItemStack[54];
		final ItemStack rl = new ItemBuilder(donate ? Material.POWERED_RAIL : Material.ACTIVATOR_RAIL).name("§0.").build();
		for (int r = 0; r != 3; r++) {
			for (int c = 0; c != 6; c++) {
				its[(r << 2) + c * 9] = rl;
			}
		}

		final ItemStack blnk = new ItemBuilder(Material.LIGHT_GRAY_STAINED_GLASS_PANE).name("§0.").build();
		for (int i = its.length - 1; i >= 0; i--) {
			if (its[i] == null) its[i] = blnk;
		}
		return its;
	}*/
	
	private static XYZ[] add(final XYZ[] ar, final XYZ el) {
		if (ar == null) return new XYZ[] {el};
		final XYZ[] na = new XYZ[ar.length + 1];
        System.arraycopy(ar, 0, na, 0, ar.length);
		na[ar.length] = el;
		return na;
	}
	
	private static XYZ[] rmv(final XYZ[] ar) {
		if (ar == null || ar.length == 1) return null;
		final XYZ[] na = new XYZ[ar.length - 1];
        System.arraycopy(ar, 0, na, 0, na.length);
		return na;
	}
	
	/*private static BaseBlockPosition[] add(final BaseBlockPosition[] ar, final BaseBlockPosition el) {
		if (ar == null) return new BaseBlockPosition[] {el};
		final BaseBlockPosition[] na = new BaseBlockPosition[ar.length + 1];
		for (int i = 0; i < ar.length; i++) {na[i] = ar[i];}
		na[ar.length] = el;
		return na;
	}
	
	private static BaseBlockPosition[] rmv(final BaseBlockPosition[] ar) {
		if (ar == null || ar.length == 1) return null;
		final BaseBlockPosition[] na = new BaseBlockPosition[ar.length - 1];
		for (int i = 0; i < na.length; i++) {na[i] = ar[i];}
		return na;
	}*/

}
