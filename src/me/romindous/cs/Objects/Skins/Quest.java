package me.romindous.cs.Objects.Skins;

import java.util.*;
import me.romindous.cs.Enums.GunType;
import org.jetbrains.annotations.Nullable;

public enum Quest {

	CYBER(1, "§7Убей врага в §eдыму, §7в §eголову, §7при §eслепоте", GunType.AWP, "cyber", "Выкованный во Льдах"),
	PYROMANCY(20, "§7Убей §e% §7противников в одной игре §5Классики", GunType.M4A1, "pyromancy", "Душа Пироманта"),
	PHANTOM(200, "§7Сыграй §e% §7игр в любых режимах §5Контры", GunType.USP, "phantom", "Фантомная Боль"),
	ROOSTER(200, "§7Убей §e% §7врагов за одну игру §5Вторжения", GunType.P90, "rooster", "Пернатое Создание"),
	CRYSTAL(4, "§7Получи §e% §7(Убийства / Смерти) на §5Эстафете", GunType.SCAR, "crystal", "Аметистовый Звон"),
	SCULK(0, "§7Убей врага через стену на §5Классике", GunType.SG13, "sculk", "Хранитель Душ"),
    ICICLE(4000, "§7Накопи §e% §7кредитов за игру §5Контры", GunType.DGL, "icicle", "Выкованный во Льдах"),
	HAPPINESS(20, "§7Сломай §e% §7спавнеров за игру §5Вторжения", GunType.MP5, "happiness", "Ключ к Счастью"),
    SHARD(100, "§7Поставить §e% §7бомб на §5Классике", GunType.TP9, "ocean", "Пляжный День"),
	ACID(1, "§7Убей врага с §e% хп §7в игре §5Контры", GunType.NOVA, "acid", "Кислотный Дождь"),
	NETHERITE(20, "§7Убей §e% §7противников за игру §5Эстафеты", GunType.AK47, "netherite", "Посол Преисподней"),
	LOADER(200, "§7Выйграй §e% §7раундов на §5Классике", GunType.NGV, "loader", "Стройплощадка"),
	;
	//убить Х без смертей
	
	public final int stat;
	public final String msg;
	public final GunType gun;
	public final String model;
    public final String name;

    static final Map<GunType, Quest[]> models;
    static final Quest[] eq = {};
    static {
        models = new EnumMap<>(GunType.class);
        final Map<GunType, List<Quest>> gqs = new EnumMap<>(GunType.class);
        for (final Quest q : Quest.values()) {
            final List<Quest> ql = gqs.get(q);
            if (ql != null) {ql.add(q); continue;}
            gqs.put(q.gun, new ArrayList<>(Arrays.asList(q)));
        }
        for (final Map.Entry<GunType, List<Quest>> en : gqs.entrySet()) {
            models.put(en.getKey(), en.getValue().toArray(eq));
        }
    }
	
	Quest(final int stat, final String msg, final GunType gun, final String mdl, final String nm) {
		this.stat = stat;
		this.msg = msg.replace("%", String.valueOf(stat));
		this.gun = gun;
		this.model = mdl;
        this.name = nm;
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
