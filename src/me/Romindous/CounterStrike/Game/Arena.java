package me.Romindous.CounterStrike.Game;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;

import me.Romindous.CounterStrike.Main;
import me.Romindous.CounterStrike.Menus.BotMenu;
import me.Romindous.CounterStrike.Objects.Shooter;
import me.Romindous.CounterStrike.Objects.Game.GameState;
import me.Romindous.CounterStrike.Objects.Game.GameType;
import me.Romindous.CounterStrike.Objects.Game.PlShooter;
import me.Romindous.CounterStrike.Objects.Game.TripWire;
import me.Romindous.CounterStrike.Objects.Loc.BrknBlck;
import me.Romindous.CounterStrike.Objects.Skins.Quest;
import me.Romindous.CounterStrike.Objects.Skins.SkinQuest;
import me.Romindous.CounterStrike.Utils.Inventories;
import me.Romindous.CounterStrike.Utils.PacketUtils;
import net.minecraft.EnumChatFormat;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Ostrov;
import ru.komiss77.modules.world.XYZ;
import ru.komiss77.notes.Slow;
import ru.komiss77.utils.TCUtils;
import ru.komiss77.utils.inventory.SmartInventory;

public class Arena {
	
	public final HashMap<Shooter, Team> shtrs = new HashMap<>();
	public final HashSet<BrknBlck> brkn = new HashSet<>();
	public final LinkedList<TripWire> tws = new LinkedList<>();
	public final String name;
	public final World w;
	public final byte min;
	public final byte max;
//	public final Area area;
	protected final XYZ[] TSpawns;
	protected final XYZ[] CTSawns;
	protected final XYZ[] spots;
	public final SmartInventory botInv;
	public final boolean rnd;
	public boolean bots;
	protected BukkitTask tsk;
	protected short tm;
	public GameState gst;
	
	public Arena(final String name, final byte min, final byte max, 
		final XYZ[] TSpawns, final XYZ[] CTSawns, final XYZ[] spots, 
		final World w, final boolean rnd, final boolean bots) {
		this.w = w;
		w.setTime(6000L);
		w.setDifficulty(Difficulty.EASY);
		w.setGameRule(GameRule.DO_MOB_SPAWNING, false);
		w.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
		w.setGameRule(GameRule.NATURAL_REGENERATION, false);
		this.gst = GameState.WAITING;
		this.TSpawns = TSpawns;
		this.CTSawns = CTSawns;
		this.spots = spots;
		
//		this.area = area;
		this.name = name;
		this.rnd = rnd;
		this.min = min;
		this.max = max;
		this.tm = 0;
		
		botInv = bots ? SmartInventory.builder().id(name + " Bots").title("         §чИграть с Ботами?")
    			.provider(new BotMenu(this)).size(1, 9).build() : null;
		this.bots = botInv != null;
		Ostrov.async(() -> ApiOstrov.shuffle(spots));
	}

	protected Arena getArena() {
		return this;
	}
	
//	public static Arena getPlArena(final Shooter pl) {
//		for (final Arena ar : Main.actvarns) {
//			if (ar.shtrs.keySet().contains(pl)) {
//				return ar;
//			}
//		}
//		if (Main.shtrs.contains(pl)) {
//			return null;
//		}
//		return null;
//	}
	
	public enum Team {
		
		Ts(EnumChatFormat.e, "§4\u9265"),
		CTs(EnumChatFormat.d, "§3\u9264"),
		NA(EnumChatFormat.h, "");
		
		public final String icn;
		public final EnumChatFormat clr;
		
		Team(final EnumChatFormat clr, final String icn) {
			this.clr = clr;
			this.icn = icn;
		}
		
		public Team getOpst() {
            return switch (this) {
                case Ts -> CTs;
                case CTs -> Ts;
                case NA -> NA;
            };
        }
	}

	public static void end(Arena ar) {
		ar.gst = GameState.WAITING;
		ApiOstrov.sendArenaData(ar.name, ru.komiss77.enums.GameState.ОЖИДАНИЕ, "§7[§5CS§7]", "§8Не Выбрано", " ", "§7Игроков: §50§7/§5" + ar.min, "", 0);
		Main.actvarns.remove(ar.name);
		for (final Shooter sh : ar.shtrs.keySet()) {
			if (sh instanceof PlShooter) {
				Main.lobbyPl(sh.getPlayer());
			}
		}
		ar.shtrs.clear();
		Inventories.updtGm(ar);
		for (final Entity e : ar.w.getEntities()) {
			if (e.getType() != EntityType.PLAYER) {
				e.remove();
			}
		}
		
		if (ar.rnd) {
			Bukkit.getConsoleSender().sendMessage("removeing arena");
			Main.mapBlds.remove(ar.name).remove(ar.w, 3);
		}
		Ostrov.async(() -> Inventories.fillGmInv());
		ar = null;
	}

