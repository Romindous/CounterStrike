package me.Romindous.CounterStrike.Objects.Skins;

import me.Romindous.CounterStrike.Enums.GunType;
import org.jetbrains.annotations.Nullable;

public enum Quest {

	АЗИМОВ(1, "§7Убей врага в §eдыму, §7в §eголову, §7при §eслепоте", GunType.AWP, 1),
	ВОЙ(20, "§7Убей §e% §7противников в одной игре §5Классики", GunType.M4, 1),
	ДУША(200, "§7Сыграй §e% §7игр в любых режимах §5Контры", GunType.USP, 1),
	ДЮНА(200, "§7Убей §e% §7врагов за одну игру §5Вторжения", GunType.P90, 1),
	ЗЕМЛЯ(4, "§7Получи §e% §7(Убийства / Смерти) на §5Эстафете", GunType.SCAR, 1),
	КРОВЬ(0, "§7Убей врага через стену на §5Классике", GunType.SG13, 1),
	ЛАТУНЬ(4000, "§7Накопи §e% §7кредитов за игру §5Контры", GunType.DGL, 1),
	ЛГБТ(20, "§7Сломай §e% §7спавнеров за игру §5Вторжения", GunType.MP5, 1),
	ОКЕАН(100, "§7Поставить §e% §7бомб на §5Классике", GunType.TP7, 1),
	ПАНК(1, "§7Убей врага с §e% хп §7в игре §5Контры", GunType.NOVA, 1),
	ТОКСИК(32, "§7Убей §e% §7противников за игру §5Эстафеты", GunType.AK47, 1),
	ГРУЗЧИК(200, "§7Выйграй §e% §7раундов на §5Классике", GunType.NGV, 1),
	;
	//убить Х без смертей
	
	public final int stat;
	public final String msg;
	public final GunType gun;
	public final int cmd;
	
	Quest(final int stat, final String msg, final GunType gun, final int cmd) {
		this.stat = stat;
		this.msg = msg.replace("%", String.valueOf(stat));
		this.gun = gun;
		this.cmd = GunType.defCMD + cmd;
	}
	
	public static @Nullable Quest getQuest(final GunType gt, final int cmd) {
		//Bukkit.broadcast(Component.text("gt-" + gt.toString() + ", cmd-" + cmd));
        return switch (gt) {
            case AK47 -> switch (cmd) {
                case 11 -> ТОКСИК;
                default -> null;
            };
            case AWP -> switch (cmd) {
                case 11 -> АЗИМОВ;
                default -> null;
            };
            case DGL -> switch (cmd) {
                case 11 -> ЛАТУНЬ;
                default -> null;
            };
            case M4 -> switch (cmd) {
                case 11 -> ВОЙ;
                default -> null;
            };
            case MP5 -> switch (cmd) {
                case 11 -> ЛГБТ;
                default -> null;
            };
            case NGV -> switch (cmd) {
                case 11 -> ГРУЗЧИК;
                default -> null;
            };
            case NOVA -> switch (cmd) {
                case 11 -> ПАНК;
                default -> null;
            };
            case P90 -> switch (cmd) {
                case 11 -> ДЮНА;
                default -> null;
            };
            case SCAR -> switch (cmd) {
                case 11 -> ЗЕМЛЯ;
                default -> null;
            };
            case SG13 -> switch (cmd) {
                case 11 -> КРОВЬ;
                default -> null;
            };
            case TP7 -> switch (cmd) {
                case 11 -> ОКЕАН;
                default -> null;
            };
            case USP -> switch (cmd) {
                case 11 -> ДУША;
                default -> null;
            };
        };
	}
	
}
