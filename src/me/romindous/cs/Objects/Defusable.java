package me.romindous.cs.Objects;

import javax.annotation.Nullable;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.input.SingleOptionDialogInput;
import me.romindous.cs.Game.Arena;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.event.ClickCallback;
import org.bukkit.block.Block;
import org.bukkit.block.BlockType;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import ru.komiss77.Ostrov;
import ru.komiss77.modules.world.BVec;
import ru.komiss77.utils.ClassUtil;
import ru.komiss77.utils.StringUtil;
import ru.komiss77.utils.TCUtil;

public abstract class Defusable extends BVec {

    public static final String OFF_CLR = "off", PLANT = "plant", KIT_MDL = PLANT + "/defuse",
        PLIERS_MDL = PLANT + "/pliers", BOMB_MDL = PLANT + "/bomb", WIRE = "wire", DISP = "disp";
    public static final BlockData FIRE = BlockType.FIRE.createBlockData();
    public static final BlockData COAL = BlockType.COAL_BLOCK.createBlockData();
//    public static final String[] COLORS = {"blue", "green", "red", "yellow"};

    public static Key disp(final String clr) {
        return Key.key(PLANT + "/" + DISP + "/" + clr);
    }

    public static Key wire(final String clr) {
        return Key.key(PLANT + "/" + WIRE + "/" + clr);
    }

    protected final WColor color;
    protected final Condition cond;
    protected final boolean check;
    protected final int volts;
    protected final String code;

    protected Condition ch_cond;
    protected boolean ch_check;
    protected int ch_volts;
    protected Shooter defusing;

    public Defusable(final Block b) {
        this(BVec.of(b));
    }

    public Defusable(final BVec loc) {
        super(loc.x, loc.y, loc.z);
        ClassUtil.shuffle(OPTIONS);
        cond = ClassUtil.rndElmt(OPTIONS);
        ch_cond = ClassUtil.rndElmt(OPTIONS);
        volts = Ostrov.random.nextInt(MAX_VOLTS);
        ch_volts = MAX_VOLTS >> 1;
        check = Ostrov.random.nextBoolean();
        ch_check = Ostrov.random.nextBoolean();
        color = ClassUtil.rndElmt(COLORS);
        code = genCode(ClassUtil.rndElmt(CODES), cond);
    }

    public void mix() {
        ch_cond = ClassUtil.rndElmt(OPTIONS);
        ch_volts = MAX_VOLTS >> 1;
        ch_check = Ostrov.random.nextBoolean();
    }

    public abstract Arena arena();

    public abstract @Nullable Shooter defusing();

    public abstract void defusing(final @Nullable Shooter sh);

    public abstract void display(final Player pl, final boolean kit);

    protected abstract DialogAction.CustomClickAction genAction(final Player pl, final WColor clr, boolean kit);

    protected enum Condition {
        NUM_SUM("<mithril>Сумма <gold>цифр <mithril>в <pink>коде <mithril>более <gold>20ти"),
        //        END_UPPER("<mithril>Код кончается на <gold>большую <mithril>букву"),
        HAS_UPPER("<mithril>В <pink>коде <mithril>есть <gold>'C' <mithril>и <gold>'S'"),
        //        HAS_LOWER("<mithril>В коде есть <gold>'a'<mithril>, <gold>'w'<mithril>, и <gold>'p'"),
        IS_LARGE("<pink>Код <mithril>имеет <gold>более 25 <mithril>символов"),
        MORE_LOWER("<mithril>Более <gold>половины <mithril>букв <pink>кода <gold>малые");

        private static final Map<String, Condition> names;

        private final SingleOptionDialogInput.OptionEntry entry;

        Condition(final String text) {
            entry = SingleOptionDialogInput.OptionEntry
                .create(name().toLowerCase(Locale.ROOT), TCUtil.form(text), false);
        }

        static {
            final Map<String, Condition> sm = new ConcurrentHashMap<>();
            for (final Condition cnd : Condition.values()) {
                sm.put(cnd.name().toLowerCase(Locale.ROOT), cnd);
            }
            names = Collections.unmodifiableMap(sm);
        }

        public SingleOptionDialogInput.OptionEntry entry(final Condition chosen) {
            return this == chosen ? SingleOptionDialogInput.OptionEntry
                .create(name().toLowerCase(Locale.ROOT), entry.display(), true) : entry;
        }

