package me.Romindous.CounterStrike.Commands;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;

import me.Romindous.CounterStrike.Main;
import me.Romindous.CounterStrike.Game.Arena;
import me.Romindous.CounterStrike.Objects.Shooter;
import me.Romindous.CounterStrike.Objects.Map.MapManager;
import me.Romindous.CounterStrike.Objects.Map.Setup;
import me.Romindous.CounterStrike.Objects.Map.TypeChoose;
import ru.komiss77.ApiOstrov;
import ru.komiss77.modules.world.WXYZ;
import ru.komiss77.modules.world.XYZ;
import ru.komiss77.utils.inventory.SmartInventory;

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
					
					sugg.add("edit");
					sugg.add("setlobby");
					sugg.add("reload");
				} else if (args[0].equalsIgnoreCase("edit")) {
					for (final String s : Main.nnactvarns.keySet()) {
						sugg.add(s);
					}
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

	/**
	 *
	 */
	@Override
	public boolean onCommand(final CommandSender send, final Command cmd, final String label, final String[] args) {
		if (label.equalsIgnoreCase("cs") && send instanceof Player) {
			final Player p = (Player) send;
			//админ комманды
			if (ApiOstrov.isLocalBuilder(p, false)) {
				//создание карты
				if (args.length == 2) {
					if (args[0].equalsIgnoreCase("edit")) {
						final MapManager mm = MapManager.edits.get(p.getUniqueId());
						if (mm == null) {
							SmartInventory.builder().size(3, 9)
	                        .id("Map "+p.getName()).title("§dРедактор Карты " + args[1])
	                        .provider(new MapManager(args[1]))
	                        .build().open(p);
						} else if (mm.stp.nm.equals(args[1])) {
							SmartInventory.builder().size(3, 9)
	                        .id("Map "+p.getName()).title("§dРедактор Карты " + args[1])
	                        .provider(mm == null ? new MapManager(args[1]) : mm)
	                        .build().open(p);
						} else {
							p.sendMessage(Main.prf() + "§cВы уже редактируете карту §5" + mm.stp.nm);
							return true;
						}
						return true;
					} else if (!args[0].equalsIgnoreCase("join")){
						p.sendMessage(Main.prf() + "§cНеправельный синтакс комманды, все комманды - §5/cs help");
						return true;
					}
					//установка лобби
				} else if (args.length == 1) {
					if (args[0].equalsIgnoreCase("setlobby")) {
						Main.lobby = new WXYZ(p.getLocation());
						Main.ars.set("lobby", new XYZ(Main.lobby.getCenterLoc()).toString());
						p.sendMessage(Main.prf() + "Точка лобби сохранена на " + 
							"(§5" + Main.lobby.x + "§7, §5" + Main.lobby.y + "§7, §5" + Main.lobby.z + "§7)!");
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
					} else if (!args[0].equalsIgnoreCase("join") && !args[0].equalsIgnoreCase("leave") && !args[0].equalsIgnoreCase("help")) {
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
						final Shooter sh = Shooter.getPlShooter(p.getName(), true);
						if (sh.arena() == null) {
							final Arena ar = Main.actvarns.get(args[1]);
							if (ar == null) {
								final Setup stp = Main.nnactvarns.get(args[1]);
								if (stp == null) {
									p.sendMessage(Main.prf() + "§cТакой карты не существует!");
									return false;
								}
								SmartInventory.builder()
								.type(InventoryType.HOPPER)
		                        .id("Game "+p.getName())
		                        .provider(new TypeChoose(stp))
		                        .title("§d§l      Выбор Типа Игры")
		                        .build().open(p);
								return true;
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
						final Shooter sh = Shooter.getPlShooter(p.getName(), true);
						if (sh.arena() == null) {
							final Arena ar = biggestArena();
							if (ar == null) {
								if (Main.nnactvarns.size() > 0) {
									SmartInventory.builder()
									.type(InventoryType.HOPPER)
			                        .id("Game "+p.getName())
			                        .provider(new TypeChoose(Main.rndElmt(Main.nnactvarns.values().toArray(new Setup[0]))))
			                        .title("§d§l      Выбор Типа Игры")
			                        .build().open(p);
									return true;
								} else {
									p.sendMessage(Main.prf() + "§cНи одной карты еще не создано!");
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
						final Arena ar = Shooter.getPlShooter(p.getName(), true).arena();
						if (ar == null) {
							p.sendMessage(Main.prf() + "§cВы не находитесь в игре!");
							return true;
						} else {
							ar.rmvPl(Shooter.getPlShooter(p.getName(), true));
							return true;
						}
						//помощь
					} else if (args[0].equalsIgnoreCase("help")) {
						if (ApiOstrov.isLocalBuilder(p, false)) {
							p.sendMessage("§5-=-=-=-=-=-=-=-=-=-=-=-=-=-=-\n"
							+ "§dПомощь по коммандам:\n"
							+ "§d/cs join (название) §7- присоединится к игре\n"
							+ "§d/cs leave §7- выход из игры\n"
							+ "§d/cs help §7- этот текст\n"
							+ "§d/cs edit (название) §7- редактирование карты\n"
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
					partyJoinMap(Shooter.getPlShooter(pl.getName(), true), pl, ar);
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