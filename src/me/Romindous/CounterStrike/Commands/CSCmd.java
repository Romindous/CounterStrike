	package me.Romindous.CounterStrike.Commands;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import com.mojang.datafixers.util.Pair;

import me.Romindous.CounterStrike.Main;
import me.Romindous.CounterStrike.Game.Arena;
import me.Romindous.CounterStrike.Objects.Setup;
import me.Romindous.CounterStrike.Objects.Shooter;
import net.minecraft.core.BaseBlockPosition;
import ru.komiss77.ApiOstrov;

public class CSCmd implements CommandExecutor, TabCompleter {
	
	@Override
	public List<String> onTabComplete(final CommandSender send, final Command cmd, final String al, final String[] args) {
		final LinkedList<String> sugg = new LinkedList<String>();
		if (send instanceof Player) {
			final Player p = (Player) send;
			if (p.hasPermission("ostrov.builder")) {
				if (args.length == 1) {
					sugg.add("join");
					sugg.add("leave");
					sugg.add("help");
					sugg.add("create");
					sugg.add("type");
					sugg.add("addTspawn");
					sugg.add("addCTspawn");
					sugg.add("setAsite");
					sugg.add("setBsite");
					sugg.add("finish");
					sugg.add("delete");
					sugg.add("setlobby");
					sugg.add("reload");
				} else if (args.length == 2 && (args[0].equalsIgnoreCase("join") || args[0].equalsIgnoreCase("delete"))) {
					sugg.addAll(Main.nnactvarns.keySet());
				} else if (args.length == 2 && (args[0].equalsIgnoreCase("setBsite") 
						|| args[0].equalsIgnoreCase("setAsite") 
						|| args[0].equalsIgnoreCase("addCTspawn") 
						|| args[0].equalsIgnoreCase("addTspawn") 
						|| args[0].equalsIgnoreCase("finish") 
						|| args[0].equalsIgnoreCase("type"))) {
					for (final String s : Main.ars.getConfigurationSection("arenas").getKeys(false)) {
						if (!Main.ars.contains("arenas." + s + ".fin")) {
							sugg.add(s);
						}
					}
				} else if (args.length == 3 && (args[0].equalsIgnoreCase("type"))) {
					sugg.add("defusal");
					sugg.add("gungame");
					sugg.add("invasion");
				} else if (args.length == 5 && (args[0].equalsIgnoreCase("create"))) {
					sugg.add("rnd");
					sugg.add("nornd");
				} 
			} else {
				if (args.length == 1) {
					sugg.add("join");
					sugg.add("leave");
					sugg.add("help");
				} else if (args.length == 2) {
					if (args[0].equalsIgnoreCase("join")) {
						sugg.addAll(Main.nnactvarns.keySet());
					}
				}
			}
		}
		return sugg;
	}

