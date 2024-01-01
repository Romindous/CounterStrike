package me.Romindous.CounterStrike.Listeners;

import java.util.Arrays;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
<<<<<<<< HEAD:CounterStrike/src/me/Romindous/CounterStrike/Listeners/InventLis.java
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerItemHeldEvent;
========
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.InventoryHolder;
>>>>>>>> 8ac1214 (Intellij Transfer):src/me/Romindous/CounterStrike/Listeners/InventLis.java
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

<<<<<<<< HEAD:CounterStrike/src/me/Romindous/CounterStrike/Listeners/InventLis.java
import com.mojang.datafixers.util.Pair;

import me.Romindous.CounterStrike.Main;
import me.Romindous.CounterStrike.Enums.GunType;
import me.Romindous.CounterStrike.Enums.NadeType;
import me.Romindous.CounterStrike.Game.Arena;
import me.Romindous.CounterStrike.Game.Defusal;
import me.Romindous.CounterStrike.Game.Invasion;
import me.Romindous.CounterStrike.Game.Arena.Team;
import me.Romindous.CounterStrike.Objects.Shooter;
import me.Romindous.CounterStrike.Utils.PacketUtils;
import ru.komiss77.ApiOstrov;
import ru.komiss77.enums.Stat;
========
import me.Romindous.CounterStrike.Main;
import me.Romindous.CounterStrike.Commands.CSCmd;
import me.Romindous.CounterStrike.Enums.GunType;
import me.Romindous.CounterStrike.Enums.NadeType;
import me.Romindous.CounterStrike.Game.Arena;
import me.Romindous.CounterStrike.Game.Arena.Team;
import me.Romindous.CounterStrike.Game.Defusal;
import me.Romindous.CounterStrike.Game.Invasion;
import me.Romindous.CounterStrike.Objects.Shooter;
import me.Romindous.CounterStrike.Objects.Game.PlShooter;
import me.Romindous.CounterStrike.Objects.Map.Setup;
import me.Romindous.CounterStrike.Objects.Map.TypeChoose;
import me.Romindous.CounterStrike.Objects.Skins.Quest;
import ru.komiss77.ApiOstrov;
import ru.komiss77.enums.Stat;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.ItemUtils;
import ru.komiss77.utils.TCUtils;
import ru.komiss77.utils.inventory.SmartInventory;
>>>>>>>> 8ac1214 (Intellij Transfer):src/me/Romindous/CounterStrike/Listeners/InventLis.java

public class InventLis implements Listener {
   
	@EventHandler
	public void onOpen(final InventoryOpenEvent e) {
<<<<<<<< HEAD:CounterStrike/src/me/Romindous/CounterStrike/Listeners/InventLis.java
		e.setCancelled(e.getInventory().getHolder() != null && e.getPlayer().getGameMode() != GameMode.CREATIVE);
========
		final InventoryHolder hld = e.getInventory().getHolder();
		if (hld == null || hld instanceof Player) return;
		e.setCancelled(e.getPlayer().getGameMode() != GameMode.CREATIVE);
>>>>>>>> 8ac1214 (Intellij Transfer):src/me/Romindous/CounterStrike/Listeners/InventLis.java
	}
	
	@EventHandler
	public void onSlot(final PlayerItemHeldEvent e) {
		final Player p = e.getPlayer();
<<<<<<<< HEAD:CounterStrike/src/me/Romindous/CounterStrike/Listeners/InventLis.java
		final Pair<Shooter, Arena> pr = Shooter.getPlShtrArena(p.getName());
		final Shooter sh = pr.getFirst();
		sh.stm = 0;
		sh.cnt = 0;
		final ItemStack it = sh.inv.getItem(e.getNewSlot());
========
		final PlShooter sh = Shooter.getPlShooter(p.getName(), true);
		sh.shtTm(0);
		sh.count(0);
		final ItemStack it = sh.item(e.getNewSlot());
>>>>>>>> 8ac1214 (Intellij Transfer):src/me/Romindous/CounterStrike/Listeners/InventLis.java
		final GunType gt = GunType.getGnTp(it);
		if (gt != null) {
			p.getWorld().playSound(p.getLocation(), gt.prm ? Sound.ITEM_ARMOR_EQUIP_IRON : Sound.ITEM_ARMOR_EQUIP_GOLD, 2, 2);
			if (((Damageable)it.getItemMeta()).hasDamage()) {
<<<<<<<< HEAD:CounterStrike/src/me/Romindous/CounterStrike/Listeners/InventLis.java
				sh.cnt = (short) ((it.getType().getMaxDurability() - ((Damageable)it.getItemMeta()).getDamage()) * gt.rtm / it.getType().getMaxDurability());
			}
		}
		if (p.isSneaking()) {
			PacketUtils.fkHlmtClnt(p, sh.inv.getHelmet());
			PacketUtils.zoom(p, false);
			e.getPlayer().setSneaking(false);
========
				sh.count((it.getType().getMaxDurability() - ((Damageable)it.getItemMeta()).getDamage()) * gt.rtm / it.getType().getMaxDurability());
			}
		}
		
