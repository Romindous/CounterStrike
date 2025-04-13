package me.romindous.cs.Menus;

import me.romindous.cs.Main;
import me.romindous.cs.Objects.Defusable;
import me.romindous.cs.Objects.Game.Bomb;
import me.romindous.cs.Objects.Game.Mobber;
import me.romindous.cs.Objects.Game.PlShooter;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemType;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Ostrov;
import ru.komiss77.enums.Stat;
import ru.komiss77.modules.items.ItemBuilder;
import ru.komiss77.utils.ClassUtil;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;
import ru.komiss77.utils.inventory.SmartInventory;

public class DefuseMenu implements InventoryProvider {

	private static final int MAX_WIRES = 51;
	private static final int MAX_DST_SQ = 24;

	private final Defusable def;
	private final String clr;

	private boolean lastKit = false;
    private int left;

	public DefuseMenu(final Defusable def) {
		this.def = def;
		this.clr = ClassUtil.rndElmt(Defusable.COLORS);
	}

	public DefuseMenu fillUp(final float full) {
		if (def.defusing() instanceof final PlShooter ps)
			ps.getPlayer().closeInventory();
        final int maxLeft = (int) (MAX_WIRES * Math.clamp(full, 0f, 1f));
		left = maxLeft >> (lastKit ? 1 : 0);
		return this;
	}

	public void open(final Player pl, final boolean kit, final boolean first) {
//		if (!psh.equals(def.defusing())) return;
		if (lastKit) {if (!kit) {
			left <<= 1; lastKit = false;}
		} else if (kit) {
			left >>= 1; lastKit = true;
		}
		final String ttl;
		if (def instanceof Bomb) {
			ttl = "§3§lРазминировка Бомбы";
		} else if (def instanceof Mobber) {
			ttl = "§3§lОбезвреживание Спавнера";
		} else ttl = "";
		SmartInventory.builder().id(def.thin() + " Defuse").title(ttl)
			.provider(this).size(6, 9).build().open(pl);
		if (first) pl.getWorld().playSound(def.center(pl.getWorld()),
			Sound.BLOCK_BEEHIVE_SHEAR, 2f, 0.5f);
	}

	private static final ClickableItem pane = ClickableItem.empty(
		new ItemBuilder(ItemType.LIGHT_GRAY_STAINED_GLASS_PANE).name("§0.").build());
	public void init(final Player p, final InventoryContent its) {

		its.set(49, ClickableItem.empty(new ItemBuilder(ItemType.BOWL)
			.name("§5§lРазрежь провода этого цвета!").model(Defusable.disp(clr)).build()));
		its.set(40, pane); its.set(48, pane); its.set(50, pane);

		final int[] wrIxs = new int[MAX_WIRES];
		for (int i = 0; i != wrIxs.length; i++) wrIxs[i] = i > 47 ? i + 3 : i;
		final int[] finIxs = shuffle(wrIxs);
		for (int i = 0; i != left; i++) {
			final int ix = finIxs[i];
			its.set(ix, ClickableItem.of(new ItemBuilder(ItemType.STRING)
				.name("§8~-~-~").model(Defusable.wire(clr)).build(), e -> {
				final Location eye = p.getEyeLocation();
				eye.getWorld().spawnParticle(Particle.ITEM, eye.add(eye.getDirection()),
					8, 0d, 0d, 0d, 0.2d, p.getInventory().getItemInMainHand());
				eye.getWorld().playSound(eye, Sound.BLOCK_TRIPWIRE_CLICK_ON, 1f, 2f);
				p.swingMainHand(); left--;
				if (def.distSq(p.getEyeLocation()) > MAX_DST_SQ) {
					p.sendMessage(Main.prf() + "§cНадо стоять ближе 4 блоков!");
					return;
				}
				if (left < 1) {
					eye.getWorld().playSound(eye, Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE, 2f, 2f);
					eye.getWorld().playSound(eye, Sound.BLOCK_IRON_TRAPDOOR_CLOSE, 2f, 2f);
					switch (def) {
						case final Bomb bmb -> {
							ApiOstrov.addStat(p, Stat.CS_bomb);
							bmb.arena().chngMn(def.defusing(), 250);
							bmb.arena().defuse();
						}
						case final Mobber mb -> mb.arena()
							.rmvSpnr(mb, def.defusing());
                        default -> {}
                    }
					p.closeInventory();
					return;
				}
				its.set(ix, ClickableItem.empty(new ItemBuilder(ItemType.STRING).name("§8~-~-~")
					.model(Defusable.wire(Defusable.OFF_CLR)).build()));
				if (def instanceof Mobber) open(p, lastKit, false);
			}));
		}

		final String[] lcls = others();
		final ClickableItem[] clicks = new ClickableItem[lcls.length];
        for (int i = 0; i != lcls.length; i++) {
            clicks[i] = ClickableItem.of(new ItemBuilder(ItemType.STRING)
				.name("§8~-~-~").model(Defusable.wire(lcls[i])).build(), e -> {
				final Location eye = p.getEyeLocation();
				eye.getWorld().playSound(eye, Sound.BLOCK_GLASS_BREAK, 2f, 2f);
				p.playSound(eye, Sound.BLOCK_TRIPWIRE_CLICK_ON, 1f, 2f);
				switch (def) {
					case final Bomb bm -> bm.arena().wrngWire();
					case final Mobber mb -> mb.spwnMb();
					default -> {}
				}
				left++;
				open(p, lastKit, true);
			});
        }
		for (int i = left; i != finIxs.length; i++) {
			its.add(clicks[finIxs[i] % clicks.length]);
		}
	}

	@Override
	public void onClose(final Player p, final InventoryContent cnt) {
		def.defusing(null);
	}

	private String[] others() {
		final String[] clrs = new String[Defusable.COLORS.length - 1];
		boolean fnd = false;
		for (int i = 0; i != Defusable.COLORS.length; i++) {
			if (fnd) clrs[i-1] = Defusable.COLORS[i];
			else {
				if (i == clrs.length) break;
				if (Defusable.COLORS[i].equals(clr)) {
					fnd = true; continue;
				}
				clrs[i] = Defusable.COLORS[i];
			}
		}
		return clrs;
	}

	private static int[] shuffle(final int[] ar) {
		int chs = ar.length >> 2;
		if (chs == 0) {
			if (ar.length > 1) {
				final int ne = ar[0];
				ar[0] = ar[ar.length - 1];
				ar[ar.length - 1] = ne;
			}
			return ar;
		}
		for (int i = ar.length - 1; i > chs; i--) {
			final int ni = Ostrov.random.nextInt(i);
			final int ne = ar[ni];
			ar[ni] = ar[i];
			ar[i] = ne;
			chs += ((chs - ni) >> 31) + 1;
		}
		return ar;
	}

}
