package me.romindous.cs.Objects.Map;

import java.util.Arrays;

import me.romindous.cs.Enums.GameType;
import me.romindous.cs.Objects.Game.PlShooter;
import org.bukkit.entity.Player;

import me.romindous.cs.Main;
import me.romindous.cs.CSCmd;
import me.romindous.cs.Game.Arena;
import me.romindous.cs.Objects.Shooter;
import org.bukkit.inventory.ItemType;
import ru.komiss77.modules.items.ItemBuilder;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;

public class TypeChoose implements InventoryProvider {
	
	private final Setup stp;
	
	public TypeChoose(final Setup stp) {
		this.stp = stp;
	}
	
	@Override
	public void init(final Player pl, final InventoryContent its) {
		if (stp == null) {
			pl.closeInventory();
			return;
		}
		
		final PlShooter sh = Shooter.getPlShooter(pl.getName(), true);
		//pl.sendMessage(stp.worlds.toString());
		
		
		if (stp.worlds.get(GameType.DEFUSAL) != null) {
			its.set(0, ClickableItem.of(new ItemBuilder(ItemType.ENDER_EYE).name("              §5§n" + GameType.DEFUSAL.name)
			.lore(Arrays.asList(" ", "§7Старый-добрый режим §dКонтры", "§7с установкой и разминировкой", "§dбомбы §7на точках §bA §7и §6B")).build(), e -> {
				final Arena ar = Main.actvarns.get(stp.nm);
				CSCmd.partyJoinMap(sh, pl, ar == null ? Main.plug.crtArena(stp.nm, GameType.DEFUSAL) : ar);
			}));
		}
		
		if (stp.worlds.get(GameType.GUNGAME) != null) {
			its.set(2, ClickableItem.of(new ItemBuilder(ItemType.ENDER_PEARL).name("              §5§n" + GameType.GUNGAME.name)
			.lore(Arrays.asList(" ", "§7Режим со сменой §dоружия", "§7при §dубийстве §7соперника, хорошо", "§7подходит для практики с ними")).build(), e -> {
				final Arena ar = Main.actvarns.get(stp.nm);
				CSCmd.partyJoinMap(sh, pl, ar == null ? Main.plug.crtArena(stp.nm, GameType.GUNGAME) : ar);
			}));
		}
		
		if (stp.worlds.get(GameType.INVASION) != null) {
			its.set(4, ClickableItem.of(new ItemBuilder(ItemType.END_PORTAL_FRAME).name("              §5§n" + GameType.INVASION.name)
			.lore(Arrays.asList(" ", "§7Режим защиты точек §bA §7и §6B", "§7от коварных §dмонстров§7, с цыклом", "§7дня и ночи на карте")).build(), e -> {
				final Arena ar = Main.actvarns.get(stp.nm);
				CSCmd.partyJoinMap(sh, pl, ar == null ? Main.plug.crtArena(stp.nm, GameType.INVASION) : ar);
			}));
		}
	}
}