		if (p.isSneaking()) {
			sh.scope(false);
			if (!ItemUtils.isBlank(p.getInventory().getItemInOffHand(), false))
				p.getInventory().setItemInOffHand(Main.air);
>>>>>>>> 8ac1214 (Intellij Transfer):src/me/Romindous/CounterStrike/Listeners/InventLis.java
		}
		
		if (it != null) {
			switch (it.getType()) {
			case GOLDEN_APPLE:
<<<<<<<< HEAD:CounterStrike/src/me/Romindous/CounterStrike/Listeners/InventLis.java
				if (pr.getSecond() != null && !((Defusal) pr.getSecond()).indon) {
					((Defusal) pr.getSecond()).indSts(PacketUtils.getNMSPlr(p).b);
========
				if (sh.arena() != null && !((Defusal) sh.arena()).indon) {
					((Defusal) sh.arena()).indSts(p);
>>>>>>>> 8ac1214 (Intellij Transfer):src/me/Romindous/CounterStrike/Listeners/InventLis.java
				}
				return;
			default:
				break;
			}
		}
<<<<<<<< HEAD:CounterStrike/src/me/Romindous/CounterStrike/Listeners/InventLis.java
		
		if (p.getInventory().getItemInMainHand() != null) {
			switch (p.getInventory().getItemInMainHand().getType()) {
			case GOLDEN_APPLE:
				if (pr.getSecond() != null && ((Defusal) pr.getSecond()).indon) {
					((Defusal) pr.getSecond()).indSts(PacketUtils.getNMSPlr(p).b);
				}
				return;
			default:
				break;
			}
		}
	}
========

        if (p.getInventory().getItemInMainHand().getType() == Material.GOLDEN_APPLE) {
            if (sh.arena() != null && ((Defusal) sh.arena()).indon) {
                ((Defusal) sh.arena()).indSts(p);
            }
        }
    }
