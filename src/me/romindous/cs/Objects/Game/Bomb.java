package me.romindous.cs.Objects.Game;

import java.time.Duration;
import java.util.*;
import com.destroystokyo.paper.ParticleBuilder;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.math.Position;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.input.SingleOptionDialogInput;
import io.papermc.paper.registry.data.dialog.input.TextDialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import me.romindous.cs.Game.Arena;
import me.romindous.cs.Game.Defusal;
import me.romindous.cs.Main;
import me.romindous.cs.Objects.Defusable;
import me.romindous.cs.Objects.Shooter;
import me.romindous.cs.Utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Display.Billboard;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemType;
import org.bukkit.util.Transformation;
import org.joml.Vector3f;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Ostrov;
import ru.komiss77.enums.Stat;
import ru.komiss77.modules.items.ItemBuilder;
import ru.komiss77.modules.world.BVec;
import ru.komiss77.utils.BlockUtil;
import ru.komiss77.utils.ItemUtil;
import ru.komiss77.utils.NumUtil;
import ru.komiss77.utils.TCUtil;
import ru.komiss77.version.Nms;

public class Bomb extends Defusable {

	public static final int WIRE_TIME = 5;

    public final TextDisplay title;
	private final Defusal ar;
	
	private static final Component bnm = TCUtil.form("§l§кБiмба Поставлена!")
		.appendNewline().append(TCUtil.form("§7Обезвредьте §eкусачками §7или §3спец. набором§7!"));
	
	public Bomb(final Block b, final Defusal df) {
		super(b); ar = df;
		title = ar.w.spawn(center(ar.w).add(0d, 1d, 0d), TextDisplay.class);
		title.setPersistent(true);
		title.setBillboard(Billboard.VERTICAL);
		title.text(bnm);
		title.setShadowed(true);
		title.setSeeThrough(true);
		title.setViewRange(100f);
		final Transformation atr = title.getTransformation();
		title.setTransformation(new Transformation(atr.getTranslation(), 
			atr.getLeftRotation(), new Vector3f(1.6f, 1.6f, 1.6f), atr.getRightRotation()));
		defusing = null;
	}

	public Defusal arena() {
		return ar;
	}

	public Shooter defusing() {
		return defusing;
	}

	public void defusing(final Shooter sh) {
		defusing = sh;
	}
	   
	public void expld() {
		title.remove();
		final Block b = block(ar.w);
		b.setBlockData(BlockUtil.air,false);
		final int X = b.getX();
		final int Y = b.getY();
		final int Z = b.getZ();
		final HashSet<BVec> cls = new HashSet<>();
		b.getWorld().spawnParticle(Particle.EXPLOSION, b.getLocation(), 20, 5d, 5d, 5d);
		b.getWorld().playSound(b.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 5f, 0.8f);

		for (int x = -5; x < 6; x++) {
			for (int y = -5; y < 6; y++) {
				for (int z = -5; z < 6; z++) {
					final int bnd = x*x + y*y + z*z;
					if (bnd > 0 && Nms.fastType(ar.w, X + x, Y + y, Z + z).isAir() && Nms.fastType(ar.w, X + x, Y + y - 1, Z + z).isOccluding() && Main.srnd.nextInt(bnd) < 6) {
						for (final Player p : b.getWorld().getPlayers()) {
							p.sendBlockChange(new Location(ar.w, X + x, Y + y, Z + z), FIRE);
							cls.add(BVec.of(X + x, Y + y, Z + z));
						} 
					} else if (Nms.fastType(ar.w, X + x, Y + y, Z + z).isOccluding() && Main.srnd.nextInt(bnd) < 10) {
						for (final Player p : b.getWorld().getPlayers()) {
							p.sendBlockChange(new Location(ar.w, X + x, Y + y, Z + z), COAL);
							cls.add(BVec.of(X + x, Y + y, Z + z));
						}
					}
				} 
			} 
		}
		
		for (final Shooter sh : ar.shtrs.keySet()) {
			final LivingEntity le = sh.getEntity();
			if (sh.isDead() || le == null) continue;
			final Location loc = le.getLocation();
			final int dSq = NumUtil.square(loc.getBlockX() - X)
				+ NumUtil.square(loc.getBlockZ() - Z);
			final int idm = NumUtil.sqrt(dSq) * 200 / (dSq + 1); if (idm == 0) continue;
			final double d = idm * (ItemUtil.isBlank(sh.item(EquipmentSlot.CHEST), false) ? 1d : 0.4d);
			if (le.getHealth() - d <= 0) {
				ar.addDth(sh);
				sh.drop(le.getLocation());
				if (sh instanceof PlShooter) {
					final Player p = sh.getPlayer();
					p.closeInventory();
					p.setGameMode(GameMode.SPECTATOR);
				} else {
					((BtShooter) sh).own().hide(le);
				}
				for (final Player p : ar.w.getPlayers()) {
					p.sendMessage("§c\u926e\u9299 " + ar.getShtrNm(sh));
				}
			} else {
				le.setHealth(le.getHealth() - d);
				le.playHurtAnimation(le.getBodyYaw());
			}
		}
		
		Ostrov.async(() -> {
			final Map<Position, BlockData> bls = new HashMap<>();
			for (final BVec bl : cls) {
				bls.put(bl.center(ar.w), ar.w.getBlockData(bl.x, bl.y, bl.z));
			}
			
			for (final Player p : ar.w.getPlayers()) {
				p.sendMultiBlockChange(bls);
			}
		}, 200);
	}

