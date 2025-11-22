package me.romindous.cs.Menus;

import java.util.Arrays;
import me.romindous.cs.Enums.GunType;
import me.romindous.cs.Objects.Shooter;
import me.romindous.cs.Objects.Skins.Quest;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import ru.komiss77.modules.items.ItemBuilder;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.player.Perm;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;
import ru.komiss77.utils.inventory.SmartInventory;

public class ChosenSkinMenu implements InventoryProvider {

	public static final ItemStack clear = new ItemBuilder(ItemType.MUSIC_DISC_PIGSTEP).name("§eСброс Обшивок").build();
	public static final ItemStack[] regInv = fillSkinInv(false), dntInv = fillSkinInv(true);

	public static void open(final Player p) {
		SmartInventory.builder().size(3, 9)
			.id("Skins "+p.getName())
			.title("§6Выбери Обшивку")
			.provider(new ChosenSkinMenu())
			.build().open(p);
	}
	
	@Override
	public void init(final Player pl, final InventoryContent its) {
		final Oplayer op = PM.getOplayer(pl);
		final Shooter sh = Shooter.getPlShooter(op.nik, true);
		pl.playSound(pl.getLocation(), Sound.BLOCK_BEEHIVE_SHEAR, 1f, 0.6f);
		
		final Inventory inv = its.getInventory();
		inv.setContents(Perm.isRank(op, 1) ? dntInv : regInv);

		its.set(22, ClickableItem.of(clear, e -> {
			for (final GunType gt : GunType.values()) {
				sh.choose(gt, GunType.DEF_MDL);
			}
			reopen(pl, its);
		}));

		int i = 0;
		for (final GunType gt : GunType.values()) {
			while (switch (i) {
				case 0, 8, 9, 10, 16, 17 -> true;
				default -> false;
			}) i++;
			final Quest shq = Quest.get(gt, sh.model(gt));
			its.set(i, ClickableItem.of(gt.item().name("§5" + gt.toString()).model(gt.skin(shq == null ? GunType.DEF_MDL : shq.model))
				.lore(Arrays.asList("§7Выбранная обшивка снаряжения!", " ", "§6Клик §7==> Выбор")).maxDamage(gt.rtm).build(), e -> {
				SmartInventory.builder().type(InventoryType.HOPPER).id("Quest "+pl.getName())
					.title("§6Выбор обшивки " + gt.name())
					.provider(new QuestSkinMenu(gt))
					.build().open(pl);
			}));
			i++;
		}
	}
	
	private static ItemStack[] fillSkinInv(final boolean donate) {
		final ItemStack[] its = new ItemStack[27];
		final ItemStack rail = new ItemBuilder(donate ? ItemType.POWERED_RAIL
			: ItemType.ACTIVATOR_RAIL).name("§0.").build();
		final ItemStack pane = new ItemBuilder(donate ? ItemType.YELLOW_STAINED_GLASS_PANE
			: ItemType.LIGHT_GRAY_STAINED_GLASS_PANE).name("§0.").build();
		final ItemStack block = new ItemBuilder(donate ? ItemType.OCHRE_FROGLIGHT
			: ItemType.IRON_BLOCK).name("§0.").build();
		final ItemStack plate = new ItemBuilder(donate ? ItemType.LIGHT_WEIGHTED_PRESSURE_PLATE
			: ItemType.HEAVY_WEIGHTED_PRESSURE_PLATE).name("§0.").build();
		for (int r = 0; r != 3; r++) {
			for (int c = 0; c != 9; c++) {
                switch (c) {
                    case 0, 8 -> its[r * 9 + c] = r == 2 ? block : rail;
					case 2, 3, 5, 6 -> its[r * 9 + c] = r == 2 ? pane : null;
					case 1, 7 -> its[r * 9 + c] = r == 0 ? null : r == 1 ? pane : plate;
                }
			}
		}
		return its;
	}
	
	public static void tryCompleteQuest(final Shooter sh, final Quest qst, final int stat) {
		if (sh.has(qst.gun, qst.model)) return;
		switch (qst) {
		case PYROMANCY, PHANTOM, ROOSTER, CRYSTAL, HAPPINESS, SHARD, NETHERITE, LOADER, ICICLE:
			if (stat < qst.stat) return;
			break;
		case ACID:
			if (stat > qst.stat) return;
			break;
		case SCULK, CYBER:
			if (stat == 0) return;
			break;
		}
		
		sh.give(qst.gun, qst.model);
		final Player p = sh.getPlayer();
		p.sendMessage("§8=-=-=-=-=-=-=-\n§7Поздравляю! Скин оружия\n§5" + 
		qst.gun.name() + " §7-§d " + qst.name +
		"\n§7теперь есть в твоем наборе!\n\n§7Можешь §eвыбрать §7его в лобби!\n§8=-=-=-=-=-=-=-=-=-");
		p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
	}

}
