package me.romindous.cs.Objects.Game;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.function.Predicate;
import com.destroystokyo.paper.entity.ai.Goal;
import me.romindous.cs.Enums.GameState;
import me.romindous.cs.Enums.GameType;
import me.romindous.cs.Enums.GunType;
import me.romindous.cs.Enums.NadeType;
import me.romindous.cs.Game.Arena;
import me.romindous.cs.Game.Arena.Team;
import me.romindous.cs.Game.Defusal;
import me.romindous.cs.Game.Gungame;
import me.romindous.cs.Game.Invasion;
import me.romindous.cs.Main;
import me.romindous.cs.Objects.Bots.BotGoal;
import me.romindous.cs.Objects.EntityShootAtEntityEvent;
import me.romindous.cs.Objects.Loc.Broken;
import me.romindous.cs.Objects.Loc.Info;
import me.romindous.cs.Objects.Shooter;
import me.romindous.cs.Objects.Skins.GunSkin;
import me.romindous.cs.Objects.TargetLe;
import me.romindous.cs.Utils.Inventories;
import me.romindous.cs.Utils.Utils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import ru.komiss77.Ostrov;
import ru.komiss77.modules.bots.Botter;
import ru.komiss77.modules.items.ItemBuilder;
import ru.komiss77.modules.world.BVec;
import ru.komiss77.notes.Slow;
import ru.komiss77.objects.IntHashMap;
import ru.komiss77.objects.SortedList;
import ru.komiss77.utils.ItemUtil;
import ru.komiss77.utils.LocUtil;
import ru.komiss77.utils.NumUtil;

public class BtShooter implements Shooter, Botter.Extent {
	
	public Shooter tgtSh;

	public WeakReference<LivingEntity> tgtLe;
	public boolean willBuy;

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

	private final Botter own;
	
	public BtShooter(final Botter owner, final Arena ar) {
		recoil = MAX_RCL; cldwn = 0; count = 0; shtTm = 0;
		money = 0; kills = 0; spwnrs = 0; deaths = 0;
		arena = ar; own = owner; willBuy = false;
		tgtSh = null; tgtLe = new WeakReference<>(null);
		pss = new LinkedList<>();
	}

	public Botter own() {
		return own;
	}

	public void swapTo(final int slot) {
		own.swapToSlot(slot);
	}

	@Override
	public String name() {
		return own.name();
	}

	public void create(Botter bt) {
		bt.setTagVis(isAlly);
	}

	public void spawn(Botter bt, @Nullable LivingEntity le) {}
	public void remove(Botter bt) {}
	public void bug(Botter bt) {
		bt.remove();
	}

	@Override
	public void teleport(Botter botter, LivingEntity livingEntity) {

	}

	@Override
	public void hide(final Botter bt, final @Nullable LivingEntity mb) {
        switch (arena) {
            case final Defusal df -> {
                if (df.isBmbOn()) {
                    final Shooter sh = df.getBomb().defusing();
                    if (sh != null && sh.equals(this))
                        df.getBomb().defusing(null);
                }
            }
            case final Invasion in -> {
                for (final Mobber m : in.mbbrs.values()) {
                    final Shooter sh = m.defusing();
                    if (sh != null && sh.equals(this))
                        m.defusing(null);
                }
            }
            case null, default -> {}
        }
    }

	@Override
	public Goal<Mob> goal(final Botter bt, final Mob org) {
		return new BotGoal(this, org);
	}
	
	@Override
	public void damage(final Botter bt, final EntityDamageEvent e) {}

	@Override
	public void click(final Botter bt, final PlayerInteractAtEntityEvent e) {
		final Player p = e.getPlayer();
		if (isAlly.test(p) && p.isSneaking()
			&& e.getHand() == EquipmentSlot.HAND) {
			final Shooter sh = Shooter.getPlShooter(p.getName(), false);
			final int slt = p.getInventory().getHeldItemSlot();
			switch (slt) {
				case 0, 1, 3, 4:
					final ItemStack bit = item(slt);
					final ItemStack pit = sh.item(slt);
					if (ItemUtil.compare(bit, pit, ItemUtil.Stat.TYPE, ItemUtil.Stat.NAME)) break;
					own.world().playSound(p.getEyeLocation(), Sound.ITEM_ARMOR_EQUIP_IRON, 1f, 1.2f);
					own.world().playSound(getLoc().toLocation(own.world()), Sound.ITEM_ARMOR_EQUIP_IRON, 1f, 1.2f);
					item(slt, pit);
					sh.item(slt, bit);
					willBuy = true;
					tryBuy();
				default: break;
			}
		}
	}

