package me.romindous.cs.Menus;

import me.romindous.cs.CSCmd;
import me.romindous.cs.Game.Arena;
import me.romindous.cs.Main;
import me.romindous.cs.Objects.Map.Setup;
import me.romindous.cs.Objects.Map.TypeChoose;
import me.romindous.cs.Objects.Shooter;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemType;
import ru.komiss77.modules.items.ItemBuilder;
import ru.komiss77.utils.ClassUtil;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;
import ru.komiss77.utils.inventory.SmartInventory;


public class GameMenu implements InventoryProvider {

    @Override
    public void init(final Player p, final InventoryContent its) {
		p.playSound(p.getLocation(), Sound.BLOCK_BEEHIVE_ENTER, 1f, 2f);
		its.fill(ClickableItem.empty(new ItemBuilder(ItemType.LIGHT_GRAY_STAINED_GLASS_PANE).name("§0.").build()));
    	its.set(0, ClickableItem.empty(new ItemBuilder(ItemType.PEARLESCENT_FROGLIGHT).name("§0.").build()));
		its.set(1, ClickableItem.empty(new ItemBuilder(ItemType.QUARTZ_SLAB).name("§0.").build()));
		its.set(2, ClickableItem.empty(new ItemBuilder(ItemType.HEAVY_WEIGHTED_PRESSURE_PLATE).name("§0.").build()));
		its.set(6, ClickableItem.empty(new ItemBuilder(ItemType.HEAVY_WEIGHTED_PRESSURE_PLATE).name("§0.").build()));
		its.set(7, ClickableItem.empty(new ItemBuilder(ItemType.QUARTZ_SLAB).name("§0.").build()));
		its.set(8, ClickableItem.empty(new ItemBuilder(ItemType.PEARLESCENT_FROGLIGHT).name("§0.").build()));

		its.set(4, ClickableItem.of(new ItemBuilder(ItemType.ENDER_EYE).name("§5Быстрый Поиск").build(), e -> {
			p.getWorld().playSound(p.getLocation(), Sound.BLOCK_IRON_TRAPDOOR_CLOSE, 2f, 2f);
			final Arena ar = CSCmd.biggestArena();
			if (ar == null) {
				if (Main.nnactvarns.size() > 0) {
					SmartInventory.builder()
						.type(InventoryType.HOPPER)
						.id("Game "+p.getName())
						.provider(new TypeChoose(ClassUtil.rndElmt(Main.nnactvarns.values().toArray(new Setup[0]))))
						.title("§d§l      Выбор Типа Игры")
						.build().open(p);
				} else {
					p.sendMessage(Main.prf() + "§cНи одной карты еще не создано!");
				}
			} else {
				CSCmd.partyJoinMap(Shooter.getPlShooter(p.getName(), true), p, ar);
			}
		}));

		int i = 0;
		for (final Setup stp : Main.nnactvarns.values()) {
			if (stp.fin) {
				final Arena ar = Main.actvarns.get(stp.nm);
				if (ar == null) {
					its.set(i + 9, ClickableItem.of(new ItemBuilder(ItemType.GREEN_CONCRETE_POWDER).name("§d" + stp.nm)
						.lore("§2Ожидание", "§7Игроков: §20§7/§2" + stp.min, " ", "§7Режим: §8Не Выбран").build(), e -> {
						p.getWorld().playSound(p.getLocation(), Sound.BLOCK_IRON_TRAPDOOR_CLOSE, 2f, 2f);
						SmartInventory.builder()
							.type(InventoryType.HOPPER)
							.id("Game "+p.getName())
							.provider(new TypeChoose(stp))
							.title("§d§l      Выбор Типа Игры")
							.build().open(p);
					}));
				} else {
					its.set(i + 9, ClickableItem.of(switch (ar.gst) {
						case WAITING -> new ItemBuilder(ar.shtrs.isEmpty() ? ItemType.GREEN_CONCRETE_POWDER : ItemType.LIME_CONCRETE_POWDER).name("§d" + stp.nm)
							.lore("§2Ожидание", "§7Игроков: §2" + ar.shtrs.size() + "§7/§2" + ar.min, " ", "§7Режим: §d" + ar.getType().name).build();
						case BEGINING -> new ItemBuilder(ItemType.YELLOW_CONCRETE_POWDER).name("§d" + stp.nm)
							.lore("§eНачало", "§7Игроков: §e" + ar.shtrs.size() + "§7/§e" + ar.max, " ", "§7Режим: §d" + ar.getType().name).build();
						case BUYTIME -> new ItemBuilder(ItemType.ORANGE_CONCRETE_POWDER).name("§d" + stp.nm)
							.lore("§6Закупка", "§7Игроков: §6" + ar.getPlaying(true, false) + "§7/§6" + ar.max, " ", "§7Режим: §d" + ar.getType().name).build();
						case ROUND, ENDRND -> new ItemBuilder(ItemType.RED_CONCRETE_POWDER).name("§d" + stp.nm)
							.lore("§6Бой", "§7Игроков: §6" + ar.getPlaying(true, false) + "§7/§6" + ar.max, " ", "§7Режим: §d" + ar.getType().name).build();
						case FINISH -> new ItemBuilder(ItemType.PURPLE_CONCRETE_POWDER).name("§d" + stp.nm)
							.lore("§5Финиш", "§7Игроков: §5" + ar.shtrs.size() + "§7/§5" + ar.max, " ", "§7Режим: §d" + ar.getType().name).build();
					}, e -> {
						p.getWorld().playSound(p.getLocation(), Sound.BLOCK_IRON_TRAPDOOR_CLOSE, 2f, 2f);
						CSCmd.partyJoinMap(Shooter.getPlShooter(p.getName(), true), p, ar);
					}));
				}
				i++;
			}
		}
    }
}
