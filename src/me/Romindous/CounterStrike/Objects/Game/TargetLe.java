package me.Romindous.CounterStrike.Objects.Game;

import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

public record TargetLe(LivingEntity le, int dst) implements Comparable<TargetLe> {
    @Override
    public int compareTo(final @NotNull TargetLe tle) {
        return tle.dst - dst;
    }

    @Override
    public boolean equals(final Object o) {
        return o instanceof TargetLe &&
            ((TargetLe) o).le.getEntityId() == le.getEntityId();
    }

    @Override
    public int hashCode() {
        return le.getEntityId();
    }

    @Override
    public String toString() {
        return le.getName() + ", " + le.getLocation().toString();
    }
}
