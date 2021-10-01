package me.Romindous.CounterStrike;

import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

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
import org.bukkit.configuration.ConfigurationSection;
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

import com.mojang.datafixers.util.Pair;

import me.Romindous.CounterStrike.Commands.CSCmd;
import me.Romindous.CounterStrike.Enums.GameState;
import me.Romindous.CounterStrike.Enums.GunType;
import me.Romindous.CounterStrike.Game.Arena;
import me.Romindous.CounterStrike.Game.Defusal;
import me.Romindous.CounterStrike.Game.Invasion;
import me.Romindous.CounterStrike.Listeners.DmgLis;
import me.Romindous.CounterStrike.Listeners.InterrLis;
import me.Romindous.CounterStrike.Listeners.InventLis;
import me.Romindous.CounterStrike.Listeners.MainLis;
import me.Romindous.CounterStrike.Objects.Nade;
import me.Romindous.CounterStrike.Objects.Shooter;
import me.Romindous.CounterStrike.Objects.SmplLoc;
import me.Romindous.CounterStrike.Utils.Inventories;
import me.Romindous.CounterStrike.Utils.PacketUtils;
import net.minecraft.EnumChatFormat;
import net.minecraft.core.BaseBlockPosition;
import net.minecraft.network.chat.ChatMessage;
import net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy;
import net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata;
import net.minecraft.network.protocol.game.PacketPlayOutSpawnEntity;
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
	public static BaseBlockPosition lobby;
	public static ScoreboardManager smg;
	public static YamlConfiguration ars;
	public static YamlConfiguration config;
	public static final SecureRandom srnd = new SecureRandom();
    public static final LivingEntity[] a = new LivingEntity[0];
	public static ItemStack cp;
	public static ItemStack bmb;
	public static ItemStack thlmt;
	public static ItemStack cthlmt;
	
	public static final HashSet<Arena> actvarns = new HashSet<>();
	public static final HashSet<String> nnactvarns = new HashSet<>();
	
	public static final HashSet<SmplLoc> crckd = new HashSet<>();
	public static final LinkedHashMap<Block, Byte> ndBlks = new LinkedHashMap<>();
	public static final LinkedHashMap<Player, BaseBlockPosition> plnts = new LinkedHashMap<>();
	public static final LinkedHashMap<World, LivingEntity[]> wlents = new LinkedHashMap<>();
	public static final HashSet<Nade> nades = new HashSet<>();
	public static final HashSet<Shooter> shtrs = new HashSet<>();
	public static final HashSet<Location> dcs = new HashSet<>();

	public static short hlmtPrc;
	public static short chstPrc;
	public static short twrPrc;
	public static short dfktPrc;
	
	public void onEnable() {
		
		plug = this;
		folder = getDataFolder();
		smg = getServer().getScoreboardManager();
	     
		getServer().getPluginManager().registerEvents(new DmgLis(), this);
		getServer().getPluginManager().registerEvents(new InterrLis(), this);
		getServer().getPluginManager().registerEvents(new MainLis(), this);
		getServer().getPluginManager().registerEvents(new InventLis(), this);
		
		getCommand("cs").setExecutor(new CSCmd());
	     
		PacketUtils.v = getServer().getClass().getPackage().getName().split("\\.")[3];
		
		loadConfigs();
     
		//game stuff (5 tick)
		new BukkitRunnable() {
			public void run() {
	
	            final Iterator<SmplLoc> ci = Main.crckd.iterator();
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
	            	if (e.getKey().getInventory().getHeldItemSlot() == 7 && loc.getBlockX() == e.getValue().getX() && loc.getBlockY() == e.getValue().getY() && loc.getBlockZ() == e.getValue().getZ()) {
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
				
				final Iterator<Location> li = Main.dcs.iterator();
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
	            
				for (final Entry<World, LivingEntity[]> we : wlents.entrySet()) {
					we.setValue(we.getKey().getLivingEntities().toArray(a));
				}
				
				for (final Shooter sh : Main.shtrs) {
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
				
				for (final Arena ar : Main.actvarns) {
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
				}
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
	        
	    	hlmtPrc = 150;
	    	chstPrc = 250;
	    	twrPrc = 200;
	    	dfktPrc = 250;
	    	rplnk = "https://download.mc-packs.net/pack/5b99bf3147118bbcfd6351455ea47c4cdaac2456.zip";
	    	cp = new ItemStack(Material.CARVED_PUMPKIN);
	    	bmb = Main.mkItm(Material.GOLDEN_APPLE, "§4§lС*4 §c\u926e", 1, "§dПКМ §7- Заложить бомбу", "§7Можно установить на точку §5A §7или §5B");
	    	thlmt = new ItemStack(Material.LEATHER_HELMET);
			final LeatherArmorMeta hm = (LeatherArmorMeta) thlmt.getItemMeta();
			hm.setColor(Color.RED);
			hm.setDisplayName("§cШапка Террориста §f\u9267");
			hm.setLore(Arrays.asList("§7Цена: §d" + Main.hlmtPrc + " §6⛃"));
			thlmt.setItemMeta(hm);
	    	cthlmt = new ItemStack(Material.LEATHER_HELMET);
			final LeatherArmorMeta chm = (LeatherArmorMeta) cthlmt.getItemMeta();
			chm.setColor(Color.TEAL);
			chm.setDisplayName("§3Шлем Спецназа §f\u9267");
			chm.setLore(Arrays.asList("§7Цена: §d" + Main.hlmtPrc + " §6⛃"));
			cthlmt.setItemMeta(chm);
	        Inventories.fillGmInv();
	        Inventories.fillLbbInv();
	        Inventories.fillTsInv();
	        Inventories.fillCTsInv();
	        
	        nnactvarns.clear();
	        if (!ars.contains("arenas")) {
	        	ars.createSection("arenas");
		        ars.save(file);
	        } else {
				for(final String s : ars.getConfigurationSection("arenas").getKeys(false)) {
					if (ars.contains("arenas." + s + ".fin")) {
						WorldManager.load(getServer().getConsoleSender(), ars.getString("arenas." + s + ".world"), Environment.NORMAL, Generator.Empty);
						final String tp;
						switch (ars.getString("arenas." + s + ".type")) {
							case "gungame":
								tp = "§dЭстафета";
								break;
							case "invasion":
								tp = "§dВторжение";
								break;
							case "defusal":
							default:
								tp = "§dКлассика";
								break;
						}
						ApiOstrov.sendArenaData(s, ru.komiss77.enums.GameState.ОЖИДАНИЕ, "§7[§5CS§7]", tp, " ", "§7Игроков: §50§7/§5" + ars.getString("arenas." + s + ".min"), "", 0);
						nnactvarns.add(s);
					}
				}
			}
	        if (ars.contains("lobby")) {
	        	lbbyW = getServer().getWorld(ars.getString("lobby.world"));
	        	lobby = new BaseBlockPosition(ars.getInt("lobby.x"), ars.getInt("lobby.y"), ars.getInt("lobby.z"));
	        }
	        
	        wlents.clear();
	        for (final World w : getServer().getWorlds()) {
	        	wlents.put(w, w.getLivingEntities().toArray(a));
	        }
        }
        catch (IOException | NullPointerException e) {
        	e.printStackTrace();
            return;
        }
	}

	public static void lobbyPl(final Player p) {
		nrmlzPl(p, true);
		final Pair<Shooter, Arena> pr = Shooter.getPlShtrArena(p.getName());
		pr.getFirst().inv = p.getInventory();
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
			pl.setPlayerListFooter("§7Сейчас в игре: §d" + String.valueOf(n) + "§7 человек!");
			if (p.getWorld().getName().equals(pl.getWorld().getName())) {
				pl.showPlayer(Main.plug, p);
				p.showPlayer(Main.plug, pl);
			} else {
				pl.hidePlayer(Main.plug, p);
				p.hidePlayer(Main.plug, pl);
			}
		}
		for (final Shooter sh : shtrs) {
			PacketUtils.sendNmTg(new Pair<Shooter, Arena>(sh, null), "§7<§5ЛОББИ§7> ", " §7[-.-]", EnumChatFormat.h);
		}
		for (final Arena ar : Main.actvarns) {
			if (ar.gst == GameState.WAITING) {
				for (final Entry<Shooter, me.Romindous.CounterStrike.Game.Arena.Team> e : ar.shtrs.entrySet()) {
					PacketUtils.sendNmTg(new Pair<Shooter, Arena>(e.getKey(), null), "§7<§d" + ar.name + "§7> ", " §7[-.-]", e.getValue().clr);
				}
			}
		}
	}
   
	public static void lobbyScore(final Player p) {
		final Scoreboard sb = Main.smg.getNewScoreboard();
		final Objective ob = sb.registerNewObjective("CS:GO", "", "§7[§5CS:GO§7]");
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
		final EntityArmorStand arm = new EntityArmorStand(EntityTypes.c, PacketUtils.getNMSWrld(loc.getWorld()));
		arm.setInvisible(true);
		arm.setNoGravity(true);
		arm.setInvulnerable(true);
		arm.setMarker(true);
		arm.setCustomName(new ChatMessage(nm));
		arm.setCustomNameVisible(true);
		arm.setPosition(loc.getX() + srnd.nextDouble() - 0.5D, loc.getY() + srnd.nextDouble() - 0.5D, loc.getZ() + srnd.nextDouble() - 0.5D);

		if (!p.isValid()) {
			return;
		}
		final PlayerConnection pc = PacketUtils.getNMSPlr(p).b;
		pc.sendPacket(new PacketPlayOutSpawnEntity(arm));
		pc.sendPacket(new PacketPlayOutEntityMetadata(arm.getId(), arm.getDataWatcher(), true));
		
		new BukkitRunnable() {
			public void run() {
				pc.sendPacket(new PacketPlayOutEntityDestroy(arm.getId()));
			}
		}.runTaskLater(Main.plug, 20L);
   }
   
   public static ItemStack mkItm(final Material mt, final String nm, final int mdl, final String... lr) {
	   final ItemStack it = new ItemStack(mt);
	   final ItemMeta im = it.getItemMeta();
	   im.setDisplayName(nm);
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
		tm.setPrefix(prf);
		tm.setSuffix(sfx);
	}
	
	public static void chgSbdTm(final Scoreboard sb, final String nm, final String prf, final String sfx) {
		final Team tm = sb.getTeam(nm);
		tm.setPrefix(prf);
		tm.setSuffix(sfx);
	}

	public Arena crtArena(final String nm) {
		final ConfigurationSection cs = Main.ars.getConfigurationSection("arenas." + nm);
		final World w = getServer().getWorld(cs.getString("world")) == null ? WorldManager.load(getServer().getConsoleSender(), cs.getString("world"), Environment.NORMAL, Generator.Empty) : getServer().getWorld(cs.getString("world"));
		final String[] tx = cs.getString("tspawns.x").split(":");
		final String[] ty = cs.getString("tspawns.y").split(":");
		final String[] tz = cs.getString("tspawns.z").split(":");
		final BaseBlockPosition[] ts = new BaseBlockPosition[tx.length];
		for (byte i = (byte) (tx.length - 1); i >= 0; i--) {
			ts[i] = new BaseBlockPosition(Integer.parseInt(tx[i]), Integer.parseInt(ty[i]), Integer.parseInt(tz[i]));
		}
		final String[] ctx = cs.getString("ctspawns.x").split(":");
		final String[] cty = cs.getString("ctspawns.y").split(":");
		final String[] ctz = cs.getString("ctspawns.z").split(":");
		final BaseBlockPosition[] cts = new BaseBlockPosition[ctx.length];
		for (byte i = (byte) (ctx.length - 1); i >= 0; i--) {
			cts[i] = new BaseBlockPosition(Integer.parseInt(ctx[i]), Integer.parseInt(cty[i]), Integer.parseInt(ctz[i]));
		}
		final Arena ar;
		final BaseBlockPosition ast;
		final BaseBlockPosition bst;
		switch (cs.getString("type")) {
		case "defusal":
			ast = new BaseBlockPosition(cs.getInt("asite.x"), cs.getInt("asite.y"), cs.getInt("asite.z"));
			bst = new BaseBlockPosition(cs.getInt("bsite.x"), cs.getInt("bsite.y"), cs.getInt("bsite.z"));
			ar = new Defusal(cs.getName(), (byte) cs.getInt("min"), (byte) cs.getInt("max"), ts, cts, w, ast, bst, (byte) 6);
			actvarns.add((Defusal) ar);
			break;
		case "invasion":
			ast = new BaseBlockPosition(cs.getInt("asite.x"), cs.getInt("asite.y"), cs.getInt("asite.z"));
			bst = new BaseBlockPosition(cs.getInt("bsite.x"), cs.getInt("bsite.y"), cs.getInt("bsite.z"));
			ar = new Invasion(cs.getName(), (byte) cs.getInt("min"), (byte) cs.getInt("max"), ts, cts, w, ast, bst);
			actvarns.add((Invasion) ar);
			break;
		default:
			ar = new Arena(cs.getName(), (byte) cs.getInt("min"), (byte) cs.getInt("max"), ts, cts, w);
			break;
		}
		return ar;
	}

	public static Location getNrLoc(final BaseBlockPosition loc, final World w) {
		return new Location(w, (Main.srnd.nextBoolean() ? -1 : 1) + loc.getX() + 0.5d, loc.getY() + 0.1d, (Main.srnd.nextBoolean() ? -1 : 1) + loc.getZ() + 0.5d);
	}

	public static Block getBBlock(final BaseBlockPosition loc, final World w) {
		return w.getBlockAt(loc.getX(), loc.getY(), loc.getZ());
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

	public static LivingEntity[] getWLnts(final String wnm) {
		for (final Entry<World, LivingEntity[]> we : wlents.entrySet()) {
			if (we.getKey().getName().equals(wnm)) {
				return we.getValue();
			}
		}
		return a;
	}
}