>>>>>>>> 8ac1214 (Intellij Transfer):src/me/Romindous/CounterStrike/Listeners/InventLis.java
   
	@EventHandler
	public void onClick(final InventoryClickEvent e) {
		final HumanEntity p = e.getWhoClicked();
		final ItemStack it = e.getCurrentItem();
<<<<<<<< HEAD:CounterStrike/src/me/Romindous/CounterStrike/Listeners/InventLis.java
========
		switch (e.getAction()) {
		case CLONE_STACK, COLLECT_TO_CURSOR, MOVE_TO_OTHER_INVENTORY:
			e.setCancelled(true);
			return;
		default:
			break;
		}
>>>>>>>> 8ac1214 (Intellij Transfer):src/me/Romindous/CounterStrike/Listeners/InventLis.java
		if (e.getClick() == ClickType.NUMBER_KEY) {
			e.setCancelled(true);
			return;
		}
<<<<<<<< HEAD:CounterStrike/src/me/Romindous/CounterStrike/Listeners/InventLis.java
		
		if (e.getView().getTitle().equalsIgnoreCase("§5§lТренировка")) {
			if (e.getCurrentItem() != null) {
				final ItemStack cp;
				e.setCancelled(true);
				if (e.getClickedInventory().getType() != InventoryType.PLAYER && it.getItemMeta().hasDisplayName()) {
					((Player) p).playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_NETHERITE, 2f, 1.5f);
					switch (it.getItemMeta().getDisplayName().charAt(1)) {
					case '5':
						cp = it.clone();
						cp.setAmount((GunType.getGnTp(it)).amo);
						p.getInventory().setItem(0, cp);
						break;
					case 'd':
						cp = it.clone();
						cp.setAmount((GunType.getGnTp(it)).amo);
						p.getInventory().setItem(1, cp);
========
		final String ttl = TCUtils.stripColor(e.getView().title());
		final Shooter pr;
		//p.sendMessage(ttl);
		switch (ttl) {
		case "Тренировка":
			if (e.getCurrentItem() != null) {
				e.setCancelled(true);
				if (e.getClickedInventory().getType() != InventoryType.PLAYER && it.getItemMeta().hasDisplayName()) {
					p.getWorld().playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_NETHERITE, 2f, 1.5f);
					final int mdl;
					final GunType gt;
					switch (TCUtils.toString(it.getItemMeta().displayName()).charAt(1)) {
					case '5':
						pr = Shooter.getPlShooter(p.getName(), true);
						gt = GunType.getGnTp(it);
						mdl = pr.getModel(gt);
						p.getInventory().setItem(0, new ItemBuilder(it.getType()).name((mdl == GunType.defCMD ? "§5" + gt.toString() : 
							"§5" + gt.toString() + " '" + Main.nrmlzStr(Quest.getQuest(gt, mdl).toString()) + "'") + " " + gt.icn)
						.setAmount(gt.amo).setModelData(mdl).build());
						break;
					case 'd':
						pr = Shooter.getPlShooter(p.getName(), true);
						gt = GunType.getGnTp(it);
						mdl = pr.getModel(gt);
						p.getInventory().setItem(1, new ItemBuilder(it.getType()).name((mdl == GunType.defCMD ? "§5" + gt.toString() : 
							"§5" + gt.toString() + " '" + Main.nrmlzStr(Quest.getQuest(gt, mdl).toString()) + "'") + " " + gt.icn)
						.setAmount(gt.amo).setModelData(mdl).build());
>>>>>>>> 8ac1214 (Intellij Transfer):src/me/Romindous/CounterStrike/Listeners/InventLis.java
						break;
					case '6':
					case 'c':
						p.getInventory().setItem(3, it);
						break;
					case '2':
					case '7':
					case '8':
						addSetItm(p.getInventory(), 4, it);
						break;
					case '3':
						p.getInventory().setItem(7, it);
						break;
					}
				}
			}
<<<<<<<< HEAD:CounterStrike/src/me/Romindous/CounterStrike/Listeners/InventLis.java
		} else if (e.getView().getTitle().equalsIgnoreCase("§c§lМагазин Террористов") || e.getView().getTitle().equalsIgnoreCase("§9§lМагазин Спецназа")) {
			if (it != null) {
				final Pair<Shooter, Arena> pr = Shooter.getPlShtrArena(p.getName());
========
			break;
		case "Магазин Спецназа":
		case "Магазин Террористов":
			if (it != null) {
				pr = Shooter.getPlShooter(p.getName(), true);
>>>>>>>> 8ac1214 (Intellij Transfer):src/me/Romindous/CounterStrike/Listeners/InventLis.java
				final ItemStack cp;
				final NadeType nt = NadeType.getNdTp(it);
				final GunType gt = GunType.getGnTp(it);
				e.setCancelled(true);
				if (e.getClickedInventory().getType() != InventoryType.PLAYER && it.getItemMeta().hasDisplayName()) {
					if (gt != null) {
<<<<<<<< HEAD:CounterStrike/src/me/Romindous/CounterStrike/Listeners/InventLis.java
						if (pr.getFirst().money - gt.prc < 0) {
							p.sendMessage(Main.prf() + "§cУ вас не хватает денег для покупки этого!");
							((Player) p).playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_TURTLE, 1f, 2f);
						} else {
							pr.getSecond().chngMn(pr.getFirst(), -gt.prc);
							cp = it.clone();
							cp.setAmount(gt.amo);
							final ItemMeta im = cp.getItemMeta();
							im.setLore(Arrays.asList());
							cp.setItemMeta(im);
							p.getInventory().setHeldItemSlot(gt.prm ? 0 : 1);
							p.dropItem(false);
							p.getInventory().setHeldItemSlot(8);
							p.getInventory().setItem(gt.prm ? 0 : 1, cp);
							((Player) p).playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_NETHERITE, 2f, 1.5f);
						}
					} else if (nt != null) {
						if (nt.prm) {
							if (pr.getFirst().money - nt.prc < 0) {
								p.sendMessage(Main.prf() + "§cУ вас не хватает денег для покупки этого!");
								((Player) p).playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_TURTLE, 1f, 2f);
							} else if (p.getInventory().getItem(3) != null) {
								p.sendMessage(Main.prf() + "§cУ вас уже есть граната в этом слоту!");
								((Player) p).playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_TURTLE, 1f, 2f);
							} else {
								pr.getSecond().chngMn(pr.getFirst(), -nt.prc);
								cp = it.clone();
								final ItemMeta im = cp.getItemMeta();
								im.setLore(Arrays.asList());
								cp.setItemMeta(im);
								p.getInventory().setItem(3, cp);
								((Player) p).playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_NETHERITE, 2f, 1.5f);
							}
						} else {
							if (pr.getFirst().money - nt.prc < 0) {
								p.sendMessage(Main.prf() + "§cУ вас не хватает денег для покупки этого!");
								((Player) p).playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_TURTLE, 1f, 2f);
							} else if (p.getInventory().getItem(4) != null && p.getInventory().getItem(4).getType() != it.getType()) {
								p.sendMessage(Main.prf() + "§cУ вас уже есть граната в этом слоту!");
								((Player) p).playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_TURTLE, 1f, 2f);
							} else {
								pr.getSecond().chngMn(pr.getFirst(), -nt.prc);
								cp = it.clone();
								final ItemMeta im = cp.getItemMeta();
								im.setLore(Arrays.asList());
								cp.setItemMeta(im);
								addSetItm(p.getInventory(), 4, cp);
								((Player) p).playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_NETHERITE, 2f, 1.5f);
========
						if (pr.money() - gt.prc < 0) {
							p.sendMessage(Main.prf() + "§cУ вас не хватает денег для покупки этого!");
							((Player) p).playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_TURTLE, 1f, 2f);
						} else {
							pr.arena().chngMn(pr, -gt.prc);
							p.getInventory().setHeldItemSlot(gt.prm ? 0 : 1);
							p.dropItem(false);
							p.getInventory().setHeldItemSlot(8);
							final int mdl = pr.getModel(gt);
							p.getInventory().setItem(gt.prm ? 0 : 1, 
							new ItemBuilder(it.getType()).name((mdl == GunType.defCMD ? "§5" + gt.toString() : 
								"§5" + gt.toString() + " '" + Main.nrmlzStr(Quest.getQuest(gt, mdl).toString()) + "'") + " " + gt.icn)
							.setAmount(gt.amo).setModelData(mdl).build());
							p.getWorld().playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_NETHERITE, 1f, 1.4f);
						}
					} else if (nt != null) {
						if (nt.prm) {
							if (pr.money() - nt.prc < 0) {
								p.sendMessage(Main.prf() + "§cУ вас не хватает денег для покупки этого!");
								((Player) p).playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_TURTLE, 1f, 2f);
							} else if (p.getInventory().getItem(NadeType.prmSlot) != null) {
								p.sendMessage(Main.prf() + "§cУ вас уже есть граната в этом слоту!");
								((Player) p).playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_TURTLE, 1f, 2f);
							} else {
								pr.arena().chngMn(pr, -nt.prc);
								cp = it.clone();
								final ItemMeta im = cp.getItemMeta();
								im.lore(Arrays.asList());
								cp.setItemMeta(im);
								p.getInventory().setItem(NadeType.prmSlot, cp);
								p.getWorld().playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_DIAMOND, 1f, 1.4f);
							}
						} else {
							if (pr.money() - nt.prc < 0) {
								p.sendMessage(Main.prf() + "§cУ вас не хватает денег для покупки этого!");
								((Player) p).playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_TURTLE, 1f, 2f);
							} else if (p.getInventory().getItem(NadeType.scdSlot) != null && 
								p.getInventory().getItem(NadeType.scdSlot).getType() != it.getType()) {
								p.sendMessage(Main.prf() + "§cУ вас уже есть граната в этом слоту!");
								((Player) p).playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_TURTLE, 1f, 2f);
							} else {
								cp = it.clone();
								final ItemMeta im = cp.getItemMeta();
								im.lore(Arrays.asList());
								cp.setItemMeta(im);
								if (addSetItm(p.getInventory(), NadeType.scdSlot, cp)) {
									pr.arena().chngMn(pr, -nt.prc);
									p.getWorld().playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_DIAMOND, 1f, 1.4f);
								} else {
									p.sendMessage(Main.prf() + "§cУ вас уже есть 2 таких гранаты!");
								}