    public void display(final Player pl, final boolean kit) { //extends EntityEvent
        pl.getWorld().playSound(center(pl.getWorld()),
            Sound.BLOCK_BEEHIVE_SHEAR, 2f, 0.5f);
        final Dialog dg = kit ? Dialog.create(builder -> builder.empty()
            .base(DialogBase.builder(TCUtil.form("<gradient:light_purple:aqua><bold>Меню Разминировки")).body(List.of(DialogBody.item(new ItemBuilder(ItemType.SHEARS).glint(true).build(),
                        DialogBody.plainMessage(TCUtil.form("<mint>=> <beige>Для Разминировки")), true, false, 16, 16),
                    DialogBody.plainMessage(TCUtil.form("<gold>Поставь правильные настройки и разрежь провода!"))))
                .inputs(List.of(
                    DialogInput.numberRange(VOLTS, 250, TCUtil.form("<white>Настрой кусачки на [<aqua>"
                        + volts + " V<white>]"), "%s, сейчас: %s V", MIN_VOLTS, MAX_VOLTS, (float) ch_volts, 1f),
                    DialogInput.bool(CHECK, TCUtil.form("\n<beige>> " + (check ? "<green>Нужна Галочка" : "<red>Не Нужна Галочка") + "\n")).initial(ch_check).build(),
                    DialogInput.text("text", 1, TCUtil.form("<beige>Разрежь <mithril>цвет проводов<beige>, которых <gold>" + (check ? "больше" : "меньше") + " <beige>всего!"),
                        true, "", 1, TextDialogInput.MultilineOptions.create(1, 1)),
                    DialogInput.singleOption("wires", 160, List.of(SingleOptionDialogInput.OptionEntry.create("wires", TCUtil.form(genWires(color, check)), false)
                    ), TCUtil.form("<beige>Провода"), false)
                )).build())
            .type(DialogType.multiAction(List.of(
                ActionButton.builder(TCUtil.form("<red>⎨ <u>Красный</u> ⎬")).tooltip(TCUtil.form("<beige>Клик - Разрезать")).width(80).action(genAction(pl, WColor.RED, true)).build(),
                ActionButton.builder(TCUtil.form("<yellow>⎨ <u>Желтый</u> ⎬")).tooltip(TCUtil.form("<beige>Клик - Разрезать")).width(80).action(genAction(pl, WColor.YELLOW, true)).build(),
                ActionButton.builder(TCUtil.form("<green>⎨ <u>Зеленый</u> ⎬")).tooltip(TCUtil.form("<beige>Клик - Разрезать")).width(80).action(genAction(pl, WColor.GREEN, true)).build(),
                ActionButton.builder(TCUtil.form("<blue>⎨ <u>Синий</u> ⎬")).tooltip(TCUtil.form("<beige>Клик - Разрезать")).width(80).action(genAction(pl, WColor.BLUE, true)).build()
            ), ActionButton.builder(TCUtil.form("Выход")).width(1).action(onClose()).build(), 4))
        ) : Dialog.create(builder -> builder.empty()
            .base(DialogBase.builder(TCUtil.form("<gradient:light_purple:aqua><bold>Меню Разминировки")).body(List.of(DialogBody.item(new ItemBuilder(ItemType.SHEARS).glint(true).build(),
                        DialogBody.plainMessage(TCUtil.form("<mint>=> <beige>Для Разминировки")), true, false, 16, 16),
                    DialogBody.plainMessage(TCUtil.form("<gold>Поставь правильные настройки и разрежь провода!\n\n<beige><bold>Серийный Код Бомбы:</bold>\n<pink><u>" + code)),
                    DialogBody.plainMessage(TCUtil.form("\n<beige>Выбери Параметр:"))))
                .inputs(List.of(
                    DialogInput.singleOption(CODE, 200, Arrays.stream(OPTIONS).map(o -> o.entry(ch_cond)).toList(), TCUtil.form("<sky>Код Бомбы"), false),
                    DialogInput.numberRange(VOLTS, 250, TCUtil.form("<white>Настрой кусачки на [<aqua>"
                        + volts + " V<white>]"), "%s, сейчас: %s V", MIN_VOLTS, MAX_VOLTS, (float) ch_volts, 1f),
                    DialogInput.bool(CHECK, TCUtil.form("\n<beige>> " + (check ? "<green>Нужна Галочка" : "<red>Не Нужна Галочка") + "\n")).initial(ch_check).build(),
                    DialogInput.text("text", 1, TCUtil.form("<beige>Разрежь <mithril>цвет проводов<beige>, которых <gold>" + (check ? "больше" : "меньше") + " <beige>всего!"),
                        true, "", 1, TextDialogInput.MultilineOptions.create(1, 1)),
                    DialogInput.singleOption("wires", 160, List.of(SingleOptionDialogInput.OptionEntry.create("wires", TCUtil.form(genWires(color, check)), false)
                    ), TCUtil.form("<beige>Провода"), false)
                )).build())
            .type(DialogType.multiAction(List.of(
                ActionButton.builder(TCUtil.form("<red>⎨ <u>Красный</u> ⎬")).tooltip(TCUtil.form("<beige>Клик - Разрезать")).width(80).action(genAction(pl, WColor.RED, false)).build(),
                ActionButton.builder(TCUtil.form("<yellow>⎨ <u>Желтый</u> ⎬")).tooltip(TCUtil.form("<beige>Клик - Разрезать")).width(80).action(genAction(pl, WColor.YELLOW, false)).build(),
                ActionButton.builder(TCUtil.form("<green>⎨ <u>Зеленый</u> ⎬")).tooltip(TCUtil.form("<beige>Клик - Разрезать")).width(80).action(genAction(pl, WColor.GREEN, false)).build(),
                ActionButton.builder(TCUtil.form("<blue>⎨ <u>Синий</u> ⎬")).tooltip(TCUtil.form("<beige>Клик - Разрезать")).width(80).action(genAction(pl, WColor.BLUE, false)).build()
            ), ActionButton.builder(TCUtil.form("Выход")).width(1).action(onClose()).build(), 4))
        );
        pl.showDialog(dg);
    }

