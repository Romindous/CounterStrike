package me.romindous.cs.Objects;

import javax.annotation.Nullable;
import me.romindous.cs.Game.Arena;
import net.kyori.adventure.key.Key;
import org.bukkit.block.Block;
import ru.komiss77.modules.world.BVec;

public abstract class Defusable extends BVec {

    public static final String OFF_CLR = "off", PLANT = "plant", KIT_MDL = PLANT + "/defuse",
        PLIERS_MDL = PLANT + "/pliers", BOMB_MDL = PLANT + "/bomb", WIRE = "wire", DISP = "disp";
    public static final String[] COLORS = {"blue", "green", "red", "yellow"};

    public static Key disp(final String clr) {
        return Key.key(PLANT + "/" + DISP + "/" + clr);
    }

    public static Key wire(final String clr) {
        return Key.key(PLANT + "/" + WIRE + "/" + clr);
    }

    public Defusable(final Block b) {
        this(BVec.of(b));
    }
    public Defusable(final BVec loc) {
        super(loc.x, loc.y, loc.z);
    }

    public abstract Arena arena();

    public abstract @Nullable Shooter defusing();

    public abstract void defusing(final @Nullable Shooter sh);
}
