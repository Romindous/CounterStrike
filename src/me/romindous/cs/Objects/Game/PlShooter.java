package me.romindous.cs.Objects.Game;

import java.util.*;
import java.util.function.Predicate;
import me.romindous.cs.Enums.GameState;
import me.romindous.cs.Enums.GunType;
import me.romindous.cs.Game.Arena;
import me.romindous.cs.Game.Arena.Team;
import me.romindous.cs.Game.Defusal;
import me.romindous.cs.Game.Gungame;
import me.romindous.cs.Main;
import me.romindous.cs.Objects.EntityShootAtEntityEvent;
import me.romindous.cs.Objects.Loc.Broken;
import me.romindous.cs.Objects.Loc.Info;
import me.romindous.cs.Objects.Shooter;
import me.romindous.cs.Objects.Skins.GunSkin;
import me.romindous.cs.Objects.TargetLe;
import me.romindous.cs.Utils.Utils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import ru.komiss77.Ostrov;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.player.Perm;
import ru.komiss77.modules.world.BVec;
import ru.komiss77.notes.Slow;
import ru.komiss77.objects.IntHashMap;
import ru.komiss77.objects.SortedList;
import ru.komiss77.utils.EntityUtil;
import ru.komiss77.utils.ItemUtil;
import ru.komiss77.utils.NumUtil;

public class PlShooter implements Shooter {

	private static final double MOB_TRC = 12d;

	private final Predicate<Player> isAlly = pl -> {
		final Arena ar = arena();
		if (ar == null) return true;
		switch (ar.gst) {
            case WAITING, BEGINING, FINISH: return true;
			case BUYTIME, ROUND, ENDRND: break;
		}
		if (ar instanceof Gungame) return false;
		final PlShooter ps = Shooter.getPlShooter(pl.getName(), true);
		return ps.arena() == null || ps.arena().shtrs.get(ps) == ar.shtrs.get(this);
	};
	
