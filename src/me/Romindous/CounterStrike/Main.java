package me.Romindous.CounterStrike;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.security.SecureRandom;
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
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.HumanEntity;
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
import me.Romindous.CounterStrike.Enums.GameState;
import me.Romindous.CounterStrike.Enums.GunType;
import me.Romindous.CounterStrike.Enums.TileType;
import me.Romindous.CounterStrike.Game.Arena;
import me.Romindous.CounterStrike.Game.Defusal;
import me.Romindous.CounterStrike.Game.Gungame;
import me.Romindous.CounterStrike.Game.Invasion;
import me.Romindous.CounterStrike.Listeners.DmgLis;
import me.Romindous.CounterStrike.Listeners.InterrLis;
import me.Romindous.CounterStrike.Listeners.InventLis;
import me.Romindous.CounterStrike.Listeners.MainLis;
import me.Romindous.CounterStrike.Objects.Nade;
import me.Romindous.CounterStrike.Objects.Setup;
import me.Romindous.CounterStrike.Objects.Shooter;
import me.Romindous.CounterStrike.Objects.SmplLoc;
import me.Romindous.CounterStrike.Utils.Inventories;
import me.Romindous.CounterStrike.Utils.MapBuilder;
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
import net.minecraft.world.phys.Vec3D;
import ru.komiss77.ApiOstrov;
import ru.komiss77.enums.Stat;
import ru.komiss77.modules.world.WorldManager;
import ru.komiss77.modules.world.WorldManager.Generator;
 
public final class Main extends JavaPlugin implements Listener {
	
	public static Main plug;
	public static File folder;
	public static World lbbyW;
	public static String rplnk;
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
	public static final HashMap<String, Shooter> shtrs = new HashMap<>();

	public static final HashSet<SmplLoc> ckracks = new HashSet<>();
	public static final HashMap<Block, Byte> ndBlks = new HashMap<>();
	public static final HashMap<Player, BaseBlockPosition> plnts = new HashMap<>();
	public static final HashMap<String, MapBuilder> mapBlds = new HashMap<>();
	public static final HashSet<Nade> nades = new HashSet<>();
	public static final HashSet<Location> decoys = new HashSet<>();
	
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
	
