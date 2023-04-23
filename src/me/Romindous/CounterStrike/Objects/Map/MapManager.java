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
import me.Romindous.CounterStrike.Game.Arena.Team;
import me.Romindous.CounterStrike.Objects.Game.GameType;
import me.Romindous.CounterStrike.Objects.Loc.Spot;
import net.minecraft.core.BaseBlockPosition;
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
		ar.spots, ar.ctSpawns, ar.tSpawns, ar.A, ar.B, ar.worlds);
	}
	
	@Override
	public void init(final Player pl, final InventoryContent its) {
		edits.put(pl.getUniqueId(), this);
		
		its.set(0, new InputButton(InputType.ANVILL, //имя
			new ItemBuilder(Material.GLOBE_BANNER_PATTERN).name("§5" + stp.nm).lore(Arrays.asList("§dКлик §7- изменить имя")).build(), "Карта", nm -> {
			stp = new Setup(nm, stp.min, stp.max, stp.rndM, stp.fin, stp.bots, stp.spots, stp.ctSpawns, stp.tSpawns, stp.A, stp.B, stp.worlds);
			reopen(pl, its);
		}));
		
		its.set(1, ClickableItem.of(new ItemBuilder(Material.RABBIT_HIDE).name("§7Минимум Игроков")
			.setAmount(stp.min).lore(Arrays.asList("§dЛКМ §7= +1", "§cПКМ §7= -1")).build(), e -> {
			switch (e.getClick()) {
			case RIGHT, SHIFT_RIGHT:
				if (stp.min == 1) return;
				stp = new Setup(stp.nm, (byte) (stp.min - 1), stp.max, 
					stp.rndM, stp.fin, stp.bots, stp.spots, stp.ctSpawns, stp.tSpawns, stp.A, stp.B, stp.worlds);
				pl.playSound(pl.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 1f, 0.6f);
				break;
			default:
				stp = new Setup(stp.nm, (byte) (stp.min + 1), stp.min == stp.max ? (byte) (stp.max + 1) : stp.max, 
					stp.rndM, stp.fin, stp.bots, stp.spots, stp.ctSpawns, stp.tSpawns, stp.A, stp.B, stp.worlds);
				pl.playSound(pl.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 1f, 1.4f);
				break;
			}
			reopen(pl, its);
		}));
		
		its.set(2, ClickableItem.of(new ItemBuilder(Material.LEATHER).name("§7Максимум Игроков")
			.setAmount(stp.max).lore(Arrays.asList("§dЛКМ §7= +1", "§cПКМ §7= -1")).build(), e -> {
			switch (e.getClick()) {
			case RIGHT, SHIFT_RIGHT:
				if (stp.max == 1) return;
				stp = new Setup(stp.nm, stp.min == stp.max ? (byte) (stp.min - 1) : stp.min, (byte) (stp.max - 1), 
					stp.rndM, stp.fin, stp.bots, stp.spots, stp.ctSpawns, stp.tSpawns, stp.A, stp.B, stp.worlds);
				pl.playSound(pl.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 1f, 0.6f);
				break;
			default:
				stp = new Setup(stp.nm, stp.min, (byte) (stp.max + 1), 
					stp.rndM, stp.fin, stp.bots, stp.spots, stp.ctSpawns, stp.tSpawns, stp.A, stp.B, stp.worlds);
				pl.playSound(pl.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 1f, 1.4f);
				break;
			}
			reopen(pl, its);
		}));
		
		if (stp.rndM) {
			its.set(3, ClickableItem.of(new ItemBuilder(Material.POTION).name("§7Рандом: §aВкл").lore(Arrays.asList("§dКлик §7- Выкл")).build(), e -> {
				stp = new Setup(stp.nm, stp.min, stp.max, false, stp.fin, stp.bots, stp.spots, stp.ctSpawns, stp.tSpawns, stp.A, stp.B, stp.worlds);
				pl.playSound(pl.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 1f, 1.4f);
				reopen(pl, its);
			}));
			
			its.set(4, new InputButton(InputType.ANVILL, new ItemBuilder(Material.ENDER_EYE)
				.name("§7Мир для §5Классики§7: " + stp.worlds.get(GameType.DEFUSAL))
				.lore(Arrays.asList("§dКлик §7- изменить")).build(), "Карта", nm -> {
				final EnumMap<GameType, String> worlds = new EnumMap<>(stp.worlds);
				worlds.put(GameType.DEFUSAL, nm);
				stp = new Setup(stp.nm, stp.min, stp.max, stp.rndM, stp.fin, stp.bots, stp.spots, stp.ctSpawns, stp.tSpawns, stp.A, stp.B, worlds);
				pl.playSound(pl.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 1f, 1.4f);
				reopen(pl, its);
			}));
			
			if (stp.isReady()) {
				stp = new Setup(stp.nm, stp.min, stp.max, stp.rndM, true, stp.bots, stp.spots, stp.ctSpawns, stp.tSpawns, stp.A, stp.B, stp.worlds);
				its.set(5, ClickableItem.of(new ItemBuilder(Material.KNOWLEDGE_BOOK).name("§aГотово").lore(Arrays.asList("§7Закрыть редактор!")).build(), e -> {
					pl.playSound(pl.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
					pl.sendMessage(Main.prf() + "Карта §d" + stp.nm + " §7сохранена!");
					Main.nnactvarns.put(stp.nm, stp);
					edits.remove(pl.getUniqueId());
					stp.save(Main.ars);
					pl.closeInventory();
				}));
			} else {
				stp = new Setup(stp.nm, stp.min, stp.max, stp.rndM, false, stp.bots, stp.spots, stp.ctSpawns, stp.tSpawns, stp.A, stp.B, stp.worlds);
				its.set(5, ClickableItem.empty(new ItemBuilder(Material.GRAY_DYE).name("§cНе Готово").lore(Arrays.asList("§7Какие-то поля пустые!")).build()));
			}
			
			its.set(6, new InputButton(InputType.ANVILL, new ItemBuilder(Material.BARRIER)
			.name("§4Удалить").lore(Arrays.asList("§7Невозвратимо!")).build(), stp.nm, e -> {
				pl.playSound(pl.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 1f, 1f);
				pl.sendMessage(Main.prf() + "Карта §5" + stp.nm + " §7удалена!");
				edits.remove(pl.getUniqueId());
				stp.delete(true);
				pl.closeInventory();
			}));
		} else {
			its.set(3, ClickableItem.of(new ItemBuilder(Material.GLASS_BOTTLE).name("§7Рандом: §cВыкл").lore(Arrays.asList("§dКлик §7- Вкл")).build(), e -> {
				stp = new Setup(stp.nm, stp.min, stp.max, true, stp.fin, stp.bots, stp.spots, stp.ctSpawns, stp.tSpawns, stp.A, stp.B, stp.worlds);
				pl.playSound(pl.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 1f, 1.4f);
				reopen(pl, its);
			}));
			
			final List<String> ctplr = new ArrayList<>();
			ctplr.add("§dЛКМ §7- добавить");
			ctplr.add("§cПКМ §7- удалить");
			ctplr.add("§eШифт+ЛКМ §7- показать");
			ctplr.add("§eШифт+ПКМ §7- убрать показ");
			ctplr.add(" ");
			final boolean cts = stp.hasSpot(Team.CTs);
			if (cts) {
				for (final Spot lc : stp.spots) {
					ctplr.add("§7" + lc.toString());
				}
			} else {
				ctplr.add("§7Точек еще нет...");
			}
			its.set(4, ClickableItem.of(new ItemBuilder(Material.BLUE_DYE).name("§7Точки §3Спецназа").lore(ctplr).build(), e -> {
				switch (e.getClick()) {
				case SHIFT_LEFT:
					if (cts) {
						final World w = pl.getWorld();
						for (final Spot lc : stp.spots) {
							if (lc.tm == Team.CTs) {
								w.getBlockAt(lc.u(), lc.v(), lc.w()).setType(Material.BLUE_STAINED_GLASS, false);
							}
						}
					}
					pl.playSound(pl.getLocation(), Sound.BLOCK_BELL_USE, 1f, 1f);
					break;
				case SHIFT_RIGHT:
					if (cts) {
						final World w = pl.getWorld();
						for (final Spot lc : stp.spots) {
							if (lc.tm == Team.CTs) {
								w.getBlockAt(lc.u(), lc.v(), lc.w()).setType(Material.AIR, false);
							}
						}
					}
					pl.playSound(pl.getLocation(), Sound.BLOCK_BELL_USE, 1f, 1f);
					break;
				case RIGHT:
					if (stp.spots == null) return;
					pl.sendMessage(Main.prf() + "Убрана предыдущая точка (" + (stp.spots.length-1) + ")");
					stp.spots[stp.spots.length - 1].getBlock(pl.getWorld()).setType(Material.AIR, false);
					stp = new Setup(stp.nm, stp.min, stp.max, stp.rndM, stp.fin, stp.bots, 
						rmv(stp.spots), stp.ctSpawns, stp.tSpawns, stp.A, stp.B, stp.worlds);
					pl.playSound(pl.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 1f, 0.6f);
					reopen(pl, its);
					return;
				default:
					final Location loc = pl.getLocation();loc.setY(loc.getY() + 1d);
					stp = new Setup(stp.nm, stp.min, stp.max, stp.rndM, stp.fin, stp.bots, 
						add(stp.spots, new Spot(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), 
								stp.spots == null ? 0 : stp.spots.length, Team.CTs, new int[0])), 
						stp.ctSpawns, stp.tSpawns, stp.A, stp.B, stp.worlds);
					loc.getBlock().setType(Material.BLUE_STAINED_GLASS, false);
					pl.sendMessage(Main.prf() + "Точка поставлена на " + 
					new BaseBlockPosition(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()).toString() + " (" + stp.spots.length + ")");
					pl.playSound(pl.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 1f, 1.4f);
					break;
				}
				pl.closeInventory();
			}));
			
			final List<String> tplr = new ArrayList<>();
			tplr.add("§dЛКМ §7- добавить");
			tplr.add("§cПКМ §7- удалить");
			tplr.add("§eШифт+ЛКМ §7- показать");
			tplr.add("§eШифт+ПКМ §7- убрать показ");
			tplr.add(" ");
			final boolean ts = stp.hasSpot(Team.Ts);
			if (ts) {
				for (final Spot lc : stp.spots) {
					tplr.add("§7" + lc.toString());
				}
			} else {
				tplr.add("§7Точек еще нет...");
			}
			its.set(5, ClickableItem.of(new ItemBuilder(Material.REDSTONE).name("§7Точки §4Террористов").lore(tplr).build(), e -> {
				switch (e.getClick()) {
				case SHIFT_LEFT:
					if (ts) {
						final World w = pl.getWorld();
						for (final Spot lc : stp.spots) {
							if (lc.tm == Team.Ts) {
								w.getBlockAt(lc.u(), lc.v(), lc.w()).setType(Material.RED_STAINED_GLASS, false);
							}
						}
					}
					pl.playSound(pl.getLocation(), Sound.BLOCK_BELL_USE, 1f, 1f);
					break;
				case SHIFT_RIGHT:
					if (ts) {
						final World w = pl.getWorld();
						for (final Spot lc : stp.spots) {
							if (lc.tm == Team.Ts) {
								w.getBlockAt(lc.u(), lc.v(), lc.w()).setType(Material.AIR, false);
							}
						}
					}
					pl.playSound(pl.getLocation(), Sound.BLOCK_BELL_USE, 1f, 1f);
					break;
				case RIGHT:
					if (stp.spots == null) return;
					pl.sendMessage(Main.prf() + "Убрана предыдущая точка (" + (stp.spots.length-1) + ")");
					stp.spots[stp.spots.length - 1].getBlock(pl.getWorld()).setType(Material.AIR, false);
					stp = new Setup(stp.nm, stp.min, stp.max, stp.rndM, stp.fin, stp.bots, 
						rmv(stp.spots), stp.ctSpawns, stp.tSpawns, stp.A, stp.B, stp.worlds);
					pl.playSound(pl.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 1f, 0.6f);
					reopen(pl, its);
					return;
				default:
					final Location loc = pl.getLocation();loc.setY(loc.getY() + 1d);
					stp = new Setup(stp.nm, stp.min, stp.max, stp.rndM, stp.fin, stp.bots, 
						add(stp.spots, new Spot(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), 
							stp.spots == null ? 0 : stp.spots.length, Team.Ts, new int[0])), 
						stp.ctSpawns, stp.tSpawns, stp.A, stp.B, stp.worlds);
					loc.getBlock().setType(Material.RED_STAINED_GLASS, false);
					pl.sendMessage(Main.prf() + "Точка поставлена на " + 
					new BaseBlockPosition(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()).toString() + " (" + stp.spots.length + ")");
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
				for (final BaseBlockPosition lc : stp.ctSpawns) {
					ctslr.add("§7" + lc.toString());
				}
			}
			its.set(6, ClickableItem.of(new ItemBuilder(Material.LAPIS_BLOCK).name("§7Спавны §3Спецназа").lore(ctslr).build(), e -> {
				switch (e.getClick()) {
				case SHIFT_LEFT:
					if (stp.ctSpawns != null) {
						final World w = pl.getWorld();
						for (final BaseBlockPosition lc : stp.ctSpawns) {
							w.getBlockAt(lc.u(), lc.v(), lc.w()).setType(Material.BLUE_CONCRETE, false);
						}
					}
					pl.playSound(pl.getLocation(), Sound.BLOCK_BELL_USE, 1f, 1f);
					break;
				case SHIFT_RIGHT:
					if (stp.ctSpawns != null) {
						final World w = pl.getWorld();
						for (final BaseBlockPosition lc : stp.ctSpawns) {
							w.getBlockAt(lc.u(), lc.v(), lc.w()).setType(Material.AIR, false);
						}
					}
					pl.playSound(pl.getLocation(), Sound.BLOCK_BELL_USE, 1f, 1f);
					break;
				case RIGHT:
					if (stp.ctSpawns == null) return;
					pl.sendMessage(Main.prf() + "Убран предыдущий спавн (" + (stp.ctSpawns.length-1) + ")");
					final BaseBlockPosition lst = stp.ctSpawns[stp.ctSpawns.length - 1];
					pl.getWorld().getBlockAt(lst.u(), lst.v(), lst.w()).setType(Material.AIR, false);
					stp = new Setup(stp.nm, stp.min, stp.max, stp.rndM, stp.fin, stp.bots, 
						stp.spots, rmv(stp.ctSpawns), stp.tSpawns, stp.A, stp.B, stp.worlds);
					pl.playSound(pl.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 1f, 0.6f);
					reopen(pl, its);
					return;
				default:
					final Location loc = pl.getLocation();
					loc.getBlock().setType(Material.BLUE_CONCRETE, false);
					stp = new Setup(stp.nm, stp.min, stp.max, stp.rndM, stp.fin, stp.bots, stp.spots, 
						add(stp.ctSpawns, new BaseBlockPosition(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ())), stp.tSpawns, stp.A, stp.B, stp.worlds);
					pl.sendMessage(Main.prf() + "Спавн поставлен на " + 
					new BaseBlockPosition(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()).toString() + " (" + stp.ctSpawns.length + ")");
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
				for (final BaseBlockPosition lc : stp.tSpawns) {
					tslr.add("§7" + lc.toString());
				}
			}
			its.set(7, ClickableItem.of(new ItemBuilder(Material.REDSTONE_BLOCK).name("§7Спавны §4Террористов").lore(tslr).build(), e -> {
				switch (e.getClick()) {
				case SHIFT_LEFT:
					if (stp.tSpawns != null) {
						final World w = pl.getWorld();
						for (final BaseBlockPosition lc : stp.tSpawns) {
							w.getBlockAt(lc.u(), lc.v(), lc.w()).setType(Material.RED_CONCRETE, false);
						}
					}
					pl.playSound(pl.getLocation(), Sound.BLOCK_BELL_USE, 1f, 1f);
					break;
				case SHIFT_RIGHT:
					if (stp.tSpawns != null) {
						final World w = pl.getWorld();
						for (final BaseBlockPosition lc : stp.tSpawns) {
							w.getBlockAt(lc.u(), lc.v(), lc.w()).setType(Material.AIR, false);
						}
					}
					pl.playSound(pl.getLocation(), Sound.BLOCK_BELL_USE, 1f, 1f);
					break;
				case RIGHT:
					if (stp.tSpawns == null) return;
					pl.sendMessage(Main.prf() + "Убран предыдущий спавн (" + (stp.tSpawns.length-1) + ")");
					final BaseBlockPosition lst = stp.tSpawns[stp.tSpawns.length - 1];
					pl.getWorld().getBlockAt(lst.u(), lst.v(), lst.w()).setType(Material.AIR, false);
					stp = new Setup(stp.nm, stp.min, stp.max, stp.rndM, stp.fin, stp.bots, 
						stp.spots, stp.ctSpawns, rmv(stp.tSpawns), stp.A, stp.B, stp.worlds);
					pl.playSound(pl.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 1f, 0.6f);
					reopen(pl, its);
					return;
				default:
					final Location loc = pl.getLocation();
					loc.getBlock().setType(Material.BLUE_CONCRETE, false);
					stp = new Setup(stp.nm, stp.min, stp.max, stp.rndM, stp.fin, stp.bots, stp.spots, stp.ctSpawns, 
						add(stp.tSpawns, new BaseBlockPosition(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ())), stp.A, stp.B, stp.worlds);
					pl.sendMessage(Main.prf() + "Спавн поставлен на " + 
					new BaseBlockPosition(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()).toString() + " (" + stp.tSpawns.length + ")");
					pl.playSound(pl.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 1f, 1.4f);
					break;
				}
				pl.closeInventory();
			}));
			
			its.set(8, ClickableItem.of(new ItemBuilder(Material.DIAMOND_BLOCK).name("§7Точка §bA")
				.lore(Arrays.asList("§dКлик §7- поставить", "§eШифт+Клик §7- показать", stp.A == null ? "§7Не поставлена..." : "§7Точка: " + stp.A.toString())).build(), e -> {
				switch (e.getClick()) {
				case SHIFT_LEFT, SHIFT_RIGHT:
					if (stp.A != null) {
						pl.teleport(new Location(pl.getWorld(), stp.A.u() + 0.5d, stp.A.v() + 0.5d, stp.A.w() + 0.5d));
						pl.sendMessage(Main.prf() + "Тут точка §bA");
					}
					break;
				default:
					final Location loc = pl.getLocation();
					stp = new Setup(stp.nm, stp.min, stp.max, stp.rndM, stp.fin, stp.bots, stp.spots, 
						stp.ctSpawns, stp.tSpawns, new BaseBlockPosition(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()), stp.B, stp.worlds);
					pl.sendMessage(Main.prf() + "Точка §bA §7теперь на " + stp.A.toString());
					pl.playSound(pl.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 1f, 1.4f);
					break;
				}
				pl.closeInventory();
			}));
			
			its.set(9, ClickableItem.of(new ItemBuilder(Material.GOLD_BLOCK).name("§7Точка §6B")
				.lore(Arrays.asList("§dКлик §7- поставить", "§eШифт+Клик §7- показать", stp.B == null ? "§7Не поставлена..." : "§7Точка: " + stp.B.toString())).build(), e -> {
				switch (e.getClick()) {
				case SHIFT_LEFT, SHIFT_RIGHT:
					if (stp.B != null) {
						pl.teleport(new Location(pl.getWorld(), stp.B.u() + 0.5d, stp.B.v() + 0.5d, stp.B.w() + 0.5d));
						pl.sendMessage(Main.prf() + "Тут точка §6B");
					}
					break;
				default:
					final Location loc = pl.getLocation();
					stp = new Setup(stp.nm, stp.min, stp.max, stp.rndM, stp.fin, stp.bots, stp.spots, 
						stp.ctSpawns, stp.tSpawns, stp.A, new BaseBlockPosition(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()), stp.worlds);
					pl.sendMessage(Main.prf() + "Точка §6B §7теперь на " + stp.B.toString());
					pl.playSound(pl.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 1f, 1.4f);
					break;
				}
				pl.closeInventory();
			}));
			
			if (stp.bots) {
				its.set(10, ClickableItem.of(new ItemBuilder(Material.FERMENTED_SPIDER_EYE).name("§7Боты: §aВкл").lore(Arrays.asList("§dКлик §7- Выкл")).build(), e -> {
					stp = new Setup(stp.nm, stp.min, stp.max, stp.rndM, stp.fin, false, stp.spots, stp.ctSpawns, stp.tSpawns, stp.A, stp.B, stp.worlds);
					pl.playSound(pl.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 1f, 1.4f);
					reopen(pl, its);
				}));
			} else {
				its.set(10, ClickableItem.of(new ItemBuilder(Material.SPIDER_EYE).name("§7Боты: §cВыкл").lore(Arrays.asList("§dКлик §7- Вкл")).build(), e -> {
					stp = new Setup(stp.nm, stp.min, stp.max, stp.rndM, stp.fin, true, stp.spots, stp.ctSpawns, stp.tSpawns, stp.A, stp.B, stp.worlds);
					pl.playSound(pl.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 1f, 1.4f);
					reopen(pl, its);
				}));
			}
			
			its.set(11, new InputButton(InputType.ANVILL, new ItemBuilder(Material.ENDER_EYE)
				.name("§7Мир для §5Классики§7: " + stp.worlds.get(GameType.DEFUSAL))
				.lore(Arrays.asList("§dКлик §7- изменить")).build(), "Карта", nm -> {
				final EnumMap<GameType, String> worlds = new EnumMap<>(stp.worlds);
				worlds.put(GameType.DEFUSAL, nm);
				stp = new Setup(stp.nm, stp.min, stp.max, stp.rndM, stp.fin, stp.bots, stp.spots, stp.ctSpawns, stp.tSpawns, stp.A, stp.B, worlds);
				pl.playSound(pl.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 1f, 1.4f);
				reopen(pl, its);
			}));
			
			its.set(12, new InputButton(InputType.ANVILL, new ItemBuilder(Material.ENDER_PEARL)
				.name("§7Мир для §5Эстафеты§7: " + stp.worlds.get(GameType.GUNGAME))
				.lore(Arrays.asList("§dКлик §7- изменить")).build(), "Карта", nm -> {
				final EnumMap<GameType, String> worlds = new EnumMap<>(stp.worlds);
				worlds.put(GameType.GUNGAME, nm);
				stp = new Setup(stp.nm, stp.min, stp.max, stp.rndM, stp.fin, stp.bots, stp.spots, stp.ctSpawns, stp.tSpawns, stp.A, stp.B, worlds);
				pl.playSound(pl.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 1f, 1.4f);
				reopen(pl, its);
			}));
			
			its.set(13, new InputButton(InputType.ANVILL, new ItemBuilder(Material.END_PORTAL_FRAME)
				.name("§7Мир для §5Вторжения§7: " + stp.worlds.get(GameType.INVASION))
				.lore(Arrays.asList("§dКлик §7- изменить")).build(), "Карта", nm -> {
				final EnumMap<GameType, String> worlds = new EnumMap<>(stp.worlds);
				worlds.put(GameType.INVASION, nm);
				stp = new Setup(stp.nm, stp.min, stp.max, stp.rndM, stp.fin, stp.bots, stp.spots, stp.ctSpawns, stp.tSpawns, stp.A, stp.B, worlds);
				pl.playSound(pl.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 1f, 1.4f);
				reopen(pl, its);
			}));
			
			if (stp.isReady()) {
				stp = new Setup(stp.nm, stp.min, stp.max, stp.rndM, true, stp.bots, stp.linkSpots(pl.getWorld()), stp.ctSpawns, stp.tSpawns, stp.A, stp.B, stp.worlds);
				its.set(14, ClickableItem.of(new ItemBuilder(Material.KNOWLEDGE_BOOK).name("§aГотово").lore(Arrays.asList("§7Закрыть редактор!")).build(), e -> {
					pl.playSound(pl.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
					pl.sendMessage(Main.prf() + "Карта §d" + stp.nm + " §7сохранена!");
					Main.nnactvarns.put(stp.nm, stp);
					edits.remove(pl.getUniqueId());
					stp.save(Main.ars);
					pl.closeInventory();
				}));
			} else {
				stp = new Setup(stp.nm, stp.min, stp.max, stp.rndM, false, stp.bots, stp.spots, stp.ctSpawns, stp.tSpawns, stp.A, stp.B, stp.worlds);
				its.set(14, ClickableItem.empty(new ItemBuilder(Material.GRAY_DYE).name("§cНе Готово").lore(Arrays.asList("§7Какие-то поля пустые!")).build()));
			}
			
			its.set(15, new InputButton(InputType.ANVILL, new ItemBuilder(Material.BARRIER)
			.name("§4Удалить").lore(Arrays.asList("§7Невозвратимо!")).build(), stp.nm, e -> {
				pl.playSound(pl.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 1f, 1f);
				pl.sendMessage(Main.prf() + "Карта §5" + stp.nm + " §7удалена!");
				edits.remove(pl.getUniqueId());
				stp.delete(true);
				pl.closeInventory();
			}));
		}
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
	
	private static Spot[] add(final Spot[] ar, final Spot el) {
		if (ar == null) return new Spot[] {el};
		final Spot[] na = new Spot[ar.length + 1];
		for (int i = 0; i < ar.length; i++) {na[i] = ar[i];}
		na[ar.length] = el;
		return na;
	}
	
	private static Spot[] rmv(final Spot[] ar) {
		if (ar == null || ar.length == 1) return null;
		final Spot[] na = new Spot[ar.length - 1];
		for (int i = 0; i < na.length; i++) {na[i] = ar[i];}
		return na;
	}
	
	private static BaseBlockPosition[] add(final BaseBlockPosition[] ar, final BaseBlockPosition el) {
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
	}

}
