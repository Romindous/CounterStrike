package me.Romindous.CounterStrike.Objects.Bots;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import me.Romindous.CounterStrike.Main;
import me.Romindous.CounterStrike.Objects.Game.BtShooter;
import net.minecraft.core.BlockPosition;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.PacketPlayInUseEntity;
import net.minecraft.network.protocol.game.PacketPlayInUseEntity.b;
import net.minecraft.network.protocol.game.PacketPlayOutSpawnEntity;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.player.EntityHuman;

public class BotManager {
	
	public static final HashMap<Integer, BtShooter> npcs = new HashMap<>();
	
	protected static final String[] names = readNames();

	public static final Method getEnt = mkGet(".entity.CraftLivingEntity");
	public static final Method getWrld = mkGet(".CraftWorld");
	private static Method mkGet(final String pth) {
		try {
			return Class.forName(Bukkit.getServer().getClass().getPackageName() + pth).getDeclaredMethod("getHandle");
		} catch (NoSuchMethodException | SecurityException | ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

	public BotManager() {
		
		BotType.values();
		
		for (final Player pl : Bukkit.getOnlinePlayers()) {
			injectPlayer(pl);
		}
        
		/*new BukkitRunnable() {
			
			@Override
			public void run() {
				final Iterator<Bot> it = npcs.values().iterator();
				while (it.hasNext()) {
					final Bot bt = it.next();
					if (bt.tick()) {
						bt.remove(true);
						it.remove();
					}
				}
			}
		}.runTaskTimer(Main.plug, 20, 2);*/
	}
	
	public static String[] readNames() {
		final File fl = new File(Bukkit.getPluginsFolder().getAbsolutePath() + File.separator + "LobbyOstrov" + File.separator + "names.txt");
		if (fl.exists()) {
			try {
				return Files.readAllLines(Path.of(fl.getAbsolutePath())).toArray(new String[0]);
			} catch (IOException e) {
				e.printStackTrace();
				return new String[] {""};
			}
		}
		return new String[] {""};
	}
	
	public static Player getNrPl(final World w, final BlockPosition bp, final int dst) {
		Player p = null;
		int dd = Integer.MAX_VALUE;
		for (final Player pl : w.getPlayers()) {
			final Location l = pl.getLocation();
			final int d = Math.abs(l.getBlockX() - bp.u()) + Math.abs(l.getBlockY() - bp.v()) + Math.abs(l.getBlockZ() - bp.w());
			if (d < dd && d < dst) {
				p = pl;
			}
		}
		return p;
	}
	
	public static LivingEntity getNrLent(final World w, final BlockPosition bp, final int dst, final boolean invTgt, final EntityType[] tgts) {
		LivingEntity lent = null;
		int dd = Integer.MAX_VALUE;
		if (invTgt) {
			boolean isNot = true;
			for (final LivingEntity le : w.getLivingEntities()) {
				final Location l = le.getLocation();
				final int d = Math.abs(l.getBlockX() - bp.u()) + Math.abs(l.getBlockY() - bp.v()) + Math.abs(l.getBlockZ() - bp.w());
				if (d < dd && d < dst && !npcs.containsKey(le.getEntityId()) && le.isOnGround()) {
					final EntityType et = le.getType();
					for (final EntityType e : tgts) {
						if (et == e) {
							isNot = false;
							break;
						}
					}
					if (isNot) {
						dd = d;
						lent = le;
					}
					isNot = true;
				}
			}
		} else {
			for (final LivingEntity le : w.getLivingEntities()) {
				final Location l = le.getLocation();
				final int d = Math.abs(l.getBlockX() - bp.u()) + Math.abs(l.getBlockY() - bp.v()) + Math.abs(l.getBlockZ() - bp.w());
				if (d < dd && d < dst && !npcs.containsKey(le.getEntityId()) && le.isOnGround()) {
					final EntityType et = le.getType();
					for (final EntityType e : tgts) {
						if (et == e) {
							dd = d;
							lent = le;
							break;
						}
					}
				}
			}
		}
		return lent;
	}

	/*public static final Packet<?>[] sbd = pcktGet();
	private static Packet<?>[] pcktGet() {
		final Packet<?>[] ps = new Packet<?>[4];
		final ScoreboardServer ss = Main.ds.aF();
		final ScoreboardTeam st = ss.g(Bot.nm);
		st.a(EnumNameTagVisibility.b);
		ps[0] = PacketPlayOutScoreboardTeam.a(st); 
		ps[1] = PacketPlayOutScoreboardTeam.a(st, true); 
		ps[2] = PacketPlayOutScoreboardTeam.a(st, Bot.nm, a.a); 
		ps[3] = PacketPlayOutScoreboardTeam.a(st, false);
		ss.d(st);
		return ps;
	}*/

	/*public static final Method getItm = itGet();
	private static Method itGet() {net.minecraft.world.item.ItemStack.
		try {
			Bukkit.broadcast(Component.text(Arrays.toString(Class.forName(Bukkit.getServer().getClass().getPackageName() + ".inventory.CraftItemStack").getMethods())));
			return Class.forName(Bukkit.getServer().getClass().getPackageName() + ".inventory.CraftItemStack").getMethod("asNMSCopy");
		} catch (NoSuchMethodException | SecurityException | ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}*/
	
	public static final Field id = getIdFld();
    private static Field getIdFld() {
    	final Field fld = PacketPlayInUseEntity.class.getDeclaredFields()[0];
    	fld.setAccessible(true);
		return fld;
	}
	
	public static void sendWrldPckts(final net.minecraft.world.level.World w, final Packet<?>... ps) {
		for (final EntityHuman e : w.w()) {
			if (e instanceof EntityPlayer) {
				final NetworkManager nm = ((EntityPlayer) e).networkManager;
				for (final Packet<?> p : ps) {
					nm.a(p);
				}
			}
		}
	}
    
    public static void removePlayer(final Player p) {
    	final Channel channel = Main.ds.bh().a(p.getName()).b.b.m;
        channel.eventLoop().submit(() -> {
            channel.pipeline().remove(p.getName());
            return null;
        });
    }

    public static void injectPlayer(final Player p) {
    	final NetworkManager nm = Main.ds.bh().a(p.getName()).networkManager;
    	nm.m.pipeline().addBefore("packet_handler", p.getName(), new ChannelDuplexHandler() {
            @Override
            public void channelRead(final ChannelHandlerContext chc, final Object packet) throws Exception  {
            	if (packet instanceof PacketPlayInUseEntity) {
            		final PacketPlayInUseEntity uep = (PacketPlayInUseEntity) packet;
            		if (uep.getActionType() == b.b) {
            			for (final BtShooter bt : BotManager.npcs.values()) {
            				if (bt.ae() == uep.getEntityId()) {
                				id.set(uep, bt.rid);
                				break;
            				}
            			}
            		}
            	}
                super.channelRead(chc, packet);
            }
            
			@Override
            public void write(final ChannelHandlerContext chc, final Object packet, final ChannelPromise channelPromise) throws Exception {
				if (packet instanceof PacketPlayOutSpawnEntity) {
					if (BotManager.npcs.get(((PacketPlayOutSpawnEntity) packet).b()) != null) return;
				}
                super.write(chc, packet, channelPromise);
            }
        });
    }
    
    public static net.minecraft.world.item.ItemStack getItem(final ItemStack it) {
    	return net.minecraft.world.item.ItemStack.fromBukkitCopy(it);
    }
    
    public static WorldServer getNMSWrld(final org.bukkit.World w) {
		try {
			return (WorldServer) getWrld.invoke(w);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			return null;
		}
  	}

	public static EntityLiving getNMSLEnt(final LivingEntity tgt) {
		try {
			return (EntityLiving) getEnt.invoke(tgt);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			return null;
		}
	}

	public static void showSpots() {
		
	}

	public static void clearBots() {
		final HashMap<Integer, BtShooter> ns = new HashMap<>();
		ns.putAll(npcs);
		for (final BtShooter bt : ns.values()) {
			bt.remove(false);
		}
		npcs.clear();
	}

	public static void updateBots(final Player p) {
		final EntityPlayer pl = Main.ds.bh().a(p.getName());
		final String wn = p.getWorld().getName();
		final NetworkManager nm = pl.networkManager;
		for (final BtShooter bt : BotManager.npcs.values()) {
			if (bt.s.getWorld().getName().equals(wn)) {
				p.sendMessage("updating bot-" + bt.rid);
				final LivingEntity le = bt.getEntity();
				if (le == null) {
					
				}
				bt.getEntity().addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 100, 1));
				bt.updateAll(nm);
			}
		}
	}
}
