package me.Romindous.CounterStrike.Enums;

public enum GameType {
	DEFUSAL("Классика"),
	GUNGAME("Эстафета"),
	INVASION("Вторжение");

	public final String name;

	GameType(final String name) {
		this.name = name;
	}
}
