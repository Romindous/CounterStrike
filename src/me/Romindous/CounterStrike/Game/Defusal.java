package me.Romindous.CounterStrike.Game;

import me.Romindous.CounterStrike.Enums.GameState;
import me.Romindous.CounterStrike.Enums.GameType;
import me.Romindous.CounterStrike.Enums.GunType;
import me.Romindous.CounterStrike.Listeners.MainLis;
import me.Romindous.CounterStrike.Main;
import me.Romindous.CounterStrike.Objects.Defusable;
import me.Romindous.CounterStrike.Objects.Game.Bomb;
import me.Romindous.CounterStrike.Objects.Game.BtShooter;
import me.Romindous.CounterStrike.Objects.Game.PlShooter;
import me.Romindous.CounterStrike.Objects.Game.TripWire;
import me.Romindous.CounterStrike.Objects.Loc.BrknBlck;
import me.Romindous.CounterStrike.Objects.Shooter;
import me.Romindous.CounterStrike.Objects.Skins.Quest;
import me.Romindous.CounterStrike.Menus.ChosenSkinMenu;
import me.Romindous.CounterStrike.Utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import ru.komiss77.ApiOstrov;
import ru.komiss77.enums.Stat;
import ru.komiss77.modules.items.ItemBuilder;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.world.WXYZ;
import ru.komiss77.modules.world.XYZ;
import ru.komiss77.utils.ClassUtil;
import ru.komiss77.utils.ItemUtil;
import ru.komiss77.utils.TCUtil;
import ru.komiss77.version.Nms;

import java.util.ArrayList;
import java.util.Map.Entry;

public class Defusal extends Arena {

	public final XYZ ast;
	public final XYZ bst;
	private final TextDisplay ads;
	private final TextDisplay bds;
	private WXYZ bLoc;
	private Bomb bmb;
	private int TBns;
	private int CTBns;
	private final Team[] wns;
	public boolean indon;
	
	public Defusal(final String name, final byte min, final byte max, 
		final XYZ[] TSpawns, final XYZ[] CTSpawns, final XYZ[] spots, 
		final org.bukkit.World w, final XYZ ast, final XYZ bst, final byte wnr, 
		final boolean rnd, final boolean bots) {
		super(name, min, max, TSpawns, CTSpawns, spots, w, rnd, bots);
		this.ast = ast;
		this.bst = bst;
		wns = new Team[(wnr << 1) - 1];
		bmb = null;
		TBns = 0;
		CTBns = 0;
		ads = w.spawn(new Location(w, ast.x + 0.5d, ast.y + 2d, ast.z + 0.5d), TextDisplay.class, it -> {
			it.setVisibleByDefault(false);
			it.text(TCUtil.form("§b§lA\n§4§l↳§c§lx§4§l↲"));
			final Transformation tr = it.getTransformation();
			it.setTransformation(new Transformation(tr.getTranslation(), tr.getLeftRotation(),
				new Vector3f(8f, 8f, 8f), tr.getRightRotation()));
			it.setBillboard(Display.Billboard.VERTICAL);
			it.setBackgroundColor(Color.fromARGB(0));
			it.setViewRange(200f);
			it.setSeeThrough(true);
			it.setShadowed(true);
			it.setGravity(false);
		});

		bds = w.spawn(new Location(w, bst.x + 0.5d, bst.y + 2d, bst.z + 0.5d), TextDisplay.class, it -> {
			it.setVisibleByDefault(false);
			it.text(TCUtil.form("§6§lB\n§4§l↳§c§lx§4§l↲"));
			final Transformation tr = it.getTransformation();
			it.setTransformation(new Transformation(tr.getTranslation(), tr.getLeftRotation(),
					new Vector3f(8f, 8f, 8f), tr.getRightRotation()));
			it.setBillboard(Display.Billboard.VERTICAL);
			it.setBackgroundColor(Color.fromARGB(0));
			it.setViewRange(200f);
			it.setSeeThrough(true);
			it.setShadowed(true);
			it.setGravity(false);
		});

		this.gst = GameState.WAITING;
		this.bLoc = null;
		updateData();
	}