	@Override
	public void death(Botter bt, EntityDeathEvent e) {
		Botter.Extent.super.death(bt, e);
	}

	public boolean tryReload(final LivingEntity le, final Location loc) {
		cldwn = Math.max(cldwn - 1, 0);
		final ItemStack it = item(EquipmentSlot.HAND);
		final GunType gun = GunType.fast(it);
		if (gun == null || !Main.hasDur(it)) return false;
        count++;
        if ((count & 0x3) == 0) {
            le.getWorld().playSound(loc, Sound.BLOCK_CHAIN_FALL, 0.5f, 2f);
        }
        if (count < gun.rtm) {
			Main.setDur(it, count);
			item(EquipmentSlot.HAND, it);
			return true;
		}
		Main.setDur(it, Main.maxDur(it));
		le.getWorld().playSound(loc, Sound.ITEM_ARMOR_EQUIP_NETHERITE, 1f, 2f);
		it.setAmount(gun.amo);
		count = 0;
		item(EquipmentSlot.HAND, it);
		return false;
    }

	public boolean tryShoot(final LivingEntity le) {
		final ItemStack it = item(EquipmentSlot.HAND);
		final GunType gun = GunType.fast(it);
		if (gun != null) {
			final boolean ps = Main.hasDur(it);
			count = count + (ps ? 0 : 1);
			if (count % gun.cld == 0) {
				if (it.getAmount() == 1) {
					if (ps) return false;
					Main.setDur(it, 0);
					count = 0;
				} else {
					if (ps) {
						Main.setDur(it, Main.maxDur(it));
						count = 0;
					}
					it.setAmount(it.getAmount() - 1);
				}
				item(EquipmentSlot.HAND, it);
				cldwn = gun.cld; rstRcl();
				final int tr = count < recoil ? count : recoil;
				final boolean ns = !gun.snp;
				if (gun.brst == 0) shoot(gun, ns, tr, new IntHashMap<>());
				else {
					final IntHashMap<Info> info = new IntHashMap<>();
					for (int i = gun.brst; i != 0; i--) shoot(gun, ns, tr, info);
				}
				Main.plyWrldSnd(le, gun.snd, 1.1f - Main.srnd.nextFloat() * 0.2f);
				return true;
			}
		}
		return false;
	}
	
	public void switchToGun() {
		final GunType gt0 = GunType.get(item(0));
		if (gt0 != null) {
			swapTo(0);
			return;
		}
		final GunType gt1 = GunType.get(item(1));
		if (gt1 != null) {
			swapTo(1);
			return;
		}
		swapTo(2);
	}

	@Override
	public Player getPlayer() {return null;}

	@Override
	public LivingEntity getEntity() {
		return own.getEntity();
	}

	@Override
	public void teleport(final LivingEntity le, final Location to) {
		own.telespawn(le, to);
		own.tag(true);
		pss.clear();
		pss.add(to.toVector());
	}

	@Override
	public boolean isDead() {
		return own.isDead();
	}

	public final LinkedList<Vector> pss;

	public void rotPss() {
		final LivingEntity le = getEntity();
		if (le == null || !le.isValid()) return;
		final Vector vc = le.getLocation().toVector();
		if (pss.size() < MAX_DST) pss.add(vc);
		pss.poll(); pss.add(vc);
	}
	public Vector getLoc() {return pss.getLast().clone();}
	public Vector getLoc(final int dst) {
		return pss.get(Math.max(pss.size() - dst, 0)).clone();
	}

	@Override
	public BVec getPos() {
		return own.getPos();
	}

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

	@Override
	public ItemStack item(final EquipmentSlot slot) {
		return own.item(slot);
	}

	@Override
	public ItemStack item(final int slot) {
		return own.item(slot);
	}

	@Override
	public void item(final EquipmentSlot slot, final ItemStack it) {
		own.item(slot, it);
	}

	@Override
	public void item(final int slot, final ItemStack it) {
		own.item(slot, it);
	}

	@Override
	public Inventory inv() {
		return own.inv();
	}

	@Override
	public void clearInv() {
		item(EquipmentSlot.HAND, ItemUtil.air);
		own.clearInv();
	}

	@Override
	public void drop(final Location loc) {
		own.drop(loc);
	}