	public PlShooter(final Player p) {
		name = p.getName();
		recoil = MAX_RCL; cldwn = 0; count = 0; shtTm = 0;
		money = 0; kills = 0; spwnrs = 0; deaths = 0;
		arena = null;

		pss = new LinkedList<>();
		skins = new EnumMap<>(GunType.class);
		inv = p.getInventory();
		final Oplayer op = PM.getOplayer(p);
		op.tag.canSee(isAlly);
		final Map<String, String> data = op.mysqlData;
		Ostrov.async(() -> {
			for (final GunType gt : GunType.values()) {
				final String skn = data.get(gt.toString());
				final GunSkin gs;
				if (skn == null || skn.isEmpty()) {
					gs = new GunSkin();
					data.put(gt.toString(), gs.toString());
				} else {
					gs = GunSkin.fromString(skn);
				}
				
				skins.put(gt, gs.has(gs.chosen) || Perm.isRank(op, 1)
					? gs : new GunSkin(GunType.DEF_MDL, gs.unlocked));
			}
		});

		new BukkitRunnable() {
			public void run() {
				rotPss();
				
				cldwn = Math.max(cldwn - 1, 0);
				final ItemStack it = item(EquipmentSlot.HAND);
				final GunType gun = GunType.fast(it);
				final Player p = getPlayer();
				if (gun == null || p == null) {
					return;
				}

//				p.sendMessage("2");
				final boolean ps = Main.hasDur(it);
				if (ps) {
					count++;
					if ((count & 0x3) == 0) {
						p.getWorld().playSound(p.getLocation(), Sound.BLOCK_CHAIN_FALL, 0.5f, 2f);
					}
					if (count < gun.rtm) Main.setDur(it, count);
					else {
						Main.setDur(it, Main.maxDur(it));
						p.getWorld().playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_NETHERITE, 1f, 2f);
						it.setAmount(gun.amo); count = 0;
						Ostrov.sync(() -> Utils.spy(p, gun.snp, p.isSneaking()), 1);
					} 
				}
				
				if (shtTm == 0) {
					if (count != 0 && !ps) {
						count = count > recoil + 1 ? recoil
							: (count - 2 < 0) ? 0 : (count - 2);
					}
				} else {
					shtTm--;
					if (!ps) count++;
//					if (scope() && scope(p.isSneaking())) return;
					if (count % gun.cld == 0) {
						final boolean one = it.getAmount() == 1;
						if (one) {
							if (ps) return;
							Main.setDur(it, 0);
						} else {
							if (ps) {
								Main.setDur(it, Main.maxDur(it));
								count = 0;
							}

							if (gun.snp && p.isSneaking()) return;
							it.setAmount(it.getAmount() - 1);
						} 
						cldwn = gun.cld;
						final int tr = count < recoil ? count : recoil;
						final boolean scp = gun.snp && p.isSneaking();
						if (gun.brst == 0) shoot(gun, !scp, tr, new IntHashMap<>());
						else {
							final IntHashMap<Info> info = new IntHashMap<>();
							for (int i = gun.brst; i != 0; i--) shoot(gun, !scp, tr, info);
						}
//						if (scp) {
//							Utils.zoom(p, false);
//							p.setSneaking(false);
//						}
						if (one) count = 0;
						if (gun.cld > 2) p.setCooldown(it, gun.cld - 1);
						Main.plyWrldSnd(p, gun.snd, 1.1f - Main.srnd.nextFloat() * 0.2f);
					}
				}
			}
		}.runTaskTimer(Main.plug, 1L, 1L);
	}
	
	@Override
	public Player getPlayer() {
		final HumanEntity p = inv.getHolder();
		return p == null ? Bukkit.getPlayer(name) : (Player) p;
	}
	
	@Override
	public LivingEntity getEntity() {
		return getPlayer();
	}
	
	private final String name;
	@Override
    public String name() {return name;}

	public final LinkedList<Vector> pss;
	public void rotPss() {
		if (isDead()) return;
		final Vector vc = getEntity().getLocation().toVector();
		if (pss.size() < MAX_DST) pss.add(vc);
		pss.poll(); pss.add(vc);
	}
	public Vector getLoc() {return pss.getLast().clone();}
	public Vector getLoc(final int dst) {
		return pss.get(Math.max(pss.size() - dst, 0)).clone();
	}
	
	public BVec getPos() {return BVec.of(getEntity().getLocation());}
	
	private int shtTm;
	public int shtTm() {return shtTm;}
	public void shtTm(final int n) {shtTm=n;}
	
	private int recoil;
	public int recoil() {return recoil;}
	public void rstRcl() {recoil=MAX_RCL;}
	
	private int cldwn;
	public int cldwn() {return cldwn;}
	public void cldwn(final int n) {cldwn=n;}
	
	private int kills;
	public int kills() {return kills;}
	public void killsI() {kills++;}
	public void kills0() {kills=0;}
	
	private int deaths;
	public int deaths() {return deaths;}
	public void deathsI() {deaths++;}
	public void deaths0() {deaths=0;}
	
	private int spwnrs;
	public int spwnrs() {return spwnrs;}
	public void spwnrsI() {spwnrs++;}
	public void spwnrs0() {spwnrs=0;}
	
	private int money;
	public int money() {return money;}
	public void money(final int n) {money=n;}
	
	private int count;
	public int count() {return count;}
	public void count(final int n) {count=n;}
	
	private Arena arena;
	public Arena arena() {return arena;}
	public void arena(final Arena ar) {arena = ar;}
	
	private PlayerInventory inv;
	public ItemStack item(final EquipmentSlot slot) {return inv.getItem(slot);}
	public ItemStack item(final int slot) {return inv.getItem(slot);}
	public void item(final EquipmentSlot slot, final ItemStack it) {
		inv.setItem(slot, it);
		if (slot != EquipmentSlot.HAND) return;
		final GunType gt = GunType.fast(it);
		final Player pl = getPlayer();
		Utils.spy(pl, gt != null && gt.snp, pl.isSneaking());
	}
	public void item(final int slot, final ItemStack it) {
		inv.setItem(slot, it);
		if (slot != inv.getHeldItemSlot()) return;
		final GunType gt = GunType.fast(it);
		final Player pl = getPlayer();
		Utils.spy(pl, gt != null && gt.snp, pl.isSneaking());
	}
	public PlayerInventory inv() {return inv;}
	public void inv(final PlayerInventory i) {inv = i;}
	public void clearInv() {
		item(EquipmentSlot.HAND, ItemUtil.air);
		inv.clear();
	}
	
	public void drop(final Location loc) {
		final World w = loc.getWorld();
		ItemStack it = item(3);
		if (!ItemUtil.isBlank(it, false)) {
			w.dropItem(loc, it).setInvulnerable(true);
		}
		it = item(4);
		if (!ItemUtil.isBlank(it, false)) {
			if (it.getAmount() == 1) {
				w.dropItem(loc, it).setInvulnerable(true);
			} else {
				it.setAmount(1);
				w.dropItem(loc, it).setInvulnerable(true);
				w.dropItem(loc, it).setInvulnerable(true);
			}
		}
		
		if (arena() instanceof Defusal) {
			it = item(0);
			if (!ItemUtil.isBlank(it, false)) {
				Main.setDur(it, 0);
				it.setAmount(1);
				w.dropItem(loc, it).setInvulnerable(true);
			}
			it = item(1);
			if (!ItemUtil.isBlank(it, false)) {
				Main.setDur(it, 0);
				it.setAmount(1);
				w.dropItem(loc, it).setInvulnerable(true);
			}
		}
		
		it = item(7);
		if (!ItemUtil.isBlank(it, false)) {
			if (it.getType() == Main.bmb.getType()) {
				if (arena instanceof final Defusal df) {
                    //bomb dropped
					if (df.getTmAmt(Team.Ts, true, true) != 1) {
						df.dropBomb(w.dropItem(loc, Main.bmb));
					}
					if (df.indon) {
						df.indSts(getPlayer());
					}
				}
			} else if (it.getType() == Material.SHEARS) {
				w.dropItem(loc, it).setInvulnerable(true);
			}
		}
		clearInv();
	}

	private final EnumMap<GunType, GunSkin> skins;
	public String model(final GunType gt) {return skin(gt).chosen;}
	public GunSkin skin(final GunType gt) {return skins.get(gt);}
	public boolean has(final GunType gt, final String mdl) {return skins.get(gt).has(mdl);}
	public boolean isDead() {return getPlayer().getGameMode() != GameMode.SURVIVAL;}

	@Override
	public void give(final GunType gt, final String mdl) {
		final GunSkin gs = skins.get(gt);
		if (gs == null) {
			final GunSkin ns = new GunSkin(mdl, GunType.DEF_MDL, mdl);
			skins.put(gt, ns);
			PM.getOplayer(name).mysqlData.put(gt.toString(), ns.toString());
			return;
		}
		final Set<String> lst = gs.unlocked;
		lst.add(mdl);
		PM.getOplayer(name).mysqlData.put(gt.toString(), gs.toString());
	}

	@Override
	public void choose(final GunType gt, final String cmd) {
		final GunSkin gs = skins.get(gt);
		final GunSkin ns;
		if (gs == null) {
			skins.put(gt, ns = new GunSkin());
			PM.getOplayer(name).mysqlData.put(gt.toString(), ns.toString());
			return;
		}	skins.put(gt, ns = new GunSkin(cmd, gs.unlocked));
		//Bukkit.getPlayer(nm).sendMessage("hs-" + Arrays.toString(ns.unlocked) + ", sel-" + gs.chosen);
		PM.getOplayer(name).mysqlData.put(gt.toString(), ns.toString());
	}

	@Override
	public void taq(final String pfx, final String sfx, final String afx) {
		final Player pl = getPlayer();
		final Oplayer op = PM.getOplayer(pl);
		op.tag.canSee(isAlly);
		op.tabPrefix(pfx, pl);
		op.tabSuffix(sfx, pl);
		op.beforeName(afx, pl);
		op.tag(pfx, sfx);
	}

	@Override
	public Predicate<Player> allyTest() {
		return isAlly;
	}
	
	@Override
	public void teleport(final LivingEntity le, final Location to) {
		le.teleport(to);
		pss.clear();
		pss.add(to.toVector());
	}

	@Override
	@Slow(priority = 4)
	public void shoot(final GunType gt, final boolean dff, final int tr, final IntHashMap<Info> blocks) {
		final LivingEntity ent = getEntity();
		final Location loc = ent.getEyeLocation();
		if (dff) {
			if (gt.brst == 0) {
				loc.setPitch(loc.getPitch() - tr * gt.yrcl + ((float) ent.getVelocity().getY() + (ent.isInWater() ? 0.005f : 0.0784f)) * 40f);
				loc.setYaw((Main.srnd.nextFloat() - 0.5f) * gt.xsprd * tr + loc.getYaw());
			} else {
				loc.setPitch((Main.srnd.nextFloat() - 0.5f) * gt.xsprd * recoil() + loc.getPitch() - tr * 0.1F
					+ ((float) ent.getVelocity().getY() + (ent.isInWater() ? 0.005f : 0.0784f)) * 40f);
				loc.setYaw((Main.srnd.nextFloat() - 0.5f) * gt.xsprd * recoil() + loc.getYaw());
			}
		}
		final double lkx = -Math.sin(Math.toRadians((180f - loc.getYaw())));
		final double lkz = -Math.cos(Math.toRadians((180f - loc.getYaw())));
		final Vector vec = loc.getDirection().normalize().multiply(TRC_STEP);
		final SortedList<TargetLe> shot = new SortedList<>();
		for (final LivingEntity e : Main.getLEs(loc.getWorld())) {
			if (!e.getType().isAlive() || e.getNoDamageTicks() != 0
				|| e.getEntityId() == ent.getEntityId()) continue;
			final Shooter sh = Shooter.getShooter(e, false);
			final Vector lc;
			if (sh == null) {
				lc = e.getLocation().toVector().subtract(
					e.getVelocity().setY(0d).multiply(MOB_TRC));
			} else {
				if (sh.isDead()) continue;
				lc = sh.getLoc(sh instanceof PlShooter || gt.snp ? 5 : 3);
			}
			final double dx = lc.getX() - loc.getX();
			final double dz = lc.getZ() - loc.getZ();
			final double ln = Math.sqrt(dx * dx + dz * dz);
			if (Math.sqrt(Math.pow(lkx - dx / ln, 2d) + Math.pow(lkz - dz / ln, 2d)) * ln < 0.4d) {
				final double pty = loc.getY() + Math.tan(Math.toRadians(-loc.getPitch())) * ln;
				if (pty > lc.getY() && pty < lc.getY() + e.getHeight()) {
					shot.add(new TargetLe(e, (int) (ln * TRC_FCT)));
				}
			}
		}

		double dmg = gt.dmg;
		final Arena ar = arena();
		final boolean brkBlks = ar != null && ar.gst == GameState.ROUND;
		final World w = ent.getWorld();
		final HashSet<BVec> bangs = new HashSet<>();
		double x = TRC_FCT * vec.getX() + loc.getX();
		double y = TRC_FCT * vec.getY() + loc.getY();
		double z = TRC_FCT * vec.getZ() + loc.getZ();
		boolean wallBang = false;

		boolean smoke = false;
		int tit = shot.size() - 1;
		final int ln = gt.snp ? 2000 : 1400;
        for (int i = (int) TRC_FCT; i != ln; i++) {
            //ent.sendMessage("route" + i);
            x += vec.getX();
            y += vec.getY();
            z += vec.getZ();
            if ((i & 63) == 0) {
                w.spawnParticle(Particle.ASH, x, y, z, 1);
            }
			final BVec bv = BVec.of((int) NumUtil.floor(x),
				(int) NumUtil.floor(y), (int) NumUtil.floor(z));
			final Info info, in = blocks.get(bv.thin());
			final boolean has;
			if (in == null) {
				info = Info.of(w, bv);
				blocks.put(bv.thin(), info);
				has = false;
			} else {
				info = in;
				has = true;
			}

			final Block b;
			final Location bl;
			if (info.smoke()) {
				smoke = true;
			} else if (info.broke()) {
				if (brkBlks && !has) {
					b = w.getBlockAt(bv.x, bv.y, bv.z);
					ar.brkn.add(new Broken(b));
					b.setType(Material.AIR, false);
					bl = new Location(w, x, y, z);
					w.playSound(bl, Sound.BLOCK_SHROOMLIGHT_FALL, 2f, 0.8f);
					w.spawnParticle(Particle.BLOCK, bl,
						40, 0.4d, 0.4d, 0.4d, info.data());
					wallBang = true;
					dmg *= 0.5f;
				}
			} else if (info.collides()) {
				if (info.pass()) continue;
				if (info.bang()) {
					if (!bangs.add(bv)) continue;
					if (info.shape().overlaps(new BoundingBox().shift(x - bv.x, y - bv.y, z - bv.z))) {
						w.spawnParticle(Particle.BLOCK, new Location(w, x, y, z), 4, 0.1d, 0.1d, 0.1d, info.data());
						Utils.crackBlock(w, bv);
						wallBang = true;
						dmg *= 0.5f;
					}
					continue;
				}
				if (info.shape().overlaps(new BoundingBox().shift(x - bv.x, y - bv.y, z - bv.z))) {
					w.spawnParticle(Particle.BLOCK, new Location(w, x, y, z), 10, 0.1d, 0.1d, 0.1d, info.data());
					Utils.crackBlock(w, bv);
					return;
				}
			}

            for (; tit >= 0; tit--) {
                final TargetLe tle = shot.get(tit);
                if (i < tle.dst()) break;

				final LivingEntity tgt = tle.le();
                final String nm;
                final boolean hst = y - tle.box().getMinY()
					> tle.box().getHeight() * 0.75d;
                final EntityShootAtEntityEvent ese;
                final Location tlc = tgt.getEyeLocation();
                if (hst) {
                    dmg *= 2f * (ItemUtil.isBlank(tgt.getEquipment().getHelmet(), false) ? 1f : 0.5f);
                    ese = new EntityShootAtEntityEvent(ent, tgt, gt, dmg,
						true, wallBang, dff && gt.snp, smoke);
                    ese.callEvent();
                    nm = "<red>銑" + NumUtil.abs((int) ((dmg - ese.getDamage()) * 5.0d));
                } else {
                    dmg *= ItemUtil.isBlank(tgt.getEquipment().getHelmet(), false) ? 1f : 0.6f;
                    ese = new EntityShootAtEntityEvent(ent, tgt, gt, dmg,
						false, wallBang, dff && gt.snp, smoke);
                    ese.callEvent();
                    nm = "<gold>" + NumUtil.abs((int) ((dmg - ese.getDamage()) * 5.0d));
                }

                if (ent instanceof final Player pl) {
                    if (hst) pl.playSound(loc, Sound.ENTITY_PLAYER_ATTACK_CRIT, 1f, 2f);
                    pl.playSound(loc, Sound.BLOCK_END_PORTAL_FRAME_FILL, 2f, 2f);
					EntityUtil.indicate(tlc, nm, pl);
                }

				dmg = ese.getDamage();
				if (dmg <= 0f) return; //dmg 0, end
            }
        }
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}
	
	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		return o instanceof Shooter && ((Shooter) o).name().equals(name);
	}
}