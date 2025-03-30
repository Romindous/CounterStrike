package me.Romindous.CounterStrike.Menus;

import java.util.Arrays;
import me.Romindous.CounterStrike.Enums.GunType;
import me.Romindous.CounterStrike.Main;
import me.Romindous.CounterStrike.Objects.Shooter;
import me.Romindous.CounterStrike.Objects.Skins.Quest;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemType;
import ru.komiss77.modules.items.ItemBuilder;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.player.Perm;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;

public class QuestSkinMenu implements InventoryProvider {

	private static final int MAX_SKINS = 5;

	private final GunType gt;

	public QuestSkinMenu(final GunType gt) {
		this.gt = gt;
	}

	@Override
	public void init(final Player pl, final InventoryContent its) {
		final Oplayer op = PM.getOplayer(pl);
		final Shooter sh = Shooter.getPlShooter(op.nik, true);
		pl.playSound(pl.getLocation(), Sound.BLOCK_BEEHIVE_ENTER, 1f, 1.4f);

        final Quest shq = Quest.get(gt, sh.model(gt));
		final boolean def = shq == null;
		its.add(ClickableItem.of(gt.item().name("§5" + gt.name()).model(gt.skin(GunType.DEF_MDL)).glint(def).maxDamage(gt.rtm)
			.lore(Arrays.asList("§7Ванильная обшивка снаряжения!", " ", def ? " " : "§6Клик §7==> Выбор")).build(), e -> {
			if (!def) {
				pl.playSound(pl.getLocation(), Sound.ITEM_ARMOR_EQUIP_NETHERITE, 2f, 1.5f);
				sh.choose(gt, GunType.DEF_MDL);
			}
			ChosenSkinMenu.open(pl);
		}));
		final boolean dnt = Perm.isRank(op, 1);
		final Quest[] qs = Quest.get(gt);
		for (int i = Math.min(qs.length, MAX_SKINS); i != 0; i--) {
			final Quest q = qs[i-1];
			if (!sh.has(gt, q.model) && !dnt) {
				its.add(ClickableItem.empty(new ItemBuilder(ItemType.GRAY_DYE)
					.name("§8" + gt.name() + " '" + Main.nrmlzStr(q.name()) + "'")
					.lore("§7Для получения:").lore(q.msg).build()));
				continue;
			}
			final boolean match = q == shq;
			its.add(ClickableItem.of(gt.item()
				.name("§5" + gt.name() + " '" + Main.nrmlzStr(q.name()) + "'")
				.model(gt.skin(q.model)).glint(match).maxDamage(gt.rtm)
				.lore(Arrays.asList("§7Для получения: §8(получена)", q.msg, " ",
					match ? " " : "§6Клик §7==> Выбор")).build(), e -> {
				if (!match) {
					pl.playSound(pl.getLocation(), Sound.ITEM_ARMOR_EQUIP_NETHERITE, 2f, 1.5f);
					sh.choose(gt, q.model);
				}
				ChosenSkinMenu.open(pl);
			}));
		}
	}

}