	            final Iterator<SmplLoc> ci = Main.ckracks.iterator();
	            while (ci.hasNext()) {
	            	final SmplLoc lc = ci.next();
	            	lc.cnt = (short) ((lc.cnt >> 3) - 1 << 3 ^ lc.cnt & 0x7);
	                if (lc.cnt >> 3 == 0) {
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
	            	e.getKey().getInventory().setItem(7, Main.mkItm(Material.GOLDEN_APPLE, "§4§lС*4 §c\u926e", 1, "§dПКМ §7- Заложить бомбу", "§7Можно установить на точку §5A §7или §5B"));
	            	pi.remove();
	            } 
	           
	            final Iterator<Entry<Block, Byte>> nbi = Main.ndBlks.entrySet().iterator();
	            while (nbi.hasNext()) {
	            	final Entry<Block, Byte> bl = nbi.next();
	            	final byte b = bl.getValue().byteValue();
	            	if (bl.getKey().getType().isAir()) {
	            		if (b >> 2 == 0) {
	            			switch (b & 0x1) {
	            			case 0:
	            				bl.getKey().setType(Material.FIRE, false);
	            				bl.setValue((byte) 30);
	            				continue;
	            			case 1:
	            				bl.getKey().setType(Material.POWDER_SNOW, false);
	            				bl.setValue((byte) 50);
	            				continue;
	            			default:
	            				bl.getKey().setType(Material.FIRE, false);
	            				bl.setValue((byte) 20);
	            				continue;
	            			}
	            		} 
	            		
	            		bl.setValue((byte)((b >> 2) - 1 << 2 ^ b & 0x3)); continue;
	            	} 
	            	if (b == 0) {
	            		bl.getKey().setType(Material.AIR, false);
	            		nbi.remove();
	            		continue;
	            	} 
	            	bl.setValue((byte) (b - 1));
	            } 
	
	            final Iterator<Nade> ni = Main.nades.iterator();
	            while (ni.hasNext()) {
	            	final Nade nd = ni.next();
	            	if (nd.prj.isValid()) {
	            		nd.tm = (byte)(nd.tm - 1);
	            		if (nd.tm != 0) {
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
				
				final Iterator<Location> li = Main.decoys.iterator();
	            while (li.hasNext()) {
	            	final Location loc = li.next();
	            	loc.getWorld().spawnParticle(Particle.SOUL, loc, 4, 0.4D, 0.4D, 0.4D, 0.0D, null, false);
	                loc.setYaw(loc.getYaw() - 1.0F);
	            	final GunType gt = GunType.values()[(int) loc.getPitch()];
	            	if (((int) loc.getYaw() & 31) < 16 && (int) loc.getYaw() % gt.cld == 0) {
	            		Main.plyWrldSht(loc, gt.snd);
	            	}
	                if (loc.getYaw() == 0.0F) {
	                	li.remove();
	                }
	            }
	            
				for (final Entry<UUID, LivingEntity[]> we : wlents.entrySet()) {
					we.setValue(Bukkit.getWorld(we.getKey()).getLivingEntities().toArray(a));
				}
				
				for (final Shooter sh : Main.shtrs.values()) {
					sh.pss.poll();
					sh.pss.add(vcFrmLc(sh.inv.getHolder().getLocation()));
					
					sh.cldwn = (byte)(sh.cldwn - (sh.cldwn == 0 ? 0 : 1));
					final ItemStack it = sh.inv.getItemInMainHand();
					final GunType gt = GunType.getGnTp(sh.inv.getItemInMainHand());
					if (gt == null) {
						continue;
					}
					final boolean ps = ((Damageable)it.getItemMeta()).hasDamage();
					if (ps) {
						sh.count = (short)(sh.count + 1);
						final HumanEntity p = sh.inv.getHolder();
						if ((sh.count & 0x3) == 0) {
							p.getWorld().playSound(p.getLocation(), Sound.BLOCK_CHAIN_FALL, 0.5f, 2f);
						} 
						Main.setDmg(it, it.getType().getMaxDurability() * sh.count / gt.rtm);
						if (sh.count >= gt.rtm) {
							p.getWorld().playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_NETHERITE, 1f, 2f);
							sh.inv.getItemInMainHand().setAmount(gt.amo);
							sh.count = 0;
						} 
					} 
	             
					if (sh.shtTm != 0) {
						sh.shtTm--;
						sh.count = (short)(sh.count + (ps ? 0 : 1));
						if (sh.count % gt.cld == 0) {
							final int tr = sh.count < sh.rclTm ? sh.count : sh.rclTm;
							if (it.getAmount() == 1) {
								if (ps) {
									continue;
								}
								Main.setDmg(it, 0);
								sh.count = 0;
							} else {
								if (ps) {
									Main.setDmg(it, it.getType().getMaxDurability());
									sh.count = 0;
								} 
								it.setAmount(it.getAmount() - 1);
							} 
							sh.cldwn = gt.cld;
							final Player p = (Player) sh.inv.getHolder();
							final boolean iw = (gt.snp && p.isSneaking());
							for (byte i = gt.brst == 0 ? 1 : gt.brst; i > 0; i--) {
								sh.shoot(gt, Main.this.getPlug(), p, !iw, tr);
							}
							plyWrldSht(p.getLocation(), gt.snd);
							if (iw) {
								PacketUtils.fkHlmtClnt(p, p.getInventory().getHelmet());
								PacketUtils.zoom(p, false);
								p.setSneaking(false);
							}
							//p.setVelocity(p.getVelocity().subtract(p.getEyeLocation().getDirection().multiply(gt.kb)));
						}
					} else if (sh.count != 0 && !ps) {
						sh.count = sh.count > sh.rclTm + 1 ? sh.rclTm : (short) ((sh.count - 2 < 0) ? 0 : (sh.count - 2));
					}
				}
				
				/*for (final Arena ar : Main.actvarns.values()) {
					for (final Shooter sh : ar.shtrs.keySet()) {
						sh.pss.poll();
						sh.pss.add(vcFrmLc(sh.inv.getHolder().getLocation()));
						
						sh.cld = (byte)(sh.cld - (sh.cld == 0 ? 0 : 1));
						final ItemStack it = sh.inv.getItemInMainHand();
						final GunType gt = GunType.getGnTp(sh.inv.getItemInMainHand());
						if (gt == null) {
							continue;
						}
						final boolean ps = ((Damageable)it.getItemMeta()).hasDamage();
						if (ps) {
							sh.cnt = (short)(sh.cnt + 1);
							final HumanEntity p = sh.inv.getHolder();
							if ((sh.cnt & 0x3) == 0) {
								p.getWorld().playSound(p.getLocation(), Sound.BLOCK_CHAIN_FALL, 0.5f, 2f);
							} 
							Main.setDmg(it, it.getType().getMaxDurability() * sh.cnt / gt.rtm);
							if (sh.cnt >= gt.rtm) {
								p.getWorld().playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_NETHERITE, 1f, 2f);
								sh.inv.getItemInMainHand().setAmount(gt.amo);
								sh.cnt = 0;
							} 
						} 
		             
						if (sh.stm != 0) {
							sh.stm--;
							sh.cnt = (short)(sh.cnt + (ps ? 0 : 1));
							if (sh.cnt % gt.cld == 0) {
								final short tr = sh.cnt < sh.rctm ? sh.cnt : sh.rctm;
								if (it.getAmount() == 1) {
									if (ps) {
										continue;
									}
									Main.setDmg(it, 0);
									sh.cnt = 0;
								} else {
									if (ps) {
										Main.setDmg(it, it.getType().getMaxDurability());
										sh.cnt = 0;
									} 
									it.setAmount(it.getAmount() - 1);
								} 
								sh.cld = gt.cld;
								final Player p = (Player) sh.inv.getHolder();
								final boolean iw = (gt.snp && p.isSneaking());
								for (byte i = gt.brst == 0 ? 1 : gt.brst; i > 0; i--) {
									sh.shoot(gt, Main.this.getPlug(), p, !iw, tr);
								}
								plyWrldSht(p.getLocation(), gt.snd);
								if (iw) {
									PacketUtils.fkHlmtClnt(p, p.getInventory().getHelmet());
									PacketUtils.zoom(p, false);
									p.setSneaking(false);
								}
								//p.setVelocity(p.getVelocity().subtract(p.getEyeLocation().getDirection().multiply(gt.kb)));
							}
						} else if (sh.cnt != 0 && !ps) {
							sh.cnt = sh.cnt > sh.rctm + 1 ? sh.rctm : (short) ((sh.cnt - 2 < 0) ? 0 : (sh.cnt - 2));
						}
					}
				}*/
			}
		}.runTaskTimer(this, 1L, 1L);
   	}