	@Override
	public boolean onCommand(final CommandSender send, final Command cmd, final String label, final String[] args) {
		if (label.equalsIgnoreCase("cs") && send instanceof Player) {
			final Player p = (Player) send;
			//админ комманды
			if (p.hasPermission("ostrov.builder")) {
				//создание карты
				if (args.length == 5 && args[0].equalsIgnoreCase("create") && !Main.ars.contains("arenas." + args[1])) {
					p.sendMessage(Main.prf() + "Начинаем cоздание арены §5" + args[1] + "§7:");
					final byte min;
					final byte max;
					//проверка на число
					try {
						min = Byte.parseByte(args[2]);
						max = (byte) ((Byte.parseByte(args[3]) >> 1) << 1);
					} catch (NumberFormatException e) {
						p.sendMessage(Main.prf() + "§cПосле названия надо вписать 2 числа!");
						return true;
					}
					if (min >= 2 && min <= max) {
						//добавляем арену
						Main.ars.set("arenas." + args[1] + ".rnd", args[4].equalsIgnoreCase("rnd"));
						Main.ars.set("arenas." + args[1] + ".min", min);
						Main.ars.set("arenas." + args[1] + ".max", max);
						Main.ars.set("arenas." + args[1] + ".type", "defusal");
						Main.ars.set("arenas." + args[1] + ".world", p.getWorld().getName());
						try {
							Main.ars.save(new File(Main.folder + File.separator + "arenas.yml"));
						} catch (IOException e) {
							e.printStackTrace();
						}
						p.sendMessage(Main.prf() + "Минимальное кол-во игроков: §d" + min);
						p.sendMessage(Main.prf() + "Максимальное кол-во игроков: §d" + max);
						return true;
					} else {
						p.sendMessage(Main.prf() + "§cПервое число должно быть меньше или равно второму и быть более 2!");
						return true;
					}
				} else if (args.length == 3 && args[0].equalsIgnoreCase("type") && Main.ars.contains("arenas." + args[1]) && !Main.ars.contains("arenas." + args[1] + ".fin")) {
					
					switch (args[2]) {
					case "defusal":
					case "gungame":
					case "invasion":
						Main.ars.set("arenas." + args[1] + ".type", args[2]);
						p.sendMessage(Main.prf() + "Режим игры изменен на §d" + args[2]);
						
						try {
							Main.ars.save(new File(Main.folder + File.separator + "arenas.yml"));
						} catch (IOException e) {
							e.printStackTrace();
						}
						
						return true;
					default:
						p.sendMessage(Main.prf() + "§cПосле названия надо вписать тип карты (defusal | gungame | invasion)!");
						return false;
					}
				} else if (args.length == 2) {
					//добавление спавнов комманд
					if (args[0].equalsIgnoreCase("addTspawn") && Main.ars.contains("arenas." + args[1]) && !Main.ars.contains("arenas." + args[1] + ".fin")) {
						if (Main.ars.contains("arenas." + args[1] + ".tspawns")) {
							final ConfigurationSection cs = Main.ars.getConfigurationSection("arenas." + args[1] + ".tspawns");
							cs.set("x", cs.getString("x") + ':' + p.getLocation().getBlockX());
							cs.set("y", cs.getString("y") + ':' + p.getLocation().getBlockY());
							cs.set("z", cs.getString("z") + ':' + p.getLocation().getBlockZ());
							
							p.sendMessage(Main.prf() + "Новый спавн для §4T§7 на коорд. (§5" + p.getLocation().getBlockX() + "§7, §5" + p.getLocation().getBlockY() + "§7, §5" + p.getLocation().getBlockZ() + "§7)!");
							
							try {
								Main.ars.save(new File(Main.folder + File.separator + "arenas.yml"));
							} catch (IOException e) {
								e.printStackTrace();
							}
							
							return true;
						} else {
							Main.ars.set("arenas." + args[1] + ".tspawns.x", p.getLocation().getBlockX());
							Main.ars.set("arenas." + args[1] + ".tspawns.y", p.getLocation().getBlockY());
							Main.ars.set("arenas." + args[1] + ".tspawns.z", p.getLocation().getBlockZ());
							
							p.sendMessage(Main.prf() + "Новый спавн для §4T§7 на коорд. (§5" + p.getLocation().getBlockX() + "§7, §5" + p.getLocation().getBlockY() + "§7, §5" + p.getLocation().getBlockZ() + "§7)!");
							
							try {
								Main.ars.save(new File(Main.folder + File.separator + "arenas.yml"));
							} catch (IOException e) {
 
								e.printStackTrace();
							}
							
							return true;
						}
					} else if (args[0].equalsIgnoreCase("addCTspawn") && Main.ars.contains("arenas." + args[1]) && !Main.ars.contains("arenas." + args[1] + ".fin")) {
						if (Main.ars.contains("arenas." + args[1] + ".ctspawns")) {
							final ConfigurationSection cs = Main.ars.getConfigurationSection("arenas." + args[1] + ".ctspawns");
							cs.set("x", cs.getString("x") + ':' + p.getLocation().getBlockX());
							cs.set("y", cs.getString("y") + ':' + p.getLocation().getBlockY());
							cs.set("z", cs.getString("z") + ':' + p.getLocation().getBlockZ());
							
							p.sendMessage(Main.prf() + "Новый спавн для §3СT§7 на коорд. (§5" + p.getLocation().getBlockX() + "§7, §5" + p.getLocation().getBlockY() + "§7, §5" + p.getLocation().getBlockZ() + "§7)!");
							
							try {
								Main.ars.save(new File(Main.folder + File.separator + "arenas.yml"));
							} catch (IOException e) {
								e.printStackTrace();
							}
							
							return true;
						} else {
							Main.ars.set("arenas." + args[1] + ".ctspawns.x", p.getLocation().getBlockX());
							Main.ars.set("arenas." + args[1] + ".ctspawns.y", p.getLocation().getBlockY());
							Main.ars.set("arenas." + args[1] + ".ctspawns.z", p.getLocation().getBlockZ());
							
							p.sendMessage(Main.prf() + "Новый спавн для §3СT§7 на коорд. (§5" + p.getLocation().getBlockX() + "§7, §5" + p.getLocation().getBlockY() + "§7, §5" + p.getLocation().getBlockZ() + "§7)!");
							
							try {
								Main.ars.save(new File(Main.folder + File.separator + "arenas.yml"));
							} catch (IOException e) {
								e.printStackTrace();
							}
							
							return true;
						}
					} else if (args[0].equalsIgnoreCase("setAsite") && Main.ars.contains("arenas." + args[1]) && !Main.ars.contains("arenas." + args[1] + ".fin")) {
						Main.ars.set("arenas." + args[1] + ".asite.x", p.getLocation().getBlockX());
						Main.ars.set("arenas." + args[1] + ".asite.y", p.getLocation().getBlockY());
						Main.ars.set("arenas." + args[1] + ".asite.z", p.getLocation().getBlockZ());
						
						p.sendMessage(Main.prf() + "Точка §5A §7поставлена на координатах (§5" + p.getLocation().getBlockX() + "§7, §5" + p.getLocation().getBlockY() + "§7, §5" + p.getLocation().getBlockZ() + "§7)!");
						
						try {
							Main.ars.save(new File(Main.folder + File.separator + "arenas.yml"));
						} catch (IOException e) {
							e.printStackTrace();
						}
						
						return true;
						//окончание разработки карты	
					} else if (args[0].equalsIgnoreCase("setBsite") && Main.ars.contains("arenas." + args[1]) && !Main.ars.contains("arenas." + args[1] + ".fin")) {
						Main.ars.set("arenas." + args[1] + ".bsite.x", p.getLocation().getBlockX());
						Main.ars.set("arenas." + args[1] + ".bsite.y", p.getLocation().getBlockY());
						Main.ars.set("arenas." + args[1] + ".bsite.z", p.getLocation().getBlockZ());
						
						p.sendMessage(Main.prf() + "Точка §5B §7поставлена на координатах (§5" + p.getLocation().getBlockX() + "§7, §5" + p.getLocation().getBlockY() + "§7, §5" + p.getLocation().getBlockZ() + "§7)!");
						
						try {
							Main.ars.save(new File(Main.folder + File.separator + "arenas.yml"));
						} catch (IOException e) {
							e.printStackTrace();
						}
						
						return true;
						//окончание разработки карты	
					} else if (args[0].equalsIgnoreCase("finish") && Main.ars.contains("arenas." + args[1]) && !Main.ars.contains("arenas." + args[1] + ".fin")) {
						final ConfigurationSection cs = Main.ars.getConfigurationSection("arenas." + args[1]);
						if (cs.contains("tspawns") && cs.contains("ctspawns") && cs.contains("asite") && cs.contains("bsite")) {
							cs.set("fin", 1);
							Main.nnactvarns.put(args[1], new Setup(cs));
							p.sendMessage(Main.prf() + "Карта §5" + args[1] + " §7успешно создана!");
							try {
								Main.ars.save(new File(Main.folder + File.separator + "arenas.yml"));
							} catch (IOException e) {
								e.printStackTrace();
							}
							return true;
						} else {
							p.sendMessage(Main.prf() + "§cСоздайте спавны для обоих §dкомманд§c, и точки §5A §cи §5B §c!");
							return true;
						}
						//удаление карты
					} else if (args[0].equalsIgnoreCase("delete") && Main.ars.contains("arenas." + args[1])) {
						Main.ars.set("arenas." + args[1], null);
						Main.nnactvarns.remove(args[1]);
						p.sendMessage(Main.prf() + "Карта §5" + args[1] + "§7 успешно удалена!");
						try {
							Main.ars.save(new File(Main.folder + File.separator + "arenas.yml"));
						} catch (IOException e) {
							e.printStackTrace();
						}
						return true;
					} else if (!args[0].equalsIgnoreCase("join")){
						p.sendMessage(Main.prf() + "§cНеправельный синтакс комманды, все комманды - §5/cs help");
						return true;
					}
					//установка лобби
				} else if (args.length == 1) {
					if (args[0].equalsIgnoreCase("setlobby")) {
						final Location loc = p.getLocation();
						Main.lobby = new BaseBlockPosition(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
						Main.ars.set("lobby.world", loc.getWorld().getName());
						Main.ars.set("lobby.x", loc.getBlockX());
						Main.ars.set("lobby.y", loc.getBlockY());
						Main.ars.set("lobby.z", loc.getBlockZ());
						p.sendMessage(Main.prf() + "Точка лобби сохранена на " + 
								"(§5" + loc.getBlockX() + "§7, §5" +loc.getBlockY() + "§7, §5" + loc.getBlockZ() + "§7)!");
						try {
							Main.ars.save(new File(Main.folder + File.separator + "arenas.yml"));
						} catch (IOException e) {
							e.printStackTrace();
						}
						return true;
						//перезапуск конфига
					} else if (args[0].equalsIgnoreCase("reload")) {
						Main.plug.loadConfigs();
						p.sendMessage(Main.prf() + "Конфиги плагина успешно перезагружены!");
						return true;
					} else if (!args[0].equalsIgnoreCase("team") && !args[0].equalsIgnoreCase("join") && !args[0].equalsIgnoreCase("leave") && !args[0].equalsIgnoreCase("help")) {
						p.sendMessage(Main.prf() + "§cНеправельный синтакс комманды, все комманды - §5/cs help");
						return true;
					}
				} else {
					p.sendMessage(Main.prf() + "§cНеправельный синтакс " + args.length + " комманды, все комманды - §5/cs help");
					return true;
				}
			}
			
			//общие комманды
			if (Main.ars.contains("lobby")) {
				//добавление на карту
				if (args.length == 2) {
					if (args[0].equalsIgnoreCase("join")) {
						final Shooter sh = Shooter.getShooter(p.getName());
						if (sh.arena == null) {
							final Arena ar = Main.actvarns.get(args[1]);
							if (ar == null) {
								if (Main.nnactvarns.containsKey(args[1])) {
									partyJoinMap(sh, p, Main.plug.crtArena(args[1]));
									return true;
								}
								p.sendMessage(Main.prf() + "§cТакой карты не существует!");
								return false;
							} else {
								partyJoinMap(sh, p, ar);
								return true;
							}
						} else {
							p.sendMessage(Main.prf() + "§cВы уже на карте, используйте §d/cs leave§c для выхода!");
							return false;
						}
					}
				} else if (args.length == 1) {
					if (args[0].equalsIgnoreCase("join")) {
						final Shooter sh = Shooter.getShooter(p.getName());
						if (sh.arena == null) {
							final Arena ar = biggestArena();
							if (ar == null) {
								if (Main.nnactvarns.size() > 0) {
									partyJoinMap(sh, p, Main.plug.crtArena((String) Main.rndElmt(Main.nnactvarns.keySet().toArray())));
									return true;
								} else {
									p.sendMessage(Main.prf() + ChatColor.RED + "Ни одной карты еще не создано!");
									return true;
								}
							} else {
								partyJoinMap(sh, p, ar);
								return true;
							}
						} else {
							p.sendMessage(Main.prf() + "§cВы уже на карте, используйте §d/cs leave§c для выхода!");
							return true;
						}
						//помощь
					} else if (args[0].equalsIgnoreCase("leave")) {
						final Arena ar = Shooter.getShooter(p.getName()).arena;
						if (ar == null) {
							p.sendMessage(Main.prf() + "§cВы не находитесь в игре!");
							return true;
						} else {
							ar.rmvPl(Shooter.getShooter(p.getName()));
							return true;
						}
						//помощь
					} else if (args[0].equalsIgnoreCase("help")) {
						if (p.hasPermission("ostrov.builder")) {
							p.sendMessage("§5-=-=-=-=-=-=-=-=-=-=-=-=-=-=-\n"
							+ "§dПомощь по коммандам:\n"
							+ "§d/cs join (название) §7- присоединится к игре\n"
							+ "§d/cs leave §7- выход из игры\n"
							+ "§d/cs help §7- этот текст\n"
							+ "§d/cs create (название) [мин.] [макс.] [ранд.] §7- создание карты\n"
							+ "§d/cs addTspawn (название) §7- добавить спавн §5T §7на карту\n"
							+ "§d/cs addCTspawn (название) §7- добавить спавн §5CT §7на карту\n"
							+ "§d/cs setAsite (название) §7- установить точку §5А §7на карте\n"
							+ "§d/cs setBsite (название) §7- установить точку §5B §7на карте\n"
							+ "§d/cs finish (название) §7- окончание разработки карты\n"
							+ "§d/cs delete (название) §7- удвление карты\n"
							+ "§d/cs setlobby §7- установка лобби\n"
							+ "§d/cs reload §7- перезагрузка конфигов\n"
							+ "§5-=-=-=-=-=-=-=-=-=-=-=-=-=-=-");
							return true;
						}
						p.sendMessage("§5-=-=-=-=-=-=-=-=-=-=-=-=-=-=-\n"
						+ "§dПомощь по коммандам:\n"
						+ "§d/cs join (название) §7- присоединится к игре\n"
						+ "§d/cs leave §7- выход из игры\n"
						+ "§d/cs help §7- этот текст\n"
						+ "§5-=-=-=-=-=-=-=-=-=-=-=-=-=-=-");
						return true;
					}
				}
			} else {
				p.sendMessage(Main.prf() + "§cСначала поставте точку лобби с помощью §9/cs setlobby");
				return true;
			}
		}
		return true;
	}

	public static void partyJoinMap(final Shooter sh, final Player p, final Arena ar) {
		if (ApiOstrov.hasParty(p) && ApiOstrov.isPartyLeader(p)) {
			for (final String s : ApiOstrov.getPartyPlayers(p)) {
				final Player pl = Bukkit.getPlayer(s);
				if (pl != null && pl.getEntityId() != p.getEntityId()) {
					pl.sendMessage(Main.prf() + "Лидер вашей компании запшел на карту §d" + ar.name + "§7!");
					partyJoinMap(Shooter.getShooter(pl.getName()), pl, ar);
				}
			}
		}
		ar.addPl(sh);
	}

	//арена на которой больше всего игроков
	public static Arena biggestArena() {
		Arena ret = null;
		
		for (final Arena ar : Main.actvarns.values()) {
			ret = ret == null ? ar : (ar.shtrs.size() > ret.shtrs.size() ? ar : ret);
		}
		
		return ret;
	}

}
