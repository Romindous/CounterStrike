package me.romindous.cs.Menus;

import me.romindous.cs.Game.Arena;
import me.romindous.cs.Main;
import me.romindous.cs.Objects.Shooter;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemType;
import ru.komiss77.modules.items.ItemBuilder;
import ru.komiss77.utils.inventory.*;


public class BotMenu implements InventoryProvider {

    private final Arena ar;

    public BotMenu(final Arena ar) {
        this.ar = ar;
    }

    @Override
    public void init(final Player p, final InventoryContent its) {
    	for (int i = 1; i < 7; i++) {
    		if (i < 4) {
				its.set(i, ar.bots ? ClickableItem.empty(new ItemBuilder(ItemType.GRAY_DYE).name("§0.").build())
						: ClickableItem.empty(new ItemBuilder(ItemType.PINK_STAINED_GLASS_PANE).name("§cНет!").build()));
    		} else {
				its.set(i + 1, ar.bots ? ClickableItem.empty(new ItemBuilder(ItemType.LIME_STAINED_GLASS_PANE).name("§aДа!").build()) 
						: ClickableItem.empty(new ItemBuilder(ItemType.GRAY_DYE).name("§0.").build()));
			}
    	}
    	
    	its.set(0, ClickableItem.empty(new ItemBuilder(ItemType.CHAIN).name("§0.").build()));
    	its.set(4, ClickableItem.empty(new ItemBuilder(ItemType.END_CRYSTAL).name("§cНет §7<=-=> §aДа").build()));
    	its.set(8, ClickableItem.empty(new ItemBuilder(ItemType.CHAIN).name("§0.").build()));
    	
    	if (ar.bots) {
        	its.set(Main.srnd.nextInt(3) + 1, ClickableItem.of(new ItemBuilder(ItemType.PINK_DYE).name("§7Клик -> §cВыключить Ботов!").build(), e -> {
				p.playSound(p.getLocation(), Sound.ENTITY_SNIFFER_DROP_SEED, 1f, 0.6f);
        		ar.bots = false;
				for (final Shooter sh : ar.shtrs.keySet()) {
					final Player pl = sh.getPlayer();
					if (pl == null) continue;
					final SmartInventory si = InventoryManager.getInventory(pl).orElse(null);
					if (si != null && si.getProvider() instanceof final BotMenu bm) {
						pl.closeInventory();
						bm.reopen(pl, its);
					}
				}
        	}));
    	} else {
        	its.set(Main.srnd.nextInt(3) + 5, ClickableItem.of(new ItemBuilder(ItemType.LIME_DYE).name("§7Клик -> §aВключить Ботов!").build(), e -> {
				p.playSound(p.getLocation(), Sound.ENTITY_SNIFFER_DROP_SEED, 1f, 1.2f);
        		ar.bots = true;
				for (final Shooter sh : ar.shtrs.keySet()) {
					final Player pl = sh.getPlayer();
					if (pl == null) continue;
					final SmartInventory si = InventoryManager.getInventory(pl).orElse(null);
					if (si != null && si.getProvider() instanceof final BotMenu bm) {
						pl.closeInventory();
						bm.reopen(pl, its);
					}
				}
        	}));
		}
    }
}
