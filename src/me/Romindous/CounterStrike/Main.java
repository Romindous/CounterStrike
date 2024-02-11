package me.Romindous.CounterStrike;

import me.Romindous.CounterStrike.Commands.CSCmd;
import me.Romindous.CounterStrike.Enums.GameType;
import me.Romindous.CounterStrike.Enums.GunType;
import me.Romindous.CounterStrike.Game.Arena;
import me.Romindous.CounterStrike.Game.Defusal;
import me.Romindous.CounterStrike.Game.Gungame;
import me.Romindous.CounterStrike.Game.Invasion;
import me.Romindous.CounterStrike.Listeners.*;
import me.Romindous.CounterStrike.Objects.Game.Nade;
import me.Romindous.CounterStrike.Objects.Game.PlShooter;
import me.Romindous.CounterStrike.Objects.Map.MapBuilder;
import me.Romindous.CounterStrike.Objects.Map.Setup;
import me.Romindous.CounterStrike.Objects.Shooter;
import me.Romindous.CounterStrike.Utils.Inventories;
import me.Romindous.CounterStrike.Utils.PacketUtils;
import net.kyori.adventure.text.Component;
import net.minecraft.core.BaseBlockPosition;
import org.bukkit.*;
import org.bukkit.World.Environment;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Display;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.Vector3f;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Ostrov;
import ru.komiss77.enums.Stat;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.world.WXYZ;
import ru.komiss77.modules.world.WorldManager;
import ru.komiss77.modules.world.WorldManager.Generator;
import ru.komiss77.modules.world.XYZ;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.TCUtils;

import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.*;
import java.util.Map.Entry;
 
public final class Main extends JavaPlugin implements Listener {
	
	public static Main plug;
	public static File folder;
	public static WXYZ lobby;
	public static ScoreboardManager smg;
	public static YamlConfiguration ars;
	public static final SecureRandom srnd = new SecureRandom();
    public static final LivingEntity[] a = new LivingEntity[0];
	public static final HashMap<UUID, LivingEntity[]> wlents = new HashMap<>();
	public static ItemStack cp;
	public static ItemStack bmb;
	public static ItemStack air;
	public static ItemStack spy;
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
		
		loadConfigs();
		getServer().getPluginManager().registerEvents(new ChatLis(), this);
		getServer().getPluginManager().registerEvents(new DmgLis(), this);
		getServer().getPluginManager().registerEvents(new InterrLis(), this);
		getServer().getPluginManager().registerEvents(new MainLis(), this);
		getServer().getPluginManager().registerEvents(new InventLis(), this);
		
		getCommand("cs").setExecutor(new CSCmd());
		
		//Ostrov things
     