	public void onDisable() {
	}

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
	        
	    	rplnk = "https://download.mc-packs.net/pack/5b99bf3147118bbcfd6351455ea47c4cdaac2456.zip";
	    	cp = new ItemStack(Material.CARVED_PUMPKIN);
	    	air = new ItemStack(Material.AIR);
	    	bmb = Main.mkItm(Material.GOLDEN_APPLE, "§4§lС*4 §c\u926e", 1, "§dПКМ §7- Заложить бомбу", "§7Можно установить на точку §5A §7или §5B");
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
					if (ars.contains("arenas." + s + ".fin")) {
						WorldManager.load(getServer().getConsoleSender(), ars.getString("arenas." + s + ".world"), Environment.NORMAL, Generator.Empty);
						final String tp;
						switch (stp.gt) {
							case GUNGAME:
								tp = "§dЭстафета";
								break;
							case INVASION:
								tp = "§dВторжение";
								break;
							case DEFUSAL:
								default:
								tp = "§dКлассика";
								break;
						}
						ApiOstrov.sendArenaData(stp.nm, ru.komiss77.enums.GameState.ОЖИДАНИЕ, "§7[§5CS§7]", tp, " ", "§7Игроков: §50§7/§5" + ars.getString("arenas." + s + ".min"), "", 0);
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
        }
        catch (IOException | NullPointerException e) {
        	e.printStackTrace();
            return;
        }
	}

	public static void lobbyPl(final Player p) {
		nrmlzPl(p, true);
		final Shooter sh = Shooter.getShooter(p.getName());
		sh.arena = null;
		sh.inv = p.getInventory();
		if (p.isInsideVehicle()) {
			p.getVehicle().remove();
		}
		if (Main.lobby != null) {
			p.teleport(getNrLoc(Main.lobby, lbbyW));
		}
		p.getInventory().setItem(2, Main.mkItm(Material.CAMPFIRE, "§dВыбор Игры", 1));
		p.getInventory().setItem(6, Main.mkItm(Material.GHAST_TEAR, "§5Магазин", 1));
		p.getInventory().setItem(8, Main.mkItm(Material.MAGMA_CREAM, "§4Выход в Лобби", 1));
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
		for (final Shooter sho : shtrs.values()) {
			if (sho.arena == null) {
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
		ob.getScore("§5Игр §7сиграно: " + ApiOstrov.getStat(p, Stat.CS_game))
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
		final PlayerConnection pc = Main.ds.bh().a(p.getName()).b;
		pc.a(new PacketPlayOutSpawnEntity(arm));
		pc.a(new PacketPlayOutEntityMetadata(arm.ae(), arm.ai(), true));
		
		new BukkitRunnable() {
			public void run() {
				pc.a(new PacketPlayOutEntityDestroy(arm.ae()));
			}
		}.runTaskLater(Main.plug, 20L);
   }
   
   public static ItemStack mkItm(final Material mt, final String nm, final int mdl, final String... lr) {
	   final ItemStack it = new ItemStack(mt);
	   final ItemMeta im = it.getItemMeta();
	   im.displayName(Component.text(nm));
       im.setCustomModelData(Integer.valueOf(mdl));
       im.setLore(Arrays.asList(lr));
	   it.setItemMeta(im);
	   return it;
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

	public Arena crtArena(final String nm) {
		final Setup stp = nnactvarns.get(nm);
		final World w = getServer().getWorld(stp.wnm) == null ? WorldManager.load(getServer().getConsoleSender(), stp.wnm, Environment.NORMAL, Generator.Empty) : getServer().getWorld(stp.wnm);
		final Arena ar;
		if (stp.rndM) {
			final MapBuilder mb = new MapBuilder(nm);
			mb.setType(stp.gt);
			
			mb.build(new Location(w, 0d, 11d, 0d));
			switch (stp.gt) {
			case DEFUSAL:
			default:
				ar = new Defusal(stp.nm, stp.min, stp.max, mb.getCTSpawns(), mb.getTSpawns(), 
					w, mb.getASite(), mb.getBSite(), (byte) 6, true);
				actvarns.put(nm, (Defusal) ar);
				break;
			case INVASION:
				ar = new Invasion(stp.nm, stp.min, stp.max, mb.getCTSpawns(), mb.getTSpawns(), 
						w, mb.getASite(), mb.getBSite(), true);
				actvarns.put(nm, (Invasion) ar);
				break;
			case GUNGAME:
				ar = new Gungame(stp.nm, stp.min, stp.max, mb.getCTSpawns(), mb.getTSpawns(), w, true);
				actvarns.put(nm, (Gungame) ar);
				break;
			}
			mapBlds.put(nm, mb);
		} else {
			switch (stp.gt) {
			case DEFUSAL:
			default:
				ar = new Defusal(stp.nm, stp.min, stp.max, stp.tSpawns, stp.ctSpawns, w, stp.A, stp.B, (byte) 6, false);
				actvarns.put(nm, (Defusal) ar);
				break;
			case INVASION:
				ar = new Invasion(stp.nm, stp.min, stp.max, stp.tSpawns, stp.ctSpawns, w, stp.A, stp.B, false);
				actvarns.put(nm, (Invasion) ar);
				break;
			case GUNGAME:
				ar = new Gungame(stp.nm, stp.min, stp.max, stp.tSpawns, stp.ctSpawns, w, false);
				actvarns.put(nm, (Gungame) ar);
				break;
			}
		}
		return ar;
	}

	public static Location getNrLoc(final BaseBlockPosition loc, final World w) {
		return new Location(w, (Main.srnd.nextBoolean() ? -1 : 1) + loc.u() + 0.5d, loc.v() + 0.1d, (Main.srnd.nextBoolean() ? -1 : 1) + loc.w() + 0.5d);
	}

	public static Block getBBlock(final BaseBlockPosition loc, final World w) {
		return w.getBlockAt(loc.u(), loc.v(), loc.w());
	}
	
	public static void plyWrldSht(final Location org, final String snd) {
		new BukkitRunnable() {
			@Override
			public void run() {
				for (final Player p : org.getWorld().getPlayers()) {
					final float d = (float) p.getLocation().distanceSquared(org);
					p.playSound(org, snd, d < 1f ? 1f : 400f / d, 1f);
				}
			}
		}.runTaskAsynchronously(plug);
	}
	
	public static void clsLbPls() {
		for (final Player p : lbbyW.getPlayers()) {
			p.closeInventory();
		}
	}
	
	public static void shwHdPls(final Player p) {
		for (final Player pl : Bukkit.getOnlinePlayers()) {
			if (p.getWorld().getName().equals(pl.getWorld().getName())) {
				pl.showPlayer(Main.plug, p);
				p.showPlayer(Main.plug, pl);
			} else {
				pl.hidePlayer(Main.plug, p);
				p.hidePlayer(Main.plug, pl);
			}
		}
	}

	protected Vec3D vcFrmLc(final Location loc) {
		return new Vec3D(loc.getX(), loc.getY(), loc.getZ());
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
	
	public static boolean rayThruAir(final Location loc, final Location ploc, final double intrvl) {
		final Vector vec = loc.toVector().subtract(ploc.toVector()).normalize().multiply(intrvl);
		while (true) {
			ploc.add(vec);
			if (ploc.getBlock().getBoundingBox().contains(ploc.getX(), ploc.getY(), ploc.getZ())) {
				return false;
			}
			if (Math.abs(ploc.getX() - loc.getX()) < 0.5D && Math.abs(ploc.getZ() - loc.getZ()) < 0.5D) {
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
}