	public String model(final GunType gt) {return gt.name + "_" + GunType.DEF_MDL;}
	public GunSkin skin(final GunType gt) {return new GunSkin();}
	public boolean has(final GunType gt, final String mdl) {return true;}
	public void give(final GunType gt, final String mdl) {}
	public void choose(final GunType gt, final String mdl) {}

	@Override
	public void taq(final String pfx, final String sfx, final String afx) {
		own.setTagVis(isAlly);
		own.tab(pfx, afx, sfx);
		own.tag(pfx, afx, sfx);
	}

	@Override
	public Predicate<Player> allyTest() {
		return isAlly;
	}

	@Override
	@Slow(priority = 4)
	public void shoot(final GunType gt, final boolean dff, final int tr, final IntHashMap<Info> blocks) {
		final LivingEntity ent = getEntity();
		if (ent == null) return;
		final Location loc = ent.getEyeLocation();
		if (tgtSh != null) {
			loc.setDirection(tgtSh.getLoc().subtract(getLoc()));
		} else {
			final LivingEntity le = tgtLe.get();
			if (le != null) {
				loc.setDirection(le.getLocation().toVector().subtract(getLoc()));
			}
		}
		if (dff) {
			loc.setPitch(loc.getPitch() + ((float) ent.getVelocity().getY() + (ent.isInWater() ? 0.005f : 0.0784f)) * 40f + Main.srnd.nextFloat() * 8f);
			if (gt.brst == 0) {
				loc.setYaw((Main.srnd.nextFloat() - 0.5f) * gt.xsprd * tr * 4f + loc.getYaw());
			} else {
				loc.setYaw((Main.srnd.nextFloat() - 0.5f) * gt.xsprd * recoil + loc.getYaw());
			}
		} else {
			loc.setPitch(loc.getPitch() + ((float) ent.getVelocity().getY() + (ent.isInWater() ? 0.005f : 0.0784f)) * 40f + Main.srnd.nextFloat() * 4f);
			loc.setYaw((Main.srnd.nextFloat() - 0.5f) * gt.xsprd * recoil * 0.1f + loc.getYaw());
		}
		final double lkx = -Math.sin(Math.toRadians((180f - loc.getYaw())));
		final double lkz = -Math.cos(Math.toRadians((180f - loc.getYaw())));
		final Vector vec = loc.getDirection().normalize().multiply(TRC_STEP);
		final SortedList<TargetLe> shot = new SortedList<>();
		for (final LivingEntity e : Main.getLEs(loc.getWorld())) {
			if (!e.getType().isAlive() || e.getNoDamageTicks() != 0
				|| e.getEntityId() == ent.getEntityId()) continue;
			final Shooter sh = Shooter.getShooter(e, false);
//				final Vector lc = sh.getLoc(sh instanceof PlShooter || gt.snp ? 4 : 2);
			if (sh != null && sh.isDead()) continue;
			final BoundingBox ebx = e.getBoundingBox();
			final double dx = ebx.getCenterX() - loc.getX();
			final double dz = ebx.getCenterZ() - loc.getZ();
			final double ln = Math.sqrt(dx * dx + dz * dz);
			if (Math.sqrt(Math.pow(lkx - dx / ln, 2d) + Math.pow(lkz - dz / ln, 2d)) * ln < 0.4d) {
				final double pty = loc.getY() + Math.tan(Math.toRadians(-loc.getPitch())) * ln;
				if (pty < ebx.getMaxY() && pty > ebx.getMinY()) {
					shot.add(new TargetLe(e, (int) (ln * TRC_FCT)));
				}
			}
		}

		float dmg = gt.dmg;
		final Arena ar = arena();
		final boolean brkBlks = arena() != null && arena().gst == GameState.ROUND;
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
				final boolean hst = y - tle.box().getMinY()
					> tle.box().getHeight() * 0.75d;
				final EntityShootAtEntityEvent ese;
                //dmg 0, end
                if (hst) {
					dmg *= 2f * (tgt.getEquipment().getHelmet() == null ? 1f : 0.5f);
					ese = new EntityShootAtEntityEvent(ent, tgt, gt, dmg,
						true, wallBang, dff && gt.snp, smoke);
                } else {
					dmg *= tgt.getEquipment().getChestplate() == null ? 1f : 0.6f;
					ese = new EntityShootAtEntityEvent(ent, tgt, gt, dmg,
						false, wallBang, dff && gt.snp, smoke);
                }
                if (dmg <= 0f) return;//dmg 0, end
                ese.callEvent();
            }
		}
	}
	
	@Override
	public void drop(final Botter bt, final Location loc) {
		ItemStack it = item(3);
		if (!ItemUtil.isBlank(it, false)) {
			own.world().dropItem(loc, it).setInvulnerable(true);
		}
		it = item(4);
		if (!ItemUtil.isBlank(it, false)) {
			if (it.getAmount() == 1) {
				own.world().dropItem(loc, it).setInvulnerable(true);
			} else {
				it.setAmount(1);
				own.world().dropItem(loc, it).setInvulnerable(true);
				own.world().dropItem(loc, it).setInvulnerable(true);
			}
		}
		
		if (arena() instanceof Defusal) {
			it = item(0);
			if (!ItemUtil.isBlank(it, false)) {
				Main.setDur(it, 0);
				it.setAmount(1);
				own.world().dropItem(loc, it).setInvulnerable(true);
			}
			it = item(1);
			if (!ItemUtil.isBlank(it, false)) {
				Main.setDur(it, 0);
				it.setAmount(1);
				own.world().dropItem(loc, it).setInvulnerable(true);
			}
		}
		
		it = item(7);
		if (!ItemUtil.isBlank(it, false)) {
			if (it.getType() == Main.bmb.getType()) {
				if (arena instanceof final Defusal df) {
                    //bomb dropped
					if (df.getTmAmt(Team.Ts, true, true) != 1) {
						df.dropBomb(own.world().dropItem(loc, Main.bmb));
					}
				}
			} else if (it.getType() == Material.SHEARS) {
				own.world().dropItem(loc, it).setInvulnerable(true);
			}
		}
		clearInv();
	}
	
	@Override
	public void pickup(final Botter bt, final Location loc) {
		for (final Item it : LocUtil.getChEnts(loc, 4d, Item.class, it -> {return it.getPickupDelay() == 0;})) {
			final ItemStack is = it.getItemStack();
			final GunType gt = GunType.get(is);
			if (gt == null) {
				final NadeType nt = NadeType.getNdTp(is);
				if (nt == null) {
					if (ItemUtil.isBlank(is, false)) return;
					switch (is.getType()) {
					case GOLDEN_APPLE:
						if (arena().shtrs.get(this) == Team.Ts) {
							item(7, is);
							((Defusal) arena()).pickBomb();
							it.remove();
							own.world().playSound(loc, Sound.ITEM_ARMOR_EQUIP_DIAMOND, 1f, 1.4f);
						} else {
							it.setPickupDelay(40);
						}
						break;
					case SHEARS:
						if (arena().shtrs.get(this) == Team.CTs) {
							item(7, is);
							it.remove();
							own.world().playSound(loc, Sound.ITEM_ARMOR_EQUIP_DIAMOND, 1f, 1.4f);
						} else {
							it.setPickupDelay(40);
						}
						break;
					default:
						break;
					}
					return;
				}
				final int slt = nt.prm ? NadeType.prmSlot : NadeType.scdSlot;
				final ItemStack eqp = item(slt);
				final NadeType ownNade = NadeType.getNdTp(eqp);
				if (ownNade == null) {
					item(slt, is);
					it.remove();
					this.own.world().playSound(loc, Sound.ITEM_ARMOR_EQUIP_DIAMOND, 1f, 1.4f);
				}
				return;
			}
			final int slt = gt.prm ? 0 : 1;
			final ItemStack eqp = item(slt);
			final GunType ownGun = GunType.get(eqp);
			if (ownGun == null) {
				item(slt, is.asQuantity(gt.amo));
				it.remove();
				own.world().playSound(loc, Sound.ITEM_ARMOR_EQUIP_NETHERITE, 1f, 1.4f);
			} else if (gt.prc > ownGun.prc) {
				own.world().dropItem(loc, eqp);
				item(slt, is.asQuantity(gt.amo));
				it.remove();
				own.world().playSound(loc, Sound.ITEM_ARMOR_EQUIP_NETHERITE, 1f, 1.4f);
			}
			switchToGun();
		}
	}

	public void tryBuy() {
//		Bukkit.broadcast(Component.text("mon " + money));
		switch (arena.getType()) {
		case INVASION:
			if (!((Invasion) arena).isDay) return;
			break;
		case DEFUSAL:
			if (!willBuy || arena.gst != GameState.BUYTIME) return;
			willBuy = false;
			break;
		case GUNGAME:
			return;
		}
		
		final Team tm = arena.shtrs.get(this);
		final boolean chs = Main.srnd.nextBoolean();
		if (!chs && tm == Team.CTs) buyItem(Shooter.kitSlt, Shooter.kitPrc, 7, tm);
		if (money < 1000) {//save
			buyItem(Shooter.helmSlt, Shooter.helmPrc, EquipmentSlot.HEAD, tm);
			if (chs) buyItem(Shooter.chestSlt, Shooter.chestPrc, EquipmentSlot.CHEST, tm);
			
			if (!buyItem(GunType.DGL.slt, GunType.DGL.prc, 1, tm)) {
				if (!buyItem(GunType.TP9.slt, GunType.TP9.prc, 1, tm)) {
					buyItem(GunType.USP.slt, GunType.USP.prc, 1, tm);
				}
			}

		} else {//buy up
			buyItem(Shooter.helmSlt, Shooter.helmPrc, EquipmentSlot.HEAD, tm);
			buyItem(Shooter.chestSlt, Shooter.chestPrc, EquipmentSlot.CHEST, tm);
			
			final GunType top = chs ? GunType.AWP : GunType.SCAR;
			if (!buyItem(top.slt, top.prc, 0, tm)) {
				final GunType gud = chs ? GunType.M4A1 : GunType.AK47;
				if (!buyItem(gud.slt, gud.prc, 0, tm)) {
					final GunType mid = chs ? GunType.P90 : GunType.MP5;
					if (!buyItem(mid.slt, mid.prc, 0, tm)) {
						final GunType low = chs ? GunType.SG13 : GunType.NOVA;
						buyItem(low.slt, low.prc, 0, tm);
					}
				}
			}
			
			if (arena.getType() != GameType.INVASION) {
				if (chs) {
					buyItem(NadeType.FRAG.slt, NadeType.FRAG.prc, NadeType.prmSlot, tm);
					buyItem(NadeType.FLASH.slt, NadeType.FLASH.prc, NadeType.scdSlot, tm);
				} else buyItem(NadeType.FLAME.slt, NadeType.FLAME.prc, NadeType.prmSlot, tm);
			}
		}
		
		switchToGun();
	}
	
	private boolean buyItem(final byte slot, final short prc, final EquipmentSlot to, final Team tm) {
		final ItemStack eq = item(to);
		final Inventory inv = switch (tm) {
            case Ts -> Inventories.TShop;
            case CTs -> Inventories.CTShop;
            case SPEC -> Inventories.LBShop;
        };
        final ItemStack it = inv.getItem(slot);
		if (ItemUtil.isBlank(it, false)) {
			Ostrov.log_warn("Tried to buy null item at slot " + slot);
			return false;
		}
		final GunType gt = GunType.get(it);
		if (gt == null) {
			final NadeType nt = NadeType.getNdTp(it);
			if (nt == null) {//armor?
                if (!ItemUtil.isBlank(eq, false) || prc > money) return false;
                money -= prc;
                item(to, it.clone());
                own.world().playSound(getLoc().toLocation(own.world()), Sound.ITEM_ARMOR_EQUIP_IRON, 1f, 1.4f);
                return true;
            }
			//nades
			if (prc > money) return false;
			final NadeType ownNade = NadeType.getNdTp(eq);
			if (ownNade != null) {
				if (ownNade.prc >= prc) return false;
				own.world().dropItem(getLoc().toLocation(own.world()), eq);
			}
			money -= prc;
			item(to, it.clone());
			own.world().playSound(getLoc().toLocation(own.world()), Sound.ITEM_ARMOR_EQUIP_DIAMOND, 1f, 1.4f);
			return true;
		}
		//guns
		if (prc > money) return false;
		final GunType ownGun = GunType.get(eq);
		if (ownGun != null) {
			if (ownGun.prc >= prc) return false;
			own.world().dropItem(getLoc().toLocation(own.world()), eq.asOne());
		}
		money -= prc;
		item(to, new ItemBuilder(it.getType().asItemType()).name("§5" + gt.name() + " " + gt.icn)
			.maxDamage(gt.rtm).amount(gt.amo).model(gt.skin(GunType.DEF_MDL)).build());
		own.world().playSound(getLoc().toLocation(own.world()), Sound.ITEM_ARMOR_EQUIP_NETHERITE, 1f, 1.4f);
		return true;
    }
	
	private boolean buyItem(final byte slot, final short prc, final int to, final Team tm) {
		final ItemStack eq = item(to);
		final Inventory inv = switch (tm) {
			case Ts -> Inventories.TShop;
			case CTs -> Inventories.CTShop;
			case SPEC -> Inventories.LBShop;
		};
		final ItemStack it = inv.getItem(slot);
		if (ItemUtil.isBlank(it, false)) {
			Ostrov.log_warn("Tried to buy null item at slot " + slot);
			return false;
		}
		final GunType gt = GunType.get(it);
		if (gt == null) {
			final NadeType nt = NadeType.getNdTp(it);
			if (nt == null) {//armor?
				if (!ItemUtil.isBlank(eq, false) || prc > money) return false;
				money -= prc;
				item(to, it.clone());
				own.world().playSound(getLoc().toLocation(own.world()), Sound.ITEM_ARMOR_EQUIP_IRON, 1f, 1.4f);
				return true;
			}
			//nades
			if (prc > money) return false;
			final NadeType ownNade = NadeType.getNdTp(eq);
			if (ownNade != null) {
				if (ownNade.prc >= prc) return false;
				own.world().dropItem(getLoc().toLocation(own.world()), eq);
			}
			money -= prc;
			item(to, it.clone());
			own.world().playSound(getLoc().toLocation(own.world()), Sound.ITEM_ARMOR_EQUIP_DIAMOND, 1f, 1.4f);
			return true;
		}
		//guns
		if (prc > money) return false;
		final GunType ownGun = GunType.get(eq);
		if (ownGun != null) {
			if (ownGun.prc >= prc) return false;
			own.world().dropItem(getLoc().toLocation(own.world()), eq.asOne());
		}
		money -= prc;
		item(to, gt.item().name("§5" + gt.name() + " " + gt.icn)
			.maxDamage(gt.rtm).amount(gt.amo).model(gt.skin(GunType.DEF_MDL)).build());
		own.world().playSound(getLoc().toLocation(own.world()), Sound.ITEM_ARMOR_EQUIP_NETHERITE, 1f, 1.4f);
		return true;
		/*final ItemStack eq = item(to);
		final GunType gt = GunType.getGnTp(eq);
		final ItemStack it;
		if (gt == null) {
			if (money < prc) {
				it = null;
			} else {
				money -= prc;
                it = switch (tm) {
                    case CTs -> Inventories.CTShop.getItem(slot);
                    default -> Inventories.TShop.getItem(slot);
                };
			}
		} else if (gt.prc < prc) {
			if (money < prc) {
				it = null;
			} else {
				money -= prc;
				world.dropItem(getLoc().toLocation(world), eq);
                it = switch (tm) {
                    case CTs -> Inventories.CTShop.getItem(slot);
                    default -> Inventories.TShop.getItem(slot);
                };
			}
		} else {
			it = null;
		}

		if (ItemUtil.isBlank(it, false)) return false;
		final GunType ngt = GunType.getGnTp(it);
		if (ngt == null) {
			item(it.clone(), to);
			world.playSound(getLoc().toLocation(world), Sound.ITEM_ARMOR_EQUIP_DIAMOND, 1f, 1.4f);
		} else {
			item(new ItemBuilder(it.getType()).name("§5" + ngt.toString() + " " + ngt.icn)
				.amount(ngt.amo).modelData(GunType.defCMD).build(), to);
			world.playSound(getLoc().toLocation(world), Sound.ITEM_ARMOR_EQUIP_NETHERITE, 1f, 1.4f);
		}
		return true;*/
	}

	public boolean tryNade(final LivingEntity tgt) {
		final NadeType pnt = NadeType.getNdTp(item(NadeType.prmSlot));
		if (pnt == null) {
			final NadeType snt = NadeType.getNdTp(item(NadeType.scdSlot));
			if (snt == null) return false;
			final Vector dir = getNadeVec(tgt.getLocation()
				.toVector().subtract(getLoc()), 1d);
			dir.setY(dir.getY() * 1.25d);
			Nade.launch(getEntity(), this, dir, snt.time << 1, NadeType.scdSlot);
			return true;
		}

		Nade.launch(getEntity(), this, getNadeVec(tgt.getLocation()
			.toVector().subtract(getLoc()), 1.25d), pnt.time << 1, NadeType.prmSlot);
		return true;
	}

	private Vector getNadeVec(final Vector dst, final double spd) {
		final double DlnSq = dst.lengthSquared() * 0.01d / spd;
		if (dst.getY() > -DlnSq) dst.setY((dst.getY() + 1d) * DlnSq);
		return dst.normalize().multiply(spd);
	}

	@Override
	public int hashCode() {
		return name().hashCode();
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		return o instanceof Shooter && ((Shooter) o).name().equals(name());
	}
}