>>>>>>>> 8ac1214 (Intellij Transfer):src/me/Romindous/CounterStrike/Listeners/InventLis.java
							}
						}
					} else {
						switch (it.getType()) {
						case SUGAR:
<<<<<<<< HEAD:CounterStrike/src/me/Romindous/CounterStrike/Listeners/InventLis.java
							if (pr.getFirst().money - Main.twrPrc < 0) {
========
							if (pr.money() - GunType.twrPrc < 0) {
>>>>>>>> 8ac1214 (Intellij Transfer):src/me/Romindous/CounterStrike/Listeners/InventLis.java
								p.sendMessage(Main.prf() + "§cУ вас не хватает денег для покупки этого!");
								((Player) p).playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_TURTLE, 1f, 2f);
							} else if (p.getInventory().getItem(3) != null) {
								p.sendMessage(Main.prf() + "§cУ вас уже есть граната в этом слоту!");
								((Player) p).playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_TURTLE, 1f, 2f);
							} else {
<<<<<<<< HEAD:CounterStrike/src/me/Romindous/CounterStrike/Listeners/InventLis.java
								pr.getSecond().chngMn(pr.getFirst(), -Main.twrPrc);
								cp = it.clone();
								final ItemMeta im = cp.getItemMeta();
								im.setLore(Arrays.asList());
								cp.setItemMeta(im);
								p.getInventory().setItem(3, cp);
								((Player) p).playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_NETHERITE, 2f, 1.5f);
							}
							break;
						case SHEARS:
							if (pr.getFirst().money - Main.dfktPrc < 0) {
========
								pr.arena().chngMn(pr, -GunType.twrPrc);
								cp = it.clone();
								final ItemMeta im = cp.getItemMeta();
								im.lore(Arrays.asList());
								cp.setItemMeta(im);
								p.getInventory().setItem(3, cp);
								p.getWorld().playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_DIAMOND, 1f, 1.4f);
							}
							break;
						case SHEARS:
							if (pr.money() - GunType.dfktPrc < 0) {
>>>>>>>> 8ac1214 (Intellij Transfer):src/me/Romindous/CounterStrike/Listeners/InventLis.java
								p.sendMessage(Main.prf() + "§cУ вас не хватает денег для покупки этого!");
								((Player) p).playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_TURTLE, 1f, 2f);
							} else if (p.getInventory().getItem(7) != null && p.getInventory().getItem(7).getType() == Material.SHEARS) {
								p.sendMessage(Main.prf() + "§cУ вас уже есть спец. набор!");
								((Player) p).playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_TURTLE, 1f, 2f);
							} else {
<<<<<<<< HEAD:CounterStrike/src/me/Romindous/CounterStrike/Listeners/InventLis.java
								pr.getSecond().chngMn(pr.getFirst(), -Main.dfktPrc);
								cp = it.clone();
								final ItemMeta im = cp.getItemMeta();
								im.setLore(Arrays.asList());
								cp.setItemMeta(im);
								p.getInventory().setItem(7, cp);
								((Player) p).playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_NETHERITE, 2f, 1.5f);
							}
							break;
						case LEATHER_HELMET:
							if (pr.getFirst().money - Main.hlmtPrc < 0) {
========
								pr.arena().chngMn(pr, -GunType.dfktPrc);
								cp = it.clone();
								final ItemMeta im = cp.getItemMeta();
								im.lore(Arrays.asList());
								cp.setItemMeta(im);
								p.getInventory().setItem(7, cp);
								p.getWorld().playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_DIAMOND, 1f, 1.4f);
							}
							break;
						case LEATHER_HELMET:
							if (pr.money() - GunType.hlmtPrc < 0) {
>>>>>>>> 8ac1214 (Intellij Transfer):src/me/Romindous/CounterStrike/Listeners/InventLis.java
								p.sendMessage(Main.prf() + "§cУ вас не хватает денег для покупки этого!");
								((Player) p).playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_TURTLE, 1f, 2f);
							} else if (p.getInventory().getHelmet() != null) {
								p.sendMessage(Main.prf() + "§cУ вас уже есть шлем!");
								((Player) p).playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_TURTLE, 1f, 2f);
							} else {
<<<<<<<< HEAD:CounterStrike/src/me/Romindous/CounterStrike/Listeners/InventLis.java
								pr.getSecond().chngMn(pr.getFirst(), -Main.hlmtPrc);
								cp = it.clone();
								final ItemMeta im = cp.getItemMeta();
								im.setLore(Arrays.asList());
								cp.setItemMeta(im);
								p.getInventory().setHelmet(cp);
								((Player) p).playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_IRON, 2f, 1.5f);
							}
							break;
						case LEATHER_CHESTPLATE:
							if (pr.getFirst().money - Main.chstPrc < 0) {
========
								pr.arena().chngMn(pr, -GunType.hlmtPrc);
								cp = it.clone();
								final ItemMeta im = cp.getItemMeta();
								im.lore(Arrays.asList());
								cp.setItemMeta(im);
								p.getInventory().setHelmet(cp);
								p.getWorld().playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_IRON, 1f, 1.4f);
							}
							break;
						case LEATHER_CHESTPLATE:
							if (pr.money() - GunType.chstPrc < 0) {
>>>>>>>> 8ac1214 (Intellij Transfer):src/me/Romindous/CounterStrike/Listeners/InventLis.java
								p.sendMessage(Main.prf() + "§cУ вас не хватает денег для покупки этого!");
								((Player) p).playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_TURTLE, 1f, 2f);
							} else if (p.getInventory().getChestplate() != null) {
								p.sendMessage(Main.prf() + "§cУ вас уже есть нагрудник!");
								((Player) p).playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_TURTLE, 1f, 2f);
							} else {
								cp = it.clone();
								final ItemMeta im = cp.getItemMeta();
<<<<<<<< HEAD:CounterStrike/src/me/Romindous/CounterStrike/Listeners/InventLis.java
								im.setLore(Arrays.asList());
								cp.setItemMeta(im);
								pr.getSecond().chngMn(pr.getFirst(), -Main.chstPrc);
								p.getInventory().setChestplate(cp);
								((Player) p).playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_IRON, 2f, 1.5f);
