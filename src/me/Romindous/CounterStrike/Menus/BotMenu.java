package me.Romindous.CounterStrike.Menus;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;

import me.Romindous.CounterStrike.Main;
import me.Romindous.CounterStrike.Game.Arena;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;



public class BotMenu implements InventoryProvider {

    private final Arena ar;
    
    public InventoryContent its;

    public BotMenu(final Arena ar) {
        this.ar = ar;
        its = null;
    }

    @Override
    public void init(final Player p, final InventoryContent its) {
    	this.its = its;
    	for (int i = 1; i < 7; i++) {
    		if (i < 4) {
				its.set(i, ar.bots ? ClickableItem.empty(new ItemBuilder(Material.GRAY_DYE).name("§0.").build()) 
						: ClickableItem.empty(new ItemBuilder(Material.PINK_STAINED_GLASS_PANE).name("§cНет!").build()));
    		} else {
				its.set(i + 1, ar.bots ? ClickableItem.empty(new ItemBuilder(Material.LIME_STAINED_GLASS_PANE).name("§aДа!").build()) 
						: ClickableItem.empty(new ItemBuilder(Material.GRAY_DYE).name("§0.").build()));
			}
    	}
    	
    	its.set(0, ClickableItem.empty(new ItemBuilder(Material.CHAIN).name("§0.").build()));
    	its.set(4, ClickableItem.empty(new ItemBuilder(Material.END_CRYSTAL).name("§cНет §7<=-=> §aДа").build()));
    	its.set(8, ClickableItem.empty(new ItemBuilder(Material.CHAIN).name("§0.").build()));
    	
    	if (ar.bots) {
        	its.set(Main.srnd.nextInt(3) + 1, ClickableItem.of(new ItemBuilder(Material.PINK_DYE).name("§7Клик -> §cВыключить Ботов!").build(), e -> {
				p.playSound(p.getLocation(), Sound.ENTITY_SNIFFER_DROP_SEED, 1f, 0.6f);
        		ar.bots = false;
        		final ArrayList<HumanEntity> hes = new ArrayList<>(its.getInventory().getViewers());
                for (final HumanEntity he : hes) {
                    reopen((Player) he, its);
                }
        	}));
    	} else {
        	its.set(Main.srnd.nextInt(3) + 5, ClickableItem.of(new ItemBuilder(Material.LIME_DYE).name("§7Клик -> §aВключить Ботов!").build(), e -> {
				p.playSound(p.getLocation(), Sound.ENTITY_SNIFFER_DROP_SEED, 1f, 1.2f);
        		ar.bots = true;
        		final ArrayList<HumanEntity> hes = new ArrayList<>(its.getInventory().getViewers());
                for (final HumanEntity he : hes) {
                    reopen((Player) he, its);
                }
        	}));
		}
    }
}
