package me.Romindous.CounterStrike;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import me.Romindous.CounterStrike.Commands.CSCmd;
import me.Romindous.CounterStrike.Enums.GunType;
import me.Romindous.CounterStrike.Game.Arena;
import me.Romindous.CounterStrike.Game.Defusal;
import me.Romindous.CounterStrike.Game.Gungame;
import me.Romindous.CounterStrike.Game.Invasion;
import me.Romindous.CounterStrike.Listeners.DmgLis;
import me.Romindous.CounterStrike.Listeners.InterrLis;
import me.Romindous.CounterStrike.Listeners.InventLis;
import me.Romindous.CounterStrike.Listeners.MainLis;
import me.Romindous.CounterStrike.Objects.Shooter;
import me.Romindous.CounterStrike.Objects.Bots.BotType;
import me.Romindous.CounterStrike.Objects.Game.GameState;
import me.Romindous.CounterStrike.Objects.Game.GameType;
import me.Romindous.CounterStrike.Objects.Game.Nade;
import me.Romindous.CounterStrike.Objects.Game.PlShooter;
import me.Romindous.CounterStrike.Objects.Loc.Spot;
import me.Romindous.CounterStrike.Objects.Loc.WXYZ;
import me.Romindous.CounterStrike.Objects.Map.MapBuilder;
import me.Romindous.CounterStrike.Objects.Map.Setup;
import me.Romindous.CounterStrike.Utils.Inventories;
import me.Romindous.CounterStrike.Utils.PacketUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.minecraft.EnumChatFormat;
import net.minecraft.core.BaseBlockPosition;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy;
import net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata;
import net.minecraft.network.protocol.game.PacketPlayOutSpawnEntity;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.network.PlayerConnection;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.decoration.EntityArmorStand;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Ostrov;
import ru.komiss77.enums.Stat;
import ru.komiss77.modules.world.WorldManager;
import ru.komiss77.modules.world.WorldManager.Generator;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.version.IServer;
import ru.komiss77.version.VM;
 
public final class Main extends JavaPlugin implements Listener {
	
	public static Main plug;
	public static File folder;
	public static World lbbyW;
	public static DedicatedServer ds;
	public static BaseBlockPosition lobby;
	public static ScoreboardManager smg;
	public static YamlConfiguration ars;
	public static YamlConfiguration config;
	public static final SecureRandom srnd = new SecureRandom();
	public static boolean maxSQL = true;
    public static final LivingEntity[] a = new LivingEntity[0];
	public static final HashMap<UUID, LivingEntity[]> wlents = new HashMap<>();
	public static ItemStack cp;
	public static ItemStack bmb;
	public static ItemStack air;
	public static ItemStack thlmt;
	public static ItemStack cthlmt;
	
	public static final HashMap<String, Arena> actvarns = new HashMap<>();
	public static final HashMap<String, Setup> nnactvarns = new HashMap<>();
	public static final HashMap<String, PlShooter> shtrs = new HashMap<>();

	public static final HashSet<WXYZ> ckracks = new HashSet<>();
	public static final ArrayList<WXYZ> ndBlks = new ArrayList<>();
	public static final HashMap<Player, BaseBlockPosition> plnts = new HashMap<>();
	public static final HashMap<String, MapBuilder> mapBlds = new HashMap<>();
	public static final ArrayList<Nade> nades = new ArrayList<>();
	public static final ArrayList<WXYZ> decoys = new ArrayList<>();
	