========
								im.lore(Arrays.asList());
								cp.setItemMeta(im);
								pr.arena().chngMn(pr, -GunType.chstPrc);
								p.getInventory().setChestplate(cp);
								p.getWorld().playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_IRON, 1f, 1.4f);
>>>>>>>> 8ac1214 (Intellij Transfer):src/me/Romindous/CounterStrike/Listeners/InventLis.java
							}
							break;
						default:
							break;
						}
					}
				}
			}
<<<<<<<< HEAD:CounterStrike/src/me/Romindous/CounterStrike/Listeners/InventLis.java
		} else if (e.getView().getTitle().equalsIgnoreCase("§5§lВыбор Игры")) {
========
			break;
		case "Выбор Игры":
>>>>>>>> 8ac1214 (Intellij Transfer):src/me/Romindous/CounterStrike/Listeners/InventLis.java
			e.setCancelled(true);
			if (it != null) {
				switch (it.getType()) {
				case GREEN_CONCRETE_POWDER:
				case YELLOW_CONCRETE_POWDER:
				case ORANGE_CONCRETE_POWDER:
				case PURPLE_CONCRETE_POWDER:
					if (it.hasItemMeta()) {
						p.getWorld().playSound(p.getLocation(), Sound.BLOCK_IRON_TRAPDOOR_CLOSE, 2f, 2f);
<<<<<<<< HEAD:CounterStrike/src/me/Romindous/CounterStrike/Listeners/InventLis.java
						((Player) p).performCommand("cs join " + it.getItemMeta().getDisplayName().substring(2));
========
						final String nm = TCUtils.toString(it.getItemMeta().displayName()).substring(2);
						final Arena ar = Main.actvarns.get(nm);
						if (ar == null) {
							final Setup stp = Main.nnactvarns.get(nm);
							if (stp == null) {
								p.sendMessage(Main.prf() + "§cТакой карты не существует!");
								return;
							}
							SmartInventory.builder()
							.type(InventoryType.HOPPER)
	                        .id("Game "+p.getName())
	                        .provider(new TypeChoose(stp))
	                        .title("§d§l      Выбор Типа Игры")
	                        .build().open((Player) p);
						} else {
							CSCmd.partyJoinMap(Shooter.getPlShooter(p.getName(), true), (Player) p, ar);
						}
>>>>>>>> 8ac1214 (Intellij Transfer):src/me/Romindous/CounterStrike/Listeners/InventLis.java
					}
					break;
				case ENDER_EYE:
					if (it.hasItemMeta()) {
						p.getWorld().playSound(p.getLocation(), Sound.BLOCK_IRON_TRAPDOOR_CLOSE, 2f, 2f);
<<<<<<<< HEAD:CounterStrike/src/me/Romindous/CounterStrike/Listeners/InventLis.java
						((Player) p).performCommand("cs join");
========
						final Arena ar = CSCmd.biggestArena();
						if (ar == null) {
							if (Main.nnactvarns.size() > 0) {
								SmartInventory.builder()
								.type(InventoryType.HOPPER)
		                        .id("Game "+p.getName())
		                        .provider(new TypeChoose(Main.rndElmt(Main.nnactvarns.values().toArray(new Setup[0]))))
		                        .title("§d§l      Выбор Типа Игры")
		                        .build().open((Player) p);
							} else {
								p.sendMessage(Main.prf() + "§cНи одной карты еще не создано!");
							}
						} else {
							CSCmd.partyJoinMap(Shooter.getPlShooter(p.getName(), true), (Player) p, ar);
						}
>>>>>>>> 8ac1214 (Intellij Transfer):src/me/Romindous/CounterStrike/Listeners/InventLis.java
					}
					break;
				default:
					break;
				}
			}
<<<<<<<< HEAD:CounterStrike/src/me/Romindous/CounterStrike/Listeners/InventLis.java
		} else if (e.getView().getTitle().equalsIgnoreCase("§eВыбор Комманды")) {
			e.setCancelled(true);
			final Pair<Shooter, Arena> pr = Shooter.getPlShtrArena(p.getName());
			if (pr.getSecond() != null) {
				switch (it.getType()) {
				case WARPED_NYLIUM:
					((Player) p).playSound(p.getLocation(), Sound.BLOCK_NETHER_GOLD_ORE_FALL, 2f, 2f);
					((Defusal) pr.getSecond()).chngTm(pr.getFirst(), Team.CTs);
					break;
				case CRIMSON_NYLIUM:
					((Player) p).playSound(p.getLocation(), Sound.BLOCK_NETHER_GOLD_ORE_FALL, 2f, 2f);
					((Defusal) pr.getSecond()).chngTm(pr.getFirst(), Team.Ts);
					break;
				case ENDER_EYE:
					((Player) p).playSound(p.getLocation(), Sound.BLOCK_NETHER_GOLD_ORE_FALL, 2f, 2f);
					((Defusal) pr.getSecond()).chngTm(pr.getFirst(), Team.NA);
========
			break;
		case "Выбор Комманды":
			e.setCancelled(true);
			pr = Shooter.getPlShooter(p.getName(), true);
			if (pr.arena() != null && it != null) {
				switch (it.getType()) {
				case WARPED_NYLIUM:
					((Player) p).playSound(p.getLocation(), Sound.BLOCK_NETHER_GOLD_ORE_FALL, 2f, 2f);
					pr.arena().chngTm(pr, Team.CTs);
					break;
				case CRIMSON_NYLIUM:
					((Player) p).playSound(p.getLocation(), Sound.BLOCK_NETHER_GOLD_ORE_FALL, 2f, 2f);
					pr.arena().chngTm(pr, Team.Ts);
					break;
				case ENDER_EYE:
					((Player) p).playSound(p.getLocation(), Sound.BLOCK_NETHER_GOLD_ORE_FALL, 2f, 2f);
					pr.arena().chngTm(pr, Team.NA);
>>>>>>>> 8ac1214 (Intellij Transfer):src/me/Romindous/CounterStrike/Listeners/InventLis.java
					break;
				default:
					break;
				}
			}
<<<<<<<< HEAD:CounterStrike/src/me/Romindous/CounterStrike/Listeners/InventLis.java
		} else if (e.getView().getTitle().equalsIgnoreCase("§3§lРазминировка Бомбы")) {
			e.setCancelled(true);
			if (it != null && it.getType() == Material.STRING) {
				final Pair<Shooter, Arena> pr = Shooter.getPlShtrArena(p.getName());
				final Arena ar = pr.getSecond();
				if (it.getItemMeta().getCustomModelData() == e.getClickedInventory().getItem(0).getItemMeta().getCustomModelData()) {
========
			break;
		case "Разминировка Бомбы":
			e.setCancelled(true);
			if (it != null && it.getType() == Material.STRING) {
				pr = Shooter.getPlShooter(p.getName(), true);
				final Arena ar = pr.arena();
				final int cmd = it.getItemMeta().getCustomModelData();
				if (cmd == e.getClickedInventory().getItem(0).getItemMeta().getCustomModelData()) {
>>>>>>>> 8ac1214 (Intellij Transfer):src/me/Romindous/CounterStrike/Listeners/InventLis.java
					final ItemStack cp = it.clone();
					final ItemMeta im = it.getItemMeta();
					im.setCustomModelData(10);
					((Player) p).playSound(p.getLocation(), Sound.BLOCK_TRIPWIRE_CLICK_ON, 1f, 2f);
					it.setItemMeta(im);
					if (!e.getClickedInventory().contains(cp)) {
<<<<<<<< HEAD:CounterStrike/src/me/Romindous/CounterStrike/Listeners/InventLis.java
						((Player) p).getWorld().playSound(p.getLocation(), Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE, 2f, 2f);
						((Player) p).getWorld().playSound(p.getLocation(), Sound.BLOCK_IRON_TRAPDOOR_CLOSE, 2f, 2f);
						p.closeInventory();
						if (ar != null && ar instanceof Defusal && pr.getFirst() != null) {
							((Defusal) ar).defuse();
							ar.chngMn(pr.getFirst(), 250);
							ApiOstrov.addStat((Player) p, Stat.CS_bomb);
						}
					} 
				} else {
					if (ar != null && ar instanceof Defusal) {
						((Defusal) ar).wrngWire();
					}
					((Player) p).playSound(p.getLocation(), Sound.ENTITY_PLAYER_HURT_SWEET_BERRY_BUSH, 1f, 2f);
				} 
			}
		} else if (e.getView().getTitle().equalsIgnoreCase("§3§lОбезвреживание Спавнера")) {
			e.setCancelled(true);
			if (it != null && it.getType() == Material.STRING) {
				final Pair<Shooter, Arena> pr = Shooter.getPlShtrArena(p.getName());
				if (it.getItemMeta().getCustomModelData() == e.getClickedInventory().getItem(0).getItemMeta().getCustomModelData()) {
========
						p.getWorld().playSound(p.getLocation(), Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE, 2f, 2f);
						p.getWorld().playSound(p.getLocation(), Sound.BLOCK_IRON_TRAPDOOR_CLOSE, 2f, 2f);
						p.closeInventory();
						if (ar != null && ar instanceof Defusal && pr != null) {
							((Defusal) ar).defuse();
							ar.chngMn(pr, 250);
							ApiOstrov.addStat((Player) p, Stat.CS_bomb);
						}
					} 
				} else if (cmd != 10) {
					if (ar != null && ar instanceof Defusal) {
						((Defusal) ar).wrngWire();
					}
					final ItemMeta im = it.getItemMeta();
					im.setCustomModelData(10);
					p.getWorld().playSound(p.getLocation(), Sound.BLOCK_GLASS_BREAK, 2f, 2f);
					((Player) p).playSound(p.getLocation(), Sound.BLOCK_TRIPWIRE_CLICK_ON, 1f, 2f);
					it.setItemMeta(im);
				}
			}
			break;
		case "Обезвреживание Спавнера":
			e.setCancelled(true);
			if (it != null && it.getType() == Material.STRING) {
				pr = Shooter.getPlShooter(p.getName(), true);
				final int cmd = it.getItemMeta().getCustomModelData();
				if (cmd == e.getClickedInventory().getItem(0).getItemMeta().getCustomModelData()) {
>>>>>>>> 8ac1214 (Intellij Transfer):src/me/Romindous/CounterStrike/Listeners/InventLis.java
					final ItemStack cp = it.clone();
					final ItemMeta im = it.getItemMeta();
					im.setCustomModelData(10);
					((Player) p).playSound(p.getLocation(), Sound.BLOCK_TRIPWIRE_CLICK_ON, 1f, 2f);
					it.setItemMeta(im);
					if (!e.getClickedInventory().contains(cp)) {
<<<<<<<< HEAD:CounterStrike/src/me/Romindous/CounterStrike/Listeners/InventLis.java
						((Player) p).getWorld().playSound(p.getLocation(), Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE, 2f, 2f);
						((Player) p).getWorld().playSound(p.getLocation(), Sound.BLOCK_IRON_TRAPDOOR_CLOSE, 2f, 2f);
						p.closeInventory();
						if (pr.getSecond() != null && pr.getSecond() instanceof Invasion && pr.getFirst() != null) {
							((Invasion) pr.getSecond()).rmvSpnr(pr.getFirst());
							ApiOstrov.addStat((Player) p, Stat.CS_spnrs);
						}
					} 
				} else {
					if (pr.getSecond() != null && pr.getSecond() instanceof Invasion && pr.getFirst() != null) {
						((Invasion) pr.getSecond()).wrngWire(pr.getFirst());
					}
					((Player) p).playSound(p.getLocation(), Sound.ENTITY_PLAYER_HURT_SWEET_BERRY_BUSH, 1f, 2f);
				} 
			}
		} else {
			e.setCancelled((p.getGameMode() != GameMode.CREATIVE));
			if (it != null && it.hasItemMeta() && it.getItemMeta().getDisplayName().equals("§сВыход")) {
				((Player) p).performCommand("cs leave");
			}
		} 
	}
   
	private void addSetItm(final PlayerInventory inv, final int slt, final ItemStack it) {
		final ItemStack s = inv.getItem(slt);
		if (s != null && s.getType() == it.getType()) {
			s.setAmount(s.getAmount() == 1 ? (s.getAmount() + 1) : s.getAmount());
		} else {
			inv.setItem(slt, it);
		} 
========
						p.getWorld().playSound(p.getLocation(), Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE, 2f, 2f);
						p.getWorld().playSound(p.getLocation(), Sound.BLOCK_IRON_TRAPDOOR_CLOSE, 2f, 2f);
						p.closeInventory();
						if (pr.arena() != null && pr.arena() instanceof Invasion && pr != null) {
							((Invasion) pr.arena()).rmvSpnr(pr);
						}
					}
				} else if (cmd != 10) {
					if (pr.arena() != null && pr.arena() instanceof Invasion && pr != null) {
						((Invasion) pr.arena()).wrngWire(pr);
					}
					final ItemMeta im = it.getItemMeta();
					im.setCustomModelData(10);
					p.getWorld().playSound(p.getLocation(), Sound.BLOCK_GLASS_BREAK, 2f, 2f);
					((Player) p).playSound(p.getLocation(), Sound.BLOCK_TRIPWIRE_CLICK_ON, 1f, 2f);
					it.setItemMeta(im);
				} 
			}
			break;
		default:
			e.setCancelled((p.getGameMode() != GameMode.CREATIVE));
			if (it != null && it.hasItemMeta() && TCUtils.toString(it.getItemMeta().displayName()).equals("§сВыход")) {
				((Player) p).performCommand("cs leave");
			}
			break;
		}
	}
	   
	@EventHandler
	public void onDrag(final InventoryDragEvent e) {
		e.setCancelled(true);
	}
	
	private boolean addSetItm(final PlayerInventory inv, final int slt, final ItemStack it) {
		final ItemStack s = inv.getItem(slt);
		if (s != null && s.getType() == it.getType()) {
			if (s.getAmount() == 1) {
				s.setAmount(2);
			} else {
				return false;
			}
		} else {
			inv.setItem(slt, it);
		}
		return true;
>>>>>>>> 8ac1214 (Intellij Transfer):src/me/Romindous/CounterStrike/Listeners/InventLis.java
	}
}