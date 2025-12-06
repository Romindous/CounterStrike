package me.romindous.cs.Objects.Game;

import java.lang.ref.WeakReference;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import com.destroystokyo.paper.ParticleBuilder;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.input.SingleOptionDialogInput;
import io.papermc.paper.registry.data.dialog.input.TextDialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import me.romindous.cs.Game.Arena;
import me.romindous.cs.Game.Invasion;
import me.romindous.cs.Main;
import me.romindous.cs.Objects.Defusable;
import me.romindous.cs.Objects.Mobs.GoalGoToSite;
import me.romindous.cs.Objects.Shooter;
import me.romindous.cs.Utils.Utils;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.BlockType;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.*;
import org.bukkit.entity.Display.Billboard;
import org.bukkit.inventory.ItemType;
import ru.komiss77.modules.items.ItemBuilder;
import ru.komiss77.modules.world.BVec;
import ru.komiss77.utils.TCUtil;
import ru.komiss77.version.Nms;

public class Mobber extends Defusable {
	
	public static final BlockData SPAWNER = BlockType
		.SPAWNER.createBlockData();
	
	public static final BlockData DEFUSED = BlockType
		.CRYING_OBSIDIAN.createBlockData();

	public final BlockDisplay ind;
	private final Invasion ar;
	public MobType mt;
  
	public Mobber(final BVec loc, final Invasion ar) {
        super(loc); this.ar = ar;
		ind = ar.w.spawn(new Location(ar.w, loc.x, loc.y, loc.z), BlockDisplay.class);
		ind.setGravity(false);
		ind.setViewRange(100f);
		ind.setBillboard(Billboard.FIXED);
		setSpwn();
		mt = MobType.WEAK;
		
		final Block b = block(ar.w);
		b.setBlockData(SPAWNER, false);
		
		final CreatureSpawner cs = (CreatureSpawner) b.getState();
		cs.setSpawnedType(mt.type);
		cs.setSpawnCount(0);
		cs.update();

		ar.mbbrs.put(this.thin(), this);
	}

	public Invasion arena() {
		return ar;
	}

	public Shooter defusing() {
		return defusing;
	}

	public void defusing(final Shooter sh) {
		defusing = sh;
	}

    public void wrngWire() {
        defusing = null;
        spwnMb();
    }

	public void spwnMb() {
        final Location loc = center(ar.w);
		final Mob mb = (Mob) ar.w.spawnEntity(Main.getNrLoc(loc), mt.type, false);
		mb.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(mt.spd);
		mb.getAttribute(Attribute.MAX_HEALTH).setBaseValue(mt.hp);
		mb.setHealth(mt.hp);
        //Bukkit.getMobGoals().removeAllGoals(mb);
        Bukkit.getMobGoals().removeAllGoals(mb);
        Bukkit.getMobGoals().addGoal(mb, 0, new GoalGoToSite(mb, ar));
		ar.TMbs.put(mb.getEntityId(), new WeakReference<>(mb));
		ar.w.spawnParticle(Particle.SOUL, loc, 40, 0.6D, 0.6D, 0.6D, 0.0D, null, false);
		
		if (mb instanceof PiglinBrute) {
			((PiglinBrute) mb).setImmuneToZombification(true);
		}
	}

	public BlockType getType() {
		return ind.getBlock().getMaterial().asBlockType();
	}

	public boolean isAlive() {
		return ind.isGlowing();
	}

	public void setSpwn() {
		ind.setBlock(SPAWNER);
		Nms.colorGlow(ind, NamedTextColor.DARK_RED, false);
	}

	public void setDef() {
		ind.setBlock(DEFUSED);
		ind.setGlowing(false);
		defusing = null;
	}

	public void set(final MobType mt) {
		this.mt = mt;
	}

	public enum MobType {
		WEAK(EntityType.ZOMBIE_VILLAGER, 16d, 0.50d),
		NORM(EntityType.STRAY, 18d, 0.54d),
		DANG(EntityType.VINDICATOR, 12d, 0.56d),
		TERM(EntityType.WITHER_SKELETON, 24d, 0.58d);

		public final EntityType type;
		public final double hp;
		public final double spd;
		public final int pow;

		MobType(final EntityType type, final double hp, final double spd) {
			this.pow = ordinal();
            this.type = type;
			this.hp = hp;
			this.spd = spd;
        }

		public static MobType get(final EntityType type) {
			return switch (type) {
				case STRAY -> NORM;
				case VINDICATOR -> DANG;
				case WITHER_SKELETON -> TERM;
				default -> WEAK;
			};
		}
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
//                    DialogInput.bool(CHECK, TCUtil.form("\n<beige>> " + (check ? "<green>Нужна Галочка" : "<red>Не Нужна Галочка") + "\n")).initial(ch_check).build(),
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
//                    DialogInput.bool(CHECK, TCUtil.form("\n<beige>> " + (check ? "<green>Нужна Галочка" : "<red>Не Нужна Галочка") + "\n")).initial(ch_check).build(),
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
                Utils.sendTtlSbTtl(pl, Arena.Team.Ts.clr + "+ Моб",
                    "<beige>Выбран не тот <mithril>параметр<beige>!", 60);
                ar.w.playSound(clc, Sound.BLOCK_GLASS_BREAK, 2f, 2f);
                wrngWire();
                return;
            }
            if (volts != ch_volts) {
                Utils.sendTtlSbTtl(pl, Arena.Team.Ts.clr + "+ Моб",
                    "<beige>Кусачки <mithril>настроены <beige>неправильно!", 60);
                ar.w.playSound(clc, Sound.BLOCK_GLASS_BREAK, 2f, 2f);
                wrngWire();
                return;
            }
            /*if (check != ch_check) {
                Utils.sendTtlSbTtl(pl, Arena.Team.Ts.clr + "+ Моб",
                    "<beige>Условия <mithril>галочки<beige> не соблюдены!", 40);
                wrngWire();
                return;
            }*/
            if (color != clr) {
                Utils.sendTtlSbTtl(pl, Arena.Team.Ts.clr + "+ Моб",
                    "<beige>Разрезан не тот <mithril>цвет <beige>проводов!", 60);
                ar.w.playSound(clc, Sound.BLOCK_GLASS_BREAK, 2f, 2f);
                wrngWire();
                return;
            }
            ar.w.playSound(clc, Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE, 2f, 2f);
            ar.w.playSound(clc, Sound.BLOCK_IRON_TRAPDOOR_CLOSE, 2f, 2f);
            new ParticleBuilder(Particle.SCULK_SOUL).count(40).location(clc).extra(0.1f)
                .offset(0.4d, 0.4d, 0.4d).receivers(100).spawn();
            ar.addSpDfs(defusing);
            ar.chngMn(defusing, Shooter.spwnrRwd);
            ar.dieSpnr(this);
        }, ClickCallback.Options.builder().uses(1).lifetime(Duration.ofDays(1)).build());
    }
}
