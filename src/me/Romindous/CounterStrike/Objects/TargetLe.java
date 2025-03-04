package me.Romindous.CounterStrike.Objects;

import org.bukkit.entity.LivingEntity;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

public record TargetLe(LivingEntity le, int dst, BoundingBox box) implements Comparable<TargetLe> {

    public TargetLe(final LivingEntity le, final int dst) {
        this(le, dst, le.getBoundingBox());
    }

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