	public void onEnable() {
		
		plug = this;
		folder = getDataFolder();
		smg = getServer().getScoreboardManager();
		//PacketUtils.v = getServer().getClass().getPackage().getName().split("\\.")[3];
        
        try {
            ds = (DedicatedServer) getServer().getClass().getMethod("getServer").invoke(getServer());
        } catch (IllegalAccessException|InvocationTargetException|NoSuchMethodException e) {
            e.printStackTrace();
        }
		
		loadConfigs();
		if (maxSQL) {return;}
		getServer().getPluginManager().registerEvents(new DmgLis(), this);
		getServer().getPluginManager().registerEvents(new InterrLis(), this);
		getServer().getPluginManager().registerEvents(new MainLis(), this);
		getServer().getPluginManager().registerEvents(new InventLis(), this);
		
		getCommand("cs").setExecutor(new CSCmd());
		
     
		//game stuff (5 tick)
		new BukkitRunnable() {
			public void run() {
	
	            final Iterator<WXYZ> ci = Main.ckracks.iterator();
	            while (ci.hasNext()) {
	            	final WXYZ lc = ci.next();
	                if (((lc.pitch = (lc.pitch >> 3) - 1 << 3 ^ lc.pitch & 0x7) >> 3) == 0) {
	                	ci.remove();
	                }
	            }
	            
	            final Iterator<Entry<Player, BaseBlockPosition>> pi = Main.plnts.entrySet().iterator();
	            while (pi.hasNext()) {
	            	final Entry<Player, BaseBlockPosition> e = pi.next();
	            	final Location loc = e.getKey().getLocation();
	            	if (e.getKey().getInventory().getHeldItemSlot() == 7 && loc.getBlockX() == e.getValue().u() && loc.getBlockY() == e.getValue().v() && loc.getBlockZ() == e.getValue().w()) {
	            		continue;
	            	}
	            	PacketUtils.sendAcBr(e.getKey(), "§c§lВы вышли из режима установки", 20);
	            	e.getKey().getInventory().setItem(7, Main.mkItm(Material.GOLDEN_APPLE, "§4§lС*4 §c\u926e", 10, "§dПКМ §7- Заложить бомбу", "§7Можно установить на точку §5A §7или §5B"));
	            	pi.remove();
	            } 
	           
	            final Iterator<WXYZ> nbi = Main.ndBlks.iterator();
	            while (nbi.hasNext()) {
	            	final WXYZ bl = nbi.next();
	            	if ((bl.pitch--) == 0) {
	            		bl.getBlock().setType(Material.AIR, false);
	            		nbi.remove();
	            		continue;
	            	}
	            } 
	
	            final Iterator<Nade> ni = Main.nades.iterator();
	            while (ni.hasNext()) {
	            	final Nade nd = ni.next();
	            	if (nd.prj.isValid()) {
	            		if ((nd.tm--) != 0) {
	            			continue;
	            		}
	            		Nade.expld(nd.prj, (Player) nd.prj.getShooter());
	            	}
	            	ni.remove();
	            } 
			}
		}.runTaskTimer(this, 5L, 5L);
		
		//shooting mechanics (1 tick)
		new BukkitRunnable() {
			public void run() {
				
				final Iterator<WXYZ> li = Main.decoys.iterator();
	            while (li.hasNext()) {
	            	final WXYZ loc = li.next();
	            	loc.w.spawnParticle(Particle.SOUL, loc.getCenterLoc(), 2, 0.2D, 0.2D, 0.2D, 0.0D, null, false);
	            	final GunType gt = GunType.values()[loc.yaw];
	            	if ((loc.pitch & 31) < 16 && loc.pitch % gt.cld == 0) {
	            		Main.plyWrldSht(loc.getCenterLoc(), gt.snd);
	            	}
	                if ((loc.pitch--) == 0) {
	                	li.remove();
	                }
	            }
	            
				for (final Entry<UUID, LivingEntity[]> we : Main.wlents.entrySet()) {
					we.setValue(Bukkit.getWorld(we.getKey()).getLivingEntities().toArray(a));
				}
			}
		}.runTaskTimer(this, 1L, 1L);
   	}

	public void onDisable() {
	}

