package me.Romindous.CounterStrike.Menus;

import me.Romindous.CounterStrike.Game.Arena;
import me.Romindous.CounterStrike.Game.Invasion;
import me.Romindous.CounterStrike.Objects.Shooter;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.inventory.*;

import java.util.ArrayList;
import java.util.Map;


public class TeamMenu implements InventoryProvider {

    private final Arena ar;

    public TeamMenu(final Arena ar) {
        this.ar = ar;
    }

    @Override
    public void init(final Player p, final InventoryContent its) {
		final Shooter sh = Shooter.getPlShooter(p.getName(), false);
		if (sh.arena() == null || !sh.arena().name.equals(ar.name)) {
			p.closeInventory();
			return;
		}

		its.fill(ClickableItem.empty(new ItemBuilder(Material.CHAIN).name("§0.").build()));

		final ArrayList<String> tlrs = new ArrayList<>();
		final ArrayList<String> clrs = new ArrayList<>();
		final ArrayList<String> slrs = new ArrayList<>();
		for (final Map.Entry<Shooter, Arena.Team> en : ar.shtrs.entrySet()) {
			switch (en.getValue()) {
                case Ts -> tlrs.add(Arena.Team.Ts.clr + "✦ §7" + sh.name());
                case CTs -> clrs.add(Arena.Team.CTs.clr + "✦ §7" + sh.name());
                case SPEC -> slrs.add(Arena.Team.SPEC.clr + "✦ §7" + sh.name());
            }
		}

		its.set(4, ClickableItem.of(new ItemBuilder(Material.WARPED_NYLIUM).name(Arena.Team.CTs.clr + "§lСпецназ")
			.addLore(clrs).build(), e -> select(p, sh, Arena.Team.CTs, its)));

		if (ar instanceof Invasion) {
			its.set(0, ClickableItem.of(new ItemBuilder(Material.WARPED_NYLIUM).name(Arena.Team.CTs.clr + "§lСпецназ")
				.addLore(clrs).build(), e -> select(p, sh, Arena.Team.CTs, its)));
		} else {
			its.set(2, ClickableItem.of(new ItemBuilder(Material.ENDER_EYE).name("§5§lСлучайная")
				.addLore(slrs).build(), e -> select(p, sh, switch (ar.gst) {
                case WAITING, BEGINING, FINISH -> Arena.Team.SPEC;
                case BUYTIME ,ROUND ,ENDRND -> ar.getMinTm();
            }, its)));

			its.set(0, ClickableItem.of(new ItemBuilder(Material.CRIMSON_NYLIUM).name(Arena.Team.Ts.clr + "§lТеррористы")
				.addLore(tlrs).build(), e -> select(p, sh, Arena.Team.Ts, its)));
		}
    }

	private void select(final Player p, final Shooter sh, final Arena.Team team, final InventoryContent its) {
		p.playSound(p.getLocation(), Sound.BLOCK_NETHER_GOLD_ORE_FALL, 2f, 2f);
		if (sh.arena().chngTm(sh, team)) {
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
	}
}