        public static Condition parse(final String name) {
            return names.get(name);
        }
    }

    protected enum WColor {RED, YELLOW, GREEN, BLUE}

    protected static final Condition[] OPTIONS = {Condition.NUM_SUM/*, Condition.END_UPPER*/,
        Condition.HAS_UPPER/*, Condition.HAS_LOWER*/, Condition.IS_LARGE, Condition.MORE_LOWER};

    private static final char[] WIRES = {'⎱', '⎛', '⎜', '⎝', '⎨', '⎫', '⎬', '⎭', '⎰', '⎱'};
    private static final String[] CODES = {"v9FcO1nV4YePiM2ud3Rl", "1Fo5PlecJvqK7pU3s2En", "Fl1Trn8PoYdMf5oI2Vu3", "G4mQeD5oFx7laP2jWtM1"};

    protected static final int MAX_VOLTS = 500;
    private static final WColor[] COLORS = WColor.values();

    protected static final String CODE = "code";
    protected static final String VOLTS = "volts";
    protected static final String CHECK = "check";

    private static String genCode(final String code, final Condition cnd) {
        return switch (cnd) {
            case NUM_SUM -> new StringBuilder(code).insert(Ostrov.random.nextInt(code.length()),
                StringUtil.rndChar(StringUtil.NUMBERS.substring(StringUtil.NUMBERS.length() >> 1))).toString();
            case HAS_UPPER -> new StringBuilder(code).insert(Ostrov.random.nextInt(code.length()), 'C')
                .insert(Ostrov.random.nextInt(code.length()), 'S').toString();
            /*case END_UPPER -> code + StringUtil.rndChar(StringUtil.UPPERS);
            case HAS_LOWER -> new StringBuilder(code).insert(Ostrov.random.nextInt(code.length()), 'a')
                .insert(Ostrov.random.nextInt(code.length()), 'w').insert(Ostrov.random.nextInt(code.length()), 'p').toString();*/
            case IS_LARGE -> new StringBuilder(code).insert(Ostrov.random.nextInt(code.length()), 'U')
                .insert(Ostrov.random.nextInt(code.length()), 'R').insert(Ostrov.random.nextInt(code.length()), 'L')
                .insert(Ostrov.random.nextInt(code.length()), 't').insert(Ostrov.random.nextInt(code.length()), 'a')
                .insert(Ostrov.random.nextInt(code.length()), 'r').insert(Ostrov.random.nextInt(code.length()), 'o').toString();
            case MORE_LOWER -> new StringBuilder(code).insert(Ostrov.random.nextInt(code.length()), 'k')
                .insert(Ostrov.random.nextInt(code.length()), 'r').insert(Ostrov.random.nextInt(code.length()), 'l').toString();
        };
    }

    private static final int MAX_WIRES  = 9;
    protected static String genWires(final WColor clr, final boolean add) {
        final int[] counts = new int[COLORS.length];
        for (int i = 0; i != counts.length; i++) {
            counts[i] = MAX_WIRES + Ostrov.random.nextInt(2);
        }
        if (add) counts[clr.ordinal()]+=4;
        else counts[clr.ordinal()]-=4;
        final List<String> wires = new ArrayList<>();
        for (int i = 0; i != counts.length; i++) {
            for (int j = 0; j != counts[i]; j++) {
                wires.add(switch (COLORS[i]) {
                    case RED -> "<red>";
                    case YELLOW -> "<yellow>";
                    case GREEN -> "<green>";
                    case BLUE -> "<blue>";
                } + WIRES[Ostrov.random.nextInt(WIRES.length)]);
            }
        }
        return String.join("", ClassUtil.shuffle(wires.toArray(i -> new String[i])));
    }

    protected DialogAction.CustomClickAction onClose() {
        return DialogAction.customClick((res, au) -> {
            final String code = res.getText(CODE);
            if (code != null) ch_cond = Condition.parse(code);
            final Boolean chb = res.getBoolean(CHECK);
            if (chb != null) ch_check = chb;
            final Float fvl = res.getFloat(VOLTS);
            if (fvl != null) ch_volts = fvl.intValue();
            defusing = null;
        }, ClickCallback.Options.builder().uses(1).lifetime(Duration.ofDays(1)).build());
    }
}
