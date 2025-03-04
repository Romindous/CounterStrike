package me.Romindous.CounterStrike.Menus;

import java.util.ArrayList;
import java.util.Map;
import me.Romindous.CounterStrike.Game.Arena;
import me.Romindous.CounterStrike.Game.Invasion;
import me.Romindous.CounterStrike.Objects.Game.PlShooter;
import me.Romindous.CounterStrike.Objects.Shooter;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemType;
import ru.komiss77.modules.items.ItemBuilder;
import ru.komiss77.utils.inventory.*;


public class TeamMenu implements InventoryProvider {

    private final Arena ar;

    public TeamMenu(final Arena ar) {
        this.ar = ar;
    }

    @Override
    public void init(final Player p, final InventoryContent its) {
		final PlShooter sh = Shooter.getPlShooter(p.getName(), false);
		if (sh.arena() == null || !sh.arena().name.equals(ar.name)) {
			p.closeInventory();
			return;
		}

		its.fill(ClickableItem.empty(new ItemBuilder(ItemType.CHAIN).name("§0.").build()));

		final ArrayList<String> tlrs = new ArrayList<>();
		final ArrayList<String> clrs = new ArrayList<>();
		final ArrayList<String> slrs = new ArrayList<>();
		for (final Map.Entry<Shooter, Arena.Team> en : ar.shtrs.entrySet()) {
			switch (en.getValue()) {
                case Ts -> tlrs.add(Arena.Team.Ts.clr + "✦ §7" + en.getKey().name());
                case CTs -> clrs.add(Arena.Team.CTs.clr + "✦ §7" + en.getKey().name());
                case SPEC -> slrs.add(Arena.Team.SPEC.clr + "✦ §7" + en.getKey().name());
            }
		}

		its.set(4, ClickableItem.of(new ItemBuilder(ItemType.WARPED_NYLIUM).name(Arena.Team.CTs.clr + "§lСпецназ")
			.lore(clrs).build(), e -> select(p, sh, Arena.Team.CTs, its)));

		if (ar instanceof Invasion) {
			its.set(0, ClickableItem.of(new ItemBuilder(ItemType.WARPED_NYLIUM).name(Arena.Team.CTs.clr + "§lСпецназ")
				.lore(clrs).build(), e -> select(p, sh, Arena.Team.CTs, its)));
		} else {
			its.set(2, ClickableItem.of(new ItemBuilder(ItemType.ENDER_EYE).name("§5§lСлучайная")
				.lore(slrs).build(), e -> select(p, sh, switch (ar.gst) {
                case WAITING, BEGINING, FINISH -> Arena.Team.SPEC;
                case BUYTIME ,ROUND ,ENDRND -> ar.getMinTm();
            }, its)));

			its.set(0, ClickableItem.of(new ItemBuilder(ItemType.CRIMSON_NYLIUM).name(Arena.Team.Ts.clr + "§lТеррористы")
				.lore(tlrs).build(), e -> select(p, sh, Arena.Team.Ts, its)));
		}
    }

	private void select(final Player p, final PlShooter sh, final Arena.Team team, final InventoryContent its) {
		p.playSound(p.getLocation(), Sound.BLOCK_NETHER_GOLD_ORE_FALL, 2f, 2f);
		switch (ar.gst) {
			case WAITING, BEGINING, FINISH:
				if (ar.chngTeam(sh, team)) {
					for (final Shooter s : ar.shtrs.keySet()) {
						final Player pl = s.getPlayer();
						if (pl == null) continue;
						final SmartInventory si = InventoryManager.getInventory(pl).orElse(null);
						if (si != null && si.getProvider() instanceof final TeamMenu bm) {
							pl.closeInventory();
							bm.reopen(pl, its);
						}
					}
				}
                break;
            case BUYTIME, ROUND, ENDRND:
				if (ar.chngTeam(sh, team)) {
					ar.addToTm(p, sh, team);
					for (final Shooter s : ar.shtrs.keySet()) {
						final Player pl = s.getPlayer();
						if (pl == null) continue;
						final SmartInventory si = InventoryManager.getInventory(pl).orElse(null);
						if (si != null && si.getProvider() instanceof final TeamMenu bm) {
							pl.closeInventory();
							bm.reopen(pl, its);
						}
					}
				}
                break;
        }
	}
}