	@SuppressWarnings("deprecation")
	public void loadConfigs() {
		try {
			File file = new File(getDataFolder() + File.separator + "config.yml");
	        if (!file.exists() || !getConfig().contains("spawns")) {
	        	getServer().getConsoleSender().sendMessage("Config for WarZone not found, creating a new one...");
	    		getConfig().options().copyDefaults(true);
	    		getConfig().save(file);
	        }
	        //значения из конфига
	        //tbl = getConfig().getString("setup.table");
	        //арены
	        file = new File(getDataFolder() + File.separator + "arenas.yml");
	        file.createNewFile();
	        ars = YamlConfiguration.loadConfiguration(file);
	        
	    	cp = new ItemStack(Material.CARVED_PUMPKIN);
	    	air = new ItemStack(Material.AIR);
	    	bmb = Main.mkItm(Material.GOLDEN_APPLE, "§4§lС*4 §c\u926e", 10, "§dПКМ §7- Заложить бомбу", "§7Можно установить на точку §5A §7или §5B");
	    	thlmt = new ItemStack(Material.LEATHER_HELMET);
			final LeatherArmorMeta hm = (LeatherArmorMeta) thlmt.getItemMeta();
			hm.setColor(Color.RED);
			hm.displayName(Component.text("§cШапка Террориста §f\u9267"));
			hm.setLore(Arrays.asList("§7Цена: §d" + GunType.hlmtPrc + " §6⛃"));
			thlmt.setItemMeta(hm);
	    	cthlmt = new ItemStack(Material.LEATHER_HELMET);
			final LeatherArmorMeta chm = (LeatherArmorMeta) cthlmt.getItemMeta();
			chm.setColor(Color.TEAL);
			chm.displayName(Component.text("§3Шлем Спецназа §f\u9267"));
			chm.setLore(Arrays.asList("§7Цена: §d" + GunType.hlmtPrc + " §6⛃"));
			cthlmt.setItemMeta(chm);
	        
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
								WorldManager.load(getServer().getConsoleSender(), wn, Environment.NORMAL, Generator.Empty);
							}
						}
						ApiOstrov.sendArenaData(stp.nm, ru.komiss77.enums.GameState.ОЖИДАНИЕ, "§7[§5CS§7]", "§8Не Выбрано", " ", "§7Игроков: §50§7/§5" + ars.getString("arenas." + s + ".min"), "", 0);
						nnactvarns.put(stp.nm, stp);
					}
				}
			}
	        
	        if (ars.contains("lobby")) {
	        	lbbyW = getServer().getWorld(ars.getString("lobby.world"));
	        	lobby = new BaseBlockPosition(ars.getInt("lobby.x"), ars.getInt("lobby.y"), ars.getInt("lobby.z"));
	        }
	        
	        wlents.clear();
	        for (final World w : getServer().getWorlds()) {
	        	wlents.put(w.getUID(), w.getLivingEntities().toArray(a));
	        }
	        
	        Inventories.fillGmInv();
	        Inventories.fillLbbInv();
	        Inventories.fillTsInv();
	        Inventories.fillCTsInv();
	        BotType.values();
        }
        catch (IOException | NullPointerException e) {
        	e.printStackTrace();
            return;
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
		if (Main.lobby != null) {
			p.teleport(getNrLoc(Main.lobby, lbbyW));
		}
		p.getInventory().setItem(2, Main.mkItm(Material.CAMPFIRE, "§dВыбор Игры", 10));
		p.getInventory().setItem(5, Main.mkItm(Material.TOTEM_OF_UNDYING, "§eВыбор Обшивки", 10));
		p.getInventory().setItem(6, Main.mkItm(Material.GHAST_TEAR, "§5Магазин", 10));
		p.getInventory().setItem(8, Main.mkItm(Material.MAGMA_CREAM, "§4Выход в Лобби", 10));
		p.getInventory().setHeldItemSlot(0);
		lobbyScore(p);
		final byte n = MainLis.getPlaying();
		for (final Player pl : Bukkit.getOnlinePlayers()) {
			pl.sendPlayerListFooter(Component.text("§7Сейчас в игре: §d" + String.valueOf(n) + "§7 человек!"));
			if (p.getWorld().getName().equals(pl.getWorld().getName())) {
				pl.showPlayer(Main.plug, p);
				p.showPlayer(Main.plug, pl);
			} else {
				pl.hidePlayer(Main.plug, p);
				p.hidePlayer(Main.plug, pl);
			}
		}
		for (final PlShooter sho : shtrs.values()) {
			if (sho.arena() == null) {
				PacketUtils.sendNmTg(sho, "§7<§5ЛОББИ§7> ", " §7[-.-]", EnumChatFormat.h);
			}
		}
		for (final Arena ar : Main.actvarns.values()) {
			if (ar.gst == GameState.WAITING) {
				for (final Entry<Shooter, Arena.Team> e : ar.shtrs.entrySet()) {
					PacketUtils.sendNmTg(e.getKey(), "§7<§d" + ar.name + "§7> ", " §7[-.-]", e.getValue().clr);
				}
			}
		}
	}
   
	public static void lobbyScore(final Player p) {
		final Scoreboard sb = Main.smg.getNewScoreboard();
		final Objective ob = sb.registerNewObjective("CS:GO", "", Component.text("§7[§5CS:GO§7]"));
		ob.setDisplaySlot(DisplaySlot.SIDEBAR);
		ob.getScore("    ")
		.setScore(10);
		ob.getScore("§7Карта: §dЛОББИ")
		.setScore(9);
		ob.getScore("   ")
		.setScore(8);
		ob.getScore("§5Игр §7сыграно: " + ApiOstrov.getStat(p, Stat.CS_game))
		.setScore(7);
		ob.getScore("§7Раундов, §3выйграно§7: " + ApiOstrov.getStat(p, Stat.CS_win))
		.setScore(6);
		ob.getScore("§7И §4проиграно§7: " + ApiOstrov.getStat(p, Stat.CS_loose))
		.setScore(5);
		ob.getScore("  ")
		.setScore(4);
		ob.getScore("§7Киллы / Смерти")
		.setScore(3);
		final String i = String.valueOf((float) ApiOstrov.getStat(p, Stat.CS_kill) / (float) ApiOstrov.getStat(p, Stat.CS_death));
		ob.getScore("§7(§dК§7/§dД§7): " + (i.length() > 4 ? i.substring(0, 5) : i))
		.setScore(2);
		ob.getScore(" ")
		.setScore(1);
		
		ob.getScore("§e   ostrov77.su")
		.setScore(0);
		p.setScoreboard(sb);
	}
   
	public Main getPlug() {
		return this;
	}
	
	public static void dmgArm(final Player p, final Location loc, final String nm) {
		final EntityArmorStand arm = new EntityArmorStand(EntityTypes.d, PacketUtils.getNMSWrld(loc.getWorld()));
		arm.j(true);//Invis
		arm.e(true);//noGrav
		arm.m(true);//Invuln
		arm.t(true);//marker
		arm.b(IChatBaseComponent.a(nm));//name
		arm.n(true);
		arm.setPosRaw(loc.getX() + srnd.nextDouble() - 0.5D, loc.getY() + srnd.nextDouble() - 0.5D, loc.getZ() + srnd.nextDouble() - 0.5D, false);

		if (!p.isValid()) {
			return;
		}
		final PlayerConnection pc = PacketUtils.getNMSPl(p).b;
		pc.a(new PacketPlayOutSpawnEntity(arm));
		pc.a(new PacketPlayOutEntityMetadata(arm.ae(), arm.ai(), true));
		
		new BukkitRunnable() {
			public void run() {
				pc.a(new PacketPlayOutEntityDestroy(arm.ae()));
			}
		}.runTaskLater(Main.plug, 20L);
   }
   
   public static ItemStack mkItm(final Material mt, final String nm, final int mdl, final String... lr) {
	   return new ItemBuilder(mt).name(nm).setModelData(mdl).lore(Arrays.asList(lr)).build();
	   /*final ItemStack it = new ItemStack(mt);
	   final ItemMeta im = it.getItemMeta();
	   im.displayName(Component.text(nm));
       im.setCustomModelData(Integer.valueOf(mdl));
       im.setLore(Arrays.asList(lr));
	   it.setItemMeta(im);
	   return it;*/
   }
   
   public static boolean isHdMat(final ItemStack it, final Material mt) {
	   return (it != null && it.getType() == mt);
   }
   
   public static void setDmg(final ItemStack it, final int d) {
	   final Damageable dg = (Damageable) it.getItemMeta();
       dg.setDamage(it.getType().getMaxDurability() - d);
       it.setItemMeta((ItemMeta) dg);
   }
   
   public static String prf() {
       return "§7[§5CS§7] ";
   }

	public static void nrmlzPl(final Player p, final boolean clrinv) {
		p.setFireTicks(0);
		for (final PotionEffect ef : p.getActivePotionEffects()) {
	        p.removePotionEffect(ef.getType());
		}
		p.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20);
		p.setHealth(20);
		p.setExp(0);
		p.setLevel(0);
		p.setGameMode(GameMode.SURVIVAL);
		p.closeInventory();
		if (clrinv) {
			p.getInventory().clear();
		}
	}
	
	public static void crtSbdTm(final Scoreboard sb, final String nm, final String prf, final String val, final String sfx) {
		final Team tm = sb.registerNewTeam(nm);
		tm.addEntry(val);
		tm.prefix(Component.text(prf));
		tm.suffix(Component.text(sfx));
	}
	
	public static void chgSbdTm(final Scoreboard sb, final String nm, final String prf, final String sfx) {
		final Team tm = sb.getTeam(nm);
		if (tm == null) {
			plug.getLogger().info("Team " + nm + " is null");
		} else {
			tm.prefix(Component.text(prf));
			tm.suffix(Component.text(sfx));
		}
	}

	public Arena crtArena(final String nm, final GameType gt) {
		final Setup stp = nnactvarns.get(nm);
		final String wnm = stp.worlds.get(gt);
		final World w = wnm == null ? null : (getServer().getWorld(wnm) == null ? WorldManager.load(
			getServer().getConsoleSender(), wnm, Environment.NORMAL, Generator.Empty) : getServer().getWorld(wnm));
		if (w == null) return null;
		final Arena ar;
		if (stp.rndM) {
			final MapBuilder mb = new MapBuilder(nm);
			mb.setType(gt);
			
			mb.build(new Location(w, 0d, 11d, 0d));
			switch (gt) {
			case DEFUSAL:
			default:
				ar = new Defusal(stp.nm, stp.min, stp.max, mb.getCTSpawns(), mb.getTSpawns(), w, mb.getASite(), mb.getBSite(), 
					new Spot[0], (byte) 6, true, false);
				actvarns.put(nm, (Defusal) ar);
				break;
			case INVASION:
				ar = new Invasion(stp.nm, stp.min, stp.max, mb.getCTSpawns(), mb.getTSpawns(), w, 
					mb.getASite(), mb.getBSite(), new Spot[0], true, false);
				actvarns.put(nm, (Invasion) ar);
				break;
			case GUNGAME:
				ar = new Gungame(stp.nm, stp.min, stp.max, mb.getCTSpawns(), mb.getTSpawns(), 
					w, new Spot[0], true, false);
				actvarns.put(nm, (Gungame) ar);
				break;
			}
			mapBlds.put(nm, mb);
		} else {
			switch (gt) {
			case DEFUSAL:
			default:
				ar = new Defusal(stp.nm, stp.min, stp.max, stp.tSpawns, stp.ctSpawns, w, stp.A, stp.B, stp.spots, (byte) 6, false, stp.bots);
				actvarns.put(nm, (Defusal) ar);
				break;
			case INVASION:
				ar = new Invasion(stp.nm, stp.min, stp.max, stp.tSpawns, stp.ctSpawns, w, stp.A, stp.B, stp.spots, false, stp.bots);
				actvarns.put(nm, (Invasion) ar);
				break;
			case GUNGAME:
				ar = new Gungame(stp.nm, stp.min, stp.max, stp.tSpawns, stp.ctSpawns, w, stp.spots, false, stp.bots);
				actvarns.put(nm, (Gungame) ar);
				break;
			}
		}
		return ar;
	}

	public static Location getNrLoc(final BaseBlockPosition loc, final World w) {
		return new Location(w, (Main.srnd.nextBoolean() ? -1 : 1) + loc.u() + 0.5d, loc.v() + 0.1d, (Main.srnd.nextBoolean() ? -1 : 1) + loc.w() + 0.5d);
	}

	public static Location getNrLoc(final WXYZ loc, final World w) {
		return new Location(w, (Main.srnd.nextBoolean() ? -1 : 1) + loc.x + 0.5d, loc.y + 0.1d, (Main.srnd.nextBoolean() ? -1 : 1) + loc.z + 0.5d);
	}

	public static Vector getNrVec(final BaseBlockPosition loc) {
		return new Vector((Main.srnd.nextBoolean() ? -1 : 1) + loc.u() + 0.5d, loc.v() + 0.1d, (Main.srnd.nextBoolean() ? -1 : 1) + loc.w() + 0.5d);
	}

	public static Block getBBlock(final BaseBlockPosition loc, final World w) {
		return w.getBlockAt(loc.u(), loc.v(), loc.w());
	}
	
	public static void plyWrldSht(final Location org, final String snd) {
		Ostrov.async(() -> {
			for (final Player p : org.getWorld().getPlayers()) {
				final float d = (float) p.getLocation().distanceSquared(org);
				p.playSound(org, snd, d < 1f ? 1f : 400f / d, 1f);
			}
		});
	}
	
	public static void clsLbPls() {
		for (final Player p : lbbyW.getPlayers()) {
			p.closeInventory();
		}
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
	
	public static void shwHdPls(final Player p) {
		for (final Player pl : Bukkit.getOnlinePlayers()) {
			if (p.getWorld().getUID().equals(pl.getWorld().getUID())) {
				pl.showPlayer(Main.plug, p);
				p.showPlayer(Main.plug, pl);
			} else {
				pl.hidePlayer(Main.plug, p);
				p.hidePlayer(Main.plug, pl);
			}
		}
	}

	public static LivingEntity[] getWLnts(final UUID wnm) {
		final LivingEntity[] arr = wlents.get(wnm);
		return arr == null ? a : arr;
	}

	public static double twoDisQuared(final BaseBlockPosition one, final BaseBlockPosition two) {
		final int dx = one.u() - two.u();
		final int dz = one.w() - two.w();
		return dx * dx + dz * dz;
	}

	public static void brdcst(final String msg) {
		Bukkit.broadcast(Component.text(msg));
	}
	
	public static boolean rayThruAir(final Location org, final Vector to, final double inc) {
		final Vector ch = org.toVector().subtract(to);
		if (ch.lengthSquared() < inc) return true;
		final Vector vec = ch.normalize().multiply(inc);
		final IServer is = VM.getNmsServer();
		final World w = org.getWorld();
		while (true) {
			to.add(vec);
			final Material mt = is.getFastMat(w, to.getBlockX(), to.getBlockY(), to.getBlockZ());
			switch (mt) {
			default:
				if (mt.isCollidable() && mt.isOccluding()) {
					if (w.getBlockAt(to.getBlockX(), to.getBlockY(), to.getBlockZ()).getBoundingBox().contains(to)) {
						return false;
					}
				}
				break;
			case POWDER_SNOW:
				return false;
			}
			if (Math.abs(to.getX() - org.getX()) < inc && Math.abs(to.getY() - org.getY()) < inc && Math.abs(to.getZ() - org.getZ()) < inc) {
				return true;
			}
		}
	}
	
	public static boolean rayThruSoft(final Location org, final Vector to, final double inc) {
		//final Vector tt = to.clone();
		final Vector ch = org.toVector().subtract(to);
		if (ch.lengthSquared() < inc) return true;
		final Vector vec = ch.normalize().multiply(inc);
		final IServer is = VM.getNmsServer();
		final World w = org.getWorld();
		while (true) {
			to.add(vec);
			switch (is.getFastMat(w, to.getBlockX(), to.getBlockY(), to.getBlockZ())) {
			default:
				if (w.getBlockAt(to.getBlockX(), to.getBlockY(), to.getBlockZ()).getBoundingBox().contains(to)) {
					return false;
				}
			case OAK_LEAVES, ACACIA_LEAVES, BIRCH_LEAVES, JUNGLE_LEAVES, 
			SPRUCE_LEAVES, DARK_OAK_LEAVES, MANGROVE_LEAVES, AZALEA_LEAVES,
			FLOWERING_AZALEA_LEAVES, 
			
			GLASS, WHITE_STAINED_GLASS, GLASS_PANE, 
			WHITE_STAINED_GLASS_PANE, DIAMOND_ORE, 
			COAL_ORE, IRON_ORE, EMERALD_ORE, 
			
			ACACIA_SLAB, BIRCH_SLAB, CRIMSON_SLAB, SPRUCE_SLAB, WARPED_SLAB, 
			DARK_OAK_SLAB, OAK_SLAB, JUNGLE_SLAB, PETRIFIED_OAK_SLAB, MANGROVE_SLAB, 
			
			ACACIA_STAIRS, BIRCH_STAIRS, CRIMSON_STAIRS, SPRUCE_STAIRS, 
			WARPED_STAIRS, DARK_OAK_STAIRS, OAK_STAIRS, JUNGLE_STAIRS, MANGROVE_STAIRS, 
			
			ACACIA_PLANKS, BIRCH_PLANKS, CRIMSON_PLANKS, SPRUCE_PLANKS, 
			WARPED_PLANKS, DARK_OAK_PLANKS, OAK_PLANKS, JUNGLE_PLANKS, MANGROVE_PLANKS, 
			
			ACACIA_TRAPDOOR, BIRCH_TRAPDOOR, CRIMSON_TRAPDOOR, DARK_OAK_TRAPDOOR, 
			JUNGLE_TRAPDOOR, MANGROVE_TRAPDOOR, OAK_TRAPDOOR, SPRUCE_TRAPDOOR, WARPED_TRAPDOOR, 
			
			ACACIA_WOOD, BIRCH_WOOD, CRIMSON_HYPHAE, SPRUCE_WOOD, 
			WARPED_HYPHAE, DARK_OAK_WOOD, OAK_WOOD, JUNGLE_WOOD, MANGROVE_WOOD, 
			
			ACACIA_LOG, BIRCH_LOG, CRIMSON_STEM, SPRUCE_LOG, 
			WARPED_STEM, DARK_OAK_LOG, OAK_LOG, JUNGLE_LOG, MANGROVE_LOG, 
			
			ACACIA_SIGN, ACACIA_WALL_SIGN, BIRCH_SIGN, BIRCH_WALL_SIGN, CRIMSON_SIGN, 
			CRIMSON_WALL_SIGN, SPRUCE_SIGN, SPRUCE_WALL_SIGN, WARPED_SIGN, 
			WARPED_WALL_SIGN, DARK_OAK_SIGN, DARK_OAK_WALL_SIGN, OAK_SIGN, 
			OAK_WALL_SIGN, JUNGLE_SIGN, JUNGLE_WALL_SIGN, MANGROVE_SIGN, MANGROVE_WALL_SIGN, 
			
			STRIPPED_ACACIA_WOOD, STRIPPED_BIRCH_WOOD, STRIPPED_CRIMSON_HYPHAE, STRIPPED_SPRUCE_WOOD, 
			STRIPPED_WARPED_HYPHAE, STRIPPED_DARK_OAK_WOOD, STRIPPED_OAK_WOOD, STRIPPED_JUNGLE_WOOD, 
			STRIPPED_MANGROVE_WOOD, 
			
			STRIPPED_ACACIA_LOG, STRIPPED_BIRCH_LOG, STRIPPED_CRIMSON_STEM, STRIPPED_SPRUCE_LOG, 
			STRIPPED_WARPED_STEM, STRIPPED_DARK_OAK_LOG, STRIPPED_OAK_LOG, STRIPPED_JUNGLE_LOG, 
			STRIPPED_MANGROVE_LOG, 
			
			ACACIA_FENCE, BIRCH_FENCE, CRIMSON_FENCE, SPRUCE_FENCE, WARPED_FENCE, DARK_OAK_FENCE, 
			OAK_FENCE, JUNGLE_FENCE, MANGROVE_FENCE, ACACIA_FENCE_GATE, BIRCH_FENCE_GATE, CRIMSON_FENCE_GATE, 
			SPRUCE_FENCE_GATE, WARPED_FENCE_GATE, DARK_OAK_FENCE_GATE, OAK_FENCE_GATE, JUNGLE_FENCE_GATE, MANGROVE_FENCE_GATE,
			
			OAK_DOOR, ACACIA_DOOR, BIRCH_DOOR, CRIMSON_DOOR, DARK_OAK_DOOR, 
			JUNGLE_DOOR, MANGROVE_DOOR, WARPED_DOOR, SPRUCE_DOOR, 
			
			BARREL, BEEHIVE, BEE_NEST, NOTE_BLOCK, JUKEBOX, CRAFTING_TABLE, 
			
			AIR, CAVE_AIR, VOID_AIR, 
			
			SEAGRASS, TALL_SEAGRASS, WEEPING_VINES, TWISTING_VINES, 
			
			BLACK_CARPET, BLUE_CARPET, BROWN_CARPET, CYAN_CARPET, GRAY_CARPET, 
			GREEN_CARPET, LIGHT_BLUE_CARPET, LIGHT_GRAY_CARPET, LIME_CARPET, 
			MAGENTA_CARPET, MOSS_CARPET, ORANGE_CARPET, PINK_CARPET, 
			PURPLE_CARPET, RED_CARPET, WHITE_CARPET, YELLOW_CARPET, 
			
			WATER, IRON_BARS, CHAIN, STRUCTURE_VOID, COBWEB, SNOW, 
			POWDER_SNOW, BARRIER, TRIPWIRE, LADDER, RAIL, POWERED_RAIL, 
			DETECTOR_RAIL, ACTIVATOR_RAIL, CAMPFIRE, SOUL_CAMPFIRE:
				break;
			}
			
			if (Math.abs(to.getX() - org.getX()) < inc && Math.abs(to.getY() - org.getY()) < inc && Math.abs(to.getZ() - org.getZ()) < inc) {
				/*while (true) {
					tt.add(vec);
					final Block b = w.getBlockAt(tt.getBlockX(), tt.getBlockY(), tt.getBlockZ());
					if (b.getType().isAir()) b.setType(Material.OAK_WOOD, false);
					if (Math.abs(tt.getX() - org.getX()) < inc && Math.abs(tt.getZ() - org.getZ()) < inc) {
						break;
					}
				}*/
				return true;
			}
		}
	}

	public static <G> G rndElmt(G[] arr) {
		return arr[srnd.nextInt(arr.length)];
	}

	public static boolean eqlsCompStr(final Component c1, final Component c2) {
		return c1 instanceof TextComponent && c2 instanceof TextComponent && ((TextComponent) c1).content().equals(((TextComponent) c2).content());
	}
	
	public static int parseInt(final String n) {
		try {
			return Integer.parseInt(n);
		} catch (final NumberFormatException e) {
			return 0;
		}
	}
}