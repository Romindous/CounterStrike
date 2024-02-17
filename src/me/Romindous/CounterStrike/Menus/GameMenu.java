package me.Romindous.CounterStrike.Menus;

import me.Romindous.CounterStrike.CSCmd;
import me.Romindous.CounterStrike.Game.Arena;
import me.Romindous.CounterStrike.Main;
import me.Romindous.CounterStrike.Objects.Map.Setup;
import me.Romindous.CounterStrike.Objects.Map.TypeChoose;
import me.Romindous.CounterStrike.Objects.Shooter;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import ru.komiss77.ApiOstrov;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;
import ru.komiss77.utils.inventory.SmartInventory;

import static org.bukkit.Material.*;


public class GameMenu implements InventoryProvider {

    @Override
    public void init(final Player p, final InventoryContent its) {
		p.playSound(p.getLocation(), Sound.BLOCK_BEEHIVE_ENTER, 1f, 2f);
		its.fill(ClickableItem.empty(new ItemBuilder(LIGHT_GRAY_STAINED_GLASS_PANE).name("§0.").build()));
    	its.set(0, ClickableItem.empty(new ItemBuilder(PEARLESCENT_FROGLIGHT).name("§0.").build()));
		its.set(1, ClickableItem.empty(new ItemBuilder(QUARTZ_SLAB).name("§0.").build()));
		its.set(2, ClickableItem.empty(new ItemBuilder(HEAVY_WEIGHTED_PRESSURE_PLATE).name("§0.").build()));
		its.set(6, ClickableItem.empty(new ItemBuilder(HEAVY_WEIGHTED_PRESSURE_PLATE).name("§0.").build()));
		its.set(7, ClickableItem.empty(new ItemBuilder(QUARTZ_SLAB).name("§0.").build()));
		its.set(8, ClickableItem.empty(new ItemBuilder(PEARLESCENT_FROGLIGHT).name("§0.").build()));

		its.set(4, ClickableItem.of(Main.mkItm(ENDER_EYE, "§5Быстрый Поиск", 10), e -> {
			p.getWorld().playSound(p.getLocation(), Sound.BLOCK_IRON_TRAPDOOR_CLOSE, 2f, 2f);
			final Arena ar = CSCmd.biggestArena();
			if (ar == null) {
				if (Main.nnactvarns.size() > 0) {
					SmartInventory.builder()
						.type(InventoryType.HOPPER)
						.id("Game "+p.getName())
						.provider(new TypeChoose(ApiOstrov.rndElmt(Main.nnactvarns.values().toArray(new Setup[0]))))
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
					its.set(i + 9, ClickableItem.of(Main.mkItm(GREEN_CONCRETE_POWDER, "§d" + stp.nm, 1, "§2Ожидание",
						"§7Игроков: §20§7/§2" + stp.min, " ", "§7Режим: §8Не Выбран"), e -> {
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
						case WAITING -> Main.mkItm(ar.shtrs.isEmpty() ? GREEN_CONCRETE_POWDER : LIME_CONCRETE_POWDER, "§d" + stp.nm,
							1, "§2Ожидание", "§7Игроков: §2" + ar.shtrs.size() + "§7/§2" + ar.min, " ", "§7Режим: §d" + ar.getType().name);
						case BEGINING -> Main.mkItm(YELLOW_CONCRETE_POWDER, "§d" + stp.nm, 1, "§eНачало",
							"§7Игроков: §e" + ar.shtrs.size() + "§7/§e" + ar.max, " ", "§7Режим: §d" + ar.getType().name);
						case BUYTIME -> Main.mkItm(ORANGE_CONCRETE_POWDER, "§d" + stp.nm, 1, "§6Закупка",
							"§7Игроков: §6" + ar.getPlaying(true, false) + "§7/§6" + ar.max, " ", "§7Режим: §d" + ar.getType().name);
						case ROUND, ENDRND -> Main.mkItm(ORANGE_CONCRETE_POWDER, "§d" + stp.nm, 1, "§6Бой",
							"§7Игроков: §6" + ar.getPlaying(true, false) + "§7/§6" + ar.max, " ", "§7Режим: §d" + ar.getType().name);
						case FINISH -> Main.mkItm(PURPLE_CONCRETE_POWDER, "§d" + stp.nm, 1, "§5Финиш",
							"§7Игроков: §5" + ar.shtrs.size() + "§7/§5" + ar.max, " ", "§7Режим: §d" + ar.getType().name);
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