		//game stuff (5 tick)
		new BukkitRunnable() {
			public void run() {

                Main.ckracks.removeIf(lc -> ((lc.pitch = (lc.pitch >> 3) - 1 << 3 ^ lc.pitch & 7) >> 3) == 0);
	            
	            final Iterator<Entry<Player, BaseBlockPosition>> pi = Main.plnts.entrySet().iterator();
	            while (pi.hasNext()) {
	            	final Entry<Player, BaseBlockPosition> e = pi.next();
	            	final Location loc = e.getKey().getLocation();
	            	if (e.getKey().getInventory().getHeldItemSlot() == 7 && loc.getBlockX() == e.getValue().u() && loc.getBlockY() == e.getValue().v() && loc.getBlockZ() == e.getValue().w()) {
	            		continue;
	            	}
	            	PacketUtils.sendAcBr(e.getKey(), "§c§lВы вышли из режима установки");
	            	e.getKey().getInventory().setItem(7, Main.mkItm(Material.GOLDEN_APPLE, "§4§lС*4 §c\u926e", 10, "§dПКМ §7- Заложить бомбу", "§7Можно установить на точку §5A §7или §5B"));
	            	pi.remove();
	            } 
	           
	            final Iterator<WXYZ> nbi = Main.ndBlks.iterator();
	            while (nbi.hasNext()) {
	            	final WXYZ bl = nbi.next();
	            	if ((bl.pitch--) == 0) {
	            		bl.getBlock().setType(Material.AIR, false);
	            		nbi.remove();
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
	            		Main.plyWrldSnd(loc.getCenterLoc(), gt.snd);
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
	        
	    	cp = new ItemStack(Material.CARVED_PUMPKIN);
	    	air = new ItemStack(Material.AIR);
	    	spy = Main.mkItm(Material.SPYGLASS, "§8О.О", 10);
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
	        	final XYZ lb = XYZ.fromString(ars.getString("lobby"));
	        	lobby = new WXYZ(Bukkit.getWorld(lb.worldName) == null ? 
	        		Bukkit.getWorld("lobby") : Bukkit.getWorld(lb.worldName), lb);
	        }
	        
	        wlents.clear();
	        for (final World w : getServer().getWorlds()) {
	        	wlents.put(w.getUID(), w.getLivingEntities().toArray(a));
	        }
	        
	        Inventories.fillGmInv();
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
		if (Main.lobby != null) p.teleport(getNrLoc(Main.lobby));
		p.getInventory().setItem(2, Main.mkItm(Material.CAMPFIRE, "§dВыбор Игры", 10));
		p.getInventory().setItem(5, Main.mkItm(Material.TOTEM_OF_UNDYING, "§eВыбор Обшивки", 10));
		p.getInventory().setItem(6, Main.mkItm(Material.GHAST_TEAR, "§5Магазин", 10));
		p.getInventory().setItem(8, Main.mkItm(Material.MAGMA_CREAM, "§4Выход в Лобби", 10));
		p.getInventory().setHeldItemSlot(0);
		lobbyScore(p);
		final Component tpl = TCUtils.format("§7Сейчас в игре: §d" + MainLis.getPlaying() + "§7 человек!");
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
		sh.setTabTag("§7<§5ЛОББИ§7> ", " §7[-.-]", Arena.Team.SPEC.clr);
	}
   
	public static void lobbyScore(final Player p) {
		PM.getOplayer(p).score.getSideBar().reset().title("§7[§5CS:GO§7]")
			.add(" ")
			.add("§7Карта: §dЛОББИ")
			.add("§7=-=-=-=-=-=-=-=-")
			.add("§5Игр §7всего: " + ApiOstrov.getStat(p, Stat.CS_game))
			.add(" ")
			.add("§7Раундов:")
			.add(Arena.Team.CTs.clr + "Выйграно§7: " + ApiOstrov.getStat(p, Stat.CS_win))
			.add(Arena.Team.Ts.clr + "Проиграно§7: " + ApiOstrov.getStat(p, Stat.CS_loose))
			.add("§7=-=-=-=-=-=-=-=-")
			.add("§7(§dК§7/§dД§7): " + ApiOstrov.toSigFigs((float) ApiOstrov.getStat(p, Stat.CS_kill)
					/ (float) ApiOstrov.getStat(p, Stat.CS_death), (byte) 2))
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
			td.text(TCUtils.format(nm));
			td.setSeeThrough(true);
			td.setShadowed(true);
		});
		p.showEntity(plug, tds);
		Ostrov.sync(() -> tds.remove(), 24);
		/*final WorldServer wm = VM.getNmsServer().toNMS(loc.getWorld());
		net.minecraft.world.entity.Display.TextDisplay tds
		final int eid = tds.af();
		tds.e(true);
		tds.a(BillboardConstraints.b);
		tds.b(160);
	    byte flagBits = tds.t();//getFlags
	    flagBits = (byte) (flagBits | 1);
	    flagBits = (byte) (flagBits | 2);
	    tds.d(flagBits);
		
		tds.setPosRaw(0.5f - srnd.nextFloat() + loc.getX(), lc * 0.001f + loc.getY() - srnd.nextFloat(), 0.5f - srnd.nextFloat() + loc.getZ(), false);
		tds.c(PaperAdventure.asVanilla(TCUtils.format(nm)));
		
		final PlayerConnection pc = VM.getNmsServer().toNMS(p).c;
		pc.a(new PacketPlayOutSpawnEntity(tds));
		pc.a(new PacketPlayOutEntityMetadata(eid, tds.aj().c()));
		tds.a(RemovalReason.a);*/
   }
   
   public static ItemStack mkItm(final Material mt, final String nm, final int mdl, final String... lr) {
	   return new ItemBuilder(mt).name(nm).setModelData(mdl).addLore(Arrays.asList(lr)).build();
   }
   
   public static boolean isHdMat(final ItemStack it, final Material mt) {
	   return (it != null && it.getType() == mt);
   }
   