	public boolean addPl(final Shooter sh) {
		final Player p = sh.getPlayer();
		if (p != null) {
			p.sendMessage(Main.prf() + "Что то пошло не так...");
		}
		return false;
	}

	public boolean rmvPl(final Shooter sh) {
		final Player p = sh.getPlayer();
		if (p != null) {
			p.sendMessage(Main.prf() + "Что то пошло не так...");
		}
		return false;
	}

	public void killSh(final Shooter sh) {
		final Player p = sh.getPlayer();
		if (p != null) {
			p.sendMessage(Main.prf() + "Что то пошло не так...");
		}
	}
	
	public void chngMn(final Shooter sh, final int n) {
		sh.money(sh.money() + n);
		final Player p = sh.getPlayer();
		if (p == null) return;
		SkinQuest.tryCompleteQuest(sh, Quest.ЛАТУНЬ, sh.money());
		PacketUtils.sendAcBr(p, (n < 0 ? "§5" : "§d+") + n + " §6⛃", 20);
		Main.chgSbdTm(p.getScoreboard(), "mn", "", "§d" + sh.money() + " §6⛃");
	}

	public void addKll(final Shooter sh) {
		sh.killsI();
	}

	public void addDth(final Shooter sh) {
		sh.deathsI();
	}
	
	public String getShtrNm(final Shooter sh) {
		final Team tm = sh == null ? null : shtrs.get(sh);
		return (tm == null ? Team.NA.clr : tm.clr) + sh.name();
	}

	public boolean isSmTm(final Shooter org, final Shooter cmp) {
		if (cmp.arena() == null || org.arena() == null || !org.arena().name.equals(cmp.arena().name)) return true;
		final Team tm = shtrs.get(org);
		return tm != null && tm == shtrs.get(cmp);
	}
	
	public int getTmAmt(final Team tm, final boolean bots, final boolean alv) {
		int n = 0;
		for (final Entry<Shooter, Team> e : shtrs.entrySet()) {
			if (e.getValue() == tm) {
				if (alv && e.getKey().isDead()) continue;
				if (e.getKey() instanceof PlShooter || bots) {
					n++;
				} 
			}
		}
		return n;
	}
	
	@Slow(priority = 3)
	public boolean canOpnShp(final Location loc, final Team tm) {
		switch (tm) {
		case Ts:
			for (final XYZ b : TSpawns) {
				if (Math.abs(loc.getBlockX() - b.x) < 3 && Math.abs(loc.getBlockZ() - b.z) < 3) {
					return true;
				}
			}
			break;
		case CTs:
			for (final XYZ b : CTSawns) {
				if (Math.abs(loc.getBlockX() - b.x) < 3 && Math.abs(loc.getBlockZ() - b.z) < 3) {
					return true;
				}
			}
			break;
		case NA:
			break;
		}
		return false;
	}

	public void chngTm(final Shooter sh, final Team nv) {
    }
	
	public GameType getType() {
		return GameType.DEFUSAL;
	}
	
	@Slow(priority = 2)
	public XYZ getClosestPos(final XYZ loc, final int dst) {
		final XYZ lc = loc.clone().add(Main.srnd.nextBoolean() ? dst : -dst, 
			Main.srnd.nextBoolean() ? dst : -dst, Main.srnd.nextBoolean() ? dst : -dst);
		int bbi = 0;
		int dd = Integer.MAX_VALUE;
		for (int i = spots.length - 1; i >= 0; i--) {
			final int d = lc.distSq(spots[i]);
			if (d < dd) {
				bbi = i;
				dd = d;
			}
		}
//		final XYZ dif = spots[bbi];
//		w.getPlayers().forEach(p -> p.sendBlockChange(dif.getCenterLoc(w), Material.STONE.createBlockData()));
		return spots[bbi];
	}

	public static String getTime(final short t, final String cc) {
		return cc + (t / 60 > 9 ? t / 60 : "0" + (t / 60))
			+ "§7:"
			+ cc + (t % 60 > 9 ? t % 60 : "0" + (t % 60));
	}

	public short getTime() {
		return tm;
	}
	
	@Slow(priority = 1)
	protected static void editLr(final ItemStack it, final boolean add, final String lmnt) {
		final ItemMeta im = it.getItemMeta();
		final List<String> lr = im.lore().stream().map(TCUtils::toString).collect(Collectors.toList());
		if (add) lr.add(lmnt);
		else lr.remove(lmnt);
		im.lore(lr.stream().map(TCUtils::format).collect(Collectors.toList()));
		it.setItemMeta(im);
	}
}