    protected DialogAction.CustomClickAction genAction(final Player pl, final WColor clr, final boolean kit) {
        return DialogAction.customClick((res, au) -> {
            final String code = res.getText(CODE);
            if (code != null) ch_cond = Condition.parse(code);
            final Boolean chb = res.getBoolean(CHECK);
            if (chb != null) ch_check = chb;
            final Float fvl = res.getFloat(VOLTS);
            if (fvl != null) ch_volts = fvl.intValue();
            final Location clc = center(ar.w);
            if (cond != ch_cond && !kit) {
                Utils.sendTtlSbTtl(pl, Arena.Team.Ts.clr + "-" + Bomb.WIRE_TIME + " сек",
                    "<beige>Выбран не тот <mithril>параметр<beige>!", 60);
                ar.w.playSound(clc, Sound.BLOCK_GLASS_BREAK, 2f, 2f);
                ar.wrngWire();
                return;
            }
            if (volts != ch_volts) {
                Utils.sendTtlSbTtl(pl, Arena.Team.Ts.clr + "-" + Bomb.WIRE_TIME + " сек",
                    "<beige>Кусачки <mithril>настроены <beige>неправильно!", 60);
                ar.w.playSound(clc, Sound.BLOCK_GLASS_BREAK, 2f, 2f);
                ar.wrngWire();
                return;
            }
            if (check != ch_check) {
                Utils.sendTtlSbTtl(pl, Arena.Team.Ts.clr + "-" + Bomb.WIRE_TIME + " сек",
                    "<beige>Условия <mithril>галочки<beige> не соблюдены!", 60);
                ar.w.playSound(clc, Sound.BLOCK_GLASS_BREAK, 2f, 2f);
                ar.wrngWire();
                return;
            }
            if (color != clr) {
                Utils.sendTtlSbTtl(pl, Arena.Team.Ts.clr + "-" + Bomb.WIRE_TIME + " сек",
                    "<beige>Разрезан не тот <mithril>цвет <beige>проводов!", 60);
                ar.w.playSound(clc, Sound.BLOCK_GLASS_BREAK, 2f, 2f);
                ar.wrngWire();
                return;
            }
            ar.w.playSound(clc, Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE, 2f, 2f);
            ar.w.playSound(clc, Sound.BLOCK_IRON_TRAPDOOR_CLOSE, 2f, 2f);
            new ParticleBuilder(Particle.SCULK_SOUL).count(20).location(clc).extra(0.1f)
                .offset(0.2d, 0.2d, 0.2d).receivers(100).spawn();
            ApiOstrov.addStat(pl, Stat.CS_bomb);
            ar.chngMn(defusing, Shooter.bmbRwd);
            ar.defuse();
        }, ClickCallback.Options.builder().uses(1).lifetime(Duration.ofDays(1)).build());
    }
}
