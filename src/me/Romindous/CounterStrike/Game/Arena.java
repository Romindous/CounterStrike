package me.Romindous.CounterStrike.Game;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import me.Romindous.CounterStrike.Enums.GameState;
import me.Romindous.CounterStrike.Enums.GameType;
import me.Romindous.CounterStrike.Main;
import me.Romindous.CounterStrike.Menus.BotMenu;
import me.Romindous.CounterStrike.Menus.GameMenu;
import me.Romindous.CounterStrike.Menus.TeamMenu;
import me.Romindous.CounterStrike.Objects.Game.BtShooter;
import me.Romindous.CounterStrike.Objects.Game.PlShooter;
import me.Romindous.CounterStrike.Objects.Game.TripWire;
import me.Romindous.CounterStrike.Objects.Loc.BrknBlck;
import me.Romindous.CounterStrike.Objects.Shooter;
import me.Romindous.CounterStrike.Objects.Skins.Quest;
import me.Romindous.CounterStrike.Menus.ChosenSkinMenu;
import me.Romindous.CounterStrike.Utils.Utils;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.scheduler.BukkitTask;
import ru.komiss77.Ostrov;
import ru.komiss77.enums.Game;
import ru.komiss77.modules.bots.BotManager;
import ru.komiss77.modules.games.GM;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.world.XYZ;
import ru.komiss77.notes.Slow;
import ru.komiss77.utils.ClassUtil;
import ru.komiss77.utils.inventory.InventoryManager;
import ru.komiss77.utils.inventory.SmartInventory;

public class Arena {

	public static SmartInventory gameInv = SmartInventory.builder().id("Arenas")
		.title("           §5§lВыбор Игры").provider(new GameMenu()).size(6, 9).build();
	public final static HashMap<XYZ, TripWire> tblks = new HashMap<>();
	protected final static String T_AMT = "tamt", CT_AMT = "ctamt",
		LIMIT = "rem", MONEY = "mn", STAGE = "gst", SCORE = "scr";

	protected static int botID = 0;
	
	public final HashMap<Shooter, Team> shtrs = new HashMap<>();
	public final HashSet<BrknBlck> brkn = new HashSet<>();
	public final HashSet<TripWire> tws = new HashSet<>();
	public final String name;
	public final World w;
	public final byte min;
	public final byte max;
//	public final Area area;
	protected final XYZ[] TSpawns;
	protected final XYZ[] CTSawns;
	protected final XYZ[] spots;
	public final SmartInventory botInv;
	public final SmartInventory teamInv;
	public final boolean rnd;
	public boolean bots;
	protected BukkitTask tsk;
	protected short time;
	public GameState gst;
	
	public Arena(final String name, final byte min, final byte max, 
		final XYZ[] TSpawns, final XYZ[] CTSawns, final XYZ[] spots, 
		final World w, final boolean rnd, final boolean bots) {
		this.w = w;
		w.setTime(6000L);
		Main.addLEWorld(w);
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
		this.time = 0;
		
		botInv = bots ? SmartInventory.builder().id(name + " Bots").title("         §чИграть с Ботами?")
			.provider(new BotMenu(this)).size(1, 9).build() : null;
		teamInv = SmartInventory.builder().id(name + " Team").title("         §eВыбор Комманды")
			.provider(new TeamMenu(this)).type(InventoryType.HOPPER).build();
		this.bots = botInv != null;
		Ostrov.async(() -> ClassUtil.shuffle(spots));
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
		
		Ts("§4", "\u9265", "tfight", "tfinish"),
		CTs("§3", "\u9264", "ctfight", "ctfinish"),
		SPEC("§7", "§8(Зритель)", "sfight", "sfinish");
		
		public final String icn;
		public final String goSnd;
		public final String finSnd;
		public final String clr;
		
		Team(final String clr, final String icn, final String goSnd, final String finSnd) {
			this.clr = clr;
			this.icn = clr + icn;
			this.goSnd = goSnd;
			this.finSnd = finSnd;
		}
		
		public Team getOpst() {
            return switch (this) {
                case Ts -> CTs;
                case CTs -> Ts;
                case SPEC -> SPEC;
            };
        }

		public NamedTextColor color() {
			return switch (this) {
				case Ts -> NamedTextColor.DARK_RED;
				case CTs -> NamedTextColor.DARK_AQUA;
				case SPEC -> NamedTextColor.GRAY;
			};
		}
	}

	public void end() {
		gst = GameState.WAITING;
		updateData();
		Main.delLEWorld(w);
		Main.actvarns.remove(name);
		for (final Shooter sh : shtrs.keySet()) {
			if (sh instanceof PlShooter) {
				Main.lobbyPl(sh.getPlayer());
			}
		}
		shtrs.clear();
		for (final Entity e : w.getEntities()) {
			if (e.getType() != EntityType.PLAYER) {
				e.remove();
			}
		}
		
		if (rnd) {
			Bukkit.getConsoleSender().sendMessage("removing arena");
			Main.mapBlds.remove(name).remove(w, 3);
		}
    }

