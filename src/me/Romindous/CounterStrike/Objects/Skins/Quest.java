package me.Romindous.CounterStrike.Objects.Skins;

import java.util.*;
import me.Romindous.CounterStrike.Enums.GunType;
import org.jetbrains.annotations.Nullable;

public enum Quest {

	АЗИМОВ(1, "§7Убей врага в §eдыму, §7в §eголову, §7при §eслепоте", GunType.AWP, "azimov"),
	ВОЙ(20, "§7Убей §e% §7противников в одной игре §5Классики", GunType.M4A1, "roar"),
	ДУША(200, "§7Сыграй §e% §7игр в любых режимах §5Контры", GunType.USP, "soul"),
	ДЮНА(200, "§7Убей §e% §7врагов за одну игру §5Вторжения", GunType.P90, "dune"),
	ЗЕМЛЯ(4, "§7Получи §e% §7(Убийства / Смерти) на §5Эстафете", GunType.SCAR, "earth"),
	КРОВЬ(0, "§7Убей врага через стену на §5Классике", GunType.SG13, "blood"),
	ЛАТУНЬ(4000, "§7Накопи §e% §7кредитов за игру §5Контры", GunType.DGL, "gold"),
	ЛГБТ(20, "§7Сломай §e% §7спавнеров за игру §5Вторжения", GunType.MP5, "rgb"),
	ОКЕАН(100, "§7Поставить §e% §7бомб на §5Классике", GunType.TP9, "ocean"),
	ПАНК(1, "§7Убей врага с §e% хп §7в игре §5Контры", GunType.NOVA, "punk"),
	ТОКСИК(32, "§7Убей §e% §7противников за игру §5Эстафеты", GunType.AK47, "toxic"),
	ГРУЗЧИК(200, "§7Выйграй §e% §7раундов на §5Классике", GunType.NGV, "loader"),
	;
	//убить Х без смертей
	
	public final int stat;
	public final String msg;
	public final GunType gun;
	public final String model;

    static final Map<GunType, Quest[]> models;
    static final Quest[] eq = {};
    static {
        models = new EnumMap<>(GunType.class);
        final Map<GunType, List<Quest>> gqs = new EnumMap<>(GunType.class);
        for (final Quest q : me.Romindous.CounterStrike.Objects.Skins.Quest.values()) {
            final List<Quest> ql = gqs.get(q);
            if (ql != null) {ql.add(q); continue;}
            gqs.put(q.gun, new ArrayList<>(Arrays.asList(q)));
        }
        for (final Map.Entry<GunType, List<Quest>> en : gqs.entrySet()) {
            models.put(en.getKey(), en.getValue().toArray(eq));
        }
    }
	
	Quest(final int stat, final String msg, final GunType gun, final String mdl) {
		this.stat = stat;
		this.msg = msg.replace("%", String.valueOf(stat));
		this.gun = gun;
		this.model = mdl;
	}

    public static Quest[] get(final GunType gt) {
        return models.getOrDefault(gt, eq);
    }

    public static @Nullable Quest get(final GunType gt, final String mdl) {
        final Quest[] qs = get(gt);
        for (final Quest q : qs)
            if (q.model.equals(mdl)) return q;
        return null;
    }
	
}
