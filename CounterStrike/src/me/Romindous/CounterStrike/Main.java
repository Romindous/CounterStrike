package me.Romindous.CounterStrike;

import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import me.Romindous.CounterStrike.Enums.GunType;
import me.Romindous.CounterStrike.Listeners.DmgLis;
import me.Romindous.CounterStrike.Listeners.InterrLis;
import me.Romindous.CounterStrike.Listeners.InventLis;
import me.Romindous.CounterStrike.Listeners.MainLis;
import me.Romindous.CounterStrike.Objects.DmgdEnt;
import me.Romindous.CounterStrike.Objects.EntityShootAtEntityEvent;
import me.Romindous.CounterStrike.Objects.Nade;
import me.Romindous.CounterStrike.Objects.Shooter;
import me.Romindous.CounterStrike.Objects.SmplLoc;
import me.Romindous.CounterStrike.Objects.TripWire;
import me.Romindous.CounterStrike.Utils.PacketUtils;
import net.minecraft.core.BlockPosition;
import net.minecraft.network.chat.ChatMessage;
import net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata;
import net.minecraft.network.protocol.game.PacketPlayOutSpawnEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.decoration.EntityArmorStand;
 
	public final class Main extends JavaPlugin implements Listener {
	public static final HashSet<SmplLoc> crckd = new HashSet<>();
	public static final HashSet<SmplLoc> bmbs = new HashSet<>();
	public static final LinkedHashMap<Block, Byte> ndBlks = new LinkedHashMap<>();
	public static final LinkedHashMap<Player, Location> plnts = new LinkedHashMap<>();
	public static final HashSet<Nade> nades = new HashSet<>();
	public static final HashSet<Shooter> shtrs = new HashSet<>();
	public static final LinkedList<DmgdEnt> htEnts = new LinkedList<>();
	public static final HashSet<Block> htBlks = new HashSet<>();
	public static final SecureRandom srnd = new SecureRandom();
	public static final HashSet<Location> dcs = new HashSet<>();
	public static final HashSet<TripWire> tws = new HashSet<>();
	public static final ItemStack cp = new ItemStack(Material.CARVED_PUMPKIN);
	   
	public static Main plug;
	 
	   
	public void onEnable() {
		plug = this;
	     
		getServer().getPluginManager().registerEvents(new DmgLis(), this);
		getServer().getPluginManager().registerEvents(new InterrLis(), this);
		getServer().getPluginManager().registerEvents(new MainLis(), this);
		getServer().getPluginManager().registerEvents(new InventLis(), this);
	     
		for (final Player p : getServer().getOnlinePlayers()) {
			p.getInventory().clear();
			p.getInventory().setItem(8, mkItm(Material.GHAST_TEAR, "§5Магазин", 1));
		} 
	     
		PacketUtils.v = getServer().getClass().getPackage().getName().split("\\.")[3];  
     
		new BukkitRunnable() {
			public void run() {
				final Iterator<Location> li = Main.dcs.iterator();
	            while (li.hasNext()) {
	            	final Location loc = li.next();
	            	loc.getWorld().spawnParticle(Particle.SOUL, loc, 4, 0.4D, 0.4D, 0.4D, 0.0D, null, false);
	                loc.setYaw(loc.getYaw() - 1.0F);
	                if (loc.getYaw() == 0.0F) {
	                	li.remove();
	                }
	            } 
	
	            final Iterator<SmplLoc> ci = Main.crckd.iterator();
	            while (ci.hasNext()) {
	            	final SmplLoc lc = ci.next();
	            	lc.cnt = (short) ((lc.cnt >> 3) - 1 << 3 ^ lc.cnt & 0x7);
	                if (lc.cnt >> 3 == 0) {
	                ci.remove();
	                }
	            } 
	           
	            for (final SmplLoc s : Main.bmbs) {
	            	Main.this.getServer().broadcastMessage(String.valueOf(s.cnt));
	                s.cnt = (short) (s.cnt - 1);
	                if (s.cnt == 0) {
	                	s.expldBmb();
	                }
	            } 
	           
	            final Iterator<Entry<Player, Location>> pi = Main.plnts.entrySet().iterator();
	            while (pi.hasNext()) {
	            	final Entry<Player, Location> e = pi.next();
	            	final Location loc = e.getKey().getLocation();
	            	if (e.getKey().getInventory().getHeldItemSlot() == 7 && loc.getBlockX() == e.getValue().getBlockX() && loc.getBlockY() == e.getValue().getBlockY() && loc.getBlockZ() == e.getValue().getBlockZ()) {
	            		continue;
	            	}
	            	PacketUtils.sendAcBr(e.getKey(), "§c§lВы вышли из режима установки", 20);
	            	e.getKey().getInventory().setItem(7, new ItemStack(Material.GOLDEN_APPLE));
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
	            				bl.setValue((byte)30);
	            				continue;
	            			case 1:
	            				bl.getKey().setType(Material.POWDER_SNOW, false);
	            				bl.setValue((byte)50);
	            				continue;
	            			default:
	            				bl.getKey().setType(Material.FIRE, false);
	            				bl.setValue((byte)20);
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
	
		new BukkitRunnable() {
			public void run() {
				try {
					dlDmg();
				} catch (NullPointerException e) {
					Main.htEnts.clear();
				} 
				Main.htEnts.clear();
	           
				for (final Block b : Main.htBlks) {
					plcBck(b, b.getType(), b.getBlockData());
					b.setType(Material.AIR);
				} 
				Main.htBlks.clear();
	    	      
				InterrLis.ents.clear();
				for (final World w : Bukkit.getWorlds()) {
					InterrLis.ents.addAll(w.getLivingEntities());
				}
	           
				for (final Shooter sh : Main.shtrs) {
					sh.cld = (byte)(sh.cld - (sh.cld == 0 ? 0 : 1));
					final ItemStack it = sh.inv.getItemInMainHand();
					final GunType gt = GunType.getGnTp(sh.inv.getItemInMainHand());
					if (gt == null) {
						continue;
					}
					boolean ps = ((Damageable)it.getItemMeta()).hasDamage();
					if (ps) {
						sh.cnt = (short)(sh.cnt + 1);
						if ((sh.cnt & 0x7) == 0) {
							HumanEntity p = sh.inv.getHolder();
							p.getWorld().playSound(p.getLocation(), Sound.BLOCK_CHAIN_FALL, 0.5f, 2f);
						} 
						Main.setDmg(it, it.getType().getMaxDurability() * sh.cnt / gt.rtm);
						if (sh.cnt >= gt.rtm) {
							sh.inv.getItemInMainHand().setAmount(gt.amo);
							sh.cnt = 0;
						} 
					} 
	             
					if (sh.is) {
						sh.cnt = (short)(sh.cnt + (ps ? 0 : 1));
						if ((sh.cnt & 0x3) == 0) {
							sh.is = false;
						}
						if (sh.cnt % gt.cld == 0) {
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
							final boolean iw = (gt.snp && p.isSneaking()); byte i;
							for (i = gt.brst == 0 ? 1 : gt.brst; i > 0; i--) {
								sh.shoot(InterrLis.ents, gt, Main.this.getPlug(), p, !iw);
							}
							if (iw) {
								PacketUtils.fkHlmtClnt(p, p.getInventory().getHelmet());
								PacketUtils.zoom(p, false);
								p.setSneaking(false);
							} 
							p.setVelocity(p.getVelocity().subtract(p.getEyeLocation().getDirection().multiply(gt.kb)));
						}  continue;
					}  if (sh.cnt != 0 && !ps) {
						sh.cnt = sh.cnt > sh.rctm + 1 ? sh.rctm : (short) ((sh.cnt - 2 < 0) ? 0 : (sh.cnt - 2));
					}
				}
			}
		}.runTaskTimer(this, 1L, 1L);
   	}
   
	private void dlDmg() {
		for (final DmgdEnt e : htEnts) {
			if (e.dmgr.isValid() && e.ent.isValid()) {
				final String nm;
				if (e.hst) {
					e.dmgr.playSound(e.dmgr.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1f, 2f);
					e.dmg *= 2f * ((e.ent.getEquipment().getHelmet() == null) ? 1f : 0.5f);
					nm = "§c銑 " + String.valueOf((int)(e.dmg * 5.0F));
				} else {
					e.dmg *= (e.ent.getEquipment().getChestplate() == null) ? 1f : 0.6f;
					nm = "§6" + String.valueOf((int)(e.dmg * 5.0f));
				}
				getServer().getPluginManager().callEvent(new EntityShootAtEntityEvent(e));
				dmgArm(e.dmgr, e.ent.getEyeLocation(), nm);
			}
			/*
			 */
		} 
	}
	
	public static void killPl(final Player p) {
		if (p.getGameMode() == GameMode.SURVIVAL) {
			p.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20);
			p.setHealth(20);
			p.getActivePotionEffects().clear();
			PacketUtils.zoom(p, false);
			p.closeInventory();
			BlockPosition sp = PacketUtils.getNMSPlr(p).getSpawn();
			p.teleport(new Location(p.getWorld(), sp.getX() + 0.5D, sp.getY() + 0.5D, sp.getZ() + 0.5D));
		} 
	}
   
	public Main getPlug() {
		return this;
	}
	
	private void dmgArm(final Player p, final Location loc, final String nm) {
		if (!p.isValid()) {
			return;
		}
		final EntityArmorStand arm = new EntityArmorStand(EntityTypes.c, PacketUtils.getNMSWrld(loc.getWorld()));
		arm.setInvisible(true);
		arm.setNoGravity(true);
		arm.setInvulnerable(true);
		arm.setMarker(true);
		arm.setCustomName(new ChatMessage(nm));
		arm.setCustomNameVisible(true);
		arm.setPosition(loc.getX() + srnd.nextDouble() - 0.5D, loc.getY() + srnd.nextDouble() - 0.5D, loc.getZ() + srnd.nextDouble() - 0.5D);
     
		PacketUtils.getNMSPlr(p).b.sendPacket(new PacketPlayOutSpawnEntity((Entity)arm));
		PacketUtils.getNMSPlr(p).b.sendPacket(new PacketPlayOutEntityMetadata(arm.getId(), arm.getDataWatcher(), true));
		
		new BukkitRunnable() {
			public void run() {
				arm.setCustomNameVisible(false);
				arm.killEntity();
				PacketUtils.getNMSPlr(p).b.sendPacket(new PacketPlayOutSpawnEntity((Entity)arm));
				PacketUtils.getNMSPlr(p).b.sendPacket(new PacketPlayOutEntityMetadata(arm.getId(), arm.getDataWatcher(), true));
			}
		}.runTaskLater(this, 20L);
   }
   
   private void plcBck(final Block b, final Material m, final BlockData bd) {
	   new BukkitRunnable() {
		   public void run() {
			   if (b.getType() == Material.AIR) {
				   b.setType(m, false);
				   b.setBlockData(bd);
				} 
			}
       }.runTaskLater(this, 100L);
   }
 
 
   
   public void onDisable() {}
 
   
   public static ItemStack mkItm(final Material mt, final String nm, final int mdl) {
	   final ItemStack it = new ItemStack(mt);
	   final ItemMeta im = it.getItemMeta();
	   im.setDisplayName(nm);
       im.setCustomModelData(Integer.valueOf(mdl));
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
       return "§8[§5CS§8] ";
   }
 }