	@Override
	public boolean addPl(final PlShooter sh) {
		sh.kills0();
		sh.spwnrs0();
		sh.deaths0();
		sh.money(0);
		sh.arena(this);
		final Player p = sh.getPlayer();
		Main.nrmlzPl(p, true);
		switch (gst) {
			case WAITING:
				chngTeam(sh, Team.SPEC);
				sh.item(2, new ItemBuilder(ItemType.NETHER_STAR).name("§eВыбор Комманды").build());
				sh.item(5, new ItemBuilder(ItemType.HEART_OF_THE_SEA).name("§чБоторейка").build());
				sh.item(6, Main.mkItm(ItemType.GHAST_TEAR, "§5Магазин", Shooter.SHOP_MDL));
				sh.item(8, new ItemBuilder(ItemType.SLIME_BALL).name("§cВыход").build());
				if (shtrs.size() == min) {
					for (final Entry<Shooter, Team> en : shtrs.entrySet()) {
						final Player pl = en.getKey().getPlayer();
						if (pl != null) {
							beginScore(pl, en.getValue());
							pl.sendMessage(Main.prf() + "§7Игрок §5" + sh.name() + " §7зашел на карту!");
							Utils.sendAcBr(pl, "§7Игроков: §5" + shtrs.size() + " §7из §5" + max + " §7(макс)!");
							if (!rnd) pl.teleport(Main.getNrLoc(Main.srnd.nextBoolean() ? ClassUtil.rndElmt(TSpawns) : ClassUtil.rndElmt(CTSawns), w));
							pl.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING,
								600, 1, true, false, false));
						}
					}
					cntBeg();
				} else {
					final int rm = min - shtrs.size();
					waitScore(p, rm);
					for (final Shooter s : shtrs.keySet()) {
						final Player pl = s.getPlayer();
						if (pl != null) {
							pl.sendMessage(Main.prf() + "§7Игрок §5" + sh.name() + " §7зашел на карту!");
							Utils.sendAcBr(pl, "§7Нужно еще §d" + rm + " §7игроков для начала!");
							PM.getOplayer(pl).score.getSideBar().update(LIMIT, "§7Ждем еще §5" + rm + (rm > 1 ? " §7игроков" : " §7игрокa"));
						}
					}
				}
				break;
			case BEGINING:
				sh.item(2, new ItemBuilder(ItemType.NETHER_STAR).name("§eВыбор Комманды").build());
				sh.item(5, new ItemBuilder(ItemType.HEART_OF_THE_SEA).name("§чБоторейка").build());
				sh.item(6, Main.mkItm(ItemType.GHAST_TEAR, "§5Магазин", Shooter.SHOP_MDL));
				sh.item(8, new ItemBuilder(ItemType.SLIME_BALL).name("§cВыход").build());
				if (!rnd) p.teleport(Main.getNrLoc(Main.srnd.nextBoolean() ? ClassUtil.rndElmt(TSpawns) : ClassUtil.rndElmt(CTSawns), w));
				p.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING,
					time * 20, 1, true, false, false));
				beginScore(p, Team.SPEC);
				chngTeam(sh, Team.SPEC);
				for (final Entry<Shooter, Team> e : shtrs.entrySet()) {
					final Player pl = e.getKey().getPlayer();
					if (pl != null) {
						pl.sendMessage(Main.prf() + "§7Игрок §5" + sh.name() + " §7зашел на карту!");
						Utils.sendAcBr(pl, "§7Игроков: §5" + shtrs.size() + " §7из §5" + max + " §7(макс)!");
					}
				}
				break;
			case BUYTIME:
			case ROUND:
			case ENDRND:
				if (!shtrs.containsKey(sh)) {//spec
					gameScore(sh, p);
					chngTeam(sh, Team.SPEC);
					p.setGameMode(GameMode.SPECTATOR);
					p.teleport(Main.getNrLoc(Main.srnd.nextBoolean() ? ClassUtil.rndElmt(TSpawns) : ClassUtil.rndElmt(CTSawns), w));
					sh.item(2, new ItemBuilder(ItemType.NETHER_STAR).name("§eВыбор Комманды").build());
					sh.item(8, new ItemBuilder(ItemType.SLIME_BALL).name("§cВыход").build());
					for (final Entry<Shooter, Team> e : shtrs.entrySet()) {
						final Shooter s = e.getKey();
						final Player pl = s.getPlayer();
						if (pl != null) {
							pl.sendMessage(Main.prf() + "§7Игрок §5" + sh.name() + " §7зашел посмотреть!");
						}
					}
					break;
				}
				break;
			case FINISH:
				p.sendMessage(Main.prf() + "§cЭта игра уже заканчивается!");
				Main.lobbyPl(p);
				return false;
		}
		updateData();
		final Component tpl = TCUtil.form("§7Сейчас в игре: §d" + MainLis.getPlaying() + "§7 человек!");
		for (final Player pl : Bukkit.getOnlinePlayers()) {
			pl.sendPlayerListFooter(tpl);
		}
		return true;
	}

	public void addToTm(final Player p, final PlShooter sh, final Team tm) {
		p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
		p.teleport(Main.getNrLoc(tm == Team.Ts ? ClassUtil.rndElmt(TSpawns) : ClassUtil.rndElmt(CTSawns), w).add(0d, 2d, 0d));
		if (gst == GameState.BUYTIME) {
			final PlayerInventory pinv = p.getInventory();
			p.setGameMode(GameMode.SURVIVAL);
			pinv.setItem(8, Main.mkItm(ItemType.GHAST_TEAR, "§5Магазин", Shooter.SHOP_MDL));
			if (tm == Team.Ts) {
				pinv.setItem(2, Main.mkItm(ItemType.BLAZE_ROD, "§fНож \u9298", Shooter.KNIFE_MDL));
			} else {
				pinv.setItem(7, Main.mkItm(ItemType.GOLD_NUGGET, "§eКусачки §f\u9268", Defusable.PLIERS_MDL));
				pinv.setItem(2, Main.mkItm(ItemType.BLAZE_ROD, "§fНож \u9298", Shooter.KNIFE_MDL));
			}
			p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 240, 240, true, false, false));
			p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 40, 2, true, false, false));
		} else {
			p.setGameMode(GameMode.SPECTATOR);
			p.getInventory().setItem(8, new ItemBuilder(ItemType.SLIME_BALL).name("§cВыход").build());
		}

		gameScore(sh, p);
		chngMn(sh, (getRnd() & 3) == 0 ? 550 : 550 + (tm == Team.Ts ? TBns : CTBns));
		for (final Entry<Shooter, Team> e : shtrs.entrySet()) {
			final Player pl = e.getKey().getPlayer();
			if (pl != null) {
				pl.sendMessage(Main.prf() + "§7Игрок §5" + sh.name() + " §7зашел играть за комманду " + tm.icn + "§7!");
			}
		}

		for (final TripWire tw : tws) {
			if (tm == tw.tm) tw.showNade(p);
		}
	}

	@Override
	public boolean rmvPl(final PlShooter sh) {
		sh.kills0();
		sh.spwnrs0();
		sh.deaths0();
		sh.money(0);
		final Team tm = shtrs.remove(sh);
		final Player p = sh.getPlayer();
		if (tm == null) {
			p.sendMessage(Main.prf() + "§cВы не находитесь в игре!");
			return false;
		}
		switch (gst) {
			case WAITING:
				if (shtrs.size() > 0) {
					final int rm = min - shtrs.size();
					for (final Entry<Shooter, Team> e : shtrs.entrySet()) {
						final Player pl = e.getKey().getPlayer();
						if (pl != null) {
							pl.sendMessage(Main.prf() + "§7Игрок §5" + sh.name() + " §7вышел с карты!");
							Utils.sendAcBr(pl, "§7Нужно еще §d" + rm + " §7игроков для начала!");
							PM.getOplayer(p).score.getSideBar()
								.update(T_AMT, Team.Ts.icn + " §7: " + getTmAmt(Team.Ts, true, true)
									+ (e.getValue() == Team.Ts ? " §7чел. §8✦ Ты" : " §7чел."))
								.update(CT_AMT, Team.CTs.icn + " §7: " + getTmAmt(Team.CTs, true, true)
									+ (e.getValue() == Team.CTs ? " §7чел. §8✦ Ты" : " §7чел."))
								.update(LIMIT, "§7Ждем еще §5" + rm + (rm > 1 ? " §7игроков" : " §7игрокa"));
						}
					}
				} else {
					this.end();
				}
				break;
			case BEGINING:
				if (shtrs.size() < min) {
					gst = GameState.WAITING;
					if (tsk != null) {
						tsk.cancel();
					}
					for (final Shooter s : shtrs.keySet()) {
						final Player pl = s.getPlayer();
						if (pl != null) {
							waitScore(pl, min - 1);
							pl.teleport(Main.getNrLoc(Main.lobby));
							pl.sendMessage(Main.prf() + "§7Игрок §5" + sh.name() + " §7вышел с карты,\n§7Слишком мало игроков для начала!");
							Utils.sendAcBr(pl, "§7Игроков: §5" + shtrs.size() + " §7из §5" + max + " §7(макс)!");
							pl.removePotionEffect(PotionEffectType.GLOWING);
						}
					}
				} else {
					for (final Entry<Shooter, Team> e : shtrs.entrySet()) {
						final Player pl = e.getKey().getPlayer();
						if (pl != null) {
							pl.sendMessage(Main.prf() + "§7Игрок §5" + sh.name() + " §7вышел с карты!");
							Utils.sendAcBr(pl, "§7Игроков: §5" + shtrs.size() + " §7из §5" + max + " §7(макс)!");
							PM.getOplayer(p).score.getSideBar()
									.update(T_AMT, Team.Ts.icn + " §7: " + getTmAmt(Team.Ts, true, true)
											+ (e.getValue() == Team.Ts ? " §7чел. §8✦ Ты" : " §7чел."))
									.update(CT_AMT, Team.CTs.icn + " §7: " + getTmAmt(Team.CTs, true, true)
											+ (e.getValue() == Team.CTs ? " §7чел. §8✦ Ты" : " §7чел."));
						}
					}
				}
				break;
			case BUYTIME:
			case ROUND:
			case ENDRND:
				if (tm == Team.SPEC) {
					for (final Shooter s : shtrs.keySet()) {
						final Player pl = s.getPlayer();
						if (pl != null) {
							pl.sendMessage(Main.prf() + "§7Игрок §5" + sh.name() + " §7вышел из просмотра!");
						}
					}
					break;
				}

				sh.drop(p.getLocation());
				if (getTmAmt(Team.CTs, false, false) == 0 || getTmAmt(Team.Ts, false, false) == 0) {
					if (tsk != null) {
						tsk.cancel();
					}
					for (final Shooter s : shtrs.keySet()) {
						final Player pl = s.getPlayer();
						if (pl != null) {
							winScore(s, pl, tm == Team.Ts);
							pl.sendMessage(Main.prf() + "§7Игрок §5" + sh.name() + " §7вышел из игры!");
						}
					}
					cntFnsh(tm.getOpst());
				} else {
					for (final Entry<Shooter, Team> e : shtrs.entrySet()) {
						final Player pl = e.getKey().getPlayer();
						if (pl != null) {
							pl.sendMessage(Main.prf() + "§7Игрок §5" + sh.name() + " §7вышел из игры!");
							PM.getOplayer(pl).score.getSideBar()
									.update(T_AMT, Team.Ts.icn + " §7: " + getTmAmt(Team.Ts, true, true)
											+ (e.getValue() == Team.Ts ? " §7чел. §8✦ Ты" : " §7чел."))
									.update(CT_AMT, Team.CTs.icn + " §7: " + getTmAmt(Team.CTs, true, true)
											+ (e.getValue() == Team.CTs ? " §7чел. §8✦ Ты" : " §7чел."));
						}
					}
				}
				break;
			case FINISH:
				if (shtrs.size() == 0) {
					this.time = 1;
				}
				break;
		}
		updateData();
		Main.lobbyPl(p);
		return true;
	}

	//счетчик
	public void cntBeg() {
		time = 30;
		gst = GameState.BEGINING;
		final Arena ar = this;
		tsk = new BukkitRunnable() {
			@Override
			public void run() {
				final String rtm = "§7Начало через: §5" + String.valueOf(time);
				switch (time) {
				case 10, 5, 4:
					for (final Shooter sh : shtrs.keySet()) {
						final Player pl = sh.getPlayer();
						if (pl != null) {
							pl.playSound(pl.getLocation(), Sound.BLOCK_DISPENSER_FAIL, 0.8f, 1.2f);
							Utils.sendSbTtl(pl, "§5§l" + time, 10);
							PM.getOplayer(pl).score.getSideBar().update(LIMIT, rtm);
						}
					}
					break;
				case 3, 2, 1:
					for (final Shooter sh : shtrs.keySet()) {
						final Player pl = sh.getPlayer();
						if (pl != null) {
							pl.playSound(pl.getLocation(), Sound.BLOCK_DISPENSER_FAIL, 0.8f, 1.4f);
							Utils.sendSbTtl(pl, "§d§l" + time, 10);
							PM.getOplayer(pl).score.getSideBar().update(LIMIT, rtm);
						}
					}
					break;
				case 0:
					if (tsk != null) {
						tsk.cancel();
					}
					final ArrayList<Shooter> sts = new ArrayList<>();
					for (final Entry<Shooter, Team> e : shtrs.entrySet()) {
						if (e.getValue() == Team.SPEC) sts.add(e.getKey());
					}
					for (final Shooter s : sts) chngTeam(s, getMinTm());
					cntBuy(true);
					if (rnd) Main.mapBlds.get(name).placeSets(ar, 5);
					break;
				default:
					for (final Shooter sh : shtrs.keySet()) {
						final Player pl = sh.getPlayer();
						if (pl != null) {
							PM.getOplayer(pl).score.getSideBar().update(LIMIT, rtm);
						}
					}
					break;
				}
				time--;
			}
		}.runTaskTimer(Main.plug, 20, 20);
	}

	public void cntBuy(final boolean pstl) {
		time = 10;
		TBns = getBns(Team.Ts);
		CTBns = getBns(Team.CTs);
		gst = GameState.BUYTIME;
		updateData();

		for (final Entity e : w.getEntitiesByClasses(Item.class, ArmorStand.class, Turtle.class)) {
			e.remove();
		}
		
		Shooter bc = null;
		blncTms();
		if (pstl) {
			if (wns[0] != null) swtchTms();
			for (final Entry<Shooter, Team> e : shtrs.entrySet()) {
				final Shooter sh = e.getKey();
				sh.money(0);
				if (sh instanceof PlShooter) {
					final Player p = sh.getPlayer();
					gameScore(sh, p);
					Utils.sendTtlSbTtl(p, getRnd() == 0 ? "" : "§eСмена Ролей", "§l§6⛃§5Закупка§6⛃", 30);
					if (e.getValue() == Team.SPEC) continue;

					Main.nrmlzPl(p, true);
				} else if (sh instanceof BtShooter) {
					((BtShooter) sh).willBuy = true;
					sh.clearInv();
				}
				switch (e.getValue()) {
				case Ts:
					sh.teleport(sh.getEntity(), Main.getNrLoc(ClassUtil.rndElmt(TSpawns), w));
					if (bc == null || sh.kills() > bc.kills()) bc = sh;
					sh.item(8, Main.mkItm(ItemType.GHAST_TEAR, "§5Магазин", Shooter.SHOP_MDL));
					sh.item(2, Main.mkItm(ItemType.BLAZE_ROD, "§fНож \u9298", Shooter.KNIFE_MDL));
					chngMn(sh, 550);
					break;
				case CTs:
					sh.teleport(sh.getEntity(), Main.getNrLoc(ClassUtil.rndElmt(CTSawns), w));
					sh.item(7, Main.mkItm(ItemType.GOLD_NUGGET, "§eКусачки §f\u9268", Defusable.PLIERS_MDL));
					sh.item(8, Main.mkItm(ItemType.GHAST_TEAR, "§5Магазин", Shooter.SHOP_MDL));
					sh.item(2, Main.mkItm(ItemType.BLAZE_ROD, "§fНож \u9298", Shooter.KNIFE_MDL));
					chngMn(sh, 550);
					break;
				case SPEC:
					break;
				}
				sh.taq(e.getValue().icn + " ", " §7[" + sh.kills() + "-" + sh.deaths() + "]", e.getValue().clr);
				sh.getEntity().addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS,
					240, 240, true, false, false));
				sh.getEntity().addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING,
					40, 2, true, false, false));
			}
		} else {
			for (final Entry<Shooter, Team> e : shtrs.entrySet()) {
				final Shooter sh = e.getKey();
				if (sh instanceof PlShooter) {
					final Player p = sh.getPlayer();
					if (p.getGameMode() == GameMode.SPECTATOR) {
						gameScore(sh, p);
						Utils.sendSbTtl(p, "§l§6⛃§5Закупка§6⛃", 30);
						if (e.getValue() == Team.SPEC) continue;

						Main.nrmlzPl(p, true);
						sh.item(8, Main.mkItm(ItemType.GHAST_TEAR, "§5Магазин", Shooter.SHOP_MDL));
						switch (e.getValue()) {
						case Ts:
							sh.teleport(p, Main.getNrLoc(ClassUtil.rndElmt(TSpawns), w));
							if (bc == null || sh.kills() > bc.kills()) {
								 bc = sh;
							}
							sh.item(2, Main.mkItm(ItemType.BLAZE_ROD, "§fНож \u9298", Shooter.KNIFE_MDL));
							chngMn(sh, 550 + TBns);
							break;
						case CTs:
							sh.teleport(p, Main.getNrLoc(ClassUtil.rndElmt(CTSawns), w));
							sh.item(7, Main.mkItm(ItemType.GOLD_NUGGET, "§eКусачки §f\u9268", Defusable.PLIERS_MDL));
							sh.item(2, Main.mkItm(ItemType.BLAZE_ROD, "§fНож \u9298", Shooter.KNIFE_MDL));
							chngMn(sh, 550 + CTBns);
							break;
						case SPEC:
							break;
						}
					} else {
						Main.nrmlzPl(p, false);
						Utils.sendSbTtl(p, "§l§6⛃§5Закупка§6⛃", 30);
						final ItemStack it0 = sh.item(0);
						if (!ItemUtil.isBlank(it0, false)) {
							final GunType gt = GunType.get(it0);
							if (gt != null) it0.setAmount(GunType.get(it0).amo);
							sh.item(0, it0);
						}
						final ItemStack it1 = sh.item(1);
						if (!ItemUtil.isBlank(it1, false)) {
							final GunType gt = GunType.get(it1);
							if (gt != null) it1.setAmount(GunType.get(it1).amo);
							sh.item(1, it1);
						}
						switch (e.getValue()) {
						case Ts:
							if (!ItemUtil.isBlank(sh.item(7), false)) {
								sh.item(7, Main.air);
								if (indon) {
									indSts(p);
								}
							}
							sh.teleport(p, Main.getNrLoc(ClassUtil.rndElmt(TSpawns), w));
							if (bc == null || sh.kills() > bc.kills()) {
								 bc = sh;
							}
							chngMn(sh, 550 + TBns);
							break;
						case CTs:
							sh.teleport(p, Main.getNrLoc(ClassUtil.rndElmt(CTSawns), w));
							chngMn(sh, 550 + CTBns);
							break;
						case SPEC:
							break;
						}
						PM.getOplayer(p).score.getSideBar()
							.update(SCORE, Team.Ts.icn + " §7: " + Team.Ts.clr + getWns(Team.Ts)
								+ " §7-=x=- " + Team.CTs.clr + getWns(Team.CTs) + " §7: " + Team.CTs.icn)
							.update(MONEY, "§7Монет: §d" + sh.money() + " §6⛃")
							.update(STAGE, "§7Cтадия: §dЗакупка");
					}
					p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 240, 240, true, false, false));
					p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 40, 2, true, false, false));
					sh.taq(e.getValue().icn + " ", " §7[" + sh.kills() + "-" + sh.deaths() + "]", e.getValue().clr);
				} else {
					((BtShooter) sh).willBuy = true;
					final ItemStack it0 = sh.item(0);
					if (!ItemUtil.isBlank(it0, false)) {
						it0.setAmount(GunType.get(it0).amo);
						sh.item(0, it0);
					}
					final ItemStack it1 = sh.item(1);
					if (!ItemUtil.isBlank(it1, false)) {
						it1.setAmount(GunType.get(it1).amo);
						sh.item(1, it1);
					}
					switch (e.getValue()) {
					case Ts:
						sh.teleport(sh.getEntity(), Main.getNrLoc(ClassUtil.rndElmt(TSpawns), w));
						sh.item(2, Main.mkItm(ItemType.BLAZE_ROD, "§fНож \u9298", Shooter.KNIFE_MDL));
						chngMn(sh, 550 + TBns);
						if (bc == null || sh.kills() > bc.kills()) {
							 bc = sh;
						}
						break;
					case CTs:
						sh.teleport(sh.getEntity(), Main.getNrLoc(ClassUtil.rndElmt(CTSawns), w));
						sh.item(7, Main.mkItm(ItemType.GOLD_NUGGET, "§eКусачки §f\u9268", Defusable.PLIERS_MDL));
						sh.item(2, Main.mkItm(ItemType.BLAZE_ROD, "§fНож \u9298", Shooter.KNIFE_MDL));
						chngMn(sh, 550 + CTBns);
						break;
					case SPEC:
						break;
					}
					sh.getEntity().addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS,
						240, 240, true, false, false));
					sh.getEntity().addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING,
						40, 2, true, false, false));
					sh.taq(e.getValue().icn + " ", " §7[" + sh.kills() + "-" + sh.deaths() + "]", e.getValue().clr);
				}
			}
		}
		
		if (noBmb()) {
			bmb = null;
			bc.item(7, Main.bmb);
			//Bukkit.broadcast(Component.text("bmb-" + bc.name()));
		}
		tsk = new BukkitRunnable() {
			@Override
			public void run() {
				final String rtm = getTime(time, "§5") + " §7до конца!";
//				for (final LivingEntity le : w.getLivingEntities()) {
//					le.setFireTicks(-1);
//				}
				switch (time) {
				case 3, 2, 1:
					for (final Shooter sh : shtrs.keySet()) {
						final Player p = sh.getPlayer();
						if (p != null) {
							p.playSound(p.getLocation(), Sound.BLOCK_DISPENSER_FAIL, 0.8f, 1.2f);
							Utils.sendSbTtl(p, "§d§l" + time, 10);
							PM.getOplayer(p).score.getSideBar().update(LIMIT, rtm);
						}
					}
					break;
				case 0:
					if (tsk != null) {
						tsk.cancel();
					}
					
					cntRnd();
					break;
				default:
					for (final Shooter sh : shtrs.keySet()) {
						final Player p = sh.getPlayer();
						if (p != null) {
							PM.getOplayer(p).score.getSideBar().update(LIMIT, rtm);
						}
					}
					break;
				}
				time--;
			}
		}.runTaskTimer(Main.plug, 20, 20);
	}

	private void swtchTms() {
		for (final Entry<Shooter, Team> e : shtrs.entrySet()) {
			e.setValue(e.getValue().getOpst());
			if (indon) {
				final Player p = e.getKey().getPlayer();
				if (p != null) {
					indSts(p);
				}
			}
		}
		
		for (byte i = (byte) (wns.length - 1); i >= 0; i--) {
			wns[i] = wns[i] == null ? null : wns[i].getOpst();
		}
	}

	private void cntRnd() {
		time = 120;
		bLoc = null;
		gst = GameState.ROUND;
		updateData();

		for (final Entry<Shooter, Team> e : shtrs.entrySet()) {
			final Player p = e.getKey().getPlayer();
			if (p != null) {
				Utils.sendSbTtl(p, e.getValue().icn.substring(0, 2) + "§lВперед", 30);
				PM.getOplayer(p).score.getSideBar().update(STAGE, "§7Cтадия: §5Бой");
				p.playSound(p, "info." + e.getValue().goSnd, 10f, 1f);
				p.removePotionEffect(PotionEffectType.SLOWNESS);
			}
		}

		final Defusal ar = this;
		tsk = new BukkitRunnable() {
			@Override
			public void run() {
				final String rtm;
				if (bmb == null || (time > 10 && (time & 1) == 0)) {
					rtm = getTime(time, "§d") + " §7до конца!";
				} else {
					rtm = getTime(time, "§c") + " §7до конца!";
					Main.plyWrldSnd(bmb.getCenterLoc(), "rand.bmbbeep", 1f);
				}
				switch (time) {
				case 60:
					for (final Shooter sh : shtrs.keySet()) {
						final Player p = sh.getPlayer();
						if (p != null) {
							Utils.sendAcBr(p, "§7Осталась §d1 §7минута!");
							PM.getOplayer(p).score.getSideBar().update(LIMIT, rtm);
						}
					}
					break;
				case 30:
					for (final Entry<Shooter, Team> e : shtrs.entrySet()) {
						final Player p = e.getKey().getPlayer();
						if (p != null) {
							Utils.sendAcBr(p, "§7Осталось §d" + time + " §7секунд!");
							PM.getOplayer(p).score.getSideBar().update(LIMIT, rtm);
							p.playSound(p.getLocation(), "info." + (e.getValue() == Team.Ts ? "t30sec" : "ct30sec"), 10f, 1f);
						}
					}
					break;
				case 10:
					for (final Shooter sh : shtrs.keySet()) {
						final Player p = sh.getPlayer();
						if (p != null) {
							Utils.sendAcBr(p, "§7Осталось §d" + time + " §7секунд!");
							PM.getOplayer(p).score.getSideBar().update(LIMIT, rtm);
						}
					}
					break;
				case 0:
					if (bmb == null) {
						rndWn(Team.CTs, "§9⌛", "§7§lВремя вышло, §3Спецназ §7победил!", "notime");
					} else {
						bmb.expld();
						bmb = null;
						rndWn(Team.Ts, "§c\u9299", "§7§lБомба взорвалась, §4Террористы §7победили!", "kaboom");
					}
					break;
				default:
					for (final Shooter sh : shtrs.keySet()) {
						final Player p = sh.getPlayer();
						if (p != null) {
							PM.getOplayer(p).score.getSideBar().update(LIMIT, rtm);
						}
					}
					break;
				}
				time--;
			}
		}.runTaskTimer(Main.plug, 20, 20);
	}

	private void rndWn(final Team tm, final String ttl, final String sbttl, final String snd) {
		if (tsk != null) {
			tsk.cancel();
		}

		for (final BrknBlck bb : brkn) {
			bb.getBlock().setBlockData(bb.bd, false);
		}
		brkn.clear();

		for (final TripWire tw : tws) {
			tw.remove();
		}
		tws.clear();
		
		Shooter bt = null;
		Shooter bct = null;
		for (final Entry<Shooter, Team> e : shtrs.entrySet()) {
			final Player p = e.getKey().getPlayer();
			if (p != null) {
				if (e.getValue() == tm) {
					ApiOstrov.addStat(p, Stat.CS_win);
					ChosenSkinMenu.tryCompleteQuest(e.getKey(), Quest.ГРУЗЧИК, ApiOstrov.getStat(p, Stat.CS_win));
				} else {
					ApiOstrov.addStat(p, Stat.CS_loose);
				}
			}
			switch (e.getValue()) {
			case Ts:
				if (bt == null || bt.kills() < e.getKey().kills()) {
					bt = e.getKey();
				}
				break;
			case CTs:
				if (bct == null || bct.kills() < e.getKey().kills()) {
					bct = e.getKey();
				}
				break;
			case SPEC:
				break;
			}
		}
		
		if (bmb != null) {
			bmb.title.remove();
			bmb.getBlock().setType(Material.AIR, false);
			bmb = null;
		}
		
		addWn(tm);
		if (getWns(tm) == wns.length / 2 + 1) {
			for (final Shooter sh : shtrs.keySet()) {
				final Player p = sh.getPlayer();
				if (p != null) {
					p.closeInventory();
					p.sendMessage(" \n§7Победа в раунде: " + (tm == Team.CTs ? "§3Спецназ" : "§4Террористы")
						+ "\n \n§5=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-\n§7Лучший спецназовец: §3" + bct.name() + 
						"\n          §7-=x=-\n§7Самый злобный террорист: §4" + bt.name() + "\n§5=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-");
				}
			}
			cntFnsh(tm);
		} else {
			for (final Entry<Shooter, Team> e : shtrs.entrySet()) {
				final Player p = e.getKey().getPlayer();
				if (p != null) {
					p.closeInventory();
					PM.getOplayer(p).score.getSideBar().update(LIMIT, "§d00:00 §7до конца!");
					Utils.sendTtlSbTtl(p, ttl, sbttl, 30);
					p.playSound(p.getLocation(), "info." + (e.getValue() == Team.Ts ? "t" : "ct") + snd, 10f, 1f);
					p.sendMessage(" \n§7Победа в раунде: " + (tm == Team.CTs ? "§3Спецназ" : "§4Террористы")
						+ "\n \n§5=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-\n§7Лучший спецназовец: §3" + bct.name() + 
						"\n          §7-=x=-\n§7Самый злобный террорист: §4" + bt.name() + "\n§5=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-");
				}
			}
			gst = GameState.ENDRND;
			new BukkitRunnable() {
				@Override
				public void run() {
					if (tsk.isCancelled()) {
						cntBuy((getRnd() & 3) == 0);
					}
				}
			}.runTaskLater(Main.plug, 100);
		}
	}

	private void cntFnsh(final Team wn) {
		time = 10;
		gst = GameState.FINISH;
		for (final Entity e : w.getEntitiesByClasses(Item.class, ArmorStand.class, Turtle.class)) {
			e.remove();
		}
		updateData();

		for (final BrknBlck bb : brkn) {
			bb.getBlock().setBlockData(bb.bd, false);
		}
		brkn.clear();

		for (final TripWire tw : tws) {
			tw.remove();
		}
		tws.clear();
		
		if (bmb != null) {
			bmb.title.remove();
			bmb.getBlock().setType(Material.AIR, false);
			bmb = null;
		}
		
		final String st = switch (wn) {
            case Ts -> "§4§lТеррористы §7выйграли!";
            case CTs -> "§3§lСпецназ §7одержал победу!";
            case SPEC -> " ";
        };

        for (final Entry<Shooter, Team> e : shtrs.entrySet()) {
			final Shooter sh = e.getKey();
			if (sh instanceof PlShooter) {
				final Player p = sh.getPlayer();
				p.closeInventory();
				p.playSound(p, "info." + e.getValue().finSnd, 10f, 1f);
				Utils.sendTtlSbTtl(p, "§5Финиш", st, 40);
				sh.taq("§7<§d" + name + "§7> ", " §7[" + sh.kills() + "-" + sh.deaths() + "]", Team.SPEC.clr);
				if (indon && sh.inv().contains(Material.GOLDEN_APPLE)) {
					indSts(p);
				}
				winScore(sh, p, wn == Team.CTs);
				if (e.getValue() == Team.SPEC) continue;

				ApiOstrov.addStat(p, Stat.CS_game);
				ChosenSkinMenu.tryCompleteQuest(sh, Quest.ДУША, ApiOstrov.getStat(p, Stat.CS_game));
				sh.clearInv();
			} else {
				((BtShooter) sh).own().remove();
			}
		}

		tsk = new BukkitRunnable() {
			@Override
			public void run() {
				final String rtm = getTime(time, "§5") + " §7до конца!";
				if (time == 0) {
					if (tsk != null) {
						tsk.cancel();
					}

					end();
				} else {
					final FireworkEffect fe = switch (wn) {
                        case Ts -> FireworkEffect.builder().with(Type.CREEPER)
							.withColor(Color.MAROON).withFlicker().build();
                        case CTs -> FireworkEffect.builder().with(Type.STAR)
							.withColor(Color.TEAL).withFlicker().build();
                        case SPEC -> FireworkEffect.builder().with(Type.BURST)
							.withColor(Color.GRAY).withFlicker().build();
                    };

                    for (final Entry<Shooter, Team> e : shtrs.entrySet()) {
						final Player pl = e.getKey().getPlayer();
						if (pl != null) {
							PM.getOplayer(pl).score.getSideBar().update(LIMIT, rtm);
							if (e.getValue() == wn) {
								final Firework fw = (Firework) pl.getWorld().spawnEntity(pl.getEyeLocation(), EntityType.FIREWORK_ROCKET);
								final FireworkMeta fm = fw.getFireworkMeta();
								fm.addEffect(fe);
								fw.setFireworkMeta(fm);
							}
						}
					}
				}
				time--;
			}
		}.runTaskTimer(Main.plug, 20, 20);
	}

	private void waitScore(final Player pl, final int rm) {
		PM.getOplayer(pl).score.getSideBar().reset().title("§7[§5CS:GO§7]")
			.add(" ")
			.add("§7Карта: §5" + name)
			.add("§7Режим: §d" + getType().name)
			.add("§7=-=-=-=-=-=-=-")
			.add("§7Комманды:")
			.add(T_AMT, Team.Ts.icn + " §7: " + getTmAmt(Team.Ts, true, true) + " §7чел.")
			.add(" ")
			.add(CT_AMT, Team.CTs.icn + " §7: " + getTmAmt(Team.CTs, true, true) + " §7чел.")
			.add("§7=-=-=-=-=-=-=-")
			.add(LIMIT, "§7Ждем еще §5" + rm + (rm > 1 ? " §7игроков" : " §7игрокa"))
			.add(" ")
			.add("§e   ostrov77.ru").build();
	}

	private void beginScore(final Player pl, final Team tm) {
		PM.getOplayer(pl).score.getSideBar().reset().title("§7[§5CS:GO§7]")
			.add(" ")
			.add("§7Карта: §5" + name)
			.add("§7Режим: §d" + getType().name)
			.add("§7=-=-=-=-=-=-=-")
			.add("§7Комманды:")
			.add(T_AMT, Team.Ts.icn + " §7: " + getTmAmt(Team.Ts, true, true)
				+ (tm == Team.Ts ? " §7чел. §8✦ Ты" : " §7чел."))
			.add(" ")
			.add(CT_AMT, Team.CTs.icn + " §7: " + getTmAmt(Team.CTs, true, true)
				+ (tm == Team.CTs ? " §7чел. §8✦ Ты" : " §7чел."))
			.add("§7=-=-=-=-=-=-=-")
			.add(LIMIT, "§7Начало через: §5" + String.valueOf(time))
			.add(" ")
			.add("§e   ostrov77.ru").build();
	}
	
	private void gameScore(final Shooter sh, final Player pl) {
		final Team tm = shtrs.get(sh);
		PM.getOplayer(pl).score.getSideBar().reset().title("§7[§5CS:GO§7]")
			.add(SCORE, Team.Ts.icn + " §7: " + Team.Ts.clr + getWns(Team.Ts) + " §7-=x=- "
				+ Team.CTs.clr + getWns(Team.CTs) + " §7: " + Team.CTs.icn)
			.add(" ")
			.add("§7Карта: §5" + name)
			.add("§7Режим: §d" + getType().name)
			.add("§7=-=-=-=-=-=-=-")
			.add(STAGE, "§7Cтадия: " + (gst == GameState.BUYTIME ? "§5Закупка" : "§5Бой"))
			.add(LIMIT, getTime(time, "§d") + " §7до конца!")
			.add(" ")
			.add(T_AMT, Team.Ts.icn + " §7: " + getTmAmt(Team.Ts, true, true)
					+ (tm == Team.Ts ? " §7чел. §8✦ Ты" : " §7чел."))
			.add(CT_AMT, Team.CTs.icn + " §7: " + getTmAmt(Team.CTs, true, true)
					+ (tm == Team.CTs ? " §7чел. §8✦ Ты" : " §7чел."))
			.add("§7=-=-=-=-=-=-=-")
			.add(MONEY, "§7Монет: §d" + sh.money() + " §6⛃")
			.add(" ")
			.add("§e   ostrov77.ru").build();
	}

	private void winScore(final Shooter sh, final Player pl, final boolean isCTWin) {
		final Team tm = shtrs.get(sh);
		PM.getOplayer(pl).score.getSideBar().reset().title("§7[§5CS:GO§7]")
			.add(SCORE, isCTWin ? Team.Ts.icn + " §7: " + Team.Ts.clr + getWns(Team.Ts) + " §7-=x=- "
				+ Team.CTs.clr + ((wns.length >> 1) + 1) + " §7: " + Team.CTs.icn
				: Team.Ts.icn + " §7: " + Team.Ts.clr + ((wns.length >> 1) + 1) + " §7-=x=- "
				+ Team.CTs.clr + getWns(Team.CTs) + " §7: " + Team.CTs.icn)
			.add(" ")
			.add("§7Карта: §5" + name)
			.add("§7Режим: §d" + getType().name)
			.add("§7=-=-=-=-=-=-=-")
			.add("§7Cтадия: §dФиниш")
			.add(LIMIT, getTime(time, "§d") + " §7до конца!")
				.add(" ")
			.add(T_AMT, Team.Ts.icn + " §7: " + getTmAmt(Team.Ts, true, true)
					+ (tm == Team.Ts ? " §7чел. §8✦ Ты" : " §7чел."))
			.add(CT_AMT, Team.CTs.icn + " §7: " + getTmAmt(Team.CTs, true, true)
					+ (tm == Team.CTs ? " §7чел. §8✦ Ты" : " §7чел."))
			.add("§7=-=-=-=-=-=-=-")
			.add(MONEY, "§7Монет: §d" + sh.money() + " §6⛃")
			.add(" ")
			.add("§e   ostrov77.ru").build();
	}

	@Override
	public void killSh(final Shooter sh) {
		final LivingEntity le = sh.getEntity();
		if (gst == GameState.ROUND) {
			addDth(sh);
			final Team tm = shtrs.get(sh);
			sh.drop(le.getLocation());
			if (sh instanceof PlShooter) {
				final Player p = sh.getPlayer();
				p.closeInventory();
				p.setGameMode(GameMode.SPECTATOR);
				final int n = getTmAmt(tm, true, true);
				switch (tm) {
				case Ts:
					for (final Entry<Shooter, Team> e : shtrs.entrySet()) {
						final Player pl = e.getKey().getPlayer();
						if (pl != null) {
							PM.getOplayer(pl).score.getSideBar().update(T_AMT, Team.Ts.icn + " §7: " + n
								+ (e.getValue() == Team.Ts ? " §7чел. §8✦ Ты" : " §7чел."));
						}
					}
					if (n == 0 && bmb == null) {
						rndWn(Team.CTs, "§9\u9264", "§7§lТеррористы были убиты!", "winct");
					}
					break;
				case CTs:
					for (final Entry<Shooter, Team> e : shtrs.entrySet()) {
						final Player pl = e.getKey().getPlayer();
						if (pl != null) {
							PM.getOplayer(pl).score.getSideBar().update(CT_AMT, Team.CTs.icn + " §7: " + n
								+ (e.getValue() == Team.CTs ? " §7чел. §8✦ Ты" : " §7чел."));
						}
					}
					if (n == 0) {
						rndWn(Team.Ts, "§c\u9265", "§7§lСпецназ пал в бою!", "wint");
					}
					break;
				case SPEC:
					break;
				}
			} else {
				((BtShooter) sh).own().hide(le);
				final int n = getTmAmt(tm, true, true);
				switch (tm) {
				case Ts:
					for (final Entry<Shooter, Team> e : shtrs.entrySet()) {
						final Player pl = e.getKey().getPlayer();
						if (pl != null) {
							PM.getOplayer(pl).score.getSideBar().update(T_AMT, Team.Ts.icn + " §7: " + n
								+ (e.getValue() == Team.Ts ? " §7чел. §8✦ Ты" : " §7чел."));
						}
					}
					if (n == 0 && bmb == null) {
						rndWn(Team.CTs, "§9\u9264", "§7§lТеррористы были убиты!", "winct");
					}
					break;
				case CTs:
					for (final Entry<Shooter, Team> e : shtrs.entrySet()) {
						final Player pl = e.getKey().getPlayer();
						if (pl != null) {
							PM.getOplayer(pl).score.getSideBar().update(CT_AMT, Team.CTs.icn + " §7: " + n
								+ (e.getValue() == Team.CTs ? " §7чел. §8✦ Ты" : " §7чел."));
						}
					}
					if (n == 0) {
						rndWn(Team.Ts, "§c\u9265", "§7§lСпецназ пал в бою!", "wint");
					}
					break;
				case SPEC:
					break;
				}
			}
		} else {
			if (sh instanceof PlShooter) {
				final Player p = sh.getPlayer();
				if (p.hasPotionEffect(PotionEffectType.GLOWING)) {
					Main.nrmlzPl(p, false);
					p.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, time * 20, 1));
				} else {
					Main.nrmlzPl(p, false);
				}
				le.teleport(rnd ? Main.lobby.getCenterLoc() : (Main.getNrLoc(Main.srnd.nextBoolean() ?
						ClassUtil.rndElmt(TSpawns) : ClassUtil.rndElmt(CTSawns), w)));
			}
		}
	}

	@Override
	public void addKll(final Shooter sh) {
		sh.killsI();
		sh.taq(shtrs.get(sh).icn + " ", " §7[" + sh.kills() + "-" + sh.deaths() + "]", shtrs.get(sh).clr);
		final Player p = sh.getPlayer();
		if (p != null) {
			ChosenSkinMenu.tryCompleteQuest(sh, Quest.ВОЙ, sh.kills());
			ApiOstrov.addStat(p, Stat.CS_kill);
		}
	}

	@Override
	public void addDth(final Shooter sh) {
		sh.deathsI();
		sh.taq(shtrs.get(sh).icn + " ", " §7[" + sh.kills() + "-" + sh.deaths() + "]", shtrs.get(sh).clr);
		final Player p = sh.getPlayer();
		if (p != null) {
			ApiOstrov.addStat(p, Stat.CS_death);
		}
	}

	public void addWn(final Team cts) {
		for (byte i = (byte) (wns.length - 1); i >= 0; i--) {
			if (wns[i] != null) {
				wns[i + 1] = cts;
				return;
			} else if (i == 0) {
				wns[i] = cts;
			}
		}
	}

	public void defuse() {
		if (bmb == null) {
			Bukkit.broadcast(Component.text("no"));
		} else {
			bmb.title.remove();
			bmb.getBlock().setType(Material.AIR);
			bmb = null;
			rndWn(Team.CTs, "§9\u926f", "§3§lБомба разминирована!", "defuse");
		}
	}

	public void wrngWire() {
		time -= Bomb.WIRE_TIME;
		if (time < 1) {
			if (tsk != null) {
				tsk.cancel();
			}
			bmb.expld();
			bmb = null;
			rndWn(Team.Ts, "§c\u9299", "§7§lБомба взорвалась, §4Террористы §7победили!", "kaboom");
		}
	}

	public void plant(final Block b) {
		if (bmb == null) {
			bmb = new Bomb(b, this);
			time = 45;
			for (final Entry<Shooter, Team> e : shtrs.entrySet()) {
				final Player p = e.getKey().getPlayer();
				if (p != null) {
					p.closeInventory();
					Utils.sendTtlSbTtl(p, "§4\u926e", "§c§lБомба установлена!", 30);
					p.playSound(p.getLocation(), "info." + (e.getValue() == Team.Ts ? "tplant" : "ctplant"), 10f, 1f);
				}
			}
		} else {
			Bukkit.broadcast(Component.text("no"));
		}
	}

	public @Nullable Bomb getBomb() {
		return bmb;
	}

	public boolean isBmbOn() {
		return bmb != null;
	}

	public @Nullable WXYZ getBombDrop() {//bomb ITEM
		return bLoc;
	}

	public void indSts(final Player p) {
		if (indon) {
			p.hideEntity(Main.plug, ads);
			p.hideEntity(Main.plug, bds);
			indon = false;
		} else {
			p.showEntity(Main.plug, ads);
			p.showEntity(Main.plug, bds);
			indon = true;
		}
	}
	
	public void dropBomb(final Item bmb) {
		final Location loc = bmb.getLocation();
		for (final Entry<Shooter, Team> n : shtrs.entrySet()) {
			final Player pl = n.getKey().getPlayer();
			if (pl != null) {
				pl.playSound(loc, "info." + (n.getValue() == Team.Ts ? "tdropbmb" : "ctdropbmb"), 10f, 1f);
			}
		}
		bmb.setPersistent(true);
		Nms.colorGlow(bmb, NamedTextColor.DARK_RED, false);
		bLoc = new WXYZ(bmb.getLocation().add(bmb.getVelocity().setY(0d).multiply(10d)));
	}
	
	public void pickBomb() {
		bLoc = null;
		for (final Entry<Shooter, Team> n : shtrs.entrySet()) {
			final Player pl = n.getKey().getPlayer();
			if (pl != null) {
				if (n.getValue() == Team.Ts) {
					pl.playSound(pl.getLocation(), "info.tpkpbmb", 1f, 1f);
				}
			}
		}
	}

	public int getWns(final Team tm) {
		byte i = 0;
		for (final Team t : wns) {
			if (t == null) {
				return i;
			} else if (t == tm) {
				i++;
			}
		}
		return i;
	}
	
	private byte getRnd() {
		for (byte i = (byte) (wns.length - 1); i >= 0; i--) {
			if (wns[i] != null) {
				return (byte) (i + 1);
			}
		}
		return 0;
	}

	private int getBns(final Team tm) {
		int t = 4;
		int n = 0;
		boolean frst = true;
		for (int i = wns.length - 1; i >= 0; i--) {
			if (wns[i] != null) {
				if (wns[i] == tm) {
					if (frst) {
						n+=2;
						frst = false;
					}
				} else {
					if (frst) {
						frst = false;
					}
					n++;
				}
				if ((t--) == 0) {
					return n * 250;
				}
			}
		}
		return n * 250;
	}

	private boolean noBmb() {
		for (final Shooter sh : shtrs.keySet()) {
			if (sh.inv().contains(Material.GOLDEN_APPLE)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public GameType getType() {
		return GameType.DEFUSAL;
	}
}