	public boolean addPl(final PlShooter sh) {
		final Player p = sh.getPlayer();
		if (p != null) {
			p.sendMessage(Main.prf() + "Что то пошло не так...");
		}
		return false;
	}

	public boolean rmvPl(final PlShooter sh) {
		final Player p = sh.getPlayer();
		if (p != null) {
			p.sendMessage(Main.prf() + "Что то пошло не так...");
		}
		return false;
	}

	public void addToTm(final Player p, final PlShooter sh, final Team tm) {}

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
		ChosenSkinMenu.tryCompleteQuest(sh, Quest.ЛАТУНЬ, sh.money());
		Utils.sendAcBr(p, (n < 0 ? "§5" : "§d+") + n + " §6⛃");
		PM.getOplayer(p).score.getSideBar().update(MONEY, "§7Монет: §d" + sh.money() + " §6⛃");
	}

	public void addKll(final Shooter sh) {
		sh.killsI();
	}

	public void addDth(final Shooter sh) {
		sh.deathsI();
	}
	
	public String getShtrNm(final Shooter sh) {
		final Team tm = sh == null ? null : shtrs.get(sh);
		return (tm == null ? Team.SPEC.clr : tm.clr) + sh.name();
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
				if (e.getKey() instanceof PlShooter || bots) n++;
			}
		}
		return n;
	}

	public int getPlaying(final boolean bots, final boolean alv) {
		int n = 0;
		for (final Entry<Shooter, Team> e : shtrs.entrySet()) {
			if (e.getValue() == Team.SPEC) switch (gst) {
					case WAITING, BEGINING: n++;
					default: continue;
                }
			if (alv && e.getKey().isDead()) continue;
			if (e.getKey() instanceof PlShooter || bots) n++;
		}
		return n;
	}
	
	@Slow(priority = 2)
	public boolean canOpnShp(final Location loc, final Team tm) {
		switch (tm) {
		case Ts:
			for (final XYZ b : TSpawns) {
				if (b.distAbs(loc) < 5) return true;
			}
			break;
		case CTs:
			for (final XYZ b : CTSawns) {
				if (b.distAbs(loc) < 5) return true;
			}
			break;
		case SPEC:
			break;
		}
		return false;
	}

	public boolean chngTeam(final Shooter sh, final Team nv) {
		final int nta = getTmAmt(nv, true, true), ota = getTmAmt(nv.getOpst(), true, true);
		final Team ptm = shtrs.get(sh);
		if (ptm != null && ptm == nv) {
			final Player p = sh.getPlayer();
			if (p != null) {
				p.sendMessage(Main.prf() + "§cТы уже в этой комманде!");
			}
			return false;
		}

		if ((nv != Team.SPEC && nv.getOpst() == ptm && nta >= ota) || nta > ota) {
			final Player p = sh.getPlayer();
			if (p != null) {
				p.sendMessage(Main.prf() + "§cВ этой комманде слишком много игроков!");
			}
			return false;
		}

		shtrs.put(sh, nv);
		if (sh instanceof PlShooter) PM.getOplayer(sh.getPlayer()).color(nv.color());
		for (final Entry<Shooter, Team> e : shtrs.entrySet()) {
			final Player p = e.getKey().getPlayer();
			if (p != null) {
				PM.getOplayer(p).score.getSideBar()
					.update(T_AMT, Team.Ts.icn + " §7: " + getTmAmt(Team.Ts, true, true)
						+ (e.getValue() == Team.Ts ? " §7чел. §8✦ Ты" : " §7чел."))
					.update(CT_AMT, Team.CTs.icn + " §7: " + getTmAmt(Team.CTs, true, true)
						+ (e.getValue() == Team.CTs ? " §7чел. §8✦ Ты" : " §7чел."));
			}
		}
		switch (gst) {
			case WAITING, BEGINING, FINISH:
				sh.taq("§7<§d" + name + "§7> ", " §7[-.-]", nv.clr);
				break;
			case BUYTIME, ROUND, ENDRND:
				sh.taq(nv.icn + " ", " §7[" + sh.kills() + "-" + sh.deaths() + "]", nv.clr);
				break;
		}
		return true;
	}

	public Team getMinTm() {
		final int tn = getTmAmt(Team.Ts, true, false);
		final int ctn = getTmAmt(Team.CTs, true, false);
		return tn < ctn ? Team.Ts : (tn == ctn ? (Main.srnd.nextBoolean() ? Team.Ts : Team.CTs) : Team.CTs);
	}

	public void blncTms() {
		if (botInv != null && bots) {
			int tmMx = max >> 1;

			int tPls = 0, ctPls = 0;
			final Iterator<Entry<Shooter, Team>> it = shtrs.entrySet().iterator();
			while (it.hasNext()) {
				final Entry<Shooter, Team> en = it.next();
				switch (en.getValue()) {
					case Ts:
						if (++tPls > tmMx) {
							if (en.getKey() instanceof BtShooter) {
								it.remove();
								((BtShooter) en.getKey()).own().remove();
							} else {
								tmMx = tPls;
							}
						}
						break;
					case CTs:
						if (++ctPls > tmMx) {
							if (en.getKey() instanceof BtShooter) {
								it.remove();
								((BtShooter) en.getKey()).own().remove();
							} else {
								tmMx = ctPls;
							}
						}
						break;
					case SPEC:
						break;
				}
			}

			for (int i = tPls; i < tmMx; i++) {
				BotManager.createBot("Bot-v" + botID++, w, bt -> {
					final BtShooter nbt = new BtShooter(bt, this);
					shtrs.put(nbt, Team.Ts);
					return nbt;
				});
			}

			for (int i = ctPls; i < tmMx; i++) {
				BotManager.createBot("Bot-v" + botID++, w, bt -> {
					final BtShooter nbt = new BtShooter(bt, this);
					shtrs.put(nbt, Team.CTs);
					return nbt;
				});
			}
		}

		for (final Entry<Shooter, Team> e : shtrs.entrySet()) {
			final Player p = e.getKey().getPlayer();
			if (p != null) {
				PM.getOplayer(p).score.getSideBar()
					.update(T_AMT, Team.Ts.icn + " §7: " + getTmAmt(Team.Ts, true, true)
						+ (e.getValue() == Team.Ts ? " §7чел. §8✦ Ты" : " §7чел."))
					.update(CT_AMT, Team.CTs.icn + " §7: " + getTmAmt(Team.CTs, true, true)
						+ (e.getValue() == Team.CTs ? " §7чел. §8✦ Ты" : " §7чел."));
			}
		}
	}
	
	public GameType getType() {
		return null;
	}

	public void updateData() {
		switch (gst) {
			case WAITING:
				GM.sendArenaData(Game.CS, this.name, ru.komiss77.enums.GameState.ОЖИДАНИЕ, shtrs.size(), "§7Тип: §d" + getType().name,
					"§5=-=-=-=-=-=-", "§7Нужно: §d" + (min - shtrs.size()) + " §7чел.", "§7Боты: " + (bots ? "§aДа" : "§cНет"));
				break;
			case BEGINING:
				GM.sendArenaData(Game.CS, this.name, ru.komiss77.enums.GameState.СТАРТ, shtrs.size(), "§7Тип: §d" + getType().name, "§7Боты: "
					+ (bots ? "§aДа" : "§cНет"), "§5=-=-=-=-=-=-", "§7Макс. §d" + max + " §7чел.");
				break;
			case BUYTIME, ROUND, ENDRND:
				GM.sendArenaData(Game.CS, this.name, ru.komiss77.enums.GameState.ЭКИПИРОВКА, getPlaying(true, false),
					"§7Тип: §d" + getType().name, "§7Боты: " + (bots ? "§aДа" : "§cНет"),
					"§5=-=-=-=-=-=-", "§7Макс. §d" + max + " §7чел.");
				break;
			case FINISH:
				GM.sendArenaData(Game.CS, this.name, ru.komiss77.enums.GameState.ФИНИШ, shtrs.size(), "§7Тип: §d" + getType().name, "§7Боты: "
					+ (bots ? "§aДа" : "§cНет"), "§5=-=-=-=-=-=-", "§7Макс. §d" + max + " §7чел.");
				break;
		}

		for (final Shooter sh : shtrs.keySet()) {
			final Player pl = sh.getPlayer();
			if (pl == null) continue;
			final SmartInventory si = InventoryManager.getInventory(pl).orElse(null);
			if (si != null && si.getProvider() instanceof GameMenu) {
				pl.closeInventory();
				gameInv.open(pl);
			}
		}
	}
	
	@Slow(priority = 2)
	public XYZ getClosestPos(final XYZ loc, final int dst) {
		final XYZ lc = loc.clone().add(Main.srnd.nextBoolean() ? dst : -dst, 
			0, Main.srnd.nextBoolean() ? dst : -dst);
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
			+ "§7:" + cc + (t % 60 > 9 ? t % 60 : "0" + (t % 60));
	}

	public short getTime() {
		return time;
	}
	
	/*@Slow(priority = 1)
	protected static void editLr(final ItemStack it, final boolean add, final String lmnt) {
		final ItemMeta im = it.getItemMeta();
		final List<String> lr = im.lore().stream().map(TCUtil::deform).collect(Collectors.toList());
		if (add) lr.add(lmnt);
		else lr.remove(lmnt);
		im.lore(lr.stream().map(TCUtil::form).collect(Collectors.toList()));
		it.setItemMeta(im);
	}*/
}