   public static void setDmg(final ItemStack it, final int d) {
	   final Damageable dg = (Damageable) it.getItemMeta();
       dg.setDamage(it.getType().getMaxDurability() - d);
       it.setItemMeta(dg);
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
	
	/*public static void crtSbdTm(final Scoreboard sb, final String nm, final String prf, final String val, final String sfx) {
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
	}*/
	
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
			case DEFUSAL:
			default:
				ar = new Defusal(stp.nm, stp.min, stp.max, mb.getCTSpawns(), mb.getTSpawns(), mb.getSpots(), w, mb.getASite(), mb.getBSite(), (byte) 6, true, stp.bots);
				actvarns.put(nm, ar);
				break;
			case INVASION:
				ar = new Invasion(stp.nm, stp.min, stp.max, mb.getCTSpawns(), mb.getTSpawns(), mb.getSpots(), w, mb.getASite(), mb.getBSite(), true, stp.bots);
				actvarns.put(nm, ar);
				break;
			case GUNGAME:
				ar = new Gungame(stp.nm, stp.min, stp.max, mb.getCTSpawns(), mb.getTSpawns(), mb.getSpots(), w, true, stp.bots);
				actvarns.put(nm, ar);
				break;
			}
			mapBlds.put(nm, mb);
		} else {
//			mar = new Area(nm, stp.bot, stp.top, w);
//			if (stp.bots) Ostrov.async(() -> mar.loadPos());
			switch (gt) {
			case DEFUSAL:
			default:
				ar = new Defusal(stp.nm, stp.min, stp.max, stp.tSpawns, stp.ctSpawns, stp.spots, w, stp.A, stp.B, (byte) 6, false, stp.bots);
				actvarns.put(nm, ar);
				break;
			case INVASION:
				ar = new Invasion(stp.nm, stp.min, stp.max, stp.tSpawns, stp.ctSpawns, stp.spots, w, stp.A, stp.B, false, stp.bots);
				actvarns.put(nm, ar);
				break;
			case GUNGAME:
				ar = new Gungame(stp.nm, stp.min, stp.max, stp.tSpawns, stp.ctSpawns, stp.spots, w, false, stp.bots);
				actvarns.put(nm, ar);
				break;
			}
		}
		return ar;
	}

	public static Location getNrLoc(final XYZ loc, final World w) {
		return new Location(w, (Main.srnd.nextBoolean() ? -1 : 1) + loc.x + 0.5d, loc.y + 0.1d, (Main.srnd.nextBoolean() ? -1 : 1) + loc.z + 0.5d);
	}

	public static Location getNrLoc(final Location loc) {
		return loc.add(Main.srnd.nextBoolean() ? -1d : 1d, 0.1d, Main.srnd.nextBoolean() ? -1d : 1d);
	}

	public static Location getNrLoc(final WXYZ loc) {
		return new Location(loc.w, (Main.srnd.nextBoolean() ? -1 : 1) + loc.x + 0.5d, loc.y + 0.1d, (Main.srnd.nextBoolean() ? -1 : 1) + loc.z + 0.5d);
	}

	public static Vector getNrVec(final XYZ loc) {
		return new Vector((Main.srnd.nextBoolean() ? -1 : 1) + loc.x + 0.5d, loc.y + 0.1d, (Main.srnd.nextBoolean() ? -1 : 1) + loc.z + 0.5d);
	}

	public static XYZ getNrPos(final XYZ loc) {
		return loc.clone().add(Main.srnd.nextBoolean() ? -1 : 1, 0, Main.srnd.nextBoolean() ? -1 : 1);
	}
	
	public static void plyWrldSnd(final Location org, final String snd) {
		Ostrov.async(() -> {
			for (final Player p : org.getWorld().getPlayers()) {
				final float d = (float) p.getLocation().distanceSquared(org);
				p.playSound(org, snd, d < 1f ? 1f : 400f / d, 1f);
			}
		});
	}

	public static void plyWrldSnd(final Location org, final Arena ar, final Arena.Team tm, final String snd) {
		Ostrov.async(() -> {
			for (final Entry<Shooter, Arena.Team> en : ar.shtrs.entrySet()) {
				final Player p = en.getKey().getPlayer();
				if (p == null || en.getValue() != tm) continue;
				final float d = (float) p.getLocation().distanceSquared(org);
				p.playSound(org, snd, d < 1f ? 1f : 400f / d, 1f);
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

	public static LivingEntity[] getWLnts(final UUID wnm) {
		final LivingEntity[] arr = wlents.get(wnm);
		return arr == null ? a : arr;
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